package com.raksys.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DatabaseIcon(
    color: Color = RaksysThemeColors.Primary,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.09f)

        // 3 horizontal database disks / layers
        val diskH = h * 0.22f
        val radius = CornerRadius(w * 0.5f, diskH * 0.45f)

        // Top disk
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.1f, h * 0.1f),
            size = Size(w * 0.8f, diskH),
            cornerRadius = radius,
            style = stroke
        )

        // Middle disk arc
        drawRoundRect(
            color = color.copy(alpha = 0.85f),
            topLeft = Offset(w * 0.1f, h * 0.38f),
            size = Size(w * 0.8f, diskH),
            cornerRadius = radius,
            style = stroke
        )

        // Bottom disk arc
        drawRoundRect(
            color = color.copy(alpha = 0.7f),
            topLeft = Offset(w * 0.1f, h * 0.66f),
            size = Size(w * 0.8f, diskH),
            cornerRadius = radius,
            style = stroke
        )
    }
}

@Composable
fun LightningIcon(
    color: Color = RaksysThemeColors.Primary,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val path = Path().apply {
            moveTo(w * 0.55f, h * 0.08f)
            lineTo(w * 0.22f, h * 0.52f)
            lineTo(w * 0.48f, h * 0.52f)
            lineTo(w * 0.40f, h * 0.92f)
            lineTo(w * 0.78f, h * 0.44f)
            lineTo(w * 0.52f, h * 0.44f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun TableGridIcon(
    color: Color = RaksysThemeColors.Primary,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.08f)

        // Outer box
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.12f, h * 0.12f),
            size = Size(w * 0.76f, h * 0.76f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = stroke
        )

        // Horizontal line
        drawLine(
            color = color,
            start = Offset(w * 0.12f, h * 0.42f),
            end = Offset(w * 0.88f, h * 0.42f),
            strokeWidth = w * 0.08f
        )

        // Vertical line
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.42f),
            end = Offset(w * 0.50f, h * 0.88f),
            strokeWidth = w * 0.08f
        )
    }
}

@Composable
fun PlugIcon(
    color: Color = RaksysThemeColors.Primary,
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.08f

        // Two prongs
        drawLine(
            color = color,
            start = Offset(w * 0.35f, h * 0.10f),
            end = Offset(w * 0.35f, h * 0.28f),
            strokeWidth = strokeW
        )
        drawLine(
            color = color,
            start = Offset(w * 0.65f, h * 0.10f),
            end = Offset(w * 0.65f, h * 0.28f),
            strokeWidth = strokeW
        )

        // Plug body
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.22f, h * 0.28f),
            size = Size(w * 0.56f, h * 0.36f),
            cornerRadius = CornerRadius(w * 0.1f, w * 0.1f),
            style = Stroke(width = strokeW)
        )

        // Cord
        val cordPath = Path().apply {
            moveTo(w * 0.5f, h * 0.64f)
            lineTo(w * 0.5f, h * 0.90f)
        }
        drawPath(path = cordPath, color = color, style = Stroke(width = strokeW))
    }
}
