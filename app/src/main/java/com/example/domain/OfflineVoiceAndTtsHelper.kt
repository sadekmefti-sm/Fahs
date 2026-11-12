package com.example.domain

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.example.core.i18n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Manages built-in Android offline Text-to-Speech (Read-Aloud) and built-in Android
 * SpeechRecognizer (Voice Notes & [Beta] Voice Answers) with zero external dependencies.
 */
class OfflineVoiceAndTtsController(
    private val context: Context,
    private val onTtsAvailabilityChanged: (Map<AppLanguage, Boolean>) -> Unit = {}
) {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val availableVoices = mutableMapOf(
        AppLanguage.FR to false,
        AppLanguage.EN to false,
        AppLanguage.AR to false
    )

    private val _voiceAvailability = MutableStateFlow<Map<AppLanguage, Boolean>>(
        mapOf(
            AppLanguage.FR to false,
            AppLanguage.EN to false,
            AppLanguage.AR to false
        )
    )
    val voiceAvailability: StateFlow<Map<AppLanguage, Boolean>> = _voiceAvailability.asStateFlow()

    private val _isListeningVoice = MutableStateFlow(false)
    val isListeningVoice: StateFlow<Boolean> = _isListeningVoice.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    checkLanguageVoices()
                } else {
                    isTtsReady = false
                    val snapshot = availableVoices.toMap()
                    _voiceAvailability.value = snapshot
                    onTtsAvailabilityChanged(snapshot)
                }
            }
        } catch (_: Exception) {
            isTtsReady = false
        }
    }

    private fun checkLanguageVoices() {
        val engine = tts ?: return
        AppLanguage.entries.forEach { lang ->
            val locale = localeForLanguage(lang)
            val code = try {
                engine.isLanguageAvailable(locale)
            } catch (_: Exception) {
                TextToSpeech.LANG_NOT_SUPPORTED
            }
            val supported = code >= TextToSpeech.LANG_AVAILABLE &&
                code != TextToSpeech.LANG_MISSING_DATA &&
                code != TextToSpeech.LANG_NOT_SUPPORTED
            availableVoices[lang] = supported
        }
        val snapshot = availableVoices.toMap()
        _voiceAvailability.value = snapshot
        onTtsAvailabilityChanged(snapshot)
    }

    fun isVoiceAvailable(language: AppLanguage): Boolean =
        isTtsReady && (availableVoices[language] == true)

    fun speakQuestion(text: String, language: AppLanguage) {
        if (!isVoiceAvailable(language) || text.isBlank()) return
        val engine = tts ?: return
        try {
            engine.language = localeForLanguage(language)
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fahs_question_tts")
        } catch (_: Exception) {
        }
    }

    fun stopSpeaking() {
        runCatching { tts?.stop() }
    }

    fun startListening(
        language: AppLanguage,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        startSpeechListening(
            language = language,
            onPartialOrFinalResult = { spoken, isFinal ->
                if (isFinal) {
                    _isListeningVoice.value = false
                    onResult(spoken)
                }
            },
            onError = { err ->
                _isListeningVoice.value = false
                onError(err)
            }
        )
    }

    fun startSpeechListening(
        language: AppLanguage,
        onPartialOrFinalResult: (spokenText: String, isFinal: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        stopSpeaking()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _isListeningVoice.value = false
            val msg = when (language) {
                AppLanguage.AR -> "التعرف الصوتي غير متوفر على هذا الهاتف. يرجى الكتابة يدوياً."
                AppLanguage.FR -> "Reconnaissance vocale non disponible sur ce téléphone. Utilisez la saisie manuelle."
                AppLanguage.EN -> "Speech recognition is not available on this phone. Please type manually."
            }
            onError(msg)
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer
            _isListeningVoice.value = true

            val localeTag = when (language) {
                AppLanguage.AR -> "ar-DZ"
                AppLanguage.FR -> "fr-FR"
                AppLanguage.EN -> "en-US"
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _isListeningVoice.value = false
                }

                override fun onError(error: Int) {
                    _isListeningVoice.value = false
                    val errText = when (language) {
                        AppLanguage.AR -> "لم يتم سماع صوت واضح (يمكنك المحاولة مرة أخرى أو الكتابة يدوياً)."
                        AppLanguage.FR -> "Aucune voix détectée clairement (réessayez ou tapez manuellement)."
                        AppLanguage.EN -> "Could not hear speech clearly (try again or type manually)."
                    }
                    onError(errText)
                }

                override fun onResults(results: Bundle?) {
                    _isListeningVoice.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val top = matches?.firstOrNull()?.trim().orEmpty()
                    if (top.isNotEmpty()) {
                        onPartialOrFinalResult(top, true)
                    } else {
                        onError(
                            when (language) {
                                AppLanguage.AR -> "لم يتم التعرف على الكلام."
                                AppLanguage.FR -> "Aucun texte reconnu."
                                AppLanguage.EN -> "No speech recognized."
                            }
                        )
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val top = matches?.firstOrNull()?.trim().orEmpty()
                    if (top.isNotEmpty()) {
                        onPartialOrFinalResult(top, false)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizer.startListening(intent)
        } catch (_: Exception) {
            _isListeningVoice.value = false
            val fallbackMsg = when (language) {
                AppLanguage.AR -> "تعذر تشغيل الميكروفون. يرجى الكتابة يدوياً."
                AppLanguage.FR -> "Impossible de démarrer le microphone. Utilisez la saisie manuelle."
                AppLanguage.EN -> "Could not start microphone. Please type manually."
            }
            onError(fallbackMsg)
        }
    }

    fun stopListening() {
        _isListeningVoice.value = false
        runCatching { speechRecognizer?.stopListening() }
        runCatching { speechRecognizer?.destroy() }
        speechRecognizer = null
    }

    fun shutdown() {
        release()
    }

    fun release() {
        stopListening()
        runCatching { tts?.stop() }
        runCatching { tts?.shutdown() }
        tts = null
    }

    companion object {
        fun localeForLanguage(language: AppLanguage): Locale = when (language) {
            AppLanguage.AR -> Locale("ar")
            AppLanguage.FR -> Locale.FRENCH
            AppLanguage.EN -> Locale.ENGLISH
        }

        /**
         * [Beta] Parses spoken voice commands into checklist answer proposals ("GOOD", "PROBLEM", "SKIPPED").
         * Supports French and English as specified in Phase 7 item 9.
         */
        fun parseBetaVoiceAnswerCommand(spokenText: String, language: AppLanguage): String? {
            if (language == AppLanguage.AR) return null // Offered in FR and EN per Phase 7 item 9
            val normalized = spokenText.trim().lowercase(Locale.ROOT)
            if (normalized.isEmpty()) return null

            val goodKeywords = listOf(
                "good", "ok", "okay", "pass", "normal", "clean",
                "bon", "bien", "correct", "impeccable", "propre", "ras"
            )
            val problemKeywords = listOf(
                "problem", "bad", "defect", "issue", "fault", "broken", "leak",
                "problème", "probleme", "mauvais", "défaut", "defaut", "panne", "fuite"
            )
            val skipKeywords = listOf(
                "skip", "unchecked", "next", "passer", "ignorer", "non vérifié", "non verifie", "suivant"
            )

            return when {
                problemKeywords.any { normalized.contains(it) } -> "PROBLEM"
                skipKeywords.any { normalized.contains(it) } -> "SKIPPED"
                goodKeywords.any { normalized.contains(it) } -> "GOOD"
                else -> null
            }
        }
    }
}
