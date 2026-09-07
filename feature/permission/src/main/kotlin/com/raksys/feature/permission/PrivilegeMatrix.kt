package com.raksys.feature.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    val grantedByTable = rolePrivileges.associateBy { it.tableName }

    LazyColumn(contentPadding = PaddingValues(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("Table", modifier = Modifier.width(180.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RaksysThemeColors.TextSecondary)
                PrivilegeType.entries.forEach {
                    Text(it.name, modifier = Modifier.width(80.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RaksysThemeColors.TextSecondary)
                }
            }
        }
        items(tables, key = { it }) { table ->
            val granted = grantedByTable[table]?.privileges.orEmpty()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(RaksysThemeColors.Surface)
                    .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(6.dp))
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    table,
                    modifier = Modifier.width(180.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = RaksysThemeColors.TextPrimary,
                )
                PrivilegeType.entries.forEach { privilege ->
                    Checkbox(
                        checked = privilege in granted,
                        onCheckedChange = { checked -> onToggle(table, privilege, checked) },
                        colors = CheckboxDefaults.colors(checkedColor = RaksysThemeColors.Primary),
                        modifier = Modifier.width(80.dp),
                    )
                }
            }
        }
    }
}
