package com.xiaominote.app.ui.screen.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaominote.app.data.db.entity.NoteEntity
import com.xiaominote.app.data.prefs.SettingsRepository
import com.xiaominote.app.data.repository.NoteRepository
import com.xiaominote.app.drawing.ColorPalette
import com.xiaominote.app.drawing.DrawingState
import com.xiaominote.app.drawing.PenConfig
import com.xiaominote.app.drawing.PenType
import com.xiaominote.app.drawing.Stroke
import com.xiaominote.app.drawing.StrokeCodec
import com.xiaominote.app.importing.DocumentImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import android.net.Uri
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorUiState(
    val noteId: Long = -1L,
    val title: String = "",
    val paperColor: Long = 0xFFFFFFFF,
    val isFavorite: Boolean = false,
    val attachmentsCount: Int = 0,
    val isLoaded: Boolean = false,
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val importer: DocumentImporter,
    private val settings: SettingsRepository,
) : ViewModel() {

    val drawingState = DrawingState()

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun load(noteId: Long) {
        if (_uiState.value.noteId == noteId && _uiState.value.isLoaded) return
        viewModelScope.launch {
            val effectiveId = if (noteId <= 0L) {
                noteRepository.createNote()
            } else {
                noteId
            }
            val note = noteRepository.getNote(effectiveId)
            if (note != null) {
                val strokes = StrokeCodec.decode(note.drawingJson)
                drawingState.replaceAll(strokes)
                _uiState.value = EditorUiState(
                    noteId = note.id,
                    title = note.title,
                    paperColor = note.paperColor,
                    isFavorite = note.isFavorite,
                    attachmentsCount = noteRepository.getAttachments(note.id).size,
                    isLoaded = true,
                )
            } else {
                _uiState.value = EditorUiState(noteId = effectiveId, isLoaded = true)
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
        viewModelScope.launch {
            noteRepository.renameNote(_uiState.value.noteId, title)
        }
    }

    fun selectPenType(type: PenType) {
        drawingState.updatePenConfig(drawingState.penConfig.copy(type = type))
    }

    fun selectColor(color: Color) {
        val argb = ColorPalette.toArgbLong(color)
        drawingState.updatePenConfig(drawingState.penConfig.copy(color = argb))
        viewModelScope.launch { settings.setDefaultPenColor(argb) }
    }

    fun setPenSize(size: Float) {
        drawingState.updatePenConfig(drawingState.penConfig.copy(size = size))
        viewModelScope.launch { settings.setDefaultPenSize(size) }
    }

    fun undo() = drawingState.undo()
    fun redo() = drawingState.redo()
    fun clearCanvas() = drawingState.clearAll()

    fun toggleFavorite() {
        viewModelScope.launch {
            noteRepository.toggleFavorite(_uiState.value.noteId)
            _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
        }
    }

    /** Persist the current strokes to the database (debounced by the screen). */
    fun saveDrawing(strokes: List<Stroke>) {
        val id = _uiState.value.noteId
        if (id <= 0L) return
        viewModelScope.launch {
            noteRepository.saveDrawing(id, strokes)
        }
    }

    fun importDocument(uri: Uri, mime: String?) {
        val id = _uiState.value.noteId
        if (id <= 0L) return
        viewModelScope.launch {
            val result = importer.importToNote(id, uri, mime)
            _uiState.value = _uiState.value.copy(attachmentsCount = _uiState.value.attachmentsCount + 1)
        }
    }
}
