package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.i18n.AppLanguage
import com.example.core.photo.InspectionPhotoHelper
import com.example.data.local.ChecklistAnswerEntity
import com.example.domain.OfflineVoiceAndTtsController
import com.example.domain.OnDeviceOcrHelper
import com.example.domain.TyreDotOcrProposal
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
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
import com.example.ui.viewmodel.FahsViewModel
import java.io.File
import kotlinx.coroutines.launch

/**
 * Returns true if the checklist item was automatically answered by a confirmed OBD2 scan.
 */
fun isChecklistItemAnsweredByObdScan(saved: ChecklistAnswerEntity?): Boolean {
    return saved != null && saved.note.contains(FahsViewModel.OBD_AUTO_ANSWER_TAG)
}

/**
 * Phase 7 (Item 1): Banner shown when viewing an item that was automatically answered by the OBD scan.
 */
@Composable
fun AnsweredByObdScanBanner(
    savedAnswer: ChecklistAnswerEntity,
    language: AppLanguage
) {
    val isGood = savedAnswer.status == "GOOD"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("answered_by_obd_scan_banner"),
        shape = RoundedCornerShape(14.dp),
        color = if (isGood) StatusPassGreenContainer else StatusWarningAmberContainer,
        border = BorderStroke(1.5.dp, if (isGood) StatusPassGreen else StatusWarningAmber)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = if (isGood) StatusPassGreen else StatusWarningAmberOnContainer,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "تمت الإجابة تلقائياً بواسطة فحص OBD2 ✓"
                        AppLanguage.FR -> "Répondu automatiquement par le scan OBD2 ✓"
                        AppLanguage.EN -> "Answered automatically by the OBD2 scan ✓"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isGood) StatusPassGreenOnContainer else StatusWarningAmberOnContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = savedAnswer.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "تم تخطي هذا السؤال تلقائياً. يمكنك دائماً تغيير الإجابة يدوياً بالأسفل إذا رغبت."
                        AppLanguage.FR -> "Question sautée automatiquement. Vous pouvez modifier la réponse manuellement ci-dessous."
                        AppLanguage.EN -> "Skipped automatically. You can still override this answer manually below."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate700
                )
            }
        }
    }
}

/**
 * Phase 7 (Item 5 & Item 9 Beta):
 * - Read-Aloud chip (hidden if the phone has no TTS voice for [language])
 * - Beta Voice Answer ("good", "problem", "skip" in FR & EN) when Read-Aloud is active,
 *   proposing the recognized answer for user confirmation before saving.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReadAloudAndBetaVoiceAnswerBar(
    isVoiceAvailableForLanguage: Boolean,
    isReadAloudEnabled: Boolean,
    onToggleReadAloud: (Boolean) -> Unit,
    onReplayQuestionAloud: () -> Unit,
    language: AppLanguage,
    isListeningVoice: Boolean,
    voiceController: OfflineVoiceAndTtsController,
    onConfirmVoiceAnswer: (String) -> Unit
) {
    // Rule 5: Hide the Read-Aloud option for a language the phone has no voice for
    if (!isVoiceAvailableForLanguage) return

    val context = LocalContext.current
    var showMicPermExplanation by remember { mutableStateOf(false) }
    var proposedVoiceAnswerStatus by remember { mutableStateOf<String?>(null) }
    var proposedVoiceRawSpoken by remember { mutableStateOf("") }
    var voiceStatusHint by remember { mutableStateOf<String?>(null) }

    fun startVoiceCommandListening() {
        voiceStatusHint = null
        voiceController.startListening(
            language = language,
            onResult = { spoken ->
                val parsed = OfflineVoiceAndTtsController.parseBetaVoiceAnswerCommand(spoken, language)
                if (parsed != null) {
                    proposedVoiceRawSpoken = spoken
                    proposedVoiceAnswerStatus = parsed
                } else {
                    voiceStatusHint = when (language) {
                        AppLanguage.FR -> "Non reconnu (« $spoken »). Dites « Bon », « Problème » ou « Passer », ou utilisez les boutons."
                        else -> "Unrecognized (\"$spoken\"). Say \"Good\", \"Problem\", or \"Skip\", or tap the manual buttons."
                    }
                }
            },
            onError = { err ->
                voiceStatusHint = err
            }
        )
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startVoiceCommandListening()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("read_aloud_bar_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isReadAloudEnabled) Emerald600 else Slate200)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToggleReadAloud(!isReadAloudEnabled) }
                        .testTag("toggle_read_aloud_chip"),
                    color = if (isReadAloudEnabled) Emerald700 else Slate100,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = if (isReadAloudEnabled) Color.White else Emerald800,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> if (isReadAloudEnabled) "القراءة الصوتية مفعلة ✓" else "تشغيل القراءة الصوتية للسؤال"
                                AppLanguage.FR -> if (isReadAloudEnabled) "Lecture vocale activée ✓" else "Activer lecture à voix haute"
                                AppLanguage.EN -> if (isReadAloudEnabled) "Read Aloud ON ✓" else "Read Questions Aloud"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isReadAloudEnabled) Color.White else Slate900
                        )
                    }
                }

                if (isReadAloudEnabled) {
                    OutlinedButton(
                        onClick = onReplayQuestionAloud,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "🔊 إعادة الاستماع"
                                AppLanguage.FR -> "🔊 Réécouter"
                                AppLanguage.EN -> "🔊 Replay"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Phase 7 (Item 9 Beta): Voice answers in Read-Aloud mode for French and English
                    if (language == AppLanguage.FR || language == AppLanguage.EN) {
                        OutlinedButton(
                            onClick = {
                                val hasMic = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasMic) {
                                    startVoiceCommandListening()
                                } else {
                                    showMicPermExplanation = true
                                }
                            },
                            enabled = !isListeningVoice,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("beta_voice_answer_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = Emerald800
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.FR -> "[Beta] Répondre à la voix (Bon / Problème / Passer)"
                                    else -> "[Beta] Voice Answer (Good / Problem / Skip)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Emerald800
                            )
                        }
                    }
                }
            }

            if (showMicPermExplanation) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "نطلب صلاحية الميكروفون لتحويل صوتك إلى نص أو إجابة محلياً على الهاتف."
                                AppLanguage.FR -> "L'accès au microphone permet de reconnaître votre réponse vocale (« Bon », « Problème », « Passer ») hors-ligne."
                                AppLanguage.EN -> "Microphone access is used to recognize your spoken answer ('Good', 'Problem', 'Skip') on-device."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald900
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                showMicPermExplanation = false
                                micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "السماح بالميكروفون"
                                    AppLanguage.FR -> "Autoriser le microphone"
                                    AppLanguage.EN -> "Allow Microphone"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Rule 1: Confirmation banner for Beta Voice Answer before entering report
            val pendingStatus = proposedVoiceAnswerStatus
            if (pendingStatus != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val statusLabel = when (pendingStatus) {
                    "GOOD" -> if (language == AppLanguage.FR) "Bon état / RAS" else "Good / Passed"
                    "PROBLEM" -> if (language == AppLanguage.FR) "Problème détecté" else "Problem Found"
                    else -> if (language == AppLanguage.FR) "Non vérifié / Passer" else "Skip / Not Checked"
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("beta_voice_answer_confirmation_box"),
                    shape = RoundedCornerShape(10.dp),
                    color = Emerald50,
                    border = BorderStroke(1.5.dp, Emerald600)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (language == AppLanguage.FR) {
                                "[Beta] Réponse vocale reconnue (« $proposedVoiceRawSpoken ») → $statusLabel. Confirmer ?"
                            } else {
                                "[Beta] Voice answer recognized (\"$proposedVoiceRawSpoken\") → $statusLabel. Confirm?"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald900
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onConfirmVoiceAnswer(pendingStatus)
                                    proposedVoiceAnswerStatus = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                modifier = Modifier.testTag("confirm_beta_voice_answer_btn")
                            ) {
                                Text(
                                    text = if (language == AppLanguage.FR) "Confirmer ✓" else "Confirm ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            OutlinedButton(
                                onClick = { proposedVoiceAnswerStatus = null },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.FR) "Annuler" else "Cancel",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            if (voiceStatusHint != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = voiceStatusHint!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
            }
        }
    }
}

/**
 * Phase 7 (Item 8 Beta): Tyre age from a photo + side-by-side manual 4-digit DOT code entry.
 * Always shows the manual entry right beside the photo button because raised black text on black
 * rubber is hard to read. Any recognized/parsed DOT date is a suggestion confirmed by the user.
 */
@Composable
fun BetaTyreAgeReaderCard(
    language: AppLanguage,
    onConfirmTyreAgeNote: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var manualDotDigits by remember { mutableStateOf("") }
    var proposedDot by remember { mutableStateOf<TyreDotOcrProposal?>(null) }
    var isRunningOcr by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showCamPermExplanation by remember { mutableStateOf(false) }
    var pendingCamFile by remember { mutableStateOf<File?>(null) }

    fun processTyrePhoto(file: File) {
        isRunningOcr = true
        statusMessage = null
        scope.launch {
            val res = OnDeviceOcrHelper.recognizeTyreDotFromFile(file)
            isRunningOcr = false
            try { file.delete() } catch (_: Exception) {}
            if (res != null) {
                proposedDot = res
                manualDotDigits = res.fourDigitCode
            } else {
                statusMessage = when (language) {
                    AppLanguage.AR -> "تعذرت قراءة الكود البارز من المطاط الأسود تلقائياً. يرجى إدخال الأرقام الأربعة (WWYY) يدوياً في الحقل المجاور."
                    AppLanguage.FR -> "Texte noir sur caoutchouc difficile à lire sur cette photo. Saisissez les 4 chiffres DOT (SSAA) manuellement à côté."
                    AppLanguage.EN -> "Raised black text on rubber was hard to read in this photo. Please enter the 4-digit DOT code (WWYY) in the manual box beside it."
                }
            }
        }
    }

    val takePicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { ok ->
        val f = pendingCamFile
        pendingCamFile = null
        if (ok && f != null) {
            processTyrePhoto(f)
        } else {
            try { f?.delete() } catch (_: Exception) {}
        }
    }

    val camPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (tempFile, uri) = InspectionPhotoHelper.createTempCameraUri(context, "tyre_dot_ocr")
            pendingCamFile = tempFile
            takePicLauncher.launch(uri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val path = InspectionPhotoHelper.copyUriToInternalStorage(context, uri, "tyre_dot_pick")
            if (path != null) {
                processTyrePhoto(File(path))
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("beta_tyre_dot_reader_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Emerald600)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "[تجريبي Beta] قراءة عمر الإطارات (كود DOT من 4 أرقام)"
                        AppLanguage.FR -> "[Bêta] Âge des pneus — Code DOT à 4 chiffres (Photo + Manuel)"
                        AppLanguage.EN -> "[Beta] Tyre Age — 4-Digit DOT Code (Photo + Manual Entry)"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Emerald900,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (language) {
                    AppLanguage.AR -> "الكتابة البارزة على المطاط الأسود قد يصعب التقاطها بالكاميرا، لذلك يتوفر الإدخال اليدوي للأرقام الأربعة (الأسبوع + السنة مثل 2421) جنباً إلى جنب مع التصوير:"
                    AppLanguage.FR -> "Le relief noir sur pneu noir étant difficile à lire par caméra, la saisie manuelle des 4 chiffres (ex. 2421 = sem. 24 de 2021) reste toujours affichée à côté :"
                    AppLanguage.EN -> "Raised black text on black rubber is hard to read by camera, so manual 4-digit entry (e.g. 2421 = week 24 of 2021) is always shown beside the photo button:"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Side-by-side: Photo button + Manual 4-digit entry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualDotDigits,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(4)
                        manualDotDigits = digits
                        if (digits.length == 4) {
                            proposedDot = OnDeviceOcrHelper.parseManualTyreDotCode(digits)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("manual_tyre_dot_input"),
                    label = {
                        Text(
                            when (language) {
                                AppLanguage.AR -> "كود DOT يدوي (4 أرقام)"
                                AppLanguage.FR -> "DOT manuel (4 chiffres)"
                                AppLanguage.EN -> "Manual DOT (4 digits)"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    placeholder = { Text("2421") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedButton(
                    onClick = {
                        val hasCam = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasCam) {
                            val (tempFile, uri) = InspectionPhotoHelper.createTempCameraUri(context, "tyre_dot_ocr")
                            pendingCamFile = tempFile
                            takePicLauncher.launch(uri)
                        } else {
                            showCamPermExplanation = true
                        }
                    },
                    enabled = !isRunningOcr,
                    modifier = Modifier.testTag("scan_tyre_dot_photo_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "صورة [Beta]"
                            AppLanguage.FR -> "Photo [Bêta]"
                            AppLanguage.EN -> "Photo [Beta]"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isRunningOcr,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (showCamPermExplanation) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "تُستخدم الكاميرا لتصوير جانب الإطار ومحاولة قراءة الأرقام الأربعة لتاريخ الصنع محلياً."
                                AppLanguage.FR -> "La caméra sert à photographier le flanc du pneu pour tenter de lire les 4 chiffres DOT hors-ligne."
                                AppLanguage.EN -> "Camera access is used to photograph the tyre sidewall and read the 4-digit DOT date code offline."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald900
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                showCamPermExplanation = false
                                camPermLauncher.launch(Manifest.permission.CAMERA)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "فتح الكاميرا"
                                    AppLanguage.FR -> "Ouvrir la caméra"
                                    AppLanguage.EN -> "Open Camera"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            val currentProp = proposedDot
            if (currentProp != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tyre_dot_confirmation_box"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (currentProp.isExpiredOrOld) StatusWarningAmberContainer else StatusPassGreenContainer,
                    border = BorderStroke(1.dp, if (currentProp.isExpiredOrOld) StatusWarningAmber else StatusPassGreen)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        val summaryLine = when (language) {
                            AppLanguage.AR -> "كود DOT ${currentProp.fourDigitCode}: الأسبوع ${currentProp.manufacturingWeek} من سنة ${currentProp.manufacturingYear} (العمر التقريبي: ${currentProp.ageYearsApprox} سنوات)"
                            AppLanguage.FR -> "Code DOT ${currentProp.fourDigitCode} : Semaine ${currentProp.manufacturingWeek} de ${currentProp.manufacturingYear} (âge approx. : ${currentProp.ageYearsApprox} ans)"
                            AppLanguage.EN -> "DOT ${currentProp.fourDigitCode}: Week ${currentProp.manufacturingWeek} of ${currentProp.manufacturingYear} (~${currentProp.ageYearsApprox} years old)"
                        }
                        Text(
                            text = summaryLine,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        if (currentProp.isExpiredOrOld) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "⚠️ تنبيه: الإطار عمره 5 سنوات أو أكثر (يُنصح بتغييره حتى لو بدت النقشة جيدة)."
                                    AppLanguage.FR -> "⚠️ Attention : Pneu âgé de 5 ans ou plus (remplacement conseillé)."
                                    AppLanguage.EN -> "⚠️ Note: Tyre is 5+ years old (replacement recommended)."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusErrorRed
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onConfirmTyreAgeNote(summaryLine, currentProp.isExpiredOrOld)
                                    proposedDot = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                modifier = Modifier.testTag("confirm_tyre_dot_btn")
                            ) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "تأكيد وإضافة للملاحظة ✓"
                                        AppLanguage.FR -> "Confirmer & Ajouter à la note ✓"
                                        AppLanguage.EN -> "Confirm & Add to Note ✓"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            OutlinedButton(
                                onClick = { proposedDot = null },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.AR -> "إغلاق"
                                        AppLanguage.FR -> "Fermer"
                                        AppLanguage.EN -> "Dismiss"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = statusMessage!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate700
                )
            }
        }
    }
}

/**
 * Phase 7 (Item 1): Finds the next checklist index, automatically skipping items
 * that were already answered by the OBD2 scan so the user is not asked those questions again.
 */
fun findNextIndexSkippingObdAnswered(
    currentIndex: Int,
    adaptiveItems: List<com.example.data.model.ChecklistItemDefinition>,
    answersMap: Map<String, ChecklistAnswerEntity>
): Int {
    for (idx in (currentIndex + 1) until adaptiveItems.size) {
        val ans = answersMap[adaptiveItems[idx].id]
        if (!isChecklistItemAnsweredByObdScan(ans)) {
            return idx
        }
    }
    return (currentIndex + 1).coerceAtMost((adaptiveItems.size - 1).coerceAtLeast(0))
}

fun findPreviousIndexSkippingObdAnswered(
    currentIndex: Int,
    adaptiveItems: List<com.example.data.model.ChecklistItemDefinition>,
    answersMap: Map<String, ChecklistAnswerEntity>
): Int {
    for (idx in (currentIndex - 1) downTo 0) {
        val ans = answersMap[adaptiveItems[idx].id]
        if (!isChecklistItemAnsweredByObdScan(ans)) {
            return idx
        }
    }
    return (currentIndex - 1).coerceAtLeast(0)
}

/**
 * Phase 7 (Item 4): Voice notes with microphone button on the note field.
 * Turns speech into text using Android's built-in speech recognition (offline where language is installed),
 * and proposes the recognized text for user confirmation before adding it to the report note.
 */
@Composable
fun VoiceNoteDictationRow(
    language: AppLanguage,
    isListeningVoice: Boolean,
    voiceController: OfflineVoiceAndTtsController,
    onConfirmDictatedNote: (String) -> Unit
) {
    val context = LocalContext.current
    var showMicExplanation by remember { mutableStateOf(false) }
    var proposedVoiceNote by remember { mutableStateOf<String?>(null) }
    var statusError by remember { mutableStateOf<String?>(null) }

    fun startDictation() {
        statusError = null
        voiceController.startListening(
            language = language,
            onResult = { spoken ->
                proposedVoiceNote = spoken
            },
            onError = { err ->
                statusError = err
            }
        )
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startDictation()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (language) {
                    AppLanguage.AR -> "ملاحظات المعاينة (كتابة يدوية أو إملاء صوتي):"
                    AppLanguage.FR -> "Note d'observation (saisie manuelle ou dictée vocale) :"
                    AppLanguage.EN -> "Inspection Note (manual typing or voice dictation):"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.weight(1f)
            )

            OutlinedButton(
                onClick = {
                    val hasMic = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasMic) {
                        startDictation()
                    } else {
                        showMicExplanation = true
                    }
                },
                enabled = !isListeningVoice,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("wizard_voice_note_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Dictate voice note",
                    tint = Emerald800,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isListeningVoice) {
                        when (language) {
                            AppLanguage.AR -> "جاري الاستماع..."
                            AppLanguage.FR -> "Écoute..."
                            AppLanguage.EN -> "Listening..."
                        }
                    } else {
                        when (language) {
                            AppLanguage.AR -> "🎤 إملاء صوتي"
                            AppLanguage.FR -> "🎤 Dicter note"
                            AppLanguage.EN -> "🎤 Voice Note"
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Emerald800
                )
            }
        }

        if (showMicExplanation) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Emerald50,
                border = BorderStroke(1.dp, Emerald200)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "لماذا نطلب الميكروفون؟ لتحويل كلامك إلى ملاحظة مكتوبة باستخدام التعرف الصوتي المدمج في أندرويد (دون إنترنت عند توفر حزمة اللغة)."
                            AppLanguage.FR -> "Pourquoi le microphone ? Pour transcrire votre note vocale via la reconnaissance vocale intégrée d'Android (hors-ligne si la langue est installée)."
                            AppLanguage.EN -> "Why allow Microphone? To turn your speech into a text note using Android's built-in speech recognition (offline where the language pack is installed)."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            showMicExplanation = false
                            micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.AR -> "السماح وبدء الإملاء"
                                AppLanguage.FR -> "Autoriser & Dicter"
                                AppLanguage.EN -> "Allow & Dictate"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        val pendingNote = proposedVoiceNote
        if (!pendingNote.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_note_confirmation_box"),
                shape = RoundedCornerShape(10.dp),
                color = Emerald50,
                border = BorderStroke(1.5.dp, Emerald600)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "النص المقترح من الصوت (يرجى التأكيد لإضافته للملاحظة):"
                            AppLanguage.FR -> "Texte reconnu par la voix (confirmez pour l'ajouter à la note) :"
                            AppLanguage.EN -> "Proposed text from voice (confirm to add to note):"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "« $pendingNote »",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onConfirmDictatedNote(pendingNote)
                                proposedVoiceNote = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                            modifier = Modifier.testTag("confirm_voice_note_btn")
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "تأكيد وإضافة للملاحظة ✓"
                                    AppLanguage.FR -> "Confirmer & Ajouter ✓"
                                    AppLanguage.EN -> "Confirm & Add ✓"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        OutlinedButton(
                            onClick = { proposedVoiceNote = null },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.AR -> "إلغاء"
                                    AppLanguage.FR -> "Annuler"
                                    AppLanguage.EN -> "Dismiss"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        if (statusError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusError!!,
                style = MaterialTheme.typography.labelSmall,
                color = Slate600
            )
        }
    }
}
