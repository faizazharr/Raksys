package com.raksys.feature.grid

import kotlin.test.Test
import kotlin.test.assertEquals

class GridExportTest {

    private val columns = listOf("id", "name", "note")

    @Test
    fun `csv quotes every cell, doubles quotes, and leaves NULL empty`() {
        val csv = rowsToCsv(columns, listOf(listOf(1, "Ana \"A\"", null)))
        assertEquals("\"id\",\"name\",\"note\"\n\"1\",\"Ana \"\"A\"\"\",\n", csv)
    }

    @Test
    fun `csv pads short rows to the column count`() {
        val csv = rowsToCsv(columns, listOf(listOf(1)))
        assertEquals("\"id\",\"name\",\"note\"\n\"1\",,\n", csv)
    }

    @Test
    fun `json keeps numbers and booleans typed and NULL as null`() {
        val json = rowsToJson(listOf("id", "ok", "price", "x"), listOf(listOf(1, true, 9.5, null)))
        assertEquals("[\n  { \"id\": 1, \"ok\": true, \"price\": 9.5, \"x\": null }\n]", json)
    }

    @Test
    fun `json escapes quotes, backslashes, newlines and control characters`() {
        val json = rowsToJson(listOf("t"), listOf(listOf("a\"b\\c\nd\te\u0001")))
        assertEquals("[\n  { \"t\": \"a\\\"b\\\\c\\nd\\te\\u0001\" }\n]", json)
    }

    @Test
    fun `json of an empty result is an empty array`() {
        assertEquals("[]", rowsToJson(columns, emptyList()))
    }

    @Test
    fun `json writes non-finite doubles as strings so the output stays valid`() {
        val json = rowsToJson(listOf("v"), listOf(listOf(Double.NaN)))
        assertEquals("[\n  { \"v\": \"NaN\" }\n]", json)
    }
}
