package com.raksys.core.model

data class ColumnDefinition(
    val name: String,
    val type: String,
    val nullable: Boolean,
    val isPrimaryKey: Boolean,
    val defaultValue: String? = null,
)

data class TableSchema(
    val name: String,
    val columns: List<ColumnDefinition>,
)

data class QueryResult(
    val columns: List<String>,
    val rows: List<List<Any?>>,
    val executionTimeMs: Long,
)

data class QueryError(
    val message: String,
    val cause: Throwable? = null,
)
