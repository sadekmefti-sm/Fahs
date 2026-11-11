package com.example

import com.example.core.i18n.AppLanguage
import com.example.data.local.ChecklistAnswerEntity
import com.example.data.local.InspectionEntity
import com.example.data.model.ChecklistSection
import com.example.domain.ChecklistEngine
import com.example.domain.ObdDiagnosticEngine
import com.example.domain.ObdSimulatorProfile
import com.example.domain.ReportEngine
import com.example.domain.VinAndPlateEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `checklist library has at least 30 items ordered by walkaround sections`() {
        val library = ChecklistEngine.fullLibrary
        assertTrue("Library should have at least 30 items, found ${library.size}", library.size >= 30)
        val sections = library.map { it.section }.distinct()
        assertEquals(9, sections.size)
        assertEquals(ChecklistSection.BEFORE_VISIT, sections.first())
        assertEquals(ChecklistSection.AFTER_DRIVE, sections.last())
    }

    @Test
    fun `diesel manual car gets diesel and manual clutch items but no GPL or automatic items`() {
        val items = ChecklistEngine.buildChecklistForCar(
            brand = "Renault",
            model = "Clio 4",
            fuelType = "DIESEL",
            gearboxType = "MANUELLE",
            isQuickMode = false
        )
        val ids = items.map { it.id }.toSet()
        assertTrue(ids.contains("start_diesel_injectors_turbo"))
        assertTrue(ids.contains("bonnet_renault_dci_turbo_oil"))
        assertTrue(ids.contains("drive_manual_clutch_slip"))
        assertFalse(ids.contains("doc_gpl_homologation"))
        assertFalse(ids.contains("bonnet_gpl_vaporizer_pipes"))
        assertFalse(ids.contains("drive_auto_gearbox_shifts"))
    }

    @Test
    fun `gpl automatic car gets GPL and automatic gearbox checks`() {
        val items = ChecklistEngine.buildChecklistForCar(
            brand = "Kia",
            model = "Picanto",
            fuelType = "GPL",
            gearboxType = "AUTOMATIQUE",
            isQuickMode = false
        )
        val ids = items.map { it.id }.toSet()
        assertTrue(ids.contains("doc_gpl_homologation"))
        assertTrue(ids.contains("bonnet_gpl_vaporizer_pipes"))
        assertTrue(ids.contains("start_gpl_switchover"))
        assertTrue(ids.contains("drive_auto_gearbox_shifts"))
        assertFalse(ids.contains("drive_manual_clutch_slip"))
        assertFalse(ids.contains("start_diesel_injectors_turbo"))
    }

    @Test
    fun `peugeot 208 essence gets puretech timing belt weak point check`() {
        val items = ChecklistEngine.buildChecklistForCar(
            brand = "Peugeot",
            model = "208",
            fuelType = "ESSENCE",
            gearboxType = "MANUELLE",
            isQuickMode = true
        )
        val ids = items.map { it.id }.toSet()
        assertTrue(ids.contains("bonnet_peugeot_puretech_belt"))
    }

    @Test
    fun `critical finding caps score below 50 and triggers AVOID verdict`() {
        val items = ChecklistEngine.buildChecklistForCar(
            brand = "Renault",
            model = "Symbol",
            fuelType = "ESSENCE",
            gearboxType = "MANUELLE",
            isQuickMode = true
        )
        val answers = items.associate { item ->
            item.id to if (item.id == "bonnet_chassis_longerons") "PROBLEM" else "GOOD"
        }
        val (score, verdict, totalCostDzd) = ChecklistEngine.calculateScoreAndVerdict(answers, items)
        assertTrue("Critical problem must cap score <= 42", score <= 42)
        assertEquals("AVOID", verdict)
        assertTrue("Estimated DZD cost should be > 0", totalCostDzd > 0)
    }

    @Test
    fun `vin validator rejects forbidden I O Q characters and decodes WMI`() {
        val invalidVin = VinAndPlateEngine.analyzeVin("VF15RJLOA5849201I")
        assertFalse(invalidVin.isFormatValid)
        assertTrue(invalidVin.forbiddenCharactersFound.containsAll(listOf('O', 'I')))

        val validVwVin = VinAndPlateEngine.analyzeVin("WVWZZZAUZJW123456")
        assertTrue(validVwVin.isFormatValid)
        assertEquals("Volkswagen", validVwVin.manufacturer)
        assertEquals(2018, validVwVin.decodedYear)
    }

    @Test
    fun `algerian plate parser decodes serial, category, year and 58 wilayas`() {
        val plateAlgiers = VinAndPlateEngine.parseAlgerianPlate("04821 118 16")
        assertTrue(plateAlgiers.isValidFormat)
        assertEquals("04821", plateAlgiers.registrationSerial)
        assertEquals(1, plateAlgiers.vehicleCategoryCode)
        assertEquals(2018, plateAlgiers.registrationYear)
        assertEquals(16, plateAlgiers.wilayaCode)
        assertEquals("Alger", plateAlgiers.wilayaNameFr)

        val plateProvisional = VinAndPlateEngine.parseAlgerianPlate("12904-100-31")
        assertTrue(plateProvisional.isValidFormat)
        assertTrue(plateProvisional.isProvisionalOrTransit)
        assertEquals(31, plateProvisional.wilayaCode)
        assertEquals("Oran", plateProvisional.wilayaNameFr)
    }

    @Test
    fun `obd mode 03 hex parser decodes P0299 P0301 and P0420 accurately`() {
        val rawHex = "43 03 02 99 03 01 04 20"
        val dtcs = ObdDiagnosticEngine.parseMode03DtcResponse(rawHex)
        assertEquals(3, dtcs.size)
        assertEquals(listOf("P0299", "P0301", "P0420"), dtcs.map { it.code })
    }

    @Test
    fun `obd pid 0101 and distance parsers detect recently cleared fraud and MIL status`() {
        // 0x83 = MIL ON (0x80) + 3 stored DTCs
        val (milOn, dtcCount, _) = ObdDiagnosticEngine.parsePid01Status("41 01 83 07 FF 00")
        assertTrue(milOn)
        assertEquals(3, dtcCount)

        // PID 31 distance since codes cleared: 0x00 0x06 = 6 km
        val distKm = ObdDiagnosticEngine.parseTwoByteDistanceKm("41 31 00 06", "31")
        assertEquals(6, distKm)

        // 0 stored codes + 6 km since clear + 1 warmup + 5 incomplete monitors => FRAUD ALERT
        assertTrue(
            ObdDiagnosticEngine.detectClearedCodeFraud(
                storedDtcCount = 0,
                distanceSinceClearKm = 6,
                warmupsSinceClear = 1,
                incompleteMonitors = 5
            )
        )

        // Healthy car with 4820 km since clear => NO fraud alert
        assertFalse(
            ObdDiagnosticEngine.detectClearedCodeFraud(
                storedDtcCount = 0,
                distanceSinceClearKm = 4820,
                warmupsSinceClear = 142,
                incompleteMonitors = 0
            )
        )
    }

    @Test
    fun `obd simulator generates all 4 profiles accurately`() {
        val healthy = ObdDiagnosticEngine.generateSimulatedScan(ObdSimulatorProfile.HEALTHY, "VF15RJL0A58492011")
        assertTrue(healthy.isCommunicationSuccess)
        assertFalse(healthy.isRecentlyClearedFraudAlert)
        assertTrue(healthy.dtcList.isEmpty())

        val clearedFraud = ObdDiagnosticEngine.generateSimulatedScan(ObdSimulatorProfile.RECENTLY_CLEARED_FRAUD, "VF15RJL0A58492011")
        assertTrue(clearedFraud.isCommunicationSuccess)
        assertTrue(clearedFraud.isRecentlyClearedFraudAlert)
        assertEquals(6, clearedFraud.distanceSinceCodesClearedKm)

        val noEcu = ObdDiagnosticEngine.generateSimulatedScan(ObdSimulatorProfile.NO_ECU_RESPONSE, "VF15RJL0A58492011")
        assertFalse(noEcu.isCommunicationSuccess)
    }

    @Test
    fun `report engine calculates negotiation offer in Millions de centimes and caps critical defects`() {
        val inspection = InspectionEntity(
            id = 10L,
            carBrand = "Renault",
            carModel = "Clio 4",
            carGeneration = "Phase 2",
            carYear = 2018,
            fuelType = "DIESEL",
            gearboxType = "MANUELLE",
            vin = "VF15RJL0A58492011",
            plateNumber = "04821 118 16",
            inspectionMode = "QUICK"
        )
        val answers = listOf(
            ChecklistAnswerEntity(
                inspectionId = 10L,
                itemId = "bonnet_chassis_longerons",
                category = "UNDER_BONNET_COLD",
                questionFr = "Longerons",
                questionAr = "اللونجرون",
                questionEn = "Chassis rails",
                status = "PROBLEM",
                severity = "CRITICAL",
                estimatedCostDzd = 120000
            )
        )
        val report = ReportEngine.buildReport(inspection, answers)
        assertTrue(report.overallScore <= 42)
        assertEquals("AVOID", report.verdict)
        assertEquals(1, report.criticalProblems.size)

        // 240 Millions asking price (2,400,000 DZD) minus 50,000 to 100,000 DZD repairs -> 2,300,000 to 2,350,000 DZD
        val (minOffer, maxOffer) = ReportEngine.calculateFairOfferRangeDzd(240.0, 50000, 100000)
        assertEquals(2300000, minOffer)
        assertEquals(2350000, maxOffer)
        assertEquals("230 Millions", ReportEngine.formatMillionsCentimes(minOffer, AppLanguage.FR))
    }
}
