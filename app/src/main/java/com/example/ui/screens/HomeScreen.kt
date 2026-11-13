package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LanguagePreferences
import com.example.data.local.InspectionEntity
import com.example.data.repository.ShopSettings
import com.example.ui.components.DongleVectorIllustration
import com.example.ui.components.StatusBadge
import com.example.ui.theme.FahsBrand
import com.example.ui.theme.FahsBrandPressed
import com.example.ui.theme.FahsBrandSoft
import com.example.ui.theme.FahsHairline
import com.example.ui.theme.FahsInk
import com.example.ui.theme.FahsMutedText
import com.example.ui.viewmodel.OptionalCarDraft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class HomeSheetStep {
    NONE,
    ASK_HAS_DONGLE,
    OFFER_GET_DONGLE,
    MANUAL_MODE_PICKER_FROM_NO_DONGLE,
    MANUAL_MODE_PICKER_DIRECT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    language: AppLanguage,
    inspections: List<InspectionEntity>,
    completedCount: Int = 0,
    inProgressCount: Int = 0,
    shopSettings: ShopSettings = ShopSettings(),
    optionalCarDraft: OptionalCarDraft? = null,
    onChooseOptionalCarModel: () -> Unit = {},
    onClearOptionalCarModel: () -> Unit = {},
    onProceedToAutomaticScan: (autoConnectRemembered: Boolean) -> Unit = {},
    onStartManualInspectionDirectly: (isQuick: Boolean) -> Unit = {},
    onOpenDongleOrderPage: () -> Unit = {},
    onSelectInspection: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSheetStep by remember { mutableStateOf(HomeSheetStep.NONE) }

    val lastInProgressInspection = remember(inspections) {
        inspections.firstOrNull { it.status.equals("IN_PROGRESS", ignoreCase = true) }
    }

    // Bottom sheet for the Automatic Path dongle gate and Manual Path Quick/Full picker
    if (activeSheetStep != HomeSheetStep.NONE) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { activeSheetStep = HomeSheetStep.NONE },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.testTag("home_flow_bottom_sheet")
        ) {
            AnimatedContent(
                targetState = activeSheetStep,
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(150))
                },
                label = "home_sheet_step"
            ) { step ->
                when (step) {
                    HomeSheetStep.ASK_HAS_DONGLE -> {
                        AskHasDongleSheetContent(
                            language = language,
                            onYesHasDongle = {
                                activeSheetStep = HomeSheetStep.NONE
                                onProceedToAutomaticScan(false)
                            },
                            onNoDongle = {
                                activeSheetStep = HomeSheetStep.OFFER_GET_DONGLE
                            }
                        )
                    }

                    HomeSheetStep.OFFER_GET_DONGLE -> {
                        OfferGetDongleSheetContent(
                            language = language,
                            shopSettings = shopSettings,
                            onYesShowMe = {
                                activeSheetStep = HomeSheetStep.NONE
                                onOpenDongleOrderPage()
                            },
                            onNotNowGoManual = {
                                activeSheetStep = HomeSheetStep.MANUAL_MODE_PICKER_FROM_NO_DONGLE
                            }
                        )
                    }

                    HomeSheetStep.MANUAL_MODE_PICKER_FROM_NO_DONGLE -> {
                        ManualModePickerSheetContent(
                            language = language,
                            showNoProblemReassurance = true,
                            optionalCarDraft = optionalCarDraft,
                            onChooseOptionalCarModel = {
                                activeSheetStep = HomeSheetStep.NONE
                                onChooseOptionalCarModel()
                            },
                            onStartManual = { isQuick ->
                                activeSheetStep = HomeSheetStep.NONE
                                onStartManualInspectionDirectly(isQuick)
                            }
                        )
                    }

                    HomeSheetStep.MANUAL_MODE_PICKER_DIRECT -> {
                        ManualModePickerSheetContent(
                            language = language,
                            showNoProblemReassurance = false,
                            optionalCarDraft = optionalCarDraft,
                            onChooseOptionalCarModel = {
                                activeSheetStep = HomeSheetStep.NONE
                                onChooseOptionalCarModel()
                            },
                            onStartManual = { isQuick ->
                                activeSheetStep = HomeSheetStep.NONE
                                onStartManualInspectionDirectly(isQuick)
                            }
                        )
                    }

                    HomeSheetStep.NONE -> {}
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Quiet Top Row: Fahs wordmark + Optional car model link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (language) {
                    AppLanguage.AR -> "فحص · Fahs"
                    AppLanguage.FR -> "Fahs"
                    AppLanguage.EN -> "Fahs"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FahsBrand
            )

            // Optional car model link / selected car pill
            if (optionalCarDraft == null) {
                Surface(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable { onChooseOptionalCarModel() }
                        .testTag("choose_car_optional_link"),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, FahsHairline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = FahsMutedText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "اختيار موديل السيارة (اختياري)"
                                AppLanguage.FR -> "Choisir le modèle (optionnel)"
                                AppLanguage.EN -> "Choose the car model (optional)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = FahsMutedText
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable { onChooseOptionalCarModel() }
                        .testTag("selected_optional_car_pill"),
                    shape = RoundedCornerShape(24.dp),
                    color = FahsBrandSoft,
                    border = BorderStroke(1.dp, FahsBrand)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = FahsInk,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${optionalCarDraft.brand} ${optionalCarDraft.model}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = FahsInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = onClearOptionalCarModel,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("clear_optional_car_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = when (language) {
                                    AppLanguage.AR -> "إزالة السيارة المختارة"
                                    AppLanguage.FR -> "Retirer le modèle choisi"
                                    AppLanguage.EN -> "Clear chosen car"
                                },
                                tint = FahsInk,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Single Main Question
        Text(
            text = when (language) {
                AppLanguage.AR -> "كيف تريد فحص السيارة؟"
                AppLanguage.FR -> "Comment voulez-vous inspecter la voiture ?"
                AppLanguage.EN -> "How do you want to inspect the car?"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("home_main_question")
        )

        // 3. Primary Large Button: Automatic Scan (Recommended)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val rememberedDongle = LanguagePreferences.getLastSuccessfulDongleAddress(context)
                    if (!rememberedDongle.isNullOrBlank()) {
                        onProceedToAutomaticScan(true)
                    } else {
                        activeSheetStep = HomeSheetStep.ASK_HAS_DONGLE
                    }
                }
                .testTag("automatic_scan_button"),
            shape = RoundedCornerShape(24.dp),
            color = FahsBrand
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DongleVectorIllustration(
                        size = 56.dp,
                        strokeColor = Color.White,
                        accentColor = Color(0xFFD7EFE7)
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = FahsBrandPressed
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "موصى به · دقيقتان، يحتاج فيشة OBD"
                                AppLanguage.FR -> "Recommandé · 2 minutes, nécessite un boîtier OBD"
                                AppLanguage.EN -> "Recommended · 2 minutes, needs an OBD dongle"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "فحص أوتوماتيكي"
                            AppLanguage.FR -> "Scan automatique"
                            AppLanguage.EN -> "Automatic scan"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "يفحص كمبيوتر المحرك، الأعطال المسجلة، مسح الذاكرة الحديث، الحرارة والشحن بضغطة واحدة."
                            AppLanguage.FR -> "Vérifie le calculateur moteur, les défauts enregistrés, les codes effacés, la température et la charge en un geste."
                            AppLanguage.EN -> "Checks the engine computer, stored faults, recently erased codes, temperature, and charging in one tap."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFD7EFE7)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "ابدأ الفحص الأوتوماتيكي"
                            AppLanguage.FR -> "Lancer le scan automatique"
                            AppLanguage.EN -> "Start automatic scan"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 4. Secondary Large Button: Manual Inspection
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    activeSheetStep = HomeSheetStep.MANUAL_MODE_PICKER_DIRECT
                }
                .testTag("start_inspection_button"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, FahsHairline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "قائمة فحص موجهة، بدون معدات"
                        AppLanguage.FR -> "Liste guidée, sans matériel"
                        AppLanguage.EN -> "Guided checklist, no equipment needed"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = FahsMutedText
                )

                Text(
                    text = when (language) {
                        AppLanguage.AR -> "فحص يدوي"
                        AppLanguage.FR -> "Inspection manuelle"
                        AppLanguage.EN -> "Manual inspection"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = when (language) {
                        AppLanguage.AR -> "معاينة الوثائق، الهيكل والصبيغة، أسفل السيارة، والتجربة على الطريق سؤالاً بسؤال."
                        AppLanguage.FR -> "Papiers, carrosserie, peinture, soubassement et essai sur route, question par question."
                        AppLanguage.EN -> "Paperwork, bodywork, paint, underbody, and road test, question by question."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = FahsMutedText
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "بدء الفحص اليدوي"
                            AppLanguage.FR -> "Commencer l'inspection manuelle"
                            AppLanguage.EN -> "Start manual inspection"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FahsBrand
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = FahsBrand,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 5. Single small line below: "Continue my last inspection" — shown ONLY when an inspection is in progress
        if (lastInProgressInspection != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { onSelectInspection(lastInProgressInspection.id) },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("continue_last_inspection_link")
                ) {
                    val carLabel = "${lastInProgressInspection.carBrand} ${lastInProgressInspection.carModel}".trim()
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "متابعة آخر فحص لي ($carLabel)"
                            AppLanguage.FR -> "Continuer ma dernière inspection ($carLabel)"
                            AppLanguage.EN -> "Continue my last inspection ($carLabel)"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = FahsBrand,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun AskHasDongleSheetContent(
    language: AppLanguage,
    onYesHasDongle: () -> Unit,
    onNoDongle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        DongleVectorIllustration(
            size = 96.dp,
            strokeColor = FahsInk,
            accentColor = FahsBrand
        )

        Text(
            text = when (language) {
                AppLanguage.AR -> "هل لديك فيشة فحص OBD؟"
                AppLanguage.FR -> "Avez-vous un boîtier OBD ?"
                AppLanguage.EN -> "Do you have an OBD dongle?"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("ask_has_dongle_title")
        )

        Text(
            text = when (language) {
                AppLanguage.AR -> "قطعة بلوتوث صغيرة تركب في منفذ السيارة تحت المقود لقراءة كمبيوتر المحرك."
                AppLanguage.FR -> "Un petit boîtier Bluetooth qui se branche sur la prise sous le volant pour lire le calculateur moteur."
                AppLanguage.EN -> "A small Bluetooth plug that connects under the steering wheel to read the engine computer."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = FahsMutedText,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onYesHasDongle,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("has_dongle_yes_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FahsBrand)
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "نعم"
                        AppLanguage.FR -> "Oui"
                        AppLanguage.EN -> "Yes"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onNoDongle,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("has_dongle_no_btn"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, FahsHairline)
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "لا"
                        AppLanguage.FR -> "Non"
                        AppLanguage.EN -> "No"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun OfferGetDongleSheetContent(
    language: AppLanguage,
    shopSettings: ShopSettings,
    onYesShowMe: () -> Unit,
    onNotNowGoManual: () -> Unit
) {
    val priceText = shopSettings.formattedDonglePrice()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DongleVectorIllustration(
            size = 84.dp,
            strokeColor = FahsInk,
            accentColor = FahsBrand
        )

        Text(
            text = when (language) {
                AppLanguage.AR -> "هل ترغب في الحصول على واحدة؟"
                AppLanguage.FR -> "Voulez-vous vous en procurer un ?"
                AppLanguage.EN -> "Would you like to get one?"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("offer_get_dongle_title")
        )

        Text(
            text = when (language) {
                AppLanguage.AR -> "تتيح لك فيشة الفحص قراءة أعطال المحرك وكشف مسح الذاكرة الحديث في دقيقتين قبل شراء أي سيارة. السعر: $priceText مع التوصيل."
                AppLanguage.FR -> "Ce petit boîtier permet de lire les défauts du moteur et de vérifier si la mémoire a été effacée récemment en 2 minutes. Prix : $priceText avec livraison."
                AppLanguage.EN -> "This small plug reads engine faults and checks if fault memory was recently erased in 2 minutes. Price: $priceText with delivery."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = FahsMutedText,
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onYesShowMe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("offer_dongle_yes_show_me_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FahsBrand)
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "نعم، اعرض لي التفاصيل"
                        AppLanguage.FR -> "Oui, voir l'offre"
                        AppLanguage.EN -> "Yes, show me"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onNotNowGoManual,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("offer_dongle_not_now_btn"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, FahsHairline)
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "ليس الآن"
                        AppLanguage.FR -> "Pas maintenant"
                        AppLanguage.EN -> "Not now"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ManualModePickerSheetContent(
    language: AppLanguage,
    showNoProblemReassurance: Boolean,
    optionalCarDraft: OptionalCarDraft?,
    onChooseOptionalCarModel: () -> Unit,
    onStartManual: (isQuick: Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showNoProblemReassurance) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("no_problem_manual_reassurance_banner"),
                shape = RoundedCornerShape(14.dp),
                color = FahsBrandSoft
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "لا مشكلة، يمكنك فحص السيارة يدوياً."
                        AppLanguage.FR -> "Pas de problème, vous pouvez inspecter la voiture manuellement."
                        AppLanguage.EN -> "No problem, you can inspect the car manually."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = FahsInk,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Text(
            text = when (language) {
                AppLanguage.AR -> "اختر مدة الفحص اليدوي"
                AppLanguage.FR -> "Choisissez le format de l'inspection manuelle"
                AppLanguage.EN -> "Choose the manual inspection format"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Optional car model row inside manual start sheet (never blocks starting)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChooseOptionalCarModel() }
                .padding(vertical = 4.dp)
                .testTag("sheet_choose_car_optional_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (optionalCarDraft != null) {
                    when (language) {
                        AppLanguage.AR -> "السيارة المختارة: ${optionalCarDraft.brand} ${optionalCarDraft.model}"
                        AppLanguage.FR -> "Voiture choisie : ${optionalCarDraft.brand} ${optionalCarDraft.model}"
                        AppLanguage.EN -> "Chosen car: ${optionalCarDraft.brand} ${optionalCarDraft.model}"
                    }
                } else {
                    when (language) {
                        AppLanguage.AR -> "اختيار موديل السيارة (اختياري)"
                        AppLanguage.FR -> "Choisir le modèle de la voiture (optionnel)"
                        AppLanguage.EN -> "Choose the car model (optional)"
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = FahsBrand,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = when (language) {
                    AppLanguage.AR -> "تغيير"
                    AppLanguage.FR -> "Modifier"
                    AppLanguage.EN -> "Change"
                },
                style = MaterialTheme.typography.bodySmall,
                color = FahsMutedText
            )
        }

        HorizontalDivider(color = FahsHairline, thickness = 1.dp)

        // Option 1: Quick Check
        Button(
            onClick = { onStartManual(true) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .testTag("start_quick_manual_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FahsBrand)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "فحص سريع (حوالي 10 دقائق)"
                        AppLanguage.FR -> "Contrôle rapide (environ 10 minutes)"
                        AppLanguage.EN -> "Quick check (about 10 minutes)"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "أهم النقاط الأساسية في الوثائق والهيكل والمحرك"
                        AppLanguage.FR -> "Les points essentiels : papiers, carrosserie et moteur"
                        AppLanguage.EN -> "The essential points: paperwork, bodywork, and engine"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD7EFE7)
                )
            }
        }

        // Option 2: Full Check
        OutlinedButton(
            onClick = { onStartManual(false) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .testTag("start_full_manual_btn"),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, FahsHairline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "فحص شامل (حوالي 30 دقيقة)"
                        AppLanguage.FR -> "Inspection complète (environ 30 minutes)"
                        AppLanguage.EN -> "Full inspection (about 30 minutes)"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "جميع نقاط المعاينة بالتفصيل مع التجربة على الطريق"
                        AppLanguage.FR -> "Tous les points de contrôle en détail avec essai sur route"
                        AppLanguage.EN -> "All inspection points in detail including road test"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = FahsMutedText
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InspectionCardItem(
    inspection: InspectionEntity,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateStr = remember(inspection.createdAt) { dateFormat.format(Date(inspection.createdAt)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, FahsHairline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "${inspection.carBrand} ${inspection.carModel}".trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val plateSuffix = if (inspection.plateNumber.isNotBlank()) " · ${inspection.plateNumber}" else ""
                    Text(
                        text = "${inspection.carGeneration.ifBlank { inspection.carYear.toString() }} · ${inspection.engineDescription.ifBlank { inspection.fuelType }}$plateSuffix",
                        style = MaterialTheme.typography.bodySmall,
                        color = FahsMutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusBadge(status = inspection.status, language = language)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = inspection.fuelType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = inspection.gearboxType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = FahsMutedText
                )
            }
        }
    }
}
