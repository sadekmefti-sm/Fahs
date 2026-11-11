# Fahs (فحص) - Algerian Used Car Inspection Assistant

Fahs is an offline-first mobile inspection companion designed specifically for Algerian buyers purchasing used cars from private sellers. In an informal market with no vehicle history registries (no Carfax), prevalent odometer tampering, cleared diagnostic trouble codes, and hidden accident repairs, Fahs serves as a trusted, knowledgeable friend that guides buyers step-by-step through physical, legal, and computer diagnostics to deliver a clear verdict and negotiation leverage.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following core product and design decisions have been confirmed for the Fahs application architecture:

* **Platform & Framework**: Native Android with Kotlin and Jetpack Compose (Kotlin 2.0+, Material 3). Provides zero-overhead access to low-level Bluetooth Classic RFCOMM/SPP for cheap ELM327 clone dongles, ML Kit camera OCR for windscreen VIN scanning, and immediate offline local persistence via Room.
* **Visual Direction**: **Emerald Green & Slate** palette. High-contrast outdoor visibility under direct sunlight, calm and trustworthy tone, avoiding alarming or accusing language while delivering unambiguous pass/fail/attention indicators.
* **Initial Vehicle Batch**: Top 10 high-volume Algerian market vehicles for Phase 2 data seeding (Renault Symbol, Renault Clio 4, Dacia Logan, Dacia Sandero Stepway, Peugeot 208, Peugeot 301, Volkswagen Golf 7, Seat Ibiza, Hyundai Accent, Dacia Duster) with adaptive engine/gearbox/GPL variations plus an "Other car" 4-question fallback.
* **Multi-Language Architecture**: Native trilingual support (Arabic with full Right-to-Left layout, French, and English), preserving popular Algerian vernacular car terms (*carte grise*, *contrôle technique*, *vignette*, *moudjahid* licence).
* **Phased Execution Model**: Strict adherence to the 9-phase roadmap, pausing after each phase for validation on the streaming emulator and physical devices.

---

## 1. Overview & Core Concept

### What It Does
1. **Adaptive Physical Walkaround**: Customizes inspection questions based on the exact vehicle (fuel, diesel DPF/injectors, manual vs. automatic, factory/retrofitted GPL/LPG, known model weak points). Follows a natural walkaround order: Pre-visit phone questions -> Documents -> Exterior body/paint -> Cold engine bay -> Interior & wear -> Underbody/suspension -> Engine start/smoke -> Test drive -> Post-drive checks.
2. **Identity & Document Fraud Prevention**: 17-character VIN syntax checking, offline manufacturer/year decoding, Algerian license plate decoding (`XXXXX 1YY WW`), 3-way VIN consistency checks (chassis stamp, manufacturer plate, carte grise, and ECU), and document compliance checks.
3. **OBD2 Diagnostics & Tamper Detection**: Real-time interrogation over ELM327 Bluetooth dongles (and built-in 4-profile vehicle simulator). Unmasks recently erased diagnostic codes by inspecting MIL status, distance traveled since DTC clearing, warm-up cycles, and live engine sensor sanity (coolant temp, fuel trims, battery voltage).
4. **Scored Inspection Report**: 0–100 overall score with 3-tier verdict (*Good to Buy*, *Negotiate / Mechanic Check*, *Walk Away*), repair cost estimates in Algerian Dinars (DZD) for negotiation, and a shareable multilingual PDF.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Home Screen**: Minimalist and clean. One prominent "Inspect a Car" action card, quick access to "My Inspections", an "OBD Dongle Guide & Simulator" card, and top-level language switcher (العربية / Français / English).
2. **Car Setup**: Fast grid selection of top Algerian brands with search, model picker with generation badges, fuel (Essence, Diesel, GPL), and gearbox (Manuelle, Automatique).
3. **Step-by-Step Inspection Wizard**:
   * One focused question per screen.
   * Large visual reference diagram of where to inspect.
   * Three large, thumb-friendly tap targets: **Good (Green)**, **Problem (Red)**, **Could Not Check (Grey)**.
   * "How do I check this?" expandable sheet with plain-language good vs. bad indicators.
   * Quick photo capture and note/voice input on every step.
   * Persistent auto-save after every question (resilience against incoming calls, low battery, or app restarts).
4. **Identity & Paperwork Hub**: Side-by-side comparison cards for VIN and plate with live discrepancy highlights.
5. **OBD2 Live Scan & Diagnostics**: Dongle pairing assistant, status progress, live gauges for temperature/voltage, and an explicit "Code Clearing Alert" if codes were erased within the last 50 km or 3 warmups.
6. **Report & Negotiation Card**: Clear verdict badge, categorized breakdown, itemized defect list with DZD repair estimates, and one-tap PDF export.

### Visual Identity & Theme Tokens
* **Primary / Trust**: Deep Emerald Green (`#0A5C36`) & Forest Jade (`#137A48`)
* **Background & Surfaces**: Clean High-Contrast Slate White (`#F8FAFC`) with Crisp Elevated Cards (`#FFFFFF`)
* **Text & Readability**: Deep Slate Charcoal (`#0F172A`) for maximum sunlight legibility; Muted Slate (`#475569`) for guidance copy
* **Status Accents**:
  * Pass / Healthy: Emerald Green (`#16A34A`)
  * Attention / Negotiate: Warm Amber (`#D97706`)
  * Problem / Walk Away: Crimson Red (`#DC2626`)
  * Unchecked / Neutral: Slate Grey (`#94A3B8`)
* **Typography**: Clean, robust sans-serif with Arabic typography scaling and RTL layout mirroring.

---

## 3. Key Product Decisions & Trade-Offs

* **Decision 1: Native Jetpack Compose vs. Hybrid Flutter**
  * *Chosen Approach*: Native Android Kotlin + Jetpack Compose.
  * *Why*: ELM327 clone communication requires reliable Bluetooth SPP RFCOMM sockets, custom byte streams, and timing control. Native Compose eliminates bridge latency, provides instant web emulator previews, and ensures compatibility with Android 8.0 (API 24) devices.
  * *Architecture Future-Proofing*: Clean separation of domain interfaces (`ObdScanner`, `InspectionRepository`, `VinDecoder`) enables straightforward porting to Flutter or Swift for iOS later.
* **Decision 2: Local SQLite (Room) First, Optional Cloud Sync**
  * *Chosen Approach*: 100% offline-functional Room database storing inspections, photos, and results. Cloud Firestore reserved strictly as an optional backup on user sign-in.
  * *Why*: Buyers in Algerian car souks or remote parking lots frequently experience zero or spotty mobile data. The app must never fail or stall due to lack of internet.
* **Decision 3: Standalone JSON Data Asset Architecture**
  * *Chosen Approach*: All vehicle catalogs, checklist criteria, known weak points, and generic OBD2 DTC definitions stored in versioned JSON assets inside `assets/data/`.
  * *Why*: Non-developer contributors and mechanics can edit, review, and expand car data, translations, and DZD repair ranges without modifying application code.
* **Decision 4: Built-in 4-Profile OBD2 Simulator**
  * *Chosen Approach*: Comprehensive mock OBD adapter built directly into the app alongside the physical Bluetooth adapter.
  * *Why*: Allows immediate development, continuous automated testing, and customer demonstrations without requiring physical dongles and vehicles.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │  Home Screen │  │  Car Select  │  │ Check Wizard │  │ OBD Scanner│  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └─────┬──────┘  │
│         │                 │                 │                │         │
│         └─────────────────┼─────────────────┴────────────────┘         │
│                           ▼                                            │
│              ViewModel Layer (StateFlow & M3 Events)                   │
├────────────────────────────────────────────────────────────────────────┤
│                         Domain & Business Logic                        │
│  ┌──────────────────┐  ┌───────────────────┐  ┌──────────────────────┐ │
│  │   VinDecoder     │  │  InspectionScorer │  │ ChecklistEngine      │ │
│  │  (WMI, Plate DZ) │  │  (Weighted, Caps) │  │ (Fuel, Gear, Issues) │ │
│  └──────────────────┘  └───────────────────┘  └──────────────────────┘ │
│                           ▲                                            │
│  ┌────────────────────────┴──────────────────────────────────────────┐ │
│  │              ObdController (Interface & State Machine)             │ │
│  │    ┌─────────────────────────────┐ ┌───────────────────────────┐  │ │
│  │    │ BluetoothSppTransport       │ │ MockObdSimulatorTransport │  │ │
│  │    │ (ELM327 Clone Tolerant)     │ │ (4 Preset Test Vehicles)  │  │ │
│  │    └─────────────────────────────┘ └───────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────────────────────┤
│                          Data & Persistence Layer                      │
│  ┌───────────────────────────────────┐  ┌────────────────────────────┐ │
│  │ Room Database (Local Inspections) │  │ Bundled JSON Repositories  │ │
│  │ - InspectionEntity                │  │ - Vehicle Catalog (DZ)     │ │
│  │ - ChecklistAnswerEntity           │  │ - 150+ Rule Library        │ │
│  │ - ObdScanEntity                   │  │ - Known Issues & DZD Costs │ │
│  └───────────────────────────────────┘  └────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Phased Roadmap & Execution Steps

### Phase 1: Foundation (Current Target)
* Project configuration, theme setup (Emerald Green & Slate).
* Full 3-language switcher (Arabic RTL, French, English) with persistent preference.
* Room Database setup for local inspection records (`InspectionDatabase`).
* Home screen with "Start Inspection", "Saved Inspections", and "OBD Dongle Guide".
* Base Navigation architecture with edge-to-edge support.

### Phase 2: Car Selection
* Catalog data format (`catalog.json`) with first batch of 10 popular Algerian cars.
* Interactive visual selector (Brand grid, model, year, fuel, gearbox).
* "Other Car" 4-question fallback wizard.

### Phase 3: Adaptive Checklist Engine
* Master checklist library with conditional filtering (fuel, gearbox, GPL tank, mileage).
* Single-question-per-screen wizard with photo capture, notes, Good/Problem/Unchecked actions.
* Known model weak points integration.
* Robust auto-save, pause, and resume capability.

### Phase 4: Identity, VIN & Algerian Documents
* 17-character VIN validator with on-device camera text scanner.
* Offline VIN decoder (WMI, manufacturer, year).
* Algerian license plate parser (`XXXXX 1YY WW`).
* 3-way VIN match screen & Algerian document verification guide (*carte grise*, *contrôle technique*, *moudjahid* licence).

### Phase 5: OBD2 Diagnostics & Tamper Scanner
* Built-in OBD2 Simulator with 4 profiles (Healthy, Fault Codes Present, Codes Recently Cleared Fraud Alert, ECU Unresponsive).
* Bluetooth Classic SPP connection engine with ELM327 clone tolerance, retries, and AT handshake.
* Stored/pending DTC reader and clear-status inspector (distance/warm-ups since cleared).
* Live telemetry health check (coolant, idle RPM, voltage).

### Phase 6: Inspection Report & Multilingual PDF
* Scoring engine with critical defect caps.
* Visual score dashboard, problem breakdown, negotiation helper (DZD repair cost total).
* Multilingual Android-shareable PDF export with proper Arabic letter shaping.

### Phase 7: History, Comparison & Optional Cloud Backup
* Saved inspections management (view, resume, duplicate, delete).
* Side-by-side vehicle comparison tool.
* Optional Google Sign-In with Cloud Firestore backup.

### Phase 8: Comprehensive Content Completion
* Expanded catalog (25+ models) and 150+ checklist items.
* Mechanic-reviewed weak points and DZD repair ranges.

### Phase 9: Release Preparation
* First-launch tutorial, OBD purchasing advice guide, privacy policy, and Google Play packaging.
