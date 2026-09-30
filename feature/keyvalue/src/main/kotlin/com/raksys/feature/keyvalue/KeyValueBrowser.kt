package com.raksys.feature.keyvalue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.RedisEntry
import com.raksys.core.model.UiState
import com.raksys.core.ui.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

private fun copyToClipboard(text: String) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    } catch (_: Exception) {}
}

@Composable
fun KeyValueBrowser(
    profile: ConnectionProfile,
    modifier: Modifier = Modifier,
) {
    val presenter = koinInject<KeyValuePresenter>()
    val state by presenter.state.collectAsState()
    val scope = rememberCoroutineScope()
    var pattern by remember { mutableStateOf("*") }
    var inspectingEntry by remember { mutableStateOf<RedisEntry?>(null) }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }

    LaunchedEffect(profile.id) { presenter.onEvent(KeyValueEvent.Scan(profile, pattern)) }

    Column(modifier = modifier.fillMaxSize().background(RaksysThemeColors.Background)) {
        // Standardized Header (46.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(RaksysThemeColors.Surface)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RaksysThemeColors.RedisColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    RedisLogo(color = RaksysThemeColors.RedisColor, size = 13.dp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Redis Keys",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaksysThemeColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))

                RaksysSearchField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    placeholder = "Pattern (cth: * atau user:*)",
                    modifier = Modifier.widthIn(max = 280.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { scope.launch { presenter.onEvent(KeyValueEvent.Scan(profile, pattern)) } },
                    enabled = state !is UiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RaksysThemeColors.PrimaryFill,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (state is UiState.Loading) {
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = { scope.launch { presenter.onEvent(KeyValueEvent.Cancel) } },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Cancel", fontSize = 11.sp)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(RaksysThemeColors.SurfaceElevated)
                    .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${profile.name} • db:${profile.database.ifBlank { "0" }}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = RaksysThemeColors.TextSecondary
                )
            }
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        Box(modifier = Modifier.weight(1f)) {
            when (val current = state) {
                is UiState.Idle -> {}
                is UiState.Loading -> RaksysLoadingState(title = "Scanning keys...")
                is UiState.Error -> RaksysErrorState(
                    errorMessage = current.message,
                    onRetry = { scope.launch { presenter.onEvent(KeyValueEvent.Scan(profile, pattern)) } },
                )
                is UiState.Success -> {
                    if (current.data.isEmpty()) {
                        RaksysEmptyState(iconContent = { KeyIcon(color = RaksysThemeColors.TextSecondary, size = 30.dp) }, title = "Tidak Ada Key", description = "Tidak ada key yang cocok dengan pola '$pattern'.")
                    } else {
                        val types = listOf("ALL", "string", "hash", "list", "set", "zset")
                        val filteredEntries = remember(current.data, selectedTypeFilter) {
                            if (selectedTypeFilter == "ALL") current.data
                            else current.data.filter { it.type.equals(selectedTypeFilter, ignoreCase = true) }
                        }

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Filter Chips Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(RaksysThemeColors.SurfaceElevated)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Tipe:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RaksysThemeColors.TextSecondary
                                )
                                types.forEach { typeKey ->
                                    val count = if (typeKey == "ALL") current.data.size else current.data.count { it.type.equals(typeKey, ignoreCase = true) }
                                    val isSelected = selectedTypeFilter == typeKey
                                    val chipColor = if (typeKey == "ALL") RaksysThemeColors.Primary else when (typeKey) {
                                        "string" -> RaksysThemeColors.TypeString
                                        "hash" -> RaksysThemeColors.TypeHash
                                        "list" -> RaksysThemeColors.TypeList
                                        "set" -> RaksysThemeColors.TypeSet
                                        "zset" -> RaksysThemeColors.TypeZset
                                        else -> RaksysThemeColors.TextSecondary
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) chipColor.copy(alpha = 0.2f) else Color.Transparent)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) chipColor else RaksysThemeColors.Border,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { selectedTypeFilter = typeKey }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "$typeKey ($count)",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) chipColor else RaksysThemeColors.TextSecondary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

                            if (filteredEntries.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Tidak ada key dengan tipe '$selectedTypeFilter'",
                                        fontSize = 12.sp,
                                        color = RaksysThemeColors.TextMuted
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(filteredEntries, key = { it.key }) { entry ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(RaksysThemeColors.Surface)
                                        .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(8.dp))
                                        .raksysInteractive(RoundedCornerShape(8.dp))
                                        .clickable { inspectingEntry = entry }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            entry.key,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = RaksysThemeColors.TextPrimary,
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RaksysStatusBadge(entry.type, RaksysThemeColors.Info, RaksysThemeColors.InfoBg)
                                            entry.ttlSeconds?.let {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                RaksysStatusBadge("TTL ${it}s", RaksysThemeColors.Warning, RaksysThemeColors.WarningBg)
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    copyToClipboard(entry.value)
                                                    ToastManager.show("Value Redis disalin")
                                                },
                                                shape = RoundedCornerShape(4.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(22.dp)
                                            ) {
                                                Text("Salin", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(RaksysThemeColors.Background)
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            entry.value,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = RaksysThemeColors.TextSecondary,
                                            maxLines = 3,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

    // Detail Value Inspector Modal
    inspectingEntry?.let { entry ->
        ModalGuard()
        AlertDialog(
            onDismissRequest = { inspectingEntry = null },
            title = {
                Text(
                    text = "Key: ${entry.key}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = RaksysThemeColors.TextPrimary
                )
            },
            text = {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RaksysStatusBadge("Tipe: ${entry.type}", RaksysThemeColors.Info, RaksysThemeColors.InfoBg)
                        entry.ttlSeconds?.let {
                            RaksysStatusBadge("TTL: ${it}s", RaksysThemeColors.Warning, RaksysThemeColors.WarningBg)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(RaksysThemeColors.Background)
                            .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(6.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(10.dp)
                    ) {
                        Text(
                            text = entry.value,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = RaksysThemeColors.TextPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        copyToClipboard(entry.value)
                        ToastManager.show("Value Redis disalin ke clipboard")
                        inspectingEntry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RaksysThemeColors.PrimaryFill)
                ) {
                    Text("Salin Value")
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectingEntry = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}
