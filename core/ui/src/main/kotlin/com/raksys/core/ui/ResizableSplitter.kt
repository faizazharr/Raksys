package com.raksys.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.awt.Cursor

@Composable
fun ResizableVerticalDivider(
    onDragDelta: (Float) -> Unit,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp,
    color: Color = RaksysThemeColors.Border,
    hoverColor: Color = RaksysThemeColors.SplitterHover,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .width(thickness)
            .fillMaxHeight()
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)))
            .hoverable(interactionSource)
            .background(if (isHovered) hoverColor else color)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.x)
                }
            }
    )
}

@Composable
fun ResizableHorizontalDivider(
    onDragDelta: (Float) -> Unit,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp,
    color: Color = RaksysThemeColors.Border,
    hoverColor: Color = RaksysThemeColors.SplitterHover,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .height(thickness)
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)))
            .hoverable(interactionSource)
            .background(if (isHovered) hoverColor else color)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.y)
                }
            }
    )
}
