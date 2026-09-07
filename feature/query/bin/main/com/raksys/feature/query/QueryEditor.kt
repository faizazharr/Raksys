package com.raksys.feature.query

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.TableSchema
import com.raksys.core.model.UiState
import com.raksys.core.ui.*
import com.raksys.feature.grid.DataGrid
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val TABLE_PAGE_SIZE = 100

private enum class QueryWorkspaceView {
    CONSOLE,
    TABLE_DATA
}

@Composable
fun QueryEditor(
    profile: ConnectionProfile,
    browsingTable: TableSchema? = null,
    modifier: Modifier = Modifier,
) {
    val presenter = koinInject<QueryPresenter>()
    val state by presenter.state.collectAsState()
    val history by presenter.history.collectAsState()
    val scope = rememberCoroutineScope()

    var activeView by remember { mutableStateOf(QueryWorkspaceView.CONSOLE) }
    var userScratchpadSql by remember { mutableStateOf("SELECT 1 AS id, 'Hello Raksys' AS message;") }
    var editorHeightDp by remember { mutableStateOf(180.dp) }
    var isHistoryOpen by remember { mutableStateOf(false) }
    var pendingDestructiveSql by remember { mutableStateOf<String?>(null) }

    val triggerExecution: (String) -> Unit = { rawSql ->
        val sql = rawSql.trim()
        if (profile.environment == com.raksys.core.model.EnvironmentType.PRODUCTION && isDestructiveSql(sql)) {
            pendingDestructiveSql = sql
        } else {
            userScratchpadSql = sql
            scope.launch { presenter.onEvent(QueryEvent.Execute(profile, sql)) }
        }
    }

    val textArea = remember {
        createSqlTextArea(
            onExecute = {
                triggerExecution(userScratchpadSql)
            }
        )
    }

    // Connect textArea execution callback
    LaunchedEffect(Unit) {
        textArea.bindExecutionShortcut {
            triggerExecution(textArea.text)
        }
    }

    var page by remember(browsingTable?.name) { mutableIntStateOf(0) }

    // When a table is selected in the navigator, switch to TABLE_DATA view safely without overwriting user scratchpad!
    LaunchedEffect(browsingTable?.name) {
        val table = browsingTable ?: return@LaunchedEffect
        activeView = QueryWorkspaceView.TABLE_DATA
        page = 0
        val sql = "SELECT * FROM ${table.name} LIMIT $TABLE_PAGE_SIZE OFFSET 0;"
        presenter.onEvent(QueryEvent.Execute(profile, sql))
    }

    // Handle pagination inside TABLE_DATA view
    LaunchedEffect(page) {
        val table = browsingTable ?: return@LaunchedEffect
        if (activeView == QueryWorkspaceView.TABLE_DATA) {
            val sql = "SELECT * FROM ${table.name} LIMIT $TABLE_PAGE_SIZE OFFSET ${page * TABLE_PAGE_SIZE};"
            presenter.onEvent(QueryEvent.Execute(profile, sql))
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(RaksysThemeColors.Background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
        // --- View Mode Selector Bar (Console vs Table Data) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RaksysThemeColors.SurfaceElevated)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Console Tab Pill
                val isConsole = activeView == QueryWorkspaceView.CONSOLE
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isConsole) RaksysThemeColors.PrimaryContainer else Color.Transparent)
                        .clickable {
                            if (!isConsole) {
                                activeView = QueryWorkspaceView.CONSOLE
                                textArea.text = userScratchpadSql
                                scope.launch { presenter.onEvent(QueryEvent.Execute(profile, userScratchpadSql)) }
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚡ SQL Console",
                        fontSize = 11.sp,
                        fontWeight = if (isConsole) FontWeight.Bold else FontWeight.Normal,
                        color = if (isConsole) RaksysThemeColors.Primary else RaksysThemeColors.TextSecondary
                    )
                }

                // Table Data Tab Pill (if any table selected)
                browsingTable?.let { table ->
                    val isTable = activeView == QueryWorkspaceView.TABLE_DATA
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isTable) RaksysThemeColors.PrimaryContainer else Color.Transparent)
                            .clickable {
                                if (!isTable) {
                                    // Save current console query before switching
                                    userScratchpadSql = textArea.text
                                    activeView = QueryWorkspaceView.TABLE_DATA
                                    val sql = "SELECT * FROM ${table.name} LIMIT $TABLE_PAGE_SIZE OFFSET ${page * TABLE_PAGE_SIZE};"
                                    scope.launch { presenter.onEvent(QueryEvent.Execute(profile, sql)) }
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📋 Data: ${table.name}",
                            fontSize = 11.sp,
                            fontWeight = if (isTable) FontWeight.Bold else FontWeight.Normal,
                            color = if (isTable) RaksysThemeColors.Primary else RaksysThemeColors.TextSecondary
                        )
                    }
                }
            }

            if (activeView == QueryWorkspaceView.TABLE_DATA && browsingTable != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(RaksysThemeColors.Surface)
                        .clickable {
                            // Copy table SQL into console and switch to console view
                            val sql = "SELECT * FROM ${browsingTable.name} LIMIT $TABLE_PAGE_SIZE OFFSET ${page * TABLE_PAGE_SIZE};"
                            userScratchpadSql = sql
                            textArea.text = sql
                            activeView = QueryWorkspaceView.CONSOLE
                            ToastManager.show("Kueri tabel dimuat ke SQL Console")
                        }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Buka di Console ↗", fontSize = 10.sp, color = RaksysThemeColors.Primary)
                }
            }
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        // --- SQL Console Toolbar & Editor (Visible in CONSOLE mode) ---
        if (activeView == QueryWorkspaceView.CONSOLE) {
            QueryToolbar(
                profile = profile,
                isRunning = state is UiState.Loading,
                onExecute = {
                    triggerExecution(textArea.text)
                },
                onCancel = { scope.launch { presenter.onEvent(QueryEvent.Cancel) } },
                onFormat = {
                    textArea.text = formatSql(textArea.text)
                    userScratchpadSql = textArea.text
                    ToastManager.show("SQL berhasil diformat")
                },
                historyCount = history.size,
                onToggleHistory = { isHistoryOpen = !isHistoryOpen }
            )

            HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

            SqlCodeEditor(textArea = textArea, modifier = Modifier.fillMaxWidth().height(editorHeightDp))

            // Draggable Horizontal Splitter!
            ResizableHorizontalDivider(
                onDragDelta = { delta ->
                    editorHeightDp = (editorHeightDp + delta.dp).coerceIn(80.dp, 500.dp)
                }
            )
        }

        QueryStatusBar(state)

        // --- Pagination Bar (Visible in TABLE_DATA mode) ---
        if (activeView == QueryWorkspaceView.TABLE_DATA && browsingTable != null) {
            HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)
            val rowsOnPage = (state as? UiState.Success)?.data?.rows?.size ?: 0
            TablePaginationBar(
                tableName = browsingTable.name,
                page = page,
                pageSize = TABLE_PAGE_SIZE,
                rowsOnPage = rowsOnPage,
                isLoading = state is UiState.Loading,
                onPrev = { if (page > 0) page -= 1 },
                onNext = { if (rowsOnPage >= TABLE_PAGE_SIZE) page += 1 },
            )
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        // --- Results Container ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(RaksysThemeColors.Background)
        ) {
            when (val current = state) {
                is UiState.Idle -> {
                    RaksysEmptyState(
                        iconLabel = "⌨️",
                        title = "Editor SQL Siap",
                        description = "Tuliskan kueri SQL di atas atau pilih tabel di navigator kiri untuk menjelajahi datanya per halaman.",
                        actionButtonText = "Jalankan Kueri Contoh",
                        onActionClick = {
                            if (textArea.text.isBlank()) {
                                textArea.text = "SELECT 1 AS id, 'Hello Raksys' AS message;"
                            }
                            userScratchpadSql = textArea.text
                            scope.launch { presenter.onEvent(QueryEvent.Execute(profile, textArea.text)) }
                        }
                    )
                }

                is UiState.Loading -> {
                    RaksysLoadingState(
                        title = "Menjalankan Query...",
                        subtitle = "Mengirim kueri ke database server dan memproses result set"
                    )
                }

                is UiState.Error -> {
                    RaksysErrorState(
                        title = "Sintaks atau Eksekusi Query Gagal",
                        errorMessage = current.message,
                        onRetry = {
                            val sql = if (activeView == QueryWorkspaceView.CONSOLE) textArea.text else "SELECT * FROM ${browsingTable?.name} LIMIT $TABLE_PAGE_SIZE OFFSET ${page * TABLE_PAGE_SIZE};"
                            scope.launch { presenter.onEvent(QueryEvent.Execute(profile, sql)) }
                        }
                    )
                }

                is UiState.Success -> {
                    DataGrid(result = current.data)
                }
            }
        }
    }

    if (isHistoryOpen) {
        VerticalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)
        QueryHistoryDrawer(
            history = history,
            onSelectQuery = { sql ->
                activeView = QueryWorkspaceView.CONSOLE
                textArea.text = sql
                userScratchpadSql = sql
                ToastManager.show("Kueri dimuat ke SQL Console")
            },
            onClear = {
                scope.launch { presenter.onEvent(QueryEvent.ClearHistory) }
                ToastManager.show("Riwayat kueri dibersihkan")
            },
            onClose = { isHistoryOpen = false }
        )
    }
}

    pendingDestructiveSql?.let { sql ->
        ProductionSafeguardDialog(
            sql = sql,
            profileName = profile.name,
            onConfirm = {
                pendingDestructiveSql = null
                userScratchpadSql = sql
                scope.launch { presenter.onEvent(QueryEvent.Execute(profile, sql)) }
            },
            onDismiss = { pendingDestructiveSql = null }
        )
    }
}
