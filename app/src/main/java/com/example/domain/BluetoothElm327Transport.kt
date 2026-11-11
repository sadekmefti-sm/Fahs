package com.example.domain

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

data class PairedBluetoothDevice(
    val name: String,
    val address: String,
    val isLikelyObdAdapter: Boolean
)

/**
 * Modular interface for future brand-specific secondary ECU mileage readers
 * (e.g. reading odometer from ABS/ESP or Instrument Cluster via UDS/CAN).
 */
interface BrandSecondaryEcuMileageReader {
    val supportedBrands: List<String>
    val protocolDescription: String
    suspend fun readSecondaryOdometerKm(transport: ObdTransport): Int?
}

/**
 * Classic Bluetooth (SPP / RFCOMM) implementation of [ObdTransport]
 * specifically tuned for cheap ELM327 v1.5 and v2.1 clone dongles common in Algeria.
 *
 * Strictly READ-ONLY: blocks any Mode 04 (clear codes) or write commands.
 */
class BluetoothElm327Transport(
    private val context: Context,
    private val deviceAddress: String,
    override val transportName: String = "Bluetooth SPP ($deviceAddress)"
) : ObdTransport {

    companion object {
        // Standard Serial Port Profile (SPP) UUID used by all ELM327 Bluetooth Classic dongles
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        fun hasBluetoothPermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        fun requiredBluetoothPermissions(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            } else {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            }
        }

        @SuppressLint("MissingPermission")
        fun getPairedDevices(context: Context): List<PairedBluetoothDevice> {
            if (!hasBluetoothPermissions(context)) return emptyList()
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter: BluetoothAdapter? = btManager?.adapter
            if (adapter == null || !adapter.isEnabled) return emptyList()

            val bonded: Set<BluetoothDevice> = try {
                adapter.bondedDevices ?: emptySet()
            } catch (_: SecurityException) {
                emptySet()
            }

            return bonded.map { device ->
                val devName = try {
                    device.name ?: "Unknown Bluetooth Device"
                } catch (_: SecurityException) {
                    "Bluetooth Device"
                }
                val upper = devName.uppercase()
                val isObd = upper.contains("OBD") ||
                    upper.contains("ELM") ||
                    upper.contains("V-LINK") ||
                    upper.contains("VGATE") ||
                    upper.contains("KONNWEI") ||
                    upper.contains("SCAN") ||
                    upper.contains("CAR") ||
                    upper.contains("CBT")
                PairedBluetoothDevice(
                    name = devName,
                    address = device.address,
                    isLikelyObdAdapter = isObd
                )
            }.sortedByDescending { it.isLikelyObdAdapter }
        }
    }

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    @SuppressLint("MissingPermission")
    override suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        if (!hasBluetoothPermissions(context)) return@withContext false
        try {
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = btManager?.adapter ?: return@withContext false
            if (!adapter.isEnabled) return@withContext false

            val device = adapter.getRemoteDevice(deviceAddress) ?: return@withContext false
            val rfcommSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            try {
                adapter.cancelDiscovery()
            } catch (_: Exception) {
            }

            rfcommSocket.connect()
            socket = rfcommSocket
            inputStream = rfcommSocket.inputStream
            outputStream = rfcommSocket.outputStream
            true
        } catch (_: Exception) {
            disconnect()
            false
        }
    }

    override suspend fun sendCommand(rawCommand: String): String = withContext(Dispatchers.IO) {
        val cleanCmd = rawCommand.trim().uppercase()

        // SAFETY GUARD: Never allow Mode 04 (Clear Trouble Codes) or ECU write commands
        if (cleanCmd.startsWith("04") || cleanCmd.startsWith("2E") || cleanCmd.startsWith("3B")) {
            return@withContext "BLOCKED_READ_ONLY_MODE"
        }

        val out = outputStream ?: return@withContext "ERROR_NOT_CONNECTED"
        val inp = inputStream ?: return@withContext "ERROR_NOT_CONNECTED"

        try {
            // Write command terminated by carriage return
            out.write("$cleanCmd\r".toByteArray(Charsets.US_ASCII))
            out.flush()

            // Clone-tolerant read loop waiting for '>' prompt character
            val responseBuilder = StringBuilder()
            val result = withTimeoutOrNull(3500L) {
                val buffer = ByteArray(256)
                while (true) {
                    if (inp.available() > 0) {
                        val readCount = inp.read(buffer)
                        if (readCount > 0) {
                            val chunk = String(buffer, 0, readCount, Charsets.US_ASCII)
                            responseBuilder.append(chunk)
                            if (chunk.contains(">")) {
                                break
                            }
                        }
                    } else {
                        delay(40L)
                    }
                }
                responseBuilder.toString()
            } ?: responseBuilder.toString()

            result
                .replace("\r", " ")
                .replace("\n", " ")
                .replace(">", "")
                .trim()
        } catch (_: Exception) {
            "ERROR_IO"
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            inputStream?.close()
        } catch (_: Exception) {
        }
        try {
            outputStream?.close()
        } catch (_: Exception) {
        }
        try {
            socket?.close()
        } catch (_: Exception) {
        }
        inputStream = null
        outputStream = null
        socket = null
    }
}
