package com.example.domain

import com.example.core.i18n.AppLanguage
import com.example.data.local.ChecklistAnswerEntity
import com.example.data.local.InspectionEntity
import com.example.data.model.ChecklistItemDefinition
import com.example.data.model.ItemSeverity
import com.example.data.model.VerificationStatus
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
    val isKnownModelWeakPoint: Boolean,
    val verificationStatus: VerificationStatus = VerificationStatus.TO_BE_VERIFIED
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

data class AreaScoreSummary(
    val sectionOrder: Int,
    val titleFr: String,
    val titleAr: String,
    val titleEn: String,
    val checkedCount: Int,
    val passedCount: Int,
    val problemCount: Int,
    val totalCount: Int,
    val scorePercent: Int? // 0..100 when checkedCount > 0, or null when 0 checked
) {
    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> titleAr
        AppLanguage.FR -> titleFr
        AppLanguage.EN -> titleEn
    }
}

data class ObdReportBlockData(
    val isScanned: Boolean,
    val isCommunicationSuccess: Boolean,
    val protocolDescription: String,
    val dtcCount: Int,
    val dtcSummaryText: String,
    val clearedCodesStatus: String, // STRONG_WARNING, CANNOT_RULE_OUT, NONE_NORMAL, NOT_AVAILABLE
    val distanceSinceClearedKm: Int?,
    val warmupsSinceCleared: Int?,
    val milBulbLitAtKeyOn: Boolean,
    val milCommandedByEcu: String, // ON, OFF, NOT_AVAILABLE
    val ecuVin: String,
    val ecuVinComparison: String, // SAME, DIFFERENT, COULD_NOT_COMPARE, NOT_AVAILABLE
    val faultCodeDetails: List<DtcInfo> = emptyList(),
    val liveReadings: List<LiveParameterGauge> = emptyList()
) {
    val hasCriticalAlert: Boolean
        get() = isScanned && (
            !milBulbLitAtKeyOn ||
                clearedCodesStatus == "STRONG_WARNING" ||
                ecuVinComparison == "DIFFERENT" ||
                dtcCount > 0
            )

    fun getConnectionStatusText(lang: AppLanguage): String {
        if (!isScanned) {
            return when (lang) {
                AppLanguage.AR -> "لم يتم إجراء فحص OBD2 (فحص يدوي فقط)"
                AppLanguage.FR -> "Non effectué (inspection visuelle uniquement)"
                AppLanguage.EN -> "Not scanned (visual walkaround only)"
            }
        }
        return if (isCommunicationSuccess) {
            val proto = if (protocolDescription.isNotBlank()) " ($protocolDescription)" else ""
            when (lang) {
                AppLanguage.AR -> "متصل بكمبيوتر السيارة بنجاح$proto"
                AppLanguage.FR -> "Connecté au calculateur moteur$proto"
                AppLanguage.EN -> "Connected to car ECU$proto"
            }
        } else {
            when (lang) {
                AppLanguage.AR -> "تعذر الاتصال بكمبيوتر السيارة (غير متوفر أو لم يستجب)"
                AppLanguage.FR -> "Communication impossible avec le calculateur"
                AppLanguage.EN -> "Could not communicate with car ECU"
            }
        }
    }

    fun getFaultCodesText(lang: AppLanguage): String {
        if (!isScanned || !isCommunicationSuccess) {
            return when (lang) {
                AppLanguage.AR -> "غير متوفر في هذه السيارة / لم يُفحص"
                AppLanguage.FR -> "Non disponible / Non vérifié"
                AppLanguage.EN -> "Not available / Not checked"
            }
        }
        if (dtcCount <= 0 || dtcSummaryText.isBlank() || dtcSummaryText == "0 DTC") {
            return when (lang) {
                AppLanguage.AR -> "0 أكواد أعطال مسجلة (مخزنة أو معلقة أو دائمة) ✓"
                AppLanguage.FR -> "0 code défaut (enregistré, en attente ou permanent) ✓"
                AppLanguage.EN -> "0 fault codes found (stored, pending, or permanent) ✓"
            }
        }
        return when (lang) {
            AppLanguage.AR -> "تم رصد $dtcCount كود عطل: $dtcSummaryText"
            AppLanguage.FR -> "$dtcCount code(s) défaut trouvé(s) : $dtcSummaryText"
            AppLanguage.EN -> "$dtcCount fault code(s) found: $dtcSummaryText"
        }
    }

    fun getClearedCodesText(lang: AppLanguage): String {
        if (!isScanned || !isCommunicationSuccess) {
            return when (lang) {
                AppLanguage.AR -> "غير متوفر في هذه السيارة"
                AppLanguage.FR -> "Non disponible sur cette voiture"
                AppLanguage.EN -> "Not available on this car"
            }
        }
        val distPartFr = distanceSinceClearedKm?.takeIf { it >= 0 }?.let { "$it km depuis effacement" }
        val warmPartFr = warmupsSinceCleared?.takeIf { it >= 0 }?.let { "$it cycles de chauffe" }
        val detailsFr = listOfNotNull(distPartFr, warmPartFr).joinToString(", ")

        val distPartAr = distanceSinceClearedKm?.takeIf { it >= 0 }?.let { "$it كم منذ المسح" }
        val warmPartAr = warmupsSinceCleared?.takeIf { it >= 0 }?.let { "$it دورات تسخين" }
        val detailsAr = listOfNotNull(distPartAr, warmPartAr).joinToString("، ")

        val distPartEn = distanceSinceClearedKm?.takeIf { it >= 0 }?.let { "$it km since clearing" }
        val warmPartEn = warmupsSinceCleared?.takeIf { it >= 0 }?.let { "$it warm-ups" }
        val detailsEn = listOfNotNull(distPartEn, warmPartEn).joinToString(", ")

        return when (clearedCodesStatus) {
            "STRONG_WARNING" -> when (lang) {
                AppLanguage.AR -> "⚠️ تنبيه: تم مسح ذاكرة الأعطال حديثاً (${detailsAr.ifBlank { "مسافة قصيرة جداً" }})"
                AppLanguage.FR -> "⚠️ Attention : mémoire effacée très récemment (${detailsFr.ifBlank { "faible kilométrage" }})"
                AppLanguage.EN -> "⚠️ Warning: fault memory cleared very recently (${detailsEn.ifBlank { "short distance/warm-ups" }})"
            }
            "CANNOT_RULE_OUT" -> when (lang) {
                AppLanguage.AR -> "لا يمكن الجزم بعدم مسح الأعطال حديثاً (اختبارات الجاهزية غير مكتملة وعداد المسافة غير متوفر)"
                AppLanguage.FR -> "Impossible d'exclure un effacement récent (moniteurs incomplets, kilométrage depuis effacement non disponible)"
                AppLanguage.EN -> "Cannot rule out recent code clearing (readiness monitors incomplete; distance counter not available on this car)"
            }
            "NONE_NORMAL" -> when (lang) {
                AppLanguage.AR -> "طبيعي — لا يوجد مؤشر على مسح حديث للذاكرة${if (detailsAr.isNotBlank()) " ($detailsAr)" else ""} ✓"
                AppLanguage.FR -> "Normal — aucun signe d'effacement récent${if (detailsFr.isNotBlank()) " ($detailsFr)" else ""} ✓"
                AppLanguage.EN -> "Normal — no sign of recent code clearing${if (detailsEn.isNotBlank()) " ($detailsEn)" else ""} ✓"
            }
            else -> when (lang) {
                AppLanguage.AR -> "غير متوفر في هذه السيارة"
                AppLanguage.FR -> "Non disponible sur cette voiture"
                AppLanguage.EN -> "Not available on this car"
            }
        }
    }

    fun getCheckEngineLightText(lang: AppLanguage): String {
        if (!isScanned) {
            return when (lang) {
                AppLanguage.AR -> "لم يُفحص"
                AppLanguage.FR -> "Non vérifié"
                AppLanguage.EN -> "Not checked"
            }
        }
        if (!milBulbLitAtKeyOn) {
            return when (lang) {
                AppLanguage.AR -> "⚠️ حرج: لمبة المحرك (Check Engine) لم تضئ عند فتح الكونتاكت (احتمال تعطيل أو إخفاء اللمبة)"
                AppLanguage.FR -> "⚠️ CRITIQUE : Le voyant Check-Engine ne s'est PAS allumé au contact (ampoule grillée ou masquée)"
                AppLanguage.EN -> "⚠️ CRITICAL: Check-Engine light did NOT light up at key-on (bulb burnt out or masked)"
            }
        }
        return when (milCommandedByEcu) {
            "ON" -> when (lang) {
                AppLanguage.AR -> "اللمبة تعمل عند الكونتاكت، والكمبيوتر يأمر بإضاءتها (يوجد عطل نشط)"
                AppLanguage.FR -> "Voyant fonctionnel au contact, commandé ALLUMÉ par le calculateur (défaut actif)"
                AppLanguage.EN -> "Bulb works at key-on; commanded ON by ECU (active fault present)"
            }
            "OFF" -> when (lang) {
                AppLanguage.AR -> "سليم — تضيء عند فتح الكونتاكت وتنطفئ بعد التشغيل ✓"
                AppLanguage.FR -> "Conforme — s'allume au contact et s'éteint normalement ✓"
                AppLanguage.EN -> "Normal — lights up at key-on and turns off normally ✓"
            }
            else -> when (lang) {
                AppLanguage.AR -> "اللمبة تضيء عند الكونتاكت (حالة أمر الكمبيوتر غير متوفرة)"
                AppLanguage.FR -> "Voyant présent au contact (état MIL calculateur non disponible)"
                AppLanguage.EN -> "Bulb lights up at key-on (ECU MIL command status not available)"
            }
        }
    }

    fun getChassisComparisonText(lang: AppLanguage): String {
        if (!isScanned || ecuVin.isBlank()) {
            return when (lang) {
                AppLanguage.AR -> "غير متوفر في هذه السيارة (الكمبيوتر لا يدعم قراءة رقم الشاسيه 09 02)"
                AppLanguage.FR -> "Non disponible sur cette voiture (service 09 02 non supporté par le calculateur)"
                AppLanguage.EN -> "Not available on this car (Service 09 02 not supported by ECU)"
            }
        }
        return when (ecuVinComparison) {
            "SAME" -> when (lang) {
                AppLanguage.AR -> "مطابق للبطاقة الرمادية ($ecuVin) ✓"
                AppLanguage.FR -> "Identique à la carte grise ($ecuVin) ✓"
                AppLanguage.EN -> "Matches the carte grise ($ecuVin) ✓"
            }
            "DIFFERENT" -> when (lang) {
                AppLanguage.AR -> "⚠️ حرج: رقم الشاسيه في الكمبيوتر ($ecuVin) مختلف عن البطاقة الرمادية!"
                AppLanguage.FR -> "⚠️ CRITIQUE : N° de châssis calculateur ($ecuVin) DIFFÉRENT de la carte grise !"
                AppLanguage.EN -> "⚠️ CRITICAL: ECU chassis number ($ecuVin) is DIFFERENT from the carte grise!"
            }
            else -> when (lang) {
                AppLanguage.AR -> "مقروء من الكمبيوتر ($ecuVin) — لم تتم مقارنته بالبطاقة الرمادية"
                AppLanguage.FR -> "Lu dans le calculateur ($ecuVin) — Non comparé à la carte grise"
                AppLanguage.EN -> "Read from ECU ($ecuVin) — Could not compare with carte grise"
            }
        }
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
    val obdSummaryNote: String?,
    val obdBlock: ObdReportBlockData,
    val areaScores: List<AreaScoreSummary> = emptyList()
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

    /**
     * Explicit one-line explanation required by Phase 5 when a critical problem caps the score at 42:
     * Shows both the capped score (42) and raw score, and names the critical item(s).
     */
    fun getCriticalCapOneLineExplanation(lang: AppLanguage): String? {
        if (!isCriticalCapTriggered || overallScore == null || rawUncappedScore == null || criticalProblems.isEmpty()) {
            return null
        }
        val names = criticalProblems.joinToString(if (lang == AppLanguage.AR) " ، " else "; ") {
            it.getQuestion(lang)
        }
        val count = criticalProblems.size
        return when (lang) {
            AppLanguage.AR -> if (count == 1) {
                "تم تقييد النتيجة عند $overallScore (النتيجة الخام من الفحص: $rawUncappedScore/100) بسبب مشكلة حرجة واحدة: $names"
            } else {
                "تم تقييد النتيجة عند $overallScore (النتيجة الخام من الفحص: $rawUncappedScore/100) بسبب $count مشاكل حرجة: $names"
            }
            AppLanguage.FR -> if (count == 1) {
                "Score plafonné à $overallScore (score brut checklist : $rawUncappedScore/100) à cause d'un problème critique : $names"
            } else {
                "Score plafonné à $overallScore (score brut checklist : $rawUncappedScore/100) à cause de $count problèmes critiques : $names"
            }
            AppLanguage.EN -> if (count == 1) {
                "Score capped at $overallScore (raw checklist score: $rawUncappedScore/100) because of 1 critical problem: $names"
            } else {
                "Score capped at $overallScore (raw checklist score: $rawUncappedScore/100) because of $count critical problems: $names"
            }
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
            isCriticalCapTriggered -> {
                val capLine = getCriticalCapOneLineExplanation(lang) ?: ""
                val advice = when (lang) {
                    AppLanguage.AR -> "هذا الأمر يستوجب رأي ميكانيكي مختص قبل دفع أي مبلغ."
                    AppLanguage.FR -> "Cela mérite l'avis d'un mécanicien avant tout engagement."
                    AppLanguage.EN -> "This deserves a trusted mechanic's opinion before committing."
                }
                "$capLine. $advice"
            }
            verdict == "NEGOTIATE" -> when (lang) {
                AppLanguage.AR -> "لم يتم رصد عيوب هيكلية قاتلة، لكن توجد ${negotiableProblems.size} ملاحظات تحتاج صيانة. اطلب من الميكانيكي تقدير تكلفة إصلاحها واخصمها من السعر المطلوب."
                AppLanguage.FR -> "Aucun défaut critique bloquant n'a été relevé, mais ${negotiableProblems.size} point(s) nécessitent des réparations. Faites chiffrer ces points par votre mécanicien pour négocier le prix."
                AppLanguage.EN -> "No critical deal-breakers were found, but ${negotiableProblems.size} item(s) need attention. Have your mechanic quote the repair cost for these items and deduct it from the asking price."
            }
            else -> when (lang) {
                AppLanguage.AR -> "اجتازت السيارة النقاط المفحوصة بنجاح ($checkedCount/$totalApplicableCount نقطة مفحوصة)."
                AppLanguage.FR -> "Le véhicule a passé les contrôles vérifiés avec succès ($checkedCount/$totalApplicableCount points vérifiés)."
                AppLanguage.EN -> "The vehicle passed the checked points cleanly ($checkedCount/$totalApplicableCount items checked)."
            }
        }
        return "$mainSummary $uncheckedNote"
    }

    /**
     * Phase 7 (Item 6): Plain-language summary at the top of the report.
     * Built from deterministic templates in 3 languages (AR, FR, EN) — no AI model, 100% offline.
     * Writes 3 or 4 sentences covering:
     * 1. Main strengths (cleanest inspected areas)
     * 2. Main problems found
     * 3. Electronic OBD2 or unchecked critical context (when applicable)
     * 4. What to ask a mechanic before buying
     */
    fun getPlainLanguageSummary(lang: AppLanguage): String {
        val carLabel = "${inspection.carBrand} ${inspection.carModel} (${inspection.carYear})"
        val topStrongSections = passedItems
            .groupBy { it.getSectionTitle(lang) }
            .entries
            .sortedByDescending { it.value.size }
            .take(2)
            .map { it.key }

        val allProblems = criticalProblems + negotiableProblems
        val topProblemNames = allProblems.take(2).map { it.getQuestion(lang) }

        return when (lang) {
            AppLanguage.AR -> {
                val sentence1 = if (topStrongSections.isNotEmpty()) {
                    "أبرز نقاط القوة في سيارة $carLabel ظهرَت في قسم (${topStrongSections.joinToString(" و ")}) مع اجتياز ${passedItems.size} نقطة فحص بنجاح."
                } else {
                    "شمل فحص سيارة $carLabel مراجعة $checkedCount نقطة من أصل $totalApplicableCount نقطة مبرمجة لهذا الموديل."
                }

                val sentence2 = when {
                    criticalProblems.isNotEmpty() -> {
                        "أما أبرز المشاكل المرصودة فهي ${criticalProblems.size} عيب حرج و${negotiableProblems.size} ملاحظة أخرى، وعلى رأسها: ${topProblemNames.joinToString("، ")}."
                    }
                    negotiableProblems.isNotEmpty() -> {
                        "لم تظهر عيوب حرجة قاتلة، لكن سُجلت ${negotiableProblems.size} ملاحظات تحتاج صيانة، أهمها: ${topProblemNames.joinToString("، ")}."
                    }
                    checkedCount > 0 -> {
                        "لم يتم رصد أي عيب ميكانيكي أو هيكلي في النقاط التي تم فحصها."
                    }
                    else -> {
                        "لم يتم إدخال إجابات كافية بعد لتحديد العيوب."
                    }
                }

                val sentence3 = when {
                    obdBlock.isScanned && obdBlock.hasCriticalAlert -> {
                        "كما أظهر فحص الكمبيوتر (OBD2) تنبيهاً إلكترونياً (${obdBlock.getFaultCodesText(lang)}) يستوجب الفحص."
                    }
                    obdBlock.isScanned && obdBlock.isCommunicationSuccess -> {
                        "وأكد فحص الكمبيوتر (OBD2) سلامة الذاكرة الإلكترونية وعدم وجود أكواد أعطال مسجلة."
                    }
                    uncheckedCriticalItems.isNotEmpty() -> {
                        "يرجى الانتباه إلى أن ${uncheckedCriticalItems.size} نقاط حرجة لم يتم فحصها خلال المعاينة."
                    }
                    else -> null
                }

                val sentence4 = when {
                    allProblems.isNotEmpty() -> {
                        "قبل الشراء، اطلب من الميكانيكي رفع السيارة وفحص هذه الملاحظات بدقة وتقدير تكلفة إصلاحها بالدينار لخصمها من السعر."
                    }
                    uncheckedItems.isNotEmpty() -> {
                        "اطلب من الميكانيكي مراجعة النقاط غير المفحوصة (${uncheckedItems.size} نقطة) على رافعة الورشة قبل دفع العربون."
                    }
                    else -> {
                        "اطلب من الميكانيكي إجراء مراقبة روتينية سريعة لأسفل الهيكل والفرامل على الرافعة لتأكيد الحالة الممتازة قبل إتمام البيع."
                    }
                }

                listOfNotNull(sentence1, sentence2, sentence3, sentence4).joinToString(" ")
            }

            AppLanguage.FR -> {
                val sentence1 = if (topStrongSections.isNotEmpty()) {
                    "Sur ce véhicule $carLabel, les principaux points forts concernent les zones « ${topStrongSections.joinToString(" » et « ")} » avec ${passedItems.size} points conformes."
                } else {
                    "L'inspection de ce véhicule $carLabel porte sur $checkedCount point(s) vérifié(s) sur $totalApplicableCount prévus."
                }

                val sentence2 = when {
                    criticalProblems.isNotEmpty() -> {
                        "Les problèmes principaux incluent ${criticalProblems.size} anomalie(s) critique(s) et ${negotiableProblems.size} point(s) secondaire(s), notamment : ${topProblemNames.joinToString(" ; ")}."
                    }
                    negotiableProblems.isNotEmpty() -> {
                        "Aucun défaut critique bloquant n'a été relevé, mais ${negotiableProblems.size} point(s) demandent une remise en état, en particulier : ${topProblemNames.joinToString(" ; ")}."
                    }
                    checkedCount > 0 -> {
                        "Aucune anomalie n'a été signalée sur l'ensemble des points contrôlés."
                    }
                    else -> {
                        "Trop peu de points ont été renseignés pour établir un bilan des défauts."
                    }
                }

                val sentence3 = when {
                    obdBlock.isScanned && obdBlock.hasCriticalAlert -> {
                        "Le diagnostic électronique OBD2 signale également un point d'attention (${obdBlock.getFaultCodesText(lang)})."
                    }
                    obdBlock.isScanned && obdBlock.isCommunicationSuccess -> {
                        "Le scan calculateur OBD2 confirme par ailleurs l'absence de code défaut et une mémoire saine."
                    }
                    uncheckedCriticalItems.isNotEmpty() -> {
                        "Notez toutefois que ${uncheckedCriticalItems.size} point(s) critique(s) n'ont pas pu être vérifiés sur place."
                    }
                    else -> null
                }

                val sentence4 = when {
                    allProblems.isNotEmpty() -> {
                        "Demandez à un mécanicien de contrôler ces anomalies sur un pont élévateur et de chiffrer les réparations en DZD avant toute négociation."
                    }
                    uncheckedItems.isNotEmpty() -> {
                        "Demandez à votre mécanicien de vérifier sur pont les ${uncheckedItems.size} point(s) non contrôlés avant de conclure l'achat."
                    }
                    else -> {
                        "Demandez simplement à un mécanicien une inspection visuelle finale sur pont (soubassement et freins) pour valider l'achat en toute sérénité."
                    }
                }

                listOfNotNull(sentence1, sentence2, sentence3, sentence4).joinToString(" ")
            }

            AppLanguage.EN -> {
                val sentence1 = if (topStrongSections.isNotEmpty()) {
                    "On this $carLabel, the main strengths are in ${topStrongSections.joinToString(" and ")}, with ${passedItems.size} passed checks overall."
                } else {
                    "This inspection of the $carLabel covers $checkedCount checked item(s) out of $totalApplicableCount applicable checks."
                }

                val sentence2 = when {
                    criticalProblems.isNotEmpty() -> {
                        "The main problems found are ${criticalProblems.size} critical issue(s) and ${negotiableProblems.size} other defect(s), led by: ${topProblemNames.joinToString("; ")}."
                    }
                    negotiableProblems.isNotEmpty() -> {
                        "No critical deal-breakers were found, though ${negotiableProblems.size} item(s) need attention, notably: ${topProblemNames.joinToString("; ")}."
                    }
                    checkedCount > 0 -> {
                        "No mechanical or structural defects were recorded across the inspected items."
                    }
                    else -> {
                        "Not enough items have been answered yet to summarise defects."
                    }
                }

                val sentence3 = when {
                    obdBlock.isScanned && obdBlock.hasCriticalAlert -> {
                        "The electronic OBD2 scan also flagged an alert (${obdBlock.getFaultCodesText(lang)}) that needs verification."
                    }
                    obdBlock.isScanned && obdBlock.isCommunicationSuccess -> {
                        "Furthermore, the OBD2 ECU scan confirmed zero fault codes and normal memory status."
                    }
                    uncheckedCriticalItems.isNotEmpty() -> {
                        "Keep in mind that ${uncheckedCriticalItems.size} critical item(s) could not be checked during the visit."
                    }
                    else -> null
                }

                val sentence4 = when {
                    allProblems.isNotEmpty() -> {
                        "Ask a trusted mechanic to inspect these flagged items on a workshop lift and give you a written repair quote in DZD before negotiating."
                    }
                    uncheckedItems.isNotEmpty() -> {
                        "Ask a mechanic to verify the ${uncheckedItems.size} unchecked item(s) on a lift before committing to buy."
                    }
                    else -> {
                        "Ask a mechanic for a quick final check of the underbody and brakes on a lift to confirm everything before payment."
                    }
                }

                listOfNotNull(sentence1, sentence2, sentence3, sentence4).joinToString(" ")
            }
        }
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
            isQuickMode = isQuickMode,
            generation = inspection.carGeneration,
            engineDescription = inspection.engineDescription,
            carYear = inspection.carYear,
            odometerKm = inspection.odometerKm,
            hasTurbo = inspection.hasTurbo,
            isFourWheelDrive = inspection.isFourWheelDrive,
            bodyType = inspection.bodyType
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

            // Phase 5 Rule: Never use unverified DZD numbers. Only use item costs if VERIFIED_BY_MECHANIC.
            val isCostVerified = def.verificationStatus == VerificationStatus.VERIFIED_BY_MECHANIC
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
                    minCostDzd = if (status == "PROBLEM" && isCostVerified) def.minRepairCostDzd else 0,
                    maxCostDzd = if (status == "PROBLEM" && isCostVerified) def.maxRepairCostDzd else 0,
                    avgCostDzd = if (status == "PROBLEM" && isCostVerified) def.getAverageCostDzd() else 0,
                    buyerNote = saved?.note ?: "",
                    photoUri = saved?.photoUri,
                    isKnownModelWeakPoint = def.isKnownModelWeakPoint,
                    verificationStatus = def.verificationStatus
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
                    minCostDzd = 0,
                    maxCostDzd = 0,
                    avgCostDzd = 0,
                    buyerNote = obdSaved.note,
                    photoUri = null,
                    isKnownModelWeakPoint = false,
                    verificationStatus = VerificationStatus.TO_BE_VERIFIED
                )
            )
        }

        val criticalProblems = allFindings.filter { it.status == "PROBLEM" && it.severity == ItemSeverity.CRITICAL }
        val negotiableProblems = allFindings
            .filter { it.status == "PROBLEM" && it.severity != ItemSeverity.CRITICAL }
            .sortedByDescending { it.severity.weight }
        val passedItems = allFindings.filter { it.status == "GOOD" }
        val uncheckedItems = allFindings.filter { it.status == "UNCHECKED" }
        val uncheckedCriticalItems = uncheckedItems.filter { it.severity == ItemSeverity.CRITICAL }

        // Build per-area scores for each checklist section
        val areaScores: List<AreaScoreSummary> = adaptiveItems
            .groupBy { it.section }
            .entries
            .sortedBy { it.key.order }
            .map { (section, defsInSection) ->
                var areaMaxPoints = 0
                var areaDeducted = 0
                var areaChecked = 0
                var areaPassed = 0
                var areaProblem = 0
                for (def in defsInSection) {
                    val st = answersById[def.id]?.status ?: "UNCHECKED"
                    if (st == "GOOD" || st == "PROBLEM") {
                        areaChecked++
                        areaMaxPoints += def.severity.weight
                        if (st == "GOOD") {
                            areaPassed++
                        } else {
                            areaProblem++
                            areaDeducted += def.severity.weight
                        }
                    }
                }
                val areaScorePct: Int? = if (areaChecked > 0 && areaMaxPoints > 0) {
                    (((areaMaxPoints - areaDeducted).toDouble() / areaMaxPoints.toDouble()) * 100.0)
                        .toInt()
                        .coerceIn(0, 100)
                } else {
                    null
                }
                AreaScoreSummary(
                    sectionOrder = section.order,
                    titleFr = section.titleFr,
                    titleAr = section.titleAr,
                    titleEn = section.titleEn,
                    checkedCount = areaChecked,
                    passedCount = areaPassed,
                    problemCount = areaProblem,
                    totalCount = defsInSection.size,
                    scorePercent = areaScorePct
                )
            }

        val checkedCount = passedItems.size + criticalProblems.size + negotiableProblems.size
        val totalApplicableCount = allFindings.size.coerceAtLeast(1)
        val coveragePercent = ((checkedCount.toDouble() / totalApplicableCount.toDouble()) * 100.0).toInt().coerceIn(0, 100)

        val minimumRequiredCount = kotlin.math.ceil(totalApplicableCount * ChecklistEngine.MINIMUM_ANSWERED_SHARE).toInt().coerceAtLeast(1)
        val isMinimumThresholdMet = checkedCount >= minimumRequiredCount && totalMaxPoints > 0

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
                importantProblemCount >= 3 || uncheckedCriticalItems.isNotEmpty() -> minOf(rawPercentage, 68)
                else -> rawPercentage
            }
        } else {
            null
        }

        val isCriticalCapTriggered = rawPercentage != null && hasCriticalProblem && rawPercentage > 42
        val isImportantCapTriggered = rawPercentage != null && !hasCriticalProblem && (importantProblemCount >= 3 || uncheckedCriticalItems.isNotEmpty()) && rawPercentage > 68

        val verdict = if (finalScore == null) {
            "INCOMPLETE"
        } else {
            when {
                hasCriticalProblem || finalScore < 50 -> "AVOID"
                uncheckedCriticalItems.isNotEmpty() || finalScore < 78 || importantProblemCount >= 1 || allFindings.any { it.status == "PROBLEM" } -> "NEGOTIATE"
                else -> "GOOD"
            }
        }

        // Only use user-entered mechanic quote (or verified costs if any exist)
        val userMechanicQuoteDzd = inspection.userQuotedRepairDzd.coerceAtLeast(0)
        val verifiedMinDzd = allFindings.filter { it.status == "PROBLEM" }.sumOf { it.minCostDzd }
        val verifiedMaxDzd = allFindings.filter { it.status == "PROBLEM" }.sumOf { it.maxCostDzd }
        val verifiedAvgDzd = allFindings.filter { it.status == "PROBLEM" }.sumOf { it.avgCostDzd }

        val effectiveMinDzd = if (userMechanicQuoteDzd > 0) userMechanicQuoteDzd else verifiedMinDzd
        val effectiveMaxDzd = if (userMechanicQuoteDzd > 0) userMechanicQuoteDzd else verifiedMaxDzd
        val effectiveAvgDzd = if (userMechanicQuoteDzd > 0) userMechanicQuoteDzd else verifiedAvgDzd

        val obdBlock = buildObdReportBlock(inspection, obdSaved, savedAnswers)

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
            minTotalRepairDzd = effectiveMinDzd,
            maxTotalRepairDzd = effectiveMaxDzd,
            avgTotalRepairDzd = effectiveAvgDzd,
            checkedCount = checkedCount,
            totalApplicableCount = totalApplicableCount,
            coveragePercent = coveragePercent,
            hasObdScanIncluded = obdBlock.isScanned,
            obdSummaryNote = obdSaved?.note,
            obdBlock = obdBlock,
            areaScores = areaScores
        )
    }

    private fun parseFaultCodeDetails(dtcSummaryText: String): List<DtcInfo> {
        if (dtcSummaryText.isBlank() || dtcSummaryText == "0 DTC") return emptyList()
        val regex = Regex("""([PCBU]\d[0-9A-F]{3})(?:\s*\((STORED|PENDING|PERMANENT)\))?""", RegexOption.IGNORE_CASE)
        return regex.findAll(dtcSummaryText).map { match ->
            val code = match.groupValues[1].uppercase(Locale.US)
            val typeStr = match.groupValues.getOrNull(2)?.uppercase(Locale.US).orEmpty()
            val dtcType = when (typeStr) {
                "PENDING" -> DtcType.PENDING
                "PERMANENT" -> DtcType.PERMANENT
                else -> DtcType.STORED
            }
            ObdDiagnosticEngine.lookupDtc(code, dtcType)
        }.distinctBy { "${it.code}_${it.dtcType}" }.toList()
    }

    private fun parseLiveReadingsFromInspection(
        inspection: InspectionEntity,
        savedAnswers: List<ChecklistAnswerEntity>
    ): List<LiveParameterGauge> {
        val liveTagMatch = Regex("""\[OBD2 Live:\s*(.*?)]""").find(inspection.notes)
        if (liveTagMatch != null) {
            val rawPayload = liveTagMatch.groupValues[1]
            val parsed = rawPayload.split(";").mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size >= 3) {
                    val idAndVal = parts[0]
                    val id = idAndVal.substringBefore("=").trim()
                    val valueText = idAndVal.substringAfter("=").trim()
                    val normalRange = parts[1].trim()
                    val status = parts[2].trim()
                    val (nameFr, nameAr, nameEn) = when (id) {
                        "temp" -> Triple("Température liquide (Coolant)", "حرارة سائل التبريد", "Coolant Temperature")
                        "rpm" -> Triple("Régime ralenti (Idle RPM)", "سرعة دوران المحرك (Ralenti)", "Idle Engine Speed")
                        "volt" -> Triple("Tension batterie & alternateur", "فولتية البطارية والدينامو", "Charging Voltage")
                        "trim" -> Triple("Correction carburant (Fuel Trim)", "تصحيح خليط الوقود (Fuel Trim)", "Fuel Trim (STFT/LTFT)")
                        "o2" -> Triple("Sonde Lambda (Capteur O2)", "حساس الأكسجين (Sonde Lambda)", "Oxygen Sensor (O2)")
                        else -> Triple(id, id, id)
                    }
                    LiveParameterGauge(
                        id = id,
                        nameFr = nameFr,
                        nameAr = nameAr,
                        nameEn = nameEn,
                        valueText = valueText,
                        normalRangeText = normalRange,
                        status = status
                    )
                } else null
            }
            if (parsed.isNotEmpty()) return parsed
        }

        // Fallback: extract any live readings recorded in auto-answered checklist items
        val fallbackList = mutableListOf<LiveParameterGauge>()
        val byId = savedAnswers.associateBy { it.itemId }
        byId["after_temp_fan_operation"]?.takeIf { it.note.contains("°C") }?.let { ans ->
            val valStr = Regex("""(-?\d+\s*°C)""").find(ans.note)?.value ?: ans.note
            fallbackList.add(
                LiveParameterGauge(
                    id = "temp",
                    nameFr = "Température liquide (Coolant)",
                    nameAr = "حرارة سائل التبريد",
                    nameEn = "Coolant Temperature",
                    valueText = valStr,
                    normalRangeText = "80 - 98 °C",
                    status = if (ans.status == "GOOD") "GREEN" else "RED"
                )
            )
        }
        byId["start_idle_rpm_stability"]?.takeIf { it.note.contains("rpm", ignoreCase = true) || it.note.contains("tr/min", ignoreCase = true) || it.note.contains("د/د") }?.let { ans ->
            val valStr = ans.note.substringAfter(":").trim().ifBlank { ans.note }
            fallbackList.add(
                LiveParameterGauge(
                    id = "rpm",
                    nameFr = "Régime ralenti (Idle RPM)",
                    nameAr = "سرعة دوران المحرك (Ralenti)",
                    nameEn = "Idle Engine Speed",
                    valueText = valStr,
                    normalRangeText = "680 - 900 tr/min",
                    status = if (ans.status == "GOOD") "GREEN" else "ORANGE"
                )
            )
        }
        byId["bonnet_battery_alternator"]?.takeIf { it.note.contains("V") }?.let { ans ->
            val valStr = Regex("""(\d+(?:[.,]\d+)?(?:\s*-\s*\d+(?:[.,]\d+)?)?\s*V)""").find(ans.note)?.value ?: ans.note
            fallbackList.add(
                LiveParameterGauge(
                    id = "volt",
                    nameFr = "Tension batterie & alternateur",
                    nameAr = "فولتية البطارية والدينامو",
                    nameEn = "Charging Voltage",
                    valueText = valStr,
                    normalRangeText = "13.5 - 14.7 V",
                    status = if (ans.status == "GOOD") "GREEN" else "RED"
                )
            )
        }
        return fallbackList
    }

    private fun buildObdReportBlock(
        inspection: InspectionEntity,
        obdSaved: ChecklistAnswerEntity?,
        savedAnswers: List<ChecklistAnswerEntity> = emptyList()
    ): ObdReportBlockData {
        val parsedLive = parseLiveReadingsFromInspection(inspection, savedAnswers)
        if (inspection.hasObdScan) {
            return ObdReportBlockData(
                isScanned = true,
                isCommunicationSuccess = inspection.obdCommunicationSuccess,
                protocolDescription = inspection.obdProtocolDescription,
                dtcCount = inspection.obdDtcCount,
                dtcSummaryText = inspection.obdDtcCodesSummary,
                clearedCodesStatus = inspection.obdClearedStatus,
                distanceSinceClearedKm = inspection.obdDistanceSinceClearedKm.takeIf { it >= 0 },
                warmupsSinceCleared = inspection.obdWarmupsSinceCleared.takeIf { it >= 0 },
                milBulbLitAtKeyOn = inspection.obdMilBulbLitAtKeyOn,
                milCommandedByEcu = inspection.obdMilCommandedByEcu,
                ecuVin = inspection.obdEcuVin.ifBlank { inspection.vin },
                ecuVinComparison = inspection.obdEcuVinComparison,
                faultCodeDetails = parseFaultCodeDetails(inspection.obdDtcCodesSummary),
                liveReadings = parsedLive
            )
        }
        if (obdSaved != null) {
            val note = obdSaved.note
            val isCommOk = obdSaved.status != "UNCHECKED"
            val hasClearedWarn = note.contains("remise à zéro", ignoreCase = true) ||
                note.contains("effacée", ignoreCase = true)
            val hasMilWarn = note.contains("Voyant Check-Engine absent", ignoreCase = true)
            val hasVinDiff = note.contains("différent de la carte grise", ignoreCase = true)
            val hasVinSame = note.contains("= Carte grise", ignoreCase = true)
            val summaryCodes = note.removePrefix("[OBD2 Scan: ").substringBefore(" |").removeSuffix("]")
            return ObdReportBlockData(
                isScanned = true,
                isCommunicationSuccess = isCommOk,
                protocolDescription = "",
                dtcCount = if (note.contains("0 DTC")) 0 else if (obdSaved.status == "PROBLEM" && !hasClearedWarn && !hasMilWarn && !hasVinDiff) 1 else 0,
                dtcSummaryText = summaryCodes,
                clearedCodesStatus = if (hasClearedWarn) "STRONG_WARNING" else if (isCommOk) "NONE_NORMAL" else "NOT_AVAILABLE",
                distanceSinceClearedKm = null,
                warmupsSinceCleared = null,
                milBulbLitAtKeyOn = !hasMilWarn,
                milCommandedByEcu = "NOT_AVAILABLE",
                ecuVin = inspection.vin,
                ecuVinComparison = when {
                    hasVinDiff -> "DIFFERENT"
                    hasVinSame -> "SAME"
                    inspection.vin.isNotBlank() -> "COULD_NOT_COMPARE"
                    else -> "NOT_AVAILABLE"
                },
                faultCodeDetails = parseFaultCodeDetails(summaryCodes),
                liveReadings = parsedLive
            )
        }
        return ObdReportBlockData(
            isScanned = false,
            isCommunicationSuccess = false,
            protocolDescription = "",
            dtcCount = 0,
            dtcSummaryText = "",
            clearedCodesStatus = "NOT_AVAILABLE",
            distanceSinceClearedKm = null,
            warmupsSinceCleared = null,
            milBulbLitAtKeyOn = true,
            milCommandedByEcu = "NOT_AVAILABLE",
            ecuVin = inspection.vin,
            ecuVinComparison = if (inspection.vin.isNotBlank()) "COULD_NOT_COMPARE" else "NOT_AVAILABLE",
            faultCodeDetails = emptyList(),
            liveReadings = emptyList()
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
     * Phase 5: Calculates the target offer in DZD by subtracting ONLY the repair cost
     * quoted by the user's own mechanic from the seller's asking price (in Millions de centimes).
     */
    fun calculateFairOfferFromMechanicQuoteDzd(
        askingPriceMillions: Double,
        mechanicQuotedRepairDzd: Int
    ): Int {
        val askingDzd = (askingPriceMillions * 10000.0).toInt().coerceAtLeast(0)
        val safeQuote = mechanicQuotedRepairDzd.coerceAtLeast(0)
        return (askingDzd - safeQuote).coerceAtLeast(0)
    }

    /**
     * Legacy range helper kept for compatibility; uses exact mechanic quote when min == max.
     */
    fun calculateFairOfferRangeDzd(
        askingPriceMillions: Double,
        minRepairDzd: Int,
        maxRepairDzd: Int
    ): Pair<Int, Int> {
        val askingDzd = (askingPriceMillions * 10000.0).toInt().coerceAtLeast(0)
        val minOffer = (askingDzd - maxRepairDzd.coerceAtLeast(0)).coerceAtLeast(0)
        val maxOffer = (askingDzd - minRepairDzd.coerceAtLeast(0)).coerceAtLeast(0)
        return Pair(minOffer, maxOffer)
    }
}
