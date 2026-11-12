package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.i18n.AppLanguage
import com.example.data.local.ChecklistAnswerEntity
import com.example.data.local.InspectionEntity
import com.example.domain.InspectionReportData
import com.example.domain.ReportEngine
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusErrorRedContainer
import com.example.ui.theme.StatusErrorRedOnContainer
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.StatusWarningAmberContainer
import com.example.ui.theme.StatusWarningAmberOnContainer

@Composable
fun CarComparisonDialog(
    inspections: List<InspectionEntity>,
    answersByInspectionId: Map<Long, List<ChecklistAnswerEntity>> = emptyMap(),
    language: AppLanguage,
    onSelectCarReport: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Build complete actual report data for each selected car
    val reports: List<InspectionReportData> = remember(inspections, answersByInspectionId) {
        inspections.map { car ->
            val savedAnswers = answersByInspectionId[car.id].orEmpty()
            ReportEngine.buildReport(car, savedAnswers)
        }
    }

    // Rank cars that have a valid score by: 1) highest overallScore, 2) fewest critical problems, 3) fewest total problems
    val bestInspectionId = remember(reports) {
        reports
            .filter { it.overallScore != null }
            .maxWithOrNull(
                compareBy<InspectionReportData> { it.overallScore ?: 0 }
                    .thenByDescending { -it.criticalProblems.size }
                    .thenByDescending { -it.negotiableProblems.size }
            )?.inspection?.id
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("car_comparison_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = Slate50
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Emerald800)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "مقارنة نتائج الفحص الفعلية جنباً إلى جنب (${reports.size})"
                                    AppLanguage.FR -> "Comparaison Côte à Côte des Résultats (${reports.size})"
                                    AppLanguage.EN -> "Side-by-Side Actual Findings Comparison (${reports.size})"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "مقارنة مباشرة للأعطال المسجلة، فحص OBD2، والنقاط غير المفحوصة لكل سيارة"
                                    AppLanguage.FR -> "Anomalies réelles, diagnostic OBD2 et points non vérifiés de chaque voiture"
                                    AppLanguage.EN -> "Compare both cars' actual recorded issues, OBD2 results, and unchecked items"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Emerald100
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_comparison_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Scrollable Comparison Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Recommendation Banner
                    val bestReport = reports.firstOrNull { it.inspection.id == bestInspectionId }
                    if (bestReport != null) {
                        val bestCar = bestReport.inspection
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Emerald100)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Emerald800,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "الأفضل حسب نتائج الفحص: ${bestCar.carBrand} ${bestCar.carModel} (${bestCar.carYear})"
                                            AppLanguage.FR -> "Meilleur résultat d'inspection : ${bestCar.carBrand} ${bestCar.carModel} (${bestCar.carYear})"
                                            AppLanguage.EN -> "Highest Rated Inspection: ${bestCar.carBrand} ${bestCar.carModel} (${bestCar.carYear})"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald900
                                    )
                                    val totalProbs = bestReport.criticalProblems.size + bestReport.negotiableProblems.size
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "النتيجة: ${bestReport.overallScore ?: "--"}/100 • عيوب حرجة: ${bestReport.criticalProblems.size} • ملاحظات صيانة: ${bestReport.negotiableProblems.size} (مجموع: $totalProbs)"
                                            AppLanguage.FR -> "Score : ${bestReport.overallScore ?: "--"}/100 • Critiques : ${bestReport.criticalProblems.size} • À négocier : ${bestReport.negotiableProblems.size} (Total : $totalProbs)"
                                            AppLanguage.EN -> "Score: ${bestReport.overallScore ?: "--"}/100 • Critical: ${bestReport.criticalProblems.size} • Negotiable: ${bestReport.negotiableProblems.size} (Total: $totalProbs)"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Emerald800
                                    )
                                }
                            }
                        }
                    }

                    // Horizontal Scrollable Columns for 2 or 3 Cars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        reports.forEach { report ->
                            val isBest = report.inspection.id == bestInspectionId
                            ComparisonCarColumn(
                                report = report,
                                isBestPick = isBest,
                                language = language,
                                onOpenReport = {
                                    onDismiss()
                                    onSelectCarReport(report.inspection.id)
                                },
                                modifier = Modifier.width(290.dp)
                            )
                        }
                    }

                    // Direct Item-by-Item Actual Findings Comparison Matrix
                    if (reports.size >= 2) {
                        SideBySideFindingsMatrixCard(
                            reports = reports,
                            language = language
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonCarColumn(
    report: InspectionReportData,
    isBestPick: Boolean,
    language: AppLanguage,
    onOpenReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val car = report.inspection
    val score = report.overallScore
    val rawScore = report.rawUncappedScore
    val scoreColor = when {
        score == null -> Slate600
        score >= 78 -> StatusPassGreen
        score >= 50 -> StatusWarningAmber
        else -> StatusErrorRed
    }

    Card(
        modifier = modifier
            .border(
                width = if (isBestPick) 2.dp else 1.dp,
                color = if (isBestPick) Emerald600 else Slate200,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBestPick) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Badge if Best Pick
            if (isBestPick) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald700,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "الأفضل في المقارنة"
                                AppLanguage.FR -> "MEILLEUR PROFIL"
                                AppLanguage.EN -> "BEST IN COMPARISON"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(26.dp))
            }

            // Car Title, Year & Odometer
            Column {
                Text(
                    text = "${car.carBrand} ${car.carModel}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                val odoStr = if (car.odometerKm > 0) " • ${car.odometerKm} km" else ""
                val plateStr = if (car.plateNumber.isNotBlank()) " • ${car.plateNumber}" else ""
                Text(
                    text = "${car.carGeneration} (${car.carYear})$odoStr$plateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }

            // Score Box (shows both raw and capped scores if capped at 42)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = scoreColor.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (score != null) "$score / 100" else "—",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scoreColor
                    )
                    if (report.isCriticalCapTriggered && rawScore != null && score != null) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "(النتيجة الخام: $rawScore/100 ← مقيّدة عند $score)"
                                AppLanguage.FR -> "(Score brut : $rawScore/100 → plafonné à $score)"
                                AppLanguage.EN -> "(Raw score: $rawScore/100 → capped at $score)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusErrorRed
                        )
                    }
                    Text(
                        text = report.getVerdictTitle(language),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            HorizontalDivider(color = Slate100)

            // 1. Actual Critical Findings on This Car
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "1. العيوب الحرجة المسجلة (${report.criticalProblems.size})"
                        AppLanguage.FR -> "1. Anomalies critiques (${report.criticalProblems.size})"
                        AppLanguage.EN -> "1. Critical Findings (${report.criticalProblems.size})"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (report.criticalProblems.isNotEmpty()) StatusErrorRed else Slate700
                )
                if (report.criticalProblems.isEmpty()) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "✓ لا توجد عيوب حرجة مسجلة"
                            AppLanguage.FR -> "✓ Aucune anomalie critique"
                            AppLanguage.EN -> "✓ None recorded (0)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusPassGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    report.criticalProblems.forEach { crit ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusErrorRedContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val notePart = if (crit.buyerNote.isNotBlank()) " — ${crit.buyerNote}" else ""
                            Text(
                                text = "✗ ${crit.getQuestion(language)}$notePart",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusErrorRedOnContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Slate100)

            // 2. Actual Negotiable / Repair Issues on This Car
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "2. ملاحظات الصيانة والتفاوض (${report.negotiableProblems.size})"
                        AppLanguage.FR -> "2. Points à négocier (${report.negotiableProblems.size})"
                        AppLanguage.EN -> "2. Negotiable Issues (${report.negotiableProblems.size})"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (report.negotiableProblems.isNotEmpty()) StatusWarningAmber else Slate700
                )
                if (report.negotiableProblems.isEmpty()) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "✓ لا توجد ملاحظات مسجلة"
                            AppLanguage.FR -> "✓ Aucun défaut à négocier"
                            AppLanguage.EN -> "✓ None recorded (0)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusPassGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    report.negotiableProblems.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusWarningAmberContainer.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val notePart = if (item.buyerNote.isNotBlank()) " — ${item.buyerNote}" else ""
                            Text(
                                text = "• ${item.getQuestion(language)}$notePart",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusWarningAmberOnContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Slate100)

            // 3. Actual OBD2 Findings on This Car
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "3. نتائج فحص الكمبيوتر OBD2"
                        AppLanguage.FR -> "3. Résultats OBD2"
                        AppLanguage.EN -> "3. OBD2 Scan Results"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "• ${report.obdBlock.getConnectionStatusText(language)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate700
                )
                if (report.obdBlock.isScanned && report.obdBlock.isCommunicationSuccess) {
                    Text(
                        text = "• ${report.obdBlock.getFaultCodesText(language)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (report.obdBlock.dtcCount > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (report.obdBlock.dtcCount > 0) StatusErrorRed else Slate700
                    )
                    Text(
                        text = "• ${report.obdBlock.getClearedCodesText(language)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (report.obdBlock.clearedCodesStatus == "STRONG_WARNING") FontWeight.Bold else FontWeight.Normal,
                        color = if (report.obdBlock.clearedCodesStatus == "STRONG_WARNING") StatusErrorRed else Slate700
                    )
                    Text(
                        text = "• ${report.obdBlock.getCheckEngineLightText(language)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (!report.obdBlock.milBulbLitAtKeyOn) StatusErrorRed else Slate700
                    )
                    Text(
                        text = "• ${report.obdBlock.getChassisComparisonText(language)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (report.obdBlock.ecuVinComparison == "DIFFERENT") StatusErrorRed else Slate700
                    )
                }
            }

            HorizontalDivider(color = Slate100)

            // 4. Coverage & Unchecked Items
            ComparisonMetricRow(
                label = when (language) {
                    AppLanguage.AR -> "النقاط المفحوصة / غير المفحوصة"
                    AppLanguage.FR -> "Points vérifiés / non vérifiés"
                    AppLanguage.EN -> "Checked / Unchecked Items"
                },
                value = when (language) {
                    AppLanguage.AR -> "مفحوص: ${report.checkedCount}/${report.totalApplicableCount} • غير مفحوص: ${report.uncheckedItems.size}"
                    AppLanguage.FR -> "Vérifiés : ${report.checkedCount}/${report.totalApplicableCount} • Non vérifiés : ${report.uncheckedItems.size}"
                    AppLanguage.EN -> "Checked: ${report.checkedCount}/${report.totalApplicableCount} • Unchecked: ${report.uncheckedItems.size}"
                },
                valueColor = Slate700
            )

            // 5. User-Entered Mechanic Repair Quote & Asking Price (only if entered by user)
            if (car.userQuotedRepairDzd > 0 || car.sellerAskingPriceMillions > 0.0) {
                val quoteText = if (car.userQuotedRepairDzd > 0) {
                    ReportEngine.formatAlgerianCurrency(car.userQuotedRepairDzd, language)
                } else "—"
                ComparisonMetricRow(
                    label = when (language) {
                        AppLanguage.AR -> "تسعيرة الميكانيكي المدخلة"
                        AppLanguage.FR -> "Devis mécanicien saisi"
                        AppLanguage.EN -> "User's Mechanic Quote"
                    },
                    value = quoteText,
                    valueColor = Emerald800,
                    boldValue = true
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onOpenReport,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBestPick) Emerald700 else Slate700
                )
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "فتح التقرير الكامل"
                        AppLanguage.FR -> "Ouvrir le rapport"
                        AppLanguage.EN -> "Open Full Report"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SideBySideFindingsMatrixCard(
    reports: List<InspectionReportData>,
    language: AppLanguage
) {
    // Collect all unique itemIds where at least one car had a PROBLEM or where the cars had different answers
    val allItemsByCar: List<Map<String, com.example.domain.ReportFindingItem>> = remember(reports) {
        reports.map { r ->
            (r.criticalProblems + r.negotiableProblems + r.passedItems + r.uncheckedItems)
                .associateBy { it.itemId }
        }
    }

    val comparedItemIds: List<String> = remember(allItemsByCar) {
        val allIds = allItemsByCar.flatMap { it.keys }.distinct()
        allIds.filter { itemId ->
            val statuses = allItemsByCar.map { it[itemId]?.status ?: "NOT_APPLICABLE" }
            statuses.any { it == "PROBLEM" } || statuses.distinct().size > 1
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("side_by_side_findings_matrix"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = when (language) {
                    AppLanguage.AR -> "مقارنة تفصيلية لنقاط الاختلاف والأعطال بين السيارتين (${comparedItemIds.size})"
                    AppLanguage.FR -> "Comparaison point par point des écarts et défauts (${comparedItemIds.size})"
                    AppLanguage.EN -> "Item-by-Item Findings Comparison (${comparedItemIds.size})"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            if (comparedItemIds.isEmpty()) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "جميع النقاط المتطابقة بين السيارتين لها نفس الحالة."
                        AppLanguage.FR -> "Tous les points communs ont obtenu exactement le même résultat sur les véhicules comparés."
                        AppLanguage.EN -> "All shared checklist items have the exact same result across the compared cars."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            } else {
                comparedItemIds.take(25).forEach { itemId ->
                    val referenceItem = allItemsByCar.firstNotNullOfOrNull { it[itemId] } ?: return@forEach
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate50,
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "[${referenceItem.getSectionTitle(language)}] ${referenceItem.getQuestion(language)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                reports.forEachIndexed { idx, rep ->
                                    val finding = allItemsByCar[idx][itemId]
                                    val carTitle = "${rep.inspection.carBrand} ${rep.inspection.carModel}"
                                    val statusLabel = when (finding?.status) {
                                        "GOOD" -> when (language) {
                                            AppLanguage.AR -> "✓ سليم"
                                            AppLanguage.FR -> "✓ Conforme"
                                            AppLanguage.EN -> "✓ Good"
                                        }
                                        "PROBLEM" -> {
                                            val noteSuffix = if (!finding.buyerNote.isNullOrBlank()) " (${finding.buyerNote})" else ""
                                            when (language) {
                                                AppLanguage.AR -> "✗ مشكل$noteSuffix"
                                                AppLanguage.FR -> "✗ Problème$noteSuffix"
                                                AppLanguage.EN -> "✗ Problem$noteSuffix"
                                            }
                                        }
                                        "UNCHECKED" -> when (language) {
                                            AppLanguage.AR -> "○ غير مفحوص"
                                            AppLanguage.FR -> "○ Non vérifié"
                                            AppLanguage.EN -> "○ Not checked"
                                        }
                                        else -> when (language) {
                                            AppLanguage.AR -> "— غير منطبق"
                                            AppLanguage.FR -> "— Non applicable"
                                            AppLanguage.EN -> "— N/A on this car"
                                        }
                                    }
                                    val statusColor = when (finding?.status) {
                                        "GOOD" -> StatusPassGreen
                                        "PROBLEM" -> StatusErrorRed
                                        else -> Slate600
                                    }

                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = carTitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate600,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = statusLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = statusColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonMetricRow(
    label: String,
    value: String,
    valueColor: Color,
    boldValue: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Slate600
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (boldValue) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
            textAlign = TextAlign.Start
        )
    }
}
