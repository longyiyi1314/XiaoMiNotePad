package com.xiaominote.app.drawing

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import java.util.UUID

/**
 * Mutable state holder for the drawing surface. Keeps the committed strokes,
 * the in-progress stroke, and an undo/redo stack.
 */
@Stable
class DrawingState(
    initial: List<Stroke> = emptyList(),
    initialPen: PenConfig = PenConfig(),
) {
    private val _strokes: SnapshotStateList<Stroke> = mutableStateListOf<Stroke>().apply { addAll(initial) }
    val strokes: List<Stroke> get() = _strokes

    private val _redoStack: SnapshotStateList<Stroke> = mutableStateListOf()
    val canUndo: Boolean get() = _strokes.isNotEmpty()
    val canRedo: Boolean get() = _redoStack.isNotEmpty()

    var penConfig by mutableStateOf(initialPen)
        private set

    var currentStroke by mutableStateOf<Stroke?>(null)
        private set

    fun updatePenConfig(config: PenConfig) {
        penConfig = config
    }

    fun beginStroke(point: Offset, pressure: Float, startTime: Long) {
        val p = Point(point.x, point.y, pressure, 0L)
        currentStroke = Stroke(
            id = UUID.randomUUID().toString(),
            config = penConfig,
            points = listOf(p),
            createdAt = startTime,
        )
        _redoStack.clear()
    }

    fun appendPoint(point: Offset, pressure: Float, startTime: Long) {
        val stroke = currentStroke ?: return
        val t = System.currentTimeMillis() - startTime
        currentStroke = stroke.copy(points = stroke.points + Point(point.x, point.y, pressure, t))
    }

    fun endStroke() {
        val stroke = currentStroke ?: return
        currentStroke = null
        if (stroke.isEmpty) return
        if (stroke.isEraser) {
            // Whole-stroke eraser: remove strokes hit by the eraser path
            eraseWith(stroke)
        } else {
            _strokes.add(stroke)
        }
    }

    private fun eraseWith(eraser: Stroke) {
        val tolerance = eraser.config.size * 1.5f
        val toRemove = _strokes.filter { stroke ->
            eraser.points.any { p -> StrokeRenderer.hits(stroke, Offset(p.x, p.y), tolerance) }
        }
        _strokes.removeAll(toRemove.toSet())
    }

    fun undo() {
        if (_strokes.isEmpty()) return
        _redoStack.add(_strokes.removeAt(_strokes.lastIndex))
    }

    fun redo() {
        if (_redoStack.isEmpty()) return
        _strokes.add(_redoStack.removeAt(_redoStack.lastIndex))
    }

    fun clearAll() {
        _strokes.clear()
        _redoStack.clear()
        currentStroke = null
    }

    fun replaceAll(strokes: List<Stroke>) {
        _strokes.clear()
        _redoStack.clear()
        _strokes.addAll(strokes)
        currentStroke = null
    }
}
