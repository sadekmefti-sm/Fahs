package com.example.domain

import com.example.data.model.ChecklistItemDefinition
import com.example.data.model.ChecklistLibrary
import com.example.data.model.ChecklistLibraryPart2
import com.example.data.model.ChecklistLibraryPart3
import com.example.data.model.ItemSeverity

object ChecklistEngine {

    val fullLibrary: List<ChecklistItemDefinition> =
        (ChecklistLibrary.allItems + ChecklistLibraryPart2.extraItems + ChecklistLibraryPart3.phase8Items)
            .sortedBy { it.section.order }

    /**
     * Filters and orders checklist items based on the exact car configuration:
     * - Fuel type (ESSENCE, DIESEL, GPL, HYBRIDE)
     * - Gearbox type (MANUELLE, AUTOMATIQUE, AUTOMATIQUE (DSG), etc.)
     * - Brand & Model (for known model weak points like Peugeot 1.2 PureTech or Renault 1.5 dCi)
     * - Quick Check (~10 min) vs Full Inspection (~40 min)
     */
    fun buildChecklistForCar(
        brand: String,
        model: String,
        fuelType: String,
        gearboxType: String,
        isQuickMode: Boolean
    ): List<ChecklistItemDefinition> {
        val normalizedFuel = fuelType.uppercase()
        val normalizedGearbox = gearboxType.uppercase()
        val normalizedId = "${brand.lowercase().trim()}_${model.lowercase().trim().replace(" ", "")}"
        val catalogModelId = com.example.data.model.AlgerianCarCatalog.models.firstOrNull {
            it.brand.equals(brand.trim(), ignoreCase = true) &&
                (it.model.equals(model.trim(), ignoreCase = true) ||
                    it.model.lowercase().contains(model.lowercase().trim()) ||
                    model.lowercase().trim().contains(it.model.lowercase()))
        }?.id ?: normalizedId

        return fullLibrary.filter { item ->
            // 1. Check Quick Mode filter
            if (isQuickMode && !item.includedInQuickCheck) {
                return@filter false
            }

            // 2. Check Fuel condition
            if (item.requiredFuels.isNotEmpty()) {
                val fuelMatch = item.requiredFuels.any { req ->
                    normalizedFuel.contains(req.uppercase())
                }
                if (!fuelMatch) return@filter false
            }

            // 3. Check Gearbox condition
            if (item.requiredGearboxes.isNotEmpty()) {
                val isManualCar = normalizedGearbox.contains("MANUEL") || normalizedGearbox.contains("MANUAL")
                val isAutoCar = normalizedGearbox.contains("AUTO") || normalizedGearbox.contains("DSG") || normalizedGearbox.contains("EDC")

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

            // 4. Check Model-Specific Known Weak Point condition
            if (item.specificModelIds.isNotEmpty()) {
                val modelMatch = item.specificModelIds.any { specId ->
                    catalogModelId.equals(specId, ignoreCase = true) ||
                        normalizedId.contains(specId) ||
                        (brand.equals("Peugeot", ignoreCase = true) && specId.startsWith("peugeot") && !normalizedFuel.contains("DIESEL")) ||
                        ((brand.equals("Renault", ignoreCase = true) || brand.equals("Dacia", ignoreCase = true)) && specId.startsWith("renault") && normalizedFuel.contains("DIESEL"))
                }
                if (!modelMatch) return@filter false
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
