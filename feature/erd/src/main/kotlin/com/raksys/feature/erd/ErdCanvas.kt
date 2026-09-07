package com.raksys.feature.erd

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.TableSchema
import com.raksys.core.ui.RaksysThemeColors
import kotlin.math.abs

@Composable
fun ErdCanvas(tables: List<TableSchema>, modifier: Modifier = Modifier) {
    val boxes = remember(tables) { computeErdLayout(tables) }
    val edges = remember(boxes) { computeErdEdges(boxes) }

    var zoomScale by remember { mutableStateOf(1f) }
    var hoveredTableName by remember { mutableStateOf<String?>(null) }

    // Find all tables directly related to the hovered table
    val relatedTableNames = remember(hoveredTableName, edges) {
        val target = hoveredTableName
        if (target == null) emptySet()
        else {
            val connected = mutableSetOf(target)
            edges.forEach { edge ->
                if (edge.fromTable == target) connected.add(edge.toTable)
                if (edge.toTable == target) connected.add(edge.fromTable)
            }
            connected
        }
    }

    val totalWidthDp = ((boxes.maxOfOrNull { it.xDp + it.widthDp } ?: 0) + 80)
    val totalHeightDp = ((boxes.maxOfOrNull { it.yDp + it.heightDp } ?: 0) + 80)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RaksysThemeColors.Background)
    ) {
        // Scrollable Canvas Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .width(totalWidthDp.dp)
                    .height(totalHeightDp.dp)
                    .graphicsLayer {
                        scaleX = zoomScale
                        scaleY = zoomScale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            ) {
                // Smooth Bézier Edge Canvas
                Canvas(modifier = Modifier.width(totalWidthDp.dp).height(totalHeightDp.dp)) {
                    edges.forEach { edge ->
                        val isHighlighted = hoveredTableName != null &&
                                (edge.fromTable == hoveredTableName || edge.toTable == hoveredTableName)
                        val isDimmed = hoveredTableName != null && !isHighlighted

                        val edgeColor = when {
                            isHighlighted -> RaksysThemeColors.Primary
                            isDimmed -> RaksysThemeColors.Border.copy(alpha = 0.35f)
                            else -> RaksysThemeColors.BorderLight
                        }
                        val strokeW = if (isHighlighted) 3.dp.toPx() else 1.5.dp.toPx()

                        val startX = edge.fromX.dp.toPx()
                        val startY = edge.fromY.dp.toPx()
                        val endX = edge.toX.dp.toPx()
                        val endY = edge.toY.dp.toPx()

                        val dx = abs(endX - startX)
                        val controlX1 = if (endX >= startX) startX + dx * 0.5f else startX - dx * 0.5f
                        val controlX2 = if (endX >= startX) endX - dx * 0.5f else endX + dx * 0.5f

                        val bezier = Path().apply {
                            moveTo(startX, startY)
                            cubicTo(controlX1, startY, controlX2, endY, endX, endY)
                        }

                        drawPath(path = bezier, color = edgeColor, style = Stroke(width = strokeW))

                        // Target Arrow/Dot
                        drawCircle(
                            color = edgeColor,
                            radius = if (isHighlighted) 4.5.dp.toPx() else 3.dp.toPx(),
                            center = Offset(endX, endY)
                        )
                    }
                }

                // Table Cards
                boxes.forEach { box ->
                    val isTableHovered = hoveredTableName == box.table.name
                    val isTableRelated = relatedTableNames.contains(box.table.name)
                    val tableAlpha = if (hoveredTableName == null || isTableRelated) 1.0f else 0.4f

                    TableErdCard(
                        table = box.table,
                        isHighlighted = isTableHovered || (hoveredTableName != null && isTableRelated),
                        onHoverChange = { isHovered ->
                            hoveredTableName = if (isHovered) box.table.name else null
                        },
                        modifier = Modifier
                            .offset(x = box.xDp.dp, y = box.yDp.dp)
                            .width(box.widthDp.dp)
                            .height(box.heightDp.dp)
                            .alpha(tableAlpha),
                    )
                }
            }
        }

        // Floating Zoom & Canvas Controls (Bottom Right)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(RaksysThemeColors.SurfaceElevated)
                .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { zoomScale = (zoomScale - 0.15f).coerceAtLeast(0.4f) },
                modifier = Modifier.size(28.dp)
            ) {
                Text("－", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.TextPrimary)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { zoomScale = 1.0f }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${(zoomScale * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RaksysThemeColors.TextSecondary
                )
            }

            IconButton(
                onClick = { zoomScale = (zoomScale + 0.15f).coerceAtMost(2.2f) },
                modifier = Modifier.size(28.dp)
            ) {
                Text("＋", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.TextPrimary)
            }

            Box(modifier = Modifier.height(16.dp).width(1.dp).background(RaksysThemeColors.Border))

            TextButton(
                onClick = { zoomScale = 1.0f },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text("Fit 1:1", fontSize = 10.sp, color = RaksysThemeColors.Primary)
            }
        }
    }
}

@Composable
private fun TableErdCard(
    table: TableSchema,
    isHighlighted: Boolean,
    onHoverChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isCardHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isCardHovered) {
        onHoverChange(isCardHovered)
    }

    val borderColor = when {
        isHighlighted -> RaksysThemeColors.Primary
        isCardHovered -> RaksysThemeColors.SplitterHover
        else -> RaksysThemeColors.BorderLight
    }

    Column(
        modifier = modifier
            .hoverable(interactionSource)
            .shadow(if (isHighlighted) 8.dp else 2.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(RaksysThemeColors.Surface)
            .border(if (isHighlighted) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(8.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isHighlighted) RaksysThemeColors.PrimaryContainer else RaksysThemeColors.SurfaceElevated)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⊞", fontSize = 12.sp, color = if (isHighlighted) RaksysThemeColors.Primary else RaksysThemeColors.TextSecondary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = table.name,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = RaksysThemeColors.TextPrimary,
            )
        }
        HorizontalDivider(color = RaksysThemeColors.Border, thickness = 1.dp)
        LazyColumn(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            items(table.columns, key = { it.name }) { column ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val marker = when {
                        column.isPrimaryKey -> "🔑"
                        column.foreignKey != null -> "🔗"
                        else -> "•"
                    }
                    Text(marker, fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = column.name,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (column.isPrimaryKey) RaksysThemeColors.Primary else RaksysThemeColors.TextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = column.type,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RaksysThemeColors.TextMuted,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
