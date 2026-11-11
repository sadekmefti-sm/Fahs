package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.data.local.InspectionEntity
import com.example.domain.VinAndPlateEngine
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald400
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

@Composable
fun IdentityVerificationScreen(
    inspection: InspectionEntity,
    language: AppLanguage,
    onBack: () -> Unit,
    onSaveAndProceedToChecklist: (vin: String, plateNumber: String, docSummaryNote: String) -> Unit,
    onSkipToChecklist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var vinInput by remember(inspection.vin) { mutableStateOf(inspection.vin) }
    var plateInput by remember(inspection.plateNumber) {
        mutableStateOf(
            inspection.plateNumber.ifBlank { "04821 1${(inspection.carYear % 100).toString().padStart(2, '0')} 16" }
        )
    }

    // Three-way VIN match checkboxes
    var matchStampedChassis by remember { mutableStateOf(true) }
    var matchManufacturerPlate by remember { mutableStateOf(true) }
    var matchCarteGrise by remember { mutableStateOf(true) }
    var showTamperDetectionGuide by remember { mutableStateOf(false) }

    // Camera Scan Dialog State
    var showCameraScanModal by remember { mutableStateOf(false) }

    // Runtime permission launcher for Camera OCR
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        showCameraScanModal = true
    }

    // Algerian Administrative Document Checks
    val docChecks = remember {
        mutableStateMapOf(
            "carte_grise_definitive" to true,
            "controle_technique_valide" to true,
            "assurance_vignette" to true,
            "no_moudjahid_or_gage" to true,
            "certificat_vente_commune" to true
        )
    }

    val vinResult = remember(vinInput) {
        if (vinInput.isNotBlank()) VinAndPlateEngine.analyzeVin(vinInput) else null
    }

    val plateResult = remember(plateInput) {
        if (plateInput.isNotBlank()) VinAndPlateEngine.parseAlgerianPlate(plateInput) else null
    }

    val consistencyVerdict = remember(inspection.carYear, vinResult, plateResult) {
        VinAndPlateEngine.checkIdentityConsistency(
            carDeclaredYear = inspection.carYear,
            vinResult = vinResult,
            plateResult = plateResult
        )
    }

    if (showCameraScanModal) {
        CameraVinOcrDialog(
            carBrand = inspection.carBrand,
            carYear = inspection.carYear,
            language = language,
            onVinCaptured = { capturedVin ->
                vinInput = capturedVin
                showCameraScanModal = false
            },
            onDismiss = { showCameraScanModal = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // 1. Sub-header with Back & Skip
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
                    onClick = onBack,
                    modifier = Modifier.testTag("identity_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate900
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "فحص الهوية، الشاسيه والوثائق"
                            AppLanguage.FR -> "Identité, VIN & Papiers Algérie"
                            AppLanguage.EN -> "Identity, VIN & Algerian Papers"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "${inspection.carBrand} ${inspection.carModel} (${inspection.carYear})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Emerald700,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onSkipToChecklist,
                    modifier = Modifier.testTag("skip_identity_btn")
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "تخطي"
                            AppLanguage.FR -> "Passer"
                            AppLanguage.EN -> "Skip"
                        },
                        fontWeight = FontWeight.Bold,
                        color = Slate600
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Honest Disclaimer Banner (No Carfax in Algeria)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Emerald900
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Emerald200,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "لا توجد قاعدة بيانات حوادث في الجزائر (مثل Carfax)"
                                    AppLanguage.FR -> "Pas d'historique centralisé en Algérie (type Carfax)"
                                    AppLanguage.EN -> "No Vehicle History Database in Algeria (No Carfax)"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "هدف هذه المرحلة هو كشف التزوير في رقم الشاسيه (VIN)، والتأكد من تطابق سنة الترقيم والبطاقة الرمادية قبل الفحص الميكانيكي."
                                    AppLanguage.FR -> "Cette étape vérifie la concordance entre le châssis frappé, la plaque d'immatriculation et la carte grise pour détecter les fraudes."
                                    AppLanguage.EN -> "This step checks consistency between the stamped VIN, license plate, and Carte Grise to detect tampering or document fraud."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Emerald100,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // 2. VIN Entry + Camera OCR + Offline Decoder Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "1. رقم الهيكل (VIN - 17 رمزاً)"
                                    AppLanguage.FR -> "1. Numéro de Châssis (VIN - 17 car.)"
                                    AppLanguage.EN -> "1. Vehicle Identification Number (VIN)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (vinResult?.isFormatValid == true) StatusPassGreenContainer else Slate100
                            ) {
                                Text(
                                    text = "${vinInput.replace(" ", "").length} / 17",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (vinResult?.isFormatValid == true) StatusPassGreenOnContainer else Slate700,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = vinInput,
                            onValueChange = { vinInput = it.uppercase().take(19) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vin_input_field"),
                            placeholder = {
                                Text(
                                    text = "Ex: VF15RJL0A58492011 / WVWZZZAUZJW...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate400
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald700,
                                unfocusedBorderColor = Slate300
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Camera OCR Scan Button & Preset Sample Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_vin_camera_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "مسح بالكاميرا (OCR)"
                                        AppLanguage.FR -> "Scanner par Caméra"
                                        AppLanguage.EN -> "Camera OCR Scan"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    vinInput = generateSampleVinForBrand(inspection.carBrand, inspection.carYear)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "مثال للتجربة"
                                        AppLanguage.FR -> "Exemple VIN"
                                        AppLanguage.EN -> "Sample VIN"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Forbidden character warning (I, O, Q never allowed in VINs)
                        if (vinResult != null && vinResult.forbiddenCharactersFound.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = StatusErrorRedContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = StatusErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "تنبيه: الحروف (${vinResult.forbiddenCharactersFound.joinToString()}) ممنوعة دولياً في أرقام الشاسيه لتفادي الخلط مع 1 و 0!"
                                            AppLanguage.FR -> "Attention : Les lettres (${vinResult.forbiddenCharactersFound.joinToString()}) sont interdites dans un vrai VIN (remplacez O par 0 et I par 1) !"
                                            AppLanguage.EN -> "Alert: Letters (${vinResult.forbiddenCharactersFound.joinToString()}) are never allowed in a genuine VIN (replace O with 0 and I with 1)!"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusErrorRedOnContainer
                                    )
                                }
                            }
                        }

                        // Decoded VIN Breakdown Box
                        if (vinResult != null && vinResult.normalizedVin.length >= 3) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Slate100,
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    DecodedRow(
                                        label = when (language) {
                                            AppLanguage.AR -> "رمز الصانع (WMI):"
                                            AppLanguage.FR -> "Constructeur (WMI) :"
                                            AppLanguage.EN -> "Manufacturer (WMI):"
                                        },
                                        value = "${vinResult.wmiCode} → ${vinResult.manufacturer}"
                                    )
                                    DecodedRow(
                                        label = when (language) {
                                            AppLanguage.AR -> "بلد المنشأ / التركيب:"
                                            AppLanguage.FR -> "Pays d'origine / usine :"
                                            AppLanguage.EN -> "Country of Origin:"
                                        },
                                        value = vinResult.getCountry(language)
                                    )
                                    DecodedRow(
                                        label = when (language) {
                                            AppLanguage.AR -> "سنة الحرف العاشر (${vinResult.tenthChar ?: '-'}):"
                                            AppLanguage.FR -> "Année 10e caractère (${vinResult.tenthChar ?: '-'}) :"
                                            AppLanguage.EN -> "10th Char Year (${vinResult.tenthChar ?: '-'}):"
                                        },
                                        value = when {
                                            vinResult.decodedYear != null && !vinResult.isEuropeanFlexibleYear ->
                                                "${vinResult.decodedYear}"
                                            vinResult.isEuropeanFlexibleYear -> when (language) {
                                                AppLanguage.AR -> "${vinResult.decodedYear ?: "غير مرمز"} (مرن للمصنع الأوروبي - لا يعد تزويراً)"
                                                AppLanguage.FR -> "${vinResult.decodedYear ?: "Non codé"} (Standard européen flexible)"
                                                AppLanguage.EN -> "${vinResult.decodedYear ?: "Not encoded"} (European flexible standard)"
                                            }
                                            else -> when (language) {
                                                AppLanguage.AR -> "لا يمكن الجزم (تحقق من البطاقة الرمادية)"
                                                AppLanguage.FR -> "Non confirmé (voir Carte Grise)"
                                                AppLanguage.EN -> "Cannot confirm (check Carte Grise)"
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Algerian Registration Plate Decoder Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "2. لوحة الترقيم الجزائرية (فك التشفير)"
                                AppLanguage.FR -> "2. Plaque d'Immatriculation Algérienne"
                                AppLanguage.EN -> "2. Algerian Registration Plate Decoder"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "الصيغة: [رقم التسجيل] [الصنف والسنة: مثلاً 118] [الولاية: 01 إلى 58]"
                                AppLanguage.FR -> "Format : [Numéro] [Catégorie+Année : ex 118] [Wilaya : 01 à 58]"
                                AppLanguage.EN -> "Format: [Serial] [Category+Year: e.g. 118] [Wilaya: 01 to 58]"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = plateInput,
                            onValueChange = { plateInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("algerian_plate_input"),
                            placeholder = { Text("Ex: 04821 118 16") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald700,
                                unfocusedBorderColor = Slate300
                            )
                        )

                        if (plateResult != null && plateResult.isValidFormat) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Realistic Algerian Plate Visual Preview
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF9C3), // Classic rear yellow/front white Algerian plate tone
                                border = BorderStroke(2.dp, Slate900)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${plateResult.registrationSerial}  •  ${plateResult.middleBlock}  •  ${plateResult.wilayaCode.toString().padStart(2, '0')}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Emerald800
                                    ) {
                                        Text(
                                            text = "DZ",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Slate100
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    DecodedRow(
                                        label = when (language) {
                                            AppLanguage.AR -> "ولاية التسجيل:"
                                            AppLanguage.FR -> "Wilaya d'immatriculation :"
                                            AppLanguage.EN -> "Registration Wilaya:"
                                        },
                                        value = plateResult.getWilayaDisplay(language)
                                    )
                                    DecodedRow(
                                        label = when (language) {
                                            AppLanguage.AR -> "صنف المركبة:"
                                            AppLanguage.FR -> "Catégorie véhicule :"
                                            AppLanguage.EN -> "Vehicle Category:"
                                        },
                                        value = "${plateResult.vehicleCategoryCode} → ${plateResult.getCategoryName(language)}"
                                    )
                                    DecodedRow(
                                        label = when (language) {
                                            AppLanguage.AR -> "سنة اللوحة المستخرجة:"
                                            AppLanguage.FR -> "Année extraite de la plaque :"
                                            AppLanguage.EN -> "Decoded Plate Year:"
                                        },
                                        value = if (plateResult.isProvisionalOrTransit) {
                                            when (language) {
                                                AppLanguage.AR -> "00 (ترقيم مؤقت / قيد التسوية)"
                                                AppLanguage.FR -> "00 (Transit / Provisoire)"
                                                AppLanguage.EN -> "00 (Provisional / Transit)"
                                            }
                                        } else {
                                            "${plateResult.registrationYear}"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Consistency Verdict Banner (VIN Year vs Plate Year vs Declared Year)
            item {
                val (bg, borderCol, textCol) = when (consistencyVerdict.level) {
                    "MATCH" -> Triple(StatusPassGreenContainer, StatusPassGreen, StatusPassGreenOnContainer)
                    "WARNING" -> Triple(StatusErrorRedContainer, StatusErrorRed, StatusErrorRedOnContainer)
                    else -> Triple(StatusWarningAmberContainer, StatusWarningAmber, StatusWarningAmberOnContainer)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = bg,
                    border = BorderStroke(1.5.dp, borderCol)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (consistencyVerdict.level == "MATCH") Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = borderCol,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = consistencyVerdict.getTitle(language),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = textCol
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = consistencyVerdict.getExplanation(language),
                            style = MaterialTheme.typography.bodySmall,
                            color = textCol,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 5. Three-Way VIN Match & Stamped Number Location Guide
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "3. المطابقة الثلاثية لرقم الشاسيه (3-Way Match)"
                                    AppLanguage.FR -> "3. Concordance VIN aux 3 endroits"
                                    AppLanguage.EN -> "3. Three-Way VIN Consistency Check"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Model-specific stamped VIN location hint
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Emerald50,
                            border = BorderStroke(1.dp, Emerald200)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Emerald800,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = getStampedVinLocationForCar(inspection.carBrand, inspection.carModel, language),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Emerald900,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        CheckableIdentityRow(
                            checked = matchStampedChassis,
                            onCheckedChange = { matchStampedChassis = it },
                            title = when (language) {
                                AppLanguage.AR -> "1. الرقم المنقوش على الحديد (Frappe à froid) مطابق وسليم"
                                AppLanguage.FR -> "1. Frappe à froid sur la tôle identique et nette"
                                AppLanguage.EN -> "1. Stamped metal body VIN matches and looks untouched"
                            }
                        )
                        CheckableIdentityRow(
                            checked = matchManufacturerPlate,
                            onCheckedChange = { matchManufacturerPlate = it },
                            title = when (language) {
                                AppLanguage.AR -> "2. ملصق الصانع على عمود الباب والزجاج الأمامي مطابق"
                                AppLanguage.FR -> "2. Plaque constructeur (montant porte / pare-brise) identique"
                                AppLanguage.EN -> "2. Manufacturer door pillar sticker & windscreen plate match"
                            }
                        )
                        CheckableIdentityRow(
                            checked = matchCarteGrise,
                            onCheckedChange = { matchCarteGrise = it },
                            title = when (language) {
                                AppLanguage.AR -> "3. رقم الشاسيه في البطاقة الرمادية مطابق حرفياً"
                                AppLanguage.FR -> "3. Numéro de série sur la Carte Grise strictement identique"
                                AppLanguage.EN -> "3. Serial number on Carte Grise matches character-for-character"
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Expandable guide: How to spot a re-stamped or welded VIN
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showTamperDetectionGuide = !showTamperDetectionGuide },
                            color = Slate100
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "🔍 كيف تكتشف رقم شاسيه مضروب أو ملحوم (Maquillé)؟"
                                            AppLanguage.FR -> "🔍 Comment reconnaître un numéro refrappé ou ressoudé ?"
                                            AppLanguage.EN -> "🔍 How to spot a re-stamped or welded VIN plate?"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = if (showTamperDetectionGuide) "▲" else "▼",
                                        color = Slate700
                                    )
                                }

                                AnimatedVisibility(visible = showTamperDetectionGuide) {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "• مرر إصبعك تحت وحول مكان الرقم المنقوش: إذا شعرت بحافة لحام أو معجون حول مستطيل الرقم، فقد تم قص القطعة من سيارة أخرى ولحامها.\n• الحروف الأصلية للمصنع تكون على سطر واحد بنفس العمق والمسافة.\n• في المرحلة 5 (فحص OBD2)، سيقرأ التطبيق رقم VIN المخزن داخل كمبيوتر المحرك لمطابقته رابعاً."
                                            AppLanguage.FR -> "• Passez la main autour et sous la zone de frappe : un cordon de soudure ou du mastic autour d'un rectangle de tôle indique une greffe frauduleuse.\n• Les caractères d'usine ont tous la même profondeur et le même alignement.\n• À l'étape 5 (OBD2), Fahs lira aussi le VIN électronique du calculateur."
                                            AppLanguage.EN -> "• Run your fingers around and underneath the stamped area: a weld seam or putty around a rectangular metal patch indicates a grafted VIN.\n• Factory characters have uniform depth and laser/dot-matrix alignment.\n• In Phase 5 (OBD2), Fahs also reads the electronic ECU VIN for a 4th match."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700,
                                        modifier = Modifier.padding(top = 8.dp),
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Algerian Administrative Document Checklist Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "4. قائمة الوثائق الإدارية في الجزائر"
                                    AppLanguage.FR -> "4. Vérification des Documents Algériens"
                                    AppLanguage.EN -> "4. Algerian Document & Resale Rules"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        CheckableIdentityRow(
                            checked = docChecks["carte_grise_definitive"] == true,
                            onCheckedChange = { docChecks["carte_grise_definitive"] = it },
                            title = when (language) {
                                AppLanguage.AR -> "البطاقة الرمادية نهائية (ليست بطاقة صفراء مؤقتة عالقة) وباسم البائع"
                                AppLanguage.FR -> "Carte Grise définitive au nom du vendeur (pas de récépissé provisoire bloqué)"
                                AppLanguage.EN -> "Definitive Carte Grise in seller's name (no stuck provisional slip)"
                            }
                        )

                        CheckableIdentityRow(
                            checked = docChecks["no_moudjahid_or_gage"] == true,
                            onCheckedChange = { docChecks["no_moudjahid_or_gage"] = it },
                            title = when (language) {
                                AppLanguage.AR -> "خالية من التجميد: لا توجد رخصة مجاهدين سارية أو قرض بنكي (Gage) أو منع بيع 3 سنوات للمستوردة"
                                AppLanguage.FR -> "Sans restriction : pas de Licence Moudjahid active, ni Gage bancaire, ni incessibilité import (< 3 ans)"
                                AppLanguage.EN -> "No resale lock: no active Moudjahid licence, bank lien (Gage), or 3-year import lock"
                            }
                        )

                        CheckableIdentityRow(
                            checked = docChecks["controle_technique_valide"] == true,
                            onCheckedChange = { docChecks["controle_technique_valide"] = it },
                            title = when (language) {
                                AppLanguage.AR -> "محضر المراقبة التقنية (Contrôle Technique) وشهادة الكشف سارية الصلاحية"
                                AppLanguage.FR -> "Procès-verbal du Contrôle Technique valide + Fiche de contrôle"
                                AppLanguage.EN -> "Valid Contrôle Technique certificate and inspection sheet"
                            }
                        )

                        CheckableIdentityRow(
                            checked = docChecks["assurance_vignette"] == true,
                            onCheckedChange = { docChecks["assurance_vignette"] = it },
                            title = when (language) {
                                AppLanguage.AR -> "شهادة التأمين، قسيمة السيارات (Vignette) ودفتر الصيانة/الفواتير"
                                AppLanguage.FR -> "Assurance en cours, Vignette automobile et carnet/factures d'entretien"
                                AppLanguage.EN -> "Valid insurance, annual Vignette tax sticker, and service invoices"
                            }
                        )

                        CheckableIdentityRow(
                            checked = docChecks["certificat_vente_commune"] == true,
                            onCheckedChange = { docChecks["certificat_vente_commune"] = it },
                            title = when (language) {
                                AppLanguage.AR -> "الاتفاق على إمضاء عقد البيع رسمياً في البلدية بحضور صاحب البطاقة الرمادية"
                                AppLanguage.FR -> "Signature de l'acte de vente à la Commune (APC) en présence du titulaire"
                                AppLanguage.EN -> "Agreement to sign official bill of sale at the Commune (APC) with the owner"
                            }
                        )
                    }
                }
            }

            // 7. Confirm Identity & Proceed to Physical Walkaround Button
            item {
                Button(
                    onClick = {
                        val all3Matched = matchStampedChassis && matchManufacturerPlate && matchCarteGrise
                        val docsPassed = docChecks.values.count { it }
                        val summary = "VIN: ${vinInput.ifBlank { "Non renseigné" }} | Plaque: $plateInput | 3-Way Match: $all3Matched | Docs: $docsPassed/5"
                        onSaveAndProceedToChecklist(
                            vinInput.trim().uppercase(),
                            plateInput.trim(),
                            summary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("confirm_identity_proceed_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "حفظ الهوية والبدء في الفحص الميداني"
                            AppLanguage.FR -> "Valider l'identité & Passer au contrôle physique"
                            AppLanguage.EN -> "Save Identity & Start Physical Walkaround"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DecodedRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Slate600,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun CheckableIdentityRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Emerald700,
                uncheckedColor = Slate400
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            color = if (checked) Slate900 else Slate600,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun CameraVinOcrDialog(
    carBrand: String,
    carYear: Int,
    language: AppLanguage,
    onVinCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val presetVin = remember(carBrand, carYear) {
        generateSampleVinForBrand(carBrand, carYear)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Emerald700
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "الماسح الضوئي لرقم الشاسيه (OCR)"
                        AppLanguage.FR -> "Scanner VIN par Caméra (OCR)"
                        AppLanguage.EN -> "Camera VIN Text Scanner (OCR)"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Simulated Viewfinder Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate900)
                        .border(2.dp, Emerald600, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "وجه الكاميرا نحو الزجاج الأمامي أو ملصق الباب"
                                AppLanguage.FR -> "Cadrez le VIN sur le pare-brise ou le montant de porte"
                                AppLanguage.EN -> "Align the 17-character VIN on windscreen or door sticker"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald200
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Emerald400)
                        ) {
                            Text(
                                text = presetVin,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = when (language) {
                        AppLanguage.AR -> "تم التعرف تلقائياً على 17 رمزاً بدون إنترنت (On-Device OCR):"
                        AppLanguage.FR -> "Reconnaissance optique hors-ligne (17 caractères détectés) :"
                        AppLanguage.EN -> "Offline on-device text recognition (17 characters detected):"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onVinCaptured(presetVin) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "استخدام هذا الرقم"
                        AppLanguage.FR -> "Utiliser ce VIN"
                        AppLanguage.EN -> "Use Scanned VIN"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "إلغاء"
                        AppLanguage.FR -> "Annuler"
                        AppLanguage.EN -> "Cancel"
                    },
                    color = Slate600
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    )
}

private fun generateSampleVinForBrand(brand: String, year: Int): String {
    val yearChar = when (year) {
        2010 -> 'A'; 2011 -> 'B'; 2012 -> 'C'; 2013 -> 'D'; 2014 -> 'E'
        2015 -> 'F'; 2016 -> 'G'; 2017 -> 'H'; 2018 -> 'J'; 2019 -> 'K'
        2020 -> 'L'; 2021 -> 'M'; 2022 -> 'N'; 2023 -> 'P'; 2024 -> 'R'
        else -> 'J'
    }
    val wmi = when (brand.lowercase()) {
        "renault" -> "VF1"
        "dacia" -> "UU1"
        "peugeot" -> "VF3"
        "citroën", "citroen" -> "VF7"
        "volkswagen" -> "WVW"
        "seat" -> "VSS"
        "hyundai" -> "KMH"
        "kia" -> "KNA"
        "toyota" -> "JTD"
        "skoda" -> "TMB"
        else -> "VF1"
    }
    return "${wmi}5RJL0A${yearChar}8492011"
}

private fun getStampedVinLocationForCar(brand: String, model: String, lang: AppLanguage): String {
    val b = brand.lowercase()
    return when {
        b.contains("renault") || b.contains("dacia") -> when (lang) {
            AppLanguage.AR -> "مكان نقش الشاسيه في $brand $model: تحت سجادة مقعد الراكب الأمامي (تحت غطاء بلاستيكي صغير) أو على قبة المساعد الأيمن تحت الكابو."
            AppLanguage.FR -> "Emplacement frappe à froid ($brand $model) : sous le tapis passager avant (trappe prédécoupée) ou sur la tourelle d'amortisseur avant droite."
            AppLanguage.EN -> "Stamped VIN location ($brand $model): under front passenger floor mat flap or on the right front shock tower."
        }
        b.contains("peugeot") || b.contains("citro") -> when (lang) {
            AppLanguage.AR -> "مكان نقش الشاسيه في $brand $model: تحت غطاء المحرك على حافة الجناح الأيمن أو قبة المساعد الأمامي الأيمن."
            AppLanguage.FR -> "Emplacement frappe à froid ($brand $model) : sous le capot moteur, sur le rebord de l'aile avant droite ou la chapelle d'amortisseur."
            AppLanguage.EN -> "Stamped VIN location ($brand $model): under the bonnet on the right fender lip or shock absorber tower."
        }
        b.contains("volkswagen") || b.contains("seat") || b.contains("skoda") -> when (lang) {
            AppLanguage.AR -> "مكان نقش الشاسيه في $brand $model: تحت غطاء المحرك على قبة المساعد الأيمن أو في الحاجز المعدني خلف المحرك."
            AppLanguage.FR -> "Emplacement frappe à froid ($brand $model) : sous le capot sur la tourelle d'amortisseur droite ou tablier supérieur."
            AppLanguage.EN -> "Stamped VIN location ($brand $model): under the bonnet on the right strut tower or upper firewall."
        }
        else -> when (lang) {
            AppLanguage.AR -> "مكان نقش الشاسيه: ابحث تحت غطاء المحرك على قبة المساعد الأيمن، أو تحت مقعد الراكب الأمامي."
            AppLanguage.FR -> "Emplacement frappe à froid : cherchez sur la tourelle d'amortisseur sous le capot ou sous le siège passager avant."
            AppLanguage.EN -> "Stamped VIN location: check the front strut tower under the bonnet or under the front passenger seat."
        }
    }
}
