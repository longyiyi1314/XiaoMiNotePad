package com.xiaominote.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.xiaominote.app.data.db.dao.AttachmentDao
import com.xiaominote.app.data.db.dao.FolderDao
import com.xiaominote.app.data.db.dao.NoteDao
import com.xiaominote.app.data.db.entity.AttachmentEntity
import com.xiaominote.app.data.db.entity.FolderEntity
import com.xiaominote.app.data.db.entity.NoteEntity

@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        AttachmentEntity::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        const val DATABASE_NAME = "notepad.db"
    }
}
