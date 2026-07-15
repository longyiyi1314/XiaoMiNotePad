package com.xiaominote.app.drawing

import androidx.compose.ui.graphics.Color

/**
 * Preset pen colours plus support for arbitrary custom colours.
 */
object ColorPalette {

    val presets: List<Color> = listOf(
        Color(0xFF000000), Color(0xFF212121), Color(0xFF424242), Color(0xFF616161),
        Color(0xFF757575), Color(0xFF9E9E9E), Color(0xFFBDBDBD), Color(0xFFFFFFFF),
        Color(0xFFB71C1C), Color(0xFFD32F2F), Color(0xFFF44336), Color(0xFFEF5350),
        Color(0xFFE64A19), Color(0xFFF57C00), Color(0xFFFB8C00), Color(0xFFFF9800),
        Color(0xFFFBC02D), Color(0xFFFDD835), Color(0xFFFFEB3B), Color(0xFFFFEE58),
        Color(0xFF689F38), Color(0xFF7CB342), Color(0xFF8BC34A), Color(0xFF4CAF50),
        Color(0xFF00796B), Color(0xFF00897B), Color(0xFF009688), Color(0xFF1B6B57),
        Color(0xFF0277BD), Color(0xFF0288D1), Color(0xFF03A9F4), Color(0xFF1E88E5),
        Color(0xFF303F9F), Color(0xFF3949AB), Color(0xFF3F51B5), Color(0xFF5C6BC0),
        Color(0xFF6A1B9A), Color(0xFF7B1FA2), Color(0xFF8E24AA), Color(0xFF9C27B0),
        Color(0xFFAD1457), Color(0xFFC2185B), Color(0xFFD81B60), Color(0xFFE91E63),
        Color(0xFF3E2723), Color(0xFF5D4037), Color(0xFF6D4C41), Color(0xFF8D6E63),
    )

    /** Highlighter-friendly presets (lighter shades). */
    val highlightPresets: List<Color> = listOf(
        Color(0x66FFEB3B), Color(0x66FFF176), Color(0x66FFF59D), Color(0x66FFF9C4),
        Color(0x66AED581), Color(0x66A5D6A7), Color(0x6681C784), Color(0x6666BB6A),
        Color(0x6680CBC4), Color(0x664DB6AC), Color(0x6626A69A), Color(0x66009688),
        Color(0x6664B5F6), Color(0x664FC3F7), Color(0x6629B6F6), Color(0x6603A9F4),
        Color(0x667986CB), Color(0x665C6BC0), Color(0x663F51B5), Color(0x66303F9F),
        Color(0x66BA68C8), Color(0x66AB47BC), Color(0x669C27B0), Color(0x668E24AA),
        Color(0x66F06292), Color(0x66EC407A), Color(0x66E91E63), Color(0x66D81B60),
        Color(0x66EF9A9A), Color(0x66E57373), Color(0x66EF5350), Color(0x66F44336),
        Color(0x66FFB74D), Color(0x66FFA726), Color(0x66FF9800), Color(0x66FB8C00),
        Color(0x66D7CCC8), Color(0x66BCAAA4), Color(0x66A1887F), Color(0x668D6E63),
    )

    fun toArgbLong(color: Color): Long {
        val a = (color.alpha * 255).toInt().toLong() shl 24
        val r = (color.red * 255).toInt().toLong() shl 16
        val g = (color.green * 255).toInt().toLong() shl 8
        val b = (color.blue * 255).toInt().toLong()
        return a or r or g or b
    }

    fun fromArgbLong(value: Long): Color = Color(value)
}
