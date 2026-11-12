package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Speed
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.ui.components.FahsTopAppBar
import com.example.ui.components.LanguageSelectorDialog
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
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

    val context = androidx.compose.ui.platform.LocalContext.current
    var showSplash by androidx.compose.runtime.saveable.rememberSaveable {
        mutableStateOf(!android.os.Build.FINGERPRINT.equals("robolectric", ignoreCase = true))
    }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showWelcomeBanner by remember {
        mutableStateOf(!com.example.core.i18n.LanguagePreferences.isOnboardingCompleted(context))
    }
    var onboardingDialogSection by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle back button to return to Home tab if on a sub-tab or CarSelection
    BackHandler(enabled = selectedTab != NavTab.HOME) {
        viewModel.selectTab(NavTab.HOME)
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
                            AppLanguage.AR -> "تم تفعيل وضع المطور (المحاكي والبيانات التجريبية)"
                            AppLanguage.FR -> "Mode Développeur activé (Simulateur & données démo)"
                            AppLanguage.EN -> "Developer Mode enabled (Simulator & demo data)"
                        }
                    } else {
                        when (currentLanguage) {
                            AppLanguage.AR -> "تم إيقاف وضع المطور"
                            AppLanguage.FR -> "Mode Développeur désactivé"
                            AppLanguage.EN -> "Developer Mode disabled"
                        }
                    }
                    snackbarHostState.showSnackbar(msg)
                }
            },
            onSelectLanguage = { lang -> viewModel.setLanguage(lang) },
            onDismiss = {
                com.example.core.i18n.LanguagePreferences.setOnboardingCompleted(context, true)
                showWelcomeBanner = false
                onboardingDialogSection = null
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            FahsTopAppBar(
                currentLanguage = currentLanguage,
                onOpenLanguagePicker = { showLanguagePicker = true },
                onOpenInfoGuide = { onboardingDialogSection = "TUTORIAL" }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_bottom_nav"),
                containerColor = Color.White,
                tonalElevation = 6.dp
            ) {
                // Tab 1: Home
                NavigationBarItem(
                    selected = selectedTab == NavTab.HOME || selectedTab == NavTab.CAR_SELECTION,
                    onClick = { viewModel.selectTab(NavTab.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavTab.HOME || selectedTab == NavTab.CAR_SELECTION) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = Strings.navHome(currentLanguage)
                        )
                    },
                    label = {
                        Text(
                            text = Strings.navHome(currentLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == NavTab.HOME || selectedTab == NavTab.CAR_SELECTION) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald800,
                        selectedTextColor = Emerald800,
                        indicatorColor = Emerald100,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate600
                    )
                )

                // Tab 2: Saved Inspections
                NavigationBarItem(
                    selected = selectedTab == NavTab.INSPECTIONS || selectedTab == NavTab.CHECKLIST_WIZARD || selectedTab == NavTab.REPORT,
                    onClick = { viewModel.selectTab(NavTab.INSPECTIONS) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavTab.INSPECTIONS || selectedTab == NavTab.CHECKLIST_WIZARD || selectedTab == NavTab.REPORT) Icons.Filled.ListAlt else Icons.Outlined.ListAlt,
                            contentDescription = Strings.navInspections(currentLanguage)
                        )
                    },
                    label = {
                        Text(
                            text = Strings.navInspections(currentLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == NavTab.INSPECTIONS || selectedTab == NavTab.CHECKLIST_WIZARD || selectedTab == NavTab.REPORT) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald800,
                        selectedTextColor = Emerald800,
                        indicatorColor = Emerald100,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate600
                    )
                )

                // Tab 3: OBD Scanner
                NavigationBarItem(
                    selected = selectedTab == NavTab.OBD,
                    onClick = { viewModel.selectTab(NavTab.OBD) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavTab.OBD) Icons.Filled.Speed else Icons.Outlined.Speed,
                            contentDescription = Strings.navObd(currentLanguage)
                        )
                    },
                    label = {
                        Text(
                            text = Strings.navObd(currentLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == NavTab.OBD) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald800,
                        selectedTextColor = Emerald800,
                        indicatorColor = Emerald100,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate600
                    )
                )

                // Tab 4: Guide
                NavigationBarItem(
                    selected = selectedTab == NavTab.GUIDE,
                    onClick = { viewModel.selectTab(NavTab.GUIDE) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavTab.GUIDE) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
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
                        selectedIconColor = Emerald800,
                        selectedTextColor = Emerald800,
                        indicatorColor = Emerald100,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate600
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter
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
                        showWelcomeCard = showWelcomeBanner,
                        onDismissWelcomeCard = {
                            com.example.core.i18n.LanguagePreferences.setOnboardingCompleted(context, true)
                            showWelcomeBanner = false
                        },
                        onOpenOnboardingSection = { section ->
                            onboardingDialogSection = section
                        },
                        onStartInspection = { isQuick ->
                            viewModel.openCarSelection(isQuickMode = isQuick)
                        },
                        onOpenObdGuide = { viewModel.selectTab(NavTab.OBD) },
                        onOpenBuyerTips = { viewModel.selectTab(NavTab.GUIDE) },
                        onViewAllInspections = { viewModel.selectTab(NavTab.INSPECTIONS) },
                        onSelectInspection = { id ->
                            viewModel.openInspectionOrReport(id)
                        }
                    )
                }

                NavTab.CAR_SELECTION -> {
                    CarSelectionScreen(
                        language = currentLanguage,
                        initialQuickMode = preferredQuickMode,
                        onBack = { viewModel.selectTab(NavTab.HOME) },
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
                                bodyType = bodyType
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
                                        AppLanguage.EN -> "Extended to Full Inspection mode"
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
                                            AppLanguage.AR -> "الفحص غير مكتمل بعد (أجب على جميع النقاط الحرجة و70% من الأسئلة لحساب النتيجة)"
                                            AppLanguage.FR -> "Inspection incomplète (répondez à tous les points critiques et 70% des questions pour calculer le score)"
                                            AppLanguage.EN -> "Inspection incomplete (answer all critical items and 70% of questions to calculate a score)"
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
                        onSeedComparisonDemo = {
                            viewModel.seedSampleComparisonInspections {
                                scope.launch {
                                    val msg = when (currentLanguage) {
                                        AppLanguage.AR -> "تمت إضافة سيارتين نموذجيتين (Symbol و 208) لتجربة المقارنة"
                                        AppLanguage.FR -> "2 voitures d'exemple ajoutées (Symbol & 208) pour tester le comparateur"
                                        AppLanguage.EN -> "Added 2 sample cars (Symbol & 208) to test comparison"
                                    }
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        },
                        onNotifyMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        },
                        onStartNewInspection = {
                            viewModel.openCarSelection(isQuickMode = false)
                        },
                        onSelectInspection = { id ->
                            viewModel.openInspectionOrReport(id)
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
                        isDeveloperModeEnabled = isDeveloperModeEnabled,
                        onSaveScanToInspection = { scanResult, milBulbLit, ecuVinComparison ->
                            viewModel.saveObdScanToActiveInspection(
                                scanResult = scanResult,
                                milBulbLitOnDashboard = milBulbLit,
                                ecuVinComparison = ecuVinComparison,
                                returnToChecklist = activeInspection != null
                            )
                            scope.launch {
                                val msg = when (currentLanguage) {
                                    AppLanguage.AR -> "تم حفظ نتائج فحص OBD2 في تقرير السيارة"
                                    AppLanguage.FR -> "Résultats OBD2 enregistrés dans l'inspection"
                                    AppLanguage.EN -> "OBD2 scan results saved to inspection"
                                }
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )
                }

                NavTab.GUIDE -> {
                    BuyerTipsScreen(language = currentLanguage)
                }
            }
            }
        }
    }
}
