package com.example.domain

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.core.photo.InspectionPhotoHelper
import com.example.data.local.InspectionEntity
import com.example.data.model.ItemSeverity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    private const val MARGIN_H = 32f
    private const val MARGIN_TOP = 30f
    private const val FOOTER_RESERVED_HEIGHT = 44f
    private const val MAX_CONTENT_Y = PAGE_HEIGHT - FOOTER_RESERVED_HEIGHT
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_H * 2f)

    // Brand & Semantic Palette
    private val COLOR_EMERALD_900 = Color.rgb(6, 78, 59)
    private val COLOR_EMERALD_800 = Color.rgb(6, 95, 70)
    private val COLOR_EMERALD_700 = Color.rgb(4, 120, 87)
    private val COLOR_EMERALD_600 = Color.rgb(5, 150, 105)
    private val COLOR_EMERALD_100 = Color.rgb(209, 250, 229)
    private val COLOR_EMERALD_50 = Color.rgb(236, 253, 245)

    private val COLOR_SLATE_900 = Color.rgb(15, 23, 42)
    private val COLOR_SLATE_800 = Color.rgb(30, 41, 59)
    private val COLOR_SLATE_700 = Color.rgb(51, 65, 85)
    private val COLOR_SLATE_600 = Color.rgb(71, 85, 105)
    private val COLOR_SLATE_500 = Color.rgb(100, 116, 139)
    private val COLOR_SLATE_300 = Color.rgb(203, 213, 225)
    private val COLOR_SLATE_200 = Color.rgb(226, 232, 240)
    private val COLOR_SLATE_100 = Color.rgb(241, 245, 249)
    private val COLOR_SLATE_50 = Color.rgb(248, 250, 252)

    private val COLOR_GREEN_BAND_BG = Color.rgb(209, 250, 229)
    private val COLOR_GREEN_BAND_BORDER = Color.rgb(5, 150, 105)
    private val COLOR_GREEN_BAND_TEXT = Color.rgb(6, 95, 70)

    private val COLOR_ORANGE_BAND_BG = Color.rgb(254, 243, 199)
    private val COLOR_ORANGE_BAND_BORDER = Color.rgb(217, 119, 6)
    private val COLOR_ORANGE_BAND_TEXT = Color.rgb(146, 64, 14)

    private val COLOR_RED_BAND_BG = Color.rgb(254, 226, 226)
    private val COLOR_RED_BAND_BORDER = Color.rgb(220, 38, 38)
    private val COLOR_RED_BAND_TEXT = Color.rgb(153, 27, 27)

    /**
     * Builds the clear PDF file name required by Phase 8:
     * Example: `Fahs_Peugeot_208_2018_2026-11-11.pdf`
     */
    fun buildSuggestedFileName(inspection: InspectionEntity): String {
        val cleanBrand = inspection.carBrand
            .trim()
            .replace(Regex("""[^\p{L}\p{N}]+"""), "_")
            .trim('_')
            .ifBlank { "Car" }
        val cleanModel = inspection.carModel
            .trim()
            .replace(Regex("""[^\p{L}\p{N}]+"""), "_")
            .trim('_')
            .ifBlank { "Model" }
        val dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .format(Date(inspection.updatedAt))
        return "Fahs_${cleanBrand}_${cleanModel}_${inspection.carYear}_${dateIso}.pdf"
    }

    /**
     * Builds the deterministic report number shown in the header (e.g. `FAHS-2026-0042`).
     */
    fun buildReportNumber(inspection: InspectionEntity): String {
        val yearStr = SimpleDateFormat("yyyy", Locale.US).format(Date(inspection.updatedAt))
        val seq = (if (inspection.id > 0) inspection.id else 1L).toString().padStart(4, '0')
        return "FAHS-$yearStr-$seq"
    }

    /**
     * Loads the bundled brand font (`R.font.cairo` for Arabic, `R.font.plus_jakarta_sans` for French/English),
     * with a direct file fallback so both Android devices and JVM Robolectric tests use the real TTF fonts.
     */
    fun loadBrandTypeface(context: Context, language: AppLanguage, bold: Boolean): Typeface {
        val style = if (bold) Typeface.BOLD else Typeface.NORMAL
        val fontRes = if (language == AppLanguage.AR) R.font.cairo else R.font.plus_jakarta_sans
        val fontFileName = if (language == AppLanguage.AR) "cairo.ttf" else "plus_jakarta_sans.ttf"

        val baseTypeface: Typeface? = runCatching {
            ResourcesCompat.getFont(context, fontRes)
        }.getOrNull() ?: runCatching {
            val candidatePaths = listOf(
                File("app/src/main/res/font/$fontFileName"),
                File("src/main/res/font/$fontFileName")
            )
            val found = candidatePaths.firstOrNull { it.exists() }
            if (found != null) Typeface.createFromFile(found) else null
        }.getOrNull()

        return if (baseTypeface != null) {
            Typeface.create(baseTypeface, style)
        } else {
            Typeface.create(Typeface.DEFAULT, style)
        }
    }

    /**
     * Generates a complete professional A4 PDF report for [report] in [language] and returns the [File].
     */
    fun generatePdfFile(
        context: Context,
        report: InspectionReportData,
        language: AppLanguage,
        sellerAskingMillions: Double? = null,
        mechanicQuotedRepairDzd: Int? = null,
        customFileName: String? = null
    ): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val fileName = customFileName ?: buildSuggestedFileName(report.inspection)
        val outFile = File(reportsDir, fileName)

        // Pass 1: Dry-run layout to compute the exact total page count ("page X of Y")
        val dummyBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val dummyCanvas = Canvas(dummyBitmap)
        val totalPages = renderDocumentLayout(
            context = context,
            report = report,
            language = language,
            sellerAskingMillions = sellerAskingMillions,
            mechanicQuotedRepairDzd = mechanicQuotedRepairDzd,
            totalPagesKnown = 1,
            dryRun = true,
            startPageCanvas = { dummyCanvas },
            finishPageCanvas = {}
        )
        dummyBitmap.recycle()

        // Pass 2: Actual vector PDF rendering with exact "page X of Y" on every page
        val pdfDocument = PdfDocument()
        val nativePdfSuccess = try {
            var activePdfPage: PdfDocument.Page? = null

            renderDocumentLayout(
                context = context,
                report = report,
                language = language,
                sellerAskingMillions = sellerAskingMillions,
                mechanicQuotedRepairDzd = mechanicQuotedRepairDzd,
                totalPagesKnown = totalPages,
                dryRun = false,
                startPageCanvas = { pageNumber ->
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    val page = pdfDocument.startPage(pageInfo)
                    activePdfPage = page
                    page.canvas
                },
                finishPageCanvas = {
                    activePdfPage?.let { pdfDocument.finishPage(it) }
                    activePdfPage = null
                }
            )

            FileOutputStream(outFile).use { out ->
                pdfDocument.writeTo(out)
            }
            outFile.exists() && outFile.length() > 100L
        } catch (_: Throwable) {
            false
        } finally {
            runCatching { pdfDocument.close() }
        }

        if (!nativePdfSuccess) {
            val bitmaps = renderReportPagesToBitmaps(
                context = context,
                report = report,
                language = language,
                sellerAskingMillions = sellerAskingMillions,
                mechanicQuotedRepairDzd = mechanicQuotedRepairDzd,
                scaleFactor = 1.5f
            )
            writeBitmapsAsPdf14(bitmaps, outFile)
            bitmaps.forEach { it.recycle() }
        }
        return outFile
    }

    /**
     * Writes rendered A4 page bitmaps into a standard, self-contained multi-page PDF 1.4 document
     * when running in JVM test environments where `android.graphics.pdf.PdfDocument`'s native handle is 0.
     */
    private fun writeBitmapsAsPdf14(pages: List<Bitmap>, outFile: File) {
        if (pages.isEmpty()) return
        val jpegStreams = pages.map { bmp ->
            val baos = java.io.ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 75, baos)
            baos.toByteArray()
        }
        FileOutputStream(outFile).use { fos ->
            val offsets = mutableMapOf<Int, Int>()
            var pos = 0
            fun writeAscii(str: String) {
                val bytes = str.toByteArray(Charsets.US_ASCII)
                fos.write(bytes)
                pos += bytes.size
            }
            fun writeRaw(bytes: ByteArray) {
                fos.write(bytes)
                pos += bytes.size
            }

            // Object numbering:
            // 1 = Catalog, 2 = Pages, then for each page i (0-based):
            // pageObj = 3 + i*3, contentObj = 4 + i*3, imageObj = 5 + i*3
            val totalObjects = 2 + pages.size * 3
            writeAscii("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n")

            offsets[1] = pos
            writeAscii("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")

            val kidsRefs = pages.indices.joinToString(" ") { i -> "${3 + i * 3} 0 R" }
            offsets[2] = pos
            writeAscii("2 0 obj\n<< /Type /Pages /Kids [ $kidsRefs ] /Count ${pages.size} >>\nendobj\n")

            for (i in pages.indices) {
                val bmp = pages[i]
                val jpegBytes = jpegStreams[i]
                val pageObj = 3 + i * 3
                val contentObj = 4 + i * 3
                val imageObj = 5 + i * 3
                val imgName = "Im$i"

                offsets[pageObj] = pos
                writeAscii(
                    "$pageObj 0 obj\n" +
                        "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $PAGE_WIDTH $PAGE_HEIGHT] " +
                        "/Resources << /XObject << /$imgName $imageObj 0 R >> >> " +
                        "/Contents $contentObj 0 R >>\nendobj\n"
                )

                val contentStream = "q\n$PAGE_WIDTH 0 0 $PAGE_HEIGHT 0 0 cm\n/$imgName Do\nQ\n"
                val contentBytes = contentStream.toByteArray(Charsets.US_ASCII)
                offsets[contentObj] = pos
                writeAscii("$contentObj 0 obj\n<< /Length ${contentBytes.size} >>\nstream\n")
                writeRaw(contentBytes)
                writeAscii("\nendstream\nendobj\n")

                offsets[imageObj] = pos
                writeAscii(
                    "$imageObj 0 obj\n" +
                        "<< /Type /XObject /Subtype /Image /Width ${bmp.width} /Height ${bmp.height} " +
                        "/ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length ${jpegBytes.size} >>\nstream\n"
                )
                writeRaw(jpegBytes)
                writeAscii("\nendstream\nendobj\n")
            }

            val xrefPos = pos
            writeAscii("xref\n0 ${totalObjects + 1}\n0000000000 65535 f \n")
            for (objId in 1..totalObjects) {
                val off = offsets[objId] ?: 0
                writeAscii(String.format(Locale.US, "%010d 00000 n \n", off))
            }
            writeAscii(
                "trailer\n<< /Size ${totalObjects + 1} /Root 1 0 R >>\nstartxref\n$xrefPos\n%%EOF\n"
            )
        }
    }

    /**
     * Renders every page of the report to high-resolution [Bitmap]s (e.g. scale=2f -> 1190x1684 px)
     * using the exact same layout and drawing code as [generatePdfFile].
     */
    fun renderReportPagesToBitmaps(
        context: Context,
        report: InspectionReportData,
        language: AppLanguage,
        sellerAskingMillions: Double? = null,
        mechanicQuotedRepairDzd: Int? = null,
        scaleFactor: Float = 2f
    ): List<Bitmap> {
        val dummyBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val dummyCanvas = Canvas(dummyBitmap)
        val totalPages = renderDocumentLayout(
            context = context,
            report = report,
            language = reportLanguageOrDefault(language),
            sellerAskingMillions = sellerAskingMillions,
            mechanicQuotedRepairDzd = mechanicQuotedRepairDzd,
            totalPagesKnown = 1,
            dryRun = true,
            startPageCanvas = { dummyCanvas },
            finishPageCanvas = {}
        )
        dummyBitmap.recycle()

        val pages = mutableListOf<Bitmap>()
        renderDocumentLayout(
            context = context,
            report = report,
            language = language,
            sellerAskingMillions = sellerAskingMillions,
            mechanicQuotedRepairDzd = mechanicQuotedRepairDzd,
            totalPagesKnown = totalPages,
            dryRun = false,
            startPageCanvas = {
                val bmp = Bitmap.createBitmap(
                    (PAGE_WIDTH * scaleFactor).toInt(),
                    (PAGE_HEIGHT * scaleFactor).toInt(),
                    Bitmap.Config.ARGB_8888
                )
                pages.add(bmp)
                Canvas(bmp).apply {
                    drawColor(Color.WHITE)
                    scale(scaleFactor, scaleFactor)
                }
            },
            finishPageCanvas = {}
        )
        return pages
    }

    private fun reportLanguageOrDefault(language: AppLanguage): AppLanguage = language

    /**
     * Creates the Android Storage Access Framework "Save File" intent (`ACTION_CREATE_DOCUMENT`)
     * pre-filled with the suggested PDF filename and hinting `Documents/Fahs` on primary storage.
     */
    fun createDownloadDocumentIntent(suggestedFileName: String): Intent {
        // Best-effort attempt to create Documents/Fahs if the OS allows it without storage permissions
        runCatching {
            val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val fahsDir = File(publicDocs, "Fahs")
            if (!fahsDir.exists()) {
                fahsDir.mkdirs()
            }
        }

        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, suggestedFileName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val initialUri = Uri.parse(
                    "content://com.android.externalstorage.documents/document/primary%3ADocuments%2FFahs"
                )
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            }
        }
    }

    /**
     * Copies a generated PDF file into the destination [Uri] returned by Android's `ACTION_CREATE_DOCUMENT` picker.
     */
    fun writePdfToDestinationUri(
        context: Context,
        sourcePdfFile: File,
        destinationUri: Uri
    ): Boolean {
        if (!sourcePdfFile.exists() || sourcePdfFile.length() <= 0L) return false
        return try {
            context.contentResolver.openOutputStream(destinationUri, "w")?.use { output ->
                FileInputStream(sourcePdfFile).use { input ->
                    input.copyTo(output)
                }
                output.flush()
            } != null
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Opens a saved PDF [Uri] in the phone's PDF viewer (`ACTION_VIEW`) with read permission granted.
     */
    fun openSavedPdfUri(context: Context, pdfUri: Uri, language: AppLanguage): Boolean {
        val title = when (language) {
            AppLanguage.AR -> "فتح تقرير الفحص PDF"
            AppLanguage.FR -> "Ouvrir le rapport PDF"
            AppLanguage.EN -> "Open PDF Report"
        }
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(Intent.createChooser(viewIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            true
        } catch (_: Exception) {
            false
        }
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
            AppLanguage.FR -> "Partager le rapport PDF (WhatsApp / Email)"
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

    /**
     * Core two-pass A4 document layout & renderer.
     * - Mirrors the entire layout right-to-left in Arabic (`language.isRtl == true`).
     * - Uses bundled Cairo (AR) and Plus Jakarta Sans (FR/EN) fonts.
     * - Never cuts text at page breaks and never splits a table row across two pages.
     */
    private fun renderDocumentLayout(
        context: Context,
        report: InspectionReportData,
        language: AppLanguage,
        sellerAskingMillions: Double?,
        mechanicQuotedRepairDzd: Int?,
        totalPagesKnown: Int,
        dryRun: Boolean,
        startPageCanvas: (pageNumber: Int) -> Canvas,
        finishPageCanvas: (pageNumber: Int) -> Unit
    ): Int {
        val isRtl = language.isRtl
        val tfRegular = loadBrandTypeface(context, language, bold = false)
        val tfBold = loadBrandTypeface(context, language, bold = true)

        // Paints
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        fun textPaint(size: Float, color: Int, bold: Boolean = false): TextPaint {
            return TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = size
                this.color = color
                typeface = if (bold) tfBold else tfRegular
            }
        }

        val headerBrandPaint = textPaint(13.0f, Color.WHITE, bold = true)
        val headerDocTitlePaint = textPaint(11.0f, Color.rgb(167, 243, 208), bold = true)
        val headerMetaLabelPaint = textPaint(8.2f, Color.rgb(209, 250, 229), bold = false)
        val headerMetaValuePaint = textPaint(8.8f, Color.WHITE, bold = true)

        val sectionHeadingPaint = textPaint(11.5f, COLOR_EMERALD_900, bold = true)
        val cardTitlePaint = textPaint(10.5f, COLOR_SLATE_900, bold = true)
        val bodyBoldPaint = textPaint(9.2f, COLOR_SLATE_900, bold = true)
        val bodyRegularPaint = textPaint(8.8f, COLOR_SLATE_700, bold = false)
        val smallLabelPaint = textPaint(7.8f, COLOR_SLATE_500, bold = false)
        val smallBoldPaint = textPaint(8.0f, COLOR_SLATE_800, bold = true)
        val tableHeaderPaint = textPaint(8.4f, Color.WHITE, bold = true)

        var currentPageNum = 1
        var canvas = startPageCanvas(currentPageNum)
        var yPos = MARGIN_TOP

        fun finishCurrentAndStartNewPage() {
            if (!dryRun) {
                drawPageFooter(
                    canvas = canvas,
                    pageNum = currentPageNum,
                    totalPages = totalPagesKnown,
                    language = language,
                    isRtl = isRtl,
                    tfRegular = tfRegular,
                    tfBold = tfBold
                )
            }
            finishPageCanvas(currentPageNum)
            currentPageNum++
            canvas = startPageCanvas(currentPageNum)
            yPos = MARGIN_TOP
        }

        fun ensureSpace(requiredHeight: Float): Boolean {
            if (yPos + requiredHeight > MAX_CONTENT_Y) {
                finishCurrentAndStartNewPage()
                return true
            }
            return false
        }

        // =========================================================================
        // 1. HEADER ON THE FIRST PAGE (Logo, Name, Title, Date, Report Number)
        // =========================================================================
        val headerHeight = 68f
        if (!dryRun) {
            fillPaint.color = COLOR_EMERALD_900
            canvas.drawRoundRect(
                RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + headerHeight),
                12f,
                12f,
                fillPaint
            )

            // Accent emerald bar along the bottom of the header
            fillPaint.color = COLOR_EMERALD_600
            canvas.drawRoundRect(
                RectF(MARGIN_H + 12f, yPos + headerHeight - 4f, PAGE_WIDTH - MARGIN_H - 12f, yPos + headerHeight),
                2f,
                2f,
                fillPaint
            )

            // Shield logo emblem on the start side (Left in LTR, Right in RTL)
            val logoSize = 42f
            val logoX = mirrorX(MARGIN_H + 14f, logoSize, isRtl)
            val logoY = yPos + 13f
            drawFahsShieldLogo(context, canvas, logoX, logoY, logoSize)

            // Brand Name & Document Title next to the logo
            val titleBlockWidth = 274f
            val titleBlockX = mirrorX(MARGIN_H + 66f, titleBlockWidth, isRtl)
            val appBrandTitle = when (language) {
                AppLanguage.AR -> "فحص (Fahs) — مساعد فحص السيارات المستعملة"
                AppLanguage.FR -> "Fahs (فحص) — Diagnostic Automobile"
                AppLanguage.EN -> "Fahs (فحص) — Used Car Inspection"
            }
            val docMainTitle = when (language) {
                AppLanguage.AR -> "تقرير فحص السيارة المستعملة"
                AppLanguage.FR -> "Rapport d'inspection automobile"
                AppLanguage.EN -> "Vehicle inspection report"
            }
            val brandH = drawTextBlock(canvas, appBrandTitle, headerBrandPaint, titleBlockX, yPos + 13f, titleBlockWidth.toInt(), isRtl)
            drawTextBlock(canvas, docMainTitle, headerDocTitlePaint, titleBlockX, yPos + 13f + brandH + 3f, titleBlockWidth.toInt(), isRtl)

            // Metadata Pill Box on the end side (Report Number & Date)
            val metaWidth = 168f
            val metaX = mirrorX(PAGE_WIDTH - MARGIN_H - 12f - metaWidth, metaWidth, isRtl)
            fillPaint.color = COLOR_EMERALD_800
            canvas.drawRoundRect(
                RectF(metaX, yPos + 10f, metaX + metaWidth, yPos + headerHeight - 10f),
                8f,
                8f,
                fillPaint
            )
            strokePaint.color = COLOR_EMERALD_600
            canvas.drawRoundRect(
                RectF(metaX, yPos + 10f, metaX + metaWidth, yPos + headerHeight - 10f),
                8f,
                8f,
                strokePaint
            )

            val reportNo = buildReportNumber(report.inspection)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(report.inspection.updatedAt))
            val repLabel = when (language) {
                AppLanguage.AR -> "رقم التقرير: $reportNo"
                AppLanguage.FR -> "Rapport N° : $reportNo"
                AppLanguage.EN -> "Report No: $reportNo"
            }
            val dateLabel = when (language) {
                AppLanguage.AR -> "التاريخ: $dateStr"
                AppLanguage.FR -> "Date : $dateStr"
                AppLanguage.EN -> "Date: $dateStr"
            }
            val repH = drawTextBlock(canvas, repLabel, headerMetaValuePaint, metaX + 8f, yPos + 15f, (metaWidth - 16f).toInt(), isRtl)
            drawTextBlock(canvas, dateLabel, headerMetaLabelPaint, metaX + 8f, yPos + 15f + repH + 4f, (metaWidth - 16f).toInt(), isRtl)
        }
        yPos += headerHeight + 10f

        // =========================================================================
        // 2. CAR SUMMARY BOX (Brand, Model, Generation, Year, Fuel, Gearbox, Mileage, Plate Label)
        // =========================================================================
        val carBoxHeight = 68f
        ensureSpace(carBoxHeight + 10f)
        if (!dryRun) {
            fillPaint.color = COLOR_SLATE_50
            canvas.drawRoundRect(
                RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + carBoxHeight),
                10f,
                10f,
                fillPaint
            )
            strokePaint.color = COLOR_SLATE_300
            canvas.drawRoundRect(
                RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + carBoxHeight),
                10f,
                10f,
                strokePaint
            )

            // Horizontal divider between the 2 rows of 4 cells
            canvas.drawLine(
                MARGIN_H + 8f,
                yPos + 34f,
                PAGE_WIDTH - MARGIN_H - 8f,
                yPos + 34f,
                strokePaint
            )

            val insp = report.inspection
            val genValue = insp.carGeneration.trim().ifBlank { "—" }
            val odoValue = if (insp.odometerKm > 0) {
                when (language) {
                    AppLanguage.AR -> "${String.format(Locale.US, "%,d", insp.odometerKm)} كم"
                    else -> "${String.format(Locale.US, "%,d", insp.odometerKm).replace(',', ' ')} km"
                }
            } else {
                when (language) {
                    AppLanguage.AR -> "غير محدد"
                    AppLanguage.FR -> "Non renseigné"
                    AppLanguage.EN -> "Not entered"
                }
            }
            val plateValue = insp.plateNumber.trim().ifBlank {
                when (language) {
                    AppLanguage.AR -> "غير مدخل"
                    AppLanguage.FR -> "Non renseigné"
                    AppLanguage.EN -> "Not entered"
                }
            }

            val row1Cells = listOf(
                when (language) {
                    AppLanguage.AR -> "العلامة (Brand)" to insp.carBrand
                    AppLanguage.FR -> "Marque" to insp.carBrand
                    AppLanguage.EN -> "Brand" to insp.carBrand
                },
                when (language) {
                    AppLanguage.AR -> "الموديل (Model)" to insp.carModel
                    AppLanguage.FR -> "Modèle" to insp.carModel
                    AppLanguage.EN -> "Model" to insp.carModel
                },
                when (language) {
                    AppLanguage.AR -> "الجيل / النسخة" to genValue
                    AppLanguage.FR -> "Génération" to genValue
                    AppLanguage.EN -> "Generation" to genValue
                },
                when (language) {
                    AppLanguage.AR -> "سنة الصنع" to insp.carYear.toString()
                    AppLanguage.FR -> "Année" to insp.carYear.toString()
                    AppLanguage.EN -> "Year" to insp.carYear.toString()
                }
            )

            val row2Cells = listOf(
                when (language) {
                    AppLanguage.AR -> "الوقود" to insp.fuelType
                    AppLanguage.FR -> "Carburant" to insp.fuelType
                    AppLanguage.EN -> "Fuel" to insp.fuelType
                },
                when (language) {
                    AppLanguage.AR -> "علبة السرعة" to insp.gearboxType
                    AppLanguage.FR -> "Boîte de vitesses" to insp.gearboxType
                    AppLanguage.EN -> "Gearbox" to insp.gearboxType
                },
                when (language) {
                    AppLanguage.AR -> "العداد (كم)" to odoValue
                    AppLanguage.FR -> "Kilométrage" to odoValue
                    AppLanguage.EN -> "Mileage" to odoValue
                },
                when (language) {
                    AppLanguage.AR -> "ترقيم / علامة اللوحة" to plateValue
                    AppLanguage.FR -> "Immatriculation / Repère" to plateValue
                    AppLanguage.EN -> "Plate / Label" to plateValue
                }
            )

            val cellWidth = CONTENT_WIDTH / 4f
            row1Cells.forEachIndexed { idx, (label, value) ->
                val cellLtrX = MARGIN_H + (idx * cellWidth)
                val cellX = mirrorX(cellLtrX, cellWidth, isRtl)
                drawTextBlock(canvas, label, smallLabelPaint, cellX + 8f, yPos + 5f, (cellWidth - 14f).toInt(), isRtl)
                drawTextBlock(canvas, value, bodyBoldPaint, cellX + 8f, yPos + 17f, (cellWidth - 14f).toInt(), isRtl)
            }
            row2Cells.forEachIndexed { idx, (label, value) ->
                val cellLtrX = MARGIN_H + (idx * cellWidth)
                val cellX = mirrorX(cellLtrX, cellWidth, isRtl)
                drawTextBlock(canvas, label, smallLabelPaint, cellX + 8f, yPos + 39f, (cellWidth - 14f).toInt(), isRtl)
                drawTextBlock(canvas, value, bodyBoldPaint, cellX + 8f, yPos + 51f, (cellWidth - 14f).toInt(), isRtl)
            }
        }
        yPos += carBoxHeight + 10f

        // =========================================================================
        // 3. RESULT BLOCK (Large Circular Score Gauge + Verdict in Coloured Band)
        // =========================================================================
        val scoreVal = report.overallScore
        val verdictTitle = report.getVerdictTitle(language)
        val rawBadgeSuffix = if (report.isCriticalCapTriggered && report.rawUncappedScore != null && scoreVal != null) {
            when (language) {
                AppLanguage.AR -> " (المقيّدة: $scoreVal/100 | الخام: ${report.rawUncappedScore}/100)"
                AppLanguage.FR -> " (Plafonné : $scoreVal/100 | Brut : ${report.rawUncappedScore}/100)"
                AppLanguage.EN -> " (Capped: $scoreVal/100 | Raw: ${report.rawUncappedScore}/100)"
            }
        } else ""
        val bandFullTitle = verdictTitle + rawBadgeSuffix

        val (bandBg, bandBorder, bandTextCol) = when (report.verdict) {
            "GOOD" -> Triple(COLOR_GREEN_BAND_BG, COLOR_GREEN_BAND_BORDER, COLOR_GREEN_BAND_TEXT)
            "NEGOTIATE" -> Triple(COLOR_ORANGE_BAND_BG, COLOR_ORANGE_BAND_BORDER, COLOR_ORANGE_BAND_TEXT)
            "AVOID" -> Triple(COLOR_RED_BAND_BG, COLOR_RED_BAND_BORDER, COLOR_RED_BAND_TEXT)
            else -> Triple(COLOR_SLATE_100, COLOR_SLATE_500, COLOR_SLATE_800)
        }
        val verdictBandPaint = textPaint(9.8f, bandTextCol, bold = true)
        val bandWidth = CONTENT_WIDTH - 118f
        val bandInnerWidth = (bandWidth - 28f).toInt()
        val bandTextH = measureTextHeight(bandFullTitle, verdictBandPaint, bandInnerWidth, isRtl)
        val bandHeight = (bandTextH + 10f).coerceAtLeast(24f)

        val capLine = report.getCriticalCapOneLineExplanation(language)
        val rawVerdictSummary = report.getVerdictSummary(language)
        val verdictSummary = if (capLine != null && rawVerdictSummary.startsWith(capLine)) {
            rawVerdictSummary.removePrefix(capLine).trim()
        } else {
            rawVerdictSummary
        }
        val bandTextWidth = (CONTENT_WIDTH - 118f - 24f).toInt()
        val summaryHeight = if (verdictSummary.isNotBlank()) {
            measureTextHeight(verdictSummary, bodyRegularPaint, bandTextWidth, isRtl)
        } else 0f
        val capHeight = if (capLine != null) measureTextHeight(capLine, smallBoldPaint, bandTextWidth, isRtl) + 5f else 0f
        val resultBlockHeight = (18f + bandHeight + capHeight + summaryHeight).coerceAtLeast(88f)

        ensureSpace(resultBlockHeight + 10f)
        if (!dryRun) {
            // Card background
            fillPaint.color = Color.WHITE
            canvas.drawRoundRect(
                RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + resultBlockHeight),
                12f,
                12f,
                fillPaint
            )
            strokePaint.color = COLOR_SLATE_300
            canvas.drawRoundRect(
                RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + resultBlockHeight),
                12f,
                12f,
                strokePaint
            )

            // 3a. Large Circular Score Gauge on the start side
            val gaugeBoxWidth = 104f
            val gaugeBoxX = mirrorX(MARGIN_H + 8f, gaugeBoxWidth, isRtl)
            val gaugeCenterX = gaugeBoxX + (gaugeBoxWidth / 2f)
            val gaugeCenterY = yPos + (resultBlockHeight / 2f)
            val gaugeRadius = 32f

            val gaugeAccentColor = when (report.verdict) {
                "GOOD" -> COLOR_GREEN_BAND_BORDER
                "NEGOTIATE" -> COLOR_ORANGE_BAND_BORDER
                "AVOID" -> COLOR_RED_BAND_BORDER
                else -> COLOR_SLATE_500
            }

            val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 7.5f
                strokeCap = Paint.Cap.ROUND
            }
            val arcRect = RectF(
                gaugeCenterX - gaugeRadius,
                gaugeCenterY - gaugeRadius,
                gaugeCenterX + gaugeRadius,
                gaugeCenterY + gaugeRadius
            )
            arcPaint.color = COLOR_SLATE_200
            canvas.drawArc(arcRect, 0f, 360f, false, arcPaint)

            if (scoreVal != null) {
                arcPaint.color = gaugeAccentColor
                val sweep = (scoreVal.coerceIn(0, 100) / 100f) * 360f
                canvas.drawArc(arcRect, -90f, sweep, false, arcPaint)
            }

            val scoreNumPaint = textPaint(17f, COLOR_SLATE_900, bold = true).apply {
                textAlign = Paint.Align.CENTER
            }
            val scoreDenomPaint = textPaint(8f, COLOR_SLATE_500, bold = true).apply {
                textAlign = Paint.Align.CENTER
            }
            val scoreStr = scoreVal?.toString() ?: "—"
            canvas.drawText(scoreStr, gaugeCenterX, gaugeCenterY + 2f, scoreNumPaint)
            canvas.drawText("/ 100", gaugeCenterX, gaugeCenterY + 14f, scoreDenomPaint)

            // 3b. Verdict Coloured Band & Details on the end side
            val bandX = mirrorX(MARGIN_H + 112f, bandWidth, isRtl)

            // Coloured Verdict Band at the top of the right/left pane
            val bandRect = RectF(bandX, yPos + 8f, bandX + bandWidth - 8f, yPos + 8f + bandHeight)
            fillPaint.color = bandBg
            canvas.drawRoundRect(bandRect, 7f, 7f, fillPaint)
            strokePaint.color = bandBorder
            canvas.drawRoundRect(bandRect, 7f, 7f, strokePaint)

            drawTextBlock(
                canvas = canvas,
                text = bandFullTitle,
                paint = verdictBandPaint,
                x = bandX + 10f,
                y = yPos + 13f,
                width = bandInnerWidth,
                isRtl = isRtl
            )

            var detailY = yPos + 8f + bandHeight + 5f
            if (capLine != null) {
                val capPaint = textPaint(8.3f, COLOR_RED_BAND_TEXT, bold = true)
                val hCap = drawTextBlock(
                    canvas = canvas,
                    text = capLine,
                    paint = capPaint,
                    x = bandX + 6f,
                    y = detailY,
                    width = bandTextWidth,
                    isRtl = isRtl
                )
                detailY += hCap + 3f
            }
            if (verdictSummary.isNotBlank()) {
                drawTextBlock(
                    canvas = canvas,
                    text = verdictSummary,
                    paint = bodyRegularPaint,
                    x = bandX + 6f,
                    y = detailY,
                    width = bandTextWidth,
                    isRtl = isRtl
                )
            }
        }
        yPos += resultBlockHeight + 10f

        // =========================================================================
        // 4. PLAIN-LANGUAGE SUMMARY (3-4 sentences from Phase 7 templates)
        // =========================================================================
        val plainSummaryTitle = when (language) {
            AppLanguage.AR -> "ملخص الفحص بلغة مبسطة (نقاط القوة، العيوب، وما تسأل عنه الميكانيكي)"
            AppLanguage.FR -> "Résumé en langage clair (Points forts, défauts & questions au mécanicien)"
            AppLanguage.EN -> "Plain-Language Summary (Strengths, Problems & What to Ask a Mechanic)"
        }
        val plainSummaryBody = report.getPlainLanguageSummary(language)
        val plainTextWidth = (CONTENT_WIDTH - 24f).toInt()
        val plainBodyH = measureTextHeight(plainSummaryBody, bodyRegularPaint, plainTextWidth, isRtl)
        val plainBoxHeight = 28f + plainBodyH + 10f

        ensureSpace(plainBoxHeight + 10f)
        if (!dryRun) {
            fillPaint.color = COLOR_EMERALD_50
            canvas.drawRoundRect(
                RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + plainBoxHeight),
                10f,
                10f,
                fillPaint
            )
            strokePaint.color = COLOR_EMERALD_600
            canvas.drawRoundRect(
                RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + plainBoxHeight),
                10f,
                10f,
                strokePaint
            )
            drawTextBlock(
                canvas = canvas,
                text = plainSummaryTitle,
                paint = sectionHeadingPaint,
                x = MARGIN_H + 12f,
                y = yPos + 7f,
                width = plainTextWidth,
                isRtl = isRtl
            )
            drawTextBlock(
                canvas = canvas,
                text = plainSummaryBody,
                paint = bodyRegularPaint,
                x = MARGIN_H + 12f,
                y = yPos + 24f,
                width = plainTextWidth,
                isRtl = isRtl
            )
        }
        yPos += plainBoxHeight + 12f

        // =========================================================================
        // 5. SCORES PER AREA (Horizontal bar for each area of the inspection)
        // =========================================================================
        val areas = report.areaScores
        if (areas.isNotEmpty()) {
            val rowH = 16.5f
            val areaBoxHeight = 28f + (areas.size * rowH) + 8f
            ensureSpace(areaBoxHeight + 12f)
            if (!dryRun) {
                fillPaint.color = Color.WHITE
                canvas.drawRoundRect(
                    RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + areaBoxHeight),
                    10f,
                    10f,
                    fillPaint
                )
                strokePaint.color = COLOR_SLATE_300
                canvas.drawRoundRect(
                    RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + areaBoxHeight),
                    10f,
                    10f,
                    strokePaint
                )

                val areaHeaderTitle = when (language) {
                    AppLanguage.AR -> "النتيجة حسب كل قسم من أقسام الفحص"
                    AppLanguage.FR -> "Scores par zone d'inspection"
                    AppLanguage.EN -> "Scores per Inspection Area"
                }
                drawTextBlock(
                    canvas = canvas,
                    text = areaHeaderTitle,
                    paint = sectionHeadingPaint,
                    x = MARGIN_H + 12f,
                    y = yPos + 7f,
                    width = (CONTENT_WIDTH - 24f).toInt(),
                    isRtl = isRtl
                )

                var areaRowY = yPos + 27f
                val labelW = 195f
                val barW = 235f
                val valueW = CONTENT_WIDTH - 24f - labelW - barW - 16f

                for (area in areas) {
                    val labelX = mirrorX(MARGIN_H + 12f, labelW, isRtl)
                    val barX = mirrorX(MARGIN_H + 12f + labelW + 8f, barW, isRtl)
                    val valX = mirrorX(MARGIN_H + 12f + labelW + barW + 16f, valueW, isRtl)

                    drawTextBlock(
                        canvas = canvas,
                        text = area.getTitle(language),
                        paint = smallBoldPaint,
                        x = labelX,
                        y = areaRowY,
                        width = labelW.toInt(),
                        isRtl = isRtl
                    )

                    // Horizontal bar track
                    val barTop = areaRowY + 3f
                    val barHeight = 7.5f
                    fillPaint.color = COLOR_SLATE_200
                    canvas.drawRoundRect(
                        RectF(barX, barTop, barX + barW, barTop + barHeight),
                        4f,
                        4f,
                        fillPaint
                    )

                    val pct = area.scorePercent
                    if (pct != null && pct > 0) {
                        val fillW = ((pct.coerceIn(0, 100) / 100f) * barW).coerceAtLeast(4f)
                        fillPaint.color = when {
                            pct >= 80 -> COLOR_GREEN_BAND_BORDER
                            pct >= 50 -> COLOR_ORANGE_BAND_BORDER
                            else -> COLOR_RED_BAND_BORDER
                        }
                        val fillRect = if (isRtl) {
                            RectF(barX + barW - fillW, barTop, barX + barW, barTop + barHeight)
                        } else {
                            RectF(barX, barTop, barX + fillW, barTop + barHeight)
                        }
                        canvas.drawRoundRect(fillRect, 4f, 4f, fillPaint)
                    }

                    val pctLabel = if (pct != null) {
                        "$pct% (${area.checkedCount}/${area.totalCount})"
                    } else {
                        when (language) {
                            AppLanguage.AR -> "غير مفحوص (0/${area.totalCount})"
                            AppLanguage.FR -> "Non vérifié (0/${area.totalCount})"
                            AppLanguage.EN -> "Unchecked (0/${area.totalCount})"
                        }
                    }
                    drawTextBlock(
                        canvas = canvas,
                        text = pctLabel,
                        paint = smallLabelPaint,
                        x = valX,
                        y = areaRowY,
                        width = valueW.toInt(),
                        isRtl = isRtl
                    )

                    areaRowY += rowH
                }
            }
            yPos += areaBoxHeight + 12f
        }

        // =========================================================================
        // 6. PROBLEMS FOUND (Clean table, most serious first, with photo thumbnails)
        // =========================================================================
        val allProblems = report.criticalProblems + report.negotiableProblems
        val problemsTitle = when (language) {
            AppLanguage.AR -> "جدول المشاكل والعيوب المرصودة — مرتبة حسب الخطورة (${allProblems.size})"
            AppLanguage.FR -> "Tableau des problèmes constatés — du plus grave au moins grave (${allProblems.size})"
            AppLanguage.EN -> "Problems Found — Most Serious First (${allProblems.size})"
        }

        // Column widths for the 5-column problems table (total = CONTENT_WIDTH = 531pt):
        // Col 1: Item & explanation (195pt)
        // Col 2: Severity tag (64pt)
        // Col 3: User's note (116pt)
        // Col 4: Approx. repair cost (86pt)
        // Col 5: Photo thumbnail (70pt)
        val col1W = 195f
        val col2W = 64f
        val col3W = 116f
        val col4W = 86f
        val col5W = CONTENT_WIDTH - col1W - col2W - col3W - col4W // 70f

        fun drawProblemsTableHeader() {
            val thHeight = 22f
            if (!dryRun) {
                fillPaint.color = COLOR_SLATE_800
                canvas.drawRoundRect(
                    RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + thHeight),
                    6f,
                    6f,
                    fillPaint
                )
                val headers = when (language) {
                    AppLanguage.AR -> listOf("العنصر المفحوص", "الخطورة", "ملاحظة المشتري", "التكلفة التقريبية", "الصورة")
                    AppLanguage.FR -> listOf("Élément & Constat", "Gravité", "Note acheteur", "Coût approx.", "Photo")
                    AppLanguage.EN -> listOf("Item & Finding", "Severity", "User's Note", "Approx. Cost", "Photo")
                }
                val widths = listOf(col1W, col2W, col3W, col4W, col5W)
                var curLtrX = MARGIN_H
                headers.forEachIndexed { idx, hText ->
                    val w = widths[idx]
                    val cellX = mirrorX(curLtrX, w, isRtl)
                    drawTextBlock(canvas, hText, tableHeaderPaint, cellX + 5f, yPos + 5f, (w - 10f).toInt(), isRtl)
                    curLtrX += w
                }
            }
            yPos += thHeight
        }

        ensureSpace(54f)
        if (!dryRun) {
            drawTextBlock(
                canvas = canvas,
                text = problemsTitle,
                paint = sectionHeadingPaint,
                x = MARGIN_H,
                y = yPos,
                width = CONTENT_WIDTH.toInt(),
                isRtl = isRtl
            )
        }
        yPos += 18f

        if (allProblems.isEmpty()) {
            val emptyProbHeight = 28f
            ensureSpace(emptyProbHeight + 10f)
            if (!dryRun) {
                fillPaint.color = COLOR_GREEN_BAND_BG
                canvas.drawRoundRect(
                    RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + emptyProbHeight),
                    8f,
                    8f,
                    fillPaint
                )
                val noProbText = when (language) {
                    AppLanguage.AR -> "✓ لم يتم تسجيل أي عيب في النقاط التي تم فحصها."
                    AppLanguage.FR -> "✓ Aucun problème constaté sur l'ensemble des points vérifiés."
                    AppLanguage.EN -> "✓ No problems recorded across the inspected items."
                }
                drawTextBlock(
                    canvas = canvas,
                    text = noProbText,
                    paint = bodyBoldPaint,
                    x = MARGIN_H + 10f,
                    y = yPos + 7f,
                    width = (CONTENT_WIDTH - 20f).toInt(),
                    isRtl = isRtl
                )
            }
            yPos += emptyProbHeight + 10f
        } else {
            drawProblemsTableHeader()

            for ((rowIdx, prob) in allProblems.withIndex()) {
                val itemText = "${prob.getQuestion(language)}\n(${prob.getSectionTitle(language)})"
                // Clean internal auto-answer prefix if present so the user's note looks clean
                val cleanNote = prob.buyerNote
                    .replace(com.example.ui.viewmodel.FahsViewModel.OBD_AUTO_ANSWER_TAG, "")
                    .trim()
                    .ifBlank { "—" }

                val approxCostText = if (prob.maxCostDzd > 0) {
                    "${ReportEngine.formatDzd(prob.minCostDzd)} – ${ReportEngine.formatDzd(prob.maxCostDzd)}"
                } else {
                    when (language) {
                        AppLanguage.AR -> "تُسعّر عند الميكانيكي"
                        AppLanguage.FR -> "À chiffrer (mécanicien)"
                        AppLanguage.EN -> "Mechanic quote"
                    }
                }

                val hasRealPhoto = InspectionPhotoHelper.isRealStoredPhoto(prob.photoUri)
                val hCol1 = measureTextHeight(itemText, bodyBoldPaint, (col1W - 10f).toInt(), isRtl)
                val hCol3 = measureTextHeight(cleanNote, bodyRegularPaint, (col3W - 10f).toInt(), isRtl)
                val hCol4 = measureTextHeight(approxCostText, smallBoldPaint, (col4W - 10f).toInt(), isRtl)
                val photoMinH = if (hasRealPhoto) 48f else 18f
                val rowHeight = (maxOf(hCol1, hCol3, hCol4, photoMinH) + 12f).coerceAtLeast(30f)

                // Never split a table row across two pages: if it doesn't fit, start a new page and redraw table header
                val startedNewPage = ensureSpace(rowHeight + 4f)
                if (startedNewPage) {
                    drawProblemsTableHeader()
                }

                if (!dryRun) {
                    val rowBg = when {
                        prob.severity == ItemSeverity.CRITICAL -> Color.rgb(254, 242, 242)
                        rowIdx % 2 == 1 -> COLOR_SLATE_50
                        else -> Color.WHITE
                    }
                    val rowRect = RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + rowHeight)
                    fillPaint.color = rowBg
                    canvas.drawRect(rowRect, fillPaint)
                    strokePaint.color = COLOR_SLATE_200
                    canvas.drawRect(rowRect, strokePaint)

                    val x1 = mirrorX(MARGIN_H, col1W, isRtl)
                    val x2 = mirrorX(MARGIN_H + col1W, col2W, isRtl)
                    val x3 = mirrorX(MARGIN_H + col1W + col2W, col3W, isRtl)
                    val x4 = mirrorX(MARGIN_H + col1W + col2W + col3W, col4W, isRtl)
                    val x5 = mirrorX(MARGIN_H + col1W + col2W + col3W + col4W, col5W, isRtl)

                    // 1. Item
                    drawTextBlock(canvas, itemText, bodyBoldPaint, x1 + 5f, yPos + 5f, (col1W - 10f).toInt(), isRtl)

                    // 2. Severity pill badge
                    val (sevBg, sevTextCol) = when (prob.severity) {
                        ItemSeverity.CRITICAL -> COLOR_RED_BAND_BG to COLOR_RED_BAND_TEXT
                        ItemSeverity.IMPORTANT -> COLOR_ORANGE_BAND_BG to COLOR_ORANGE_BAND_TEXT
                        ItemSeverity.MINOR -> COLOR_SLATE_200 to COLOR_SLATE_800
                    }
                    val pillRect = RectF(x2 + 4f, yPos + 6f, x2 + col2W - 4f, yPos + 22f)
                    fillPaint.color = sevBg
                    canvas.drawRoundRect(pillRect, 5f, 5f, fillPaint)
                    val sevPaint = textPaint(7.6f, sevTextCol, bold = true).apply {
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(
                        prob.severity.getLabel(language),
                        pillRect.centerX(),
                        pillRect.centerY() + 2.8f,
                        sevPaint
                    )

                    // 3. User's note
                    drawTextBlock(canvas, cleanNote, bodyRegularPaint, x3 + 5f, yPos + 5f, (col3W - 10f).toInt(), isRtl)

                    // 4. Approx repair cost
                    drawTextBlock(canvas, approxCostText, smallBoldPaint, x4 + 5f, yPos + 5f, (col4W - 10f).toInt(), isRtl)

                    // 5. Photo thumbnail (resized & JPEG-compressed for WhatsApp)
                    val thumb = if (hasRealPhoto) {
                        InspectionPhotoHelper.loadCompressedPdfThumbnailBitmap(prob.photoUri, maxDimensionPx = 160, jpegQuality = 72)
                    } else null

                    if (thumb != null) {
                        val maxThumbW = col5W - 10f
                        val maxThumbH = rowHeight - 10f
                        val aspect = thumb.width.toFloat().coerceAtLeast(1f) / thumb.height.toFloat().coerceAtLeast(1f)
                        var drawW = maxThumbW
                        var drawH = drawW / aspect
                        if (drawH > maxThumbH) {
                            drawH = maxThumbH
                            drawW = drawH * aspect
                        }
                        val thumbLeft = x5 + (col5W - drawW) / 2f
                        val thumbTop = yPos + (rowHeight - drawH) / 2f
                        canvas.drawBitmap(thumb, null, RectF(thumbLeft, thumbTop, thumbLeft + drawW, thumbTop + drawH), null)
                    } else {
                        drawTextBlock(canvas, "—", smallLabelPaint, x5 + 10f, yPos + 6f, (col5W - 16f).toInt(), isRtl)
                    }
                }
                yPos += rowHeight
            }
            yPos += 10f
        }

        // =========================================================================
        // 7. OBD SCAN SECTION (Fault codes + explanations, cleared-codes, live readings with markers, or 1-line if not scanned)
        // =========================================================================
        val obd = report.obdBlock
        val obdSectionTitle = when (language) {
            AppLanguage.AR -> "قسم الفحص الإلكتروني للكمبيوتر (OBD2)"
            AppLanguage.FR -> "Diagnostic électronique du calculateur (OBD2)"
            AppLanguage.EN -> "OBD2 Electronic Diagnostic Section"
        }

        if (!obd.isScanned) {
            val oneLineBoxH = 38f
            ensureSpace(oneLineBoxH + 10f)
            if (!dryRun) {
                fillPaint.color = COLOR_SLATE_100
                canvas.drawRoundRect(
                    RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + oneLineBoxH),
                    8f,
                    8f,
                    fillPaint
                )
                strokePaint.color = COLOR_SLATE_300
                canvas.drawRoundRect(
                    RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + oneLineBoxH),
                    8f,
                    8f,
                    strokePaint
                )
                val noScanOneLine = when (language) {
                    AppLanguage.AR -> "لم يتم إجراء فحص إلكتروني للكمبيوتر (OBD2) خلال هذه المعاينة (فحص يدوي وبصري فقط)."
                    AppLanguage.FR -> "Aucun scan OBD2 n'a été effectué lors de cette inspection (contrôle visuel et mécanique uniquement)."
                    AppLanguage.EN -> "No OBD2 electronic scan was performed during this inspection (visual and mechanical check only)."
                }
                drawTextBlock(canvas, obdSectionTitle, sectionHeadingPaint, MARGIN_H + 10f, yPos + 5f, (CONTENT_WIDTH - 20f).toInt(), isRtl)
                drawTextBlock(canvas, noScanOneLine, bodyRegularPaint, MARGIN_H + 10f, yPos + 20f, (CONTENT_WIDTH - 20f).toInt(), isRtl)
            }
            yPos += oneLineBoxH + 10f
        } else {
            val statusLines = listOf(
                when (language) {
                    AppLanguage.AR -> "• الاتصال بالكمبيوتر: ${obd.getConnectionStatusText(language)}"
                    AppLanguage.FR -> "• Connexion calculateur : ${obd.getConnectionStatusText(language)}"
                    AppLanguage.EN -> "• ECU connection: ${obd.getConnectionStatusText(language)}"
                },
                when (language) {
                    AppLanguage.AR -> "• حالة مسح الذاكرة الحديث: ${obd.getClearedCodesText(language)}"
                    AppLanguage.FR -> "• État d'effacement de la mémoire : ${obd.getClearedCodesText(language)}"
                    AppLanguage.EN -> "• Cleared-codes finding: ${obd.getClearedCodesText(language)}"
                },
                when (language) {
                    AppLanguage.AR -> "• لمبة المحرك (Check Engine): ${obd.getCheckEngineLightText(language)}"
                    AppLanguage.FR -> "• Voyant Check-Engine : ${obd.getCheckEngineLightText(language)}"
                    AppLanguage.EN -> "• Check-Engine light: ${obd.getCheckEngineLightText(language)}"
                },
                when (language) {
                    AppLanguage.AR -> "• مقارنة رقم الشاسيه الإلكتروني (09 02): ${obd.getChassisComparisonText(language)}"
                    AppLanguage.FR -> "• Comparaison N° châssis ECU (09 02) : ${obd.getChassisComparisonText(language)}"
                    AppLanguage.EN -> "• ECU VIN comparison (09 02): ${obd.getChassisComparisonText(language)}"
                }
            )
            val dtcExplanationLines = if (obd.faultCodeDetails.isNotEmpty()) {
                obd.faultCodeDetails.map { dtc ->
                    "• [${dtc.code} — ${dtc.dtcType.getLabel(language)}] ${dtc.getTitle(language)} : ${dtc.getExplanation(language)}"
                }
            } else {
                listOf(
                    when (language) {
                        AppLanguage.AR -> "• أكواد الأعطال: ${obd.getFaultCodesText(language)}"
                        AppLanguage.FR -> "• Codes défauts : ${obd.getFaultCodesText(language)}"
                        AppLanguage.EN -> "• Fault codes: ${obd.getFaultCodesText(language)}"
                    }
                )
            }

            val combinedObdText = (statusLines + dtcExplanationLines).joinToString("\n")
            val obdTextW = (CONTENT_WIDTH - 24f).toInt()
            val obdTextH = measureTextHeight(combinedObdText, bodyRegularPaint, obdTextW, isRtl)
            val liveRowsCount = obd.liveReadings.size
            val liveSectionH = if (liveRowsCount > 0) (20f + liveRowsCount * 16f) else 0f
            val obdCardH = 28f + obdTextH + liveSectionH + 10f

            ensureSpace(obdCardH + 10f)
            if (!dryRun) {
                fillPaint.color = if (obd.hasCriticalAlert) COLOR_RED_BAND_BG else COLOR_EMERALD_50
                canvas.drawRoundRect(
                    RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + obdCardH),
                    10f,
                    10f,
                    fillPaint
                )
                strokePaint.color = if (obd.hasCriticalAlert) COLOR_RED_BAND_BORDER else COLOR_EMERALD_600
                canvas.drawRoundRect(
                    RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + obdCardH),
                    10f,
                    10f,
                    strokePaint
                )

                drawTextBlock(canvas, obdSectionTitle, sectionHeadingPaint, MARGIN_H + 12f, yPos + 7f, obdTextW, isRtl)
                val drawnH = drawTextBlock(canvas, combinedObdText, bodyRegularPaint, MARGIN_H + 12f, yPos + 24f, obdTextW, isRtl)

                if (obd.liveReadings.isNotEmpty()) {
                    var liveY = yPos + 28f + drawnH
                    val liveHeader = when (language) {
                        AppLanguage.AR -> "القراءات الحية المسجلة (مع مؤشر الحالة):"
                        AppLanguage.FR -> " Valeurs en direct mesurées (avec marqueur d'état) :"
                        AppLanguage.EN -> "Live Engine Readings (with status markers):"
                    }
                    drawTextBlock(canvas, liveHeader, bodyBoldPaint, MARGIN_H + 12f, liveY, obdTextW, isRtl)
                    liveY += 16f

                    for (gauge in obd.liveReadings) {
                        val markerColor = when (gauge.status) {
                            "GREEN" -> COLOR_GREEN_BAND_BORDER
                            "ORANGE" -> COLOR_ORANGE_BAND_BORDER
                            "RED" -> COLOR_RED_BAND_BORDER
                            else -> COLOR_SLATE_500
                        }
                        val markerTag = when (gauge.status) {
                            "GREEN" -> "OK"
                            "ORANGE" -> "WARN"
                            "RED" -> "ALERT"
                            else -> "N/A"
                        }
                        val dotX = mirrorX(MARGIN_H + 16f, 8f, isRtl)
                        fillPaint.color = markerColor
                        canvas.drawCircle(dotX + 4f, liveY + 6f, 4f, fillPaint)

                        val rowText = "${gauge.getName(language)} : ${gauge.valueText} (Normal : ${gauge.normalRangeText}) [$markerTag]"
                        val rowTextX = mirrorX(MARGIN_H + 30f, CONTENT_WIDTH - 42f, isRtl)
                        drawTextBlock(canvas, rowText, smallBoldPaint, rowTextX, liveY, (CONTENT_WIDTH - 42f).toInt(), isRtl)
                        liveY += 15f
                    }
                }
            }
            yPos += obdCardH + 10f
        }

        // =========================================================================
        // 8. NOT CHECKED (Skipped items, with critical ones highlighted)
        // =========================================================================
        val uncheckedHeader = when (language) {
            AppLanguage.AR -> "نقاط لم يتم فحصها (${report.uncheckedItems.size} من أصل ${report.totalApplicableCount})"
            AppLanguage.FR -> "Points non vérifiés (${report.uncheckedItems.size} sur ${report.totalApplicableCount})"
            AppLanguage.EN -> "Not Checked (${report.uncheckedItems.size} of ${report.totalApplicableCount})"
        }
        ensureSpace(36f)
        if (!dryRun) {
            drawTextBlock(canvas, uncheckedHeader, sectionHeadingPaint, MARGIN_H, yPos, CONTENT_WIDTH.toInt(), isRtl)
        }
        yPos += 18f

        if (report.uncheckedItems.isEmpty()) {
            val allCheckedH = 24f
            ensureSpace(allCheckedH + 8f)
            if (!dryRun) {
                val allCheckedMsg = when (language) {
                    AppLanguage.AR -> "✓ تم فحص جميع النقاط المبرمجة لهذا الموديل بالكامل (0 غير مفحوص)."
                    AppLanguage.FR -> "✓ Tous les points de contrôle prévus ont été vérifiés (0 point ignoré)."
                    AppLanguage.EN -> "✓ All scheduled checklist items were checked (0 skipped items)."
                }
                drawTextBlock(canvas, allCheckedMsg, bodyRegularPaint, MARGIN_H + 8f, yPos, (CONTENT_WIDTH - 16f).toInt(), isRtl)
            }
            yPos += allCheckedH + 8f
        } else {
            val orderedUnchecked = report.uncheckedCriticalItems +
                report.uncheckedItems.filter { it.severity != ItemSeverity.CRITICAL }

            for (item in orderedUnchecked) {
                val isCrit = item.severity == ItemSeverity.CRITICAL
                val critBadge = if (isCrit) {
                    when (language) {
                        AppLanguage.AR -> "[حرج — لم يُفحص] "
                        AppLanguage.FR -> "[CRITIQUE — NON VÉRIFIÉ] "
                        AppLanguage.EN -> "[CRITICAL — NOT CHECKED] "
                    }
                } else "• "
                val lineStr = "$critBadge${item.getSectionTitle(language)} : ${item.getQuestion(language)}"
                val linePaint = if (isCrit) textPaint(8.8f, COLOR_RED_BAND_TEXT, bold = true) else bodyRegularPaint
                val itemH = measureTextHeight(lineStr, linePaint, (CONTENT_WIDTH - 20f).toInt(), isRtl) + if (isCrit) 10f else 4f

                ensureSpace(itemH + 4f)
                if (!dryRun) {
                    if (isCrit) {
                        fillPaint.color = COLOR_RED_BAND_BG
                        canvas.drawRoundRect(
                            RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + itemH),
                            6f,
                            6f,
                            fillPaint
                        )
                        strokePaint.color = COLOR_RED_BAND_BORDER
                        canvas.drawRoundRect(
                            RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + itemH),
                            6f,
                            6f,
                            strokePaint
                        )
                        drawTextBlock(canvas, lineStr, linePaint, MARGIN_H + 10f, yPos + 4f, (CONTENT_WIDTH - 20f).toInt(), isRtl)
                    } else {
                        drawTextBlock(canvas, lineStr, linePaint, MARGIN_H + 8f, yPos + 1f, (CONTENT_WIDTH - 16f).toInt(), isRtl)
                    }
                }
                yPos += itemH + 3f
            }
            yPos += 6f
        }

        // =========================================================================
        // 9. NEGOTIATION TOTAL (Highlighted box)
        // =========================================================================
        val effectiveAskingMillions = sellerAskingMillions?.takeIf { it > 0.0 }
            ?: report.inspection.sellerAskingPriceMillions.takeIf { it > 0.0 }
        val effectiveMechanicQuoteDzd = mechanicQuotedRepairDzd?.takeIf { it > 0 }
            ?: report.inspection.userQuotedRepairDzd.takeIf { it > 0 }
            ?: report.avgTotalRepairDzd.takeIf { it > 0 }

        val negBoxTitle = when (language) {
            AppLanguage.AR -> "إجمالي تكلفة الإصلاح والتفاوض على السعر"
            AppLanguage.FR -> "Total de négociation & Coût approximatif des réparations"
            AppLanguage.EN -> "Negotiation Total & Approximate Repair Cost"
        }

        val totalRepairLine = if (effectiveMechanicQuoteDzd != null && effectiveMechanicQuoteDzd > 0) {
            when (language) {
                AppLanguage.AR -> "إجمالي تكلفة الإصلاح التقديرية: ${ReportEngine.formatAlgerianCurrency(effectiveMechanicQuoteDzd, language)}"
                AppLanguage.FR -> "Total approximatif des réparations : ${ReportEngine.formatAlgerianCurrency(effectiveMechanicQuoteDzd, language)}"
                AppLanguage.EN -> "Total approximate repair cost: ${ReportEngine.formatAlgerianCurrency(effectiveMechanicQuoteDzd, language)}"
            }
        } else {
            when (language) {
                AppLanguage.AR -> "إجمالي تكلفة الإصلاح: لم يتم إدخال تسعيرة ميكانيكي بعد (يُنصح بتسعير العيوب المرصودة أعلاه عند ميكانيكي قبل التفاوض)."
                AppLanguage.FR -> "Coût total des réparations : aucun devis mécanicien saisi (faites chiffrer les problèmes ci-dessus par votre mécanicien)."
                AppLanguage.EN -> "Total repair cost: no mechanic quote entered yet (ask your mechanic to quote the problems listed above)."
            }
        }

        val offerSummaryLine = if (effectiveAskingMillions != null && effectiveAskingMillions > 0) {
            val quoteToSubtract = effectiveMechanicQuoteDzd ?: 0
            val targetOfferDzd = ReportEngine.calculateFairOfferFromMechanicQuoteDzd(
                askingPriceMillions = effectiveAskingMillions,
                mechanicQuotedRepairDzd = quoteToSubtract
            )
            when (language) {
                AppLanguage.AR -> "السعر المطلوب من البائع: ${effectiveAskingMillions.toInt()} مليون سنتيم ← العرض المقترح بعد خصم الإصلاحات: ${ReportEngine.formatMillionsCentimes(targetOfferDzd, language)} (${ReportEngine.formatDzd(targetOfferDzd)})"
                AppLanguage.FR -> "Prix demandé : ${effectiveAskingMillions.toInt()} Millions → Offre recommandée après déduction des réparations : ${ReportEngine.formatMillionsCentimes(targetOfferDzd, language)} (${ReportEngine.formatDzd(targetOfferDzd)})"
                AppLanguage.EN -> "Seller's Asking Price: ${effectiveAskingMillions.toInt()}M → Target Offer After Repairs: ${ReportEngine.formatMillionsCentimes(targetOfferDzd, language)} (${ReportEngine.formatDzd(targetOfferDzd)})"
            }
        } else ""

        val negBodyText = if (offerSummaryLine.isNotBlank()) "$totalRepairLine\n$offerSummaryLine" else totalRepairLine
        val negTextW = (CONTENT_WIDTH - 24f).toInt()
        val negBodyH = measureTextHeight(negBodyText, bodyBoldPaint, negTextW, isRtl)
        val negBoxH = 28f + negBodyH + 12f

        ensureSpace(negBoxH + 8f)
        if (!dryRun) {
            fillPaint.color = COLOR_ORANGE_BAND_BG
            canvas.drawRoundRect(
                RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + negBoxH),
                10f,
                10f,
                fillPaint
            )
            strokePaint.color = COLOR_ORANGE_BAND_BORDER
            strokePaint.strokeWidth = 1.5f
            canvas.drawRoundRect(
                RectF(MARGIN_H, yPos, PAGE_WIDTH - MARGIN_H, yPos + negBoxH),
                10f,
                10f,
                strokePaint
            )
            strokePaint.strokeWidth = 1f

            val negTitlePaint = textPaint(11f, COLOR_ORANGE_BAND_TEXT, bold = true)
            drawTextBlock(canvas, negBoxTitle, negTitlePaint, MARGIN_H + 12f, yPos + 7f, negTextW, isRtl)
            drawTextBlock(canvas, negBodyText, bodyBoldPaint, MARGIN_H + 12f, yPos + 24f, negTextW, isRtl)
        }
        yPos += negBoxH + 8f

        // Finish last page with footer
        if (!dryRun) {
            drawPageFooter(
                canvas = canvas,
                pageNum = currentPageNum,
                totalPages = totalPagesKnown,
                language = language,
                isRtl = isRtl,
                tfRegular = tfRegular,
                tfBold = tfBold
            )
        }
        finishPageCanvas(currentPageNum)
        return currentPageNum
    }

    /**
     * Mirrors a horizontal coordinate across [PAGE_WIDTH] when rendering in Arabic RTL.
     */
    private fun mirrorX(ltrLeft: Float, elementWidth: Float, isRtl: Boolean): Float {
        return if (isRtl) (PAGE_WIDTH - ltrLeft - elementWidth) else ltrLeft
    }

    private fun measureTextHeight(
        text: String,
        paint: TextPaint,
        width: Int,
        isRtl: Boolean
    ): Float {
        val safeW = width.coerceAtLeast(40)
        val dir = if (isRtl) TextDirectionHeuristics.RTL else TextDirectionHeuristics.LTR
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, safeW)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(dir)
            .setLineSpacing(1.5f, 1.05f)
            .setIncludePad(false)
            .build()
        return layout.height.toFloat()
    }

    private fun drawTextBlock(
        canvas: Canvas,
        text: String,
        paint: TextPaint,
        x: Float,
        y: Float,
        width: Int,
        isRtl: Boolean
    ): Float {
        val safeW = width.coerceAtLeast(40)
        val dir = if (isRtl) TextDirectionHeuristics.RTL else TextDirectionHeuristics.LTR
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, safeW)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(dir)
            .setLineSpacing(1.5f, 1.05f)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    private fun drawFahsShieldLogo(
        context: Context,
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float
    ) {
        val logoBmp = runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_fahs_shield_logo_1794321364551)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.ic_fahs_launcher_fg)
        }.getOrNull()

        if (logoBmp != null && logoBmp.width > 0) {
            val dest = RectF(x, y, x + size, y + size)
            val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawBitmap(logoBmp, null, dest, null)
            canvas.drawRoundRect(dest, 8f, 8f, clipPaint)
            return
        }

        // Vector shield fallback when running in JVM environments where bitmap resources may not decode
        val shieldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_EMERALD_600
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
        }
        val cx = x + size / 2f
        val top = y + 2f
        val bottom = y + size - 2f
        val path = Path().apply {
            moveTo(cx, top)
            lineTo(x + size - 3f, top + size * 0.22f)
            lineTo(x + size - 5f, top + size * 0.62f)
            lineTo(cx, bottom)
            lineTo(x + 5f, top + size * 0.62f)
            lineTo(x + 3f, top + size * 0.22f)
            close()
        }
        canvas.drawPath(path, shieldPaint)
        canvas.drawPath(path, borderPaint)

        val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2.6f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx - 7f, y + size * 0.52f, cx - 2f, y + size * 0.64f, checkPaint)
        canvas.drawLine(cx - 2f, y + size * 0.64f, cx + 8f, y + size * 0.38f, checkPaint)
    }

    /**
     * 10. FOOTER ON EVERY PAGE ("page X of Y", app name, and mechanic disclaimer)
     */
    private fun drawPageFooter(
        canvas: Canvas,
        pageNum: Int,
        totalPages: Int,
        language: AppLanguage,
        isRtl: Boolean,
        tfRegular: Typeface,
        tfBold: Typeface
    ) {
        val lineY = PAGE_HEIGHT - 32f
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_SLATE_300
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN_H, lineY, PAGE_WIDTH - MARGIN_H, lineY, dividerPaint)

        val disclaimerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_SLATE_500
            textSize = 7.6f
            typeface = tfRegular
        }
        val pageNumPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_EMERALD_900
            textSize = 8.2f
            typeface = tfBold
        }

        val disclaimerText = when (language) {
            AppLanguage.AR -> "فحص (Fahs) — أداة مساعدة للمشتري ولا تعوّض الفحص المعمّق لدى ميكانيكي مختص على رافعة."
            AppLanguage.FR -> "Fahs (فحص) — Ce rapport est une aide à l'inspection et ne remplace pas l'examen par un mécanicien professionnel."
            AppLanguage.EN -> "Fahs (فحص) — This report is a buyer's inspection aid and does not replace a professional mechanic."
        }
        val pageText = when (language) {
            AppLanguage.AR -> "الصفحة $pageNum من $totalPages"
            AppLanguage.FR -> "page $pageNum sur $totalPages"
            AppLanguage.EN -> "page $pageNum of $totalPages"
        }

        val disclaimerW = CONTENT_WIDTH - 95f
        val pageW = 90f
        val disclaimerX = mirrorX(MARGIN_H, disclaimerW, isRtl)
        val pageX = mirrorX(PAGE_WIDTH - MARGIN_H - pageW, pageW, isRtl)

        drawTextBlock(canvas, disclaimerText, disclaimerPaint, disclaimerX, lineY + 6f, disclaimerW.toInt(), isRtl)
        drawTextBlock(canvas, pageText, pageNumPaint, pageX, lineY + 6f, pageW.toInt(), isRtl)
    }
}
