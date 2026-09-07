package com.raksys.feature.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.raksys.core.database.DatabaseDriver
import com.raksys.core.database.DocumentDatabaseDriver
import com.raksys.core.database.KeyValueDriver
import com.raksys.core.model.*
import com.raksys.core.security.CredentialStore
import com.raksys.core.ui.DbTypeLogo
import com.raksys.core.ui.RaksysThemeColors
import com.raksys.core.ui.ToastManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.UUID

private fun defaultPort(type: DbType): String = when (type) {
    DbType.POSTGRES -> "5432"
    DbType.MYSQL -> "3306"
    DbType.SQLITE -> ""
    DbType.MONGODB -> "27017"
    DbType.REDIS -> "6379"
}

private fun defaultUsername(type: DbType): String = when (type) {
    DbType.POSTGRES -> "postgres"
    DbType.MYSQL -> "root"
    else -> ""
}

private fun defaultDatabase(type: DbType): String = when (type) {
    DbType.SQLITE -> "app.db"
    DbType.REDIS -> "0"
    else -> ""
}

private fun typeColor(type: DbType): Color = when (type) {
    DbType.POSTGRES -> RaksysThemeColors.PostgresColor
    DbType.MYSQL -> RaksysThemeColors.MysqlColor
    DbType.SQLITE -> RaksysThemeColors.SqliteColor
    DbType.MONGODB -> RaksysThemeColors.MongodbColor
    DbType.REDIS -> RaksysThemeColors.RedisColor
}

/**
 * Step 2 of adding a connection — the engine (initialDbType) was already picked in
 * DatabaseTypePickerDialog, so this only ever shows fields relevant to that one engine.
 * Changing engine goes back through onChangeType rather than re-exposing a picker in here.
 */
@Composable
fun ConnectionFormDialog(
    initialDbType: DbType,
    onDismiss: () -> Unit,
    onChangeType: () -> Unit,
    submitState: UiState<Unit>,
    onSave: (ConnectionProfile, password: String, sshPassword: String, createNew: Boolean) -> Unit,
) {
    val dbType = initialDbType
    // Only Postgres/MySQL have an explicit CREATE DATABASE step — Mongo creates a DB implicitly
    // on first write, Redis's numbered DBs always exist, and SQLite creates its file on connect.
    val createModeApplicable = dbType == DbType.POSTGRES || dbType == DbType.MYSQL

    var name by remember { mutableStateOf("") }
    var environment by remember { mutableStateOf(EnvironmentType.DEVELOPMENT) }
    var createNew by remember { mutableStateOf(false) }
    var host by remember { mutableStateOf("localhost") }
    var port by remember { mutableStateOf(defaultPort(dbType)) }
    var database by remember { mutableStateOf(defaultDatabase(dbType)) }
    var username by remember { mutableStateOf(defaultUsername(dbType)) }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var sshEnabled by remember { mutableStateOf(false) }
    var sshHost by remember { mutableStateOf("") }
    var sshPort by remember { mutableStateOf("22") }
    var sshUsername by remember { mutableStateOf("") }
    var sshAuthMethod by remember { mutableStateOf(SshAuthMethod.PASSWORD) }
    var sshPassword by remember { mutableStateOf("") }
    var isSshPasswordVisible by remember { mutableStateOf(false) }
    var sshPrivateKeyPath by remember { mutableStateOf("") }

    val sqlDriver = koinInject<DatabaseDriver>()
    val documentDriver = koinInject<DocumentDatabaseDriver>()
    val keyValueDriver = koinInject<KeyValueDriver>()
    val credentialStore = koinInject<CredentialStore>()
    val scope = rememberCoroutineScope()
    var inlineTestState by remember { mutableStateOf<UiState<Unit>>(UiState.Idle) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = RaksysThemeColors.TextPrimary,
        unfocusedTextColor = RaksysThemeColors.TextPrimary,
        focusedBorderColor = RaksysThemeColors.Primary,
        unfocusedBorderColor = RaksysThemeColors.BorderLight,
        focusedContainerColor = RaksysThemeColors.SurfaceElevated,
        unfocusedContainerColor = RaksysThemeColors.SurfaceElevated,
        focusedPlaceholderColor = RaksysThemeColors.TextMuted,
        unfocusedPlaceholderColor = RaksysThemeColors.TextMuted,
    )

    val sshApplicable = dbType != DbType.SQLITE
    val validationError = validateConnectionForm(
        ConnectionFormFields(
            dbType = dbType,
            host = host,
            port = port,
            database = database,
            sshEnabled = sshEnabled,
            sshHost = sshHost,
            sshUsername = sshUsername,
            sshAuthMethod = sshAuthMethod,
            sshPassword = sshPassword,
            sshPrivateKeyPath = sshPrivateKeyPath,
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = RaksysThemeColors.Surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .width(520.dp)
                .heightIn(max = 660.dp)
                .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header (Fixed)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RaksysThemeColors.SurfaceElevated)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(typeColor(dbType).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                DbTypeLogo(typeName = dbType.name, color = typeColor(dbType), size = 16.dp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Koneksi ${dbType.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RaksysThemeColors.TextPrimary
                                )
                                Text(
                                    text = "Langkah 2 dari 2 — detail koneksi & otentikasi",
                                    fontSize = 11.sp,
                                    color = RaksysThemeColors.TextSecondary
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Text("✕", fontSize = 13.sp, color = RaksysThemeColors.TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "‹ Ganti tipe database",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RaksysThemeColors.Primary,
                        modifier = Modifier.clickable { onChangeType() }
                    )
                }

                HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Connection Name
                    FormFieldLabel("Nama Profil Koneksi")
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Contoh: Production PostgreSQL atau Local Dev", fontSize = 13.sp) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                        colors = fieldColors,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FormFieldLabel("Environment Tag")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            EnvironmentType.DEVELOPMENT to ("Local / Dev" to RaksysThemeColors.EnvDev),
                            EnvironmentType.STAGING to ("Staging" to RaksysThemeColors.EnvStaging),
                            EnvironmentType.PRODUCTION to ("Production" to RaksysThemeColors.EnvProd),
                        ).forEach { (env, meta) ->
                            val (label, color) = meta
                            val isSelected = environment == env
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.2f) else RaksysThemeColors.SurfaceElevated)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) color else RaksysThemeColors.BorderLight,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { environment = env }
                                    .padding(vertical = 7.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else RaksysThemeColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (createModeApplicable) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RaksysThemeColors.SurfaceElevated)
                                .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = "Buat Database Baru di Server Ini",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RaksysThemeColors.TextPrimary,
                                )
                                Text(
                                    text = if (createNew) {
                                        "Server $dbType-nya harus udah jalan di host/port di bawah — database-nya bakal dibuatin."
                                    } else {
                                        "Off = connect ke database yang udah ada. Nyalain kalau database-nya belum ada."
                                    },
                                    fontSize = 11.sp,
                                    color = RaksysThemeColors.TextSecondary,
                                )
                            }
                            Switch(
                                checked = createNew,
                                onCheckedChange = { createNew = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = RaksysThemeColors.Primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Specific Fields based on DB Type
                    if (dbType == DbType.SQLITE) {
                        FormFieldLabel("Lokasi File SQLite (.db / .sqlite)")
                        OutlinedTextField(
                            value = database,
                            onValueChange = { database = it },
                            placeholder = { Text("/path/to/database.db atau ./local.sqlite", fontSize = 13.sp) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                            colors = fieldColors,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kalau file di path ini belum ada, bakal dibuat otomatis pas pertama kali connect.",
                            fontSize = 11.sp,
                            color = RaksysThemeColors.TextMuted,
                        )
                    } else {
                        // Host & Port
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(2.6f)) {
                                FormFieldLabel("Host / Server Address")
                                OutlinedTextField(
                                    value = host,
                                    onValueChange = { host = it },
                                    placeholder = { Text("localhost atau 127.0.0.1", fontSize = 13.sp) },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                                    colors = fieldColors,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(modifier = Modifier.weight(1.2f)) {
                                FormFieldLabel("Port")
                                OutlinedTextField(
                                    value = port,
                                    onValueChange = { port = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                                    colors = fieldColors,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Database Name
                        FormFieldLabel(
                            when {
                                dbType == DbType.REDIS -> "Database Index (0-15)"
                                createNew -> "Nama Database Baru (bakal dibuat)"
                                else -> "Nama Database"
                            }
                        )
                        OutlinedTextField(
                            value = database,
                            onValueChange = { database = it },
                            placeholder = {
                                Text(
                                    if (dbType == DbType.REDIS) "0" else "Contoh: my_database",
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                            colors = fieldColors,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Username & Password
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (dbType.family != DbFamily.KEY_VALUE) {
                                Column(modifier = Modifier.weight(1f)) {
                                    FormFieldLabel("Username")
                                    OutlinedTextField(
                                        value = username,
                                        onValueChange = { username = it },
                                        placeholder = { Text("postgres / root", fontSize = 13.sp) },
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                                        colors = fieldColors,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                FormFieldLabel("Password")
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    placeholder = { Text("••••••••", fontSize = 13.sp) },
                                    singleLine = true,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }, modifier = Modifier.size(24.dp)) {
                                            Text(if (isPasswordVisible) "👁" else "👁‍🗨", fontSize = 12.sp)
                                        }
                                    },
                                    textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                                    colors = fieldColors,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    if (sshApplicable) {
                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        // SSH Tunnel Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RaksysThemeColors.SurfaceElevated)
                                .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Akses via SSH Tunnel (Bastion)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RaksysThemeColors.TextPrimary
                                )
                                Text(
                                    text = "Gunakan jump host untuk mengakses database di private network",
                                    fontSize = 11.sp,
                                    color = RaksysThemeColors.TextSecondary
                                )
                            }
                            Switch(
                                checked = sshEnabled,
                                onCheckedChange = { sshEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = RaksysThemeColors.Primary
                                )
                            )
                        }

                        if (sshEnabled) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(2.6f)) {
                                    FormFieldLabel("SSH Host")
                                    OutlinedTextField(
                                        value = sshHost,
                                        onValueChange = { sshHost = it },
                                        placeholder = { Text("bastion.example.com", fontSize = 13.sp) },
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                                        colors = fieldColors,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Column(modifier = Modifier.weight(1.2f)) {
                                    FormFieldLabel("Port")
                                    OutlinedTextField(
                                        value = sshPort,
                                        onValueChange = { sshPort = it },
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                                        colors = fieldColors,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            FormFieldLabel("SSH Username")
                            OutlinedTextField(
                                value = sshUsername,
                                onValueChange = { sshUsername = it },
                                placeholder = { Text("ubuntu / ec2-user", fontSize = 13.sp) },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                                colors = fieldColors,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FormFieldLabel("Metode Autentikasi SSH")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SshAuthMethod.entries.forEach { method ->
                                    FilterChip(
                                        selected = sshAuthMethod == method,
                                        onClick = { sshAuthMethod = method },
                                        label = {
                                            Text(
                                                text = if (method == SshAuthMethod.PASSWORD) "Password" else "Private Key (.pem/.rsa)",
                                                fontSize = 12.sp,
                                                color = if (sshAuthMethod == method) RaksysThemeColors.Primary else RaksysThemeColors.TextSecondary
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RaksysThemeColors.PrimaryContainer,
                                            containerColor = RaksysThemeColors.SurfaceElevated
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (sshAuthMethod == SshAuthMethod.PASSWORD) {
                                FormFieldLabel("SSH Password")
                                OutlinedTextField(
                                    value = sshPassword,
                                    onValueChange = { sshPassword = it },
                                    singleLine = true,
                                    visualTransformation = if (isSshPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isSshPasswordVisible = !isSshPasswordVisible }, modifier = Modifier.size(24.dp)) {
                                            Text(if (isSshPasswordVisible) "👁" else "👁‍🗨", fontSize = 12.sp)
                                        }
                                    },
                                    textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                                    colors = fieldColors,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                FormFieldLabel("Path Private Key")
                                OutlinedTextField(
                                    value = sshPrivateKeyPath,
                                    onValueChange = { sshPrivateKeyPath = it },
                                    placeholder = { Text("~/.ssh/id_rsa atau /path/to/key.pem", fontSize = 13.sp) },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 13.sp, color = RaksysThemeColors.TextPrimary),
                                    colors = fieldColors,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RaksysThemeColors.ErrorBg)
                                .border(1.dp, RaksysThemeColors.ErrorBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "⚠️ $validationError",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                    }

                    if (submitState is UiState.Error) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RaksysThemeColors.ErrorBg)
                                .border(1.dp, RaksysThemeColors.ErrorBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "✕ ${submitState.message}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                    }
                }

                HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

                fun testConnectionNow() {
                    val tempId = "test-${UUID.randomUUID()}"
                    val testProfile = ConnectionProfile(
                        id = tempId,
                        name = name.ifBlank { "Test DB" },
                        dbType = dbType,
                        host = host,
                        port = port.toIntOrNull() ?: 0,
                        database = database,
                        username = username,
                        environment = environment,
                        sshEnabled = sshEnabled,
                        sshHost = sshHost,
                        sshPort = sshPort.toIntOrNull() ?: 22,
                        sshUsername = sshUsername,
                        sshAuthMethod = sshAuthMethod,
                        sshPrivateKeyPath = sshPrivateKeyPath,
                    )
                    scope.launch {
                        inlineTestState = UiState.Loading
                        credentialStore.save(tempId, password)
                        val result = when (dbType.family) {
                            DbFamily.RELATIONAL -> sqlDriver.testConnection(testProfile)
                            DbFamily.DOCUMENT -> documentDriver.testConnection(testProfile)
                            DbFamily.KEY_VALUE -> keyValueDriver.testConnection(testProfile)
                        }
                        credentialStore.delete(tempId)
                        sqlDriver.invalidate(tempId)
                        result
                            .onSuccess {
                                inlineTestState = UiState.Success(Unit)
                                ToastManager.show("✓ Uji koneksi berhasil!")
                            }
                            .onFailure {
                                inlineTestState = UiState.Error(it.message ?: "Koneksi gagal")
                            }
                    }
                }

                // Fixed Footer Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RaksysThemeColors.SurfaceElevated)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Test Connection Button & Status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { testConnectionNow() },
                            enabled = validationError == null && inlineTestState !is UiState.Loading,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RaksysThemeColors.Primary),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = SolidColor(RaksysThemeColors.Primary)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                if (inlineTestState is UiState.Loading) "Menguji..." else "⚡ Uji Koneksi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        when (inlineTestState) {
                            is UiState.Loading -> {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = RaksysThemeColors.Primary, strokeWidth = 2.dp)
                            }
                            is UiState.Success -> {
                                Text("✓ Terhubung", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.Success)
                            }
                            is UiState.Error -> {
                                Text("✕ Gagal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.Error)
                            }
                            else -> {}
                        }
                    }

                    // Right: Cancel & Save Buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = RaksysThemeColors.TextSecondary)
                        ) {
                            Text("Batal", fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            enabled = validationError == null && submitState !is UiState.Loading,
                            onClick = {
                                val profile = ConnectionProfile(
                                    id = UUID.randomUUID().toString(),
                                    name = name.ifBlank { "Untitled DB" },
                                    dbType = dbType,
                                    host = host,
                                    port = port.toIntOrNull() ?: 0,
                                    database = database,
                                    username = username,
                                    environment = environment,
                                    sshEnabled = sshEnabled,
                                    sshHost = sshHost,
                                    sshPort = sshPort.toIntOrNull() ?: 22,
                                    sshUsername = sshUsername,
                                    sshAuthMethod = sshAuthMethod,
                                    sshPrivateKeyPath = sshPrivateKeyPath,
                                )
                                onSave(profile, password, sshPassword, createNew)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RaksysThemeColors.Primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            if (submitState is UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (createNew) "Membuat database..." else "Menyimpan...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            } else {
                                Text(
                                    if (createNew) "Buat & Simpan" else "Simpan Koneksi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormFieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = RaksysThemeColors.TextSecondary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}
