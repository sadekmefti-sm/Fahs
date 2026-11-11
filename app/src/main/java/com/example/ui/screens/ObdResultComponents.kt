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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.data.model.ItemSeverity
import com.example.domain.ClearedCodesAlertLevel
import com.example.domain.DtcInfo
import com.example.domain.EcuVinComparisonChoice
import com.example.domain.LiveParameterGauge
import com.example.domain.ObdDiagnosticEngine
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
    expectedVin: String = "",
    ecuVinComparison: EcuVinComparisonChoice = EcuVinComparisonChoice.COULD_NOT_COMPARE,
    onEcuVinComparisonChange: (EcuVinComparisonChoice) -> Unit = {},
    milBulbLitOnDashboard: Boolean,
    onMilBulbToggle: (Boolean) -> Unit,
    hasActiveInspection: Boolean,
    onSaveToInspection: () -> Unit,
    isDeveloperModeEnabled: Boolean = false,
    onExportRawLog: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Adapter & Protocol Status Banner (Never labels dongle a clone)
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
            if (isDeveloperModeEnabled && scanResult.rawExchangeLog.isNotEmpty()) {
                DeveloperRawExchangeLogCard(
                    exchangeCount = scanResult.rawExchangeLog.size,
                    language = language,
                    onExportRawLog = onExportRawLog
                )
            }
            if (hasActiveInspection && !scanResult.isSimulated) {
                SaveObdToInspectionButton(
                    language = language,
                    onClick = onSaveToInspection
                )
            }
            return@Column
        }

        // 2. Cleared-Codes Status Card (Calm, non-accusing, based strictly on values actually read)
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

        // 5. ECU VIN Consistency Check (Service 09 02)
        if (!scanResult.ecuVin.isNullOrBlank()) {
            EcuVinComparisonCard(
                ecuVin = scanResult.ecuVin,
                comparisonChoice = ecuVinComparison,
                onComparisonChange = onEcuVinComparisonChange,
                language = language
            )
        }

        // 6. Live Engine Health Gauges
        if (scanResult.liveParameters.isNotEmpty()) {
            LiveGaugesCard(
                gauges = scanResult.liveParameters,
                language = language
            )
        }

        // 7. Brand-Specific Secondary ECU Odometer Architecture Info
        SecondaryEcuMileageInfoCard(language = language)

        // 8. Developer Mode Raw Exchange Log Export (only in Developer Mode)
        if (isDeveloperModeEnabled && scanResult.rawExchangeLog.isNotEmpty()) {
            DeveloperRawExchangeLogCard(
                exchangeCount = scanResult.rawExchangeLog.size,
                language = language,
                onExportRawLog = onExportRawLog
            )
        }

        // 9. Save Scan to Active Inspection (strictly blocked for simulated results)
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
    val alertLevel = scanResult.clearedCodesAlertLevel
    val notAvailableText = ObdDiagnosticEngine.notAvailableLabel(language)

    val containerColor = when (alertLevel) {
        ClearedCodesAlertLevel.STRONG_WARNING -> StatusErrorRedContainer
        ClearedCodesAlertLevel.CANNOT_RULE_OUT -> StatusWarningAmberContainer
        ClearedCodesAlertLevel.NOT_AVAILABLE -> Slate100
        ClearedCodesAlertLevel.NONE_NORMAL -> StatusPassGreenContainer
    }
    val borderColor = when (alertLevel) {
        ClearedCodesAlertLevel.STRONG_WARNING -> StatusErrorRed
        ClearedCodesAlertLevel.CANNOT_RULE_OUT -> StatusWarningAmber
        ClearedCodesAlertLevel.NOT_AVAILABLE -> Slate200
        ClearedCodesAlertLevel.NONE_NORMAL -> StatusPassGreen
    }
    val contentColor = when (alertLevel) {
        ClearedCodesAlertLevel.STRONG_WARNING -> StatusErrorRedOnContainer
        ClearedCodesAlertLevel.CANNOT_RULE_OUT -> StatusWarningAmberOnContainer
        ClearedCodesAlertLevel.NOT_AVAILABLE -> Slate800
        ClearedCodesAlertLevel.NONE_NORMAL -> StatusPassGreenOnContainer
    }
    val iconTint = when (alertLevel) {
        ClearedCodesAlertLevel.STRONG_WARNING -> StatusErrorRed
        ClearedCodesAlertLevel.CANNOT_RULE_OUT -> StatusWarningAmber
        ClearedCodesAlertLevel.NOT_AVAILABLE -> Slate600
        ClearedCodesAlertLevel.NONE_NORMAL -> StatusPassGreen
    }
    val iconVector = when (alertLevel) {
        ClearedCodesAlertLevel.STRONG_WARNING -> Icons.Default.Warning
        ClearedCodesAlertLevel.CANNOT_RULE_OUT -> Icons.Default.Info
        ClearedCodesAlertLevel.NOT_AVAILABLE -> Icons.Default.Info
        ClearedCodesAlertLevel.NONE_NORMAL -> Icons.Default.VerifiedUser
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("obd_cleared_fraud_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(width = 1.5.dp, color = borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when (alertLevel) {
                        ClearedCodesAlertLevel.STRONG_WARNING -> when (language) {
                            AppLanguage.AR -> "ملاحظة هامة: تم مسح ذاكرة الأعطال منذ فترة قصيرة"
                            AppLanguage.FR -> "Note importante : Mémoire des défauts remise à zéro récemment"
                            AppLanguage.EN -> "Important Note: Fault Memory Was Reset Recently"
                        }
                        ClearedCodesAlertLevel.CANNOT_RULE_OUT -> when (language) {
                            AppLanguage.AR -> "اختبارات الجاهزية غير مكتملة: لا يمكن استبعاد مسح حديث للذاكرة"
                            AppLanguage.FR -> "Tests d'autodiagnostic incomplets : remise à zéro récente non exclue"
                            AppLanguage.EN -> "Readiness Monitors Incomplete: Cannot Rule Out a Recent Reset"
                        }
                        ClearedCodesAlertLevel.NOT_AVAILABLE -> when (language) {
                            AppLanguage.AR -> "بيانات مسح الذاكرة غير متوفرة في هذه السيارة"
                            AppLanguage.FR -> "Historique d'effacement non disponible sur cette voiture"
                            AppLanguage.EN -> "Reset History Not Available on This Car"
                        }
                        ClearedCodesAlertLevel.NONE_NORMAL -> when (language) {
                            AppLanguage.AR -> "ذاكرة الكمبيوتر لم يتم مسحها حديثاً"
                            AppLanguage.FR -> "Mémoire non effacée récemment"
                            AppLanguage.EN -> "Fault Memory Not Cleared Recently"
                        }
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val distKm = scanResult.distanceSinceCodesClearedKm
            val warmups = scanResult.warmupsSinceCodesCleared
            val incMon = scanResult.incompleteReadinessMonitors
            val totMon = scanResult.totalReadinessMonitors

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FraudMetricPill(
                    label = when (language) {
                        AppLanguage.AR -> "المسافة منذ المسح"
                        AppLanguage.FR -> "Distance depuis RAZ"
                        AppLanguage.EN -> "Distance since reset"
                    },
                    value = if (distKm != null) "$distKm km" else notAvailableText,
                    isSuspicious = distKm != null && distKm < 50,
                    modifier = Modifier.weight(1f)
                )
                FraudMetricPill(
                    label = when (language) {
                        AppLanguage.AR -> "دورات التسخين"
                        AppLanguage.FR -> "Cycles de chauffe"
                        AppLanguage.EN -> "Warm-up cycles"
                    },
                    value = warmups?.toString() ?: notAvailableText,
                    isSuspicious = warmups != null && warmups < 3,
                    modifier = Modifier.weight(1f)
                )
                FraudMetricPill(
                    label = when (language) {
                        AppLanguage.AR -> "اختبارات غير مكتملة"
                        AppLanguage.FR -> "Tests incomplets"
                        AppLanguage.EN -> "Incomplete monitors"
                    },
                    value = if (incMon != null && totMon != null && totMon > 0) "$incMon/$totMon" else notAvailableText,
                    isSuspicious = incMon != null && incMon >= 3 && alertLevel == ClearedCodesAlertLevel.STRONG_WARNING,
                    modifier = Modifier.weight(1f)
                )
            }

            when (alertLevel) {
                ClearedCodesAlertLevel.STRONG_WARNING -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    val distDescFr = if (distKm != null) "il y a $distKm km" else ""
                    val warmDescFr = if (warmups != null) "($warmups cycle(s) de chauffe)" else ""
                    val distDescAr = if (distKm != null) "قبل $distKm كم" else ""
                    val warmDescAr = if (warmups != null) "($warmups دورة تسخين)" else ""
                    val distDescEn = if (distKm != null) "$distKm km ago" else ""
                    val warmDescEn = if (warmups != null) "($warmups warm-up cycle(s))" else ""

                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "أفاد كمبيوتر السيارة بأن ذاكرة الأعطال قد تم مسحها $distDescAr $warmDescAr (مثلاً بعد صيانة أو فصل البطارية). نظراً لأن السيارة لم تسر مسافة كافية لإعادة فحص جميع الحساسات، يُنصح بقيادتها لمدة 20 إلى 30 دقيقة ثم إعادة الفحص للتأكد من عدم ظهور أي كود عطل."
                            AppLanguage.FR -> "Le calculateur indique une remise à zéro des codes défauts $distDescFr $warmDescFr (par exemple après un entretien ou une batterie débranchée). La voiture ayant peu roulé depuis, faites un essai routier de 20 à 30 minutes puis relancez le scan pour vérifier qu'aucun défaut ne réapparaît."
                            AppLanguage.EN -> "The ECU reports that fault memory was reset $distDescEn $warmDescEn (for instance after maintenance or a battery disconnect). Because the car has only been driven a short distance since then, take a 20–30 minute test drive and scan again to make sure no fault codes return."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                        lineHeight = 19.sp
                    )
                }

                ClearedCodesAlertLevel.CANNOT_RULE_OUT -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "بعض اختبارات الفحص الذاتي ($incMon من $totMon) لم تكتمل بعد، بينما لا توفر هذه السيارة عداد المسافة منذ المسح. في السيارات الأقدم قد يكون هذا أمراً عادياً، لكن لا يمكن استبعاد مسح حديث للذاكرة أو فصل البطارية. يُفضل إجراء تجربة قيادة قبل الحسم."
                            AppLanguage.FR -> "Plusieurs tests d'autodiagnostic ($incMon sur $totMon) sont incomplets et ce calculateur ne fournit pas la distance parcourue depuis la dernière remise à zéro. Sur les voitures anciennes, cela peut être normal, mais on ne peut pas exclure un effacement récent ou une batterie débranchée."
                            AppLanguage.EN -> "Several self-test monitors ($incMon of $totMon) are incomplete and this car does not report distance or warm-ups since reset. On older cars this can happen normally, so a recent memory reset or battery disconnect cannot be ruled out."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                        lineHeight = 19.sp
                    )
                }

                ClearedCodesAlertLevel.NOT_AVAILABLE -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "لم يرسل كمبيوتر هذه السيارة عدادات المسافة أو دورات التسخين منذ آخر مسح. لا يدخل هذا العنصر في التقييم."
                            AppLanguage.FR -> "Le calculateur de cette voiture ne communique pas les compteurs de distance ou de cycles depuis le dernier effacement. Cet élément n'est pas compté dans le score."
                            AppLanguage.EN -> "This car's ECU does not provide distance or warm-up counters since the last reset. This leaves the score unaffected."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700,
                        lineHeight = 18.sp
                    )
                }

                ClearedCodesAlertLevel.NONE_NORMAL -> Unit
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
                style = MaterialTheme.typography.labelLarge,
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
    milCommandedOn: Boolean?,
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

                val badgeBg = when (milCommandedOn) {
                    true -> StatusErrorRedContainer
                    false -> StatusPassGreenContainer
                    null -> Slate100
                }
                val badgeFg = when (milCommandedOn) {
                    true -> StatusErrorRedOnContainer
                    false -> StatusPassGreenOnContainer
                    null -> Slate700
                }
                val badgeText = when (milCommandedOn) {
                    true -> "ECU MIL: ON"
                    false -> "ECU MIL: OFF"
                    null -> when (language) {
                        AppLanguage.AR -> "ECU MIL: غير متوفر"
                        AppLanguage.FR -> "ECU MIL: Non dispo"
                        AppLanguage.EN -> "ECU MIL: N/A"
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg,
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
                            AppLanguage.AR -> "تنبيه: عدم إضاءة لمبة المحرك عند وضع Contact قد يعني تعطل اللمبة أو تغطيتها داخل العداد لإخفاء عطل."
                            AppLanguage.FR -> "Attention : Si le voyant ne s'allume pas au contact, l'ampoule/LED du compteur est peut-être hors service ou masquée."
                            AppLanguage.EN -> "Warning: If the check-engine light does not illuminate with ignition ON, the dashboard bulb/LED may be faulty or covered."
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
    comparisonChoice: EcuVinComparisonChoice,
    onComparisonChange: (EcuVinComparisonChoice) -> Unit,
    language: AppLanguage
) {
    val isDifferent = comparisonChoice == EcuVinComparisonChoice.DIFFERENT
    val isSame = comparisonChoice == EcuVinComparisonChoice.SAME

    val borderColor = when (comparisonChoice) {
        EcuVinComparisonChoice.DIFFERENT -> StatusErrorRed
        EcuVinComparisonChoice.SAME -> StatusPassGreen
        EcuVinComparisonChoice.COULD_NOT_COMPARE -> Emerald200
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("obd_ecu_vin_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (language) {
                    AppLanguage.AR -> "رقم الشاسيه المقروء من كمبيوتر السيارة (Service 09 02)"
                    AppLanguage.FR -> "Numéro de châssis lu dans le calculateur (Service 09 02)"
                    AppLanguage.EN -> "Car's Chassis Number Read from ECU (Service 09 02)"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Large characters box so user never has to type a VIN
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isDifferent -> StatusErrorRedContainer
                    isSame -> StatusPassGreenContainer
                    else -> Slate100
                },
                border = BorderStroke(1.dp, borderColor)
            ) {
                Text(
                    text = ecuVin,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 21.sp,
                        letterSpacing = 1.8.sp
                    ),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = when {
                        isDifferent -> StatusErrorRedOnContainer
                        isSame -> StatusPassGreenOnContainer
                        else -> Slate900
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (language) {
                    AppLanguage.AR -> "قارن هذا الرقم مع البطاقة الرمادية"
                    AppLanguage.FR -> "Comparez ce numéro avec la carte grise"
                    AppLanguage.EN -> "Compare this with the carte grise"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Same
                val sameSelected = comparisonChoice == EcuVinComparisonChoice.SAME
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onComparisonChange(EcuVinComparisonChoice.SAME) }
                        .testTag("ecu_vin_same_btn"),
                    color = if (sameSelected) StatusPassGreen else Slate100,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "متطابق"
                            AppLanguage.FR -> "Identique"
                            AppLanguage.EN -> "Same"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (sameSelected) Color.White else Slate700,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                    )
                }

                // 2. Different (Critical finding)
                val diffSelected = comparisonChoice == EcuVinComparisonChoice.DIFFERENT
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onComparisonChange(EcuVinComparisonChoice.DIFFERENT) }
                        .testTag("ecu_vin_different_btn"),
                    color = if (diffSelected) StatusErrorRed else Slate100,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "مختلف"
                            AppLanguage.FR -> "Différent"
                            AppLanguage.EN -> "Different"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (diffSelected) Color.White else Slate700,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                    )
                }

                // 3. Could not compare
                val unknownSelected = comparisonChoice == EcuVinComparisonChoice.COULD_NOT_COMPARE
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onComparisonChange(EcuVinComparisonChoice.COULD_NOT_COMPARE) }
                        .testTag("ecu_vin_could_not_compare_btn"),
                    color = if (unknownSelected) Slate700 else Slate100,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "تعذر المقارنة"
                            AppLanguage.FR -> "Non comparé"
                            AppLanguage.EN -> "Could not compare"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (unknownSelected) Color.White else Slate700,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                    )
                }
            }

            if (isDifferent) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = StatusErrorRedContainer
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "ملاحظة حرجة: رقم الشاسيه المسجل داخل كمبيوتر المحرك يختلف عن البطاقة الرمادية! قد يكون كمبيوتر المحرك مستبدلاً من سيارة أخرى أو توجد مشكلة في هوية السيارة."
                            AppLanguage.FR -> "Anomalie critique : Le numéro de châssis enregistré dans le calculateur diffère de la carte grise (calculateur remplacé ou identité non conforme)."
                            AppLanguage.EN -> "Critical finding: The chassis number stored in the car's computer does not match the carte grise (replaced ECU or identity mismatch)."
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
                    AppLanguage.AR -> "القراءات الحية للمحرك والبطارية (المحرك دافئ في وضع Ralenti)"
                    AppLanguage.FR -> "Mesures moteur & batterie en direct (au ralenti)"
                    AppLanguage.EN -> "Live Engine & Battery Readings (Warm Idle)"
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
                    AppLanguage.AR -> "لا يوفر معيار OBD2 العالمي قراءة العداد في جميع السيارات، لذلك يعتمد التطبيق على مقارنة المسافة منذ آخر مسح للأعطال مع تآكل المقصورة وتاريخ البطاقة الرمادية."
                    AppLanguage.FR -> "Le standard OBD2 universel n'expose pas toujours le kilométrage du compteur. Fahs croise la distance parcourue depuis le dernier effacement avec l'usure physique (volant, pédales) et les documents."
                    AppLanguage.EN -> "Standard OBD2 does not expose dashboard odometer values on all cars. Fahs cross-checks distance since last code clear with physical wear (pedals, steering wheel) and paperwork."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Slate700,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun DeveloperRawExchangeLogCard(
    exchangeCount: Int,
    language: AppLanguage,
    onExportRawLog: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dev_export_raw_obd_log_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100),
        border = BorderStroke(1.dp, Slate600)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Slate800,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "[وضع المطورين] سجل التبادل الخام مع جهاز ELM327 ($exchangeCount أمر)"
                        AppLanguage.FR -> "[Mode Développeur] Journal brut d'échange ELM327 ($exchangeCount commandes)"
                        AppLanguage.EN -> "[Developer Mode] Raw ELM327 Exchange Log ($exchangeCount commands)"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when (language) {
                    AppLanguage.AR -> "احفظ سجل الأوامر والردود الخام في ملف نصي (.txt) لإرساله عند فحص سيارة حقيقية."
                    AppLanguage.FR -> "Enregistrez et partagez l'échange brut avec le boîtier dans un fichier texte (.txt) pour diagnostic."
                    AppLanguage.EN -> "Save and share the raw command/reply exchange with the dongle as a .txt file for troubleshooting."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Slate700
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onExportRawLog,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dev_export_raw_obd_log_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "حفظ ومشاركة ملف السجل الخام (.txt)"
                        AppLanguage.FR -> "Enregistrer / Envoyer le fichier brut (.txt)"
                        AppLanguage.EN -> "Save & Share Raw Exchange Log (.txt)"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
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
