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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Line icons for actions, drawn on the same 16-unit grid and 1.5 stroke as [WorkspaceIcons].
 *
 * Pass [contentDescription] whenever the icon is the only thing a button shows, so screen readers
 * announce what it does. A parent `clickable` / `IconButton` merges it into its own node. Leave it
 * null when a text label sits next to the icon.
 */
private fun lineStroke(w: Float) = Stroke(width = w, cap = StrokeCap.Round, join = StrokeJoin.Round)

private fun Modifier.describe(text: String?): Modifier =
    if (text == null) this else this.semantics { contentDescription = text }

@Composable
fun SearchIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        drawCircle(color, radius = 4.75f * u, center = Offset(6.75f * u, 6.75f * u), style = st)
        drawLine(color, Offset(10.4f * u, 10.4f * u), Offset(14f * u, 14f * u), strokeWidth = 1.5f * u, cap = StrokeCap.Round)
    }
}

@Composable
fun RefreshIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        // Three-quarter circle plus an arrow head at its open end.
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 290f,
            useCenter = false,
            topLeft = Offset(2.5f * u, 2.5f * u),
            size = Size(11f * u, 11f * u),
            style = st,
        )
        val head = Path().apply { moveTo(9.5f * u, 1.5f * u); lineTo(11.8f * u, 3.6f * u); lineTo(9f * u, 5f * u) }
        drawPath(head, color, style = st)
    }
}

/** Password visibility: an eye, crossed out when [slashed]. */
@Composable
fun EyeIcon(color: Color, slashed: Boolean, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        val eye = Path().apply {
            moveTo(1.5f * u, 8f * u)
            cubicTo(4f * u, 3.5f * u, 12f * u, 3.5f * u, 14.5f * u, 8f * u)
            cubicTo(12f * u, 12.5f * u, 4f * u, 12.5f * u, 1.5f * u, 8f * u)
            close()
        }
        drawPath(eye, color, style = st)
        drawCircle(color, radius = 2f * u, center = Offset(8f * u, 8f * u), style = st)
        if (slashed) drawLine(color, Offset(3f * u, 13f * u), Offset(13f * u, 3f * u), strokeWidth = 1.5f * u, cap = StrokeCap.Round)
    }
}

@Composable
fun CopyIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        drawRoundRect(color, Offset(5.5f * u, 5.5f * u), Size(8f * u, 8f * u), CornerRadius(1.5f * u), st)
        val back = Path().apply { moveTo(3.5f * u, 10.5f * u); lineTo(3.5f * u, 3.5f * u); lineTo(10.5f * u, 3.5f * u) }
        drawPath(back, color, style = st)
    }
}

@Composable
fun EditIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        val pencil = Path().apply {
            moveTo(2.5f * u, 13.5f * u); lineTo(3.2f * u, 10.2f * u); lineTo(10.8f * u, 2.6f * u)
            lineTo(13.4f * u, 5.2f * u); lineTo(5.8f * u, 12.8f * u); close()
        }
        drawPath(pencil, color, style = st)
    }
}

@Composable
fun TrashIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        drawLine(color, Offset(2.5f * u, 4f * u), Offset(13.5f * u, 4f * u), strokeWidth = 1.5f * u, cap = StrokeCap.Round)
        val bin = Path().apply {
            moveTo(4f * u, 4f * u); lineTo(4.8f * u, 13.5f * u); lineTo(11.2f * u, 13.5f * u); lineTo(12f * u, 4f * u)
            moveTo(6.2f * u, 4f * u); lineTo(6.2f * u, 2.5f * u); lineTo(9.8f * u, 2.5f * u); lineTo(9.8f * u, 4f * u)
        }
        drawPath(bin, color, style = st)
    }
}

@Composable
fun KeyIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        drawCircle(color, radius = 3f * u, center = Offset(5.2f * u, 10.8f * u), style = st)
        val shaft = Path().apply {
            moveTo(7.4f * u, 8.6f * u); lineTo(13.5f * u, 2.5f * u)
            moveTo(11.2f * u, 4.8f * u); lineTo(13f * u, 6.6f * u)
        }
        drawPath(shaft, color, style = st)
    }
}

@Composable
fun LinkIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        drawRoundRect(color, Offset(1.5f * u, 5.5f * u), Size(7f * u, 5f * u), CornerRadius(2.5f * u), st)
        drawRoundRect(color, Offset(7.5f * u, 5.5f * u), Size(7f * u, 5f * u), CornerRadius(2.5f * u), st)
    }
}

@Composable
fun LockIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        drawRoundRect(color, Offset(3f * u, 7f * u), Size(10f * u, 7f * u), CornerRadius(1.8f * u), st)
        val shackle = Path().apply {
            moveTo(5.3f * u, 7f * u); lineTo(5.3f * u, 5f * u)
            cubicTo(5.3f * u, 1.6f * u, 10.7f * u, 1.6f * u, 10.7f * u, 5f * u); lineTo(10.7f * u, 7f * u)
        }
        drawPath(shackle, color, style = st)
    }
}

@Composable
fun UserIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        drawCircle(color, radius = 2.8f * u, center = Offset(8f * u, 5.2f * u), style = st)
        val shoulders = Path().apply {
            moveTo(2.5f * u, 14f * u)
            cubicTo(2.5f * u, 9.5f * u, 13.5f * u, 9.5f * u, 13.5f * u, 14f * u)
        }
        drawPath(shoulders, color, style = st)
    }
}

@Composable
fun PlusIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        drawLine(color, Offset(8f * u, 3f * u), Offset(8f * u, 13f * u), strokeWidth = 1.5f * u, cap = StrokeCap.Round)
        drawLine(color, Offset(3f * u, 8f * u), Offset(13f * u, 8f * u), strokeWidth = 1.5f * u, cap = StrokeCap.Round)
    }
}

@Composable
fun DocumentIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        val page = Path().apply {
            moveTo(3.5f * u, 1.8f * u); lineTo(9.5f * u, 1.8f * u); lineTo(12.5f * u, 4.8f * u)
            lineTo(12.5f * u, 14.2f * u); lineTo(3.5f * u, 14.2f * u); close()
            moveTo(9.5f * u, 1.8f * u); lineTo(9.5f * u, 4.8f * u); lineTo(12.5f * u, 4.8f * u)
        }
        drawPath(page, color, style = st)
    }
}

/** A table / grid: outer frame, one header rule, one column rule. */
@Composable
fun TableIcon(color: Color, size: Dp = 14.dp, contentDescription: String? = null, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size).describe(contentDescription)) {
        val u = this.size.width / 16f
        val st = lineStroke(1.5f * u)
        drawRoundRect(color, Offset(1.75f * u, 2.75f * u), Size(12.5f * u, 10.5f * u), CornerRadius(1.5f * u), st)
        drawLine(color, Offset(1.75f * u, 6.5f * u), Offset(14.25f * u, 6.5f * u), strokeWidth = 1.5f * u)
        drawLine(color, Offset(6f * u, 6.5f * u), Offset(6f * u, 13.25f * u), strokeWidth = 1.5f * u)
    }
}
