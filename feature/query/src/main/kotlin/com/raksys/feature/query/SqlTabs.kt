package com.raksys.feature.query

data class SqlTab(val id: Int, val title: String, val sql: String)

/**
 * The SQL console's tab strip as an immutable value, so the switching rules can be unit-tested.
 *
 * Only the text of each tab is kept per tab. The result grid always shows the most recent run.
 * Always call [withActiveText] with the editor's current text before [select]ing another tab.
 */
class SqlTabs private constructor(
    val tabs: List<SqlTab>,
    val activeId: Int,
    private val nextId: Int,
) {
    val active: SqlTab get() = tabs.first { it.id == activeId }

    fun withActiveText(sql: String): SqlTabs =
        SqlTabs(tabs.map { if (it.id == activeId) it.copy(sql = sql) else it }, activeId, nextId)

    fun select(id: Int): SqlTabs = if (tabs.any { it.id == id }) SqlTabs(tabs, id, nextId) else this

    /** Appends a tab and makes it active. Titles count up and are never reused after a close. */
    fun add(sql: String = ""): SqlTabs =
        SqlTabs(tabs + SqlTab(nextId, "Query $nextId", sql), nextId, nextId + 1)

    /** Closes a tab; the last remaining tab cannot be closed. Closing the active tab activates its neighbour. */
    fun close(id: Int): SqlTabs {
        if (tabs.size <= 1) return this
        val index = tabs.indexOfFirst { it.id == id }
        if (index < 0) return this
        val remaining = tabs.filterNot { it.id == id }
        val newActive = if (id != activeId) activeId else remaining[(index - 1).coerceAtLeast(0)].id
        return SqlTabs(remaining, newActive, nextId)
    }

    companion object {
        fun initial(sql: String): SqlTabs = SqlTabs(listOf(SqlTab(1, "Query 1", sql)), 1, 2)
    }
}
