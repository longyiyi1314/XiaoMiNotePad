package com.xiaominote.app.data.db.dao

import androidx.room.ColumnInfo

data class FolderNoteCount(
    @ColumnInfo(name = "folderId") val folderId: Long?,
    @ColumnInfo(name = "cnt") val count: Int,
)
