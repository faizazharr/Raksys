package com.raksys.feature.navigator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.TableSchema
import com.raksys.core.model.UiState
import com.raksys.core.ui.RaksysEmptyState
import com.raksys.core.ui.RaksysErrorState
import com.raksys.core.ui.RaksysLoadingState
import com.raksys.core.ui.RaksysThemeColors
import com.raksys.core.ui.TableGridIcon
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SchemaNavigator(
    profile: ConnectionProfile,
    selectedTable: TableSchema? = null,
    onSelectTable: (TableSchema) -> Unit,
) {
    val presenter = koinInject<NavigatorPresenter>()
    val state by presenter.state.collectAsState()
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(profile.id) { presenter.onEvent(NavigatorEvent.Load(profile)) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(RaksysThemeColors.Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(RaksysThemeColors.PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        TableGridIcon(color = RaksysThemeColors.Primary, size = 13.dp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tables & Views",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RaksysThemeColors.TextPrimary
                    )
                }

                IconButton(
                    onClick = { scope.launch { presenter.onEvent(NavigatorEvent.Load(profile)) } },
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(RaksysThemeColors.SurfaceElevated)
                ) {
                    Text("🔄", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter tabel...", fontSize = 11.sp, color = RaksysThemeColors.TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RaksysThemeColors.Primary,
                    unfocusedBorderColor = RaksysThemeColors.Border,
                    focusedContainerColor = RaksysThemeColors.Background,
                    unfocusedContainerColor = RaksysThemeColors.Background
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            )
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        Box(modifier = Modifier.weight(1f)) {
            when (val current = state) {
                is UiState.Idle -> {}

                is UiState.Loading -> {
                    RaksysLoadingState(
                        title = "Memuat Skema...",
                        subtitle = "Mengambil daftar tabel dari ${profile.name}"
                    )
                }

                is UiState.Error -> {
                    RaksysErrorState(
                        title = "Gagal Membaca Skema",
                        errorMessage = current.message,
                        onRetry = { scope.launch { presenter.onEvent(NavigatorEvent.Load(profile)) } }
                    )
                }

                is UiState.Success -> {
                    val filteredTables = if (searchQuery.isBlank()) {
                        current.data
                    } else {
                        current.data.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    }

                    if (filteredTables.isEmpty()) {
                        RaksysEmptyState(
                            iconContent = {
                                TableGridIcon(color = RaksysThemeColors.TextMuted, size = 26.dp)
                            },
                            title = if (searchQuery.isBlank()) "Tidak Ada Tabel" else "Tabel Tidak Ditemukan",
                            description = if (searchQuery.isBlank()) {
                                "Database '${profile.database.ifBlank { profile.name }}' belum memiliki tabel data."
                            } else {
                                "Tidak ada tabel yang cocok dengan '$searchQuery'."
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(6.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            items(filteredTables, key = { it.name }) { table ->
                                val isSelected = selectedTable?.name == table.name
                                TableItemRow(
                                    table = table,
                                    isSelected = isSelected,
                                    onClick = { onSelectTable(table) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
