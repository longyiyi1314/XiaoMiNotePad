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
 * the in-progress stroke, and an undo/redo stack. Supports multiple pages.
 */
@Stable
class DrawingState(
    initial: List<List<Stroke>> = listOf(emptyList()),
    initialPen: PenConfig = PenConfig(),
) {
    private val _pages: SnapshotStateList<SnapshotStateList<Stroke>> = mutableStateListOf()
    private var _currentPageIndex by mutableStateOf(0)

    init {
        initial.forEach { pageStrokes ->
            val page = mutableStateListOf<Stroke>()
            page.addAll(pageStrokes)
            _pages.add(page)
        }
        if (_pages.isEmpty()) {
            _pages.add(mutableStateListOf())
        }
    }

    val currentPageIndex: Int get() = _currentPageIndex
    val totalPages: Int get() = _pages.size
    val strokes: List<Stroke> get() = _pages.getOrNull(_currentPageIndex) ?: emptyList()

    private val _redoStack: SnapshotStateList<Stroke> = mutableStateListOf()
    val canUndo: Boolean get() = strokes.isNotEmpty()
    val canRedo: Boolean get() = _redoStack.isNotEmpty()
    val canGoNext: Boolean get() = _currentPageIndex < _pages.size - 1
    val canGoPrevious: Boolean get() = _currentPageIndex > 0

    var penConfig by mutableStateOf(initialPen)
        private set

    var currentStroke by mutableStateOf<Stroke?>(null)
        private set

    fun updatePenConfig(config: PenConfig) {
        penConfig = config
    }

    fun goToPage(index: Int) {
        if (index >= 0 && index < _pages.size) {
            _currentPageIndex = index
            _redoStack.clear()
        }
    }

    fun nextPage() {
        if (canGoNext) {
            _currentPageIndex++
            _redoStack.clear()
        }
    }

    fun previousPage() {
        if (canGoPrevious) {
            _currentPageIndex--
            _redoStack.clear()
        }
    }

    fun addPage() {
        _pages.add(mutableStateListOf())
        _currentPageIndex = _pages.size - 1
        _redoStack.clear()
    }

    fun removePage(index: Int) {
        if (_pages.size <= 1) return
        if (index >= 0 && index < _pages.size) {
            _pages.removeAt(index)
            if (_currentPageIndex >= _pages.size) {
                _currentPageIndex = _pages.size - 1
            }
            _redoStack.clear()
        }
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
        val currentPage = _pages.getOrNull(_currentPageIndex) ?: return
        if (stroke.isEraser) {
            val tolerance = stroke.config.size * 1.5f
            val toRemove = currentPage.filter { s ->
                stroke.points.any { p -> StrokeRenderer.hits(s, Offset(p.x, p.y), tolerance) }
            }
            currentPage.removeAll(toRemove.toSet())
        } else {
            currentPage.add(stroke)
        }
    }

    fun undo() {
        val currentPage = _pages.getOrNull(_currentPageIndex) ?: return
        if (currentPage.isEmpty()) return
        _redoStack.add(currentPage.removeAt(currentPage.lastIndex))
    }

    fun redo() {
        if (_redoStack.isEmpty()) return
        val currentPage = _pages.getOrNull(_currentPageIndex) ?: return
        currentPage.add(_redoStack.removeAt(_redoStack.lastIndex))
    }

    fun clearAll() {
        val currentPage = _pages.getOrNull(_currentPageIndex) ?: return
        currentPage.clear()
        _redoStack.clear()
        currentStroke = null
    }

    fun replaceAll(allPages: List<List<Stroke>>) {
        _pages.clear()
        _redoStack.clear()
        allPages.forEach { pageStrokes ->
            val page = mutableStateListOf<Stroke>()
            page.addAll(pageStrokes)
            _pages.add(page)
        }
        if (_pages.isEmpty()) {
            _pages.add(mutableStateListOf())
        }
        _currentPageIndex = 0
        currentStroke = null
    }

    fun getAllPages(): List<List<Stroke>> {
        return _pages.map { it.toList() }
    }
}
