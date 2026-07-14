package com.xiaominote.app.ui.screen.recyclebin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaominote.app.data.db.entity.NoteEntity
import com.xiaominote.app.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    val trashedNotes: StateFlow<List<NoteEntity>> = noteRepository.observeTrashed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restore(id: Long) = viewModelScope.launch { noteRepository.restore(id) }
    fun deletePermanently(id: Long) = viewModelScope.launch { noteRepository.deletePermanently(id) }
    fun emptyAll() = viewModelScope.launch { noteRepository.emptyRecycleBin() }
}
