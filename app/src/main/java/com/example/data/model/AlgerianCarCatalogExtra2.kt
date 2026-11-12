package com.example.data.model

object AlgerianCarCatalogExtra2 {

    val additionalModels2: List<AlgerianCarModel> = listOf(
        AlgerianCarModel(
            id = "renault_clio2_3",
            brand = "Renault",
            model = "Clio 2 / Campus / Clio 3",
            popularRank = 29,
            generations = listOf(
                CarGeneration("Clio 3 (Phase 1 & 2)", "2006 - 2013", 2010),
                CarGeneration("Clio 2 Campus / Compus (La Batta)", "2001 - 2012", 2008)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE"),
            commonEngines = listOf("1.2 16v 75ch", "1.4 16v 98ch", "1.5 dCi 65ch / 85ch", "1.9 dTi"),
            obdPortHintFr = "Sous le cendrier / vide-poches devant le levier de vitesse",
            obdPortHintAr = "تحت طفاية السجائر أو الدرج الصغير أمام عصا السرعة",
            obdPortHintEn = "Under the ashtray / storage tray in front of the gear lever",
            keyWeakPointFr = "Sur 1.5 dCi 65/85ch : surveiller la limaille de pompe HP Delphi et les bobines sur 1.4 16v",
            keyWeakPointAr = "في محرك 1.5 dCi: افحص سلامة مضخة الديزل والحاقنات، وفي 1.4 16v افحص بوبينات الإشعال",
            keyWeakPointEn = "On 1.5 dCi: check high-pressure fuel pump/injectors; on 1.4 16v check ignition coils"
        ),
        AlgerianCarModel(
            id = "renault_megane",
            brand = "Renault",
            model = "Mégane 2 / Mégane 3",
            popularRank = 30,
            generations = listOf(
                CarGeneration("Mégane 3 (BZ / Coupé / Bose)", "2009 - 2016", 2013),
                CarGeneration("Mégane 2 (Casquette / Sedan)", "2003 - 2009", 2007)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (EDC)"),
            commonEngines = listOf("1.5 dCi 105ch / 110ch", "1.9 dCi 130ch", "1.6 16v 110ch"),
            obdPortHintFr = "En bas de la console centrale devant le levier (sous cache plastique)",
            obdPortHintAr = "في أسفل الكونسول الأوسط أمام عصا السرعة خلف الغطاء",
            obdPortHintEn = "Lower center console in front of the gear stick behind plastic cover",
            keyWeakPointFr = "Contrôler les coussinets de bielle sur anciens 1.5/1.9 dCi, le lecteur de carte et le verrou de colonne",
            keyWeakPointAr = "افحص صوت السبائك (Coussinets) في محركات dCi القديمة، قارئ البطاقة، وقفل عمود المقود الكهربائي",
            keyWeakPointEn = "Check rod bearing noise on older 1.5/1.9 dCi, key-card slot reader, and steering column lock"
        ),
        AlgerianCarModel(
            id = "renault_fluence",
            brand = "Renault",
            model = "Fluence",
            popularRank = 31,
            generations = listOf(
                CarGeneration("Fluence Phase 2", "2013 - 2016", 2014),
                CarGeneration("Fluence Phase 1", "2010 - 2013", 2012)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.6 16v 110ch", "1.5 dCi 105ch / 110ch"),
            obdPortHintFr = "Devant le levier de vitesse sous la trappe coulissante",
            obdPortHintAr = "أمام عصا السرعة تحت الغطاء الصغير",
            obdPortHintEn = "In front of the gear lever beneath the small sliding flap",
            keyWeakPointFr = "Sur 1.6 16v : vérifier la poulie déphaseur d'arbre à cames (bruit au démarrage) et l'usure taxi",
            keyWeakPointAr = "في محرك 1.6 16v: استمع لصوت بكرة الكامات (Poulie déphaseur) عند التشغيل البارد وتحقق من استهلاكها",
            keyWeakPointEn = "On 1.6 16v: listen for VVT camshaft dephaser pulley rattle on cold start and check commercial wear",
            defaultBodyType = "BERLINE"
        ),
        AlgerianCarModel(
            id = "peugeot_206",
            brand = "Peugeot",
            model = "206 / 206+",
            popularRank = 32,
            generations = listOf(
                CarGeneration("206+ (One Line / Urban)", "2009 - 2013", 2011),
                CarGeneration("206 (1.4 Essence / 1.4 HDi / 1.9D / 2.0 HDi)", "2000 - 2009", 2006)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.4 Essence 75ch (TU3)", "1.4 HDi 70ch", "1.9D DW8", "2.0 HDi 90ch"),
            obdPortHintFr = "Sous le volant à gauche derrière le cache de la boîte à fusibles",
            obdPortHintAr = "أسفل المقود من الجهة اليسرى خلف غطاء الفيوزات",
            obdPortHintEn = "Under the steering wheel on the left behind the fuse cover",
            keyWeakPointFr = "Contrôler l'essieu/train arrière (roues inclinées /\\), le joint de culasse sur 1.4 TU3 et le commodo COM2000",
            keyWeakPointAr = "افحص المحور الخلفي (ميلان العجلات الخلفية Train arrière)، جوان الكولاس في 1.4 بنزين، وذراع الإشارات",
            keyWeakPointEn = "Inspect rear torsion beam axle camber (/\\ wheels), head gasket on 1.4 TU3 petrol, and indicator stalk"
        ),
        AlgerianCarModel(
            id = "peugeot_307_406",
            brand = "Peugeot",
            model = "307 / 406",
            popularRank = 33,
            generations = listOf(
                CarGeneration("307 Phase 2 (1.6 HDi / 2.0 HDi)", "2005 - 2008", 2007),
                CarGeneration("307 Phase 1 / 406 Phase 2", "2000 - 2005", 2003)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE"),
            commonEngines = listOf("2.0 HDi 90ch / 110ch", "1.6 HDi 90ch / 110ch", "1.6 16v 110ch"),
            obdPortHintFr = "Sous le volant à gauche ou dans le vide-poches central",
            obdPortHintAr = "أسفل المقود يساراً خلف غطاء الفيوزات أو في الدرج الأوسط",
            obdPortHintEn = "Under steering wheel on the left or inside the center cubby",
            keyWeakPointFr = "Vérifier le volant moteur bimasse sur HDi 110ch, le multiplexage électrique (BSI) et le circuit d'eau",
            keyWeakPointAr = "افحص الفولان موتور (Volant bimasse) في HDi 110، علبة الكهرباء BSI، ودورة التبريد",
            keyWeakPointEn = "Check dual-mass flywheel on HDi 110hp, BSI electrical multiplexing, and cooling system"
        ),
        AlgerianCarModel(
            id = "peugeot_2008_3008",
            brand = "Peugeot",
            model = "2008 / 3008",
            popularRank = 34,
            generations = listOf(
                CarGeneration("3008 II / 2008 II (GT Line / Allure)", "2016 - 2022", 2018),
                CarGeneration("2008 I / 3008 I", "2012 - 2016", 2015)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (EAT6 / EAT8)"),
            commonEngines = listOf("1.6 HDi / BlueHDi 100ch / 120ch", "2.0 HDi 150ch", "1.2 PureTech 110ch / 130ch"),
            obdPortHintFr = "Sous le volant à gauche derrière la trappe fusibles ou sous la console centrale",
            obdPortHintAr = "أسفل المقود يساراً خلف غطاء الفيوزات أو أسفل الكونسول الأوسط",
            obdPortHintEn = "Under steering wheel behind fuse flap or under center console",
            keyWeakPointFr = "Sur BlueHDi : vérifier le réservoir AdBlue ; sur 1.2 PureTech : vérifier la courroie humide et la consommation d'huile",
            keyWeakPointAr = "في محرك BlueHDi: افحص نظام AdBlue، وفي 1.2 PureTech افحص حزام التوزيع المغمور بالزيت واستهلاك الزيت",
            keyWeakPointEn = "On BlueHDi: check AdBlue tank faults; on 1.2 PureTech: check wet timing belt and oil consumption",
            defaultBodyType = "SUV"
        ),
        AlgerianCarModel(
            id = "citroen_berlingo",
            brand = "Citroën",
            model = "Berlingo (M59 / B9 / Multispace)",
            popularRank = 35,
            generations = listOf(
                CarGeneration("Berlingo II (B9 Multispace / XTR)", "2008 - 2018", 2014),
                CarGeneration("Berlingo I Phase 2 (M59)", "2003 - 2011", 2008)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("MANUELLE"),
            commonEngines = listOf("1.6 HDi 90ch / 92ch", "1.9D DW8", "2.0 HDi 90ch"),
            obdPortHintFr = "Sous le volant derrière le cache fusibles",
            obdPortHintAr = "أسفل المقود خلف غطاء علبة الفيوزات",
            obdPortHintEn = "Under the steering wheel behind the fuse box cover",
            keyWeakPointFr = "Contrôler l'essieu arrière (affaissement par surcharge), les joints d'injecteurs 1.6 HDi et les portes latérales",
            keyWeakPointAr = "افحص المحور الخلفي من آثار الحمولة الثقيلة، جوانات حاقنات 1.6 HDi، وسكك الأبواب الجانبية",
            keyWeakPointEn = "Check rear axle sag from heavy loads, 1.6 HDi injector seals, and sliding door rollers",
            defaultBodyType = "UTILITAIRE"
        ),
        AlgerianCarModel(
            id = "citroen_c3_c4",
            brand = "Citroën",
            model = "C3 / C4",
            popularRank = 36,
            generations = listOf(
                CarGeneration("C3 III / C4 II Phase 2", "2015 - 2020", 2017),
                CarGeneration("C3 I & II / C4 I", "2004 - 2014", 2011)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.6 HDi 92ch / 115ch", "1.4 HDi 70ch", "1.2 PureTech 82ch", "1.4 Essence"),
            obdPortHintFr = "Sous le volant à gauche ou derrière le cendrier central",
            obdPortHintAr = "أسفل المقود يساراً أو خلف الدرج الأوسط",
            obdPortHintEn = "Under steering wheel on the left or behind center ashtray",
            keyWeakPointFr = "Vérifier les joints d'injecteurs sur HDi, la courroie humide sur 1.2 PureTech et les coupelles avant",
            keyWeakPointAr = "افحص جوانات الحاقنات في محركات HDi، حزام التوزيع في 1.2 PureTech، وكراسي المساعدات الأمامية",
            keyWeakPointEn = "Check injector seals on HDi engines, wet belt on 1.2 PureTech, and front strut mounts"
        ),
        AlgerianCarModel(
            id = "vw_golf5_6",
            brand = "Volkswagen",
            model = "Golf 5 / Golf 6",
            popularRank = 37,
            generations = listOf(
                CarGeneration("Golf 6 (2.0 TDI / 1.6 TDI / GTD)", "2009 - 2013", 2011),
                CarGeneration("Golf 5 (1.9 TDI / 2.0 TDI)", "2004 - 2009", 2007)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (DSG)"),
            commonEngines = listOf("1.9 TDI 105ch", "2.0 TDI 110ch / 140ch / 170ch", "1.6 TDI 105ch", "1.6 Essence"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur au-dessus des pédales",
            obdPortHintAr = "تحت الطابلوه جهة السائق فوق الدواسات",
            obdPortHintEn = "Under driver dashboard above the pedals",
            keyWeakPointFr = "Vérifier le volant moteur bimasse, le turbo à géométrie variable, la culasse poreuse (anciens 2.0 TDI BKD) et le kilométrage réel",
            keyWeakPointAr = "افحص الفولان موتور، التيربو، سلامة الكولاس في محركات 2.0 TDI القديمة، وحقيقة العداد عبر الكمبيوتر",
            keyWeakPointEn = "Check dual-mass flywheel, VNT turbo boost, porous head on early 2.0 TDI (BKD), and true ECU mileage"
        ),
        AlgerianCarModel(
            id = "vw_caddy_tiguan",
            brand = "Volkswagen",
            model = "Caddy / Tiguan / Passat",
            popularRank = 38,
            generations = listOf(
                CarGeneration("Caddy 4 / Tiguan 2 (Sovack DZ / R-Line)", "2015 - 2020", 2018),
                CarGeneration("Caddy 3 / Tiguan 1 / Passat B6-B7", "2006 - 2015", 2012)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (DSG)"),
            commonEngines = listOf("2.0 TDI 110ch / 140ch / 150ch / 177ch", "1.9 TDI 105ch"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur à gauche des pédales",
            obdPortHintAr = "تحت الطابلوه جهة السائق يسار الدواسات",
            obdPortHintEn = "Under driver dashboard to the left of the pedals",
            keyWeakPointFr = "Vérifier la pompe à eau 2.0 TDI, le volant moteur DSG, la vanne EGR et les lames arrière sur Caddy",
            keyWeakPointAr = "افحص مضخة الماء في 2.0 TDI، الفولان موتور وعلبة DSG، صمام EGR، وصفائح النوابض الخلفية في كادي",
            keyWeakPointEn = "Inspect 2.0 TDI water pump, DSG flywheel, EGR valve, and rear leaf springs on Caddy",
            defaultBodyType = "SUV",
            hasFourWheelDriveOption = true
        ),
        AlgerianCarModel(
            id = "seat_arona_ateca",
            brand = "Seat",
            model = "Arona / Ateca",
            popularRank = 39,
            generations = listOf(
                CarGeneration("Arona / Ateca (Sovack DZ FR / Xcellence / Style)", "2017 - 2021", 2019)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (DSG)"),
            commonEngines = listOf("1.6 MPI 110ch (Arona)", "2.0 TDI 143ch / 150ch (Ateca)"),
            obdPortHintFr = "Sous le volant à gauche près de la poignée capot",
            obdPortHintAr = "أسفل المقود يساراً بالقرب من مقبض فتح الغطاء الأمامي",
            obdPortHintEn = "Under steering wheel on the left near the hood release lever",
            keyWeakPointFr = "Sur Arona 1.6 MPI : vérifier la consommation d'huile ; sur Ateca 2.0 TDI : vérifier la pompe à eau et la boîte DSG",
            keyWeakPointAr = "في أرونا 1.6 MPI: راقب مستوى الزيت؛ وفي أتيكا 2.0 TDI افحص مضخة الماء وغيارات علبة DSG",
            keyWeakPointEn = "On Arona 1.6 MPI: check oil level; on Ateca 2.0 TDI: check water pump seepage and DSG shifts",
            defaultBodyType = "SUV",
            hasFourWheelDriveOption = true
        ),
        AlgerianCarModel(
            id = "skoda_rapid",
            brand = "Skoda",
            model = "Rapid / Spaceback / Yeti",
            popularRank = 40,
            generations = listOf(
                CarGeneration("Rapid / Spaceback (Sovack DZ)", "2013 - 2019", 2018)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.6 MPI 90ch / 110ch", "1.6 TDI 90ch / 105ch", "2.0 TDI (Yeti)"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur",
            obdPortHintAr = "تحت الطابلوه جهة السائق",
            obdPortHintEn = "Under driver-side dashboard",
            keyWeakPointFr = "Vérifier les silentblocs de train avant, l'usage intensif/VTC et l'étanchéité de la pompe à eau",
            keyWeakPointAr = "افحص جلد نظام التعليق الأمامي، آثار الاستهلاك المفرط، وعدم وجود تسريب في مضخة الماء",
            keyWeakPointEn = "Check front suspension bushings, high-mileage commercial wear, and water pump leaks",
            defaultBodyType = "BERLINE"
        ),
        AlgerianCarModel(
            id = "hyundai_atos_i20",
            brand = "Hyundai",
            model = "i20 / Getz / Atos Prime",
            popularRank = 41,
            generations = listOf(
                CarGeneration("i20 (TMC Tiaret / 2ème Gén)", "2014 - 2020", 2018),
                CarGeneration("Getz / Atos Prime (GL / GLS)", "2001 - 2011", 2008)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.2 / 1.4 MPI (i20)", "1.1 Epsilon (Atos)", "1.4 Essence / 1.5 CRDi (Getz)"),
            obdPortHintFr = "Sous le volant près de la boîte à fusibles",
            obdPortHintAr = "أسفل المقود بالقرب من علبة الفيوزات",
            obdPortHintEn = "Under the steering wheel near the fuse box",
            keyWeakPointFr = "Sur Atos/Getz : vérifier la corrosion des bas de caisse/longerons, le joint de culasse et la direction",
            keyWeakPointAr = "في أتوس وجيتز: افحص الصدأ في العتبات والشاسيه، دورة التبريد، وعلبة التوجيه",
            keyWeakPointEn = "On Atos/Getz: inspect sills/chassis rails for rust, cooling/head gasket health, and steering rack"
        ),
        AlgerianCarModel(
            id = "hyundai_santafe_creta",
            brand = "Hyundai",
            model = "Santa Fe / Creta",
            popularRank = 42,
            generations = listOf(
                CarGeneration("Creta / Santa Fe 3 (TMC Tiaret)", "2013 - 2020", 2017),
                CarGeneration("Santa Fe 2 (2.2 CRDi)", "2006 - 2012", 2010)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("AUTOMATIQUE", "MANUELLE"),
            commonEngines = listOf("2.2 CRDi 197ch / 200ch", "1.6 CRDi 128ch (Creta)", "1.6 MPI (Creta)"),
            obdPortHintFr = "Sous le volant à gauche derrière la trappe fusibles",
            obdPortHintAr = "أسفل المقود يساراً خلف غطاء الفيوزات",
            obdPortHintEn = "Under steering wheel on the left behind fuse flap",
            keyWeakPointFr = "Contrôler la boîte de transfert 4x4 sur Santa Fe, la crépine d'huile 2.2 CRDi et le volant moteur",
            keyWeakPointAr = "افحص علبة الدفع الرباعي 4x4 في سانتافي، مصفاة الزيت في محرك 2.2 CRDi، والفولان موتور",
            keyWeakPointEn = "Check AWD transfer case splines on Santa Fe, oil pickup strainer on 2.2 CRDi, and flywheel",
            defaultBodyType = "SUV",
            hasFourWheelDriveOption = true
        ),
        AlgerianCarModel(
            id = "kia_rio",
            brand = "Kia",
            model = "Rio / Cerato",
            popularRank = 43,
            generations = listOf(
                CarGeneration("Rio 4 (Gloviz DZ / GT Line)", "2017 - 2021", 2018),
                CarGeneration("Rio 3 / Cerato", "2011 - 2017", 2014)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.4 MPI 100ch / 109ch", "1.2 MPI 84ch", "1.6 MPI"),
            obdPortHintFr = "Sous le volant à gauche dans le panneau à fusibles",
            obdPortHintAr = "أسفل المقود يساراً داخل لوحة الفيوزات",
            obdPortHintEn = "Under steering wheel on the left inside the fuse panel",
            keyWeakPointFr = "Vérifier le jeu dans la colonne de direction électrique (bague étoile EPS) et l'usure de l'embrayage",
            keyWeakPointAr = "افحص عدم وجود طقطقة في عمود المقود الكهربائي (EPS) وحالة الدبرياج",
            keyWeakPointEn = "Check electric power steering column coupler play (EPS clunk) and clutch wear"
        ),
        AlgerianCarModel(
            id = "toyota_corolla",
            brand = "Toyota",
            model = "Corolla / Auris",
            popularRank = 44,
            generations = listOf(
                CarGeneration("Corolla 11 / 12 (Millenium / Sedan)", "2013 - 2022", 2016),
                CarGeneration("Corolla 9 / 10 (1.4 D-4D / VVT-i)", "2002 - 2012", 2008)
            ),
            availableFuels = listOf("ESSENCE", "DIESEL", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.4 D-4D 90ch", "2.0 D-4D 126ch", "1.3 / 1.6 VVT-i"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur au-dessus des pédales",
            obdPortHintAr = "تحت الطابلوه جهة السائق فوق الدواسات",
            obdPortHintEn = "Under driver dashboard above the pedals",
            keyWeakPointFr = "Sur 1.4 D-4D : vérifier le turbo et la consommation d'huile ; éviter la boîte robotisée MMT si elle broute",
            keyWeakPointAr = "في محرك 1.4 D-4D: افحص التيربو ومستوى الزيت؛ واحذر من العلبة الآلية MMT إذا كانت ترتجف",
            keyWeakPointEn = "On 1.4 D-4D: check turbo and oil consumption; watch out for jerky MMT automated manual gearbox",
            defaultBodyType = "BERLINE"
        ),
        AlgerianCarModel(
            id = "nissan_qashqai_sunny",
            brand = "Nissan",
            model = "Qashqai / Sunny / Micra / Navara",
            popularRank = 45,
            generations = listOf(
                CarGeneration("Qashqai J10 / J11 (1.5 dCi / 2.0 dCi)", "2008 - 2019", 2014),
                CarGeneration("Sunny / Micra / Navara D40", "2006 - 2017", 2012)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.5 dCi 106ch / 110ch (Qashqai)", "1.5 Essence (Sunny)", "1.2 Essence (Micra)", "2.5 dCi (Navara)"),
            obdPortHintFr = "Sous le volant à gauche derrière la trappe fusibles",
            obdPortHintAr = "أسفل المقود يساراً خلف غطاء الفيوزات",
            obdPortHintEn = "Under steering wheel on the left behind fuse flap",
            keyWeakPointFr = "Sur Qashqai 1.5 dCi : contrôler les coussinets de bielle, le turbo et le FAP ; sur Navara : inspecter le châssis",
            keyWeakPointAr = "في قاشقاي 1.5 dCi: افحص السبائك (Coussinets) والتيربو؛ وفي نافارا افحص الشاسيه من التشققات",
            keyWeakPointEn = "On Qashqai 1.5 dCi: check rod bearings, turbo, and DPF; on Navara: inspect frame rails for cracks",
            defaultBodyType = "SUV",
            hasFourWheelDriveOption = true
        ),
        AlgerianCarModel(
            id = "ford_fiesta_focus",
            brand = "Ford",
            model = "Fiesta / Focus",
            popularRank = 46,
            generations = listOf(
                CarGeneration("Fiesta 6 / Focus 3 (Titanium / Trend)", "2010 - 2018", 2014),
                CarGeneration("Fiesta 5 / Focus 2", "2004 - 2010", 2008)
            ),
            availableFuels = listOf("ESSENCE", "DIESEL", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.25 / 1.4 Zetec Essence", "1.4 TDCi 68ch", "1.6 TDCi 90ch / 115ch", "1.5 TDCi"),
            obdPortHintFr = "Sous le volant à gauche dans le petit vide-poches basculant",
            obdPortHintAr = "أسفل المقود يساراً داخل الدرج الصغير القابل للفتح",
            obdPortHintEn = "Under steering wheel on the left inside the flip-down storage cubby",
            keyWeakPointFr = "Vérifier l'étanchéité du boîtier thermostat/pompe à eau, les joints d'injecteurs TDCi et éviter la boîte PowerShift à sec",
            keyWeakPointAr = "افحص عدم تسرب الماء من علبة الثرموستات، جوانات حاقنات TDCi، واحذر من أعطال علبة PowerShift الأوتوماتيكية",
            keyWeakPointEn = "Check thermostat housing coolant leaks, TDCi injector seals, and avoid dry-clutch PowerShift gearbox faults"
        ),
        AlgerianCarModel(
            id = "opel_corsa_astra",
            brand = "Opel",
            model = "Corsa / Astra",
            popularRank = 47,
            generations = listOf(
                CarGeneration("Corsa D / E & Astra J", "2008 - 2018", 2013),
                CarGeneration("Corsa C & Astra H", "2003 - 2009", 2007)
            ),
            availableFuels = listOf("ESSENCE", "DIESEL", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.2 / 1.4 Twinport Essence", "1.3 CDTI 75ch / 90ch", "1.7 CDTI 110ch / 125ch"),
            obdPortHintFr = "Sous l'autoradio devant le levier de vitesse ou sous le volant à gauche",
            obdPortHintAr = "تحت المسجل أمام عصا السرعة أو أسفل المقود يساراً",
            obdPortHintEn = "Below radio in front of gear lever or under steering wheel on the left",
            keyWeakPointFr = "Contrôler la chaîne de distribution (1.2/1.4 Essence et 1.3 CDTI), le circuit de refroidissement et les roulements de boîte M32",
            keyWeakPointAr = "افحص صوت سلسلة التوزيع (La chaîne)، دورة التبريد، وعدم وجود صفير في رولمانات علبة السرعة",
            keyWeakPointEn = "Check timing chain rattle (1.2/1.4 petrol & 1.3 CDTI), cooling system, and M32 gearbox bearing whine"
        ),
        AlgerianCarModel(
            id = "chery_qq_tiggo",
            brand = "Chery",
            model = "Tiggo 2 / 4 / 7 / 8 Pro & QQ",
            popularRank = 48,
            generations = listOf(
                CarGeneration("Gamme Chery Algérie (Tiggo 2 Pro / 4 Pro / 7 Pro / 8 Pro / Arrizo 5)", "2023 - 2025", 2024),
                CarGeneration("Chery QQ / Tiggo 1ère Gén", "2006 - 2016", 2012)
            ),
            availableFuels = listOf("ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (CVT / DCT)"),
            commonEngines = listOf("1.5L Essence (Tiggo 2 / 4 / Arrizo)", "1.5T / 1.6T (Tiggo 7 / 8 Pro)", "0.8L / 1.1L (Chery QQ)"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur au-dessus des pédales",
            obdPortHintAr = "تحت الطابلوه جهة السائق فوق الدواسات",
            obdPortHintEn = "Under driver-side dashboard above the pedals",
            keyWeakPointFr = "Sur QQ : vérifier le joint de culasse et la corrosion ; sur Tiggo récents : vérifier le carnet de garantie et la boîte CVT/DCT",
            keyWeakPointAr = "في QQ: افحص جوان الكولاس والصدأ؛ وفي سيارات Tiggo الحديثة: تحقق من دفتر الضمان وسلاسة علبة CVT/DCT",
            keyWeakPointEn = "On QQ: check head gasket and rust; on newer Tiggo: verify warranty service book and CVT/DCT smoothness",
            defaultBodyType = "SUV"
        ),
        AlgerianCarModel(
            id = "geely_gx3_coolray",
            brand = "Geely",
            model = "GX3 Pro / Coolray / Emgrand",
            popularRank = 49,
            generations = listOf(
                CarGeneration("Geely Algérie (GX3 Pro / Coolray / Emgrand / Starray)", "2023 - 2025", 2024)
            ),
            availableFuels = listOf("ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (CVT / DCT)"),
            commonEngines = listOf("1.5L DVVT 103ch (GX3 Pro / Emgrand)", "1.5TD 172ch / 181ch (Coolray)"),
            obdPortHintFr = "Sous le tableau de bord à gauche du volant",
            obdPortHintAr = "تحت الطابلوه على يسار المقود",
            obdPortHintEn = "Under the dashboard to the left of the steering wheel",
            keyWeakPointFr = "Vérifier l'historique d'entretien en concession (maintien de garantie), l'absence de choc caché et l'installation GPL si ajoutée",
            keyWeakPointAr = "تحقق من فواتير الصيانة الدورية للحفاظ على الضمان، خلو الهيكل من الصدمات، وجودة تركيب الغاز GPL إن وجد",
            keyWeakPointEn = "Verify dealer service records (warranty validity), inspect for hidden body repairs, and check LPG/GPL install if fitted",
            defaultBodyType = "SUV"
        ),
        AlgerianCarModel(
            id = "chinese_haval_dfsk_jac",
            brand = "Changan / DFSK / Haval",
            model = "DFSK / Great Wall / Haval / Jetour / JAC",
            popularRank = 50,
            generations = listOf(
                CarGeneration("Jetour (X70 / Dashing) / Haval (H6 / Jolion) / BAIC", "2019 - 2025", 2023),
                CarGeneration("DFSK (K01 / C31 / Glory) / Great Wall Wingle / Changan", "2008 - 2023", 2016)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.2 / 1.5 Essence / GPL (DFSK)", "1.5T (Jetour / Haval / Glory)", "2.0 TDI / 2.8 TC (Great Wall Wingle)"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur près de la colonne de direction",
            obdPortHintAr = "تحت الطابلوه جهة السائق بالقرب من عمود المقود",
            obdPortHintEn = "Under driver dashboard near the steering column",
            keyWeakPointFr = "Sur utilitaires DFSK/Wingle : vérifier le châssis, le pont arrière et le refroidissement ; sur SUV Turbo : vérifier la boîte DCT",
            keyWeakPointAr = "في النفعيات DFSK/Wingle: افحص الشاسيه، الكرونة الخلفية (Pont) والتبريد؛ وفي سيارات SUV افحص علبة السرعة الأوتوماتيكية",
            keyWeakPointEn = "On DFSK/Wingle pickups: inspect frame, rear differential, and cooling; on turbo SUVs: check DCT gearbox",
            defaultBodyType = "UTILITAIRE",
            hasFourWheelDriveOption = true
        ),
        AlgerianCarModel(
            id = "mahindra_mitsubishi_pickup",
            brand = "Mitsubishi / Mahindra",
            model = "Mitsubishi L200 / Mahindra Scorpio & KUV100",
            popularRank = 51,
            generations = listOf(
                CarGeneration("Mitsubishi L200 (Sportero / Double Cabine) & Mahindra KUV100", "2008 - 2021", 2015),
                CarGeneration("Mahindra Scorpio / Pickup (2.2 mHawk / 2.5)", "2006 - 2018", 2012)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("2.5 DI-D 136ch / 178ch (L200)", "2.2 mHawk Diesel (Scorpio)", "1.2 mFalcon (KUV100)"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur",
            obdPortHintAr = "تحت الطابلوه جهة السائق",
            obdPortHintEn = "Under the driver-side dashboard",
            keyWeakPointFr = "Inspecter le châssis échelle (fissures/rouille), l'enclenchement de la boîte de transfert 4x4 et la chauffe moteur",
            keyWeakPointAr = "افحص الشاسيه السلمي من الشقوق والصدأ، تعشيق الدفع الرباعي 4x4، وحرارة المحرك تحت الحمل",
            keyWeakPointEn = "Inspect ladder frame for cracks/rust, test 4x4 transfer case engagement, and check cooling under load",
            defaultBodyType = "PICKUP",
            hasFourWheelDriveOption = true
        )
    )
}
