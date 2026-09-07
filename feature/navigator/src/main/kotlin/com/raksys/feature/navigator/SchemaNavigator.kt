package com.raksys.feature.navigator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raksys.core.model.TableSchema

@Composable
fun SchemaNavigator(tables: List<TableSchema>, onSelectTable: (TableSchema) -> Unit) {
    Column(modifier = Modifier.padding(4.dp)) {
        tables.forEach { table ->
            Text(
                text = table.name,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .clickable { onSelectTable(table) },
            )
        }
    }
}
