package com.xiaominote.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A folder that groups notes. Supports one level of nesting via [parentId]
 * (null = top level).
 */
@Entity(
    tableName = "folders",
    indices = [Index("parentId"), Index("remoteId", unique = true)]
)
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    /** Stable UUID used as the sync file name across devices. */
    val remoteId: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    /** ARGB color as Long, used for the folder chip/icon tint */
    val color: Long = 0xFF1B6B57,
    val parentId: Long? = null,
    /** Remote UUID of the parent folder, resolved on pull. */
    val parentRemoteId: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
