package com.raksys.core.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * App-level commands that the menu bar can trigger in a feature screen without the app module
 * depending on that feature's internals. A screen observes [runQueryTick] and runs when it changes.
 */
object RaksysCommands {
    var runQueryTick by mutableStateOf(0)
        private set

    fun requestRunQuery() {
        runQueryTick++
    }
}
