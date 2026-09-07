package com.raksys.feature.document

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

private fun formatJsonPretty(raw: String): String {
    if (raw.isBlank()) return raw
    val sb = StringBuilder()
    var indent = 0
    var inQuotes = false
    var escape = false

    for (char in raw) {
        if (escape) {
            sb.append(char)
            escape = false
            continue
        }
        if (char == '\\') {
            sb.append(char)
            escape = true
            continue
        }
        if (char == '"') {
            inQuotes = !inQuotes
            sb.append(char)
            continue
        }
        if (inQuotes) {
            sb.append(char)
            continue
        }

        when (char) {
            '{', '[' -> {
                sb.append(char)
                indent++
                sb.append("\n").append("  ".repeat(indent))
            }
            '}', ']' -> {
                indent = maxOf(0, indent - 1)
                sb.append("\n").append("  ".repeat(indent))
                sb.append(char)
            }
            ',' -> {
                sb.append(char)
                sb.append("\n").append("  ".repeat(indent))
            }
            ':' -> {
                sb.append(": ")
            }
            ' ', '\n', '\r', '\t' -> {
                // skip excessive raw whitespace outside strings
            }
            else -> sb.append(char)
        }
    }
    return sb.toString()
}

@Composable
fun DocumentCollectionList(
    profile: ConnectionProfile,
    selectedCollection: String?,
    onSelectCollection: (String) -> Unit,
) {
    val presenter = koinInject<DocumentPresenter>()
    val state by presenter.collections.collectAsState()
    val scope = rememberCoroutineScope()
    var filterText by remember { mutableStateOf("") }

    LaunchedEffect(profile.id) { presenter.onEvent(DocumentEvent.LoadCollections(profile)) }

    Column(modifier = Modifier.fillMaxHeight().background(RaksysThemeColors.Surface)) {
        // Unified Header (46.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RaksysThemeColors.MongodbColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    MongodbLogo(color = RaksysThemeColors.MongodbColor, size = 13.dp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Collections",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaksysThemeColors.TextPrimary
                )
            }

            IconButton(
                onClick = { scope.launch { presenter.onEvent(DocumentEvent.LoadCollections(profile)) } },
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(RaksysThemeColors.SurfaceElevated)
            ) {
                Text("🔄", fontSize = 11.sp)
            }
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        // Filter collections
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                placeholder = { Text("Cari collection...", fontSize = 11.sp, color = RaksysThemeColors.TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RaksysThemeColors.Primary,
                    unfocusedBorderColor = RaksysThemeColors.Border,
                    focusedContainerColor = RaksysThemeColors.Background,
                    unfocusedContainerColor = RaksysThemeColors.Background
                ),
                modifier = Modifier.fillMaxWidth().height(32.dp)
            )
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        Box(modifier = Modifier.weight(1f)) {
            when (val current = state) {
                is UiState.Idle -> {}
                is UiState.Loading -> RaksysLoadingState(title = "Memuat Collections...")
                is UiState.Error -> RaksysErrorState(
                    errorMessage = current.message,
                    onRetry = { scope.launch { presenter.onEvent(DocumentEvent.LoadCollections(profile)) } },
                )
                is UiState.Success -> {
                    val filtered = current.data.filter { filterText.isBlank() || it.name.contains(filterText, ignoreCase = true) }
                    if (filtered.isEmpty()) {
                        RaksysEmptyState(iconLabel = "🍃", title = "Tidak Ada Collection", description = if (filterText.isBlank()) "Database ini belum punya collection." else "Tidak ada collection cocok.")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(filtered, key = { it.name }) { collection ->
                                val isSelected = collection.name == selectedCollection
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) RaksysThemeColors.PrimaryContainer else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) RaksysThemeColors.Primary else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { onSelectCollection(collection.name) }
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🗂️", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = collection.name,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) RaksysThemeColors.Primary else RaksysThemeColors.TextPrimary
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

@Composable
fun DocumentViewer(
    profile: ConnectionProfile,
    collection: String,
    modifier: Modifier = Modifier,
) {
    val presenter = koinInject<DocumentPresenter>()
    val state by presenter.documents.collectAsState()
    val scope = rememberCoroutineScope()
    var searchDocQuery by remember { mutableStateOf("") }
    var isPrettyFormatted by remember { mutableStateOf(true) }

    LaunchedEffect(collection) { presenter.onEvent(DocumentEvent.LoadDocuments(profile, collection)) }

    Column(modifier = modifier.fillMaxSize().background(RaksysThemeColors.Background)) {
        // Unified Header (46.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(RaksysThemeColors.Surface)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RaksysThemeColors.MongodbColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    MongodbLogo(color = RaksysThemeColors.MongodbColor, size = 13.dp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = collection,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RaksysThemeColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = searchDocQuery,
                    onValueChange = { searchDocQuery = it },
                    placeholder = { Text("Filter _id / keyword dokumen...", fontSize = 11.sp, color = RaksysThemeColors.TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RaksysThemeColors.Primary,
                        unfocusedBorderColor = RaksysThemeColors.Border,
                        focusedContainerColor = RaksysThemeColors.Background,
                        unfocusedContainerColor = RaksysThemeColors.Background
                    ),
                    modifier = Modifier.widthIn(max = 260.dp).height(32.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Format toggle button
                OutlinedButton(
                    onClick = { isPrettyFormatted = !isPrettyFormatted },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RaksysThemeColors.TextSecondary),
                    shape = RoundedCornerShape(5.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(if (isPrettyFormatted) "Mode: Rapi (Pretty)" else "Mode: Compact", fontSize = 10.sp)
                }

                IconButton(
                    onClick = { scope.launch { presenter.onEvent(DocumentEvent.LoadDocuments(profile, collection)) } },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(RaksysThemeColors.SurfaceElevated)
                ) {
                    Text("🔄", fontSize = 11.sp)
                }
            }
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val current = state) {
                is UiState.Idle -> {}
                is UiState.Loading -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                    RaksysLoadingState(title = "Memuat Dokumen...", modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { scope.launch { presenter.onEvent(DocumentEvent.Cancel) } },
                        modifier = Modifier.padding(bottom = 16.dp),
                    ) { Text("Batalkan") }
                }
                is UiState.Error -> RaksysErrorState(
                    errorMessage = current.message,
                    onRetry = { scope.launch { presenter.onEvent(DocumentEvent.LoadDocuments(profile, collection)) } },
                )
                is UiState.Success -> {
                    val filteredDocs = current.data.filter {
                        searchDocQuery.isBlank() || it.id.contains(searchDocQuery, ignoreCase = true) || it.json.contains(searchDocQuery, ignoreCase = true)
                    }

                    if (filteredDocs.isEmpty()) {
                        RaksysEmptyState(
                            iconLabel = "📭",
                            title = "Collection Kosong",
                            description = if (searchDocQuery.isBlank()) "'$collection' tidak punya dokumen." else "Tidak ada dokumen yang cocok dengan filter."
                        )
                    } else {
                        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredDocs, key = { it.id }) { document ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(RaksysThemeColors.Surface)
                                        .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "_id: ${document.id}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RaksysThemeColors.Primary,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        OutlinedButton(
                                            onClick = {
                                                copyToClipboard(document.json)
                                                ToastManager.show("JSON dokumen disalin")
                                            },
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("📋 Salin JSON", fontSize = 10.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val displayText = if (isPrettyFormatted) formatJsonPretty(document.json) else document.json
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(RaksysThemeColors.Background)
                                            .padding(10.dp)
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        Text(
                                            text = displayText,
                                            fontSize = 11.sp,
                                            color = RaksysThemeColors.TextPrimary,
                                            fontFamily = FontFamily.Monospace
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
