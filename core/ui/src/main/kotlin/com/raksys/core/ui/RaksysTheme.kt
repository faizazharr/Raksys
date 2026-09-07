package com.raksys.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object RaksysThemeColors {
    // Professional Studio Color Palette (Slate / Deep Charcoal with high readability)
    val Background = Color(0xFF141820)
    val Surface = Color(0xFF1C212B)
    val SurfaceElevated = Color(0xFF262C38)
    val SurfaceHover = Color(0xFF323A49)
    val Border = Color(0xFF313846)
    val BorderLight = Color(0xFF454E60)

    val Primary = Color(0xFF4F8CF6)
    val PrimaryDark = Color(0xFF3070E6)
    val PrimaryContainer = Color(0xFF1E3258)

    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFFA0AEC0)
    val TextMuted = Color(0xFF718096)

    val Success = Color(0xFF34D399)
    val SuccessBg = Color(0xFF0D3325)
    val SuccessBorder = Color(0xFF059669)

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
    val EnvDev = Color(0xFF10B981)
    val EnvDevBg = Color(0xFF064E3B)
    val EnvStaging = Color(0xFFF59E0B)
    val EnvStagingBg = Color(0xFF78350F)
    val EnvProd = Color(0xFFEF4444)
    val EnvProdBg = Color(0xFF7F1D1D)

    // Splitter / Handle Colors
    val SplitterHover = Color(0xFF4F8CF6)
}

@Composable
fun RaksysAppTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = RaksysThemeColors.Primary,
        onPrimary = Color.White,
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
        content = content
    )
}
