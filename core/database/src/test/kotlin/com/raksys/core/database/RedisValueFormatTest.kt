package com.raksys.core.database

import kotlin.test.Test
import kotlin.test.assertEquals

class RedisValueFormatTest {

    @Test
    fun `hash formats as json-like object`() {
        val result = formatRedisHash(linkedMapOf("name" to "Alice", "role" to "admin"))
        assertEquals("{\"name\": \"Alice\", \"role\": \"admin\"}", result)
    }

    @Test
    fun `hash escapes quotes and backslashes in values`() {
        val result = formatRedisHash(linkedMapOf("note" to "she said \"hi\" \\ bye"))
        assertEquals("{\"note\": \"she said \\\"hi\\\" \\\\ bye\"}", result)
    }

    @Test
    fun `list formats as json-like array`() {
        val result = formatRedisList(listOf("a", "b", "c"))
        assertEquals("[\"a\", \"b\", \"c\"]", result)
    }

    @Test
    fun `empty set formats as empty array`() {
        val result = formatRedisSet(emptySet())
        assertEquals("[]", result)
    }
}
