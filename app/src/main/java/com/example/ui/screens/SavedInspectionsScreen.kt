package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.auth.GoogleAuthHelper
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.data.local.InspectionEntity
import com.example.domain.ReportEngine
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusWarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedInspectionsScreen(
    language: AppLanguage,
    inspections: List<InspectionEntity>,
    answersByInspectionId: Map<Long, List<com.example.data.local.ChecklistAnswerEntity>> = emptyMap(),
    cloudUserEmail: String?,
    cloudUserName: String?,
    isCloudSyncing: Boolean,
    isDeveloperModeEnabled: Boolean = false,
    onRefreshAuth: () -> Unit,
    onBackupToCloud: () -> Unit,
    onRestoreFromCloud: () -> Unit,
    onDeleteAllCloudBackups: () -> Unit = {},
    onSeedComparisonDemo: () -> Unit,
    onNotifyMessage: (String) -> Unit,
    onStartNewInspection: () -> Unit,
    onSelectInspection: (Long) -> Unit,
    onDuplicateInspection: (Long) -> Unit = {},
    onDeleteInspection: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { androidx.credentials.CredentialManager.create(context) }

    var selectedFilter by remember { mutableStateOf("ALL") }
    var itemToDelete by remember { mutableStateOf<InspectionEntity?>(null) }
    var showDeleteCloudConfirm by remember { mutableStateOf(false) }
    val selectedForComparison = remember { mutableStateListOf<Long>() }
    var showComparisonDialog by remember { mutableStateOf(false) }

    val filteredInspections = remember(inspections, selectedFilter) {
        when (selectedFilter) {
            "IN_PROGRESS" -> inspections.filter { it.status == "IN_PROGRESS" }
            "COMPLETED" -> inspections.filter { it.status == "COMPLETED" }
            else -> inspections
        }
    }

    val comparedInspections = remember(inspections, selectedForComparison.toList()) {
        inspections.filter { it.id in selectedForComparison }
    }

    if (showComparisonDialog && comparedInspections.size >= 2) {
        CarComparisonDialog(
            inspections = comparedInspections,
            answersByInspectionId = answersByInspectionId,
            language = language,
            onSelectCarReport = { id ->
                showComparisonDialog = false
                onSelectInspection(id)
            },
            onDismiss = { showComparisonDialog = false }
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    text = Strings.delete(language),
                    fontWeight = FontWeight.Bold,
                    color = StatusErrorRed
                )
            },
            text = {
                Text(
                    text = Strings.deleteConfirmMsg(language),
                    color = Slate700
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let {
                            selectedForComparison.remove(it.id)
                            onDeleteInspection(it.id)
                        }
                        itemToDelete = null
                    }
                ) {
                    Text(
                        text = Strings.delete(language),
                        fontWeight = FontWeight.Bold,
                        color = StatusErrorRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(
                        text = Strings.cancel(language),
                        color = Slate600
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDeleteCloudConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteCloudConfirm = false },
            title = {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "حذف النسخ الاحتياطية السحابية؟"
                        AppLanguage.FR -> "Supprimer les sauvegardes Cloud ?"
                        AppLanguage.EN -> "Delete All Cloud Backups?"
                    },
                    fontWeight = FontWeight.Bold,
                    color = StatusErrorRed
                )
            },
            text = {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "سيتم حذف جميع الفحوصات المحفوظة في مساحتك السحابية نهائياً. ستبقى الفحوصات المحفوظة داخل هاتفك كما هي."
                        AppLanguage.FR -> "Toutes les inspections enregistrées dans votre espace Cloud seront supprimées définitivement. Vos inspections locales sur ce téléphone seront conservées."
                        AppLanguage.EN -> "All inspections stored in your Cloud backup will be permanently deleted. Your local inspections on this phone will remain untouched."
                    },
                    color = Slate700
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteCloudConfirm = false
                        onDeleteAllCloudBackups()
                    },
                    modifier = Modifier.testTag("confirm_delete_cloud_backups_btn")
                ) {
                    Text(
                        text = Strings.delete(language),
                        fontWeight = FontWeight.Bold,
                        color = StatusErrorRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCloudConfirm = false }) {
                    Text(
                        text = Strings.cancel(language),
                        color = Slate600
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Sticky Comparison Action Bar when at least 1 car is selected
        if (selectedForComparison.isNotEmpty()) {
            Surface(
                color = Emerald800,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "مقارنة السيارات: تم اختيار ${selectedForComparison.size}/3"
                                AppLanguage.FR -> "Comparateur : ${selectedForComparison.size}/3 sélectionnée(s)"
                                AppLanguage.EN -> "Compare: ${selectedForComparison.size}/3 selected"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (selectedForComparison.size < 2) {
                                when (language) {
                                    AppLanguage.AR -> "اختر سيارة ثانية على الأقل لفتح جدول المقارنة"
                                    AppLanguage.FR -> "Sélectionnez au moins une 2ème voiture pour comparer"
                                    AppLanguage.EN -> "Select at least a 2nd car to open comparison"
                                }
                            } else {
                                when (language) {
                                    AppLanguage.AR -> "جاهز للمقارنة جنباً إلى جنب"
                                    AppLanguage.FR -> "Prêt pour la comparaison côte à côte"
                                    AppLanguage.EN -> "Ready for side-by-side comparison"
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald100
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { selectedForComparison.clear() }
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "إلغاء"
                                    AppLanguage.FR -> "Vider"
                                    AppLanguage.EN -> "Clear"
                                },
                                color = Emerald100
                            )
                        }

                        if (selectedForComparison.size >= 2) {
                            Button(
                                onClick = { showComparisonDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Emerald900
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("open_comparison_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "قارن الآن"
                                        AppLanguage.FR -> "Comparer"
                                        AppLanguage.EN -> "Compare"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Optional Google Cloud Backup & Restore Card
            item {
                OptionalCloudBackupCard(
                    language = language,
                    cloudUserEmail = cloudUserEmail,
                    cloudUserName = cloudUserName,
                    isCloudSyncing = isCloudSyncing,
                    onSignInClick = {
                        GoogleAuthHelper.onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                onRefreshAuth()
                                val msg = when (language) {
                                    AppLanguage.AR -> "تم تسجيل الدخول بحساب Google بنجاح"
                                    AppLanguage.FR -> "Connecté avec Google avec succès"
                                    AppLanguage.EN -> "Signed in with Google"
                                }
                                onNotifyMessage(msg)
                            },
                            onAuthError = { err ->
                                onNotifyMessage(err)
                            },
                            scope = coroutineScope
                        )
                    },
                    onSignOutClick = {
                        GoogleAuthHelper.signOut(
                            credentialManager = credentialManager,
                            scope = coroutineScope,
                            onSignOutComplete = {
                                onRefreshAuth()
                                val msg = when (language) {
                                    AppLanguage.AR -> "تم تسجيل الخروج من الحساب السحابي"
                                    AppLanguage.FR -> "Déconnecté du compte Cloud"
                                    AppLanguage.EN -> "Signed out of Cloud account"
                                }
                                onNotifyMessage(msg)
                            }
                        )
                    },
                    onBackupClick = onBackupToCloud,
                    onRestoreClick = onRestoreFromCloud,
                    onDeleteCloudBackupsClick = { showDeleteCloudConfirm = true }
                )
            }

            // 2. Filter Chips + Compare Helper Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("${Strings.viewAll(language)} (${inspections.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald700,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "IN_PROGRESS",
                            onClick = { selectedFilter = "IN_PROGRESS" },
                            label = { Text(Strings.statusInProgress(language)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusWarningAmber,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "COMPLETED",
                            onClick = { selectedFilter = "COMPLETED" },
                            label = { Text(Strings.statusCompleted(language)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusPassGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    // Helper banner for side-by-side comparison
                    if (inspections.size >= 2 && selectedForComparison.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Emerald100.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                    contentDescription = null,
                                    tint = Emerald800,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "نصيحة: اضغط على زر «مقارنة» في بطاقتين أو 3 بطاقات لمقارنة السيارات جنباً إلى جنب."
                                        AppLanguage.FR -> "Astuce : Cochez « Comparer » sur 2 ou 3 voitures pour les comparer côte à côte."
                                        AppLanguage.EN -> "Tip: Tap 'Compare' on 2 or 3 cars below to compare them side by side."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Emerald900,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (isDeveloperModeEnabled && inspections.size < 2) {
                        OutlinedButton(
                            onClick = onSeedComparisonDemo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("seed_comparison_demo_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "[وضع المطور] إضافة سيارتين نموذجيتين لتجربة المقارنة"
                                    AppLanguage.FR -> "[DEV] Charger 2 voitures d'exemple pour tester le comparateur"
                                    AppLanguage.EN -> "[DEV] Load 2 sample cars to test Side-by-Side Comparison"
                                },
                                color = Emerald800,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            if (filteredInspections.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Emerald100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = Emerald700,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = Strings.noInspectionsYet(language),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = Strings.noInspectionsDesc(language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate600,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onStartNewInspection,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Strings.startInspectionTitle(language),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredInspections, key = { it.id }) { inspection ->
                    val isSelectedForCompare = inspection.id in selectedForComparison
                    SavedInspectionDetailedCard(
                        inspection = inspection,
                        language = language,
                        isSelectedForCompare = isSelectedForCompare,
                        onToggleCompare = {
                            if (isSelectedForCompare) {
                                selectedForComparison.remove(inspection.id)
                            } else {
                                if (selectedForComparison.size >= 3) {
                                    val maxMsg = when (language) {
                                        AppLanguage.AR -> "يمكنك مقارنة 3 سيارات كحد أقصى في نفس الوقت"
                                        AppLanguage.FR -> "Vous pouvez comparer jusqu'à 3 voitures maximum"
                                        AppLanguage.EN -> "You can compare up to 3 cars at once"
                                    }
                                    onNotifyMessage(maxMsg)
                                } else {
                                    selectedForComparison.add(inspection.id)
                                }
                            }
                        },
                        onClick = { onSelectInspection(inspection.id) },
                        onDuplicate = { onDuplicateInspection(inspection.id) },
                        onDelete = { itemToDelete = inspection }
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionalCloudBackupCard(
    language: AppLanguage,
    cloudUserEmail: String?,
    cloudUserName: String?,
    isCloudSyncing: Boolean,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onDeleteCloudBackupsClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("optional_cloud_backup_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (cloudUserEmail != null) Emerald100 else Slate100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (cloudUserEmail != null) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = if (cloudUserEmail != null) Emerald700 else Slate600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "النسخ الاحتياطي السحابي (اختياري)"
                                AppLanguage.FR -> "Sauvegarde Cloud (Optionnelle)"
                                AppLanguage.EN -> "Cloud Backup (Optional)"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = if (cloudUserEmail != null) {
                                "${cloudUserName ?: ""} ($cloudUserEmail)".trim()
                            } else {
                                when (language) {
                                    AppLanguage.AR -> "التطبيق يعمل 100% بدون إنترنت أو حساب. سجل الدخول لحفظ فحوصاتك."
                                    AppLanguage.FR -> "100% utilisable sans compte. Connectez-vous pour sauvegarder vos rapports."
                                    AppLanguage.EN -> "100% usable offline without an account. Sign in to back up inspections."
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }

                if (isCloudSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = Emerald700
                    )
                }
            }

            if (cloudUserEmail == null) {
                Button(
                    onClick = onSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_sign_in_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "تسجيل الدخول عبر Google للنسخ الاحتياطي"
                            AppLanguage.FR -> "Se connecter avec Google (Sauvegarde Cloud)"
                            AppLanguage.EN -> "Sign in with Google (Cloud Backup)"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBackupClick,
                        enabled = !isCloudSyncing,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cloud_backup_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "حفظ سحابي"
                                AppLanguage.FR -> "Sauvegarder"
                                AppLanguage.EN -> "Back Up"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onRestoreClick,
                        enabled = !isCloudSyncing,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cloud_restore_btn"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Emerald800,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "استرجاع"
                                AppLanguage.FR -> "Restaurer"
                                AppLanguage.EN -> "Restore"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald800
                        )
                    }

                    IconButton(
                        onClick = onDeleteCloudBackupsClick,
                        enabled = !isCloudSyncing,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("cloud_delete_all_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Cloud Backups",
                            tint = StatusErrorRed.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onSignOutClick,
                        enabled = !isCloudSyncing,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("cloud_sign_out_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = Slate600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavedInspectionDetailedCard(
    inspection: InspectionEntity,
    language: AppLanguage,
    isSelectedForCompare: Boolean = false,
    onToggleCompare: () -> Unit = {},
    onDuplicate: () -> Unit = {},
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateFormatted = remember(inspection.updatedAt) { dateFormat.format(Date(inspection.updatedAt)) }
    val isCompleted = inspection.status.equals("COMPLETED", ignoreCase = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelectedForCompare) 2.dp else 0.dp,
                color = if (isSelectedForCompare) Emerald600 else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag("saved_inspection_${inspection.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelectedForCompare) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Brand/Model & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "${inspection.carBrand} ${inspection.carModel}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val plateSuffix = if (inspection.plateNumber.isNotBlank()) " • ${inspection.plateNumber}" else ""
                    Text(
                        text = "${inspection.carGeneration} • ${inspection.carYear}$plateSuffix",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusBadge(status = inspection.status, language = language)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs badges + Compare Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = inspection.fuelType,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate700,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = inspection.gearboxType,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate700,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Emerald100
                    ) {
                        Text(
                            text = inspection.inspectionMode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Emerald900,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Compare Selection Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelectedForCompare) Emerald700 else Slate100,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleCompare() }
                        .testTag("compare_toggle_${inspection.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelectedForCompare) Icons.Default.Check else Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            tint = if (isSelectedForCompare) Color.White else Slate700,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "مقارنة"
                                AppLanguage.FR -> "Comparer"
                                AppLanguage.EN -> "Compare"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelectedForCompare) Color.White else Slate700,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress, Repair Cost & Score Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "الإجابات: ${inspection.answeredQuestions}/${inspection.totalQuestions} • المشاكل: ${inspection.problemCount}"
                            AppLanguage.FR -> "Réponses : ${inspection.answeredQuestions}/${inspection.totalQuestions} • Défauts : ${inspection.problemCount}"
                            AppLanguage.EN -> "Answered: ${inspection.answeredQuestions}/${inspection.totalQuestions} • Defects: ${inspection.problemCount}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (inspection.problemCount > 0) StatusWarningAmber else Slate600
                    )
                    if (inspection.userQuotedRepairDzd > 0) {
                        Text(
                            text = ReportEngine.formatAlgerianCurrency(inspection.userQuotedRepairDzd, language),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Slate700
                        )
                    }
                }

                if (inspection.overallScore != null) {
                    Text(
                        text = "${inspection.overallScore}/100",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        color = when {
                            inspection.overallScore >= 78 -> StatusPassGreen
                            inspection.overallScore >= 50 -> StatusWarningAmber
                            else -> StatusErrorRed
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("duplicate_inspection_${inspection.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate",
                            tint = Slate600,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_inspection_${inspection.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = StatusErrorRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) Emerald700 else StatusWarningAmber
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.Visibility else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCompleted) Strings.viewReport(language) else Strings.resumeInspection(language),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
