package com.raksys.feature.grid

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * Pure formatters for exporting a result set, kept free of UI so they can be unit-tested.
 */

/** RFC 4180 style CSV: every cell quoted, quotes doubled, SQL NULL written as an empty cell. */
internal fun rowsToCsv(columns: List<String>, rows: List<List<Any?>>): String = buildString {
    appendLine(columns.joinToString(",") { csvCell(it) })
    rows.forEach { row ->
        appendLine(columns.indices.joinToString(",") { i -> row.getOrNull(i)?.let { csvCell(it.toString()) } ?: "" })
    }
}

private fun csvCell(value: String) = "\"" + value.replace("\"", "\"\"") + "\""

/**
 * JSON array of objects. Numbers and booleans stay JSON numbers/booleans, NULL becomes `null`, and
 * every string is escaped (quotes, backslashes, newlines, control characters).
 */
internal fun rowsToJson(columns: List<String>, rows: List<List<Any?>>): String {
    if (rows.isEmpty()) return "[]"
    return rows.joinToString(prefix = "[\n", separator = ",\n", postfix = "\n]") { row ->
        columns.indices.joinToString(prefix = "  { ", separator = ", ", postfix = " }") { i ->
            "${jsonString(columns[i])}: ${jsonValue(row.getOrNull(i))}"
        }
    }
}

private fun jsonValue(value: Any?): String = when (value) {
    null -> "null"
    is Boolean -> value.toString()
    is Int, is Long, is Short, is Byte -> value.toString()
    is Double -> if (value.isFinite()) value.toString() else jsonString(value.toString())
    is Float -> if (value.isFinite()) value.toString() else jsonString(value.toString())
    is java.math.BigDecimal, is java.math.BigInteger -> value.toString()
    else -> jsonString(value.toString())
}

private fun jsonString(value: String): String = buildString(value.length + 2) {
    append('"')
    for (ch in value) {
        when {
            ch == '"' -> append("\\\"")
            ch == '\\' -> append("\\\\")
            ch == '\n' -> append("\\n")
            ch == '\r' -> append("\\r")
            ch == '\t' -> append("\\t")
            ch < ' ' -> append("\\u%04x".format(ch.code))
            else -> append(ch)
        }
    }
    append('"')
}

/** Native "Save as" dialog. Returns null when the user cancels. */
internal fun chooseSaveFile(suggestedName: String): File? {
    val dialog = FileDialog(null as Frame?, "Simpan hasil kueri", FileDialog.SAVE)
    dialog.file = suggestedName
    dialog.isVisible = true
    val name = dialog.file ?: return null
    return File(dialog.directory ?: "", name)
}

/** UTF-8 with a BOM for CSV so Excel opens non-ASCII text correctly; plain UTF-8 otherwise. */
internal fun writeExport(file: File, content: String, withBom: Boolean) {
    val bytes = content.toByteArray(Charsets.UTF_8)
    file.outputStream().use { out ->
        if (withBom) out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
        out.write(bytes)
    }
}
