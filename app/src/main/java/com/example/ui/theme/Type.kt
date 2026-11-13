package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.i18n.AppLanguage

val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Bold)
)

/**
 * Tabular figures style for live readings (temperature, voltage, RPM, distance)
 * so digits have equal width and never jitter while updating.
 */
fun tabularReadingStyle(language: AppLanguage, fontSizeSp: Int = 36): TextStyle {
    val isAr = language == AppLanguage.AR
    val family = if (isAr) CairoFontFamily else PlusJakartaSans
    val lineHeightSp = if (isAr) (fontSizeSp + 12) else (fontSizeSp + 6)
    return TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = fontSizeSp.sp,
        lineHeight = lineHeightSp.sp,
        fontFeatureSettings = "tnum",
        platformStyle = PlatformTextStyle(includeFontPadding = true)
    )
}

/**
 * Approved 5-size scale with generous Arabic line-height protection:
 * 1. DisplayReading (36sp Bold, tnum) -> displayLarge / headlineLarge
 * 2. ScreenTitle (24sp Bold)          -> headlineMedium
 * 3. SectionHeading (18sp SemiBold)   -> titleLarge / titleMedium
 * 4. Body (15sp Regular/Medium)       -> bodyLarge / bodyMedium / titleSmall
 * 5. CaptionUnit (13sp Medium)        -> bodySmall / labelLarge / labelMedium / labelSmall
 */
fun getTypography(language: AppLanguage): Typography {
    val isAr = language == AppLanguage.AR
    val family = if (isAr) CairoFontFamily else PlusJakartaSans
    val platformPadding = PlatformTextStyle(includeFontPadding = true)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            lineHeight = if (isAr) 48.sp else 42.sp,
            fontFeatureSettings = "tnum",
            platformStyle = platformPadding
        ),
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            lineHeight = if (isAr) 48.sp else 42.sp,
            fontFeatureSettings = "tnum",
            platformStyle = platformPadding
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = if (isAr) 36.sp else 30.sp,
            platformStyle = platformPadding
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = if (isAr) 28.sp else 24.sp,
            platformStyle = platformPadding
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = if (isAr) 28.sp else 24.sp,
            platformStyle = platformPadding
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = if (isAr) 26.sp else 22.sp,
            platformStyle = platformPadding
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = if (isAr) 26.sp else 22.sp,
            platformStyle = platformPadding
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = if (isAr) 26.sp else 22.sp,
            platformStyle = platformPadding
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = if (isAr) 22.sp else 18.sp,
            platformStyle = platformPadding
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = if (isAr) 24.sp else 20.sp,
            platformStyle = platformPadding
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = if (isAr) 22.sp else 18.sp,
            platformStyle = platformPadding
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = if (isAr) 20.sp else 17.sp,
            platformStyle = platformPadding
        )
    )
}

val Typography = getTypography(AppLanguage.FR)
