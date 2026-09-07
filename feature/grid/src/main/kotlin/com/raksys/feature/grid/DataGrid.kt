package com.raksys.feature.grid

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raksys.core.model.QueryResult

@Composable
fun DataGrid(result: QueryResult) {
    LazyColumn {
        item {
            Row {
                result.columns.forEach { column ->
                    Text(column, modifier = Modifier.padding(4.dp))
                }
            }
        }
        items(result.rows) { row ->
            Row {
                row.forEach { cell ->
                    Text(cell?.toString() ?: "NULL", modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}
