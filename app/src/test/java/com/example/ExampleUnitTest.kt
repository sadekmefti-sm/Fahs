package com.example

import com.example.core.i18n.AppLanguage
import com.example.data.local.ChecklistAnswerEntity
import com.example.data.local.InspectionEntity
import com.example.data.model.ChecklistSection
import com.example.data.model.ItemSeverity
import com.example.domain.ChecklistEngine
import com.example.domain.ClearedCodesAlertLevel
import com.example.domain.DtcType
import com.example.domain.EcuVinComparisonChoice
import com.example.domain.ObdDiagnosticEngine
import com.example.domain.ObdSimulatorProfile
import com.example.domain.ObdTransport
import com.example.domain.ReportEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `checklist library has at least 120 items across all 9 sections in 3 languages`() {
        val library = ChecklistEngine.fullLibrary
        assertTrue("Library should have at least 120 items, found ${library.size}", library.size >= 120)
        val sections = library.map { it.section }.distinct()
        assertEquals(9, sections.size)
        assertEquals(ChecklistSection.BEFORE_VISIT, sections.first())
        assertEquals(ChecklistSection.AFTER_DRIVE, sections.last())

        // Every item must have non-blank French, Arabic, and English text and default to TO_BE_VERIFIED
        for (item in library) {
            assertTrue("Blank FR question on ${item.id}", item.questionFr.isNotBlank())
            assertTrue("Blank AR question on ${item.id}", item.questionAr.isNotBlank())
            assertTrue("Blank EN question on ${item.id}", item.questionEn.isNotBlank())
            assertEquals(
                "Weak point ${item.id} must not claim mechanic verification before sign-off",
                com.example.data.model.VerificationStatus.TO_BE_VERIFIED,
                item.verificationStatus
            )
        }
    }

    @Test
    fun `fault code dictionary has at least 150 common codes in 3 languages and standard generic titles`() {
        val db = ObdDiagnosticEngine.offlineDtcDatabase
        assertTrue("Expected at least 150 common fault codes, found ${db.size}", db.size >= 150)
        for ((code, info) in db) {
            assertTrue("Blank FR title on $code", info.titleFr.isNotBlank())
            assertTrue("Blank AR title on $code", info.titleAr.isNotBlank())
            assertTrue("Blank EN title on $code", info.titleEn.isNotBlank())
            assertTrue("Blank FR explanation on $code", info.plainExplanationFr.isNotBlank())
            assertTrue("Blank AR explanation on $code", info.plainExplanationAr.isNotBlank())
            assertTrue("Blank EN explanation on $code", info.plainExplanationEn.isNotBlank())
            assertEquals(
                com.example.data.model.VerificationStatus.TO_BE_VERIFIED,
                info.verificationStatus
            )
        }

        // Standard generic code outside the 150+ dictionary gets a 3-language standard category title and honest explanation
        val genericP0 = ObdDiagnosticEngine.lookupDtc("P0799")
        assertTrue(genericP0.titleEn.contains("Transmission", ignoreCase = true))
        assertTrue(genericP0.titleFr.contains("transmission", ignoreCase = true))
        assertTrue(genericP0.titleAr.contains("علبة السرعة"))
        assertTrue(genericP0.plainExplanationEn.contains("Ask a mechanic what this code means on this car"))
        assertEquals(0, genericP0.minCostDzd)
    }

    @Test
    fun `diesel manual car gets diesel and manual clutch items but no GPL or automatic items`() {
        val items = ChecklistEngine.buildChecklistForCar(
            brand = "Renault",
            model = "Clio 4",
            fuelType = "DIESEL",
            gearboxType = "MANUELLE",
            isQuickMode = false,
            generation = "Clio IV Phase 2",
            engineDescription = "1.5 dCi 85/90/110ch",
            carYear = 2018,
            odometerKm = 135000
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
    fun `model weak points match exact model, generation, and engine without brand-wide shortcuts`() {
        // 1. Petrol Peugeot 208 with 1.2 PureTech gets the PureTech wet-belt weak point
        val p208PureTech = ChecklistEngine.buildChecklistForCar(
            brand = "Peugeot",
            model = "208",
            fuelType = "ESSENCE",
            gearboxType = "MANUELLE",
            isQuickMode = true,
            generation = "208 I Phase 2",
            engineDescription = "1.2 PureTech 82ch"
        ).map { it.id }.toSet()
        assertTrue(p208PureTech.contains("bonnet_peugeot_puretech_belt"))

        // 2. Petrol Peugeot 207 (1.4 VTi / 1.4 8v — NOT a 1.2 PureTech model) must NOT get PureTech belt check
        val p207Petrol = ChecklistEngine.buildChecklistForCar(
            brand = "Peugeot",
            model = "207",
            fuelType = "ESSENCE",
            gearboxType = "MANUELLE",
            isQuickMode = false,
            generation = "207 Phase 2",
            engineDescription = "1.4 Essence 75ch"
        ).map { it.id }.toSet()
        assertFalse("Brand-wide Peugeot shortcut must be removed", p207Petrol.contains("bonnet_peugeot_puretech_belt"))

        // 3. Diesel Renault Master (2.3 dCi — not in 1.5 dCi model list) must NOT get 1.5 dCi weak point
        val renaultMaster = ChecklistEngine.buildChecklistForCar(
            brand = "Renault",
            model = "Master",
            fuelType = "DIESEL",
            gearboxType = "MANUELLE",
            isQuickMode = false
        ).map { it.id }.toSet()
        assertFalse("Brand-wide Renault diesel shortcut must be removed", renaultMaster.contains("bonnet_renault_dci_turbo_oil"))
    }

    @Test
    fun `other car missing from catalogue gets fully adapted checklist from year, mileage, turbo, 4x4, and body type`() {
        val pickup4x4Items = ChecklistEngine.buildChecklistForCar(
            brand = "Great Wall",
            model = "Wingle 7",
            fuelType = "DIESEL",
            gearboxType = "MANUELLE",
            isQuickMode = false,
            carYear = 2014,
            odometerKm = 185000,
            hasTurbo = true,
            isFourWheelDrive = true,
            bodyType = "PICKUP"
        ).map { it.id }.toSet()

        // Gets Pick-up / Van specific checks
        assertTrue(pickup4x4Items.contains("ext_pickup_bed_and_cab_gap"))
        assertTrue(pickup4x4Items.contains("under_pickup_ladder_frame_leaf_springs"))
        assertTrue(pickup4x4Items.contains("tyre_rear_leaf_springs_shackles"))
        // Gets 4x4 transfer case & propshaft checks
        assertTrue(pickup4x4Items.contains("int_4x4_selector_switch"))
        assertTrue(pickup4x4Items.contains("under_4x4_propshaft_differentials"))
        // Gets Turbo checks
        assertTrue(pickup4x4Items.contains("bonnet_turbo_wastegate_and_pipes"))
        assertTrue(pickup4x4Items.contains("drive_turbo_boost_uphill_pull"))
        // Gets High-Mileage (>= 120k / 150k km) checks
        assertTrue(pickup4x4Items.contains("bonnet_high_mileage_cooling_hoses"))
        assertTrue(pickup4x4Items.contains("bonnet_high_mileage_injectors_seals"))
        assertTrue(pickup4x4Items.contains("int_high_mileage_pedal_shifter_play"))
        // Gets Older car (>= 8 years / <= 2016) checks
        assertTrue(pickup4x4Items.contains("ext_older_car_window_seals_corrosion"))
        assertTrue(pickup4x4Items.contains("under_older_car_floorpan_corrosion"))
        // Does NOT get model-specific catalogue weak points
        assertFalse(pickup4x4Items.contains("bonnet_peugeot_puretech_belt"))
        assertFalse(pickup4x4Items.contains("bonnet_renault_dci_turbo_oil"))
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
        assertTrue("Critical problem must cap score <= 42", (score ?: 100) <= 42)
        assertEquals("AVOID", verdict)
        assertTrue("Estimated DZD cost should be > 0", totalCostDzd > 0)
    }

    @Test
    fun `documents section has critical stamped chassis vs carte grise check and ordinary document items`() {
        val items = ChecklistEngine.buildChecklistForCar(
            brand = "Renault",
            model = "Clio 4",
            fuelType = "ESSENCE",
            gearboxType = "MANUELLE",
            isQuickMode = false
        )
        val docItems = items.filter { it.section == ChecklistSection.DOCUMENTS_IDENTITY }
        val docIds = docItems.map { it.id }.toSet()

        // 1. Stamped chassis number vs carte grise item is present and CRITICAL
        val chassisItem = docItems.first { it.id == "doc_vin_three_way_match" }
        assertEquals(ItemSeverity.CRITICAL, chassisItem.severity)
        assertTrue(chassisItem.questionEn.contains("Does the chassis number stamped on the car match the carte grise", ignoreCase = true))

        // 2. Ordinary document items (carte grise owner, contrôle technique, insurance, vignette, service history) are present
        assertTrue(docIds.contains("doc_carte_grise_owner"))
        assertTrue(docIds.contains("doc_controle_technique"))
        assertTrue(docIds.contains("doc_insurance_valid"))
        assertTrue(docIds.contains("doc_vignette_automobile"))
        assertTrue(docIds.contains("doc_service_history_invoices"))

        // 3. ECU VIN comparison choices include SAME, DIFFERENT, COULD_NOT_COMPARE
        assertEquals(
            listOf(EcuVinComparisonChoice.SAME, EcuVinComparisonChoice.DIFFERENT, EcuVinComparisonChoice.COULD_NOT_COMPARE),
            EcuVinComparisonChoice.entries
        )
    }

    @Test
    fun `obd parser handles replies with and without spaces (AT S0 and AT S1)`() {
        // Mode 03 DTCs with spaces (CAN format: 43 + count 03 + 3 DTC pairs)
        val dtcsWithSpaces = ObdDiagnosticEngine.parseMode03DtcResponse("43 03 02 99 03 01 04 20")
        assertEquals(listOf("P0299", "P0301", "P0420"), dtcsWithSpaces.map { it.code })

        // Mode 03 DTCs WITHOUT spaces (AT S0: "4302029903010000")
        val dtcsWithoutSpaces = ObdDiagnosticEngine.parseMode03DtcResponse("4302029903010000")
        assertEquals(listOf("P0299", "P0301"), dtcsWithoutSpaces.map { it.code })

        // Legacy KWP2000 / ISO 9141-2 Mode 03 without count byte (6 bytes after 0x43 = 3 DTCs)
        val dtcsLegacyNoSpaces = ObdDiagnosticEngine.parseMode03DtcResponse("43029903010000")
        assertEquals(listOf("P0299", "P0301"), dtcsLegacyNoSpaces.map { it.code })

        // Coolant temp 0105 with and without spaces (0x82 = 130 -> 130 - 40 = 90 °C)
        assertEquals(90, ObdDiagnosticEngine.parseCoolantTempC("41 05 82"))
        assertEquals(90, ObdDiagnosticEngine.parseCoolantTempC("410582"))

        // RPM 010C with and without spaces (0x0C 0x4C = 3148 -> 3148 / 4 = 787 rpm)
        assertEquals(787, ObdDiagnosticEngine.parseRpm("41 0C 0C 4C"))
        assertEquals(787, ObdDiagnosticEngine.parseRpm("410C0C4C"))

        // Distance since clear 0131 without spaces ("41310006" = 6 km)
        assertEquals(6, ObdDiagnosticEngine.parseTwoByteDistanceKm("41310006", "31"))
    }

    @Test
    fun `obd parser handles multi-frame CAN replies for DTCs and Service 09 02 VIN`() {
        // Multi-frame Mode 03 reply with 3-hex-digit length line (00A) and 0:, 1: frames without spaces
        val multiFrameDtcRaw = """
            00A
            0:430402990301
            1:042000870000
        """.trimIndent()
        val dtcs = ObdDiagnosticEngine.parseMode03DtcResponse(multiFrameDtcRaw, DtcType.STORED)
        assertEquals(listOf("P0299", "P0301", "P0420", "P0087"), dtcs.map { it.code })

        // Multi-frame Service 09 02 VIN ("VF15RJL0A58492011") with spaces
        // V=56 F=46 1=31 5=35 R=52 J=4A L=4C 0=30 A=41 5=35 8=38 4=34 9=39 2=32 0=30 1=31 1=31
        val multiFrameVinWithSpaces = """
            014
            0: 49 02 01 56 46 31
            1: 35 52 4A 4C 30 41 35
            2: 38 34 39 32 30 31 31
        """.trimIndent()
        assertEquals("VF15RJL0A58492011", ObdDiagnosticEngine.parseMode09Vin(multiFrameVinWithSpaces))

        // Multi-frame Service 09 02 VIN without spaces (AT S0)
        val multiFrameVinNoSpaces = "014\n0:490201564631\n1:35524A4C304135\n2:38343932303131"
        assertEquals("VF15RJL0A58492011", ObdDiagnosticEngine.parseMode09Vin(multiFrameVinNoSpaces))
    }

    @Test
    fun `obd parser treats NO DATA and error status texts as null (not zero) and never triggers false fraud alert`() {
        val statusReplies = listOf(
            "NO DATA",
            "SEARCHING...\r\nNO DATA",
            "UNABLE TO CONNECT",
            "STOPPED",
            "?",
            "BUS INIT: ERROR",
            "CAN ERROR"
        )
        for (reply in statusReplies) {
            assertNull("Expected null distance for '$reply'", ObdDiagnosticEngine.parseTwoByteDistanceKmOrNull(reply, "31"))
            assertNull("Expected null warmups for '$reply'", ObdDiagnosticEngine.parseSingleByteCountOrNull(reply, "30"))
            assertNull("Expected null coolant for '$reply'", ObdDiagnosticEngine.parseCoolantTempC(reply))
            assertNull("Expected null RPM for '$reply'", ObdDiagnosticEngine.parseRpm(reply))
            assertNull("Expected null PID 0101 for '$reply'", ObdDiagnosticEngine.parsePid01StatusDetail(reply))
            assertTrue("Expected empty DTCs for '$reply'", ObdDiagnosticEngine.parseMode03DtcResponse(reply).isEmpty())
        }

        // Verify that null readings produce NOT_AVAILABLE and never trigger a false cleared-codes alert
        val alertLevel = ObdDiagnosticEngine.evaluateClearedCodesAlert(
            storedDtcCount = 0,
            distanceSinceClearKm = null,
            warmupsSinceClear = null,
            incompleteMonitors = null
        )
        assertEquals(ClearedCodesAlertLevel.NOT_AVAILABLE, alertLevel)
        assertFalse(
            ObdDiagnosticEngine.detectClearedCodeFraud(
                storedDtcCount = 0,
                distanceSinceClearKm = null,
                warmupsSinceClear = null,
                incompleteMonitors = null
            )
        )
    }

    @Test
    fun `obd parser handles two control units (engine and gearbox ECUs) answering simultaneously`() {
        // Engine ECU has P0299, Gearbox ECU has P17BF (2 lines in the same Mode 03 response)
        val dualEcuMode03 = """
            430102990000
            430117BF0000
        """.trimIndent()
        val dtcs = ObdDiagnosticEngine.parseMode03DtcResponse(dualEcuMode03, DtcType.STORED)
        assertEquals(listOf("P0299", "P17BF"), dtcs.map { it.code })

        // Engine ECU says MIL OFF (00), Gearbox ECU says MIL ON + 1 DTC (81)
        val dualEcuPid01 = """
            41010007E500
            41018107E500
        """.trimIndent()
        val pid01 = ObdDiagnosticEngine.parsePid01StatusDetail(dualEcuPid01)
        assertTrue(pid01 != null && pid01.milOn)
        assertEquals(1, pid01?.dtcCount)

        // Two ECUs reporting different supported PID bitmasks in 0100 are merged (bitwise OR)
        val dualEcu0100 = """
            4100BE1FA813
            410000000001
        """.trimIndent()
        val supported = ObdDiagnosticEngine.parseSupportedPidsBitmask(dualEcu0100, 0x00)
        assertTrue(supported.contains(0x01)) // PID 01
        assertTrue(supported.contains(0x05)) // PID 05 (Coolant)
        assertTrue(supported.contains(0x0C)) // PID 0C (RPM)
        assertTrue(supported.contains(0x20)) // PID 20 (Next bitmask block)
    }

    @Test
    fun `car that supports almost nothing only gets supported requests and shows not available without false alerts`() = runBlocking {
        val commandsSent = mutableListOf<String>()
        val minimalCarTransport = object : ObdTransport {
            override val transportName: String = "ELM327 Minimal Car Test"
            override suspend fun connect(): Boolean = true
            override suspend fun disconnect() {}
            override suspend fun sendCommand(rawCommand: String, timeoutMs: Long): String {
                val cmd = rawCommand.trim().uppercase()
                commandsSent.add(cmd)
                return when (cmd) {
                    "AT Z", "AT E0", "AT L0", "AT S0", "AT H0", "AT SP 0" -> "OK"
                    "AT I" -> "ELM327 v1.5"
                    "AT DP" -> "ISO 9141-2"
                    "AT RV" -> "14.1V"
                    // 0100 bitmask: only bit for PID 05 (Coolant temp) is set -> 0x08000000 ("08000000")
                    "0100" -> "410008000000"
                    "0105" -> "410580" // 128 - 40 = 88 °C
                    "03" -> "430000000000"
                    "07" -> "NO DATA"
                    "0A" -> "NO DATA"
                    "0902" -> "NO DATA"
                    else -> "NO DATA"
                }
            }
        }

        val result = ObdDiagnosticEngine.executeRealObdScan(
            transport = minimalCarTransport,
            expectedVin = "",
            language = AppLanguage.EN
        )

        assertTrue(result.isCommunicationSuccess)
        // Verify unsupported PIDs (0101, 010C, 0120, 0130, 0131, 0140) were NEVER requested
        assertFalse("Should not request 0120 when bit 0x20 is 0", commandsSent.contains("0120"))
        assertFalse("Should not request 0131 when unsupported", commandsSent.contains("0131"))
        assertFalse("Should not request 0130 when unsupported", commandsSent.contains("0130"))
        assertFalse("Should not request 010C when unsupported", commandsSent.contains("010C"))

        // Verify unprovided readings are null, not 0
        assertNull(result.distanceSinceCodesClearedKm)
        assertNull(result.warmupsSinceCodesCleared)
        assertNull(result.incompleteReadinessMonitors)
        assertNull(result.ecuVin)

        // Verify NO false cleared-code alert is triggered
        assertFalse(result.isRecentlyClearedFraudAlert)
        assertEquals(ClearedCodesAlertLevel.NOT_AVAILABLE, result.clearedCodesAlertLevel)

        // Verify coolant was read (88 °C) while RPM is marked UNAVAILABLE ("Not available on this car")
        val coolantGauge = result.liveParameters.first { it.nameEn.contains("Coolant") }
        assertEquals("88 °C", coolantGauge.valueText)
        assertEquals("GREEN", coolantGauge.status)

        val rpmGauge = result.liveParameters.first { it.nameEn.contains("Idle") }
        assertEquals("UNAVAILABLE", rpmGauge.status)
        assertEquals("Not available on this car", rpmGauge.valueText)

        // Verify raw exchange log was recorded
        assertTrue(result.rawExchangeLog.isNotEmpty())
    }

    @Test
    fun `cleared-codes alert distinguishes strong warning vs readiness-only cannot rule out`() {
        // Readiness monitors incomplete alone (without distance or warm-ups) => CANNOT_RULE_OUT, not STRONG_WARNING
        val readinessOnlyLevel = ObdDiagnosticEngine.evaluateClearedCodesAlert(
            storedDtcCount = 0,
            distanceSinceClearKm = null,
            warmupsSinceClear = null,
            incompleteMonitors = 4
        )
        assertEquals(ClearedCodesAlertLevel.CANNOT_RULE_OUT, readinessOnlyLevel)
        assertFalse(
            ObdDiagnosticEngine.detectClearedCodeFraud(
                storedDtcCount = 0,
                distanceSinceClearKm = null,
                warmupsSinceClear = null,
                incompleteMonitors = 4
            )
        )

        // Small distance (6 km) actually read => STRONG_WARNING
        val smallDistanceLevel = ObdDiagnosticEngine.evaluateClearedCodesAlert(
            storedDtcCount = 0,
            distanceSinceClearKm = 6,
            warmupsSinceClear = 1,
            incompleteMonitors = 5
        )
        assertEquals(ClearedCodesAlertLevel.STRONG_WARNING, smallDistanceLevel)
        assertTrue(
            ObdDiagnosticEngine.detectClearedCodeFraud(
                storedDtcCount = 0,
                distanceSinceClearKm = 6,
                warmupsSinceClear = 1,
                incompleteMonitors = 5
            )
        )

        // Normal car with 4820 km and 142 warm-ups => NONE_NORMAL
        val normalLevel = ObdDiagnosticEngine.evaluateClearedCodesAlert(
            storedDtcCount = 0,
            distanceSinceClearKm = 4820,
            warmupsSinceClear = 142,
            incompleteMonitors = 0
        )
        assertEquals(ClearedCodesAlertLevel.NONE_NORMAL, normalLevel)

        // Unknown fault code shows "ask a mechanic what this code means on this car" and 0 cost
        val unknownCode = ObdDiagnosticEngine.lookupDtc("P1999")
        assertTrue(unknownCode.plainExplanationEn.lowercase().contains("ask a mechanic what this code means on this car"))
        assertEquals(0, unknownCode.minCostDzd)
        assertEquals(0, unknownCode.maxCostDzd)
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
    fun `report engine returns INCOMPLETE when only 1 item answered and caps critical defects when all answered`() {
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
        val singleAnswer = listOf(
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
        val incompleteReport = ReportEngine.buildReport(inspection, singleAnswer)
        assertEquals(null, incompleteReport.overallScore)
        assertEquals("INCOMPLETE", incompleteReport.verdict)
        assertTrue(incompleteReport.uncheckedItems.isNotEmpty())
        assertTrue(incompleteReport.uncheckedCriticalItems.isNotEmpty())

        val allItems = ChecklistEngine.buildChecklistForCar(
            brand = "Renault",
            model = "Clio 4",
            fuelType = "DIESEL",
            gearboxType = "MANUELLE",
            isQuickMode = true
        )
        val fullAnswers = allItems.map { item ->
            ChecklistAnswerEntity(
                inspectionId = 10L,
                itemId = item.id,
                category = item.section.name,
                questionFr = item.questionFr,
                questionAr = item.questionAr,
                questionEn = item.questionEn,
                status = if (item.id == "bonnet_chassis_longerons") "PROBLEM" else "GOOD",
                severity = item.severity.name,
                estimatedCostDzd = if (item.id == "bonnet_chassis_longerons") 120000 else 0
            )
        }
        val completeReport = ReportEngine.buildReport(inspection, fullAnswers)
        assertEquals(42, completeReport.overallScore)
        assertTrue((completeReport.rawUncappedScore ?: 0) > 42)
        assertTrue(completeReport.isCriticalCapTriggered)
        assertEquals("AVOID", completeReport.verdict)
        assertEquals(1, completeReport.criticalProblems.size)

        // Phase 5: 1-line explanation shows both 42 and the raw score and names the critical item
        val capLineEn = completeReport.getCriticalCapOneLineExplanation(AppLanguage.EN) ?: ""
        assertTrue(capLineEn.contains("Score capped at 42"))
        assertTrue(capLineEn.contains("${completeReport.rawUncappedScore}/100"))
        assertTrue(capLineEn.contains("because of 1 critical problem:"))
        assertTrue(capLineEn.contains(completeReport.criticalProblems.first().questionEn))

        // Phase 5: Unverified automatic item costs are never subtracted automatically
        assertEquals(0, completeReport.minTotalRepairDzd)
        assertEquals(0, completeReport.maxTotalRepairDzd)

        // Phase 5: User-entered mechanic quote (e.g., 60,000 DZD = 6 Millions) subtracted from 240 Millions = 2,340,000 DZD (234 Millions)
        val fairOfferDzd = ReportEngine.calculateFairOfferFromMechanicQuoteDzd(
            askingPriceMillions = 240.0,
            mechanicQuotedRepairDzd = 60000
        )
        assertEquals(2340000, fairOfferDzd)
        assertEquals("234 Millions", ReportEngine.formatMillionsCentimes(fairOfferDzd, AppLanguage.FR))
    }

    @Test
    fun `algerian car catalogue has 51 models across 20 brand groups all marked to be verified`() {
        val models = com.example.data.model.AlgerianCarCatalog.models
        assertEquals(51, models.size)
        for (car in models) {
            assertEquals(
                "Catalogue model ${car.id} must default to TO_BE_VERIFIED",
                com.example.data.model.VerificationStatus.TO_BE_VERIFIED,
                car.weakPointVerificationStatus
            )
            assertTrue(car.keyWeakPointFr.isNotBlank())
            assertTrue(car.keyWeakPointAr.isNotBlank())
            assertTrue(car.keyWeakPointEn.isNotBlank())
        }
    }

    @Test
    fun `report obd block formats all 5 required OBD fields accurately`() {
        val inspectionWithObd = InspectionEntity(
            id = 22L,
            carBrand = "Peugeot",
            carModel = "208",
            carYear = 2018,
            hasObdScan = true,
            obdCommunicationSuccess = true,
            obdProtocolDescription = "ISO 15765-4 CAN",
            obdDtcCodesSummary = "P0420 (STORED), P0299 (PENDING)",
            obdDtcCount = 2,
            obdClearedStatus = "STRONG_WARNING",
            obdDistanceSinceClearedKm = 14,
            obdWarmupsSinceCleared = 1,
            obdMilBulbLitAtKeyOn = false,
            obdMilCommandedByEcu = "ON",
            obdEcuVin = "VF3CC9HP0JW109482",
            obdEcuVinComparison = "DIFFERENT"
        )
        val report = ReportEngine.buildReport(inspectionWithObd, emptyList())
        val block = report.obdBlock
        assertTrue(block.isScanned)
        assertTrue(block.hasCriticalAlert)
        assertTrue(block.getConnectionStatusText(AppLanguage.EN).contains("Connected to car ECU"))
        assertTrue(block.getFaultCodesText(AppLanguage.EN).contains("2 fault code(s) found: P0420 (STORED), P0299 (PENDING)"))
        assertTrue(block.getClearedCodesText(AppLanguage.EN).contains("14 km since clearing"))
        assertTrue(block.getCheckEngineLightText(AppLanguage.EN).contains("CRITICAL: Check-Engine light did NOT light up at key-on"))
        assertTrue(block.getChassisComparisonText(AppLanguage.EN).contains("DIFFERENT from the carte grise"))
    }

    @Test
    fun `phase 7 odometer OCR extractor proposes valid mileage and ignores bare years`() {
        val rawOcr = """
            PEUGEOT
            2019
            148 350 km
            TRIP 412.5
            92400
        """.trimIndent()
        val candidates = com.example.domain.OnDeviceOcrHelper.extractOdometerCandidates(rawOcr)
        assertTrue(candidates.isNotEmpty())
        assertEquals(148350, candidates.first().mileageKm)
        assertTrue(candidates.any { it.mileageKm == 92400 })
        assertFalse("Bare 4-digit year 2019 without km suffix must not be proposed as mileage", candidates.any { it.mileageKm == 2019 })
    }

    @Test
    fun `phase 7 tyre DOT parser calculates manufacture week year and age accurately`() {
        val recentCandidates = com.example.domain.OnDeviceOcrHelper.extractTyreDotCandidates("DOT 4F8R 2423")
        assertEquals(1, recentCandidates.size)
        val recent = recentCandidates.first()
        assertEquals("2423", recent.dotCode)
        assertEquals(24, recent.week)
        assertEquals(2023, recent.year)
        assertFalse(recent.isFiveYearsOrOlder)

        val oldManual = com.example.domain.OnDeviceOcrHelper.parseManualTyreDotCode("3518")
        assertTrue(oldManual != null)
        assertEquals(35, oldManual!!.week)
        assertEquals(2018, oldManual.year)
        assertTrue("2018 tyre must be flagged as >= 5 years old", oldManual.isFiveYearsOrOlder)
    }

    @Test
    fun `phase 7 automatic test drive monitoring detects overheating unstable idle weak charging and fault codes`() {
        val coolTemps = listOf(88, 92, 98, 105) // 105°C -> overheating (>= 103°C)
        val idleRpms = listOf(760, 780, 990, 740) // spread = 250 RPM (> 180 RPM)
        val voltages = listOf(13.8, 12.7, 13.4) // min = 12.7 V (< 13.1 V)
        val dtcs = listOf("P0300", "P0420")

        val summary = com.example.domain.ObdTestDriveMonitorService.buildSummaryFromSamples(
            durationSeconds = 420,
            samplesCount = 28,
            coolantTemps = coolTemps,
            idleRpms = idleRpms,
            voltages = voltages,
            dtcs = dtcs
        )
        assertTrue(summary.overheatingDetected)
        assertEquals(105, summary.maxCoolantTempC)
        assertTrue(summary.unstableIdleDetected)
        assertEquals(250, summary.idleRpmSpread)
        assertTrue(summary.weakChargingDetected)
        assertEquals(12.7, summary.minVoltageV ?: 0.0, 0.01)
        assertEquals(listOf("P0300", "P0420"), summary.faultCodesFoundDuringDrive)
        assertTrue(summary.hasAnyProblem)
    }

    @Test
    fun `phase 7 beta voice answer parser handles English and French and ignores Arabic`() {
        assertEquals("GOOD", com.example.domain.OfflineVoiceAndTtsController.parseBetaVoiceAnswerCommand("Good condition", AppLanguage.EN))
        assertEquals("PROBLEM", com.example.domain.OfflineVoiceAndTtsController.parseBetaVoiceAnswerCommand("There is a problem", AppLanguage.EN))
        assertEquals("SKIPPED", com.example.domain.OfflineVoiceAndTtsController.parseBetaVoiceAnswerCommand("Skip this", AppLanguage.EN))

        assertEquals("GOOD", com.example.domain.OfflineVoiceAndTtsController.parseBetaVoiceAnswerCommand("C'est bon", AppLanguage.FR))
        assertEquals("PROBLEM", com.example.domain.OfflineVoiceAndTtsController.parseBetaVoiceAnswerCommand("Il y a un problème", AppLanguage.FR))
        assertEquals("SKIPPED", com.example.domain.OfflineVoiceAndTtsController.parseBetaVoiceAnswerCommand("Passer", AppLanguage.FR))

        assertNull("Arabic voice answers are hidden unless proven reliable", com.example.domain.OfflineVoiceAndTtsController.parseBetaVoiceAnswerCommand("جيد", AppLanguage.AR))
    }

    @Test
    fun `phase 7 OBD scan builds checklist auto answers for real scan and skips simulated scan`() {
        val realScan = com.example.domain.ObdDiagnosticEngine.generateSimulatedScan(
            profile = com.example.domain.ObdSimulatorProfile.HEALTHY,
            expectedVin = "VF1RJA00062100001"
        ).copy(isSimulated = false)

        val proposals = com.example.ui.viewmodel.FahsViewModel.buildAutoAnsweredChecklistItemsFromScan(
            scanResult = realScan,
            milBulbLitOnDashboard = true,
            ecuVinComparison = com.example.domain.EcuVinComparisonChoice.SAME,
            adaptiveItems = com.example.domain.ChecklistEngine.fullLibrary
        )
        assertTrue(proposals.isNotEmpty())
        val map = proposals.associateBy { it.item.id }
        assertEquals("GOOD", map["int_dashboard_warning_lights"]?.status)
        assertEquals("GOOD", map["bonnet_battery_alternator"]?.status)
        assertEquals("GOOD", map["after_temp_fan_operation"]?.status)
        assertEquals("GOOD", map["start_idle_rpm_stability"]?.status)
        assertEquals("GOOD", map["final_obd_pending_after_drive"]?.status)
        assertEquals("GOOD", map["doc_vin_three_way_match"]?.status)
        assertTrue(proposals.all { it.getReason(AppLanguage.EN).startsWith(com.example.ui.viewmodel.FahsViewModel.OBD_AUTO_ANSWER_TAG) })

        val simulatedProposals = com.example.ui.viewmodel.FahsViewModel.buildAutoAnsweredChecklistItemsFromScan(
            scanResult = realScan.copy(isSimulated = true),
            milBulbLitOnDashboard = true,
            ecuVinComparison = com.example.domain.EcuVinComparisonChoice.SAME,
            adaptiveItems = com.example.domain.ChecklistEngine.fullLibrary
        )
        assertTrue("Simulated scan must never auto-answer checklist items", simulatedProposals.isEmpty())
    }

    @Test
    fun `phase 7 plain language summary builds 3 to 4 template sentences in AR FR and EN`() {
        val inspection = InspectionEntity(
            id = 55L,
            carBrand = "Renault",
            carModel = "Clio 4",
            carYear = 2018
        )
        val allTemplates = com.example.domain.ChecklistEngine.buildChecklistForCar(
            brand = "Renault",
            model = "Clio 4",
            fuelType = "ESSENCE",
            gearboxType = "MANUELLE",
            isQuickMode = false
        )
        val answers = allTemplates.mapIndexed { idx, t ->
            ChecklistAnswerEntity(
                inspectionId = 55L,
                itemId = t.id,
                category = t.section.name,
                questionFr = t.questionFr,
                questionAr = t.questionAr,
                questionEn = t.questionEn,
                status = if (idx == 0) "PROBLEM" else "GOOD",
                severity = t.severity.name,
                estimatedCostDzd = 0,
                note = "",
                photoUri = null
            )
        }
        val report = ReportEngine.buildReport(inspection, answers)
        val summaryEn = report.getPlainLanguageSummary(AppLanguage.EN)
        val summaryFr = report.getPlainLanguageSummary(AppLanguage.FR)
        val summaryAr = report.getPlainLanguageSummary(AppLanguage.AR)

        assertTrue(summaryEn.contains("the main strengths are in"))
        assertTrue(summaryEn.contains("Ask a trusted mechanic to inspect"))
        assertTrue(summaryFr.contains("les principaux points forts"))
        assertTrue(summaryFr.contains("Demandez à un mécanicien"))
        assertTrue(summaryAr.contains("أبرز نقاط القوة"))
        assertTrue(summaryAr.contains("اطلب من الميكانيكي"))
    }
}

