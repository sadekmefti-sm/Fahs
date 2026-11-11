package com.example.domain

import com.example.core.i18n.AppLanguage
import com.example.data.local.ChecklistAnswerEntity
import com.example.data.local.InspectionEntity
import com.example.data.model.ChecklistItemDefinition
import com.example.data.model.ChecklistSection
import com.example.data.model.ItemSeverity
import java.util.Locale

data class ReportFindingItem(
    val itemId: String,
    val sectionTitleFr: String,
    val sectionTitleAr: String,
    val sectionTitleEn: String,
    val questionFr: String,
    val questionAr: String,
    val questionEn: String,
    val explanationFr: String,
    val explanationAr: String,
    val explanationEn: String,
    val status: String, // GOOD, PROBLEM, UNCHECKED
    val severity: ItemSeverity,
    val minCostDzd: Int,
    val maxCostDzd: Int,
    val avgCostDzd: Int,
    val buyerNote: String,
    val photoUri: String?,
    val isKnownModelWeakPoint: Boolean
) {
    fun getSectionTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> sectionTitleAr
        AppLanguage.FR -> sectionTitleFr
        AppLanguage.EN -> sectionTitleEn
    }

    fun getQuestion(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> questionAr
        AppLanguage.FR -> questionFr
        AppLanguage.EN -> questionEn
    }

    fun getExplanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> explanationAr
        AppLanguage.FR -> explanationFr
        AppLanguage.EN -> explanationEn
    }
}

data class InspectionReportData(
    val inspection: InspectionEntity,
    val overallScore: Int?,
    val rawUncappedScore: Int?,
    val isCriticalCapTriggered: Boolean,
    val isImportantCapTriggered: Boolean,
    val verdict: String, // GOOD, NEGOTIATE, AVOID, or INCOMPLETE
    val criticalProblems: List<ReportFindingItem>,
    val negotiableProblems: List<ReportFindingItem>,
    val passedItems: List<ReportFindingItem>,
    val uncheckedItems: List<ReportFindingItem>,
    val uncheckedCriticalItems: List<ReportFindingItem>,
    val minimumRequiredCount: Int,
    val isMinimumThresholdMet: Boolean,
    val minTotalRepairDzd: Int,
    val maxTotalRepairDzd: Int,
    val avgTotalRepairDzd: Int,
    val checkedCount: Int,
    val totalApplicableCount: Int,
    val coveragePercent: Int,
    val hasObdScanIncluded: Boolean,
    val obdSummaryNote: String?
) {
    fun getVerdictTitle(lang: AppLanguage): String = when (verdict) {
        "INCOMPLETE" -> when (lang) {
            AppLanguage.AR -> "الفحص غير مكتمل — لا توجد نتيجة أو حكم بعد"
            AppLanguage.FR -> "Inspection incomplète — Aucun score ni verdict"
            AppLanguage.EN -> "Inspection Incomplete — No Score or Verdict Yet"
        }
        "GOOD" -> when (lang) {
            AppLanguage.AR -> "سيارة بحالة جيدة (Bon achat)"
            AppLanguage.FR -> "Bon état général — Achat recommandé"
            AppLanguage.EN -> "Good Condition — Recommended Buy"
        }
        "NEGOTIATE" -> when (lang) {
            AppLanguage.AR -> "صالحة للشراء مع التفاوض على السعر (À négocier)"
            AppLanguage.FR -> "Acheter avec prudence — À négocier"
            AppLanguage.EN -> "Buy with Caution — Negotiate Repair Costs"
        }
        else -> when (lang) {
            AppLanguage.AR -> "تنبيه هام — تستوجب رأي ميكانيكي أو تجنبها (À éviter)"
            AppLanguage.FR -> "Attention — Risque élevé / Avis mécanicien requis"
            AppLanguage.EN -> "High Risk — Mechanic Opinion Required / Walk Away"
        }
    }

    fun getVerdictSummary(lang: AppLanguage): String {
        val uncheckedNote = when (lang) {
            AppLanguage.AR -> "عدد النقاط غير المفحوصة: ${uncheckedItems.size} من أصل $totalApplicableCount."
            AppLanguage.FR -> "Points non vérifiés : ${uncheckedItems.size} sur $totalApplicableCount."
            AppLanguage.EN -> "Items not checked: ${uncheckedItems.size} out of $totalApplicableCount."
        }
        if (verdict == "INCOMPLETE" || overallScore == null) {
            val critNote = if (uncheckedCriticalItems.isNotEmpty()) {
                val names = uncheckedCriticalItems.joinToString(" • ") { it.getQuestion(lang) }
                when (lang) {
                    AppLanguage.AR -> " النقاط الحرجة غير المفحوصة (${uncheckedCriticalItems.size}): $names"
                    AppLanguage.FR -> " Points critiques non vérifiés (${uncheckedCriticalItems.size}) : $names"
                    AppLanguage.EN -> " Unchecked critical items (${uncheckedCriticalItems.size}): $names"
                }
            } else ""
            val baseIncomplete = when (lang) {
                AppLanguage.AR -> "لا يتم عرض نتيجة أو حكم حتى يتم فحص ما لا يقل عن $minimumRequiredCount نقطة (70%) وجميع النقاط الحرجة في هذا الوضع. $uncheckedNote"
                AppLanguage.FR -> "Aucun score ni verdict n'est affiché tant qu'au moins $minimumRequiredCount points (70%) et la totalité des points critiques du mode choisi n'ont pas été vérifiés. $uncheckedNote"
                AppLanguage.EN -> "No score or verdict is shown until at least $minimumRequiredCount items (70%) and all critical items in the chosen mode are answered. $uncheckedNote"
            }
            return baseIncomplete + critNote
        }

        val mainSummary = when {
            isCriticalCapTriggered -> when (lang) {
                AppLanguage.AR -> "تم تقييد النتيجة القصوى إلى $overallScore/100 (بدلاً من $rawUncappedScore/100) بسبب رصد ${criticalProblems.size} نقطة حرجة تمس سلامة الشاسيه، المحرك أو الوثائق. هذا الأمر يستحق رأي ميكانيكي مختص قبل دفع أي مبلغ."
                AppLanguage.FR -> "Le score a été plafonné à $overallScore/100 (score brut : $rawUncappedScore/100) car ${criticalProblems.size} anomalie(s) critique(s) ont été relevées (châssis, moteur, calculateur ou documents). Cela mérite l'avis d'un mécanicien avant tout engagement."
                AppLanguage.EN -> "The score was capped at $overallScore/100 (uncapped: $rawUncappedScore/100) because ${criticalProblems.size} critical finding(s) were recorded. This deserves a trusted mechanic's opinion before committing."
            }
            verdict == "NEGOTIATE" -> when (lang) {
                AppLanguage.AR -> "لم يتم رصد عيوب هيكلية قاتلة، لكن توجد ${negotiableProblems.size} ملاحظات تحتاج صيانة بتكلفة تقديرية بين ${ReportEngine.formatDzd(minTotalRepairDzd)} و ${ReportEngine.formatDzd(maxTotalRepairDzd)}. استخدم هذه القائمة للتفاوض الهادئ مع البائع."
                AppLanguage.FR -> "Aucun défaut critique bloquant n'a été relevé, mais ${negotiableProblems.size} point(s) nécessitent des frais estimés entre ${ReportEngine.formatDzd(minTotalRepairDzd)} et ${ReportEngine.formatDzd(maxTotalRepairDzd)}. Utilisez ce chiffrage pour négocier sereinement."
                AppLanguage.EN -> "No critical deal-breakers were found, but ${negotiableProblems.size} item(s) need attention costing roughly ${ReportEngine.formatDzd(minTotalRepairDzd)} to ${ReportEngine.formatDzd(maxTotalRepairDzd)}. Use this breakdown to negotiate a fair price."
            }
            else -> when (lang) {
                AppLanguage.AR -> "اجتازت السيارة النقاط المفحوصة بنجاح ($checkedCount/$totalApplicableCount نقطة مفحوصة)."
                AppLanguage.FR -> "Le véhicule a passé les contrôles vérifiés avec succès ($checkedCount/$totalApplicableCount points vérifiés)."
                AppLanguage.EN -> "The vehicle passed the checked points cleanly ($checkedCount/$totalApplicableCount items checked)."
            }
        }
        return "$mainSummary $uncheckedNote"
    }
}

object ReportEngine {

    fun buildReport(
        inspection: InspectionEntity,
        savedAnswers: List<ChecklistAnswerEntity>
    ): InspectionReportData {
        val isQuickMode = inspection.inspectionMode.equals("QUICK", ignoreCase = true)
        val adaptiveItems: List<ChecklistItemDefinition> = ChecklistEngine.buildChecklistForCar(
            brand = inspection.carBrand,
            model = inspection.carModel,
            fuelType = inspection.fuelType,
            gearboxType = inspection.gearboxType,
            isQuickMode = isQuickMode
        )

        val answersById = savedAnswers.associateBy { it.itemId }

        val allFindings = mutableListOf<ReportFindingItem>()
        var totalMaxPoints = 0
        var deductedPoints = 0
        var hasCriticalProblem = false
        var importantProblemCount = 0

        for (def in adaptiveItems) {
            val saved = answersById[def.id]
            val status = saved?.status ?: "UNCHECKED"
            if (status == "GOOD" || status == "PROBLEM") {
                totalMaxPoints += def.severity.weight
                if (status == "PROBLEM") {
                    deductedPoints += def.severity.weight
                    if (def.severity == ItemSeverity.CRITICAL) {
                        hasCriticalProblem = true
                    } else if (def.severity == ItemSeverity.IMPORTANT) {
                        importantProblemCount++
                    }
                }
            }

            allFindings.add(
                ReportFindingItem(
                    itemId = def.id,
                    sectionTitleFr = def.section.titleFr,
                    sectionTitleAr = def.section.titleAr,
                    sectionTitleEn = def.section.titleEn,
                    questionFr = def.questionFr,
                    questionAr = def.questionAr,
                    questionEn = def.questionEn,
                    explanationFr = if (status == "PROBLEM") def.badResultFr else def.goodResultFr,
                    explanationAr = if (status == "PROBLEM") def.badResultAr else def.goodResultAr,
                    explanationEn = if (status == "PROBLEM") def.badResultEn else def.goodResultEn,
                    status = status,
                    severity = def.severity,
                    minCostDzd = if (status == "PROBLEM") def.minRepairCostDzd else 0,
                    maxCostDzd = if (status == "PROBLEM") def.maxRepairCostDzd else 0,
                    avgCostDzd = if (status == "PROBLEM") def.getAverageCostDzd() else 0,
                    buyerNote = saved?.note ?: "",
                    photoUri = saved?.photoUri,
                    isKnownModelWeakPoint = def.isKnownModelWeakPoint
                )
            )
        }

        // Also include synthetic OBD2 ECU scan answer if recorded
        val obdSaved = answersById["obd_ecu_diagnostic_scan"]
        if (obdSaved != null) {
            val obdSeverity = try {
                ItemSeverity.valueOf(obdSaved.severity)
            } catch (_: Exception) {
                ItemSeverity.IMPORTANT
            }
            if (obdSaved.status == "GOOD" || obdSaved.status == "PROBLEM") {
                totalMaxPoints += obdSeverity.weight
                if (obdSaved.status == "PROBLEM") {
                    deductedPoints += obdSeverity.weight
                    if (obdSeverity == ItemSeverity.CRITICAL) {
                        hasCriticalProblem = true
                    } else {
                        importantProblemCount++
                    }
                }
            }
            val estAvg = obdSaved.estimatedCostDzd
            val estMin = (estAvg * 0.7).toInt()
            val estMax = (estAvg * 1.35).toInt()
            allFindings.add(
                ReportFindingItem(
                    itemId = obdSaved.itemId,
                    sectionTitleFr = "Diagnostic Électronique OBD2 (ELM327)",
                    sectionTitleAr = "الفحص الإلكتروني للكمبيوتر OBD2",
                    sectionTitleEn = "OBD2 Electronic ECU Scan",
                    questionFr = obdSaved.questionFr,
                    questionAr = obdSaved.questionAr,
                    questionEn = obdSaved.questionEn,
                    explanationFr = obdSaved.note.ifBlank { "Scan calculateur OBD2 effectué" },
                    explanationAr = obdSaved.note.ifBlank { "تم إجراء فحص الكمبيوتر OBD2" },
                    explanationEn = obdSaved.note.ifBlank { "OBD2 ECU scan performed" },
                    status = obdSaved.status,
                    severity = obdSeverity,
                    minCostDzd = if (obdSaved.status == "PROBLEM") estMin else 0,
                    maxCostDzd = if (obdSaved.status == "PROBLEM") estMax else 0,
                    avgCostDzd = if (obdSaved.status == "PROBLEM") estAvg else 0,
                    buyerNote = obdSaved.note,
                    photoUri = null,
                    isKnownModelWeakPoint = false
                )
            )
        }

        val criticalProblems = allFindings.filter { it.status == "PROBLEM" && it.severity == ItemSeverity.CRITICAL }
        val negotiableProblems = allFindings.filter { it.status == "PROBLEM" && it.severity != ItemSeverity.CRITICAL }
        val passedItems = allFindings.filter { it.status == "GOOD" }
        val uncheckedItems = allFindings.filter { it.status == "UNCHECKED" }
        val uncheckedCriticalItems = uncheckedItems.filter { it.severity == ItemSeverity.CRITICAL }

        val checkedCount = passedItems.size + criticalProblems.size + negotiableProblems.size
        val totalApplicableCount = allFindings.size.coerceAtLeast(1)
        val coveragePercent = ((checkedCount.toDouble() / totalApplicableCount.toDouble()) * 100.0).toInt().coerceIn(0, 100)

        val minimumRequiredCount = kotlin.math.ceil(totalApplicableCount * ChecklistEngine.MINIMUM_ANSWERED_SHARE).toInt().coerceAtLeast(1)
        val isMinimumThresholdMet = checkedCount >= minimumRequiredCount && uncheckedCriticalItems.isEmpty() && totalMaxPoints > 0

        val rawPercentage: Int? = if (isMinimumThresholdMet) {
            (((totalMaxPoints - deductedPoints).toDouble() / totalMaxPoints.toDouble()) * 100.0)
                .toInt()
                .coerceIn(0, 100)
        } else {
            null
        }

        val finalScore: Int? = if (rawPercentage != null) {
            when {
                hasCriticalProblem -> minOf(rawPercentage, 42)
                importantProblemCount >= 3 -> minOf(rawPercentage, 68)
                else -> rawPercentage
            }
        } else {
            null
        }

        val isCriticalCapTriggered = rawPercentage != null && hasCriticalProblem && rawPercentage > 42
        val isImportantCapTriggered = rawPercentage != null && !hasCriticalProblem && importantProblemCount >= 3 && rawPercentage > 68

        val verdict = if (finalScore == null) {
            "INCOMPLETE"
        } else {
            when {
                hasCriticalProblem || finalScore < 50 -> "AVOID"
                finalScore < 78 || importantProblemCount >= 1 || allFindings.any { it.status == "PROBLEM" } -> "NEGOTIATE"
                else -> "GOOD"
            }
        }

        val minTotalDzd = allFindings.filter { it.status == "PROBLEM" }.sumOf { it.minCostDzd }
        val maxTotalDzd = allFindings.filter { it.status == "PROBLEM" }.sumOf { it.maxCostDzd }
        val avgTotalDzd = allFindings.filter { it.status == "PROBLEM" }.sumOf { it.avgCostDzd }

        return InspectionReportData(
            inspection = inspection,
            overallScore = finalScore,
            rawUncappedScore = rawPercentage,
            isCriticalCapTriggered = isCriticalCapTriggered,
            isImportantCapTriggered = isImportantCapTriggered,
            verdict = verdict,
            criticalProblems = criticalProblems,
            negotiableProblems = negotiableProblems,
            passedItems = passedItems,
            uncheckedItems = uncheckedItems,
            uncheckedCriticalItems = uncheckedCriticalItems,
            minimumRequiredCount = minimumRequiredCount,
            isMinimumThresholdMet = isMinimumThresholdMet,
            minTotalRepairDzd = minTotalDzd,
            maxTotalRepairDzd = maxTotalDzd,
            avgTotalRepairDzd = avgTotalDzd,
            checkedCount = checkedCount,
            totalApplicableCount = totalApplicableCount,
            coveragePercent = coveragePercent,
            hasObdScanIncluded = obdSaved != null,
            obdSummaryNote = obdSaved?.note
        )
    }

    /**
     * Formats DZD with space thousands separator (e.g. 45000 -> "45 000 DZD")
     */
    fun formatDzd(amountDzd: Int): String {
        val formatted = String.format(Locale.US, "%,d", amountDzd).replace(',', ' ')
        return "$formatted DZD"
    }

    /**
     * Converts DZD to Algerian "Millions de centimes" (1 Million centimes = 10,000 DZD)
     * Example: 45,000 DZD -> "4.5 Millions"
     */
    fun formatMillionsCentimes(amountDzd: Int, lang: AppLanguage): String {
        val millions = amountDzd.toDouble() / 10000.0
        val numStr = if (millions % 1.0 == 0.0) {
            millions.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", millions)
        }
        return when (lang) {
            AppLanguage.AR -> "$numStr مليون سنتيم"
            AppLanguage.FR -> "$numStr Millions"
            AppLanguage.EN -> "$numStr Million centimes"
        }
    }

    fun formatAlgerianCurrency(amountDzd: Int, lang: AppLanguage): String {
        if (amountDzd <= 0) return "0 DZD"
        return "${formatDzd(amountDzd)} (${formatMillionsCentimes(amountDzd, lang)})"
    }

    /**
     * Calculates a fair negotiation offer range in DZD given the seller's asking price in Millions de centimes.
     * Returns Pair(minSuggestedOfferDzd, maxSuggestedOfferDzd)
     */
    fun calculateFairOfferRangeDzd(
        askingPriceMillions: Double,
        minRepairDzd: Int,
        maxRepairDzd: Int
    ): Pair<Int, Int> {
        val askingDzd = (askingPriceMillions * 10000.0).toInt().coerceAtLeast(0)
        val minOffer = (askingDzd - maxRepairDzd).coerceAtLeast(0)
        val maxOffer = (askingDzd - minRepairDzd).coerceAtLeast(0)
        return Pair(minOffer, maxOffer)
    }
}
