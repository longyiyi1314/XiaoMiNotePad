package com.xiaominote.app.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaominote.app.data.prefs.SettingsRepository
import com.xiaominote.app.data.sync.GithubSyncManager
import com.xiaominote.app.data.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val token: String = "",
    val owner: String = "",
    val repo: String = "",
    val branch: String = "main",
    val syncEnabled: Boolean = false,
    val syncIntervalMinutes: Int = 15,
    val syncOnWifiOnly: Boolean = true,
    val darkTheme: String = "system",
    val dynamicColor: Boolean = true,
    val themeSeed: String = "teal",
    val recycleBinRetentionDays: Int = 30,
    val verifying: Boolean = false,
    val verifyMessage: String? = null,
    val syncing: Boolean = false,
    val syncMessage: String? = null,
)

private data class SyncCreds(val token: String, val owner: String, val repo: String, val branch: String)
private data class SyncOpts(val enabled: Boolean, val interval: Int, val wifiOnly: Boolean)
private data class ThemeOpts(val darkTheme: String, val dynamicColor: Boolean, val seed: String)
private data class TransientState(
    val verifying: Boolean = false,
    val verifyMessage: String? = null,
    val syncing: Boolean = false,
    val syncMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val syncManager: GithubSyncManager,
    private val syncScheduler: SyncScheduler,
) : ViewModel() {

    private val transient = MutableStateFlow(TransientState())

    private val creds = combine(
        settings.githubToken, settings.githubOwner, settings.githubRepo, settings.githubBranch
    ) { t, o, r, b -> SyncCreds(t, o, r, b) }

    private val syncOpts = combine(
        settings.syncEnabled, settings.syncIntervalMinutes, settings.syncOnWifiOnly
    ) { e, i, w -> SyncOpts(e, i, w) }

    private val themeOpts = combine(
        settings.darkTheme, settings.dynamicColor, settings.themeSeed
    ) { d, c, s -> ThemeOpts(d, c, s) }

    val uiState: StateFlow<SettingsUiState> = combine(
        creds, syncOpts, themeOpts, settings.recycleBinRetentionDays, transient
    ) { c, s, th, retention, tr ->
        SettingsUiState(
            token = c.token,
            owner = c.owner,
            repo = c.repo,
            branch = c.branch,
            syncEnabled = s.enabled,
            syncIntervalMinutes = s.interval,
            syncOnWifiOnly = s.wifiOnly,
            darkTheme = th.darkTheme,
            dynamicColor = th.dynamicColor,
            themeSeed = th.seed,
            recycleBinRetentionDays = retention,
            verifying = tr.verifying,
            verifyMessage = tr.verifyMessage,
            syncing = tr.syncing,
            syncMessage = tr.syncMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updateCredentials(token: String, owner: String, repo: String, branch: String) {
        viewModelScope.launch { settings.setGithubCredentials(token, owner, repo, branch) }
    }

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.setSyncEnabled(enabled)
            syncScheduler.reschedule()
        }
    }

    fun setSyncInterval(minutes: Int) {
        viewModelScope.launch {
            settings.setSyncInterval(minutes)
            syncScheduler.reschedule()
        }
    }

    fun setWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            settings.setWifiOnly(wifiOnly)
            syncScheduler.reschedule()
        }
    }

    fun setDarkTheme(value: String) {
        viewModelScope.launch { settings.setDarkTheme(value) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settings.setDynamicColor(enabled) }
    }

    fun setThemeSeed(seedId: String) {
        viewModelScope.launch { settings.setThemeSeed(seedId) }
    }

    fun setRecycleBinRetention(days: Int) {
        viewModelScope.launch { settings.setRecycleBinRetention(days) }
    }

    fun verifyCredentials() {
        viewModelScope.launch {
            transient.value = transient.value.copy(verifying = true, verifyMessage = null)
            val result = syncManager.verifyCredentials()
            transient.value = transient.value.copy(
                verifying = false,
                verifyMessage = result.fold(
                    onSuccess = { "验证成功：$it" },
                    onFailure = { "验证失败：${it.message}" },
                )
            )
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            transient.value = transient.value.copy(syncing = true, syncMessage = null)
            val result = runCatching { syncManager.sync() }
            transient.value = transient.value.copy(
                syncing = false,
                syncMessage = result.fold(
                    onSuccess = { r ->
                        if (r.success) "同步完成：上传 ${r.pushedNotes} 笔记，下载 ${r.pulledNotes} 笔记"
                        else "同步完成（有错误）：${r.errors.joinToString()}"
                    },
                    onFailure = { "同步失败：${it.message}" },
                )
            )
        }
    }
}
