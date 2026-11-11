package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.ItemSeverity
import com.example.domain.DtcInfo
import com.example.domain.LiveParameterGauge
import com.example.domain.ObdScanResult
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
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
import com.example.ui.theme.StatusErrorRedContainer
import com.example.ui.theme.StatusErrorRedOnContainer
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusPassGreenContainer
import com.example.ui.theme.StatusPassGreenOnContainer
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.StatusWarningAmberContainer
import com.example.ui.theme.StatusWarningAmberOnContainer

@Composable
fun ObdScanResultSection(
    scanResult: ObdScanResult,
    language: AppLanguage,
    expectedVin: String,
    milBulbLitOnDashboard: Boolean,
    onMilBulbToggle: (Boolean) -> Unit,
    hasActiveInspection: Boolean,
    onSaveToInspection: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Adapter & Protocol Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (scanResult.isCommunicationSuccess) Slate900 else StatusWarningAmberContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (scanResult.isCommunicationSuccess) {
                            when (language) {
                                AppLanguage.AR -> "متصل بكمبيوتر السيارة (قراءة فقط آمنة 100%)"
                                AppLanguage.FR -> "Connecté au calculateur (Lecture seule 100% sûre)"
                                AppLanguage.EN -> "Connected to Vehicle ECU (100% Safe Read-Only)"
                            }
                        } else {
                            when (language) {
                                AppLanguage.AR -> "السيارة لا تستجيب لبروتوكول OBD2 القياسي"
                                AppLanguage.FR -> "Le calculateur ne répond pas au standard OBD2"
                                AppLanguage.EN -> "Vehicle ECU Does Not Respond to Standard OBD2"
                            }
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (scanResult.isCommunicationSuccess) Color.White else StatusWarningAmberOnContainer
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${scanResult.adapterVersion} • ${scanResult.detectedProtocol}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = if (scanResult.isCommunicationSuccess) Emerald200 else StatusWarningAmberOnContainer
                )

                if (!scanResult.isCommunicationSuccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "هذا طبيعي في بعض السيارات القديمة (قبل 2004). يمكنك متابعة الفحص اليدوي الكامل بدون مشكلة، ولن تتعطل أي ميزة في التطبيق."
                            AppLanguage.FR -> "C'est fréquent sur certains véhicules anciens (avant 2004). Vous pouvez poursuivre l'inspection physique complète normalement."
                            AppLanguage.EN -> "This is normal on some older cars (pre-2004). You can continue the full physical inspection without any issue."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusWarningAmberOnContainer
                    )
                }
            }
        }

        if (!scanResult.isCommunicationSuccess) {
            if (hasActiveInspection && !scanResult.isSimulated) {
                SaveObdToInspectionButton(
                    language = language,
                    onClick = onSaveToInspection
                )
            }
            return@Column
        }

        // 2. Prominent Cleared-Code Fraud Detection Card
        ClearedCodeFraudCard(
            scanResult = scanResult,
            language = language
        )

        // 3. Check-Engine (MIL) Bulb Tamper Check
        MilBulbTamperCard(
            milCommandedOn = scanResult.milCommandedOn,
            milBulbLitOnDashboard = milBulbLitOnDashboard,
            onMilBulbToggle = onMilBulbToggle,
            language = language
        )

        // 4. Stored, Pending & Permanent Fault Codes
        DtcFaultListCard(
            dtcList = scanResult.dtcList,
            freezeFrameSummary = scanResult.freezeFrameSummary,
            language = language
        )

        // 5. ECU VIN Consistency Check
        if (!scanResult.ecuVin.isNullOrBlank()) {
            EcuVinComparisonCard(
                ecuVin = scanResult.ecuVin,
                expectedVin = expectedVin,
                language = language
            )
        }

        // 6. 60-Second Guided Live Engine Health Gauges
        if (scanResult.liveParameters.isNotEmpty()) {
            LiveGaugesCard(
                gauges = scanResult.liveParameters,
                language = language
            )
        }

        // 7. Brand-Specific Secondary ECU Odometer Architecture Info
        SecondaryEcuMileageInfoCard(language = language)

        // 8. Save Scan to Active Inspection ( strictly blocked for simulated results )
        if (hasActiveInspection && !scanResult.isSimulated) {
            SaveObdToInspectionButton(
                language = language,
                onClick = onSaveToInspection
            )
        }
    }
}

@Composable
private fun ClearedCodeFraudCard(
    scanResult: ObdScanResult,
    language: AppLanguage
) {
    val isFraud = scanResult.isRecentlyClearedFraudAlert
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("obd_cleared_fraud_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFraud) StatusErrorRedContainer else StatusPassGreenContainer
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isFraud) StatusErrorRed else StatusPassGreen
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isFraud) Icons.Default.Warning else Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = if (isFraud) StatusErrorRed else StatusPassGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isFraud) {
                        when (language) {
                            AppLanguage.AR -> "تنبيه هام: تم مسح ذاكرة الأعطال قبل البيع بوقت قصير جداً!"
                            AppLanguage.FR -> "Alerte : Mémoire des défauts effacée très récemment !"
                            AppLanguage.EN -> "Alert: Fault Memory Was Cleared Very Recently!"
                        }
                    } else {
                        when (language) {
                            AppLanguage.AR -> "ذاكرة الكمبيوتر لم يتم مسحها حديثاً (سجل موثوق)"
                            AppLanguage.FR -> "Mémoire non effacée récemment (Historique fiable)"
                            AppLanguage.EN -> "Memory Not Recently Cleared (Reliable History)"
                        }
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isFraud) StatusErrorRedOnContainer else StatusPassGreenOnContainer
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Metric Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FraudMetricPill(
                    label = when (language) {
                        AppLanguage.AR -> "المسافة منذ المسح"
                        AppLanguage.FR -> "Distance depuis RAZ"
                        AppLanguage.EN -> "Distance since clear"
                    },
                    value = "${scanResult.distanceSinceCodesClearedKm} km",
                    isSuspicious = scanResult.distanceSinceCodesClearedKm < 50,
                    modifier = Modifier.weight(1f)
                )
                FraudMetricPill(
                    label = when (language) {
                        AppLanguage.AR -> "دورات التسخين"
                        AppLanguage.FR -> "Cycles de chauffe"
                        AppLanguage.EN -> "Warm-up cycles"
                    },
                    value = "${scanResult.warmupsSinceCodesCleared}",
                    isSuspicious = scanResult.warmupsSinceCodesCleared < 3,
                    modifier = Modifier.weight(1f)
                )
                FraudMetricPill(
                    label = when (language) {
                        AppLanguage.AR -> "اختبارات غير مكتملة"
                        AppLanguage.FR -> "Tests incomplets"
                        AppLanguage.EN -> "Incomplete monitors"
                    },
                    value = "${scanResult.incompleteReadinessMonitors}/${scanResult.totalReadinessMonitors}",
                    isSuspicious = scanResult.incompleteReadinessMonitors >= 3,
                    modifier = Modifier.weight(1f)
                )
            }

            if (isFraud) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "رغم أن عدد الأعطال الظاهرة هو 0، إلا أن كمبيوتر السيارة يخبرنا أنه تم مسح الذاكرة قبل ${scanResult.distanceSinceCodesClearedKm} كم فقط (${scanResult.warmupsSinceCodesCleared} دورة تسخين). لم تسر السيارة مسافة كافية لإعادة فحص الحساسات. يُنصح بقيادة السيارة لمدة 20-30 دقيقة وإعادة الفحص للتأكد من عدم عودة لمبة المحرك."
                        AppLanguage.FR -> "Bien qu'aucun code confirmé ne s'affiche, le calculateur indique que la mémoire a été remise à zéro il y a seulement ${scanResult.distanceSinceCodesClearedKm} km (${scanResult.warmupsSinceCodesCleared} cycle). La voiture n'a pas assez roulé pour que d'éventuels défauts réapparaissent. Faites un essai routier de 20 à 30 minutes puis relancez le scan."
                        AppLanguage.EN -> "Although 0 stored codes are shown, the ECU reports its memory was cleared only ${scanResult.distanceSinceCodesClearedKm} km ago (${scanResult.warmupsSinceCodesCleared} warmup cycle). The car has not been driven enough for hidden faults to reappear. Take a 20–30 minute test drive and scan again."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = StatusErrorRedOnContainer,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun FraudMetricPill(
    label: String,
    value: String,
    isSuspicious: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.85f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSuspicious) StatusErrorRed else Slate900
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Slate700
            )
        }
    }
}

@Composable
private fun MilBulbTamperCard(
    milCommandedOn: Boolean,
    milBulbLitOnDashboard: Boolean,
    onMilBulbToggle: (Boolean) -> Unit,
    language: AppLanguage
) {
    val isTampered = !milBulbLitOnDashboard
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isTampered) StatusErrorRed else Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "فحص لمبة المحرك (Check Engine) في الطابلوه"
                        AppLanguage.FR -> "Vérification du voyant Check Engine au tableau de bord"
                        AppLanguage.EN -> "Dashboard Check-Engine Bulb Verification"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (milCommandedOn) StatusErrorRedContainer else StatusPassGreenContainer
                ) {
                    Text(
                        text = if (milCommandedOn) "ECU MIL: ON" else "ECU MIL: OFF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (milCommandedOn) StatusErrorRedOnContainer else StatusPassGreenOnContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (language) {
                    AppLanguage.AR -> "عند وضع المفتاح في Contact (قبل تشغيل المحرك)، هل تضيء لمبة المحرك البرتقالية في الطابلوه؟"
                    AppLanguage.FR -> "Quand vous mettez le contact (moteur arrêté), le voyant moteur orange s'allume-t-il sur le compteur ?"
                    AppLanguage.EN -> "When you turn the ignition ON (engine off), does the orange check-engine light illuminate on the dashboard?"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Slate700
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onMilBulbToggle(true) },
                    color = if (milBulbLitOnDashboard) StatusPassGreen else Slate100,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "نعم، تضيء طبيعياً ✓"
                            AppLanguage.FR -> "Oui, il s'allume ✓"
                            AppLanguage.EN -> "Yes, it lights up ✓"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (milBulbLitOnDashboard) Color.White else Slate700,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onMilBulbToggle(false) }
                        .testTag("mil_bulb_tampered_btn"),
                    color = if (!milBulbLitOnDashboard) StatusErrorRed else Slate100,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "لا، لا تضيء أبداً ✗"
                            AppLanguage.FR -> "Non, jamais allumé ✗"
                            AppLanguage.EN -> "No, never lights up ✗"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!milBulbLitOnDashboard) Color.White else Slate700,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                    )
                }
            }

            if (isTampered) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = StatusErrorRedContainer
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "حيلة شائعة جداً: عدم إضاءة لمبة المحرك عند وضع Contact يعني غالباً أنه تم نزع اللمبة (LED) أو تغطيتها بشريط لاصق أسود داخل العداد لإخفاء عطل دائم!"
                            AppLanguage.FR -> "Attention : Si le voyant ne s'allume pas au contact, la LED du compteur a probablement été masquée avec du ruban adhésif ou dessoudée pour cacher un défaut moteur."
                            AppLanguage.EN -> "Warning: If the check-engine light does not illuminate with ignition ON, the dashboard LED may have been covered with tape or disabled to hide a fault."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusErrorRedOnContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DtcFaultListCard(
    dtcList: List<DtcInfo>,
    freezeFrameSummary: String?,
    language: AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (language) {
                    AppLanguage.AR -> "أكواد الأعطال المسجلة (${dtcList.size})"
                    AppLanguage.FR -> "Codes défauts détectés (${dtcList.size})"
                    AppLanguage.EN -> "Detected Trouble Codes (${dtcList.size})"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (dtcList.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = StatusPassGreenContainer
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusPassGreen
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "لا توجد أكواد أعطال مسجلة في كمبيوتر المحرك (Modes 03 / 07 / 0A)."
                                AppLanguage.FR -> "Aucun code défaut stocké, en attente ou permanent (Modes 03 / 07 / 0A)."
                                AppLanguage.EN -> "No stored, pending, or permanent fault codes found (Modes 03 / 07 / 0A)."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusPassGreenOnContainer
                        )
                    }
                }
            } else {
                dtcList.forEach { dtc ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Slate50,
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (dtc.severity == ItemSeverity.CRITICAL) StatusErrorRed else StatusWarningAmber
                                    ) {
                                        Text(
                                            text = dtc.code,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dtc.dtcType.getLabel(language),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate600
                                    )
                                }

                                if (dtc.maxCostDzd > 0) {
                                    Text(
                                        text = "${dtc.minCostDzd / 1000}k - ${dtc.maxCostDzd / 1000}k DZD",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusErrorRed
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = dtc.getTitle(language),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = dtc.getExplanation(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                if (!freezeFrameSummary.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = StatusWarningAmberContainer
                    ) {
                        Text(
                            text = "Freeze Frame (Mode 02): $freezeFrameSummary",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusWarningAmberOnContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EcuVinComparisonCard(
    ecuVin: String,
    expectedVin: String,
    language: AppLanguage
) {
    val hasExpected = expectedVin.length == 17
    val matches = !hasExpected || ecuVin.equals(expectedVin, ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (matches) Emerald200 else StatusErrorRed)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (language) {
                    AppLanguage.AR -> "رقم الشاسيه المخزن في كمبيوتر المحرك (Mode 09)"
                    AppLanguage.FR -> "Numéro VIN lu dans le calculateur (Mode 09)"
                    AppLanguage.EN -> "Electronic VIN Stored in Engine ECU (Mode 09)"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ecuVin,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (matches) Emerald800 else StatusErrorRed
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (matches) {
                    when (language) {
                        AppLanguage.AR -> "متطابق مع هوية السيارة والبطاقة الرمادية ✓"
                        AppLanguage.FR -> "Cohérent avec le VIN physique et la carte grise ✓"
                        AppLanguage.EN -> "Matches physical VIN and registration card ✓"
                    }
                } else {
                    when (language) {
                        AppLanguage.AR -> "تحذير: رقم الشاسيه في الكمبيوتر يختلف عن الرقم المدخل ($expectedVin)! قد يكون الكمبيوتر مستبدلاً."
                        AppLanguage.FR -> "Attention : Le VIN du calculateur diffère du VIN saisi ($expectedVin) !"
                        AppLanguage.EN -> "Warning: ECU VIN differs from entered VIN ($expectedVin)!"
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (matches) StatusPassGreen else StatusErrorRed
            )
        }
    }
}

@Composable
private fun LiveGaugesCard(
    gauges: List<LiveParameterGauge>,
    language: AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (language) {
                    AppLanguage.AR -> "الفحص الحي للمحرك (60 ثانية - المحرك دافئ في وضع Ralenti)"
                    AppLanguage.FR -> "Contrôle moteur en direct (60s au ralenti moteur chaud)"
                    AppLanguage.EN -> "60-Second Live Engine Health Check (Warm Idle)"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(10.dp))

            gauges.forEach { gauge ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = gauge.getName(language),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )
                        Text(
                            text = "Normal: ${gauge.normalRangeText}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }

                    val badgeBg = when (gauge.status) {
                        "GREEN" -> StatusPassGreenContainer
                        "RED" -> StatusErrorRedContainer
                        "UNAVAILABLE" -> Slate100
                        else -> StatusWarningAmberContainer
                    }
                    val badgeFg = when (gauge.status) {
                        "GREEN" -> StatusPassGreenOnContainer
                        "RED" -> StatusErrorRedOnContainer
                        "UNAVAILABLE" -> Slate700
                        else -> StatusWarningAmberOnContainer
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = gauge.valueText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryEcuMileageInfoCard(
    language: AppLanguage
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Emerald50,
        border = BorderStroke(1.dp, Emerald200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = when (language) {
                    AppLanguage.AR -> "ملاحظة حول كشف التلاعب بعداد الكيلومترات (Odometer Rollback)"
                    AppLanguage.FR -> "Note sur la vérification du kilométrage réel (Odomètre)"
                    AppLanguage.EN -> "Note on Odometer Rollback & Secondary ECU Mileage"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Emerald900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (language) {
                    AppLanguage.AR -> "لا يوفر معيار OBD2 العالمي قراءة العداد في جميع السيارات، لذلك يعتمد التطبيق على مقارنة المسافة منذ آخر مسح للأعطال مع تآكل المقصورة وتاريخ البطاقة الرمادية. وحدات القراءة الخاصة من كمبيوتر ABS/علبة السرعة مهيأة برمجياً كإضافات مستقلة (أولها قابلية للتطبيق في الجزائر: مجموعة Renault/Dacia عبر CAN ومجموعة VW/Seat/Skoda عبر UDS)."
                    AppLanguage.FR -> "Le standard OBD2 universel n'expose pas toujours le kilométrage du compteur. Fahs croise la distance parcourue depuis le dernier effacement avec l'usure physique (volant, pédales) et les documents. L'architecture modulaire prévoit en priorité les modules spécifiques Renault/Dacia (ABS CAN) et groupe VW/Seat/Skoda (UDS)."
                    AppLanguage.EN -> "Standard OBD2 does not expose dashboard odometer values on all cars. Fahs cross-checks distance since last code clear with physical wear (pedals, steering wheel) and paperwork. Brand-specific secondary ECU modules are architected for Renault/Dacia (ABS CAN) and VW/Seat/Skoda (UDS) first."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Slate700,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SaveObdToInspectionButton(
    language: AppLanguage,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("save_obd_to_inspection_btn"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
    ) {
        Icon(
            imageVector = Icons.Default.Save,
            contentDescription = null,
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (language) {
                AppLanguage.AR -> "حفظ نتائج فحص OBD2 في تقرير السيارة الحالية"
                AppLanguage.FR -> "Enregistrer ce scan OBD2 dans l'inspection en cours"
                AppLanguage.EN -> "Save OBD2 Scan Results to Active Inspection"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
