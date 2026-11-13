# Fahs (فحص) — Google Play Publishing & Production Guide (`dz.fahs.app`)

This step-by-step guide covers everything required to publish **Fahs (`dz.fahs.app`)** on Google Play in **Arabic**, **French**, and **English**.

---

## 1. App Identity & Effect on Firebase / Google Sign-In

- **Application ID (`applicationId`)**: `dz.fahs.app` (configured in `app/build.gradle.kts`).
- **Internal Kotlin Namespace (`namespace`)**: `com.example` (kept as the internal compile namespace for `R` resources and source folders; Google Play, Android devices, and Firebase identify your app strictly by `applicationId = "dz.fahs.app"`).

### Does changing `applicationId` to `dz.fahs.app` affect Firebase & Google Sign-In?
**Yes.** Firebase Authentication (Google Sign-In via Credential Manager) and Cloud Firestore verify two things at runtime:
1. The Android app's **`applicationId`** (`dz.fahs.app`), and
2. The **SHA-1 and SHA-256 certificate fingerprints** of the key that signed the APK/AAB.

Before publishing to Google Play, complete these 4 steps in the [Firebase Console](https://console.firebase.google.com/):
1. Open your Firebase project (`spheric-ray-467416-n4` or your production Firebase project) -> **Project Settings** -> **General** -> **Your apps**.
2. Click **Add app** -> **Android** and enter package name: **`dz.fahs.app`**.
3. Add **both** sets of signing certificate fingerprints:
   - Your **Upload Key** SHA-1 and SHA-256 (from your release keystore: `keytool -list -v -keystore my-upload-key.jks -alias upload`).
   - Your **Google Play App Signing Key** SHA-1 and SHA-256 (copied from **Google Play Console -> Test and release -> Setup -> App signing**).
4. Download the updated `google-services.json` from Firebase Console and replace `app/google-services.json` before building your final release `.aab`.

---

## 2. Google Play Store Listing Texts (3 Languages)

> **Policy Note:** Google Play titles must be **30 characters or fewer**, with no emojis or promotional buzzwords ("Best", "Free", "#1"). Short descriptions must be **80 characters or fewer**.

### A. Arabic (`ar`) — العربية
- **App Title (28 chars):**
  `فحص - معاينة سيارات مستعملة`
- **Short Description (75 chars):**
  `مساعدك الميداني لفحص السيارات المستعملة في الجزائر وقراءة OBD2 وتقرير PDF.`
- **Full Description:**
  ```text
  تطبيق «فحص» (Fahs) هو مساعدك الميداني خطوة بخطوة لمعاينة وفحص أي سيارة مستعملة في الجزائر قبل الشراء، باللغات العربية والفرنسية والإنجليزية، وبدون الحاجة للاتصال بالإنترنت.

  المزايا الرئيسية:
  • قائمة فحص ذكية تتكيف مع السيارة: أكثر من 120 نقطة فحص مرتبة حسب دورانك الطبيعي حول السيارة (قبل الموعد، الوثائق والبطاقة الرمادية ورقم الشاسيه، الهيكل والصبيغة، تحت الغطاء والمحرك بارد، المقصورة، أسفل السيارة، تشغيل المحرك، والقيادة التجريبية).
  • كتالوج السوق الجزائري: يضم 51 موديلاً شائعاً في الجزائر مع نقاط الضعف المعروفة لكل محرك وعلبة سرعة، بالإضافة إلى دعم كامل لأي سيارة غير مدرجة (حسب الوقود، التيربو، الدفع الرباعي، سنة الصنع، والكيلومترات).
  • فحص الكمبيوتر الإلكتروني (OBD2 ELM327): اربط هاتفك بفيشة بلوتوث ELM327 لقراءة أكواد الأعطال (أكثر من 150 كود مشروح بالعربية والفرنسية والإنجليزية)، وكشف ما إذا تم مسح ذاكرة الأعطال حديثاً قبل اللقاء، ومراقبة حرارة المحرك والفولتية أثناء القيادة التجريبية.
  • ميزات ذكية تعمل بدون إنترنت: قراءة عداد الكيلومترات وكود تاريخ الإطارات (DOT) من صورة بالكاميرا، إدخال الملاحظات بالصوت، وقراءة أسئلة الفحص صوتياً.
  • تقرير PDF احترافي للتفاوض: احصل على تقرير مفصل جاهز للتحميل أو المشاركة عبر واتساب، يوضح النتيجة من 100، نقاط القوة والعيوب مرتبة حسب الخطورة مع الصور، وحاسبة التفاوض بالسنتيم والدينار الجزائري بناءً على تسعيرة الميكانيكي.

  تنبيه هام: صُمم تطبيق «فحص» لمساعدة المشتري على اكتشاف العيوب الواضحة وتنظيم المعاينة الميدانية، لكنه لا يعوض الفحص المعمق فوق الرافعة (Pont) لدى ميكانيكي مختص قبل الشراء.
  ```

### B. French (`fr-FR`) — Français
- **App Title (29 chars):**
  `Fahs : Inspection Auto & OBD2`
- **Short Description (78 chars):**
  `Guide d'inspection de voiture d'occasion en Algérie, scan OBD2 et rapport PDF.`
- **Full Description:**
  ```text
  Fahs est votre assistant de terrain étape par étape pour inspecter une voiture d'occasion en Algérie avant l'achat. Disponible en français, arabe et anglais, l'application fonctionne 100 % hors-ligne devant le véhicule.

  Fonctionnalités principales :
  • Checklist guidée adaptative (+120 points) : Organisée dans l'ordre naturel autour de la voiture (avant le rendez-vous, carte grise et numéro de châssis frappé, carrosserie et peinture, sous le capot moteur froid, habitacle, soubassement, démarrage à froid et essai routier).
  • Catalogue du marché algérien : 51 modèles courants en Algérie avec leurs points de vigilance par motorisation (Essence, Diesel, GPL, boîte auto/DSG/BVA) ainsi qu'un mode adaptatif pour tout autre véhicule.
  • Diagnostic OBD2 Bluetooth (ELM327) : Lecture en mode sécurisé (lecture seule) des codes défauts (+150 codes expliqués clairement), détection d'effacement récent de la mémoire calculateur, suivi automatique pendant l'essai routier (température, ralenti, tension batterie) et remplissage automatique des points du contrôle liés au scan.
  • Outils intelligents hors-ligne : Lecture du kilométrage et de l'âge des pneus (DOT) par photo sur l'appareil, dictée vocale des notes et lecture audio des questions.
  • Rapport PDF professionnel & Négociation en Centimes : Téléchargez ou partagez sur WhatsApp un rapport PDF complet (jauge /100, scores par zone, tableau des défauts avec photos, bilan OBD2 et calculateur d'offre en millions de centimes / DZD).

  Avis important : Fahs vous aide à écarter les véhicules à risque et à préparer votre négociation, mais ne remplace pas l'examen final sur pont élévateur par un mécanicien professionnel.
  ```

### C. English (`en-US`) — English
- **App Title (29 chars):**
  `Fahs: Used Car Check & OBD2`
- **Short Description (78 chars):**
  `Step-by-step used car inspection checklist, ELM327 OBD2 scan & PDF report.`
- **Full Description:**
  ```text
  Fahs is an offline-first pre-purchase used car inspection assistant and ELM327 OBD2 diagnostic companion built for car buyers in Algeria. It works in Arabic (RTL), French, and English without requiring an internet connection at the car.

  Key Features:
  • Adaptive 9-Section Walkaround Checklist (120+ items): Guides you one question at a time from paperwork and stamped chassis number verification to exterior bodywork, cold engine bay, interior, underbody, startup, and road test.
  • Algerian Car Catalogue: Covers 51 popular models across 20 brand groups with engine-specific weak points, plus intelligent fallback rules for any unlisted car (Petrol, Diesel, LPG/GPL, Turbo, 4x4, SUV, Pick-up, or Van).
  • Safe Read-Only ELM327 OBD2 Scanner: Reads stored, pending, and permanent fault codes (150+ codes explained in plain language), checks whether fault memory was cleared recently before the meeting, monitors live coolant temperature, idle RPM, and charging voltage during the test drive, and auto-answers matching checklist items.
  • Offline Smart Tools: On-device odometer mileage OCR from a photo, tyre DOT age reader, offline voice notes, and text-to-speech question reading.
  • Professional A4 PDF Report & Price Negotiation: Save to Documents/Fahs or share via WhatsApp a complete inspection report with a 0–100 circular score gauge, per-area bars, severity-sorted defects with photo thumbnails, and a DZD / Millions of Centimes negotiation summary.

  Disclaimer: Fahs helps buyers perform a structured field inspection and spot red flags early, but it does not replace a final lift inspection by a qualified mechanic.
  ```

---

## 3. Screenshots to Take for Google Play Console

Take **6 to 8 phone screenshots** (1080 × 1920 px or 1080 × 2400 px, 9:16 portrait) and **2 tablet screenshots** in your primary store language (plus Arabic RTL screenshots for the Arabic store listing):

1. **Screenshot 1 — Home Screen (`HomeScreen`)**: Shows the Emerald header, language switcher (`العربية / Français / English`), "Start New Inspection" hero card, and Saved Inspections list.
2. **Screenshot 2 — Car Selection & Odometer Photo OCR (`CarSelectionScreen`)**: Shows a popular Algerian model (e.g., Peugeot 208 or Renault Clio 4), fuel/gearbox chips (`Essence / Diesel / GPL`), and the "Read Odometer from Photo (Offline)" button.
3. **Screenshot 3 — Guided Checklist Wizard (`ChecklistWizardScreen`)**: Shows one clear inspection question, severity badge, "How to check?" expandable guide, Good / Problem / Skip buttons, and photo/voice-note tools.
4. **Screenshot 4 — OBD2 Scanner & Cleared-Codes Detection (`ObdGuideScreen`)**: Shows the ELM327 scan results, stored/pending fault codes with plain-language explanations, cleared-codes alert status, and the "Start test drive (automatic monitoring)" card.
5. **Screenshot 5 — Inspection Report & Area Scores (`InspectionReportScreen`)**: Shows the 0–100 score card, Plain-Language Summary, Scores per Inspection Area bars, and Mechanic Quote Negotiation Calculator in Millions of Centimes / DZD.
6. **Screenshot 6 — Professional PDF Report Preview (`SamplePdfPreviewDialog`)**: Shows Page 1 of the generated A4 PDF report (in Arabic RTL or French) with the circular score gauge, coloured verdict band, and problems table with photo thumbnails.

---

## 4. Google Play Console — Data Safety Form Answers

Fahs is **offline-first**: all inspections, photos, and OBD scans stay strictly on the user's phone unless the user explicitly signs in with Google and taps **"Back Up"** to save their inspections to their private Cloud Firestore space.

Use the following exact answers in **Google Play Console -> App content -> Data safety**:

| Step / Question | Answer to Select | Explanation |
| :--- | :--- | :--- |
| **Does your app collect or share any of the required user data types?** | **Yes** | Only when the user uses the optional Google Sign-In & Cloud Firestore Backup feature. (All data is encrypted in transit via HTTPS/TLS, and Nothing is shared with third parties). |
| **Is all of the user data collected by your app encrypted in transit?** | **Yes** | Firebase Auth and Cloud Firestore use TLS/HTTPS encryption in transit. |
| **Do you provide a way for users to request that their data is deleted?** | **Yes** | Users can delete individual or all cloud backups directly in the app (`Inspections -> Delete All Cloud Backups`) and via the web deletion URL below. |
| **Personal Info -> Name / Email address / User IDs** | **Collected (Optional), Not Shared** | Collected only if the user chooses to sign in with Google for optional Cloud Backup. Purpose: *App functionality, Account management*. |
| **App info and performance -> Diagnostics / Crash logs** | **No** | No third-party analytics or advertising SDKs are bundled. |
| **Photos and videos** | **Not collected (Processed on-device only)** | Inspection photos and odometer/tyre OCR photos are stored and processed strictly locally on the user's device and inside the user's local PDF report; they are never uploaded to Cloud Firestore. |
| **Audio files (Microphone)** | **Not collected (Ephemeral on-device only)** | Microphone audio is processed ephemerally by Android's built-in speech recognizer for voice notes/answers and is never recorded or transmitted by the app. |
| **Location** | **Not collected** | Fahs does not request `ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION`. |

---

## 5. Privacy Policy & Account Deletion Web Addresses

Google Play requires two public HTTPS URLs in **App content -> Privacy policy** and **App content -> Data safety -> Account deletion**.
You can host these two pages on your domain (or free GitHub Pages / Firebase Hosting):

1. **Privacy Policy URL:**
   `https://fahs.dz/privacy`
2. **Account & Cloud Data Deletion URL:**
   `https://fahs.dz/delete-account`

### What to put on `https://fahs.dz/privacy`:
- State clearly that **Fahs (`dz.fahs.app`)** stores all vehicle inspections, checklist answers, photos, and OBD2 scan results locally on the user's phone by default.
- Explain the 4 permissions used and why:
  - **Camera (`CAMERA`)**: To take photos of car defects for the local PDF report and read odometer/tyre numbers on-device using bundled Google ML Kit OCR.
  - **Microphone (`RECORD_AUDIO`)**: To convert spoken notes into text using Android's speech recognition.
  - **Bluetooth (`BLUETOOTH_CONNECT`)**: To connect in read-only mode to a paired ELM327 OBD2 adapter.
  - **Notifications (`POST_NOTIFICATIONS`)**: To display the active foreground notification during automatic test-drive monitoring while the phone screen is off.
- State that optional Cloud Backup uses **Google Sign-In** and **Google Cloud Firestore** only when initiated by the user, and no data is ever sold or shared with advertisers.
- Provide your support email (`sadekmefti@gmail.com`) and WhatsApp (`+971524830059`).

### What to put on `https://fahs.dz/delete-account`:
- **In-App Instant Deletion:** Open **Fahs** -> go to the **Inspections (`فحوصاتي`)** tab -> in the Cloud Backup card, tap **Delete All Cloud Backups (`حذف جميع النسخ السحابية`)** and **Sign Out**. This immediately deletes all stored documents under `/users/{userId}/inspections` in Cloud Firestore.
- **Web / Email Deletion Request (without the app installed):** Users can email `sadekmefti@gmail.com` with the subject `"Delete Fahs Cloud Data"` from their Google account email address, and all associated Firebase Auth / Firestore records will be permanently erased within 7 days.
