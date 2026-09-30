package com.raksys.feature.query

import com.raksys.core.model.ColumnDefinition
import com.raksys.core.model.ForeignKeyRef
import com.raksys.core.model.TableSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TableStructureInspectorTest {

    @Test
    fun `generateTableDdl generates valid CREATE TABLE DDL`() {
        val table = TableSchema(
            name = "products",
            columns = listOf(
                ColumnDefinition(name = "id", type = "BIGINT", nullable = false, isPrimaryKey = true),
                ColumnDefinition(name = "title", type = "VARCHAR(255)", nullable = false, isPrimaryKey = false),
                ColumnDefinition(name = "description", type = "TEXT", nullable = true, isPrimaryKey = false),
                ColumnDefinition(name = "price", type = "DECIMAL(10,2)", nullable = false, isPrimaryKey = false, defaultValue = "0.00"),
                ColumnDefinition(
                    name = "category_id",
                    type = "INT",
                    nullable = false,
                    isPrimaryKey = false,
                    foreignKey = ForeignKeyRef("categories", "id")
                )
            )
        )

        val ddl = generateTableDdl(table)
        assertTrue(ddl.startsWith("CREATE TABLE products ("))
        assertTrue(ddl.contains("id BIGINT PRIMARY KEY"))
        assertTrue(ddl.contains("title VARCHAR(255) NOT NULL"))
        assertTrue(ddl.contains("description TEXT"))
        assertTrue(ddl.contains("price DECIMAL(10,2) NOT NULL DEFAULT 0.00"))
        assertTrue(ddl.contains("category_id INT NOT NULL REFERENCES categories(id)"))
        assertTrue(ddl.endsWith(");"))
    }

    @Test
    fun `generateInsertTemplate generates formatted INSERT template`() {
        val table = TableSchema(
            name = "users",
            columns = listOf(
                ColumnDefinition(name = "id", type = "INT", nullable = false, isPrimaryKey = true),
                ColumnDefinition(name = "email", type = "VARCHAR", nullable = false, isPrimaryKey = false)
            )
        )

        val insert = generateInsertTemplate(table)
        assertEquals("INSERT INTO users (id, email)\nVALUES (?, ?);", insert)
    }
}
