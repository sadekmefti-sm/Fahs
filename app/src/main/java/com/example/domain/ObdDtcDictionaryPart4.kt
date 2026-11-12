package com.example.domain

import com.example.data.model.ItemSeverity
import com.example.data.model.VerificationStatus

/**
 * Phase 4 Expansion (Part 4): Common OBD-II Fault Codes (P0501 - U0423)
 * covering idle control, cruise control, alternator, thermostats, automatic/DSG gearboxes,
 * DPF/FAP, AdBlue/SCR, VAG/PSA/Renault common P1xxx/P2xxx codes, ABS/ESP (C0xxx),
 * Airbag (B0xxx), and CAN network (U0xxx), PLUS standard generic SAE J2012 trilingual titles.
 */
object ObdDtcDictionaryPart4 {

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
            "P0501", ItemSeverity.IMPORTANT,
            "Capteur de vitesse du véhicule 'A' : plage ou performance",
            "عدم انتظام إشارة حساس سرعة السيارة (تأكد من عداد الكيلومترات)",
            "Vehicle Speed Sensor 'A' Range/Performance",
            "Le signal de vitesse fluctue ou ne correspond pas aux capteurs ABS. Vérifiez que le compteur kilométrique n'a pas été manipulé.",
            "إشارة السرعة متذبذبة أو لا تتطابق مع حساسات ABS. تحقق من سلامة عداد المسافات.",
            "The vehicle speed signal is erratic or disagrees with wheel speed sensors. Check odometer operation."
        ),
        dtc(
            "P0504", ItemSeverity.MINOR,
            "Contacteur de pédale de frein 'A' / 'B' : corrélation",
            "خلل في حساس دواسة الفرامل (Contacteur de stop)",
            "Brake Switch 'A' / 'B' Correlation",
            "Le contacteur sous la pédale de frein est usé : les feux stop peuvent rester éteints et le régulateur de vitesse se désactive.",
            "تلف الحساس أسفل دواسة الفرامل، مما قد يعطل أضواء التوقف الخلفية ومثبت السرعة.",
            "The brake pedal switch contacts disagree, disabling cruise control and potentially affecting brake lights."
        ),
        dtc(
            "P0505", ItemSeverity.MINOR,
            "Système de commande du ralenti (Pas-à-pas / IAC) : dysfonctionnement",
            "خلل في منظم دوران المحرك عند التوقف (Moteur pas-à-pas / Ralenti)",
            "Idle Air Control (IAC) System",
            "Le régulateur de ralenti est encrassé : le moteur cale aux feux rouges ou reste accéléré tout seul.",
            "اتساخ منظم السلانسيه (Pas-à-pas)، مما يجعل المحرك ينطفئ عند التوقف أو يبقى مرتفع الدوران.",
            "The idle air control valve is dirty or sticking, causing stalling at stops or high idle."
        ),
        dtc(
            "P0506", ItemSeverity.MINOR,
            "Système de ralenti : régime inférieur à la consigne",
            "دوران المحرك في السلانسيه أضعف من الطبيعي",
            "Idle Air Control System RPM Lower Than Expected",
            "Le boîtier papillon est encrassé et ne laisse plus passer assez d'air au ralenti.",
            "بوابة الهواء متسخة ولا تسمح بمرور هواء كافٍ عند التوقف.",
            "Carbon buildup in the throttle body is restricting idle airflow, causing a low unstable idle."
        ),
        dtc(
            "P0507", ItemSeverity.MINOR,
            "Système de ralenti : régime supérieur à la consigne (Prise d'air)",
            "دوران المحرك في السلانسيه أعلى من الطبيعي (تسرب هواء)",
            "Idle Air Control System RPM Higher Than Expected",
            "Le ralenti reste trop haut à cause d'une prise d'air sur l'admission ou d'un papillon mal calibré.",
            "يبقى دوران المحرك مرتفعاً بسبب تسرب هواء في الثلاجة أو حاجة البوابة للبرمجة.",
            "Idle RPM stays too high due to a vacuum leak on the intake manifold or uncalibrated throttle body."
        ),
        dtc(
            "P0520", ItemSeverity.CRITICAL,
            "Capteur / manocontact de pression d'huile moteur : panne du circuit",
            "عطل في حساس ضغط زيت المحرك (Mano d'huile)",
            "Engine Oil Pressure Sensor / Switch Circuit",
            "Le manocontact de pression d'huile est défaillant ou la pression d'huile est réellement insuffisante. À vérifier d'urgence.",
            "حساس ضغط الزيت تالف أو أن ضغط الزيت ضعيف فعلياً. يجب فحصه فوراً.",
            "The oil pressure switch/sensor has failed or actual oil pressure is dangerously low. Inspect immediately."
        ),
        dtc(
            "P0524", ItemSeverity.CRITICAL,
            "Pression d'huile moteur trop basse !",
            "انخفاض خطير في ضغط زيت المحرك!",
            "Engine Oil Pressure Too Low",
            "Alerte critique : la pompe à huile, la crépine bouchée ou les coussinets usés ne maintiennent plus la pression d'huile.",
            "تنبيه خطير جداً: مضخة الزيت أو المصفاة المسدودة (Crépine) لا توفر ضغط زيت كافٍ للمحرك.",
            "Critical warning: the oil pump, clogged pickup strainer (e.g., PureTech belt debris), or worn bearings cannot maintain oil pressure."
        ),
        dtc(
            "P0530", ItemSeverity.MINOR,
            "Capteur de pression du fluide de climatisation 'A' : panne",
            "عطل في حساس ضغط غاز المكيف (Mano de clim / Gaz vide)",
            "A/C Refrigerant Pressure Sensor 'A' Circuit",
            "Le circuit de climatisation est vide de gaz (fuite) ou le pressostat de clim est HS, empêchant le compresseur de s'enclencher.",
            "فرغ غاز المكيف بسبب تسرب أو تلف حساس الضغط، مما يمنع اشتغال كمبروسر المكيف.",
            "The A/C system has lost its refrigerant charge or the pressure sensor is faulty, preventing compressor engagement."
        ),
        dtc(
            "P0560", ItemSeverity.MINOR,
            "Tension du système électrique du véhicule anormale",
            "عدم استقرار الجهد الكهربائي للسيارة (البطارية / الدينامو)",
            "System Voltage",
            "Fluctuation de tension de la batterie ou du régulateur d'alternateur.",
            "تذبذب في فولتية البطارية أو منظم الدينامو.",
            "Abnormal fluctuation in battery or alternator charging voltage."
        ),
        dtc(
            "P0562", ItemSeverity.IMPORTANT,
            "Tension du système électrique trop basse (Batterie faible / Alternateur)",
            "انخفاض جهد البطارية أو ضعف شحن الدينامو",
            "System Voltage Low",
            "La batterie est déchargée ou l'alternateur ne charge plus suffisamment.",
            "البطارية ضعيفة أو الدينامو لا يشحن بالقدر الكافي.",
            "Battery voltage dropped too low due to a weak battery or failing alternator."
        ),
        dtc(
            "P0563", ItemSeverity.IMPORTANT,
            "Tension du système électrique trop haute (Régulateur d'alternateur HS)",
            "شحن زائد من الدينامو (تلف منظم الفولتية Régulateur)",
            "System Voltage High",
            "Le régulateur de l'alternateur envoie plus de 15 V, ce qui risque d'endommager la batterie et les calculateurs.",
            "منظم الدينامو يرسل جهد شحن مرتفعاً جداً (فوق 15 فولت) مما قد يتلف البطارية والكمبيوتر.",
            "The alternator voltage regulator is overcharging above 15 V, risking battery and ECU damage."
        ),
        dtc(
            "P0571", ItemSeverity.MINOR,
            "Circuit du contacteur de frein 'A' (Régulateur de vitesse)",
            "عطل في حساس دواسة الفرامل الخاص بمثبت السرعة",
            "Brake Switch 'A' Circuit",
            "Contacteur de pédale de frein HS désactivant le régulateur de vitesse.",
            "تلف حساس دواسة الفرامل مما يعطل مثبت السرعة (Cruise control).",
            "Faulty brake pedal switch disabling cruise control."
        ),
        dtc(
            "P0597", ItemSeverity.IMPORTANT,
            "Commande du thermostat piloté : circuit ouvert",
            "عطل كهربائي في الثرموستات الإلكتروني (Boîtier d'eau)",
            "Thermostat Heater Control Circuit / Open",
            "La résistance du thermostat piloté (fréquent sur Peugeot/Citroën VTi/THP et Opel) est coupée.",
            "احتراق المسخن الداخلي للثرموستات الإلكتروني (شائع في محركات بيجو وستروين وأوبل).",
            "The electrically controlled thermostat heater circuit is open (common on PSA and Opel engines)."
        ),
        dtc(
            "P0601", ItemSeverity.CRITICAL,
            "Erreur de somme de contrôle mémoire interne du calculateur (Checksum)",
            "خطأ داخلي في ذاكرة كمبيوتر المحرك (أو تعديل غير سليم للبرمجة)",
            "Internal Control Module Memory Check Sum Error",
            "Le calculateur moteur a une mémoire corrompue ou a subi une reprogrammation (stage 1 / défapage) mal réalisée.",
            "ذاكرة كمبيوتر المحرك بها خلل أو خضعت لتعديل برمجي (إلغاء البيئة أو رفع العزم) بطريقة غير صحيحة.",
            "The engine ECU internal flash memory checksum failed, often after a poorly executed remap or DPF/EGR delete."
        ),
        dtc(
            "P0606", ItemSeverity.CRITICAL,
            "Défaillance du processeur interne du calculateur moteur (ECM/PCM)",
            "عطل داخلي في معالج كمبيوتر المحرك (Calculateur)",
            "ECM / PCM Processor",
            "Panne électronique interne du calculateur moteur ou infiltration d'eau dans sa prise.",
            "خلل إلكتروني داخلي في كمبيوتر المحرك أو تسرب ماء إلى فيشته.",
            "Internal processor fault inside the engine control unit or water ingress in the ECU connector."
        ),
        dtc(
            "P0620", ItemSeverity.IMPORTANT,
            "Circuit de commande de l'alternateur : dysfonctionnement",
            "عطل في دائرة التحكم في الدينامو (Alternateur piloté)",
            "Generator Control Circuit",
            "Le calculateur ne communique plus avec le régulateur de l'alternateur (ou alternateur adaptable non compatible monté).",
            "الكمبيوتر لا يتواصل مع منظم الدينامو (أو تم تركيب منظم تجاري غير متوافق).",
            "Communication fault between the ECU and the smart alternator regulator."
        ),
        dtc(
            "P0638", ItemSeverity.IMPORTANT,
            "Commande du papillon motorisé (Banque 1) : plage ou performance",
            "خلل في استجابة بوابة الهواء الكهربائية (Boîtier papillon)",
            "Throttle Actuator Control Range/Performance (Bank 1)",
            "Le volet papillon est bloqué par la calamine ou ses engrenages en plastique sont usés.",
            "تعلق بوابة الهواء بسبب الكربون أو تآكل تروسها الداخلية.",
            "The motorized throttle flap is sticking from carbon buildup or worn internal plastic gears."
        ),
        dtc(
            "P0641", ItemSeverity.IMPORTANT,
            "Tension de référence des capteurs 'A' (5 Volts) : circuit ouvert",
            "خلل في خط تغذية الحساسات (5 فولت - الدائرة A)",
            "Sensor Reference Voltage 'A' Circuit / Open",
            "Un des capteurs branchés sur la ligne 5V (ex: capteur EGR, pression clim ou rampe) est en court-circuit et perturbe les autres.",
            "أحد الحساسات المشتركة في خط 5 فولت به تماس كهربائي ويؤثر على بقية الحساسات.",
            "One of the sensors sharing the 5V reference rail (e.g., EGR, A/C pressure, or rail sensor) is shorting the supply line."
        ),
        dtc(
            "P0651", ItemSeverity.IMPORTANT,
            "Tension de référence des capteurs 'B' (5 Volts) : circuit ouvert",
            "خلل في خط تغذية الحساسات (5 فولت - الدائرة B)",
            "Sensor Reference Voltage 'B' Circuit / Open",
            "Court-circuit sur un capteur alimenté par la ligne 5V secondaire du calculateur.",
            "تماس في أحد الحساسات المتصلة بخط التغذية الثاني (5 فولت) من الكمبيوتر.",
            "Short circuit on a sensor powered by the ECU secondary 5V reference line."
        ),
        dtc(
            "P0670", ItemSeverity.MINOR,
            "Boîtier de commande des bougies de préchauffage : panne du circuit",
            "عطل في علبة/ريلاي شمعات التسخين (Boîtier de préchauffage)",
            "Glow Plug Control Module Control Circuit",
            "Le relais de préchauffage diesel est défaillant.",
            "خلل في علبة التحكم بشمعات التسخين لمحرك الديزل.",
            "The diesel glow plug control module/relay has a circuit fault."
        ),
        dtc(
            "P0671", ItemSeverity.MINOR,
            "Bougie de préchauffage du cylindre n°1 : circuit ouvert",
            "تلف شمعة التسخين (Bougie de préchauffage) رقم 1",
            "Cylinder 1 Glow Plug Circuit / Open",
            "La bougie de préchauffage du cylindre 1 est grillée.",
            "شمعة التسخين الخاصة بالسيلندر رقم 1 محترقة.",
            "Diesel glow plug on cylinder 1 has burned out."
        ),
        dtc(
            "P0672", ItemSeverity.MINOR,
            "Bougie de préchauffage du cylindre n°2 : circuit ouvert",
            "تلف شمعة التسخين (Bougie de préchauffage) رقم 2",
            "Cylinder 2 Glow Plug Circuit / Open",
            "La bougie de préchauffage du cylindre 2 est grillée.",
            "شمعة التسخين الخاصة بالسيلندر رقم 2 محترقة.",
            "Diesel glow plug on cylinder 2 has burned out."
        ),
        dtc(
            "P0673", ItemSeverity.MINOR,
            "Bougie de préchauffage du cylindre n°3 : circuit ouvert",
            "تلف شمعة التسخين (Bougie de préchauffage) رقم 3",
            "Cylinder 3 Glow Plug Circuit / Open",
            "La bougie de préchauffage du cylindre 3 est grillée.",
            "شمعة التسخين الخاصة بالسيلندر رقم 3 محترقة.",
            "Diesel glow plug on cylinder 3 has burned out."
        ),
        dtc(
            "P0674", ItemSeverity.MINOR,
            "Bougie de préchauffage du cylindre n°4 : circuit ouvert",
            "تلف شمعة التسخين (Bougie de préchauffage) رقم 4",
            "Cylinder 4 Glow Plug Circuit / Open",
            "La bougie de préchauffage du cylindre 4 est grillée.",
            "شمعة التسخين الخاصة بالسيلندر رقم 4 محترقة.",
            "Diesel glow plug on cylinder 4 has burned out."
        ),
        dtc(
            "P0683", ItemSeverity.MINOR,
            "Communication boîtier de préchauffage / calculateur moteur",
            "خلل اتصال بين علبة التسخين وكمبيوتر المحرك",
            "Glow Plug Control Module to PCM Communication Circuit",
            "Une ou plusieurs bougies de préchauffage ou le boîtier relais sont à remplacer.",
            "يلزم فحص شمعات التسخين أو علبة الريلاي الخاصة بها.",
            "One or more glow plugs or the glow plug relay module needs replacement."
        ),
        dtc(
            "P0705", ItemSeverity.CRITICAL,
            "Capteur de position du sélecteur de boîte automatique (PRNDL)",
            "عطل في حساس موضع ذراع علبة السرعة الأوتوماتيكية (PRNDL)",
            "Transmission Range Sensor 'A' Circuit (PRNDL Input)",
            "La boîte automatique ne sait plus sur quelle position (P, R, N, D) se trouve le levier. Peut empêcher le démarrage.",
            "علبة السرعة الأوتوماتيكية لا تتعرف على موضع الذراع (P, R, N, D) مما قد يمنع تشغيل المحرك.",
            "The automatic gearbox inhibitor/range switch cannot detect lever position (P, R, N, D)."
        ),
        dtc(
            "P0715", ItemSeverity.CRITICAL,
            "Capteur de vitesse d'entrée de boîte automatique (Turbine) : panne",
            "عطل في حساس سرعة الدخول داخل علبة السرعة الأوتوماتيكية",
            "Input / Turbine Speed Sensor 'A' Circuit",
            "Capteur interne de la boîte automatique (ou carte mécatronique) en défaut, provoquant un blocage sur un seul rapport.",
            "خلل في حساس السرعة الداخلي للعلبة الأوتوماتيكية مما يجعلها تعلق في غيار واحد.",
            "Internal transmission input speed sensor fault, often locking the gearbox in limp-home gear."
        ),
        dtc(
            "P0720", ItemSeverity.CRITICAL,
            "Capteur de vitesse de sortie de boîte de vitesses : panne du circuit",
            "عطل في حساس سرعة الخروج لعلبة السرعة الأوتوماتيكية",
            "Output Speed Sensor Circuit",
            "La boîte automatique ne lit plus la vitesse de sortie, causant des passages de rapports brutaux.",
            "علبة السرعة لا تقرأ سرعة الخروج مما يسبب نتعات قوية عند تبديل الغيارات.",
            "Transmission output shaft speed sensor circuit fault, causing harsh shifting."
        ),
        dtc(
            "P0730", ItemSeverity.CRITICAL,
            "Rapport de démultiplication incorrect (Patinage de boîte automatique)",
            "انزلاق داخلي في علبة السرعة الأوتوماتيكية (Patinage)",
            "Incorrect Gear Ratio (Automatic Transmission Slipping)",
            "Alerte grave : les disques d'embrayage internes de la boîte automatique patinent ou la pression hydraulique est trop faible.",
            "تنبيه خطير: انزلاق في كلتشات علبة السرعة الأوتوماتيكية أو ضعف شديد في ضغط الزيت الداخلي.",
            "Severe alert: internal automatic transmission clutches are slipping between input and output shafts."
        ),
        dtc(
            "P0741", ItemSeverity.CRITICAL,
            "Embrayage du convertisseur de couple (BVA) : blocage ou patinage",
            "خلل أو انزلاق في محول العزم (Convertisseur BVA)",
            "Torque Converter Clutch Solenoid Circuit Performance / Stuck Off",
            "Le convertisseur de couple de la boîte automatique patine ou son électrovanne de pontage est usée.",
            "انزلاق في الطنجرة (Convertisseur) الخاصة بالعلبة الأوتوماتيكية أو تلف صمامها.",
            "The torque converter lock-up clutch is slipping or its solenoid valve is worn."
        ),
        dtc(
            "P0841", ItemSeverity.CRITICAL,
            "Capteur de pression d'huile de boîte automatique / DSG : plage/performance",
            "خلل في ضغط الزيت داخل وحدة الميكاترونيك (DSG / BVA)",
            "Transmission Fluid Pressure Sensor / Switch 'A' Circuit Range/Performance",
            "Perte de pression hydraulique dans le bloc mécatronique (très fréquent sur boîte DSG7 DQ200).",
            "فقدان ضغط الزيت الهيدروليكي داخل وحدة الميكاترونيك (شائع جداً في علب DSG7).",
            "Hydraulic pressure drop inside the transmission valve body / DSG7 mechatronic unit."
        ),
        dtc(
            "P1351", ItemSeverity.MINOR,
            "[PSA 1.6 / 1.4 HDi] Boîtier de préchauffage alimenté mais bougies non alimentées",
            "[محركات HDi] تلف شمعات التسخين (Bougies de préchauffage)",
            "[PSA HDi] Glow Plug Relay Controlled - Glow Plugs Not Supplied",
            "Code extrêmement fréquent sur tous les moteurs Peugeot/Citroën HDi : une ou plusieurs bougies de préchauffage sont grillées.",
            "كود شائع جداً في محركات بيجو وستروين HDi: يدل على احتراق واحدة أو أكثر من شمعات التسخين.",
            "Extremely common on Peugeot/Citroën HDi engines: one or more glow plugs are open-circuit."
        ),
        dtc(
            "P17D8", ItemSeverity.CRITICAL,
            "[Boîte DSG / S-Tronic] Limitation de couple à cause de la température d'embrayage",
            "[علبة DSG] ارتفاع حرارة الدبرياج وتآكله",
            "[DSG Gearbox] Torque Limitation Due to Clutch Temperature",
            "Les embrayages de la boîte automatique à double embrayage chauffent et patinent.",
            "ارتفاع حرارة كلتشات علبة DSG بسبب انزلاقها وتآكلها.",
            "Dual-clutch pack is overheating from slip and wear."
        ),
        dtc(
            "P189C", ItemSeverity.CRITICAL,
            "[Boîte DSG7] Fonctionnement limité par manque de pression hydraulique",
            "[علبة DSG7] نقص ضغط الزيت في الميكاترونيك (توقف الغيارات)",
            "[DSG7 Gearbox] Function Restriction Due to Insufficient Pressure Build-Up",
            "Fissure du corps d'accumulateur dans le mécatronique DSG7 : la boîte peut passer au point mort en roulant.",
            "تصدع مجمع الضغط داخل ميكاترونيك DSG7، مما قد يفصل الحركة فجأة أثناء السير.",
            "Cracked accumulator housing inside the DSG7 mechatronic unit causing loss of drive."
        ),
        dtc(
            "P2004", ItemSeverity.IMPORTANT,
            "Volets de turbulence d'admission (Swirl Flaps) bloqués ouverts (Banque 1)",
            "تعلق بوابات الدوامة الهوائية في ثلاجة المحرك (Volet Swirl)",
            "Intake Manifold Runner Control Stuck Open (Bank 1)",
            "Les volets d'admission (fréquents sur 2.0 TDI et HDi) sont grippés par la calamine.",
            "بوابات توجيه الهواء داخل ثلاجة المحرك عالقة بسبب تراكم الكربون (شائع في 2.0 TDI).",
            "Intake manifold swirl flaps are stuck open due to soot accumulation."
        ),
        dtc(
            "P2015", ItemSeverity.IMPORTANT,
            "Capteur de position des volets d'admission (Collecteur 2.0 TDI) : hors plage",
            "عطل شهير في ذراع بوابات ثلاجة المحرك (2.0 TDI)",
            "Intake Manifold Runner Position Sensor / Switch Circuit Range/Performance",
            "Défaut très connu sur les moteurs 2.0 TDI (Golf, Tiguan, Octavia, Leon) : la butée du moteur des volets d'admission est usée.",
            "عطل معروف جداً في محركات 2.0 TDI: تآكل ذراع محرك بوابات الثلاجة.",
            "Well-known 2.0 TDI fault: worn mechanical stop on the intake manifold flap actuator motor."
        ),
        dtc(
            "P207F", ItemSeverity.IMPORTANT,
            "Qualité du liquide AdBlue / Réducteur hors spécifications (Système SCR)",
            "خلل في جودة سائل AdBlue أو حساس نظام البيئة SCR",
            "Reductant Quality Performance (AdBlue / DEF System)",
            "Anomalie du système AdBlue (cristallisation de l'urée, capteur NOx ou réservoir AdBlue sur BlueHDi / TDI).",
            "مشكلة في نظام AdBlue (تبلور السائل أو عطل في الحساس أو الخزان، شائع في محركات BlueHDi).",
            "AdBlue/SCR system fault (urea crystallization, NOx sensor, or tank pump on BlueHDi / TDI)."
        ),
        dtc(
            "P20E8", ItemSeverity.CRITICAL,
            "[PSA BlueHDi / SCR] Pression de liquide AdBlue trop basse (Réservoir AdBlue)",
            "[محركات BlueHDi] انخفاض ضغط مضخة خزان AdBlue (عطل مكلف)",
            "Reductant Pressure Too Low (AdBlue Pump / Tank Failure)",
            "Défaut classique des moteurs Peugeot/Citroën BlueHDi : la pompe intégrée au réservoir AdBlue est en panne (ou cristallisée). Peut bloquer le démarrage après le décompte kilométrique.",
            "عطل شهير جداً في محركات BlueHDi: تعطل مضخة خزان AdBlue بسبب التبلور، وقد يمنع تشغيل المحرك بعد انتهاء العد التنازلي.",
            "Classic Peugeot/Citroën BlueHDi fault: the pump inside the AdBlue tank has failed, which triggers a no-start countdown."
        ),
        dtc(
            "P2111", ItemSeverity.IMPORTANT,
            "Système de commande du papillon motorisé : bloqué ouvert",
            "بوابة الهواء الكهربائية عالقة في وضع الفتح",
            "Throttle Actuator Control System - Stuck Open",
            "Le boîtier papillon motorisé est grippé mécaniquement.",
            "تعطل ميكانيكي في بوابة الهواء الكهربائية.",
            "The electronic throttle body plate is mechanically stuck open."
        ),
        dtc(
            "P2135", ItemSeverity.IMPORTANT,
            "Corrélation des capteurs de position de papillon / pédale 'A' et 'B'",
            "عدم تطابق إشارتي حساس بوابة الهواء أو الدعسة (A و B)",
            "Throttle / Pedal Position Sensor / Switch 'A' / 'B' Voltage Correlation",
            "Mauvais contact dans la prise du boîtier papillon ou piste usée, mettant le moteur en sécurité.",
            "سوء اتصال في فيشة بوابة الهواء أو تآكل داخلي يدخل المحرك في وضع الطوارئ.",
            "Poor contact in the throttle body connector or worn sensor tracks causing limp mode."
        ),
        dtc(
            "P2138", ItemSeverity.IMPORTANT,
            "Corrélation des capteurs de pédale d'accélérateur 'D' et 'E'",
            "خلل في حساس دواسة البنزين الإلكترونية",
            "Throttle / Pedal Position Sensor / Switch 'D' / 'E' Voltage Correlation",
            "Le potentiomètre de la pédale d'accélérateur est usé ou a pris l'humidité.",
            "تآكل حساس دواسة الوقود أو وصول رطوبة إليه.",
            "The accelerator pedal position sensor assembly is worn or damp."
        ),
        dtc(
            "P2263", ItemSeverity.CRITICAL,
            "Performance du système de suralimentation du turbo (Pression non atteinte)",
            "ضعف أداء نظام شحن التيربو (تسرب هواء أو تكلس التيربو)",
            "Turbocharger / Supercharger Boost System Performance",
            "Écart important sur la pression de turbo : durite d'échangeur fendue, capsule percée ou géométrie variable grippée.",
            "فرق كبير في ضغط التيربو بسبب شق في أنبوب الإنتركولر أو تكلس ريش التيربو.",
            "Boost pressure deviation caused by a split intercooler hose, leaking vacuum actuator, or stuck turbo vanes."
        ),
        dtc(
            "P2279", ItemSeverity.IMPORTANT,
            "Fuite d'air détectée dans le système d'admission",
            "تسرب هواء في أنابيب السحب أو ثلاجة المحرك",
            "Intake Air System Leak",
            "Prise d'air entre le débitmètre/turbo et le moteur (joint de collecteur, durite de reniflard ou échangeur).",
            "وجود تسرب للهواء بين التيربو والمحرك (في أنابيب الهواء أو جوان الثلاجة).",
            "Unmetered air or boost leak between the turbo/MAF and the intake manifold."
        ),
        dtc(
            "P242F", ItemSeverity.CRITICAL,
            "Filtre à particules (FAP / DPF) saturé de cendres — Régénération bloquée",
            "انسداد كامل لمصفاة الدخان (FAP / DPF) وتوقف التنظيف الذاتي",
            "Diesel Particulate Filter Restriction - Ash Accumulation",
            "Le filtre à particules est complètement saturé : le moteur est bridé en mode dégradé. Nécessite un nettoyage professionnel ou remplacement.",
            "انسداد مصفاة البيئة (FAP) بالكامل ودخول المحرك في وضع الحماية (فقدان العزم). يحتاج تنظيفاً مختصاً أو استبدالاً.",
            "The diesel particulate filter is completely full of ash/soot, locking the engine in limp mode."
        ),
        dtc(
            "P2452", ItemSeverity.IMPORTANT,
            "Capteur de pression différentielle du filtre à particules (FAP/DPF) : panne",
            "عطل في حساس فرق الضغط لمصفاة البيئة (Capteur FAP)",
            "Diesel Particulate Filter Differential Pressure Sensor 'A' Circuit",
            "Le capteur qui mesure le colmatage du FAP ou ses petites durites en caoutchouc sont coupés.",
            "تلف الحساس الذي يقيس امتلاء مصفاة FAP أو تشقق أنابيبه المطاطية الصغيرة.",
            "The DPF differential pressure sensor or its small rubber sensing hoses have failed."
        ),
        dtc(
            "P2453", ItemSeverity.IMPORTANT,
            "Capteur de pression différentielle du FAP : signal incohérent",
            "قراءة غير صحيحة من حساس ضغط مصفاة FAP",
            "Diesel Particulate Filter Differential Pressure Sensor 'A' Range/Performance",
            "Capteur de pression FAP défaillant empêchant les régénérations automatiques.",
            "خلل في حساس ضغط FAP يمنع عملية التنظيف الذاتي للمصفاة.",
            "Faulty DPF pressure sensor preventing automatic filter regeneration."
        ),
        dtc(
            "P2463", ItemSeverity.CRITICAL,
            "Filtre à particules (FAP / DPF) : accumulation excessive de suie",
            "تراكم مفرط للسخام داخل مصفاة الدخان (FAP مسدود)",
            "Diesel Particulate Filter Restriction - Soot Accumulation",
            "Le FAP est colmaté par la suie (trajets urbains courts ou problème d'injection).",
            "امتلاء مصفاة الدخان بالسخام بسبب السير داخل المدينة أو مشكلة في الاحتراق.",
            "Excessive soot buildup in the DPF from short city trips or an upstream boost/injection issue."
        ),
        dtc(
            "P2563", ItemSeverity.IMPORTANT,
            "Capteur de position de commande de géométrie du turbo : plage/performance",
            "خلل في حساس موضع صمام التيربو (Actuateur de turbo)",
            "Turbocharger Boost Control Position Sensor 'A' Circuit Range/Performance",
            "La capsule à dépression avec capteur de recopie sur le turbo (fréquent sur 1.6 HDi / 2.0 TDI) est percée ou grippée.",
            "تلف أو ثقب في كبسولة التحكم بالتيربو المزودة بحساس موضع (شائع في 1.6 HDi و 2.0 TDI).",
            "The turbocharger actuator diaphragm or its position sensor is sticking or leaking vacuum."
        ),
        dtc(
            "C0031", ItemSeverity.IMPORTANT,
            "Capteur de vitesse de roue avant gauche (ABS / ESP)",
            "عطل في حساس ABS للعجلة الأمامية اليسرى",
            "Left Front Wheel Speed Sensor",
            "Capteur ABS avant gauche coupé ou cible magnétique du roulement encrassée, allumant les voyants ABS et ESP.",
            "تلف حساس ABS الأمامي الأيسر أو اتساخ حلقة الرولمان، مما يضيء لمبات ABS و ESP.",
            "Front-left ABS wheel speed sensor or magnetic bearing ring fault."
        ),
        dtc(
            "C0034", ItemSeverity.IMPORTANT,
            "Capteur de vitesse de roue avant droite (ABS / ESP)",
            "عطل في حساس ABS للعجلة الأمامية اليمنى",
            "Right Front Wheel Speed Sensor",
            "Capteur ABS avant droit coupé ou encrassé.",
            "تلف حساس ABS للعجلة الأمامية اليمنى أو تقطع سلكه.",
            "Front-right ABS wheel speed sensor circuit or tone ring fault."
        ),
        dtc(
            "C0037", ItemSeverity.IMPORTANT,
            "Capteur de vitesse de roue arrière gauche (ABS / ESP)",
            "عطل في حساس ABS للعجلة الخلفية اليسرى",
            "Left Rear Wheel Speed Sensor",
            "Capteur ABS arrière gauche en défaut.",
            "خلل في حساس ABS للعجلة الخلفية اليسرى.",
            "Rear-left ABS wheel speed sensor fault."
        ),
        dtc(
            "C0040", ItemSeverity.IMPORTANT,
            "Capteur de vitesse de roue arrière droite (ABS / ESP)",
            "عطل في حساس ABS للعجلة الخلفية اليمنى",
            "Right Rear Wheel Speed Sensor",
            "Capteur ABS arrière droit en défaut.",
            "خلل في حساس ABS للعجلة الخلفية اليمنى.",
            "Rear-right ABS wheel speed sensor fault."
        ),
        dtc(
            "B0001", ItemSeverity.CRITICAL,
            "Circuit d'allumeur de l'airbag frontal conducteur (Contacteur tournant / Airbag)",
            "خلل في دائرة وسادة الهواء (Airbag) للسائق أو شريط المقود",
            "Driver Frontal Stage 1 Deployment Control",
            "Airbag conducteur débranché, contacteur tournant sous le volant coupé, ou résistance soudée après un accident. À inspecter très attentivement.",
            "انقطاع في شريط المقود (Contacteur tournant) أو أن الإيرباغ مفصول بعد حادث. افحص المقود بعناية.",
            "Driver airbag squib circuit open/high resistance (broken clockspring ribbon or prior airbag deployment)."
        ),
        dtc(
            "B0002", ItemSeverity.CRITICAL,
            "Circuit d'allumeur de l'airbag frontal conducteur (Niveau 2)",
            "خلل في المرحلة الثانية لإيرباغ السائق",
            "Driver Frontal Stage 2 Deployment Control",
            "Anomalie sur le second circuit de déclenchement de l'airbag conducteur.",
            "خلل في الدائرة الثانية لوسادة الهواء الخاصة بالسائق.",
            "Driver frontal stage 2 airbag squib circuit fault."
        ),
        dtc(
            "B0010", ItemSeverity.CRITICAL,
            "Circuit d'allumeur de l'airbag frontal passager",
            "خلل في دائرة وسادة الهواء (Airbag) للراكب الأمامي",
            "Passenger Frontal Stage 1 Deployment Control",
            "Airbag passager en défaut ou prise sous la planche de bord/siège mal connectée.",
            "خلل في إيرباغ الراكب الأمامي أو في الفيشة أسفل الطابلوه/المقعد.",
            "Passenger frontal airbag circuit fault or loose under-dash/seat connector."
        ),
        dtc(
            "U0001", ItemSeverity.CRITICAL,
            "Bus de communication CAN haute vitesse : panne du réseau",
            "خلل في شبكة الاتصال الرئيسية بين كمبيوترات السيارة (CAN Bus)",
            "High Speed CAN Communication Bus",
            "Perturbation électrique sur le réseau multiplexé reliant les calculateurs (faisceau abîmé, humidité ou boîtier mal branché).",
            "تشويش أو قطع في شبكة الأسلاك التي تربط كمبيوترات السيارة ببعضها (بسبب رطوبة أو تلاعب في الضفيرة).",
            "Electrical fault on the high-speed CAN network linking the car's control modules."
        ),
        dtc(
            "U0100", ItemSeverity.CRITICAL,
            "Perte de communication avec le calculateur moteur (ECM/PCM)",
            "انقطاع الاتصال مع كمبيوتر المحرك (ECM/PCM)",
            "Lost Communication With ECM / PCM 'A'",
            "Un autre boîtier a perdu le contact avec le calculateur moteur (coupure d'alimentation, relais principal ou prise oxydée).",
            "فقدت بقية الأنظمة الاتصال بكمبيوتر المحرك بسبب انقطاع التغذية أو تأكسد الفيشة.",
            "Other control modules lost CAN communication with the engine ECU due to a power relay drop or corroded plug."
        ),
        dtc(
            "U0101", ItemSeverity.CRITICAL,
            "Perte de communication avec le calculateur de boîte automatique (TCM)",
            "انقطاع الاتصال مع كمبيوتر علبة السرعة الأوتوماتيكية (Mécatronique / TCM)",
            "Lost Communication With TCM (Transmission Control Module)",
            "Le calculateur de boîte automatique (ou mécatronique DSG) ne répond plus sur le réseau CAN.",
            "كمبيوتر علبة السرعة الأوتوماتيكية (أو وحدة الميكاترونيك) توقف عن الاستجابة.",
            "The automatic transmission control module (or DSG mechatronic) stopped responding on the CAN bus."
        ),
        dtc(
            "U0121", ItemSeverity.IMPORTANT,
            "Perte de communication avec le bloc ABS / ESP",
            "انقطاع الاتصال مع وحدة الفرامل المانعة للانغلاق (ABS / ESP)",
            "Lost Communication With Anti-Lock Brake System (ABS) Control Module",
            "Le calculateur ABS ne répond plus (fusible ABS grillé, prise oxydée ou bloc ABS défaillant).",
            "عدم استجابة كمبيوتر ABS (بسبب احتراق فيوز أو تأكسد الفيشة أو تلف وحدة ABS).",
            "The ABS/ESP module is not communicating on the CAN bus."
        ),
        dtc(
            "U0140", ItemSeverity.IMPORTANT,
            "Perte de communication avec le boîtier habitacle (BSI / UCH / BCM)",
            "انقطاع الاتصال مع علبة التحكم الداخلية (BSI / UCH)",
            "Lost Communication With Body Control Module (BCM / BSI / UCH)",
            "Problème de communication avec le boîtier central habitacle (BSI chez Peugeot/Citroën, UCH chez Renault).",
            "خلل اتصال مع العلبة المركزية للمقصورة (BSI في بيجو أو UCH في رونو).",
            "Communication drop with the central body control module (BSI / UCH / BCM)."
        ),
        dtc(
            "U0151", ItemSeverity.CRITICAL,
            "Perte de communication avec le calculateur d'Airbag (SRS)",
            "انقطاع الاتصال مع كمبيوتر وسائد الهواء (Airbag SRS)",
            "Lost Communication With Restraints Control Module (Airbag SRS)",
            "Le calculateur d'airbag est débranché ou hors service (parfois débranché après un choc avec déclenchement d'airbags).",
            "كمبيوتر الإيرباغ مفصول أو تالف (يفصله البعض أحياناً بعد حادث انفجرت فيه الوسائد الهوائية).",
            "The airbag control module is unplugged or dead (sometimes disconnected after a crash deployment)."
        ),
        dtc(
            "U0155", ItemSeverity.IMPORTANT,
            "Perte de communication avec le combiné d'instruments (Compteur)",
            "انقطاع الاتصال مع لوحة العدادات (تحقق من عدم فك العداد)",
            "Lost Communication With Instrument Panel Cluster (IPC) Control Module",
            "Le compteur a été débranché sous contact ou présente un mauvais contact (indice fréquent de démontage du compteur).",
            "تم فصل لوحة العدادات والسيارة في وضع التشغيل أو يوجد سوء اتصال (قد يدل على فك العداد سابقاً).",
            "The instrument cluster was unplugged with ignition on or has a loose plug (possible sign of cluster removal)."
        ),
        dtc(
            "U0401", ItemSeverity.MINOR,
            "Données invalides reçues du calculateur moteur (ECM/PCM)",
            "استقبال بيانات غير صالحة من كمبيوتر المحرك",
            "Invalid Data Received From ECM / PCM 'A'",
            "Code compagnon : le calculateur moteur a signalé un défaut qui désactive temporairement l'ESP ou le régulateur.",
            "كود مرافق يظهر عندما يسجل المحرك عطلاً فيوقف نظام التوازن ESP أو مثبت السرعة مؤقتاً.",
            "Companion code logged when an engine fault temporarily disables ESP or cruise control."
        )
    )

    /**
     * Generates a standard generic SAE J2012 title in 3 languages (FR, AR, EN) for any standard
     * generic fault code not already in the 150+ detailed dictionary, while keeping the honest
     * Phase 2 instruction ("Ask a mechanic what this code means on this car") and 0 cost.
     */
    fun getStandardGenericTitles(cleanCode: String): Triple<String, String, String> {
        if (cleanCode.length != 5) {
            return Triple(
                "Code défaut calculateur ($cleanCode)",
                "كود عطل مسجل في الكمبيوتر ($cleanCode)",
                "Control Unit Fault Code ($cleanCode)"
            )
        }
        val family = cleanCode[0]
        val typeDigit = cleanCode[1]
        val subsystem = cleanCode[2]

        val isManufacturerSpecific = typeDigit == '1' || typeDigit == '3'
        val stdLabelFr = if (isManufacturerSpecific) "Code constructeur" else "Code standard"
        val stdLabelAr = if (isManufacturerSpecific) "كود خاص بالصانع" else "كود قياسي"
        val stdLabelEn = if (isManufacturerSpecific) "Manufacturer-specific code" else "Standard generic code"

        return when (family) {
            'P' -> when (subsystem) {
                '0', '1', '2' -> Triple(
                    "$stdLabelFr dosage air/carburant et injection ($cleanCode)",
                    "$stdLabelAr في نظام الوقود والهواء والحقن ($cleanCode)",
                    "$stdLabelEn — Fuel and Air Metering / Injection ($cleanCode)"
                )
                '3' -> Triple(
                    "$stdLabelFr système d'allumage ou ratés de combustion ($cleanCode)",
                    "$stdLabelAr في نظام الإشعال أو الاحتراق ($cleanCode)",
                    "$stdLabelEn — Ignition System or Misfire ($cleanCode)"
                )
                '4' -> Triple(
                    "$stdLabelFr contrôle antipollution auxiliaire (EGR / Catalyseur / FAP) ($cleanCode)",
                    "$stdLabelAr في نظام البيئة والانبعاثات ($cleanCode)",
                    "$stdLabelEn — Auxiliary Emission Controls ($cleanCode)"
                )
                '5' -> Triple(
                    "$stdLabelFr vitesse véhicule, ralenti et entrées auxiliaires ($cleanCode)",
                    "$stdLabelAr في سرعة السيارة والسلانسيه والمداخل المساعدة ($cleanCode)",
                    "$stdLabelEn — Vehicle Speed, Idle Control & Auxiliary Inputs ($cleanCode)"
                )
                '6' -> Triple(
                    "$stdLabelFr calculateur moteur et sorties électriques ($cleanCode)",
                    "$stdLabelAr في كمبيوتر المحرك والدوائر الكهربائية ($cleanCode)",
                    "$stdLabelEn — Computer & Auxiliary Outputs ($cleanCode)"
                )
                '7', '8', '9', 'A' -> Triple(
                    "$stdLabelFr transmission / boîte de vitesses ($cleanCode)",
                    "$stdLabelAr في علبة السرعة ونقل الحركة ($cleanCode)",
                    "$stdLabelEn — Transmission / Gearbox System ($cleanCode)"
                )
                else -> Triple(
                    "$stdLabelFr groupe motopropulseur ($cleanCode)",
                    "$stdLabelAr في المحرك ونقل الحركة ($cleanCode)",
                    "$stdLabelEn — Powertrain System ($cleanCode)"
                )
            }
            'C' -> Triple(
                "$stdLabelFr châssis / freinage ABS-ESP / direction ($cleanCode)",
                "$stdLabelAr في نظام الشاسيه والفرامل ABS/ESP والمقود ($cleanCode)",
                "$stdLabelEn — Chassis / ABS-ESP / Steering System ($cleanCode)"
            )
            'B' -> Triple(
                "$stdLabelFr habitacle / airbags / confort ($cleanCode)",
                "$stdLabelAr في أنظمة المقصورة والوسائد الهوائية Airbag ($cleanCode)",
                "$stdLabelEn — Body / Airbags / Interior Electronics ($cleanCode)"
            )
            'U' -> Triple(
                "$stdLabelFr communication réseau multiplexé CAN ($cleanCode)",
                "$stdLabelAr في شبكة الاتصال بين كمبيوترات السيارة CAN ($cleanCode)",
                "$stdLabelEn — Network & Module Communication ($cleanCode)"
            )
            else -> Triple(
                "Code défaut calculateur ($cleanCode)",
                "كود عطل مسجل في الكمبيوتر ($cleanCode)",
                "Control Unit Fault Code ($cleanCode)"
            )
        }
    }
}
