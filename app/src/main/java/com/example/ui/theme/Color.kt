package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Approved "Instrument Panel" Palette (Light & Dark)
// ============================================================================

// Light Mode Core Tokens
val FahsInk = Color(0xFF0E1F1A)              // Scan surface & primary text on Paper (15.8:1 contrast)
val FahsInkElevated = Color(0xFF162D26)      // Subtle step on dark #0E1F1A instrument surface
val FahsInkHairline = Color(0xFF244239)      // 1dp hairline on dark #0E1F1A instrument surface
val FahsBrand = Color(0xFF0F6B4F)            // Primary action buttons (deep dark green ink, distinct from result green)
val FahsBrandPressed = Color(0xFF0A4F3A)     // Pressed / tag surface on Brand button
val FahsBrandSoft = Color(0xFFDCECE6)        // Subtle active navigation pill / chip background
val FahsPaper = Color(0xFFF6F7F5)            // Calm light background for Home, lists, Guide, Account
val FahsSurface = Color(0xFFFFFFFF)          // Interactive object surface
val FahsHairline = Color(0xFFE3E6E2)         // 1dp list divider on Paper
val FahsMutedText = Color(0xFF52605B)        // Secondary text & units on Paper (6.1:1 contrast)
val FahsDarkMutedText = Color(0xFFA5B8B1)    // Secondary text & scope line on #0E1F1A (8.4:1 contrast)

// Neutral Scan Progress Arc (Never green/teal so green only ever means a result)
val FahsProgressArcNeutral = Color(0xFFD6E2EE) // Pale grey-blue on #0E1F1A (11.9:1 contrast)

// Strict Result-Only Semantic Colours (NEVER use on buttons, headers, or progress arcs)
val ResultNormalGreen = Color(0xFF1E9E5A)    // Normal / No problem detected by the scan
val ResultCheckOrange = Color(0xFFE08A00)    // To check / Some points to check
val ResultProblemRed = Color(0xFFD43D2F)     // Problem / Serious warning
val ResultIncompleteGrey = Color(0xFF6E7873) // Not available / Not checked / Incomplete scan

// Dark Mode Tokens (Android Dark Theme support)
val FahsPaperDark = Color(0xFF091410)        // Deep obsidian background in dark mode
val FahsSurfaceDark = Color(0xFF122720)      // Interactive card surface in dark mode
val FahsHairlineDark = Color(0xFF1F352E)     // 1dp divider in dark mode
val FahsTextPrimaryDark = Color(0xFFF1F4F2)  // Primary text in dark mode
val FahsTextMutedDark = Color(0xFFA5B8B1)    // Secondary text in dark mode
val FahsBrandDark = Color(0xFF148361)        // Primary button in dark mode

// ============================================================================
// Legacy Token Aliases (Mapped to the new Instrument Panel system so existing
// screens transition seamlessly to the new palette without decorative green)
// ============================================================================
val Emerald900 = FahsInk
val Emerald800 = FahsBrand
val Emerald700 = FahsBrand
val Emerald600 = FahsBrand
val Emerald500 = ResultNormalGreen
val Emerald400 = FahsProgressArcNeutral
val Emerald200 = FahsHairline
val Emerald100 = FahsBrandSoft
val Emerald50 = FahsPaper

val Slate950 = FahsPaperDark
val Slate900 = FahsInk
val Slate800 = FahsInk
val Slate700 = FahsMutedText
val Slate600 = FahsMutedText
val Slate500 = FahsMutedText
val Slate400 = Color(0xFF7B8983)
val Slate300 = FahsHairline
val Slate200 = FahsHairline
val Slate100 = Color(0xFFECEFED)
val Slate50 = FahsPaper

val StatusPassGreen = ResultNormalGreen
val StatusPassGreenContainer = Color(0xFFE3F5EC)
val StatusPassGreenOnContainer = Color(0xFF0F5731)

val StatusWarningAmber = ResultCheckOrange
val StatusWarningAmberContainer = Color(0xFFFCF0DC)
val StatusWarningAmberOnContainer = Color(0xFF6B4000)

val StatusErrorRed = ResultProblemRed
val StatusErrorRedContainer = Color(0xFFFBE5E3)
val StatusErrorRedOnContainer = Color(0xFF751B13)

val StatusNeutralSlate = ResultIncompleteGrey
val StatusNeutralSlateContainer = Color(0xFFECEFED)
val StatusNeutralSlateOnContainer = FahsInk
