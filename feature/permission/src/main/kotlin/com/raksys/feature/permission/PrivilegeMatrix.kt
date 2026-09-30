package com.raksys.feature.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.PrivilegeType
import com.raksys.core.model.TablePrivilege
import com.raksys.core.ui.RaksysThemeColors

@Composable
fun PrivilegeMatrix(
    tables: List<String>,
    rolePrivileges: List<TablePrivilege>,
    onToggle: (table: String, privilege: PrivilegeType, checked: Boolean) -> Unit,
) {
    var filterText by remember { mutableStateOf("") }
    val grantedByTable = remember(rolePrivileges) { rolePrivileges.associateBy { it.tableName } }

    val filteredTables = remember(tables, filterText) {
        if (filterText.isBlank()) tables
        else tables.filter { it.contains(filterText, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Matrix Header Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RaksysThemeColors.SurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Matriks Hak Akses (${filteredTables.size} tabel)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RaksysThemeColors.TextPrimary
            )

            // Search Filter
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(RaksysThemeColors.Background)
                    .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔍", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    BasicTextField(
                        value = filterText,
                        onValueChange = { filterText = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 11.sp, color = RaksysThemeColors.TextPrimary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (filterText.isEmpty()) {
                                Text("Filter tabel...", fontSize = 10.sp, color = RaksysThemeColors.TextMuted)
                            }
                            inner()
                        }
                    )
                    if (filterText.isNotEmpty()) {
                        Text(
                            text = "✕",
                            fontSize = 10.sp,
                            color = RaksysThemeColors.TextMuted,
                            modifier = Modifier.clickable { filterText = "" }.padding(2.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)

        // Matrix Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
        ) {
            LazyColumn(contentPadding = PaddingValues(10.dp), modifier = Modifier.fillMaxHeight()) {
                item {
                    Row(
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tabel",
                            modifier = Modifier.width(180.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = RaksysThemeColors.TextSecondary
                        )

                        PrivilegeType.entries.forEach {
                            Text(
                                text = it.name,
                                modifier = Modifier.width(72.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = RaksysThemeColors.TextSecondary
                            )
                        }

                        Text(
                            text = "Aksi Massal",
                            modifier = Modifier.width(90.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = RaksysThemeColors.TextSecondary
                        )
                    }
                }

                items(filteredTables, key = { it }) { table ->
                    val granted = grantedByTable[table]?.privileges.orEmpty()
                    val allGranted = PrivilegeType.entries.all { it in granted }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RaksysThemeColors.Surface)
                            .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = table,
                            modifier = Modifier.width(180.dp),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = RaksysThemeColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        PrivilegeType.entries.forEach { privilege ->
                            Checkbox(
                                checked = privilege in granted,
                                onCheckedChange = { checked -> onToggle(table, privilege, checked) },
                                colors = CheckboxDefaults.colors(checkedColor = RaksysThemeColors.Primary),
                                modifier = Modifier.width(72.dp),
                            )
                        }

                        // Batch Action Button (Semua / Cabut)
                        OutlinedButton(
                            onClick = {
                                val targetChecked = !allGranted
                                PrivilegeType.entries.forEach { p ->
                                    if ((p in granted) != targetChecked) {
                                        onToggle(table, p, targetChecked)
                                    }
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (allGranted) RaksysThemeColors.Error else RaksysThemeColors.Primary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (allGranted) RaksysThemeColors.Error.copy(alpha = 0.5f) else RaksysThemeColors.Primary.copy(alpha = 0.5f)
                                )
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(24.dp).width(85.dp)
                        ) {
                            Text(
                                text = if (allGranted) "✕ Cabut" else "✓ Semua",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
