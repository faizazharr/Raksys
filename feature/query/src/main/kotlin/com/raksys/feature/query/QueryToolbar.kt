package com.raksys.feature.query

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.ui.RaksysThemeColors

@Composable
fun QueryToolbar(
    profile: ConnectionProfile,
    isRunning: Boolean,
    onExecute: () -> Unit,
    onCancel: () -> Unit,
    onFormat: (() -> Unit)? = null,
    historyCount: Int = 0,
    onToggleHistory: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RaksysThemeColors.Surface)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onExecute,
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RaksysThemeColors.SuccessBorder,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("▶ Run SQL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("⌘↵", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            if (onFormat != null) {
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(
                    onClick = onFormat,
                    enabled = !isRunning,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RaksysThemeColors.TextSecondary),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = SolidColor(RaksysThemeColors.Border)
                    ),
                    shape = RoundedCornerShape(5.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("⚡ Format", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (onToggleHistory != null) {
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(
                    onClick = onToggleHistory,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RaksysThemeColors.TextSecondary),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = SolidColor(RaksysThemeColors.Border)
                    ),
                    shape = RoundedCornerShape(5.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (historyCount > 0) "📜 Riwayat ($historyCount)" else "📜 Riwayat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            if (isRunning) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RaksysThemeColors.Error),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = SolidColor(RaksysThemeColors.Error)
                    ),
                    shape = RoundedCornerShape(5.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(RaksysThemeColors.SurfaceElevated)
                .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "${profile.name} • ${profile.database.ifBlank { profile.dbType.name }}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = RaksysThemeColors.TextSecondary
            )
        }
    }
}
