package com.xiaominote.app.drawing

import androidx.compose.runtime.Immutable

/**
 * A single point in a stroke. Coordinates are in canvas-space pixels.
 * [pressure] is 0f..1f (from stylus pressure or a default for finger).
 */
@Immutable
data class Point(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,
    /** milliseconds since stroke start, used for replay/smoothing */
    val t: Long = 0L,
)

/**
 * Type of pen/tool. Each has distinct rendering behaviour.
 */
enum class PenType {
    BALLPOINT,   // thin constant-width line
    FOUNTAIN,    // width varies with pressure & speed
    BRUSH,       // soft variable width, rounded caps
    MARKER,      // semi-transparent, flat
    HIGHLIGHTER, // very transparent, wide, behind ink
    PENCIL,      // textured, slightly noisy
    ERASER,      // removes strokes (whole or pixel)
}

/**
 * Configuration for the active tool.
 */
@Immutable
data class PenConfig(
    val type: PenType = PenType.BALLPOINT,
    val color: Long = 0xFF000000,
    val size: Float = 4f,        // base stroke width in px
    val opacity: Float = 1f,     // 0..1
)

/**
 * A single continuous stroke drawn on the canvas.
 */
@Immutable
data class Stroke(
    val id: String,
    val config: PenConfig,
    val points: List<Point>,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val isEmpty: Boolean get() = points.isEmpty()
    val isEraser: Boolean get() = config.type == PenType.ERASER
}
