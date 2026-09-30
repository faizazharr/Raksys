package com.raksys.feature.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import com.raksys.core.model.QueryResult
import com.raksys.core.ui.ModalGuard
import com.raksys.core.ui.SearchIcon
import com.raksys.core.ui.RaksysEmptyState
import com.raksys.core.ui.RaksysThemeColors
import com.raksys.core.ui.TableGridIcon
import com.raksys.core.ui.ToastManager
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

private enum class GridSortDirection { ASC, DESC }

private fun compareGridCells(a: Any?, b: Any?): Int {
    if (a == null && b == null) return 0
    if (a == null) return 1
    if (b == null) return -1
    val aStr = a.toString()
    val bStr = b.toString()
    val aNum = aStr.toDoubleOrNull()
    val bNum = bStr.toDoubleOrNull()
    if (aNum != null && bNum != null) {
        return aNum.compareTo(bNum)
    }
    return aStr.compareTo(bStr, ignoreCase = true)
}

private fun copyToClipboard(text: String) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    } catch (_: Exception) {
        // Fallback gracefully on headless or unsupported systems
    }
}

/** Longer text than this opens in the cell inspector when the cell is clicked. */
private const val INSPECT_THRESHOLD = 35

/** Cells never lay out more than this many characters; the full value is in the inspector. */
private const val CELL_DISPLAY_LIMIT = 200

@Composable
private fun GridRow(
    rowIndex: Int,
    row: List<Any?>,
    columnWidths: List<androidx.compose.ui.unit.Dp>,
    isRowSelected: Boolean,
    selectedColumn: Int?,
    onCellClick: (rowIndex: Int, colIndex: Int, fullText: String) -> Unit,
    onRowClick: (rowIndex: Int) -> Unit,
) {
    val rowBg = when {
        isRowSelected -> RaksysThemeColors.PrimaryContainer.copy(alpha = 0.35f)
        rowIndex % 2 == 0 -> RaksysThemeColors.Background
        else -> RaksysThemeColors.Surface
    }

    Row(
        modifier = Modifier
            .background(rowBg)
            .border(width = 0.5.dp, color = RaksysThemeColors.Border.copy(alpha = 0.35f))
            .clickable { onRowClick(rowIndex) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(46.dp)
                .padding(vertical = 5.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${rowIndex + 1}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isRowSelected) RaksysThemeColors.Primary else RaksysThemeColors.TextMuted
            )
        }

        row.forEachIndexed { colIndex, cell ->
            val colWidth = columnWidths.getOrElse(colIndex) { 150.dp }
            val isCellSelected = selectedColumn == colIndex

            Box(
                modifier = Modifier
                    .width(colWidth)
                    .border(
                        width = if (isCellSelected) 1.5.dp else 0.5.dp,
                        color = if (isCellSelected) RaksysThemeColors.Primary else RaksysThemeColors.Border.copy(alpha = 0.25f)
                    )
                    .clickable { onCellClick(rowIndex, colIndex, cell?.toString() ?: "NULL") }
                    .padding(vertical = 5.dp, horizontal = 8.dp)
            ) {
                if (cell == null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(RaksysThemeColors.SurfaceElevated)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "NULL",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RaksysThemeColors.TextMuted
                        )
                    }
                } else {
                    // Laying out a multi-megabyte string just to show one ellipsised line is what makes
                    // scrolling stall on wide JSON / text columns, so only a prefix is handed to Text.
                    val full = cell.toString()
                    Text(
                        text = if (full.length > CELL_DISPLAY_LIMIT) full.take(CELL_DISPLAY_LIMIT) + "…" else full,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isRowSelected) RaksysThemeColors.TextPrimary else RaksysThemeColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GridActionButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = RaksysThemeColors.TextSecondary),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = androidx.compose.ui.graphics.SolidColor(RaksysThemeColors.Border)
        ),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = Modifier.height(28.dp)
    ) {
        Text(label, fontSize = 11.sp)
    }
}

/** Asks where to save, writes the file, and reports the outcome in a toast. */
private fun saveExport(suggestedName: String, content: String, withBom: Boolean, rows: Int) {
    val file = chooseSaveFile(suggestedName) ?: return
    try {
        writeExport(file, content, withBom)
        ToastManager.show("$rows baris disimpan ke ${file.name}")
    } catch (e: Exception) {
        ToastManager.show("Gagal menyimpan file: ${e.message ?: "tidak diketahui"}", isError = true)
    }
}

@Composable
fun DataGrid(
    result: QueryResult,
    modifier: Modifier = Modifier,
) {
    if (result.rows.isEmpty()) {
        RaksysEmptyState(
            iconContent = {
                TableGridIcon(color = RaksysThemeColors.TextMuted, size = 28.dp)
            },
            title = "Hasil Kueri Kosong",
            description = "Kueri dieksekusi dalam ${result.executionTimeMs} ms, namun tidak ada baris data (0 rows) yang dikembalikan.",
            modifier = modifier
        )
        return
    }

    // Dynamic column width calculation based on header length and content sample
    val columnWidths = remember(result) {
        result.columns.mapIndexed { colIdx, colName ->
            val maxLen = maxOf(
                colName.length,
                result.rows.take(40).maxOfOrNull { row -> row.getOrNull(colIdx)?.toString()?.length ?: 4 } ?: 4
            )
            (maxLen * 8.5f + 26f).toInt().coerceIn(85, 340).dp
        }
    }

    var selectedRowIndex by remember { mutableStateOf<Int?>(null) }
    var selectedCellCoord by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var inspectingCell by remember { mutableStateOf<Pair<String, String>?>(null) } // (columnName, cellText)

    var filterQuery by remember { mutableStateOf("") }
    var sortState by remember { mutableStateOf<Pair<Int, GridSortDirection>?>(null) }

    // Filtering scans every cell of up to 10,000 rows, so wait for a short pause in typing.
    var appliedFilter by remember { mutableStateOf("") }
    LaunchedEffect(filterQuery) {
        if (filterQuery.isBlank()) appliedFilter = "" else {
            delay(200)
            appliedFilter = filterQuery
        }
    }

    val filteredRows = remember(result.rows, appliedFilter) {
        if (appliedFilter.isBlank()) result.rows
        else {
            result.rows.filter { row ->
                row.any { it?.toString()?.contains(appliedFilter, ignoreCase = true) == true }
            }
        }
    }

    val displayRows = remember(filteredRows, sortState) {
        val (colIdx, dir) = sortState ?: return@remember filteredRows
        filteredRows.sortedWith { r1, r2 ->
            val v1 = r1.getOrNull(colIdx)
            val v2 = r2.getOrNull(colIdx)
            val cmp = compareGridCells(v1, v2)
            if (dir == GridSortDirection.ASC) cmp else -cmp
        }
    }

    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RaksysThemeColors.Background)
    ) {
        // --- DataGrid Action Bar (Info & Export) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RaksysThemeColors.SurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${displayRows.size}${if (filterQuery.isNotBlank()) " / ${result.rows.size}" else ""} baris • ${result.columns.size} kolom",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RaksysThemeColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${result.executionTimeMs} ms)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = RaksysThemeColors.TextMuted
                )
                selectedCellCoord?.let { (r, c) ->
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "• Sel terpilih: R${r + 1}:C${c + 1} (${result.columns.getOrElse(c) { "" }})",
                        fontSize = 11.sp,
                        color = RaksysThemeColors.Primary
                    )
                }
            }

            // Quick Filter input in action bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(RaksysThemeColors.Background)
                    .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchIcon(color = RaksysThemeColors.TextMuted, size = 12.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    BasicTextField(
                        value = filterQuery,
                        onValueChange = { filterQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 11.sp,
                            color = RaksysThemeColors.TextPrimary
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (filterQuery.isEmpty()) {
                                Text("Filter baris...", fontSize = 11.sp, color = RaksysThemeColors.TextMuted)
                            }
                            innerTextField()
                        }
                    )
                    if (filterQuery.isNotEmpty()) {
                        Text(
                            text = "✕",
                            fontSize = 11.sp,
                            color = RaksysThemeColors.TextMuted,
                            modifier = Modifier
                                .clickable { filterQuery = "" }
                                .padding(2.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                selectedCellCoord?.let { (r, c) ->
                    val cellVal = displayRows.getOrNull(r)?.getOrNull(c)?.toString() ?: "NULL"
                    OutlinedButton(
                        onClick = {
                            copyToClipboard(cellVal)
                            ToastManager.show("Nilai sel disalin ke clipboard")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RaksysThemeColors.TextSecondary),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(RaksysThemeColors.Border)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Salin Sel", fontSize = 11.sp)
                    }
                }

                GridActionButton("Salin CSV") {
                    copyToClipboard(rowsToCsv(result.columns, displayRows))
                    ToastManager.show("Data disalin ke clipboard sebagai CSV (${displayRows.size} baris)")
                }
                GridActionButton("Salin JSON") {
                    copyToClipboard(rowsToJson(result.columns, displayRows))
                    ToastManager.show("Data disalin ke clipboard sebagai JSON (${displayRows.size} baris)")
                }
                GridActionButton("Simpan CSV…") {
                    saveExport("hasil-kueri.csv", rowsToCsv(result.columns, displayRows), withBom = true, rows = displayRows.size)
                }
                GridActionButton("Simpan JSON…") {
                    saveExport("hasil-kueri.json", rowsToJson(result.columns, displayRows), withBom = false, rows = displayRows.size)
                }
            }
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        // --- Grid Table Container ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
        ) {
            Column(modifier = Modifier.fillMaxHeight()) {
                // Sticky Header Row
                Row(
                    modifier = Modifier
                        .background(RaksysThemeColors.Surface)
                        .border(width = 1.dp, color = RaksysThemeColors.Border)
                ) {
                    // Row Index Header
                    Box(
                        modifier = Modifier
                            .width(46.dp)
                            .padding(vertical = 7.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RaksysThemeColors.TextMuted
                        )
                    }

                    result.columns.forEachIndexed { colIdx, column ->
                        val colWidth = columnWidths.getOrElse(colIdx) { 150.dp }
                        val isSorted = sortState?.first == colIdx
                        Box(
                            modifier = Modifier
                                .width(colWidth)
                                .border(width = 0.5.dp, color = RaksysThemeColors.Border)
                                .clickable {
                                    sortState = when {
                                        sortState?.first != colIdx -> colIdx to GridSortDirection.ASC
                                        sortState?.second == GridSortDirection.ASC -> colIdx to GridSortDirection.DESC
                                        else -> null
                                    }
                                }
                                .padding(vertical = 7.dp, horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = column,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSorted) RaksysThemeColors.Primary else RaksysThemeColors.TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (isSorted) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (sortState?.second == GridSortDirection.ASC) "▲" else "▼",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RaksysThemeColors.Primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Scrollable Rows
                // Selection is handed to each row as plain values, so clicking a cell recomposes only the
                // row that lost and the row that gained the selection instead of every visible row.
                val onCellClick = remember(result) {
                    { rowIndex: Int, colIndex: Int, fullText: String ->
                        selectedRowIndex = rowIndex
                        selectedCellCoord = Pair(rowIndex, colIndex)
                        if (fullText.length > INSPECT_THRESHOLD) {
                            inspectingCell = Pair(result.columns.getOrElse(colIndex) { "Column" }, fullText)
                        }
                    }
                }
                val onRowClick = remember { { rowIndex: Int -> selectedRowIndex = rowIndex } }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(displayRows) { rowIndex, row ->
                        GridRow(
                            rowIndex = rowIndex,
                            row = row,
                            columnWidths = columnWidths,
                            isRowSelected = selectedRowIndex == rowIndex,
                            selectedColumn = selectedCellCoord?.takeIf { it.first == rowIndex }?.second,
                            onCellClick = onCellClick,
                            onRowClick = onRowClick,
                        )
                    }
                }
            }
        }
    }

    // --- Cell Detail Inspector Modal ---
    inspectingCell?.let { (columnName, cellContent) ->
        ModalGuard()
        AlertDialog(
            onDismissRequest = { inspectingCell = null },
            title = {
                Text(
                    text = "Detail Nilai: $columnName",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaksysThemeColors.TextPrimary
                )
            },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(RaksysThemeColors.Background)
                            .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(6.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(10.dp)
                    ) {
                        Text(
                            text = cellContent,
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
                        copyToClipboard(cellContent)
                        ToastManager.show("Nilai sel disalin ke clipboard")
                        inspectingCell = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RaksysThemeColors.PrimaryFill)
                ) {
                    Text("Salin Nilai")
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectingCell = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}
