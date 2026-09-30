package com.raksys.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.TableSchema
import com.raksys.core.ui.RaksysStatusBadge
import com.raksys.core.ui.RaksysThemeColors

enum class CommandCategory(val label: String, val color: Color, val bg: Color) {
    TABLE("Tabel", Color(0xFF60A5FA), Color(0xFF1E3A5F)),
    CONNECTION("Koneksi", Color(0xFF34D399), Color(0xFF133E2B)),
    NAVIGATION("Navigasi", Color(0xFFA78BFA), Color(0xFF2E1F4D)),
    ACTION("Aksi", Color(0xFFFBBF24), Color(0xFF45300F)),
}

data class PaletteItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val category: CommandCategory,
    val icon: String,
    val onExecute: () -> Unit,
)

@Composable
fun CommandPaletteDialog(
    currentProfile: ConnectionProfile?,
    connections: List<ConnectionProfile>,
    tables: List<TableSchema>,
    onSelectProfile: (ConnectionProfile) -> Unit,
    onSelectTable: (TableSchema) -> Unit,
    onSelectTab: (RelationalTab) -> Unit,
    onNewConnection: () -> Unit,
    onToggleSidebar: () -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()

    // Build items based on current context
    val allItems = remember(currentProfile, connections, tables) {
        val list = mutableListOf<PaletteItem>()

        // 1. Table items (if relational database active)
        tables.forEach { table ->
            list.add(
                PaletteItem(
                    id = "table-${table.name}",
                    title = table.name,
                    subtitle = "${table.columns.size} kolom • Buka data tabel",
                    category = CommandCategory.TABLE,
                    icon = "⊞",
                    onExecute = { onSelectTable(table) }
                )
            )
        }

        // 2. Navigation items
        if (currentProfile?.dbType?.family == com.raksys.core.model.DbFamily.RELATIONAL) {
            list.add(
                PaletteItem(
                    id = "nav-console",
                    title = "Buka SQL Console & Skema",
                    subtitle = "Tulis dan jalankan kueri SQL manual serta eksplorasi skema",
                    category = CommandCategory.NAVIGATION,
                    icon = "⚡",
                    onExecute = { onSelectTab(RelationalTab.QUERY) }
                )
            )
            list.add(
                PaletteItem(
                    id = "nav-erd",
                    title = "Buka Entity Relationship Diagram (ERD)",
                    subtitle = "Visualisasi relasi skema dan Bézier edge",
                    category = CommandCategory.NAVIGATION,
                    icon = "🗺️",
                    onExecute = { onSelectTab(RelationalTab.ERD) }
                )
            )
            list.add(
                PaletteItem(
                    id = "nav-perm",
                    title = "Buka Matriks Hak Akses (Permissions)",
                    subtitle = "Kelola izin SELECT, INSERT, UPDATE, DELETE",
                    category = CommandCategory.NAVIGATION,
                    icon = "🔐",
                    onExecute = { onSelectTab(RelationalTab.PERMISSIONS) }
                )
            )
        }

        // 3. Connection items
        connections.forEach { conn ->
            val isCurrent = currentProfile?.id == conn.id
            list.add(
                PaletteItem(
                    id = "conn-${conn.id}",
                    title = conn.name,
                    subtitle = "${conn.dbType.name} • ${conn.host}:${conn.port}${if (isCurrent) " (Aktif)" else ""}",
                    category = CommandCategory.CONNECTION,
                    icon = "🔌",
                    onExecute = { onSelectProfile(conn) }
                )
            )
        }

        // 4. Quick Studio Actions
        list.add(
            PaletteItem(
                id = "action-new-conn",
                title = "Tambah Koneksi Baru",
                subtitle = "Buka wizard setup database baru",
                category = CommandCategory.ACTION,
                icon = "➕",
                onExecute = onNewConnection
            )
        )
        list.add(
            PaletteItem(
                id = "action-toggle-sidebar",
                title = "Toggle Sidebar Koneksi (⌘B)",
                subtitle = "Tampilkan atau sembunyikan sidebar",
                category = CommandCategory.ACTION,
                icon = "◧",
                onExecute = onToggleSidebar
            )
        )

        list
    }

    // Filtered items
    val filteredItems = remember(allItems, query) {
        if (query.isBlank()) allItems
        else allItems.filter {
            it.title.contains(query, ignoreCase = true) ||
            (it.subtitle?.contains(query, ignoreCase = true) == true) ||
            it.category.label.contains(query, ignoreCase = true)
        }
    }

    // Keep selected index in bounds
    LaunchedEffect(filteredItems.size) {
        if (selectedIndex >= filteredItems.size) {
            selectedIndex = (filteredItems.size - 1).coerceAtLeast(0)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = RaksysThemeColors.Surface,
            tonalElevation = 12.dp,
            modifier = Modifier
                .width(580.dp)
                .heightIn(max = 480.dp)
                .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(12.dp))
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.DirectionDown -> {
                                if (filteredItems.isNotEmpty()) {
                                    selectedIndex = (selectedIndex + 1) % filteredItems.size
                                }
                                true
                            }
                            Key.DirectionUp -> {
                                if (filteredItems.isNotEmpty()) {
                                    selectedIndex = if (selectedIndex <= 0) filteredItems.size - 1 else selectedIndex - 1
                                }
                                true
                            }
                            Key.Enter -> {
                                if (filteredItems.isNotEmpty() && selectedIndex in filteredItems.indices) {
                                    filteredItems[selectedIndex].onExecute()
                                    onDismiss()
                                }
                                true
                            }
                            Key.Escape -> {
                                onDismiss()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Search Input Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RaksysThemeColors.SurfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔍", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            selectedIndex = 0
                        },
                        placeholder = { Text("Ketik perintah, nama tabel, atau koneksi...", fontSize = 13.sp, color = RaksysThemeColors.TextMuted) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }, modifier = Modifier.size(22.dp)) {
                            Text("✕", fontSize = 11.sp, color = RaksysThemeColors.TextMuted)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(RaksysThemeColors.Background)
                            .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ESC to close", fontSize = 10.sp, color = RaksysThemeColors.TextMuted)
                    }
                }

                HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

                // Results List
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ditemukan hasil untuk '$query'",
                            fontSize = 12.sp,
                            color = RaksysThemeColors.TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        contentPadding = PaddingValues(6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(filteredItems, key = { _, item -> item.id }) { index, item ->
                            val isSelected = index == selectedIndex

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) RaksysThemeColors.PrimaryContainer else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) RaksysThemeColors.Primary.copy(alpha = 0.5f) else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        item.onExecute()
                                        onDismiss()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.icon, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontFamily = if (item.category == CommandCategory.TABLE) FontFamily.Monospace else FontFamily.Default,
                                        color = if (isSelected) RaksysThemeColors.Primary else RaksysThemeColors.TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.subtitle != null) {
                                        Text(
                                            text = item.subtitle,
                                            fontSize = 10.sp,
                                            color = RaksysThemeColors.TextMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                RaksysStatusBadge(
                                    text = item.category.label,
                                    statusColor = item.category.color,
                                    bgColor = item.category.bg
                                )

                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("↵", fontSize = 12.sp, color = RaksysThemeColors.Primary)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

                // Footer Hint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RaksysThemeColors.SurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gunakan ↑ ↓ untuk navigasi, ↵ untuk memilih",
                        fontSize = 10.sp,
                        color = RaksysThemeColors.TextMuted
                    )
                    Text(
                        text = "${filteredItems.size} hasil",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RaksysThemeColors.TextMuted
                    )
                }
            }
        }
    }
}
