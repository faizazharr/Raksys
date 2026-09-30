package com.raksys.feature.query

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.ui.PlusIcon
import com.raksys.core.ui.RaksysThemeColors
import com.raksys.core.ui.raksysInteractive

/** Strip of SQL console tabs: select, close (when more than one), and add. */
@Composable
fun SqlTabBar(
    tabs: SqlTabs,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RaksysThemeColors.Surface)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tabs.tabs.forEach { tab ->
            val selected = tab.id == tabs.activeId
            val shape = RoundedCornerShape(6.dp)
            Row(
                modifier = Modifier
                    .height(28.dp)
                    .clip(shape)
                    .background(if (selected) RaksysThemeColors.PrimaryContainer else Color.Transparent)
                    .border(1.dp, if (selected) RaksysThemeColors.Primary.copy(alpha = 0.6f) else Color.Transparent, shape)
                    .raksysInteractive(shape, enabled = !selected)
                    .clickable { onSelect(tab.id) }
                    .padding(start = 10.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tab.title,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) RaksysThemeColors.Primary else RaksysThemeColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (tabs.tabs.size > 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onClose(tab.id) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 11.sp,
                            color = RaksysThemeColors.TextSecondary,
                            modifier = Modifier.semantics { contentDescription = "Tutup ${tab.title}" },
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .raksysInteractive(RoundedCornerShape(6.dp))
                .clickable { onAdd() },
            contentAlignment = Alignment.Center,
        ) {
            PlusIcon(color = RaksysThemeColors.TextSecondary, size = 13.dp, contentDescription = "Tab kueri baru")
        }
    }
}
