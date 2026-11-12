package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LanguagePreferences
import com.example.data.local.AppDatabase
import com.example.data.local.ChecklistAnswerEntity
import com.example.data.local.InspectionEntity
import com.example.data.model.ChecklistItemDefinition
import com.example.data.repository.CloudBackupRepository
import com.example.data.repository.InspectionRepository
import com.example.data.repository.ShopSettings
import com.example.data.repository.ShopSettingsRepository
import com.example.domain.ChecklistEngine
import com.example.domain.EcuVinComparisonChoice
import com.example.domain.ObdScanResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavTab {
    HOME,
    CAR_SELECTION,
    CHECKLIST_WIZARD,
    REPORT,
    INSPECTIONS,
    OBD,
    GUIDE
}

@OptIn(ExperimentalCoroutinesApi::class)
class FahsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InspectionRepository = InspectionRepository(
        AppDatabase.getDatabase(application).inspectionDao()
    )
    private val cloudBackupRepository: CloudBackupRepository by lazy {
        CloudBackupRepository(getApplication())
    }
    private val shopSettingsRepository: ShopSettingsRepository = ShopSettingsRepository(application)
    val shopSettings: StateFlow<ShopSettings> = shopSettingsRepository.settings

    private val _currentLanguage = MutableStateFlow(
        LanguagePreferences.getInitialLanguage(application)
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _isDeveloperModeEnabled = MutableStateFlow(
        LanguagePreferences.isDeveloperModeEnabled(application)
    )
    val isDeveloperModeEnabled: StateFlow<Boolean> = _isDeveloperModeEnabled.asStateFlow()

    private val _selectedTab = MutableStateFlow(NavTab.HOME)
    val selectedTab: StateFlow<NavTab> = _selectedTab.asStateFlow()

    private val _preferredQuickMode = MutableStateFlow(false)
    val preferredQuickMode: StateFlow<Boolean> = _preferredQuickMode.asStateFlow()

    private val _activeInspectionId = MutableStateFlow<Long?>(null)
    val activeInspectionId: StateFlow<Long?> = _activeInspectionId.asStateFlow()

    private val _cloudUserEmail = MutableStateFlow<String?>(null)
    val cloudUserEmail: StateFlow<String?> = _cloudUserEmail.asStateFlow()

    private val _cloudUserName = MutableStateFlow<String?>(null)
    val cloudUserName: StateFlow<String?> = _cloudUserName.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    val activeInspection: StateFlow<InspectionEntity?> = _activeInspectionId
        .flatMapLatest { id ->
            if (id != null) repository.observeInspection(id) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val activeInspectionAnswers: StateFlow<List<ChecklistAnswerEntity>> = _activeInspectionId
        .flatMapLatest { id ->
            if (id != null) repository.getAnswers(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val inspections: StateFlow<List<InspectionEntity>> = repository.allInspections
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allAnswersByInspectionId: StateFlow<Map<Long, List<ChecklistAnswerEntity>>> = repository.allAnswers
        .map { list -> list.groupBy { it.inspectionId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val completedCount: StateFlow<Int> = repository.completedCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val inProgressCount: StateFlow<Int> = repository.inProgressCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        refreshAuthState()
        viewModelScope.launch {
            shopSettingsRepository.refreshFromFirestore()
        }
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        _isDeveloperModeEnabled.value = enabled
        LanguagePreferences.setDeveloperModeEnabled(getApplication(), enabled)
    }

    fun refreshAuthState() {
        val user = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
        _cloudUserEmail.value = user?.email
        _cloudUserName.value = user?.displayName
    }

    fun backupAllInspectionsToCloud(onResult: (Boolean, String) -> Unit) {
        val currentUser = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
        val lang = _currentLanguage.value
        if (currentUser == null) {
            val msg = when (lang) {
                AppLanguage.AR -> "يرجى تسجيل الدخول بحساب Google أولاً للنسخ الاحتياطي"
                AppLanguage.FR -> "Veuillez d'abord vous connecter avec Google pour sauvegarder."
                AppLanguage.EN -> "Please sign in with Google first to back up."
            }
            onResult(false, msg)
            return
        }

        val localList = inspections.value
        if (localList.isEmpty()) {
            val msg = when (lang) {
                AppLanguage.AR -> "لا توجد فحوصات محلية لنسخها احتياطياً"
                AppLanguage.FR -> "Aucune inspection locale à sauvegarder."
                AppLanguage.EN -> "No local inspections to back up."
            }
            onResult(false, msg)
            return
        }

        viewModelScope.launch {
            _isCloudSyncing.value = true
            var successCount = 0
            var lastError: String? = null
            for (item in localList) {
                val res = cloudBackupRepository.backupInspection(item)
                if (res.isSuccess) {
                    successCount++
                } else {
                    lastError = res.exceptionOrNull()?.localizedMessage
                }
            }
            _isCloudSyncing.value = false

            if (successCount > 0) {
                val msg = when (lang) {
                    AppLanguage.AR -> "تم نسخ $successCount فحوصات احتياطياً في السحابة بنجاح"
                    AppLanguage.FR -> "$successCount inspection(s) sauvegardée(s) dans le Cloud avec succès"
                    AppLanguage.EN -> "Successfully backed up $successCount inspection(s) to Cloud"
                }
                onResult(true, msg)
            } else {
                val msg = when (lang) {
                    AppLanguage.AR -> "تعذر النسخ الاحتياطي: ${lastError ?: "خطأ في الاتصال"}"
                    AppLanguage.FR -> "Échec de sauvegarde : ${lastError ?: "Erreur réseau"}"
                    AppLanguage.EN -> "Backup failed: ${lastError ?: "Network error"}"
                }
                onResult(false, msg)
            }
        }
    }

    fun restoreInspectionsFromCloud(onResult: (Boolean, String) -> Unit) {
        val currentUser = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
        val lang = _currentLanguage.value
        if (currentUser == null) {
            val msg = when (lang) {
                AppLanguage.AR -> "يرجى تسجيل الدخول بحساب Google أولاً للاسترجاع"
                AppLanguage.FR -> "Veuillez d'abord vous connecter avec Google pour restaurer."
                AppLanguage.EN -> "Please sign in with Google first to restore."
            }
            onResult(false, msg)
            return
        }

        viewModelScope.launch {
            _isCloudSyncing.value = true
            val res = cloudBackupRepository.getBackedUpInspections()
            _isCloudSyncing.value = false

            res.fold(
                onSuccess = { cloudList ->
                    if (cloudList.isEmpty()) {
                        val msg = when (lang) {
                            AppLanguage.AR -> "لا توجد نسخ احتياطية محفوظة في حسابك السحابي بعد"
                            AppLanguage.FR -> "Aucune inspection trouvée dans votre sauvegarde Cloud."
                            AppLanguage.EN -> "No backed-up inspections found in your Cloud account."
                        }
                        onResult(true, msg)
                        return@fold
                    }

                    val existingLocal = inspections.value
                    var restoredCount = 0
                    for (backup in cloudList) {
                        val alreadyExists = existingLocal.any { local ->
                            (local.id == backup.localId && local.carBrand == backup.carBrand && local.carModel == backup.carModel) ||
                                (backup.vin.isNotBlank() && local.vin == backup.vin && local.carModel == backup.carModel)
                        }
                        if (!alreadyExists) {
                            repository.insertInspection(backup.toLocalEntity())
                            restoredCount++
                        }
                    }

                    val msg = when (lang) {
                        AppLanguage.AR -> if (restoredCount > 0) {
                            "تم استرجاع $restoredCount فحوصات من السحابة بنجاح"
                        } else {
                            "جميع فحوصاتك السحابية (${cloudList.size}) موجودة بالفعل على هاتفك"
                        }
                        AppLanguage.FR -> if (restoredCount > 0) {
                            "$restoredCount inspection(s) restaurée(s) depuis le Cloud"
                        } else {
                            "Vos ${cloudList.size} inspection(s) Cloud sont déjà sur ce téléphone"
                        }
                        AppLanguage.EN -> if (restoredCount > 0) {
                            "Restored $restoredCount inspection(s) from Cloud"
                        } else {
                            "All ${cloudList.size} Cloud inspection(s) are already on this phone"
                        }
                    }
                    onResult(true, msg)
                },
                onFailure = { err ->
                    val msg = when (lang) {
                        AppLanguage.AR -> "فشل الاسترجاع من السحابة: ${err.localizedMessage ?: ""}"
                        AppLanguage.FR -> "Échec de restauration Cloud : ${err.localizedMessage ?: ""}"
                        AppLanguage.EN -> "Cloud restore failed: ${err.localizedMessage ?: ""}"
                    }
                    onResult(false, msg)
                }
            )
        }
    }

    fun seedSampleComparisonInspections(onSeeded: () -> Unit = {}) {
        if (!_isDeveloperModeEnabled.value) return
        viewModelScope.launch {
            val sample1Items = ChecklistEngine.buildChecklistForCar(
                brand = "Renault",
                model = "Symbol",
                fuelType = "GPL",
                gearboxType = "MANUELLE",
                isQuickMode = false,
                generation = "Symbol 3 (DZ Oued Tlélat)",
                engineDescription = "1.6 MPI 80ch",
                carYear = 2019,
                odometerKm = 94000
            )
            val sample1 = InspectionEntity(
                carBrand = "Renault",
                carModel = "Symbol",
                carGeneration = "Symbol 3 (DZ Oued Tlélat)",
                carYear = 2019,
                odometerKm = 94000,
                fuelType = "GPL",
                gearboxType = "MANUELLE",
                engineDescription = "1.6 MPI 80ch",
                vin = "VF14SRB04K0519283",
                plateNumber = "04821 119 16",
                inspectionMode = "FULL",
                overallScore = 88,
                verdict = "NEGOTIATE",
                status = "COMPLETED",
                totalEstimatedCostDzd = 0,
                hasObdScan = true,
                obdCommunicationSuccess = true,
                obdProtocolDescription = "ISO 15765-4 CAN (11 bit, 500 kbps)",
                obdDtcCodesSummary = "0 DTC",
                obdDtcCount = 0,
                obdClearedStatus = "NONE_NORMAL",
                obdDistanceSinceClearedKm = 4200,
                obdWarmupsSinceCleared = 115,
                obdMilBulbLitAtKeyOn = true,
                obdMilCommandedByEcu = "OFF",
                obdEcuVin = "VF14SRB04K0519283",
                obdEcuVinComparison = "SAME",
                notes = "[DEV DEMO] Première main, révision GPL à jour.",
                totalQuestions = sample1Items.size,
                answeredQuestions = sample1Items.size,
                problemCount = 1
            )
            val id1 = repository.insertInspection(sample1)
            val answers1 = sample1Items.mapIndexed { idx, def ->
                val isProblem = def.id == "under_tyres_wear_date" || (idx == 12 && def.severity.name != "CRITICAL")
                ChecklistAnswerEntity(
                    inspectionId = id1,
                    itemId = def.id,
                    category = def.section.name,
                    questionFr = def.questionFr,
                    questionAr = def.questionAr,
                    questionEn = def.questionEn,
                    status = if (isProblem) "PROBLEM" else "GOOD",
                    severity = def.severity.name,
                    estimatedCostDzd = 0,
                    note = if (isProblem) "Pneus avant usés à 70%" else ""
                )
            }
            repository.saveAnswers(answers1)

            val sample2Items = ChecklistEngine.buildChecklistForCar(
                brand = "Peugeot",
                model = "208",
                fuelType = "DIESEL",
                gearboxType = "MANUELLE",
                isQuickMode = false,
                generation = "208 I Phase 2 (Tech Vision / Allure)",
                engineDescription = "1.6 HDi 92ch",
                carYear = 2018,
                odometerKm = 152000
            )
            val sample2 = InspectionEntity(
                carBrand = "Peugeot",
                carModel = "208",
                carGeneration = "208 I Phase 2 (Tech Vision / Allure)",
                carYear = 2018,
                odometerKm = 152000,
                fuelType = "DIESEL",
                gearboxType = "MANUELLE",
                engineDescription = "1.6 HDi 92ch",
                vin = "VF3CC9HP0JW109482",
                plateNumber = "11290 118 09",
                inspectionMode = "FULL",
                overallScore = 42,
                verdict = "AVOID",
                status = "COMPLETED",
                totalEstimatedCostDzd = 0,
                hasObdScan = true,
                obdCommunicationSuccess = true,
                obdProtocolDescription = "ISO 15765-4 CAN (11 bit, 500 kbps)",
                obdDtcCodesSummary = "P0420 (STORED), P0299 (PENDING)",
                obdDtcCount = 2,
                obdClearedStatus = "STRONG_WARNING",
                obdDistanceSinceClearedKm = 12,
                obdWarmupsSinceCleared = 1,
                obdMilBulbLitAtKeyOn = true,
                obdMilCommandedByEcu = "ON",
                obdEcuVin = "VF3CC9HP0JW109482",
                obdEcuVinComparison = "SAME",
                notes = "[DEV DEMO] Mémoire effacée il y a 12 km, suintement turbo et embrayage haut.",
                totalQuestions = sample2Items.size,
                answeredQuestions = sample2Items.size,
                problemCount = 3
            )
            val id2 = repository.insertInspection(sample2)
            val answers2 = sample2Items.mapIndexed { idx, def ->
                val isProblem = idx == 5 || idx == 14 || idx == 20
                ChecklistAnswerEntity(
                    inspectionId = id2,
                    itemId = def.id,
                    category = def.section.name,
                    questionFr = def.questionFr,
                    questionAr = def.questionAr,
                    questionEn = def.questionEn,
                    status = if (isProblem) "PROBLEM" else "GOOD",
                    severity = def.severity.name,
                    estimatedCostDzd = 0,
                    note = if (isProblem) "Anomalie constatée lors de l'inspection" else ""
                )
            }
            repository.saveAnswers(answers2)
            onSeeded()
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        LanguagePreferences.saveLanguage(getApplication(), language)
    }

    fun selectTab(tab: NavTab) {
        _selectedTab.value = tab
    }

    fun openCarSelection(isQuickMode: Boolean = false) {
        _preferredQuickMode.value = isQuickMode
        _selectedTab.value = NavTab.CAR_SELECTION
    }

    fun openChecklistForInspection(inspectionId: Long) {
        _activeInspectionId.value = inspectionId
        _selectedTab.value = NavTab.CHECKLIST_WIZARD
    }

    fun openInspectionOrReport(inspectionId: Long) {
        _activeInspectionId.value = inspectionId
        viewModelScope.launch {
            val insp = repository.getInspection(inspectionId)
            if (insp != null && insp.status.equals("COMPLETED", ignoreCase = true)) {
                _selectedTab.value = NavTab.REPORT
            } else {
                _selectedTab.value = NavTab.CHECKLIST_WIZARD
            }
        }
    }

    fun openReportForActiveInspection() {
        _selectedTab.value = NavTab.REPORT
    }

    fun createConfiguredInspection(
        brand: String,
        model: String,
        generation: String,
        year: Int,
        fuelType: String,
        gearboxType: String,
        engineDesc: String,
        isQuickMode: Boolean,
        plateNumber: String = "",
        odometerKm: Int = 0,
        hasTurbo: Boolean = false,
        isFourWheelDrive: Boolean = false,
        bodyType: String = "CITADINE",
        onCreated: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val adaptiveList = ChecklistEngine.buildChecklistForCar(
                brand = brand,
                model = model,
                fuelType = fuelType,
                gearboxType = gearboxType,
                isQuickMode = isQuickMode,
                generation = generation,
                engineDescription = engineDesc,
                carYear = year,
                odometerKm = odometerKm,
                hasTurbo = hasTurbo,
                isFourWheelDrive = isFourWheelDrive,
                bodyType = bodyType
            )
            val newInspection = InspectionEntity(
                carBrand = brand,
                carModel = model,
                carGeneration = generation,
                carYear = year,
                odometerKm = odometerKm,
                fuelType = fuelType,
                gearboxType = gearboxType,
                engineDescription = engineDesc,
                hasTurbo = hasTurbo,
                isFourWheelDrive = isFourWheelDrive,
                bodyType = bodyType,
                plateNumber = plateNumber.trim(),
                inspectionMode = if (isQuickMode) "QUICK" else "FULL",
                totalQuestions = adaptiveList.size,
                answeredQuestions = 0,
                status = "IN_PROGRESS"
            )
            val id = repository.insertInspection(newInspection)
            _activeInspectionId.value = id
            _selectedTab.value = NavTab.CHECKLIST_WIZARD
            onCreated(id)
        }
    }

    fun saveChecklistAnswer(
        item: ChecklistItemDefinition,
        status: String,
        note: String,
        photoUri: String?,
        totalAdaptiveItems: Int
    ) {
        val inspectionId = _activeInspectionId.value ?: return
        viewModelScope.launch {
            val existingAnswers = activeInspectionAnswers.value
            val existingForItem = existingAnswers.firstOrNull { it.itemId == item.id }

            val entity = ChecklistAnswerEntity(
                id = existingForItem?.id ?: 0L,
                inspectionId = inspectionId,
                itemId = item.id,
                category = item.section.name,
                questionFr = item.questionFr,
                questionAr = item.questionAr,
                questionEn = item.questionEn,
                status = status,
                severity = item.severity.name,
                estimatedCostDzd = 0,
                note = note,
                photoUri = photoUri,
                timestamp = System.currentTimeMillis()
            )
            repository.saveAnswer(entity)

            val currentInspection = repository.getInspection(inspectionId) ?: return@launch
            val updatedMap = existingAnswers.associateBy { it.itemId }.toMutableMap()
            updatedMap[item.id] = entity

            val answeredCount = updatedMap.size
            val problemCount = updatedMap.values.count { it.status == "PROBLEM" }

            repository.updateInspection(
                currentInspection.copy(
                    totalQuestions = totalAdaptiveItems,
                    answeredQuestions = answeredCount,
                    problemCount = problemCount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveUserNegotiationInputs(
        askingPriceMillions: Double,
        mechanicQuotedRepairDzd: Int
    ) {
        val inspectionId = _activeInspectionId.value ?: return
        viewModelScope.launch {
            val current = repository.getInspection(inspectionId) ?: return@launch
            val safeQuote = mechanicQuotedRepairDzd.coerceAtLeast(0)
            repository.updateInspection(
                current.copy(
                    sellerAskingPriceMillions = askingPriceMillions.coerceAtLeast(0.0),
                    userQuotedRepairDzd = safeQuote,
                    totalEstimatedCostDzd = safeQuote,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun extendActiveInspectionToFull() {
        val inspectionId = _activeInspectionId.value ?: return
        viewModelScope.launch {
            val current = repository.getInspection(inspectionId) ?: return@launch
            val fullList = ChecklistEngine.buildChecklistForCar(
                brand = current.carBrand,
                model = current.carModel,
                fuelType = current.fuelType,
                gearboxType = current.gearboxType,
                isQuickMode = false,
                generation = current.carGeneration,
                engineDescription = current.engineDescription,
                carYear = current.carYear,
                odometerKm = current.odometerKm,
                hasTurbo = current.hasTurbo,
                isFourWheelDrive = current.isFourWheelDrive,
                bodyType = current.bodyType
            )
            repository.updateInspection(
                current.copy(
                    inspectionMode = "FULL",
                    totalQuestions = fullList.size,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun finishActiveInspection(score: Int?, verdict: String, totalRepairDzd: Int) {
        val inspectionId = _activeInspectionId.value ?: return
        viewModelScope.launch {
            val current = repository.getInspection(inspectionId) ?: return@launch
            repository.updateInspection(
                current.copy(
                    status = if (score != null) "COMPLETED" else "IN_PROGRESS",
                    overallScore = score,
                    verdict = verdict,
                    totalEstimatedCostDzd = current.userQuotedRepairDzd,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _selectedTab.value = NavTab.REPORT
        }
    }

    private val _lastObdScanResult = MutableStateFlow<ObdScanResult?>(null)
    val lastObdScanResult: StateFlow<ObdScanResult?> = _lastObdScanResult.asStateFlow()

    fun saveObdScanToActiveInspection(
        scanResult: ObdScanResult,
        milBulbLitOnDashboard: Boolean,
        ecuVinComparison: EcuVinComparisonChoice = EcuVinComparisonChoice.COULD_NOT_COMPARE,
        returnToChecklist: Boolean = true
    ) {
        // Hard block: a simulated result must never be saved into an inspection
        if (scanResult.isSimulated) return

        _lastObdScanResult.value = scanResult
        val inspectionId = _activeInspectionId.value ?: return
        viewModelScope.launch {
            val current = repository.getInspection(inspectionId) ?: return@launch
            val existingAnswers = activeInspectionAnswers.value

            val isMilTampered = !milBulbLitOnDashboard || (scanResult.milCommandedOn == true && !milBulbLitOnDashboard)
            val isEcuVinDifferent = !scanResult.ecuVin.isNullOrBlank() &&
                ecuVinComparison == EcuVinComparisonChoice.DIFFERENT
            val hasFaultsOrFraud = scanResult.dtcList.isNotEmpty() ||
                scanResult.isRecentlyClearedFraudAlert ||
                isMilTampered ||
                isEcuVinDifferent

            val dtcCodesSummary = if (scanResult.dtcList.isEmpty()) {
                "0 DTC"
            } else {
                scanResult.dtcList.joinToString(", ") { "${it.code} (${it.dtcType.name})" }
            }

            val distKm = scanResult.distanceSinceCodesClearedKm
            val warmups = scanResult.warmupsSinceCodesCleared
            val obdSummaryNote = buildString {
                append("[OBD2 Scan: $dtcCodesSummary")
                if (scanResult.isRecentlyClearedFraudAlert) {
                    val distPart = if (distKm != null) "il y a $distKm km" else ""
                    val warmPart = if (warmups != null) " ($warmups cycles)" else ""
                    append(" | Note: Mémoire remise à zéro récemment $distPart$warmPart".trimEnd())
                } else if (distKm != null && distKm >= 0) {
                    append(" | Effacé il y a $distKm km")
                }
                if (isMilTampered) {
                    append(" | ALERTE: Voyant Check-Engine absent au contact")
                }
                if (!scanResult.ecuVin.isNullOrBlank()) {
                    when (ecuVinComparison) {
                        EcuVinComparisonChoice.SAME ->
                            append(" | Châssis ECU (${scanResult.ecuVin}) = Carte grise")
                        EcuVinComparisonChoice.DIFFERENT ->
                            append(" | ALERTE CRITIQUE: Châssis ECU (${scanResult.ecuVin}) différent de la carte grise")
                        EcuVinComparisonChoice.COULD_NOT_COMPARE ->
                            append(" | Châssis ECU: ${scanResult.ecuVin} (non comparé)")
                    }
                }
                append("]")
            }

            val existingObdItem = existingAnswers.firstOrNull { it.itemId == "obd_ecu_diagnostic_scan" }
            val obdAnswer = ChecklistAnswerEntity(
                id = existingObdItem?.id ?: 0L,
                inspectionId = inspectionId,
                itemId = "obd_ecu_diagnostic_scan",
                category = "OBD_DIAGNOSTIC",
                questionFr = "Diagnostic électronique OBD2 (Calculateur moteur, codes défauts & effacement récent)",
                questionAr = "الفحص الإلكتروني للكمبيوتر OBD2 (أكواد الأعطال ومسح الذاكرة الحديث)",
                questionEn = "OBD2 Electronic ECU Scan (Fault codes, MIL bulb & recently cleared memory)",
                status = if (!scanResult.isCommunicationSuccess) "UNCHECKED" else if (hasFaultsOrFraud) "PROBLEM" else "GOOD",
                severity = if (scanResult.isRecentlyClearedFraudAlert || isMilTampered || isEcuVinDifferent || scanResult.dtcList.any { it.severity.name == "CRITICAL" }) "CRITICAL" else "IMPORTANT",
                estimatedCostDzd = 0,
                note = obdSummaryNote,
                photoUri = null,
                timestamp = System.currentTimeMillis()
            )
            repository.saveAnswer(obdAnswer)

            val updatedMap = existingAnswers.associateBy { it.itemId }.toMutableMap()
            updatedMap[obdAnswer.itemId] = obdAnswer

            val problemCount = updatedMap.values.count { it.status == "PROBLEM" }

            val mergedNotes = if (current.notes.contains("[OBD2 Scan:")) {
                current.notes.replace(Regex("\\[OBD2 Scan:.*?]"), obdSummaryNote)
            } else if (current.notes.isBlank()) {
                obdSummaryNote
            } else {
                "${current.notes}\n$obdSummaryNote"
            }

            val updatedVin = if (!scanResult.ecuVin.isNullOrBlank()) scanResult.ecuVin else current.vin
            val milCommandedStr = when (scanResult.milCommandedOn) {
                true -> "ON"
                false -> "OFF"
                null -> "NOT_AVAILABLE"
            }
            val vinComparisonStr = if (scanResult.ecuVin.isNullOrBlank()) {
                "NOT_AVAILABLE"
            } else {
                ecuVinComparison.name
            }

            repository.updateInspection(
                current.copy(
                    vin = updatedVin,
                    hasObdScan = true,
                    obdCommunicationSuccess = scanResult.isCommunicationSuccess,
                    obdProtocolDescription = scanResult.detectedProtocol,
                    obdDtcCodesSummary = dtcCodesSummary,
                    obdDtcCount = scanResult.dtcList.size,
                    obdClearedStatus = scanResult.clearedCodesAlertLevel.name,
                    obdDistanceSinceClearedKm = distKm ?: -1,
                    obdWarmupsSinceCleared = warmups ?: -1,
                    obdMilBulbLitAtKeyOn = milBulbLitOnDashboard,
                    obdMilCommandedByEcu = milCommandedStr,
                    obdEcuVin = scanResult.ecuVin.orEmpty(),
                    obdEcuVinComparison = vinComparisonStr,
                    problemCount = problemCount,
                    notes = mergedNotes,
                    updatedAt = System.currentTimeMillis()
                )
            )

            if (returnToChecklist) {
                _selectedTab.value = NavTab.CHECKLIST_WIZARD
            }
        }
    }

    fun duplicateInspection(id: Long, onDuplicated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val original = repository.getInspection(id) ?: return@launch
            val existingAnswers = allAnswersByInspectionId.value[id].orEmpty()
            val now = System.currentTimeMillis()
            val duplicatedEntity = original.copy(
                id = 0L,
                plateNumber = if (original.plateNumber.isNotBlank()) "${original.plateNumber} (Copie)" else "",
                status = "IN_PROGRESS",
                createdAt = now,
                updatedAt = now
            )
            val newId = repository.insertInspection(duplicatedEntity)
            if (existingAnswers.isNotEmpty()) {
                val copiedAnswers = existingAnswers.map { ans ->
                    ans.copy(
                        id = 0L,
                        inspectionId = newId,
                        timestamp = now
                    )
                }
                repository.saveAnswers(copiedAnswers)
            }
            onDuplicated(newId)
        }
    }

    fun deleteAllCloudBackups(onResult: (Boolean, String) -> Unit) {
        val currentUser = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
        val lang = _currentLanguage.value
        if (currentUser == null) {
            val msg = when (lang) {
                AppLanguage.AR -> "يرجى تسجيل الدخول بحساب Google أولاً"
                AppLanguage.FR -> "Veuillez d'abord vous connecter avec Google."
                AppLanguage.EN -> "Please sign in with Google first."
            }
            onResult(false, msg)
            return
        }

        viewModelScope.launch {
            _isCloudSyncing.value = true
            val res = cloudBackupRepository.deleteAllBackupsForCurrentUser()
            _isCloudSyncing.value = false
            res.fold(
                onSuccess = { count ->
                    val msg = when (lang) {
                        AppLanguage.AR -> "تم حذف جميع النسخ الاحتياطية السحابية ($count) نهائياً"
                        AppLanguage.FR -> "Toutes vos sauvegardes Cloud ($count) ont été supprimées définitivement"
                        AppLanguage.EN -> "Permanently deleted all Cloud backups ($count)"
                    }
                    onResult(true, msg)
                },
                onFailure = { err ->
                    val msg = when (lang) {
                        AppLanguage.AR -> "تعذر حذف النسخ السحابية: ${err.localizedMessage ?: ""}"
                        AppLanguage.FR -> "Échec de suppression Cloud : ${err.localizedMessage ?: ""}"
                        AppLanguage.EN -> "Failed to delete Cloud backups: ${err.localizedMessage ?: ""}"
                    }
                    onResult(false, msg)
                }
            )
        }
    }

    fun deleteInspection(id: Long) {
        viewModelScope.launch {
            repository.deleteInspection(id)
        }
    }
}
