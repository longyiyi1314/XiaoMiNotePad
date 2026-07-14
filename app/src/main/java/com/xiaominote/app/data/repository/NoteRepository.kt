package com.xiaominote.app.data.repository

import com.xiaominote.app.data.db.dao.AttachmentDao
import com.xiaominote.app.data.db.dao.NoteDao
import com.xiaominote.app.data.db.entity.AttachmentEntity
import com.xiaominote.app.data.db.entity.NoteEntity
import com.xiaominote.app.drawing.Stroke
import com.xiaominote.app.drawing.StrokeCodec
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val attachmentDao: AttachmentDao,
) {

    // ---- Observation ----
    fun observeNotesInFolder(folderId: Long?): Flow<List<NoteEntity>> =
        noteDao.observeNotesInFolder(folderId)

    fun observeFavorites(): Flow<List<NoteEntity>> = noteDao.observeFavorites()

    fun observeTrashed(): Flow<List<NoteEntity>> = noteDao.observeTrashed()

    fun observeNote(id: Long): Flow<NoteEntity?> = noteDao.observeNote(id)

    fun search(query: String): Flow<List<NoteEntity>> = noteDao.search(query)

    suspend fun getNote(id: Long): NoteEntity? = noteDao.getById(id)

    suspend fun getStrokes(id: Long): List<Stroke> =
        noteDao.getById(id)?.let { StrokeCodec.decode(it.drawingJson) } ?: emptyList()

    // ---- CRUD ----
    suspend fun createNote(
        title: String = "",
        folderId: Long? = null,
        paperColor: Long = 0xFFFFFFFF,
        coverColor: Long = 0xFFA6F2D9,
    ): Long {
        val now = System.currentTimeMillis()
        val note = NoteEntity(
            title = title,
            folderId = folderId,
            paperColor = paperColor,
            coverColor = coverColor,
            createdAt = now,
            updatedAt = now,
        )
        return noteDao.upsert(note)
    }

    suspend fun findByRemoteId(remoteId: String): NoteEntity? = noteDao.getByRemoteId(remoteId)

    suspend fun updateNote(note: NoteEntity) {
        noteDao.update(note.copy(updatedAt = System.currentTimeMillis(), localDirty = true))
    }

    suspend fun saveDrawing(noteId: Long, strokes: List<Stroke>) {
        val note = noteDao.getById(noteId) ?: return
        noteDao.update(
            note.copy(
                drawingJson = StrokeCodec.encode(strokes),
                updatedAt = System.currentTimeMillis(),
                localDirty = true,
            )
        )
    }

    suspend fun renameNote(id: Long, title: String) {
        val note = noteDao.getById(id) ?: return
        noteDao.update(note.copy(title = title, updatedAt = System.currentTimeMillis(), localDirty = true))
    }

    suspend fun updatePaperColor(id: Long, color: Long) {
        val note = noteDao.getById(id) ?: return
        noteDao.update(note.copy(paperColor = color, updatedAt = System.currentTimeMillis(), localDirty = true))
    }

    suspend fun setPlainText(id: Long, text: String) {
        val note = noteDao.getById(id) ?: return
        noteDao.update(note.copy(plainText = text, updatedAt = System.currentTimeMillis(), localDirty = true))
    }

    // ---- Favorites ----
    suspend fun toggleFavorite(id: Long) {
        val note = noteDao.getById(id) ?: return
        noteDao.setFavorite(id, !note.isFavorite)
    }

    // ---- Folders ----
    suspend fun moveToFolder(id: Long, folderId: Long?) {
        noteDao.moveToFolder(id, folderId)
    }

    // ---- Recycle bin ----
    suspend fun trash(id: Long) = noteDao.trash(id)
    suspend fun restore(id: Long) = noteDao.restore(id)
    suspend fun deletePermanently(id: Long) = noteDao.deletePermanently(id)
    suspend fun emptyRecycleBin() = noteDao.emptyRecycleBin()
    suspend fun purgeOlderThan(before: Long) = noteDao.purgeOlderThan(before)

    // ---- Attachments ----
    fun observeAttachments(noteId: Long): Flow<List<AttachmentEntity>> =
        attachmentDao.observeForNote(noteId)

    suspend fun addAttachment(attachment: AttachmentEntity): Long =
        attachmentDao.upsert(attachment)

    suspend fun getAttachments(noteId: Long): List<AttachmentEntity> =
        attachmentDao.getForNote(noteId)

    suspend fun deleteAttachment(id: Long) = attachmentDao.delete(id)

    // ---- Sync helpers ----
    suspend fun getDirtyNotes(): List<NoteEntity> = noteDao.getDirtyNotes()

    /**
     * Insert or update a note arriving from sync. Matches by [NoteEntity.remoteId].
     * Keeps the existing local [id] when updating; resolves the local folderId
     * from [NoteEntity.folderRemoteId] (null if the folder isn't present locally yet).
     */
    suspend fun upsertFromSync(remote: NoteEntity) {
        val existing = noteDao.getByRemoteId(remote.remoteId)
        if (existing != null) {
            // Last-write-wins: only apply if remote is newer.
            if (remote.updatedAt >= existing.updatedAt) {
                noteDao.update(
                    remote.copy(
                        id = existing.id,
                        folderId = existing.folderId, // keep local until folder sync resolves
                        localDirty = false,
                        lastSyncedAt = System.currentTimeMillis(),
                    )
                )
            }
        } else {
            noteDao.upsert(remote.copy(localDirty = false, lastSyncedAt = System.currentTimeMillis()))
        }
    }

    suspend fun markSynced(id: Long) = noteDao.markSynced(id)
}
