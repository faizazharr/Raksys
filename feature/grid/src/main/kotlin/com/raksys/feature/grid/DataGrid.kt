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

    val filteredRows = remember(result.rows, filterQuery) {
        if (filterQuery.isBlank()) result.rows
        else {
            result.rows.filter { row ->
                row.any { it?.toString()?.contains(filterQuery, ignoreCase = true) == true }
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
                        fontSize = 10.sp,
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
                    Text("🔍", fontSize = 10.sp)
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
                                Text("Filter baris...", fontSize = 10.sp, color = RaksysThemeColors.TextMuted)
                            }
                            innerTextField()
                        }
                    )
                    if (filterQuery.isNotEmpty()) {
                        Text(
                            text = "✕",
                            fontSize = 10.sp,
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
                        Text("📋 Salin Sel", fontSize = 10.sp)
                    }
                }

                OutlinedButton(
                    onClick = {
                        val csv = buildString {
                            appendLine(result.columns.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
                            displayRows.forEach { row ->
                                appendLine(row.joinToString(",") { cell ->
                                    if (cell == null) "" else "\"${cell.toString().replace("\"", "\"\"")}\""
                                })
                            }
                        }
                        copyToClipboard(csv)
                        ToastManager.show("Data disalin ke clipboard sebagai CSV (${displayRows.size} baris)")
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RaksysThemeColors.TextSecondary),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(RaksysThemeColors.Border)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("Export CSV", fontSize = 10.sp)
                }

                OutlinedButton(
                    onClick = {
                        val jsonRows = displayRows.map { row ->
                            result.columns.mapIndexed { i, col ->
                                val v = row.getOrNull(i)
                                "\"$col\": " + if (v == null) "null" else "\"${v.toString().replace("\"", "\\\"")}\""
                            }.joinToString(", ", "{ ", " }")
                        }
                        val json = "[\n  " + jsonRows.joinToString(",\n  ") + "\n]"
                        copyToClipboard(json)
                        ToastManager.show("Data disalin ke clipboard sebagai JSON (${displayRows.size} baris)")
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RaksysThemeColors.TextSecondary),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(RaksysThemeColors.Border)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("Export JSON", fontSize = 10.sp)
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
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RaksysThemeColors.Primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Scrollable Rows
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(displayRows) { rowIndex, row ->
                        val isRowSelected = selectedRowIndex == rowIndex
                        val isEven = rowIndex % 2 == 0
                        val rowBg = when {
                            isRowSelected -> RaksysThemeColors.PrimaryContainer.copy(alpha = 0.35f)
                            isEven -> RaksysThemeColors.Background
                            else -> RaksysThemeColors.Surface
                        }

                        Row(
                            modifier = Modifier
                                .background(rowBg)
                                .border(width = 0.5.dp, color = RaksysThemeColors.Border.copy(alpha = 0.35f))
                                .clickable { selectedRowIndex = rowIndex },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Row Index
                            Box(
                                modifier = Modifier
                                    .width(46.dp)
                                    .padding(vertical = 5.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${rowIndex + 1}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isRowSelected) RaksysThemeColors.Primary else RaksysThemeColors.TextMuted
                                )
                            }

                            row.forEachIndexed { colIndex, cell ->
                                val colWidth = columnWidths.getOrElse(colIndex) { 150.dp }
                                val isCellSelected = selectedCellCoord == Pair(rowIndex, colIndex)
                                val isNull = cell == null
                                val cellText = cell?.toString() ?: "NULL"

                                Box(
                                    modifier = Modifier
                                        .width(colWidth)
                                        .border(
                                            width = if (isCellSelected) 1.5.dp else 0.5.dp,
                                            color = if (isCellSelected) RaksysThemeColors.Primary else RaksysThemeColors.Border.copy(alpha = 0.25f)
                                        )
                                        .clickable {
                                            selectedRowIndex = rowIndex
                                            selectedCellCoord = Pair(rowIndex, colIndex)
                                            // Double click or long text inspection
                                            if (cellText.length > 35) {
                                                inspectingCell = Pair(result.columns.getOrElse(colIndex) { "Column" }, cellText)
                                            }
                                        }
                                        .padding(vertical = 5.dp, horizontal = 8.dp)
                                ) {
                                    if (isNull) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(RaksysThemeColors.SurfaceElevated)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "NULL",
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = RaksysThemeColors.TextMuted
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = cellText,
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
                }
            }
        }
    }

    // --- Cell Detail Inspector Modal ---
    inspectingCell?.let { (columnName, cellContent) ->
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
                    colors = ButtonDefaults.buttonColors(containerColor = RaksysThemeColors.Primary)
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
