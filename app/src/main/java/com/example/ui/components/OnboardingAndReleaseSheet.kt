package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.i18n.AppLanguage
import com.example.data.repository.ShopSettings
import com.example.data.repository.WilayaDeliveryFee
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
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
import com.example.ui.theme.StatusWarningAmber

@Composable
fun FirstLaunchWelcomeCard(
    language: AppLanguage,
    onOpenTutorial: () -> Unit,
    onOpenDongleBuyingGuide: () -> Unit,
    onDismissWelcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("first_launch_welcome_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Emerald700),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Emerald100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Emerald800,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "مرحباً بك في تطبيق فحص (Fahs)"
                                AppLanguage.FR -> "Bienvenue sur Fahs — Votre ami mécanicien"
                                AppLanguage.EN -> "Welcome to Fahs — Your Mechanic Friend"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "دليل البداية السريعة • شراء فيشة OBD2 • الخصوصية"
                                AppLanguage.FR -> "Guide de démarrage • Boîtier ELM327 • Confidentialité"
                                AppLanguage.EN -> "Quick Start Guide • ELM327 Dongle • Privacy"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald800,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                TextButton(
                    onClick = onDismissWelcome,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("dismiss_welcome_card_btn")
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "إخفاء"
                            AppLanguage.FR -> "Masquer"
                            AppLanguage.EN -> "Hide"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Text(
                text = when (language) {
                    AppLanguage.AR -> "يساعدك تطبيق فحص على معاينة أي سيارة مستعملة في الجزائر خطوة بخطوة (الوثائق، الهيكل، المحرك، وفحص الكمبيوتر OBD2) بدون الحاجة للإنترنت."
                    AppLanguage.FR -> "Fahs vous accompagne devant la voiture étape par étape : papiers algériens, carrosserie, moteur à froid, scan OBD2 et rapport PDF chiffré en centimes."
                    AppLanguage.EN -> "Fahs guides you step-by-step next to the car: Algerian paperwork, bodywork, cold engine, OBD2 tamper scan, and a scored PDF report."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Slate700,
                lineHeight = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenTutorial,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_tutorial_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "كيف يعمل التطبيق؟"
                            AppLanguage.FR -> "Comment ça marche ?"
                            AppLanguage.EN -> "How It Works"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onOpenDongleBuyingGuide,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_dongle_buying_guide_btn"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Emerald700),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = Emerald800,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "شراء فيشة ELM327"
                            AppLanguage.FR -> "Choisir un ELM327"
                            AppLanguage.EN -> "Buy ELM327 Dongle"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald800,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingAndReleaseDialog(
    language: AppLanguage,
    initialSection: String = "TUTORIAL", // TUTORIAL, DONGLE_GUIDE, PRIVACY
    shopSettings: ShopSettings = ShopSettings(),
    isDeveloperModeEnabled: Boolean = false,
    onToggleDeveloperMode: (Boolean) -> Unit = {},
    onSelectLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    var activeSection by remember(initialSection) { mutableStateOf(initialSection) }
    var versionTapCount by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("onboarding_release_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = Slate50
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Emerald Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Emerald800)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "دليل فحص (Fahs) ومعلومات الإصدار"
                                    AppLanguage.FR -> "Guide Fahs, Boîtier OBD2 & Confidentialité"
                                    AppLanguage.EN -> "Fahs Guide, OBD2 Dongle & Privacy"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = buildString {
                                    append("Version 1.0 • Android 8.0+ • 100% Offline-First")
                                    if (isDeveloperModeEnabled) {
                                        append(" • [DEV MODE ON]")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Emerald100,
                                modifier = Modifier
                                    .clickable {
                                        versionTapCount += 1
                                        if (versionTapCount >= 7) {
                                            versionTapCount = 0
                                            onToggleDeveloperMode(!isDeveloperModeEnabled)
                                        }
                                    }
                                    .padding(vertical = 2.dp)
                                    .testTag("about_version_tap_target")
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_onboarding_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Quick Language Switcher Row + Section Tabs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Instant Language Pills
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = lang == language
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Emerald100 else Slate100,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Emerald700 else Slate200
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectLanguage(lang) }
                            ) {
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Emerald900 else Slate700,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // 3 Section Filter Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = activeSection == "TUTORIAL",
                            onClick = { activeSection = "TUTORIAL" },
                            label = {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "1. طريقة الفحص"
                                        AppLanguage.FR -> "1. Tutoriel"
                                        AppLanguage.EN -> "1. Tutorial"
                                    },
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald700,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("onboarding_tab_tutorial")
                        )
                        FilterChip(
                            selected = activeSection == "DONGLE_GUIDE",
                            onClick = { activeSection = "DONGLE_GUIDE" },
                            label = {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "2. شراء ELM327"
                                        AppLanguage.FR -> "2. Acheter ELM327"
                                        AppLanguage.EN -> "2. Buy ELM327"
                                    },
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald700,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("onboarding_tab_dongle")
                        )
                        FilterChip(
                            selected = activeSection == "PRIVACY",
                            onClick = { activeSection = "PRIVACY" },
                            label = {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "3. الخصوصية"
                                        AppLanguage.FR -> "3. Confidentialité"
                                        AppLanguage.EN -> "3. Privacy"
                                    },
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald700,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("onboarding_tab_privacy")
                        )
                    }
                }

                // Body Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (activeSection) {
                        "DONGLE_GUIDE" -> DongleBuyingGuideContent(language, shopSettings)
                        "PRIVACY" -> PrivacyAndReleaseInfoContent(
                            language = language,
                            shopSettings = shopSettings,
                            isDeveloperModeEnabled = isDeveloperModeEnabled,
                            onVersionTappedSevenTimes = {
                                onToggleDeveloperMode(!isDeveloperModeEnabled)
                            }
                        )
                        else -> TutorialWalkthroughContent(language)
                    }
                }

                // Bottom Action Button
                Surface(
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .height(48.dp)
                            .testTag("finish_onboarding_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "فهمت، لنبدأ الفحص الآن"
                                AppLanguage.FR -> "Compris, prêt à inspecter"
                                AppLanguage.EN -> "Got it, ready to inspect"
                            },
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialWalkthroughContent(language: AppLanguage) {
    InfoStepCard(
        icon = Icons.Default.DirectionsCar,
        badgeColor = Emerald700,
        title = when (language) {
            AppLanguage.AR -> "الخطوة 1: اختر السيارة وطابق الوثائق والشاسيه"
            AppLanguage.FR -> "Étape 1 : Choisissez la voiture & vérifiez la Carte Grise"
            AppLanguage.EN -> "Step 1: Choose the Car & Verify Algerian Documents"
        },
        body = when (language) {
            AppLanguage.AR -> "اختر ماركة وموديل السيارة ونوع الوقود (بنزين، ديزل، سيرغاز GPL). بعدها تحقق من تطابق رقم الشاسيه المحفور على الهيكل مع البطاقة الرمادية الأصلية وعدم وجود عائق بيع."
            AppLanguage.FR -> "Sélectionnez la marque, le modèle, le carburant (Essence, Diesel, GPL) et la boîte. Vérifiez ensuite que le numéro de châssis frappé sur la voiture correspond à la carte grise originale."
            AppLanguage.EN -> "Pick the brand, model, fuel (Petrol, Diesel, LPG/GPL), and gearbox. Then verify that the chassis number stamped on the car matches the original registration card."
        }
    )

    InfoStepCard(
        icon = Icons.Default.CheckCircle,
        badgeColor = StatusPassGreen,
        title = when (language) {
            AppLanguage.AR -> "الخطوة 2: الفحص الميداني سؤالاً بسؤال (مع حفظ تلقائي)"
            AppLanguage.FR -> "Étape 2 : Inspection physique guidée autour du véhicule"
            AppLanguage.EN -> "Step 2: Guided Walkaround Checklist (Auto-Saved)"
        },
        body = when (language) {
            AppLanguage.AR -> "يعرض لك التطبيق سؤالاً واحداً واضحاً في كل شاشة حسب ترتيب دورانك حول السيارة: الهيكل والصبيغة، تحت الغطاء والمحرك بارد، المقصورة، أسفل السيارة، تشغيل المحرك، والقيادة التجريبية. اضغط «كيف أفحص هذا؟» لرؤية الشرح المبسط، والتقط صوراً بالكاميرا لأي عيب."
            AppLanguage.FR -> "Une seule question simple par écran dans l'ordre naturel autour de la voiture : carrosserie, longerons sous le capot à froid, habitacle, soubassement, démarrage et essai routier. Appuyez sur « Comment vérifier ? » en cas de doute. Tout est sauvegardé automatiquement après chaque réponse."
            AppLanguage.EN -> "One clear question per screen in natural walkaround order: exterior paint/gaps, cold engine bay & chassis legs, interior wear, suspension, cold start smoke, and test drive. Every answer is saved automatically."
        }
    )

    InfoStepCard(
        icon = Icons.Default.Speed,
        badgeColor = StatusWarningAmber,
        title = when (language) {
            AppLanguage.AR -> "الخطوة 3: فحص كمبيوتر السيارة OBD2 وكشف مسح الأكواد"
            AppLanguage.FR -> "Étape 3 : Scan OBD2 & Détection des codes effacés"
            AppLanguage.EN -> "Step 3: OBD2 Scan & Cleared-Codes Detection"
        },
        body = when (language) {
            AppLanguage.AR -> "اربط هاتفك بفيشة البلوتوث ELM327 لقراءة أعطال المحرك وكشف ما إذا تم مسح ذاكرة الكمبيوتر مؤخراً قبل اللقاء (المسافة المقطوعة وعدد مرات التسخين منذ آخر مسح)."
            AppLanguage.FR -> "Connectez un petit boîtier Bluetooth ELM327 pour lire les défauts du calculateur et vérifier si la mémoire a été effacée récemment (kilométrage parcouru et cycles de chauffe depuis le dernier effacement)."
            AppLanguage.EN -> "Connect a Bluetooth ELM327 dongle to read stored/pending faults and check if fault memory was erased recently (distance traveled and warm-up cycles since codes were cleared)."
        }
    )

    InfoStepCard(
        icon = Icons.Default.PictureAsPdf,
        badgeColor = Emerald800,
        title = when (language) {
            AppLanguage.AR -> "الخطوة 4: التقرير النهائي، التفاوض بالسنتيم ومقارنة السيارات"
            AppLanguage.FR -> "Étape 4 : Score /100, Négociation en Centimes & PDF"
            AppLanguage.EN -> "Step 4: Score /100, Negotiation in Centimes & PDF Report"
        },
        body = when (language) {
            AppLanguage.AR -> "بعد الإجابة على النقاط الحرجة ونسبة كافية من الفحص، تحصل على تقييم من 100 مع توضيح أي نقاط لم تُفحص، وحساب عرض السعر المقترح بناءً على تسعيرة الميكانيكي التي تدخلها، ومشاركة التقرير كملف PDF مع الصور."
            AppLanguage.FR -> "Une fois les points critiques et un nombre suffisant de questions vérifiés, obtenez un score sur 100 avec le détail des points non vérifiés, le calcul d'offre basé sur le devis mécanicien saisi, et exportez le rapport complet en PDF avec photos."
            AppLanguage.EN -> "Once all critical items and enough checklist items are answered, get a 0–100 score showing any unchecked items, a negotiation calculator based on your mechanic's quote, and export a PDF report with photos."
        }
    )
}

@Composable
fun DongleBuyingGuideContent(
    language: AppLanguage,
    shopSettings: ShopSettings
) {
    // 1. Technical Buying Advice Card (Always visible)
    InfoStepCard(
        icon = Icons.Default.ShoppingBag,
        badgeColor = Emerald700,
        title = when (language) {
            AppLanguage.AR -> "أي فيشة ELM327 تختار في الجزائر؟ (V1.5 مقابل V2.1)"
            AppLanguage.FR -> "Quel boîtier ELM327 choisir en Algérie ? (V1.5 vs V2.1)"
            AppLanguage.EN -> "Which ELM327 Dongle Should You Choose in Algeria? (V1.5 vs V2.1)"
        },
        body = when (language) {
            AppLanguage.AR -> buildString {
                appendLine("• النسخة الموصى بها بشدة: «ELM327 Bluetooth Classic V1.5» المزودة بشريحة PIC18F25K80 الأصلية، أو أجهزة Vgate iCar2 / Konnwei KW902.")
                appendLine("• لماذا نتجنب النسخ الرخيصة V2.1؟ أغلب الفيشات الزرقاء الشفافة المكتوب عليها V2.1 في السوق تحذف بروتوكولات السيارات الأقدم (مثل Renault Symbol / Clio / Logan / Peugeot 207-301) وتفشل في الاتصال بالكمبيوتر.")
                append("• ملاحظة لهواتف أندرويد: اختر دائماً نسخة Bluetooth Classic (وليس Wi-Fi) ليبقى الإنترنت يعمل في هاتفك أثناء الفحص.")
            }
            AppLanguage.FR -> buildString {
                appendLine("• Modèle recommandé : privilégiez un boîtier « ELM327 Bluetooth Classic Version 1.5 » (équipé de la puce PIC18F25K80) ou un Vgate iCar2 / Konnwei KW902.")
                appendLine("• Pourquoi éviter les boîtiers V2.1 bas de gamme ? Beaucoup de petits boîtiers bleus transparents estampillés V2.1 ne supportent pas les protocoles ISO/KWP utilisés sur plusieurs Renault, Dacia et Peugeot en Algérie.")
                append("• Conseil Android : choisissez le Bluetooth Classique (et non Wi-Fi) pour garder votre connexion mobile active pendant le scan.")
            }
            AppLanguage.EN -> buildString {
                appendLine("• Recommended model: choose an 'ELM327 Bluetooth Classic V1.5' adapter (with the genuine PIC18F25K80 chip) or a Vgate iCar2 / Konnwei KW902.")
                appendLine("• Why avoid cheap V2.1 boards? Many translucent blue V2.1 clones omit ISO/KWP protocols required by older Renault, Dacia, and Peugeot models common in Algeria.")
                append("• Android tip: pick Bluetooth Classic (not Wi-Fi) so your phone keeps mobile data while scanning.")
            }
        }
    )

    // 2. Configurable "Order a Tested Dongle" & 58-Wilaya Delivery Calculator Card
    if (shopSettings.isShopConfiguredAndEnabled()) {
        DongleOrderCalculatorCard(
            language = language,
            shopSettings = shopSettings
        )
    }

    // 3. Pairing Guide Card
    InfoStepCard(
        icon = Icons.Default.Bluetooth,
        badgeColor = StatusWarningAmber,
        title = when (language) {
            AppLanguage.AR -> "طريقة الربط بالهاتف خطوة بخطوة (كود البلوتوث)"
            AppLanguage.FR -> "Comment jumeler le boîtier en 30 secondes ?"
            AppLanguage.EN -> "How to Pair the Dongle in 30 Seconds"
        },
        body = when (language) {
            AppLanguage.AR -> "1. والمحرك متوقف، ركب الفيشة في منفذ OBD2 للسيارة (تجد مكانه بالوصف داخل التطبيق لكل سيارة).\n2. افتح الكونتاكت (تضيء لمبات الطابلوه) أو شغل المحرك.\n3. افتح إعدادات البلوتوث في هاتفك، ابحث عن جهاز باسم «OBDII» أو «V-LINK»، وأدخل كود الاقتران: 1234 أو 0000.\n4. افتح تطبيق فحص واضغط «اتصال بفيشة البلوتوث»."
            AppLanguage.FR -> "1. Contact coupé, branchez le boîtier sur la prise OBD2 de la voiture (l'emplacement exact est indiqué dans Fahs pour chaque modèle).\n2. Mettez le contact (voyants du tableau de bord allumés) ou démarrez le moteur.\n3. Dans les paramètres Bluetooth de votre téléphone Android, associez l'appareil « OBDII » ou « V-LINK » avec le code PIN 1234 ou 0000.\n4. Revenez dans Fahs et lancez le scan."
            AppLanguage.EN -> "1. With ignition off, plug the adapter into the car's 16-pin OBD2 port (Fahs shows the exact location for each model).\n2. Turn the key to Ignition ON (dashboard lights lit).\n3. In Android Bluetooth settings, pair with 'OBDII' or 'V-LINK' using PIN 1234 or 0000.\n4. Return to Fahs and tap Connect & Scan."
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DongleOrderCalculatorCard(
    language: AppLanguage,
    shopSettings: ShopSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val defaultWilaya = remember(shopSettings.deliveryFees) {
        shopSettings.deliveryFees.find { it.wilayaCode == "16" }
            ?: shopSettings.deliveryFees.firstOrNull()
    }
    var selectedWilaya by remember(defaultWilaya) { mutableStateOf(defaultWilaya) }
    var isHomeDelivery by remember { mutableStateOf(false) } // Default to Stop Desk (more economical)
    var quantity by remember { mutableIntStateOf(1) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var addressNote by remember { mutableStateOf("") }
    var showWilayaPickerDialog by remember { mutableStateOf(false) }

    val stockBadgeText = when (language) {
        AppLanguage.AR -> if (shopSettings.inStock) "متوفر حالياً" else "غير متوفر مؤقتاً"
        AppLanguage.FR -> if (shopSettings.inStock) "En stock" else "Rupture temporaire"
        AppLanguage.EN -> if (shopSettings.inStock) "In stock" else "Out of stock"
    }

    if (showWilayaPickerDialog && shopSettings.deliveryFees.isNotEmpty()) {
        WilayaPickerDialog(
            language = language,
            wilayas = shopSettings.deliveryFees,
            selectedWilaya = selectedWilaya,
            onSelectWilaya = { wilaya ->
                selectedWilaya = wilaya
                showWilayaPickerDialog = false
            },
            onDismiss = { showWilayaPickerDialog = false }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dongle_shop_order_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Emerald700),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Title + Stock Status Badge
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
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Emerald100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Emerald800,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "اطلب فيشة ELM327 V1.5 مجربة ومضمونة"
                                AppLanguage.FR -> "Commander un boîtier ELM327 V1.5 testé"
                                AppLanguage.EN -> "Order a Tested ELM327 V1.5 Dongle"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "السعر: ${shopSettings.formattedDonglePrice()} • توصيل 58 ولاية (الدفع عند الاستلام)"
                                AppLanguage.FR -> "Prix : ${shopSettings.formattedDonglePrice()} • Livraison 58 wilayas (Paiement à la livraison)"
                                AppLanguage.EN -> "Price: ${shopSettings.formattedDonglePrice()} • 58-Wilaya Delivery (Cash on Delivery)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald800
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (shopSettings.inStock) Emerald50 else StatusErrorRedContainer,
                    border = BorderStroke(
                        1.dp,
                        if (shopSettings.inStock) Emerald700 else StatusErrorRed
                    ),
                    modifier = Modifier.testTag("shop_stock_badge")
                ) {
                    Text(
                        text = stockBadgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (shopSettings.inStock) Emerald800 else StatusErrorRedOnContainer,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Step 1: Wilaya Selector Button
            val currentWilaya = selectedWilaya
            if (currentWilaya != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "1. اختر الولاية لحساب تكلفة التوصيل:"
                            AppLanguage.FR -> "1. Choisissez votre wilaya pour calculer la livraison :"
                            AppLanguage.EN -> "1. Select your wilaya to calculate delivery:"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showWilayaPickerDialog = true }
                            .testTag("select_wilaya_btn"),
                        shape = RoundedCornerShape(12.dp),
                        color = Slate50,
                        border = BorderStroke(1.dp, Emerald700)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Emerald800,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentWilaya.localizedName(language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "تغيير الولاية (58)"
                                    AppLanguage.FR -> "Changer (58)"
                                    AppLanguage.EN -> "Change (58)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Emerald800,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Step 2: Delivery Method (Stop Desk vs Home Delivery)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "2. طريقة التوصيل:"
                            AppLanguage.FR -> "2. Mode de livraison :"
                            AppLanguage.EN -> "2. Delivery method:"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = !isHomeDelivery,
                            onClick = { isHomeDelivery = false },
                            label = {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "مكتب التوصيل Stop Desk (${shopSettings.formatDzd(currentWilaya.stopDeskFeeDzd)})"
                                        AppLanguage.FR -> "Stop Desk (${shopSettings.formatDzd(currentWilaya.stopDeskFeeDzd)})"
                                        AppLanguage.EN -> "Stop Desk (${shopSettings.formatDzd(currentWilaya.stopDeskFeeDzd)})"
                                    },
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald700,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("delivery_mode_stop_desk")
                        )

                        FilterChip(
                            selected = isHomeDelivery,
                            onClick = { isHomeDelivery = true },
                            label = {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "توصيل للمنزل (${shopSettings.formatDzd(currentWilaya.homeFeeDzd)})"
                                        AppLanguage.FR -> "À domicile (${shopSettings.formatDzd(currentWilaya.homeFeeDzd)})"
                                        AppLanguage.EN -> "Home Delivery (${shopSettings.formatDzd(currentWilaya.homeFeeDzd)})"
                                    },
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald700,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("delivery_mode_home")
                        )
                    }
                }

                // Step 3: Quantity + Optional Customer Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "الكمية المطلوبة:"
                            AppLanguage.FR -> "Quantité :"
                            AppLanguage.EN -> "Quantity:"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate800,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (quantity > 1) quantity -= 1 },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("qty_minus_btn"),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease quantity",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            modifier = Modifier.testTag("qty_value_text")
                        )

                        OutlinedButton(
                            onClick = { if (quantity < 10) quantity += 1 },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("qty_plus_btn"),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase quantity",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Optional Customer Name & Phone & Commune
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_customer_name_input"),
                    singleLine = true,
                    label = {
                        Text(
                            when (language) {
                                AppLanguage.AR -> "الاسم واللقب (اختياري لرسالة الطلب)"
                                AppLanguage.FR -> "Nom & Prénom (optionnel pour le message)"
                                AppLanguage.EN -> "Full Name (optional for order message)"
                            }
                        )
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_customer_phone_input"),
                        singleLine = true,
                        label = {
                            Text(
                                when (language) {
                                    AppLanguage.AR -> "رقم الهاتف (اختياري)"
                                    AppLanguage.FR -> "Téléphone (optionnel)"
                                    AppLanguage.EN -> "Phone (optional)"
                                }
                            )
                        }
                    )

                    OutlinedTextField(
                        value = addressNote,
                        onValueChange = { addressNote = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_customer_commune_input"),
                        singleLine = true,
                        label = {
                            Text(
                                when (language) {
                                    AppLanguage.AR -> "البلدية (اختياري)"
                                    AppLanguage.FR -> "Commune (optionnel)"
                                    AppLanguage.EN -> "Commune (optional)"
                                }
                            )
                        }
                    )
                }

                // Step 4: Live Total Breakdown Box
                val unitPrice = shopSettings.donglePriceDzd
                val subtotal = unitPrice * quantity
                val deliveryFee = if (isHomeDelivery) currentWilaya.homeFeeDzd else currentWilaya.stopDeskFeeDzd
                val totalDzd = subtotal + deliveryFee

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_total_summary_box"),
                    shape = RoundedCornerShape(12.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "سعر الفيشة ($quantity × ${shopSettings.formatDzd(unitPrice)}):"
                                    AppLanguage.FR -> "Boîtier ($quantity × ${shopSettings.formatDzd(unitPrice)}) :"
                                    AppLanguage.EN -> "Dongle ($quantity × ${shopSettings.formatDzd(unitPrice)}):"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                            Text(
                                text = shopSettings.formatDzd(subtotal),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "التوصيل إلى ${currentWilaya.nameAr} (${if (isHomeDelivery) "للمنزل" else "Stop Desk"}):"
                                    AppLanguage.FR -> "Livraison ${currentWilaya.nameFr} (${if (isHomeDelivery) "Domicile" else "Stop Desk"}) :"
                                    AppLanguage.EN -> "Delivery to ${currentWilaya.nameEn} (${if (isHomeDelivery) "Home" else "Stop Desk"}):"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                            Text(
                                text = shopSettings.formatDzd(deliveryFee),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "المجموع عند الاستلام:"
                                    AppLanguage.FR -> "Total à payer à la livraison :"
                                    AppLanguage.EN -> "Total Cash on Delivery:"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Emerald900
                            )
                            Text(
                                text = shopSettings.formatDzd(totalDzd),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Emerald800,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.testTag("order_total_dzd_text")
                            )
                        }
                    }
                }

                // Step 5: Order Action Buttons
                val orderMessage = shopSettings.buildOrderMessage(
                    language = language,
                    wilaya = currentWilaya,
                    isHomeDelivery = isHomeDelivery,
                    quantity = quantity,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    addressNote = addressNote
                )

                Button(
                    onClick = {
                        val waUri = shopSettings.buildWhatsAppUri(orderMessage)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUri)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            copyOrderToClipboard(context, language, orderMessage)
                        }
                    },
                    enabled = shopSettings.inStock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("order_dongle_whatsapp_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald700,
                        disabledContainerColor = Slate200
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (shopSettings.inStock) {
                            when (language) {
                                AppLanguage.AR -> "إرسال الطلب عبر واتساب (${shopSettings.whatsappNumber})"
                                AppLanguage.FR -> "Commander sur WhatsApp (${shopSettings.whatsappNumber})"
                                AppLanguage.EN -> "Order via WhatsApp (${shopSettings.whatsappNumber})"
                            }
                        } else {
                            when (language) {
                                AppLanguage.AR -> "غير متوفر حالياً — سيتوفر قريباً"
                                AppLanguage.FR -> "Rupture de stock — Bientôt disponible"
                                AppLanguage.EN -> "Temporarily out of stock"
                            }
                        },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (shopSettings.contactEmail.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val subject = Uri.encode("Fahs ELM327 V1.5 Order - ${currentWilaya.nameFr}")
                                    val body = Uri.encode(orderMessage)
                                    val mailIntent = Intent(
                                        Intent.ACTION_SENDTO,
                                        Uri.parse("mailto:${shopSettings.contactEmail}?subject=$subject&body=$body")
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(mailIntent)
                                } catch (_: Exception) {
                                    copyOrderToClipboard(context, language, orderMessage)
                                }
                            },
                            enabled = shopSettings.inStock,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("order_dongle_email_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "طلب عبر الإيميل"
                                    AppLanguage.FR -> "Par Email"
                                    AppLanguage.EN -> "Order by Email"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            copyOrderToClipboard(context, language, orderMessage)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_order_message_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "نسخ نص الطلب"
                                AppLanguage.FR -> "Copier le message"
                                AppLanguage.EN -> "Copy Order Text"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun copyOrderToClipboard(
    context: Context,
    language: AppLanguage,
    orderMessage: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("Fahs ELM327 Order", orderMessage))
    val toastMsg = when (language) {
        AppLanguage.AR -> "تم نسخ تفاصيل الطلب إلى الحافظة"
        AppLanguage.FR -> "Détails de la commande copiés dans le presse-papiers"
        AppLanguage.EN -> "Order details copied to clipboard"
    }
    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
}

@Composable
private fun WilayaPickerDialog(
    language: AppLanguage,
    wilayas: List<WilayaDeliveryFee>,
    selectedWilaya: WilayaDeliveryFee?,
    onSelectWilaya: (WilayaDeliveryFee) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredWilayas = remember(wilayas, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            wilayas
        } else {
            wilayas.filter { w ->
                w.wilayaCode.lowercase().contains(q) ||
                    w.nameFr.lowercase().contains(q) ||
                    w.nameAr.contains(q) ||
                    w.nameEn.lowercase().contains(q)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .testTag("wilaya_picker_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "اختر الولاية (58 ولاية)"
                            AppLanguage.FR -> "Choisissez la Wilaya (58 wilayas)"
                            AppLanguage.EN -> "Select Wilaya (58 Wilayas)"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate600
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wilaya_search_input"),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Slate600
                        )
                    },
                    placeholder = {
                        Text(
                            when (language) {
                                AppLanguage.AR -> "ابحث برقم الولاية أو الاسم (مثال: 16 أو الجزائر أو وهران)..."
                                AppLanguage.FR -> "Rechercher par code ou nom (ex: 16, Alger, Oran)..."
                                AppLanguage.EN -> "Search by code or name (e.g., 16, Algiers, Oran)..."
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredWilayas, key = { it.wilayaCode }) { wilaya ->
                        val isSelected = selectedWilaya?.wilayaCode == wilaya.wilayaCode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectWilaya(wilaya) }
                                .testTag("wilaya_item_${wilaya.wilayaCode}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Emerald50 else Slate50,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Emerald700 else Slate200
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = wilaya.localizedName(language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Stop: ${wilaya.stopDeskFeeDzd} • Dom: ${wilaya.homeFeeDzd} DZD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Emerald800,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
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
private fun PrivacyAndReleaseInfoContent(
    language: AppLanguage,
    shopSettings: ShopSettings,
    isDeveloperModeEnabled: Boolean,
    onVersionTappedSevenTimes: () -> Unit
) {
    var tapCounter by remember { mutableIntStateOf(0) }

    InfoStepCard(
        icon = Icons.Default.PrivacyTip,
        badgeColor = Emerald700,
        title = when (language) {
            AppLanguage.AR -> "سياسة الخصوصية والتواصل: بياناتك ملكك وحدك"
            AppLanguage.FR -> "Politique de Confidentialité & Contact : 100% Privé"
            AppLanguage.EN -> "Privacy Policy & Contact: 100% Private"
        },
        body = when (language) {
            AppLanguage.AR -> buildString {
                appendLine("• التخزين المحلي أولاً: جميع الفحوصات، الصور، والملاحظات تحفظ داخل هاتفك فقط ولا تغادره أبداً بدون إذنك.")
                appendLine("• استخدام الكاميرا: تُستخدم الكاميرا حصرياً لتصوير ملاحظات وعيوب الفحص لإدراجها في تقرير PDF.")
                appendLine("• استخدام البلوتوث: يُستخدم حصرياً للاتصال بفيشة الفحص ELM327 OBD2.")
                appendLine("• النسخ الاحتياطي الاختياري: فقط في حال اخترت تسجيل الدخول بحساب Google والضغط على «حفظ سحابي»، يتم حفظ ملخصات الفحص في مساحتك الخاصة على Firebase Firestore.")
                if (shopSettings.contactEmail.isNotBlank()) {
                    appendLine("• البريد الإلكتروني للتواصل: ${shopSettings.contactEmail}")
                }
                if (shopSettings.whatsappNumber.isNotBlank()) {
                    append("• واتساب: ${shopSettings.whatsappNumber}")
                }
            }.trim()

            AppLanguage.FR -> buildString {
                appendLine("• Stockage local par défaut : Toutes vos inspections, photos et notes sont enregistrées uniquement sur votre téléphone.")
                appendLine("• Caméra : Utilisée exclusivement pour photographier les défauts observés et les joindre à votre rapport PDF.")
                appendLine("• Bluetooth : Utilisé exclusivement pour communiquer avec votre boîtier ELM327 OBD2.")
                appendLine("• Sauvegarde Cloud optionnelle : Uniquement si vous choisissez de vous connecter avec Google et cliquez sur « Sauvegarder », vos inspections sont copiées dans votre espace privé Cloud Firestore.")
                if (shopSettings.contactEmail.isNotBlank()) {
                    appendLine("• Email de contact : ${shopSettings.contactEmail}")
                }
                if (shopSettings.whatsappNumber.isNotBlank()) {
                    append("• WhatsApp : ${shopSettings.whatsappNumber}")
                }
            }.trim()

            AppLanguage.EN -> buildString {
                appendLine("• Offline Local Storage First: All your inspections, photos, and notes are stored strictly on your phone by default.")
                appendLine("• Camera Permission: Used exclusively to photograph inspection findings and attach them to your PDF report.")
                appendLine("• Bluetooth Permission: Used exclusively to communicate with your ELM327 OBD2 dongle.")
                appendLine("• Optional Cloud Backup: Only if you explicitly sign in with Google and tap 'Back Up' are your inspection summaries saved to your private Cloud Firestore space.")
                if (shopSettings.contactEmail.isNotBlank()) {
                    appendLine("• Contact Email: ${shopSettings.contactEmail}")
                }
                if (shopSettings.whatsappNumber.isNotBlank()) {
                    append("• WhatsApp: ${shopSettings.whatsappNumber}")
                }
            }.trim()
        }
    )

    InfoStepCard(
        icon = Icons.Default.VerifiedUser,
        badgeColor = Slate800,
        title = when (language) {
            AppLanguage.AR -> "تنبيه هام للمشتري (إخلاء مسؤولية فني)"
            AppLanguage.FR -> "Avis important à l'acheteur (Conseil mécanique)"
            AppLanguage.EN -> "Important Buyer Notice (Mechanical Disclaimer)"
        },
        body = when (language) {
            AppLanguage.AR -> "صُمم تطبيق «فحص» ليكون الصديق الخبير الذي يقف بجانبك ويرشدك خطوة بخطوة، لكنه لا يعوض الفحص المعمق فوق الحفرة أو الرافعة عند ميكانيكي أو طولي ثقة في حال ظهور مؤشرات خطيرة في الشاسيه أو المحرك."
            AppLanguage.FR -> "Fahs est conçu comme l'ami connaisseur qui vous guide méthodiquement devant le véhicule. En cas de doute sérieux sur les longerons, la compression moteur ou une boîte automatique, demandez toujours la confirmation d'un mécanicien ou d'un tôlier sur pont élévateur avant de payer."
            AppLanguage.EN -> "Fahs acts as the knowledgeable friend standing next to you during an inspection. If critical signs appear on the chassis rails, engine internals, or automatic gearbox, always have a trusted mechanic confirm on a lift before paying."
        }
    )

    // Version card in About/Privacy tab (7 taps toggles Developer Mode)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDeveloperModeEnabled) Emerald100 else Slate100,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                tapCounter += 1
                if (tapCounter >= 7) {
                    tapCounter = 0
                    onVersionTappedSevenTimes()
                }
            }
            .testTag("privacy_version_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isDeveloperModeEnabled) {
                    when (language) {
                        AppLanguage.AR -> "إصدار التطبيق 1.0 • وضع المطور مفعّل (اضغط 7 مرات لإيقافه)"
                        AppLanguage.FR -> "Version 1.0 • Mode Développeur ACTIVÉ (7 appuis pour désactiver)"
                        AppLanguage.EN -> "Version 1.0 • Developer Mode ENABLED (7 taps to disable)"
                    }
                } else {
                    when (language) {
                        AppLanguage.AR -> "إصدار التطبيق 1.0 (Fahs DZ)"
                        AppLanguage.FR -> "Version de l'application 1.0 (Fahs DZ)"
                        AppLanguage.EN -> "Application Version 1.0 (Fahs DZ)"
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDeveloperModeEnabled) Emerald900 else Slate700
            )
        }
    }
}

@Composable
private fun InfoStepCard(
    icon: ImageVector,
    badgeColor: Color,
    title: String,
    body: String
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = Slate700,
                lineHeight = 20.sp
            )
        }
    }
}
