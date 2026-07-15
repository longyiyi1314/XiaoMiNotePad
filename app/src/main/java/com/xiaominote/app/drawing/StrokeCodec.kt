package com.xiaominote.app.drawing

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StrokeCodec {
    private val gson = Gson()
    private val strokeListType = object : TypeToken<List<Stroke?>?>() {}.type
    private val pagesType = object : TypeToken<List<List<Stroke?>?>?>() {}.type

    fun encode(strokes: List<Stroke>): String =
        if (strokes.isEmpty()) "[]" else gson.toJson(strokes)

    fun decode(json: String?): List<Stroke> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        @Suppress("UNCHECKED_CAST")
        return runCatching { (gson.fromJson(json, strokeListType) as List<Stroke?>?)?.filterNotNull() ?: emptyList() }
            .getOrElse { emptyList() }
    }

    fun encodePages(pages: List<List<Stroke>>): String =
        if (pages.isEmpty() || pages.size == 1 && pages[0].isEmpty()) "[]" else gson.toJson(pages)

    fun decodePages(json: String?): List<List<Stroke>> {
        if (json.isNullOrBlank() || json == "[]") return listOf(emptyList())
        @Suppress("UNCHECKED_CAST")
        return runCatching {
            val raw = gson.fromJson(json, pagesType) as List<List<Stroke?>?>?
            raw?.map { page -> page?.filterNotNull() ?: emptyList() } ?: listOf(emptyList())
        }.getOrElse { listOf(emptyList()) }
    }
}
