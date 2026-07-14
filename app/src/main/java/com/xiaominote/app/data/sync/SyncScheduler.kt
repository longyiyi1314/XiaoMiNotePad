package com.xiaominote.app.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.xiaominote.app.data.prefs.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init() {
        // React to sync-enabled / interval / wifi-only changes.
        settings.syncEnabled
            .distinctUntilChanged()
            .onEach { enabled -> if (enabled) reschedule() else cancel() }
            .launchIn(scope)
    }

    /** Re-reads settings and reschedules the periodic worker. */
    suspend fun reschedule() {
        val enabled = settings.syncEnabled.first()
        if (!enabled) {
            cancel()
            return
        }
        val intervalMin = settings.syncIntervalMinutes.first().toLong()
        val wifiOnly = settings.syncOnWifiOnly.first()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<GithubSyncWorker>(
            intervalMin.coerceAtLeast(15), TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    /** Trigger an immediate one-off sync (used by the manual "sync now" button). */
    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        const val WORK_NAME = "notepad_github_sync"
    }
}
