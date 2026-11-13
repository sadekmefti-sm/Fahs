package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DesignMockupRendererTest {

    // Approved Instrument Panel & Paper Palette
    private val COLOR_INK = Color.parseColor("#0E1F1A")          // Scan surface & primary text
    private val COLOR_INK_ELEVATED = Color.parseColor("#162D26") // Subtle dark surface step
    private val COLOR_INK_HAIRLINE = Color.parseColor("#244239") // Hairline on dark instrument surface
    private val COLOR_BRAND = Color.parseColor("#0F6B4F")        // Primary action button (deep dark green ink)
    private val COLOR_PAPER = Color.parseColor("#F6F7F5")        // Light calm background
    private val COLOR_SURFACE = Color.parseColor("#FFFFFF")      // Interactive object surface
    private val COLOR_HAIRLINE = Color.parseColor("#E3E6E2")     // 1dp list divider
    private val COLOR_MUTED_TEXT = Color.parseColor("#52605B")   // Secondary text on paper (7.1:1 on #F6F7F5)
    private val COLOR_DARK_MUTED = Color.parseColor("#A5B8B1")   // Secondary text on #0E1F1A (8.4:1 contrast)

    // Strict Result-Only Semantic Colours (Never used on buttons or decorative headers)
    private val COLOR_RESULT_GREEN = Color.parseColor("#1E9E5A")
    private val COLOR_RESULT_ORANGE = Color.parseColor("#E08A00")
    private val COLOR_RESULT_RED = Color.parseColor("#D43D2F")
    private val COLOR_RESULT_GREY = Color.parseColor("#6E7873")

    private val W = 720
    private val H = 1520

    @Test
    fun renderAllSixDesignProposalMockups() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val jakartaRegular = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans) ?: Typeface.SANS_SERIF
        val jakartaBold = Typeface.create(jakartaRegular, Typeface.BOLD)
        val cairoRegular = ResourcesCompat.getFont(context, R.font.cairo) ?: Typeface.SANS_SERIF
        val cairoBold = Typeface.create(cairoRegular, Typeface.BOLD)

        val outDir1 = File("../design_mockups").apply { mkdirs() }
        val outDir2 = File("/.aistudio/artifacts/brain/6b4ac22d-0983-4414-b677-09169a0178c4").apply { mkdirs() }

        fun save(name: String, bmp: Bitmap) {
            listOf(File(outDir1, name), File(outDir2, name)).forEach { file ->
                runCatching {
                    FileOutputStream(file).use { out ->
                        bmp.compress(Bitmap.CompressFormat.PNG, 95, out)
                    }
                }
            }
        }

        save("mockup_1_home_fr.png", renderHome(isAr = false, jakartaRegular, jakartaBold))
        save("mockup_2_home_ar.png", renderHome(isAr = true, cairoRegular, cairoBold))
        save("mockup_3_scan_progress_fr.png", renderScanInProgress(isAr = false, jakartaRegular, jakartaBold))
        save("mockup_4_scan_progress_ar.png", renderScanInProgress(isAr = true, cairoRegular, cairoBold))
        save("mockup_5_scan_result_fr.png", renderScanResult(isAr = false, jakartaRegular, jakartaBold))
        save("mockup_6_scan_result_ar.png", renderScanResult(isAr = true, cairoRegular, cairoBold))
    }

    private fun drawTextBlock(
        canvas: Canvas,
        text: String,
        tf: Typeface,
        sizePx: Float,
        color: Int,
        x: Float,
        y: Float,
        width: Int,
        isRtl: Boolean,
        lineMult: Float = if (isRtl) 1.28f else 1.18f,
        alignCenter: Boolean = false
    ): Float {
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tf
            textSize = sizePx
            this.color = color
        }
        val alignment = when {
            alignCenter -> Layout.Alignment.ALIGN_CENTER
            else -> Layout.Alignment.ALIGN_NORMAL
        }
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, tp, width.coerceAtLeast(20))
            .setAlignment(alignment)
            .setTextDirection(if (isRtl) TextDirectionHeuristics.RTL else TextDirectionHeuristics.LTR)
            .setLineSpacing(0f, lineMult)
            .setIncludePad(true)
            .build()
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    private fun drawStatusBar(canvas: Canvas, darkSurface: Boolean) {
        val fg = if (darkSurface) Color.WHITE else COLOR_INK
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fg
            textSize = 22f
            isFakeBoldText = true
        }
        canvas.drawText("10:42", 44f, 44f, p)
        // Battery & signal minimal vector icons
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fg
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fg
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(636f, 26f, 672f, 44f), 4f, 4f, stroke)
        canvas.drawRect(640f, 30f, 664f, 40f, fill)
        canvas.drawRect(674f, 31f, 677f, 39f, fill)
    }

    private fun drawBottomNav(
        canvas: Canvas,
        isAr: Boolean,
        tfReg: Typeface,
        tfBold: Typeface,
        selectedIndex: Int
    ) {
        val navTop = H - 136f
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_PAPER }
        canvas.drawRect(0f, navTop, W.toFloat(), H.toFloat(), bgPaint)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_HAIRLINE
            strokeWidth = 2f
        }
        canvas.drawLine(0f, navTop, W.toFloat(), navTop, linePaint)

        val labels = if (isAr) {
            listOf("الرئيسية", "تقاريري", "الدليل", "حسابي")
        } else {
            listOf("Accueil", "Mes rapports", "Guide", "Compte")
        }
        val colW = W / 4f
        for (i in 0..3) {
            val visualIdx = if (isAr) (3 - i) else i
            val cx = visualIdx * colW + colW / 2f
            val isSel = i == selectedIndex

            if (isSel) {
                val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#DCECE6")
                }
                canvas.drawRoundRect(RectF(cx - 56f, navTop + 16f, cx + 56f, navTop + 62f), 23f, 23f, pillPaint)
            }

            // Simple clean geometric line icon (consistent 2.5px weight)
            val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isSel) COLOR_INK else COLOR_MUTED_TEXT
                style = Paint.Style.STROKE
                strokeWidth = 3f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            val iy = navTop + 39f
            when (i) {
                0 -> { // Home roof + walls
                    val path = Path().apply {
                        moveTo(cx - 14f, iy + 2f)
                        lineTo(cx, iy - 12f)
                        lineTo(cx + 14f, iy + 2f)
                        moveTo(cx - 10f, iy)
                        lineTo(cx - 10f, iy + 12f)
                        lineTo(cx + 10f, iy + 12f)
                        lineTo(cx + 10f, iy)
                    }
                    canvas.drawPath(path, iconPaint)
                }
                1 -> { // Reports list lines
                    canvas.drawRoundRect(RectF(cx - 12f, iy - 12f, cx + 12f, iy + 12f), 4f, 4f, iconPaint)
                    canvas.drawLine(cx - 6f, iy - 4f, cx + 6f, iy - 4f, iconPaint)
                    canvas.drawLine(cx - 6f, iy + 4f, cx + 4f, iy + 4f, iconPaint)
                }
                2 -> { // Guide book
                    canvas.drawRoundRect(RectF(cx - 13f, iy - 11f, cx + 13f, iy + 11f), 3f, 3f, iconPaint)
                    canvas.drawLine(cx, iy - 11f, cx, iy + 11f, iconPaint)
                }
                3 -> { // Account person
                    canvas.drawCircle(cx, iy - 5f, 6f, iconPaint)
                    canvas.drawArc(RectF(cx - 12f, iy + 3f, cx + 12f, iy + 19f), 180f, 180f, false, iconPaint)
                }
            }

            drawTextBlock(
                canvas = canvas,
                text = labels[i],
                tf = if (isSel) tfBold else tfReg,
                sizePx = 21f,
                color = if (isSel) COLOR_INK else COLOR_MUTED_TEXT,
                x = visualIdx * colW + 6f,
                y = navTop + 68f,
                width = (colW - 12f).toInt(),
                isRtl = isAr,
                alignCenter = true
            )
        }

        // Gesture pill
        val pill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#B8C0BC") }
        canvas.drawRoundRect(RectF(W / 2f - 68f, H - 18f, W / 2f + 68f, H - 10f), 4f, 4f, pill)
    }

    private fun renderHome(isAr: Boolean, tfReg: Typeface, tfBold: Typeface): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(COLOR_PAPER)
        drawStatusBar(canvas, darkSurface = false)

        val pad = 40f
        val contentW = (W - pad * 2).toInt()
        var y = 92f

        // Quiet wordmark row (no green banner, no decorative box)
        val wordmark = if (isAr) "فحص  ·  Fahs" else "Fahs"
        drawTextBlock(canvas, wordmark, tfBold, 28f, COLOR_BRAND, pad, y, contentW, isAr)

        // Optional car model pill on the opposite side
        val carChipText = if (isAr) "اختيار السيارة (اختياري)" else "Choisir le modèle (optionnel)"
        val chipW = 285f
        val chipX = if (isAr) pad else (W - pad - chipW)
        val chipStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_HAIRLINE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(RectF(chipX, y - 4f, chipX + chipW, y + 44f), 24f, 24f, chipStroke)
        drawTextBlock(canvas, carChipText, tfReg, 20f, COLOR_MUTED_TEXT, chipX + 16f, y + 6f, (chipW - 32f).toInt(), isAr, alignCenter = true)

        y += 88f

        // Single main question
        val question = if (isAr) "كيف تريد فحص السيارة؟" else "Comment voulez-vous inspecter la voiture ?"
        val qH = drawTextBlock(canvas, question, tfBold, 44f, COLOR_INK, pad, y, contentW, isAr)
        y += qH + 44f

        // Large Primary Surface 1: Automatic Scan (Recommended, deep #0F6B4F brand surface)
        val card1H = 380f
        val card1Rect = RectF(pad, y, W - pad, y + card1H)
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_BRAND }
        canvas.drawRoundRect(card1Rect, 32f, 32f, brandPaint)

        // Subtle custom vector illustration of the ELM327 dongle +无线 pulse on the card
        val illusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val iconCx = if (isAr) (W - pad - 68f) else (pad + 68f)
        val iconCy = y + 72f
        // Dongle connector trapezoid + body
        canvas.drawRoundRect(RectF(iconCx - 28f, iconCy - 20f, iconCx + 28f, iconCy + 24f), 8f, 8f, illusPaint)
        canvas.drawRect(iconCx - 16f, iconCy - 32f, iconCx + 16f, iconCy - 20f, illusPaint)
        canvas.drawCircle(iconCx - 10f, iconCy, 3f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
        canvas.drawCircle(iconCx + 10f, iconCy, 3f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })

        // Small recommended pill tag
        val recTag = if (isAr) "موصى به · دقيقتان، بفيشة OBD" else "Recommandé · 2 minutes, avec boîtier OBD"
        val tagBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0A4F3A") }
        val tagX = if (isAr) pad + 32f else pad + 124f
        val tagW = W - pad * 2 - 156f
        canvas.drawRoundRect(RectF(tagX, y + 48f, tagX + tagW, y + 94f), 23f, 23f, tagBg)
        drawTextBlock(canvas, recTag, tfBold, 21f, Color.WHITE, tagX + 18f, y + 56f, (tagW - 36f).toInt(), isAr)

        // Primary Title & plain explanation
        val title1 = if (isAr) "فحص أوتوماتيكي" else "Scan automatique"
        val sub1 = if (isAr) {
            "يفحص كمبيوتر المحرك، الأعطال المسجلة، مسح الذاكرة الحديث، الحرارة والشحن بضغطة واحدة."
        } else {
            "Vérifie le calculateur moteur, les défauts enregistrés, les codes effacés, la température et la charge en un geste."
        }
        val t1H = drawTextBlock(canvas, title1, tfBold, 38f, Color.WHITE, pad + 32f, y + 132f, contentW - 64, isAr)
        drawTextBlock(canvas, sub1, tfReg, 24f, Color.parseColor("#D7EFE7"), pad + 32f, y + 140f + t1H, contentW - 64, isAr)

        // Action row inside primary button surface
        val cta1 = if (isAr) "ابدأ الفحص الأوتوماتيكي ←" else "Lancer le scan automatique →"
        drawTextBlock(canvas, cta1, tfBold, 25f, Color.WHITE, pad + 32f, y + card1H - 64f, contentW - 64, isAr)

        y += card1H + 32f

        // Large Secondary Surface 2: Manual Inspection (Calm white surface, crisp ink border)
        val card2H = 320f
        val card2Rect = RectF(pad, y, W - pad, y + card2H)
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_SURFACE }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_HAIRLINE
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(card2Rect, 32f, 32f, whitePaint)
        canvas.drawRoundRect(card2Rect, 32f, 32f, borderPaint)

        val tag2 = if (isAr) "قائمة فحص موجهة · بدون أي جهاز" else "Liste guidée · sans matériel"
        drawTextBlock(canvas, tag2, tfBold, 21f, COLOR_MUTED_TEXT, pad + 32f, y + 36f, contentW - 64, isAr)

        val title2 = if (isAr) "فحص يدوي خطوة بخطوة" else "Inspection manuelle"
        val sub2 = if (isAr) {
            "معاينة الوثائق، الهيكل والصبيغة، أسفل السيارة، والتجربة على الطريق سؤالاً بسؤال."
        } else {
            "Papiers, carrosserie, peinture, soubassement et essai sur route, question par question."
        }
        val t2H = drawTextBlock(canvas, title2, tfBold, 34f, COLOR_INK, pad + 32f, y + 76f, contentW - 64, isAr)
        drawTextBlock(canvas, sub2, tfReg, 23f, COLOR_MUTED_TEXT, pad + 32f, y + 84f + t2H, contentW - 64, isAr)

        val cta2 = if (isAr) "بدء الفحص اليدوي ←" else "Commencer l'inspection manuelle →"
        drawTextBlock(canvas, cta2, tfBold, 24f, COLOR_BRAND, pad + 32f, y + card2H - 60f, contentW - 64, isAr)

        y += card2H + 40f

        // Single small line below when an inspection is in progress
        val resumeLine = if (isAr) {
            "متابعة آخر فحص قيد الإنجاز (Peugeot 208 · أمس)"
        } else {
            "Continuer ma dernière inspection (Peugeot 208 · hier)"
        }
        drawTextBlock(canvas, resumeLine, tfBold, 23f, COLOR_BRAND, pad, y, contentW, isAr, alignCenter = true)

        drawBottomNav(canvas, isAr, tfReg, tfBold, selectedIndex = 0)
        return bmp
    }

    private fun renderScanInProgress(isAr: Boolean, tfReg: Typeface, tfBold: Typeface): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        // Instrument Panel Dark Surface (#0E1F1A)
        canvas.drawColor(COLOR_INK)
        drawStatusBar(canvas, darkSurface = true)

        val pad = 40f
        val contentW = (W - pad * 2).toInt()
        var y = 92f

        // Top bar: Cancel / Back and optional car label
        val topLabel = if (isAr) "فحص المحرك والإلكترونيات" else "Scan moteur et électronique"
        drawTextBlock(canvas, topLabel, tfBold, 26f, Color.WHITE, pad, y, contentW, isAr)
        val subCar = if (isAr) "Peugeot 208 · متّصل بـ OBDII" else "Peugeot 208 · Connecté à OBDII"
        drawTextBlock(canvas, subCar, tfReg, 21f, COLOR_DARK_MUTED, pad, y + 36f, contentW, isAr)

        y += 120f

        // Signature Instrument Cluster Dial in the center
        val cx = W / 2f
        val cy = y + 210f
        val radius = 176f

        // Outer subtle instrument ring
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_INK_HAIRLINE
            style = Paint.Style.STROKE
            strokeWidth = 18f
            strokeCap = Paint.Cap.ROUND
        }
        val activeArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D6E2EE") // Neutral pale grey-blue progress arc (never green, so green only means a result)
            style = Paint.Style.STROKE
            strokeWidth = 18f
            strokeCap = Paint.Cap.ROUND
        }

        // Draw 6 discrete segments around a 260-degree instrument sweep (mirrored in RTL)
        val totalSegments = 6
        val completedSegments = 3
        val gapDeg = 8f
        val totalSweep = 260f
        val segSweep = (totalSweep - gapDeg * (totalSegments - 1)) / totalSegments
        val arcRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        for (i in 0 until totalSegments) {
            val startAngle = if (isAr) {
                // Mirror direction in Arabic: sweep counter-clockwise from right (40 deg)
                40f - i * (segSweep + gapDeg) - segSweep
            } else {
                // Clockwise from bottom-left (140 deg)
                140f + i * (segSweep + gapDeg)
            }
            val p = if (i < completedSegments) activeArcPaint else trackPaint
            canvas.drawArc(arcRect, startAngle, segSweep, false, p)
        }

        // Center tabular progress figure (step 3 of 6, elapsed seconds)
        val stepNumPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tfBold
            textSize = 72f
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("3 / 6", cx, cy + 16f, stepNumPaint)

        val stepSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tfReg
            textSize = 22f
            color = COLOR_DARK_MUTED
            textAlign = Paint.Align.CENTER
        }
        val elapsedText = if (isAr) "قراءة مباشرة من السيارة" else "Lecture directe sur la voiture"
        canvas.drawText(elapsedText, cx, cy + 62f, stepSubPaint)

        y = cy + radius + 56f

        // Current plain-language step (1 sentence, large & calm, zero command/protocol codes)
        val currentStepTitle = if (isAr) {
            "التحقق مما إذا تم مسح الأعطال مؤخراً…"
        } else {
            "Vérification d'un effacement récent des défauts…"
        }
        val csH = drawTextBlock(canvas, currentStepTitle, tfBold, 34f, Color.WHITE, pad, y, contentW, isAr, alignCenter = true)
        y += csH + 16f

        val keepRunningHint = if (isAr) {
            "اترك المحرك يعمل. يستغرق الفحص حوالي دقيقة واحدة."
        } else {
            "Laissez le moteur tourner. Le scan dure environ une minute."
        }
        drawTextBlock(canvas, keepRunningHint, tfReg, 22f, COLOR_DARK_MUTED, pad, y, contentW, isAr, alignCenter = true)

        y += 72f

        // Hairline-separated step list on the dark surface (no white cards)
        val steps = if (isAr) {
            listOf(
                Triple("الاتصال بكمبيوتر المحرك", "تم", true),
                Triple("قراءة الأعطال المسجلة في الذاكرة", "تم", true),
                Triple("التحقق مما إذا تم مسح الأعطال مؤخراً", "جاري القراءة…", true),
                Triple("قراءة حرارة المحرك واستقرار الدوران", "التالي", false),
                Triple("فحص البطارية وشحن الدينامو", "التالي", false),
                Triple("قراءة رقم الشاسيه المسجل في الكمبيوتر", "التالي", false)
            )
        } else {
            listOf(
                Triple("Connexion au calculateur moteur", "Terminé", true),
                Triple("Lecture des défauts enregistrés", "Terminé", true),
                Triple("Vérification d'un effacement récent", "En cours…", true),
                Triple("Lecture de la température et du ralenti", "Suivant", false),
                Triple("Vérification de la batterie et de la charge", "Suivant", false),
                Triple("Lecture du numéro de châssis enregistré", "Suivant", false)
            )
        }

        val hairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_INK_HAIRLINE
            strokeWidth = 2f
        }
        for ((idx, item) in steps.withIndex()) {
            canvas.drawLine(pad, y, W - pad, y, hairPaint)
            val isDone = idx < 2
            val isCurrent = idx == 2
            val titleCol = when {
                isCurrent -> Color.WHITE
                isDone -> Color.parseColor("#D0E2DC")
                else -> COLOR_DARK_MUTED
            }
            val statusCol = when {
                isCurrent -> Color.parseColor("#D6E2EE")
                isDone -> COLOR_DARK_MUTED
                else -> Color.parseColor("#5D756D")
            }
            val statusW = 150
            val titleW = contentW - statusW - 16
            val titleX = if (isAr) (pad + statusW + 16f) else pad
            val statusX = if (isAr) pad else (W - pad - statusW)

            drawTextBlock(canvas, item.first, if (isCurrent) tfBold else tfReg, 23f, titleCol, titleX, y + 20f, titleW, isAr)
            drawTextBlock(canvas, item.second, if (isCurrent) tfBold else tfReg, 21f, statusCol, statusX, y + 22f, statusW, isAr)
            y += 76f
        }
        canvas.drawLine(pad, y, W - pad, y, hairPaint)

        return bmp
    }

    private fun renderScanResult(isAr: Boolean, tfReg: Typeface, tfBold: Typeface): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(COLOR_PAPER)

        // Top Instrument Header in Dark Ink (#0E1F1A) housing the Verdict Band & Scope Line
        val darkHeaderH = 388f
        val inkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_INK }
        canvas.drawRect(0f, 0f, W.toFloat(), darkHeaderH, inkPaint)
        drawStatusBar(canvas, darkSurface = true)

        val pad = 36f
        val contentW = (W - pad * 2).toInt()
        var y = 76f

        // Car & Date header line + optional "Add car model" link
        val carHeader = if (isAr) {
            "Peugeot 208 · اليوم 10:42"
        } else {
            "Peugeot 208 · Aujourd'hui 10:42"
        }
        drawTextBlock(canvas, carHeader, tfReg, 21f, COLOR_DARK_MUTED, pad, y, contentW, isAr)
        y += 42f

        // Large Verdict Band (Orange #E08A00: "Some points to check") + settled dial indicator
        val bandH = 132f
        val bandRect = RectF(pad, y, W - pad, y + bandH)
        val orangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_RESULT_ORANGE }
        canvas.drawRoundRect(bandRect, 24f, 24f, orangePaint)

        // Settled dial icon inside the verdict band (start side)
        val dialCx = if (isAr) (W - pad - 64f) else (pad + 64f)
        val dialCy = y + bandH / 2f
        val dialRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(dialCx, dialCy, 34f, dialRing)
        val exclamPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tfBold
            textSize = 40f
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("!", dialCx, dialCy + 14f, exclamPaint)

        val verdictLabel = if (isAr) "بعض النقاط تستوجب التحقق" else "Quelques points à vérifier"
        val verdictSub = if (isAr) {
            "تم رصد عطل واحد مسجل وفولتية شحن ضعيفة قليلاً."
        } else {
            "1 défaut enregistré et une tension de charge un peu faible."
        }
        val vTextX = if (isAr) (pad + 24f) else (pad + 118f)
        val vTextW = contentW - 142
        drawTextBlock(canvas, verdictLabel, tfBold, 31f, Color.WHITE, vTextX, y + 20f, vTextW, isAr)
        drawTextBlock(canvas, verdictSub, tfReg, 21f, Color.WHITE, vTextX, y + 68f, vTextW, isAr)

        y += bandH + 20f

        // Mandatory fixed scope line under the verdict
        val scopeLine = if (isAr) {
            "يغطي هذا الفحص المحرك والإلكترونيات فقط. لا يمكنه رؤية الهيكل أو الدبرياج أو نظام التعليق أو الوسائد الهوائية (Airbag) أو الفرامل (ABS)."
        } else {
            "Ce résultat concerne uniquement le moteur et l'électronique. Le scan ne voit ni la carrosserie, ni l'embrayage, ni la suspension, ni les airbags ou l'ABS."
        }
        drawTextBlock(canvas, scopeLine, tfReg, 19.5f, COLOR_DARK_MUTED, pad, y, contentW, isAr)

        y = darkHeaderH + 20f

        // Result rows on light #F6F7F5 paper separated by #E3E6E2 hairlines (NO individual cards!)
        data class ResultRowSpec(
            val dotColor: Int,
            val statusText: String,
            val title: String,
            val value: String,
            val detail: String? = null
        )

        val rows = if (isAr) {
            listOf(
                ResultRowSpec(
                    COLOR_RESULT_ORANGE,
                    "للمراقبة",
                    "الأعطال المسجلة",
                    "عطل واحد مسجل",
                    "ضعف كفاءة علبة البيئة (الكاتاليزور) · P0420"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "طبيعي",
                    "هل تم مسح الأعطال مؤخراً؟",
                    "لا · آخر مسح قبل 62,344 كم"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "طبيعي",
                    "لمبة تحذير المحرك",
                    "منطفئة (تضيء بشكل سليم عند فتح الكونتاكت)"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "طبيعي",
                    "حرارة المحرك",
                    "91 °C"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "طبيعي",
                    "استقرار دوران المحرك",
                    "مستقر · 780 دورة/د"
                ),
                ResultRowSpec(
                    COLOR_RESULT_ORANGE,
                    "للمراقبة",
                    "البطارية والشحن",
                    "13.3 V · شحن ضعيف قليلاً"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "طبيعي",
                    "خليط الوقود والهواء",
                    "طبيعي"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "طبيعي",
                    "رقم الشاسيه في الكمبيوتر",
                    "VF3CC8HR0JW049281 · مطابق"
                )
            )
        } else {
            listOf(
                ResultRowSpec(
                    COLOR_RESULT_ORANGE,
                    "À vérifier",
                    "Défauts enregistrés",
                    "1 défaut enregistré",
                    "Rendement catalyseur faible · P0420"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "Normal",
                    "Défauts effacés récemment ?",
                    "Non · Dernier effacement il y a 62 344 km"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "Normal",
                    "Voyant moteur",
                    "Éteint (s'allume normalement au contact)"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "Normal",
                    "Température du moteur",
                    "91 °C"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "Normal",
                    "Ralenti du moteur",
                    "Stable · 780 tr/min"
                ),
                ResultRowSpec(
                    COLOR_RESULT_ORANGE,
                    "À vérifier",
                    "Batterie et charge",
                    "13,3 V · Charge un peu faible"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "Normal",
                    "Mélange carburant",
                    "Normal"
                ),
                ResultRowSpec(
                    COLOR_RESULT_GREEN,
                    "Normal",
                    "Numéro de châssis (calculateur)",
                    "VF3CC8HR0JW049281 · Identique"
                )
            )
        }

        val hairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_HAIRLINE
            strokeWidth = 2f
        }
        val explainBtnText = if (isAr) "اشرح لي" else "Expliquer"

        for (r in rows) {
            val rowH = if (r.detail != null) 114f else 84f
            // Colored status dot + status label + title on start side; value + Explain pill on end side
            val dotX = if (isAr) (W - pad - 10f) else (pad + 10f)
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = r.dotColor }
            canvas.drawCircle(dotX, y + 24f, 7f, dotPaint)

            // Explain button on the far end side
            val expW = 112f
            val expX = if (isAr) pad else (W - pad - expW)
            val expRect = RectF(expX, y + 14f, expX + expW, y + 58f)
            val expStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_HAIRLINE
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRoundRect(expRect, 14f, 14f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_SURFACE })
            canvas.drawRoundRect(expRect, 14f, 14f, expStroke)
            drawTextBlock(canvas, explainBtnText, tfBold, 18.5f, COLOR_BRAND, expX + 8f, y + 22f, (expW - 16f).toInt(), isAr, alignCenter = true)

            // Main row text block
            val textX = if (isAr) (pad + expW + 18f) else (pad + 28f)
            val textW = (contentW - expW - 46f).toInt()

            val headerLine = "${r.title}  ·  ${r.statusText}"
            drawTextBlock(canvas, headerLine, tfReg, 20f, COLOR_MUTED_TEXT, textX, y + 6f, textW, isAr)
            drawTextBlock(canvas, r.value, tfBold, 24f, COLOR_INK, textX, y + 36f, textW, isAr)
            if (r.detail != null) {
                drawTextBlock(canvas, r.detail, tfReg, 20f, r.dotColor, textX, y + 70f, textW, isAr)
            }

            y += rowH
            canvas.drawLine(pad, y, W - pad, y, hairPaint)
        }

        // Collapsed "Details for a mechanic" hairline row
        val mechRow = if (isAr) "تفاصيل للميكانيكي (الأكواد الخام، الحساسات، البروتوكول)  ▾" else "Détails pour un mécanicien (codes bruts, sondes, protocole)  ▾"
        drawTextBlock(canvas, mechRow, tfBold, 20f, COLOR_MUTED_TEXT, pad, y + 14f, contentW, isAr)
        y += 58f

        // Bottom 3 Action Buttons (Part 4 specification)
        // 1. Primary highlighted button: Add a 3-minute visual check (56dp+ touch target)
        val btn1H = 96f
        val btn1Rect = RectF(pad, y, W - pad, y + btn1H)
        canvas.drawRoundRect(btn1Rect, 24f, 24f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_BRAND })
        val btn1Title = if (isAr) "إضافة فحص بصري في 3 دقائق (10 نقاط)" else "Ajouter un contrôle visuel de 3 minutes"
        val btn1Sub = if (isAr) "الفحص الإلكتروني لا يرى هذه النقاط العشر. افحصها بنفسك." else "Le scan ne voit pas ces dix points. Vérifiez-les vous-même."
        drawTextBlock(canvas, btn1Title, tfBold, 24f, Color.WHITE, pad + 20f, y + 14f, contentW - 40, isAr, alignCenter = true)
        drawTextBlock(canvas, btn1Sub, tfReg, 18.5f, Color.parseColor("#D7EFE7"), pad + 20f, y + 52f, contentW - 40, isAr, alignCenter = true)

        y += btn1H + 16f

        // 2. Secondary & 3. Tertiary buttons side by side or stacked cleanly
        val halfW = (contentW - 16) / 2f
        val leftX = if (isAr) (pad + halfW + 16f) else pad
        val rightX = if (isAr) pad else (pad + halfW + 16f)

        val btn2Rect = RectF(leftX, y, leftX + halfW, y + 68f)
        canvas.drawRoundRect(btn2Rect, 18f, 18f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_SURFACE })
        canvas.drawRoundRect(btn2Rect, 18f, 18f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_INK
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        })
        val btn2Text = if (isAr) "حفظ واستخراج التقرير" else "Enregistrer et rapport"
        drawTextBlock(canvas, btn2Text, tfBold, 20f, COLOR_INK, leftX + 10f, y + 18f, (halfW - 20f).toInt(), isAr, alignCenter = true)

        val btn3Rect = RectF(rightX, y, rightX + halfW, y + 68f)
        val btn3Text = if (isAr) "الفحص اليدوي الكامل" else "Inspection manuelle complète"
        drawTextBlock(canvas, btn3Text, tfBold, 19.5f, COLOR_MUTED_TEXT, rightX + 10f, y + 18f, (halfW - 20f).toInt(), isAr, alignCenter = true)

        return bmp
    }
}
