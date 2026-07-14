package com.xiaominote.app.drawing

import androidx.compose.ui.graphics.Color

/**
 * Preset pen colours plus support for arbitrary custom colours.
 */
object ColorPalette {

    val presets: List<Color> = listOf(
        Color(0xFF000000), // black
        Color(0xFF737373), // dark grey
        Color(0xFFFFFFFF), // white
        Color(0xFFE53935), // red
        Color(0xFFFB8C00), // orange
        Color(0xFFFDD835), // yellow
        Color(0xFF43A047), // green
        Color(0xFF1B6B57), // teal (brand)
        Color(0xFF1E88E5), // blue
        Color(0xFF3E6374), // slate blue
        Color(0xFF8E24AA), // purple
        Color(0xFFD81B60), // pink
        Color(0xFF6D4C41), // brown
    )

    /** Highlighter-friendly presets (lighter shades). */
    val highlightPresets: List<Color> = listOf(
        Color(0x66FFF176), // soft yellow
        Color(0x6680CBC4), // soft teal
        Color(0x6690CAF9), // soft blue
        Color(0x66EF9A9A), // soft red
        Color(0x66CE93D8), // soft purple
        Color(0x66A5D6A7), // soft green
    )

    fun toArgbLong(color: Color): Long = color.value.toLong()

    fun fromArgbLong(value: Long): Color = Color(value)
}
