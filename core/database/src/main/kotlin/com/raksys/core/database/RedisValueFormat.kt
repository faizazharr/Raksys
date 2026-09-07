package com.raksys.core.database

private fun jsonQuote(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

internal fun formatRedisHash(map: Map<String, String>): String =
    map.entries.joinToString(prefix = "{", postfix = "}") { (k, v) -> "${jsonQuote(k)}: ${jsonQuote(v)}" }

internal fun formatRedisList(items: List<String>): String =
    items.joinToString(prefix = "[", postfix = "]") { jsonQuote(it) }

internal fun formatRedisSet(items: Set<String>): String =
    items.joinToString(prefix = "[", postfix = "]") { jsonQuote(it) }
