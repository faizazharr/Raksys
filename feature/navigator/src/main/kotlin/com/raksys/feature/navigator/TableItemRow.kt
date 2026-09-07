package com.raksys.feature.navigator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.TableSchema
import com.raksys.core.ui.RaksysThemeColors

@Composable
fun TableItemRow(
    table: TableSchema,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (isSelected) RaksysThemeColors.PrimaryContainer else Color.Transparent
    val borderColor = if (isSelected) RaksysThemeColors.Primary else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) RaksysThemeColors.Primary else RaksysThemeColors.SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⊞",
                fontSize = 11.sp,
                color = if (isSelected) Color.White else RaksysThemeColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = table.name,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) RaksysThemeColors.TextPrimary else RaksysThemeColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (table.columns.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(RaksysThemeColors.SurfaceElevated)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "${table.columns.size} col",
                    fontSize = 10.sp,
                    color = RaksysThemeColors.TextMuted
                )
            }
        }
    }
}
