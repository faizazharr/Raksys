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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * One family of monochrome line icons for the workspace chrome. They share a stroke weight
 * (≈ 1.5 px at 16 dp), round caps, and a 16-unit grid, and take their color from the caller so
 * they follow the same contrast rules as adjacent text. Replaces emoji, which render differently
 * on every OS.
 */
private fun stroke(w: Float) = Stroke(width = w, cap = StrokeCap.Round, join = StrokeJoin.Round)

@Composable
fun QueryIcon(color: Color, size: Dp = 14.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val u = this.size.width / 16f
        val st = stroke(1.5f * u)
        val chevron = Path().apply {
            moveTo(3f * u, 4.5f * u); lineTo(7f * u, 8f * u); lineTo(3f * u, 11.5f * u)
        }
        drawPath(chevron, color, style = st)
        drawLine(color, Offset(8.5f * u, 12f * u), Offset(13f * u, 12f * u), strokeWidth = 1.5f * u, cap = StrokeCap.Round)
    }
}

@Composable
fun ErdIcon(color: Color, size: Dp = 14.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val u = this.size.width / 16f
        val st = stroke(1.5f * u)
        val r = CornerRadius(1.5f * u)
        drawRoundRect(color, Offset(1.5f * u, 1.5f * u), Size(5f * u, 4f * u), r, st)
        drawRoundRect(color, Offset(9.5f * u, 10.5f * u), Size(5f * u, 4f * u), r, st)
        drawRoundRect(color, Offset(9.5f * u, 1.5f * u), Size(5f * u, 4f * u), r, st)
        val link = Path().apply {
            moveTo(6.5f * u, 3.5f * u); lineTo(9.5f * u, 3.5f * u)
            moveTo(4f * u, 5.5f * u); lineTo(4f * u, 12.5f * u); lineTo(9.5f * u, 12.5f * u)
        }
        drawPath(link, color, style = st)
    }
}

@Composable
fun ShieldIcon(color: Color, size: Dp = 14.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val u = this.size.width / 16f
        val st = stroke(1.5f * u)
        val shield = Path().apply {
            moveTo(8f * u, 1.5f * u)
            lineTo(13.5f * u, 3.5f * u)
            lineTo(13.5f * u, 8f * u)
            cubicTo(13.5f * u, 11.2f * u, 11f * u, 13.6f * u, 8f * u, 14.5f * u)
            cubicTo(5f * u, 13.6f * u, 2.5f * u, 11.2f * u, 2.5f * u, 8f * u)
            lineTo(2.5f * u, 3.5f * u)
            close()
        }
        drawPath(shield, color, style = st)
        val tick = Path().apply { moveTo(5.5f * u, 8f * u); lineTo(7.4f * u, 9.9f * u); lineTo(10.6f * u, 6.3f * u) }
        drawPath(tick, color, style = st)
    }
}

/** Sidebar toggle: a window outline with the leading pane filled when [sidebarOpen]. */
@Composable
fun SidebarIcon(color: Color, sidebarOpen: Boolean, size: Dp = 14.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val u = this.size.width / 16f
        val st = stroke(1.5f * u)
        drawRoundRect(color, Offset(1.5f * u, 2.5f * u), Size(13f * u, 11f * u), CornerRadius(2f * u), st)
        if (sidebarOpen) {
            drawRect(color.copy(alpha = 0.35f), Offset(2.25f * u, 3.25f * u), Size(3.75f * u, 9.5f * u))
        }
        drawLine(color, Offset(6.5f * u, 3f * u), Offset(6.5f * u, 13f * u), strokeWidth = 1.5f * u, cap = StrokeCap.Round)
    }
}
