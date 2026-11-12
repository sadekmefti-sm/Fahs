package com.example.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.core.i18n.AppLanguage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

data class OdometerOcrCandidate(
    val mileageKm: Int,
    val rawMatchedText: String
)

data class OdometerOcrProposal(
    val proposedKm: Int,
    val allCandidatesKm: List<Int>,
    val rawMatchedText: String
)

data class TyreDotCandidate(
    val fourDigitCode: String,
    val weekOfYear: Int,
    val manufactureYear: Int,
    val ageYears: Int,
    val isExpiredOrOld: Boolean // >= 5 years warning
) {
    val dotCode: String get() = fourDigitCode
    val week: Int get() = weekOfYear
    val year: Int get() = manufactureYear
    val manufacturingWeek: Int get() = weekOfYear
    val manufacturingYear: Int get() = manufactureYear
    val ageYearsApprox: Int get() = ageYears
    val isFiveYearsOrOlder: Boolean get() = isExpiredOrOld

    fun formatSummary(language: AppLanguage): String = when (language) {
        AppLanguage.AR -> {
            val status = when {
                ageYears >= 6 -> "تاريخ قديم ($ageYears سنوات — يُنصح بتغيير الإطار)"
                ageYears == 5 -> "عمر متوسط (5 سنوات — افحص التشققات)"
                else -> "تاريخ حديث ($ageYears سنوات)"
            }
            "الكود $fourDigitCode (الأسبوع $weekOfYear من سنة $manufactureYear) • $status"
        }
        AppLanguage.FR -> {
            val status = when {
                ageYears >= 6 -> "Pneu ancien ($ageYears ans — remplacement conseillé)"
                ageYears == 5 -> "5 ans — vérifier les craquelures"
                else -> "Récent ($ageYears ans)"
            }
            "Code DOT $fourDigitCode (Semaine $weekOfYear / $manufactureYear) • $status"
        }
        AppLanguage.EN -> {
            val status = when {
                ageYears >= 6 -> "Old tyre ($ageYears yrs — replacement advised)"
                ageYears == 5 -> "5 years old — inspect sidewalls for cracking"
                else -> "Recent ($ageYears yrs old)"
            }
            "DOT $fourDigitCode (Week $weekOfYear of $manufactureYear) • $status"
        }
    }
}

typealias TyreDotOcrProposal = TyreDotCandidate

/**
 * Offline on-device OCR helper powered by bundled Google ML Kit Text Recognition.
 * Runs 100% locally on the phone from first launch with no internet or model download.
 * Downscales images before inference and closes the recognizer immediately to stay fast on 3 GB RAM phones.
 */
object OnDeviceOcrHelper {

    private const val CURRENT_YEAR = 2026
    private const val CURRENT_TWO_DIGIT_YEAR = 26

    /**
     * Recognizes text from a local image file and returns a proposal with ranked odometer mileage candidates (in km).
     */
    suspend fun recognizeOdometerFromFile(imageFile: File): OdometerOcrProposal? =
        withContext(Dispatchers.Default) {
            val rawText = recognizeRawTextFromFile(imageFile)
            if (rawText.isBlank()) return@withContext null
            val candidates = extractOdometerCandidates(rawText)
            val top = candidates.firstOrNull() ?: return@withContext null
            OdometerOcrProposal(
                proposedKm = top.mileageKm,
                allCandidatesKm = candidates.map { it.mileageKm },
                rawMatchedText = top.rawMatchedText
            )
        }

    /**
     * [Beta] Recognizes 4-digit tyre sidewall DOT date codes (WWYY) from a local image file.
     */
    suspend fun recognizeTyreDotFromFile(imageFile: File): TyreDotCandidate? =
        withContext(Dispatchers.Default) {
            val rawText = recognizeRawTextFromFile(imageFile)
            if (rawText.isBlank()) return@withContext null
            extractTyreDotCandidates(rawText).firstOrNull()
        }

    private suspend fun recognizeRawTextFromFile(imageFile: File): String = withContext(Dispatchers.IO) {
        if (!imageFile.exists()) return@withContext ""
        val bitmap = decodeDownscaledBitmap(imageFile, maxDimension = 1280) ?: return@withContext ""
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val visionText = recognizer.process(inputImage).await()
            visionText.text ?: ""
        } catch (_: Exception) {
            ""
        } finally {
            runCatching { recognizer.close() }
            runCatching { bitmap.recycle() }
        }
    }

    private fun decodeDownscaledBitmap(file: File, maxDimension: Int): Bitmap? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
            val width = boundsOptions.outWidth
            val height = boundsOptions.outHeight
            if (width <= 0 || height <= 0) return null

            var sampleSize = 1
            while ((width / sampleSize) > maxDimension || (height / sampleSize) > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Pure parser that extracts realistic odometer readings (1,000 to 999,999 km) from raw OCR text.
     * Handles grouped digits like "148 250 km", "084320", or "215.400".
     */
    fun extractOdometerCandidates(rawOcrText: String): List<OdometerOcrCandidate> {
        if (rawOcrText.isBlank()) return emptyList()
        val results = mutableListOf<OdometerOcrCandidate>()
        val lines = rawOcrText.lines()

        // Match sequences of 4 to 6 digits, optionally separated by a single space/dot/comma before the last 3 digits
        val groupedRegex = Regex("""(?<!\d)(\d{1,3}[\s.,]?\d{3}|\d{4,6})(?!\d)""")

        for (line in lines) {
            val cleanLine = line.trim()
            if (cleanLine.isEmpty()) continue
            val hasKmHint = cleanLine.contains("km", ignoreCase = true) ||
                cleanLine.contains("odo", ignoreCase = true) ||
                cleanLine.contains("total", ignoreCase = true)

            for (match in groupedRegex.findAll(cleanLine)) {
                val rawMatch = match.value.trim()
                // Ignore decimal trip readings like "124.5" (already excluded because 3 digits after dot are required, but guard against years 1990..2027 if no km hint)
                val digitsOnly = rawMatch.filter { it.isDigit() }
                val value = digitsOnly.toIntOrNull() ?: continue
                if (value !in 500..999999) continue
                // Filter out standalone 4-digit years (1990..2026) unless accompanied by "km"
                if (digitsOnly.length == 4 && value in 1990..2027 && !hasKmHint) continue

                results.add(
                    OdometerOcrCandidate(
                        mileageKm = value,
                        rawMatchedText = if (hasKmHint) "$rawMatch km" else rawMatch
                    )
                )
            }
        }

        return results
            .distinctBy { it.mileageKm }
            .sortedWith(
                compareByDescending<OdometerOcrCandidate> { it.rawMatchedText.contains("km", ignoreCase = true) }
                    .thenByDescending { it.mileageKm }
            )
            .take(4)
    }

    /**
     * [Beta] Pure parser that extracts valid 4-digit tyre DOT date codes (WWYY: week 01..53, year 00..26)
     * from raw OCR text or manual entry.
     */
    fun extractTyreDotCandidates(rawOcrText: String): List<TyreDotCandidate> {
        if (rawOcrText.isBlank()) return emptyList()
        val candidates = mutableListOf<Pair<TyreDotCandidate, Boolean>>()
        val normalized = rawOcrText.uppercase()
        val fourDigitRegex = Regex("""(?<!\d)(\d{4})(?!\d)""")

        for (match in fourDigitRegex.findAll(normalized)) {
            val code = match.value
            val parsed = parseManualTyreDotCode(code) ?: continue
            val prefixStart = (match.range.first - 25).coerceAtLeast(0)
            val contextWindow = normalized.substring(prefixStart, match.range.first)
            val nearDotKeyword = contextWindow.contains("DOT")
            candidates.add(parsed to nearDotKeyword)
        }

        return candidates
            .sortedByDescending { it.second }
            .map { it.first }
            .distinctBy { it.fourDigitCode }
            .take(4)
    }

    /**
     * Parses a 4-digit WWYY tyre date code (e.g., "2421" -> Week 24 of 2021).
     */
    fun parseManualTyreDotCode(fourDigits: String): TyreDotCandidate? {
        val clean = fourDigits.trim().filter { it.isDigit() }
        if (clean.length != 4) return null
        val week = clean.substring(0, 2).toIntOrNull() ?: return null
        val yy = clean.substring(2, 4).toIntOrNull() ?: return null
        if (week !in 1..53) return null
        if (yy !in 0..CURRENT_TWO_DIGIT_YEAR) return null

        val fullYear = 2000 + yy
        val age = (CURRENT_YEAR - fullYear).coerceAtLeast(0)
        return TyreDotCandidate(
            fourDigitCode = clean,
            weekOfYear = week,
            manufactureYear = fullYear,
            ageYears = age,
            isExpiredOrOld = age >= 5
        )
    }
}
