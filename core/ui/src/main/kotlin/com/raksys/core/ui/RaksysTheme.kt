package com.raksys.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** One complete set of semantic colors. Two instances exist: [DarkPalette] and [LightPalette]. */
@Immutable
class Palette(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceHover: Color,
    val border: Color,
    val borderLight: Color,
    val primary: Color,
    val primaryFill: Color,
    val primaryContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val success: Color,
    val successBg: Color,
    val successBorder: Color,
    val successFill: Color,
    val error: Color,
    val errorBg: Color,
    val errorBorder: Color,
    val warning: Color,
    val warningBg: Color,
    val info: Color,
    val infoBg: Color,
    val purple: Color,
    val purpleBg: Color,
    val postgresColor: Color,
    val postgresBg: Color,
    val mysqlColor: Color,
    val mysqlBg: Color,
    val sqliteColor: Color,
    val sqliteBg: Color,
    val mongodbColor: Color,
    val mongodbBg: Color,
    val redisColor: Color,
    val redisBg: Color,
    val envDev: Color,
    val envDevBg: Color,
    val envDevText: Color,
    val envStaging: Color,
    val envStagingBg: Color,
    val envStagingText: Color,
    val envProd: Color,
    val envProdBg: Color,
    val envProdText: Color,
    val envProdFill: Color,
    val typeString: Color,
    val typeHash: Color,
    val typeList: Color,
    val typeSet: Color,
    val typeZset: Color,
    val focusRing: Color,
    val splitterHover: Color,
)

private val DarkPalette = Palette(
    background = Color(0xFF141820),
    surface = Color(0xFF1C212B),
    surfaceElevated = Color(0xFF262C38),
    surfaceHover = Color(0xFF323A49),
    border = Color(0xFF313846),
    borderLight = Color(0xFF454E60),
    primary = Color(0xFF78A9FA),
    primaryFill = Color(0xFF2B67D6),
    primaryContainer = Color(0xFF1E3258),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFFA0AEC0),
    textMuted = Color(0xFF98A4B8),
    success = Color(0xFF34D399),
    successBg = Color(0xFF0D3325),
    successBorder = Color(0xFF059669),
    successFill = Color(0xFF047857),
    error = Color(0xFFF87171),
    errorBg = Color(0xFF381A1B),
    errorBorder = Color(0xFFDC2626),
    warning = Color(0xFFFBBF24),
    warningBg = Color(0xFF382B10),
    info = Color(0xFF60A5FA),
    infoBg = Color(0xFF152A47),
    purple = Color(0xFFA78BFA),
    purpleBg = Color(0xFF2E1F4D),
    postgresColor = Color(0xFF6AA9E0),
    postgresBg = Color(0xFF1E334D),
    mysqlColor = Color(0xFFF59E0B),
    mysqlBg = Color(0xFF3D2D14),
    sqliteColor = Color(0xFF38BDF8),
    sqliteBg = Color(0xFF1B374C),
    mongodbColor = Color(0xFF4ADE80),
    mongodbBg = Color(0xFF193B26),
    redisColor = Color(0xFFF87171),
    redisBg = Color(0xFF421D1D),
    envDev = Color(0xFF10B981),
    envDevBg = Color(0xFF064E3B),
    envDevText = Color(0xFF6EE7B7),
    envStaging = Color(0xFFF59E0B),
    envStagingBg = Color(0xFF78350F),
    envStagingText = Color(0xFFFCD34D),
    envProd = Color(0xFFEF4444),
    envProdBg = Color(0xFF7F1D1D),
    envProdText = Color(0xFFFCA5A5),
    envProdFill = Color(0xFFC62828),
    typeString = Color(0xFF60A5FA),
    typeHash = Color(0xFF34D399),
    typeList = Color(0xFFFBBF24),
    typeSet = Color(0xFFA78BFA),
    typeZset = Color(0xFFF472B6),
    focusRing = Color(0xFF78A9FA),
    splitterHover = Color(0xFF4F8CF6),
)

private val LightPalette = Palette(
    background = Color(0xFFF5F6F8),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFEEF1F5),
    surfaceHover = Color(0xFFE1E6EE),
    border = Color(0xFFD3D9E2),
    borderLight = Color(0xFFC2CAD6),
    primary = Color(0xFF1D5FD1),
    primaryFill = Color(0xFF2B67D6),
    primaryContainer = Color(0xFFDBE7FC),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF4A5565),
    textMuted = Color(0xFF5B6676),
    success = Color(0xFF047857),
    successBg = Color(0xFFD1FAE5),
    successBorder = Color(0xFF059669),
    successFill = Color(0xFF047857),
    error = Color(0xFFB91C1C),
    errorBg = Color(0xFFFEE2E2),
    errorBorder = Color(0xFFDC2626),
    warning = Color(0xFF92400E),
    warningBg = Color(0xFFFEF3C7),
    info = Color(0xFF1D4ED8),
    infoBg = Color(0xFFDBEAFE),
    purple = Color(0xFF6D28D9),
    purpleBg = Color(0xFFEDE7FB),
    postgresColor = Color(0xFF1E5FA8),
    postgresBg = Color(0xFFDCEBFA),
    mysqlColor = Color(0xFF92400E),
    mysqlBg = Color(0xFFFDEBC8),
    sqliteColor = Color(0xFF0B6A94),
    sqliteBg = Color(0xFFD6EEF9),
    mongodbColor = Color(0xFF166534),
    mongodbBg = Color(0xFFD5F1DF),
    redisColor = Color(0xFFB42318),
    redisBg = Color(0xFFFBDADA),
    envDev = Color(0xFF059669),
    envDevBg = Color(0xFFD1FAE5),
    envDevText = Color(0xFF065F46),
    envStaging = Color(0xFFD97706),
    envStagingBg = Color(0xFFFEF3C7),
    envStagingText = Color(0xFF92400E),
    envProd = Color(0xFFDC2626),
    envProdBg = Color(0xFFFEE2E2),
    envProdText = Color(0xFF991B1B),
    envProdFill = Color(0xFFC62828),
    typeString = Color(0xFF1D4ED8),
    typeHash = Color(0xFF047857),
    typeList = Color(0xFF92400E),
    typeSet = Color(0xFF6D28D9),
    typeZset = Color(0xFFBE185D),
    focusRing = Color(0xFF1D5FD1),
    splitterHover = Color(0xFF2B67D6),
)

/**
 * Semantic colors used everywhere in the app. Each property reads the active palette, so a change of
 * [isDark] recomposes every composable that has read a color. Never hard-code a color in a feature:
 * add a token here with a value for both appearances and check its contrast (4.5:1 for text).
 */
object RaksysThemeColors {
    /** Set by [RaksysAppTheme] from the appearance setting; true means the dark palette. */
    var isDark by mutableStateOf(true)

    private val p: Palette get() = if (isDark) DarkPalette else LightPalette

    val Background: Color get() = p.background
    val Surface: Color get() = p.surface
    val SurfaceElevated: Color get() = p.surfaceElevated
    val SurfaceHover: Color get() = p.surfaceHover
    val Border: Color get() = p.border
    val BorderLight: Color get() = p.borderLight
    /** Accent for text, icons, and borders (6.8:1 on dark Surface, 6.3:1 on light Surface). */
    val Primary: Color get() = p.primary
    /** Accent for filled controls that carry white text (5.2:1). Never put white text on Primary. */
    val PrimaryFill: Color get() = p.primaryFill
    val PrimaryContainer: Color get() = p.primaryContainer
    val TextPrimary: Color get() = p.textPrimary
    val TextSecondary: Color get() = p.textSecondary
    /** Tertiary text: 4.9:1 or better on every surface in both appearances. */
    val TextMuted: Color get() = p.textMuted
    val Success: Color get() = p.success
    val SuccessBg: Color get() = p.successBg
    val SuccessBorder: Color get() = p.successBorder
    /** Filled positive control with white text (5.5:1). */
    val SuccessFill: Color get() = p.successFill
    val Error: Color get() = p.error
    val ErrorBg: Color get() = p.errorBg
    val ErrorBorder: Color get() = p.errorBorder
    val Warning: Color get() = p.warning
    val WarningBg: Color get() = p.warningBg
    val Info: Color get() = p.info
    val InfoBg: Color get() = p.infoBg
    val Purple: Color get() = p.purple
    val PurpleBg: Color get() = p.purpleBg
    /** Database brand colors: the color is for text and icons, the Bg for the badge behind it. */
    val PostgresColor: Color get() = p.postgresColor
    val PostgresBg: Color get() = p.postgresBg
    val MysqlColor: Color get() = p.mysqlColor
    val MysqlBg: Color get() = p.mysqlBg
    val SqliteColor: Color get() = p.sqliteColor
    val SqliteBg: Color get() = p.sqliteBg
    val MongodbColor: Color get() = p.mongodbColor
    val MongodbBg: Color get() = p.mongodbBg
    val RedisColor: Color get() = p.redisColor
    val RedisBg: Color get() = p.redisBg
    /** Environment identity: Env* marks dots, edges, and borders; *Text is the legible label on the matching *Bg. */
    val EnvDev: Color get() = p.envDev
    val EnvDevBg: Color get() = p.envDevBg
    val EnvDevText: Color get() = p.envDevText
    val EnvStaging: Color get() = p.envStaging
    val EnvStagingBg: Color get() = p.envStagingBg
    val EnvStagingText: Color get() = p.envStagingText
    val EnvProd: Color get() = p.envProd
    val EnvProdBg: Color get() = p.envProdBg
    val EnvProdText: Color get() = p.envProdText
    /** Filled destructive control with white text (5.6:1). */
    val EnvProdFill: Color get() = p.envProdFill
    /** Redis value-type colors. */
    val TypeString: Color get() = p.typeString
    val TypeHash: Color get() = p.typeHash
    val TypeList: Color get() = p.typeList
    val TypeSet: Color get() = p.typeSet
    val TypeZset: Color get() = p.typeZset
    /** Keyboard focus ring. */
    val FocusRing: Color get() = p.focusRing
    val SplitterHover: Color get() = p.splitterHover
    val PrimaryDark: Color get() = p.primaryFill
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
    val dark = when (RaksysSettings.appearance) {
        Appearance.SYSTEM -> isSystemInDarkTheme()
        Appearance.LIGHT -> false
        Appearance.DARK -> true
    }
    // Written before any child reads a color, so the first frame already uses the right palette.
    if (RaksysThemeColors.isDark != dark) RaksysThemeColors.isDark = dark

    val colorScheme = if (dark) {
        darkColorScheme(
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
    } else {
        lightColorScheme(
            primary = RaksysThemeColors.Primary,
            onPrimary = Color.White,
            primaryContainer = RaksysThemeColors.PrimaryContainer,
            onPrimaryContainer = Color(0xFF0F2A5C),
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
            onErrorContainer = Color(0xFF7F1D1D),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RaksysTypography,
        content = content
    )
}
