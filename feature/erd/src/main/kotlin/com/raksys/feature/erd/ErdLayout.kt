package com.raksys.feature.erd

import com.raksys.core.model.TableSchema
import kotlin.math.ceil
import kotlin.math.sqrt

data class TableLayoutBox(
    val table: TableSchema,
    val xDp: Int,
    val yDp: Int,
    val widthDp: Int,
    val heightDp: Int,
)

data class ErdEdge(
    val fromTable: String,
    val toTable: String,
    val fromX: Int,
    val fromY: Int,
    val toX: Int,
    val toY: Int,
)

const val ERD_BOX_WIDTH_DP = 220
const val ERD_BOX_HEIGHT_DP = 220
private const val GAP_X_DP = 80
private const val GAP_Y_DP = 60

/** Simple grid auto-layout: roughly square, uniform cell size. Not force-directed, but deterministic and legible. */
fun computeErdLayout(
    tables: List<TableSchema>,
    boxWidthDp: Int = ERD_BOX_WIDTH_DP,
    boxHeightDp: Int = ERD_BOX_HEIGHT_DP,
): List<TableLayoutBox> {
    if (tables.isEmpty()) return emptyList()
    val columns = ceil(sqrt(tables.size.toDouble())).toInt().coerceAtLeast(1)
    return tables.mapIndexed { index, table ->
        val col = index % columns
        val row = index / columns
        TableLayoutBox(
            table = table,
            xDp = col * (boxWidthDp + GAP_X_DP),
            yDp = row * (boxHeightDp + GAP_Y_DP),
            widthDp = boxWidthDp,
            heightDp = boxHeightDp,
        )
    }
}

/** One edge per foreign key column, anchored to the nearest horizontal edge between the two boxes. */
fun computeErdEdges(boxes: List<TableLayoutBox>): List<ErdEdge> {
    val byName = boxes.associateBy { it.table.name }
    val edges = mutableListOf<ErdEdge>()
    for (box in boxes) {
        for (column in box.table.columns) {
            val fk = column.foreignKey ?: continue
            val target = byName[fk.referencedTable] ?: continue
            if (target.table.name == box.table.name) continue

            val (fromX, toX) = if (box.xDp <= target.xDp) {
                (box.xDp + box.widthDp) to target.xDp
            } else {
                box.xDp to (target.xDp + target.widthDp)
            }
            edges += ErdEdge(
                fromTable = box.table.name,
                toTable = target.table.name,
                fromX = fromX,
                fromY = box.yDp + box.heightDp / 2,
                toX = toX,
                toY = target.yDp + target.heightDp / 2,
            )
        }
    }
    return edges
}
