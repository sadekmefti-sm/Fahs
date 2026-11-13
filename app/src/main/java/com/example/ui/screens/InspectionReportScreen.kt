package com.example.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.photo.InspectionPhotoHelper
import com.example.data.local.ChecklistAnswerEntity
import com.example.data.local.InspectionEntity
import com.example.data.model.ItemSeverity
import com.example.domain.InspectionReportData
import com.example.domain.ObdReportBlockData
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
import java.io.File

@Composable
fun InspectionReportScreen(
    inspection: InspectionEntity,
    savedAnswers: List<ChecklistAnswerEntity>,
    language: AppLanguage,
    onBackToInspections: () -> Unit,
    onReopenChecklist: () -> Unit,
    onOpenObdScanner: () -> Unit,
    onSaveNegotiationInputs: (Double, Int) -> Unit = { _, _ -> },
    onPdfGeneratedNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val report: InspectionReportData = remember(inspection, savedAnswers) {
        ReportEngine.buildReport(inspection, savedAnswers)
    }

    var askingPriceMillionsInput by remember(inspection.id) {
        mutableStateOf(
            if (inspection.sellerAskingPriceMillions > 0.0) {
                val m = inspection.sellerAskingPriceMillions
                if (m % 1.0 == 0.0) m.toInt().toString() else m.toString()
            } else ""
        )
    }
    var mechanicQuoteDzdInput by remember(inspection.id) {
        mutableStateOf(
            if (inspection.userQuotedRepairDzd > 0) inspection.userQuotedRepairDzd.toString() else ""
        )
    }

    val askingMillions = askingPriceMillionsInput.toDoubleOrNull() ?: 0.0
    val mechanicQuoteDzd = mechanicQuoteDzdInput.toIntOrNull() ?: 0

    var showPassedItems by remember { mutableStateOf(false) }
    var showUncheckedItems by remember { mutableStateOf(true) }

    var pendingDownloadPdfFile by remember { mutableStateOf<File?>(null) }
    var savedDownloadUri by remember { mutableStateOf<Uri?>(null) }
    var savedDownloadFileName by remember { mutableStateOf<String?>(null) }

    val downloadDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val destinationUri = result.data?.data
        val sourceFile = pendingDownloadPdfFile
        if (result.resultCode == Activity.RESULT_OK && destinationUri != null && sourceFile != null) {
            val success = PdfReportGenerator.writePdfToDestinationUri(
                context = context,
                sourcePdfFile = sourceFile,
                destinationUri = destinationUri
            )
            if (success) {
                savedDownloadUri = destinationUri
                savedDownloadFileName = sourceFile.name
                val msg = when (language) {
                    AppLanguage.AR -> "تم حفظ التقرير بنجاح (${sourceFile.name})"
                    AppLanguage.FR -> "Rapport PDF enregistré avec succès (${sourceFile.name})"
                    AppLanguage.EN -> "PDF report saved successfully (${sourceFile.name})"
                }
                onPdfGeneratedNotification(msg)
            } else {
                val errMsg = when (language) {
                    AppLanguage.AR -> "تعذر حفظ ملف PDF في المجلد المختار."
                    AppLanguage.FR -> "Impossible d'enregistrer le fichier PDF à l'emplacement choisi."
                    AppLanguage.EN -> "Could not save the PDF file to the selected location."
                }
                onPdfGeneratedNotification(errMsg)
            }
        }
    }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "التقرير النهائي لفحص السيارة"
                            AppLanguage.FR -> "Rapport Final d'Inspection"
                            AppLanguage.EN -> "Final Inspection Report"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val odoPart = if (inspection.odometerKm > 0) " • ${inspection.odometerKm} km" else ""
                    Text(
                        text = "${inspection.carBrand} ${inspection.carModel} (${inspection.carYear})$odoPart • ${inspection.fuelType} • ${inspection.gearboxType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }
            }
        }

        // 2. Score Gauge, Verdict & Critical Cap Card (shows both raw and capped scores + 1-line explanation)
        item {
            ScoreVerdictHeroCard(
                report = report,
                language = language
            )
        }

        // 2b. Phase 7 (Item 6): Plain-Language Summary Card (3-4 sentences from deterministic templates in 3 languages)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plain_language_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald50),
                border = BorderStroke(1.5.dp, Emerald600)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Emerald800,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "ملخص الفحص بلغة مبسطة (نقاط القوة، العيوب، وما تسأل عنه الميكانيكي)"
                                AppLanguage.FR -> "Résumé en langage clair (Points forts, défauts & questions au mécanicien)"
                                AppLanguage.EN -> "Plain-Language Summary (Strengths, Problems & What to Ask a Mechanic)"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald900
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = report.getPlainLanguageSummary(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate900,
                        lineHeight = 21.sp,
                        modifier = Modifier.testTag("plain_language_summary_text")
                    )
                }
            }
        }

        // 2c. Phase 8: Scores per Area Card (horizontal bars matching the PDF report)
        if (report.areaScores.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scores_per_area_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "النتائج حسب قسم الفحص"
                                AppLanguage.FR -> "Scores par zone d'inspection"
                                AppLanguage.EN -> "Scores per Inspection Area"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        report.areaScores.forEach { area ->
                            val scorePct = area.scorePercent
                            val barColor = when {
                                scorePct == null -> Slate300
                                scorePct >= 75 -> StatusPassGreen
                                scorePct >= 50 -> StatusWarningAmber
                                else -> StatusErrorRed
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = area.getTitle(language),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate800,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = if (scorePct != null) {
                                            "$scorePct% (${area.passedCount}/${area.checkedCount})"
                                        } else {
                                            when (language) {
                                                AppLanguage.AR -> "غير مفحوص (0/${area.totalCount})"
                                                AppLanguage.FR -> "Non vérifié (0/${area.totalCount})"
                                                AppLanguage.EN -> "Not checked (0/${area.totalCount})"
                                            }
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (scorePct != null) barColor else Slate600
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (scorePct ?: 0) / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = barColor,
                                    trackColor = Slate100
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Phase 8 Primary Actions: Download PDF + Share PDF + Post-Save Confirmation with "Open" Action
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
                    val suggestedFileName = remember(inspection) {
                        PdfReportGenerator.buildSuggestedFileName(inspection)
                    }
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "تقرير PDF الاحترافي ($suggestedFileName)"
                            AppLanguage.FR -> "Rapport PDF Professionnel ($suggestedFileName)"
                            AppLanguage.EN -> "Professional PDF Report ($suggestedFileName)"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Button 1: Download (Android standard "save file" picker defaulting to Documents/Fahs)
                        Button(
                            onClick = {
                                val generatedFile = PdfReportGenerator.generatePdfFile(
                                    context = context,
                                    report = report,
                                    language = language,
                                    sellerAskingMillions = askingMillions.takeIf { it > 0.0 },
                                    mechanicQuotedRepairDzd = mechanicQuoteDzd.takeIf { it > 0 }
                                )
                                pendingDownloadPdfFile = generatedFile
                                val createIntent = PdfReportGenerator.createDownloadDocumentIntent(generatedFile.name)
                                try {
                                    downloadDocumentLauncher.launch(createIntent)
                                } catch (_: Exception) {
                                    val msg = when (language) {
                                        AppLanguage.AR -> "تم تحضير ملف PDF (${generatedFile.name})"
                                        AppLanguage.FR -> "Fichier PDF prêt (${generatedFile.name})"
                                        AppLanguage.EN -> "PDF file ready (${generatedFile.name})"
                                    }
                                    onPdfGeneratedNotification(msg)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("download_pdf_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "تنزيل (حفظ)"
                                    AppLanguage.FR -> "Télécharger"
                                    AppLanguage.EN -> "Download"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Button 2: Share (Android share sheet with PDF attached for WhatsApp, email, etc.)
                        Button(
                            onClick = {
                                val file = PdfReportGenerator.sharePdfReport(
                                    context = context,
                                    report = report,
                                    language = language,
                                    sellerAskingMillions = askingMillions.takeIf { it > 0.0 },
                                    mechanicQuotedRepairDzd = mechanicQuoteDzd.takeIf { it > 0 }
                                )
                                val msg = when (language) {
                                    AppLanguage.AR -> "تم إنشاء تقرير PDF جاهز للمشاركة (${file.name})"
                                    AppLanguage.FR -> "Rapport PDF généré et prêt à partager (${file.name})"
                                    AppLanguage.EN -> "PDF report generated and ready to share (${file.name})"
                                }
                                onPdfGeneratedNotification(msg)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("export_pdf_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "مشاركة PDF"
                                    AppLanguage.FR -> "Partager"
                                    AppLanguage.EN -> "Share"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Post-save confirmation banner with "Open" action
                    AnimatedVisibility(visible = savedDownloadUri != null) {
                        val currentSavedUri = savedDownloadUri
                        val currentSavedName = savedDownloadFileName ?: suggestedFileName
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("downloaded_pdf_confirmation_card"),
                            shape = RoundedCornerShape(12.dp),
                            color = StatusPassGreenContainer,
                            border = BorderStroke(1.dp, StatusPassGreen)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusPassGreenOnContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = when (language) {
                                                AppLanguage.AR -> "تم حفظ التقرير على الهاتف"
                                                AppLanguage.FR -> "Rapport enregistré sur le téléphone"
                                                AppLanguage.EN -> "Report saved on phone"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusPassGreenOnContainer
                                        )
                                        Text(
                                            text = currentSavedName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate800,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (currentSavedUri != null) {
                                            val opened = PdfReportGenerator.openSavedPdfUri(
                                                context = context,
                                                pdfUri = currentSavedUri,
                                                language = language
                                            )
                                            if (!opened) {
                                                val noViewerMsg = when (language) {
                                                    AppLanguage.AR -> "لا يوجد تطبيق قارئ PDF مثبت على الهاتف."
                                                    AppLanguage.FR -> "Aucune application de lecture PDF installée."
                                                    AppLanguage.EN -> "No PDF viewer app installed on this phone."
                                                }
                                                onPdfGeneratedNotification(noViewerMsg)
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("open_downloaded_pdf_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "فتح"
                                            AppLanguage.FR -> "Ouvrir"
                                            AppLanguage.EN -> "Open"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
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

        // 4. Dedicated OBD2 Diagnostic Block in the Report (Phase 5)
        item {
            ObdReportBlockCard(
                obdBlock = report.obdBlock,
                language = language,
                onOpenObdScanner = onOpenObdScanner
            )
        }

        // 5. Mechanic Quote & Price Negotiation Helper (Phase 5 Option b: user-entered mechanic quote only)
        item {
            NegotiationCalculatorCard(
                askingPriceMillionsInput = askingPriceMillionsInput,
                mechanicQuoteDzdInput = mechanicQuoteDzdInput,
                onAskingPriceChange = { newPrice ->
                    askingPriceMillionsInput = newPrice
                    onSaveNegotiationInputs(newPrice.toDoubleOrNull() ?: 0.0, mechanicQuoteDzd)
                },
                onMechanicQuoteChange = { newQuote ->
                    mechanicQuoteDzdInput = newQuote
                    onSaveNegotiationInputs(askingMillions, newQuote.toIntOrNull() ?: 0)
                },
                language = language
            )
        }

        // 6. Optional Plate Label & General Notes Summary Card
        item {
            IdentityAndPaperworkReportCard(
                report = report,
                language = language
            )
        }

        // 7. Critical Issues Section (Red)
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

        // 8. Important & Minor Issues to Negotiate Section (Amber)
        if (report.negotiableProblems.isNotEmpty()) {
            item {
                FindingsSectionCard(
                    title = when (language) {
                        AppLanguage.AR -> "نقاط للتفاوض والصيانة (${report.negotiableProblems.size})"
                        AppLanguage.FR -> "Points à négocier / Réparations à chiffrer (${report.negotiableProblems.size})"
                        AppLanguage.EN -> "Items to Negotiate / Repairs to Quote (${report.negotiableProblems.size})"
                    },
                    subtitle = when (language) {
                        AppLanguage.AR -> "اطلب من الميكانيكي تسعير هذه النقاط واخصم تكلفتها من السعر المطلوب"
                        AppLanguage.FR -> "Faites chiffrer ces défauts par votre mécanicien et déduisez-les du prix demandé"
                        AppLanguage.EN -> "Ask your mechanic to quote these items and subtract the quote from the asking price"
                    },
                    items = report.negotiableProblems,
                    isCritical = false,
                    language = language
                )
            }
        }

        // 9. Honest "Could Not Check" Section
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
                                        AppLanguage.AR -> "تم فحص جميع النقاط المبرمجة للسيارة بنسبة 100% (0 نقطة غير مفحوصة) ✓"
                                        AppLanguage.FR -> "Tous les points prévus ont été vérifiés (0 point non vérifié) ✓"
                                        AppLanguage.EN -> "All scheduled items were verified (0 items not checked) ✓"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusPassGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                if (report.uncheckedCriticalItems.isNotEmpty()) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = StatusErrorRedContainer,
                                        border = BorderStroke(1.dp, StatusErrorRed)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.AR -> "نقاط حرجة لم يتم فحصها (${report.uncheckedCriticalItems.size}) — يجب فحصها لإظهار النتيجة:"
                                                    AppLanguage.FR -> "Points CRITIQUES non vérifiés (${report.uncheckedCriticalItems.size}) — requis pour calculer le score :"
                                                    AppLanguage.EN -> "Unchecked CRITICAL items (${report.uncheckedCriticalItems.size}) — required before a score is shown:"
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = StatusErrorRedOnContainer
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            report.uncheckedCriticalItems.forEach { crit ->
                                                Text(
                                                    text = "⚠️ [${crit.getSectionTitle(language)}] ${crit.getQuestion(language)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = StatusErrorRedOnContainer,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "عدد النقاط غير المفحوصة: ${report.uncheckedItems.size} من أصل ${report.totalApplicableCount} (مستبعدة من النتيجة):"
                                        AppLanguage.FR -> "${report.uncheckedItems.size} point(s) sur ${report.totalApplicableCount} non vérifié(s) (exclus du score) :"
                                        AppLanguage.EN -> "${report.uncheckedItems.size} of ${report.totalApplicableCount} item(s) not checked (left out of the score):"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                report.uncheckedItems.forEach { item ->
                                    val critPrefix = if (item.severity == ItemSeverity.CRITICAL) "[CRITIQUE] " else ""
                                    Text(
                                        text = "• $critPrefix[${item.getSectionTitle(language)}] ${item.getQuestion(language)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (item.severity == ItemSeverity.CRITICAL) StatusErrorRed else Slate700,
                                        fontWeight = if (item.severity == ItemSeverity.CRITICAL) FontWeight.SemiBold else FontWeight.Normal,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 10. Collapsible Passed Items Section
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
    val scoreVal = report.overallScore
    val rawVal = report.rawUncappedScore
    val ringColor = when (report.verdict) {
        "INCOMPLETE" -> Color(0xFF94A3B8)
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
                        if (scoreVal != null) {
                            drawArc(
                                color = ringColor,
                                startAngle = -90f,
                                sweepAngle = (scoreVal / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = 22f, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (scoreVal != null) "$scoreVal" else "—",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = if (scoreVal != null) {
                                "/ 100"
                            } else {
                                when (language) {
                                    AppLanguage.AR -> "غير مكتمل"
                                    AppLanguage.FR -> "Incomplet"
                                    AppLanguage.EN -> "Incomplete"
                                }
                            },
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

                    // Show both numbers (raw score and capped score) when critical cap is triggered
                    if (report.isCriticalCapTriggered && rawVal != null && scoreVal != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.testTag("raw_vs_capped_score_badge")
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "النتيجة الخام: $rawVal/100 ← النتيجة المقيّدة: $scoreVal/100"
                                    AppLanguage.FR -> "Score brut : $rawVal/100 → Score plafonné : $scoreVal/100"
                                    AppLanguage.EN -> "Raw score: $rawVal/100 → Capped score: $scoreVal/100"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "تم فحص ${report.checkedCount}/${report.totalApplicableCount} • غير مفحوص: ${report.uncheckedItems.size}"
                            AppLanguage.FR -> "Vérifiés : ${report.checkedCount}/${report.totalApplicableCount} • Non vérifiés : ${report.uncheckedItems.size}"
                            AppLanguage.EN -> "Checked: ${report.checkedCount}/${report.totalApplicableCount} • Not checked: ${report.uncheckedItems.size}"
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

            val capExplanation = report.getCriticalCapOneLineExplanation(language)
            if (capExplanation != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("critical_cap_explanation_banner"),
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
                            text = capExplanation,
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
private fun ObdReportBlockCard(
    obdBlock: ObdReportBlockData,
    language: AppLanguage,
    onOpenObdScanner: () -> Unit
) {
    val borderColor = when {
        !obdBlock.isScanned -> Slate200
        obdBlock.hasCriticalAlert -> StatusErrorRed
        else -> Emerald600
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("obd_report_block_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = if (obdBlock.hasCriticalAlert) StatusErrorRed else Emerald700,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "نتائج الفحص الإلكتروني للكمبيوتر (OBD2)"
                            AppLanguage.FR -> "Résultats du Diagnostic OBD2"
                            AppLanguage.EN -> "OBD2 Diagnostic Findings"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                if (!obdBlock.isScanned) {
                    OutlinedButton(
                        onClick = onOpenObdScanner,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "إجراء الفحص"
                                AppLanguage.FR -> "Lancer scan"
                                AppLanguage.EN -> "Run Scan"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            HorizontalDivider(color = Slate100)

            ObdReportRow(
                label = when (language) {
                    AppLanguage.AR -> "1. حالة الاتصال بالكمبيوتر"
                    AppLanguage.FR -> "1. État de la connexion"
                    AppLanguage.EN -> "1. Connection status"
                },
                value = obdBlock.getConnectionStatusText(language),
                isWarning = obdBlock.isScanned && !obdBlock.isCommunicationSuccess
            )

            ObdReportRow(
                label = when (language) {
                    AppLanguage.AR -> "2. أكواد الأعطال المسجلة"
                    AppLanguage.FR -> "2. Codes défauts trouvés"
                    AppLanguage.EN -> "2. Fault codes found"
                },
                value = obdBlock.getFaultCodesText(language),
                isWarning = obdBlock.dtcCount > 0
            )

            ObdReportRow(
                label = when (language) {
                    AppLanguage.AR -> "3. حالة مسح الذاكرة الحديث"
                    AppLanguage.FR -> "3. Statut d'effacement des codes"
                    AppLanguage.EN -> "3. Cleared-codes status"
                },
                value = obdBlock.getClearedCodesText(language),
                isWarning = obdBlock.clearedCodesStatus == "STRONG_WARNING" || obdBlock.clearedCodesStatus == "CANNOT_RULE_OUT"
            )

            ObdReportRow(
                label = when (language) {
                    AppLanguage.AR -> "4. فحص لمبة المحرك (Check Engine)"
                    AppLanguage.FR -> "4. Contrôle du voyant Check-Engine"
                    AppLanguage.EN -> "4. Check-Engine light check"
                },
                value = obdBlock.getCheckEngineLightText(language),
                isWarning = obdBlock.isScanned && (!obdBlock.milBulbLitAtKeyOn || obdBlock.milCommandedByEcu == "ON")
            )

            ObdReportRow(
                label = when (language) {
                    AppLanguage.AR -> "5. مقارنة رقم الشاسيه الإلكتروني (09 02)"
                    AppLanguage.FR -> "5. Comparaison du N° de châssis (09 02)"
                    AppLanguage.EN -> "5. Chassis-number comparison (09 02)"
                },
                value = obdBlock.getChassisComparisonText(language),
                isWarning = obdBlock.ecuVinComparison == "DIFFERENT"
            )
        }
    }
}

@Composable
private fun ObdReportRow(
    label: String,
    value: String,
    isWarning: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Slate600
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isWarning) FontWeight.Bold else FontWeight.Medium,
            color = if (isWarning) StatusErrorRed else Slate900
        )
    }
}

@Composable
private fun NegotiationCalculatorCard(
    askingPriceMillionsInput: String,
    mechanicQuoteDzdInput: String,
    onAskingPriceChange: (String) -> Unit,
    onMechanicQuoteChange: (String) -> Unit,
    language: AppLanguage
) {
    val askingMillions = askingPriceMillionsInput.toDoubleOrNull() ?: 0.0
    val mechanicQuoteDzd = mechanicQuoteDzdInput.toIntOrNull() ?: 0
    val targetOfferDzd = ReportEngine.calculateFairOfferFromMechanicQuoteDzd(
        askingPriceMillions = askingMillions,
        mechanicQuotedRepairDzd = mechanicQuoteDzd
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
                        AppLanguage.AR -> "حاسبة العرض العادل (حسب تسعيرة الميكانيكي الخاص بك)"
                        AppLanguage.FR -> "Calculateur d'offre (selon le devis de votre mécanicien)"
                        AppLanguage.EN -> "Fair Offer Calculator (from your mechanic's quote)"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (language) {
                    AppLanguage.AR -> "لا يخصم التطبيق مبالغ تقديرية غير معتمدة تلقائياً. أدخل السعر المطلوب من البائع وتكلفة الإصلاح التي حددها الميكانيكي الخاص بك بعد معاينة الأعطال لحساب السعر العادل."
                    AppLanguage.FR -> "L'application ne déduit aucun montant automatique non vérifié. Saisissez le prix demandé par le vendeur et le devis réel de votre mécanicien pour calculer votre offre."
                    AppLanguage.EN -> "The app does not subtract unverified automatic repair estimates. Enter the seller's asking price and the repair cost quoted by your own mechanic to calculate your offer."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Slate600,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Seller Asking Price Input in Millions de centimes
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
                                AppLanguage.EN -> "Seller's Asking Price (Millions)"
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

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Mechanic's Quoted Repair Cost Input in DZD
            OutlinedTextField(
                value = mechanicQuoteDzdInput,
                onValueChange = { newVal ->
                    if (newVal.all { it.isDigit() } && newVal.length <= 8) {
                        onMechanicQuoteChange(newVal)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mechanic_repair_quote_dzd_input"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = Emerald700
                    )
                },
                label = {
                    Text(
                        when (language) {
                            AppLanguage.AR -> "تكلفة الإصلاح حسب تسعيرة الميكانيكي (بالدينار DZD)"
                            AppLanguage.FR -> "Coût des réparations chiffré par votre mécanicien (en DZD)"
                            AppLanguage.EN -> "Repair Cost Quoted by Your Mechanic (in DZD)"
                        }
                    )
                },
                supportingText = {
                    if (mechanicQuoteDzd > 0) {
                        Text(
                            text = "= ${ReportEngine.formatAlgerianCurrency(mechanicQuoteDzd, language)}",
                            color = Emerald800,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "مثال: 45000 (أي 4.5 مليون سنتيم)"
                                AppLanguage.FR -> "Exemple : 45000 (soit 4.5 Millions de centimes)"
                                AppLanguage.EN -> "Example: 45000 (= 4.5 Million centimes)"
                            },
                            color = Slate600
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald700,
                    unfocusedBorderColor = Slate300
                )
            )

            if (askingMillions > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fair_offer_result_box"),
                    shape = RoundedCornerShape(12.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "السعر المستهدف بعد خصم تسعيرة الميكانيكي:"
                                AppLanguage.FR -> "Offre cible (Prix demandé − Devis mécanicien) :"
                                AppLanguage.EN -> "Target Fair Offer (Asking Price − Mechanic Quote):"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ReportEngine.formatMillionsCentimes(targetOfferDzd, language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald800
                        )
                        Text(
                            text = "(${ReportEngine.formatDzd(targetOfferDzd)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }
                }
            }
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
                        AppLanguage.AR -> "تسمية السيارة والملاحظات العامة"
                        AppLanguage.FR -> "Repère d'immatriculation & Notes générales"
                        AppLanguage.EN -> "Optional Plate Label & General Notes"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val plateDisplay = report.inspection.plateNumber.ifBlank {
                when (language) {
                    AppLanguage.AR -> "بدون تسمية اختيارية"
                    AppLanguage.FR -> "Aucun repère saisi"
                    AppLanguage.EN -> "No label entered"
                }
            }

            Text(
                text = when (language) {
                    AppLanguage.AR -> "اللوحة / التسمية الاختيارية: $plateDisplay"
                    AppLanguage.FR -> "Repère / Immatriculation : $plateDisplay"
                    AppLanguage.EN -> "Optional plate / label: $plateDisplay"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

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
                Column(modifier = Modifier.weight(1f)) {
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
                                color = Slate700,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            if (item.maxCostDzd > 0) {
                                Text(
                                    text = "${item.minCostDzd / 1000}k - ${item.maxCostDzd / 1000}k DZD",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isCritical) StatusErrorRed else StatusWarningAmberOnContainer,
                                    maxLines = 1,
                                    softWrap = false
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

                        val realThumb = remember(item.photoUri) {
                            InspectionPhotoHelper.loadThumbnailBitmap(item.photoUri, maxDimensionPx = 280)
                        }
                        if (item.buyerNote.isNotBlank() || realThumb != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (realThumb != null) {
                                    Image(
                                        bitmap = realThumb.asImageBitmap(),
                                        contentDescription = "Inspection evidence photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Emerald600, RoundedCornerShape(8.dp))
                                    )
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
