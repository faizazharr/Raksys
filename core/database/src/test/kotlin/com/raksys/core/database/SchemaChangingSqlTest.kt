package com.raksys.core.database

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SchemaChangingSqlTest {

    @Test
    fun `DDL statements invalidate the cached schema`() {
        assertTrue(isSchemaChangingSql("CREATE TABLE users (id INT)"))
        assertTrue(isSchemaChangingSql("  alter table users add column age int"))
        assertTrue(isSchemaChangingSql("DROP TABLE users;"))
        assertTrue(isSchemaChangingSql("rename table a to b"))
    }

    @Test
    fun `reads and data changes keep the cached schema`() {
        assertFalse(isSchemaChangingSql("SELECT * FROM users"))
        assertFalse(isSchemaChangingSql("INSERT INTO users (id) VALUES (1)"))
        assertFalse(isSchemaChangingSql("UPDATE users SET name = 'a' WHERE id = 1"))
        assertFalse(isSchemaChangingSql("SELECT 'CREATE TABLE x' AS text"))
    }
}
