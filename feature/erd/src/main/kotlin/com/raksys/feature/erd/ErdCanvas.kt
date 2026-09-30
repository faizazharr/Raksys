package com.raksys.feature.erd

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawWithContent
import com.raksys.core.model.ColumnDefinition
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.raksys.core.ui.RaksysSearchField
import com.raksys.core.ui.LinkIcon
import com.raksys.core.ui.KeyIcon
import com.raksys.core.ui.RaksysThemeColors
import com.raksys.core.ui.ToastManager
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import kotlin.math.abs

private fun copyToClipboard(text: String) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    } catch (_: Exception) {}
}

/**
 * Generates official Mermaid ER Diagram Markdown string from table schemas.
 */
fun generateMermaidErd(tables: List<TableSchema>): String {
    val sb = StringBuilder("erDiagram\n")
    // 1. Tables and columns definition
    tables.forEach { table ->
        sb.append("    ${table.name} {\n")
        table.columns.forEach { col ->
            val cleanType = col.type.replace(Regex("[^a-zA-Z0-9_]"), "_").lowercase().ifBlank { "string" }
            val keyTag = when {
                col.isPrimaryKey -> " PK"
                col.foreignKey != null -> " FK"
                else -> ""
            }
            sb.append("        $cleanType ${col.name}$keyTag\n")
        }
        sb.append("    }\n")
    }

    // 2. Foreign Key relationships
    val addedEdges = mutableSetOf<String>()
    tables.forEach { table ->
        table.columns.forEach { col ->
            col.foreignKey?.let { fk ->
                val edgeKey = "${fk.referencedTable}-->${table.name}:${col.name}"
                if (addedEdges.add(edgeKey)) {
                    sb.append("    ${fk.referencedTable} ||--o{ ${table.name} : \"${col.name}\"\n")
                }
            }
        }
    }
    return sb.toString()
}

@Composable
fun ErdCanvas(tables: List<TableSchema>, modifier: Modifier = Modifier) {
    val boxes = remember(tables) { computeErdLayout(tables) }
    val edges = remember(boxes) { computeErdEdges(boxes) }

    var zoomScale by remember { mutableStateOf(1f) }
    // Hover changes on every mouse move between tables. It is kept as a State object and read only
    // inside draw / layer blocks and per-card derived states, so hovering does NOT recompose the
    // whole diagram (with 80 tables that used to cost more than a full CPU core).
    val hoveredState = remember { mutableStateOf<String?>(null) }
    val setHovered = remember { { name: String? -> hoveredState.value = name } }
    var erdSearchQuery by remember { mutableStateOf("") }

    // Tables directly related to the hovered table.
    val relatedState = remember(edges) {
        derivedStateOf {
            val target = hoveredState.value
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
    }
    val searchState = rememberUpdatedState(erdSearchQuery)

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
                // Room for the floating search bar so it never covers the first row of tables.
                .padding(top = 60.dp)
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
                    val hoveredTableName = hoveredState.value
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
                    key(box.table.name) {
                        ErdCardHost(
                            box = box,
                            hoveredState = hoveredState,
                            relatedState = relatedState,
                            searchState = searchState,
                            setHovered = setHovered,
                        )
                    }
                }
            }
        }

        // Floating Table Search Bar (Top Left)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(RaksysThemeColors.SurfaceElevated)
                .border(1.dp, RaksysThemeColors.BorderLight, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RaksysSearchField(
                value = erdSearchQuery,
                onValueChange = { erdSearchQuery = it },
                placeholder = "Cari tabel di ERD...",
                modifier = Modifier.width(180.dp)
            )

            if (erdSearchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                val matchCount = tables.count { it.name.contains(erdSearchQuery, ignoreCase = true) }
                Text(
                    text = "$matchCount/${tables.size}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (matchCount > 0) RaksysThemeColors.Primary else RaksysThemeColors.Error
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { erdSearchQuery = "" },
                    modifier = Modifier.size(20.dp)
                ) {
                    Text("✕", modifier = Modifier.semantics { contentDescription = "Hapus pencarian" }, fontSize = 11.sp, color = RaksysThemeColors.TextMuted)
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
                Text("－", modifier = Modifier.semantics { contentDescription = "Perkecil" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.TextPrimary)
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
                Text("＋", modifier = Modifier.semantics { contentDescription = "Perbesar" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.TextPrimary)
            }

            Box(modifier = Modifier.height(16.dp).width(1.dp).background(RaksysThemeColors.Border))

            TextButton(
                onClick = { zoomScale = 1.0f },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text("Fit 1:1", fontSize = 11.sp, color = RaksysThemeColors.Primary)
            }

            Box(modifier = Modifier.height(16.dp).width(1.dp).background(RaksysThemeColors.Border))

            Button(
                onClick = {
                    val mermaid = generateMermaidErd(tables)
                    copyToClipboard(mermaid)
                    ToastManager.show("Diagram Mermaid (${tables.size} tabel) disalin ke clipboard")
                },
                colors = ButtonDefaults.buttonColors(containerColor = RaksysThemeColors.PrimaryFill),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text("Export Mermaid", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/**
 * One table on the canvas. It reads the shared hover / search states itself, through a per-card
 * derived state, so a hover change recomposes only the few cards whose highlight actually changes.
 * Dimming is applied in the graphics layer, which repaints without recomposing.
 */
@Composable
private fun ErdCardHost(
    box: TableLayoutBox,
    hoveredState: State<String?>,
    relatedState: State<Set<String>>,
    searchState: State<String>,
    setHovered: (String?) -> Unit,
) {
    val name = box.table.name
    val isHighlighted by remember(name) {
        derivedStateOf {
            val hovered = hoveredState.value
            val search = searchState.value
            (search.isNotBlank() && name.contains(search, ignoreCase = true)) ||
                hovered == name ||
                (hovered != null && name in relatedState.value)
        }
    }
    val onHoverChange = remember(name) { { hovered: Boolean -> setHovered(if (hovered) name else null) } }
    val dimColor = RaksysThemeColors.Background

    TableErdCard(
        table = box.table,
        isHighlighted = isHighlighted,
        onHoverChange = onHoverChange,
        modifier = Modifier
            .offset(x = box.xDp.dp, y = box.yDp.dp)
            .width(box.widthDp.dp)
            .height(box.heightDp.dp)
            // Dimming is a translucent wash drawn on top, not `alpha`: alpha turns each of the (many)
            // cards into an offscreen layer and re-composites all of them on every hover change.
            .drawWithContent {
                drawContent()
                val search = searchState.value
                val hovered = hoveredState.value
                val matches = search.isNotBlank() && name.contains(search, ignoreCase = true)
                val visibility = when {
                    search.isNotBlank() && !matches -> 0.25f
                    hovered == null || name in relatedState.value || matches -> 1.0f
                    else -> 0.4f
                }
                if (visibility < 1f) {
                    drawRoundRect(
                        color = dimColor.copy(alpha = 1f - visibility),
                        cornerRadius = CornerRadius(8.dp.toPx()),
                    )
                }
            },
    )
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
        ErdColumns(table.columns)
    }
}

/**
 * Column rows of a card. A LazyColumn per card is costly to set up and there can be dozens of cards,
 * so ordinary tables use a plain scrolling Column and only very wide tables stay lazy.
 */
@Composable
private fun ErdColumns(columns: List<ColumnDefinition>) {
    if (columns.size <= LAZY_COLUMN_THRESHOLD) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).verticalScroll(rememberScrollState())) {
            columns.forEach { ErdColumnRow(it) }
        }
    } else {
        LazyColumn(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            items(columns, key = { it.name }) { ErdColumnRow(it) }
        }
    }
}

private const val LAZY_COLUMN_THRESHOLD = 40

@Composable
private fun ErdColumnRow(column: ColumnDefinition) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            column.isPrimaryKey -> KeyIcon(color = RaksysThemeColors.Warning, size = 11.dp, contentDescription = "Primary key")
            column.foreignKey != null -> LinkIcon(color = RaksysThemeColors.Info, size = 11.dp, contentDescription = "Foreign key")
            else -> Text("•", fontSize = 11.sp, color = RaksysThemeColors.TextMuted)
        }
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
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = RaksysThemeColors.TextMuted,
            maxLines = 1,
        )
    }
}
