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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
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
}
