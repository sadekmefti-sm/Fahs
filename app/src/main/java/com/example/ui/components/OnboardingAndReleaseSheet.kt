package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PrivacyTip
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.i18n.AppLanguage
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Column {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "مرحباً بك في تطبيق فحص (Fahs)"
                                AppLanguage.FR -> "Bienvenue sur Fahs — Votre ami mécanicien"
                                AppLanguage.EN -> "Welcome to Fahs — Your Mechanic Friend"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "دليل البداية السريعة • شراء فيشة OBD2 • الخصوصية"
                                AppLanguage.FR -> "Guide de démarrage • Boîtier ELM327 • Confidentialité"
                                AppLanguage.EN -> "Quick Start Guide • ELM327 Dongle • Privacy"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald800
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
                        color = Slate600
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
                        fontWeight = FontWeight.Bold
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
                        color = Emerald800
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingAndReleaseDialog(
    language: AppLanguage,
    initialSection: String = "TUTORIAL", // TUTORIAL, DONGLE_GUIDE, PRIVACY
    shopSettings: com.example.data.repository.ShopSettings = com.example.data.repository.ShopSettings(),
    isDeveloperModeEnabled: Boolean = false,
    onToggleDeveloperMode: (Boolean) -> Unit = {},
    onSelectLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    var activeSection by remember(initialSection) { mutableStateOf(initialSection) }
    var versionTapCount by remember { mutableStateOf(0) }

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
                        Column {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "دليل فحص (Fahs) ومعلومات الإصدار"
                                    AppLanguage.FR -> "Guide Fahs, Boîtier OBD2 & Confidentialité"
                                    AppLanguage.EN -> "Fahs Guide, OBD2 Dongle & Privacy"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
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
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // 3 Section Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = activeSection == "TUTORIAL",
                            onClick = { activeSection = "TUTORIAL" },
                            label = {
                                Text(
                                    when (language) {
                                        AppLanguage.AR -> "1. طريقة الفحص"
                                        AppLanguage.FR -> "1. Tutoriel"
                                        AppLanguage.EN -> "1. Tutorial"
                                    }
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
                                    when (language) {
                                        AppLanguage.AR -> "2. شراء ELM327"
                                        AppLanguage.FR -> "2. Acheter ELM327"
                                        AppLanguage.EN -> "2. Buy ELM327"
                                    }
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
                                    when (language) {
                                        AppLanguage.AR -> "3. الخصوصية"
                                        AppLanguage.FR -> "3. Confidentialité"
                                        AppLanguage.EN -> "3. Privacy"
                                    }
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
                            fontWeight = FontWeight.Bold
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
            AppLanguage.AR -> "بعد الإجابة على النقاط الحرجة ونسبة كافية من الفحص، تحصل على تقييم من 100 مع توضيح أي نقاط لم تُفحص، وحساب تكلفة الإصلاحات بالدينار وبالمليون سنتيم، ومشاركة التقرير كملف PDF مع الصور."
            AppLanguage.FR -> "Une fois les points critiques et un nombre suffisant de questions vérifiés, obtenez un score sur 100 avec le détail des points non vérifiés, le total des frais en DZD et en Millions de centimes, et exportez le rapport complet en PDF avec photos."
            AppLanguage.EN -> "Once all critical items and enough checklist items are answered, get a 0–100 score showing any unchecked items, estimated repairs in DZD and Millions de centimes, and export a PDF report with photos."
        }
    )
}

@Composable
private fun DongleBuyingGuideContent(
    language: AppLanguage,
    shopSettings: com.example.data.repository.ShopSettings
) {
    val priceFormatted = shopSettings.formattedDonglePrice()
    val stockStatusText = when (language) {
        AppLanguage.AR -> if (shopSettings.inStock) "متوفر حالياً للطلب" else "غير متوفر مؤقتاً"
        AppLanguage.FR -> if (shopSettings.inStock) "En stock actuellement" else "Rupture de stock temporaire"
        AppLanguage.EN -> if (shopSettings.inStock) "Currently in stock" else "Temporarily out of stock"
    }

    InfoStepCard(
        icon = Icons.Default.ShoppingBag,
        badgeColor = Emerald700,
        title = when (language) {
            AppLanguage.AR -> "أي فيشة ELM327 أشتري في الجزائر؟ (السعر والنوع)"
            AppLanguage.FR -> "Quel boîtier ELM327 acheter en Algérie ? (Prix & Version)"
            AppLanguage.EN -> "Which ELM327 Dongle Should You Buy in Algeria?"
        },
        body = when (language) {
            AppLanguage.AR -> "• النوع الموصى به: فيشة «ELM327 Bluetooth V1.5» (ذات الشريحة الأصلية PIC18F25K80) أو Vgate iCar2 / Konnwei KW902.\n• السعر المبدئي للفيشة: $priceFormatted ($stockStatusText).\n• التوصيل متاح لجميع الولايات (58 ولاية) إلى المنزل أو مكتب التوصيل (Stop Desk).\n• للطلب أو الاستفسار عبر واتساب: ${shopSettings.whatsappNumber}"
            AppLanguage.FR -> "• Modèle recommandé : privilégiez un boîtier « ELM327 Bluetooth Classique Version 1.5 » (puce PIC18F25K80) ou un Vgate iCar2 / Konnwei KW902.\n• Prix du boîtier : à partir de $priceFormatted ($stockStatusText).\n• Livraison disponible dans les 58 wilayas (à domicile ou en bureau Stop Desk).\n• Contact WhatsApp : ${shopSettings.whatsappNumber}"
            AppLanguage.EN -> "• Recommended model: 'ELM327 Bluetooth Classic V1.5' (PIC18F25K80 chip) or Vgate iCar2 / Konnwei KW902.\n• Starting price: $priceFormatted ($stockStatusText).\n• Delivery available across all 58 wilayas (home delivery or stop desk).\n• WhatsApp contact: ${shopSettings.whatsappNumber}"
        }
    )

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

@Composable
private fun PrivacyAndReleaseInfoContent(
    language: AppLanguage,
    shopSettings: com.example.data.repository.ShopSettings,
    isDeveloperModeEnabled: Boolean,
    onVersionTappedSevenTimes: () -> Unit
) {
    var tapCounter by remember { mutableStateOf(0) }

    InfoStepCard(
        icon = Icons.Default.PrivacyTip,
        badgeColor = Emerald700,
        title = when (language) {
            AppLanguage.AR -> "سياسة الخصوصية والتواصل: بياناتك ملكك وحدك"
            AppLanguage.FR -> "Politique de Confidentialité & Contact : 100% Privé"
            AppLanguage.EN -> "Privacy Policy & Contact: 100% Private"
        },
        body = when (language) {
            AppLanguage.AR -> "• التخزين المحلي أولاً: جميع الفحوصات، الصور، والملاحظات تحفظ داخل هاتفك فقط ولا تغادره أبداً بدون إذنك.\n• استخدام الكاميرا: تُستخدم الكاميرا حصرياً لتصوير ملاحظات وعيوب الفحص لإدراجها في تقرير PDF.\n• استخدام البلوتوث: يُستخدم حصرياً للاتصال بفيشة الفحص ELM327 OBD2.\n• النسخ الاحتياطي الاختياري: فقط في حال اخترت تسجيل الدخول بحساب Google والضغط على «حفظ سحابي»، يتم حفظ ملخصات الفحص في مساحتك الخاصة على Firebase Firestore.\n• البريد الإلكتروني للتواصل: ${shopSettings.contactEmail}\n• واتساب: ${shopSettings.whatsappNumber}"
            AppLanguage.FR -> "• Stockage local par défaut : Toutes vos inspections, photos et notes sont enregistrées uniquement sur votre téléphone.\n• Caméra : Utilisée exclusivement pour photographier les défauts observés et les joindre à votre rapport PDF.\n• Bluetooth : Utilisé exclusivement pour communiquer avec votre boîtier ELM327 OBD2.\n• Sauvegarde Cloud optionnelle : Uniquement si vous choisissez de vous connecter avec Google et cliquez sur « Sauvegarder », vos inspections sont copiées dans votre espace privé Cloud Firestore.\n• Email de contact : ${shopSettings.contactEmail}\n• WhatsApp : ${shopSettings.whatsappNumber}"
            AppLanguage.EN -> "• Offline Local Storage First: All your inspections, photos, and notes are stored strictly on your phone by default.\n• Camera Permission: Used exclusively to photograph inspection findings and attach them to your PDF report.\n• Bluetooth Permission: Used exclusively to communicate with your ELM327 OBD2 dongle.\n• Optional Cloud Backup: Only if you explicitly sign in with Google and tap 'Back Up' are your inspection summaries saved to your private Cloud Firestore space.\n• Contact Email: ${shopSettings.contactEmail}\n• WhatsApp: ${shopSettings.whatsappNumber}"
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
                    color = Slate900
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
