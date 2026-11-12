package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.core.photo.InspectionPhotoHelper
import com.example.data.local.ChecklistAnswerEntity
import com.example.data.local.InspectionEntity
import com.example.data.model.ChecklistItemDefinition
import com.example.data.model.ChecklistSection
import com.example.data.model.ItemSeverity
import com.example.data.model.VerificationStatus
import com.example.domain.ChecklistEngine
import java.io.File
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusErrorRedContainer
import com.example.ui.theme.StatusErrorRedOnContainer
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusPassGreenContainer
import com.example.ui.theme.StatusPassGreenOnContainer
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.StatusWarningAmberContainer
import com.example.ui.theme.StatusWarningAmberOnContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChecklistWizardScreen(
    inspection: InspectionEntity,
    savedAnswers: List<ChecklistAnswerEntity>,
    language: AppLanguage,
    onSaveAnswer: (
        item: ChecklistItemDefinition,
        status: String,
        note: String,
        photoUri: String?,
        totalAdaptiveItems: Int
    ) -> Unit,
    onExtendToFullInspection: () -> Unit,
    onPauseAndExit: () -> Unit,
    onFinishInspection: (score: Int?, verdict: String, totalRepairDzd: Int) -> Unit,
    onOpenObdScanner: () -> Unit = {},
    onOpenLiveReport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isQuickMode = inspection.inspectionMode.equals("QUICK", ignoreCase = true)

    val adaptiveItems = remember(
        inspection.carBrand,
        inspection.carModel,
        inspection.carGeneration,
        inspection.carYear,
        inspection.odometerKm,
        inspection.fuelType,
        inspection.gearboxType,
        inspection.engineDescription,
        inspection.hasTurbo,
        inspection.isFourWheelDrive,
        inspection.bodyType,
        isQuickMode
    ) {
        ChecklistEngine.buildChecklistForCar(
            brand = inspection.carBrand,
            model = inspection.carModel,
            fuelType = inspection.fuelType,
            gearboxType = inspection.gearboxType,
            isQuickMode = isQuickMode,
            generation = inspection.carGeneration,
            engineDescription = inspection.engineDescription,
            carYear = inspection.carYear,
            odometerKm = inspection.odometerKm,
            hasTurbo = inspection.hasTurbo,
            isFourWheelDrive = inspection.isFourWheelDrive,
            bodyType = inspection.bodyType
        )
    }

    val answersMap = remember(savedAnswers) {
        savedAnswers.associateBy { it.itemId }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var hasInitializedIndex by remember(inspection.id) { mutableStateOf(false) }

    // Resume automatically at the first unanswered question when opening an inspection
    LaunchedEffect(savedAnswers, adaptiveItems) {
        if (!hasInitializedIndex && adaptiveItems.isNotEmpty() && savedAnswers.isNotEmpty()) {
            val firstUnanswered = adaptiveItems.indexOfFirst { item ->
                val ans = answersMap[item.id]
                ans == null
            }
            if (firstUnanswered in adaptiveItems.indices) {
                currentIndex = firstUnanswered
            }
            hasInitializedIndex = true
        }
    }

    val safeIndex = currentIndex.coerceIn(0, (adaptiveItems.size - 1).coerceAtLeast(0))
    val currentItem = adaptiveItems.getOrNull(safeIndex) ?: return
    val currentSaved = answersMap[currentItem.id]

    // Phase 7 (Items 4, 5, 9): Offline TTS & SpeechRecognizer controller
    val voiceController = remember(context) {
        com.example.domain.OfflineVoiceAndTtsController(context)
    }
    androidx.compose.runtime.DisposableEffect(voiceController) {
        onDispose { voiceController.shutdown() }
    }
    val voiceAvailabilityMap by voiceController.voiceAvailability.collectAsState()
    val isListeningVoice by voiceController.isListeningVoice.collectAsState()
    var isReadAloudEnabled by remember {
        mutableStateOf(com.example.core.i18n.LanguagePreferences.isReadAloudEnabled(context))
    }
    val isVoiceAvailableForCurrentLang = voiceAvailabilityMap[language] == true

    // Automatically read each question aloud when Read-Aloud is enabled and voice is available
    LaunchedEffect(currentItem.id, isReadAloudEnabled, isVoiceAvailableForCurrentLang, language) {
        if (isReadAloudEnabled && isVoiceAvailableForCurrentLang) {
            voiceController.speakQuestion(currentItem.getQuestion(language), language)
        }
    }

    var showHowToCheck by remember(currentItem.id) { mutableStateOf(false) }
    var showNoteEditor by remember(currentItem.id) {
        mutableStateOf(!currentSaved?.note.isNullOrBlank())
    }
    var noteText by remember(currentItem.id, currentSaved?.note) {
        mutableStateOf(currentSaved?.note ?: "")
    }
    var photoLabel by remember(currentItem.id, currentSaved?.photoUri) {
        mutableStateOf(
            currentSaved?.photoUri?.takeIf { InspectionPhotoHelper.isRealStoredPhoto(it) }
        )
    }
    var pendingCameraTempFile by remember(currentItem.id) { mutableStateOf<File?>(null) }

    // Zero-permission Android Photo Picker for choosing an image from gallery (copies into app storage)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            // User cancelled the picker — never store a fake file name
            return@rememberLauncherForActivityResult
        }
        val savedInternalPath = InspectionPhotoHelper.copyUriToInternalStorage(
            context = context,
            sourceUri = uri,
            itemId = currentItem.id
        ) ?: return@rememberLauncherForActivityResult

        photoLabel = savedInternalPath
        onSaveAnswer(
            currentItem,
            currentSaved?.status ?: "PROBLEM",
            noteText,
            savedInternalPath,
            adaptiveItems.size
        )
    }

    // Camera capture launcher (copies captured image into app's persistent internal storage)
    val cameraTakePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val tempFile = pendingCameraTempFile
        pendingCameraTempFile = null
        if (!success || tempFile == null) {
            try {
                tempFile?.delete()
            } catch (_: Exception) {}
            return@rememberLauncherForActivityResult
        }
        val savedInternalPath = InspectionPhotoHelper.copyFileToInternalStorage(
            context = context,
            sourceFile = tempFile,
            itemId = currentItem.id
        ) ?: return@rememberLauncherForActivityResult

        photoLabel = savedInternalPath
        onSaveAnswer(
            currentItem,
            currentSaved?.status ?: "PROBLEM",
            noteText,
            savedInternalPath,
            adaptiveItems.size
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (tempFile, uri) = InspectionPhotoHelper.createTempCameraUri(context, currentItem.id)
            pendingCameraTempFile = tempFile
            cameraTakePictureLauncher.launch(uri)
        }
    }

    val liveEvaluation = remember(answersMap, adaptiveItems) {
        val statusMap = answersMap.mapValues { it.value.status }
        ChecklistEngine.evaluateChecklist(statusMap, adaptiveItems)
    }
    val answeredCount = liveEvaluation.answeredCount
    val progressFraction = if (adaptiveItems.isNotEmpty()) {
        (safeIndex + 1).toFloat() / adaptiveItems.size.toFloat()
    } else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // 1. Sticky Progress & Car Summary Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 3.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPauseAndExit,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("wizard_pause_exit_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Pause and Save",
                            tint = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "${inspection.carBrand} ${inspection.carModel}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currentItem.section.getTitle(language),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Emerald700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Emerald50,
                                border = BorderStroke(1.dp, Emerald200)
                            ) {
                                Text(
                                    text = "${inspection.fuelType} • ${inspection.gearboxType}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald800,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Question Counter Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Slate900
                    ) {
                        Text(
                            text = "${safeIndex + 1} / ${adaptiveItems.size}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Progress bar
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    color = Emerald600,
                    trackColor = Slate200
                )

                // Horizontal Section Quick-Jump Bar
                val sectionsInFlow = remember(adaptiveItems) {
                    adaptiveItems.map { it.section }.distinct()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sectionsInFlow.forEach { sec ->
                        val isCurrentSection = sec == currentItem.section
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val firstIdx = adaptiveItems.indexOfFirst { it.section == sec }
                                    if (firstIdx >= 0) currentIndex = firstIdx
                                },
                            color = if (isCurrentSection) Emerald700 else Slate100
                        ) {
                            Text(
                                text = sec.getTitle(language),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isCurrentSection) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrentSection) Color.White else Slate700,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Main Scrollable Single-Question Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Auto-save, OBD2 Scanner Quick Action & Quick->Full Extension Banner (Responsive FlowRow)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate100
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Emerald700,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "حفظ تلقائي ($answeredCount/${adaptiveItems.size})"
                                AppLanguage.FR -> "Sauvegarde auto ($answeredCount/${adaptiveItems.size})"
                                AppLanguage.EN -> "Auto-saved ($answeredCount/${adaptiveItems.size})"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate700,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                val hasObdScanned = answersMap.containsKey("obd_ecu_diagnostic_scan")
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenObdScanner() }
                        .testTag("wizard_open_obd_btn"),
                    color = if (hasObdScanned) StatusPassGreenContainer else Emerald100,
                    border = BorderStroke(1.dp, if (hasObdScanned) StatusPassGreen else Emerald600)
                ) {
                    Text(
                        text = if (hasObdScanned) {
                            when (language) {
                                AppLanguage.AR -> "OBD2 مفحوص ✓"
                                AppLanguage.FR -> "OBD2 Scanné ✓"
                                AppLanguage.EN -> "OBD2 Scanned ✓"
                            }
                        } else {
                            when (language) {
                                AppLanguage.AR -> "⚡ فحص OBD2"
                                AppLanguage.FR -> "⚡ Scanner OBD2"
                                AppLanguage.EN -> "⚡ Scan OBD2"
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (hasObdScanned) StatusPassGreenOnContainer else Emerald900,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val statusMap = answersMap.mapValues { it.value.status }
                            val (score, verdict, totalDzd) = ChecklistEngine.calculateScoreAndVerdict(
                                answers = statusMap,
                                items = adaptiveItems
                            )
                            onFinishInspection(score, verdict, totalDzd)
                        }
                        .testTag("wizard_open_report_btn"),
                    color = Slate900
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "📄 التقرير والـ PDF"
                            AppLanguage.FR -> "📄 Rapport & PDF"
                            AppLanguage.EN -> "📄 Report & PDF"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }

                if (isQuickMode) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onExtendToFullInspection() },
                        color = StatusWarningAmberContainer
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "+ فحص شامل"
                                AppLanguage.FR -> "+ Mode complet"
                                AppLanguage.EN -> "+ Full Check"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusWarningAmberOnContainer,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Phase 7 (Item 5 & Item 9 Beta): Read-Aloud Bar (hidden if phone has no voice for current language) + Beta Voice Answers
            ReadAloudAndBetaVoiceAnswerBar(
                isVoiceAvailableForLanguage = isVoiceAvailableForCurrentLang,
                isReadAloudEnabled = isReadAloudEnabled,
                onToggleReadAloud = { enabled ->
                    isReadAloudEnabled = enabled
                    com.example.core.i18n.LanguagePreferences.setReadAloudEnabled(context, enabled)
                    if (!enabled) {
                        voiceController.stopSpeaking()
                    }
                },
                onReplayQuestionAloud = {
                    voiceController.speakQuestion(currentItem.getQuestion(language), language)
                },
                language = language,
                isListeningVoice = isListeningVoice,
                voiceController = voiceController,
                onConfirmVoiceAnswer = { confirmedStatus ->
                    onSaveAnswer(currentItem, confirmedStatus, noteText, photoLabel, adaptiveItems.size)
                    if (safeIndex < adaptiveItems.size - 1) {
                        currentIndex = findNextIndexSkippingObdAnswered(safeIndex, adaptiveItems, answersMap)
                    }
                }
            )

            // Phase 7 (Item 1): If this item was answered by the OBD scan, show clear badge
            if (isChecklistItemAnsweredByObdScan(currentSaved) && currentSaved != null) {
                AnsweredByObdScanBanner(
                    savedAnswer = currentSaved,
                    language = language
                )
            }

            // Visual Car Zone Diagram + Severity & Repair Cost Card
            CarZoneDiagramCard(
                section = currentItem.section,
                severity = currentItem.severity,
                isKnownWeakPoint = currentItem.isKnownModelWeakPoint,
                verificationStatus = currentItem.verificationStatus,
                minCostDzd = currentItem.minRepairCostDzd,
                maxCostDzd = currentItem.maxRepairCostDzd,
                whereToLook = currentItem.getWhereToLook(language),
                language = language
            )

            // Primary Question Card (Large Outdoor Typography)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, Slate200),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = currentItem.getQuestion(language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // "How do I check this?" Expandable Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showHowToCheck = !showHowToCheck }
                            .testTag("how_to_check_toggle"),
                        color = Emerald50,
                        border = BorderStroke(1.dp, Emerald200)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = Emerald800,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "كيف أفحص هذه النقطة؟ (دليل الميكانيكي)"
                                            AppLanguage.FR -> "Comment vérifier ce point ? (Guide)"
                                            AppLanguage.EN -> "How do I check this? (Mechanic's Guide)"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald900
                                    )
                                }
                                Text(
                                    text = if (showHowToCheck) "▲" else "▼",
                                    color = Emerald800,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            AnimatedVisibility(visible = showHowToCheck) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Text(
                                        text = currentItem.getHowToCheck(language),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Slate800,
                                        lineHeight = 21.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Good vs Bad comparison boxes
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = StatusPassGreenContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = StatusPassGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = currentItem.getGoodResult(language),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = StatusPassGreenOnContainer
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = StatusErrorRedContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = StatusErrorRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = currentItem.getBadResult(language),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = StatusErrorRedOnContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Phase 7 (Item 8 Beta): Tyre Age 4-Digit DOT Code Reader (Photo + Manual Side-by-Side)
            if (currentItem.id == "ext_tires_wear_date") {
                BetaTyreAgeReaderCard(
                    language = language,
                    onConfirmTyreAgeNote = { confirmedDotLine, isOldTyre ->
                        val updatedNote = if (noteText.isBlank()) confirmedDotLine else "$noteText | $confirmedDotLine"
                        noteText = updatedNote
                        showNoteEditor = true
                        val statusToSave = if (isOldTyre) "PROBLEM" else (currentSaved?.status ?: "GOOD")
                        onSaveAnswer(currentItem, statusToSave, updatedNote, photoLabel, adaptiveItems.size)
                    }
                )
            }

            // 3. Three Large Outdoor Answer Buttons (GOOD / PROBLEM / COULD NOT CHECK)
            val currentStatus = currentSaved?.status
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: GOOD (GREEN)
                AnswerOptionButton(
                    label = when (language) {
                        AppLanguage.AR -> "سليم / جيد (Good)"
                        AppLanguage.FR -> "Bon état / RAS"
                        AppLanguage.EN -> "Good / Passed"
                    },
                    subLabel = currentItem.getGoodResult(language),
                    isSelected = currentStatus == "GOOD",
                    activeBgColor = StatusPassGreen,
                    inactiveBgColor = StatusPassGreenContainer.copy(alpha = 0.55f),
                    activeContentColor = Color.White,
                    inactiveContentColor = StatusPassGreenOnContainer,
                    borderColor = StatusPassGreen,
                    icon = Icons.Default.CheckCircle,
                    testTag = "answer_good_btn",
                    onClick = {
                        onSaveAnswer(currentItem, "GOOD", noteText, photoLabel, adaptiveItems.size)
                        if (safeIndex < adaptiveItems.size - 1) {
                            currentIndex = findNextIndexSkippingObdAnswered(safeIndex, adaptiveItems, answersMap)
                        }
                    }
                )

                // Button 2: PROBLEM (RED)
                AnswerOptionButton(
                    label = when (language) {
                        AppLanguage.AR -> "مشكلة / عيب ملاحظ (Problem)"
                        AppLanguage.FR -> "Problème détecté / Défaut"
                        AppLanguage.EN -> "Problem Found / Defect"
                    },
                    subLabel = currentItem.getBadResult(language),
                    isSelected = currentStatus == "PROBLEM",
                    activeBgColor = StatusErrorRed,
                    inactiveBgColor = StatusErrorRedContainer.copy(alpha = 0.55f),
                    activeContentColor = Color.White,
                    inactiveContentColor = StatusErrorRedOnContainer,
                    borderColor = StatusErrorRed,
                    icon = Icons.Default.Cancel,
                    testTag = "answer_problem_btn",
                    onClick = {
                        onSaveAnswer(currentItem, "PROBLEM", noteText, photoLabel, adaptiveItems.size)
                        if (safeIndex < adaptiveItems.size - 1) {
                            currentIndex = findNextIndexSkippingObdAnswered(safeIndex, adaptiveItems, answersMap)
                        }
                    }
                )

                // Button 3: COULD NOT CHECK (GREY)
                AnswerOptionButton(
                    label = when (language) {
                        AppLanguage.AR -> "تعذر الفحص / تخطي"
                        AppLanguage.FR -> "Non vérifié / Impossible à voir"
                        AppLanguage.EN -> "Could Not Check / Skip"
                    },
                    subLabel = when (language) {
                        AppLanguage.AR -> "سيظهر في التقرير كعنصر غير مفحوص للأمانة"
                        AppLanguage.FR -> "Sera mentionné comme 'Non vérifié' dans le rapport final"
                        AppLanguage.EN -> "Will be clearly listed as 'Not Checked' in the report"
                    },
                    isSelected = currentStatus == "UNCHECKED",
                    activeBgColor = Slate700,
                    inactiveBgColor = Slate100,
                    activeContentColor = Color.White,
                    inactiveContentColor = Slate800,
                    borderColor = Slate400,
                    icon = Icons.Default.RemoveCircleOutline,
                    testTag = "answer_unchecked_btn",
                    onClick = {
                        onSaveAnswer(currentItem, "UNCHECKED", noteText, photoLabel, adaptiveItems.size)
                        if (safeIndex < adaptiveItems.size - 1) {
                            currentIndex = findNextIndexSkippingObdAnswered(safeIndex, adaptiveItems, answersMap)
                        }
                    }
                )
            }

            // 4. Photo (Camera + Gallery + Thumbnail) & Note Attachment Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Button A: Take Photo with Camera
                        OutlinedButton(
                            onClick = {
                                val hasCamPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCamPerm) {
                                    val (tempFile, uri) = InspectionPhotoHelper.createTempCameraUri(context, currentItem.id)
                                    pendingCameraTempFile = tempFile
                                    cameraTakePictureLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wizard_take_photo_camera_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "التقاط بالكاميرا"
                                    AppLanguage.FR -> "Prendre photo"
                                    AppLanguage.EN -> "Take Photo"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Button B: Choose from Gallery
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wizard_pick_photo_gallery_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "من المعرض"
                                    AppLanguage.FR -> "Galerie"
                                    AppLanguage.EN -> "Gallery"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Button C: Note
                        OutlinedButton(
                            onClick = { showNoteEditor = !showNoteEditor },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wizard_toggle_note_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "ملاحظة"
                                    AppLanguage.FR -> "Note"
                                    AppLanguage.EN -> "Note"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Real Photo Thumbnail Preview (only shown when a real file exists in internal storage)
                    val thumbnailBitmap = remember(photoLabel) {
                        InspectionPhotoHelper.loadThumbnailBitmap(photoLabel, maxDimensionPx = 320)
                    }
                    if (photoLabel != null && thumbnailBitmap != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Emerald50,
                            border = BorderStroke(1.dp, Emerald200)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = thumbnailBitmap.asImageBitmap(),
                                    contentDescription = "Attached inspection photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Emerald600, RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "تم إرفاق وحفظ الصورة في ذاكرة التطبيق ✓"
                                            AppLanguage.FR -> "Photo enregistrée dans le rapport & PDF ✓"
                                            AppLanguage.EN -> "Photo saved to inspection & PDF ✓"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald900
                                    )
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "ستظهر بحجم مصغر داخل تقرير PDF"
                                            AppLanguage.FR -> "Sera incluse en miniature dans le PDF"
                                            AppLanguage.EN -> "Will be included at reduced size in the PDF"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate600
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val oldPath = photoLabel
                                        photoLabel = null
                                        if (!oldPath.isNullOrBlank()) {
                                            try {
                                                File(oldPath).delete()
                                            } catch (_: Exception) {}
                                        }
                                        onSaveAnswer(
                                            currentItem,
                                            currentSaved?.status ?: "UNCHECKED",
                                            noteText,
                                            null,
                                            adaptiveItems.size
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove photo",
                                        tint = StatusErrorRed
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = showNoteEditor) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            // Phase 7 (Item 4): Voice Note Microphone Dictation Row with confirmation before adding
                            VoiceNoteDictationRow(
                                language = language,
                                isListeningVoice = isListeningVoice,
                                voiceController = voiceController,
                                onConfirmDictatedNote = { spokenText ->
                                    val combined = if (noteText.isBlank()) spokenText else "$noteText $spokenText"
                                    noteText = combined
                                    onSaveAnswer(
                                        currentItem,
                                        currentSaved?.status ?: "UNCHECKED",
                                        combined,
                                        photoLabel,
                                        adaptiveItems.size
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { newNote ->
                                    noteText = newNote
                                    onSaveAnswer(
                                        currentItem,
                                        currentSaved?.status ?: "UNCHECKED",
                                        newNote,
                                        photoLabel,
                                        adaptiveItems.size
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wizard_note_text_field"),
                                placeholder = {
                                    Text(
                                        when (language) {
                                            AppLanguage.AR -> "اكتب أو أملِ ملاحظتك هنا (مثال: خدش في الجناح الأيمن)..."
                                            AppLanguage.FR -> "Notez ou dictez votre observation (ex: aile avant droite repeinte)..."
                                            AppLanguage.EN -> "Type or dictate your note here (e.g. right front fender resprayed)..."
                                        }
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Emerald700,
                                    unfocusedBorderColor = Slate300
                                )
                            )
                        }
                    }
                }
            }
        }

        // 5. Bottom Navigation Bar (Previous / Next / Finish Report)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (safeIndex > 0) {
                            currentIndex = findPreviousIndexSkippingObdAnswered(safeIndex, adaptiveItems, answersMap)
                        }
                    },
                    enabled = safeIndex > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("wizard_prev_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "السابق"
                            AppLanguage.FR -> "Précédent"
                            AppLanguage.EN -> "Previous"
                        },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (safeIndex < adaptiveItems.size - 1) {
                    Button(
                        onClick = {
                            currentIndex = findNextIndexSkippingObdAnswered(safeIndex, adaptiveItems, answersMap)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("wizard_next_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "السؤال التالي"
                                AppLanguage.FR -> "Suivant"
                                AppLanguage.EN -> "Next Item"
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            val statusMap = answersMap.mapValues { it.value.status }
                            val (score, verdict, totalDzd) = ChecklistEngine.calculateScoreAndVerdict(
                                answers = statusMap,
                                items = adaptiveItems
                            )
                            onFinishInspection(score, verdict, totalDzd)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("wizard_finish_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "إنهاء وحساب النتيجة"
                                AppLanguage.FR -> "Terminer & Calculer Score"
                                AppLanguage.EN -> "Finish & Calculate Score"
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CarZoneDiagramCard(
    section: ChecklistSection,
    severity: ItemSeverity,
    isKnownWeakPoint: Boolean,
    verificationStatus: VerificationStatus,
    minCostDzd: Int,
    maxCostDzd: Int,
    whereToLook: String,
    language: AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top-Down Car Schematic Highlighting Active Inspection Zone
            Box(
                modifier = Modifier
                    .size(width = 74.dp, height = 96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate800)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Car outline
                    drawRoundRect(
                        color = Color(0xFF64748B),
                        topLeft = Offset(w * 0.18f, h * 0.08f),
                        size = Size(w * 0.64f, h * 0.84f),
                        cornerRadius = CornerRadius(18f, 18f),
                        style = Stroke(width = 3.5f)
                    )
                    // Highlighted inspection zone
                    val highlightColor = Color(0xFF34D399)
                    when (section) {
                        ChecklistSection.UNDER_BONNET_COLD,
                        ChecklistSection.ENGINE_START,
                        ChecklistSection.AFTER_DRIVE -> {
                            // Front Engine Bay
                            drawRoundRect(
                                color = highlightColor,
                                topLeft = Offset(w * 0.22f, h * 0.10f),
                                size = Size(w * 0.56f, h * 0.24f),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                        }
                        ChecklistSection.INTERIOR,
                        ChecklistSection.DOCUMENTS_IDENTITY,
                        ChecklistSection.BEFORE_VISIT -> {
                            // Cabin Center
                            drawRoundRect(
                                color = highlightColor,
                                topLeft = Offset(w * 0.24f, h * 0.36f),
                                size = Size(w * 0.52f, h * 0.32f),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                        }
                        else -> {
                            // Perimeter / Exterior / Suspension
                            drawRoundRect(
                                color = highlightColor,
                                topLeft = Offset(w * 0.12f, h * 0.04f),
                                size = Size(w * 0.76f, h * 0.92f),
                                cornerRadius = CornerRadius(22f, 22f),
                                style = Stroke(
                                    width = 5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sevColor = when (severity) {
                        ItemSeverity.CRITICAL -> StatusErrorRed
                        ItemSeverity.IMPORTANT -> StatusWarningAmber
                        ItemSeverity.MINOR -> Emerald600
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = sevColor
                    ) {
                        Text(
                            text = severity.getLabel(language),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (isKnownWeakPoint) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Emerald700
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "نقطة ضعف الموديل"
                                    AppLanguage.FR -> "Point faible modèle"
                                    AppLanguage.EN -> "Model Weak Point"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StatusWarningAmberContainer
                        ) {
                            Text(
                                text = verificationStatus.getLabel(language),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarningAmberOnContainer,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Emerald200,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = whereToLook,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                }

                if (maxCostDzd > 0 && verificationStatus == VerificationStatus.VERIFIED_BY_MECHANIC) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "تكلفة الإصلاح التقريبية: $minCostDzd - $maxCostDzd دج"
                            AppLanguage.FR -> "Coût réparation approx. : $minCostDzd - $maxCostDzd DZD"
                            AppLanguage.EN -> "Approx. repair cost: $minCostDzd - $maxCostDzd DZD"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Emerald200
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerOptionButton(
    label: String,
    subLabel: String,
    isSelected: Boolean,
    activeBgColor: Color,
    inactiveBgColor: Color,
    activeContentColor: Color,
    inactiveContentColor: Color,
    borderColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = if (isSelected) activeBgColor else inactiveBgColor,
        border = BorderStroke(if (isSelected) 2.5.dp else 1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) activeContentColor else borderColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) activeContentColor else inactiveContentColor
                )
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) activeContentColor.copy(alpha = 0.9f) else inactiveContentColor.copy(alpha = 0.85f),
                    maxLines = 2
                )
            }
        }
    }
}
