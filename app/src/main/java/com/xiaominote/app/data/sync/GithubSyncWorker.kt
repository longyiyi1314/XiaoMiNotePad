package com.xiaominote.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.xiaominote.app.data.prefs.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class GithubSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: GithubSyncManager,
    private val settings: SettingsRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val enabled = settings.syncEnabled.first()
        if (!enabled) return Result.success()

        return try {
            val result = syncManager.sync()
            if (result.success) Result.success() else Result.retry()
        } catch (t: Throwable) {
            Result.retry()
        }
    }
}
