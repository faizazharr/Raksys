package com.raksys.feature.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import com.raksys.core.model.UiState
import com.raksys.core.security.CredentialStore
import com.raksys.core.ui.ModalGuard
import com.raksys.core.ui.RaksysSearchField
import com.raksys.core.ui.LightningIcon
import com.raksys.core.ui.PlugIcon
import com.raksys.core.ui.RaksysThemeColors
import com.raksys.core.ui.ToastManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.UUID

@Composable
fun ConnectionListScreen(
    selectedProfile: ConnectionProfile? = null,
    onSelect: (ConnectionProfile) -> Unit,
    showAddDialog: Boolean,
    onShowAddDialogChange: (Boolean) -> Unit,
) {
    val presenter = koinInject<ConnectionPresenter>()
    val credentialStore = koinInject<CredentialStore>()
    val profiles by presenter.profiles.collectAsState()
    val testState by presenter.testState.collectAsState()
    val saveState by presenter.saveState.collectAsState()

    var testingProfileId by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<ConnectionProfile?>(null) }
    var pickTypeOpen by remember { mutableStateOf(false) }
    var formDbType by remember { mutableStateOf<DbType?>(null) }
    var editingProfile by remember { mutableStateOf<ConnectionProfile?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var quickCreateOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(showAddDialog) {
        if (showAddDialog) {
            presenter.onEvent(ConnectionEvent.ResetSaveState)
            pickTypeOpen = true
            formDbType = null
            editingProfile = null
        } else {
            pickTypeOpen = false
            formDbType = null
            editingProfile = null
        }
    }

    LaunchedEffect(saveState) {
        if (saveState is UiState.Success) {
            formDbType = null
            editingProfile = null
            quickCreateOpen = false
            onShowAddDialogChange(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(RaksysThemeColors.Surface)
    ) {
        // Sidebar Header (Tinggi 46.dp persis sejajar dengan Studio Workspace Header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RaksysThemeColors.PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    LightningIcon(color = RaksysThemeColors.Primary, size = 14.dp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Connections",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaksysThemeColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(RaksysThemeColors.SurfaceElevated)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${profiles.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaksysThemeColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 11.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = {
                        scope.launch { presenter.onEvent(ConnectionEvent.ResetSaveState) }
                        quickCreateOpen = true
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Buat DB Lokal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.Primary)
                }

                Button(
                    onClick = { onShowAddDialogChange(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RaksysThemeColors.PrimaryFill,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("+ New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        if (profiles.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                RaksysSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Filter koneksi...",
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = RaksysThemeColors.SurfaceElevated
                )
            }
            HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)
        }

        val filteredProfiles = remember(profiles, searchQuery) {
            if (searchQuery.isBlank()) profiles
            else profiles.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.host.contains(searchQuery, ignoreCase = true) ||
                it.database.contains(searchQuery, ignoreCase = true) ||
                it.dbType.name.contains(searchQuery, ignoreCase = true)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (profiles.isEmpty()) {
                // Sidebar Compact Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(RaksysThemeColors.SurfaceElevated)
                            .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        PlugIcon(color = RaksysThemeColors.Primary, size = 26.dp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Belum Ada Koneksi",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaksysThemeColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Simpan profil database pertama Anda untuk mulai eksplorasi.",
                        fontSize = 11.sp,
                        color = RaksysThemeColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onShowAddDialogChange(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RaksysThemeColors.PrimaryFill,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("+ Buat Koneksi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (filteredProfiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada koneksi yang cocok dengan '$searchQuery'",
                        fontSize = 12.sp,
                        color = RaksysThemeColors.TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProfiles, key = { it.id }) { profile ->
                        val isSelected = selectedProfile?.id == profile.id
                        val isTestingThis = testingProfileId == profile.id && testState is UiState.Loading

                        ConnectionCard(
                            profile = profile,
                            isSelected = isSelected,
                            isTesting = isTestingThis,
                            onClick = { onSelect(profile) },
                            onTest = {
                                testingProfileId = profile.id
                                scope.launch { presenter.onEvent(ConnectionEvent.Test(profile)) }
                            },
                            onEdit = {
                                editingProfile = profile
                                formDbType = profile.dbType
                            },
                            onClone = {
                                val newId = UUID.randomUUID().toString()
                                val clone = profile.copy(id = newId, name = "${profile.name} (Copy)")
                                val pass = credentialStore.get(profile.id) ?: ""
                                val sshPass = credentialStore.get("${profile.id}:ssh") ?: ""
                                scope.launch {
                                    presenter.onEvent(ConnectionEvent.Add(clone, pass, sshPass, createNew = false))
                                    ToastManager.show("Koneksi '${profile.name}' berhasil digandakan")
                                }
                            },
                            onDelete = { pendingDelete = profile }
                        )
                    }
                }
            }
        }

        if (testingProfileId != null && testState !is UiState.Idle) {
            HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)
            ConnectionTestBanner(state = testState, onDismiss = { testingProfileId = null })
        }
    }

    if (pickTypeOpen) {
        DatabaseTypePickerDialog(
            onDismiss = { onShowAddDialogChange(false) },
            onSelect = { type ->
                pickTypeOpen = false
                formDbType = type
            },
        )
    }

    formDbType?.let { type ->
        ConnectionFormDialog(
            initialDbType = type,
            editingProfile = editingProfile,
            onDismiss = {
                formDbType = null
                editingProfile = null
                onShowAddDialogChange(false)
            },
            onChangeType = {
                formDbType = null
                editingProfile = null
                pickTypeOpen = true
            },
            submitState = saveState,
            onSave = { profile, password, sshPassword, createNew ->
                scope.launch { presenter.onEvent(ConnectionEvent.Add(profile, password, sshPassword, createNew)) }
            },
        )
    }

    if (quickCreateOpen) {
        QuickCreateLocalDbDialog(
            onDismiss = { quickCreateOpen = false },
            submitState = saveState,
            onCreate = { profile, password ->
                scope.launch { presenter.onEvent(ConnectionEvent.Add(profile, password, "", createNew = true)) }
            },
        )
    }

    pendingDelete?.let { profile ->
        ModalGuard()
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Hapus Koneksi?") },
            text = { Text("Koneksi '${profile.name}' dan kredensial tersimpannya akan dihapus permanen. Tindakan ini tidak bisa dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { presenter.onEvent(ConnectionEvent.Delete(profile)) }
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = RaksysThemeColors.Error),
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Batal") }
            },
        )
    }
}
