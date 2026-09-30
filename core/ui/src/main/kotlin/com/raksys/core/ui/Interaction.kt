package com.raksys.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Pointer and keyboard feedback for custom clickable rows, tabs, and cards: a soft highlight while
 * the pointer is over the element and a visible ring while the `.clickable` that follows it has
 * keyboard focus.
 *
 * Place it directly *before* `.clickable { }` in the modifier chain: hover is tracked here, and
 * focus is read from the clickable's own focus target, so there is still exactly one tab stop and
 * Enter / Space keep activating the element.
 *
 * Skip the hover highlight on elements that already paint a selected state with [enabled] = false.
 */
@Composable
fun Modifier.raksysInteractive(
    shape: Shape = RoundedCornerShape(6.dp),
    enabled: Boolean = true,
    hoverColor: Color = RaksysThemeColors.SurfaceHover.copy(alpha = 0.55f),
): Modifier {
    val hoverSource = remember { MutableInteractionSource() }
    val hovered by hoverSource.collectIsHoveredAsState()
    var focused by remember { mutableStateOf(false) }
    return this
        .hoverable(hoverSource)
        .background(if (hovered && enabled) hoverColor else Color.Transparent, shape)
        .border(if (focused) 2.dp else 0.dp, if (focused) RaksysThemeColors.FocusRing else Color.Transparent, shape)
        .onFocusChanged { focused = it.hasFocus }
}
