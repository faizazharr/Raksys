package com.raksys.core.ui

/** Desktop platform conventions that change what the UI should say (⌘ on macOS, Ctrl elsewhere). */
object RaksysPlatform {
    val isMac: Boolean = System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true)

    /** The primary shortcut modifier as shown to people: "⌘" on macOS, "Ctrl+" elsewhere. */
    val modLabel: String = if (isMac) "⌘" else "Ctrl+"

    /** Label for a shortcut such as `shortcut("K")` → "⌘K" or "Ctrl+K". */
    fun shortcut(key: String): String = modLabel + key
}
