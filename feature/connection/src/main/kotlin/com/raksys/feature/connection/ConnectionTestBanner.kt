package com.raksys.feature.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.UiState
import com.raksys.core.ui.RaksysThemeColors

@Composable
fun ConnectionTestBanner(state: UiState<Unit>, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        when (state) {
            is UiState.Loading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RaksysThemeColors.InfoBg)
                        .border(1.dp, RaksysThemeColors.Info.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = RaksysThemeColors.Info
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Menguji koneksi...",
                        fontSize = 12.sp,
                        color = RaksysThemeColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            is UiState.Success -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RaksysThemeColors.SuccessBg)
                        .border(1.dp, RaksysThemeColors.SuccessBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✓", color = RaksysThemeColors.Success, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Koneksi Berhasil!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD1FAE5)
                            )
                            Text(
                                text = "Host & kredensial terhubung valid.",
                                fontSize = 10.sp,
                                color = Color(0xFFA7F3D0)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Text("✕", fontSize = 10.sp, color = Color(0xFFA7F3D0))
                    }
                }
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RaksysThemeColors.ErrorBg)
                        .border(1.dp, RaksysThemeColors.ErrorBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✕", color = RaksysThemeColors.Error, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Koneksi Gagal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF7B72)
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                            Text("✕", fontSize = 10.sp, color = Color(0xFFFF7B72))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.message,
                        fontSize = 10.sp,
                        color = Color(0xFFFFA198),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            is UiState.Idle -> {}
        }
    }
}
