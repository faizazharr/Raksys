package com.raksys.core.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsStoreTest {

    private fun tempFile(): File = kotlin.io.path.createTempDirectory("raksys-settings-test").toFile()
        .apply { deleteOnExit() }
        .let { File(it, "nested/settings.json") }

    @Test
    fun `a missing file gives defaults`() {
        assertEquals(SettingsData(), SettingsStore(tempFile()).read())
    }

    @Test
    fun `settings round-trip through the file, creating parent folders`() {
        val store = SettingsStore(tempFile())
        store.write(SettingsData(appearance = Appearance.LIGHT, saveQueryHistory = false))
        assertEquals(SettingsData(Appearance.LIGHT, false), store.read())
    }

    @Test
    fun `a corrupt file gives defaults instead of crashing`() {
        val file = tempFile().also { it.parentFile.mkdirs(); it.writeText("{ nope") }
        assertEquals(SettingsData(), SettingsStore(file).read())
    }

    @Test
    fun `unknown keys from a newer version are ignored`() {
        val file = tempFile().also { it.parentFile.mkdirs(); it.writeText("""{"appearance":"DARK","futureThing":42}""") }
        assertEquals(Appearance.DARK, SettingsStore(file).read().appearance)
    }

    @Test
    fun `every color token has a readable text pair in both palettes`() {
        // Guards the palettes against silent contrast regressions on the main text tokens.
        fun lum(c: androidx.compose.ui.graphics.Color): Double {
            fun f(v: Float) = if (v <= 0.03928f) v / 12.92 else Math.pow(((v + 0.055) / 1.055), 2.4)
            return 0.2126 * f(c.red) + 0.7152 * f(c.green) + 0.0722 * f(c.blue)
        }
        fun ratio(a: androidx.compose.ui.graphics.Color, b: androidx.compose.ui.graphics.Color): Double {
            val (hi, lo) = lum(a).let { la -> lum(b).let { lb -> if (la >= lb) la to lb else lb to la } }
            return (hi + 0.05) / (lo + 0.05)
        }
        for (dark in listOf(true, false)) {
            RaksysThemeColors.isDark = dark
            val surfaces = listOf(RaksysThemeColors.Background, RaksysThemeColors.Surface, RaksysThemeColors.SurfaceElevated)
            val texts = listOf(RaksysThemeColors.TextPrimary, RaksysThemeColors.TextSecondary, RaksysThemeColors.TextMuted, RaksysThemeColors.Primary)
            for (s in surfaces) for (t in texts) {
                check(ratio(t, s) >= 4.5) { "dark=$dark text $t on $s is ${ratio(t, s)}" }
            }
            val pairs = listOf(
                RaksysThemeColors.Success to RaksysThemeColors.SuccessBg,
                RaksysThemeColors.Error to RaksysThemeColors.ErrorBg,
                RaksysThemeColors.Warning to RaksysThemeColors.WarningBg,
                RaksysThemeColors.Info to RaksysThemeColors.InfoBg,
                RaksysThemeColors.EnvDevText to RaksysThemeColors.EnvDevBg,
                RaksysThemeColors.EnvStagingText to RaksysThemeColors.EnvStagingBg,
                RaksysThemeColors.EnvProdText to RaksysThemeColors.EnvProdBg,
                RaksysThemeColors.PostgresColor to RaksysThemeColors.PostgresBg,
                RaksysThemeColors.MysqlColor to RaksysThemeColors.MysqlBg,
                RaksysThemeColors.SqliteColor to RaksysThemeColors.SqliteBg,
                RaksysThemeColors.MongodbColor to RaksysThemeColors.MongodbBg,
                RaksysThemeColors.RedisColor to RaksysThemeColors.RedisBg,
                RaksysThemeColors.Purple to RaksysThemeColors.PurpleBg,
                androidx.compose.ui.graphics.Color.White to RaksysThemeColors.PrimaryFill,
                androidx.compose.ui.graphics.Color.White to RaksysThemeColors.EnvProdFill,
                androidx.compose.ui.graphics.Color.White to RaksysThemeColors.SuccessFill,
            )
            for ((fg, bg) in pairs) check(ratio(fg, bg) >= 4.5) { "dark=$dark $fg on $bg is ${ratio(fg, bg)}" }
        }
        RaksysThemeColors.isDark = true
    }
}
