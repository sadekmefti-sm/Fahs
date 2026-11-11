package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.data.model.AlgerianCarCatalog
import com.example.data.model.AlgerianCarModel
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
        plateNumber: String
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
    var selectedFuel by remember { mutableStateOf("ESSENCE") }
    var selectedGearbox by remember { mutableStateOf("MANUELLE") }
    var selectedEngine by remember { mutableStateOf("") }
    var isQuickMode by remember { mutableStateOf(initialQuickMode) }
    var optionalPlateLabel by remember { mutableStateOf("") }

    // Custom "Other Car" states
    var customBrand by remember { mutableStateOf("") }
    var customModel by remember { mutableStateOf("") }
    var customBodyType by remember { mutableStateOf("Berline / Citadine") }

    val filteredModels = remember(searchQuery, selectedBrandFilter) {
        AlgerianCarCatalog.models.filter { car ->
            val matchesBrand = selectedBrandFilter == "All" ||
                car.brand.equals(selectedBrandFilter, ignoreCase = true)
            val matchesQuery = com.example.data.model.AlgerianCarCatalogExtra.matchesFuzzySearch(car, searchQuery)
            matchesBrand && matchesQuery
        }
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
                        color = Slate900
                    )
                    Text(
                        text = when {
                            selectedCar != null || isOtherCarMode -> when (language) {
                                AppLanguage.AR -> "الخطوة 2 من 2: حدد المحرك، الوقود ونوع الفحص"
                                AppLanguage.FR -> "Étape 2/2 : Motorisation, carburant et mode"
                                AppLanguage.EN -> "Step 2/2: Engine, fuel & inspection mode"
                            }
                            else -> when (language) {
                                AppLanguage.AR -> "الخطوة 1 من 2: أشهر 10 سيارات في السوق الجزائري أو بحث"
                                AppLanguage.FR -> "Étape 1/2 : Top 10 marché algérien ou recherche"
                                AppLanguage.EN -> "Step 1/2: Top 10 Algerian models or search"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
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
                                    AppLanguage.AR -> "الكل (Top 10)"
                                    AppLanguage.FR -> "Tous (Top 10 DZ)"
                                    AppLanguage.EN -> "All (Top 10 DZ)"
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
                            selectedFuel = car.availableFuels.first()
                            selectedGearbox = car.availableGearboxes.first()
                            selectedEngine = car.commonEngines.first()
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
                                selectedYear = 2016
                                selectedFuel = "ESSENCE"
                                selectedGearbox = "MANUELLE"
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
                                        AppLanguage.AR -> "أجب عن 4 أسئلة بسيطة (الوقود، علبة السرعة، السنة، الصنف) ليتكيف الفحص معها"
                                        AppLanguage.FR -> "Répondez à 4 questions (carburant, boîte, année, carrosserie) pour adapter l'inspection"
                                        AppLanguage.EN -> "Answer 4 quick questions (fuel, gearbox, year, body) to adapt the checklist"
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
                                        AppLanguage.AR -> "بيانات السيارة"
                                        AppLanguage.FR -> "Identité du véhicule"
                                        AppLanguage.EN -> "Vehicle Identity"
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
                                                AppLanguage.AR -> "العلامة (مثال: Toyota, Chery, Ford)"
                                                AppLanguage.FR -> "Marque (ex: Toyota, Chery, Ford)"
                                                AppLanguage.EN -> "Brand (e.g. Toyota, Chery, Ford)"
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
                                                AppLanguage.AR -> "الموديل (مثال: Yaris, Tiggo, Fiesta)"
                                                AppLanguage.FR -> "Modèle (ex: Yaris, Tiggo, Fiesta)"
                                                AppLanguage.EN -> "Model (e.g. Yaris, Tiggo, Fiesta)"
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
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

                    // Generation & Year Selector
                    item {
                        ConfigSectionCard(
                            title = when (language) {
                                AppLanguage.AR -> "1. الجيل وسنة الصنع"
                                AppLanguage.FR -> "1. Génération et Année"
                                AppLanguage.EN -> "1. Generation & Year"
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
                                            Column {
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

                // Fuel Type Selector (Essence, Diesel, GPL)
                item {
                    val fuels = selectedCar?.availableFuels ?: listOf("ESSENCE", "DIESEL", "GPL", "HYBRIDE")
                    ConfigSectionCard(
                        title = when (language) {
                            AppLanguage.AR -> "2. نوع الوقود (يكيف أسئلة المحرك والغاز)"
                            AppLanguage.FR -> "2. Carburant (adapte les contrôles moteur/GPL)"
                            AppLanguage.EN -> "2. Fuel Type (adapts engine & LPG checks)"
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
                                    onClick = { selectedFuel = fuel },
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
                            AppLanguage.AR -> "3. علبة السرعة (La Boîte)"
                            AppLanguage.FR -> "3. Boîte de vitesses"
                            AppLanguage.EN -> "3. Gearbox Type"
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

                // Engine or Body Type
                item {
                    val engines = selectedCar?.commonEngines ?: listOf("1.2 / 1.4 Essence", "1.6 MPI / GPL", "1.5 / 1.6 / 2.0 Diesel")
                    ConfigSectionCard(
                        title = when (language) {
                            AppLanguage.AR -> "4. المحرك (Motorisation)"
                            AppLanguage.FR -> "4. Motorisation"
                            AppLanguage.EN -> "4. Engine Variant"
                        }
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            engines.forEach { eng ->
                                val isSelected = selectedEngine == eng
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedEngine = eng },
                                    label = { Text(eng) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Slate900,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Inspection Depth Selector (Quick 10m vs Full 40m)
                item {
                    ConfigSectionCard(
                        title = when (language) {
                            AppLanguage.AR -> "5. مستوى الفحص"
                            AppLanguage.FR -> "5. Niveau d'inspection"
                            AppLanguage.EN -> "5. Inspection Depth"
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
                                        text = "15 " + when (language) {
                                            AppLanguage.AR -> "نقطة حرجة"
                                            AppLanguage.FR -> "points critiques"
                                            AppLanguage.EN -> "critical checks"
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
                                        text = "45 " + when (language) {
                                            AppLanguage.AR -> "نقطة شاملة"
                                            AppLanguage.FR -> "points complets"
                                            AppLanguage.EN -> "complete checks"
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
                            AppLanguage.AR -> "6. رقم اللوحة أو علامة مميزة (اختياري)"
                            AppLanguage.FR -> "6. Immatriculation ou repère (optionnel)"
                            AppLanguage.EN -> "6. Plate Number or Label (Optional)"
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
                            val finalGen = selectedGeneration.ifBlank { customBodyType }
                            onCarConfirmed(
                                finalBrand,
                                finalModel,
                                finalGen,
                                selectedYear,
                                selectedFuel,
                                selectedGearbox,
                                selectedEngine,
                                isQuickMode,
                                optionalPlateLabel.trim()
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Column {
                        Text(
                            text = "${car.brand} ${car.model}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = car.generations.joinToString(" • ") { it.name },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600,
                            maxLines = 1
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
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Common Engines & Fuels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
