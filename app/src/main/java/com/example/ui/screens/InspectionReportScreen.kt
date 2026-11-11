package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.data.local.ChecklistAnswerEntity
import com.example.data.local.InspectionEntity
import com.example.data.model.ItemSeverity
import com.example.domain.InspectionReportData
import com.example.domain.PdfReportGenerator
import com.example.domain.ReportEngine
import com.example.domain.ReportFindingItem
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
fun InspectionReportScreen(
    inspection: InspectionEntity,
    savedAnswers: List<ChecklistAnswerEntity>,
    language: AppLanguage,
    onBackToInspections: () -> Unit,
    onReopenChecklist: () -> Unit,
    onOpenObdScanner: () -> Unit,
    onPdfGeneratedNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val report: InspectionReportData = remember(inspection, savedAnswers) {
        ReportEngine.buildReport(inspection, savedAnswers)
    }

    var askingPriceMillionsInput by remember { mutableStateOf("235") }
    val askingMillions = askingPriceMillionsInput.toDoubleOrNull() ?: 0.0

    var showPassedItems by remember { mutableStateOf(false) }
    var showUncheckedItems by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Navigation & Car Title Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToInspections,
                        modifier = Modifier.testTag("report_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Slate900
                        )
                    }
                    Column {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "التقرير النهائي لفحص السيارة"
                                AppLanguage.FR -> "Rapport Final d'Inspection"
                                AppLanguage.EN -> "Final Inspection Report"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "${inspection.carBrand} ${inspection.carModel} (${inspection.carYear}) • ${inspection.fuelType} • ${inspection.gearboxType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }
            }
        }

        // 2. Score Gauge, Verdict & Critical Cap Card
        item {
            ScoreVerdictHeroCard(
                report = report,
                language = language
            )
        }

        // 3. Primary Actions: Export/Share PDF + Edit Checklist + Scan OBD2
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val file = PdfReportGenerator.sharePdfReport(
                                context = context,
                                report = report,
                                language = language,
                                sellerAskingMillions = askingMillions.takeIf { it > 0.0 }
                            )
                            val msg = when (language) {
                                AppLanguage.AR -> "تم إنشاء تقرير PDF جاهز للمشاركة (${file.name})"
                                AppLanguage.FR -> "Rapport PDF généré et prêt à partager (${file.name})"
                                AppLanguage.EN -> "PDF report generated and ready to share (${file.name})"
                            }
                            onPdfGeneratedNotification(msg)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("export_pdf_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "حفظ ومشاركة التقرير PDF (واتساب / ملفات)"
                                AppLanguage.FR -> "Exporter & Partager le PDF (WhatsApp / Fichiers)"
                                AppLanguage.EN -> "Export & Share PDF Report (WhatsApp / Files)"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReopenChecklist,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_checklist_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "تعديل الإجابات"
                                    AppLanguage.FR -> "Modifier réponses"
                                    AppLanguage.EN -> "Edit Answers"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenObdScanner,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("report_open_obd_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "فحص OBD2"
                                    AppLanguage.FR -> "Scanner OBD2"
                                    AppLanguage.EN -> "OBD2 Scan"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Interactive Algerian Price Negotiation Helper (Millions de centimes & DZD)
        item {
            NegotiationCalculatorCard(
                report = report,
                askingPriceMillionsInput = askingPriceMillionsInput,
                onAskingPriceChange = { askingPriceMillionsInput = it },
                language = language
            )
        }

        // 5. Identity, VIN, Algerian Plate & Documents Summary Card
        item {
            IdentityAndPaperworkReportCard(
                report = report,
                language = language
            )
        }

        // 6. Critical Issues Section (Red)
        if (report.criticalProblems.isNotEmpty()) {
            item {
                FindingsSectionCard(
                    title = when (language) {
                        AppLanguage.AR -> "عيوب حرجة ومخاطر عالية (${report.criticalProblems.size})"
                        AppLanguage.FR -> "Anomalies Critiques (${report.criticalProblems.size})"
                        AppLanguage.EN -> "Critical Issues Found (${report.criticalProblems.size})"
                    },
                    subtitle = when (language) {
                        AppLanguage.AR -> "تستوجب فحصاً معمقاً عند ميكانيكي أو التراجع عن الشراء"
                        AppLanguage.FR -> "Ces points ont plafonné le score et exigent l'avis d'un professionnel"
                        AppLanguage.EN -> "These items capped the score and require a specialist's opinion"
                    },
                    items = report.criticalProblems,
                    isCritical = true,
                    language = language
                )
            }
        }

        // 7. Important & Minor Issues to Negotiate Section (Amber)
        if (report.negotiableProblems.isNotEmpty()) {
            item {
                FindingsSectionCard(
                    title = when (language) {
                        AppLanguage.AR -> "نقاط للتفاوض والصيانة (${report.negotiableProblems.size})"
                        AppLanguage.FR -> "Points à négocier / Frais à prévoir (${report.negotiableProblems.size})"
                        AppLanguage.EN -> "Items to Negotiate / Upcoming Repairs (${report.negotiableProblems.size})"
                    },
                    subtitle = when (language) {
                        AppLanguage.AR -> "عيوب قابلة للإصلاح — اخصم تكلفتها من السعر المطلوب"
                        AppLanguage.FR -> "Défauts réparables — déduisez ces montants lors de la négociation"
                        AppLanguage.EN -> "Repairable items — use these Algerian market estimates to negotiate"
                    },
                    items = report.negotiableProblems,
                    isCritical = false,
                    language = language
                )
            }
        }

        // 8. Honest "Could Not Check" Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showUncheckedItems = !showUncheckedItems },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircleOutline,
                                contentDescription = null,
                                tint = Slate600,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "نقاط تعذر فحصها (${report.uncheckedItems.size})"
                                    AppLanguage.FR -> "Points non vérifiés (${report.uncheckedItems.size})"
                                    AppLanguage.EN -> "Items Not Checked (${report.uncheckedItems.size})"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Text(
                            text = if (showUncheckedItems) "▲" else "▼",
                            fontWeight = FontWeight.Bold,
                            color = Slate600
                        )
                    }

                    AnimatedVisibility(visible = showUncheckedItems) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            if (report.uncheckedItems.isEmpty()) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "تم فحص جميع النقاط المبرمجة للسيارة بنسبة 100% ✓"
                                        AppLanguage.FR -> "Tous les points prévus ont été vérifiés (100% de couverture) ✓"
                                        AppLanguage.EN -> "All scheduled items were verified (100% coverage) ✓"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusPassGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "للأمانة والوضوح: هذه النقاط لم يتم التأكد منها خلال الزيارة:"
                                        AppLanguage.FR -> "Par transparence, ces éléments n'ont pas pu être contrôlés pendant la visite :"
                                        AppLanguage.EN -> "For transparency, these items were skipped or could not be checked:"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                report.uncheckedItems.forEach { item ->
                                    Text(
                                        text = "• [${item.getSectionTitle(language)}] ${item.getQuestion(language)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 9. Collapsible Passed Items Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPassedItems = !showPassedItems },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusPassGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "النقاط السليمة والمطابقة (${report.passedItems.size})"
                                    AppLanguage.FR -> "Points conformes (${report.passedItems.size})"
                                    AppLanguage.EN -> "Passed Checks (${report.passedItems.size})"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Text(
                            text = if (showPassedItems) "▲" else "▼",
                            fontWeight = FontWeight.Bold,
                            color = Emerald700
                        )
                    }

                    AnimatedVisibility(visible = showPassedItems) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            report.passedItems.forEach { item ->
                                Text(
                                    text = "✓ ${item.getQuestion(language)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreVerdictHeroCard(
    report: InspectionReportData,
    language: AppLanguage
) {
    val ringColor = when (report.verdict) {
        "GOOD" -> Color(0xFF34D399)
        "NEGOTIATE" -> Color(0xFFFBBF24)
        else -> Color(0xFFF87171)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("report_score_hero_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Score Gauge
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color.White.copy(alpha = 0.14f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 22f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = (report.overallScore / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = 22f, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${report.overallScore}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "/ 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate300
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ringColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, ringColor)
                    ) {
                        Text(
                            text = report.getVerdictTitle(language),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ringColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "التغطية: ${report.checkedCount}/${report.totalApplicableCount} نقطة مفحوصة (${report.coveragePercent}%)"
                            AppLanguage.FR -> "Couverture : ${report.checkedCount}/${report.totalApplicableCount} points (${report.coveragePercent}%)"
                            AppLanguage.EN -> "Coverage: ${report.checkedCount}/${report.totalApplicableCount} checked (${report.coveragePercent}%)"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Emerald200
                    )

                    Text(
                        text = if (report.hasObdScanIncluded) {
                            when (language) {
                                AppLanguage.AR -> "✓ يشمل فحص الكمبيوتر OBD2"
                                AppLanguage.FR -> "✓ Inclut le diagnostic OBD2"
                                AppLanguage.EN -> "✓ Includes OBD2 ECU Scan"
                            }
                        } else {
                            when (language) {
                                AppLanguage.AR -> "○ بدون فحص OBD2 (فحص يدوي)"
                                AppLanguage.FR -> "○ Sans scan OBD2 (inspection visuelle)"
                                AppLanguage.EN -> "○ Visual inspection (no OBD2 scan)"
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate300
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = report.getVerdictSummary(language),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.92f),
                lineHeight = 19.sp
            )

            if (report.isCriticalCapTriggered) {
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "تقييد الأمان: النتيجة الخام كانت ${report.rawUncappedScore}/100 لكن تم خفضها إلى ${report.overallScore}/100 لوجود عيب حرج لا يعوضه نظافة باقي السيارة."
                                AppLanguage.FR -> "Plafond de sécurité : Score brut ${report.rawUncappedScore}/100 ramené à ${report.overallScore}/100 en raison d'un défaut critique."
                                AppLanguage.EN -> "Safety Cap: Raw score ${report.rawUncappedScore}/100 capped at ${report.overallScore}/100 due to a critical defect."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusErrorRedOnContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NegotiationCalculatorCard(
    report: InspectionReportData,
    askingPriceMillionsInput: String,
    onAskingPriceChange: (String) -> Unit,
    language: AppLanguage
) {
    val askingMillions = askingPriceMillionsInput.toDoubleOrNull() ?: 0.0
    val (minOfferDzd, maxOfferDzd) = ReportEngine.calculateFairOfferRangeDzd(
        askingPriceMillions = askingMillions,
        minRepairDzd = report.minTotalRepairDzd,
        maxRepairDzd = report.maxTotalRepairDzd
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("negotiation_helper_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Emerald600)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = Emerald700,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "مساعد التفاوض على السعر (بالسنتيم والدينار الجزائري)"
                        AppLanguage.FR -> "Aide à la négociation (en Millions de centimes & DZD)"
                        AppLanguage.EN -> "Algerian Price Negotiation Helper (Millions & DZD)"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total Estimated Repairs Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (report.avgTotalRepairDzd > 0) StatusWarningAmberContainer else StatusPassGreenContainer
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "مجموع تكاليف الإصلاح المقدرة في السوق الجزائري:"
                            AppLanguage.FR -> "Total estimé des réparations à prévoir :"
                            AppLanguage.EN -> "Total Estimated Repair Costs (Algerian Market):"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (report.avgTotalRepairDzd > 0) StatusWarningAmberOnContainer else StatusPassGreenOnContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${ReportEngine.formatDzd(report.minTotalRepairDzd)} – ${ReportEngine.formatDzd(report.maxTotalRepairDzd)} (~${ReportEngine.formatMillionsCentimes(report.avgTotalRepairDzd, language)})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (report.avgTotalRepairDzd > 0) StatusWarningAmberOnContainer else StatusPassGreenOnContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seller Asking Price Input in Millions de centimes
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = askingPriceMillionsInput,
                    onValueChange = { newVal ->
                        if (newVal.all { it.isDigit() || it == '.' } && newVal.length <= 6) {
                            onAskingPriceChange(newVal)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("asking_price_millions_input"),
                    label = {
                        Text(
                            when (language) {
                                AppLanguage.AR -> "السعر المطلوب (مليون سنتيم)"
                                AppLanguage.FR -> "Prix demandé (en Millions)"
                                AppLanguage.EN -> "Asking Price (Millions centimes)"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald700,
                        unfocusedBorderColor = Slate300
                    )
                )

                OutlinedButton(
                    onClick = {
                        val cur = askingPriceMillionsInput.toIntOrNull() ?: 200
                        onAskingPriceChange((cur - 10).coerceAtLeast(50).toString())
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("-10M", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val cur = askingPriceMillionsInput.toIntOrNull() ?: 200
                        onAskingPriceChange((cur + 10).toString())
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+10M", fontWeight = FontWeight.Bold)
                }
            }

            if (askingMillions > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "السعر العادل المقترح بعد خصم الإصلاحات:"
                                AppLanguage.FR -> "Offre équitable suggérée (après déduction des frais) :"
                                AppLanguage.EN -> "Suggested Fair Offer Range (after repair deduction):"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${ReportEngine.formatMillionsCentimes(minOfferDzd, language)}  –  ${ReportEngine.formatMillionsCentimes(maxOfferDzd, language)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald800
                        )
                        Text(
                            text = "(${ReportEngine.formatDzd(minOfferDzd)} – ${ReportEngine.formatDzd(maxOfferDzd)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (language) {
                    AppLanguage.AR -> "* الأسعار التقديرية استرشادية وتختلف حسب الولاية وتوفر قطع الغيار الأصلية أو التجارية."
                    AppLanguage.FR -> "* Ces fourchettes sont indicatives et varient selon la wilaya et le choix des pièces (origine ou adaptable)."
                    AppLanguage.EN -> "* Estimates are indicative and vary by wilaya and parts choice (OEM vs aftermarket)."
                },
                style = MaterialTheme.typography.labelSmall,
                color = Slate600
            )
        }
    }
}

@Composable
private fun IdentityAndPaperworkReportCard(
    report: InspectionReportData,
    language: AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Emerald700,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "الهوية، رقم الشاسيه (VIN) والوثائق الجزائرية"
                        AppLanguage.FR -> "Identité, VIN, Immatriculation & Documents"
                        AppLanguage.EN -> "Identity, VIN, Algerian Plate & Paperwork"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val vinDisplay = report.inspection.vin.ifBlank {
                when (language) {
                    AppLanguage.AR -> "لم يتم إدخاله"
                    AppLanguage.FR -> "Non renseigné"
                    AppLanguage.EN -> "Not entered"
                }
            }
            val plateDisplay = report.inspection.plateNumber.ifBlank {
                when (language) {
                    AppLanguage.AR -> "لم يتم إدخالها"
                    AppLanguage.FR -> "Non renseignée"
                    AppLanguage.EN -> "Not entered"
                }
            }

            Text(
                text = "VIN: $vinDisplay",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            if (report.vinAnalysis != null && report.vinAnalysis.isFormatValid) {
                Text(
                    text = "${report.vinAnalysis.manufacturer} • ${report.vinAnalysis.countryFr}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Emerald800
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Immatriculation: $plateDisplay",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            if (report.plateAnalysis != null && report.plateAnalysis.isValidFormat) {
                Text(
                    text = "Wilaya ${report.plateAnalysis.getWilayaDisplay(language)} • ${report.plateAnalysis.getCategoryName(language)} • ${report.plateAnalysis.registrationYear ?: "00 (Provisoire)"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Emerald800
                )
            }

            if (report.inspection.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Slate50
                ) {
                    Text(
                        text = report.inspection.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FindingsSectionCard(
    title: String,
    subtitle: String,
    items: List<ReportFindingItem>,
    isCritical: Boolean,
    language: AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isCritical) StatusErrorRed else StatusWarningAmber
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCritical) Icons.Default.Cancel else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isCritical) StatusErrorRed else StatusWarningAmber,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            items.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCritical) StatusErrorRedContainer.copy(alpha = 0.4f) else StatusWarningAmberContainer.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.getSectionTitle(language),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                            if (item.maxCostDzd > 0) {
                                Text(
                                    text = "${item.minCostDzd / 1000}k - ${item.maxCostDzd / 1000}k DZD",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isCritical) StatusErrorRed else StatusWarningAmberOnContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.getQuestion(language),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = item.getExplanation(language),
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700,
                            lineHeight = 18.sp
                        )

                        if (item.buyerNote.isNotBlank() || item.photoUri != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (item.photoUri != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Emerald100
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PhotoCamera,
                                                contentDescription = null,
                                                tint = Emerald800,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Photo ✓",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Emerald900
                                            )
                                        }
                                    }
                                }
                                if (item.buyerNote.isNotBlank()) {
                                    Text(
                                        text = "📝 ${item.buyerNote}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate800
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
