package com.raksys.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Tracks whether a Compose dialog is open.
 *
 * The SQL editor is a Swing component, and Swing components are drawn on top of Compose content, so
 * a dialog opened over the editor would be partly covered by it. While [isOpen] is true the editor
 * hides itself. Every dialog calls [ModalGuard] once, just before it composes its `Dialog` /
 * `AlertDialog`, so the count follows the dialog's lifetime.
 */
object RaksysModals {
    var openCount by mutableStateOf(0)
        internal set

    val isOpen: Boolean get() = openCount > 0
}

@Composable
fun ModalGuard() {
    DisposableEffect(Unit) {
        RaksysModals.openCount++
        onDispose { RaksysModals.openCount-- }
    }
}
