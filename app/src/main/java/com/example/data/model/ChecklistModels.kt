package com.example.data.model

import com.example.core.i18n.AppLanguage

enum class ChecklistSection(
    val order: Int,
    val titleFr: String,
    val titleAr: String,
    val titleEn: String,
    val locationHintFr: String,
    val locationHintAr: String,
    val locationHintEn: String
) {
    BEFORE_VISIT(
        1,
        "1. Avant la visite",
        "1. قبل المعاينة",
        "1. Before the Visit",
        "Par téléphone et préparation",
        "عبر الهاتف والتحضير للمعاينة",
        "By phone & preparation"
    ),
    DOCUMENTS_IDENTITY(
        2,
        "2. Papiers & Identité",
        "2. الوثائق والهوية",
        "2. Documents & Identity",
        "Carte grise, châssis VIN et plaque",
        "البطاقة الرمادية، رقم الشاسيه والترقيم",
        "Carte grise, VIN stamp & registration plate"
    ),
    EXTERIOR(
        3,
        "3. Extérieur & Carrosserie",
        "3. الهيكل الخارجي والإطارات",
        "3. Exterior & Bodywork",
        "Tour du véhicule à la lumière du jour",
        "جولة حول السيارة في ضوء النهار",
        "Walk around the car in daylight"
    ),
    UNDER_BONNET_COLD(
        4,
        "4. Sous le capot (Moteur froid)",
        "4. تحت غطاء المحرك (بارد)",
        "4. Under the Bonnet (Engine Cold)",
        "Capot ouvert, avant de démarrer",
        "غطاء المحرك مفتوح قبل التشغيل",
        "Hood open, before starting engine"
    ),
    INTERIOR(
        5,
        "5. Habitacle & Équipements",
        "5. المقصورة الداخلية والتجهيزات",
        "5. Interior & Controls",
        "Siège conducteur, tableau de bord et coffre",
        "مقعد السائق، الطابلوه وصندوق الخلف",
        "Driver seat, dashboard & trunk"
    ),
    UNDERNEATH_SUSPENSION(
        6,
        "6. Soubassement & Suspension",
        "6. أسفل السيارة ونظام التعليق",
        "6. Underneath & Suspension",
        "Sous la voiture et passages de roues",
        "تحت الهيكل وعند العجلات الأربع",
        "Underbody & wheel arches"
    ),
    ENGINE_START(
        7,
        "7. Démarrage & Ralenti",
        "7. تشغيل المحرك والدخان",
        "7. Engine Start & Idle",
        "Premier démarrage à froid et échappement",
        "أول تشغيل للمحرك ومراقبة العادم",
        "First cold start & exhaust check"
    ),
    TEST_DRIVE(
        8,
        "8. Essai routier",
        "8. القيادة التجريبية",
        "8. Test Drive",
        "Sur route : boîte, embrayage, freins",
        "على الطريق: القير، الدبرياج والفرامل",
        "On road: gearbox, clutch & brakes"
    ),
    AFTER_DRIVE(
        9,
        "9. Après l'essai",
        "9. بعد القيادة التجريبية",
        "9. After the Drive",
        "Moteur chaud : ventilateur et fuites",
        "المحرك ساخن: المروحة والتسريبات",
        "Warm engine: cooling fan & leaks"
    );

    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> titleAr
        AppLanguage.FR -> titleFr
        AppLanguage.EN -> titleEn
    }

    fun getLocationHint(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> locationHintAr
        AppLanguage.FR -> locationHintFr
        AppLanguage.EN -> locationHintEn
    }
}

enum class ItemSeverity(
    val weight: Int,
    val labelFr: String,
    val labelAr: String,
    val labelEn: String
) {
    MINOR(3, "Mineur", "بسيط", "Minor"),
    IMPORTANT(8, "Important", "مهم", "Important"),
    CRITICAL(20, "Critique (Alerte)", "حرج (خطير)", "Critical");

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> labelAr
        AppLanguage.FR -> labelFr
        AppLanguage.EN -> labelEn
    }
}

data class ChecklistItemDefinition(
    val id: String,
    val section: ChecklistSection,
    val severity: ItemSeverity,
    val includedInQuickCheck: Boolean,
    val requiredFuels: Set<String> = emptySet(), // empty = all fuels; or "DIESEL", "GPL", "ESSENCE"
    val requiredGearboxes: Set<String> = emptySet(), // empty = all; or "MANUELLE", "AUTOMATIQUE"
    val specificModelIds: Set<String> = emptySet(), // empty = all models; or e.g. "peugeot_208"
    val isKnownModelWeakPoint: Boolean = false,
    val questionFr: String,
    val questionAr: String,
    val questionEn: String,
    val whereToLookFr: String,
    val whereToLookAr: String,
    val whereToLookEn: String,
    val howToCheckFr: String,
    val howToCheckAr: String,
    val howToCheckEn: String,
    val goodResultFr: String,
    val goodResultAr: String,
    val goodResultEn: String,
    val badResultFr: String,
    val badResultAr: String,
    val badResultEn: String,
    val minRepairCostDzd: Int = 0,
    val maxRepairCostDzd: Int = 0
) {
    fun getQuestion(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> questionAr
        AppLanguage.FR -> questionFr
        AppLanguage.EN -> questionEn
    }

    fun getWhereToLook(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> whereToLookAr
        AppLanguage.FR -> whereToLookFr
        AppLanguage.EN -> whereToLookEn
    }

    fun getHowToCheck(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> howToCheckAr
        AppLanguage.FR -> howToCheckFr
        AppLanguage.EN -> howToCheckEn
    }

    fun getGoodResult(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> goodResultAr
        AppLanguage.FR -> goodResultFr
        AppLanguage.EN -> goodResultEn
    }

    fun getBadResult(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> badResultAr
        AppLanguage.FR -> badResultFr
        AppLanguage.EN -> badResultEn
    }

    fun getAverageCostDzd(): Int =
        if (minRepairCostDzd > 0 && maxRepairCostDzd > 0) (minRepairCostDzd + maxRepairCostDzd) / 2
        else maxRepairCostDzd
}
