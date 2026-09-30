package com.raksys.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object RaksysThemeColors {
    // Professional Studio Color Palette (Slate / Deep Charcoal with high readability)
    val Background = Color(0xFF141820)
    val Surface = Color(0xFF1C212B)
    val SurfaceElevated = Color(0xFF262C38)
    val SurfaceHover = Color(0xFF323A49)
    val Border = Color(0xFF313846)
    val BorderLight = Color(0xFF454E60)

    // Accent for text, icons, and borders on dark surfaces (6.8:1 on Surface).
    val Primary = Color(0xFF78A9FA)
    // Accent for filled controls that carry white text (5.2:1). Never use Primary as a fill behind white text.
    val PrimaryFill = Color(0xFF2B67D6)
    val PrimaryDark = PrimaryFill
    val PrimaryContainer = Color(0xFF1E3258)

    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFFA0AEC0)
    // Tertiary text. 5.7:1 on Surface, 4.9:1 on SurfaceElevated (WCAG AA needs 4.5:1 for small text).
    val TextMuted = Color(0xFF98A4B8)

    val Success = Color(0xFF34D399)
    val SuccessBg = Color(0xFF0D3325)
    val SuccessBorder = Color(0xFF059669)
    // Filled positive control that carries white text (5.5:1).
    val SuccessFill = Color(0xFF047857)

    val Error = Color(0xFFF87171)
    val ErrorBg = Color(0xFF381A1B)
    val ErrorBorder = Color(0xFFDC2626)

    val Warning = Color(0xFFFBBF24)
    val WarningBg = Color(0xFF382B10)

    val Info = Color(0xFF60A5FA)
    val InfoBg = Color(0xFF152A47)

    // Database Brand Colors
    val PostgresColor = Color(0xFF5B9BD5)
    val MysqlColor = Color(0xFFF59E0B)
    val SqliteColor = Color(0xFF38BDF8)
    val MongodbColor = Color(0xFF4ADE80)
    val RedisColor = Color(0xFFF87171)

    // Environment Colors
    // Environment identity: the Env* color marks dots, edges, and borders; the *Text color is the
    // legible label color on the matching *Bg (all above 5:1).
    val EnvDev = Color(0xFF10B981)
    val EnvDevBg = Color(0xFF064E3B)
    val EnvDevText = Color(0xFF6EE7B7)
    val EnvStaging = Color(0xFFF59E0B)
    val EnvStagingBg = Color(0xFF78350F)
    val EnvStagingText = Color(0xFFFCD34D)
    val EnvProd = Color(0xFFEF4444)
    val EnvProdBg = Color(0xFF7F1D1D)
    val EnvProdText = Color(0xFFFCA5A5)
    // Filled destructive controls that carry white text (5.6:1).
    val EnvProdFill = Color(0xFFC62828)

    // Keyboard focus ring.
    val FocusRing = Color(0xFF78A9FA)

    // Splitter / Handle Colors
    val SplitterHover = Color(0xFF4F8CF6)
}

/**
 * macOS text styles (HIG › Typography): Body 13, Callout 12, Caption 11, Title 3 15, Title 2 17.
 * Nothing below 11 sp; system default font, regular / semibold weights only.
 */
val RaksysTypography = Typography(
    displaySmall = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun RaksysAppTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = RaksysThemeColors.Primary,
        onPrimary = Color(0xFF0B1220),
        primaryContainer = RaksysThemeColors.PrimaryContainer,
        onPrimaryContainer = Color(0xFFDCE7FE),
        surface = RaksysThemeColors.Surface,
        onSurface = RaksysThemeColors.TextPrimary,
        surfaceVariant = RaksysThemeColors.SurfaceElevated,
        onSurfaceVariant = RaksysThemeColors.TextSecondary,
        background = RaksysThemeColors.Background,
        onBackground = RaksysThemeColors.TextPrimary,
        outline = RaksysThemeColors.Border,
        outlineVariant = RaksysThemeColors.BorderLight,
        error = RaksysThemeColors.Error,
        onError = Color.White,
        errorContainer = RaksysThemeColors.ErrorBg,
        onErrorContainer = Color(0xFFFECACA),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RaksysTypography,
        content = content
    )
}
