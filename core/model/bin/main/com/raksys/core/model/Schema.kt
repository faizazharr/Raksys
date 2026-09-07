package com.raksys.core.model

data class ForeignKeyRef(
    val referencedTable: String,
    val referencedColumn: String,
)

data class ColumnDefinition(
    val name: String,
    val type: String,
    val nullable: Boolean,
    val isPrimaryKey: Boolean,
    val defaultValue: String? = null,
    val foreignKey: ForeignKeyRef? = null,
)

data class TableSchema(
    val name: String,
    val columns: List<ColumnDefinition>,
)

data class QueryResult(
    val columns: List<String>,
    val rows: List<List<Any?>>,
    val executionTimeMs: Long,
    val truncated: Boolean = false,
)

data class QueryError(
    val message: String,
    val cause: Throwable? = null,
)
