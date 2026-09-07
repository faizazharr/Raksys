package com.raksys.feature.query

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.ui.RaksysThemeColors

/**
 * Prev/Next controls for the safe, app-generated "browse table" query (LIMIT/OFFSET built by
 * Raksys itself). Intentionally not available for free-typed SQL — auto-appending OFFSET to an
 * arbitrary user query is unsafe (it may already have its own LIMIT/ORDER BY/subqueries).
 */
@Composable
fun TablePaginationBar(
    tableName: String,
    page: Int,
    pageSize: Int,
    rowsOnPage: Int,
    isLoading: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RaksysThemeColors.SurfaceElevated)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$tableName • baris ${page * pageSize + 1}-${page * pageSize + rowsOnPage}",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = RaksysThemeColors.TextSecondary,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(
                onClick = onPrev,
                enabled = !isLoading && page > 0,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(28.dp),
            ) {
                Text("‹ Prev", fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = onNext,
                enabled = !isLoading && rowsOnPage >= pageSize,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(28.dp),
            ) {
                Text("Next ›", fontSize = 11.sp)
            }
        }
    }
}
