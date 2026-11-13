package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LanguagePreferences
import com.example.data.repository.ShopSettings
import com.example.ui.components.SamplePdfReportsShowcaseCard
import com.example.ui.theme.FahsBrand
import com.example.ui.theme.FahsBrandSoft
import com.example.ui.theme.FahsHairline
import com.example.ui.theme.FahsInk
import com.example.ui.theme.FahsMutedText
import com.example.ui.theme.ResultProblemRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountScreen(
    language: AppLanguage,
    cloudUserEmail: String?,
    cloudUserName: String?,
    isCloudSyncing: Boolean,
    shopSettings: ShopSettings,
    isDeveloperModeEnabled: Boolean,
    onSelectLanguage: (AppLanguage) -> Unit,
    onOpenDongleOrder: () -> Unit,
    onOpenPrivacyAndTerms: () -> Unit,
    onBackupToCloud: () -> Unit,
    onRestoreFromCloud: () -> Unit,
    onDeleteAllDataAndAccount: () -> Unit,
    onToggleDeveloperMode: (Boolean) -> Unit,
    onSeedDeveloperDemoInspections: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var rememberedDongleName by remember {
        mutableStateOf(LanguagePreferences.getLastSuccessfulDongleName(context))
    }
    var rememberedDongleAddress by remember {
        mutableStateOf(LanguagePreferences.getLastSuccessfulDongleAddress(context))
    }
    var versionTapCount by remember { mutableIntStateOf(0) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "حذف الحساب والبيانات؟"
                        AppLanguage.FR -> "Supprimer le compte et les données ?"
                        AppLanguage.EN -> "Delete account and data?"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "سيتم حذف جميع التقارير المحفوظة والنسخ الاحتياطية السحابية نهائياً. لا يمكن التراجع عن هذا الإجراء."
                        AppLanguage.FR -> "Tous vos rapports enregistrés et vos sauvegardes en ligne seront supprimés définitivement. Cette action est irréversible."
                        AppLanguage.EN -> "All saved reports and online backups will be permanently deleted. This action cannot be undone."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAllDataAndAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ResultProblemRed)
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "نعم، احذف كل شيء"
                            AppLanguage.FR -> "Oui, tout supprimer"
                            AppLanguage.EN -> "Yes, delete everything"
                        },
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "إلغاء"
                            AppLanguage.FR -> "Annuler"
                            AppLanguage.EN -> "Cancel"
                        }
                    )
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("account_screen"),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Screen Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "حسابي والإعدادات"
                        AppLanguage.FR -> "Compte et réglages"
                        AppLanguage.EN -> "Account and settings"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "اللغة، النسخ الاحتياطي، الفيشة المسجلة، والخصوصية."
                        AppLanguage.FR -> "Langue, sauvegarde en ligne, boîtier enregistré et confidentialité."
                        AppLanguage.EN -> "Language, online backup, saved dongle, and privacy."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = FahsMutedText
                )
            }
        }

        // 1. Language Section
        item {
            AccountSectionGroup(
                title = when (language) {
                    AppLanguage.AR -> "اللغة"
                    AppLanguage.FR -> "Langue"
                    AppLanguage.EN -> "Language"
                }
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = null,
                            tint = FahsInk,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "اختر لغة التطبيق"
                                AppLanguage.FR -> "Choisir la langue de l'application"
                                AppLanguage.EN -> "Choose app language"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = lang == language
                            Surface(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clickable { onSelectLanguage(lang) }
                                    .testTag("account_lang_${lang.code}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) FahsBrand else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) FahsBrand else FahsHairline
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Equipment (Get an OBD dongle & My dongle)
        item {
            AccountSectionGroup(
                title = when (language) {
                    AppLanguage.AR -> "المعدات (فيشة OBD)"
                    AppLanguage.FR -> "Matériel (boîtier OBD)"
                    AppLanguage.EN -> "Equipment (OBD dongle)"
                }
            ) {
                AccountHairlineRow(
                    icon = Icons.Outlined.ShoppingBag,
                    title = when (language) {
                        AppLanguage.AR -> "الحصول على فيشة فحص OBD"
                        AppLanguage.FR -> "Obtenir un boîtier OBD"
                        AppLanguage.EN -> "Get an OBD dongle"
                    },
                    subtitle = if (shopSettings.isShopConfiguredAndEnabled()) {
                        when (language) {
                            AppLanguage.AR -> "نسخة مجربة · ${shopSettings.formattedDonglePrice()} · توصيل لكل الولايات"
                            AppLanguage.FR -> "Modèle testé · ${shopSettings.formattedDonglePrice()} · livraison toutes wilayas"
                            AppLanguage.EN -> "Tested model · ${shopSettings.formattedDonglePrice()} · all-wilaya delivery"
                        }
                    } else {
                        when (language) {
                            AppLanguage.AR -> "كيف تختار الفيشة المناسبة لسيارتك"
                            AppLanguage.FR -> "Comment choisir le bon boîtier pour votre voiture"
                            AppLanguage.EN -> "How to choose the right dongle for your car"
                        }
                    },
                    onClick = onOpenDongleOrder,
                    testTag = "account_get_dongle_row"
                )

                HorizontalDivider(color = FahsHairline, thickness = 1.dp)

                // My dongle (remembered dongle + Forget button)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bluetooth,
                            contentDescription = null,
                            tint = FahsInk,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "الفيشة الخاصة بي"
                                    AppLanguage.FR -> "Mon boîtier"
                                    AppLanguage.EN -> "My dongle"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (!rememberedDongleAddress.isNullOrBlank()) {
                                    val label = rememberedDongleName ?: rememberedDongleAddress!!
                                    when (language) {
                                        AppLanguage.AR -> "محفوظة للاتصال التلقائي: $label"
                                        AppLanguage.FR -> "Enregistré pour connexion directe : $label"
                                        AppLanguage.EN -> "Saved for automatic connection: $label"
                                    }
                                } else {
                                    when (language) {
                                        AppLanguage.AR -> "لم يتم حفظ أي فيشة بعد. سيتم حفظها تلقائياً بعد أول فحص ناجح."
                                        AppLanguage.FR -> "Aucun boîtier enregistré. Il sera mémorisé après votre premier scan."
                                        AppLanguage.EN -> "No dongle saved yet. It will be remembered after your first scan."
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = FahsMutedText
                            )
                        }
                    }

                    if (!rememberedDongleAddress.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = {
                                LanguagePreferences.clearLastSuccessfulDongle(context)
                                rememberedDongleName = null
                                rememberedDongleAddress = null
                                val msg = when (language) {
                                    AppLanguage.AR -> "تم نسيان الفيشة المحفوظة"
                                    AppLanguage.FR -> "Boîtier oublié"
                                    AppLanguage.EN -> "Saved dongle forgotten"
                                }
                                onShowMessage(msg)
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("account_forget_dongle_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, FahsHairline)
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "نسيان هذه الفيشة"
                                    AppLanguage.FR -> "Oublier ce boîtier"
                                    AppLanguage.EN -> "Forget this dongle"
                                },
                                color = FahsInk,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 3. Online Backup
        item {
            AccountSectionGroup(
                title = when (language) {
                    AppLanguage.AR -> "النسخ الاحتياطي لتقاريري"
                    AppLanguage.FR -> "Sauvegarde de mes rapports"
                    AppLanguage.EN -> "Backup of my reports"
                }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudUpload,
                            contentDescription = null,
                            tint = FahsInk,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (!cloudUserEmail.isNullOrBlank()) {
                                    cloudUserName?.takeIf { it.isNotBlank() } ?: cloudUserEmail
                                } else {
                                    when (language) {
                                        AppLanguage.AR -> "حفظ التقارير عبر حساب Google"
                                        AppLanguage.FR -> "Sauvegarder mes rapports avec Google"
                                        AppLanguage.EN -> "Back up my reports with Google"
                                    }
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (!cloudUserEmail.isNullOrBlank()) {
                                    cloudUserEmail
                                } else {
                                    when (language) {
                                        AppLanguage.AR -> "التطبيق يعمل بالكامل بدون حساب. تسجيل الدخول اختياري لحفظ تقاريرك عند تغيير الهاتف."
                                        AppLanguage.FR -> "L'application fonctionne sans compte. La connexion est optionnelle pour conserver vos rapports si vous changez de téléphone."
                                        AppLanguage.EN -> "The app works without an account. Signing in is optional to keep your reports if you change phones."
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = FahsMutedText
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onBackupToCloud,
                            enabled = !isCloudSyncing,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("account_backup_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FahsBrand)
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "حفظ تقاريري"
                                    AppLanguage.FR -> "Sauvegarder"
                                    AppLanguage.EN -> "Back up now"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        OutlinedButton(
                            onClick = onRestoreFromCloud,
                            enabled = !isCloudSyncing,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("account_restore_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, FahsHairline)
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "استرجاع تقاريري"
                                    AppLanguage.FR -> "Restaurer"
                                    AppLanguage.EN -> "Restore"
                                },
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 4. Privacy, Help & About
        item {
            AccountSectionGroup(
                title = when (language) {
                    AppLanguage.AR -> "الخصوصية والمساعدة"
                    AppLanguage.FR -> "Confidentialité et aide"
                    AppLanguage.EN -> "Privacy and help"
                }
            ) {
                AccountHairlineRow(
                    icon = Icons.Outlined.Lock,
                    title = when (language) {
                        AppLanguage.AR -> "سياسة الخصوصية وشروط الاستخدام"
                        AppLanguage.FR -> "Politique de confidentialité et conditions"
                        AppLanguage.EN -> "Privacy policy and terms of use"
                    },
                    subtitle = when (language) {
                        AppLanguage.AR -> "بياناتك وصورك تبقى على هاتفك"
                        AppLanguage.FR -> "Vos données et photos restent sur votre téléphone"
                        AppLanguage.EN -> "Your data and photos stay on your phone"
                    },
                    onClick = onOpenPrivacyAndTerms,
                    testTag = "account_privacy_row"
                )

                HorizontalDivider(color = FahsHairline, thickness = 1.dp)

                AccountHairlineRow(
                    icon = Icons.Outlined.Email,
                    title = when (language) {
                        AppLanguage.AR -> "اتصل بنا"
                        AppLanguage.FR -> "Nous contacter"
                        AppLanguage.EN -> "Contact us"
                    },
                    subtitle = shopSettings.contactEmail.ifBlank { "sadekmefti@gmail.com" },
                    onClick = {
                        val email = shopSettings.contactEmail.ifBlank { "sadekmefti@gmail.com" }
                        runCatching {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                            context.startActivity(intent)
                        }
                    },
                    testTag = "account_contact_row"
                )

                HorizontalDivider(color = FahsHairline, thickness = 1.dp)

                AccountHairlineRow(
                    icon = Icons.Outlined.Info,
                    title = when (language) {
                        AppLanguage.AR -> "حول تطبيق فحص (Fahs)"
                        AppLanguage.FR -> "À propos de Fahs"
                        AppLanguage.EN -> "About Fahs"
                    },
                    subtitle = buildString {
                        append(
                            when (language) {
                                AppLanguage.AR -> "الإصدار 1.0 · يعمل بدون إنترنت"
                                AppLanguage.FR -> "Version 1.0 · Fonctionne hors ligne"
                                AppLanguage.EN -> "Version 1.0 · Works offline"
                            }
                        )
                        if (isDeveloperModeEnabled) {
                            append(" · [Mode développeur actif]")
                        }
                    },
                    onClick = {
                        versionTapCount += 1
                        if (versionTapCount >= 7) {
                            versionTapCount = 0
                            onToggleDeveloperMode(!isDeveloperModeEnabled)
                        }
                    },
                    testTag = "account_about_version_row"
                )
            }
        }

        // 5. Delete Account & Data
        item {
            AccountSectionGroup(
                title = when (language) {
                    AppLanguage.AR -> "إدارة البيانات"
                    AppLanguage.FR -> "Gestion des données"
                    AppLanguage.EN -> "Data management"
                }
            ) {
                AccountHairlineRow(
                    icon = Icons.Outlined.DeleteOutline,
                    title = when (language) {
                        AppLanguage.AR -> "حذف حسابي وجميع بياناتي"
                        AppLanguage.FR -> "Supprimer mon compte et mes données"
                        AppLanguage.EN -> "Delete my account and data"
                    },
                    subtitle = when (language) {
                        AppLanguage.AR -> "حذف كافة التقارير المحفوظة على الهاتف وفي السحابة"
                        AppLanguage.FR -> "Effacer tous les rapports sur ce téléphone et en ligne"
                        AppLanguage.EN -> "Erase all reports on this phone and online"
                    },
                    titleColor = ResultProblemRed,
                    onClick = { showDeleteConfirmDialog = true },
                    testTag = "account_delete_all_row"
                )
            }
        }

        // 6. Developer Mode Tools (Hidden unless version tapped 7 times)
        if (isDeveloperModeEnabled) {
            item {
                AccountSectionGroup(
                    title = when (language) {
                        AppLanguage.AR -> "أدوات المطور (مخفية للمستخدم العادي)"
                        AppLanguage.FR -> "Outils développeur (masqués par défaut)"
                        AppLanguage.EN -> "Developer tools (hidden by default)"
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SamplePdfReportsShowcaseCard(currentLanguage = language)
                        OutlinedButton(
                            onClick = onSeedDeveloperDemoInspections,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("dev_seed_sample_inspections_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "إضافة تقارير تجريبية للمقارنة"
                                    AppLanguage.FR -> "Ajouter 2 rapports de démonstration"
                                    AppLanguage.EN -> "Add 2 demo reports for comparison"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountSectionGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = FahsMutedText,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(color = FahsHairline, thickness = 1.dp)
        content()
        HorizontalDivider(color = FahsHairline, thickness = 1.dp)
    }
}

@Composable
private fun AccountHairlineRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable { onClick() }
            .padding(vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = titleColor,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = FahsMutedText
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = FahsMutedText,
            modifier = Modifier.size(20.dp)
        )
    }
}
