package com.raksys.feature.query

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import com.raksys.core.model.QueryResult
import com.raksys.feature.grid.DataGrid
import kotlinx.coroutines.launch
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane

@Composable
fun QueryEditor(onExecute: suspend (String) -> Result<QueryResult>) {
    val textArea = remember {
        RSyntaxTextArea().apply {
            syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_SQL
        }
    }
    var result by remember { mutableStateOf<QueryResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column {
        SwingPanel(
            factory = { RTextScrollPane(textArea) },
            modifier = Modifier.fillMaxWidth().height(200.dp),
        )
        Button(onClick = {
            val sql = textArea.text
            scope.launch {
                onExecute(sql)
                    .onSuccess { result = it; error = null }
                    .onFailure { error = it.message; result = null }
            }
        }) {
            Text("Execute")
        }
        error?.let { Text(it) }
        result?.let { DataGrid(it) }
    }
}
