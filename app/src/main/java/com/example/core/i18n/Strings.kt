package com.example.core.i18n

object Strings {
    fun appName(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "فحص"
        AppLanguage.FR -> "Fahs"
        AppLanguage.EN -> "Fahs"
    }

    fun appTagline(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "دليلك الذكي لفحص السيارات المستعملة في الجزائر"
        AppLanguage.FR -> "Votre guide d'inspection automobile d'occasion en Algérie"
        AppLanguage.EN -> "Used car inspection assistant in Algeria"
    }

    // Navigation (4 bottom tabs: Home | My reports | Guide | Account)
    fun navHome(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "الرئيسية"
        AppLanguage.FR -> "Accueil"
        AppLanguage.EN -> "Home"
    }

    fun navInspections(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "تقاريري"
        AppLanguage.FR -> "Mes rapports"
        AppLanguage.EN -> "My reports"
    }

    fun navObd(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "الفحص الأوتوماتيكي"
        AppLanguage.FR -> "Scan automatique"
        AppLanguage.EN -> "Automatic scan"
    }

    fun navGuide(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "الدليل"
        AppLanguage.FR -> "Guide"
        AppLanguage.EN -> "Guide"
    }

    fun navAccount(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "حسابي"
        AppLanguage.FR -> "Compte"
        AppLanguage.EN -> "Account"
    }

    // Home Screen
    fun startInspectionTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "ابدأ فحص سيارة جديدة"
        AppLanguage.FR -> "Inspecter un véhicule"
        AppLanguage.EN -> "Inspect a Vehicle"
    }

    fun startInspectionDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "فحص ميكانيكي وهيكلي متكيف مع نوع السيارة والبطاقة الرمادية وجهاز OBD2"
        AppLanguage.FR -> "Contrôle physique adapté, vérification carte grise et scan diagnostic OBD2"
        AppLanguage.EN -> "Adaptive physical check, carte grise verification, and OBD2 diagnostic scan"
    }

    fun quickCheck(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "فحص سريع (~10 دقائق)"
        AppLanguage.FR -> "Contrôle rapide (~10 min)"
        AppLanguage.EN -> "Quick Check (~10 min)"
    }

    fun quickCheckDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "أهم النقاط الحرجة قبل إضاعة الوقت في المعاينة"
        AppLanguage.FR -> "Les points les plus critiques avant d'aller plus loin"
        AppLanguage.EN -> "The most critical points before spending more time"
    }

    fun fullInspection(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "فحص شامل (~40 دقيقة)"
        AppLanguage.FR -> "Inspection complète (~40 min)"
        AppLanguage.EN -> "Full Inspection (~40 min)"
    }

    fun fullInspectionDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "فحص دقيق لكافة أجزاء السيارة، الهيكل، المحرك، والقيادة التجريبية"
        AppLanguage.FR -> "Vérification exhaustive de la carrosserie, moteur, essai routier et OBD"
        AppLanguage.EN -> "Comprehensive check of bodywork, engine, test drive, and OBD"
    }

    fun offlineBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "يعمل 100% بدون إنترنت"
        AppLanguage.FR -> "Fonctionne 100% hors-ligne"
        AppLanguage.EN -> "Works 100% Offline"
    }

    fun noAccountNeeded(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "بدون حساب أو إعلانات"
        AppLanguage.FR -> "Sans compte ni publicité"
        AppLanguage.EN -> "No Account or Ads Needed"
    }

    fun algerianAdapted(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "مخصص للسوق الجزائري (بطاقة رمادية، غاز سيرغاز GPL)"
        AppLanguage.FR -> "Adapté au marché algérien (carte grise, dossier GPL)"
        AppLanguage.EN -> "Adapted to Algeria (carte grise, GPL installation)"
    }

    fun myInspections(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "فحوصاتي المحفوظة"
        AppLanguage.FR -> "Mes inspections"
        AppLanguage.EN -> "My Saved Inspections"
    }

    fun viewAll(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "عرض الكل"
        AppLanguage.FR -> "Voir tout"
        AppLanguage.EN -> "View all"
    }

    fun noInspectionsYet(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "لا توجد فحوصات محفوظة بعد"
        AppLanguage.FR -> "Aucune inspection enregistrée pour le moment"
        AppLanguage.EN -> "No inspections saved yet"
    }

    fun noInspectionsDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "ابدأ فحصك الأول عند معاينة أي سيارة وسيتكفل التطبيق بحفظ تقدمك تلقائياً."
        AppLanguage.FR -> "Lancez votre première inspection lors de la visite d'un véhicule. Tout s'enregistre automatiquement."
        AppLanguage.EN -> "Start your first inspection during a car visit. Everything is saved automatically."
    }

    // OBD Tools & Guide
    fun obdGuideTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "دليل جهاز الفحص ELM327"
        AppLanguage.FR -> "Guide du boîtier OBD2 ELM327"
        AppLanguage.EN -> "ELM327 OBD2 Dongle Guide"
    }

    fun obdGuideDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "كيفية توصيل جهاز بلوتوث رخيص لكشف الأعطال المخفية ومسح الأكواد الحديث"
        AppLanguage.FR -> "Comment connecter un boîtier Bluetooth économique pour déceler les défauts cachés"
        AppLanguage.EN -> "How to connect a cheap Bluetooth dongle to detect hidden faults and cleared codes"
    }

    fun obdSimulatorTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "محاكي الفحص المدمج"
        AppLanguage.FR -> "Simulateur OBD intégré"
        AppLanguage.EN -> "Built-in OBD Simulator"
    }

    fun obdSimulatorDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "جرب فحص سيارة سليمة، أو سيارة بأعطال، أو حالة مسح حديث للأكواد بدون جهاز"
        AppLanguage.FR -> "Testez une voiture saine, en panne ou avec codes récemment effacés sans matériel"
        AppLanguage.EN -> "Test a healthy car, faulty car, or cleared-codes alert without hardware"
    }

    // Algerian Buyer Tips
    fun buyerTipsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "نصائح لشراء سيارة في الجزائر"
        AppLanguage.FR -> "Conseils d'achat en Algérie"
        AppLanguage.EN -> "Buying Used Cars in Algeria"
    }

    fun tip1Title(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "تطابق البطاقة الرمادية والـ VIN"
        AppLanguage.FR -> "Concordance Carte Grise et VIN"
        AppLanguage.EN -> "Carte Grise & VIN Match"
    }

    fun tip1Desc(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "قارن رقم الهيكل (VIN) على صاج السيارة، لوحة الصانع، والبطاقة الرمادية الأصلية مع بطاقة هوية البائع."
        AppLanguage.FR -> "Vérifiez que le numéro de châssis frappé correspond à la plaque constructeur et à la carte grise originale."
        AppLanguage.EN -> "Compare the stamped chassis VIN with the manufacturer plate and original carte grise."
    }

    fun tip2Title(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "فحص المحرك وهو بارد تماماً"
        AppLanguage.FR -> "Contrôle moteur à froid impératif"
        AppLanguage.EN -> "Always inspect a cold engine"
    }

    fun tip2Desc(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "اطلب من البائع عدم تسخين المحرك قبل وصولك لرؤية صعوبة التشغيل أو خروج الدخان الملون."
        AppLanguage.FR -> "Exigez que le moteur soit froid à votre arrivée pour observer le démarrage et la couleur des fumées."
        AppLanguage.EN -> "Ask the seller not to warm up the engine before you arrive to observe cold start behavior."
    }

    fun tip3Title(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "كشف مسح الأعطال الاحتيالي"
        AppLanguage.FR -> "Détection de l'effacement de défauts"
        AppLanguage.EN -> "Detecting erased fault codes"
    }

    fun tip3Desc(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "يقوم بعض الباعة بمسح لمبة المحرك Check Engine قبل البيع مباشرة. فحص الـ OBD يكشف المسافة منذ آخر مسح."
        AppLanguage.FR -> "Certains vendeurs effacent les voyants avant la visite. Notre scan OBD2 vérifie la distance depuis l'effacement."
        AppLanguage.EN -> "Some sellers clear the Check Engine light right before sale. Our OBD scan checks distance since clear."
    }

    fun tip4Title(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "سيارات رخصة مجاهدين (Moudjahid)"
        AppLanguage.FR -> "Véhicules sous licence Moudjahid"
        AppLanguage.EN -> "Moudjahid licence vehicles"
    }

    fun tip4Desc(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "انتبه لشروط رفع اليد أو التنازل القانوني لتفادي الوقوع في مشاكل تجميد الملف في الدائرة."
        AppLanguage.FR -> "Attention aux conditions de mainlevée ou procuration pour éviter le blocage de dossier à la daïra."
        AppLanguage.EN -> "Pay attention to release conditions and legal transfer restrictions at the local daïra."
    }

    // Status & Verdicts
    fun statusInProgress(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "قيد الفحص"
        AppLanguage.FR -> "En cours"
        AppLanguage.EN -> "In Progress"
    }

    fun statusCompleted(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "مكتمل"
        AppLanguage.FR -> "Terminé"
        AppLanguage.EN -> "Completed"
    }

    fun verdictGood(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "حالة جيدة للشراء"
        AppLanguage.FR -> "Favorable à l'achat"
        AppLanguage.EN -> "Good to Buy"
    }

    fun verdictNegotiate(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "تفاوض على السعر أو استشر ميكانيكياً"
        AppLanguage.FR -> "Négocier ou avis mécanicien"
        AppLanguage.EN -> "Negotiate or Mechanic Check"
    }

    fun verdictAvoid(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "تراجع عن الشراء (مخاطر عالية)"
        AppLanguage.FR -> "À éviter (risques majeurs)"
        AppLanguage.EN -> "Walk Away (High Risk)"
    }

    fun selectLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "تغيير لغة التطبيق"
        AppLanguage.FR -> "Changer la langue"
        AppLanguage.EN -> "Change App Language"
    }

    fun cancel(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "إلغاء"
        AppLanguage.FR -> "Annuler"
        AppLanguage.EN -> "Cancel"
    }

    fun confirm(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "تأكيد"
        AppLanguage.FR -> "Confirmer"
        AppLanguage.EN -> "Confirm"
    }

    fun delete(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "حذف"
        AppLanguage.FR -> "Supprimer"
        AppLanguage.EN -> "Delete"
    }

    fun resumeInspection(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "متابعة الفحص"
        AppLanguage.FR -> "Reprendre l'inspection"
        AppLanguage.EN -> "Resume Inspection"
    }

    fun viewReport(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "عرض التقرير"
        AppLanguage.FR -> "Voir le rapport"
        AppLanguage.EN -> "View Report"
    }

    fun deleteConfirmMsg(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "هل أنت متأكد من حذف هذا الفحص نهائياً؟"
        AppLanguage.FR -> "Voulez-vous vraiment supprimer définitivement cette inspection ?"
        AppLanguage.EN -> "Are you sure you want to permanently delete this inspection?"
    }

    fun scoreOutOf100(score: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "$score / 100"
        AppLanguage.FR -> "$score / 100"
        AppLanguage.EN -> "$score / 100"
    }

    fun dzdCurrency(amount: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> "$amount دج"
        AppLanguage.FR -> "$amount DZD"
        AppLanguage.EN -> "$amount DZD"
    }
}
