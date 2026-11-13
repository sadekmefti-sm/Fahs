package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.data.model.AlgerianCarCatalog
import com.example.data.model.AlgerianCarCatalogExtra
import com.example.domain.ChecklistEngine
import com.example.domain.ObdDiagnosticEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Fahs", appName)
  }

  @Test
  fun `verify arabic language rtl configuration`() {
    assertTrue(AppLanguage.AR.isRtl)
    assertEquals("فحص", Strings.appName(AppLanguage.AR))
    assertEquals("Fahs", Strings.appName(AppLanguage.FR))
  }

  @Test
  fun `verify phase 8 catalog fuzzy search checklist and dtc dictionary`() {
    // 1. Verify expanded Algerian catalog (28 models across 14 brands)
    assertTrue(AlgerianCarCatalog.models.size >= 28)

    // 2. Verify Arabic & typo-tolerant fuzzy search
    val symbol = AlgerianCarCatalog.models.first { it.id == "renault_symbol" }
    val accent = AlgerianCarCatalog.models.first { it.id == "hyundai_accent" }
    val golf = AlgerianCarCatalog.models.first { it.id == "vw_golf7" }
    assertTrue(AlgerianCarCatalogExtra.matchesFuzzySearch(symbol, "سامبول"))
    assertTrue(AlgerianCarCatalogExtra.matchesFuzzySearch(symbol, "symbole"))
    assertTrue(AlgerianCarCatalogExtra.matchesFuzzySearch(accent, "اكصنت"))
    assertTrue(AlgerianCarCatalogExtra.matchesFuzzySearch(golf, "غولف"))

    // 3. Verify adaptive checklist includes new Phase 8 weak points
    val golfTdiChecklist = ChecklistEngine.buildChecklistForCar(
      brand = "Volkswagen",
      model = "Golf 7",
      fuelType = "DIESEL",
      gearboxType = "AUTOMATIQUE (DSG)",
      isQuickMode = false
    )
    assertTrue(golfTdiChecklist.any { it.id == "weakpoint_vag_tdi_waterpump_flywheel" })
    assertTrue(golfTdiChecklist.any { it.id == "int_sunroof_panoramic_check" })

    val symbolGplChecklist = ChecklistEngine.buildChecklistForCar(
      brand = "Renault",
      model = "Symbol",
      fuelType = "GPL",
      gearboxType = "MANUELLE",
      isQuickMode = false
    )
    assertTrue(symbolGplChecklist.any { it.id == "weakpoint_gpl_valve_clearance" })

    // 4. Verify expanded trilingual OBD2 DTC dictionary
    assertTrue(ObdDiagnosticEngine.offlineDtcDatabase.size >= 29)
    val dsgFault = ObdDiagnosticEngine.lookupDtc("P17BF")
    assertNotNull(dsgFault)
    assertTrue(dsgFault.titleFr.contains("DSG7"))
    assertTrue(dsgFault.titleAr.contains("DSG7"))

    // 5. Phase 1 checks: unknown DTC has 0 cost, empty checklist has null score ("INCOMPLETE"), simulated scan is flagged
    val unknownDtc = ObdDiagnosticEngine.lookupDtc("P9999")
    assertEquals(0, unknownDtc.minCostDzd)
    assertEquals(0, unknownDtc.maxCostDzd)

    val (emptyScore, emptyVerdict, _) = ChecklistEngine.calculateScoreAndVerdict(
      answers = emptyMap(),
      items = golfTdiChecklist
    )
    assertEquals(null, emptyScore)
    assertEquals("INCOMPLETE", emptyVerdict)

    val simScan = ObdDiagnosticEngine.generateSimulatedScan(
      com.example.domain.ObdSimulatorProfile.HEALTHY,
      ""
    )
    assertTrue(simScan.isSimulated)
  }

  @Test
  fun `launch MainActivity and navigate tabs without crashing`() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("first_launch_welcome_card").assertExists()
    composeTestRule.onNodeWithTag("top_bar_info_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("onboarding_release_dialog").assertExists()
    composeTestRule.onNodeWithTag("onboarding_tab_dongle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dongle_shop_order_card").assertExists()
    composeTestRule.onNodeWithTag("delivery_mode_home").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("onboarding_tab_privacy").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("finish_onboarding_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("start_inspection_button").assertExists()
    composeTestRule.onNodeWithTag("start_inspection_button").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `verify phase 6 shop settings bundled json and 58 wilaya order calculator`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    // 1. Default Kotlin constructor has no hardcoded phone/email/price
    val blankSettings = com.example.data.repository.ShopSettings()
    assertEquals("", blankSettings.whatsappNumber)
    assertEquals("", blankSettings.contactEmail)
    assertEquals(0, blankSettings.donglePriceDzd)

    // 2. Bundled JSON loads all 58 wilayas and configured shop values
    val repo = com.example.data.repository.ShopSettingsRepository(context)
    val bundled = repo.loadBundledDefault()
    assertTrue(bundled.isShopConfiguredAndEnabled())
    assertEquals("+971524830059", bundled.whatsappNumber)
    assertEquals("sadekmefti@gmail.com", bundled.contactEmail)
    assertEquals(2500, bundled.donglePriceDzd)
    assertTrue(bundled.inStock)
    assertEquals(58, bundled.deliveryFees.size)

    // 3. Verify Algiers (16) and Oran (31) delivery calculations
    val algiers = bundled.deliveryFees.first { it.wilayaCode == "16" }
    assertEquals(2500 + algiers.stopDeskFeeDzd, bundled.calculateOrderTotalDzd(algiers, isHomeDelivery = false, quantity = 1))
    assertEquals(2500 * 2 + algiers.homeFeeDzd, bundled.calculateOrderTotalDzd(algiers, isHomeDelivery = true, quantity = 2))

    val msgFr = bundled.buildOrderMessage(
      language = AppLanguage.FR,
      wilaya = algiers,
      isHomeDelivery = true,
      quantity = 1,
      customerName = "Karim",
      customerPhone = "0550123456",
      addressNote = "Bab Ezzouar"
    )
    assertTrue(msgFr.contains("16 - Alger"))
    assertTrue(msgFr.contains("Livraison à domicile"))
    assertTrue(msgFr.contains("Karim"))
    assertTrue(bundled.buildWhatsAppUri(msgFr).startsWith("https://wa.me/971524830059?text="))
  }

  @Test
  fun `verify phase 7 cloud backup entity round trip and incomplete score preservation`() {
    val cloudBackup = com.example.data.repository.CloudInspectionBackup(
      id = "insp_10",
      userId = "uid_123",
      localId = 10L,
      carBrand = "Dacia",
      carModel = "Duster",
      carGeneration = "Duster II",
      carYear = 2021L,
      odometerKm = 68000L,
      fuelType = "DIESEL",
      gearboxType = "MANUELLE",
      engineDescription = "1.5 dCi 110ch",
      hasTurbo = true,
      isFourWheelDrive = true,
      bodyType = "SUV",
      vin = "UU1HJD20561928374",
      plateNumber = "12940 121 16",
      inspectionMode = "FULL",
      status = "IN_PROGRESS",
      overallScore = -1L,
      verdict = "INCOMPLETE",
      totalEstimatedCostDzd = 0L,
      problemCount = 0L,
      answeredQuestions = 8L,
      totalQuestions = 35L,
      notes = "En cours"
    )
    val localEntity = cloudBackup.toLocalEntity()
    assertEquals("Dacia", localEntity.carBrand)
    assertEquals("Duster", localEntity.carModel)
    assertEquals(null, localEntity.overallScore)
    assertEquals(null, localEntity.verdict)
    assertTrue(localEntity.hasTurbo)
    assertTrue(localEntity.isFourWheelDrive)
    assertEquals("SUV", localEntity.bodyType)
  }

  @Test
  fun `verify phase 8 professional pdf report generation in 3 languages and render sample previews`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 1. Create a realistic test photo on disk for a problem finding thumbnail
    val photoDir = java.io.File(context.filesDir, "inspection_photos").apply { mkdirs() }
    val testPhotoFile = java.io.File(photoDir, "sample_leak_photo.jpg")
    val sampleBmp = android.graphics.Bitmap.createBitmap(320, 240, android.graphics.Bitmap.Config.ARGB_8888)
    val sampleCanvas = android.graphics.Canvas(sampleBmp)
    val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
      color = android.graphics.Color.rgb(30, 41, 59)
    }
    sampleCanvas.drawRect(0f, 0f, 320f, 240f, bgPaint)
    val highlightPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
      color = android.graphics.Color.rgb(220, 38, 38)
      style = android.graphics.Paint.Style.STROKE
      strokeWidth = 6f
    }
    sampleCanvas.drawCircle(160f, 120f, 58f, highlightPaint)
    val labelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
      color = android.graphics.Color.WHITE
      textSize = 22f
      isFakeBoldText = true
    }
    sampleCanvas.drawText("Carter / Joint", 95f, 128f, labelPaint)
    java.io.FileOutputStream(testPhotoFile).use { out ->
      sampleBmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
    }

    // 2. Build a realistic Peugeot 208 (2018) test inspection with fixed date 2026-11-11
    val fixedDateCal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).apply {
      set(2026, java.util.Calendar.NOVEMBER, 11, 14, 30, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val checklist = ChecklistEngine.buildChecklistForCar(
      brand = "Peugeot",
      model = "208",
      fuelType = "ESSENCE",
      gearboxType = "MANUELLE",
      isQuickMode = false,
      engineDescription = "1.2 PureTech 82ch",
      hasTurbo = false,
      isFourWheelDrive = false,
      bodyType = "CITADINE"
    )

    val answersMap = mutableMapOf<String, String>()
    val savedAnswerEntities = mutableListOf<com.example.data.local.ChecklistAnswerEntity>()

    // Populate realistic answers:
    // - 1 Critical problem (with photo + note)
    // - 2 Important problems (with notes)
    // - 1 Minor problem
    // - 3 Skipped items (1 Critical skipped, 2 Important skipped)
    // - All remaining items PASS
    val criticalProblemItem = checklist.first { it.severity == com.example.data.model.ItemSeverity.CRITICAL }
    val importantProblemItems = checklist.filter { it.severity == com.example.data.model.ItemSeverity.IMPORTANT }.take(2)
    val minorProblemItem = checklist.firstOrNull { it.severity == com.example.data.model.ItemSeverity.MINOR }
      ?: checklist.filter { it.severity == com.example.data.model.ItemSeverity.IMPORTANT }.drop(2).first()
    val skippedCriticalItem = checklist.filter {
      it.severity == com.example.data.model.ItemSeverity.CRITICAL && it.id != criticalProblemItem.id
    }.first()
    val skippedImportantItems = checklist.filter {
      it.severity == com.example.data.model.ItemSeverity.IMPORTANT &&
        it.id !in importantProblemItems.map { p -> p.id } &&
        it.id != minorProblemItem.id
    }.take(2)

    for (item in checklist) {
      val status = when {
        item.id == criticalProblemItem.id -> "PROBLEM"
        item.id in importantProblemItems.map { it.id } -> "PROBLEM"
        item.id == minorProblemItem.id -> "PROBLEM"
        item.id == skippedCriticalItem.id -> "UNCHECKED"
        item.id in skippedImportantItems.map { it.id } -> "UNCHECKED"
        else -> "GOOD"
      }
      if (status != "UNCHECKED") {
        answersMap[item.id] = status
      }
      val comment = when (item.id) {
        criticalProblemItem.id -> "Suintement d'huile visible + courroie humide à vérifier d'urgence"
        importantProblemItems[0].id -> "Usure prononcée à l'avant (DOT 1419), prévoir remplacement"
        importantProblemItems[1].id -> "Légère vibration au freinage à 90 km/h (disques avant)"
        minorProblemItem.id -> "Rayure superficielle pare-chocs arrière droit"
        else -> ""
      }
      val photoPath = if (item.id == criticalProblemItem.id) testPhotoFile.absolutePath else null
      savedAnswerEntities.add(
        com.example.data.local.ChecklistAnswerEntity(
          inspectionId = 42L,
          itemId = item.id,
          category = item.section.titleFr,
          questionFr = item.questionFr,
          questionAr = item.questionAr,
          questionEn = item.questionEn,
          status = status,
          severity = item.severity.name,
          estimatedCostDzd = if (status == "PROBLEM") item.getAverageCostDzd() else 0,
          note = comment,
          photoUri = photoPath
        )
      )
    }

    val (score, verdict, totalCost) = ChecklistEngine.calculateScoreAndVerdict(answersMap, checklist)
    val obdNotes = buildString {
      append("Vendeur particulier à Bab Ezzouar. Carnet d'entretien partiel.\n")
      append("[OBD2 Scan: 2 codes | P0420, P0299 | Readiness: ATTENTION: Codes effacés récemment (18 km depuis effacement)]\n")
      append("[OBD2 Live: rpm=780 tr/min|680 - 900 tr/min|GREEN;temp=91 °C|80 - 98 °C|GREEN;volt=13.3 V|13.5 - 14.7 V|ORANGE;trim=+9.4 %|-10% .. +10%|ORANGE]")
    }

    val testInspection = com.example.data.local.InspectionEntity(
      id = 42L,
      carBrand = "Peugeot",
      carModel = "208",
      carGeneration = "Phase 1 (2012-2019)",
      carYear = 2018,
      odometerKm = 142000,
      fuelType = "ESSENCE",
      gearboxType = "MANUELLE",
      engineDescription = "1.2 PureTech 82ch",
      hasTurbo = false,
      isFourWheelDrive = false,
      bodyType = "CITADINE",
      vin = "VF3CC8HR0JW049281",
      plateNumber = "11845-118-16",
      inspectionMode = "FULL",
      status = "COMPLETED",
      overallScore = score,
      verdict = verdict,
      totalEstimatedCostDzd = totalCost,
      sellerAskingPriceMillions = 235.0,
      userQuotedRepairDzd = 65000,
      hasObdScan = true,
      obdCommunicationSuccess = true,
      obdProtocolDescription = "ISO 15765-4 CAN (11 bit, 500 kbps)",
      obdDtcCodesSummary = "P0420 (STORED), P0299 (STORED)",
      obdDtcCount = 2,
      obdClearedStatus = "STRONG_WARNING",
      obdDistanceSinceClearedKm = 18,
      obdWarmupsSinceCleared = 1,
      obdMilBulbLitAtKeyOn = true,
      obdMilCommandedByEcu = "ON",
      obdEcuVin = "VF3CC8HR0JW049281",
      obdEcuVinComparison = "SAME",
      problemCount = 4,
      answeredQuestions = checklist.size - 3,
      totalQuestions = checklist.size,
      createdAt = fixedDateCal.timeInMillis,
      updatedAt = fixedDateCal.timeInMillis,
      notes = obdNotes
    )

    // 3. Verify suggested filename and SAF intent
    val suggestedFileName = com.example.domain.PdfReportGenerator.buildSuggestedFileName(testInspection)
    assertEquals("Fahs_Peugeot_208_2018_2026-11-11.pdf", suggestedFileName)

    val downloadIntent = com.example.domain.PdfReportGenerator.createDownloadDocumentIntent(suggestedFileName)
    assertEquals(android.content.Intent.ACTION_CREATE_DOCUMENT, downloadIntent.action)
    assertEquals("application/pdf", downloadIntent.type)
    assertEquals(suggestedFileName, downloadIntent.getStringExtra(android.content.Intent.EXTRA_TITLE))

    // 4. Build report data and verify Phase 8 structures
    val reportData = com.example.domain.ReportEngine.buildReport(testInspection, savedAnswerEntities)
    assertTrue(reportData.areaScores.isNotEmpty())
    assertTrue(reportData.obdBlock.isScanned)
    assertEquals(2, reportData.obdBlock.faultCodeDetails.size)
    assertEquals("P0420", reportData.obdBlock.faultCodeDetails.first().code)
    assertEquals(4, reportData.obdBlock.liveReadings.size)

    // 5. Generate PDF reports and high-resolution page preview PNGs in all 3 languages (EN, FR, AR)
    val outputDir = java.io.File("build/reports/phase8_samples").apply { mkdirs() }
    val artifactDir = java.io.File("/.aistudio/artifacts/brain/6b4ac22d-0983-4414-b677-09169a0178c4").apply { mkdirs() }
    for (lang in listOf(AppLanguage.EN, AppLanguage.FR, AppLanguage.AR)) {
      val pdfFile = com.example.domain.PdfReportGenerator.generatePdfFile(
        context = context,
        report = reportData,
        language = lang,
        sellerAskingMillions = 235.0,
        mechanicQuotedRepairDzd = 65000
      )
      assertTrue(pdfFile.exists())
      assertTrue("PDF should be compact enough for WhatsApp (< 500 KB)", pdfFile.length() < 500_000L)

      val langPdfName = "Fahs_Peugeot_208_2018_2026-11-11_${lang.code.uppercase()}.pdf"
      pdfFile.copyTo(java.io.File(outputDir, langPdfName), overwrite = true)
      runCatching { pdfFile.copyTo(java.io.File(artifactDir, langPdfName), overwrite = true) }

      val pageBitmaps = com.example.domain.PdfReportGenerator.renderReportPagesToBitmaps(
        context = context,
        report = reportData,
        language = lang,
        sellerAskingMillions = 235.0,
        mechanicQuotedRepairDzd = 65000,
        scaleFactor = 2f
      )
      assertTrue(pageBitmaps.isNotEmpty())
      pageBitmaps.forEachIndexed { idx, bmp ->
        val pngName = "sample_report_${lang.code}_page${idx + 1}.png"
        val pngFile = java.io.File(outputDir, pngName)
        java.io.FileOutputStream(pngFile).use { out ->
          bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 95, out)
        }
        runCatching { pngFile.copyTo(java.io.File(artifactDir, pngName), overwrite = true) }
      }
    }
  }
}
