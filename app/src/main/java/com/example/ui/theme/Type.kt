package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.domain.model.DeenFontConfig
import com.example.domain.model.LatinFontChoice

val AmiriFontFamily = FontFamily(
    Font(R.font.amiri, FontWeight.Normal)
)

val NotoBengaliFontFamily = FontFamily(
    Font(R.font.noto_sans_bengali, FontWeight.Normal)
)

val PlusJakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal)
)

val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal)
)

val LocalDeenFontConfig = staticCompositionLocalOf { DeenFontConfig() }

fun scaledArabicDisplayStyle(fontConfig: DeenFontConfig = DeenFontConfig()): TextStyle {
    val scale = fontConfig.effectiveArabicScale
    return TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (24f * scale).sp,
        lineHeight = (42f * scale).sp,
        textDirection = TextDirection.Rtl
    )
}

fun scaledArabicCompactStyle(fontConfig: DeenFontConfig = DeenFontConfig()): TextStyle {
    val scale = fontConfig.effectiveArabicScale
    return TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (20f * scale).sp,
        lineHeight = (35f * scale).sp,
        textDirection = TextDirection.Rtl
    )
}

fun scaledBengaliBodyStyle(fontConfig: DeenFontConfig = DeenFontConfig()): TextStyle {
    val scale = fontConfig.effectiveBengaliScale
    return TextStyle(
        fontFamily = NotoBengaliFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (15f * scale).sp,
        lineHeight = (24f * scale).sp
    )
}

fun scaledEnglishBodyStyle(fontConfig: DeenFontConfig = DeenFontConfig()): TextStyle {
    val scale = fontConfig.effectiveEnglishScale
    val family = when (fontConfig.latinFontChoice) {
        LatinFontChoice.PLUS_JAKARTA -> PlusJakartaFontFamily
        LatinFontChoice.INTER -> InterFontFamily
    }
    return TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = (16f * scale).sp,
        lineHeight = (24f * scale).sp
    )
}

val ArabicDisplayStyle = scaledArabicDisplayStyle()
val ArabicCompactStyle = scaledArabicCompactStyle()
val BengaliBodyStyle = scaledBengaliBodyStyle()

fun buildDeenTypography(
    fontConfig: DeenFontConfig = DeenFontConfig(),
    language: AppLanguage = AppLanguage.ENGLISH
): Typography {
    val bodyScale = if (language == AppLanguage.BENGALI) {
        fontConfig.effectiveBengaliScale
    } else {
        fontConfig.effectiveEnglishScale
    }
    val primaryHeadingFamily = when {
        language == AppLanguage.BENGALI -> NotoBengaliFontFamily
        fontConfig.latinFontChoice == LatinFontChoice.INTER -> InterFontFamily
        else -> PlusJakartaFontFamily
    }
    val primaryBodyFamily = when {
        language == AppLanguage.BENGALI -> NotoBengaliFontFamily
        fontConfig.latinFontChoice == LatinFontChoice.PLUS_JAKARTA -> PlusJakartaFontFamily
        else -> InterFontFamily
    }

    return Typography(
        displaySmall = TextStyle(
            fontFamily = primaryHeadingFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (32f * bodyScale).sp,
            lineHeight = (40f * bodyScale).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = primaryHeadingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (24f * bodyScale).sp,
            lineHeight = (33f * bodyScale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = primaryHeadingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20f * bodyScale).sp,
            lineHeight = (28f * bodyScale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = primaryHeadingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16f * bodyScale).sp,
            lineHeight = (23f * bodyScale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = primaryHeadingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14f * bodyScale).sp,
            lineHeight = (20f * bodyScale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = primaryBodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16f * bodyScale).sp,
            lineHeight = (24f * bodyScale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = primaryBodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14f * bodyScale).sp,
            lineHeight = (21f * bodyScale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = primaryBodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12f * bodyScale).sp,
            lineHeight = (18f * bodyScale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = primaryHeadingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13f * bodyScale).sp,
            lineHeight = (18f * bodyScale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = primaryBodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11f * bodyScale).sp,
            lineHeight = (16f * bodyScale).sp
        )
    )
}

val DeenTypography = buildDeenTypography()
