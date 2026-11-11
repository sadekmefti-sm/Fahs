package com.example.domain

import com.example.core.i18n.AppLanguage
import com.example.data.model.ItemSeverity
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

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
        "Codes EffacésRécemment (Distance faible)",
        "مسح حديث للأكواد (مسافة قصيرة منذ المسح)",
        "Codes Recently Cleared (Short distance since reset)",
        "0 défaut affiché mais mémoire remise à zéro il y a 6 km (1 cycle)",
        "0 عطل ظاهر لكن تمت إعادة ضبط الذاكرة قبل 6 كم (دورة تسخين واحدة)",
        "0 codes shown but memory reset 6 km ago (1 warm-up cycle)"
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

enum class ClearedCodesAlertLevel {
    /** Values were read and show normal history (not cleared recently). */
    NONE_NORMAL,
    /** Distance/warmups were not available or normal, but >= 3 readiness monitors are incomplete with 0 codes. */
    CANNOT_RULE_OUT,
    /** Actually read distance (< 50 km) or warm-ups (< 3) since clear with 0 stored codes. */
    STRONG_WARNING,
    /** Car did not provide cleared-codes metrics, or stored fault codes are already present. */
    NOT_AVAILABLE
}

enum class EcuVinComparisonChoice {
    SAME,
    DIFFERENT,
    COULD_NOT_COMPARE
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
    val status: String // "GREEN", "ORANGE", "RED", "UNAVAILABLE"
) {
    fun getName(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> nameAr
        AppLanguage.FR -> nameFr
        AppLanguage.EN -> nameEn
    }
}

data class Pid01StatusResult(
    val milOn: Boolean,
    val dtcCount: Int,
    val incompleteMonitors: Int,
    val totalSupportedMonitors: Int
)

data class ReassembledObdMessage(
    val bytes: List<Int>,
    val isMultiFrame: Boolean
)

data class ObdScanResult(
    val isCommunicationSuccess: Boolean,
    val adapterVersion: String,
    val detectedProtocol: String,
    val milCommandedOn: Boolean?,
    val storedDtcCount: Int?,
    val incompleteReadinessMonitors: Int?,
    val totalReadinessMonitors: Int?,
    val distanceSinceCodesClearedKm: Int?,
    val warmupsSinceCodesCleared: Int?,
    val distanceWithMilOnKm: Int?,
    val timeSinceCodesClearedMin: Int?,
    val ecuVin: String?,
    val dtcList: List<DtcInfo>,
    val freezeFrameSummary: String?,
    val liveParameters: List<LiveParameterGauge>,
    val clearedCodesAlertLevel: ClearedCodesAlertLevel = ClearedCodesAlertLevel.NOT_AVAILABLE,
    val isRecentlyClearedFraudAlert: Boolean = false,
    val supportedPidsCount: Int = 0,
    val rawExchangeLog: List<ObdCommandExchange> = emptyList(),
    val isSimulated: Boolean = false
)

/**
 * Abstract transport interface so Bluetooth Classic SPP (Android now) and
 * BLE / Wi-Fi or test transports can be swapped cleanly.
 */
interface ObdTransport {
    val transportName: String
    suspend fun connect(): Boolean
    suspend fun sendCommand(rawCommand: String, timeoutMs: Long = 2500L): String
    suspend fun disconnect()
}

object ObdDiagnosticEngine {

    fun notAvailableLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "غير متوفر في هذه السيارة"
        AppLanguage.FR -> "Non disponible sur cette voiture"
        AppLanguage.EN -> "Not available on this car"
    }

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

    private val STATUS_ERROR_PATTERNS = listOf(
        "NO DATA",
        "UNABLE TO CONNECT",
        "STOPPED",
        "BUS INIT: ERROR",
        "BUS INIT: ...ERROR",
        "BUS INIT:...ERROR",
        "CAN ERROR",
        "BUFFER FULL",
        "DATA ERROR",
        "ACT ALERT",
        "LP ALERT",
        "ERROR_NOT_CONNECTED",
        "ERROR_IO",
        "ERROR"
    )

    private val ISO_TP_FRAME_REGEX = Regex("""^([0-9A-F]):\s*([0-9A-F\s]+)$""")
    private val ISO_TP_LEN_LINE_REGEX = Regex("""^[0-9A-F]{3}$""")
    private val PURE_HEX_REGEX = Regex("""^[0-9A-F]+$""")

    /**
     * Reassembles an ELM327 reply into one or more logical messages:
     * - Strips `SEARCHING...`, `BUS INIT: ...OK`, prompts (`>`), and status errors (`NO DATA`, `?`, `CAN ERROR`, etc.)
     * - Works identically whether the dongle sends spaces (`AT S1`) or no spaces (`AT S0`)
     * - Reassembles ISO 15765-4 multi-frame messages (`0:`, `1:`, `2:`...)
     * - Keeps replies from multiple control units (ECUs) as separate messages
     */
    fun reassembleMessages(rawReply: String): List<ReassembledObdMessage> {
        if (rawReply.isBlank()) return emptyList()

        val lines = rawReply
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .split("\n")

        val messages = mutableListOf<ReassembledObdMessage>()
        var currentMultiFrameBytes: MutableList<Int>? = null
        var sawIsoTpLengthHeader = false

        fun flushMultiFrame() {
            val active = currentMultiFrameBytes
            if (!active.isNullOrEmpty()) {
                messages.add(ReassembledObdMessage(active.toList(), isMultiFrame = true))
            }
            currentMultiFrameBytes = null
        }

        for (rawLine in lines) {
            var line = rawLine.uppercase(Locale.US)
                .replace(">", "")
                .replace("SEARCHING...", "")
                .replace("BUS INIT: ...OK", "")
                .replace("BUS INIT:...OK", "")
                .replace("BUS INIT: OK", "")
                .trim()

            if (line.isEmpty() || line == "?" || line == "OK") continue

            val isErrorLine = STATUS_ERROR_PATTERNS.any { pattern -> line.contains(pattern) }
            if (isErrorLine) {
                continue
            }

            // 3-hex-digit ISO-TP byte count line (e.g., "014" or "00A")
            val compactNoSpaces = line.replace(Regex("\\s+"), "")
            if (ISO_TP_LEN_LINE_REGEX.matches(compactNoSpaces)) {
                flushMultiFrame()
                sawIsoTpLengthHeader = true
                continue
            }

            // ISO-TP multi-frame sequence line (e.g., "0: 49 02 01 56 46 31" or "0:490201564631")
            val frameMatch = ISO_TP_FRAME_REGEX.matchEntire(line)
            if (frameMatch != null) {
                val seqIndex = frameMatch.groupValues[1].toInt(16)
                val hexBody = frameMatch.groupValues[2].replace(Regex("\\s+"), "")
                val frameBytes = parseHexPairsToBytes(hexBody)
                if (frameBytes.isNotEmpty()) {
                    if (seqIndex == 0 || currentMultiFrameBytes == null) {
                        flushMultiFrame()
                        currentMultiFrameBytes = frameBytes.toMutableList()
                    } else {
                        currentMultiFrameBytes?.addAll(frameBytes)
                    }
                }
                continue
            }

            // Standard single-frame line (with or without spaces, e.g. "41 05 7B" or "41057B")
            flushMultiFrame()
            if (compactNoSpaces.length >= 2 &&
                compactNoSpaces.length % 2 == 0 &&
                PURE_HEX_REGEX.matches(compactNoSpaces)
            ) {
                val lineBytes = parseHexPairsToBytes(compactNoSpaces)
                if (lineBytes.isNotEmpty()) {
                    messages.add(
                        ReassembledObdMessage(
                            bytes = lineBytes,
                            isMultiFrame = sawIsoTpLengthHeader
                        )
                    )
                    sawIsoTpLengthHeader = false
                }
            }
        }

        flushMultiFrame()
        return messages
    }

    private fun parseHexPairsToBytes(cleanHex: String): List<Int> {
        if (cleanHex.length < 2) return emptyList()
        val out = ArrayList<Int>(cleanHex.length / 2)
        var i = 0
        while (i + 1 < cleanHex.length) {
            val byteVal = cleanHex.substring(i, i + 2).toIntOrNull(16) ?: return emptyList()
            out.add(byteVal)
            i += 2
        }
        return out
    }

    /**
     * Extracts payload bytes after `[modeResp, pidHex]` for all responding ECUs.
     * Works whether or not the dongle sends spaces (`AT S0` or `AT S1`), and across multi-frame or multi-ECU replies.
     */
    fun extractAllPidPayloads(rawHex: String, modeResp: String, pidHex: String): List<List<Int>> {
        val targetMode = modeResp.trim().toIntOrNull(16) ?: return emptyList()
        val targetPid = pidHex.trim().toIntOrNull(16) ?: return emptyList()

        val messages = reassembleMessages(rawHex)
        val payloads = mutableListOf<List<Int>>()

        for (msg in messages) {
            val bytes = msg.bytes
            if (bytes.size >= 2 && bytes[0] == targetMode && bytes[1] == targetPid) {
                payloads.add(bytes.drop(2))
            } else if (bytes.size > 2) {
                // Support optional header bytes if present before mode/pid
                val idx = (0 until bytes.size - 1).firstOrNull { i ->
                    bytes[i] == targetMode && bytes[i + 1] == targetPid
                }
                if (idx != null && idx + 2 <= bytes.size) {
                    payloads.add(bytes.drop(idx + 2))
                }
            }
        }
        return payloads
    }

    /**
     * Returns the payload bytes for `[modeResp, pidHex]` from the primary responding ECU
     * (prefers the payload with the most data bytes when multiple ECUs respond), or emptyList() if unavailable.
     */
    fun extractPayloadBytes(rawHex: String, modeResp: String, pidHex: String): List<Int> {
        val all = extractAllPidPayloads(rawHex, modeResp, pidHex)
        return all.maxByOrNull { it.size } ?: emptyList()
    }

    /**
     * Parses a Mode 01 Supported PIDs bitmask reply (`0100`, `0120`, `0140`).
     * Combines bitmasks with bitwise OR across all responding ECUs.
     * Returns the set of supported PID numbers (e.g. 0x01..0x20 for basePid = 0x00).
     */
    fun parseSupportedPidsBitmask(rawHex: String, basePid: Int = 0x00): Set<Int> {
        val pidHex = String.format(Locale.US, "%02X", basePid)
        val payloads = extractAllPidPayloads(rawHex, "41", pidHex)
        if (payloads.isEmpty()) return emptySet()

        val supported = mutableSetOf<Int>()
        for (bytes in payloads) {
            if (bytes.size < 4) continue
            for (byteIdx in 0..3) {
                val b = bytes[byteIdx] and 0xFF
                for (bit in 0..7) {
                    val isSet = (b and (1 shl (7 - bit))) != 0
                    if (isSet) {
                        val pidNum = basePid + (byteIdx * 8) + bit + 1
                        supported.add(pidNum)
                    }
                }
            }
        }
        return supported
    }

    /**
     * Parses Mode 03 (`43`), Mode 07 (`47`), or Mode 0A (`4A`) fault code responses.
     * - Works with or without spaces (`AT S0` / `AT S1`)
     * - Handles CAN single-frame (`43 <count> <b1><b2>...`), CAN multi-frame (`0: 43 <count>... 1: ...`),
     *   and legacy ISO 9141-2 / KWP2000 frames (`43 <b1><b2> <b3><b4> <b5><b6>`) without guessing blindly
     * - Aggregates fault codes across multiple responding ECUs
     */
    fun parseMode03DtcResponse(
        rawHex: String,
        dtcType: DtcType = DtcType.STORED,
        isCanProtocol: Boolean? = null
    ): List<DtcInfo> {
        val messages = reassembleMessages(rawHex)
        if (messages.isEmpty()) return emptyList()

        val validServiceHeaders = setOf(0x43, 0x47, 0x4A)
        val codes = mutableListOf<DtcInfo>()

        for (msg in messages) {
            val bytes = msg.bytes
            val headerIndex = bytes.indexOfFirst { it in validServiceHeaders }
            if (headerIndex < 0) continue

            val payload = bytes.drop(headerIndex + 1)
            if (payload.isEmpty() || payload.all { it == 0 }) continue

            // Determine whether payload[0] is the ISO 15765-4 CAN DTC count byte:
            // 1. Multi-frame ISO-TP messages ("0:", "1:" or 3-digit length header) are always CAN -> payload[0] is count.
            // 2. If caller knows protocol is CAN (isCanProtocol == true) -> payload[0] is count.
            // 3. If caller knows protocol is legacy non-CAN (isCanProtocol == false) -> no count byte.
            // 4. When protocol is unspecified:
            //    - In J1979, every DTC is 2 bytes. If payload has an odd number of bytes (e.g. 43 02 02 99 03 01 -> 5 bytes),
            //      payload[0] is the 1-byte CAN DTC count.
            //    - If payload has an even number of bytes (e.g. 6 bytes after 43 in a padded 7-byte CAN frame "43 01 02 99 00 00 00"),
            //      check if payload[0] is 1 or 2 and followed by exact (payload[0]*2) non-zero DTC bytes and odd trailing zero padding.
            val hasCountByte = when {
                msg.isMultiFrame -> true
                isCanProtocol == true -> true
                isCanProtocol == false -> false
                payload.size % 2 == 1 -> true
                else -> {
                    val possibleCount = payload[0]
                    val dtcBytesEnd = 1 + (possibleCount * 2)
                    possibleCount in 1..2 &&
                        payload.size > dtcBytesEnd &&
                        (payload[1] != 0 || payload[2] != 0) &&
                        payload.getOrNull(dtcBytesEnd - 1) != 0 &&
                        payload.subList(dtcBytesEnd, payload.size).all { it == 0 }
                }
            }

            val expectedCount: Int? = if (hasCountByte) payload[0] else null
            val dtcBytes = if (hasCountByte) payload.drop(1) else payload

            if (expectedCount == 0) continue

            var i = 0
            var parsedForThisEcu = 0
            while (i + 1 < dtcBytes.size) {
                if (expectedCount != null && parsedForThisEcu >= expectedCount) break
                val b1 = dtcBytes[i]
                val b2 = dtcBytes[i + 1]
                if (b1 != 0 || b2 != 0) {
                    val prefixChar = when ((b1 shr 6) and 0x03) {
                        0 -> 'P'
                        1 -> 'C'
                        2 -> 'B'
                        else -> 'U'
                    }
                    val secondDigit = (b1 shr 4) and 0x03
                    val thirdHex = (b1 and 0x0F).toString(16).uppercase(Locale.US)
                    val fourthFifthHex = b2.toString(16).uppercase(Locale.US).padStart(2, '0')
                    val codeStr = "$prefixChar$secondDigit$thirdHex$fourthFifthHex"
                    codes.add(lookupDtc(codeStr, dtcType))
                    parsedForThisEcu++
                }
                i += 2
            }
        }

        return codes.distinctBy { "${it.code}_${it.dtcType}" }
    }

    /**
     * Parses Mode 01 PID 01 (Monitor status since DTCs cleared).
     * Returns null when the car did not provide a valid 4-byte response (unknown is NOT zero).
     * If multiple ECUs reply to 0101, merges MIL status, max DTC count, and monitor counts.
     */
    fun parsePid01StatusDetail(rawHex: String): Pid01StatusResult? {
        val payloads = extractAllPidPayloads(rawHex, "41", "01").filter { it.size >= 4 }
        if (payloads.isEmpty()) return null

        var anyMilOn = false
        var maxDtcCount = 0
        var maxIncomplete = 0
        var maxSupportedMonitors = 0

        for (bytes in payloads) {
            val a = bytes[0]
            val b = bytes[1]
            val c = bytes[2]
            val d = bytes[3]

            val milOn = (a and 0x80) != 0
            val dtcCount = a and 0x7F
            if (milOn) anyMilOn = true
            if (dtcCount > maxDtcCount) maxDtcCount = dtcCount

            var supportedCount = 0
            var incompleteCount = 0

            // Continuous monitors in byte B: bits 0..2 = supported, bits 4..6 = incomplete
            for (bit in 0..2) {
                val isSupported = (b and (1 shl bit)) != 0
                val isIncomplete = (b and (1 shl (bit + 4))) != 0
                if (isSupported) {
                    supportedCount++
                    if (isIncomplete) incompleteCount++
                } else if (isIncomplete) {
                    supportedCount++
                    incompleteCount++
                }
            }

            // Non-continuous monitors in byte C (supported) and byte D (incomplete)
            for (bit in 0..7) {
                val isSupported = (c and (1 shl bit)) != 0
                val isIncomplete = (d and (1 shl bit)) != 0
                if (isSupported) {
                    supportedCount++
                    if (isIncomplete) incompleteCount++
                }
            }

            if (incompleteCount > maxIncomplete) maxIncomplete = incompleteCount
            if (supportedCount > maxSupportedMonitors) maxSupportedMonitors = supportedCount
        }

        return Pid01StatusResult(
            milOn = anyMilOn,
            dtcCount = maxDtcCount,
            incompleteMonitors = maxIncomplete,
            totalSupportedMonitors = maxSupportedMonitors
        )
    }

    /**
     * Legacy compatibility helper returning Triple(milOn, dtcCount, incompleteMonitors).
     * Note: real scan logic uses [parsePid01StatusDetail] so null/unavailable is preserved.
     */
    fun parsePid01Status(rawHex: String): Triple<Boolean, Int, Int> {
        val detail = parsePid01StatusDetail(rawHex) ?: return Triple(false, 0, 0)
        return Triple(detail.milOn, detail.dtcCount, detail.incompleteMonitors)
    }

    /**
     * Parses a 2-byte unsigned PID (`0121` Distance with MIL on, `0131` Distance since codes cleared, `014E` Time since clear).
     * Returns null when not available (unknown is NOT zero).
     */
    fun parseTwoByteDistanceKmOrNull(rawHex: String, pidHex: String): Int? {
        val bytes = extractPayloadBytes(rawHex, "41", pidHex)
        if (bytes.size < 2) return null
        return (bytes[0] * 256) + bytes[1]
    }

    fun parseTwoByteDistanceKm(rawHex: String, pidHex: String): Int {
        return parseTwoByteDistanceKmOrNull(rawHex, pidHex) ?: 0
    }

    /**
     * Parses a 1-byte unsigned PID (`0130` Warm-ups since codes cleared).
     * Returns null when not available (unknown is NOT zero).
     */
    fun parseSingleByteCountOrNull(rawHex: String, pidHex: String): Int? {
        val bytes = extractPayloadBytes(rawHex, "41", pidHex)
        return bytes.firstOrNull()
    }

    fun parseSingleByteCount(rawHex: String, pidHex: String): Int {
        return parseSingleByteCountOrNull(rawHex, pidHex) ?: 0
    }

    fun parseCoolantTempC(rawHex: String): Int? {
        val bytes = extractPayloadBytes(rawHex, "41", "05")
        if (bytes.isEmpty()) return null
        return bytes[0] - 40
    }

    fun parseRpm(rawHex: String): Int? {
        val bytes = extractPayloadBytes(rawHex, "41", "0C")
        if (bytes.size < 2) return null
        return ((bytes[0] * 256) + bytes[1]) / 4
    }

    /**
     * Parses Short-Term (`0106`) or Long-Term (`0107`) Fuel Trim Bank 1 in percent (-100% to +99.2%).
     */
    fun parseFuelTrimPercent(rawHex: String, pidHex: String): Double? {
        val bytes = extractPayloadBytes(rawHex, "41", pidHex)
        if (bytes.isEmpty()) return null
        return ((bytes[0] - 128) * 100.0) / 128.0
    }

    /**
     * Parses O2 Sensor (`0114` or `0115`) output voltage in Volts (0.00 V to 1.275 V).
     */
    fun parseO2SensorVoltage(rawHex: String, pidHex: String): Double? {
        val bytes = extractPayloadBytes(rawHex, "41", pidHex)
        if (bytes.isEmpty()) return null
        return bytes[0] / 200.0
    }

    /**
     * Parses battery voltage from `AT RV` response (e.g. "14.1V", "12,6 V", "13.8").
     */
    fun parseBatteryVoltage(rawAtRv: String): Double? {
        val clean = rawAtRv.trim()
        if (clean.isEmpty() ||
            clean.contains("?") ||
            STATUS_ERROR_PATTERNS.any { clean.uppercase(Locale.US).contains(it) }
        ) {
            return null
        }
        return Regex("""(\d+(?:[.,]\d+)?)""")
            .find(clean)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace(',', '.')
            ?.toDoubleOrNull()
            ?.takeIf { it in 5.0..24.0 }
    }

    /**
     * Parses Service 09 PID 02 (`09 02`) Vehicle Identification Number (VIN).
     * Supports both ISO 15765-4 CAN multi-frame (`0: 49 02 01 ... 1: ...`) and
     * ISO 9141-2 / KWP2000 multi-line (`49 02 01 ... \n 49 02 02 ...`), with or without spaces.
     */
    fun parseMode09Vin(rawHex: String): String? {
        val messages = reassembleMessages(rawHex)
        if (messages.isEmpty()) return null

        val asciiBytes = mutableListOf<Int>()
        for (msg in messages) {
            val bytes = msg.bytes
            val idx = (0 until bytes.size - 1).firstOrNull { i ->
                bytes[i] == 0x49 && bytes[i + 1] == 0x02
            } ?: continue

            // After 49 02, byte[idx + 2] is the data-item count (01 on CAN) or line number (01..05 on KWP)
            val dataStart = if (idx + 3 <= bytes.size) idx + 3 else idx + 2
            asciiBytes.addAll(bytes.drop(dataStart))
        }

        if (asciiBytes.isEmpty()) return null

        val chars = StringBuilder()
        for (b in asciiBytes) {
            val ch = b.toChar()
            if (ch in '0'..'9' || (ch in 'A'..'Z' && ch != 'I' && ch != 'O' && ch != 'Q') ||
                (ch in 'a'..'z' && ch.uppercaseChar() != 'I' && ch.uppercaseChar() != 'O' && ch.uppercaseChar() != 'Q')
            ) {
                chars.append(ch.uppercaseChar())
            }
        }

        val candidate = chars.toString()
        return if (candidate.length >= 17) {
            candidate.take(17)
        } else {
            null
        }
    }

    /**
     * Evaluates cleared-codes status ONLY from values that were actually read:
     * - Never treats null ("not available") as 0
     * - Strong warning (`STRONG_WARNING`) is raised ONLY when distance since clear (< 50 km)
     *   or warm-ups since clear (< 3) was actually read from the car and stored DTC count is 0.
     * - Readiness monitors alone (`incompleteMonitors >= 3` when distance and warm-ups are unavailable or normal)
     *   raise `CANNOT_RULE_OUT` ("cannot rule out") because monitors alone are weak evidence on older cars.
     */
    fun evaluateClearedCodesAlert(
        storedDtcCount: Int?,
        distanceSinceClearKm: Int?,
        warmupsSinceClear: Int?,
        incompleteMonitors: Int?
    ): ClearedCodesAlertLevel {
        if (storedDtcCount != null && storedDtcCount > 0) {
            return ClearedCodesAlertLevel.NONE_NORMAL
        }

        val hasLowDistance = distanceSinceClearKm != null && distanceSinceClearKm in 0..45
        val hasLowWarmups = warmupsSinceClear != null && warmupsSinceClear in 0..2
        if (hasLowDistance || hasLowWarmups) {
            return ClearedCodesAlertLevel.STRONG_WARNING
        }

        val hasHighIncompleteMonitors = incompleteMonitors != null && incompleteMonitors >= 3
        if (hasHighIncompleteMonitors) {
            // If both distance (>= 100 km) and warmups (>= 10) were actually read and are well above reset thresholds,
            // incomplete monitors are just normal for this car's driving cycle, not a recent reset.
            val provenNotRecentlyReset =
                (distanceSinceClearKm != null && distanceSinceClearKm >= 100) &&
                    (warmupsSinceClear != null && warmupsSinceClear >= 10)
            return if (provenNotRecentlyReset) {
                ClearedCodesAlertLevel.NONE_NORMAL
            } else {
                ClearedCodesAlertLevel.CANNOT_RULE_OUT
            }
        }

        if (distanceSinceClearKm != null || warmupsSinceClear != null || incompleteMonitors != null) {
            return ClearedCodesAlertLevel.NONE_NORMAL
        }

        return ClearedCodesAlertLevel.NOT_AVAILABLE
    }

    /**
     * Compatibility wrapper returning true only for strong cleared-code evidence (distance or warm-ups).
     */
    fun detectClearedCodeFraud(
        storedDtcCount: Int?,
        distanceSinceClearKm: Int?,
        warmupsSinceClear: Int?,
        incompleteMonitors: Int?
    ): Boolean {
        return evaluateClearedCodesAlert(
            storedDtcCount = storedDtcCount,
            distanceSinceClearKm = distanceSinceClearKm,
            warmupsSinceClear = warmupsSinceClear,
            incompleteMonitors = incompleteMonitors
        ) == ClearedCodesAlertLevel.STRONG_WARNING
    }

    /**
     * Generates a complete realistic ObdScanResult for any of the 4 built-in Simulator Profiles
     * (Accessible only in Developer Mode; never saveable to an inspection or PDF).
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
                    LiveParameterGauge("temp", "Température Liquide (Coolant)", "حرارة ماء التبريد", "Coolant Temperature", "89 °C", "80 - 98 °C", "GREEN"),
                    LiveParameterGauge("rpm", "Régime Ralenti (Idle RPM)", "سرعة دوران المحرك (Ralenti)", "Idle Engine Speed", "780 tr/min (Stable ±8)", "680 - 900 tr/min", "GREEN"),
                    LiveParameterGauge("volt", "Tension Batterie & Alternateur", "فولتية البطارية والدينامو", "Alternator Charging Voltage", "14.1 V", "13.5 - 14.7 V", "GREEN"),
                    LiveParameterGauge("trim", "Correction Carburant (Fuel Trim)", "تصحيح خليط الوقود (STFT/LTFT)", "Short/Long Fuel Trim", "+1.6 %", "-10% à +10%", "GREEN"),
                    LiveParameterGauge("o2", "Sonde Lambda (O2 Sensor)", "حساس الأكسجين (Sonde Lambda)", "O2 Sensor Activity", "0.68 V", "0.10 - 0.90 V", "GREEN")
                ),
                clearedCodesAlertLevel = ClearedCodesAlertLevel.NONE_NORMAL,
                isRecentlyClearedFraudAlert = false,
                supportedPidsCount = 18,
                isSimulated = true
            )

            ObdSimulatorProfile.STORED_FAULTS -> ObdScanResult(
                isCommunicationSuccess = true,
                adapterVersion = "SIMULATOR ONLY",
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
                freezeFrameSummary = "P0299 enregistré à 2 640 tr/min, 88 km/h, Température moteur 92°C",
                liveParameters = listOf(
                    LiveParameterGauge("temp", "Température Liquide (Coolant)", "حرارة ماء التبريد", "Coolant Temperature", "95 °C", "80 - 98 °C", "GREEN"),
                    LiveParameterGauge("rpm", "Régime Ralenti (Idle RPM)", "سرعة دوران المحرك (Ralenti)", "Idle Engine Speed", "890 tr/min (Instable ±95)", "680 - 900 tr/min", "ORANGE"),
                    LiveParameterGauge("volt", "Tension Batterie & Alternateur", "فولتية البطارية والدينامو", "Alternator Charging Voltage", "13.8 V", "13.5 - 14.7 V", "GREEN"),
                    LiveParameterGauge("trim", "Correction Carburant (Fuel Trim)", "تصحيح خليط الوقود (STFT/LTFT)", "Short/Long Fuel Trim", "+19.5 %", "-10% à +10%", "RED"),
                    LiveParameterGauge("o2", "Sonde Lambda (O2 Sensor)", "حساس الأكسجين (Sonde Lambda)", "O2 Sensor Activity", "0.18 V", "0.10 - 0.90 V", "ORANGE")
                ),
                clearedCodesAlertLevel = ClearedCodesAlertLevel.NONE_NORMAL,
                isRecentlyClearedFraudAlert = false,
                supportedPidsCount = 18,
                isSimulated = true
            )

            ObdSimulatorProfile.RECENTLY_CLEARED_FRAUD -> ObdScanResult(
                isCommunicationSuccess = true,
                adapterVersion = "SIMULATOR ONLY",
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
                    LiveParameterGauge("temp", "Température Liquide (Coolant)", "حرارة ماء التبريد", "Coolant Temperature", "91 °C", "80 - 98 °C", "GREEN"),
                    LiveParameterGauge("rpm", "Régime Ralenti (Idle RPM)", "سرعة دوران المحرك (Ralenti)", "Idle Engine Speed", "810 tr/min (Stable ±15)", "680 - 900 tr/min", "GREEN"),
                    LiveParameterGauge("volt", "Tension Batterie & Alternateur", "فولتية البطارية والدينامو", "Alternator Charging Voltage", "13.9 V", "13.5 - 14.7 V", "GREEN"),
                    LiveParameterGauge("trim", "Correction Carburant (Fuel Trim)", "تصحيح خليط الوقود (STFT/LTFT)", "Short/Long Fuel Trim", "+11.7 %", "-10% à +10%", "ORANGE")
                ),
                clearedCodesAlertLevel = ClearedCodesAlertLevel.STRONG_WARNING,
                isRecentlyClearedFraudAlert = true,
                supportedPidsCount = 14,
                isSimulated = true
            )

            ObdSimulatorProfile.NO_ECU_RESPONSE -> ObdScanResult(
                isCommunicationSuccess = false,
                adapterVersion = "SIMULATOR ONLY",
                detectedProtocol = "UNABLE TO CONNECT (NO OBD2 RESPONSE)",
                milCommandedOn = null,
                storedDtcCount = null,
                incompleteReadinessMonitors = null,
                totalReadinessMonitors = null,
                distanceSinceCodesClearedKm = null,
                warmupsSinceCodesCleared = null,
                distanceWithMilOnKm = null,
                timeSinceCodesClearedMin = null,
                ecuVin = null,
                dtcList = emptyList(),
                freezeFrameSummary = null,
                liveParameters = emptyList(),
                clearedCodesAlertLevel = ClearedCodesAlertLevel.NOT_AVAILABLE,
                isRecentlyClearedFraudAlert = false,
                supportedPidsCount = 0,
                isSimulated = true
            )
        }
    }

    /**
     * Executes a full READ-ONLY diagnostic scan over any [ObdTransport].
     * - Follows the chosen [language] for all progress messages
     * - Uses a longer wait (10s) with 1 retry for the initial protocol search on older cars
     * - Queries supported parameters (`0100`, `0120`, `0140`) and requests ONLY supported PIDs
     * - Samples idle RPM multiple times over a few seconds to judge stability
     * - Reads the car's own VIN via Service `09 02` when supported
     * - Preserves null ("not available") whenever a parameter is unsupported or returns NO DATA
     */
    suspend fun executeRealObdScan(
        transport: ObdTransport,
        expectedVin: String = "",
        language: AppLanguage = AppLanguage.FR,
        onProgress: (String) -> Unit = {}
    ): ObdScanResult {
        val exchangeLog = mutableListOf<ObdCommandExchange>()

        suspend fun sendLogged(cmd: String, timeoutMs: Long = 2500L): String {
            val start = System.currentTimeMillis()
            val raw = transport.sendCommand(cmd, timeoutMs)
            val elapsed = System.currentTimeMillis() - start
            exchangeLog.add(
                ObdCommandExchange(
                    timestampMs = start,
                    command = cmd,
                    durationMs = elapsed,
                    rawResponse = raw
                )
            )
            return raw
        }

        // 1. Adapter Initialization (cope gracefully with commands that return ?)
        onProgress(
            when (language) {
                AppLanguage.AR -> "جاري تهيئة جهاز الفحص ELM327..."
                AppLanguage.FR -> "Initialisation du boîtier ELM327..."
                AppLanguage.EN -> "Initializing ELM327 adapter..."
            }
        )
        val atzResp = sendLogged("AT Z", timeoutMs = 2500L)
        sendLogged("AT E0", timeoutMs = 1500L)
        sendLogged("AT L0", timeoutMs = 1500L)
        sendLogged("AT S0", timeoutMs = 1500L)
        sendLogged("AT H0", timeoutMs = 1500L)
        sendLogged("AT SP 0", timeoutMs = 1500L)

        val rawAti = if (atzResp.contains("ELM", ignoreCase = true)) {
            atzResp
        } else {
            sendLogged("AT I", timeoutMs = 1500L)
        }
        val cleanAdapterVer = rawAti
            .replace("\n", " ")
            .replace("AT Z", "", ignoreCase = true)
            .replace("AT I", "", ignoreCase = true)
            .replace("?", "")
            .trim()
            .ifBlank { "ELM327 OBD2" }

        // Read battery voltage at the OBD2 port via AT RV (supported by the ELM327 chip itself)
        val voltRaw = sendLogged("AT RV", timeoutMs = 1800L)
        val parsedVolt = parseBatteryVoltage(voltRaw)

        // 2. First ECU Request: protocol search can take several seconds on older cars (ISO 9141-2 / KWP2000).
        // Use a 10-second timeout, and retry once if the first attempt fails to connect.
        onProgress(
            when (language) {
                AppLanguage.AR -> "البحث عن بروتوكول كمبيوتر السيارة وقراءة الحساسات المدعومة (0100)..."
                AppLanguage.FR -> "Recherche du protocole calculateur et lecture des paramètres supportés (0100)..."
                AppLanguage.EN -> "Searching for vehicle protocol and supported parameters (0100)..."
            }
        )
        var pid0100Resp = sendLogged("0100", timeoutMs = 10000L)
        var supportedPids = parseSupportedPidsBitmask(pid0100Resp, basePid = 0x00).toMutableSet()

        if (supportedPids.isEmpty()) {
            onProgress(
                when (language) {
                    AppLanguage.AR -> "إعادة المحاولة للاتصال بكمبيوتر السيارة (قد يستغرق بضع ثوانٍ في السيارات القديمة)..."
                    AppLanguage.FR -> "Nouvelle tentative de connexion au calculateur (voitures plus anciennes)..."
                    AppLanguage.EN -> "Retrying connection to vehicle ECU (older cars may take a few seconds)..."
                }
            )
            delay(500L)
            pid0100Resp = sendLogged("0100", timeoutMs = 10000L)
            supportedPids = parseSupportedPidsBitmask(pid0100Resp, basePid = 0x00).toMutableSet()
        }

        // Also check 0101 if 0100 succeeded or as a fallback check if 0100 returned nothing
        onProgress(
            when (language) {
                AppLanguage.AR -> "قراءة حالة لمبة المحرك والاختبارات الذاتية (PID 0101)..."
                AppLanguage.FR -> "Lecture de l'état du voyant moteur et des tests d'émission (PID 0101)..."
                AppLanguage.EN -> "Reading check-engine light status and readiness monitors (PID 0101)..."
            }
        )
        val pid0101Resp = if (supportedPids.isEmpty() || 0x01 in supportedPids) {
            sendLogged("0101", timeoutMs = if (supportedPids.isEmpty()) 8000L else 2500L)
        } else {
            ""
        }
        val pid01Status = parsePid01StatusDetail(pid0101Resp)

        // If neither 0100 nor 0101 received any valid OBD2 response from the vehicle ECU:
        if (supportedPids.isEmpty() && pid01Status == null) {
            val unavailableText = notAvailableLabel(language)
            val voltOnlyGauges = if (parsedVolt != null) {
                listOf(
                    LiveParameterGauge(
                        id = "volt",
                        nameFr = "Tension prise OBD2 (Batterie)",
                        nameAr = "فولتية البطارية عبر منفذ OBD2",
                        nameEn = "OBD2 Port Battery Voltage",
                        valueText = String.format(Locale.US, "%.1f V", parsedVolt),
                        normalRangeText = "12.3 - 14.7 V",
                        status = when {
                            parsedVolt in 12.3..14.7 -> "GREEN"
                            parsedVolt in 11.8..12.29 || parsedVolt in 14.71..15.1 -> "ORANGE"
                            else -> "RED"
                        }
                    )
                )
            } else {
                emptyList()
            }

            return ObdScanResult(
                isCommunicationSuccess = false,
                adapterVersion = cleanAdapterVer,
                detectedProtocol = unavailableText,
                milCommandedOn = null,
                storedDtcCount = null,
                incompleteReadinessMonitors = null,
                totalReadinessMonitors = null,
                distanceSinceCodesClearedKm = null,
                warmupsSinceCodesCleared = null,
                distanceWithMilOnKm = null,
                timeSinceCodesClearedMin = null,
                ecuVin = null,
                dtcList = emptyList(),
                freezeFrameSummary = null,
                liveParameters = voltOnlyGauges,
                clearedCodesAlertLevel = ClearedCodesAlertLevel.NOT_AVAILABLE,
                isRecentlyClearedFraudAlert = false,
                supportedPidsCount = 0,
                rawExchangeLog = exchangeLog,
                isSimulated = false
            )
        }

        // Query 0120 and 0140 if supported
        if (0x20 in supportedPids) {
            val pid0120Resp = sendLogged("0120", timeoutMs = 2500L)
            supportedPids.addAll(parseSupportedPidsBitmask(pid0120Resp, basePid = 0x20))
        }
        if (0x40 in supportedPids) {
            val pid0140Resp = sendLogged("0140", timeoutMs = 2500L)
            supportedPids.addAll(parseSupportedPidsBitmask(pid0140Resp, basePid = 0x40))
        }

        // Read detected protocol description from ELM327
        val rawProtocol = sendLogged("AT DP", timeoutMs = 1500L)
            .replace("\n", " ")
            .replace("AUTO,", "", ignoreCase = true)
            .trim()
        val protocolResp = if (rawProtocol.isBlank() || rawProtocol.contains("?")) {
            notAvailableLabel(language)
        } else {
            rawProtocol
        }
        val isCanProtocol: Boolean? = when {
            rawProtocol.contains("15765", ignoreCase = true) || rawProtocol.contains("CAN", ignoreCase = true) -> true
            rawProtocol.contains("9141", ignoreCase = true) || rawProtocol.contains("14230", ignoreCase = true) || rawProtocol.contains("KWP", ignoreCase = true) -> false
            else -> null
        }

        // 3. Read Stored (Mode 03), Pending (Mode 07), and Permanent (Mode 0A) Fault Codes
        onProgress(
            when (language) {
                AppLanguage.AR -> "قراءة أكواد الأعطال المسجلة والمؤقتة والدائمة (Modes 03 / 07 / 0A)..."
                AppLanguage.FR -> "Lecture des codes défauts stockés, en attente et permanents (Modes 03 / 07 / 0A)..."
                AppLanguage.EN -> "Reading stored, pending, and permanent fault codes (Modes 03 / 07 / 0A)..."
            }
        )
        val storedDtcs = parseMode03DtcResponse(sendLogged("03", timeoutMs = 3000L), DtcType.STORED, isCanProtocol)
        val pendingDtcs = parseMode03DtcResponse(sendLogged("07", timeoutMs = 3000L), DtcType.PENDING, isCanProtocol)
        val permanentDtcs = parseMode03DtcResponse(sendLogged("0A", timeoutMs = 2500L), DtcType.PERMANENT, isCanProtocol)
        val allDtcs = (storedDtcs + pendingDtcs + permanentDtcs).distinctBy { "${it.code}_${it.dtcType}" }

        // 4. Read Cleared-Codes History Parameters ONLY if supported by the car
        onProgress(
            when (language) {
                AppLanguage.AR -> "فحص سجل مسح الذاكرة والمسافة المقطوعة منذ آخر مسح..."
                AppLanguage.FR -> "Vérification de l'historique d'effacement de la mémoire..."
                AppLanguage.EN -> "Checking distance and warm-ups since codes were last cleared..."
            }
        )
        val distMilKm: Int? = if (0x21 in supportedPids) {
            parseTwoByteDistanceKmOrNull(sendLogged("0121", timeoutMs = 2500L), "21")
        } else null

        val warmups: Int? = if (0x30 in supportedPids) {
            parseSingleByteCountOrNull(sendLogged("0130", timeoutMs = 2500L), "30")
        } else null

        val distSinceClearKm: Int? = if (0x31 in supportedPids) {
            parseTwoByteDistanceKmOrNull(sendLogged("0131", timeoutMs = 2500L), "31")
        } else null

        val timeSinceClearMin: Int? = if (0x4E in supportedPids) {
            parseTwoByteDistanceKmOrNull(sendLogged("014E", timeoutMs = 2500L), "4E")
        } else null

        // 5. Read Car's Own Chassis Number (VIN) with Service 09 02 when supported
        onProgress(
            when (language) {
                AppLanguage.AR -> "قراءة رقم الشاسيه المخزن في كمبيوتر السيارة (Service 09 02)..."
                AppLanguage.FR -> "Lecture du numéro de châssis du calculateur (Service 09 02)..."
                AppLanguage.EN -> "Reading vehicle chassis number from ECU (Service 09 02)..."
            }
        )
        val mode0900Resp = sendLogged("0900", timeoutMs = 2500L)
        val mode09Supported = extractAllPidPayloads(mode0900Resp, "49", "00")
        val supportsPid0902 = mode09Supported.isEmpty() || mode09Supported.any { bytes ->
            bytes.isNotEmpty() && ((bytes[0] and 0x40) != 0)
        }
        val ecuVin: String? = if (supportsPid0902) {
            parseMode09Vin(sendLogged("0902", timeoutMs = 3500L))
        } else null

        // 6. Live Engine Readings (requesting ONLY supported parameters)
        onProgress(
            when (language) {
                AppLanguage.AR -> "قراءة حرارة المحرك، استقرار سرعة الدوران (Ralenti) والحساسات الحية..."
                AppLanguage.FR -> "Lecture de la température, stabilité du ralenti et sondes en direct..."
                AppLanguage.EN -> "Reading coolant temperature, idle stability, fuel trims, and O2 sensor..."
            }
        )
        val coolantC: Int? = if (0x05 in supportedPids) {
            parseCoolantTempC(sendLogged("0105", timeoutMs = 2000L))
        } else null

        // Sample idle RPM several times over a few seconds to judge stability
        val rpmSamples = mutableListOf<Int>()
        if (0x0C in supportedPids) {
            val firstRpm = parseRpm(sendLogged("010C", timeoutMs = 2000L))
            if (firstRpm != null) {
                rpmSamples.add(firstRpm)
                if (firstRpm > 300) {
                    repeat(3) {
                        delay(450L)
                        val sample = parseRpm(sendLogged("010C", timeoutMs = 1800L))
                        if (sample != null && sample > 0) {
                            rpmSamples.add(sample)
                        }
                    }
                }
            }
        }
        val avgRpm: Int? = if (rpmSamples.isNotEmpty()) rpmSamples.average().roundToInt() else null
        val rpmSpread: Int? = if (rpmSamples.size >= 2) (rpmSamples.max() - rpmSamples.min()) else null

        // Short-Term (0106) and Long-Term (0107) Fuel Trims
        val stftPercent: Double? = if (0x06 in supportedPids) {
            parseFuelTrimPercent(sendLogged("0106", timeoutMs = 2000L), "06")
        } else null
        val ltftPercent: Double? = if (0x07 in supportedPids) {
            parseFuelTrimPercent(sendLogged("0107", timeoutMs = 2000L), "07")
        } else null

        // O2 Sensor Voltage (0114 Bank 1 Sensor 1, or 0115 Bank 1 Sensor 2)
        val o2Voltage: Double? = when {
            0x14 in supportedPids -> parseO2SensorVoltage(sendLogged("0114", timeoutMs = 2000L), "14")
            0x15 in supportedPids -> parseO2SensorVoltage(sendLogged("0115", timeoutMs = 2000L), "15")
            else -> null
        }

        val unavailableText = notAvailableLabel(language)
        val engineRunning = (avgRpm ?: 0) > 400

        // Build Idle RPM text & status with stability check
        val (rpmValueText, rpmStatus) = when {
            avgRpm == null -> unavailableText to "UNAVAILABLE"
            avgRpm == 0 -> {
                val stoppedLabel = when (language) {
                    AppLanguage.AR -> "0 د/د (المحرك متوقف)"
                    AppLanguage.FR -> "0 tr/min (Moteur arrêté)"
                    AppLanguage.EN -> "0 rpm (Engine off)"
                }
                stoppedLabel to "ORANGE"
            }
            rpmSpread != null && rpmSpread > 65 -> {
                val unstableLabel = when (language) {
                    AppLanguage.AR -> "$avgRpm د/د (متذبذب ±${rpmSpread / 2})"
                    AppLanguage.FR -> "$avgRpm tr/min (Instable ±${rpmSpread / 2})"
                    AppLanguage.EN -> "$avgRpm rpm (Unstable ±${rpmSpread / 2})"
                }
                unstableLabel to "ORANGE"
            }
            avgRpm in 650..920 -> {
                val stableSuffix = if (rpmSpread != null) " (±${rpmSpread / 2})" else ""
                val unit = if (language == AppLanguage.AR) "د/د" else if (language == AppLanguage.FR) "tr/min" else "rpm"
                "$avgRpm $unit$stableSuffix" to "GREEN"
            }
            else -> {
                val unit = if (language == AppLanguage.AR) "د/د" else if (language == AppLanguage.FR) "tr/min" else "rpm"
                "$avgRpm $unit" to "ORANGE"
            }
        }

        // Build Fuel Trim text & status
        val primaryTrim = ltftPercent ?: stftPercent
        val (trimValueText, trimStatus) = if (primaryTrim == null) {
            unavailableText to "UNAVAILABLE"
        } else {
            val formatted = buildString {
                if (stftPercent != null && ltftPercent != null) {
                    append(String.format(Locale.US, "ST %+.1f%% / LT %+.1f%%", stftPercent, ltftPercent))
                } else {
                    append(String.format(Locale.US, "%+.1f %%", primaryTrim))
                }
            }
            val worstAbs = maxOf(abs(stftPercent ?: 0.0), abs(ltftPercent ?: 0.0))
            val status = when {
                worstAbs <= 10.0 -> "GREEN"
                worstAbs <= 18.0 -> "ORANGE"
                else -> "RED"
            }
            formatted to status
        }

        // Build O2 Sensor text & status
        val (o2ValueText, o2Status) = if (o2Voltage == null) {
            unavailableText to "UNAVAILABLE"
        } else {
            val formatted = String.format(Locale.US, "%.2f V", o2Voltage)
            val status = when {
                o2Voltage in 0.08..0.92 -> "GREEN"
                else -> "ORANGE"
            }
            formatted to status
        }

        // Build Battery Voltage text & status with real limits based on whether alternator is charging
        val (voltValueText, voltStatus) = if (parsedVolt == null) {
            unavailableText to "UNAVAILABLE"
        } else {
            val formatted = String.format(Locale.US, "%.1f V", parsedVolt)
            val status = if (engineRunning) {
                when {
                    parsedVolt in 13.5..14.7 -> "GREEN"
                    parsedVolt in 12.6..13.49 || parsedVolt in 14.71..15.1 -> "ORANGE"
                    else -> "RED"
                }
            } else {
                when {
                    parsedVolt in 12.3..14.7 -> "GREEN"
                    parsedVolt in 11.8..12.29 || parsedVolt in 14.71..15.1 -> "ORANGE"
                    else -> "RED"
                }
            }
            formatted to status
        }

        val liveGauges = listOf(
            LiveParameterGauge(
                id = "temp",
                nameFr = "Température Liquide (Coolant)",
                nameAr = "حرارة ماء التبريد",
                nameEn = "Coolant Temperature",
                valueText = if (coolantC != null) "$coolantC °C" else unavailableText,
                normalRangeText = "80 - 98 °C",
                status = when {
                    coolantC == null -> "UNAVAILABLE"
                    coolantC in 75..99 -> "GREEN"
                    coolantC > 103 -> "RED"
                    else -> "ORANGE"
                }
            ),
            LiveParameterGauge(
                id = "rpm",
                nameFr = "Régime Ralenti (Idle RPM)",
                nameAr = "سرعة دوران المحرك (Ralenti)",
                nameEn = "Idle Engine Speed",
                valueText = rpmValueText,
                normalRangeText = "680 - 900 tr/min",
                status = rpmStatus
            ),
            LiveParameterGauge(
                id = "volt",
                nameFr = "Tension Batterie & Alternateur",
                nameAr = "فولتية البطارية والدينامو",
                nameEn = "Battery & Alternator Voltage",
                valueText = voltValueText,
                normalRangeText = if (engineRunning) "13.5 - 14.7 V" else "12.3 - 14.7 V",
                status = voltStatus
            ),
            LiveParameterGauge(
                id = "trim",
                nameFr = "Correction Carburant (Fuel Trim)",
                nameAr = "تصحيح خليط الوقود (Fuel Trim)",
                nameEn = "Fuel Trim (STFT / LTFT)",
                valueText = trimValueText,
                normalRangeText = "-10% à +10%",
                status = trimStatus
            ),
            LiveParameterGauge(
                id = "o2",
                nameFr = "Sonde Lambda (O2 Sensor)",
                nameAr = "حساس الأكسجين (Sonde Lambda)",
                nameEn = "Oxygen Sensor (O2)",
                valueText = o2ValueText,
                normalRangeText = "0.10 - 0.90 V",
                status = o2Status
            )
        )

        val effectiveStoredDtcCount: Int? = when {
            pid01Status != null -> maxOf(pid01Status.dtcCount, storedDtcs.size)
            storedDtcs.isNotEmpty() -> storedDtcs.size
            else -> null
        }

        val alertLevel = evaluateClearedCodesAlert(
            storedDtcCount = effectiveStoredDtcCount ?: allDtcs.size,
            distanceSinceClearKm = distSinceClearKm,
            warmupsSinceClear = warmups,
            incompleteMonitors = pid01Status?.incompleteMonitors
        )

        return ObdScanResult(
            isCommunicationSuccess = true,
            adapterVersion = cleanAdapterVer,
            detectedProtocol = protocolResp,
            milCommandedOn = pid01Status?.milOn,
            storedDtcCount = effectiveStoredDtcCount,
            incompleteReadinessMonitors = pid01Status?.incompleteMonitors,
            totalReadinessMonitors = pid01Status?.totalSupportedMonitors,
            distanceSinceCodesClearedKm = distSinceClearKm,
            warmupsSinceCodesCleared = warmups,
            distanceWithMilOnKm = distMilKm,
            timeSinceCodesClearedMin = timeSinceClearMin,
            ecuVin = ecuVin,
            dtcList = allDtcs,
            freezeFrameSummary = null,
            liveParameters = liveGauges,
            clearedCodesAlertLevel = alertLevel,
            isRecentlyClearedFraudAlert = alertLevel == ClearedCodesAlertLevel.STRONG_WARNING,
            supportedPidsCount = supportedPids.size,
            rawExchangeLog = exchangeLog,
            isSimulated = false
        )
    }
}
