package com.raksys.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.raksys.core.database.JdbcDatabaseDriver
import com.raksys.core.model.ConnectionProfile
import com.raksys.feature.connection.ConnectionListScreen
import com.raksys.feature.connection.ConnectionRepository
import com.raksys.feature.query.QueryEditor

fun main() = application {
    val repository = remember { ConnectionRepository() }
    val driver = remember { JdbcDatabaseDriver() }
    var selected by remember { mutableStateOf<ConnectionProfile?>(null) }

    Window(onCloseRequest = ::exitApplication, title = "Raksys") {
        MaterialTheme {
            Row {
                Box(modifier = Modifier.width(240.dp)) {
                    ConnectionListScreen(repository = repository, onSelect = { selected = it })
                }
                selected?.let { profile ->
                    QueryEditor(onExecute = { sql ->
                        driver.executeQuery(profile, password = "", sql = sql)
                    })
                }
            }
        }
    }
}
