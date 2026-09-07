package com.raksys.feature.erd

import com.raksys.core.model.ColumnDefinition
import com.raksys.core.model.ForeignKeyRef
import com.raksys.core.model.TableSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErdLayoutTest {

    private fun table(name: String, columns: List<ColumnDefinition> = emptyList()) = TableSchema(name, columns)

    @Test
    fun `empty table list produces empty layout`() {
        assertEquals(emptyList(), computeErdLayout(emptyList()))
    }

    @Test
    fun `single table is placed at origin`() {
        val boxes = computeErdLayout(listOf(table("users")))
        assertEquals(1, boxes.size)
        assertEquals(0, boxes[0].xDp)
        assertEquals(0, boxes[0].yDp)
    }

    @Test
    fun `tables are laid out in a grid without overlapping`() {
        val tables = (1..9).map { table("table_$it") }
        val boxes = computeErdLayout(tables, boxWidthDp = 100, boxHeightDp = 100)

        val positions = boxes.map { it.xDp to it.yDp }
        assertEquals(positions.size, positions.toSet().size, "no two tables should share the same top-left position")
    }

    @Test
    fun `edge is drawn for a foreign key referencing another table`() {
        val orders = table(
            "orders",
            listOf(ColumnDefinition("user_id", "int", nullable = false, isPrimaryKey = false, foreignKey = ForeignKeyRef("users", "id"))),
        )
        val users = table("users", listOf(ColumnDefinition("id", "int", nullable = false, isPrimaryKey = true)))

        val boxes = computeErdLayout(listOf(users, orders))
        val edges = computeErdEdges(boxes)

        assertEquals(1, edges.size)
        assertEquals("orders", edges[0].fromTable)
        assertEquals("users", edges[0].toTable)
    }

    @Test
    fun `foreign key referencing an unknown table produces no edge`() {
        val orders = table(
            "orders",
            listOf(ColumnDefinition("user_id", "int", nullable = false, isPrimaryKey = false, foreignKey = ForeignKeyRef("missing_table", "id"))),
        )

        val boxes = computeErdLayout(listOf(orders))
        assertTrue(computeErdEdges(boxes).isEmpty())
    }

    @Test
    fun `self-referencing foreign key produces no edge to avoid a degenerate line`() {
        val employees = table(
            "employees",
            listOf(ColumnDefinition("manager_id", "int", nullable = true, isPrimaryKey = false, foreignKey = ForeignKeyRef("employees", "id"))),
        )

        val boxes = computeErdLayout(listOf(employees))
        assertTrue(computeErdEdges(boxes).isEmpty())
    }
}
