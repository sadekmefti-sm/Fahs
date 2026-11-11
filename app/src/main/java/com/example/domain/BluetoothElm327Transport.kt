package com.example.domain

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PairedBluetoothDevice(
    val name: String,
    val address: String,
    val isLikelyObdAdapter: Boolean
)

data class ObdCommandExchange(
    val timestampMs: Long,
    val command: String,
    val durationMs: Long,
    val rawResponse: String
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
 * for ELM327 Bluetooth Classic dongles.
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
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                arrayOf(Manifest.permission.BLUETOOTH)
            }
        }

        fun openAndroidBluetoothSettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }

        fun saveRawExchangeLogToFile(
            context: Context,
            exchanges: List<ObdCommandExchange>,
            adapterName: String,
            carLabel: String
        ): File? {
            return try {
                val dir = File(context.filesDir, "obd_logs")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val dateFmt = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                val humanFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
                val now = Date()
                val file = File(dir, "fahs_obd_raw_${dateFmt.format(now)}.txt")

                val sb = StringBuilder()
                sb.appendLine("=== FAHS OBD2 RAW EXCHANGE LOG (DEVELOPER MODE) ===")
                sb.appendLine("Date: ${humanFmt.format(now)}")
                sb.appendLine("Adapter: $adapterName")
                sb.appendLine("Car Context: ${carLabel.ifBlank { "None selected" }}")
                sb.appendLine("Total Commands: ${exchanges.size}")
                sb.appendLine("===================================================")
                sb.appendLine()

                exchanges.forEachIndexed { index, ex ->
                    val timeStr = humanFmt.format(Date(ex.timestampMs))
                    sb.appendLine("[#${index + 1}] $timeStr (${ex.durationMs} ms)")
                    sb.appendLine("TX > ${ex.command}")
                    sb.appendLine("RX <")
                    sb.appendLine(ex.rawResponse.ifBlank { "(empty)" })
                    sb.appendLine("---------------------------------------------------")
                }

                file.writeText(sb.toString(), Charsets.UTF_8)
                file
            } catch (_: Exception) {
                null
            }
        }

        fun shareRawExchangeLogFile(context: Context, file: File) {
            try {
                val authority = "${context.packageName}.fileprovider"
                val uri = FileProvider.getUriForFile(context, authority, file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Fahs OBD2 Raw Log - ${file.name}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, file.name).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (_: Exception) {
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
                    device.name ?: "Bluetooth Device"
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
                    upper.contains("CBT") ||
                    upper.contains("IOS-VLINK")
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

    override suspend fun sendCommand(rawCommand: String, timeoutMs: Long): String = withContext(Dispatchers.IO) {
        val cleanCmd = rawCommand.trim().uppercase()

        // SAFETY GUARD: Never allow Mode 04 (Clear Trouble Codes) or ECU write commands
        if (cleanCmd.startsWith("04") || cleanCmd.startsWith("2E") || cleanCmd.startsWith("3B")) {
            return@withContext "BLOCKED_READ_ONLY_MODE"
        }

        val out = outputStream ?: return@withContext "ERROR_NOT_CONNECTED"
        val inp = inputStream ?: return@withContext "ERROR_NOT_CONNECTED"

        try {
            // Drain any stale bytes sitting in the input buffer before sending
            while (inp.available() > 0) {
                val discard = ByteArray(128)
                inp.read(discard)
            }

            // Write command terminated by carriage return
            out.write("$cleanCmd\r".toByteArray(Charsets.US_ASCII))
            out.flush()

            // Read until the ELM327 '>' prompt character or timeoutMs expires
            val responseBuilder = StringBuilder()
            val result = withTimeoutOrNull(timeoutMs) {
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
                        delay(35L)
                    }
                }
                responseBuilder.toString()
            } ?: responseBuilder.toString()

            // Preserve line breaks (\n) so multi-frame (0:, 1:) and multi-ECU replies stay distinct
            result
                .replace("\r\n", "\n")
                .replace("\r", "\n")
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
