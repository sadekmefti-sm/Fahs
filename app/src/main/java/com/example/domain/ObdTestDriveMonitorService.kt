package com.example.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LanguagePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class TestDriveMonitoringSummary(
    val deviceName: String,
    val durationSeconds: Int,
    val samplesRecorded: Int,
    val maxCoolantTempC: Int?,
    val minCoolantTempC: Int?,
    val idleRpmMin: Int?,
    val idleRpmMax: Int?,
    val idleRpmSpread: Int?,
    val minChargingVoltage: Double?,
    val maxChargingVoltage: Double?,
    val detectedDtcCodes: List<String>,
    val hasOverheatingWarning: Boolean,
    val hasUnstableIdleWarning: Boolean,
    val hasWeakChargingWarning: Boolean,
    val hasFaultCodesDuringDrive: Boolean
) {
    val hasAnyIssue: Boolean
        get() = hasOverheatingWarning ||
            hasUnstableIdleWarning ||
            hasWeakChargingWarning ||
            hasFaultCodesDuringDrive

    val hasAnyProblem: Boolean get() = hasAnyIssue
    val overheatingDetected: Boolean get() = hasOverheatingWarning
    val unstableIdleDetected: Boolean get() = hasUnstableIdleWarning
    val weakChargingDetected: Boolean get() = hasWeakChargingWarning
    val minIdleRpm: Int? get() = idleRpmMin
    val maxIdleRpm: Int? get() = idleRpmMax
    val minVoltage: Double? get() = minChargingVoltage
    val maxVoltage: Double? get() = maxChargingVoltage
    val minVoltageV: Double? get() = minChargingVoltage
    val faultCodesAppeared: List<String> get() = detectedDtcCodes
    val faultCodesFoundDuringDrive: List<String> get() = detectedDtcCodes

    fun getSummaryLine(language: AppLanguage): String =
        formatFindingsLines(language).joinToString("\n")

    fun formatDurationText(language: AppLanguage): String {
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        return when (language) {
            AppLanguage.AR -> "${mins} د ${secs} ث ($samplesRecorded قراءة)"
            AppLanguage.FR -> "${mins} min ${secs} s ($samplesRecorded lectures)"
            AppLanguage.EN -> "${mins}m ${secs}s ($samplesRecorded readings)"
        }
    }

    fun formatFindingsLines(language: AppLanguage): List<String> {
        val na = ObdDiagnosticEngine.notAvailableLabel(language)
        val tempText = if (maxCoolantTempC != null) {
            val status = if (hasOverheatingWarning) {
                when (language) {
                    AppLanguage.AR -> "تحذير: ارتفاع حرارة ($maxCoolantTempC °C)"
                    AppLanguage.FR -> "ALERTE : Surchauffe ($maxCoolantTempC °C)"
                    AppLanguage.EN -> "ALERT: Overheating ($maxCoolantTempC °C)"
                }
            } else {
                when (language) {
                    AppLanguage.AR -> "طبيعية (الأقصى $maxCoolantTempC °C)"
                    AppLanguage.FR -> "Normale (Max $maxCoolantTempC °C)"
                    AppLanguage.EN -> "Normal (Max $maxCoolantTempC °C)"
                }
            }
            status
        } else na

        val idleText = if (idleRpmSpread != null && idleRpmMin != null && idleRpmMax != null) {
            if (hasUnstableIdleWarning) {
                when (language) {
                    AppLanguage.AR -> "غير مستقر ($idleRpmMin–$idleRpmMax د/د، تذبذب $idleRpmSpread)"
                    AppLanguage.FR -> "Instable ($idleRpmMin–$idleRpmMax tr/min, écart $idleRpmSpread)"
                    AppLanguage.EN -> "Unstable ($idleRpmMin–$idleRpmMax rpm, spread $idleRpmSpread)"
                }
            } else {
                when (language) {
                    AppLanguage.AR -> "مستقر ($idleRpmMin–$idleRpmMax د/د)"
                    AppLanguage.FR -> "Stable ($idleRpmMin–$idleRpmMax tr/min)"
                    AppLanguage.EN -> "Stable ($idleRpmMin–$idleRpmMax rpm)"
                }
            }
        } else na

        val voltText = if (minChargingVoltage != null && maxChargingVoltage != null) {
            val rangeStr = String.format(Locale.US, "%.1f–%.1f V", minChargingVoltage, maxChargingVoltage)
            if (hasWeakChargingWarning) {
                when (language) {
                    AppLanguage.AR -> "شحن ضعيف أو غير منتظم ($rangeStr)"
                    AppLanguage.FR -> "Charge faible ou irrégulière ($rangeStr)"
                    AppLanguage.EN -> "Weak or irregular charging ($rangeStr)"
                }
            } else {
                when (language) {
                    AppLanguage.AR -> "جيد ($rangeStr)"
                    AppLanguage.FR -> "Correct ($rangeStr)"
                    AppLanguage.EN -> "Good ($rangeStr)"
                }
            }
        } else na

        val dtcText = if (detectedDtcCodes.isEmpty()) {
            when (language) {
                AppLanguage.AR -> "0 (لم يظهر أي كود عطل أثناء القيادة)"
                AppLanguage.FR -> "0 (Aucun code défaut apparu pendant l'essai)"
                AppLanguage.EN -> "0 (No fault codes appeared during the drive)"
            }
        } else {
            detectedDtcCodes.joinToString(", ")
        }

        return when (language) {
            AppLanguage.AR -> listOf(
                "• حرارة المحرك أثناء القيادة: $tempText",
                "• استقرار سرعة الخمول (Ralenti): $idleText",
                "• شحن الدينامو والبطارية: $voltText",
                "• أكواد الأعطال التي ظهرت أثناء التجربة: $dtcText"
            )
            AppLanguage.FR -> listOf(
                "• Température moteur pendant l'essai : $tempText",
                "• Stabilité du ralenti : $idleText",
                "• Charge alternateur & batterie : $voltText",
                "• Codes défauts apparus pendant l'essai : $dtcText"
            )
            AppLanguage.EN -> listOf(
                "• Engine temperature during drive: $tempText",
                "• Idle speed stability: $idleText",
                "• Alternator & battery charging: $voltText",
                "• Fault codes appeared during drive: $dtcText"
            )
        }
    }
}

data class TestDriveLiveState(
    val isRunning: Boolean = false,
    val deviceName: String = "",
    val elapsedSeconds: Int = 0,
    val samplesRecorded: Int = 0,
    val latestCoolantC: Int? = null,
    val maxCoolantC: Int? = null,
    val latestRpm: Int? = null,
    val latestVoltage: Double? = null,
    val minVoltage: Double? = null,
    val detectedDtcCodes: List<String> = emptyList(),
    val statusMessage: String = "",
    val completedSummaryToConfirm: TestDriveMonitoringSummary? = null
)

/**
 * Foreground service that records live OBD2 telemetry (coolant temperature, idle stability,
 * charging voltage, and stored/pending fault codes) during a road test while the phone screen is off.
 */
class ObdTestDriveMonitorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitorJob: Job? = null
    private var activeTransport: BluetoothElm327Transport? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_MONITOR
        if (action == ACTION_STOP_MONITOR) {
            stopMonitoringAndSummarize()
            return START_NOT_STICKY
        }

        val deviceAddress = intent?.getStringExtra(EXTRA_DEVICE_ADDRESS).orEmpty()
        val deviceName = intent?.getStringExtra(EXTRA_DEVICE_NAME).orEmpty().ifBlank { "ELM327 OBD2" }
        if (deviceAddress.isBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val lang = LanguagePreferences.getInitialLanguage(applicationContext)
        createNotificationChannel()
        val initialNotification = buildNotification(
            language = lang,
            deviceName = deviceName,
            elapsedSec = 0,
            coolantC = null,
            voltage = null
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                } else {
                    0
                }
                ServiceCompat.startForeground(this, NOTIFICATION_ID, initialNotification, fgsType)
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } catch (_: Exception) {
            // In unit tests or restricted contexts, continue gracefully
        }

        startPollingLoop(deviceAddress, deviceName, lang)
        return START_NOT_STICKY
    }

    private fun startPollingLoop(
        deviceAddress: String,
        deviceName: String,
        language: AppLanguage
    ) {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            val startTimeMs = System.currentTimeMillis()
            val coolantSamples = mutableListOf<Int>()
            val idleRpmSamples = mutableListOf<Int>()
            val voltageSamples = mutableListOf<Double>()
            val dtcSet = linkedSetOf<String>()

            updateLiveState(
                TestDriveLiveState(
                    isRunning = true,
                    deviceName = deviceName,
                    statusMessage = when (language) {
                        AppLanguage.AR -> "جاري الاتصال بـ $deviceName لمراقبة القيادة التجريبية..."
                        AppLanguage.FR -> "Connexion à $deviceName pour le suivi de l'essai routier..."
                        AppLanguage.EN -> "Connecting to $deviceName for test-drive monitoring..."
                    },
                    completedSummaryToConfirm = null
                )
            )

            val transport = BluetoothElm327Transport(
                context = applicationContext,
                deviceAddress = deviceAddress,
                transportName = deviceName
            )
            activeTransport = transport

            val connected = transport.connect()
            if (!connected) {
                updateLiveState(
                    TestDriveLiveState(
                        isRunning = false,
                        deviceName = deviceName,
                        statusMessage = when (language) {
                            AppLanguage.AR -> "تعذر الاتصال بـ $deviceName لبدء مراقبة القيادة التجريبية."
                            AppLanguage.FR -> "Impossible de se connecter à $deviceName pour l'essai routier."
                            AppLanguage.EN -> "Could not connect to $deviceName for test-drive monitoring."
                        }
                    )
                )
                stopForegroundCompat()
                stopSelf()
                return@launch
            }

            // Initialize safe read-only ELM327 mode
            transport.sendCommand("ATZ", timeoutMs = 2500L)
            transport.sendCommand("ATE0", timeoutMs = 1500L)
            transport.sendCommand("ATL0", timeoutMs = 1500L)
            transport.sendCommand("ATS0", timeoutMs = 1500L)
            transport.sendCommand("ATH0", timeoutMs = 1500L)
            transport.sendCommand("ATSP0", timeoutMs = 2000L)

            var sampleCounter = 0
            while (isActive && _liveState.value.isRunning) {
                val elapsedSec = ((System.currentTimeMillis() - startTimeMs) / 1000L).toInt()

                // 1. Coolant Temperature (0105)
                val rawCoolant = transport.sendCommand("0105", timeoutMs = 2000L)
                val coolantC = ObdDiagnosticEngine.parseCoolantTempC(rawCoolant)
                if (coolantC != null && coolantC in -20..150) {
                    coolantSamples.add(coolantC)
                }

                // 2. Engine RPM (010C)
                val rawRpm = transport.sendCommand("010C", timeoutMs = 2000L)
                val rpm = ObdDiagnosticEngine.parseRpm(rawRpm)
                if (rpm != null && rpm in 480..1050) {
                    // Record idle-band RPM samples when the car is stopped at traffic lights / idling
                    idleRpmSamples.add(rpm)
                }

                // 3. Battery & Alternator Voltage (ATRV)
                val rawVolt = transport.sendCommand("ATRV", timeoutMs = 1800L)
                val volt = ObdDiagnosticEngine.parseBatteryVoltage(rawVolt)
                if (volt != null && volt in 8.0..18.5) {
                    voltageSamples.add(volt)
                }

                // 4. Every 3rd cycle, check stored (03) and pending (07) DTCs
                if (sampleCounter % 3 == 0) {
                    val stored = ObdDiagnosticEngine.parseMode03DtcResponse(
                        rawHex = transport.sendCommand("03", timeoutMs = 2500L),
                        dtcType = DtcType.STORED
                    )
                    val pending = ObdDiagnosticEngine.parseMode03DtcResponse(
                        rawHex = transport.sendCommand("07", timeoutMs = 2500L),
                        dtcType = DtcType.PENDING
                    )
                    (stored + pending).forEach { dtcSet.add(it.code) }
                }

                sampleCounter += 1

                val maxCoolant = coolantSamples.maxOrNull()
                val minVolt = voltageSamples.minOrNull()
                val currentLang = LanguagePreferences.getInitialLanguage(applicationContext)

                updateLiveState(
                    TestDriveLiveState(
                        isRunning = true,
                        deviceName = deviceName,
                        elapsedSeconds = elapsedSec,
                        samplesRecorded = sampleCounter,
                        latestCoolantC = coolantC,
                        maxCoolantC = maxCoolant,
                        latestRpm = rpm,
                        latestVoltage = volt,
                        minVoltage = minVolt,
                        detectedDtcCodes = dtcSet.toList(),
                        statusMessage = when (currentLang) {
                            AppLanguage.AR -> "مراقبة القيادة نشطة (${elapsedSec} ث) — يمكنك إغلاق الشاشة ووضع الهاتف جانباً"
                            AppLanguage.FR -> "Suivi d'essai actif (${elapsedSec} s) — vous pouvez poser le téléphone"
                            AppLanguage.EN -> "Test-drive monitoring active (${elapsedSec}s) — safe to turn screen off"
                        }
                    )
                )

                updateForegroundNotification(
                    language = currentLang,
                    deviceName = deviceName,
                    elapsedSec = elapsedSec,
                    coolantC = maxCoolant ?: coolantC,
                    voltage = volt
                )

                delay(3000L)
            }

            // Build final summary when stopped
            val totalElapsed = ((System.currentTimeMillis() - startTimeMs) / 1000L).toInt().coerceAtLeast(1)
            val summary = buildSummaryFromSamples(
                deviceName = deviceName,
                durationSeconds = totalElapsed,
                samplesRecorded = sampleCounter,
                coolantSamples = coolantSamples,
                idleRpmSamples = idleRpmSamples,
                voltageSamples = voltageSamples,
                detectedDtcCodes = dtcSet.toList()
            )
            val currentLang = LanguagePreferences.getInitialLanguage(applicationContext)
            updateLiveState(
                TestDriveLiveState(
                    isRunning = false,
                    deviceName = deviceName,
                    elapsedSeconds = totalElapsed,
                    samplesRecorded = sampleCounter,
                    maxCoolantC = summary.maxCoolantTempC,
                    minVoltage = summary.minChargingVoltage,
                    detectedDtcCodes = summary.detectedDtcCodes,
                    statusMessage = when (currentLang) {
                        AppLanguage.AR -> "انتهت مراقبة القيادة التجريبية. راجع النتائج المقترحة بالأسفل لتأكيدها."
                        AppLanguage.FR -> "Suivi de l'essai terminé. Vérifiez et confirmez le résumé ci-dessous."
                        AppLanguage.EN -> "Test-drive monitoring complete. Review and confirm the summary below."
                    },
                    completedSummaryToConfirm = summary
                )
            )

            runCatching { transport.disconnect() }
            activeTransport = null
            stopForegroundCompat()
            stopSelf()
        }
    }

    private fun stopMonitoringAndSummarize() {
        updateLiveState(_liveState.value.copy(isRunning = false))
    }

    override fun onDestroy() {
        updateLiveState(_liveState.value.copy(isRunning = false))
        monitorJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun stopForegroundCompat() {
        try {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fahs Test-Drive OBD2 Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors engine temperature, charging voltage, and fault codes during a test drive"
            }
            mgr.createNotificationChannel(channel)
        }
    }

    private fun updateForegroundNotification(
        language: AppLanguage,
        deviceName: String,
        elapsedSec: Int,
        coolantC: Int?,
        voltage: Double?
    ) {
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val notification = buildNotification(language, deviceName, elapsedSec, coolantC, voltage)
        runCatching { mgr.notify(NOTIFICATION_ID, notification) }
    }

    private fun buildNotification(
        language: AppLanguage,
        deviceName: String,
        elapsedSec: Int,
        coolantC: Int?,
        voltage: Double?
    ): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ObdTestDriveMonitorService::class.java).apply {
            action = ACTION_STOP_MONITOR
        }
        val stopPending = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (language) {
            AppLanguage.AR -> "فحص (Fahs): مراقبة القيادة التجريبية نشطة"
            AppLanguage.FR -> "Fahs : Suivi d'essai routier OBD2 en cours"
            AppLanguage.EN -> "Fahs: Test-Drive OBD2 Monitoring Active"
        }
        val tempPart = if (coolantC != null) "$coolantC °C" else "—"
        val voltPart = if (voltage != null) String.format(Locale.US, "%.1f V", voltage) else "—"
        val content = when (language) {
            AppLanguage.AR -> "$deviceName • الحرارة: $tempPart • الشحن: $voltPart (${elapsedSec} ث)"
            AppLanguage.FR -> "$deviceName • Temp: $tempPart • Tension: $voltPart (${elapsedSec} s)"
            AppLanguage.EN -> "$deviceName • Temp: $tempPart • Voltage: $voltPart (${elapsedSec}s)"
        }
        val stopLabel = when (language) {
            AppLanguage.AR -> "إيقاف وعرض النتيجة"
            AppLanguage.FR -> "Arrêter & Voir le bilan"
            AppLanguage.EN -> "Stop & View Summary"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openPending)
            .addAction(0, stopLabel, stopPending)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "fahs_test_drive_obd_channel"
        private const val NOTIFICATION_ID = 7402
        const val ACTION_START_MONITOR = "com.example.action.START_TEST_DRIVE_MONITOR"
        const val ACTION_STOP_MONITOR = "com.example.action.STOP_TEST_DRIVE_MONITOR"
        const val EXTRA_DEVICE_ADDRESS = "extra_device_address"
        const val EXTRA_DEVICE_NAME = "extra_device_name"

        private val _liveState = MutableStateFlow(TestDriveLiveState())
        val liveState: StateFlow<TestDriveLiveState> = _liveState.asStateFlow()

        private val _isMonitoring = MutableStateFlow(false)
        val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

        private val _statusMessage = MutableStateFlow("")
        val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

        private val _samplesCount = MutableStateFlow(0)
        val samplesCount: StateFlow<Int> = _samplesCount.asStateFlow()

        private val _completedSummary = MutableStateFlow<TestDriveMonitoringSummary?>(null)
        val completedSummary: StateFlow<TestDriveMonitoringSummary?> = _completedSummary.asStateFlow()

        private fun updateLiveState(newState: TestDriveLiveState) {
            _liveState.value = newState
            _isMonitoring.value = newState.isRunning
            _statusMessage.value = newState.statusMessage
            _samplesCount.value = newState.samplesRecorded
            _completedSummary.value = newState.completedSummaryToConfirm
        }

        fun startMonitoring(
            context: Context,
            deviceAddress: String,
            deviceName: String,
            @Suppress("UNUSED_PARAMETER") language: AppLanguage = AppLanguage.FR
        ) {
            startService(context, deviceAddress, deviceName)
        }

        fun stopMonitoring(context: Context) {
            stopService(context)
        }

        fun clearCompletedSummary() {
            clearPendingSummary()
        }

        fun startService(context: Context, deviceAddress: String, deviceName: String) {
            val intent = Intent(context, ObdTestDriveMonitorService::class.java).apply {
                action = ACTION_START_MONITOR
                putExtra(EXTRA_DEVICE_ADDRESS, deviceAddress)
                putExtra(EXTRA_DEVICE_NAME, deviceName)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ObdTestDriveMonitorService::class.java).apply {
                action = ACTION_STOP_MONITOR
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {
                updateLiveState(_liveState.value.copy(isRunning = false))
            }
        }

        fun clearPendingSummary() {
            updateLiveState(_liveState.value.copy(completedSummaryToConfirm = null))
        }

        /**
         * Pure helper (also unit-testable) that builds a TestDriveMonitoringSummary from recorded samples.
         */
        fun buildSummaryFromSamples(
            durationSeconds: Int,
            samplesCount: Int,
            coolantTemps: List<Int>,
            idleRpms: List<Int>,
            voltages: List<Double>,
            dtcs: List<String>
        ): TestDriveMonitoringSummary = buildSummaryFromSamples(
            deviceName = "ELM327 OBD2",
            durationSeconds = durationSeconds,
            samplesRecorded = samplesCount,
            coolantSamples = coolantTemps,
            idleRpmSamples = idleRpms,
            voltageSamples = voltages,
            detectedDtcCodes = dtcs
        )

        fun buildSummaryFromSamples(
            deviceName: String,
            durationSeconds: Int,
            samplesRecorded: Int,
            coolantSamples: List<Int>,
            idleRpmSamples: List<Int>,
            voltageSamples: List<Double>,
            detectedDtcCodes: List<String>
        ): TestDriveMonitoringSummary {
            val maxTemp = coolantSamples.maxOrNull()
            val minTemp = coolantSamples.minOrNull()
            val idleMin = idleRpmSamples.minOrNull()
            val idleMax = idleRpmSamples.maxOrNull()
            val idleSpread = if (idleRpmSamples.size >= 2 && idleMin != null && idleMax != null) {
                idleMax - idleMin
            } else null
            val minVolt = voltageSamples.minOrNull()
            val maxVolt = voltageSamples.maxOrNull()

            val overheating = maxTemp != null && maxTemp >= 103
            val unstableIdle = idleSpread != null && idleSpread > 85
            val weakCharging = (minVolt != null && minVolt < 13.2) || (maxVolt != null && maxVolt > 15.0)
            val hasFaults = detectedDtcCodes.isNotEmpty()

            return TestDriveMonitoringSummary(
                deviceName = deviceName,
                durationSeconds = durationSeconds,
                samplesRecorded = samplesRecorded,
                maxCoolantTempC = maxTemp,
                minCoolantTempC = minTemp,
                idleRpmMin = idleMin,
                idleRpmMax = idleMax,
                idleRpmSpread = idleSpread,
                minChargingVoltage = minVolt,
                maxChargingVoltage = maxVolt,
                detectedDtcCodes = detectedDtcCodes,
                hasOverheatingWarning = overheating,
                hasUnstableIdleWarning = unstableIdle,
                hasWeakChargingWarning = weakCharging,
                hasFaultCodesDuringDrive = hasFaults
            )
        }
    }
}
