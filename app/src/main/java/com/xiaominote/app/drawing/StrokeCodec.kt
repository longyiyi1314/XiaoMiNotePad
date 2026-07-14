package com.xiaominote.app.drawing

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StrokeCodec {
    private val gson = Gson()
    private val strokeListType = object : TypeToken<List<Stroke?>?>() {}.type

    fun encode(strokes: List<Stroke>): String =
        if (strokes.isEmpty()) "[]" else gson.toJson(strokes)

    fun decode(json: String?): List<Stroke> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        @Suppress("UNCHECKED_CAST")
        return runCatching { (gson.fromJson(json, strokeListType) as List<Stroke?>?)?.filterNotNull() ?: emptyList() }
            .getOrElse { emptyList() }
    }
}
