package com.raksys.feature.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import com.raksys.core.model.EnvironmentType
import com.raksys.core.model.UiState
import com.raksys.core.ui.DbTypeLogo
import com.raksys.core.ui.RaksysThemeColors
import java.util.UUID

private data class QuickType(val type: DbType, val label: String, val defaultPort: String, val defaultUsername: String, val color: Color)

private val quickTypes = listOf(
    QuickType(DbType.POSTGRES, "PostgreSQL", "5432", "postgres", RaksysThemeColors.PostgresColor),
    QuickType(DbType.MYSQL, "MySQL", "3306", "root", RaksysThemeColors.MysqlColor),
    QuickType(DbType.SQLITE, "SQLite", "", "", RaksysThemeColors.SqliteColor),
)

/**
 * Shortcut path buat bikin database baru di server lokal — cuma nama database (+ password kalau perlu),
 * gak minta host/username/SSH/environment kayak ConnectionFormDialog. Asumsi: server lokal udah jalan
 * (Postgres/MySQL), atau SQLite yang filenya dibuat otomatis. Host selalu "localhost".
 */
@Composable
fun QuickCreateLocalDbDialog(
    onDismiss: () -> Unit,
    submitState: UiState<Unit>,
    onCreate: (ConnectionProfile, password: String) -> Unit,
) {
    var selectedType by remember { mutableStateOf(DbType.POSTGRES) }
    var databaseName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val current = quickTypes.first { it.type == selectedType }
    var port by remember(selectedType) { mutableStateOf(current.defaultPort) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = RaksysThemeColors.TextPrimary,
        unfocusedTextColor = RaksysThemeColors.TextPrimary,
        focusedBorderColor = RaksysThemeColors.Primary,
        unfocusedBorderColor = RaksysThemeColors.BorderLight,
        focusedContainerColor = RaksysThemeColors.SurfaceElevated,
        unfocusedContainerColor = RaksysThemeColors.SurfaceElevated,
    )

    val trimmedName = databaseName.trim()
    val validationError = when {
        trimmedName.isBlank() -> "Nama database wajib diisi"
        selectedType != DbType.SQLITE && port.toIntOrNull() == null -> "Port harus angka"
        else -> null
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = RaksysThemeColors.Surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .width(400.dp)
                .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(14.dp))
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RaksysThemeColors.SurfaceElevated)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buat Database Lokal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RaksysThemeColors.TextPrimary,
                        )
                    }
                    Text(
                        text = "Server lokal harus udah jalan. Isi nama database aja, langsung dibuat & tersambung.",
                        fontSize = 11.sp,
                        color = RaksysThemeColors.TextSecondary,
                    )
                }

                HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Tipe Database", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            quickTypes.forEach { option ->
                                val isSelected = option.type == selectedType
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) option.color.copy(alpha = 0.18f) else RaksysThemeColors.SurfaceElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) option.color else RaksysThemeColors.BorderLight,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedType = option.type }
                                        .padding(vertical = 10.dp)
                                ) {
                                    DbTypeLogo(typeName = option.type.name, color = option.color, size = 18.dp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(option.label, fontSize = 10.sp, color = RaksysThemeColors.TextPrimary)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = databaseName,
                        onValueChange = { databaseName = it },
                        label = { Text("Nama Database", fontSize = 12.sp) },
                        placeholder = { Text(if (selectedType == DbType.SQLITE) "app.db" else "my_new_db", fontSize = 12.sp) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (selectedType != DbType.SQLITE) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = port,
                                onValueChange = { port = it },
                                label = { Text("Port", fontSize = 12.sp) },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password (${current.defaultUsername})", fontSize = 12.sp) },
                                singleLine = true,
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }, modifier = Modifier.size(20.dp)) {
                                        Text(if (isPasswordVisible) "🙈" else "👁", fontSize = 11.sp)
                                    }
                                },
                                colors = fieldColors,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    if (submitState is UiState.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RaksysThemeColors.Error.copy(alpha = 0.12f))
                                .padding(10.dp)
                        ) {
                            Text(submitState.message, fontSize = 11.sp, color = RaksysThemeColors.Error)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) { Text("Batal") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val profile = ConnectionProfile(
                                    id = UUID.randomUUID().toString(),
                                    name = trimmedName,
                                    dbType = selectedType,
                                    host = "localhost",
                                    port = port.toIntOrNull() ?: 0,
                                    database = trimmedName,
                                    username = current.defaultUsername,
                                    environment = EnvironmentType.DEVELOPMENT,
                                )
                                onCreate(profile, password)
                            },
                            enabled = validationError == null && submitState !is UiState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = RaksysThemeColors.Primary, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            if (submitState is UiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Membuat...", fontSize = 12.sp)
                            } else {
                                Text("Buat Database", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
