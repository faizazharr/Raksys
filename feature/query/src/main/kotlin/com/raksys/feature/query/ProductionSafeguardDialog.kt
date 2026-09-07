package com.raksys.feature.query

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.ui.RaksysThemeColors

fun isDestructiveSql(sql: String): Boolean {
    val clean = sql.trim().uppercase()
    val hasDrop = Regex("""\bDROP\s+(TABLE|DATABASE|SCHEMA|VIEW|INDEX)\b""").containsMatchIn(clean)
    val hasTruncate = Regex("""\bTRUNCATE\s+(TABLE\s+)?\w+""").containsMatchIn(clean)
    val hasDeleteWithoutWhere = Regex("""\bDELETE\s+FROM\s+\w+\s*(;|$)""").containsMatchIn(clean)
    val hasUpdateWithoutWhere = Regex("""\bUPDATE\s+\w+\s+SET\b""").containsMatchIn(clean) && !clean.contains("WHERE")
    val hasAlterDrop = Regex("""\bALTER\s+TABLE\s+\w+\s+DROP\b""").containsMatchIn(clean)
    return hasDrop || hasTruncate || hasDeleteWithoutWhere || hasUpdateWithoutWhere || hasAlterDrop
}

@Composable
fun ProductionSafeguardDialog(
    sql: String,
    profileName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(10.dp),
        containerColor = RaksysThemeColors.Surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RaksysThemeColors.EnvProdBg)
                        .border(1.dp, RaksysThemeColors.EnvProd.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Peringatan Server PRODUCTION",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaksysThemeColors.EnvProd
                    )
                    Text(
                        text = "Kueri destruktif terdeteksi di: $profileName",
                        fontSize = 11.sp,
                        color = RaksysThemeColors.TextSecondary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Kueri ini mengandung perintah (DROP, TRUNCATE, DELETE/UPDATE tanpa WHERE) yang dapat memodifikasi atau menghapus data secara permanen di database Production.",
                    fontSize = 12.sp,
                    color = RaksysThemeColors.TextPrimary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(RaksysThemeColors.Background)
                        .border(1.dp, RaksysThemeColors.EnvProd.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        Text(
                            text = sql.trim(),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = RaksysThemeColors.Warning,
                            maxLines = 8
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Apakah Anda yakin ingin tetap mengeksekusi kueri ini?",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RaksysThemeColors.TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RaksysThemeColors.EnvProd,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Saya Mengerti, Jalankan Kueri", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Batal", fontSize = 11.sp)
            }
        }
    )
}
