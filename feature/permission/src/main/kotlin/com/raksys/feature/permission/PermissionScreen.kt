package com.raksys.feature.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DatabaseRole
import com.raksys.core.model.PrivilegeType
import com.raksys.core.model.UiState
import com.raksys.core.ui.RaksysEmptyState
import com.raksys.core.ui.RaksysErrorState
import com.raksys.core.ui.RaksysLoadingState
import com.raksys.core.ui.RaksysStatusBadge
import com.raksys.core.ui.RaksysThemeColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private data class RevokeRequest(val role: String, val table: String, val privilege: PrivilegeType)

@Composable
fun PermissionScreen(profile: ConnectionProfile, modifier: Modifier = Modifier) {
    val presenter = koinInject<PermissionPresenter>()
    val state by presenter.state.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedRole by remember(profile.id) { mutableStateOf<String?>(null) }
    var pendingRevoke by remember { mutableStateOf<RevokeRequest?>(null) }

    LaunchedEffect(profile.id) { presenter.onEvent(PermissionEvent.Load(profile)) }

    Column(modifier = modifier.fillMaxSize().background(RaksysThemeColors.Background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                "Roles & Permissions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RaksysThemeColors.TextPrimary,
            )
        }
        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        when (val current = state) {
            is UiState.Idle -> {}
            is UiState.Loading -> RaksysLoadingState(title = "Memuat role & privilege...")
            is UiState.Error -> RaksysErrorState(
                errorMessage = current.message,
                onRetry = { scope.launch { presenter.onEvent(PermissionEvent.Load(profile)) } },
            )
            is UiState.Success -> {
                val data = current.data
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.width(220.dp).fillMaxHeight().background(RaksysThemeColors.Surface)) {
                        if (data.roles.isEmpty()) {
                            RaksysEmptyState(iconLabel = "👤", title = "Belum Ada Role", description = "Database ini belum punya role/user selain default.")
                        } else {
                            LazyColumn(contentPadding = PaddingValues(8.dp)) {
                                items(data.roles, key = { it.name }) { role ->
                                    RoleRow(role = role, isSelected = role.name == selectedRole, onClick = { selectedRole = role.name })
                                }
                            }
                        }
                    }

                    VerticalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

                    Box(modifier = Modifier.fillMaxSize()) {
                        val role = selectedRole
                        if (role == null) {
                            RaksysEmptyState(iconLabel = "🔐", title = "Pilih Role", description = "Pilih role di panel kiri untuk lihat/atur privilege per tabel.")
                        } else {
                            PrivilegeMatrix(
                                tables = data.tables,
                                rolePrivileges = data.privileges.filter { it.roleName == role },
                                onToggle = { table, privilege, checked ->
                                    if (checked) {
                                        scope.launch { presenter.onEvent(PermissionEvent.Grant(profile, role, table, privilege)) }
                                    } else {
                                        pendingRevoke = RevokeRequest(role, table, privilege)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    pendingRevoke?.let { req ->
        AlertDialog(
            onDismissRequest = { pendingRevoke = null },
            title = { Text("Cabut Akses?") },
            text = { Text("Role '${req.role}' bakal kehilangan akses ${req.privilege.name} ke tabel '${req.table}'.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { presenter.onEvent(PermissionEvent.Revoke(profile, req.role, req.table, req.privilege)) }
                        pendingRevoke = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = RaksysThemeColors.Error),
                ) { Text("Cabut") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRevoke = null }) { Text("Batal") }
            },
        )
    }
}

@Composable
private fun RoleRow(role: DatabaseRole, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) RaksysThemeColors.PrimaryContainer else androidx.compose.ui.graphics.Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Column {
            Text(role.name, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = RaksysThemeColors.TextPrimary)
            Spacer(modifier = Modifier.width(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (role.isSuperuser) RaksysStatusBadge("superuser", RaksysThemeColors.Warning, RaksysThemeColors.WarningBg)
                if (!role.canLogin) RaksysStatusBadge("no login", RaksysThemeColors.TextMuted, RaksysThemeColors.SurfaceElevated)
            }
        }
    }
}
