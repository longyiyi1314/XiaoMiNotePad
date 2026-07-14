package com.xiaominote.app.ui.screen.home

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaominote.app.data.db.entity.FolderEntity
import com.xiaominote.app.data.db.entity.NoteEntity
import com.xiaominote.app.data.repository.FolderRepository
import com.xiaominote.app.data.repository.NoteRepository
import com.xiaominote.app.importing.DocumentImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeTab(val label: String) {
    ALL("全部"),
    FAVORITES("收藏"),
}

data class HomeUiState(
    val folders: List<FolderEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val currentFolderId: Long? = null,
    val currentFolderName: String? = null,
    val tab: HomeTab = HomeTab.ALL,
    val searchQuery: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val importer: DocumentImporter,
) : ViewModel() {

    private val currentFolder = MutableStateFlow<Long?>(null)
    private val tab = MutableStateFlow(HomeTab.ALL)
    private val query = MutableStateFlow("")

    private val notesFlow = combine(currentFolder, tab, query) { folderId, currentTab, q ->
        Triple(folderId, currentTab, q)
    }.flatMapLatest { (folderId, currentTab, q) ->
        when {
            q.isNotBlank() -> noteRepository.search(q)
            currentTab == HomeTab.FAVORITES -> noteRepository.observeFavorites()
            else -> noteRepository.observeNotesInFolder(folderId)
        }
    }

    private val foldersFlow = currentFolder.flatMapLatest { parentId ->
        folderRepository.observeByParent(parentId)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        foldersFlow, notesFlow, currentFolder, tab, query
    ) { folders, notes, folderId, currentTab, q ->
        val folderName = folderId?.let { runCatching { folderRepository.getById(it)?.name }.getOrNull() }
        HomeUiState(
            folders = folders,
            notes = notes,
            currentFolderId = folderId,
            currentFolderName = folderName,
            tab = currentTab,
            searchQuery = q,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun openFolder(folderId: Long) {
        currentFolder.value = folderId
        tab.value = HomeTab.ALL
    }

    fun navigateUp(): Boolean {
        val current = currentFolder.value ?: return false
        viewModelScope.launch {
            val parent = folderRepository.getById(current)?.parentId
            currentFolder.value = parent
        }
        return true
    }

    fun setTab(newTab: HomeTab) {
        tab.value = newTab
    }

    fun setQuery(q: String) {
        query.value = q
    }

    fun createFolder(name: String, color: Long) {
        viewModelScope.launch {
            folderRepository.createFolder(name, color, currentFolder.value)
        }
    }

    fun renameFolder(folderId: Long, name: String) {
        viewModelScope.launch { folderRepository.rename(folderId, name) }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch { folderRepository.delete(folderId) }
    }

    fun toggleFavorite(noteId: Long) {
        viewModelScope.launch { noteRepository.toggleFavorite(noteId) }
    }

    fun trashNote(noteId: Long) {
        viewModelScope.launch { noteRepository.trash(noteId) }
    }

    fun moveNoteToFolder(noteId: Long, folderId: Long?) {
        viewModelScope.launch { noteRepository.moveToFolder(noteId, folderId) }
    }

    /**
     * Processes an incoming share/view intent (PDF/PPT/Word/image): creates a
     * new note, imports the document, then navigates to the editor.
     */
    fun processShareIntent(intent: Intent?, onNoteCreated: (Long) -> Unit) {
        if (intent == null || intent.action !in setOf(Intent.ACTION_SEND, Intent.ACTION_VIEW)) return
        val uri = when (intent.action) {
            Intent.ACTION_SEND -> intent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
            else -> intent.data
        } ?: return
        val mime = intent.type
        viewModelScope.launch {
            val noteId = noteRepository.createNote(title = "导入文件")
            runCatching { importer.importToNote(noteId, uri, mime) }
            onNoteCreated(noteId)
        }
    }
}
