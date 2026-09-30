package com.raksys.feature.query

import kotlin.test.Test
import kotlin.test.assertEquals

class SqlTabsTest {

    @Test
    fun `starts with one tab holding the initial text`() {
        val tabs = SqlTabs.initial("SELECT 1")
        assertEquals(listOf("Query 1"), tabs.tabs.map { it.title })
        assertEquals("SELECT 1", tabs.active.sql)
    }

    @Test
    fun `adding a tab activates it and switching back keeps each tab's text`() {
        var tabs = SqlTabs.initial("SELECT 1")
        tabs = tabs.withActiveText("SELECT 1 -- edited").add("SELECT 2")
        assertEquals("Query 2", tabs.active.title)
        assertEquals("SELECT 2", tabs.active.sql)

        tabs = tabs.withActiveText("SELECT 2 -- edited").select(1)
        assertEquals("SELECT 1 -- edited", tabs.active.sql)
        assertEquals("SELECT 2 -- edited", tabs.tabs.first { it.id == 2 }.sql)
    }

    @Test
    fun `the last remaining tab cannot be closed`() {
        val tabs = SqlTabs.initial("x")
        assertEquals(tabs.tabs, tabs.close(1).tabs)
    }

    @Test
    fun `closing the active tab activates the tab before it, or the next one when it was first`() {
        var tabs = SqlTabs.initial("a").add("b").add("c")   // 1, 2, 3 — 3 active
        tabs = tabs.select(2).close(2)
        assertEquals(1, tabs.activeId)

        tabs = SqlTabs.initial("a").add("b").select(1).close(1)
        assertEquals(2, tabs.activeId)
    }

    @Test
    fun `closing an inactive tab keeps the active one`() {
        val tabs = SqlTabs.initial("a").add("b").add("c").close(1)
        assertEquals(3, tabs.activeId)
        assertEquals(listOf(2, 3), tabs.tabs.map { it.id })
    }

    @Test
    fun `titles keep counting up after a close`() {
        val tabs = SqlTabs.initial("a").add("b").close(2).add("c")
        assertEquals(listOf("Query 1", "Query 3"), tabs.tabs.map { it.title })
    }
}
