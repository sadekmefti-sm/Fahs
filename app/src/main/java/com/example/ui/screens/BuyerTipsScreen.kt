package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.data.model.AlgerianCarCatalog
import com.example.data.model.AlgerianCarCatalogExtra
import com.example.data.model.ItemSeverity
import com.example.domain.ObdDiagnosticEngine
import com.example.domain.ReportEngine
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusWarningAmber

@Composable
fun BuyerTipsScreen(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf("TIPS") } // TIPS, DTC_DICTIONARY, WEAK_POINTS
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
            .background(Slate50),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner with Total Content Stats
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Emerald700),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = Strings.buyerTipsTitle(language),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "${AlgerianCarCatalog.models.size} سيارة جزائرية • ${allDtcList.size} كود عطل OBD2 • 100% بدون إنترنت"
                                    AppLanguage.FR -> "${AlgerianCarCatalog.models.size} modèles DZ • ${allDtcList.size} codes OBD2 • 100% Hors-ligne"
                                    AppLanguage.EN -> "${AlgerianCarCatalog.models.size} DZ models • ${allDtcList.size} OBD2 codes • 100% Offline"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald100
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "موسوعة المشتري الجزائري: نصائح كشف التحايل، نقاط الضعف المعروفة لكل سيارة في السوق، وقاموس أكواد الأعطال OBD2 بالدارجة والعربية والفرنسية."
                            AppLanguage.FR -> "Encyclopédie de l'acheteur en Algérie : conseils anti-fraude, points faibles par modèle et dictionnaire des codes défauts OBD2 avec coûts en DZD."
                            AppLanguage.EN -> "Algerian Buyer's Encyclopedia: anti-fraud guide, model-specific weak points, and offline OBD2 fault code dictionary with DZD repair costs."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate200,
                        lineHeight = 21.sp
                    )
                }
            }
        }

        // Section Switcher Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSection == "TIPS",
                    onClick = { selectedSection = "TIPS" },
                    label = {
                        Text(
                            when (language) {
                                AppLanguage.AR -> "نصائح الفحص (6)"
                                AppLanguage.FR -> "Conseils (6)"
                                AppLanguage.EN -> "Buyer Tips (6)"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald700,
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
                                AppLanguage.AR -> "عيوب السيارات (${AlgerianCarCatalog.models.size})"
                                AppLanguage.FR -> "Points faibles (${AlgerianCarCatalog.models.size})"
                                AppLanguage.EN -> "Weak Points (${AlgerianCarCatalog.models.size})"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald700,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("guide_tab_weak_points")
                )
                FilterChip(
                    selected = selectedSection == "DTC_DICTIONARY",
                    onClick = { selectedSection = "DTC_DICTIONARY" },
                    label = {
                        Text(
                            when (language) {
                                AppLanguage.AR -> "أكواد OBD2 (${allDtcList.size})"
                                AppLanguage.FR -> "Codes OBD2 (${allDtcList.size})"
                                AppLanguage.EN -> "OBD2 Codes (${allDtcList.size})"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald700,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("guide_tab_dtc_dict")
                )
            }
        }

        when (selectedSection) {
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
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Emerald700,
                            unfocusedBorderColor = Slate200
                        )
                    )
                }

                items(filteredModels.size) { index ->
                    val car = filteredModels[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = Emerald700,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${car.brand} ${car.model}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Emerald100
                                ) {
                                    Text(
                                        text = car.commonEngines.firstOrNull() ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald900,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "نقطة الضعف الرئيسية: ${car.keyWeakPointAr}"
                                    AppLanguage.FR -> "Point faible principal : ${car.keyWeakPointFr}"
                                    AppLanguage.EN -> "Key Weak Point: ${car.keyWeakPointEn}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate800,
                                fontWeight = FontWeight.Medium
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Slate100,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "مكان فيشة OBD2: ${car.obdPortHintAr}"
                                        AppLanguage.FR -> "Prise OBD2 : ${car.obdPortHintFr}"
                                        AppLanguage.EN -> "OBD2 Port: ${car.obdPortHintEn}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
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
                                    AppLanguage.AR -> "ابحث عن كود عطل (مثال: P0299, P0087, P0420, تيربو، حاقن...)"
                                    AppLanguage.FR -> "Rechercher un code (ex: P0299, P0087, P0420, turbo, injecteur...)"
                                    AppLanguage.EN -> "Search fault code (e.g., P0299, P0087, P0420, turbo, injector...)"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Emerald700,
                            unfocusedBorderColor = Slate200
                        )
                    )
                }

                items(filteredDtcList.size) { index ->
                    val dtc = filteredDtcList[index]
                    val badgeColor = when (dtc.severity) {
                        ItemSeverity.CRITICAL -> StatusErrorRed
                        ItemSeverity.IMPORTANT -> StatusWarningAmber
                        ItemSeverity.MINOR -> StatusPassGreen
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = badgeColor.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "${dtc.code} • ${dtc.severity.getLabel(language)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                if (dtc.maxCostDzd > 0) {
                                    Text(
                                        text = "${ReportEngine.formatDzd(dtc.minCostDzd)} – ${ReportEngine.formatDzd(dtc.maxCostDzd)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate700
                                    )
                                }
                            }

                            Text(
                                text = dtc.getTitle(language),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            Text(
                                text = dtc.getExplanation(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            else -> {
                // Tip 1: Carte Grise & VIN
                item {
                    BuyerTipCard(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        iconColor = Emerald700,
                        title = Strings.tip1Title(language),
                        content = Strings.tip1Desc(language),
                        actionNotice = when (language) {
                            AppLanguage.AR -> "تنبيه: لا تدفع عربوناً أبداً إذا كانت البطاقة الرمادية مؤقتة (Carte provisoire) أو باسم شخص غير حاضر بدون وكالة موثقة."
                            AppLanguage.FR -> "Alerte : Ne versez jamais d'acompte si la carte est provisoire ou au nom d'un tiers sans procuration notariée valide."
                            AppLanguage.EN -> "Alert: Never pay a deposit if the carte grise is temporary or in someone else's name without a notarized power of attorney."
                        }
                    )
                }

                // Tip 2: Cold Engine Start
                item {
                    BuyerTipCard(
                        icon = Icons.Default.Speed,
                        iconColor = StatusWarningAmber,
                        title = Strings.tip2Title(language),
                        content = Strings.tip2Desc(language),
                        actionNotice = when (language) {
                            AppLanguage.AR -> "لاحظ لون الدخان: أبيض كثيف (حرق ماء التبريد)، أزرق (حرق زيت المحرك)، أسود كثيف (خلل في حاقنات الوقود Injecteurs)."
                            AppLanguage.FR -> "Observez la fumée : blanche dense (joint de culasse), bleue (consommation d'huile), noire (injecteurs encrassés/défaillants)."
                            AppLanguage.EN -> "Watch smoke color: thick white (coolant/head gasket), blue (burning engine oil), black (faulty injectors/air mix)."
                        }
                    )
                }

                // Tip 3: Erased Codes
                item {
                    BuyerTipCard(
                        icon = Icons.Default.Build,
                        iconColor = StatusErrorRed,
                        title = Strings.tip3Title(language),
                        content = Strings.tip3Desc(language),
                        actionNotice = when (language) {
                            AppLanguage.AR -> "عند فتح الكونتاكت، يجب أن تضيء لمبة Check Engine ولمبة الزيت والبطارية. إذا كانت لمبة المحرك لا تضيء أبداً، فقد تكون منزوعة عمداً."
                            AppLanguage.FR -> "Au contact, le voyant moteur orange DOIT s'allumer avec la batterie. S'il ne s'allume jamais, l'ampoule a peut-être été neutralisée."
                            AppLanguage.EN -> "At ignition ON, the orange Check Engine light MUST illuminate. If it never turns on, the bulb may have been removed."
                        }
                    )
                }

                // Tip 4: Moudjahid Licence
                item {
                    BuyerTipCard(
                        icon = Icons.Default.Block,
                        iconColor = StatusWarningAmber,
                        title = Strings.tip4Title(language),
                        content = Strings.tip4Desc(language),
                        actionNotice = when (language) {
                            AppLanguage.AR -> "تأكد من شرط 3 أو 5 سنوات، ووجود شهادة رفع اليد من مديرية المجاهدين لتفادي حجز السيارة أو رفض نقل الملكية."
                            AppLanguage.FR -> "Vérifiez l'échéance légale (3 ou 5 ans) et l'existence de la mainlevée officielle délivrée par la direction des Moudjahidine."
                            AppLanguage.EN -> "Verify the legal holding period (3 or 5 years) and the official release certificate from the Moudjahidine administration."
                        }
                    )
                }

                // Tip 5: GPL / Sirghaz
                item {
                    BuyerTipCard(
                        icon = Icons.Default.LocalGasStation,
                        iconColor = Emerald700,
                        title = when (language) {
                            AppLanguage.AR -> "فحص خزان ونظام سيرغاز (GPL)"
                            AppLanguage.FR -> "Vérification de l'installation Sirghaz (GPL)"
                            AppLanguage.EN -> "GPL / LPG System Inspection"
                        },
                        content = when (language) {
                            AppLanguage.AR -> "السيارات المحولة للغاز شائعة جداً في الجزائر. تأكد من تاريخ صلاحية خزان الغاز (10 سنوات كحد أقصى) وتدوين عبارة (ES/GPL) على البطاقة الرمادية."
                            AppLanguage.FR -> "Les voitures équipées au GPL sont très courantes. Contrôlez la date limite du réservoir (max 10 ans) et la mention ES/GPL sur la carte grise."
                            AppLanguage.EN -> "LPG converted cars are very common in Algeria. Check the tank expiry date (max 10 years) and ES/GPL mention on the carte grise."
                        },
                        actionNotice = when (language) {
                            AppLanguage.AR -> "رائحة الغاز القوية عند تشغيل المحرك دليل على تسريب خطير في الأنابيب أو المبخر (Vapodétendeur)."
                            AppLanguage.FR -> "Une forte odeur de gaz au démarrage indique une fuite dangereuse sur les raccords ou le vapodétendeur."
                            AppLanguage.EN -> "A strong gas smell at start indicates a hazardous leak in lines or the vaporizer/regulator."
                        }
                    )
                }

                // Tip 6: Longeron & Chassis Repairs
                item {
                    BuyerTipCard(
                        icon = Icons.Default.Construction,
                        iconColor = StatusErrorRed,
                        title = when (language) {
                            AppLanguage.AR -> "فحص الشاسيه واللونجرون (Longeron)"
                            AppLanguage.FR -> "Contrôle des longerons et du marbre"
                            AppLanguage.EN -> "Chassis legs & collision signs"
                        },
                        content = when (language) {
                            AppLanguage.AR -> "انظر تحت غطاء المحرك إلى أعمدة الشاسيه الأمامية (اللونجرون). ابحث عن آثار لحام غير أصلية، اعوجاج، أو صمغ سيليكون (Mastic) تم وضعه حديثاً لإخفاء حادث اصطدام سابق."
                            AppLanguage.FR -> "Inspectez les longerons avant sous le capot. Cherchez des soudures non d'origine, des plis, ou du mastic récent masquant un choc frontal."
                            AppLanguage.EN -> "Inspect the front chassis legs under the bonnet. Look for non-factory welding, creases, or fresh sealant applied to conceal a crash."
                        },
                        actionNotice = when (language) {
                            AppLanguage.AR -> "أي اعوجاج في اللونجرون أو ضربة في الشاسيه تستدعي التراجع الفوري عن الشراء."
                            AppLanguage.FR -> "Tout longeron touché ou redressé au marbre doit vous inciter à renoncer à l'achat."
                            AppLanguage.EN -> "Any damaged or re-welded chassis leg is a major safety defect: walk away from the purchase."
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate700,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Slate100
            ) {
                Text(
                    text = actionNotice,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Slate800,
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
