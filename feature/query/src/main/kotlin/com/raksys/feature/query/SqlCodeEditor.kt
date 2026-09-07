package com.raksys.feature.query

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.raksys.core.ui.RaksysThemeColors
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane

@Composable
fun SqlCodeEditor(textArea: RSyntaxTextArea, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(RaksysThemeColors.Surface)) {
        SwingPanel(
            factory = {
                RTextScrollPane(textArea).apply {
                    border = null
                    verticalScrollBar.background = java.awt.Color(0x16, 0x1B, 0x22)
                    horizontalScrollBar.background = java.awt.Color(0x16, 0x1B, 0x22)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

fun createSqlTextArea(onExecute: (() -> Unit)? = null): RSyntaxTextArea = RSyntaxTextArea().apply {
    syntaxEditingStyle = org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_SQL
    isCodeFoldingEnabled = true
    background = java.awt.Color(0x16, 0x1B, 0x22)
    currentLineHighlightColor = java.awt.Color(0x21, 0x26, 0x2D)
    caretColor = java.awt.Color.WHITE
    foreground = java.awt.Color(0xF0, 0xF6, 0xFC)
    try {
        val theme = org.fife.ui.rsyntaxtextarea.Theme.load(
            javaClass.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml")
        )
        theme.apply(this)
    } catch (_: Exception) {
        // Gunakan styling fallback jika resource theme dark bawaan tidak ada
    }

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
