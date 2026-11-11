package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.WarningAmber
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
import com.example.data.local.InspectionEntity
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
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusWarningAmber

@Composable
fun CarComparisonDialog(
    inspections: List<InspectionEntity>,
    language: AppLanguage,
    onSelectCarReport: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Rank cars by: 1) highest overallScore, 2) lowest totalEstimatedCostDzd, 3) lowest problemCount
    val bestInspectionId = remember(inspections) {
        inspections.maxWithOrNull(
            compareBy<InspectionEntity> { it.overallScore ?: 50 }
                .thenByDescending { -it.totalEstimatedCostDzd }
                .thenByDescending { -it.problemCount }
        )?.id
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Column {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "مقارنة السيارات (${inspections.size})"
                                    AppLanguage.FR -> "Comparateur Côte à Côte (${inspections.size})"
                                    AppLanguage.EN -> "Side-by-Side Comparison (${inspections.size})"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "قارن النتيجة، تكلفة الإصلاح بالـ سنتيم، وعدد الأعطال"
                                    AppLanguage.FR -> "Score, frais en Millions de centimes et défauts"
                                    AppLanguage.EN -> "Compare score, repair costs in DZD/Centimes & defects"
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
                    val bestCar = inspections.firstOrNull { it.id == bestInspectionId }
                    if (bestCar != null) {
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
                                Column {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "الخيار الأفضل للشراء: ${bestCar.carBrand} ${bestCar.carModel} (${bestCar.carYear})"
                                            AppLanguage.FR -> "Meilleur choix : ${bestCar.carBrand} ${bestCar.carModel} (${bestCar.carYear})"
                                            AppLanguage.EN -> "Safest Pick: ${bestCar.carBrand} ${bestCar.carModel} (${bestCar.carYear})"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald900
                                    )
                                    val costStr = ReportEngine.formatAlgerianCurrency(
                                        bestCar.totalEstimatedCostDzd,
                                        language
                                    )
                                    Text(
                                        text = when (language) {
                                            AppLanguage.AR -> "أعلى تقييم (${bestCar.overallScore ?: "--"}/100) مع ${bestCar.problemCount} أعطال وتكلفة إصلاح تقديرية $costStr"
                                            AppLanguage.FR -> "Meilleur score (${bestCar.overallScore ?: "--"}/100) avec ${bestCar.problemCount} défaut(s) et frais estimés à $costStr"
                                            AppLanguage.EN -> "Highest score (${bestCar.overallScore ?: "--"}/100) with ${bestCar.problemCount} defect(s) and est. repairs of $costStr"
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
                        inspections.forEach { car ->
                            val isBest = car.id == bestInspectionId
                            ComparisonCarColumn(
                                car = car,
                                isBestPick = isBest,
                                language = language,
                                onOpenReport = {
                                    onDismiss()
                                    onSelectCarReport(car.id)
                                },
                                modifier = Modifier.width(260.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonCarColumn(
    car: InspectionEntity,
    isBestPick: Boolean,
    language: AppLanguage,
    onOpenReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val score = car.overallScore ?: 100
    val scoreColor = when {
        score >= 78 -> StatusPassGreen
        score >= 50 -> StatusWarningAmber
        else -> StatusErrorRed
    }

    val verdictLabel = when (car.verdict?.uppercase()) {
        "GOOD" -> when (language) {
            AppLanguage.AR -> "حالة جيدة"
            AppLanguage.FR -> "Bon état"
            AppLanguage.EN -> "Good Condition"
        }
        "NEGOTIATE" -> when (language) {
            AppLanguage.AR -> "تفاوض على السعر"
            AppLanguage.FR -> "À négocier"
            AppLanguage.EN -> "Negotiate Price"
        }
        "AVOID" -> when (language) {
            AppLanguage.AR -> "تجنب الشراء / خطر"
            AppLanguage.FR -> "À éviter / Risqué"
            AppLanguage.EN -> "Avoid / High Risk"
        }
        else -> when (language) {
            AppLanguage.AR -> "قيد الفحص"
            AppLanguage.FR -> "En cours"
            AppLanguage.EN -> "In Progress"
        }
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
                                AppLanguage.AR -> "الأفضل للشراء"
                                AppLanguage.FR -> "MEILLEUR CHOIX"
                                AppLanguage.EN -> "BEST PICK"
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

            // Car Title & Year
            Column {
                Text(
                    text = "${car.carBrand} ${car.carModel}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "${car.carGeneration} • ${car.carYear}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }

            // Score Box
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
                        text = "$score / 100",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scoreColor
                    )
                    Text(
                        text = verdictLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }

            HorizontalDivider(color = Slate100)

            // Repair Cost in DZD + Millions de centimes
            ComparisonMetricRow(
                label = when (language) {
                    AppLanguage.AR -> "تكلفة الإصلاح التقديرية"
                    AppLanguage.FR -> "Frais de remise en état"
                    AppLanguage.EN -> "Estimated Repair Cost"
                },
                value = ReportEngine.formatAlgerianCurrency(car.totalEstimatedCostDzd, language),
                valueColor = if (car.totalEstimatedCostDzd > 0) StatusWarningAmber else StatusPassGreen,
                boldValue = true
            )

            // Defects Count
            ComparisonMetricRow(
                label = when (language) {
                    AppLanguage.AR -> "الأعطال المكتشفة"
                    AppLanguage.FR -> "Défauts constatés"
                    AppLanguage.EN -> "Defects Found"
                },
                value = "${car.problemCount}",
                valueColor = if (car.problemCount > 0) StatusErrorRed else StatusPassGreen,
                boldValue = true
            )

            // Checklist Coverage
            ComparisonMetricRow(
                label = when (language) {
                    AppLanguage.AR -> "نقاط الفحص المجابة"
                    AppLanguage.FR -> "Points vérifiés"
                    AppLanguage.EN -> "Items Checked"
                },
                value = "${car.answeredQuestions} / ${car.totalQuestions} (${car.inspectionMode})",
                valueColor = Slate700
            )

            // Fuel & Gearbox
            ComparisonMetricRow(
                label = when (language) {
                    AppLanguage.AR -> "المحرك وعلبة السرعة"
                    AppLanguage.FR -> "Motorisation & Boîte"
                    AppLanguage.EN -> "Fuel & Gearbox"
                },
                value = "${car.fuelType} • ${car.gearboxType}",
                valueColor = Slate700
            )

            // Registration Plate & VIN
            ComparisonMetricRow(
                label = when (language) {
                    AppLanguage.AR -> "الترقيم / الشاسيه VIN"
                    AppLanguage.FR -> "Matricule / VIN"
                    AppLanguage.EN -> "Plate / VIN"
                },
                value = buildString {
                    append(car.plateNumber.ifBlank { "—" })
                    if (car.vin.isNotBlank()) {
                        append("\nVIN: ${car.vin.take(17)}")
                    }
                },
                valueColor = Slate700
            )

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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (boldValue) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
            textAlign = TextAlign.Start
        )
    }
}
