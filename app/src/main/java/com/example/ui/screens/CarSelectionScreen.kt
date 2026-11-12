package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.data.model.AlgerianCarCatalog
import com.example.data.model.AlgerianCarModel
import com.example.data.model.CarBodyType
import com.example.domain.ChecklistEngine
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.StatusWarningAmberContainer
import com.example.ui.theme.StatusWarningAmberOnContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CarSelectionScreen(
    language: AppLanguage,
    initialQuickMode: Boolean = false,
    onBack: () -> Unit,
    onCarConfirmed: (
        brand: String,
        model: String,
        generation: String,
        year: Int,
        fuelType: String,
        gearboxType: String,
        engineDesc: String,
        isQuickMode: Boolean,
        plateNumber: String,
        odometerKm: Int,
        hasTurbo: Boolean,
        isFourWheelDrive: Boolean,
        bodyType: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBrandFilter by remember { mutableStateOf("All") }
    var selectedCar by remember { mutableStateOf<AlgerianCarModel?>(null) }
    var isOtherCarMode by remember { mutableStateOf(false) }

    // Configuration states when a car or "Other car" is selected
    var selectedGeneration by remember { mutableStateOf("") }
    var selectedYear by remember { mutableIntStateOf(2018) }
    var yearInputText by remember { mutableStateOf("2018") }
    var odometerInputText by remember { mutableStateOf("") }
    var selectedFuel by remember { mutableStateOf("ESSENCE") }
    var selectedGearbox by remember { mutableStateOf("MANUELLE") }
    var selectedEngine by remember { mutableStateOf("") }
    var selectedBodyType by remember { mutableStateOf(CarBodyType.CITADINE.name) }
    var hasTurbo by remember { mutableStateOf(false) }
    var isFourWheelDrive by remember { mutableStateOf(false) }
    var isQuickMode by remember { mutableStateOf(initialQuickMode) }
    var optionalPlateLabel by remember { mutableStateOf("") }

    // Custom "Other Car" states
    var customBrand by remember { mutableStateOf("") }
    var customModel by remember { mutableStateOf("") }

    // Phase 7 (Item 3): Offline ML Kit Odometer OCR states (suggestion that user confirms + manual fallback)
    val context = androidx.compose.ui.platform.LocalContext.current
    val ocrScope = androidx.compose.runtime.rememberCoroutineScope()
    var pendingOdoCameraFile by remember { mutableStateOf<java.io.File?>(null) }
    var isRunningOdoOcr by remember { mutableStateOf(false) }
    var odoOcrProposal by remember { mutableStateOf<com.example.domain.OdometerOcrProposal?>(null) }
    var odoOcrStatusMsg by remember { mutableStateOf<String?>(null) }
    var showOdoCameraPermExplanation by remember { mutableStateOf(false) }

    fun processOdometerPhotoFile(file: java.io.File) {
        isRunningOdoOcr = true
        odoOcrStatusMsg = null
        ocrScope.launch {
            val proposal = com.example.domain.OnDeviceOcrHelper.recognizeOdometerFromFile(file)
            isRunningOdoOcr = false
            try { file.delete() } catch (_: Exception) {}
            if (proposal != null) {
                odoOcrProposal = proposal
            } else {
                odoOcrStatusMsg = when (language) {
                    AppLanguage.AR -> "لم يتمكن القارئ المحلي من تمييز رقم الكيلومترات بوضوح. يرجى كتابته يدوياً في الحقل أعلاه."
                    AppLanguage.FR -> "Lecture automatique impossible sur cette photo. Veuillez saisir le kilométrage manuellement ci-dessus."
                    AppLanguage.EN -> "Could not clearly read a mileage number from this photo. Please type it manually above."
                }
            }
        }
    }

    val odoCameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        val f = pendingOdoCameraFile
        pendingOdoCameraFile = null
        if (success && f != null) {
            processOdometerPhotoFile(f)
        } else {
            try { f?.delete() } catch (_: Exception) {}
        }
    }

    val odoCameraPermLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (tempFile, uri) = com.example.core.photo.InspectionPhotoHelper.createTempCameraUri(context, "odometer_ocr")
            pendingOdoCameraFile = tempFile
            odoCameraLauncher.launch(uri)
        }
    }

    val odoGalleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = com.example.core.photo.InspectionPhotoHelper.copyUriToInternalStorage(context, uri, "odometer_ocr_pick")
            if (savedPath != null) {
                processOdometerPhotoFile(java.io.File(savedPath))
            }
        }
    }

    val parsedOdometerKm = odometerInputText.filter { it.isDigit() }.toIntOrNull() ?: 0
    val parsedYear = yearInputText.filter { it.isDigit() }.toIntOrNull()?.takeIf { it in 1985..2026 } ?: selectedYear

    val filteredModels = remember(searchQuery, selectedBrandFilter) {
        AlgerianCarCatalog.models.filter { car ->
            val matchesBrand = selectedBrandFilter == "All" ||
                car.brand.equals(selectedBrandFilter, ignoreCase = true)
            val matchesQuery = com.example.data.model.AlgerianCarCatalogExtra.matchesFuzzySearch(car, searchQuery)
            matchesBrand && matchesQuery
        }
    }

    val effectiveBrandForPreview = selectedCar?.brand ?: customBrand.ifBlank { "Autre" }
    val effectiveModelForPreview = selectedCar?.model ?: customModel.ifBlank { "Véhicule" }
    val effectiveGenForPreview = selectedGeneration.ifBlank { selectedBodyType }

    val quickModeItemCount = remember(
        effectiveBrandForPreview,
        effectiveModelForPreview,
        effectiveGenForPreview,
        parsedYear,
        selectedFuel,
        selectedGearbox,
        selectedEngine,
        parsedOdometerKm,
        hasTurbo,
        isFourWheelDrive,
        selectedBodyType
    ) {
        ChecklistEngine.buildChecklistForCar(
            brand = effectiveBrandForPreview,
            model = effectiveModelForPreview,
            fuelType = selectedFuel,
            gearboxType = selectedGearbox,
            isQuickMode = true,
            generation = effectiveGenForPreview,
            engineDescription = selectedEngine,
            carYear = parsedYear,
            odometerKm = parsedOdometerKm,
            hasTurbo = hasTurbo,
            isFourWheelDrive = isFourWheelDrive,
            bodyType = selectedBodyType
        ).size
    }

    val fullModeItemCount = remember(
        effectiveBrandForPreview,
        effectiveModelForPreview,
        effectiveGenForPreview,
        parsedYear,
        selectedFuel,
        selectedGearbox,
        selectedEngine,
        parsedOdometerKm,
        hasTurbo,
        isFourWheelDrive,
        selectedBodyType
    ) {
        ChecklistEngine.buildChecklistForCar(
            brand = effectiveBrandForPreview,
            model = effectiveModelForPreview,
            fuelType = selectedFuel,
            gearboxType = selectedGearbox,
            isQuickMode = false,
            generation = effectiveGenForPreview,
            engineDescription = selectedEngine,
            carYear = parsedYear,
            odometerKm = parsedOdometerKm,
            hasTurbo = hasTurbo,
            isFourWheelDrive = isFourWheelDrive,
            bodyType = selectedBodyType
        ).size
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Sub-header bar with Back Button & Step Indicator
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        when {
                            selectedCar != null -> selectedCar = null
                            isOtherCarMode -> isOtherCarMode = false
                            else -> onBack()
                        }
                    },
                    modifier = Modifier.testTag("car_selection_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate900
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            selectedCar != null -> "${selectedCar!!.brand} ${selectedCar!!.model}"
                            isOtherCarMode -> when (language) {
                                AppLanguage.AR -> "سيارة أخرى غير مدرجة"
                                AppLanguage.FR -> "Autre véhicule (Sur mesure)"
                                AppLanguage.EN -> "Other Car (Custom Setup)"
                            }
                            else -> when (language) {
                                AppLanguage.AR -> "اختر السيارة المراد فحصها"
                                AppLanguage.FR -> "Choisir le véhicule à inspecter"
                                AppLanguage.EN -> "Select Vehicle to Inspect"
                            }
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            selectedCar != null || isOtherCarMode -> when (language) {
                                AppLanguage.AR -> "الخطوة 2 من 2: حدد السنة، الكيلومترات، المحرك ونوع الفحص"
                                AppLanguage.FR -> "Étape 2/2 : Année, kilométrage, motorisation et mode"
                                AppLanguage.EN -> "Step 2/2: Year, mileage, engine & inspection mode"
                            }
                            else -> when (language) {
                                AppLanguage.AR -> "الخطوة 1 من 2: اختر الموديل من القائمة أو ابحث"
                                AppLanguage.FR -> "Étape 1/2 : Catalogue marché algérien ou recherche"
                                AppLanguage.EN -> "Step 1/2: Algerian market catalogue or search"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Step 1: Catalog Picker
        if (selectedCar == null && !isOtherCarMode) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("car_search_input"),
                        placeholder = {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "ابحث عن علامة أو موديل (مثال: Symbol, Clio, Golf, dCi)..."
                                    AppLanguage.FR -> "Rechercher marque, modèle ou moteur (ex: Clio, 208, TDI)..."
                                    AppLanguage.EN -> "Search brand, model or engine (e.g. Symbol, Golf, HDi)..."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate400
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Emerald700
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Emerald700,
                            unfocusedBorderColor = Slate200,
                            focusedTextColor = Slate900,
                            unfocusedTextColor = Slate900
                        )
                    )
                }

                // Horizontal Brand Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AlgerianCarCatalog.popularBrands.forEach { brand ->
                            val isSelected = selectedBrandFilter == brand
                            val label = if (brand == "All") {
                                when (language) {
                                    AppLanguage.AR -> "الكل"
                                    AppLanguage.FR -> "Toutes marques"
                                    AppLanguage.EN -> "All Brands"
                                }
                            } else brand

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedBrandFilter = brand },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Emerald700,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Slate700
                                )
                            )
                        }
                    }
                }

                // Model Cards List
                items(filteredModels, key = { it.id }) { car ->
                    CatalogCarCard(
                        car = car,
                        language = language,
                        onSelect = {
                            selectedCar = car
                            val firstGen = car.generations.first()
                            selectedGeneration = firstGen.name
                            selectedYear = firstGen.defaultYear
                            yearInputText = firstGen.defaultYear.toString()
                            selectedFuel = car.availableFuels.first()
                            selectedGearbox = car.availableGearboxes.first()
                            val firstEng = car.commonEngines.first()
                            selectedEngine = firstEng
                            selectedBodyType = car.defaultBodyType
                            hasTurbo = ChecklistEngine.inferHasTurbo(selectedFuel, firstEng)
                            isFourWheelDrive = false
                        }
                    )
                }

                // "Other Car" Fallback Card at bottom
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isOtherCarMode = true
                                selectedGeneration = ""
                                selectedYear = 2016
                                yearInputText = "2016"
                                selectedFuel = "ESSENCE"
                                selectedGearbox = "MANUELLE"
                                selectedEngine = "1.2 / 1.4 / 1.6 Essence"
                                selectedBodyType = CarBodyType.BERLINE.name
                                hasTurbo = false
                                isFourWheelDrive = false
                            }
                            .testTag("other_car_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Emerald50),
                        border = BorderStroke(1.5.dp, Emerald600)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Emerald700),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "سيارة أخرى غير موجودة في القائمة؟"
                                        AppLanguage.FR -> "Votre voiture n'est pas dans la liste ?"
                                        AppLanguage.EN -> "Car not in the list? Pick Other Car"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald900
                                )
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "حدد الوقود، علبة السرعة، السنة، الكيلومترات ونوع الهيكل ليتكيف الفحص معها بالكامل"
                                        AppLanguage.FR -> "Indiquez carburant, boîte, année, kilométrage et carrosserie pour adapter toute l'inspection"
                                        AppLanguage.EN -> "Specify fuel, gearbox, year, mileage, and body type for a fully adapted checklist"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Emerald800
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Step 2: Vehicle Configuration & Adaptive Setup
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isOtherCarMode) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "بيانات السيارة ونوع الهيكل"
                                        AppLanguage.FR -> "Identité et carrosserie du véhicule"
                                        AppLanguage.EN -> "Vehicle Identity & Body Type"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = customBrand,
                                    onValueChange = { customBrand = it },
                                    label = {
                                        Text(
                                            when (language) {
                                                AppLanguage.AR -> "العلامة (مثال: Chery, Geely, Ford)"
                                                AppLanguage.FR -> "Marque (ex: Chery, Geely, Ford)"
                                                AppLanguage.EN -> "Brand (e.g. Chery, Geely, Ford)"
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = customModel,
                                    onValueChange = { customModel = it },
                                    label = {
                                        Text(
                                            when (language) {
                                                AppLanguage.AR -> "الموديل (مثال: Tiggo, Coolray, Ranger)"
                                                AppLanguage.FR -> "Modèle (ex: Tiggo, Coolray, Ranger)"
                                                AppLanguage.EN -> "Model (e.g. Tiggo, Coolray, Ranger)"
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "نوع الهيكل (يكيف أسئلة الصندوق، النوابض والدفع):"
                                        AppLanguage.FR -> "Type de carrosserie (adapte les points châssis/suspension) :"
                                        AppLanguage.EN -> "Body Type (adapts chassis & suspension checks):"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate700
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CarBodyType.entries.forEach { body ->
                                        val isSelected = selectedBodyType == body.name
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedBodyType = body.name
                                                if (body == CarBodyType.PICKUP) {
                                                    selectedFuel = "DIESEL"
                                                    hasTurbo = true
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = body.getLabel(language),
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Emerald700,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Known Weak Point & OBD Port Banner for Selected Catalog Car
                selectedCar?.let { car ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Emerald900)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Emerald200,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when (language) {
                                                AppLanguage.AR -> "نقطة الفحص الخاصة بهذا الموديل"
                                                AppLanguage.FR -> "Point de vigilance connu sur ce modèle"
                                                AppLanguage.EN -> "Known Model Watchpoint"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald200
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = StatusWarningAmberContainer
                                    ) {
                                        Text(
                                            text = car.weakPointVerificationStatus.getLabel(language),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusWarningAmberOnContainer,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> car.keyWeakPointAr
                                        AppLanguage.FR -> car.keyWeakPointFr
                                        AppLanguage.EN -> car.keyWeakPointEn
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🔌 OBD2: " + when (language) {
                                        AppLanguage.AR -> car.obdPortHintAr
                                        AppLanguage.FR -> car.obdPortHintFr
                                        AppLanguage.EN -> car.obdPortHintEn
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Emerald100
                                )
                            }
                        }
                    }

                    // Generation Selector
                    item {
                        ConfigSectionCard(
                            title = when (language) {
                                AppLanguage.AR -> "1. الجيل (Génération)"
                                AppLanguage.FR -> "1. Génération du modèle"
                                AppLanguage.EN -> "1. Model Generation"
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                car.generations.forEach { gen ->
                                    val isSelected = selectedGeneration == gen.name
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedGeneration = gen.name
                                                selectedYear = gen.defaultYear
                                                yearInputText = gen.defaultYear.toString()
                                            },
                                        color = if (isSelected) Emerald50 else Slate100,
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Emerald700 else Slate200
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp)
                                            ) {
                                                Text(
                                                    text = gen.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate900
                                                )
                                                Text(
                                                    text = gen.yearRange,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Slate600
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Emerald700
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Year & Odometer Mileage Card (Asked for every car)
                item {
                    ConfigSectionCard(
                        title = when (language) {
                            AppLanguage.AR -> "2. سنة الصنع والكيلومترات المعروضة في العداد"
                            AppLanguage.FR -> "2. Année et kilométrage affiché au compteur"
                            AppLanguage.EN -> "2. Year & Mileage Shown on Odometer"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = yearInputText,
                                    onValueChange = { raw ->
                                        val digits = raw.filter { it.isDigit() }.take(4)
                                        yearInputText = digits
                                        digits.toIntOrNull()?.let { yr ->
                                            if (yr in 1985..2026) selectedYear = yr
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .testTag("car_year_input"),
                                    label = {
                                        Text(
                                            when (language) {
                                                AppLanguage.AR -> "سنة الصنع"
                                                AppLanguage.FR -> "Année"
                                                AppLanguage.EN -> "Year"
                                            }
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = odometerInputText,
                                    onValueChange = { raw ->
                                        odometerInputText = raw.filter { it.isDigit() }.take(7)
                                    },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("car_odometer_input"),
                                    label = {
                                        Text(
                                            when (language) {
                                                AppLanguage.AR -> "عداد الكيلومترات (كم)"
                                                AppLanguage.FR -> "Kilométrage (km)"
                                                AppLanguage.EN -> "Odometer (km)"
                                            }
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            when (language) {
                                                AppLanguage.AR -> "مثال: 135000"
                                                AppLanguage.FR -> "Ex: 135000"
                                                AppLanguage.EN -> "e.g. 135000"
                                            }
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            // Phase 7 (Item 3): Mileage from a photo (Bundled ML Kit OCR with user confirmation & manual fallback)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = {
                                        val hasCam = androidx.core.content.ContextCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.CAMERA
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                        if (hasCam) {
                                            val (tempFile, uri) = com.example.core.photo.InspectionPhotoHelper.createTempCameraUri(context, "odometer_ocr")
                                            pendingOdoCameraFile = tempFile
                                            odoCameraLauncher.launch(uri)
                                        } else {
                                            showOdoCameraPermExplanation = true
                                        }
                                    },
                                    enabled = !isRunningOdoOcr,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("scan_odometer_photo_btn"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "📷 قراءة العداد من صورة (بدون إنترنت)"
                                            AppLanguage.FR -> "📷 Lire compteur par photo (Hors-ligne)"
                                            AppLanguage.EN -> "📷 Read Odometer from Photo (Offline)"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald800
                                    )
                                }

                                androidx.compose.material3.OutlinedButton(
                                    onClick = {
                                        odoGalleryLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    enabled = !isRunningOdoOcr,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "صورة من المعرض"
                                            AppLanguage.FR -> "Galerie"
                                            AppLanguage.EN -> "Gallery"
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            if (showOdoCameraPermExplanation) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Emerald50,
                                    border = BorderStroke(1.dp, Emerald200)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = when (language) {
                                                AppLanguage.AR -> "نحتاج صلاحية الكاميرا لتصوير عداد الكيلومترات وقراءة الرقم محلياً على هاتفك دون إنترنت."
                                                AppLanguage.FR -> "L'accès à la caméra sert uniquement à photographier le compteur pour lire le kilométrage sur le téléphone (hors-ligne)."
                                                AppLanguage.EN -> "Camera access is used to photograph the odometer so on-device text recognition can propose the mileage offline."
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Emerald900
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                showOdoCameraPermExplanation = false
                                                odoCameraPermLauncher.launch(android.Manifest.permission.CAMERA)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.AR -> "متابعة وفتح الكاميرا"
                                                    AppLanguage.FR -> "Continuer & Ouvrir la caméra"
                                                    AppLanguage.EN -> "Continue & Open Camera"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            if (isRunningOdoOcr) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "جاري قراءة أرقام العداد محلياً (Google ML Kit)..."
                                        AppLanguage.FR -> "Reconnaissance du kilométrage en cours (ML Kit hors-ligne)..."
                                        AppLanguage.EN -> "Reading odometer digits on-device (offline ML Kit)..."
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald800
                                )
                            }

                            val currentOdoProposal = odoOcrProposal
                            if (currentOdoProposal != null) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("odometer_ocr_confirmation_box"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Emerald50,
                                    border = BorderStroke(1.5.dp, Emerald600)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = when (language) {
                                                AppLanguage.AR -> "الرقم المقترح من الصورة: ${currentOdoProposal.proposedKm} كم — هل تؤكد هذا الرقم؟"
                                                AppLanguage.FR -> "Kilométrage lu sur la photo : ${currentOdoProposal.proposedKm} km — Confirmez-vous ?"
                                                AppLanguage.EN -> "Proposed mileage from photo: ${currentOdoProposal.proposedKm} km — Confirm?"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald900
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            currentOdoProposal.allCandidatesKm.forEach { candidateKm ->
                                                Button(
                                                    onClick = {
                                                        odometerInputText = candidateKm.toString()
                                                        odoOcrProposal = null
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                                    modifier = Modifier.testTag("confirm_odometer_ocr_${candidateKm}_btn")
                                                ) {
                                                    Text(
                                                        text = when (language) {
                                                            AppLanguage.AR -> "تأكيد $candidateKm كم ✓"
                                                            AppLanguage.FR -> "Confirmer $candidateKm km ✓"
                                                            AppLanguage.EN -> "Confirm $candidateKm km ✓"
                                                        },
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                            androidx.compose.material3.OutlinedButton(
                                                onClick = { odoOcrProposal = null },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = when (language) {
                                                        AppLanguage.AR -> "إلغاء / كتابة يدوية"
                                                        AppLanguage.FR -> "Ignorer / Saisie manuelle"
                                                        AppLanguage.EN -> "Dismiss / Type Manually"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (odoOcrStatusMsg != null) {
                                Text(
                                    text = odoOcrStatusMsg!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate700
                                )
                            }

                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "اختيار سريع لفئة الكيلومترات:"
                                    AppLanguage.FR -> "Sélection rapide de la tranche kilométrique :"
                                    AppLanguage.EN -> "Quick mileage band selection:"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate600
                            )

                            val mileageBands = listOf(
                                40000 to "< 60 000 km",
                                95000 to "60k – 120k km",
                                150000 to "120k – 180k km",
                                210000 to "> 180 000 km"
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                mileageBands.forEach { (kmVal, label) ->
                                    val isBandSelected = parsedOdometerKm == kmVal
                                    FilterChip(
                                        selected = isBandSelected,
                                        onClick = { odometerInputText = kmVal.toString() },
                                        label = { Text(label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald700,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Fuel Type Selector (Essence, Diesel, GPL, Hybride)
                item {
                    val fuels = selectedCar?.availableFuels ?: listOf("ESSENCE", "DIESEL", "GPL", "HYBRIDE")
                    ConfigSectionCard(
                        title = when (language) {
                            AppLanguage.AR -> "3. نوع الوقود (يكيف أسئلة المحرك والغاز)"
                            AppLanguage.FR -> "3. Carburant (adapte les contrôles moteur/GPL)"
                            AppLanguage.EN -> "3. Fuel Type (adapts engine & LPG checks)"
                        }
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            fuels.forEach { fuel ->
                                val isSelected = selectedFuel == fuel
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedFuel = fuel
                                        hasTurbo = ChecklistEngine.inferHasTurbo(fuel, selectedEngine)
                                    },
                                    label = {
                                        Text(
                                            text = when (fuel) {
                                                "GPL" -> "Essence + GPL (Sirghaz)"
                                                "DIESEL" -> "Diesel (Mazout)"
                                                "ESSENCE" -> "Essence"
                                                else -> fuel
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Emerald700,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Gearbox Selector
                item {
                    val gearboxes = selectedCar?.availableGearboxes ?: listOf("MANUELLE", "AUTOMATIQUE")
                    ConfigSectionCard(
                        title = when (language) {
                            AppLanguage.AR -> "4. علبة السرعة (La Boîte)"
                            AppLanguage.FR -> "4. Boîte de vitesses"
                            AppLanguage.EN -> "4. Gearbox Type"
                        }
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            gearboxes.forEach { box ->
                                val isSelected = selectedGearbox == box
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedGearbox = box },
                                    label = {
                                        Text(
                                            text = box,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Emerald700,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Engine Variant + Turbo & 4x4 Conditions
                item {
                    val engines = selectedCar?.commonEngines ?: listOf(
                        "1.2 / 1.4 / 1.6 Essence",
                        "1.2 / 1.4 / 1.5 Turbo Essence",
                        "1.6 MPI / GPL",
                        "1.5 / 1.6 / 2.0 Turbo Diesel"
                    )
                    ConfigSectionCard(
                        title = when (language) {
                            AppLanguage.AR -> "5. المحرك، التيربو والدفع الرباعي (4x4)"
                            AppLanguage.FR -> "5. Motorisation, Turbo et Transmission 4x4"
                            AppLanguage.EN -> "5. Engine, Turbo & 4x4 Drive"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                engines.forEach { eng ->
                                    val isSelected = selectedEngine == eng
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedEngine = eng
                                            hasTurbo = ChecklistEngine.inferHasTurbo(selectedFuel, eng)
                                        },
                                        label = { Text(eng) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Slate900,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilterChip(
                                    selected = hasTurbo,
                                    onClick = { hasTurbo = !hasTurbo },
                                    label = {
                                        Text(
                                            text = when (language) {
                                                AppLanguage.AR -> if (hasTurbo) "تيربو (Turbo) ✓" else "تيربو (Turbo)"
                                                AppLanguage.FR -> if (hasTurbo) "Moteur Turbo ✓" else "Moteur Turbo"
                                                AppLanguage.EN -> if (hasTurbo) "Turbocharged ✓" else "Turbocharged"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Emerald700,
                                        selectedLabelColor = Color.White
                                    )
                                )

                                val show4x4Option = isOtherCarMode ||
                                    selectedCar?.hasFourWheelDriveOption == true ||
                                    selectedBodyType == CarBodyType.SUV.name ||
                                    selectedBodyType == CarBodyType.PICKUP.name

                                if (show4x4Option) {
                                    FilterChip(
                                        selected = isFourWheelDrive,
                                        onClick = { isFourWheelDrive = !isFourWheelDrive },
                                        label = {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.AR -> if (isFourWheelDrive) "دفع رباعي 4x4 ✓" else "دفع رباعي 4x4"
                                                    AppLanguage.FR -> if (isFourWheelDrive) "Transmission 4x4 / AWD ✓" else "Transmission 4x4 / AWD"
                                                    AppLanguage.EN -> if (isFourWheelDrive) "4x4 / AWD ✓" else "4x4 / AWD"
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald700,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Inspection Depth Selector (Quick vs Full) with live adapted counts
                item {
                    ConfigSectionCard(
                        title = when (language) {
                            AppLanguage.AR -> "6. مستوى الفحص"
                            AppLanguage.FR -> "6. Niveau d'inspection"
                            AppLanguage.EN -> "6. Inspection Depth"
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { isQuickMode = true },
                                color = if (isQuickMode) StatusWarningAmberContainer else Slate100,
                                border = BorderStroke(
                                    width = if (isQuickMode) 2.dp else 1.dp,
                                    color = if (isQuickMode) StatusWarningAmber else Slate200
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "⚡ " + Strings.quickCheck(language),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$quickModeItemCount " + when (language) {
                                            AppLanguage.AR -> "نقطة أساسية مكيفة"
                                            AppLanguage.FR -> "points essentiels adaptés"
                                            AppLanguage.EN -> "adapted key checks"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { isQuickMode = false },
                                color = if (!isQuickMode) Emerald50 else Slate100,
                                border = BorderStroke(
                                    width = if (!isQuickMode) 2.dp else 1.dp,
                                    color = if (!isQuickMode) Emerald700 else Slate200
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🔍 " + Strings.fullInspection(language),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald900
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$fullModeItemCount " + when (language) {
                                            AppLanguage.AR -> "نقطة شاملة مكيفة"
                                            AppLanguage.FR -> "points complets adaptés"
                                            AppLanguage.EN -> "adapted full checks"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600
                                    )
                                }
                            }
                        }
                    }
                }

                // Optional free-text plate number or label (no validation, no decoding)
                item {
                    ConfigSectionCard(
                        title = when (language) {
                            AppLanguage.AR -> "7. رقم اللوحة أو علامة مميزة (اختياري)"
                            AppLanguage.FR -> "7. Immatriculation ou repère (optionnel)"
                            AppLanguage.EN -> "7. Plate Number or Label (Optional)"
                        }
                    ) {
                        Column {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "تسمية حرة لمساعدتك في التعرف على هذه السيارة ضمن الفحوصات المحفوظة (بدون تحقق أو فك تشفير)."
                                    AppLanguage.FR -> "Note libre pour reconnaître facilement cette voiture dans vos inspections sauvegardées."
                                    AppLanguage.EN -> "Optional free-text label to help you recognise this car in your saved inspections."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = optionalPlateLabel,
                                onValueChange = { optionalPlateLabel = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("optional_plate_label_input"),
                                placeholder = {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "مثال: 04821 118 16 أو سيارة البائع في البليدة"
                                            AppLanguage.FR -> "Ex: 04821 118 16 ou Clio grise Blida"
                                            AppLanguage.EN -> "e.g. 04821 118 16 or Grey Clio Blida"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Slate400
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = Emerald700,
                                    unfocusedBorderColor = Slate200,
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900
                                )
                            )
                        }
                    }
                }

                // Launch Inspection CTA Button
                item {
                    Button(
                        onClick = {
                            val finalBrand = selectedCar?.brand ?: customBrand.ifBlank { "Autre" }
                            val finalModel = selectedCar?.model ?: customModel.ifBlank { "Véhicule" }
                            val finalGen = selectedGeneration.ifBlank {
                                CarBodyType.entries.firstOrNull { it.name == selectedBodyType }?.getLabel(language)
                                    ?: selectedBodyType
                            }
                            onCarConfirmed(
                                finalBrand,
                                finalModel,
                                finalGen,
                                parsedYear,
                                selectedFuel,
                                selectedGearbox,
                                selectedEngine,
                                isQuickMode,
                                optionalPlateLabel.trim(),
                                parsedOdometerKm,
                                hasTurbo,
                                isFourWheelDrive,
                                selectedBodyType
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("confirm_start_inspection_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "ابدأ الفحص الميداني الآن"
                                AppLanguage.FR -> "Démarrer l'inspection sur place"
                                AppLanguage.EN -> "Launch On-Site Inspection Now"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CatalogCarCard(
    car: AlgerianCarModel,
    language: AppLanguage,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("car_item_${car.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    // Brand Monogram Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Emerald50)
                            .border(1.dp, Emerald200, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = car.brand.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald800
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${car.brand} ${car.model}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = car.generations.joinToString(" • ") { it.name },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald100
                ) {
                    Text(
                        text = "#${car.popularRank} DZ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Common Engines & Fuels Row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                car.commonEngines.take(2).forEach { eng ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = eng,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate700,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                if (car.availableFuels.contains("GPL")) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StatusWarningAmberContainer
                    ) {
                        Text(
                            text = "Compatible GPL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusWarningAmberOnContainer,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
