package com.example.domain

import com.example.data.model.ItemSeverity

object ObdDtcDictionaryExtra {

    val extraDtcMap: Map<String, DtcInfo> = mapOf(
        "P0234" to DtcInfo(
            code = "P0234",
            severity = ItemSeverity.CRITICAL,
            titleFr = "Suralimentation excessive du turbo (Géométrie variable grippée)",
            titleAr = "زيادة مفرطة في ضغط التيربو (تكلس ريش التيربو أو الصمام)",
            titleEn = "Turbocharger Overboost Condition (Sticking VNT vanes)",
            plainExplanationFr = "Le turbo envoie trop de pression, ce qui met le moteur en mode sécurité (coupure d'accélération à 110 km/h). Souvent causé par la géométrie variable encrassée sur TDI/dCi/HDi.",
            plainExplanationAr = "التيربو يولد ضغطاً زائداً فيدخل المحرك في وضع الحماية (فقدان العزم فجأة في الطريق السريع). سببه غالباً تكلس الكربون داخل التيربو.",
            plainExplanationEn = "The turbocharger is producing excessive boost, forcing the ECU into limp mode on highways. Usually caused by soot-jammed variable geometry vanes.",
            minCostDzd = 20000,
            maxCostDzd = 95000
        ),
        "P0088" to DtcInfo(
            code = "P0088",
            severity = ItemSeverity.CRITICAL,
            titleFr = "Pression de la rampe d'injection trop élevée",
            titleAr = "ارتفاع مفرط في ضغط مسطرة الوقود (Régulateur de pression)",
            titleEn = "Fuel Rail / System Pressure Too High",
            plainExplanationFr = "Le régulateur de pression sur la pompe d'injection est bloqué ou défaillant, provoquant des claquements moteur et un risque d'arrêt brusque.",
            plainExplanationAr = "منظم ضغط الديزل على المضخة عالق أو تالف، مما يسبب صوت طقطقة قوي في المحرك واحتمال توقفه فجأة.",
            plainExplanationEn = "The fuel pressure regulator on the injection pump is sticking, causing harsh diesel knock and potential engine shutdown.",
            minCostDzd = 22000,
            maxCostDzd = 75000
        ),
        "P0201" to DtcInfo(
            code = "P0201",
            severity = ItemSeverity.CRITICAL,
            titleFr = "Défaut du circuit de l'injecteur n°1",
            titleAr = "عطل في الحاقن (Injecteur) رقم 1 أو فيشته الكهربائية",
            titleEn = "Injector Circuit / Open - Cylinder 1",
            plainExplanationFr = "L'injecteur du cylindre 1 est défectueux ou sa prise électrique fait mauvais contact. Le moteur tourne sur 3 cylindres avec de fortes vibrations.",
            plainExplanationAr = "حاقن الوقود رقم 1 تالف أو هناك قطع في سلكه الكهربائي. يعمل المحرك على 3 سيلندرات مع اهتزاز قوي.",
            plainExplanationEn = "Fuel injector #1 or its wiring connector has failed, causing the engine to run rough on 3 cylinders.",
            minCostDzd = 25000,
            maxCostDzd = 70000
        ),
        "P0202" to DtcInfo(
            code = "P0202",
            severity = ItemSeverity.CRITICAL,
            titleFr = "Défaut du circuit de l'injecteur n°2",
            titleAr = "عطل في الحاقن (Injecteur) رقم 2",
            titleEn = "Injector Circuit / Open - Cylinder 2",
            plainExplanationFr = "L'injecteur du cylindre 2 présente une anomalie électrique ou mécanique (très fréquent sur 1.5 dCi et 1.6 TDI/HDi).",
            plainExplanationAr = "حاقن الوقود رقم 2 يعاني من خلل كهربائي أو ميكانيكي (شائع في محركات 1.5 dCi و 1.6 HDi/TDI).",
            plainExplanationEn = "Fuel injector #2 has an electrical or mechanical fault, common on high-mileage diesel engines.",
            minCostDzd = 25000,
            maxCostDzd = 70000
        ),
        "P0203" to DtcInfo(
            code = "P0203",
            severity = ItemSeverity.CRITICAL,
            titleFr = "Défaut du circuit de l'injecteur n°3",
            titleAr = "عطل في الحاقن (Injecteur) رقم 3",
            titleEn = "Injector Circuit / Open - Cylinder 3",
            plainExplanationFr = "Anomalie sur l'injecteur n°3 entraînant des à-coups, de la fumée et une surconsommation.",
            plainExplanationAr = "خلل في الحاقن رقم 3 يؤدي إلى تقطيع في المشي، دخان واستهلاك زائد للوقود.",
            plainExplanationEn = "Fault on cylinder #3 injector causing hesitation, smoke, and rough running.",
            minCostDzd = 25000,
            maxCostDzd = 70000
        ),
        "P0204" to DtcInfo(
            code = "P0204",
            severity = ItemSeverity.CRITICAL,
            titleFr = "Défaut du circuit de l'injecteur n°4",
            titleAr = "عطل في الحاقن (Injecteur) رقم 4",
            titleEn = "Injector Circuit / Open - Cylinder 4",
            plainExplanationFr = "Anomalie sur l'injecteur n°4. Sur diesel, un injecteur qui fuit peut endommager le piston s'il est négligé.",
            plainExplanationAr = "خلل في الحاقن رقم 4. في محركات الديزل، إهمال الحاقن التالف قد يضر بالمكبس (Piston).",
            plainExplanationEn = "Fault on cylinder #4 injector. On diesel engines, a leaking injector can damage the piston if ignored.",
            minCostDzd = 25000,
            maxCostDzd = 70000
        ),
        "P0302" to DtcInfo(
            code = "P0302",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Ratés d'allumage sur le cylindre n°2",
            titleAr = "خلل احتراق (تقطيع) في السيلندر رقم 2",
            titleEn = "Cylinder 2 Misfire Detected",
            plainExplanationFr = "Bougie d'allumage, bobine crayon ou injecteur GPL/Essence défaillant sur le cylindre 2.",
            plainExplanationAr = "شمعة الاحتراق (البوجي)، البوبينة أو حاقن الغاز/البنزين ضعيف في الغرفة رقم 2.",
            plainExplanationEn = "Faulty spark plug, ignition coil, or injector on cylinder 2.",
            minCostDzd = 8000,
            maxCostDzd = 42000
        ),
        "P0303" to DtcInfo(
            code = "P0303",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Ratés d'allumage sur le cylindre n°3",
            titleAr = "خلل احتراق (تقطيع) في السيلندر رقم 3",
            titleEn = "Cylinder 3 Misfire Detected",
            plainExplanationFr = "Bougie, bobine d'allumage ou soupape bridée (sur moteur GPL) au cylindre 3.",
            plainExplanationAr = "خلل في البوجي، البوبينة أو ضيق في الصبابات (في سيارات الغاز GPL) بالسيلندر رقم 3.",
            plainExplanationEn = "Spark plug, ignition coil, or tight valve clearance (on LPG cars) on cylinder 3.",
            minCostDzd = 8000,
            maxCostDzd = 42000
        ),
        "P0304" to DtcInfo(
            code = "P0304",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Ratés d'allumage sur le cylindre n°4",
            titleAr = "خلل احتراق (تقطيع) في السيلندر رقم 4",
            titleEn = "Cylinder 4 Misfire Detected",
            plainExplanationFr = "Bougie, bobine d'allumage ou injecteur défaillant sur le cylindre 4.",
            plainExplanationAr = "خلل في البوجي أو البوبينة أو الحاقن في السيلندر رقم 4.",
            plainExplanationEn = "Misfire on cylinder 4 due to ignition coil, spark plug, or fuel delivery.",
            minCostDzd = 8000,
            maxCostDzd = 42000
        ),
        "P0172" to DtcInfo(
            code = "P0172",
            severity = ItemSeverity.MINOR,
            titleFr = "Mélange carburant trop riche (Consommation élevée / Réglage GPL)",
            titleAr = "خليط الوقود غني جداً (استهلاك زائد أو عيار غاز GPL)",
            titleEn = "System Too Rich (Bank 1 - High fuel trim or LPG map)",
            plainExplanationFr = "Le moteur reçoit trop de carburant ou pas assez d'air (filtre à air bouché, sonde lambda fatiguée ou vapodétendeur GPL mal calibré).",
            plainExplanationAr = "المحرك يتلقى وقوداً زائداً أو هواءً ناقصاً (فلتر هواء متسخ، حساس الشكمان Lambda أو حاجة لضبط الغاز GPL).",
            plainExplanationEn = "The engine is receiving too much fuel or insufficient air (clogged air filter, aging O2 sensor, or rich LPG calibration).",
            minCostDzd = 6000,
            maxCostDzd = 28000
        ),
        "P0100" to DtcInfo(
            code = "P0100",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Défaut du débitmètre d'air massique (MAF)",
            titleAr = "عطل في حساس الهواء (Débitmètre d'air)",
            titleEn = "Mass or Volume Air Flow (MAF) Circuit Malfunction",
            plainExplanationFr = "Le capteur qui mesure l'air entrant (débitmètre) est encrassé ou HS, causant une perte de tirage et une hausse de consommation.",
            plainExplanationAr = "حساس قياس الهواء (الديبيمتر) متسخ أو تالف، مما يضعف عزم السيارة ويزيد استهلاك الوقود.",
            plainExplanationEn = "The Mass Air Flow sensor is dirty or faulty, causing sluggish acceleration and higher fuel consumption.",
            minCostDzd = 10000,
            maxCostDzd = 35000
        ),
        "P0106" to DtcInfo(
            code = "P0106",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Capteur de pression d'admission (MAP) hors plage",
            titleAr = "خلل في حساس ضغط الثلاجة (Capteur MAP)",
            titleEn = "Manifold Absolute Pressure (MAP) Sensor Range/Performance",
            plainExplanationFr = "Capteur de pression du collecteur encrassé ou prise d'air sur l'admission (fréquent sur 1.2 16v et 1.6 MPI).",
            plainExplanationAr = "حساس ضغط الهواء متسخ بالزيت أو يوجد تسرب هواء في ثلاجة المحرك (شائع في 1.2 و 1.6 MPI).",
            plainExplanationEn = "Intake manifold pressure sensor fouled with oil vapor or vacuum leak on intake manifold.",
            minCostDzd = 7000,
            maxCostDzd = 24000
        ),
        "P0128" to DtcInfo(
            code = "P0128",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Température moteur trop basse (Thermostat bloqué ouvert ou supprimé)",
            titleAr = "حرارة المحرك أقل من الطبيعي (الثرموستات مفتوح دائماً أو منزوع!)",
            titleEn = "Coolant Thermostat (Coolant Temp Below Regulating Temperature)",
            plainExplanationFr = "Attention : en Algérie, certains vendeurs retirent le thermostat (calorstat) pour masquer un problème de surchauffe moteur ! Vérifiez pourquoi le moteur reste froid.",
            plainExplanationAr = "انتبه جيداً: في الجزائر، يقوم بعض البائعين بنزع الثرموستات (Calorstat) عمداً لإخفاء مشكلة سخانة في المحرك! افحص دورة التبريد بدقة.",
            plainExplanationEn = "Warning: in Algeria, sellers sometimes remove the thermostat completely to hide a head-gasket or overheating problem! Inspect cooling carefully.",
            minCostDzd = 8000,
            maxCostDzd = 65000
        ),
        "P0380" to DtcInfo(
            code = "P0380",
            severity = ItemSeverity.MINOR,
            titleFr = "Bougies ou boîtier de préchauffage Diesel défectueux",
            titleAr = "تلف شمعات التسخين (Bougies de préchauffage) في محرك الديزل",
            titleEn = "Glow Plug / Heater Circuit 'A' Malfunction",
            plainExplanationFr = "Une ou plusieurs bougies de préchauffage sont grillées. Le démarrage à froid le matin en hiver sera difficile avec un peu de fumée blanche.",
            plainExplanationAr = "واحدة أو أكثر من شمعات التسخين تالفة، مما يصعّب تشغيل محرك الديزل في الصباح البارد.",
            plainExplanationEn = "One or more diesel glow plugs have burned out, making cold morning starts harder.",
            minCostDzd = 6000,
            maxCostDzd = 22000
        ),
        "P0016" to DtcInfo(
            code = "P0016",
            severity = ItemSeverity.CRITICAL,
            titleFr = "Décalage entre vilebrequin et arbre à cames (Chaîne/Courroie détendue)",
            titleAr = "عدم تطابق توقيت المحرك (ارتخاء سلسلة أو حزام التوزيع La chaîne)",
            titleEn = "Crankshaft - Camshaft Position Correlation (Stretched Timing Chain)",
            plainExplanationFr = "Alerte grave : la chaîne ou la courroie de distribution est détendue ou décalée d'une dent. Risque de casse moteur si l'on continue à rouler sans remplacer le kit distribution.",
            plainExplanationAr = "تنبيه خطير: سلسلة التوزيع (La chaîne) مرتخية أو متحركة عن مكانها. يجب استبدال طقم التوزيع فوراً لتفادي انكسار المحرك.",
            plainExplanationEn = "Critical alert: the timing chain or belt has stretched or jumped a tooth. Immediate timing kit replacement is required to prevent engine damage.",
            minCostDzd = 35000,
            maxCostDzd = 110000
        ),
        "P0335" to DtcInfo(
            code = "P0335",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Capteur de position du vilebrequin (Capteur PMH) défaillant",
            titleAr = "خلل في حساس الكرنك (Capteur PMH) يسبب صعوبة التشغيل",
            titleEn = "Crankshaft Position Sensor 'A' Circuit Malfunction",
            plainExplanationFr = "Très connu sur Renault/Dacia : le capteur PMH sale ou HS empêche la voiture de démarrer par intermittence à chaud.",
            plainExplanationAr = "معروف جداً في سيارات رونو وداسيا: حساس PMH المتسخ أو التالف يمنع تشغيل السيارة أحياناً وهي ساخنة.",
            plainExplanationEn = "Very common on Renault/Dacia: a failing crank sensor (PMH) causes intermittent no-start conditions when warm.",
            minCostDzd = 6000,
            maxCostDzd = 18000
        ),
        "P0130" to DtcInfo(
            code = "P0130",
            severity = ItemSeverity.MINOR,
            titleFr = "Anomalie de la sonde Lambda amont (Capteur d'oxygène)",
            titleAr = "خلل في حساس الشكمان الأمامي (Sonde Lambda)",
            titleEn = "O2 Sensor Circuit Malfunction (Bank 1 Sensor 1)",
            plainExplanationFr = "La sonde à oxygène sur l'échappement est vieillissante, ce qui augmente légèrement la consommation d'essence.",
            plainExplanationAr = "حساس الأكسجين في العادم ضعيف، مما يزيد استهلاك البنزين قليلاً.",
            plainExplanationEn = "The upstream oxygen sensor is aging, slightly increasing petrol consumption.",
            minCostDzd = 12000,
            maxCostDzd = 32000
        ),
        "P2101" to DtcInfo(
            code = "P2101",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Défaut du boîtier papillon motorisé (Encrassement / Commande)",
            titleAr = "خلل أو اتساخ في بوابة الهواء (Boîtier papillon)",
            titleEn = "Throttle Actuator Control Motor Circuit Range/Performance",
            plainExplanationFr = "Le papillon des gaz est encrassé ou son moteur électrique grippe, provoquant un ralenti instable et un voyant EPC/Moteur.",
            plainExplanationAr = "بوابة الهواء متسخة بالكربون أو تعاني من خلل كهربائي، مما يسبب تذبذب دوران المحرك وإضاءة لمبة EPC.",
            plainExplanationEn = "The electronic throttle body is gummed up with carbon or sticking, causing unstable idle and EPC warning lights.",
            minCostDzd = 8000,
            maxCostDzd = 45000
        ),
        "P0700" to DtcInfo(
            code = "P0700",
            severity = ItemSeverity.CRITICAL,
            titleFr = "Anomalie du calculateur de boîte de vitesses automatique (DSG / EDC / BVA)",
            titleAr = "عطل مسجل في كمبيوتر علبة السرعة الأوتوماتيكية (DSG / EDC / BVA)",
            titleEn = "Transmission Control System (MIL Request - Auto Gearbox Fault)",
            plainExplanationFr = "La boîte automatique a enregistré un défaut interne (embrayages, mécatronique ou électrovannes). Exigez un diagnostic spécialisé boîte auto avant d'acheter.",
            plainExplanationAr = "علبة السرعة الأوتوماتيكية سجلت عطلاً داخلياً (في الدبرياج أو الميكاترونيك). يجب فحص العلبة عند مختص قبل الشراء.",
            plainExplanationEn = "The automatic transmission has logged an internal fault (clutches, mechatronic unit, or solenoids). Have a transmission specialist inspect it before buying.",
            minCostDzd = 60000,
            maxCostDzd = 240000
        ),
        "P17BF" to DtcInfo(
            code = "P17BF",
            severity = ItemSeverity.CRITICAL,
            titleFr = "[Boîte DSG7] Protection contre le jeu de la pompe hydraulique Mécatronique",
            titleAr = "[علبة DSG7] ضعف ضغط مضخة الميكاترونيك (Mécatronique DSG)",
            titleEn = "[DSG7 Gearbox] Hydraulic Pump Play Protection (Mechatronic Accumulator)",
            plainExplanationFr = "Défaut classique des boîtes DSG7 (Golf 7, Ibiza, Polo, Octavia, Leon) : l'accumulateur de pression du bloc mécatronique fuit. Réparation indispensable.",
            plainExplanationAr = "عطل شهير في علب DSG7 (غولف، إبيزا، بولو، ليون، أوكتافيا): تسرب ضغط الزيت داخل وحدة الميكاترونيك ويحتاج إصلاحاً مختصاً.",
            plainExplanationEn = "Classic DSG7 (DQ200) fault on VW/Seat/Skoda: the hydraulic pressure accumulator inside the mechatronic unit is losing pressure.",
            minCostDzd = 75000,
            maxCostDzd = 190000
        ),
        "P0340" to DtcInfo(
            code = "P0340",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Capteur de position d'arbre à cames défaillant",
            titleAr = "خلل في حساس الكامات العلوي (Capteur d'arbre à cames)",
            titleEn = "Camshaft Position Sensor 'A' Circuit Malfunction",
            plainExplanationFr = "Le capteur d'arbre à cames ne synchronise plus l'injection au démarrage, causant un démarrage long ou un calage brutal.",
            plainExplanationAr = "حساس الكامات العلوي لا يرسل إشارة التوقيت، مما يسبب تأخر تشغيل المحرك أو انطفاءه فجأة.",
            plainExplanationEn = "The camshaft position sensor is failing, causing extended cranking on start or sudden stalling.",
            minCostDzd = 7000,
            maxCostDzd = 22000
        ),
        "P0500" to DtcInfo(
            code = "P0500",
            severity = ItemSeverity.IMPORTANT,
            titleFr = "Défaut du capteur de vitesse du véhicule (VSS / ABS)",
            titleAr = "عطل في حساس سرعة السيارة (قد يوقف عداد الكيلومترات!)",
            titleEn = "Vehicle Speed Sensor 'A' Malfunction (Odometer / ABS Signal)",
            plainExplanationFr = "Attention : si le capteur de vitesse est débranché ou HS, le compteur kilométrique ne tourne plus ! Vérifiez que le kilométrage avance pendant l'essai routier.",
            plainExplanationAr = "انتبه: إذا كان حساس السرعة مفصولاً أو معطلاً، فإن عداد الكيلومترات يتوقف عن العد! تأكد أن العداد يحسب المسافة أثناء القيادة التجريبية.",
            plainExplanationEn = "Warning: if the speed sensor is unplugged or faulty, the odometer stops recording kilometers! Verify the odometer increments during the test drive.",
            minCostDzd = 8000,
            maxCostDzd = 28000
        )
    )
}
