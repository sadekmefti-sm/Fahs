package com.example.data.model

data class AlgerianCarModel(
    val id: String,
    val brand: String,
    val model: String,
    val popularRank: Int,
    val generations: List<CarGeneration>,
    val availableFuels: List<String>, // ESSENCE, DIESEL, GPL
    val availableGearboxes: List<String>, // MANUELLE, AUTOMATIQUE
    val commonEngines: List<String>,
    val obdPortHintFr: String,
    val obdPortHintAr: String,
    val obdPortHintEn: String,
    val keyWeakPointFr: String,
    val keyWeakPointAr: String,
    val keyWeakPointEn: String,
    val defaultBodyType: String = when (id) {
        "renault_symbol", "peugeot_301", "dacia_logan", "hyundai_accent", "skoda_octavia", "citroen_celysee", "fiat_tipo", "chevrolet_sail", "renault_fluence", "skoda_rapid", "toyota_corolla" -> "BERLINE"
        "dacia_duster", "kia_sportage", "hyundai_tucson", "peugeot_2008_3008", "vw_caddy_tiguan", "seat_arona_ateca", "hyundai_santafe_creta", "nissan_qashqai_sunny", "chery_qq_tiggo", "geely_gx3_coolray" -> "SUV"
        "renault_kangoo", "peugeot_partner", "citroen_berlingo", "chinese_haval_dfsk_jac" -> "UTILITAIRE"
        "toyota_hilux", "mahindra_mitsubishi_pickup" -> "PICKUP"
        else -> "CITADINE"
    },
    val hasFourWheelDriveOption: Boolean = id in setOf(
        "dacia_duster", "toyota_hilux", "kia_sportage", "hyundai_tucson",
        "vw_caddy_tiguan", "seat_arona_ateca", "hyundai_santafe_creta",
        "nissan_qashqai_sunny", "chinese_haval_dfsk_jac", "mahindra_mitsubishi_pickup"
    ),
    val weakPointVerificationStatus: VerificationStatus = VerificationStatus.TO_BE_VERIFIED
)

data class CarGeneration(
    val name: String,
    val yearRange: String,
    val defaultYear: Int
)

object AlgerianCarCatalog {
    val popularBrands = listOf(
        "All",
        "Renault",
        "Dacia",
        "Peugeot",
        "Volkswagen",
        "Seat",
        "Hyundai",
        "Kia",
        "Toyota",
        "Skoda",
        "Citroën",
        "Fiat",
        "Chevrolet",
        "Suzuki",
        "Nissan",
        "Ford",
        "Opel",
        "Chery",
        "Geely",
        "Changan / DFSK / Haval",
        "Mitsubishi / Mahindra"
    )

    val baseModels: List<AlgerianCarModel> = listOf(
        AlgerianCarModel(
            id = "renault_symbol",
            brand = "Renault",
            model = "Symbol",
            popularRank = 1,
            generations = listOf(
                CarGeneration("Symbol 3 (DZ Oued Tlélat)", "2013 - 2021", 2018),
                CarGeneration("Symbol 2", "2008 - 2013", 2011)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE"),
            commonEngines = listOf("1.2 16v 75ch", "1.6 MPI 80ch", "1.5 dCi 85ch"),
            obdPortHintFr = "Dans la boîte à gants ou console centrale devant le levier",
            obdPortHintAr = "داخل صندوق القفازات (Boîte à gants) أو أمام عصا السرعة",
            obdPortHintEn = "Inside glovebox or lower center console in front of gear stick",
            keyWeakPointFr = "Vérifier l'étanchéité du circuit de refroidissement et l'état du vapodétendeur GPL",
            keyWeakPointAr = "تأكد من عدم نقص ماء التبريد وسلامة تركيبة الغاز (سيرغاز GPL)",
            keyWeakPointEn = "Check coolant system leaks and LPG/GPL regulator condition"
        ),
        AlgerianCarModel(
            id = "renault_clio4",
            brand = "Renault",
            model = "Clio 4",
            popularRank = 2,
            generations = listOf(
                CarGeneration("Phase 2 (GT Line / Limited)", "2016 - 2020", 2018),
                CarGeneration("Phase 1", "2012 - 2016", 2014)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (EDC)"),
            commonEngines = listOf("1.5 dCi 90ch", "0.9 TCe 90ch", "1.2 16v 75ch"),
            obdPortHintFr = "Au bas de la console centrale devant le levier de vitesse (sous cache)",
            obdPortHintAr = "في أسفل الكونسول الأوسط أمام عصا السرعة تحت الغطاء البلاستيكي",
            obdPortHintEn = "Bottom of center console in front of gear lever under plastic cover",
            keyWeakPointFr = "Sur 1.5 dCi : contrôler le souffle du turbo et le bruit des injecteurs à froid",
            keyWeakPointAr = "في محرك 1.5 dCi: افحص صفير التيربو وصوت الحاقنات (Injecteurs) عند التشغيل البارد",
            keyWeakPointEn = "On 1.5 dCi: inspect turbo whistle and cold-start injector clatter"
        ),
        AlgerianCarModel(
            id = "peugeot_208",
            brand = "Peugeot",
            model = "208",
            popularRank = 3,
            generations = listOf(
                CarGeneration("208 I Phase 2 (Tech Vision / Allure)", "2015 - 2019", 2017),
                CarGeneration("208 I Phase 1", "2012 - 2015", 2013)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.6 HDi 92ch", "1.2 PureTech 82ch", "1.6 e-HDi 115ch"),
            obdPortHintFr = "Sous le volant à gauche, derrière le cache de la boîte à fusibles",
            obdPortHintAr = "أسفل المقود من الجهة اليسرى خلف غطاء علبة الفيوزات",
            obdPortHintEn = "Under steering wheel on the left, behind the fuse box cover",
            keyWeakPointFr = "Sur 1.2 PureTech : vérifier l'état de la courroie de distribution humide par le bouchon d'huile",
            keyWeakPointAr = "في محرك 1.2 PureTech: افحص عرض وتشققات حزام التوزيع (La chaîne/courroie) عبر فتحة الزيت",
            keyWeakPointEn = "On 1.2 PureTech: inspect wet timing belt condition through the oil filler cap"
        ),
        AlgerianCarModel(
            id = "dacia_stepway",
            brand = "Dacia",
            model = "Sandero Stepway",
            popularRank = 4,
            generations = listOf(
                CarGeneration("Stepway 2 Phase 2 (Extrême)", "2016 - 2021", 2019),
                CarGeneration("Stepway 2 Phase 1", "2013 - 2016", 2015)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE"),
            commonEngines = listOf("1.6 MPI 80ch", "1.5 dCi 85ch", "0.9 TCe 90ch"),
            obdPortHintFr = "À l'intérieur de la boîte à gants côté passager",
            obdPortHintAr = "داخل صندوق القفازات (Boîte à gants) بجهة الراكب",
            obdPortHintEn = "Inside the passenger glovebox compartment",
            keyWeakPointFr = "Vérifier le jeu dans la crémaillère de direction et les rotules de suspension avant",
            keyWeakPointAr = "تأكد من عدم وجود طقطقة في نظام التوجيه (Crémaillère) ومساعدات الأمام",
            keyWeakPointEn = "Check steering rack play and front suspension ball joints"
        ),
        AlgerianCarModel(
            id = "vw_golf7",
            brand = "Volkswagen",
            model = "Golf 7",
            popularRank = 5,
            generations = listOf(
                CarGeneration("Golf 7.5 Facelift (GTD / R-Line)", "2017 - 2020", 2018),
                CarGeneration("Golf 7 Phase 1", "2012 - 2017", 2015)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("AUTOMATIQUE (DSG)", "MANUELLE"),
            commonEngines = listOf("2.0 TDI 150ch / 184ch", "2.0 TDI 110ch / 143ch", "1.4 TSI"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur (prise violette apparente)",
            obdPortHintAr = "تحت الطابلوه جهة السائق فوق الدواسات (منفذ بنفسجي واضح)",
            obdPortHintEn = "Under driver dashboard above pedals (visible purple socket)",
            keyWeakPointFr = "Boîte DSG : vérifier l'absence d'à-coups au passage des rapports et la pompe à eau",
            keyWeakPointAr = "علبة سرعة DSG: تأكد من سلاسة الغيارات وعدم وجود تسريب في مضخة الماء (Pompe à eau)",
            keyWeakPointEn = "DSG Gearbox: check for smooth shifts without jerking and inspect water pump leaks"
        ),
        AlgerianCarModel(
            id = "seat_ibiza",
            brand = "Seat",
            model = "Ibiza",
            popularRank = 6,
            generations = listOf(
                CarGeneration("Ibiza 5 (Sovack DZ / FR / Style)", "2017 - 2021", 2018),
                CarGeneration("Ibiza 4 Phase 2", "2012 - 2017", 2015)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (DSG)"),
            commonEngines = listOf("1.6 MPI 110ch", "1.4 16v 85ch", "1.6 TDI 90ch"),
            obdPortHintFr = "Sous le volant à gauche derrière le petit rangement",
            obdPortHintAr = "أسفل المقود من الجهة اليسرى خلف الدرج الصغير",
            obdPortHintEn = "Under steering wheel on the left behind the small storage cubby",
            keyWeakPointFr = "Sur 1.6 MPI : vérifier la stabilité du ralenti et l'alignement du capot/ailes avant",
            keyWeakPointAr = "في محرك 1.6 MPI: راقب استقرار دوران المحرك وتوازي فتحات الغطاء الأمامي",
            keyWeakPointEn = "On 1.6 MPI: check idle stability and front hood/fender panel gaps"
        ),
        AlgerianCarModel(
            id = "hyundai_accent",
            brand = "Hyundai",
            model = "Accent RB",
            popularRank = 7,
            generations = listOf(
                CarGeneration("Accent RB (TMC Tiaret / GL)", "2011 - 2019", 2017),
                CarGeneration("Grand i10 / i20", "2014 - 2020", 2018)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.4 MPI 107ch", "1.6 MPI 124ch", "1.6 CRDi 128ch"),
            obdPortHintFr = "Sous le volant, derrière la trappe à fusibles intérieure",
            obdPortHintAr = "أسفل المقود خلف غطاء علبة الفيوزات الداخلية",
            obdPortHintEn = "Under steering wheel behind interior fuse box cover",
            keyWeakPointFr = "Contrôler la colonne de direction assistée électrique (EPS) et l'usure de l'embrayage",
            keyWeakPointAr = "افحص عدم وجود ثقل أو صوت في مقود الكهرباء (EPS) وحالة الدبرياج",
            keyWeakPointEn = "Check electric power steering (EPS) column noise and clutch wear"
        ),
        AlgerianCarModel(
            id = "peugeot_301",
            brand = "Peugeot",
            model = "301",
            popularRank = 8,
            generations = listOf(
                CarGeneration("Phase 2 (Restylée)", "2017 - 2020", 2018),
                CarGeneration("Phase 1 (Allure / Active)", "2012 - 2016", 2014)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.6 HDi 92ch", "1.2 VTi / PureTech", "1.6 VTi 115ch"),
            obdPortHintFr = "Sous le volant à gauche derrière le cache fusibles",
            obdPortHintAr = "أسفل المقود يساراً خلف غطاء الفيوزات",
            obdPortHintEn = "Under steering wheel on the left behind fuse cover",
            keyWeakPointFr = "Vérifier si le véhicule a servi de taxi ou usage intensif (usure pédales, sièges, joints)",
            keyWeakPointAr = "تأكد من عدم استهلاك السيارة المفرط (قارن تآكل الدواسات والمقود مع العداد)",
            keyWeakPointEn = "Check for heavy commercial wear (compare pedal and steering wear against odometer)"
        ),
        AlgerianCarModel(
            id = "dacia_duster",
            brand = "Dacia",
            model = "Duster",
            popularRank = 9,
            generations = listOf(
                CarGeneration("Duster 2", "2018 - 2022", 2019),
                CarGeneration("Duster 1 Phase 2", "2013 - 2017", 2015)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE"),
            commonEngines = listOf("1.5 dCi 110ch (4x2 / 4x4)", "1.6 16v 105ch"),
            obdPortHintFr = "Dans la boîte à gants côté passager",
            obdPortHintAr = "داخل صندوق القفازات (Boîte à gants)",
            obdPortHintEn = "Inside passenger side glovebox",
            keyWeakPointFr = "Inspecter le soubassement (chocs tout-terrain), les injecteurs 1.5 dCi et la boîte 6 rapports",
            keyWeakPointAr = "افحص أسفل الهيكل (آثار الطرق الوعرة)، حاقنات الديزل 1.5 dCi وعلبة 6 سرعات",
            keyWeakPointEn = "Inspect underbody for off-road impacts, 1.5 dCi injectors, and 6-speed gearbox"
        ),
        AlgerianCarModel(
            id = "kia_picanto",
            brand = "Kia",
            model = "Picanto",
            popularRank = 10,
            generations = listOf(
                CarGeneration("Picanto 3 (GT Line / Gloviz DZ)", "2017 - 2021", 2018),
                CarGeneration("Picanto 2 (Brazuca / Pop)", "2011 - 2017", 2014)
            ),
            availableFuels = listOf("ESSENCE", "GPL"),
            availableGearboxes = listOf("AUTOMATIQUE", "MANUELLE"),
            commonEngines = listOf("1.2 MPI 84ch (4 cylindres)", "1.0 MPI 67ch"),
            obdPortHintFr = "En bas à gauche du volant près de la poignée capot",
            obdPortHintAr = "في الأسفل يسار المقود بالقرب من مقبض فتح غطاء المحرك",
            obdPortHintEn = "Lower left of steering wheel near the hood release lever",
            keyWeakPointFr = "Vérifier les longerons avant (petit compartiment moteur) et le fonctionnement de la boîte auto",
            keyWeakPointAr = "افحص الشاسيه الأمامي (اللونجرون) بدقة، وسلاسة غيارات العلبة الأوتوماتيكية",
            keyWeakPointEn = "Carefully inspect front chassis legs (compact engine bay) and automatic transmission shifts"
        )
    )

    val models: List<AlgerianCarModel> = baseModels + AlgerianCarCatalogExtra.additionalModels + AlgerianCarCatalogExtra2.additionalModels2
}
