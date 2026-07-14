package com.xiaominote.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.xiaominote.app.data.db.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE isTrashed = 0 AND folderId IS :folderId ORDER BY updatedAt DESC")
    fun observeNotesInFolder(folderId: Long?): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isTrashed = 0 ORDER BY updatedAt DESC")
    fun observeAllActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isTrashed = 0 AND isFavorite = 1 ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isTrashed = 1 ORDER BY trashedAt DESC")
    fun observeTrashed(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeNote(id: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): NoteEntity?

    @Query("""
        SELECT * FROM notes
        WHERE isTrashed = 0
          AND (title LIKE '%' || :query || '%' OR plainText LIKE '%' || :query || '%')
        ORDER BY updatedAt DESC
    """)
    fun search(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("UPDATE notes SET isFavorite = :favorite, updatedAt = :updatedAt, localDirty = 1 WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET folderId = :folderId, updatedAt = :updatedAt, localDirty = 1 WHERE id = :id")
    suspend fun moveToFolder(id: Long, folderId: Long?, updatedAt: Long = System.currentTimeMillis())

    /** Soft delete -> move to recycle bin */
    @Query("UPDATE notes SET isTrashed = 1, trashedAt = :trashedAt, localDirty = 1 WHERE id = :id")
    suspend fun trash(id: Long, trashedAt: Long = System.currentTimeMillis())

    /** Restore from recycle bin */
    @Query("UPDATE notes SET isTrashed = 0, trashedAt = NULL, localDirty = 1 WHERE id = :id")
    suspend fun restore(id: Long)

    /** Permanently delete a single note */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    /** Empty the recycle bin entirely */
    @Query("DELETE FROM notes WHERE isTrashed = 1")
    suspend fun emptyRecycleBin()

    /** Purge recycle-bin items older than [before] timestamp */
    @Query("DELETE FROM notes WHERE isTrashed = 1 AND trashedAt IS NOT NULL AND trashedAt < :before")
    suspend fun purgeOlderThan(before: Long)

    @Query("SELECT * FROM notes WHERE localDirty = 1")
    suspend fun getDirtyNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<NoteEntity>

    @Query("UPDATE notes SET localDirty = 0, lastSyncedAt = :syncedAt WHERE id = :id")
    suspend fun markSynced(id: Long, syncedAt: Long = System.currentTimeMillis())
}
