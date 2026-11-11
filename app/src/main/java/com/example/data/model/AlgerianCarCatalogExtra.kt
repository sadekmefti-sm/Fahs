package com.example.data.model

object AlgerianCarCatalogExtra {

    val additionalModels: List<AlgerianCarModel> = listOf(
        AlgerianCarModel(
            id = "dacia_logan",
            brand = "Dacia",
            model = "Logan",
            popularRank = 11,
            generations = listOf(
                CarGeneration("Logan 2 Phase 2 (Lauréate / DZ)", "2017 - 2021", 2019),
                CarGeneration("Logan 2 Phase 1", "2013 - 2016", 2014),
                CarGeneration("Logan 1 (1.4 / 1.6 MPI)", "2005 - 2012", 2010)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE"),
            commonEngines = listOf("1.6 MPI 80ch", "1.2 16v 75ch", "1.5 dCi 85ch", "1.4 MPI 75ch"),
            obdPortHintFr = "À l'intérieur de la boîte à gants côté passager",
            obdPortHintAr = "داخل صندوق القفازات (Boîte à gants) بجهة الراكب",
            obdPortHintEn = "Inside the passenger glovebox compartment",
            keyWeakPointFr = "Vérifier l'usage intensif/taxi, le réglage des soupapes si équipée GPL et l'état du train avant",
            keyWeakPointAr = "تحقق من عدم استهلاكها كسيارة أجرة، وضبط الصبابات (Soupapes) إذا كانت بالغاز GPL",
            keyWeakPointEn = "Check for heavy taxi wear, valve clearance if running on LPG/GPL, and front suspension"
        ),
        AlgerianCarModel(
            id = "vw_polo",
            brand = "Volkswagen",
            model = "Polo",
            popularRank = 12,
            generations = listOf(
                CarGeneration("Polo 6 (AW / Beats / Carat)", "2018 - 2022", 2019),
                CarGeneration("Polo 5 (6R / 6C Conceptline / Match)", "2010 - 2017", 2015)
            ),
            availableFuels = listOf("ESSENCE", "DIESEL", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (DSG)"),
            commonEngines = listOf("1.6 MPI 90ch / 110ch", "1.4 16v 85ch", "1.2 TSI", "1.6 TDI 90ch"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur au-dessus de la pédale d'embrayage",
            obdPortHintAr = "تحت الطابلوه جهة السائق فوق دواسة الدبرياج",
            obdPortHintEn = "Under driver dashboard above the clutch/brake pedal area",
            keyWeakPointFr = "Contrôler les bobines d'allumage sur Essence, la pompe à eau et les silentblocs de triangles avant",
            keyWeakPointAr = "افحص بوبينات الإشعال (Bobines) في البنزين، مضخة الماء، وجلد المقصات الأمامية",
            keyWeakPointEn = "Check ignition coils on petrol models, water pump seepage, and front control arm bushings"
        ),
        AlgerianCarModel(
            id = "hyundai_i10",
            brand = "Hyundai",
            model = "Grand i10",
            popularRank = 13,
            generations = listOf(
                CarGeneration("Grand i10 (TMC Tiaret / Berline & Sedan)", "2016 - 2020", 2018),
                CarGeneration("i10 1ère Gén", "2008 - 2013", 2012)
            ),
            availableFuels = listOf("ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.2 MPI 87ch (4 cylindres)", "1.0 MPI 66ch"),
            obdPortHintFr = "Sous le volant à gauche derrière le petit cache à fusibles",
            obdPortHintAr = "أسفل المقود من الجهة اليسرى خلف غطاء الفيوزات",
            obdPortHintEn = "Under steering wheel on the left behind the fuse panel cover",
            keyWeakPointFr = "Contrôler la dureté de la pédale d'embrayage, le support moteur inférieur et la crémaillère EPS",
            keyWeakPointAr = "تأكد من ليونة دواسة الدبرياج، كرسي المحرك السفلي، وعدم وجود طقطقة في المقود الكهربائي",
            keyWeakPointEn = "Check clutch pedal stiffness, lower engine mount, and electric steering column play"
        ),
        AlgerianCarModel(
            id = "peugeot_207",
            brand = "Peugeot",
            model = "207",
            popularRank = 14,
            generations = listOf(
                CarGeneration("207 Phase 2 (Série 64 / Allure)", "2009 - 2014", 2012),
                CarGeneration("207 Phase 1", "2006 - 2009", 2008)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE"),
            commonEngines = listOf("1.6 HDi 90ch / 92ch", "1.4 HDi 70ch", "1.4 Essence 75ch"),
            obdPortHintFr = "Sous le volant à gauche derrière la trappe d'accès aux fusibles",
            obdPortHintAr = "أسفل المقود يساراً خلف غطاء علبة الفيوزات",
            obdPortHintEn = "Under steering wheel on the left behind the fuse access cover",
            keyWeakPointFr = "Sur 1.6 HDi : vérifier les joints d'injecteurs (goudron noir) et l'alignement du train arrière",
            keyWeakPointAr = "في محرك 1.6 HDi: افحص جوانات الحاقنات (تراكم زفت أسود) واستقامة المحور الخلفي (Train arrière)",
            keyWeakPointEn = "On 1.6 HDi: check injector copper seals (black tar buildup) and rear axle wheel camber"
        ),
        AlgerianCarModel(
            id = "peugeot_308",
            brand = "Peugeot",
            model = "308",
            popularRank = 15,
            generations = listOf(
                CarGeneration("308 II (T9 Féline / Allure / GT Line)", "2014 - 2020", 2017),
                CarGeneration("308 I", "2008 - 2013", 2012)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (EAT6)"),
            commonEngines = listOf("1.6 HDi 92ch / 115ch", "1.6 BlueHDi 120ch", "2.0 HDi 150ch", "1.2 PureTech 130ch"),
            obdPortHintFr = "Sous l'accoudoir ou derrière le cache sous la console centrale devant le levier",
            obdPortHintAr = "في أسفل الكونسول الأوسط أمام عصا السرعة خلف الغطاء البلاستيكي",
            obdPortHintEn = "Lower center console in front of the gear lever behind the trim cover",
            keyWeakPointFr = "Sur 1.6 HDi / BlueHDi : vérifier le FAP, le volant moteur bimasse et l'écran tactile",
            keyWeakPointAr = "افحص حالة مصفاة البيئة FAP، صوت الفولان موتور (Volant bimasse) واستجابة الشاشة اللمسية",
            keyWeakPointEn = "Check DPF/FAP soot load, dual-mass flywheel rattle at idle, and touchscreen responsiveness"
        ),
        AlgerianCarModel(
            id = "renault_kangoo",
            brand = "Renault",
            model = "Kangoo",
            popularRank = 16,
            generations = listOf(
                CarGeneration("Kangoo 2 Phase 2 (Extrême / Limousine)", "2013 - 2020", 2016),
                CarGeneration("Kangoo 1 (D65 / Express)", "2002 - 2012", 2009)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE"),
            commonEngines = listOf("1.5 dCi 85ch / 90ch / 110ch", "1.9 dTi / 1.9D", "1.6 16v"),
            obdPortHintFr = "Sous le tableau de bord à gauche du volant dans le compartiment fusibles",
            obdPortHintAr = "أسفل الطابلوه على يسار المقود داخل علبة الفيوزات",
            obdPortHintEn = "Under the dashboard to the left of the steering wheel inside the fuse compartment",
            keyWeakPointFr = "Vérifier la surcharge commerciale (essieu arrière, portes coulissantes) et le turbo 1.5 dCi",
            keyWeakPointAr = "افحص آثار الحمولة التجارية الثقيلة (المحور الخلفي والأبواب السحابة) وحالة تيربو 1.5 dCi",
            keyWeakPointEn = "Check for heavy commercial overloading (rear axle sag, sliding doors) and 1.5 dCi turbo health"
        ),
        AlgerianCarModel(
            id = "peugeot_partner",
            brand = "Peugeot",
            model = "Partner Tepee",
            popularRank = 17,
            generations = listOf(
                CarGeneration("Partner II Phase 2 / 3 (Tepee Outdoor)", "2012 - 2019", 2016),
                CarGeneration("Partner I (Origin / M59)", "2003 - 2012", 2010)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("MANUELLE"),
            commonEngines = listOf("1.6 HDi 90ch / 92ch", "1.9D / 2.0 HDi"),
            obdPortHintFr = "Sous le volant à droite ou à gauche derrière le cache fusibles",
            obdPortHintAr = "أسفل المقود خلف غطاء علبة الفيوزات",
            obdPortHintEn = "Under steering wheel behind the fuse box cover",
            keyWeakPointFr = "Contrôler l'affaissement du train arrière (roues en / \\) et les fuites d'huile autour du doseur d'air",
            keyWeakPointAr = "راقب ميلان العجلات الخلفية (تآكل المحور الخلفي Train arrière) وتسريبات الزيت حول المحرك",
            keyWeakPointEn = "Check rear axle sag/negative camber and oil seepage around the air intake doser"
        ),
        AlgerianCarModel(
            id = "seat_leon",
            brand = "Seat",
            model = "Leon",
            popularRank = 18,
            generations = listOf(
                CarGeneration("Leon 3 (5F FR / Linea R / Style)", "2013 - 2020", 2018),
                CarGeneration("Leon 2", "2006 - 2012", 2011)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE (DSG)"),
            commonEngines = listOf("2.0 TDI 143ch / 150ch / 184ch", "1.6 TDI 110ch", "1.4 TSI"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur à gauche des pédales",
            obdPortHintAr = "تحت الطابلوه جهة السائق على يسار الدواسات",
            obdPortHintEn = "Under driver-side dashboard to the left of the pedals",
            keyWeakPointFr = "Contrôler l'étanchéité du toit ouvrant panoramique, la pompe à eau 2.0 TDI et le volant moteur",
            keyWeakPointAr = "افحص عدم تسرب الماء من السقف البانورامي، مضخة الماء في 2.0 TDI، وصوت الفولان موتور",
            keyWeakPointEn = "Check panoramic sunroof drain leaks, 2.0 TDI water pump, and dual-mass flywheel noise"
        ),
        AlgerianCarModel(
            id = "skoda_octavia",
            brand = "Skoda",
            model = "Octavia",
            popularRank = 19,
            generations = listOf(
                CarGeneration("Octavia 3 Phase 2 (Sovack DZ / Clever / Laurin & Klement)", "2017 - 2020", 2018),
                CarGeneration("Octavia 3 Phase 1", "2013 - 2017", 2015)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("AUTOMATIQUE (DSG)", "MANUELLE"),
            commonEngines = listOf("2.0 TDI 143ch / 150ch", "1.6 MPI 110ch"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur près de la poignée d'ouverture capot",
            obdPortHintAr = "تحت الطابلوه جهة السائق بالقرب من مقبض فتح غطاء المحرك",
            obdPortHintEn = "Under driver dashboard near the bonnet release handle",
            keyWeakPointFr = "Vérifier le kilométrage réel via le calculateur DSG (souvent gros rouleurs) et le volant moteur",
            keyWeakPointAr = "تحقق من الكيلومترات الحقيقية عبر كمبيوتر علبة DSG (غالباً ما تقطع مسافات طويلة) والفولان موتور",
            keyWeakPointEn = "Verify true mileage inside the DSG transmission ECU (often high-mileage cruisers) and flywheel"
        ),
        AlgerianCarModel(
            id = "skoda_fabia",
            brand = "Skoda",
            model = "Fabia",
            popularRank = 20,
            generations = listOf(
                CarGeneration("Fabia 3 (Sovack DZ / Monte Carlo / Ambition)", "2015 - 2020", 2018),
                CarGeneration("Fabia 2", "2007 - 2014", 2012)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.6 MPI 90ch / 110ch", "1.2 HTP / 1.4 16v"),
            obdPortHintFr = "Sous le volant à gauche derrière le vide-poches conducteur",
            obdPortHintAr = "أسفل المقود يساراً خلف درج التخزين الصغير للسائق",
            obdPortHintEn = "Under steering wheel on the left behind the driver storage cubby",
            keyWeakPointFr = "Sur 1.6 MPI : vérifier la stabilité du ralenti, les silentblocs de barre stabilisatrice et la climatisation",
            keyWeakPointAr = "في محرك 1.6 MPI: افحص استقرار دوران المحرك، جلد عمود التوازن وكفاءة المكيف",
            keyWeakPointEn = "On 1.6 MPI: check idle stability, anti-roll bar bushings, and A/C compressor cooling"
        ),
        AlgerianCarModel(
            id = "toyota_hilux",
            brand = "Toyota",
            model = "Hilux",
            popularRank = 21,
            generations = listOf(
                CarGeneration("Hilux 8 (Revo Simple / Double Cabine)", "2016 - 2023", 2018),
                CarGeneration("Hilux 7 (Vigo 2.5 D-4D / 3.0 D-4D)", "2006 - 2015", 2013)
            ),
            availableFuels = listOf("DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("2.4 D-4D 150ch", "2.5 D-4D 102ch / 144ch", "3.0 D-4D"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur au-dessus de la pédale d'accélérateur",
            obdPortHintAr = "تحت الطابلوه جهة السائق فوق الدواسات",
            obdPortHintEn = "Under driver dashboard above the pedal cluster",
            keyWeakPointFr = "Inspecter le châssis échelle (corrosion/fissures), les lames de ressort et les valeurs des injecteurs D-4D",
            keyWeakPointAr = "افحص شاسيه الحديد السلمي (عدم وجود شقوق أو لحام)، صفائح النوابض (Lames) وحاقنات D-4D",
            keyWeakPointEn = "Inspect ladder frame chassis for cracks/welds, rear leaf springs, and D-4D injector correction values"
        ),
        AlgerianCarModel(
            id = "toyota_yaris",
            brand = "Toyota",
            model = "Yaris",
            popularRank = 22,
            generations = listOf(
                CarGeneration("Yaris 3 (XP130 France / Sedan)", "2012 - 2019", 2015),
                CarGeneration("Yaris 2", "2006 - 2011", 2009)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.3 VVT-i 99ch", "1.0 VVT-i 69ch", "1.4 D-4D 90ch"),
            obdPortHintFr = "Sous le tableau de bord à gauche du volant près du levier de capot",
            obdPortHintAr = "تحت الطابلوه يسار المقود بالقرب من مقبض فتح غطاء المحرك",
            obdPortHintEn = "Under dashboard to the left of steering wheel near hood release",
            keyWeakPointFr = "Vérifier le bruit de la chaîne de distribution à froid et l'état de l'embrayage en conduite urbaine",
            keyWeakPointAr = "استمع لصوت سلسلة التوزيع (La chaîne) عند التشغيل البارد وافحص حالة الدبرياج",
            keyWeakPointEn = "Listen for timing chain rattle on cold start and check clutch wear from city driving"
        ),
        AlgerianCarModel(
            id = "kia_sportage",
            brand = "Kia",
            model = "Sportage",
            popularRank = 23,
            generations = listOf(
                CarGeneration("Sportage 4 (GT Line / Gloviz DZ)", "2016 - 2021", 2018),
                CarGeneration("Sportage 3", "2010 - 2015", 2014)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("AUTOMATIQUE", "MANUELLE"),
            commonEngines = listOf("2.0 CRDi 178ch / 185ch", "1.7 CRDi 115ch", "2.0 Essence"),
            obdPortHintFr = "Sous le volant à gauche derrière le couvercle de la boîte à fusibles",
            obdPortHintAr = "أسفل المقود يساراً خلف غطاء علبة الفيوزات",
            obdPortHintEn = "Under steering wheel on the left behind the fuse box lid",
            keyWeakPointFr = "Contrôler la boîte de transfert 4x4 (cannelures), le toit ouvrant panoramique et le volant moteur",
            keyWeakPointAr = "افحص علبة الدفع الرباعي 4x4، سلاسة السقف البانورامي وعدم وجود رجة في الفولان موتور",
            keyWeakPointEn = "Check AWD transfer case splines, panoramic roof mechanism, and flywheel vibration"
        ),
        AlgerianCarModel(
            id = "hyundai_tucson",
            brand = "Hyundai",
            model = "Tucson",
            popularRank = 24,
            generations = listOf(
                CarGeneration("Tucson 3 (TMC Tiaret / Tahkout)", "2015 - 2020", 2018),
                CarGeneration("ix35 (Tucson 2)", "2010 - 2015", 2013)
            ),
            availableFuels = listOf("DIESEL", "ESSENCE"),
            availableGearboxes = listOf("AUTOMATIQUE", "MANUELLE"),
            commonEngines = listOf("2.0 CRDi 177ch (4x4 / 4x2)", "1.7 CRDi 141ch DCT", "2.0 MPI"),
            obdPortHintFr = "Sous le volant à gauche dans le panneau à fusibles",
            obdPortHintAr = "أسفل المقود من الجهة اليسرى داخل لوحة الفيوزات",
            obdPortHintEn = "Under steering wheel on the left inside the fuse panel",
            keyWeakPointFr = "Sur boîte DCT7 : vérifier l'absence de broutage en 1ère/2ème et l'état du toit panoramique",
            keyWeakPointAr = "في العلبة الأوتوماتيكية DCT: تأكد من عدم وجود رجفة عند الانطلاق وسلامة السقف البانورامي",
            keyWeakPointEn = "On DCT gearbox: check for launch judder in 1st/2nd gear and panoramic sunroof operation"
        ),
        AlgerianCarModel(
            id = "citroen_celysee",
            brand = "Citroën",
            model = "C-Elysée",
            popularRank = 25,
            generations = listOf(
                CarGeneration("Phase 2 (Feel / Shine)", "2017 - 2020", 2018),
                CarGeneration("Phase 1", "2012 - 2016", 2014)
            ),
            availableFuels = listOf("ESSENCE", "GPL", "DIESEL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.6 HDi 92ch", "1.2 PureTech / VTi", "1.6 VTi 115ch"),
            obdPortHintFr = "Sous le volant à gauche derrière le cache fusibles (identique Peugeot 301)",
            obdPortHintAr = "أسفل المقود يساراً خلف غطاء الفيوزات (مطابق لبيجو 301)",
            obdPortHintEn = "Under steering wheel on the left behind fuse cover (same platform as Peugeot 301)",
            keyWeakPointFr = "Vérifier l'usure commerciale/taxi, les coupelles d'amortisseurs avant et la courroie sur 1.2 VTi",
            keyWeakPointAr = "تحقق من عدم استهلاكها المفرط كسيارة أجرة، كراسي المساعدات الأمامية، وحزام التوزيع في 1.2 VTi",
            keyWeakPointEn = "Check for commercial/taxi wear, front strut mounts, and wet timing belt on 1.2 VTi"
        ),
        AlgerianCarModel(
            id = "fiat_tipo",
            brand = "Fiat",
            model = "Tipo / 500 / Doblo",
            popularRank = 26,
            generations = listOf(
                CarGeneration("Fiat Algérie (2023 - 2025 Sedan / Hatchback / 500 Hybrid)", "2023 - 2025", 2023),
                CarGeneration("Tipo Phase 1 (2016 - 2020)", "2016 - 2020", 2018)
            ),
            availableFuels = listOf("ESSENCE", "DIESEL", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.4 Fire 95ch", "1.6 e-Torq 110ch BVA", "1.0 GSE Hybrid", "1.6 MultiJet 120ch"),
            obdPortHintFr = "Sous le tableau de bord à gauche du volant derrière la trappe",
            obdPortHintAr = "أسفل الطابلوه على يسار المقود خلف الغطاء",
            obdPortHintEn = "Under the dashboard to the left of the steering wheel behind the access flap",
            keyWeakPointFr = "Sur 1.4 95ch : contrôler le niveau d'huile moteur régulièrement et les biellettes de barre stabilisatrice",
            keyWeakPointAr = "في محرك 1.4 95ch: راقب مستوى زيت المحرك بانتظام وافحص أذرع التوازن الأمامية (Biellettes)",
            keyWeakPointEn = "On 1.4 95hp: monitor engine oil level closely and inspect front sway bar links"
        ),
        AlgerianCarModel(
            id = "chevrolet_sail",
            brand = "Chevrolet",
            model = "Sail / Aveo / Spark",
            popularRank = 27,
            generations = listOf(
                CarGeneration("Sail 2 / New Sail", "2011 - 2016", 2014),
                CarGeneration("Aveo / Spark / Optra", "2007 - 2015", 2012)
            ),
            availableFuels = listOf("ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE"),
            commonEngines = listOf("1.2 16v 86ch (Chaîne)", "1.4 16v 102ch", "1.5 8v"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur près de la colonne de direction",
            obdPortHintAr = "تحت الطابلوه جهة السائق بالقرب من عمود المقود",
            obdPortHintEn = "Under driver dashboard near the steering column",
            keyWeakPointFr = "Vérifier scrupuleusement le circuit de refroidissement (vase d'expansion, thermostat) et la corrosion",
            keyWeakPointAr = "افحص بدقة دورة التبريد (قارورة الماء والثرموستات) وتأكد من عدم وجود صدأ أو سخانة سابقة",
            keyWeakPointEn = "Carefully inspect cooling system (expansion tank, thermostat) for past overheating and rust"
        ),
        AlgerianCarModel(
            id = "suzuki_swift",
            brand = "Suzuki",
            model = "Swift / Alto / Celerio",
            popularRank = 28,
            generations = listOf(
                CarGeneration("Assemblage Saïda DZ (Swift / Alto 800 / Baleno)", "2018 - 2020", 2019),
                CarGeneration("Marutti / Alto / Celerio Import", "2008 - 2016", 2014)
            ),
            availableFuels = listOf("ESSENCE", "GPL"),
            availableGearboxes = listOf("MANUELLE", "AUTOMATIQUE"),
            commonEngines = listOf("1.2 DualJet 83ch / 90ch", "0.8L 3 cylindres (Alto 800)", "1.0 K10B"),
            obdPortHintFr = "Sous le tableau de bord côté conducteur au-dessus de la pédale de frein",
            obdPortHintAr = "تحت الطابلوه جهة السائق فوق دواسة الفرامل",
            obdPortHintEn = "Under driver dashboard above the brake pedal",
            keyWeakPointFr = "Vérifier l'état de l'embrayage, des supports moteur et l'absence de choc sur les longerons",
            keyWeakPointAr = "افحص حالة الدبرياج، كراسي المحرك، وسلامة الشاسيه الأمامي (اللونجرون) من الحوادث",
            keyWeakPointEn = "Check clutch condition, engine mounts, and front chassis rails for collision damage"
        )
    )

    /**
     * Phonetic and Arabic/French alias dictionary so buyers searching in Arabic or with common typos
     * (e.g., "اكصنت", "سامبول", "كليو", "بيجو", "داسيا", "غولف", "بيكانتو", "symbole", "peugot")
     * immediately find the right vehicle.
     */
    private val searchAliases: Map<String, List<String>> = mapOf(
        "renault" to listOf("رونو", "رينو", "reno", "renaut"),
        "symbol" to listOf("سامبول", "سيمبول", "سمبول", "symbole", "simbol"),
        "clio 4" to listOf("كليو", "كليو 4", "clio4", "clio", "بطة", "كومبيس"),
        "kangoo" to listOf("كانغو", "كونغو", "kango", "express"),
        "dacia" to listOf("داسيا", "داشيا", "dasia"),
        "sandero stepway" to listOf("ستيبواي", "سانديرو", "ستابواي", "stepway", "sandero"),
        "logan" to listOf("لوغان", "لوجان", "mcv"),
        "duster" to listOf("داستر", "دوستر", "4x4"),
        "peugeot" to listOf("بيجو", "بوجو", "peugot", "pijo"),
        "208" to listOf("208", "بيجو 208"),
        "301" to listOf("301", "بيجو 301"),
        "207" to listOf("207", "سيري 64", "206"),
        "308" to listOf("308", "فيلين"),
        "partner tepee" to listOf("بارتنر", "تيبي", "برلينغو", "partner", "berlingo"),
        "volkswagen" to listOf("فولكسفاغن", "فولسفاغن", "vw", "volswagen", "فولكس"),
        "golf 7" to listOf("غولف", "جولف", "golf", "gtd", "rline"),
        "polo" to listOf("بولو", "بولو 6", "بولو 5"),
        "seat" to listOf("سيات", "سياط", "siat"),
        "ibiza" to listOf("إبيزا", "ايبيزا", "أبيزا", "fr", "ارونا", "arona"),
        "leon" to listOf("ليون", "ليون 3", "خط ار"),
        "hyundai" to listOf("هيونداي", "هونداي", "hyndai", "hundai"),
        "accent rb" to listOf("أكسنت", "اكصنت", "اكسنت", "rb", "accent"),
        "grand i10" to listOf("اي 10", "جراند", "i10", "i20", "اي 20"),
        "tucson" to listOf("توسان", "توكسون", "ix35", "سانتافي"),
        "kia" to listOf("كيا", "kiya"),
        "picanto" to listOf("بيكانتو", "بيكونتو", "برازوكا", "ريو", "rio"),
        "sportage" to listOf("سبورتاج", "سورينتو"),
        "skoda" to listOf("سكودا", "شكودا", "scoda"),
        "octavia" to listOf("أوكتافيا", "اوكتافيا", "كليفر"),
        "fabia" to listOf("فابيا", "مونتي كارلو"),
        "toyota" to listOf("تويوتا", "طويوطا", "طويوتا"),
        "hilux" to listOf("هايلكس", "هيلوكس", "طويوطا نفعية"),
        "yaris" to listOf("ياريس", "كورولا", "corolla"),
        "citroën" to listOf("سيتروين", "ستروين", "citroen"),
        "c-elysée" to listOf("سي إليزي", "إليزي", "celysee", "elysee"),
        "fiat" to listOf("فيات", "فياط"),
        "tipo / 500 / doblo" to listOf("تيبو", "دوبلو", "فيات 500", "tipo", "doblo"),
        "chevrolet" to listOf("شيفروليه", "شفرولي", "chevrole"),
        "sail / aveo / spark" to listOf("سايل", "أفيو", "سبارك", "أوبترا", "sail", "aveo", "spark"),
        "suzuki" to listOf("سوزوكي", "سويفت", "ماروتي", "ألتو", "swift", "alto", "maruti")
    )

    fun matchesFuzzySearch(car: AlgerianCarModel, rawQuery: String): Boolean {
        val q = rawQuery.trim().lowercase()
        if (q.isEmpty()) return true

        if (car.brand.lowercase().contains(q) ||
            car.model.lowercase().contains(q) ||
            car.commonEngines.any { it.lowercase().contains(q) } ||
            car.generations.any { it.name.lowercase().contains(q) }
        ) {
            return true
        }

        val brandAliases = searchAliases[car.brand.lowercase()] ?: emptyList()
        val modelAliases = searchAliases[car.model.lowercase()] ?: emptyList()
        return (brandAliases + modelAliases).any { alias ->
            alias.lowercase().contains(q) || q.contains(alias.lowercase())
        }
    }
}
