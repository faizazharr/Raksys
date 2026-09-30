package com.raksys.feature.query

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.raksys.core.ui.RaksysModals
import com.raksys.core.ui.RaksysThemeColors
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane

@Composable
fun SqlCodeEditor(textArea: RSyntaxTextArea, modifier: Modifier = Modifier) {
    // Reading isDark here re-runs this body (and the update block below) when the appearance changes.
    val dark = RaksysThemeColors.isDark
    val chrome = RaksysThemeColors.Surface
    SideEffect { textArea.applyEditorTheme(dark) }
    Box(modifier = modifier.background(chrome)) {
        // A Swing component paints over Compose dialogs, so step aside while one is open. The text lives
        // in [textArea], which survives, and the editor comes back when the dialog closes.
        if (!RaksysModals.isOpen) {
            SwingPanel(
                factory = {
                    RTextScrollPane(textArea).apply { border = null }
                },
                update = { pane ->
                    val awt = java.awt.Color(chrome.red, chrome.green, chrome.blue)
                    pane.verticalScrollBar.background = awt
                    pane.horizontalScrollBar.background = awt
                    pane.repaint()
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Applies RSyntaxTextArea's bundled dark or light (default) syntax theme, so the editor matches the
 * app appearance. Falls back to plain colors if the theme resource is missing.
 */
private fun RSyntaxTextArea.applyEditorTheme(dark: Boolean) {
    val resource = if (dark) "dark.xml" else "default.xml"
    try {
        val theme = org.fife.ui.rsyntaxtextarea.Theme.load(
            javaClass.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/$resource")
        )
        theme.apply(this)
    } catch (_: Exception) {
        background = if (dark) java.awt.Color(0x16, 0x1B, 0x22) else java.awt.Color.WHITE
        foreground = if (dark) java.awt.Color(0xF0, 0xF6, 0xFC) else java.awt.Color(0x1F, 0x29, 0x37)
        caretColor = if (dark) java.awt.Color.WHITE else java.awt.Color.BLACK
    }
}

fun createSqlTextArea(onExecute: (() -> Unit)? = null): RSyntaxTextArea = RSyntaxTextArea().apply {
    syntaxEditingStyle = org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_SQL
    isCodeFoldingEnabled = true
    applyEditorTheme(RaksysThemeColors.isDark)

    if (onExecute != null) {
        bindExecutionShortcut(onExecute)
    }
}

fun RSyntaxTextArea.bindExecutionShortcut(onExecute: () -> Unit) {
    val executeAction = object : javax.swing.AbstractAction() {
        override fun actionPerformed(e: java.awt.event.ActionEvent?) {
            onExecute()
        }
    }
    // Meta (Cmd on macOS) + Enter
    inputMap.put(
        javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, java.awt.Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx),
        "raksys.executeSql"
    )
    // Ctrl + Enter
    inputMap.put(
        javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, java.awt.event.InputEvent.CTRL_DOWN_MASK),
        "raksys.executeSql"
    )
    // Explicit Cmd + Enter for macOS AWT
    inputMap.put(
        javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, java.awt.event.InputEvent.META_DOWN_MASK),
        "raksys.executeSql"
    )
    actionMap.put("raksys.executeSql", executeAction)
}

fun formatSql(rawSql: String): String {
    if (rawSql.isBlank()) return rawSql
    var formatted = rawSql.trim()
    val keywords = listOf(
        "select", "from", "where", "left join", "right join", "inner join", "cross join", "join",
        "group by", "order by", "having", "limit", "offset", "insert into", "values",
        "update", "set", "delete from", "create table", "drop table", "alter table",
        "and", "or", "as", "distinct", "desc", "asc", "null", "not null", "is null", "is not null",
        "primary key", "foreign key", "references", "default", "count", "sum", "avg", "min", "max"
    )
    for (kw in keywords) {
        val regex = Regex("\\b$kw\\b", RegexOption.IGNORE_CASE)
        formatted = regex.replace(formatted) { it.value.uppercase() }
    }
    return formatted
}
