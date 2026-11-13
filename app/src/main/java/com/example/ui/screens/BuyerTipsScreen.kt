package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.data.model.AlgerianCarCatalog
import com.example.data.model.AlgerianCarCatalogExtra
import com.example.data.model.ItemSeverity
import com.example.data.repository.ShopSettings
import com.example.domain.ObdDiagnosticEngine
import com.example.ui.components.DongleBuyingGuideContent
import com.example.ui.components.DongleVectorIllustration
import com.example.ui.components.ObdPortUnderSteeringVectorIllustration
import com.example.ui.theme.FahsBrand
import com.example.ui.theme.FahsBrandSoft
import com.example.ui.theme.FahsHairline
import com.example.ui.theme.FahsInk
import com.example.ui.theme.FahsMutedText
import com.example.ui.theme.ResultCheckOrange
import com.example.ui.theme.ResultNormalGreen
import com.example.ui.theme.ResultProblemRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuyerTipsScreen(
    language: AppLanguage,
    shopSettings: ShopSettings = ShopSettings(),
    initialSection: String = "HOW_TO_USE_DONGLE",
    modifier: Modifier = Modifier
) {
    // Sections in Guide tab:
    // HOW_TO_USE_DONGLE (How to use a dongle & where the OBD port is)
    // TIPS (Buyer tips)
    // WEAK_POINTS (Model tips & OBD port location per car)
    // ORDER_DONGLE (Order an OBD dongle)
    // DTC_DICTIONARY (Offline fault code explanations)
    var selectedSection by remember(initialSection) { mutableStateOf(initialSection) }
    var dtcSearchQuery by remember { mutableStateOf("") }
    var modelSearchQuery by remember { mutableStateOf("") }

    val allDtcList = remember {
        ObdDiagnosticEngine.offlineDtcDatabase.values.toList()
    }
    val filteredDtcList = remember(dtcSearchQuery, allDtcList) {
        val q = dtcSearchQuery.trim().lowercase()
        if (q.isEmpty()) allDtcList
        else allDtcList.filter {
            it.code.lowercase().contains(q) ||
                it.titleFr.lowercase().contains(q) ||
                it.titleAr.contains(q) ||
                it.titleEn.lowercase().contains(q) ||
                it.plainExplanationFr.lowercase().contains(q) ||
                it.plainExplanationAr.contains(q)
        }
    }

    val filteredModels = remember(modelSearchQuery) {
        AlgerianCarCatalog.models.filter { car ->
            AlgerianCarCatalogExtra.matchesFuzzySearch(car, modelSearchQuery)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("guide_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quiet Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "دليل الفحص والشراء"
                        AppLanguage.FR -> "Guide d'inspection et d'achat"
                        AppLanguage.EN -> "Inspection and buying guide"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "طريقة استعمال الفيشة، مكان منفذ OBD، نصائح الشراء وطلب الفيشة."
                        AppLanguage.FR -> "Utiliser un boîtier, trouver la prise OBD, conseils d'achat et commande."
                        AppLanguage.EN -> "How to use a dongle, where the OBD port is, buyer tips, and ordering."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = FahsMutedText
                )
            }
        }

        // Section Switcher Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSection == "HOW_TO_USE_DONGLE",
                    onClick = { selectedSection = "HOW_TO_USE_DONGLE" },
                    label = {
                        Text(
                            when (language) {
                                AppLanguage.AR -> "استعمال الفيشة ومكان المنفذ"
                                AppLanguage.FR -> "Utiliser le boîtier & prise OBD"
                                AppLanguage.EN -> "How to use & OBD port"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FahsBrand,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("guide_tab_how_to_use")
                )
                FilterChip(
                    selected = selectedSection == "TIPS",
                    onClick = { selectedSection = "TIPS" },
                    label = {
                        Text(
                            when (language) {
                                AppLanguage.AR -> "نصائح الشراء"
                                AppLanguage.FR -> "Conseils d'achat"
                                AppLanguage.EN -> "Buyer tips"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FahsBrand,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("guide_tab_tips")
                )
                FilterChip(
                    selected = selectedSection == "WEAK_POINTS",
                    onClick = { selectedSection = "WEAK_POINTS" },
                    label = {
                        Text(
                            when (language) {
                                AppLanguage.AR -> "نصائح حسب السيارة (${AlgerianCarCatalog.models.size})"
                                AppLanguage.FR -> "Modèles & prises (${AlgerianCarCatalog.models.size})"
                                AppLanguage.EN -> "Cars & OBD ports (${AlgerianCarCatalog.models.size})"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FahsBrand,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("guide_tab_weak_points")
                )
                FilterChip(
                    selected = selectedSection == "ORDER_DONGLE",
                    onClick = { selectedSection = "ORDER_DONGLE" },
                    label = {
                        Text(
                            when (language) {
                                AppLanguage.AR -> "طلب فيشة OBD"
                                AppLanguage.FR -> "Commander un boîtier"
                                AppLanguage.EN -> "Order a dongle"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FahsBrand,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("guide_tab_order_dongle")
                )
                FilterChip(
                    selected = selectedSection == "DTC_DICTIONARY",
                    onClick = { selectedSection = "DTC_DICTIONARY" },
                    label = {
                        Text(
                            when (language) {
                                AppLanguage.AR -> "شرح الأعطال (${allDtcList.size})"
                                AppLanguage.FR -> "Défauts moteur (${allDtcList.size})"
                                AppLanguage.EN -> "Fault dictionary (${allDtcList.size})"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FahsBrand,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("guide_tab_dtc_dict")
                )
            }
        }

        when (selectedSection) {
            "HOW_TO_USE_DONGLE" -> {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, FahsHairline)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ObdPortUnderSteeringVectorIllustration(size = 76.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "أين أجد منفذ الفحص في السيارة؟"
                                            AppLanguage.FR -> "Où se trouve la prise dans la voiture ?"
                                            AppLanguage.EN -> "Where is the port in the car?"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "في أغلب السيارات، يوجد المنفذ تحت المقود بالقرب من الدواسات، أو خلف غطاء بلاستيكي صغير بجانب علبة الفيوزات أو منفضة السجائر."
                                            AppLanguage.FR -> "Sur la plupart des voitures, la prise se trouve sous le volant près des pédales, ou derrière un petit cache près des fusibles ou de la console centrale."
                                            AppLanguage.EN -> "On most cars, the port is under the steering wheel near the pedals, or behind a small plastic cover near the fuse box or center console."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FahsMutedText
                                    )
                                }
                            }

                            HorizontalDivider(color = FahsHairline, thickness = 1.dp)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                DongleVectorIllustration(size = 72.dp, isConnected = true)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "الخطوات الثلاث للفحص الأوتوماتيكي"
                                            AppLanguage.FR -> "Les 3 étapes pour lancer le scan"
                                            AppLanguage.EN -> "The 3 steps to run the scan"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "1. أدخل الفيشة في منفذ السيارة.\n2. افتح الكونتاكت وشغّل المحرك.\n3. اضغط على «فحص أوتوماتيكي» في الصفحة الرئيسية."
                                            AppLanguage.FR -> "1. Branchez le boîtier sur la prise sous le volant.\n2. Mettez le contact et démarrez le moteur.\n3. Appuyez sur « Scan automatique » sur l'écran d'accueil."
                                            AppLanguage.EN -> "1. Plug the dongle into the port under the steering wheel.\n2. Turn the key and start the engine.\n3. Tap 'Automatic scan' on the Home screen."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "ORDER_DONGLE" -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        DongleBuyingGuideContent(
                            language = language,
                            shopSettings = shopSettings
                        )
                    }
                }
            }

            "WEAK_POINTS" -> {
                item {
                    OutlinedTextField(
                        value = modelSearchQuery,
                        onValueChange = { modelSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weak_points_search_input"),
                        placeholder = {
                            Text(
                                when (language) {
                                    AppLanguage.AR -> "ابحث عن سيارة (مثال: سامبول، كليو، غولف، 208، أكسنت...)"
                                    AppLanguage.FR -> "Rechercher un modèle (ex: Symbol, Clio 4, Golf 7, 208, Hilux...)"
                                    AppLanguage.EN -> "Search model (e.g., Symbol, Clio 4, Golf 7, 208, Hilux...)"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = FahsBrand,
                            unfocusedBorderColor = FahsHairline
                        )
                    )
                }

                items(filteredModels.size) { index ->
                    val car = filteredModels[index]
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, FahsHairline)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = FahsBrand,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${car.brand} ${car.model}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FahsBrandSoft
                                ) {
                                    Text(
                                        text = car.commonEngines.firstOrNull() ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FahsInk,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "نقطة المراقبة الرئيسية: ${car.keyWeakPointAr}"
                                    AppLanguage.FR -> "Point à surveiller : ${car.keyWeakPointFr}"
                                    AppLanguage.EN -> "Main point to check: ${car.keyWeakPointEn}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            HorizontalDivider(color = FahsHairline, thickness = 1.dp)

                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "مكان منفذ الفحص: ${car.obdPortHintAr}"
                                    AppLanguage.FR -> "Emplacement prise OBD : ${car.obdPortHintFr}"
                                    AppLanguage.EN -> "OBD port location: ${car.obdPortHintEn}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = FahsMutedText
                            )
                        }
                    }
                }
            }

            "DTC_DICTIONARY" -> {
                item {
                    OutlinedTextField(
                        value = dtcSearchQuery,
                        onValueChange = { dtcSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dtc_dictionary_search_input"),
                        placeholder = {
                            Text(
                                when (language) {
                                    AppLanguage.AR -> "ابحث عن عطل (مثال: تيربو، حاقن، P0420...)"
                                    AppLanguage.FR -> "Rechercher un défaut (ex: turbo, injecteur, P0420...)"
                                    AppLanguage.EN -> "Search fault (e.g., turbo, injector, P0420...)"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = FahsBrand,
                            unfocusedBorderColor = FahsHairline
                        )
                    )
                }

                items(filteredDtcList.size) { index ->
                    val dtc = filteredDtcList[index]
                    val badgeColor = when (dtc.severity) {
                        ItemSeverity.CRITICAL -> ResultProblemRed
                        ItemSeverity.IMPORTANT -> ResultCheckOrange
                        ItemSeverity.MINOR -> ResultNormalGreen
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, FahsHairline)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = badgeColor.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "${dtc.code} · ${dtc.severity.getLabel(language)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = dtc.getTitle(language),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = dtc.getExplanation(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = FahsMutedText
                            )
                        }
                    }
                }
            }

            else -> {
                item {
                    BuyerTipCard(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        iconColor = FahsBrand,
                        title = Strings.tip1Title(language),
                        content = Strings.tip1Desc(language),
                        actionNotice = when (language) {
                            AppLanguage.AR -> "تنبيه: لا تدفع عربوناً أبداً إذا كانت البطاقة الرمادية مؤقتة أو باسم شخص غير حاضر بدون وكالة موثقة."
                            AppLanguage.FR -> "Conseil : Ne versez jamais d'acompte si la carte grise est provisoire ou au nom d'un tiers sans procuration notariée."
                            AppLanguage.EN -> "Tip: Never pay a deposit if the registration card is temporary or in someone else's name without a notarized power of attorney."
                        }
                    )
                }

                item {
                    BuyerTipCard(
                        icon = Icons.Default.Speed,
                        iconColor = ResultCheckOrange,
                        title = Strings.tip2Title(language),
                        content = Strings.tip2Desc(language),
                        actionNotice = when (language) {
                            AppLanguage.AR -> "لاحظ لون الدخان: أبيض كثيف (ماء التبريد)، أزرق (حرق زيت المحرك)، أسود كثيف (نظام الوقود)."
                            AppLanguage.FR -> "Observez la fumée : blanche épaisse (liquide de refroidissement), bleue (huile brûlée), noire (injection)."
                            AppLanguage.EN -> "Watch the exhaust smoke: thick white (coolant), blue (burning oil), black (fuel injection)."
                        }
                    )
                }

                item {
                    BuyerTipCard(
                        icon = Icons.Default.Build,
                        iconColor = ResultProblemRed,
                        title = Strings.tip3Title(language),
                        content = Strings.tip3Desc(language),
                        actionNotice = when (language) {
                            AppLanguage.AR -> "عند فتح الكونتاكت، يجب أن تضيء لمبة المحرك ثم تنطفئ بعد التشغيل. إذا لم تضئ أبداً فقد تكون معطلة عمداً."
                            AppLanguage.FR -> "Au contact, le voyant moteur doit s'allumer puis s'éteindre une fois le moteur démarré."
                            AppLanguage.EN -> "When turning the key on, the engine warning light must light up and then turn off once started."
                        }
                    )
                }

                item {
                    BuyerTipCard(
                        icon = Icons.Default.Block,
                        iconColor = ResultCheckOrange,
                        title = Strings.tip4Title(language),
                        content = Strings.tip4Desc(language),
                        actionNotice = when (language) {
                            AppLanguage.AR -> "تأكد من انتهاء مدة التجميد القانونية ووجود شهادة رفع اليد الرسمية."
                            AppLanguage.FR -> "Vérifiez l'échéance légale et la mainlevée officielle avant tout engagement."
                            AppLanguage.EN -> "Verify the legal holding period and official release certificate before committing."
                        }
                    )
                }

                item {
                    BuyerTipCard(
                        icon = Icons.Default.LocalGasStation,
                        iconColor = FahsBrand,
                        title = when (language) {
                            AppLanguage.AR -> "فحص خزان ونظام سيرغاز (GPL)"
                            AppLanguage.FR -> "Vérification de l'installation Sirghaz (GPL)"
                            AppLanguage.EN -> "LPG / Sirghaz system check"
                        },
                        content = when (language) {
                            AppLanguage.AR -> "تأكد من تاريخ صلاحية خزان الغاز (10 سنوات كحد أقصى) وتدوين عبارة الغاز على البطاقة الرمادية."
                            AppLanguage.FR -> "Contrôlez la date d'épreuve du réservoir (maximum 10 ans) et la mention GPL sur la carte grise."
                            AppLanguage.EN -> "Check the tank test date (max 10 years) and LPG mention on the registration card."
                        },
                        actionNotice = when (language) {
                            AppLanguage.AR -> "رائحة الغاز القوية عند تشغيل المحرك تستوجب فحص الأنابيب والمبخر فوراً."
                            AppLanguage.FR -> "Une odeur de gaz au démarrage nécessite un contrôle immédiat de l'installation."
                            AppLanguage.EN -> "Any gas smell at startup requires an immediate check of the lines and regulator."
                        }
                    )
                }

                item {
                    BuyerTipCard(
                        icon = Icons.Default.Construction,
                        iconColor = ResultProblemRed,
                        title = when (language) {
                            AppLanguage.AR -> "فحص الشاسيه والأعمدة الأمامية"
                            AppLanguage.FR -> "Contrôle des longerons sous le capot"
                            AppLanguage.EN -> "Checking the front chassis legs"
                        },
                        content = when (language) {
                            AppLanguage.AR -> "انظر تحت غطاء المحرك إلى أعمدة الشاسيه الأمامية وابحث عن أي آثار لحام أو اعوجاج أو معجون حديث."
                            AppLanguage.FR -> "Inspectez les longerons avant sous le capot à la recherche de soudures, plis ou mastic récent."
                            AppLanguage.EN -> "Inspect the front frame rails under the bonnet for welds, creases, or fresh filler."
                        },
                        actionNotice = when (language) {
                            AppLanguage.AR -> "الفحص الأوتوماتيكي بالفيشة لا يرى الحوادث أو الصدأ؛ افحص الهيكل دائماً بعينك."
                            AppLanguage.FR -> "Le scan automatique ne voit ni la carrosserie ni les chocs : vérifiez toujours visuellement."
                            AppLanguage.EN -> "An automatic scan cannot see bodywork or crash repairs: always inspect visually."
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BuyerTipCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    content: String,
    actionNotice: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, FahsHairline)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = FahsMutedText
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = FahsHairline, thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = actionNotice,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
