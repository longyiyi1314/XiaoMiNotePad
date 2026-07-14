package com.xiaominote.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notepad_settings")

/**
 * App settings backed by DataStore. Holds GitHub sync credentials/options,
 * theme and recycle-bin retention.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // GitHub sync
    val githubToken: Flow<String> = context.dataStore.data.map { it[TOKEN] ?: "" }
    val githubOwner: Flow<String> = context.dataStore.data.map { it[OWNER] ?: "" }
    val githubRepo: Flow<String> = context.dataStore.data.map { it[REPO] ?: "" }
    val githubBranch: Flow<String> = context.dataStore.data.map { it[BRANCH] ?: "main" }
    val syncEnabled: Flow<Boolean> = context.dataStore.data.map { it[SYNC_ENABLED] ?: false }
    val syncIntervalMinutes: Flow<Int> = context.dataStore.data.map { it[SYNC_INTERVAL] ?: 15 }
    val syncOnWifiOnly: Flow<Boolean> = context.dataStore.data.map { it[WIFI_ONLY] ?: true }

    // Theme
    val darkTheme: Flow<String> = context.dataStore.data.map { it[DARK_THEME] ?: "system" }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_COLOR] ?: true }

    // Recycle bin retention in days (0 = never auto-purge)
    val recycleBinRetentionDays: Flow<Int> = context.dataStore.data.map { it[RETENTION_DAYS] ?: 30 }

    val defaultPenColor: Flow<Long> = context.dataStore.data.map { it[DEFAULT_PEN_COLOR] ?: 0xFF000000L }
    val defaultPenSize: Flow<Float> = context.dataStore.data.map { it[DEFAULT_PEN_SIZE] ?: 4f }

    suspend fun setGithubCredentials(
        token: String,
        owner: String,
        repo: String,
        branch: String = "main",
    ) {
        context.dataStore.edit {
            it[TOKEN] = token.trim()
            it[OWNER] = owner.trim()
            it[REPO] = repo.trim()
            it[BRANCH] = branch.trim().ifEmpty { "main" }
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SYNC_ENABLED] = enabled }
    }

    suspend fun setSyncInterval(minutes: Int) {
        context.dataStore.edit { it[SYNC_INTERVAL] = minutes.coerceIn(15, 1440) }
    }

    suspend fun setWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { it[WIFI_ONLY] = wifiOnly }
    }

    suspend fun setDarkTheme(value: String) {
        context.dataStore.edit { it[DARK_THEME] = value }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[DYNAMIC_COLOR] = enabled }
    }

    suspend fun setRecycleBinRetention(days: Int) {
        context.dataStore.edit { it[RETENTION_DAYS] = days.coerceAtLeast(0) }
    }

    suspend fun setDefaultPenColor(color: Long) {
        context.dataStore.edit { it[DEFAULT_PEN_COLOR] = color }
    }

    suspend fun setDefaultPenSize(size: Float) {
        context.dataStore.edit { it[DEFAULT_PEN_SIZE] = size }
    }

    companion object {
        private val TOKEN = stringPreferencesKey("github_token")
        private val OWNER = stringPreferencesKey("github_owner")
        private val REPO = stringPreferencesKey("github_repo")
        private val BRANCH = stringPreferencesKey("github_branch")
        private val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        private val SYNC_INTERVAL = intPreferencesKey("sync_interval_min")
        private val WIFI_ONLY = booleanPreferencesKey("sync_wifi_only")
        private val DARK_THEME = stringPreferencesKey("dark_theme")
        private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val RETENTION_DAYS = intPreferencesKey("recycle_retention_days")
        private val DEFAULT_PEN_COLOR = longPreferencesKey("default_pen_color")
        private val DEFAULT_PEN_SIZE = floatPreferencesKey("default_pen_size")
    }
}
