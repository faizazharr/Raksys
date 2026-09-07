package com.raksys.feature.query

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.QueryResult
import com.raksys.core.model.UiState
import com.raksys.core.ui.RaksysThemeColors

@Composable
fun QueryStatusBar(state: UiState<QueryResult>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RaksysThemeColors.SurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        when (state) {
            is UiState.Idle -> {
                Text(
                    text = "Siap mengeksekusi query. Tekan 'Run SQL' untuk menjalankan.",
                    fontSize = 11.sp,
                    color = RaksysThemeColors.TextMuted
                )
            }

            is UiState.Loading -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(11.dp),
                        strokeWidth = 2.dp,
                        color = RaksysThemeColors.Primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mengeksekusi SQL query...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = RaksysThemeColors.Primary
                    )
                }
            }

            is UiState.Success -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✓ Sukses",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaksysThemeColors.Success
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${state.data.rows.size} baris dikembalikan",
                        fontSize = 11.sp,
                        color = RaksysThemeColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• Selesai dalam ${state.data.executionTimeMs} ms",
                        fontSize = 11.sp,
                        color = RaksysThemeColors.TextMuted
                    )
                    if (state.data.truncated) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• Dipotong ke 10.000 baris pertama",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = RaksysThemeColors.Warning
                        )
                    }
                }
            }

            is UiState.Error -> {
                Text(
                    text = "✕ Query Gagal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaksysThemeColors.Error
                )
            }
        }
    }
}
