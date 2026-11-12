package com.example.domain

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.core.i18n.AppLanguage
import com.example.core.photo.InspectionPhotoHelper
import com.example.data.model.ItemSeverity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_H = 36f
    private const val CONTENT_WIDTH = (PAGE_WIDTH - 72)

    /**
     * Generates a complete offline PDF report for the given [InspectionReportData] in [language]
     * and returns the generated [File].
     */
    fun generatePdfFile(
        context: Context,
        report: InspectionReportData,
        language: AppLanguage,
        sellerAskingMillions: Double? = null,
        mechanicQuotedRepairDzd: Int? = null
    ): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val safeBrand = report.inspection.carBrand.replace(Regex("[^a-zA-Z0-9]"), "_")
        val safeModel = report.inspection.carModel.replace(Regex("[^a-zA-Z0-9]"), "_")
        val fileName = "Fahs_${safeBrand}_${safeModel}_${report.inspection.id}_${language.code}.pdf"
        val outFile = File(reportsDir, fileName)

        val pdfDocument = PdfDocument()
        val isRtl = language.isRtl
        val alignment = if (isRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL

        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(6, 78, 59) // Emerald 900
            style = Paint.Style.FILL
        }
        val boxBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(241, 245, 249) // Slate 100
            style = Paint.Style.FILL
        }
        val redBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(254, 226, 226)
            style = Paint.Style.FILL
        }
        val amberBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(254, 243, 199)
            style = Paint.Style.FILL
        }
        val greenBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(209, 250, 229)
            style = Paint.Style.FILL
        }

        val whiteTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val whiteSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(167, 243, 208)
            textSize = 11f
        }
        val sectionTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyBoldPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(51, 65, 85)
            textSize = 9.5f
        }
        val redTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(185, 28, 28)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var pageNumber = 1
        var currentPage = pdfDocument.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        )
        var canvas = currentPage.canvas
        var yPos = 32f

        fun startNewPageIfNeeded(requiredHeight: Float) {
            if (yPos + requiredHeight > PAGE_HEIGHT - 44f) {
                drawFooter(canvas, pageNumber, language)
                pdfDocument.finishPage(currentPage)
                pageNumber++
                currentPage = pdfDocument.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                )
                canvas = currentPage.canvas
                yPos = 36f
            }
        }

        // 1. Top Banner
        canvas.drawRoundRect(
            RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + 76f),
            12f,
            12f,
            headerBgPaint
        )

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
            .format(Date(report.inspection.updatedAt))
        val bannerTitle = when (language) {
            AppLanguage.AR -> "تقرير فحص السيارة قبل الشراء — فحص (Fahs)"
            AppLanguage.FR -> "RAPPORT D'INSPECTION AUTOMOBILE — FAHS (فحص)"
            AppLanguage.EN -> "PRE-PURCHASE VEHICLE INSPECTION REPORT — FAHS (فحص)"
        }
        val odoPart = if (report.inspection.odometerKm > 0) " • ${report.inspection.odometerKm} km" else ""
        val carHeaderLine = "${report.inspection.carBrand} ${report.inspection.carModel} (${report.inspection.carYear})$odoPart • ${report.inspection.fuelType} • ${report.inspection.gearboxType} • $dateStr"

        drawMultilineText(canvas, bannerTitle, whiteTitlePaint, MARGIN_H + 16f, yPos + 14f, CONTENT_WIDTH - 32, alignment)
        drawMultilineText(canvas, carHeaderLine, whiteSubPaint, MARGIN_H + 16f, yPos + 44f, CONTENT_WIDTH - 32, alignment)
        yPos += 90f

        // 2. Overall Score & Verdict Box (including both Raw Score and Capped Score + 1-line explanation when capped)
        val verdictBoxPaint = when (report.verdict) {
            "INCOMPLETE" -> boxBgPaint
            "GOOD" -> greenBoxPaint
            "NEGOTIATE" -> amberBoxPaint
            else -> redBoxPaint
        }
        val scoreHeaderPrefix = if (report.overallScore != null) {
            if (report.isCriticalCapTriggered && report.rawUncappedScore != null) {
                val rawLabel = when (language) {
                    AppLanguage.AR -> "النتيجة المقيّدة: ${report.overallScore}/100 (النتيجة الخام: ${report.rawUncappedScore}/100)"
                    AppLanguage.FR -> "Score plafonné : ${report.overallScore}/100 (Score brut : ${report.rawUncappedScore}/100)"
                    AppLanguage.EN -> "Capped Score: ${report.overallScore}/100 (Raw Score: ${report.rawUncappedScore}/100)"
                }
                "$rawLabel — ${report.getVerdictTitle(language)}"
            } else {
                "${report.overallScore}/100 — ${report.getVerdictTitle(language)}"
            }
        } else {
            report.getVerdictTitle(language)
        }
        val capOneLine = report.getCriticalCapOneLineExplanation(language)
        val summaryText = report.getVerdictSummary(language)
        val combinedSummary = if (capOneLine != null && !summaryText.contains(capOneLine)) {
            "$capOneLine\n$summaryText"
        } else {
            summaryText
        }
        val estimatedSummaryLines = (combinedSummary.length / 68) + 3
        val verdictBoxHeight = (48f + estimatedSummaryLines * 14f).coerceAtLeast(86f)

        canvas.drawRoundRect(
            RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + verdictBoxHeight),
            10f,
            10f,
            verdictBoxPaint
        )
        drawMultilineText(canvas, scoreHeaderPrefix, sectionTitlePaint, MARGIN_H + 14f, yPos + 10f, CONTENT_WIDTH - 28, alignment)
        drawMultilineText(canvas, combinedSummary, bodyPaint, MARGIN_H + 14f, yPos + 32f, CONTENT_WIDTH - 28, alignment)
        yPos += verdictBoxHeight + 12f

        // 3. Optional Plate Label & User-Entered Mechanic Quote / Fair Offer Box
        val plateText = if (report.inspection.plateNumber.isNotBlank()) report.inspection.plateNumber else "—"
        val effectiveAskingMillions = sellerAskingMillions?.takeIf { it > 0.0 }
            ?: report.inspection.sellerAskingPriceMillions.takeIf { it > 0.0 }
        val effectiveMechanicQuoteDzd = mechanicQuotedRepairDzd?.takeIf { it > 0 }
            ?: report.inspection.userQuotedRepairDzd.takeIf { it > 0 }

        val quoteLine = if (effectiveMechanicQuoteDzd != null) {
            when (language) {
                AppLanguage.AR -> "تكلفة الإصلاح المقدرة من الميكانيكي (مدخلة من المستخدم): ${ReportEngine.formatAlgerianCurrency(effectiveMechanicQuoteDzd, language)}"
                AppLanguage.FR -> "Devis réparations mécanicien (saisi par l'acheteur) : ${ReportEngine.formatAlgerianCurrency(effectiveMechanicQuoteDzd, language)}"
                AppLanguage.EN -> "Mechanic's Quoted Repair Cost (user-entered): ${ReportEngine.formatAlgerianCurrency(effectiveMechanicQuoteDzd, language)}"
            }
        } else {
            when (language) {
                AppLanguage.AR -> "تكلفة الإصلاح: لم يتم إدخال تسعيرة ميكانيكي بعد"
                AppLanguage.FR -> "Coût des réparations : aucun devis mécanicien saisi"
                AppLanguage.EN -> "Repair cost: no mechanic quote entered yet"
            }
        }

        val offerLine = if (effectiveAskingMillions != null && effectiveAskingMillions > 0) {
            val quoteToSubtract = effectiveMechanicQuoteDzd ?: 0
            val targetOfferDzd = ReportEngine.calculateFairOfferFromMechanicQuoteDzd(
                askingPriceMillions = effectiveAskingMillions,
                mechanicQuotedRepairDzd = quoteToSubtract
            )
            when (language) {
                AppLanguage.AR -> "السعر المطلوب: ${effectiveAskingMillions.toInt()} مليون • السعر المقترح بعد خصم تسعيرة الميكانيكي: ${ReportEngine.formatMillionsCentimes(targetOfferDzd, language)} (${ReportEngine.formatDzd(targetOfferDzd)})"
                AppLanguage.FR -> "Prix demandé : ${effectiveAskingMillions.toInt()} Millions • Offre cible après déduction du devis : ${ReportEngine.formatMillionsCentimes(targetOfferDzd, language)} (${ReportEngine.formatDzd(targetOfferDzd)})"
                AppLanguage.EN -> "Asking Price: ${effectiveAskingMillions.toInt()}M • Target Offer After Mechanic Quote: ${ReportEngine.formatMillionsCentimes(targetOfferDzd, language)} (${ReportEngine.formatDzd(targetOfferDzd)})"
            }
        } else ""

        val box3Height = if (offerLine.isNotBlank()) 68f else 48f
        canvas.drawRoundRect(
            RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + box3Height),
            10f,
            10f,
            boxBgPaint
        )
        drawMultilineText(
            canvas,
            "Repère / Immatriculation: $plateText",
            bodyBoldPaint,
            MARGIN_H + 12f,
            yPos + 8f,
            CONTENT_WIDTH - 24,
            alignment
        )
        drawMultilineText(canvas, quoteLine, bodyPaint, MARGIN_H + 12f, yPos + 24f, CONTENT_WIDTH - 24, alignment)
        if (offerLine.isNotBlank()) {
            drawMultilineText(canvas, offerLine, bodyBoldPaint, MARGIN_H + 12f, yPos + 42f, CONTENT_WIDTH - 24, alignment)
        }
        yPos += box3Height + 12f

        // 4. Dedicated OBD2 Diagnostic Block in the PDF
        startNewPageIfNeeded(125f)
        val obdBlock = report.obdBlock
        val obdBoxPaint = if (!obdBlock.isScanned) {
            boxBgPaint
        } else if (obdBlock.hasCriticalAlert) {
            redBoxPaint
        } else {
            greenBoxPaint
        }
        val obdHeader = when (language) {
            AppLanguage.AR -> "نتائج الفحص الإلكتروني للكمبيوتر (OBD2)"
            AppLanguage.FR -> "Bloc Diagnostic Électronique OBD2 (Calculateur)"
            AppLanguage.EN -> "OBD2 Electronic Diagnostic Findings"
        }
        val obdLines = listOf(
            when (language) {
                AppLanguage.AR -> "• حالة الاتصال: ${obdBlock.getConnectionStatusText(language)}"
                AppLanguage.FR -> "• État de connexion : ${obdBlock.getConnectionStatusText(language)}"
                AppLanguage.EN -> "• Connection status: ${obdBlock.getConnectionStatusText(language)}"
            },
            when (language) {
                AppLanguage.AR -> "• أكواد الأعطال: ${obdBlock.getFaultCodesText(language)}"
                AppLanguage.FR -> "• Codes défauts : ${obdBlock.getFaultCodesText(language)}"
                AppLanguage.EN -> "• Fault codes found: ${obdBlock.getFaultCodesText(language)}"
            },
            when (language) {
                AppLanguage.AR -> "• حالة مسح الذاكرة: ${obdBlock.getClearedCodesText(language)}"
                AppLanguage.FR -> "• Effacement récent : ${obdBlock.getClearedCodesText(language)}"
                AppLanguage.EN -> "• Cleared-codes status: ${obdBlock.getClearedCodesText(language)}"
            },
            when (language) {
                AppLanguage.AR -> "• فحص لمبة المحرك (Check Engine): ${obdBlock.getCheckEngineLightText(language)}"
                AppLanguage.FR -> "• Voyant Check-Engine : ${obdBlock.getCheckEngineLightText(language)}"
                AppLanguage.EN -> "• Check-Engine light check: ${obdBlock.getCheckEngineLightText(language)}"
            },
            when (language) {
                AppLanguage.AR -> "• مقارنة رقم الشاسيه الإلكتروني (09 02): ${obdBlock.getChassisComparisonText(language)}"
                AppLanguage.FR -> "• Comparaison N° de châssis ECU (09 02) : ${obdBlock.getChassisComparisonText(language)}"
                AppLanguage.EN -> "• Chassis-number comparison (09 02): ${obdBlock.getChassisComparisonText(language)}"
            }
        )
        val obdContentText = obdLines.joinToString("\n")
        val obdBoxHeight = 114f
        canvas.drawRoundRect(
            RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + obdBoxHeight),
            10f,
            10f,
            obdBoxPaint
        )
        drawMultilineText(canvas, obdHeader, sectionTitlePaint, MARGIN_H + 12f, yPos + 8f, CONTENT_WIDTH - 24, alignment)
        drawMultilineText(canvas, obdContentText, bodyPaint, MARGIN_H + 12f, yPos + 28f, CONTENT_WIDTH - 24, alignment)
        yPos += obdBoxHeight + 14f

        // 5. Critical & Negotiable Problems Section
        val allProblems = report.criticalProblems + report.negotiableProblems
        val problemsHeader = when (language) {
            AppLanguage.AR -> "1. العيوب والملاحظات المرصودة (${allProblems.size})"
            AppLanguage.FR -> "1. Anomalies et points à négocier (${allProblems.size})"
            AppLanguage.EN -> "1. Recorded Issues & Negotiation Points (${allProblems.size})"
        }
        startNewPageIfNeeded(30f)
        drawMultilineText(canvas, problemsHeader, sectionTitlePaint, MARGIN_H, yPos, CONTENT_WIDTH, alignment)
        yPos += 22f

        if (allProblems.isEmpty()) {
            val noProblemMsg = when (language) {
                AppLanguage.AR -> "لا توجد عيوب مسجلة في النقاط المفحوصة ✓"
                AppLanguage.FR -> "Aucune anomalie signalée sur les points contrôlés ✓"
                AppLanguage.EN -> "No defects recorded on inspected items ✓"
            }
            drawMultilineText(canvas, noProblemMsg, bodyPaint, MARGIN_H + 8f, yPos, CONTENT_WIDTH - 16, alignment)
            yPos += 24f
        } else {
            for (item in allProblems) {
                val photoBitmap = InspectionPhotoHelper.loadThumbnailBitmap(item.photoUri, maxDimensionPx = 240)
                val neededHeight = if (photoBitmap != null) 136f else 58f
                startNewPageIfNeeded(neededHeight)
                val sevTag = item.severity.getLabel(language)
                val costSuffix = if (item.maxCostDzd > 0) {
                    " (${ReportEngine.formatDzd(item.minCostDzd)} - ${ReportEngine.formatDzd(item.maxCostDzd)})"
                } else ""
                val line1 = "• [$sevTag] ${item.getQuestion(language)}$costSuffix"
                val line2 = "   ${item.getExplanation(language)}" +
                    if (item.buyerNote.isNotBlank()) " | Note: ${item.buyerNote}" else ""

                val h1 = drawMultilineText(canvas, line1, bodyBoldPaint, MARGIN_H + 6f, yPos, CONTENT_WIDTH - 12, alignment)
                yPos += h1 + 2f
                val h2 = drawMultilineText(canvas, line2, bodyPaint, MARGIN_H + 12f, yPos, CONTENT_WIDTH - 24, alignment)
                yPos += h2 + 4f

                if (photoBitmap != null) {
                    val targetHeight = 68f
                    val aspect = photoBitmap.width.toFloat().coerceAtLeast(1f) / photoBitmap.height.toFloat().coerceAtLeast(1f)
                    val targetWidth = (targetHeight * aspect).coerceIn(54f, 120f)
                    val leftX = if (isRtl) (PAGE_WIDTH - MARGIN_H - 12f - targetWidth) else (MARGIN_H + 16f)
                    val destRect = RectF(leftX, yPos, leftX + targetWidth, yPos + targetHeight)
                    canvas.drawBitmap(photoBitmap, null, destRect, null)
                    yPos += targetHeight + 6f
                } else {
                    yPos += 4f
                }
            }
        }

        yPos += 8f

        // 6. Unchecked Items Section (Honest disclosure including all unchecked critical items)
        startNewPageIfNeeded(34f)
        val uncheckedHeader = when (language) {
            AppLanguage.AR -> "2. نقاط لم يتم فحصها (${report.uncheckedItems.size} من أصل ${report.totalApplicableCount})"
            AppLanguage.FR -> "2. Points non vérifiés lors de la visite (${report.uncheckedItems.size} sur ${report.totalApplicableCount})"
            AppLanguage.EN -> "2. Items Not Checked During Visit (${report.uncheckedItems.size} of ${report.totalApplicableCount})"
        }
        drawMultilineText(canvas, uncheckedHeader, sectionTitlePaint, MARGIN_H, yPos, CONTENT_WIDTH, alignment)
        yPos += 20f

        if (report.uncheckedItems.isEmpty()) {
            val allCheckedMsg = when (language) {
                AppLanguage.AR -> "تم فحص جميع النقاط المبرمجة للسيارة بنسبة 100% ✓"
                AppLanguage.FR -> "100% des points prévus ont été vérifiés ✓"
                AppLanguage.EN -> "100% of applicable checklist items were verified ✓"
            }
            drawMultilineText(canvas, allCheckedMsg, bodyPaint, MARGIN_H + 8f, yPos, CONTENT_WIDTH - 16, alignment)
            yPos += 22f
        } else {
            // Always list all unchecked CRITICAL items first, followed by other unchecked items
            val orderedUnchecked = report.uncheckedCriticalItems +
                report.uncheckedItems.filter { it.severity != ItemSeverity.CRITICAL }
            for (item in orderedUnchecked) {
                startNewPageIfNeeded(24f)
                val prefix = if (item.severity == ItemSeverity.CRITICAL) "⚠️ [CRITIQUE]" else "○"
                val paintToUse = if (item.severity == ItemSeverity.CRITICAL) redTextPaint else bodyPaint
                val h = drawMultilineText(
                    canvas,
                    "$prefix ${item.getSectionTitle(language)} : ${item.getQuestion(language)}",
                    paintToUse,
                    MARGIN_H + 8f,
                    yPos,
                    CONTENT_WIDTH - 16,
                    alignment
                )
                yPos += h + 4f
            }
        }

        yPos += 8f

        // 7. Passed Items Summary
        startNewPageIfNeeded(34f)
        val passedHeader = when (language) {
            AppLanguage.AR -> "3. النقاط السليمة (${report.passedItems.size})"
            AppLanguage.FR -> "3. Points vérifiés et conformes (${report.passedItems.size})"
            AppLanguage.EN -> "3. Passed Items (${report.passedItems.size})"
        }
        drawMultilineText(canvas, passedHeader, sectionTitlePaint, MARGIN_H, yPos, CONTENT_WIDTH, alignment)
        yPos += 20f

        for (item in report.passedItems.take(15)) {
            startNewPageIfNeeded(22f)
            val h = drawMultilineText(
                canvas,
                "✓ ${item.getQuestion(language)}",
                bodyPaint,
                MARGIN_H + 8f,
                yPos,
                CONTENT_WIDTH - 16,
                alignment
            )
            yPos += h + 4f
        }

        drawFooter(canvas, pageNumber, language)
        pdfDocument.finishPage(currentPage)

        FileOutputStream(outFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return outFile
    }

    /**
     * Launches the Android system share sheet (WhatsApp, Gmail, Drive, Telegram, etc.)
     * for the generated PDF report using zero-permission FileProvider.
     */
    fun sharePdfReport(
        context: Context,
        report: InspectionReportData,
        language: AppLanguage,
        sellerAskingMillions: Double? = null,
        mechanicQuotedRepairDzd: Int? = null
    ): File {
        val pdfFile = generatePdfFile(
            context = context,
            report = report,
            language = language,
            sellerAskingMillions = sellerAskingMillions,
            mechanicQuotedRepairDzd = mechanicQuotedRepairDzd
        )
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val shareTitle = when (language) {
            AppLanguage.AR -> "مشاركة تقرير فحص السيارة PDF"
            AppLanguage.FR -> "Partager le rapport PDF (WhatsApp / Fichiers)"
            AppLanguage.EN -> "Share Vehicle Inspection PDF Report"
        }

        val scoreLabel = report.overallScore?.let { "$it/100" } ?: when (language) {
            AppLanguage.AR -> "غير مكتمل"
            AppLanguage.FR -> "Incomplet"
            AppLanguage.EN -> "Incomplete"
        }
        val shareText = when (language) {
            AppLanguage.AR -> "تقرير فحص سيارة ${report.inspection.carBrand} ${report.inspection.carModel} (${report.inspection.carYear}) — النتيجة: $scoreLabel (${report.getVerdictTitle(language)})"
            AppLanguage.FR -> "Rapport d'inspection Fahs : ${report.inspection.carBrand} ${report.inspection.carModel} (${report.inspection.carYear}) — Score : $scoreLabel (${report.getVerdictTitle(language)})"
            AppLanguage.EN -> "Fahs Inspection Report: ${report.inspection.carBrand} ${report.inspection.carModel} (${report.inspection.carYear}) — Score: $scoreLabel (${report.getVerdictTitle(language)})"
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, shareText)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, shareTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (_: Exception) {
        }
        return pdfFile
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        paint: TextPaint,
        x: Float,
        y: Float,
        width: Int,
        alignment: Layout.Alignment
    ): Float {
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width.coerceAtLeast(100))
            .setAlignment(alignment)
            .setLineSpacing(2f, 1.05f)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    private fun drawFooter(canvas: Canvas, pageNum: Int, language: AppLanguage) {
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8.5f
        }
        val disclaimer = when (language) {
            AppLanguage.AR -> "تطبيق فحص (Fahs) — أداة مساعدة للمشتري ولا تغني عن الفحص الميكانيكي على رافعة في الورشة. الصفحة $pageNum"
            AppLanguage.FR -> "Généré hors-ligne par Fahs (فحص) — Aide à l'achat, ne remplace pas un contrôle sur pont en atelier. Page $pageNum"
            AppLanguage.EN -> "Generated offline by Fahs (فحص) — Buying aid; does not replace a workshop inspection on a lift. Page $pageNum"
        }
        canvas.drawText(disclaimer, MARGIN_H, PAGE_HEIGHT - 18f, footerPaint)
    }
}
