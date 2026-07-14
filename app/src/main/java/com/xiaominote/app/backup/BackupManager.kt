package com.xiaominote.app.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.xiaominote.app.data.db.AppDatabase
import com.xiaominote.app.data.db.entity.FolderEntity
import com.xiaominote.app.data.db.entity.NoteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Full local backup/restore. Exports every note + folder to a single JSON
 * file the user can save anywhere (Documents, cloud drive, USB). Restore
 * merges by remoteId (existing items are kept if newer).
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val gson: Gson,
) {

    data class BackupBundle(
        val version: Int = 1,
        val exportedAt: Long = System.currentTimeMillis(),
        val folders: List<FolderEntity>,
        val notes: List<NoteEntity>,
    )

    /** Writes a full backup JSON to [dest]. Returns the byte count written. */
    suspend fun exportBackup(dest: Uri): Long {
        val notes = db.noteDao().getAllNotes()
        val folders = db.folderDao().getAllFolders()
        val bundle = BackupBundle(folders = folders, notes = notes)
        val json = gson.toJson(bundle)

        return context.contentResolver.openOutputStream(dest)?.use { out ->
            val bytes = json.toByteArray(Charsets.UTF_8)
            out.write(bytes)
            bytes.size.toLong()
        } ?: 0L
    }

    /** Restores from a backup JSON at [src]. Returns counts restored. */
    suspend fun importBackup(src: Uri): RestoreResult {
        val json = context.contentResolver.openInputStream(src)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        } ?: return RestoreResult(0, 0, "Cannot read file")

        val bundle = runCatching { gson.fromJson(json, BackupBundle::class.java) }.getOrNull()
            ?: return RestoreResult(0, 0, "Invalid backup file")

        var foldersRestored = 0
        var notesRestored = 0

        // Folders first (notes reference them).
        for (folder in bundle.folders) {
            val existing = db.folderDao().getByRemoteId(folder.remoteId)
            if (existing == null) {
                db.folderDao().upsert(folder.copy(id = 0L, parentId = null))
                foldersRestored++
            } else if (folder.updatedAt > existing.updatedAt) {
                db.folderDao().update(folder.copy(id = existing.id, parentId = existing.parentId))
                foldersRestored++
            }
        }

        for (note in bundle.notes) {
            val existing = db.noteDao().getByRemoteId(note.remoteId)
            if (existing == null) {
                db.noteDao().upsert(note.copy(id = 0L, folderId = null, localDirty = true))
                notesRestored++
            } else if (note.updatedAt > existing.updatedAt) {
                db.noteDao().update(
                    note.copy(id = existing.id, folderId = existing.folderId, localDirty = true)
                )
                notesRestored++
            }
        }

        return RestoreResult(notesRestored, foldersRestored)
    }

    data class RestoreResult(
        val notesRestored: Int,
        val foldersRestored: Int,
        val error: String? = null,
    )
}
