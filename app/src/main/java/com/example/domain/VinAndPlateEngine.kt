package com.example.domain

import com.example.core.i18n.AppLanguage

data class VinAnalysisResult(
    val normalizedVin: String,
    val isValidLength: Boolean,
    val forbiddenCharactersFound: List<Char>,
    val isAlphanumericValid: Boolean,
    val wmiCode: String,
    val manufacturer: String,
    val countryFr: String,
    val countryAr: String,
    val countryEn: String,
    val tenthChar: Char?,
    val decodedYear: Int?, // null if non-standard or European manufacturer that doesn't encode year in pos 10
    val isEuropeanFlexibleYear: Boolean,
    val checkDigitStatus: String // "STANDARD_PASS", "UNVERIFIED_EUROPEAN", "CANNOT_CONFIRM"
) {
    val isFormatValid: Boolean
        get() = isValidLength && forbiddenCharactersFound.isEmpty() && isAlphanumericValid

    fun getCountry(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> countryAr
        AppLanguage.FR -> countryFr
        AppLanguage.EN -> countryEn
    }
}

data class AlgerianPlateResult(
    val rawInput: String,
    val isValidFormat: Boolean,
    val registrationSerial: String,
    val middleBlock: String,
    val vehicleCategoryCode: Int?,
    val categoryNameFr: String,
    val categoryNameAr: String,
    val categoryNameEn: String,
    val isProvisionalOrTransit: Boolean,
    val registrationYear: Int?, // null if 00 / temporary transit
    val wilayaCode: Int,
    val wilayaNameFr: String,
    val wilayaNameAr: String
) {
    fun getCategoryName(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> categoryNameAr
        AppLanguage.FR -> categoryNameFr
        AppLanguage.EN -> categoryNameEn
    }

    fun getWilayaDisplay(lang: AppLanguage): String {
        val codeStr = wilayaCode.toString().padStart(2, '0')
        return when (lang) {
            AppLanguage.AR -> "$codeStr - $wilayaNameAr ($wilayaNameFr)"
            else -> "$codeStr - $wilayaNameFr"
        }
    }
}

data class ConsistencyVerdict(
    val level: String, // "MATCH", "CANNOT_CONFIRM", "WARNING"
    val titleFr: String,
    val titleAr: String,
    val titleEn: String,
    val explanationFr: String,
    val explanationAr: String,
    val explanationEn: String
) {
    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> titleAr
        AppLanguage.FR -> titleFr
        AppLanguage.EN -> titleEn
    }

    fun getExplanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> explanationAr
        AppLanguage.FR -> explanationFr
        AppLanguage.EN -> explanationEn
    }
}

object VinAndPlateEngine {

    private val FORBIDDEN_VIN_CHARS = setOf('I', 'O', 'Q')

    // Comprehensive WMI dictionary for vehicles found on the Algerian market
    private data class WmiEntry(
        val manufacturer: String,
        val countryFr: String,
        val countryAr: String,
        val countryEn: String,
        val usesFlexibleYearEncoding: Boolean = false
    )

    private val wmiMap: Map<String, WmiEntry> = mapOf(
        // Algeria Local Assembly (Oued Tlélat, Tiaret, Ival, Sovack)
        "BR1" to WmiEntry("Renault Algérie (Oued Tlélat)", "Algérie (Montage local DZ)", "الجزائر (تركيب محلي وادي تليلات)", "Algeria (Local Assembly DZ)", true),
        "BR2" to WmiEntry("TMC Hyundai / Kia Algérie", "Algérie (Montage local DZ)", "الجزائر (تركيب محلي تيارت)", "Algeria (Local Assembly DZ)", false),
        "BRS" to WmiEntry("Sovac Algérie (VW / Seat / Skoda)", "Algérie (Relizane DZ)", "الجزائر (تركيب غليزان)", "Algeria (Local Assembly DZ)", false),
        // France (PSA & Renault)
        "VF1" to WmiEntry("Renault", "France", "فرنسا", "France", true),
        "VF2" to WmiEntry("Renault Utilitaires", "France", "فرنسا", "France", true),
        "VF3" to WmiEntry("Peugeot", "France", "فرنسا", "France", true),
        "VF7" to WmiEntry("Citroën", "France", "فرنسا", "France", true),
        "VR3" to WmiEntry("Peugeot", "France / Slovaquie", "فرنسا / سلوفاكيا", "France / Slovakia", true),
        "VR7" to WmiEntry("Citroën", "France", "فرنسا", "France", true),
        // Romania & Morocco (Dacia / Renault)
        "UU1" to WmiEntry("Dacia / Renault", "Roumanie (Mioveni)", "رومانيا", "Romania", true),
        "MA1" to WmiEntry("Renault / Dacia", "Maroc (Tanger / Somaca)", "المغرب", "Morocco", true),
        // Germany (VW, Audi, BMW, Mercedes, Opel)
        "WVW" to WmiEntry("Volkswagen", "Allemagne", "ألمانيا", "Germany", false),
        "WVG" to WmiEntry("Volkswagen SUV / Touran / Tiguan", "Allemagne", "ألمانيا", "Germany", false),
        "WV1" to WmiEntry("Volkswagen Utilitaires (Caddy)", "Allemagne", "ألمانيا", "Germany", false),
        "WV2" to WmiEntry("Volkswagen Transporter / Caddy", "Allemagne", "ألمانيا", "Germany", false),
        "WAU" to WmiEntry("Audi", "Allemagne", "ألمانيا", "Germany", false),
        "WBA" to WmiEntry("BMW", "Allemagne", "ألمانيا", "Germany", false),
        "WDB" to WmiEntry("Mercedes-Benz", "Allemagne", "ألمانيا", "Germany", false),
        "WDD" to WmiEntry("Mercedes-Benz", "Allemagne", "ألمانيا", "Germany", false),
        "W0L" to WmiEntry("Opel", "Allemagne", "ألمانيا", "Germany", false),
        "WF0" to WmiEntry("Ford Europe", "Allemagne", "ألمانيا", "Germany", true),
        // Spain (Seat, Renault, Peugeot, VW)
        "VSS" to WmiEntry("Seat", "Espagne (Martorell)", "إسبانيا", "Spain", false),
        "VS6" to WmiEntry("Ford Espagne", "Espagne", "إسبانيا", "Spain", true),
        "VS7" to WmiEntry("Citroën Espagne", "Espagne", "إسبانيا", "Spain", true),
        // Czech Republic (Skoda, Hyundai)
        "TMB" to WmiEntry("Skoda", "République Tchèque", "التشيك", "Czech Republic", false),
        "TMA" to WmiEntry("Hyundai Europe", "République Tchèque", "التشيك", "Czech Republic", false),
        // South Korea (Hyundai, Kia, Chevrolet/Daewoo)
        "KMH" to WmiEntry("Hyundai", "Corée du Sud", "كوريا الجنوبية", "South Korea", false),
        "KNA" to WmiEntry("Kia", "Corée du Sud", "كوريا الجنوبية", "South Korea", false),
        "KNE" to WmiEntry("Kia", "Corée du Sud", "كوريا الجنوبية", "South Korea", false),
        "KNM" to WmiEntry("Renault Samsung", "Corée du Sud", "كوريا الجنوبية", "South Korea", false),
        "KL1" to WmiEntry("Chevrolet (GM Korea)", "Corée du Sud", "كوريا الجنوبية", "South Korea", false),
        // Slovak Republic (Kia Zilina)
        "U5Y" to WmiEntry("Kia Europe", "Slovaquie", "سلوفاكيا", "Slovakia", false),
        // Turkey (Renault Symbol/Clio/Megane, Fiat, Toyota, Hyundai)
        "NM0" to WmiEntry("Ford Otosan", "Turquie", "تركيا", "Turkey", true),
        "NM4" to WmiEntry("Tofas / Fiat", "Turquie", "تركيا", "Turkey", true),
        "NMT" to WmiEntry("Toyota Turquie", "Turquie", "تركيا", "Turkey", false),
        "NLH" to WmiEntry("Hyundai Assan", "Turquie", "تركيا", "Turkey", false),
        // Japan (Toyota, Nissan, Suzuki, Mitsubishi)
        "JTD" to WmiEntry("Toyota", "Japon", "اليابان", "Japan", false),
        "JT1" to WmiEntry("Toyota", "Japon", "اليابان", "Japan", false),
        "JN1" to WmiEntry("Nissan", "Japon", "اليابان", "Japan", false),
        "JS1" to WmiEntry("Suzuki", "Japon", "اليابان", "Japan", false),
        "JS2" to WmiEntry("Suzuki", "Japon", "اليابان", "Japan", false),
        "JMB" to WmiEntry("Mitsubishi", "Japon", "اليابان", "Japan", false),
        // India (Maruti Suzuki, Hyundai i10)
        "MA3" to WmiEntry("Suzuki / Maruti", "Inde", "الهند", "India", false),
        "MAL" to WmiEntry("Hyundai Inde (i10 / Grand i10)", "Inde", "الهند", "India", false),
        // Italy (Fiat)
        "ZFA" to WmiEntry("Fiat", "Italie", "إيطاليا", "Italy", true),
        // China (Chery, Geely, JAC, Great Wall, MG, DFSK)
        "LVV" to WmiEntry("Chery", "Chine", "الصين", "China", false),
        "L6T" to WmiEntry("Geely", "Chine", "الصين", "China", false),
        "LB3" to WmiEntry("Geely", "Chine", "الصين", "China", false),
        "LJ1" to WmiEntry("JAC Motors", "Chine", "الصين", "China", false),
        "LGW" to WmiEntry("Great Wall / Haval", "Chine", "الصين", "China", false),
        "LSJ" to WmiEntry("MG (SAIC)", "Chine", "الصين", "China", false),
        "LVZ" to WmiEntry("DFSK / Dongfeng", "Chine", "الصين", "China", false)
    )

    // Standard ISO 3779 10th character year map (2001 - 2029)
    private val tenthCharYearMap: Map<Char, Int> = mapOf(
        '1' to 2001, '2' to 2002, '3' to 2003, '4' to 2004, '5' to 2005,
        '6' to 2006, '7' to 2007, '8' to 2008, '9' to 2009,
        'A' to 2010, 'B' to 2011, 'C' to 2012, 'D' to 2013, 'E' to 2014,
        'F' to 2015, 'G' to 2016, 'H' to 2017, 'J' to 2018, 'K' to 2019,
        'L' to 2020, 'M' to 2021, 'N' to 2022, 'P' to 2023, 'R' to 2024,
        'S' to 2025, 'T' to 2026, 'V' to 2027, 'W' to 2028, 'X' to 2029,
        'Y' to 2000
    )

    // All 58 Algerian Wilayas (1 to 58)
    val algerianWilayas: Map<Int, Pair<String, String>> = mapOf(
        1 to ("Adrar" to "أدرار"),
        2 to ("Chlef" to "الشلف"),
        3 to ("Laghouat" to "الأغواط"),
        4 to ("Oum El Bouaghi" to "أم البواقي"),
        5 to ("Batna" to "باتنة"),
        6 to ("Béjaïa" to "بجاية"),
        7 to ("Biskra" to "بسكرة"),
        8 to ("Béchar" to "بشار"),
        9 to ("Blida" to "البليدة"),
        10 to ("Bouira" to "البويرة"),
        11 to ("Tamanrasset" to "تمنراست"),
        12 to ("Tébessa" to "تبسة"),
        13 to ("Tlemcen" to "تلمسان"),
        14 to ("Tiaret" to "تيارت"),
        15 to ("Tizi Ouzou" to "تيزي وزو"),
        16 to ("Alger" to "الجزائر"),
        17 to ("Djelfa" to "الجلفة"),
        18 to ("Jijel" to "جيجل"),
        19 to ("Sétif" to "سطيف"),
        20 to ("Saïda" to "سعيدة"),
        21 to ("Skikda" to "سكيكدة"),
        22 to ("Sidi Bel Abbès" to "سيدي بلعباس"),
        23 to ("Annaba" to "عنابة"),
        24 to ("Guelma" to "قالمة"),
        25 to ("Constantine" to "قسنطينة"),
        26 to ("Médéa" to "المدية"),
        27 to ("Mostaganem" to "مستغانم"),
        28 to ("M'Sila" to "المسيلة"),
        29 to ("Mascara" to "معسكر"),
        30 to ("Ouargla" to "ورقلة"),
        31 to ("Oran" to "وهران"),
        32 to ("El Bayadh" to "البيض"),
        33 to ("Illizi" to "إليزي"),
        34 to ("Bordj Bou Arréridj" to "برج بوعريريج"),
        35 to ("Boumerdès" to "بومرداس"),
        36 to ("El Tarf" to "الطارف"),
        37 to ("Tindouf" to "تندوف"),
        38 to ("Tissemsilt" to "تيسمسيلت"),
        39 to ("El Oued" to "الوادي"),
        40 to ("Khenchela" to "خنشلة"),
        41 to ("Souk Ahras" to "سوق أهراس"),
        42 to ("Tipaza" to "تيبازة"),
        43 to ("Mila" to "ميلة"),
        44 to ("Aïn Defla" to "عين الدفلى"),
        45 to ("Naâma" to "النعامة"),
        46 to ("Aïn Témouchent" to "عين تموشنت"),
        47 to ("Ghardaïa" to "غرداية"),
        48 to ("Relizane" to "غليزان"),
        49 to ("Timimoun" to "تيميمون"),
        50 to ("Bordj Badji Mokhtar" to "برج باجي مختار"),
        51 to ("Ouled Djellal" to "أولاد جلال"),
        52 to ("Béni Abbès" to "بني عباس"),
        53 to ("In Salah" to "عين صالح"),
        54 to ("In Guezzam" to "عين قزام"),
        55 to ("Touggourt" to "تقرت"),
        56 to ("Djanet" to "جانت"),
        57 to ("El M'Ghair" to "المغير"),
        58 to ("El Meniaa" to "المنيعة")
    )

    fun analyzeVin(rawVin: String): VinAnalysisResult {
        val cleaned = rawVin.uppercase().replace(" ", "").replace("-", "").trim()
        val isValidLen = cleaned.length == 17
        val forbidden = cleaned.filter { it in FORBIDDEN_VIN_CHARS }.toList().distinct()
        val isAlphaNum = cleaned.all { it.isLetterOrDigit() }

        val wmi = if (cleaned.length >= 3) cleaned.substring(0, 3) else cleaned
        val matchedEntry = wmiMap[wmi] ?: inferRegionByFirstChar(wmi)

        val tenth = cleaned.getOrNull(9)
        val rawYear = tenth?.let { tenthCharYearMap[it] }

        // PSA (Peugeot/Citroën), Renault, and Fiat in Europe often use position 10 for model/plant serial
        val isEuroFlex = matchedEntry.usesFlexibleYearEncoding
        val decodedYear = if (isEuroFlex && rawYear == null) null else rawYear

        val checkStatus = when {
            !isValidLen || forbidden.isNotEmpty() -> "INVALID_FORMAT"
            isEuroFlex -> "UNVERIFIED_EUROPEAN"
            else -> "STANDARD_PASS"
        }

        return VinAnalysisResult(
            normalizedVin = cleaned,
            isValidLength = isValidLen,
            forbiddenCharactersFound = forbidden,
            isAlphanumericValid = isAlphaNum,
            wmiCode = wmi,
            manufacturer = matchedEntry.manufacturer,
            countryFr = matchedEntry.countryFr,
            countryAr = matchedEntry.countryAr,
            countryEn = matchedEntry.countryEn,
            tenthChar = tenth,
            decodedYear = decodedYear,
            isEuropeanFlexibleYear = isEuroFlex,
            checkDigitStatus = checkStatus
        )
    }

    private fun inferRegionByFirstChar(wmi: String): WmiEntry {
        val first = wmi.firstOrNull() ?: return WmiEntry(
            "Constructeur non répertorié",
            "Inconnu",
            "غير معروف",
            "Unknown",
            true
        )
        return when (first) {
            'V' -> WmiEntry("Constructeur Européen (France / Espagne)", "Europe (Zone V)", "أوروبا (فرنسا / إسبانيا)", "Europe (France / Spain)", true)
            'W' -> WmiEntry("Constructeur Allemand", "Allemagne", "ألمانيا", "Germany", false)
            'Z' -> WmiEntry("Constructeur Italien", "Italie", "إيطاليا", "Italy", true)
            'T', 'U' -> WmiEntry("Constructeur Europe Centrale / Est", "Europe Centrale", "أوروبا الوسطى / الشرقية", "Central / Eastern Europe", true)
            'J' -> WmiEntry("Constructeur Japonais", "Japon", "اليابان", "Japan", false)
            'K' -> WmiEntry("Constructeur Coréen", "Corée du Sud", "كوريا الجنوبية", "South Korea", false)
            'L' -> WmiEntry("Constructeur Chinois", "Chine", "الصين", "China", false)
            'M', 'N' -> WmiEntry("Constructeur Asie / Turquie", "Asie / Turquie", "آسيا / تركيا", "Asia / Turkey", false)
            '1', '4', '5' -> WmiEntry("Constructeur USA", "États-Unis", "الولايات المتحدة", "United States", false)
            'B' -> WmiEntry("Assemblage Afrique / Algérie", "Algérie / Afrique", "الجزائر / إفريقيا", "Algeria / Africa", true)
            else -> WmiEntry("Autre Constructeur ($wmi)", "International", "دولي", "International", true)
        }
    }

    /**
     * Parses an Algerian registration plate in the format:
     * [Serial 1..6 digits] [Category + 2-digit Year (3 digits)] [Wilaya (2 digits, 01..58)]
     * Example: "01245 118 16" or "1245-118-16" or "124511816"
     */
    fun parseAlgerianPlate(rawPlate: String): AlgerianPlateResult {
        val trimmed = rawPlate.trim()
        // Split by spaces, hyphens, or dots
        val tokens = trimmed.split(Regex("[\\s\\-.]+")).filter { it.isNotBlank() }

        val serial: String
        val middle: String
        val wilayaStr: String

        if (tokens.size >= 3) {
            serial = tokens[0]
            middle = tokens[1]
            wilayaStr = tokens[2]
        } else {
            val digitsOnly = trimmed.filter { it.isDigit() }
            if (digitsOnly.length >= 6) {
                wilayaStr = digitsOnly.takeLast(2)
                middle = digitsOnly.dropLast(2).takeLast(3)
                serial = digitsOnly.dropLast(5)
            } else {
                return invalidPlateResult(rawPlate)
            }
        }

        if (middle.length != 3 || !middle.all { it.isDigit() } || !wilayaStr.all { it.isDigit() }) {
            return invalidPlateResult(rawPlate)
        }

        val wilayaCode = wilayaStr.toIntOrNull() ?: return invalidPlateResult(rawPlate)
        val wilayaPair = algerianWilayas[wilayaCode] ?: return invalidPlateResult(rawPlate)

        val categoryDigit = middle[0].digitToInt()
        val yearDigits = middle.substring(1, 3).toIntOrNull() ?: 0

        val (catFr, catAr, catEn) = when (categoryDigit) {
            1 -> Triple("Véhicule Particulier (VP / Tourisme)", "سيارة سياحية خاصة (VP)", "Private Passenger Car (VP)")
            2 -> Triple("Camion (Poids Lourd)", "شاحنة", "Truck")
            3 -> Triple("Camionnette / Utilitaire Léger (Fourgon)", "سيارة نفعية خفيفة (Camionnette)", "Light Commercial Van")
            4 -> Triple("Autocar / Autobus", "حافلة نقل", "Bus / Coach")
            5 -> Triple("Tracteur Routier", "جرار طرقي", "Road Tractor")
            6 -> Triple("Autre Tracteur / Agricole", "جرار فلاحي", "Agricultural Tractor")
            7 -> Triple("Véhicule Spécial", "مركبة خاصة", "Special Vehicle")
            8 -> Triple("Remorque / Semi-remorque", "مقطورة", "Trailer")
            9 -> Triple("Motocyclette", "دراجة نارية", "Motorcycle")
            else -> Triple("Catégorie inconnue", "صنف غير معروف", "Unknown Category")
        }

        val isProvisional = yearDigits == 0 // e.g., 100 or 300 ("00" transit / DGD temporary)
        val fullYear = when {
            isProvisional -> null
            yearDigits <= 35 -> 2000 + yearDigits
            else -> 1900 + yearDigits
        }

        return AlgerianPlateResult(
            rawInput = rawPlate,
            isValidFormat = serial.isNotEmpty() && serial.all { it.isDigit() } && categoryDigit in 1..9 && wilayaCode in 1..58,
            registrationSerial = serial,
            middleBlock = middle,
            vehicleCategoryCode = categoryDigit,
            categoryNameFr = catFr,
            categoryNameAr = catAr,
            categoryNameEn = catEn,
            isProvisionalOrTransit = isProvisional,
            registrationYear = fullYear,
            wilayaCode = wilayaCode,
            wilayaNameFr = wilayaPair.first,
            wilayaNameAr = wilayaPair.second
        )
    }

    private fun invalidPlateResult(raw: String) = AlgerianPlateResult(
        rawInput = raw,
        isValidFormat = false,
        registrationSerial = "",
        middleBlock = "",
        vehicleCategoryCode = null,
        categoryNameFr = "",
        categoryNameAr = "",
        categoryNameEn = "",
        isProvisionalOrTransit = false,
        registrationYear = null,
        wilayaCode = 0,
        wilayaNameFr = "",
        wilayaNameAr = ""
    )

    /**
     * Evaluates consistency between the declared vehicle year, the decoded VIN year,
     * and the Algerian plate registration year. Never falsely accuses of fraud when
     * European manufacturers do not use standard 10th-character year encoding.
     */
    fun checkIdentityConsistency(
        carDeclaredYear: Int,
        vinResult: VinAnalysisResult?,
        plateResult: AlgerianPlateResult?
    ): ConsistencyVerdict {
        if (plateResult != null && plateResult.isValidFormat && plateResult.isProvisionalOrTransit) {
            return ConsistencyVerdict(
                level = "WARNING",
                titleFr = "Immatriculation provisoire / Transit ('00')",
                titleAr = "ترقيم مؤقت أو عبوري ('00')",
                titleEn = "Provisional / Transit Registration ('00')",
                explanationFr = "La plaque comporte '00' à la place de l'année. Vérifiez que le dossier de dédouanement ou la mainlevée est totalement finalisé avant tout paiement.",
                explanationAr = "اللوحة تحمل الرقم '00' بدل السنة. تحقق جيداً من اكتمال ملف الجمركة أو رفع اليد قبل دفع أي مبلغ.",
                explanationEn = "The plate uses '00' instead of the year. Verify that customs clearance or resale release paperwork is 100% complete before paying."
            )
        }

        val plateYear = plateResult?.takeIf { it.isValidFormat }?.registrationYear
        val vinYear = vinResult?.takeIf { it.isFormatValid }?.decodedYear

        if (plateYear != null && plateYear != carDeclaredYear) {
            val diff = kotlin.math.abs(plateYear - carDeclaredYear)
            return if (diff <= 1) {
                ConsistencyVerdict(
                    level = "CANNOT_CONFIRM",
                    titleFr = "Léger décalage d'année ($carDeclaredYear vs plaque $plateYear)",
                    titleAr = "فرق بسيط في السنة ($carDeclaredYear مقابل اللوحة $plateYear)",
                    titleEn = "Minor 1-Year Offset ($carDeclaredYear vs plate $plateYear)",
                    explanationFr = "Un écart d'un an entre la fabrication et la première mise en circulation en Algérie est fréquent (voiture fabriquée fin $carDeclaredYear et immatriculée début $plateYear). Vérifiez la Carte Grise.",
                    explanationAr = "الفرق بسنة واحدة بين سنة الصنع وأول تسجيل في الجزائر أمر شائع (صنعت أواخر $carDeclaredYear وسجلت بداية $plateYear). تأكد من البطاقة الرمادية.",
                    explanationEn = "A 1-year difference between production and first Algerian registration is common. Confirm on the Carte Grise."
                )
            } else {
                ConsistencyVerdict(
                    level = "WARNING",
                    titleFr = "Incohérence entre l'année annoncée ($carDeclaredYear) et la plaque ($plateYear)",
                    titleAr = "تضارب بين السنة المعلنة ($carDeclaredYear) وسنة لوحة الترقيم ($plateYear)",
                    titleEn = "Mismatch Between Declared Year ($carDeclaredYear) and Plate ($plateYear)",
                    explanationFr = "La plaque indique une année $plateYear alors que le véhicule est annoncé comme $carDeclaredYear. Vérifiez attentivement la date de première mise en circulation sur la Carte Grise.",
                    explanationAr = "لوحة الترقيم تشير إلى سنة $plateYear بينما السيارة معلنة كـ $carDeclaredYear. افحص تاريخ أول وضع في السير على البطاقة الرمادية بدقة.",
                    explanationEn = "The registration plate encodes $plateYear while the car is listed as $carDeclaredYear. Check the first registration date on the Carte Grise carefully."
                )
            }
        }

        if (vinResult != null && vinResult.isFormatValid) {
            if (vinResult.isEuropeanFlexibleYear) {
                return ConsistencyVerdict(
                    level = "CANNOT_CONFIRM",
                    titleFr = "VIN valide (${vinResult.manufacturer}) • Année VIN non standardisée",
                    titleAr = "رقم VIN صحيح (${vinResult.manufacturer}) • السنة غير مرمزة قياسياً",
                    titleEn = "Valid VIN (${vinResult.manufacturer}) • Non-Standard Year Code",
                    explanationFr = "Plusieurs constructeurs européens (${vinResult.manufacturer}) n'utilisent pas le 10e caractère pour l'année. Ce n'est pas un signe de fraude : fiez-vous à la Carte Grise et aux étiquettes.",
                    explanationAr = "العديد من المصنعين الأوروبيين (${vinResult.manufacturer}) لا يضعون السنة في الحرف العاشر. هذا ليس علامة غش، اعتمد على البطاقة الرمادية وملصقات السيارة.",
                    explanationEn = "Several European manufacturers (${vinResult.manufacturer}) do not encode the model year in the 10th character. This is normal—rely on the Carte Grise."
                )
            }
            if (vinYear != null && kotlin.math.abs(vinYear - carDeclaredYear) > 1) {
                return ConsistencyVerdict(
                    level = "WARNING",
                    titleFr = "Écart entre l'année du VIN ($vinYear) et l'année annoncée ($carDeclaredYear)",
                    titleAr = "اختلاف بين سنة رقم الشاسيه ($vinYear) والسنة المعلنة ($carDeclaredYear)",
                    titleEn = "Offset Between VIN Year ($vinYear) and Declared Year ($carDeclaredYear)",
                    explanationFr = "Le 10e caractère du VIN correspond à $vinYear. Demandez à vérifier la date exacte sur la Carte Grise et les vitres.",
                    explanationAr = "الحرف العاشر في رقم الشاسيه يشير إلى سنة $vinYear. تحقق من التاريخ المسجل في البطاقة الرمادية وزجاج السيارة.",
                    explanationEn = "The 10th VIN character decodes to $vinYear. Verify the exact date on the Carte Grise and window glass codes."
                )
            }
        }

        return ConsistencyVerdict(
            level = "MATCH",
            titleFr = "Identité et année cohérentes ($carDeclaredYear)",
            titleAr = "بيانات الهوية وسنة السيارة متطابقة ($carDeclaredYear)",
            titleEn = "Identity & Year Consistent ($carDeclaredYear)",
            explanationFr = "Le format du VIN et l'année d'immatriculation concordent avec le véhicule inspecté. Pensez à vérifier la frappe à froid sur la carrosserie.",
            explanationAr = "تركيبة رقم الشاسيه وسنة الترقيم متوافقة مع السيارة. لا تنسَ مطابقة الرقم المنقوش على الحديد.",
            explanationEn = "The VIN format and registration plate year match the vehicle. Remember to verify the physical metal stamping."
        )
    }
}
