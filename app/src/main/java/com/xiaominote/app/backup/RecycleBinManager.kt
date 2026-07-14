package com.xiaominote.app.backup

import com.xiaominote.app.data.repository.NoteRepository
import com.xiaominote.app.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maintains the recycle bin: permanently purges soft-deleted notes that have
 * been trashed for longer than the configured retention period. Called on app
 * start and after each sync.
 */
@Singleton
class RecycleBinManager @Inject constructor(
    private val noteRepository: NoteRepository,
    private val settings: SettingsRepository,
) {

    suspend fun purgeExpired() {
        val retentionDays = settings.recycleBinRetentionDays.first()
        if (retentionDays <= 0) return // 0 = keep forever
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays.toLong())
        noteRepository.purgeOlderThan(cutoff)
    }
}
