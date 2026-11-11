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
    composeTestRule.onNodeWithTag("onboarding_tab_privacy").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("finish_onboarding_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("start_inspection_button").assertExists()
    composeTestRule.onNodeWithTag("start_inspection_button").performClick()
    composeTestRule.waitForIdle()
  }
}
