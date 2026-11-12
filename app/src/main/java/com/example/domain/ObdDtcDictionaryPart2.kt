package com.example.domain

import com.example.data.model.ItemSeverity
import com.example.data.model.VerificationStatus

/**
 * Phase 4 Expansion (Part 2): Common OBD-II Fault Codes (P0001 - P0198)
 * with titles and plain explanations in French, Arabic, and English.
 * All entries default to [VerificationStatus.TO_BE_VERIFIED] until signed off by a mechanic.
 */
object ObdDtcDictionaryPart2 {

    private fun dtc(
        code: String,
        severity: ItemSeverity,
        titleFr: String,
        titleAr: String,
        titleEn: String,
        expFr: String,
        expAr: String,
        expEn: String,
        minCost: Int = 0,
        maxCost: Int = 0
    ): Pair<String, DtcInfo> = code to DtcInfo(
        code = code,
        severity = severity,
        titleFr = titleFr,
        titleAr = titleAr,
        titleEn = titleEn,
        plainExplanationFr = expFr,
        plainExplanationAr = expAr,
        plainExplanationEn = expEn,
        minCostDzd = minCost,
        maxCostDzd = maxCost,
        verificationStatus = VerificationStatus.TO_BE_VERIFIED
    )

    val dtcMap: Map<String, DtcInfo> = mapOf(
        dtc(
            "P0001", ItemSeverity.CRITICAL,
            "Commande du régulateur de volume de carburant : circuit ouvert",
            "انقطاع في دارة منظم كمية الوقود على المضخة",
            "Fuel Volume Regulator Control Circuit / Open",
            "L'électrovanne de dosage de carburant sur la pompe haute pression ne répond plus. Peut provoquer un refus de démarrer ou un calage brutal.",
            "الصمام الكهربائي لتنظيم الوقود على مضخة الضغط العالي لا يستجيب، مما قد يمنع التشغيل أو يوقف المحرك فجأة.",
            "The fuel metering solenoid on the high-pressure pump is open-circuit, which can cause a no-start or sudden stall."
        ),
        dtc(
            "P0002", ItemSeverity.CRITICAL,
            "Régulateur de volume de carburant : performance ou plage de mesure",
            "خلل في أداء منظم كمية الوقود (تذبذب الضغط)",
            "Fuel Volume Regulator Control Circuit Range/Performance",
            "La pression de carburant fluctue anormalement à cause du régulateur de débit grippé ou d'un filtre à gasoil colmaté.",
            "يتذبذب ضغط الوقود بشكل غير طبيعي بسبب تكلس منظم التدفق أو انسداد فلتر الديزل.",
            "Fuel rail pressure fluctuates abnormally due to a sticking metering valve or clogged diesel filter."
        ),
        dtc(
            "P0003", ItemSeverity.IMPORTANT,
            "Commande du régulateur de volume de carburant : tension trop basse",
            "انخفاض الجهد الكهربائي لمنظم كمية الوقود",
            "Fuel Volume Regulator Control Circuit Low",
            "Court-circuit à la masse ou prise endommagée sur le régulateur de la pompe d'injection.",
            "تماس كهربائي أو تلف في الفيشة الخاصة بمنظم مضخة الحقن.",
            "Short to ground or damaged connector on the injection pump fuel volume regulator."
        ),
        dtc(
            "P0004", ItemSeverity.IMPORTANT,
            "Commande du régulateur de volume de carburant : tension trop haute",
            "ارتفاع الجهد الكهربائي لمنظم كمية الوقود",
            "Fuel Volume Regulator Control Circuit High",
            "Problème de faisceau électrique ou électrovanne de pompe en court-circuit.",
            "مشكلة في الضفيرة الكهربائية أو تماس داخل صمام المضخة.",
            "Wiring harness fault or shorted fuel metering solenoid on the pump."
        ),
        dtc(
            "P0010", ItemSeverity.IMPORTANT,
            "Actionneur de déphasage d'arbre à cames d'admission (Banque 1)",
            "عطل في صمام توقيت الصمامات المتغير (VVT / Vanos / Déphaseur)",
            "Intake Camshaft Position Actuator Circuit / Open (Bank 1)",
            "L'électrovanne de déphasage d'arbre à cames est encrassée par une huile trop vieille ou débranchée.",
            "الصمام الكهرومغناطيسي لتوقيت الكامات متسخ بسبب زيت قديم أو مفصول كهربائياً.",
            "The variable valve timing (VVT) solenoid is sludged from old oil or electrically open."
        ),
        dtc(
            "P0011", ItemSeverity.IMPORTANT,
            "Position d'arbre à cames d'admission trop avancée (Déphaseur / VVT)",
            "تقدم مفرط في توقيت عمود الكامات (خلل في البولي Déphaseur أو الزيت)",
            "Intake Camshaft Position Timing - Over-Advanced (Bank 1)",
            "La poulie déphaseur ou l'électrovanne VVT grippe (souvent à cause de vidanges trop espacées ou d'un niveau d'huile bas).",
            "بكرة الكامات المتغيرة (Déphaseur) أو صمام VVT عالق، غالباً بسبب تأخر تغيير الزيت أو نقص مستواه.",
            "The VVT phaser pulley or solenoid is sticking, often due to overdue oil changes or low engine oil level."
        ),
        dtc(
            "P0012", ItemSeverity.IMPORTANT,
            "Position d'arbre à cames d'admission trop retardée (Banque 1)",
            "تأخر مفرط في توقيت عمود الكامات (انخفاض ضغط الزيت أو VVT)",
            "Intake Camshaft Position Timing - Over-Retarded (Bank 1)",
            "Le déphaseur d'arbre à cames ne reçoit pas assez de pression d'huile ou la chaîne commence à s'allonger.",
            "منظم الكامات لا يصله ضغط زيت كافٍ أو أن سلسلة التوزيع بدأت في الارتخاء.",
            "The camshaft phaser is not receiving sufficient oil pressure or the timing chain is starting to stretch."
        ),
        dtc(
            "P0013", ItemSeverity.IMPORTANT,
            "Actionneur de déphasage d'arbre à cames d'échappement (Banque 1)",
            "عطل في صمام توقيت كامات العادم (VVT Échappement)",
            "Exhaust Camshaft Position Actuator Circuit / Open (Bank 1)",
            "Défaut électrique ou encrassement de l'électrovanne VVT côté échappement.",
            "خلل كهربائي أو انسداد في صمام VVT لجهة العادم.",
            "Electrical fault or oil sludge in the exhaust-side VVT actuator solenoid."
        ),
        dtc(
            "P0014", ItemSeverity.IMPORTANT,
            "Arbre à cames d'échappement : calage trop avancé (Banque 1)",
            "عدم انتظام توقيت كامات العادم (تقدم زائد)",
            "Exhaust Camshaft Position Timing - Over-Advanced (Bank 1)",
            "Calage variable d'échappement bloqué ou chaîne de distribution détendue.",
            "تعلق نظام التوقيت المتغير للعادم أو ارتخاء في سلسلة التوزيع.",
            "Exhaust variable timing stuck advanced or timing chain slack."
        ),
        dtc(
            "P0017", ItemSeverity.CRITICAL,
            "Corrélation vilebrequin / arbre à cames d'échappement (Distribution décalée)",
            "عدم تطابق الكرنك مع كامات العادم (تزحزح التوزيع)",
            "Crankshaft Position - Camshaft Position Correlation (Bank 1 Sensor B)",
            "La distribution (chaîne ou courroie) est décalée ou détendue côté échappement. Risque mécanique sérieux.",
            "سلسلة أو حزام التوزيع غير متطابق مع عمود كامات العادم. خطر ميكانيكي على المحرك.",
            "The timing chain or belt is out of phase on the exhaust camshaft. Serious mechanical risk."
        ),
        dtc(
            "P0030", ItemSeverity.MINOR,
            "Circuit de chauffage de la sonde Lambda amont (Banque 1 Capteur 1)",
            "عطل في مسخن حساس الشكمان الأمامي (Sonde Lambda 1)",
            "HO2S Heater Control Circuit (Bank 1 Sensor 1)",
            "La résistance chauffante interne de la première sonde à oxygène est coupée. Allume le voyant moteur et augmente un peu la consommation à froid.",
            "احتراق مسخن حساس الأكسجين الأول، مما يضيء لمبة المحرك ويزيد الاستهلاك قليلاً عند البرودة.",
            "The internal heater element of the upstream O2 sensor has failed, turning on the check engine light."
        ),
        dtc(
            "P0036", ItemSeverity.MINOR,
            "Circuit de chauffage de la sonde Lambda aval (Banque 1 Capteur 2)",
            "عطل في مسخن حساس الشكمان الخلفي بعد الكاتاليزور",
            "HO2S Heater Control Circuit (Bank 1 Sensor 2)",
            "Le chauffage de la sonde située après le catalyseur est HS (souvent après suppression du catalyseur ou fil touchant l'échappement).",
            "تعطل مسخن الحساس الخلفي بعد علبة البيئة (غالباً بعد إلغاء الكاتاليزور أو احتراق السلك).",
            "Downstream oxygen sensor heater circuit fault, often after catalyst work or damaged underbody wiring."
        ),
        dtc(
            "P0089", ItemSeverity.CRITICAL,
            "Performance du régulateur de pression de carburant 1",
            "خلل في أداء منظم ضغط الديزل/الوقود",
            "Fuel Pressure Regulator 1 Performance",
            "Le régulateur de pression n'arrive pas à stabiliser la pression dans la rampe commune (ralenti instable, calage en accélération).",
            "منظم الضغط عاجز عن تثبيت ضغط الوقود في المسطرة (يسبب رجفة في السلانسيه أو انطفاء عند الدعس).",
            "The fuel pressure regulator cannot hold steady rail pressure, causing rough idle or stalling under load."
        ),
        dtc(
            "P0090", ItemSeverity.CRITICAL,
            "Électrovanne de dosage / régulateur de pression carburant : circuit ouvert",
            "انقطاع كهربائي في صمام تنظيم ضغط الوقود",
            "Fuel Pressure Regulator 1 Control Circuit / Open",
            "Coupure électrique sur l'électrovanne de régulation de pression de carburant.",
            "فصل أو قطع في سلك صمام تنظيم ضغط الوقود.",
            "Open electrical circuit on the fuel pressure regulator solenoid."
        ),
        dtc(
            "P0093", ItemSeverity.CRITICAL,
            "Fuite importante détectée dans le système d'alimentation carburant",
            "اكتشاف تسرب كبير أو هبوط حاد في ضغط الوقود",
            "Fuel System Leak Detected - Large Leak",
            "Chute brutale de pression dans la rampe d'injection : fuite externe, injecteur bloqué ouvert ou pompe HP fatiguée.",
            "هبوط مفاجئ في ضغط مسطرة الحقن بسبب تسرب خارجي، أو حاقن مفتوح، أو ضعف المضخة.",
            "Sudden drop in high-pressure fuel rail pressure due to an injector return leak, fuel line leak, or weak pump."
        ),
        dtc(
            "P0101", ItemSeverity.IMPORTANT,
            "Débitmètre d'air massique (MAF) : valeurs incohérentes",
            "قراءة غير منطقية من حساس الهواء (Débitmètre)",
            "Mass or Volume Air Flow (MAF) Circuit Range/Performance",
            "Le débitmètre envoie des mesures fausses (grille sale, prise d'air après le débitmètre ou vanne EGR bloquée).",
            "حساس الهواء يرسل قراءات خاطئة بسبب اتساخه أو وجود تسرب هواء في أنبوب التيربو.",
            "The MAF sensor is reporting implausible airflow due to dirt on the sensor element or an intake air leak."
        ),
        dtc(
            "P0102", ItemSeverity.IMPORTANT,
            "Débitmètre d'air (MAF) : signal trop bas",
            "إشارة ضعيفة جداً من حساس الهواء (أو فيشة مفصولة)",
            "Mass or Volume Air Flow (MAF) Circuit Low Input",
            "Le débitmètre est souvent débranché (parfois volontairement par un vendeur pour masquer un débitmètre HS !) ou son fil est coupé.",
            "غالباً ما تكون فيشة حساس الهواء مفصولة (أحياناً يفصلها البائع عمداً لإخفاء عطل الحساس!) أو السلك مقطوع.",
            "The MAF sensor is often unplugged (sometimes intentionally to hide a dead sensor) or has a broken wire."
        ),
        dtc(
            "P0103", ItemSeverity.IMPORTANT,
            "Débitmètre d'air (MAF) : signal trop haut",
            "إشارة مرتفعة بشكل غير طبيعي من حساس الهواء",
            "Mass or Volume Air Flow (MAF) Circuit High Input",
            "Court-circuit ou débitmètre d'air défectueux.",
            "تماس كهربائي أو تلف في حساس الهواء.",
            "Short circuit or faulty Mass Air Flow sensor."
        ),
        dtc(
            "P0105", ItemSeverity.IMPORTANT,
            "Capteur de pression absolue du collecteur (MAP) : panne du circuit",
            "عطل في دارة حساس ضغط الهواء (MAP)",
            "Manifold Absolute Pressure (MAP) / Barometric Pressure Circuit",
            "Le capteur de pression d'admission ne répond plus, causant des pertes de puissance.",
            "توقف حساس ضغط ثلاجة المحرك عن الاستجابة مما يسبب ضعف العزم.",
            "The intake manifold pressure sensor circuit has failed, reducing engine power."
        ),
        dtc(
            "P0107", ItemSeverity.IMPORTANT,
            "Capteur de pression d'admission (MAP) : tension trop basse",
            "إشارة منخفضة من حساس ضغط الهواء (MAP)",
            "Manifold Absolute Pressure (MAP) Circuit Low Input",
            "Capteur MAP débranché, fil coupé ou court-circuit à la masse.",
            "حساس ضغط الهواء مفصول أو سلكه مقطوع.",
            "MAP sensor unplugged, broken wire, or short to ground."
        ),
        dtc(
            "P0108", ItemSeverity.IMPORTANT,
            "Capteur de pression d'admission (MAP) : tension trop haute",
            "إشارة مرتفعة من حساس ضغط الهواء أو ضغط التيربو",
            "Manifold Absolute Pressure (MAP) Circuit High Input",
            "Pression d'admission excessive ou capteur MAP en court-circuit.",
            "ضغط هواء مرتفع جداً أو خلل كهربائي في حساس الضغط.",
            "Excessive manifold pressure reading or shorted MAP sensor."
        ),
        dtc(
            "P0110", ItemSeverity.MINOR,
            "Capteur de température d'air d'admission (IAT) : panne du circuit",
            "عطل في حساس حرارة الهواء الداخل للمحرك (IAT)",
            "Intake Air Temperature (IAT) Sensor 1 Circuit",
            "La sonde de température d'air (souvent intégrée au débitmètre ou au capteur MAP) est en défaut.",
            "خلل في حساس قياس حرارة الهواء (غالباً مدمج مع الديبيمتر).",
            "The intake air temperature sensor (often integrated into the MAF or MAP sensor) has a circuit fault."
        ),
        dtc(
            "P0113", ItemSeverity.MINOR,
            "Capteur de température d'air d'admission (IAT) : signal trop haut (Circuit ouvert)",
            "فيشة حساس حرارة الهواء مفصولة أو السلك مقطوع",
            "Intake Air Temperature (IAT) Sensor 1 Circuit High",
            "Indique que la sonde d'air est débranchée (le calculateur lit -40 °C par défaut).",
            "يدل غالباً على أن فيشة حساس الهواء مفصولة (الكمبيوتر يقرأ -40 درجة).",
            "Typically indicates the intake air temperature sensor is unplugged (ECU reads -40°C)."
        ),
        dtc(
            "P0115", ItemSeverity.IMPORTANT,
            "Capteur de température du liquide de refroidissement (ECT) : panne",
            "عطل في حساس حرارة ماء التبريد (Mano de température)",
            "Engine Coolant Temperature (ECT) Sensor 1 Circuit",
            "La sonde de température d'eau est défaillante : le ventilateur peut tourner en permanence et le démarrage à froid est perturbé.",
            "حساس حرارة الماء تالف: قد تعمل مروحة التبريد باستمرار ويتأثر التشغيل الصباحي.",
            "The coolant temperature sensor is faulty, often causing the radiator fan to run constantly."
        ),
        dtc(
            "P0117", ItemSeverity.IMPORTANT,
            "Capteur de température d'eau (ECT) : signal trop bas (Surchauffe ou court-circuit)",
            "إشارة منخفضة من حساس حرارة الماء (تماس أو حرارة مرتفعة جداً)",
            "Engine Coolant Temperature (ECT) Sensor 1 Circuit Low",
            "Court-circuit de la sonde de température d'eau ou moteur ayant subi une forte surchauffe.",
            "تماس في حساس الحرارة أو أن المحرك تعرض لسخونة مفرطة.",
            "Shorted coolant temperature sensor or extreme overheating condition."
        ),
        dtc(
            "P0118", ItemSeverity.IMPORTANT,
            "Capteur de température d'eau (ECT) : signal trop haut (Sonde débranchée)",
            "حساس حرارة الماء مفصول أو سلكه مقطوع",
            "Engine Coolant Temperature (ECT) Sensor 1 Circuit High",
            "La sonde de température d'eau est débranchée ou coupée (parfois débranchée pour forcer le ventilateur à tourner tout le temps !).",
            "فيشة حساس الحرارة مفصولة (يفصلها البعض عمداً لجعل المروحة تشتغل دائماً لإخفاء السخانة!).",
            "Coolant sensor is unplugged (sometimes unplugged intentionally to force the cooling fan to run nonstop)."
        ),
        dtc(
            "P0120", ItemSeverity.IMPORTANT,
            "Capteur de position de pédale / papillon des gaz 'A' : panne",
            "عطل في حساس دعسة الوقود أو بوابة الهواء (Papillon)",
            "Throttle / Pedal Position Sensor / Switch 'A' Circuit",
            "Le calculateur ne lit plus correctement l'ouverture du papillon ou de la pédale d'accélérateur (mode dégradé, accélération bloquée).",
            "كمبيوتر السيارة لا يقرأ زاوية بوابة الهواء أو دعسة البنزين بشكل صحيح مما يضعف الاستجابة.",
            "The ECU cannot reliably read the throttle body or accelerator pedal position."
        ),
        dtc(
            "P0121", ItemSeverity.IMPORTANT,
            "Capteur de position du papillon 'A' : plage ou performance",
            "عدم انتظام إشارة بوابة الهواء (Boîtier papillon)",
            "Throttle / Pedal Position Sensor 'A' Circuit Range/Performance",
            "Piste électrique du boîtier papillon usée ou papillon encrassé.",
            "تآكل المسار الكهربائي لبوابة الهواء أو تراكم الأوساخ عليها.",
            "Worn potentiometer track inside the throttle body or carbon buildup on the throttle plate."
        ),
        dtc(
            "P0122", ItemSeverity.IMPORTANT,
            "Capteur de position du papillon 'A' : tension trop basse",
            "انخفاض إشارة حساس بوابة الهواء أو الدعسة",
            "Throttle / Pedal Position Sensor 'A' Circuit Low",
            "Faisceau du boîtier papillon endommagé ou capteur en court-circuit.",
            "تلف في أسلاك بوابة الهواء أو تماس في الحساس.",
            "Damaged throttle body wiring harness or shorted position sensor."
        ),
        dtc(
            "P0123", ItemSeverity.IMPORTANT,
            "Capteur de position du papillon 'A' : tension trop haute",
            "ارتفاع إشارة حساس بوابة الهواء",
            "Throttle / Pedal Position Sensor 'A' Circuit High",
            "Prise du boîtier papillon mal branchée ou capteur défectueux.",
            "فيشة بوابة الهواء غير مثبتة جيداً أو الحساس تالف.",
            "Loose throttle body connector or faulty throttle position sensor."
        ),
        dtc(
            "P0131", ItemSeverity.MINOR,
            "Sonde Lambda amont (Banque 1 Capteur 1) : tension basse (Mélange pauvre)",
            "إشارة ضعيفة من حساس الشكمان الأمامي",
            "O2 Sensor Circuit Low Voltage (Bank 1 Sensor 1)",
            "La sonde indique un mélange constamment pauvre ou la sonde elle-même est fatiguée.",
            "حساس الأكسجين يشير إلى خليط فقير باستمرار أو أن الحساس نفسه انتهى عمره.",
            "Upstream O2 sensor voltage stays low due to an exhaust/intake leak or failing sensor."
        ),
        dtc(
            "P0132", ItemSeverity.MINOR,
            "Sonde Lambda amont (Banque 1 Capteur 1) : tension haute",
            "جهد مرتفع في حساس الشكمان الأمامي",
            "O2 Sensor Circuit High Voltage (Bank 1 Sensor 1)",
            "Mélange trop riche ou court-circuit dans la sonde à oxygène amont.",
            "خليط وقود غني جداً أو تماس داخلي في حساس الأكسجين الأمامي.",
            "Rich fuel mixture or internal short in the upstream O2 sensor."
        ),
        dtc(
            "P0133", ItemSeverity.MINOR,
            "Sonde Lambda amont (Banque 1 Capteur 1) : réponse trop lente",
            "بطء استجابة حساس الأكسجين الأمامي (تقدم عمر الحساس)",
            "O2 Sensor Circuit Slow Response (Bank 1 Sensor 1)",
            "La sonde à oxygène est encrassée par la suie et réagit trop lentement, ce qui augmente la consommation de carburant.",
            "تراكم الكربون على حساس الأكسجين جعله بطيء الاستجابة، مما يرفع استهلاك البنزين.",
            "The upstream O2 sensor is sluggish from soot buildup, increasing fuel consumption."
        ),
        dtc(
            "P0134", ItemSeverity.MINOR,
            "Sonde Lambda amont (Banque 1 Capteur 1) : aucune activité détectée",
            "توقف حساس الشكمان الأمامي عن إرسال الإشارة",
            "O2 Sensor Circuit No Activity Detected (Bank 1 Sensor 1)",
            "La sonde Lambda est débranchée ou hors service.",
            "حساس الأكسجين الأمامي مفصول أو معطل تماماً.",
            "The upstream oxygen sensor is unplugged or dead."
        ),
        dtc(
            "P0135", ItemSeverity.MINOR,
            "Chauffage de la sonde Lambda amont (Banque 1 Capteur 1) : panne",
            "تلف دائرة التسخين في حساس الأكسجين الأمامي",
            "O2 Sensor Heater Circuit (Bank 1 Sensor 1)",
            "Le chauffage interne de la sonde Lambda avant le catalyseur est coupé.",
            "تعطل المسخن الداخلي لحساس الشكمان قبل علبة البيئة.",
            "Upstream O2 sensor internal heater failure."
        ),
        dtc(
            "P0136", ItemSeverity.MINOR,
            "Sonde Lambda aval (Banque 1 Capteur 2) : panne du circuit",
            "عطل في حساس الشكمان الخلفي (بعد الكاتاليزور)",
            "O2 Sensor Circuit (Bank 1 Sensor 2)",
            "La sonde de contrôle située après le catalyseur est endommagée ou débranchée.",
            "حساس المراقبة الموجود بعد الكاتاليزور تالف أو مفصول.",
            "The post-catalyst oxygen sensor is damaged or unplugged."
        ),
        dtc(
            "P0138", ItemSeverity.MINOR,
            "Sonde Lambda aval (Banque 1 Capteur 2) : tension haute",
            "ارتفاع إشارة حساس الشكمان الخلفي",
            "O2 Sensor Circuit High Voltage (Bank 1 Sensor 2)",
            "Signal anormal de la sonde arrière ou catalyseur saturé.",
            "إشارة غير طبيعية من الحساس الخلفي أو تشبع في الكاتاليزور.",
            "Abnormal downstream O2 sensor signal or saturated catalyst."
        ),
        dtc(
            "P0141", ItemSeverity.MINOR,
            "Chauffage de la sonde Lambda aval (Banque 1 Capteur 2) : panne",
            "عطل مسخن حساس الشكمان الخلفي (بعد علبة البيئة)",
            "O2 Sensor Heater Circuit (Bank 1 Sensor 2)",
            "Résistance de chauffe de la sonde arrière grillée.",
            "احتراق مقاومة التسخين داخل حساس الشكمان الخلفي.",
            "Downstream O2 sensor heater element burned out."
        ),
        dtc(
            "P0170", ItemSeverity.MINOR,
            "Anomalie de correction de richesse carburant (Fuel Trim Banque 1)",
            "خلل في ضبط خليط الوقود والهواء (بنزين / غاز GPL)",
            "Fuel Trim (Bank 1)",
            "Le calculateur atteint sa limite de correction de carburant (prise d'air, pression d'essence faible ou réglage GPL décalé).",
            "وصل الكمبيوتر إلى الحد الأقصى لتصحيح الوقود بسبب تسرب هواء أو ضعف مضخة البنزين أو عيار الغاز.",
            "The ECU has reached its fuel trim adjustment limit due to an air leak, weak fuel pump, or LPG calibration drift."
        ),
        dtc(
            "P0180", ItemSeverity.MINOR,
            "Capteur de température de carburant 'A' : panne du circuit",
            "عطل في حساس حرارة الوقود (الديزل)",
            "Fuel Temperature Sensor 'A' Circuit",
            "La sonde qui mesure la température du gasoil sur le circuit retour ou la pompe est coupée.",
            "تلف الحساس الذي يقيس حرارة الديزل على أنابيب الراجع أو المضخة.",
            "The diesel fuel temperature sensor on the return line or pump has an electrical fault."
        ),
        dtc(
            "P0190", ItemSeverity.CRITICAL,
            "Capteur de pression de la rampe de carburant : panne du circuit",
            "عطل في حساس ضغط مسطرة الوقود (Capteur de rampe)",
            "Fuel Rail Pressure Sensor 'A' Circuit",
            "Le capteur fixé au bout de la rampe d'injection n'envoie plus de signal fiable. Cause des à-coups violents ou un refus de démarrer.",
            "الحساس المثبت على مسطرة الحاقنات لا يرسل إشارة صحيحة، مما يسبب تقطيعاً قوياً أو عدم اشتغال المحرك.",
            "The pressure sensor on the common rail is failing, causing severe bucking or a no-start."
        ),
        dtc(
            "P0191", ItemSeverity.CRITICAL,
            "Capteur de pression de la rampe de carburant : plage / performance",
            "تذبذب غير طبيعي في قراءة حساس مسطرة الديزل/الوقود",
            "Fuel Rail Pressure Sensor 'A' Circuit Range/Performance",
            "Écart anormal entre la pression demandée et la pression mesurée dans la rampe (capteur défaillant, filtre bouché ou fuite d'injecteur).",
            "فرق كبير بين الضغط المطلوب والضغط المقاس في المسطرة (بسبب الحساس أو انسداد الفلتر أو رجوع الديزل من الحاقنات).",
            "Mismatch between target and measured fuel rail pressure due to a faulty rail sensor, clogged filter, or injector leak-off."
        ),
        dtc(
            "P0192", ItemSeverity.CRITICAL,
            "Capteur de pression de la rampe de carburant : tension trop basse",
            "انخفاض إشارة حساس ضغط مسطرة الوقود",
            "Fuel Rail Pressure Sensor 'A' Circuit Low",
            "Prise du capteur de rampe oxydée ou court-circuit à la masse.",
            "تأكسد فيشة حساس المسطرة أو تماس كهربائي.",
            "Corroded fuel rail pressure sensor plug or short to ground."
        ),
        dtc(
            "P0193", ItemSeverity.CRITICAL,
            "Capteur de pression de la rampe de carburant : tension trop haute",
            "ارتفاع إشارة حساس ضغط مسطرة الوقود (أو الفيشة مفصولة)",
            "Fuel Rail Pressure Sensor 'A' Circuit High",
            "Capteur de pression de rampe débranché ou en court-circuit, faisant tourner le moteur en mode secours bruyant.",
            "فيشة حساس المسطرة مفصولة أو بها تماس، مما يجعل المحرك يعمل بصوت خشن في وضع الطوارئ.",
            "Fuel rail pressure sensor unplugged or shorted, forcing noisy limp-home injection mode."
        ),
        dtc(
            "P0196", ItemSeverity.MINOR,
            "Capteur de température d'huile moteur : plage ou performance",
            "خلل في قراءة حساس حرارة زيت المحرك",
            "Engine Oil Temperature Sensor 'A' Range/Performance",
            "La sonde de température/niveau d'huile sous le carter donne une valeur incohérente.",
            "حساس حرارة ومستوى الزيت أسفل الكارتير يعطي قراءة غير دقيقة.",
            "The oil temperature/level sensor in the sump is reporting an implausible reading."
        )
    )
}
