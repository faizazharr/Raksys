package com.raksys.feature.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.raksys.core.model.DbType
import com.raksys.core.ui.DbTypeLogo
import com.raksys.core.ui.RaksysThemeColors

private data class DbTypeInfo(val type: DbType, val label: String, val subtitle: String, val color: androidx.compose.ui.graphics.Color)

/**
 * Step 1 of adding a connection: pick the engine before anything else, so the detail form that
 * follows only ever shows fields relevant to that engine — no guessing what applies to what.
 */
@Composable
fun DatabaseTypePickerDialog(
    onDismiss: () -> Unit,
    onSelect: (DbType) -> Unit,
) {
    val options = listOf(
        DbTypeInfo(DbType.POSTGRES, "PostgreSQL", "Relational, open source", RaksysThemeColors.PostgresColor),
        DbTypeInfo(DbType.MYSQL, "MySQL", "Relational, paling umum dipakai", RaksysThemeColors.MysqlColor),
        DbTypeInfo(DbType.SQLITE, "SQLite", "File lokal, gak perlu server", RaksysThemeColors.SqliteColor),
        DbTypeInfo(DbType.MONGODB, "MongoDB", "Document-oriented NoSQL", RaksysThemeColors.MongodbColor),
        DbTypeInfo(DbType.REDIS, "Redis", "Key-value, cache & queue", RaksysThemeColors.RedisColor),
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = RaksysThemeColors.Surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .width(440.dp)
                .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(14.dp))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RaksysThemeColors.SurfaceElevated)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Pilih Tipe Database",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RaksysThemeColors.TextPrimary,
                        )
                        Text(
                            text = "Langkah 1 dari 2 — engine yang dipilih menentukan field apa yang perlu diisi",
                            fontSize = 11.sp,
                            color = RaksysThemeColors.TextSecondary,
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Text("✕", fontSize = 13.sp, color = RaksysThemeColors.TextMuted)
                    }
                }

                HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(RaksysThemeColors.SurfaceElevated)
                                .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(10.dp))
                                .clickable { onSelect(option.type) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(option.color.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                DbTypeLogo(typeName = option.type.name, color = option.color, size = 18.dp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RaksysThemeColors.TextPrimary,
                                )
                                Text(
                                    text = option.subtitle,
                                    fontSize = 11.sp,
                                    color = RaksysThemeColors.TextSecondary,
                                )
                            }
                            Text("›", fontSize = 18.sp, color = RaksysThemeColors.TextMuted)
                        }
                    }
                }
            }
        }
    }
}
