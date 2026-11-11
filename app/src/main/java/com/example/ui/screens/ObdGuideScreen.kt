package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.data.local.InspectionEntity
import com.example.data.model.AlgerianCarCatalog
import com.example.domain.BluetoothElm327Transport
import com.example.domain.EcuVinComparisonChoice
import com.example.domain.ObdDiagnosticEngine
import com.example.domain.ObdScanResult
import com.example.domain.ObdSimulatorProfile
import com.example.domain.PairedBluetoothDevice
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusErrorRedContainer
import com.example.ui.theme.StatusErrorRedOnContainer
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.StatusWarningAmberContainer
import com.example.ui.theme.StatusWarningAmberOnContainer
import kotlinx.coroutines.launch

@Composable
fun ObdGuideScreen(
    language: AppLanguage,
    activeInspection: InspectionEntity? = null,
    initialScanResult: ObdScanResult? = null,
    isDeveloperModeEnabled: Boolean = false,
    onSaveScanToInspection: (ObdScanResult, Boolean, EcuVinComparisonChoice) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1 = Real Bluetooth ELM327 (default), 2 = Guide & Port Locations, 0 = Simulator (Developer Mode only)
    var selectedSubTab by remember(isDeveloperModeEnabled) {
        mutableIntStateOf(1)
    }
    var selectedProfile by remember { mutableStateOf(ObdSimulatorProfile.HEALTHY) }

    val expectedVin = activeInspection?.vin?.trim() ?: ""

    var currentScanResult by remember(initialScanResult) {
        mutableStateOf(initialScanResult?.takeIf { !it.isSimulated })
    }

    var milBulbLitOnDashboard by remember { mutableStateOf(true) }
    var ecuVinComparison by remember(currentScanResult?.ecuVin) {
        mutableStateOf(EcuVinComparisonChoice.COULD_NOT_COMPARE)
    }

    // Real Bluetooth states
    var hasBtPermission by remember {
        mutableStateOf(BluetoothElm327Transport.hasBluetoothPermissions(context))
    }
    var pairedDevices by remember {
        mutableStateOf(
            if (hasBtPermission) BluetoothElm327Transport.getPairedDevices(context) else emptyList()
        )
    }
    var isScanningBt by remember { mutableStateOf(false) }
    var btStatusMessage by remember { mutableStateOf<String?>(null) }

    val btPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants.values.any { it } || BluetoothElm327Transport.hasBluetoothPermissions(context)
        hasBtPermission = granted
        if (granted) {
            pairedDevices = BluetoothElm327Transport.getPairedDevices(context)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Card + Active Car OBD Port Hint
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald800)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Emerald600),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Strings.obdGuideTitle(language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "فحص كمبيوتر السيارة وكشف مسح الأعطال الاحتيالي قبل البيع"
                                    AppLanguage.FR -> "Lecture calculateur, codes défauts & détection de remise à zéro récente"
                                    AppLanguage.EN -> "ECU fault reader & recently-cleared memory fraud detector"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald100
                            )
                        }
                    }

                    if (activeInspection != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val matchedCar = remember(activeInspection.carBrand, activeInspection.carModel) {
                            AlgerianCarCatalog.models.firstOrNull { car ->
                                car.brand.equals(activeInspection.carBrand, ignoreCase = true) &&
                                    car.model.equals(activeInspection.carModel, ignoreCase = true)
                            }
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Emerald900
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = Emerald200,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${activeInspection.carBrand} ${activeInspection.carModel} (${activeInspection.carYear})",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                if (matchedCar != null) {
                                    val portHint = when (language) {
                                        AppLanguage.AR -> matchedCar.obdPortHintAr
                                        AppLanguage.FR -> matchedCar.obdPortHintFr
                                        AppLanguage.EN -> matchedCar.obdPortHintEn
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📍 OBD2: $portHint",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Emerald100
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Mode Segmented Tabs (Real Bluetooth / Connection Guide; Simulator only in Developer Mode)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ObdModeTabButton(
                    title = when (language) {
                        AppLanguage.AR -> "بلوتوث ELM327"
                        AppLanguage.FR -> "Bluetooth ELM327"
                        AppLanguage.EN -> "Bluetooth ELM327"
                    },
                    selected = selectedSubTab == 1,
                    testTag = "obd_tab_bluetooth",
                    onClick = { selectedSubTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                ObdModeTabButton(
                    title = when (language) {
                        AppLanguage.AR -> "دليل وأماكن المنفذ"
                        AppLanguage.FR -> "Guide & Prises"
                        AppLanguage.EN -> "Guide & Ports"
                    },
                    selected = selectedSubTab == 2,
                    testTag = "obd_tab_guide",
                    onClick = { selectedSubTab = 2 },
                    modifier = Modifier.weight(1f)
                )
                if (isDeveloperModeEnabled) {
                    ObdModeTabButton(
                        title = when (language) {
                            AppLanguage.AR -> "[DEV] محاكي"
                            AppLanguage.FR -> "[DEV] Simulateur"
                            AppLanguage.EN -> "[DEV] Simulator"
                        },
                        selected = selectedSubTab == 0,
                        testTag = "obd_tab_simulator",
                        onClick = { selectedSubTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Content per Sub-Tab
        when (if (!isDeveloperModeEnabled && selectedSubTab == 0) 1 else selectedSubTab) {
            0 -> {
                // Simulator 4-Profile Selector Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = Emerald700,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "محاكي OBD2 المدمج — جرّب الحالات الأربع بدون جهاز:"
                                        AppLanguage.FR -> "Simulateur OBD2 intégré — Testez les 4 scénarios sans boîtier :"
                                        AppLanguage.EN -> "Built-in OBD2 Simulator — Test all 4 car scenarios without hardware:"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            ObdSimulatorProfile.entries.forEach { profile ->
                                val isSelected = selectedProfile == profile
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedProfile = profile
                                            currentScanResult = ObdDiagnosticEngine.generateSimulatedScan(
                                                profile = profile,
                                                expectedVin = expectedVin
                                            )
                                        }
                                        .testTag("sim_profile_${profile.name.lowercase()}"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Emerald50 else Slate50,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Emerald700 else Slate200
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val dotColor = when (profile) {
                                            ObdSimulatorProfile.HEALTHY -> StatusPassGreen
                                            ObdSimulatorProfile.STORED_FAULTS -> StatusErrorRed
                                            ObdSimulatorProfile.RECENTLY_CLEARED_FRAUD -> StatusWarningAmber
                                            ObdSimulatorProfile.NO_ECU_RESPONSE -> Slate600
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = profile.getTitle(language),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                            Text(
                                                text = profile.getSubtitle(language),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate600,
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Render Simulated Scan Result (Developer Mode only — never saveable to an inspection or PDF)
                item {
                    val simScan = ObdDiagnosticEngine.generateSimulatedScan(
                        profile = selectedProfile,
                        expectedVin = expectedVin
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = StatusWarningAmberContainer,
                        border = BorderStroke(1.dp, StatusWarningAmber)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "وضع المطورين فقط: هذه بيانات محاكاة للتجربة ولا يمكن حفظها في أي فحص حقيقي أو ملف PDF."
                                AppLanguage.FR -> "Mode Développeur uniquement : résultat simulé impossible à enregistrer dans une inspection ou un PDF."
                                AppLanguage.EN -> "Developer Mode only: simulated result cannot be saved into any inspection or PDF."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusWarningAmberOnContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    ObdScanResultSection(
                        scanResult = simScan,
                        language = language,
                        expectedVin = expectedVin,
                        milBulbLitOnDashboard = milBulbLitOnDashboard,
                        onMilBulbToggle = { milBulbLitOnDashboard = it },
                        hasActiveInspection = false,
                        onSaveToInspection = {}
                    )
                }
            }

            1 -> {
                // Real Bluetooth Classic ELM327 Connection Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = Emerald700,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "الاتصال بجهاز ELM327 Bluetooth Classic (SPP)"
                                        AppLanguage.FR -> "Connexion au boîtier ELM327 Bluetooth Classic (SPP)"
                                        AppLanguage.EN -> "Connect to ELM327 Bluetooth Classic (SPP) Dongle"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "يستخدم التطبيق صلاحية البلوتوث فقط للاتصال بجهاز الفحص ELM327 وقراءة بيانات المحرك بأمان تام (قراءة فقط دون أي تعديل على السيارة)."
                                    AppLanguage.FR -> "Fahs utilise le Bluetooth uniquement pour communiquer avec votre boîtier ELM327 en lecture seule (aucune écriture ni modification du calculateur)."
                                    AppLanguage.EN -> "Fahs uses Bluetooth solely to communicate with your ELM327 adapter in 100% safe read-only mode (never writes to the car)."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!hasBtPermission) {
                                Button(
                                    onClick = {
                                        btPermissionLauncher.launch(
                                            BluetoothElm327Transport.requiredBluetoothPermissions()
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("grant_bt_permission_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                                ) {
                                    Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "السماح بالوصول إلى البلوتوث وعرض الأجهزة المقترنة"
                                            AppLanguage.FR -> "Autoriser le Bluetooth et afficher les boîtiers appairés"
                                            AppLanguage.EN -> "Allow Bluetooth & List Paired ELM327 Adapters"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "الأجهزة المقترنة بالهاتف (${pairedDevices.size})"
                                            AppLanguage.FR -> "Appareils Bluetooth appairés (${pairedDevices.size})"
                                            AppLanguage.EN -> "Paired Bluetooth Devices (${pairedDevices.size})"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )

                                    OutlinedButton(
                                        onClick = {
                                            pairedDevices = BluetoothElm327Transport.getPairedDevices(context)
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when (language) {
                                                AppLanguage.AR -> "تحديث"
                                                AppLanguage.FR -> "Actualiser"
                                                AppLanguage.EN -> "Refresh"
                                            },
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (pairedDevices.isEmpty()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = StatusWarningAmberContainer
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.AR -> "لم يتم العثور على جهاز مقترن. يرجى توصيل جهاز ELM327 بمنفذ السيارة، فتح Contact، والاقتران بـ OBDII في إعدادات بلوتوث الهاتف (الرمز 1234 أو 0000)."
                                                    AppLanguage.FR -> "Aucun boîtier appairé détecté. Branchez l'ELM327, mettez le contact et appairez 'OBDII' dans les paramètres Bluetooth du téléphone (code 1234 ou 0000)."
                                                    AppLanguage.EN -> "No paired adapter found. Plug in the ELM327, turn ignition ON, and pair 'OBDII' in your phone's Bluetooth settings (PIN 1234 or 0000)."
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = StatusWarningAmberOnContainer
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = {
                                                    BluetoothElm327Transport.openAndroidBluetoothSettings(context)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("open_android_bt_settings_btn"),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bluetooth,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = when (language) {
                                                        AppLanguage.AR -> "فتح إعدادات البلوتوث في الهاتف للاقتران"
                                                        AppLanguage.FR -> "Ouvrir les paramètres Bluetooth Android"
                                                        AppLanguage.EN -> "Open Android Bluetooth Settings to Pair"
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    pairedDevices.forEach { dev: PairedBluetoothDevice ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (dev.isLikelyObdAdapter) Emerald50 else Slate50,
                                            border = BorderStroke(
                                                1.dp,
                                                if (dev.isLikelyObdAdapter) Emerald600 else Slate200
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = dev.name,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Slate900
                                                    )
                                                    Text(
                                                        text = dev.address,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Slate600
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        isScanningBt = true
                                                        scope.launch {
                                                            btStatusMessage = when (language) {
                                                                AppLanguage.AR -> "جاري الاتصال عبر البلوتوث بـ ${dev.name}..."
                                                                AppLanguage.FR -> "Connexion Bluetooth RFCOMM à ${dev.name}..."
                                                                AppLanguage.EN -> "Connecting via Bluetooth RFCOMM to ${dev.name}..."
                                                            }
                                                            val transport = BluetoothElm327Transport(
                                                                context = context,
                                                                deviceAddress = dev.address,
                                                                transportName = dev.name
                                                            )
                                                            val connected = transport.connect()
                                                            if (connected) {
                                                                currentScanResult = ObdDiagnosticEngine.executeRealObdScan(
                                                                    transport = transport,
                                                                    expectedVin = expectedVin,
                                                                    language = language,
                                                                    onProgress = { step -> btStatusMessage = step }
                                                                )
                                                                transport.disconnect()
                                                                btStatusMessage = when (language) {
                                                                    AppLanguage.AR -> "اكتمل الفحص عبر البلوتوث بنجاح ✓"
                                                                    AppLanguage.FR -> "Scan Bluetooth terminé avec succès ✓"
                                                                    AppLanguage.EN -> "Bluetooth scan completed ✓"
                                                                }
                                                            } else {
                                                                btStatusMessage = when (language) {
                                                                    AppLanguage.AR -> "تعذر الاتصال بـ ${dev.name}. تأكد من وضع المفتاح في Contact وأن الجهاز مضاء."
                                                                    AppLanguage.FR -> "Impossible de joindre ${dev.name}. Vérifiez que le contact est mis et le boîtier branché."
                                                                    AppLanguage.EN -> "Could not connect to ${dev.name}. Ensure ignition is ON and the dongle is plugged in."
                                                                }
                                                            }
                                                            isScanningBt = false
                                                        }
                                                    },
                                                    enabled = !isScanningBt,
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                                                ) {
                                                    Text(
                                                        text = when (language) {
                                                            AppLanguage.AR -> "فحص الآن"
                                                            AppLanguage.FR -> "Scanner"
                                                            AppLanguage.EN -> "Scan"
                                                        },
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = {
                                            BluetoothElm327Transport.openAndroidBluetoothSettings(context)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bluetooth,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (language) {
                                                AppLanguage.AR -> "الجهاز غير ظاهر؟ افتح إعدادات البلوتوث للاقتران"
                                                AppLanguage.FR -> "Boîtier absent ? Ouvrir les paramètres Bluetooth"
                                                AppLanguage.EN -> "Dongle not listed? Open Bluetooth Settings"
                                            },
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                if (isScanningBt || btStatusMessage != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Emerald50
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isScanningBt) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = Emerald700
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                            }
                                            Text(
                                                text = btStatusMessage ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Emerald900
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Show real scan result below only after an actual Bluetooth scan has run
                val realScan = currentScanResult?.takeIf { !it.isSimulated }
                if (realScan != null) {
                    item {
                        ObdScanResultSection(
                            scanResult = realScan,
                            language = language,
                            expectedVin = expectedVin,
                            ecuVinComparison = ecuVinComparison,
                            onEcuVinComparisonChange = { ecuVinComparison = it },
                            milBulbLitOnDashboard = milBulbLitOnDashboard,
                            onMilBulbToggle = { milBulbLitOnDashboard = it },
                            hasActiveInspection = activeInspection != null,
                            onSaveToInspection = {
                                onSaveScanToInspection(realScan, milBulbLitOnDashboard, ecuVinComparison)
                            },
                            isDeveloperModeEnabled = isDeveloperModeEnabled,
                            onExportRawLog = {
                                val carCtx = if (activeInspection != null) {
                                    "${activeInspection.carBrand} ${activeInspection.carModel} (${activeInspection.carYear}) [${realScan.detectedProtocol}]"
                                } else {
                                    realScan.detectedProtocol
                                }
                                val savedFile = BluetoothElm327Transport.saveRawExchangeLogToFile(
                                    context = context,
                                    exchanges = realScan.rawExchangeLog,
                                    adapterName = realScan.adapterVersion,
                                    carLabel = carCtx
                                )
                                if (savedFile != null) {
                                    BluetoothElm327Transport.shareRawExchangeLogFile(context, savedFile)
                                    btStatusMessage = when (language) {
                                        AppLanguage.AR -> "تم حفظ ملف السجل الخام: ${savedFile.name}"
                                        AppLanguage.FR -> "Journal brut enregistré : ${savedFile.name}"
                                        AppLanguage.EN -> "Raw exchange log saved: ${savedFile.name}"
                                    }
                                }
                            }
                        )
                    }
                }
            }

            2 -> {
                // Guide & Port Locations Sub-Tab
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "خطوات التوصيل البسيطة"
                                    AppLanguage.FR -> "Étapes simples de connexion"
                                    AppLanguage.EN -> "Simple Connection Steps"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            StepRowItem(
                                stepNumber = "1",
                                title = when (language) {
                                    AppLanguage.AR -> "ابحث عن منفذ OBD2 في السيارة"
                                    AppLanguage.FR -> "Localisez la prise OBD2"
                                    AppLanguage.EN -> "Locate the OBD2 port"
                                },
                                desc = when (language) {
                                    AppLanguage.AR -> "منفذ ذو 16 فتحة، يوجد عادة تحت المقود أو داخل صندوق القفازات (Boîte à gants)."
                                    AppLanguage.FR -> "Prise 16 broches trapézoïdale, sous le volant ou dans la boîte à gants."
                                    AppLanguage.EN -> "16-pin trapezoidal connector under the steering wheel or glovebox."
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            StepRowItem(
                                stepNumber = "2",
                                title = when (language) {
                                    AppLanguage.AR -> "أدر المفتاح إلى وضع التشغيل (Contact On)"
                                    AppLanguage.FR -> "Mettez le contact (Contact On)"
                                    AppLanguage.EN -> "Turn the ignition ON"
                                },
                                desc = when (language) {
                                    AppLanguage.AR -> "أدر مفتاح السيارة لتضيء لمبات الطابلوه دون تشغيل المحرك بعد."
                                    AppLanguage.FR -> "Allumez les voyants du tableau de bord sans démarrer le moteur."
                                    AppLanguage.EN -> "Turn the key so the dashboard lights up without starting the engine."
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            StepRowItem(
                                stepNumber = "3",
                                title = when (language) {
                                    AppLanguage.AR -> "اقترن بالبلوتوث برمز 1234 أو 0000"
                                    AppLanguage.FR -> "Appairez en Bluetooth (code 1234 ou 0000)"
                                    AppLanguage.EN -> "Pair Bluetooth with PIN 1234 or 0000"
                                },
                                desc = when (language) {
                                    AppLanguage.AR -> "في إعدادات البلوتوث بهاتفك، اختر OBDII أو Scanner وأدخل الرمز 1234."
                                    AppLanguage.FR -> "Dans les paramètres Bluetooth, sélectionnez OBDII et saisissez le code 1234."
                                    AppLanguage.EN -> "In your phone's Bluetooth settings, select OBDII and enter PIN 1234."
                                }
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Emerald700,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "أماكن المنفذ في أشهر السيارات بالجزائر"
                                        AppLanguage.FR -> "Emplacement de la prise sur les modèles populaires"
                                        AppLanguage.EN -> "Port locations in popular Algerian cars"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            PortLocationItem(
                                model = "Renault Symbol / Clio 4",
                                location = when (language) {
                                    AppLanguage.AR -> "في أسفل الكونسول الأوسط أمام عصا القير، أو تحت غطاء الفوزيبل."
                                    AppLanguage.FR -> "Au bas de la console centrale devant le levier, ou boîte à fusibles."
                                    AppLanguage.EN -> "Base of center console in front of gear lever, or behind fuse box."
                                }
                            )

                            PortLocationItem(
                                model = "Peugeot 208 / 301 / Citroën C-Elysée",
                                location = when (language) {
                                    AppLanguage.AR -> "أسفل الطابلوه على يسار المقود (خلف غطاء الفيوزات)."
                                    AppLanguage.FR -> "Sous le tableau de bord à gauche du volant, derrière le cache fusibles."
                                    AppLanguage.EN -> "Under the dashboard to the left of the steering wheel behind fuse panel."
                                }
                            )

                            PortLocationItem(
                                model = "Dacia Logan / Sandero / Duster",
                                location = when (language) {
                                    AppLanguage.AR -> "داخل صندوق القفازات (Boîte à gants) بالجهة اليمنى."
                                    AppLanguage.FR -> "À l'intérieur de la boîte à gants côté passager."
                                    AppLanguage.EN -> "Inside the glovebox compartment on the passenger side."
                                }
                            )

                            PortLocationItem(
                                model = "Volkswagen Golf 7 / Polo / Seat Ibiza",
                                location = when (language) {
                                    AppLanguage.AR -> "أسفل مقود السائق مباشرة بالجهة اليسرى مفتوح بلون بنفسجي."
                                    AppLanguage.FR -> "Sous le volant à gauche, prise violette visible sans démontage."
                                    AppLanguage.EN -> "Under the steering wheel on the left side, visible purple socket."
                                }
                            )

                            PortLocationItem(
                                model = "Hyundai Accent / Kia Picanto",
                                location = when (language) {
                                    AppLanguage.AR -> "خلف باب علبة الفيوزات الداخلية أسفل يسار المقود."
                                    AppLanguage.FR -> "Derrière la trappe à fusibles sous le volant à gauche."
                                    AppLanguage.EN -> "Behind the interior fuse box door under the steering wheel."
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ObdModeTabButton(
    title: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Emerald800 else Color.White,
        border = BorderStroke(1.dp, if (selected) Emerald800 else Slate200)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Slate700,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StepRowItem(
    stepNumber: String,
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Emerald700),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = Slate600,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun PortLocationItem(
    model: String,
    location: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = model,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Emerald800
        )
        Text(
            text = location,
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
            color = Slate100
        ) {}
    }
}
