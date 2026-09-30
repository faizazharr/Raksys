package com.raksys.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.raksys.core.database.DatabaseDriver
import com.raksys.core.database.DocumentDatabaseDriver
import com.raksys.core.database.KeyValueDriver
import com.raksys.core.database.SshTunnelManager
import com.raksys.core.database.databaseModule
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbFamily
import com.raksys.core.model.TableSchema
import com.raksys.core.security.securityModule
import com.raksys.core.ui.*
import com.raksys.feature.connection.ConnectionListScreen
import com.raksys.feature.connection.connectionModule
import com.raksys.feature.document.DocumentCollectionList
import com.raksys.feature.document.DocumentViewer
import com.raksys.feature.document.documentModule
import com.raksys.feature.erd.ErdScreen
import com.raksys.feature.erd.erdModule
import com.raksys.feature.keyvalue.KeyValueBrowser
import com.raksys.feature.keyvalue.keyValueModule
import com.raksys.feature.navigator.SchemaNavigator
import com.raksys.feature.navigator.navigatorModule
import com.raksys.feature.permission.PermissionScreen
import com.raksys.feature.permission.permissionModule
import com.raksys.feature.query.QueryEditor
import com.raksys.feature.query.queryModule
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontFamily
import com.raksys.feature.connection.ConnectionPresenter
import com.raksys.feature.navigator.NavigatorPresenter
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import java.awt.Taskbar
import javax.imageio.ImageIO

enum class RelationalTab(val label: String) {
    QUERY("Query Editor"),
    ERD("Visual ERD"),
    PERMISSIONS("Roles & Permissions"),
}

@Composable
private fun RelationalTabIcon(tab: RelationalTab, color: Color) {
    when (tab) {
        RelationalTab.QUERY -> QueryIcon(color)
        RelationalTab.ERD -> ErdIcon(color)
        RelationalTab.PERMISSIONS -> ShieldIcon(color)
    }
}

@Composable
private fun RelationalTabBar(
    selected: RelationalTab,
    onSelect: (RelationalTab) -> Unit,
    isSidebarVisible: Boolean,
    onToggleSidebar: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(RaksysThemeColors.Surface)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleSidebar,
                modifier = Modifier
                    .size(28.dp)
                    .semantics { contentDescription = if (isSidebarVisible) "Sembunyikan sidebar" else "Tampilkan sidebar" }
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (!isSidebarVisible) RaksysThemeColors.PrimaryContainer else RaksysThemeColors.SurfaceElevated)
            ) {
                SidebarIcon(
                    color = if (!isSidebarVisible) RaksysThemeColors.Primary else RaksysThemeColors.TextSecondary,
                    sidebarOpen = isSidebarVisible,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Segmented Pill Container
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(RaksysThemeColors.SurfaceElevated)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RelationalTab.entries.forEach { tab ->
                    val isSelected = tab == selected
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (isSelected) RaksysThemeColors.PrimaryContainer else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) RaksysThemeColors.Primary.copy(alpha = 0.6f) else Color.Transparent,
                                shape = RoundedCornerShape(5.dp)
                            )
                            .raksysInteractive(RoundedCornerShape(5.dp), enabled = !isSelected)
                            .clickable { onSelect(tab) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RelationalTabIcon(
                            tab = tab,
                            color = if (isSelected) RaksysThemeColors.Primary else RaksysThemeColors.TextSecondary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) RaksysThemeColors.Primary else RaksysThemeColors.TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

fun main() {
    // Set dock icon on macOS AWT Taskbar if supported
    try {
        if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
            val iconStream = object {}.javaClass.getResourceAsStream("/icon.png")
            if (iconStream != null) {
                val image = ImageIO.read(iconStream)
                Taskbar.getTaskbar().iconImage = image
            }
        }
    } catch (_: Exception) {
        // Fallback gracefully on systems without Taskbar support
    }

    startKoin {
        modules(
            securityModule,
            databaseModule,
            connectionModule,
            navigatorModule,
            queryModule,
            documentModule,
            keyValueModule,
            permissionModule,
            erdModule,
        )
    }

    application {
        KoinContext {
            val sqlDriver = koinInject<DatabaseDriver>()
            val documentDriver = koinInject<DocumentDatabaseDriver>()
            val keyValueDriver = koinInject<KeyValueDriver>()
            val sshTunnelManager = koinInject<SshTunnelManager>()
            val connectionPresenter = koinInject<ConnectionPresenter>()
            val navigatorPresenter = koinInject<NavigatorPresenter>()

            val allConnections by connectionPresenter.profiles.collectAsState()
            val schemaState by navigatorPresenter.state.collectAsState()
            val currentTables = (schemaState as? com.raksys.core.model.UiState.Success)?.data ?: emptyList()

            var selectedProfile by remember { mutableStateOf<ConnectionProfile?>(null) }
            var selectedTable by remember { mutableStateOf<TableSchema?>(null) }
            var selectedCollection by remember { mutableStateOf<String?>(null) }
            var showAddConnectionDialog by remember { mutableStateOf(false) }
            var isCommandPaletteOpen by remember { mutableStateOf(false) }
            var workspaceTab by remember { mutableStateOf(RelationalTab.QUERY) }

            // Spatial & Layout States
            var isSidebarVisible by remember { mutableStateOf(true) }
            var sidebarWidthDp by remember { mutableStateOf(260.dp) }
            var navigatorWidthDp by remember { mutableStateOf(240.dp) }

            Window(
                onCloseRequest = {
                    sqlDriver.close()
                    documentDriver.close()
                    keyValueDriver.close()
                    sshTunnelManager.closeAll()
                    exitApplication()
                },
                title = "Raksys Database Studio",
                icon = androidx.compose.ui.res.painterResource("icon.png"),
            ) {
                val mod = RaksysPlatform.isMac
                val isRelational = selectedProfile?.dbType?.family == DbFamily.RELATIONAL
                MenuBar {
                    Menu("File") {
                        Item(
                            "New Connection…",
                            shortcut = KeyShortcut(Key.N, meta = mod, ctrl = !mod),
                            onClick = { showAddConnectionDialog = true },
                        )
                    }
                    Menu("View") {
                        Item(
                            if (isSidebarVisible) "Hide Sidebar" else "Show Sidebar",
                            shortcut = KeyShortcut(Key.B, meta = mod, ctrl = !mod),
                            onClick = { isSidebarVisible = !isSidebarVisible },
                        )
                        Item(
                            "Command Palette",
                            shortcut = KeyShortcut(Key.K, meta = mod, ctrl = !mod),
                            onClick = { isCommandPaletteOpen = !isCommandPaletteOpen },
                        )
                    }
                    Menu("Query") {
                        // ⌘↵ / Ctrl+↵ is handled by the SQL editor itself, so no accelerator is registered here.
                        Item(
                            "Run SQL",
                            enabled = isRelational && workspaceTab == RelationalTab.QUERY,
                            onClick = { RaksysCommands.requestRunQuery() },
                        )
                    }
                    Menu("Go") {
                        Item(
                            "Query Editor",
                            shortcut = KeyShortcut(Key.One, meta = mod, ctrl = !mod),
                            enabled = isRelational,
                            onClick = { workspaceTab = RelationalTab.QUERY },
                        )
                        Item(
                            "Visual ERD",
                            shortcut = KeyShortcut(Key.Two, meta = mod, ctrl = !mod),
                            enabled = isRelational,
                            onClick = { workspaceTab = RelationalTab.ERD },
                        )
                        Item(
                            "Roles & Permissions",
                            shortcut = KeyShortcut(Key.Three, meta = mod, ctrl = !mod),
                            enabled = isRelational,
                            onClick = { workspaceTab = RelationalTab.PERMISSIONS },
                        )
                    }
                }
                RaksysAppTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(RaksysThemeColors.Background)
                        ) {
                            // Environment edge: a 2 dp line across the window in the active connection's
                            // environment color, so DEV / STG / PROD is visible from anywhere in the app.
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(
                                        when (selectedProfile?.environment) {
                                            com.raksys.core.model.EnvironmentType.DEVELOPMENT -> RaksysThemeColors.EnvDev
                                            com.raksys.core.model.EnvironmentType.STAGING -> RaksysThemeColors.EnvStaging
                                            com.raksys.core.model.EnvironmentType.PRODUCTION -> RaksysThemeColors.EnvProd
                                            null -> RaksysThemeColors.Border
                                        }
                                    )
                            )
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                            if (isSidebarVisible) {
                                Box(modifier = Modifier.width(sidebarWidthDp).fillMaxHeight()) {
                                    ConnectionListScreen(
                                        selectedProfile = selectedProfile,
                                        onSelect = {
                                            selectedProfile = it
                                            selectedTable = null
                                            selectedCollection = null
                                        },
                                        showAddDialog = showAddConnectionDialog,
                                        onShowAddDialogChange = { showAddConnectionDialog = it },
                                    )
                                }

                                ResizableVerticalDivider(
                                    onDragDelta = { delta ->
                                        sidebarWidthDp = (sidebarWidthDp + delta.dp).coerceIn(170.dp, 450.dp)
                                    }
                                )
                            }

                            val currentProfile = selectedProfile
                            when {
                                currentProfile == null -> {
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                        WelcomeWorkspace(
                                            onNewConnection = { showAddConnectionDialog = true },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                currentProfile.dbType.family == DbFamily.RELATIONAL -> {
                                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                        RelationalTabBar(
                                            selected = workspaceTab,
                                            onSelect = { workspaceTab = it },
                                            isSidebarVisible = isSidebarVisible,
                                            onToggleSidebar = { isSidebarVisible = !isSidebarVisible }
                                        )
                                        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

                                        Row(modifier = Modifier.fillMaxSize()) {
                                            when (workspaceTab) {
                                                RelationalTab.QUERY -> {
                                                    Box(modifier = Modifier.width(navigatorWidthDp).fillMaxHeight()) {
                                                        SchemaNavigator(
                                                            profile = currentProfile,
                                                            selectedTable = selectedTable,
                                                            onSelectTable = { selectedTable = it },
                                                        )
                                                    }
                                                    ResizableVerticalDivider(
                                                        onDragDelta = { delta ->
                                                            navigatorWidthDp = (navigatorWidthDp + delta.dp).coerceIn(160.dp, 420.dp)
                                                        }
                                                    )
                                                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                                        QueryEditor(
                                                            profile = currentProfile,
                                                            browsingTable = selectedTable,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }

                                                RelationalTab.ERD -> {
                                                    ErdScreen(profile = currentProfile, modifier = Modifier.fillMaxSize())
                                                }

                                                RelationalTab.PERMISSIONS -> {
                                                    PermissionScreen(profile = currentProfile, modifier = Modifier.fillMaxSize())
                                                }
                                            }
                                        }
                                    }
                                }

                                currentProfile.dbType.family == DbFamily.DOCUMENT -> {
                                    Box(modifier = Modifier.width(navigatorWidthDp).fillMaxHeight()) {
                                        DocumentCollectionList(
                                            profile = currentProfile,
                                            selectedCollection = selectedCollection,
                                            onSelectCollection = { selectedCollection = it },
                                        )
                                    }
                                    ResizableVerticalDivider(
                                        onDragDelta = { delta ->
                                            navigatorWidthDp = (navigatorWidthDp + delta.dp).coerceIn(160.dp, 420.dp)
                                        }
                                    )
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                        val collection = selectedCollection
                                        if (collection != null) {
                                            DocumentViewer(
                                                profile = currentProfile,
                                                collection = collection,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        } else {
                                            RaksysEmptyState(
                                                iconContent = {
                                                    MongodbLogo(color = RaksysThemeColors.MongodbColor, size = 32.dp)
                                                },
                                                title = "Pilih Collection",
                                                description = "Pilih salah satu collection di panel kiri untuk melihat dan menyaring dokumen MongoDB."
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                        KeyValueBrowser(
                                            profile = currentProfile,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

                        // Studio Bottom Status Bar (24.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .background(RaksysThemeColors.SurfaceElevated)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val curr = selectedProfile
                                if (curr != null) {
                                    val (envColor, envLabel) = when (curr.environment) {
                                        com.raksys.core.model.EnvironmentType.DEVELOPMENT -> RaksysThemeColors.EnvDev to "DEV"
                                        com.raksys.core.model.EnvironmentType.STAGING -> RaksysThemeColors.EnvStaging to "STG"
                                        com.raksys.core.model.EnvironmentType.PRODUCTION -> RaksysThemeColors.EnvProd to "PROD"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(envColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${curr.name} • ${curr.dbType.name} [$envLabel]",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RaksysThemeColors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val dbDesc = if (curr.dbType == com.raksys.core.model.DbType.SQLITE) curr.database.ifBlank { "SQLite File" }
                                    else "${curr.host}:${curr.port} / ${curr.database.ifBlank { "default" }}"
                                    Text(
                                        text = "• $dbDesc",
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = RaksysThemeColors.TextMuted
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(RaksysThemeColors.TextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tidak Ada Koneksi Aktif",
                                        fontSize = 11.sp,
                                        color = RaksysThemeColors.TextMuted
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RaksysThemeColors.SurfaceElevated)
                                        .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(4.dp))
                                        .clickable { isCommandPaletteOpen = true }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${RaksysPlatform.shortcut("K")} Command Palette",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = RaksysThemeColors.Primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = "${RaksysPlatform.modLabel}↵ Run SQL  •  ${RaksysPlatform.shortcut("B")} Sidebar",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = RaksysThemeColors.TextMuted
                                )
                            }
                        }
                    }

                    if (isCommandPaletteOpen) {
                        CommandPaletteDialog(
                            currentProfile = selectedProfile,
                            connections = allConnections,
                            tables = currentTables,
                            onSelectProfile = { profile ->
                                selectedProfile = profile
                                selectedTable = null
                                selectedCollection = null
                            },
                            onSelectTable = { table ->
                                selectedTable = table
                                workspaceTab = RelationalTab.QUERY
                            },
                            onSelectTab = { tab ->
                                workspaceTab = tab
                            },
                            onNewConnection = {
                                showAddConnectionDialog = true
                            },
                            onToggleSidebar = {
                                isSidebarVisible = !isSidebarVisible
                            },
                            onDismiss = { isCommandPaletteOpen = false }
                        )
                    }

                    // Global Floating Toast Host (elevated above bottom status bar)
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 26.dp)) {
                        ToastHost()
                    }
                }
            }
        }
    }
}
}

