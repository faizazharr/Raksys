package com.raksys.feature.connection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raksys.core.model.ConnectionProfile

@Composable
fun ConnectionListScreen(
    repository: ConnectionRepository,
    onSelect: (ConnectionProfile) -> Unit,
) {
    var profiles by remember { mutableStateOf(repository.list()) }

    Column(modifier = Modifier.padding(8.dp)) {
        Text("Connections")
        LazyColumn {
            items(profiles) { profile ->
                ListItem(
                    headlineContent = { Text(profile.name) },
                    supportingContent = { Text("${profile.dbType} @ ${profile.host}:${profile.port}") },
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .clickable { onSelect(profile) },
                )
            }
        }
    }
}
