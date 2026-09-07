package com.raksys.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.ui.DatabaseIcon
import com.raksys.core.ui.DbTypeLogo
import com.raksys.core.ui.LightningIcon
import com.raksys.core.ui.RaksysThemeColors
import com.raksys.core.ui.TableGridIcon

@Composable
fun WelcomeWorkspace(
    onNewConnection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RaksysThemeColors.Background)
    ) {
        // --- 1. Top Bar (Tinggi 46.dp, persis simetris dengan Sidebar Connection) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(RaksysThemeColors.Surface)
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
                    DatabaseIcon(color = RaksysThemeColors.Primary, size = 14.dp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Studio Workspace",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaksysThemeColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(RaksysThemeColors.SurfaceElevated)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Idle • Siap Dihubungkan",
                        fontSize = 11.sp,
                        color = RaksysThemeColors.TextSecondary
                    )
                }
            }

            Button(
                onClick = onNewConnection,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RaksysThemeColors.Primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("+ Buat Koneksi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        // --- 2. Action Banner (Kompak, Mengisi Area Atas Workspace) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RaksysThemeColors.SurfaceElevated)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(RaksysThemeColors.Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💡", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Kelola database relasional SQL, dokumen MongoDB, dan Redis cache dalam satu jendela.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RaksysThemeColors.TextPrimary
                    )
                    Text(
                        text = "Pilih profil koneksi di panel kiri atau klik setup cepat untuk mengonfigurasi host baru.",
                        fontSize = 11.sp,
                        color = RaksysThemeColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
                onClick = onNewConnection,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RaksysThemeColors.Primary),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(RaksysThemeColors.Primary)
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Setup Cepat →", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        // --- 3. Workspace Cards Grid (Simetris 1:1, Mepet 12dp padding) ---
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Kolom Kiri: Alur Kerja Studio (Workflow)
            Card(
                colors = CardDefaults.cardColors(containerColor = RaksysThemeColors.Surface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(RaksysThemeColors.PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            LightningIcon(color = RaksysThemeColors.Primary, size = 14.dp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Alur Kerja Studio (Workflow)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RaksysThemeColors.TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    QuickActionStep(
                        step = "1",
                        title = "Hubungkan Sumber Data",
                        desc = "Masukkan host, port, nama database, serta otentikasi login.",
                        onClick = onNewConnection
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickActionStep(
                        step = "2",
                        title = "Inspeksi Skema & Metadata",
                        desc = "Daftar tabel, kolom, primary key, dan struktur indeks skema.",
                        onClick = null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickActionStep(
                        step = "3",
                        title = "Eksekusi Query & Grid Data",
                        desc = "Tulis query SQL/NoSQL dan navigasi result set per halaman.",
                        onClick = null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickActionStep(
                        step = "4",
                        title = "Akses Jaringan Privat (SSH)",
                        desc = "Terowongan aman SSH Tunnel (Bastion host) untuk server internal.",
                        onClick = null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickActionStep(
                        step = "5",
                        title = "Eksplorasi & Manajemen Data",
                        desc = "Filter kolom, pagination cerdas, dan navigasi multi-koleksi.",
                        onClick = null
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Tip box kompak bawah
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(RaksysThemeColors.SurfaceElevated)
                            .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔒", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kredensial disimpan aman via OS Keyring (Apple Keychain / Secret Service).",
                                fontSize = 11.sp,
                                color = RaksysThemeColors.TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Kolom Kanan: Database Engine Profiles (Simetris 1:1)
            Card(
                colors = CardDefaults.cardColors(containerColor = RaksysThemeColors.Surface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(RaksysThemeColors.PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            TableGridIcon(color = RaksysThemeColors.Primary, size = 14.dp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Engine Didukung & Port Standar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RaksysThemeColors.TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    EngineInfoRow(
                        name = "PostgreSQL",
                        port = "5432",
                        type = "SQL Relasional",
                        desc = "Pagination, schema navigator, table viewer",
                        color = RaksysThemeColors.PostgresColor,
                        dbTypeName = "POSTGRES",
                        onClick = onNewConnection
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EngineInfoRow(
                        name = "MySQL / MariaDB",
                        port = "3306",
                        type = "SQL Relasional",
                        desc = "Fast query execution & connection pooling",
                        color = RaksysThemeColors.MysqlColor,
                        dbTypeName = "MYSQL",
                        onClick = onNewConnection
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EngineInfoRow(
                        name = "SQLite",
                        port = "File",
                        type = "Embedded",
                        desc = "File database lokal (.db / .sqlite) langsung",
                        color = RaksysThemeColors.SqliteColor,
                        dbTypeName = "SQLITE",
                        onClick = onNewConnection
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EngineInfoRow(
                        name = "MongoDB",
                        port = "27017",
                        type = "NoSQL Document",
                        desc = "Collection browser, JSON viewer, field filter",
                        color = RaksysThemeColors.MongodbColor,
                        dbTypeName = "MONGODB",
                        onClick = onNewConnection
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EngineInfoRow(
                        name = "Redis",
                        port = "6379",
                        type = "Key-Value",
                        desc = "Key scanner (String, Hash, Set, List) & TTL",
                        color = RaksysThemeColors.RedisColor,
                        dbTypeName = "REDIS",
                        onClick = onNewConnection
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Tip box kompak bawah di kolom kanan agar persis simetris tingginya
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(RaksysThemeColors.SurfaceElevated)
                            .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dukungan multi-engine memungkinkan analisis data SQL & NoSQL sekaligus.",
                                fontSize = 11.sp,
                                color = RaksysThemeColors.TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionStep(
    step: String,
    title: String,
    desc: String,
    onClick: (() -> Unit)?,
) {
    val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(RaksysThemeColors.SurfaceElevated)
            .border(1.dp, RaksysThemeColors.Border.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
            .then(clickModifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(RaksysThemeColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.TextPrimary)
            Text(desc, fontSize = 11.sp, color = RaksysThemeColors.TextSecondary)
        }

        if (onClick != null) {
            Text("→", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.Primary)
        }
    }
}

@Composable
private fun EngineInfoRow(
    name: String,
    port: String,
    type: String,
    desc: String,
    color: Color,
    dbTypeName: String = "",
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(RaksysThemeColors.SurfaceElevated)
            .border(1.dp, RaksysThemeColors.Border.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
            .then(clickModifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            if (dbTypeName.isNotBlank()) {
                DbTypeLogo(typeName = dbTypeName, color = color, size = 14.dp)
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.TextPrimary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = 0.18f))
                        .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (port == "File") "FILE" else ":$port",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = color,
                        textAlign = TextAlign.Center,
                        lineHeight = 10.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, fontSize = 10.sp, color = RaksysThemeColors.TextMuted)
        }
    }
}
