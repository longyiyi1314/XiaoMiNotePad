package com.xiaominote.app.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Renders a [Stroke] onto a [DrawScope] using behaviour appropriate to its
 * [PenType]. Highlighters should be drawn first (behind ink) by the canvas —
 * the caller is responsible for ordering.
 */
fun DrawScope.drawStroke(stroke: Stroke) {
    val pts = stroke.points
    if (pts.size < 2) {
        // Draw a dot for a single-point tap
        if (pts.size == 1) {
            val p = pts[0]
            val r = effectiveWidth(stroke.config, 1f) / 2f
            drawCircle(
                color = paintColor(stroke.config),
                radius = r,
                center = Offset(p.x, p.y),
            )
        }
        return
    }

    when (stroke.config.type) {
        PenType.HIGHLIGHTER -> drawVariableWidth(stroke, alpha = 0.28f, cap = StrokeCap.Square)
        PenType.MARKER -> drawVariableWidth(stroke, alpha = 0.55f, cap = StrokeCap.Square)
        PenType.BRUSH -> drawVariableWidth(stroke, alpha = stroke.config.opacity, cap = StrokeCap.Round, pressureScale = 1.6f)
        PenType.FOUNTAIN -> drawVariableWidth(stroke, alpha = stroke.config.opacity, cap = StrokeCap.Round, pressureScale = 1.3f)
        PenType.PENCIL -> drawPencil(stroke)
        PenType.BALLPOINT -> drawVariableWidth(stroke, alpha = stroke.config.opacity, cap = StrokeCap.Round, pressureScale = 1.1f)
        PenType.ERASER -> { /* handled by stroke removal, not drawing */ }
    }
}

private fun DrawScope.drawVariableWidth(
    stroke: Stroke,
    alpha: Float,
    cap: StrokeCap,
    pressureScale: Float = 1f,
) {
    val pts = stroke.points
    val color = paintColor(stroke.config).copy(alpha = alpha.coerceIn(0f, 1f))
    val path = Path()
    path.moveTo(pts[0].x, pts[0].y)

    // Smooth using mid-point quadratic curves
    for (i in 1 until pts.size - 1) {
        val mid = midpoint(pts[i], pts[i + 1])
        path.quadraticBezierTo(pts[i].x, pts[i].y, mid.x, mid.y)
    }
    val last = pts.last()
    path.lineTo(last.x, last.y)

    val avgPressure = pts.map { it.pressure }.average().toFloat().coerceIn(0.05f, 1f)
    val width = effectiveWidth(stroke.config, avgPressure) * pressureScale

    drawPath(
        path = path,
        color = color,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = width,
            cap = cap,
            join = StrokeJoin.Round,
        )
    )
}

/** Pencil: thin stroke with a dashed noise effect to simulate texture. */
private fun DrawScope.drawPencil(stroke: Stroke) {
    val pts = stroke.points
    val color = paintColor(stroke.config).copy(alpha = 0.75f)
    val path = Path()
    path.moveTo(pts[0].x, pts[0].y)
    for (i in 1 until pts.size - 1) {
        val mid = midpoint(pts[i], pts[i + 1])
        path.quadraticBezierTo(pts[i].x, pts[i].y, mid.x, mid.y)
    }
    path.lineTo(pts.last().x, pts.last().y)

    val avgPressure = pts.map { it.pressure }.average().toFloat().coerceIn(0.05f, 1f)
    val width = max(1f, stroke.config.size * 0.6f * (0.5f + avgPressure * 0.5f))

    drawPath(
        path = path,
        color = color,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = width,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(width * 1.2f, width * 0.6f), 0f)
        )
    )
}

private fun paintColor(config: PenConfig): Color = Color(config.color)

private fun effectiveWidth(config: PenConfig, pressure: Float): Float {
    val base = config.size
    return when (config.type) {
        PenType.HIGHLIGHTER -> base * 4f
        PenType.MARKER -> base * 2.2f
        PenType.BRUSH -> base * (0.4f + pressure * 1.2f)
        PenType.FOUNTAIN -> base * (0.5f + pressure * 0.8f)
        PenType.PENCIL -> base * 0.7f
        PenType.BALLPOINT -> base * (0.7f + pressure * 0.3f)
        PenType.ERASER -> base * 3f
    }
}

private fun midpoint(a: Point, b: Point): Offset =
    Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f)

/**
 * Hit-test helper. Returns true if [point] lies within [tolerance] of any
 * segment of [stroke]. Used by the eraser to remove whole strokes.
 */
object StrokeRenderer {
    fun hits(stroke: Stroke, point: Offset, tolerance: Float): Boolean {
        val tol = tolerance + stroke.config.size
        val pts = stroke.points
        if (pts.isEmpty()) return false
        if (pts.size == 1) {
            return abs(pts[0].x - point.x) <= tol && abs(pts[0].y - point.y) <= tol
        }
        for (i in 0 until pts.size - 1) {
            if (distanceToSegment(point, pts[i], pts[i + 1]) <= tol) return true
        }
        return false
    }

    private fun distanceToSegment(p: Offset, a: Point, b: Point): Float {
        val ax = a.x; val ay = a.y
        val bx = b.x; val by = b.y
        val dx = bx - ax
        val dy = by - ay
        val lenSq = dx * dx + dy * dy
        if (lenSq == 0f) {
            val ddx = p.x - ax; val ddy = p.y - ay
            return sqrt(ddx * ddx + ddy * ddy)
        }
        var t = ((p.x - ax) * dx + (p.y - ay) * dy) / lenSq
        t = max(0f, min(1f, t))
        val projX = ax + t * dx
        val projY = ay + t * dy
        val ddx = p.x - projX; val ddy = p.y - projY
        return sqrt(ddx * ddx + ddy * ddy)
    }
}
