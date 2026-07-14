package com.xiaominote.app.ui.screen.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaominote.app.backup.BackupManager
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val busy: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportTo(dest: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState(busy = true)
            val result = runCatching { backupManager.exportBackup(dest) }
            _uiState.value = BackupUiState(
                message = result.fold(
                    onSuccess = { "备份成功，写入 ${it / 1024} KB" },
                    onFailure = { "备份失败：${it.message}" },
                )
            )
        }
    }

    fun importFrom(src: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState(busy = true)
            val result = runCatching { backupManager.importBackup(src) }
            _uiState.value = BackupUiState(
                message = result.fold(
                    onSuccess = { it.error?.let { e -> "恢复失败：$e" } ?: "恢复成功：${it.notesRestored} 笔记，${it.foldersRestored} 文件夹" },
                    onFailure = { "恢复失败：${it.message}" },
                )
            )
        }
    }
}
