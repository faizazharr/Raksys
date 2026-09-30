package com.raksys.feature.query

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.raksys.core.model.ColumnDefinition
import com.raksys.core.model.TableSchema
import com.raksys.core.ui.RaksysStatusBadge
import com.raksys.core.ui.RaksysThemeColors
import com.raksys.core.ui.ToastManager
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

private fun copyToClipboard(text: String) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    } catch (_: Exception) {}
}

/**
 * Generates standard SQL DDL (CREATE TABLE) from a TableSchema.
 */
fun generateTableDdl(table: TableSchema): String {
    val colDefs = table.columns.map { col ->
        val sb = StringBuilder("  ${col.name} ${col.type}")
        if (col.isPrimaryKey) sb.append(" PRIMARY KEY")
        if (!col.nullable && !col.isPrimaryKey) sb.append(" NOT NULL")
        col.defaultValue?.let { sb.append(" DEFAULT $it") }
        col.foreignKey?.let { fk ->
            sb.append(" REFERENCES ${fk.referencedTable}(${fk.referencedColumn})")
        }
        sb.toString()
    }
    return "CREATE TABLE ${table.name} (\n${colDefs.joinToString(",\n")}\n);"
}

/**
 * Generates an SQL INSERT template statement.
 */
fun generateInsertTemplate(table: TableSchema): String {
    val cols = table.columns.joinToString(", ") { it.name }
    val placeholders = table.columns.joinToString(", ") { "?" }
    return "INSERT INTO ${table.name} ($cols)\nVALUES ($placeholders);"
}

@Composable
fun TableStructureInspector(
    table: TableSchema,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredColumns = remember(table.columns, searchQuery) {
        if (searchQuery.isBlank()) table.columns
        else table.columns.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.type.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RaksysThemeColors.Background)
    ) {
        // Toolbar: Search filter + Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RaksysThemeColors.SurfaceElevated)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🛠️ Struktur: ${table.name}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaksysThemeColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(RaksysThemeColors.PrimaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${table.columns.size} Kolom",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaksysThemeColors.Primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter kolom / tipe...", fontSize = 11.sp, color = RaksysThemeColors.TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RaksysThemeColors.Primary,
                        unfocusedBorderColor = RaksysThemeColors.Border,
                        focusedContainerColor = RaksysThemeColors.Background,
                        unfocusedContainerColor = RaksysThemeColors.Background,
                        focusedTextColor = RaksysThemeColors.TextPrimary,
                        unfocusedTextColor = RaksysThemeColors.TextPrimary,
                    ),
                    modifier = Modifier.width(220.dp).height(32.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val ddl = generateTableDdl(table)
                        copyToClipboard(ddl)
                        ToastManager.show("DDL '${table.name}' disalin ke clipboard")
                    },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("📋 Salin DDL", fontSize = 11.sp, color = RaksysThemeColors.TextPrimary)
                }

                Button(
                    onClick = {
                        val insert = generateInsertTemplate(table)
                        copyToClipboard(insert)
                        ToastManager.show("Template INSERT disalin ke clipboard")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RaksysThemeColors.Primary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("📝 Salin Template INSERT", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RaksysThemeColors.Surface)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderLabel("#", width = 36.dp)
            HeaderLabel("Nama Kolom", modifier = Modifier.weight(1.8f))
            HeaderLabel("Tipe Data", modifier = Modifier.weight(1.5f))
            HeaderLabel("Atribut / Kunci", modifier = Modifier.weight(1.8f))
            HeaderLabel("Nullability", modifier = Modifier.weight(1f))
            HeaderLabel("Nilai Default", modifier = Modifier.weight(1.2f))
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        if (filteredColumns.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada kolom yang cocok dengan filter '$searchQuery'",
                    fontSize = 12.sp,
                    color = RaksysThemeColors.TextMuted
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(filteredColumns, key = { _, col -> col.name }) { index, col ->
                    ColumnRow(index = index + 1, column = col)
                    HorizontalDivider(color = RaksysThemeColors.Border.copy(alpha = 0.5f), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun HeaderLabel(text: String, modifier: Modifier = Modifier, width: androidx.compose.ui.unit.Dp? = null) {
    val m = if (width != null) modifier.width(width) else modifier
    Text(
        text = text,
        modifier = m,
        color = RaksysThemeColors.TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ColumnRow(index: Int, column: ColumnDefinition) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                copyToClipboard(column.name)
                ToastManager.show("Nama kolom '${column.name}' disalin")
            }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Index
        Text(
            text = "$index",
            modifier = Modifier.width(36.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = RaksysThemeColors.TextMuted
        )

        // Column Name
        Row(
            modifier = Modifier.weight(1.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = column.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = if (column.isPrimaryKey) RaksysThemeColors.Primary else RaksysThemeColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Type
        Text(
            text = column.type,
            modifier = Modifier.weight(1.5f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = RaksysThemeColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Badges: PK & FK
        Row(
            modifier = Modifier.weight(1.8f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (column.isPrimaryKey) {
                RaksysStatusBadge(
                    text = "🔑 PK",
                    statusColor = RaksysThemeColors.Warning,
                    bgColor = RaksysThemeColors.WarningBg
                )
            }
            column.foreignKey?.let { fk ->
                RaksysStatusBadge(
                    text = "🔗 ➔ ${fk.referencedTable}.${fk.referencedColumn}",
                    statusColor = RaksysThemeColors.Info,
                    bgColor = RaksysThemeColors.InfoBg
                )
            }
            if (!column.isPrimaryKey && column.foreignKey == null) {
                Text(
                    text = "-",
                    fontSize = 11.sp,
                    color = RaksysThemeColors.TextMuted
                )
            }
        }

        // Nullability
        Box(modifier = Modifier.weight(1f)) {
            if (column.nullable) {
                RaksysStatusBadge(
                    text = "NULL",
                    statusColor = RaksysThemeColors.TextSecondary,
                    bgColor = RaksysThemeColors.SurfaceElevated
                )
            } else {
                RaksysStatusBadge(
                    text = "NOT NULL",
                    statusColor = Color(0xFFF87171),
                    bgColor = Color(0xFF3B1818)
                )
            }
        }

        // Default Value
        Text(
            text = column.defaultValue ?: "-",
            modifier = Modifier.weight(1.2f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = if (column.defaultValue != null) RaksysThemeColors.TextPrimary else RaksysThemeColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
