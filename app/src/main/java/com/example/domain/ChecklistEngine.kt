package com.example.domain

import com.example.data.model.AlgerianCarCatalog
import com.example.data.model.ChecklistItemDefinition
import com.example.data.model.ChecklistLibrary
import com.example.data.model.ChecklistLibraryPart2
import com.example.data.model.ChecklistLibraryPart3
import com.example.data.model.ChecklistLibraryPart4
import com.example.data.model.ChecklistLibraryPart5
import com.example.data.model.ChecklistLibraryPart6
import com.example.data.model.ChecklistLibraryPart7
import com.example.data.model.ItemSeverity

object ChecklistEngine {

    private const val REFERENCE_CURRENT_YEAR = 2026

    val fullLibrary: List<ChecklistItemDefinition> =
        (
            ChecklistLibrary.allItems +
                ChecklistLibraryPart2.extraItems +
                ChecklistLibraryPart3.phase8Items +
                ChecklistLibraryPart4.section1To4Items +
                ChecklistLibraryPart5.section5To7Items +
                ChecklistLibraryPart6.section8To9Items +
                ChecklistLibraryPart7.extraSectionItems
            )
            .distinctBy { it.id }
            .sortedBy { it.section.order }

    /**
     * Infers whether an engine description or car configuration is turbocharged when not explicitly specified.
     */
    fun inferHasTurbo(fuelType: String, engineDescription: String): Boolean {
        val descUpper = engineDescription.uppercase()
        val fuelUpper = fuelType.uppercase()
        if (descUpper.contains("SDI") || descUpper.contains("1.9 D") || descUpper.contains("ATMO")) {
            return false
        }
        val turboKeywords = listOf(
            "TURBO", "TSI", "TFSI", "TDI", "HDI", "DCI", "TCE", "CRDI", "TGDI", "T-GDI",
            "TDCI", "CDTI", "JTD", "MULTIJET", "D-4D", "D4D", "DID", "THP", "ECOBOOST", "BLUEHDI"
        )
        if (turboKeywords.any { descUpper.contains(it) }) return true
        // Modern diesels are almost universally turbocharged unless marked SDI / 1.9D
        if (fuelUpper.contains("DIESEL") && descUpper.isBlank()) return true
        return false
    }

    /**
     * Filters and orders checklist items based on the exact car configuration:
     * - Exact Model & Generation for model weak points (no brand-wide shortcuts)
     * - Engine, Fuel type (ESSENCE, DIESEL, GPL, HYBRIDE) & Gearbox type (MANUELLE, AUTOMATIQUE, DSG, etc.)
     * - Year / Age (minYear, maxYear, minAgeYears)
     * - Odometer Mileage band (minMileageKm, maxMileageKm)
     * - Turbocharger (requiresTurbo)
     * - Four-Wheel Drive / AWD (requiresFourWheelDrive)
     * - Body type (CITADINE, BERLINE, SUV, UTILITAIRE, PICKUP)
     * - Quick Check vs Full Inspection
     * - "Other car" path: cars missing from the catalogue still get a fully adapted checklist
     *   from fuel, gearbox, year, body type, mileage, turbo, and 4x4.
     */
    fun buildChecklistForCar(
        brand: String,
        model: String,
        fuelType: String,
        gearboxType: String,
        isQuickMode: Boolean,
        generation: String = "",
        engineDescription: String = "",
        carYear: Int = 2018,
        odometerKm: Int = 0,
        hasTurbo: Boolean? = null,
        isFourWheelDrive: Boolean? = null,
        bodyType: String = ""
    ): List<ChecklistItemDefinition> {
        val normalizedFuel = fuelType.uppercase().trim()
        val normalizedGearbox = gearboxType.uppercase().trim()
        val cleanBrand = brand.trim()
        val cleanModel = model.trim()
        val cleanGeneration = generation.trim()
        val cleanEngine = engineDescription.trim()

        // Match exact catalogue model by Brand + Model (+ Generation if provided)
        val matchingModels = AlgerianCarCatalog.models.filter {
            it.brand.equals(cleanBrand, ignoreCase = true) &&
                it.model.equals(cleanModel, ignoreCase = true)
        }
        val exactCatalogModel = when {
            matchingModels.isEmpty() -> null
            cleanGeneration.isNotBlank() -> matchingModels.firstOrNull { carModel ->
                carModel.generations.any { gen -> gen.name.equals(cleanGeneration, ignoreCase = true) }
            } ?: matchingModels.first()
            else -> matchingModels.first()
        }

        val effectiveGeneration = cleanGeneration.ifBlank {
            exactCatalogModel?.generations?.firstOrNull()?.name.orEmpty()
        }
        val effectiveBodyType = bodyType.trim().uppercase().ifBlank {
            exactCatalogModel?.defaultBodyType?.uppercase() ?: "BERLINE"
        }
        val effectiveHasTurbo = hasTurbo ?: inferHasTurbo(normalizedFuel, cleanEngine)
        val effectiveIs4x4 = isFourWheelDrive ?: false
        val effectiveYear = if (carYear in 1980..REFERENCE_CURRENT_YEAR) carYear else 2018
        val carAgeYears = (REFERENCE_CURRENT_YEAR - effectiveYear).coerceAtLeast(0)

        return fullLibrary.filter { item ->
            // 1. Quick Mode filter
            if (isQuickMode && !item.includedInQuickCheck) {
                return@filter false
            }

            // 2. Fuel condition
            if (item.requiredFuels.isNotEmpty()) {
                val fuelMatch = item.requiredFuels.any { req ->
                    normalizedFuel.contains(req.uppercase())
                }
                if (!fuelMatch) return@filter false
            }

            // 3. Gearbox condition
            if (item.requiredGearboxes.isNotEmpty()) {
                val isManualCar = normalizedGearbox.contains("MANUEL") || normalizedGearbox.contains("MANUAL")
                val isAutoCar = normalizedGearbox.contains("AUTO") ||
                    normalizedGearbox.contains("DSG") ||
                    normalizedGearbox.contains("EDC") ||
                    normalizedGearbox.contains("CVT") ||
                    normalizedGearbox.contains("BVA")

                val gearMatch = item.requiredGearboxes.any { req ->
                    val reqUpper = req.uppercase()
                    when {
                        reqUpper.contains("MANUEL") -> isManualCar
                        reqUpper.contains("AUTO") -> isAutoCar
                        else -> normalizedGearbox.contains(reqUpper)
                    }
                }
                if (!gearMatch) return@filter false
            }

            // 4. Exact Model + Generation + Engine condition for Model Weak Points
            // (No brand-wide Peugeot / Renault / Dacia shortcuts!)
            if (item.specificModelIds.isNotEmpty()) {
                val catalogId = exactCatalogModel?.id ?: return@filter false
                val idMatches = item.specificModelIds.any { specId ->
                    specId.equals(catalogId, ignoreCase = true)
                }
                if (!idMatches) return@filter false

                if (item.requiredGenerations.isNotEmpty()) {
                    if (effectiveGeneration.isBlank()) return@filter false
                    val genMatches = item.requiredGenerations.any { reqGen ->
                        val cleanReq = reqGen.trim()
                        effectiveGeneration.equals(cleanReq, ignoreCase = true) ||
                            effectiveGeneration.contains(cleanReq, ignoreCase = true) ||
                            cleanReq.contains(effectiveGeneration, ignoreCase = true)
                    }
                    if (!genMatches) return@filter false
                }
            }

            // 5. Specific Engine condition (when item restricts to specific engine strings like "1.2 PureTech")
            if (item.requiredEngines.isNotEmpty()) {
                // Only match if the selected engine matches one of requiredEngines (or if no engine was specified on a model weak point, check if the model's only matching fuel engines include it)
                if (cleanEngine.isNotBlank()) {
                    val engineMatches = item.requiredEngines.any { reqEng ->
                        cleanEngine.contains(reqEng, ignoreCase = true) ||
                            reqEng.contains(cleanEngine, ignoreCase = true)
                    }
                    if (!engineMatches) return@filter false
                } else if (exactCatalogModel != null) {
                    val modelHasRequiredEngine = exactCatalogModel.commonEngines.any { catEng ->
                        item.requiredEngines.any { reqEng -> catEng.contains(reqEng, ignoreCase = true) }
                    }
                    if (!modelHasRequiredEngine) return@filter false
                } else {
                    return@filter false
                }
            }

            // 6. Year & Age conditions
            if (item.minYear != null && effectiveYear < item.minYear) {
                return@filter false
            }
            if (item.maxYear != null && effectiveYear > item.maxYear) {
                return@filter false
            }
            if (item.minAgeYears != null && carAgeYears < item.minAgeYears) {
                return@filter false
            }

            // 7. Odometer Mileage Band conditions
            if (item.minMileageKm != null) {
                if (odometerKm <= 0) {
                    // When mileage was not entered (> 0), include high-mileage checks only if the car is older (>= 7 years)
                    if (carAgeYears < 7) return@filter false
                } else if (odometerKm < item.minMileageKm) {
                    return@filter false
                }
            }
            if (item.maxMileageKm != null && odometerKm > 0 && odometerKm > item.maxMileageKm) {
                return@filter false
            }

            // 8. Turbocharger condition
            if (item.requiresTurbo != null && item.requiresTurbo != effectiveHasTurbo) {
                return@filter false
            }

            // 9. Four-Wheel Drive (4x4 / AWD) condition
            if (item.requiresFourWheelDrive != null && item.requiresFourWheelDrive != effectiveIs4x4) {
                return@filter false
            }

            // 10. Body Type condition
            if (item.requiredBodyTypes.isNotEmpty()) {
                val bodyMatch = item.requiredBodyTypes.any { reqBody ->
                    reqBody.equals(effectiveBodyType, ignoreCase = true)
                }
                if (!bodyMatch) return@filter false
            }

            true
        }
    }

    /**
     * Minimum share of applicable checklist items (70%) that must be answered, in addition to
     * 100% of CRITICAL items in the chosen mode, before a score or verdict is calculated.
     */
    const val MINIMUM_ANSWERED_SHARE = 0.70

    data class ScoreEvaluation(
        val overallScore: Int?, // null when minimum threshold or critical items are not all answered
        val verdict: String, // "GOOD", "NEGOTIATE", "AVOID", or "INCOMPLETE"
        val totalEstimatedRepairDzd: Int,
        val answeredCount: Int,
        val uncheckedCount: Int,
        val totalCount: Int,
        val minimumRequiredCount: Int,
        val uncheckedCriticalItems: List<ChecklistItemDefinition>,
        val isMinimumThresholdMet: Boolean
    )

    fun evaluateChecklist(
        answers: Map<String, String>, // itemId -> GOOD, PROBLEM, UNCHECKED
        items: List<ChecklistItemDefinition>
    ): ScoreEvaluation {
        var totalMaxPoints = 0
        var deductedPoints = 0
        var hasCriticalProblem = false
        var importantProblemCount = 0
        var totalEstimatedRepairDzd = 0
        var answeredCount = 0
        val uncheckedCritical = mutableListOf<ChecklistItemDefinition>()

        for (item in items) {
            val status = answers[item.id] ?: "UNCHECKED"
            if (status == "GOOD" || status == "PROBLEM") {
                answeredCount++
                totalMaxPoints += item.severity.weight
                if (status == "PROBLEM") {
                    deductedPoints += item.severity.weight
                    totalEstimatedRepairDzd += item.getAverageCostDzd()
                    if (item.severity == ItemSeverity.CRITICAL) {
                        hasCriticalProblem = true
                    } else if (item.severity == ItemSeverity.IMPORTANT) {
                        importantProblemCount++
                    }
                }
            } else if (item.severity == ItemSeverity.CRITICAL) {
                uncheckedCritical.add(item)
            }
        }

        val totalCount = items.size
        val uncheckedCount = (totalCount - answeredCount).coerceAtLeast(0)
        val minimumRequiredCount = kotlin.math.ceil(totalCount * MINIMUM_ANSWERED_SHARE).toInt().coerceAtLeast(1)
        val isMinimumThresholdMet = totalCount > 0 &&
            answeredCount >= minimumRequiredCount &&
            uncheckedCritical.isEmpty()

        if (!isMinimumThresholdMet || totalMaxPoints == 0) {
            return ScoreEvaluation(
                overallScore = null,
                verdict = "INCOMPLETE",
                totalEstimatedRepairDzd = totalEstimatedRepairDzd,
                answeredCount = answeredCount,
                uncheckedCount = uncheckedCount,
                totalCount = totalCount,
                minimumRequiredCount = minimumRequiredCount,
                uncheckedCriticalItems = uncheckedCritical,
                isMinimumThresholdMet = false
            )
        }

        val rawPercentage = (((totalMaxPoints - deductedPoints).toDouble() / totalMaxPoints.toDouble()) * 100.0).toInt()
            .coerceIn(0, 100)

        val finalScore = if (hasCriticalProblem) {
            minOf(rawPercentage, 42)
        } else if (importantProblemCount >= 3) {
            minOf(rawPercentage, 68)
        } else {
            rawPercentage
        }

        val verdict = when {
            hasCriticalProblem || finalScore < 50 -> "AVOID"
            finalScore < 78 || importantProblemCount >= 1 -> "NEGOTIATE"
            else -> "GOOD"
        }

        return ScoreEvaluation(
            overallScore = finalScore,
            verdict = verdict,
            totalEstimatedRepairDzd = totalEstimatedRepairDzd,
            answeredCount = answeredCount,
            uncheckedCount = uncheckedCount,
            totalCount = totalCount,
            minimumRequiredCount = minimumRequiredCount,
            uncheckedCriticalItems = uncheckedCritical,
            isMinimumThresholdMet = true
        )
    }

    /**
     * Calculates an objective 0..100 score and verdict from answered items ONLY when the
     * minimum share of items (70%) and all critical items in the chosen mode have been answered.
     * Otherwise returns (null, "INCOMPLETE", totalRepairEstimateDzd).
     */
    fun calculateScoreAndVerdict(
        answers: Map<String, String>, // itemId -> GOOD, PROBLEM, UNCHECKED
        items: List<ChecklistItemDefinition>
    ): Triple<Int?, String, Int> {
        val eval = evaluateChecklist(answers, items)
        return Triple(eval.overallScore, eval.verdict, eval.totalEstimatedRepairDzd)
    }
}
