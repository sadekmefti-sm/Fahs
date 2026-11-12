package com.example.domain

import com.example.data.model.ItemSeverity
import com.example.data.model.VerificationStatus

/**
 * Phase 4 Expansion (Part 3): Common OBD-II Fault Codes (P0200 - P0499)
 * covering injectors, turbocharger boost control, knocking, glow plugs, EGR, catalyst & EVAP
 * in French, Arabic, and English.
 */
object ObdDtcDictionaryPart3 {

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
            "P0200", ItemSeverity.CRITICAL,
            "Défaut général du circuit des injecteurs",
            "عطل عام في الدائرة الكهربائية للحاقنات (Injecteurs)",
            "Injector Circuit / Open",
            "Problème d'alimentation électrique des injecteurs dans le faisceau moteur ou le calculateur.",
            "مشكلة في التغذية الكهربائية للحاقنات داخل ضفيرة المحرك أو الكمبيوتر.",
            "Electrical supply fault to the fuel injectors in the engine harness or ECU driver."
        ),
        dtc(
            "P0217", ItemSeverity.CRITICAL,
            "Condition de surchauffe du moteur détectée",
            "تسجيل حالة ارتفاع مفرط في حرارة المحرك (سخانة!)",
            "Engine Coolant Over Temperature Condition",
            "Le calculateur a enregistré une surchauffe critique du moteur. Vérifiez impérativement le joint de culasse et le vase d'expansion.",
            "سجل الكمبيوتر وصول المحرك لدرجة حرارة خطيرة. افحص جوان الكولاس وقارورة الماء بدقة شديدة.",
            "The ECU recorded a severe engine overheating event. Inspect the head gasket and coolant reservoir carefully."
        ),
        dtc(
            "P0219", ItemSeverity.IMPORTANT,
            "Condition de surrégime moteur enregistrée",
            "تسجيل تجاوز السرعة القصوى لدوران المحرك (Surrégime)",
            "Engine Overspeed Condition",
            "Le moteur a été poussé au-delà de la zone rouge (rétrogradage brutal ou conduite abusive).",
            "تعرض المحرك لدوران فوق الخط الأحمر (نتيجة غيار عكسي عنيف أو قيادة قاسية).",
            "The engine was revved past the redline RPM limit (often from a money-shift downshift or hard driving)."
        ),
        dtc(
            "P0220", ItemSeverity.IMPORTANT,
            "Capteur de position de pédale d'accélérateur / papillon 'B'",
            "خلل في المسار الثاني لحساس دعسة الوقود أو البوابة 'B'",
            "Throttle / Pedal Position Sensor / Switch 'B' Circuit",
            "Le second circuit de sécurité de la pédale d'accélérateur ou du papillon ne concorde pas avec le premier.",
            "المسار الكهربائي الثاني لدعسة الوقود لا يتطابق مع المسار الأول فيدخل المحرك وضع الأمان.",
            "The secondary safety track of the accelerator pedal or throttle sensor disagrees with the primary track."
        ),
        dtc(
            "P0230", ItemSeverity.CRITICAL,
            "Circuit primaire de la pompe à carburant (Relais / Pompe de gavage)",
            "عطل في دائرة مضخة الوقود الكهربائية أو الريلاي (Pompe de gavage)",
            "Fuel Pump Primary Circuit",
            "Le relais de pompe à essence/gavage ou la pompe dans le réservoir présente des coupures électriques.",
            "تقطع كهربائي في ريلاي مضخة الوقود أو المضخة الموجودة داخل الخزان.",
            "The fuel pump relay or in-tank lift pump has an intermittent electrical circuit fault."
        ),
        dtc(
            "P0235", ItemSeverity.IMPORTANT,
            "Capteur de pression de suralimentation du turbo 'A' : panne du circuit",
            "عطل في حساس ضغط التيربو (Capteur de suralimentation)",
            "Turbocharger / Supercharger Boost Sensor 'A' Circuit",
            "Le capteur qui mesure la pression du turbo (sur l'échangeur ou la tubulure) est débranché ou HS.",
            "الحساس الذي يقيس ضغط هواء التيربو مفصول أو تالف.",
            "The turbo boost pressure sensor on the intercooler pipe or manifold is unplugged or faulty."
        ),
        dtc(
            "P0236", ItemSeverity.IMPORTANT,
            "Capteur de pression de suralimentation du turbo 'A' : plage/performance",
            "قراءة غير متطابقة من حساس ضغط التيربو",
            "Turbocharger / Supercharger Boost Sensor 'A' Circuit Range/Performance",
            "La pression mesurée par le capteur de turbo est incohérente avec la pression atmosphérique.",
            "قراءة حساس ضغط التيربو غير متطابقة مع الضغط الجوي.",
            "Boost pressure sensor reading does not correlate with barometric pressure at key-on."
        ),
        dtc(
            "P0237", ItemSeverity.IMPORTANT,
            "Capteur de pression du turbo 'A' : signal trop bas",
            "إشارة منخفضة من حساس ضغط التيربو",
            "Turbocharger / Supercharger Boost Sensor 'A' Circuit Low",
            "Fil coupé ou court-circuit sur la prise du capteur de pression turbo.",
            "قطع في السلك أو تماس في فيشة حساس ضغط التيربو.",
            "Broken wire or short to ground on the turbo boost sensor connector."
        ),
        dtc(
            "P0238", ItemSeverity.IMPORTANT,
            "Capteur de pression du turbo 'A' : signal trop haut",
            "إشارة مرتفعة جداً من حساس ضغط التيربو",
            "Turbocharger / Supercharger Boost Sensor 'A' Circuit High",
            "Surtension sur le capteur de suralimentation ou électrovanne de turbo bloquée.",
            "ارتفاع مفرط في إشارة حساس التيربو أو تعلق صمام التحكم.",
            "High voltage on the boost pressure sensor or stuck turbo control solenoid."
        ),
        dtc(
            "P0243", ItemSeverity.IMPORTANT,
            "Électrovanne de décharge du turbo (Wastegate / N75) : panne",
            "عطل في الصمام الكهربائي للتيربو (Électrovanne N75 / Wastegate)",
            "Turbocharger / Supercharger Wastegate Solenoid 'A'",
            "L'électrovanne qui pilote le turbo est coupée ou débranchée : la voiture roule sans turbo (comme un moteur atmosphérique).",
            "الصمام الكهربائي الذي يتحكم في التيربو معطل أو مفصول، فتفقد السيارة عزم التيربو تماماً.",
            "The solenoid controlling the turbo wastegate/VNT is open-circuit, leaving the engine with no turbo boost."
        ),
        dtc(
            "P0263", ItemSeverity.IMPORTANT,
            "Cylindre n°1 : défaut d'équilibrage / contribution (Injecteur ou compression)",
            "ضعف مساهمة السيلندر رقم 1 (خلل في الحاقن أو ضغط الغرفة)",
            "Cylinder 1 Contribution / Balance",
            "Le cylindre 1 fournit moins de force que les autres : injecteur diesel fatigué ou baisse de compression.",
            "السيلندر رقم 1 يعطي قوة أقل من البقية بسبب ضعف الحاقن أو نقص ضغط الغرفة.",
            "Cylinder 1 is contributing less torque than the others due to a weak diesel injector or low compression."
        ),
        dtc(
            "P0266", ItemSeverity.IMPORTANT,
            "Cylindre n°2 : défaut d'équilibrage / contribution",
            "ضعف مساهمة السيلندر رقم 2 (تصحيح الحاقن مرتفع)",
            "Cylinder 2 Contribution / Balance",
            "Correction d'injection hors tolérance sur le cylindre 2.",
            "نسبة تصحيح الحاقن رقم 2 خارج المجال الطبيعي.",
            "Injector correction or cylinder balance out of tolerance on cylinder 2."
        ),
        dtc(
            "P0269", ItemSeverity.IMPORTANT,
            "Cylindre n°3 : défaut d'équilibrage / contribution",
            "ضعف مساهمة السيلندر رقم 3",
            "Cylinder 3 Contribution / Balance",
            "Correction d'injection hors tolérance sur le cylindre 3.",
            "نسبة تصحيح الحاقن رقم 3 خارج المجال الطبيعي.",
            "Injector correction or cylinder balance out of tolerance on cylinder 3."
        ),
        dtc(
            "P0272", ItemSeverity.IMPORTANT,
            "Cylindre n°4 : défaut d'équilibrage / contribution",
            "ضعف مساهمة السيلندر رقم 4",
            "Cylinder 4 Contribution / Balance",
            "Correction d'injection hors tolérance sur le cylindre 4.",
            "نسبة تصحيح الحاقن رقم 4 خارج المجال الطبيعي.",
            "Injector correction or cylinder balance out of tolerance on cylinder 4."
        ),
        dtc(
            "P0313", ItemSeverity.MINOR,
            "Ratés d'allumage détectés avec un niveau de carburant très bas",
            "تقطيع في المحرك بسبب نفاد الوقود أو انخفاضه الشديد",
            "Misfire Detected with Low Fuel",
            "La pompe a aspiré de l'air parce que le véhicule a roulé sur la réserve.",
            "سحبت المضخة الهواء بسبب السير والخزان شبه فارغ.",
            "Air was drawn into the fuel system while driving on an almost empty tank."
        ),
        dtc(
            "P0321", ItemSeverity.IMPORTANT,
            "Capteur de régime moteur (PMH) : signal irrégulier",
            "تقطع في إشارة حساس الكرنك (Capteur PMH)",
            "Ignition / Distributor Engine Speed Input Circuit Range/Performance",
            "Coupures brèves du signal de vilebrequin causant des à-coups ou un compte-tours qui tombe à zéro.",
            "انقطاعات لحظية في إشارة حساس الكرنك تسبب نتعات أو سقوط مؤشر RPM للصفر.",
            "Intermittent crankshaft speed signal dropouts causing bucking or tachometer drops."
        ),
        dtc(
            "P0322", ItemSeverity.IMPORTANT,
            "Capteur de régime moteur (PMH) : absence de signal",
            "انعدام الإشارة من حساس الكرنك (لا يشتغل المحرك)",
            "Ignition / Distributor Engine Speed Input Circuit No Signal",
            "Le calculateur ne voit pas le moteur tourner au démarreur à cause du capteur PMH HS.",
            "الكمبيوتر لا يستشعر دوران المحرك عند التشغيل بسبب تلف حساس PMH.",
            "The ECU detects no engine rotation during cranking due to a failed crank sensor."
        ),
        dtc(
            "P0325", ItemSeverity.MINOR,
            "Capteur de cliquetis 1 (Knock Sensor) : panne du circuit",
            "عطل في حساس الطرق / الصرقعة (Capteur de cliquetis)",
            "Knock Sensor 1 Circuit (Bank 1 or Single Sensor)",
            "Le capteur vissé sur le bloc moteur qui détecte le cliquetis (essence de mauvaise qualité) est débranché ou HS.",
            "الحساس المثبت على جسم المحرك لكشف الصرقعة مفصول أو تالف.",
            "The knock sensor bolted to the engine block is unplugged or faulty, reducing ignition advance."
        ),
        dtc(
            "P0336", ItemSeverity.IMPORTANT,
            "Capteur de position du vilebrequin 'A' : plage ou cible endommagée",
            "خلل في قراءة حساس الكرنك أو ترس الإشارة",
            "Crankshaft Position Sensor 'A' Circuit Range/Performance",
            "Limaille sur le capteur PMH ou couronne cible du volant moteur abîmée.",
            "تراكم برادة الحديد على حساس الكرنك أو تضرر أسنان الحذافة.",
            "Metal debris on the crank sensor tip or damaged flywheel tone ring teeth."
        ),
        dtc(
            "P0341", ItemSeverity.IMPORTANT,
            "Capteur d'arbre à cames 'A' : signal incohérent (Vérifier la distribution)",
            "إشارة غير منتظمة من حساس الكامات (افحص سلسلة التوزيع)",
            "Camshaft Position Sensor 'A' Circuit Range/Performance",
            "Le signal d'arbre à cames est instable : capteur défaillant ou chaîne de distribution détendue.",
            "إشارة حساس الكامات غير مستقرة بسبب ضعف الحساس أو ارتخاء سلسلة التوزيع.",
            "Erratic camshaft position signal caused by a failing sensor or stretched timing chain."
        ),
        dtc(
            "P0351", ItemSeverity.IMPORTANT,
            "Bobine d'allumage 'A' (Cylindre 1) : défaut du circuit primaire/secondaire",
            "عطل في البوبينة (Bobine d'allumage) رقم 1",
            "Ignition Coil 'A' Primary / Secondary Circuit",
            "La bobine d'allumage du cylindre 1 est coupée ou sa prise est mal verrouillée.",
            "بوبينة الإشعال للسيلندر رقم 1 تالفة أو فيشتها غير محكمة.",
            "Ignition coil #1 has a primary/secondary electrical fault or loose connector."
        ),
        dtc(
            "P0352", ItemSeverity.IMPORTANT,
            "Bobine d'allumage 'B' (Cylindre 2) : défaut du circuit",
            "عطل في البوبينة (Bobine d'allumage) رقم 2",
            "Ignition Coil 'B' Primary / Secondary Circuit",
            "La bobine d'allumage du cylindre 2 est en défaut électrique.",
            "خلل كهربائي في بوبينة الإشعال للسيلندر رقم 2.",
            "Ignition coil #2 has a primary/secondary electrical fault."
        ),
        dtc(
            "P0353", ItemSeverity.IMPORTANT,
            "Bobine d'allumage 'C' (Cylindre 3) : défaut du circuit",
            "عطل في البوبينة (Bobine d'allumage) رقم 3",
            "Ignition Coil 'C' Primary / Secondary Circuit",
            "La bobine d'allumage du cylindre 3 est en défaut électrique.",
            "خلل كهربائي في بوبينة الإشعال للسيلندر رقم 3.",
            "Ignition coil #3 has a primary/secondary electrical fault."
        ),
        dtc(
            "P0354", ItemSeverity.IMPORTANT,
            "Bobine d'allumage 'D' (Cylindre 4) : défaut du circuit",
            "عطل في البوبينة (Bobine d'allumage) رقم 4",
            "Ignition Coil 'D' Primary / Secondary Circuit",
            "La bobine d'allumage du cylindre 4 est en défaut électrique.",
            "خلل كهربائي في بوبينة الإشعال للسيلندر رقم 4.",
            "Ignition coil #4 has a primary/secondary electrical fault."
        ),
        dtc(
            "P0400", ItemSeverity.IMPORTANT,
            "Dysfonctionnement du débit de recyclage des gaz d'échappement (EGR)",
            "خلل عام في نظام استرجاع غازات العادم (EGR)",
            "Exhaust Gas Recirculation (EGR) Flow",
            "La vanne EGR est grippée par la suie ou a été condamnée mécaniquement sans reprogrammation.",
            "صمام EGR عالق بسبب الكربون أو تم سده بصفحة معدنية دون تعديل البرمجة.",
            "The EGR valve is stuck with carbon soot or blocked off with a blanking plate."
        ),
        dtc(
            "P0402", ItemSeverity.IMPORTANT,
            "Débit EGR excessif (Vanne EGR bloquée ouverte)",
            "تدفق مفرط لغازات EGR (الصمام عالق في وضع الفتح)",
            "Exhaust Gas Recirculation (EGR) Flow Excessive Detected",
            "La vanne EGR ne se referme plus complètement : le moteur étouffe à bas régime et fume noir.",
            "صمام EGR لا يغلق بالكامل، مما يخنق المحرك في السرعات المنخفضة ويخرج دخاناً أسود.",
            "The EGR valve cannot close fully, choking the engine at low RPM and producing black smoke."
        ),
        dtc(
            "P0403", ItemSeverity.IMPORTANT,
            "Commande électrique de la vanne EGR : panne du circuit",
            "عطل كهربائي في محرك صمام EGR (أو الفيشة مفصولة)",
            "Exhaust Gas Recirculation (EGR) Control Circuit",
            "La prise électrique de la vanne EGR est débranchée ou son moteur interne est grillé.",
            "فيشة صمام EGR مفصولة أو محركه الكهربائي الداخلي محترق.",
            "The EGR valve electrical connector is unplugged or its internal actuator motor has burned out."
        ),
        dtc(
            "P0404", ItemSeverity.IMPORTANT,
            "Vanne EGR : plage ou performance de position",
            "عدم تطابق موضع صمام EGR بسبب تراكم الكربون",
            "Exhaust Gas Recirculation (EGR) Control Circuit Range/Performance",
            "La vanne EGR peine à atteindre la position demandée à cause de la calamine.",
            "صمام EGR يجد صعوبة في التحرك بسبب تراكم الكربون الكثيف بداخله.",
            "The EGR pintle struggles to reach its commanded position due to heavy carbon buildup."
        ),
        dtc(
            "P0405", ItemSeverity.IMPORTANT,
            "Capteur de position de la vanne EGR 'A' : signal trop bas",
            "إشارة منخفضة من حساس موضع صمام EGR",
            "Exhaust Gas Recirculation Sensor 'A' Circuit Low",
            "Recopie de position de la vanne EGR défaillante.",
            "تلف حساس قراءة موضع صمام EGR.",
            "EGR position feedback sensor circuit low."
        ),
        dtc(
            "P0409", ItemSeverity.IMPORTANT,
            "Capteur de position de la vanne EGR 'A' : panne du circuit",
            "عطل في حساس موضع صمام EGR",
            "Exhaust Gas Recirculation Sensor 'A' Circuit",
            "La partie électronique de la vanne EGR est hors service (très fréquent sur 1.5 dCi et 1.6 HDi/TDI).",
            "تلف الجزء الإلكتروني من صمام EGR (شائع في محركات 1.5 dCi و 1.6 HDi/TDI).",
            "Electronic position sensor inside the EGR valve assembly has failed."
        ),
        dtc(
            "P0440", ItemSeverity.MINOR,
            "Système de recyclage des vapeurs de carburant (EVAP / Canister)",
            "خلل في نظام تدوير أبخرة البنزين (Canister / EVAP)",
            "Evaporative Emission System",
            "Bouchon de réservoir mal fermé ou circuit de vapeurs d'essence fuyard.",
            "غطاء خزان البنزين غير محكم الإغلاق أو تسرب في أنابيب علبة الفحم (Canister).",
            "Loose fuel filler cap or leak in the charcoal canister vapor recovery lines."
        ),
        dtc(
            "P0441", ItemSeverity.MINOR,
            "Système EVAP : débit de purge des vapeurs d'essence incorrect",
            "خلل في صمام تصريف أبخرة البنزين (Électrovanne Canister)",
            "Evaporative Emission System Incorrect Purge Flow",
            "L'électrovanne de purge canister claque ou reste ouverte, causant parfois une odeur d'essence.",
            "صمام تصريف أبخرة البنزين عالق، مما قد يسبب رائحة بنزين أو تذبذباً بسيطاً في السلانسيه.",
            "The canister purge solenoid valve is stuck open or closed."
        ),
        dtc(
            "P0442", ItemSeverity.MINOR,
            "Système EVAP : petite fuite de vapeurs détectée",
            "تسرب بسيط في نظام أبخرة خزان الوقود",
            "Evaporative Emission System Leak Detected (Small Leak)",
            "Joint de bouchon de réservoir usé ou petite durite de vapeurs fendue.",
            "تآكل جلدة غطاء الخزان أو تشقق أنبوب صغير لأبخرة الوقود.",
            "Worn fuel cap seal or small cracked vapor hose."
        ),
        dtc(
            "P0443", ItemSeverity.MINOR,
            "Électrovanne de purge canister (EVAP) : panne du circuit",
            "عطل كهربائي في صمام أبخرة البنزين (Canister)",
            "Evaporative Emission System Purge Control Valve Circuit",
            "Électrovanne canister débranchée ou coupée.",
            "صمام علبة الفحم مفصول أو تالف كهربائياً.",
            "Canister purge control solenoid unplugged or open-circuit."
        ),
        dtc(
            "P0455", ItemSeverity.MINOR,
            "Système EVAP : fuite importante détectée (Bouchon de réservoir)",
            "تسرب كبير في أبخرة الوقود (تأكد من غطاء الخزان)",
            "Evaporative Emission System Leak Detected (Large Leak)",
            "Bouchon de réservoir absent/non étanche ou durite du canister débranchée.",
            "غطاء خزان البنزين غير محكم أو أنبوب علبة الفحم مفصول.",
            "Missing/loose fuel filler cap or disconnected canister purge hose."
        ),
        dtc(
            "P0460", ItemSeverity.MINOR,
            "Capteur de niveau de carburant 'A' (Jauge du réservoir) : panne",
            "خلل في عوامة خزان الوقود (Jauge à carburant)",
            "Fuel Level Sensor 'A' Circuit",
            "La jauge dans le réservoir envoie un niveau instable (fréquent sur les voitures roulant au GPL).",
            "عوامة الخزان تعطي قراءة غير مستقرة لمؤشر البنزين.",
            "The in-tank fuel level sender is giving erratic gauge readings."
        ),
        dtc(
            "P0480", ItemSeverity.IMPORTANT,
            "Commande du ventilateur de refroidissement 1 (Petite vitesse) : panne",
            "عطل في تشغيل مروحة التبريد (السرعة الأولى أو المقاومة)",
            "Fan 1 Control Circuit",
            "La résistance ou le relais de première vitesse du ventilateur moteur est grillé. Risque de chauffe dans les embouteillages.",
            "احتراق مقاومة أو ريلاي السرعة الأولى لمروحة الراديتر، مما قد يسبب ارتفاع الحرارة في الازدحام.",
            "The low-speed radiator fan resistor or relay has failed, risking overheating in traffic."
        ),
        dtc(
            "P0481", ItemSeverity.CRITICAL,
            "Commande du ventilateur de refroidissement 2 (Grande vitesse) : panne",
            "عطل في السرعة القصوى لمروحة تبريد المحرك",
            "Fan 2 Control Circuit",
            "Le relais ou le moteur de grande vitesse du ventilateur ne fonctionne plus. Risque sérieux de surchauffe en été ou avec la clim.",
            "تعطل السرعة العالية لمروحة التبريد. خطر حقيقي لارتفاع حرارة المحرك صيفاً أو مع المكيف.",
            "The high-speed cooling fan relay or motor circuit is faulty. High overheating risk in summer."
        ),
        dtc(
            "P0488", ItemSeverity.IMPORTANT,
            "Commande du papillon EGR : performance ou blocage",
            "تعلق أو اتساخ خانق EGR (Boîtier doseur d'air)",
            "Exhaust Gas Recirculation Throttle Control Circuit 'A' Range/Performance",
            "Le volet doseur d'air associé à l'EGR grippe à cause des vapeurs d'huile et de suie.",
            "خانق الهواء المرتبط بنظام EGR عالق بسبب تراكم الزيوت والكربون.",
            "The EGR air intake throttle flap is sticking due to oil and soot accumulation."
        ),
        dtc(
            "P0489", ItemSeverity.IMPORTANT,
            "Commande de la vanne EGR : tension trop basse (Court-circuit)",
            "انخفاض الجهد في صمام EGR",
            "Exhaust Gas Recirculation Control Circuit Low",
            "Court-circuit ou vanne EGR défaillante.",
            "تماس كهربائي أو تلف في صمام EGR.",
            "Short circuit to ground or failed EGR valve actuator."
        ),
        dtc(
            "P0490", ItemSeverity.IMPORTANT,
            "Commande de la vanne EGR : tension trop haute",
            "ارتفاع الجهد في صمام EGR (أو انسداد ميكانيكي)",
            "Exhaust Gas Recirculation Control Circuit High",
            "La vanne EGR force électriquement car son mécanisme est bloqué par la calamine.",
            "محرك صمام EGR يستهلك تياراً عالياً لأن الصمام عالق ميكانيكياً بالكربون.",
            "The EGR actuator draws high voltage/current because the valve mechanism is jammed with carbon."
        )
    )
}
