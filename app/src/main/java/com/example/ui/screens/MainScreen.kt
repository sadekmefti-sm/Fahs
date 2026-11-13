package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LanguagePreferences
import com.example.core.i18n.Strings
import com.example.ui.components.LanguageSelectorDialog
import com.example.ui.theme.FahsBrandSoft
import com.example.ui.theme.FahsHairline
import com.example.ui.theme.FahsInk
import com.example.ui.theme.FahsMutedText
import com.example.ui.viewmodel.FahsViewModel
import com.example.ui.viewmodel.NavTab
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: FahsViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val preferredQuickMode by viewModel.preferredQuickMode.collectAsStateWithLifecycle()
    val optionalCarDraft by viewModel.optionalCarDraft.collectAsStateWithLifecycle()
    val preselectedBrandFromVin by viewModel.preselectedBrandFromVin.collectAsStateWithLifecycle()
    val carSelectionReturnTab by viewModel.carSelectionReturnTab.collectAsStateWithLifecycle()
    val inspections by viewModel.inspections.collectAsStateWithLifecycle()
    val activeInspection by viewModel.activeInspection.collectAsStateWithLifecycle()
    val activeInspectionAnswers by viewModel.activeInspectionAnswers.collectAsStateWithLifecycle()
    val completedCount by viewModel.completedCount.collectAsStateWithLifecycle()
    val inProgressCount by viewModel.inProgressCount.collectAsStateWithLifecycle()
    val lastObdScanResult by viewModel.lastObdScanResult.collectAsStateWithLifecycle()
    val cloudUserEmail by viewModel.cloudUserEmail.collectAsStateWithLifecycle()
    val cloudUserName by viewModel.cloudUserName.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val isDeveloperModeEnabled by viewModel.isDeveloperModeEnabled.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showSplash by androidx.compose.runtime.saveable.rememberSaveable {
        mutableStateOf(!android.os.Build.FINGERPRINT.equals("robolectric", ignoreCase = true))
    }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var onboardingDialogSection by remember { mutableStateOf<String?>(null) }
    var guideInitialSection by remember { mutableStateOf("HOW_TO_USE_DONGLE") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle back button to return to Home tab if on a sub-tab or secondary screen
    BackHandler(enabled = selectedTab != NavTab.HOME) {
        when (selectedTab) {
            NavTab.CAR_SELECTION -> viewModel.selectTab(carSelectionReturnTab)
            else -> viewModel.selectTab(NavTab.HOME)
        }
    }

    if (showSplash) {
        com.example.ui.components.FahsSplashScreen(
            language = currentLanguage,
            onSplashFinished = { showSplash = false }
        )
        return
    }

    if (showLanguagePicker) {
        LanguageSelectorDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { lang ->
                viewModel.setLanguage(lang)
            },
            onDismiss = { showLanguagePicker = false }
        )
    }

    val activeOnboardingSection = onboardingDialogSection
    if (activeOnboardingSection != null) {
        com.example.ui.components.OnboardingAndReleaseDialog(
            language = currentLanguage,
            initialSection = activeOnboardingSection,
            shopSettings = shopSettings,
            isDeveloperModeEnabled = isDeveloperModeEnabled,
            onToggleDeveloperMode = { enabled ->
                viewModel.setDeveloperModeEnabled(enabled)
                scope.launch {
                    val msg = if (enabled) {
                        when (currentLanguage) {
                            AppLanguage.AR -> "تم تفعيل وضع المطور"
                            AppLanguage.FR -> "Mode développeur activé"
                            AppLanguage.EN -> "Developer mode enabled"
                        }
                    } else {
                        when (currentLanguage) {
                            AppLanguage.AR -> "تم إيقاف وضع المطور"
                            AppLanguage.FR -> "Mode développeur désactivé"
                            AppLanguage.EN -> "Developer mode disabled"
                        }
                    }
                    snackbarHostState.showSnackbar(msg)
                }
            },
            onSelectLanguage = { lang -> viewModel.setLanguage(lang) },
            onDismiss = {
                LanguagePreferences.setOnboardingCompleted(context, true)
                onboardingDialogSection = null
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = FahsHairline, thickness = 1.dp)
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("main_bottom_nav"),
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    val isHomeSelected = selectedTab == NavTab.HOME ||
                        selectedTab == NavTab.OBD ||
                        (selectedTab == NavTab.CAR_SELECTION && carSelectionReturnTab == NavTab.HOME)
                    val isReportsSelected = selectedTab == NavTab.INSPECTIONS ||
                        selectedTab == NavTab.CHECKLIST_WIZARD ||
                        selectedTab == NavTab.REPORT

                    // Tab 1: Home
                    NavigationBarItem(
                        selected = isHomeSelected,
                        onClick = { viewModel.selectTab(NavTab.HOME) },
                        modifier = Modifier.testTag("bottom_tab_home"),
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = Strings.navHome(currentLanguage)
                            )
                        },
                        label = {
                            Text(
                                text = Strings.navHome(currentLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isHomeSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FahsInk,
                            selectedTextColor = FahsInk,
                            indicatorColor = FahsBrandSoft,
                            unselectedIconColor = FahsMutedText,
                            unselectedTextColor = FahsMutedText
                        )
                    )

                    // Tab 2: My reports
                    NavigationBarItem(
                        selected = isReportsSelected,
                        onClick = { viewModel.selectTab(NavTab.INSPECTIONS) },
                        modifier = Modifier.testTag("bottom_tab_reports"),
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ListAlt,
                                contentDescription = Strings.navInspections(currentLanguage)
                            )
                        },
                        label = {
                            Text(
                                text = Strings.navInspections(currentLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isReportsSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FahsInk,
                            selectedTextColor = FahsInk,
                            indicatorColor = FahsBrandSoft,
                            unselectedIconColor = FahsMutedText,
                            unselectedTextColor = FahsMutedText
                        )
                    )

                    // Tab 3: Guide (how to use a dongle, where the OBD port is, buyer tips, order a dongle)
                    NavigationBarItem(
                        selected = selectedTab == NavTab.GUIDE,
                        onClick = {
                            guideInitialSection = "HOW_TO_USE_DONGLE"
                            viewModel.selectTab(NavTab.GUIDE)
                        },
                        modifier = Modifier.testTag("bottom_tab_guide"),
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                contentDescription = Strings.navGuide(currentLanguage)
                            )
                        },
                        label = {
                            Text(
                                text = Strings.navGuide(currentLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == NavTab.GUIDE) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FahsInk,
                            selectedTextColor = FahsInk,
                            indicatorColor = FahsBrandSoft,
                            unselectedIconColor = FahsMutedText,
                            unselectedTextColor = FahsMutedText
                        )
                    )

                    // Tab 4: Account (sign-in, backup, language, privacy, terms, get a dongle, my dongle, contact, about)
                    NavigationBarItem(
                        selected = selectedTab == NavTab.ACCOUNT,
                        onClick = { viewModel.selectTab(NavTab.ACCOUNT) },
                        modifier = Modifier.testTag("bottom_tab_account"),
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = Strings.navAccount(currentLanguage)
                            )
                        },
                        label = {
                            Text(
                                text = Strings.navAccount(currentLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == NavTab.ACCOUNT) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FahsInk,
                            selectedTextColor = FahsInk,
                            indicatorColor = FahsBrandSoft,
                            unselectedIconColor = FahsMutedText,
                            unselectedTextColor = FahsMutedText
                        )
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                when (selectedTab) {
                    NavTab.HOME -> {
                        HomeScreen(
                            language = currentLanguage,
                            inspections = inspections,
                            completedCount = completedCount,
                            inProgressCount = inProgressCount,
                            shopSettings = shopSettings,
                            optionalCarDraft = optionalCarDraft,
                            onChooseOptionalCarModel = {
                                viewModel.openOptionalCarSelectionForSession(returnTab = NavTab.HOME)
                            },
                            onClearOptionalCarModel = {
                                viewModel.clearOptionalCarSelection()
                            },
                            onProceedToAutomaticScan = { autoConnectRemembered ->
                                viewModel.startAutomaticScanFromHome(autoConnectRemembered)
                            },
                            onStartManualInspectionDirectly = { isQuick ->
                                viewModel.startManualInspectionDirectly(isQuickMode = isQuick)
                            },
                            onOpenDongleOrderPage = {
                                onboardingDialogSection = "DONGLE_GUIDE"
                            },
                            onSelectInspection = { id ->
                                viewModel.openInspectionOrReport(id)
                            }
                        )
                    }

                    NavTab.CAR_SELECTION -> {
                        val isOptionalOnly = carSelectionReturnTab == NavTab.HOME ||
                            carSelectionReturnTab == NavTab.OBD ||
                            carSelectionReturnTab == NavTab.REPORT
                        CarSelectionScreen(
                            language = currentLanguage,
                            initialQuickMode = preferredQuickMode,
                            preselectedBrand = preselectedBrandFromVin,
                            isOptionalSelectionOnly = isOptionalOnly,
                            onSkipCarSelection = {
                                if (carSelectionReturnTab == NavTab.CHECKLIST_WIZARD) {
                                    viewModel.startManualInspectionDirectly(isQuickMode = preferredQuickMode)
                                } else {
                                    viewModel.selectTab(carSelectionReturnTab)
                                }
                            },
                            onBack = { viewModel.selectTab(carSelectionReturnTab) },
                            onCarConfirmed = { brand, model, generation, year, fuel, gearbox, engine, isQuick, plateNumber, odometerKm, hasTurbo, isFourWheelDrive, bodyType ->
                                viewModel.createConfiguredInspection(
                                    brand = brand,
                                    model = model,
                                    generation = generation,
                                    year = year,
                                    fuelType = fuel,
                                    gearboxType = gearbox,
                                    engineDesc = engine,
                                    isQuickMode = isQuick,
                                    plateNumber = plateNumber,
                                    odometerKm = odometerKm,
                                    hasTurbo = hasTurbo,
                                    isFourWheelDrive = isFourWheelDrive,
                                    bodyType = bodyType,
                                    targetTab = carSelectionReturnTab
                                )
                            }
                        )
                    }

                    NavTab.CHECKLIST_WIZARD -> {
                        val currentInsp = activeInspection
                        if (currentInsp != null) {
                            ChecklistWizardScreen(
                                inspection = currentInsp,
                                savedAnswers = activeInspectionAnswers,
                                language = currentLanguage,
                                onSaveAnswer = { item, status, note, photoUri, totalAdaptiveItems ->
                                    viewModel.saveChecklistAnswer(
                                        item = item,
                                        status = status,
                                        note = note,
                                        photoUri = photoUri,
                                        totalAdaptiveItems = totalAdaptiveItems
                                    )
                                },
                                onExtendToFullInspection = {
                                    viewModel.extendActiveInspectionToFull()
                                    scope.launch {
                                        val msg = when (currentLanguage) {
                                            AppLanguage.AR -> "تم توسيع الفحص إلى الفحص الشامل"
                                            AppLanguage.FR -> "Inspection étendue au mode complet"
                                            AppLanguage.EN -> "Extended to full inspection mode"
                                        }
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                },
                                onPauseAndExit = {
                                    viewModel.selectTab(NavTab.INSPECTIONS)
                                    scope.launch {
                                        val msg = when (currentLanguage) {
                                            AppLanguage.AR -> "تم حفظ تقدم الفحص، يمكنك المتابعة في أي وقت"
                                            AppLanguage.FR -> "Progression sauvegardée. Vous pouvez reprendre plus tard."
                                            AppLanguage.EN -> "Progress saved. You can resume anytime."
                                        }
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                },
                                onFinishInspection = { score, verdict, totalRepairDzd ->
                                    viewModel.finishActiveInspection(score, verdict, totalRepairDzd)
                                    scope.launch {
                                        val msg = if (score != null) {
                                            when (currentLanguage) {
                                                AppLanguage.AR -> "اكتمل الفحص! النتيجة: $score/100"
                                                AppLanguage.FR -> "Inspection terminée ! Score : $score/100"
                                                AppLanguage.EN -> "Inspection finished! Score: $score/100"
                                            }
                                        } else {
                                            when (currentLanguage) {
                                                AppLanguage.AR -> "الفحص غير مكتمل بعد"
                                                AppLanguage.FR -> "Inspection incomplète"
                                                AppLanguage.EN -> "Inspection incomplete"
                                            }
                                        }
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                },
                                onOpenObdScanner = {
                                    viewModel.selectTab(NavTab.OBD)
                                },
                                onOpenLiveReport = {
                                    viewModel.openReportForActiveInspection()
                                }
                            )
                        } else {
                            viewModel.selectTab(NavTab.INSPECTIONS)
                        }
                    }

                    NavTab.REPORT -> {
                        val currentInsp = activeInspection
                        if (currentInsp != null) {
                            InspectionReportScreen(
                                inspection = currentInsp,
                                savedAnswers = activeInspectionAnswers,
                                language = currentLanguage,
                                onBackToInspections = {
                                    viewModel.selectTab(NavTab.INSPECTIONS)
                                },
                                onReopenChecklist = {
                                    viewModel.selectTab(NavTab.CHECKLIST_WIZARD)
                                },
                                onOpenObdScanner = {
                                    viewModel.selectTab(NavTab.OBD)
                                },
                                onSaveNegotiationInputs = { askingMillions, mechanicQuoteDzd ->
                                    viewModel.saveUserNegotiationInputs(askingMillions, mechanicQuoteDzd)
                                },
                                onPdfGeneratedNotification = { msg ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            )
                        } else {
                            viewModel.selectTab(NavTab.INSPECTIONS)
                        }
                    }

                    NavTab.INSPECTIONS -> {
                        val allAnswersMap by viewModel.allAnswersByInspectionId.collectAsStateWithLifecycle()
                        SavedInspectionsScreen(
                            language = currentLanguage,
                            inspections = inspections,
                            answersByInspectionId = allAnswersMap,
                            cloudUserEmail = cloudUserEmail,
                            cloudUserName = cloudUserName,
                            isCloudSyncing = isCloudSyncing,
                            isDeveloperModeEnabled = isDeveloperModeEnabled,
                            onRefreshAuth = { viewModel.refreshAuthState() },
                            onBackupToCloud = {
                                viewModel.backupAllInspectionsToCloud { _, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },
                            onRestoreFromCloud = {
                                viewModel.restoreInspectionsFromCloud { _, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },
                            onDeleteAllCloudBackups = {
                                viewModel.deleteAllCloudBackups { _, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },
                            onSeedComparisonDemo = {
                                viewModel.seedSampleComparisonInspections {
                                    scope.launch {
                                        val msg = when (currentLanguage) {
                                            AppLanguage.AR -> "تمت إضافة سيارتين نموذجيتين لتجربة المقارنة"
                                            AppLanguage.FR -> "2 voitures d'exemple ajoutées pour tester le comparateur"
                                            AppLanguage.EN -> "Added 2 sample cars to test comparison"
                                        }
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            },
                            onNotifyMessage = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            },
                            onStartNewInspection = {
                                viewModel.selectTab(NavTab.HOME)
                            },
                            onSelectInspection = { id ->
                                viewModel.openInspectionOrReport(id)
                            },
                            onDuplicateInspection = { id ->
                                viewModel.duplicateInspection(id) {
                                    scope.launch {
                                        val message = when (currentLanguage) {
                                            AppLanguage.AR -> "تم نسخ الفحص بنجاح"
                                            AppLanguage.FR -> "Inspection dupliquée avec succès"
                                            AppLanguage.EN -> "Inspection duplicated"
                                        }
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                            },
                            onDeleteInspection = { id ->
                                viewModel.deleteInspection(id)
                                scope.launch {
                                    val message = when (currentLanguage) {
                                        AppLanguage.AR -> "تم حذف الفحص"
                                        AppLanguage.FR -> "Inspection supprimée"
                                        AppLanguage.EN -> "Inspection deleted"
                                    }
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }

                    NavTab.OBD -> {
                        ObdGuideScreen(
                            language = currentLanguage,
                            activeInspection = activeInspection,
                            initialScanResult = lastObdScanResult,
                            shopSettings = shopSettings,
                            onOpenDongleBuyingGuide = {
                                onboardingDialogSection = "DONGLE_GUIDE"
                            },
                            isDeveloperModeEnabled = isDeveloperModeEnabled,
                            onAddCarModelForTips = {
                                viewModel.openOptionalCarSelectionForSession(returnTab = NavTab.OBD)
                            },
                            onDetectedEcuVin = { vin ->
                                viewModel.setPreselectedBrandFromVin(vin)
                            },
                            onSaveScanToInspection = { scanResult, milBulbLit, ecuVinComparison ->
                                viewModel.saveObdScanToActiveInspection(
                                    scanResult = scanResult,
                                    milBulbLitOnDashboard = milBulbLit,
                                    ecuVinComparison = ecuVinComparison,
                                    returnToChecklist = false
                                )
                                scope.launch {
                                    val msg = when (currentLanguage) {
                                        AppLanguage.AR -> "تم حفظ نتيجة الفحص في تقاريري"
                                        AppLanguage.FR -> "Résultat du scan enregistré dans Mes rapports"
                                        AppLanguage.EN -> "Scan result saved to My reports"
                                    }
                                    snackbarHostState.showSnackbar(msg)
                                }
                            },
                            onConfirmTestDriveSummary = { summary ->
                                viewModel.applyConfirmedTestDriveSummary(
                                    summary = summary,
                                    returnToChecklist = false
                                )
                                scope.launch {
                                    val msg = when (currentLanguage) {
                                        AppLanguage.AR -> "تم تأكيد وحفظ ملخص تجربة القيادة في التقرير"
                                        AppLanguage.FR -> "Bilan d'essai routier confirmé et enregistré"
                                        AppLanguage.EN -> "Test-drive summary confirmed and saved to report"
                                    }
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        )
                    }

                    NavTab.GUIDE -> {
                        BuyerTipsScreen(
                            language = currentLanguage,
                            shopSettings = shopSettings,
                            initialSection = guideInitialSection
                        )
                    }

                    NavTab.ACCOUNT -> {
                        AccountScreen(
                            language = currentLanguage,
                            cloudUserEmail = cloudUserEmail,
                            cloudUserName = cloudUserName,
                            isCloudSyncing = isCloudSyncing,
                            shopSettings = shopSettings,
                            isDeveloperModeEnabled = isDeveloperModeEnabled,
                            onSelectLanguage = { lang -> viewModel.setLanguage(lang) },
                            onOpenDongleOrder = {
                                onboardingDialogSection = "DONGLE_GUIDE"
                            },
                            onOpenPrivacyAndTerms = {
                                onboardingDialogSection = "PRIVACY"
                            },
                            onBackupToCloud = {
                                viewModel.backupAllInspectionsToCloud { _, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },
                            onRestoreFromCloud = {
                                viewModel.restoreInspectionsFromCloud { _, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },
                            onDeleteAllDataAndAccount = {
                                inspections.forEach { insp ->
                                    viewModel.deleteInspection(insp.id)
                                }
                                viewModel.deleteAllCloudBackups { _, _ -> }
                                scope.launch {
                                    val msg = when (currentLanguage) {
                                        AppLanguage.AR -> "تم حذف كافة البيانات والتقارير المحفوظة"
                                        AppLanguage.FR -> "Toutes les données et rapports ont été supprimés"
                                        AppLanguage.EN -> "All saved data and reports have been deleted"
                                    }
                                    snackbarHostState.showSnackbar(msg)
                                }
                            },
                            onToggleDeveloperMode = { enabled ->
                                viewModel.setDeveloperModeEnabled(enabled)
                                scope.launch {
                                    val msg = if (enabled) {
                                        when (currentLanguage) {
                                            AppLanguage.AR -> "تم تفعيل وضع المطور"
                                            AppLanguage.FR -> "Mode développeur activé"
                                            AppLanguage.EN -> "Developer mode enabled"
                                        }
                                    } else {
                                        when (currentLanguage) {
                                            AppLanguage.AR -> "تم إيقاف وضع المطور"
                                            AppLanguage.FR -> "Mode développeur désactivé"
                                            AppLanguage.EN -> "Developer mode disabled"
                                        }
                                    }
                                    snackbarHostState.showSnackbar(msg)
                                }
                            },
                            onSeedDeveloperDemoInspections = {
                                viewModel.seedSampleComparisonInspections {
                                    scope.launch {
                                        val msg = when (currentLanguage) {
                                            AppLanguage.AR -> "تمت إضافة سيارتين نموذجيتين"
                                            AppLanguage.FR -> "2 rapports de démonstration ajoutés"
                                            AppLanguage.EN -> "Added 2 demo reports"
                                        }
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            },
                            onShowMessage = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        )
                    }
                }
            }
        }
    }
}
