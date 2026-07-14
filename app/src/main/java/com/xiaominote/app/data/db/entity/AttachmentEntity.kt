package com.xiaominote.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An imported document (PDF / PPT / Word / image) attached to a note.
 * The importer rasterises each page to an image stored under [storedPath]
 * so handwriting can be drawn on top.
 */
@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val noteId: Long,
    val type: AttachmentType,
    /** Original file name provided by the user */
    val originalName: String,
    /** Relative path inside app's files dir where page images live */
    val storedPath: String,
    val pageCount: Int = 1,
    val importedAt: Long = System.currentTimeMillis(),
)

enum class AttachmentType(val mimeType: String) {
    PDF("application/pdf"),
    PPT("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    WORD("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    IMAGE("image/*"),
    OTHER("application/octet-stream");
}
