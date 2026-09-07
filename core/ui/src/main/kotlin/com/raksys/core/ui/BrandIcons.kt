package com.raksys.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Authentic PostgreSQL Elephant Mark (Slonik).
 * Faithful vector rendering of the PostgreSQL mascot with head contour, trunk, ear, and tusk.
 */
@Composable
fun PostgresLogo(color: Color = RaksysThemeColors.PostgresColor, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Large Ear (behind head)
        val ear = Path().apply {
            moveTo(w * 0.52f, h * 0.22f)
            cubicTo(w * 0.72f, h * 0.16f, w * 0.88f, h * 0.32f, w * 0.86f, h * 0.54f)
            cubicTo(w * 0.84f, h * 0.72f, w * 0.68f, h * 0.78f, w * 0.52f, h * 0.68f)
            close()
        }
        drawPath(path = ear, color = color.copy(alpha = 0.55f))

        // Head & Body Dome
        val head = Path().apply {
            moveTo(w * 0.30f, h * 0.22f)
            cubicTo(w * 0.44f, h * 0.12f, w * 0.64f, h * 0.18f, w * 0.66f, h * 0.38f)
            cubicTo(w * 0.68f, h * 0.54f, w * 0.58f, h * 0.68f, w * 0.42f, h * 0.68f)
            cubicTo(w * 0.36f, h * 0.68f, w * 0.28f, h * 0.60f, w * 0.24f, h * 0.46f)
            cubicTo(w * 0.22f, h * 0.34f, w * 0.24f, h * 0.26f, w * 0.30f, h * 0.22f)
            close()
        }
        drawPath(path = head, color = color)

        // Iconic Curled Trunk
        val trunk = Path().apply {
            moveTo(w * 0.26f, h * 0.46f)
            cubicTo(w * 0.16f, h * 0.58f, w * 0.18f, h * 0.80f, w * 0.32f, h * 0.88f)
            cubicTo(w * 0.42f, h * 0.92f, w * 0.48f, h * 0.84f, w * 0.44f, h * 0.76f)
            cubicTo(w * 0.40f, h * 0.72f, w * 0.34f, h * 0.74f, w * 0.30f, h * 0.78f)
            cubicTo(w * 0.26f, h * 0.74f, w * 0.26f, h * 0.62f, w * 0.34f, h * 0.54f)
            close()
        }
        drawPath(path = trunk, color = color)

        // White Tusk
        val tusk = Path().apply {
            moveTo(w * 0.34f, h * 0.60f)
            cubicTo(w * 0.44f, h * 0.64f, w * 0.50f, h * 0.74f, w * 0.46f, h * 0.82f)
            cubicTo(w * 0.40f, h * 0.78f, w * 0.36f, h * 0.70f, w * 0.32f, h * 0.64f)
            close()
        }
        drawPath(path = tusk, color = Color.White.copy(alpha = 0.9f))

        // Eye Dot
        drawCircle(color = Color.White, radius = w * 0.045f, center = Offset(w * 0.38f, h * 0.36f))
        drawCircle(color = RaksysThemeColors.Background, radius = w * 0.022f, center = Offset(w * 0.37f, h * 0.36f))
    }
}

/**
 * Authentic MySQL Dolphin Mark (Sakila).
 * Faithful vector rendering of the jumping dolphin with dorsal fin, flipper, and tail flukes.
 */
@Composable
fun MysqlLogo(color: Color = RaksysThemeColors.MysqlColor, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Leaping Dolphin Body
        val body = Path().apply {
            moveTo(w * 0.08f, h * 0.68f)
            // Snout and forehead arch
            cubicTo(w * 0.12f, h * 0.44f, w * 0.26f, h * 0.18f, w * 0.52f, h * 0.14f)
            // Dorsal fin
            cubicTo(w * 0.56f, h * 0.12f, w * 0.58f, h * 0.20f, w * 0.62f, h * 0.24f)
            cubicTo(w * 0.68f, h * 0.18f, w * 0.72f, h * 0.20f, w * 0.74f, h * 0.26f)
            // Back towards tail flukes
            cubicTo(w * 0.84f, h * 0.34f, w * 0.92f, h * 0.46f, w * 0.94f, h * 0.56f)
            // Tail fluke bottom
            cubicTo(w * 0.88f, h * 0.52f, w * 0.82f, h * 0.54f, w * 0.78f, h * 0.58f)
            // Underside belly arch
            cubicTo(w * 0.66f, h * 0.48f, w * 0.48f, h * 0.50f, w * 0.38f, h * 0.64f)
            // Pectoral flipper
            cubicTo(w * 0.32f, h * 0.72f, w * 0.24f, h * 0.80f, w * 0.18f, h * 0.82f)
            cubicTo(w * 0.20f, h * 0.74f, w * 0.24f, h * 0.68f, w * 0.26f, h * 0.64f)
            // Chin / lower jaw
            cubicTo(w * 0.18f, h * 0.70f, w * 0.12f, h * 0.74f, w * 0.08f, h * 0.68f)
            close()
        }
        drawPath(path = body, color = color)

        // Eye
        drawCircle(color = Color.White, radius = w * 0.035f, center = Offset(w * 0.22f, h * 0.48f))
        drawCircle(color = RaksysThemeColors.Background, radius = w * 0.018f, center = Offset(w * 0.22f, h * 0.48f))
    }
}

/**
 * Authentic SQLite Feather / Quill Mark.
 * Faithful angled feather silhouette with quill shaft line.
 */
@Composable
fun SqliteLogo(color: Color = RaksysThemeColors.SqliteColor, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Feather body vane
        val feather = Path().apply {
            moveTo(w * 0.84f, h * 0.10f)
            cubicTo(w * 0.44f, h * 0.14f, w * 0.14f, h * 0.46f, w * 0.18f, h * 0.78f)
            cubicTo(w * 0.24f, h * 0.88f, w * 0.34f, h * 0.90f, w * 0.42f, h * 0.84f)
            cubicTo(w * 0.68f, h * 0.64f, w * 0.86f, h * 0.38f, w * 0.84f, h * 0.10f)
            close()
        }
        drawPath(path = feather, color = color)

        // Feather vanes detail (subtle highlight)
        val shaft = Path().apply {
            moveTo(w * 0.80f, h * 0.14f)
            cubicTo(w * 0.54f, h * 0.42f, w * 0.36f, h * 0.68f, w * 0.22f, h * 0.90f)
        }
        drawPath(path = shaft, color = Color.White.copy(alpha = 0.85f), style = Stroke(width = w * 0.06f))
    }
}

/**
 * Authentic MongoDB Leaf / Droplet Mark.
 * Characteristic pointed top, dual-tone bilateral leaf shape with central spine.
 */
@Composable
fun MongodbLogo(color: Color = RaksysThemeColors.MongodbColor, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Right side of leaf (darker / base tone)
        val rightLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.06f)
            cubicTo(w * 0.76f, h * 0.26f, w * 0.82f, h * 0.60f, w * 0.52f, h * 0.94f)
            lineTo(w * 0.50f, h * 0.94f)
            close()
        }
        drawPath(path = rightLeaf, color = color.copy(alpha = 0.75f))

        // Left side of leaf (brighter highlight tone)
        val leftLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.06f)
            cubicTo(w * 0.24f, h * 0.26f, w * 0.18f, h * 0.60f, w * 0.48f, h * 0.94f)
            lineTo(w * 0.50f, h * 0.94f)
            close()
        }
        drawPath(path = leftLeaf, color = color)

        // Center spine vein
        val spine = Path().apply {
            moveTo(w * 0.50f, h * 0.16f)
            cubicTo(w * 0.48f, h * 0.46f, w * 0.50f, h * 0.74f, w * 0.50f, h * 0.90f)
        }
        drawPath(path = spine, color = RaksysThemeColors.Background, style = Stroke(width = w * 0.05f))
    }
}

/**
 * Authentic Redis Stacked Isometric Layers Mark.
 * Three isometric diamond plates stacked vertically in 3D perspective.
 */
@Composable
fun RedisLogo(color: Color = RaksysThemeColors.RedisColor, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        fun layer(cy: Float, alpha: Float): Path = Path().apply {
            moveTo(w * 0.50f, cy - h * 0.09f)
            lineTo(w * 0.88f, cy)
            lineTo(w * 0.50f, cy + h * 0.09f)
            lineTo(w * 0.12f, cy)
            close()
        }

        // Bottom plate
        drawPath(path = layer(h * 0.72f, 0.5f), color = color.copy(alpha = 0.45f))
        // Middle plate
        drawPath(path = layer(h * 0.52f, 0.75f), color = color.copy(alpha = 0.75f))
        // Top plate
        drawPath(path = layer(h * 0.32f, 1f), color = color)

        // Top layer facet highlight
        val topHighlight = Path().apply {
            moveTo(w * 0.50f, h * 0.23f)
            lineTo(w * 0.88f, h * 0.32f)
            lineTo(w * 0.50f, h * 0.35f)
            close()
        }
        drawPath(path = topHighlight, color = Color.White.copy(alpha = 0.30f))
    }
}

/** Dispatches to the matching authentic brand mark for a [com.raksys.core.model.DbType] name. */
@Composable
fun DbTypeLogo(typeName: String, color: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    when (typeName) {
        "POSTGRES" -> PostgresLogo(color, size, modifier)
        "MYSQL" -> MysqlLogo(color, size, modifier)
        "SQLITE" -> SqliteLogo(color, size, modifier)
        "MONGODB" -> MongodbLogo(color, size, modifier)
        "REDIS" -> RedisLogo(color, size, modifier)
        else -> DatabaseIcon(color, size, modifier)
    }
}

