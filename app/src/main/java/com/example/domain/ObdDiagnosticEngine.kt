package com.example.domain

import com.example.core.i18n.AppLanguage
import com.example.data.model.ItemSeverity

enum class ObdSimulatorProfile(
    val titleFr: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleFr: String,
    val subtitleAr: String,
    val subtitleEn: String
) {
    HEALTHY(
        "Véhicule Sain (RAS)",
        "سيارة سليمة (بدون أعطال)",
        "Healthy Car (No Faults)",
        "0 défaut, non effacé récemment (4 820 km), paramètres moteur normaux",
        "0 كود عطل، لم يتم مسح الذاكرة حديثاً (4820 كم)، قراءات مثالية",
        "0 fault codes, not cleared recently (4,820 km), normal live readings"
    ),
    STORED_FAULTS(
        "Défauts Moteur Stockés (Injection / Turbo)",
        "أعطال مسجلة في المحرك (حاقنات / تيربو)",
        "Stored Engine Faults (Injection / Turbo)",
        "Voyant Check Engine commandé ON, 3 codes défauts (P0299, P0301, P0420)",
        "لمبة Check Engine مفعلة، 3 أكواد أعطال مخزنة (P0299, P0301, P0420)",
        "MIL commanded ON, 3 fault codes stored (P0299, P0301, P0420)"
    ),
    RECENTLY_CLEARED_FRAUD(
        "Codes Effacés juste avant la vente (Suspect)",
        "مسح حديث للأكواد قبل البيع (شبهة تحايل)",
        "Codes Recently Cleared Before Sale (Suspicious)",
        "0 défaut affiché MAIS mémoire effacée il y a seulement 6 km (1 cycle, tests incomplets)",
        "0 عطل ظاهر لكن تم مسح الذاكرة قبل 6 كم فقط (دورة تسخين واحدة، اختبارات غير مكتملة)",
        "0 codes shown BUT memory cleared only 6 km ago (1 warmup cycle, monitors incomplete)"
    ),
    NO_ECU_RESPONSE(
        "Calculateur Non Supporté / Pas de réponse",
        "كمبيوتر السيارة لا يستجيب (موديل قديم)",
        "No ECU Response (Older / Non-OBD2 Car)",
        "Connexion ELM327 établie mais le véhicule ne répond pas au standard OBD2",
        "تم الاتصال بجهاز ELM327 لكن السيارة لا تدعم بروتوكول OBD2 القياسي",
        "ELM327 connected but the vehicle ECU does not respond to standard OBD2"
    );

    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> titleAr
        AppLanguage.FR -> titleFr
        AppLanguage.EN -> titleEn
    }

    fun getSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> subtitleAr
        AppLanguage.FR -> subtitleFr
        AppLanguage.EN -> subtitleEn
    }
}

enum class DtcType(val labelFr: String, val labelAr: String, val labelEn: String) {
    STORED("Confirmé (Mode 03)", "مؤكد (Mode 03)", "Stored (Mode 03)"),
    PENDING("En attente (Mode 07)", "قيد الظهور (Mode 07)", "Pending (Mode 07)"),
    PERMANENT("Permanent (Mode 0A)", "دائم (Mode 0A)", "Permanent (Mode 0A)");

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> labelAr
        AppLanguage.FR -> labelFr
        AppLanguage.EN -> labelEn
    }
}

data class DtcInfo(
    val code: String,
    val dtcType: DtcType = DtcType.STORED,
    val severity: ItemSeverity,
    val titleFr: String,
    val titleAr: String,
    val titleEn: String,
    val plainExplanationFr: String,
    val plainExplanationAr: String,
    val plainExplanationEn: String,
    val minCostDzd: Int,
    val maxCostDzd: Int
) {
    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> titleAr
        AppLanguage.FR -> titleFr
        AppLanguage.EN -> titleEn
    }

    fun getExplanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> plainExplanationAr
        AppLanguage.FR -> plainExplanationFr
        AppLanguage.EN -> plainExplanationEn
    }

    fun getAverageCostDzd(): Int = (minCostDzd + maxCostDzd) / 2
}

data class LiveParameterGauge(
    val id: String,
    val nameFr: String,
    val nameAr: String,
    val nameEn: String,
    val valueText: String,
    val normalRangeText: String,
    val status: String // "GREEN", "ORANGE", "RED"
) {
    fun getName(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> nameAr
        AppLanguage.FR -> nameFr
        AppLanguage.EN -> nameEn
    }
}

data class ObdScanResult(
    val isCommunicationSuccess: Boolean,
    val adapterVersion: String,
    val isCloneAdapter: Boolean,
    val detectedProtocol: String,
    val milCommandedOn: Boolean,
    val storedDtcCount: Int,
    val incompleteReadinessMonitors: Int,
    val totalReadinessMonitors: Int,
    val distanceSinceCodesClearedKm: Int,
    val warmupsSinceCodesCleared: Int,
    val distanceWithMilOnKm: Int,
    val timeSinceCodesClearedMin: Int,
    val ecuVin: String?,
    val dtcList: List<DtcInfo>,
    val freezeFrameSummary: String?,
    val liveParameters: List<LiveParameterGauge>,
    val isRecentlyClearedFraudAlert: Boolean,
    val isSimulated: Boolean = false
)

/**
 * Abstract transport interface so Bluetooth Classic SPP (Android now) and
 * BLE / Wi-Fi (future iPhone version) or Simulator can be swapped cleanly.
 */
interface ObdTransport {
    val transportName: String
    suspend fun connect(): Boolean
    suspend fun sendCommand(rawCommand: String): String
    suspend fun disconnect()
}

object ObdDiagnosticEngine {

    // Offline trilingual database of common OBD2 trouble codes on the Algerian market
    val offlineDtcDatabase: Map<String, DtcInfo> = mapOf(
        "P0299" to DtcInfo(
            code = "P0299",
            severity = ItemSeverity.CRITICAL,
            titleFr = "Pression de suralimentation du turbo trop faible",
            titleAr = "ضعف ضغط شاحن التيربو (Turbo sous-alimentation)",
            titleEn = "Turbocharger / Supercharger Underboost Condition",
            plainExplanationFr = "Le turbo ne fournit pas assez de pression. Cela cause une perte de puissance en montée et peut venir d'une durite percée, de l'électrovanne ou d'un turbo usé.",
            plainExplanationAr = "التيربو لا يعطي الضغط الكافي مما يسبب ضعف العزم في المرتفعات (الطلعة). قد يكون السبب ثقباً في أنبوب الهواء أو بداية تلف التيربو نفسه.",
            plainExplanationEn = "The turbocharger is not building enough boost pressure, causing sluggish uphill acceleration due to a boost leak, faulty solenoid, or worn turbo.",
            minCostDzd = 25000,
            maxCostDzd = 135000
        ),
        "P0300" to DtcInfo(
            code = "P0300",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Ratés d'allumage détectés sur plusieurs cylindres",
            titleAr = "تقطيع أو خلل احتراق في عدة سيلندرات (Ratés d'allumage)",
            titleEn = "Random / Multiple Cylinder Misfire Detected",
            plainExplanationFr = "Le moteur broute ou tremble. Souvent lié aux bougies, bobines d'allumage, injecteurs GPL/Essence ou perte de compression.",
            plainExplanationAr = "المحرك يرجف أو يقطع. غالباً بسبب شمعات الاحتراق (Les bougies)، الكويلات (Bobines) أو حاقنات الغاز/البنزين.",
            plainExplanationEn = "The engine stumbles or shakes at idle or under load. Commonly caused by spark plugs, ignition coils, or fuel/LPG injectors.",
            minCostDzd = 12000,
            maxCostDzd = 55000
        ),
        "P0301" to DtcInfo(
            code = "P0301",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Ratés d'allumage sur le cylindre n°1",
            titleAr = "خلل احتراق (تقطيع) في السيلندر رقم 1",
            titleEn = "Cylinder 1 Misfire Detected",
            plainExplanationFr = "Le cylindre n°1 ne brûle pas correctement le carburant (bobine, bougie, injecteur encrassé ou soupape).",
            plainExplanationAr = "الغرفة رقم 1 في المحرك لا تحرق الوقود بانتظام (بسبب البوجي، البوبينة أو الحاقن).",
            plainExplanationEn = "Cylinder 1 is not firing properly due to a faulty spark plug, coil pack, injector, or valve seating.",
            minCostDzd = 8000,
            maxCostDzd = 45000
        ),
        "P0420" to DtcInfo(
            code = "P0420",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Efficacité du catalyseur inférieure au seuil",
            titleAr = "ضعف كفاءة علبة البيئة / الكاتاليزور (Catalyseur)",
            titleEn = "Catalyst System Efficiency Below Threshold (Bank 1)",
            plainExplanationFr = "Très fréquent en Algérie : le catalyseur est bouché, usé ou a été vidé/supprimé.",
            plainExplanationAr = "شائع جداً في الجزائر: علبة البيئة (Catalyseur) مسدودة أو تالفة أو تم تفريغها مسبقاً.",
            plainExplanationEn = "Very common: the catalytic converter is worn out, clogged, or has been hollowed out.",
            minCostDzd = 20000,
            maxCostDzd = 90000
        ),
        "P0401" to DtcInfo(
            code = "P0401",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Débit insuffisant de la vanne EGR (Recyclage des gaz)",
            titleAr = "انسداد أو ضعف تدفق صمام EGR",
            titleEn = "Exhaust Gas Recirculation (EGR) Flow Insufficient",
            plainExplanationFr = "La vanne EGR est encrassée par la calamine (suie diesel), provoquant des à-coups et de la fumée noire.",
            plainExplanationAr = "صمام EGR مسدود بتراكم الكربون والدخان الأسود مما يسبب ضعف السحب.",
            plainExplanationEn = "The EGR valve is clogged with carbon soot, causing hesitation and black exhaust smoke.",
            minCostDzd = 10000,
            maxCostDzd = 48000
        ),
        "P0087" to DtcInfo(
            code = "P0087",
            severity = ItemSeverity.CRITICAL,
            titleFr = "Pression de la rampe d'injection trop basse (HDi / dCi / TDI)",
            titleAr = "انخفاض ضغط الوقود في مسطرة الحاقنات (Pompe / Injecteurs)",
            titleEn = "Fuel Rail / System Pressure Too Low",
            plainExplanationFr = "La pompe haute pression ou un injecteur fuit. Risque de coupure moteur en roulant et réparation coûteuse.",
            plainExplanationAr = "ضعف في مضخة الضغط العالي (La pompe HP) أو تسريب في الحاقنات. قد يؤدي لتوقف المحرك فجأة.",
            plainExplanationEn = "High-pressure fuel pump or injector leak. Risk of stalling under load and expensive diesel injection repair.",
            minCostDzd = 45000,
            maxCostDzd = 180000
        ),
        "P0171" to DtcInfo(
            code = "P0171",
            severity = ItemSeverity.MINOR,
            titleFr = "Mélange trop pauvre (prise d'air ou réglage GPL)",
            titleAr = "فقر خليط الوقود والهواء (تسرب هواء أو عيار غاز GPL)",
            titleEn = "System Too Lean (Bank 1 - Vacuum leak or LPG calibration)",
            plainExplanationFr = "Trop d'air ou pas assez de carburant. Très fréquent sur les voitures équipées au GPL dont le calculateur gaz est mal réglé.",
            plainExplanationAr = "نسبة الهواء أعلى من الوقود. يظهر كثيراً في سيارات الغاز (GPL) التي تحتاج إعادة ضبط.",
            plainExplanationEn = "Too much air or not enough fuel. Very common on LPG-converted cars needing gas map calibration.",
            minCostDzd = 5000,
            maxCostDzd = 25000
        ),
        "P2002" to DtcInfo(
            code = "P2002",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Filtre à particules (FAP / DPF) colmaté ou défectueux",
            titleAr = "انسداد مصفاة الدخان (FAP / DPF) في محرك الديزل",
            titleEn = "Diesel Particulate Filter (DPF) Efficiency Below Threshold",
            plainExplanationFr = "Le filtre à particules est bouché par la conduite en ville.",
            plainExplanationAr = "فلتر البيئة للديزل (FAP) مسدود بسبب السير داخل المدينة.",
            plainExplanationEn = "The diesel particulate filter is clogged from short urban trips.",
            minCostDzd = 18000,
            maxCostDzd = 85000
        )
    ) + ObdDtcDictionaryExtra.extraDtcMap

    fun lookupDtc(code: String, type: DtcType = DtcType.STORED): DtcInfo {
        val clean = code.uppercase().trim()
        val found = offlineDtcDatabase[clean]
        if (found != null) {
            return found.copy(dtcType = type)
        }
        return DtcInfo(
            code = clean,
            dtcType = type,
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Code défaut calculateur ($clean)",
            titleAr = "كود عطل مسجل في الكمبيوتر ($clean)",
            titleEn = "Engine Control Unit Fault Code ($clean)",
            plainExplanationFr = "Demandez à un mécanicien ce que ce code signifie sur cette voiture.",
            plainExplanationAr = "اسأل ميكانيكيًا عن معنى هذا الكود في هذه السيارة.",
            plainExplanationEn = "Ask a mechanic what this code means on this car.",
            minCostDzd = 0,
            maxCostDzd = 0
        )
    }

    /**
     * Parses raw hexadecimal response from OBD2 Mode 03 / Mode 07 / Mode 0A into 5-char DTC codes
     * Example raw: "43 03 02 99 03 01 04 20" -> ["P0299", "P0301", "P0420"]
     */
    fun parseMode03DtcResponse(rawHex: String, dtcType: DtcType = DtcType.STORED): List<DtcInfo> {
        val cleaned = rawHex.uppercase()
            .replace("SEARCHING...", "")
            .replace("NO DATA", "")
            .replace(">", "")
            .trim()
        if (cleaned.isBlank()) return emptyList()

        val bytes = cleaned.split(Regex("\\s+")).filter { it.length == 2 && it.all { c -> c.isDigit() || c in 'A'..'F' } }
        if (bytes.isEmpty()) return emptyList()

        // Strip response header byte (43, 47, or 4A) and optional count byte if present
        val startIndex = if (bytes[0] in setOf("43", "47", "4A")) {
            if (bytes.size % 2 == 0) 2 else 1
        } else 0

        val codes = mutableListOf<DtcInfo>()
        var i = startIndex
        while (i + 1 < bytes.size) {
            val b1 = bytes[i].toIntOrNull(16) ?: break
            val b2 = bytes[i + 1].toIntOrNull(16) ?: break
            if (b1 != 0 || b2 != 0) {
                val prefixChar = when ((b1 shr 6) and 0x03) {
                    0 -> 'P'
                    1 -> 'C'
                    2 -> 'B'
                    else -> 'U'
                }
                val secondDigit = (b1 shr 4) and 0x03
                val thirdHex = (b1 and 0x0F).toString(16).uppercase()
                val fourthFifthHex = b2.toString(16).uppercase().padStart(2, '0')
                val codeStr = "$prefixChar$secondDigit$thirdHex$fourthFifthHex"
                codes.add(lookupDtc(codeStr, dtcType))
            }
            i += 2
        }
        return codes.distinctBy { it.code }
    }

    /**
     * Parses Mode 01 PID 01 (Monitor status since DTCs cleared):
     * Returns Triple(milOn: Boolean, dtcCount: Int, incompleteMonitorsCount: Int)
     */
    fun parsePid01Status(rawHex: String): Triple<Boolean, Int, Int> {
        val bytes = extractPayloadBytes(rawHex, "41", "01")
        if (bytes.size < 4) return Triple(false, 0, 0)
        val a = bytes[0]
        val b = bytes[1]
        val c = bytes[2]
        val d = bytes[3]

        val milOn = (a and 0x80) != 0
        val dtcCount = a and 0x7F

        // Count incomplete readiness monitors in bytes B, C, D
        var incomplete = 0
        if ((b and 0x40) != 0) incomplete++
        if ((b and 0x20) != 0) incomplete++
        if ((b and 0x10) != 0) incomplete++
        for (bit in 0..7) {
            val supported = (c and (1 shl bit)) != 0
            val notReady = (d and (1 shl bit)) != 0
            if (supported && notReady) incomplete++
        }
        return Triple(milOn, dtcCount, incomplete)
    }

    /**
     * Parses a 2-byte distance PID (PID 21 Distance with MIL on, or PID 31 Distance since codes cleared) in km
     */
    fun parseTwoByteDistanceKm(rawHex: String, pidHex: String): Int {
        val bytes = extractPayloadBytes(rawHex, "41", pidHex)
        if (bytes.size < 2) return 0
        return (bytes[0] * 256) + bytes[1]
    }

    /**
     * Parses a 1-byte PID (PID 30 Warm-ups since codes cleared)
     */
    fun parseSingleByteCount(rawHex: String, pidHex: String): Int {
        val bytes = extractPayloadBytes(rawHex, "41", pidHex)
        return bytes.firstOrNull() ?: 0
    }

    private fun extractPayloadBytes(rawHex: String, modeResp: String, pidHex: String): List<Int> {
        val tokens = rawHex.uppercase()
            .replace(">", " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.length == 2 }
        val idx = tokens.indexOfFirst { it == modeResp }
        val start = if (idx >= 0 && tokens.getOrNull(idx + 1) == pidHex.uppercase()) {
            idx + 2
        } else 0
        return tokens.drop(start).mapNotNull { it.toIntOrNull(16) }
    }

    /**
     * Evaluates whether the car shows signs of recently erased fault codes right before the sale:
     * - 0 stored codes AND (distance since clear < 50 km OR warmups < 3 OR >= 3 readiness monitors incomplete)
     */
    fun detectClearedCodeFraud(
        storedDtcCount: Int,
        distanceSinceClearKm: Int,
        warmupsSinceClear: Int,
        incompleteMonitors: Int
    ): Boolean {
        if (storedDtcCount > 0) return false
        return distanceSinceClearKm in 0..45 || warmupsSinceClear in 0..2 || incompleteMonitors >= 3
    }

    /**
     * Generates a complete realistic ObdScanResult for any of the 4 built-in Simulator Profiles
     */
    fun generateSimulatedScan(
        profile: ObdSimulatorProfile,
        expectedVin: String
    ): ObdScanResult {
        val cleanVin = expectedVin.trim().ifBlank { null }
        return when (profile) {
            ObdSimulatorProfile.HEALTHY -> ObdScanResult(
                isCommunicationSuccess = true,
                adapterVersion = "SIMULATOR ONLY",
                isCloneAdapter = false,
                detectedProtocol = "SIMULATED CAN",
                milCommandedOn = false,
                storedDtcCount = 0,
                incompleteReadinessMonitors = 0,
                totalReadinessMonitors = 8,
                distanceSinceCodesClearedKm = 4820,
                warmupsSinceCodesCleared = 142,
                distanceWithMilOnKm = 0,
                timeSinceCodesClearedMin = 6400,
                ecuVin = cleanVin,
                dtcList = emptyList(),
                freezeFrameSummary = null,
                liveParameters = listOf(
                    LiveParameterGauge("temp", "Température Liquide (Coolant)", "حرارة ماء التبريد", "Coolant Temperature", "89 °C", "82 - 96 °C", "GREEN"),
                    LiveParameterGauge("rpm", "Régime Ralenti (Idle RPM)", "سرعة دوران المحرك (Ralenti)", "Idle Engine Speed", "780 tr/min", "720 - 850 tr/min", "GREEN"),
                    LiveParameterGauge("volt", "Tension Batterie & Alternateur", "فولتية البطارية والدينامو", "Alternator Charging Voltage", "14.1 V", "13.6 - 14.5 V", "GREEN"),
                    LiveParameterGauge("trim", "Correction Carburant (Fuel Trim)", "تصحيح خليط الوقود (STFT/LTFT)", "Short/Long Fuel Trim", "+1.5 %", "-8% à +8%", "GREEN"),
                    LiveParameterGauge("o2", "Sonde Lambda (O2 Sensor)", "حساس الأكسجين (Sonde Lambda)", "O2 Sensor Activity", "0.68 V (Actif)", "0.1 - 0.9 V", "GREEN")
                ),
                isRecentlyClearedFraudAlert = false,
                isSimulated = true
            )

            ObdSimulatorProfile.STORED_FAULTS -> ObdScanResult(
                isCommunicationSuccess = true,
                adapterVersion = "SIMULATOR ONLY",
                isCloneAdapter = false,
                detectedProtocol = "SIMULATED CAN",
                milCommandedOn = true,
                storedDtcCount = 3,
                incompleteReadinessMonitors = 1,
                totalReadinessMonitors = 8,
                distanceSinceCodesClearedKm = 640,
                warmupsSinceCodesCleared = 28,
                distanceWithMilOnKm = 315,
                timeSinceCodesClearedMin = 920,
                ecuVin = cleanVin,
                dtcList = listOf(
                    lookupDtc("P0299", DtcType.STORED),
                    lookupDtc("P0301", DtcType.STORED),
                    lookupDtc("P0420", DtcType.PENDING)
                ),
                freezeFrameSummary = "P0299 enregistré à 2 640 tr/min, 88 km/h, Température moteur 92°C, Charge 84%",
                liveParameters = listOf(
                    LiveParameterGauge("temp", "Température Liquide (Coolant)", "حرارة ماء التبريد", "Coolant Temperature", "95 °C", "82 - 96 °C", "GREEN"),
                    LiveParameterGauge("rpm", "Régime Ralenti (Idle RPM)", "سرعة دوران المحرك (Ralenti)", "Idle Engine Speed", "890 tr/min (Instable)", "720 - 850 tr/min", "ORANGE"),
                    LiveParameterGauge("volt", "Tension Batterie & Alternateur", "فولتية البطارية والدينامو", "Alternator Charging Voltage", "13.8 V", "13.6 - 14.5 V", "GREEN"),
                    LiveParameterGauge("trim", "Correction Carburant (Fuel Trim)", "تصحيح خليط الوقود (STFT/LTFT)", "Short/Long Fuel Trim", "+16.4 % (Riche/Fuite)", "-8% à +8%", "RED"),
                    LiveParameterGauge("o2", "Sonde Lambda (O2 Sensor)", "حساس الأكسجين (Sonde Lambda)", "O2 Sensor Activity", "0.18 V (Lent)", "0.1 - 0.9 V", "ORANGE")
                ),
                isRecentlyClearedFraudAlert = false,
                isSimulated = true
            )

            ObdSimulatorProfile.RECENTLY_CLEARED_FRAUD -> ObdScanResult(
                isCommunicationSuccess = true,
                adapterVersion = "SIMULATOR ONLY",
                isCloneAdapter = false,
                detectedProtocol = "SIMULATED KWP",
                milCommandedOn = false,
                storedDtcCount = 0,
                incompleteReadinessMonitors = 5,
                totalReadinessMonitors = 8,
                distanceSinceCodesClearedKm = 6,
                warmupsSinceCodesCleared = 1,
                distanceWithMilOnKm = 0,
                timeSinceCodesClearedMin = 14,
                ecuVin = cleanVin,
                dtcList = listOf(
                    lookupDtc("P0401", DtcType.PERMANENT)
                ),
                freezeFrameSummary = null,
                liveParameters = listOf(
                    LiveParameterGauge("temp", "Température Liquide (Coolant)", "حرارة ماء التبريد", "Coolant Temperature", "91 °C", "82 - 96 °C", "GREEN"),
                    LiveParameterGauge("rpm", "Régime Ralenti (Idle RPM)", "سرعة دوران المحرك (Ralenti)", "Idle Engine Speed", "810 tr/min", "720 - 850 tr/min", "GREEN"),
                    LiveParameterGauge("volt", "Tension Batterie & Alternateur", "فولتية البطارية والدينامو", "Alternator Charging Voltage", "13.9 V", "13.6 - 14.5 V", "GREEN"),
                    LiveParameterGauge("trim", "Correction Carburant (Fuel Trim)", "تصحيح خليط الوقود (STFT/LTFT)", "Short/Long Fuel Trim", "+11.2 %", "-8% à +8%", "ORANGE")
                ),
                isRecentlyClearedFraudAlert = true,
                isSimulated = true
            )

            ObdSimulatorProfile.NO_ECU_RESPONSE -> ObdScanResult(
                isCommunicationSuccess = false,
                adapterVersion = "SIMULATOR ONLY",
                isCloneAdapter = false,
                detectedProtocol = "UNABLE TO CONNECT (NO OBD2 RESPONSE)",
                milCommandedOn = false,
                storedDtcCount = 0,
                incompleteReadinessMonitors = 0,
                totalReadinessMonitors = 0,
                distanceSinceCodesClearedKm = 0,
                warmupsSinceCodesCleared = 0,
                distanceWithMilOnKm = 0,
                timeSinceCodesClearedMin = 0,
                ecuVin = null,
                dtcList = emptyList(),
                freezeFrameSummary = null,
                liveParameters = emptyList(),
                isRecentlyClearedFraudAlert = false,
                isSimulated = true
            )
        }
    }

    /**
     * Executes a full READ-ONLY diagnostic scan over any [ObdTransport] (such as [BluetoothElm327Transport]).
     * Never invents fallback readings or copies the user's VIN when the car does not respond.
     */
    suspend fun executeRealObdScan(
        transport: ObdTransport,
        expectedVin: String,
        onProgress: (String) -> Unit = {}
    ): ObdScanResult {
        onProgress("Initialisation du boîtier ELM327 (ATZ, ATE0, ATSP0)...")
        val atzResp = transport.sendCommand("AT Z")
        transport.sendCommand("AT E0")
        transport.sendCommand("AT L0")
        transport.sendCommand("AT S0")
        transport.sendCommand("AT H0")
        transport.sendCommand("AT SP 0")

        val adapterVer = if (atzResp.contains("ELM", ignoreCase = true)) {
            atzResp
        } else {
            transport.sendCommand("AT I").ifBlank { "Non communiqué" }
        }
        val isClone = adapterVer.contains("1.5") || adapterVer.contains("2.1")

        onProgress("Lecture de l'état moteur et du voyant Check Engine (PID 0101)...")
        val pid0101 = transport.sendCommand("0101")
        if (pid0101.contains("UNABLE TO CONNECT", ignoreCase = true) ||
            pid0101.contains("NO DATA", ignoreCase = true) ||
            pid0101.contains("ERROR", ignoreCase = true) ||
            pid0101.isBlank()
        ) {
            return ObdScanResult(
                isCommunicationSuccess = false,
                adapterVersion = adapterVer,
                isCloneAdapter = isClone,
                detectedProtocol = "Aucune réponse du calculateur (No ECU response)",
                milCommandedOn = false,
                storedDtcCount = 0,
                incompleteReadinessMonitors = 0,
                totalReadinessMonitors = 0,
                distanceSinceCodesClearedKm = 0,
                warmupsSinceCodesCleared = 0,
                distanceWithMilOnKm = 0,
                timeSinceCodesClearedMin = 0,
                ecuVin = null,
                dtcList = emptyList(),
                freezeFrameSummary = null,
                liveParameters = emptyList(),
                isRecentlyClearedFraudAlert = false,
                isSimulated = false
            )
        }

        val rawProtocol = transport.sendCommand("AT DP").trim()
        val protocolResp = if (rawProtocol.isBlank() || rawProtocol.contains("?")) {
            "Non disponible sur cette voiture"
        } else {
            rawProtocol
        }
        val (milOn, dtcCount, incompleteMonitors) = parsePid01Status(pid0101)

        onProgress("Lecture des codes défauts stockés, en attente et permanents (Modes 03 / 07 / 0A)...")
        val storedDtcs = parseMode03DtcResponse(transport.sendCommand("03"), DtcType.STORED)
        val pendingDtcs = parseMode03DtcResponse(transport.sendCommand("07"), DtcType.PENDING)
        val permanentDtcs = parseMode03DtcResponse(transport.sendCommand("0A"), DtcType.PERMANENT)
        val allDtcs = (storedDtcs + pendingDtcs + permanentDtcs).distinctBy { "${it.code}_${it.dtcType}" }

        onProgress("Vérification d'effacement récent des codes (PIDs 0121, 0130, 0131)...")
        val distMilKm = parseTwoByteDistanceKm(transport.sendCommand("0121"), "21")
        val warmups = parseSingleByteCount(transport.sendCommand("0130"), "30")
        val distSinceClearKm = parseTwoByteDistanceKm(transport.sendCommand("0131"), "31")
        val timeSinceClearMin = parseTwoByteDistanceKm(transport.sendCommand("014E"), "4E")

        onProgress("Lecture des paramètres moteur en direct (Température, Ralenti, Tension)...")
        val coolantBytes = extractPayloadBytes(transport.sendCommand("0105"), "41", "05")
        val coolantC: Int? = if (coolantBytes.isNotEmpty()) coolantBytes[0] - 40 else null

        val rpmBytes = extractPayloadBytes(transport.sendCommand("010C"), "41", "0C")
        val rpmVal: Int? = if (rpmBytes.size >= 2) ((rpmBytes[0] * 256) + rpmBytes[1]) / 4 else null

        val voltRaw = transport.sendCommand("AT RV").trim()
        val parsedVolt: Double? = if (voltRaw.isNotBlank() &&
            !voltRaw.contains("?") &&
            !voltRaw.contains("NO DATA", ignoreCase = true) &&
            !voltRaw.contains("ERROR", ignoreCase = true)
        ) {
            Regex("""(\d+(?:[.,]\d+)?)""").find(voltRaw)?.groupValues?.getOrNull(1)
                ?.replace(',', '.')
                ?.toDoubleOrNull()
                ?.takeIf { it in 5.0..20.0 }
        } else null

        val unavailableLabel = "Non disponible sur cette voiture"

        val liveGauges = listOf(
            LiveParameterGauge(
                id = "temp",
                nameFr = "Température Liquide (Coolant)",
                nameAr = "حرارة ماء التبريد",
                nameEn = "Coolant Temperature",
                valueText = if (coolantC != null) "$coolantC °C" else unavailableLabel,
                normalRangeText = "82 - 96 °C",
                status = when {
                    coolantC == null -> "UNAVAILABLE"
                    coolantC in 75..99 -> "GREEN"
                    coolantC > 102 -> "RED"
                    else -> "ORANGE"
                }
            ),
            LiveParameterGauge(
                id = "rpm",
                nameFr = "Régime Ralenti (Idle RPM)",
                nameAr = "سرعة دوران المحرك (Ralenti)",
                nameEn = "Idle Engine Speed",
                valueText = if (rpmVal != null) "$rpmVal tr/min" else unavailableLabel,
                normalRangeText = "700 - 880 tr/min",
                status = when {
                    rpmVal == null -> "UNAVAILABLE"
                    rpmVal in 680..900 -> "GREEN"
                    else -> "ORANGE"
                }
            ),
            LiveParameterGauge(
                id = "volt",
                nameFr = "Tension Batterie & Alternateur",
                nameAr = "فولتية البطارية والدينامو",
                nameEn = "Alternator Charging Voltage",
                valueText = if (parsedVolt != null) String.format(java.util.Locale.US, "%.1f V", parsedVolt) else unavailableLabel,
                normalRangeText = "13.2 - 14.6 V",
                status = when {
                    parsedVolt == null -> "UNAVAILABLE"
                    parsedVolt in 13.2..14.6 -> "GREEN"
                    parsedVolt in 12.0..13.19 || parsedVolt in 14.61..15.0 -> "ORANGE"
                    else -> "RED"
                }
            )
        )

        val isFraudAlert = detectClearedCodeFraud(
            storedDtcCount = allDtcs.count { it.dtcType == DtcType.STORED },
            distanceSinceClearKm = distSinceClearKm,
            warmupsSinceClear = warmups,
            incompleteMonitors = incompleteMonitors
        )

        return ObdScanResult(
            isCommunicationSuccess = true,
            adapterVersion = adapterVer,
            isCloneAdapter = isClone,
            detectedProtocol = protocolResp,
            milCommandedOn = milOn,
            storedDtcCount = maxOf(dtcCount, storedDtcs.size),
            incompleteReadinessMonitors = incompleteMonitors,
            totalReadinessMonitors = 8,
            distanceSinceCodesClearedKm = distSinceClearKm,
            warmupsSinceCodesCleared = warmups,
            distanceWithMilOnKm = distMilKm,
            timeSinceCodesClearedMin = timeSinceClearMin,
            ecuVin = null,
            dtcList = allDtcs,
            freezeFrameSummary = null,
            liveParameters = liveGauges,
            isRecentlyClearedFraudAlert = isFraudAlert,
            isSimulated = false
        )
    }
}

