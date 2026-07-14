package com.xiaominote.app.data.db

import androidx.room.TypeConverter
import com.xiaominote.app.data.db.entity.AttachmentType

class Converters {

    @TypeConverter
    fun fromAttachmentType(type: AttachmentType?): String? = type?.name

    @TypeConverter
    fun toAttachmentType(value: String?): AttachmentType? =
        value?.let { runCatching { AttachmentType.valueOf(it) }.getOrNull() }
}
