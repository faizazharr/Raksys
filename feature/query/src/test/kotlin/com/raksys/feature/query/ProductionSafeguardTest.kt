package com.raksys.feature.query

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductionSafeguardTest {

    @Test
    fun `detects DROP TABLE as destructive`() {
        assertTrue(isDestructiveSql("DROP TABLE users;"))
        assertTrue(isDestructiveSql("drop table if exists orders cascade;"))
        assertTrue(isDestructiveSql("DROP DATABASE production_db;"))
        assertTrue(isDestructiveSql("DROP SCHEMA public;"))
    }

    @Test
    fun `detects TRUNCATE TABLE as destructive`() {
        assertTrue(isDestructiveSql("TRUNCATE TABLE logs;"))
        assertTrue(isDestructiveSql("truncate payments;"))
    }

    @Test
    fun `detects DELETE without WHERE as destructive`() {
        assertTrue(isDestructiveSql("DELETE FROM users;"))
        assertTrue(isDestructiveSql("delete from customers"))
    }

    @Test
    fun `allows safe SELECT and safe queries`() {
        assertFalse(isDestructiveSql("SELECT * FROM users WHERE active = true;"))
        assertFalse(isDestructiveSql("SELECT COUNT(*) FROM orders;"))
        assertFalse(isDestructiveSql("INSERT INTO audit_logs (id, event) VALUES (1, 'login');"))
    }

    @Test
    fun `allows DELETE with WHERE clause`() {
        assertFalse(isDestructiveSql("DELETE FROM sessions WHERE expired_at < NOW();"))
    }

    @Test
    fun `detects UPDATE without WHERE as destructive`() {
        assertTrue(isDestructiveSql("UPDATE users SET is_active = false;"))
        assertFalse(isDestructiveSql("UPDATE users SET is_active = false WHERE id = 42;"))
    }
}
