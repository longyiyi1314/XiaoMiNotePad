package com.xiaominote.app.data.db.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single note. Contains both handwriting strokes (as JSON) and optional
 * plain-text content for search. Soft-deleted notes go to the recycle bin
 * via [isTrashed]/[trashedAt] and are purged after a retention period.
 */
@Immutable
@Entity(
    tableName = "notes",
    indices = [
        Index("remoteId", unique = true),
        Index("folderId"),
        Index("isFavorite"),
        Index("isTrashed"),
        Index("updatedAt"),
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    /** Stable UUID used as the sync file name across devices. */
    val remoteId: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val folderId: Long? = null,
    /** Remote UUID of the parent folder, resolved on pull. */
    val folderRemoteId: String? = null,

    /** JSON-serialized list of [com.xiaominote.app.drawing.Stroke] */
    val drawingJson: String = "[]",

    /** Extracted/typed text content, searchable */
    val plainText: String = "",

    /** ARGB paper background color as Long */
    val paperColor: Long = 0xFFFFFFFF,

    /** Cover color shown on the note card as Long */
    val coverColor: Long = 0xFFA6F2D9,

    val isFavorite: Boolean = false,

    // Recycle bin / soft delete
    val isTrashed: Boolean = false,
    val trashedAt: Long? = null,

    // Sync metadata
    val localDirty: Boolean = true,
    val lastSyncedAt: Long? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
