package com.raksys.feature.navigator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.raksys.core.model.TableSchema
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

@Composable
fun TableItemRow(
    table: TableSchema,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val bg = if (isSelected) RaksysThemeColors.PrimaryContainer else Color.Transparent
    val borderColor = if (isSelected) RaksysThemeColors.Primary else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) RaksysThemeColors.Primary else RaksysThemeColors.SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⊞",
                fontSize = 11.sp,
                color = if (isSelected) Color.White else RaksysThemeColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = table.name,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) RaksysThemeColors.TextPrimary else RaksysThemeColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (table.columns.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(RaksysThemeColors.SurfaceElevated)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "${table.columns.size} col",
                    fontSize = 10.sp,
                    color = RaksysThemeColors.TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Action Menu Button ⋮
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(20.dp)
            ) {
                Text(
                    text = "⋮",
                    fontSize = 12.sp,
                    color = RaksysThemeColors.TextMuted
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(RaksysThemeColors.Surface)
            ) {
                DropdownMenuItem(
                    text = { Text("📄 Buka Data (100 baris)", fontSize = 11.sp, color = RaksysThemeColors.TextPrimary) },
                    onClick = {
                        menuExpanded = false
                        onClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("📋 Salin Nama Tabel", fontSize = 11.sp, color = RaksysThemeColors.TextPrimary) },
                    onClick = {
                        menuExpanded = false
                        copyToClipboard(table.name)
                        ToastManager.show("Nama tabel '${table.name}' disalin")
                    }
                )
                DropdownMenuItem(
                    text = { Text("⚡ Salin Kueri SELECT", fontSize = 11.sp, color = RaksysThemeColors.TextPrimary) },
                    onClick = {
                        menuExpanded = false
                        val sql = "SELECT * FROM ${table.name} LIMIT 100;"
                        copyToClipboard(sql)
                        ToastManager.show("Kueri SELECT disalin")
                    }
                )
                DropdownMenuItem(
                    text = { Text("📜 Salin Template DDL", fontSize = 11.sp, color = RaksysThemeColors.TextPrimary) },
                    onClick = {
                        menuExpanded = false
                        val ddl = if (table.columns.isNotEmpty()) {
                            "CREATE TABLE ${table.name} (\n" +
                                    table.columns.joinToString(",\n") { "  ${it.name} ${it.type}${if (!it.nullable) " NOT NULL" else ""}${if (it.isPrimaryKey) " PRIMARY KEY" else ""}" } +
                                    "\n);"
                        } else {
                            "CREATE TABLE ${table.name} ();"
                        }
                        copyToClipboard(ddl)
                        ToastManager.show("Template DDL disalin")
                    }
                )
            }
        }
    }
}
