package com.example.ui.components

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.i18n.AppLanguage
import com.example.domain.PdfReportGenerator
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusPassGreenContainer
import com.example.ui.theme.StatusPassGreenOnContainer
import java.io.File

/**
 * Interactive card + full-screen A4 preview dialog that lets the user inspect, download,
 * or share the Phase 8 sample PDF reports in Arabic, French, and English directly on their phone.
 */
@Composable
fun SamplePdfReportsShowcaseCard(
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    var selectedSampleLang by remember { mutableStateOf<AppLanguage?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sample_pdf_reports_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Emerald600)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = Emerald700,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.AR -> "نماذج تقارير PDF الاحترافية (Peugeot 208 2018)"
                            AppLanguage.FR -> "Exemples de Rapports PDF (Peugeot 208 2018)"
                            AppLanguage.EN -> "Sample PDF Reports (Peugeot 208 2018)"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.AR -> "معاينة وتنزيل ومشاركة التقرير التجريبي بالعربية والفرنسية والإنجليزية"
                            AppLanguage.FR -> "Aperçu, téléchargement et partage en Arabe, Français et Anglais"
                            AppLanguage.EN -> "Preview, download & share the sample report in Arabic, French & English"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { selectedSampleLang = AppLanguage.AR },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_sample_pdf_ar_btn"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PDF العربية",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = { selectedSampleLang = AppLanguage.FR },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_sample_pdf_fr_btn"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PDF Français",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = { selectedSampleLang = AppLanguage.EN },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_sample_pdf_en_btn"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PDF English",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }

    val activeSampleLang = selectedSampleLang
    if (activeSampleLang != null) {
        SamplePdfPreviewDialog(
            initialLanguage = activeSampleLang,
            onDismiss = { selectedSampleLang = null }
        )
    }
}

@Composable
fun SamplePdfPreviewDialog(
    initialLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var previewLang by remember { mutableStateOf(initialLanguage) }
    var pendingDownloadFile by remember { mutableStateOf<File?>(null) }
    var savedUri by remember { mutableStateOf<Uri?>(null) }
    var savedFileName by remember { mutableStateOf<String?>(null) }

    val sampleReport = remember(previewLang) {
        PdfReportGenerator.buildSampleTestInspectionReport(context, previewLang)
    }
    val pageBitmaps = remember(previewLang, sampleReport) {
        PdfReportGenerator.renderReportPagesToBitmaps(
            context = context,
            report = sampleReport,
            language = previewLang,
            sellerAskingMillions = 235.0,
            mechanicQuotedRepairDzd = 65000,
            scaleFactor = 2f
        )
    }

    val downloadSampleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val destUri = result.data?.data
        val srcFile = pendingDownloadFile
        if (result.resultCode == Activity.RESULT_OK && destUri != null && srcFile != null) {
            val ok = PdfReportGenerator.writePdfToDestinationUri(context, srcFile, destUri)
            if (ok) {
                savedUri = destUri
                savedFileName = srcFile.name
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .testTag("sample_pdf_preview_dialog"),
            shape = RoundedCornerShape(18.dp),
            color = Slate100
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar with Language Switcher + Close
                Surface(
                    color = Emerald900,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Fahs_Peugeot_208_2018_2026-11-11_${previewLang.code.uppercase()}.pdf",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "A4 • ${pageBitmaps.size} pages • Cairo (AR) / Plus Jakarta Sans (FR/EN)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Emerald100
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("close_sample_pdf_dialog_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Language selector chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                AppLanguage.AR to "العربية (RTL)",
                                AppLanguage.FR to "Français",
                                AppLanguage.EN to "English"
                            ).forEach { (lang, label) ->
                                FilterChip(
                                    selected = previewLang == lang,
                                    onClick = {
                                        previewLang = lang
                                        savedUri = null
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color.White,
                                        selectedLabelColor = Emerald900,
                                        containerColor = Emerald800,
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Download & Share Buttons for this Sample PDF
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val fileName = "Fahs_Peugeot_208_2018_2026-11-11_${previewLang.code.uppercase()}.pdf"
                                    val pdfFile = PdfReportGenerator.generatePdfFile(
                                        context = context,
                                        report = sampleReport,
                                        language = previewLang,
                                        sellerAskingMillions = 235.0,
                                        mechanicQuotedRepairDzd = 65000,
                                        customFileName = fileName
                                    )
                                    pendingDownloadFile = pdfFile
                                    runCatching {
                                        downloadSampleLauncher.launch(
                                            PdfReportGenerator.createDownloadDocumentIntent(fileName)
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Emerald900,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (previewLang) {
                                        AppLanguage.AR -> "تنزيل PDF"
                                        AppLanguage.FR -> "Télécharger"
                                        AppLanguage.EN -> "Download PDF"
                                    },
                                    color = Emerald900,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Button(
                                onClick = {
                                    PdfReportGenerator.sharePdfReport(
                                        context = context,
                                        report = sampleReport,
                                        language = previewLang,
                                        sellerAskingMillions = 235.0,
                                        mechanicQuotedRepairDzd = 65000
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (previewLang) {
                                        AppLanguage.AR -> "مشاركة PDF"
                                        AppLanguage.FR -> "Partager PDF"
                                        AppLanguage.EN -> "Share PDF"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        val currentSavedUri = savedUri
                        if (currentSavedUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StatusPassGreenContainer,
                                border = BorderStroke(1.dp, StatusPassGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = savedFileName ?: "Saved",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusPassGreenOnContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            PdfReportGenerator.openSavedPdfUri(
                                                context = context,
                                                pdfUri = currentSavedUri,
                                                language = previewLang
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when (previewLang) {
                                                AppLanguage.AR -> "فتح"
                                                AppLanguage.FR -> "Ouvrir"
                                                AppLanguage.EN -> "Open"
                                            },
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Scrollable A4 pages preview
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(pageBitmaps) { index, bmp ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = when (previewLang) {
                                    AppLanguage.AR -> "الصفحة ${index + 1} من ${pageBitmaps.size}"
                                    AppLanguage.FR -> "Page ${index + 1} sur ${pageBitmaps.size}"
                                    AppLanguage.EN -> "Page ${index + 1} of ${pageBitmaps.size}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Sample PDF Page ${index + 1}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, Slate200, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                }
            }
        }
    }
}
