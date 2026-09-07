package com.raksys.feature.query

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.ui.RaksysThemeColors
import com.raksys.core.ui.ToastManager
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun copyToClipboard(text: String) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    } catch (_: Exception) {}
}

@Composable
fun QueryHistoryDrawer(
    history: List<QueryHistoryItem>,
    onSelectQuery: (String) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Column(
        modifier = modifier
            .width(360.dp)
            .fillMaxHeight()
            .background(RaksysThemeColors.Surface)
            .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(0.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(RaksysThemeColors.SurfaceElevated)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📜 Riwayat Kueri",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaksysThemeColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(RaksysThemeColors.PrimaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${history.size}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaksysThemeColors.Primary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = onClear,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("Bersihkan", fontSize = 11.sp, color = RaksysThemeColors.TextMuted)
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("✕", fontSize = 11.sp, color = RaksysThemeColors.TextSecondary)
                }
            }
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⏱️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum Ada Riwayat",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RaksysThemeColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kueri yang dieksekusi akan otomatis tercatat di sini.",
                        fontSize = 11.sp,
                        color = RaksysThemeColors.TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RaksysThemeColors.SurfaceElevated),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(6.dp))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Status baris atas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                if (item.isSuccess) RaksysThemeColors.Success.copy(alpha = 0.15f)
                                                else RaksysThemeColors.Error.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = if (item.isSuccess) "✓ SUKSES" else "✕ GAGAL",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isSuccess) RaksysThemeColors.Success else RaksysThemeColors.Error
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    item.executionTimeMs?.let {
                                        Text(
                                            text = "${it} ms",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = RaksysThemeColors.TextMuted
                                        )
                                    }

                                    item.rowCount?.let {
                                        Text(
                                            text = " • $it baris",
                                            fontSize = 10.sp,
                                            color = RaksysThemeColors.TextMuted
                                        )
                                    }
                                }

                                Text(
                                    text = timeFormat.format(Date(item.timestamp)),
                                    fontSize = 10.sp,
                                    color = RaksysThemeColors.TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // SQL snippet
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RaksysThemeColors.Background)
                                    .border(1.dp, RaksysThemeColors.Border.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .clickable { onSelectQuery(item.sql) }
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = item.sql.trim(),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = RaksysThemeColors.TextPrimary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Tombol aksi
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        copyToClipboard(item.sql)
                                        ToastManager.show("SQL disalin ke clipboard")
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("📋 Salin", fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Button(
                                    onClick = { onSelectQuery(item.sql) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RaksysThemeColors.Primary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("⚡ Muat ke Editor", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
