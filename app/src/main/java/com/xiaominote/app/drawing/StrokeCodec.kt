package com.xiaominote.app.drawing

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Serialises a list of [Stroke]s to/from the JSON blob stored in
 * [com.xiaominote.app.data.db.entity.NoteEntity.drawingJson].
 */
object StrokeCodec {
    private val gson = Gson()
    private val type = object : TypeToken<List<Stroke>>() {}.type

    fun encode(strokes: List<Stroke>): String =
        if (strokes.isEmpty()) "[]" else gson.toJson(strokes, type)

    fun decode(json: String?): List<Stroke> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        return runCatching { gson.fromJson(json, type) ?: emptyList() }
            .getOrElse { emptyList() }
    }
}
