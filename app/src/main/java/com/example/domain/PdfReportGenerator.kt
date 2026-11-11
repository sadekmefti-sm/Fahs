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
        sellerAskingMillions: Double? = null
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
        val carHeaderLine = "${report.inspection.carBrand} ${report.inspection.carModel} (${report.inspection.carYear}) • ${report.inspection.fuelType} • ${report.inspection.gearboxType} • $dateStr"

        drawMultilineText(canvas, bannerTitle, whiteTitlePaint, MARGIN_H + 16f, yPos + 14f, CONTENT_WIDTH - 32, alignment)
        drawMultilineText(canvas, carHeaderLine, whiteSubPaint, MARGIN_H + 16f, yPos + 44f, CONTENT_WIDTH - 32, alignment)
        yPos += 90f

        // 2. Overall Score & Verdict Box
        val verdictBoxPaint = when (report.verdict) {
            "INCOMPLETE" -> boxBgPaint
            "GOOD" -> greenBoxPaint
            "NEGOTIATE" -> amberBoxPaint
            else -> redBoxPaint
        }
        val verdictText = if (report.overallScore != null) {
            "${report.overallScore}/100 — ${report.getVerdictTitle(language)}"
        } else {
            report.getVerdictTitle(language)
        }
        val summaryText = report.getVerdictSummary(language)
        val estimatedSummaryLines = (summaryText.length / 72) + 2
        val verdictBoxHeight = (46f + estimatedSummaryLines * 14f).coerceAtLeast(82f)

        canvas.drawRoundRect(
            RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + verdictBoxHeight),
            10f,
            10f,
            verdictBoxPaint
        )
        drawMultilineText(canvas, verdictText, sectionTitlePaint, MARGIN_H + 14f, yPos + 10f, CONTENT_WIDTH - 28, alignment)
        drawMultilineText(canvas, summaryText, bodyPaint, MARGIN_H + 14f, yPos + 32f, CONTENT_WIDTH - 28, alignment)
        yPos += verdictBoxHeight + 14f

        // 3. Optional Plate Label, ECU Chassis Number (if read) & Repair Cost Summary Box
        val ecuVinPart = if (report.inspection.vin.isNotBlank()) {
            "N° Châssis ECU (09 02): ${report.inspection.vin}   |   "
        } else ""
        val plateText = if (report.inspection.plateNumber.isNotBlank()) report.inspection.plateNumber else "—"

        val costRangeLine = when (language) {
            AppLanguage.AR -> "التكلفة التقديرية للإصلاحات: ${ReportEngine.formatDzd(report.minTotalRepairDzd)} إلى ${ReportEngine.formatDzd(report.maxTotalRepairDzd)} (${ReportEngine.formatMillionsCentimes(report.avgTotalRepairDzd, language)})"
            AppLanguage.FR -> "Frais de remise en état estimés : ${ReportEngine.formatDzd(report.minTotalRepairDzd)} à ${ReportEngine.formatDzd(report.maxTotalRepairDzd)} (~${ReportEngine.formatMillionsCentimes(report.avgTotalRepairDzd, language)})"
            AppLanguage.EN -> "Estimated Repair Range: ${ReportEngine.formatDzd(report.minTotalRepairDzd)} to ${ReportEngine.formatDzd(report.maxTotalRepairDzd)} (~${ReportEngine.formatMillionsCentimes(report.avgTotalRepairDzd, language)})"
        }

        val offerLine = if (sellerAskingMillions != null && sellerAskingMillions > 0) {
            val (minOffer, maxOffer) = ReportEngine.calculateFairOfferRangeDzd(
                sellerAskingMillions,
                report.minTotalRepairDzd,
                report.maxTotalRepairDzd
            )
            when (language) {
                AppLanguage.AR -> "السعر المطلوب: ${sellerAskingMillions.toInt()} مليون • العرض المقترح بعد خصم الإصلاح: ${ReportEngine.formatMillionsCentimes(minOffer, language)} إلى ${ReportEngine.formatMillionsCentimes(maxOffer, language)}"
                AppLanguage.FR -> "Prix demandé : ${sellerAskingMillions.toInt()} Millions • Offre suggérée après déduction : ${ReportEngine.formatMillionsCentimes(minOffer, language)} à ${ReportEngine.formatMillionsCentimes(maxOffer, language)}"
                AppLanguage.EN -> "Asking Price: ${sellerAskingMillions.toInt()}M • Suggested Offer After Repairs: ${ReportEngine.formatMillionsCentimes(minOffer, language)} to ${ReportEngine.formatMillionsCentimes(maxOffer, language)}"
            }
        } else ""

        canvas.drawRoundRect(
            RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + 74f),
            10f,
            10f,
            boxBgPaint
        )
        drawMultilineText(
            canvas,
            "${ecuVinPart}Repère / Immatriculation: $plateText",
            bodyBoldPaint,
            MARGIN_H + 12f,
            yPos + 10f,
            CONTENT_WIDTH - 24,
            alignment
        )
        drawMultilineText(canvas, costRangeLine, redTextPaint, MARGIN_H + 12f, yPos + 28f, CONTENT_WIDTH - 24, alignment)
        if (offerLine.isNotBlank()) {
            drawMultilineText(canvas, offerLine, bodyPaint, MARGIN_H + 12f, yPos + 46f, CONTENT_WIDTH - 24, alignment)
        }
        yPos += 88f

        // 4. Critical & Negotiable Problems Section
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

        // 5. Unchecked Items Section (Honest disclosure including all unchecked critical items)
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

        // 6. Passed Items Summary
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
        sellerAskingMillions: Double? = null
    ): File {
        val pdfFile = generatePdfFile(context, report, language, sellerAskingMillions)
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
            AppLanguage.AR -> "تطبيق فحص (Fahs) — الأسعار التقديرية بالدينار الجزائري استرشادية وتختلف حسب الولاية ونوع قطع الغيار. الصفحة $pageNum"
            AppLanguage.FR -> "Généré hors-ligne par Fahs (فحص) — Les coûts en DZD sont indicatifs selon la wilaya et le type de pièce. Page $pageNum"
            AppLanguage.EN -> "Generated offline by Fahs (فحص) — DZD repair estimates are indicative and vary by wilaya. Page $pageNum"
        }
        canvas.drawText(disclaimer, MARGIN_H, PAGE_HEIGHT - 18f, footerPaint)
    }
}
