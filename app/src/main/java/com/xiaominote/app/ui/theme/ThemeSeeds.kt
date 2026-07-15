package com.xiaominote.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemeSeed(val id: String, val label: String, val seed: Color) {
    TEAL("teal", "松绿", Color(0xFF1B6B57)),
    INDIGO("indigo", "靛蓝", Color(0xFF3F51B5)),
    ROSE("rose", "玫红", Color(0xFFD81B60)),
    ORANGE("orange", "暖橙", Color(0xFFE65100)),
    PURPLE("purple", "紫罗兰", Color(0xFF7B1FA2)),
    BROWN("brown", "暖棕", Color(0xFF6D4C41)),
    DEEP_GREEN("deep_green", "森林", Color(0xFF2E7D32)),
    SKY("sky", "天青", Color(0xFF0277BD)),
}

private fun lightSchemeFromSeed(seed: Color) = when (seed) {
    ThemeSeed.TEAL.seed -> lightColorScheme(
        primary = Color(0xFF006A55), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF83F7D5), onPrimaryContainer = Color(0xFF002018),
        secondary = Color(0xFF4B6359), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCDE8DB), onSecondaryContainer = Color(0xFF072018),
        tertiary = Color(0xFF3E6374), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFC2E8FB), onTertiaryContainer = Color(0xFF001F2A),
        background = Color(0xFFFBFDFA), onBackground = Color(0xFF191C1A),
        surface = Color(0xFFFBFDFA), onSurface = Color(0xFF191C1A),
        surfaceVariant = Color(0xFFDBE5DF), onSurfaceVariant = Color(0xFF3F4945),
        outline = Color(0xFF6F7975), error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    )
    ThemeSeed.INDIGO.seed -> lightColorScheme(
        primary = Color(0xFF3F51B5), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE0E7FF), onPrimaryContainer = Color(0xFF00125E),
        secondary = Color(0xFF595D72), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDDE1F9), onSecondaryContainer = Color(0xFF161B2C),
        tertiary = Color(0xFF725272), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFDD7FA), onTertiaryContainer = Color(0xFF29102C),
        background = Color(0xFFFEFBFF), onBackground = Color(0xFF1B1B1F),
        surface = Color(0xFFFEFBFF), onSurface = Color(0xFF1B1B1F),
        surfaceVariant = Color(0xFFE2E1EC), onSurfaceVariant = Color(0xFF45464F),
        outline = Color(0xFF767680), error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    )
    ThemeSeed.ROSE.seed -> lightColorScheme(
        primary = Color(0xFFAD1457), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD9DE), onPrimaryContainer = Color(0xFF3F0019),
        secondary = Color(0xFF725559), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDADF), onSecondaryContainer = Color(0xFF2B1418),
        tertiary = Color(0xFF785631), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDDB6), onTertiaryContainer = Color(0xFF2A1600),
        background = Color(0xFFFFFBFF), onBackground = Color(0xFF201A1A),
        surface = Color(0xFFFFFBFF), onSurface = Color(0xFF201A1A),
        surfaceVariant = Color(0xFFF4DDDF), onSurfaceVariant = Color(0xFF534344),
        outline = Color(0xFF857374), error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    )
    ThemeSeed.ORANGE.seed -> lightColorScheme(
        primary = Color(0xFFE65100), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDBCD), onPrimaryContainer = Color(0xFF3B0A00),
        secondary = Color(0xFF775747), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDBCD), onSecondaryContainer = Color(0xFF2C1609),
        tertiary = Color(0xFF6C5E2E), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF7E2A5), onTertiaryContainer = Color(0xFF221B00),
        background = Color(0xFFFFFBFF), onBackground = Color(0xFF201A17),
        surface = Color(0xFFFFFBFF), onSurface = Color(0xFF201A17),
        surfaceVariant = Color(0xFFF5DED3), onSurfaceVariant = Color(0xFF53433C),
        outline = Color(0xFF85736B), error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    )
    ThemeSeed.PURPLE.seed -> lightColorScheme(
        primary = Color(0xFF7B1FA2), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF8D7FF), onPrimaryContainer = Color(0xFF2A003A),
        secondary = Color(0xFF6B586D), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF4DBF4), onSecondaryContainer = Color(0xFF251628),
        tertiary = Color(0xFF83524E), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDAD5), onTertiaryContainer = Color(0xFF33110F),
        background = Color(0xFFFFFBFF), onBackground = Color(0xFF1E1A1D),
        surface = Color(0xFFFFFBFF), onSurface = Color(0xFF1E1A1D),
        surfaceVariant = Color(0xFFEDDFE9), onSurfaceVariant = Color(0xFF4E444C),
        outline = Color(0xFF7F747C), error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    )
    ThemeSeed.BROWN.seed -> lightColorScheme(
        primary = Color(0xFF6D4C41), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDBCB), onPrimaryContainer = Color(0xFF2A0E04),
        secondary = Color(0xFF6E584C), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF9DBCB), onSecondaryContainer = Color(0xFF27170D),
        tertiary = Color(0xFF59603C), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFDEE5B5), onTertiaryContainer = Color(0xFF171D01),
        background = Color(0xFFFFFBFF), onBackground = Color(0xFF201A17),
        surface = Color(0xFFFFFBFF), onSurface = Color(0xFF201A17),
        surfaceVariant = Color(0xFFF5DED3), onSurfaceVariant = Color(0xFF53433C),
        outline = Color(0xFF85736B), error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    )
    ThemeSeed.DEEP_GREEN.seed -> lightColorScheme(
        primary = Color(0xFF2E7D32), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB3F5B0), onPrimaryContainer = Color(0xFF002205),
        secondary = Color(0xFF50634D), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD3E8CC), onSecondaryContainer = Color(0xFF0E1F0E),
        tertiary = Color(0xFF39656B), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFBCEBF1), onTertiaryContainer = Color(0xFF001F23),
        background = Color(0xFFFCFDF6), onBackground = Color(0xFF1A1C18),
        surface = Color(0xFFFCFDF6), onSurface = Color(0xFF1A1C18),
        surfaceVariant = Color(0xFFDEE5D6), onSurfaceVariant = Color(0xFF42493F),
        outline = Color(0xFF72796E), error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    )
    ThemeSeed.SKY.seed -> lightColorScheme(
        primary = Color(0xFF0277BD), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFCFE5FF), onPrimaryContainer = Color(0xFF001E30),
        secondary = Color(0xFF515F70), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD5E3F7), onSecondaryContainer = Color(0xFF0E1C2B),
        tertiary = Color(0xFF68578A), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE9DDFF), onTertiaryContainer = Color(0xFF221342),
        background = Color(0xFFFCFCFF), onBackground = Color(0xFF1A1C1E),
        surface = Color(0xFFFCFCFF), onSurface = Color(0xFF1A1C1E),
        surfaceVariant = Color(0xFFDEE3EB), onSurfaceVariant = Color(0xFF42474E),
        outline = Color(0xFF73777F), error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    )
    else -> lightColorScheme()
}

private fun darkSchemeFromSeed(seed: Color) = when (seed) {
    ThemeSeed.TEAL.seed -> darkColorScheme(
        primary = Color(0xFF67DABA), onPrimary = Color(0xFF00382B),
        primaryContainer = Color(0xFF00513F), onPrimaryContainer = Color(0xFF83F7D5),
        secondary = Color(0xFFB1CCBF), onSecondary = Color(0xFF1C352C),
        secondaryContainer = Color(0xFF334B42), onSecondaryContainer = Color(0xFFCDE8DB),
        tertiary = Color(Color(0xFFA6CCDF).value), onTertiary = Color(0xFF073544),
        tertiaryContainer = Color(0xFF254B5B), onTertiaryContainer = Color(0xFFC2E8FB),
        background = Color(0xFF191C1A), onBackground = Color(0xFFE1E3E0),
        surface = Color(0xFF191C1A), onSurface = Color(0xFFE1E3E0),
        surfaceVariant = Color(0xFF3F4945), onSurfaceVariant = Color(0xFFBFC9C3),
        outline = Color(0xFF899390), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    )
    ThemeSeed.INDIGO.seed -> darkColorScheme(
        primary = Color(0xFFBEC2FF), onPrimary = Color(0xFF1B2378),
        primaryContainer = Color(0xFF2A369F), onPrimaryContainer = Color(0xFFE0E7FF),
        secondary = Color(0xFFC1C4DD), onSecondary = Color(0xFF2B2F42),
        secondaryContainer = Color(0xFF424659), onSecondaryContainer = Color(0xFFDDE1F9),
        tertiary = Color(0xFFE0B7E1), onTertiary = Color(0xFF402743),
        tertiaryContainer = Color(0xFF593E5A), onTertiaryContainer = Color(0xFFFDD7FA),
        background = Color(0xFF131318), onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF131318), onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF45464F), onSurfaceVariant = Color(0xFFC6C5D0),
        outline = Color(0xFF908F99), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    )
    ThemeSeed.ROSE.seed -> darkColorScheme(
        primary = Color(0xFFFFB2BB), onPrimary = Color(0xFF66002D),
        primaryContainer = Color(0xFF8E0042), onPrimaryContainer = Color(0xFFFFD9DE),
        secondary = Color(0xFFE0BDC1), onSecondary = Color(0xFF42292D),
        secondaryContainer = Color(0xFF593F42), onSecondaryContainer = Color(0xFFFFDADF),
        tertiary = Color(0xFFE9C18F), onTertiary = Color(0xFF442A09),
        tertiaryContainer = Color(0xFF5E3F1D), onTertiaryContainer = Color(0xFFFFDDB6),
        background = Color(0xFF201A1A), onBackground = Color(0xFFEDE0E0),
        surface = Color(0xFF201A1A), onSurface = Color(0xFFEDE0E0),
        surfaceVariant = Color(0xFF534344), onSurfaceVariant = Color(0xFFD8C2C4),
        outline = Color(0xFFA08C8E), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    )
    ThemeSeed.ORANGE.seed -> darkColorScheme(
        primary = Color(0xFFFFB595), onPrimary = Color(0xFF5B1D00),
        primaryContainer = Color(0xFF823300), onPrimaryContainer = Color(0xFFFFDBCD),
        secondary = Color(0xFFE7BEA8), onSecondary = Color(0xFF452B1C),
        secondaryContainer = Color(0xFF5E4130), onSecondaryContainer = Color(0xFFFFDBCD),
        tertiary = Color(0xFFD6C68C), onTertiary = Color(0xFF3A2F04),
        tertiaryContainer = Color(0xFF534719), onTertiaryContainer = Color(0xFFF7E2A5),
        background = Color(0xFF201A17), onBackground = Color(0xFFEDE0DB),
        surface = Color(0xFF201A17), onSurface = Color(0xFFEDE0DB),
        surfaceVariant = Color(0xFF53433C), onSurfaceVariant = Color(0xFFD8C2B5),
        outline = Color(0xFFA08C83), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    )
    ThemeSeed.PURPLE.seed -> darkColorScheme(
        primary = Color(0xFFF0B0FF), onPrimary = Color(0xFF450060),
        primaryContainer = Color(0xFF630089), onPrimaryContainer = Color(0xFFF8D7FF),
        secondary = Color(0xFFD7BED8), onSecondary = Color(0xFF3B2B3E),
        secondaryContainer = Color(0xFF534155), onSecondaryContainer = Color(0xFFF4DBF4),
        tertiary = Color(0xFFF7B7AE), onTertiary = Color(0xFF4C2522),
        tertiaryContainer = Color(0xFF693B37), onTertiaryContainer = Color(0xFFFFDAD5),
        background = Color(0xFF1E1A1D), onBackground = Color(0xFFE9DFE5),
        surface = Color(0xFF1E1A1D), onSurface = Color(0xFFE9DFE5),
        surfaceVariant = Color(0xFF4E444C), onSurfaceVariant = Color(0xFFD1C3CE),
        outline = Color(0xFF9A8E97), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    )
    ThemeSeed.BROWN.seed -> darkColorScheme(
        primary = Color(0xFFFFB595), onPrimary = Color(0xFF451E10),
        primaryContainer = Color(0xFF643422), onPrimaryContainer = Color(0xFFFFDBCB),
        secondary = Color(0xFFE7BEA8), onSecondary = Color(0xFF3E2A1D),
        secondaryContainer = Color(0xFF564033), onSecondaryContainer = Color(0xFFF9DBCB),
        tertiary = Color(0xFFC2C99A), onTertiary = Color(0xFF2A3210),
        tertiaryContainer = Color(0xFF414925), onTertiaryContainer = Color(0xFFDEE5B5),
        background = Color(0xFF201A17), onBackground = Color(0xFFEDE0DB),
        surface = Color(0xFF201A17), onSurface = Color(0xFFEDE0DB),
        surfaceVariant = Color(0xFF53433C), onSurfaceVariant = Color(0xFFD8C2B5),
        outline = Color(0xFFA08C83), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    )
    ThemeSeed.DEEP_GREEN.seed -> darkColorScheme(
        primary = Color(0xFF98D898), onPrimary = Color(0xFF00390C),
        primaryContainer = Color(0xFF1B5A21), onPrimaryContainer = Color(0xFFB3F5B0),
        secondary = Color(0xFFB8CCB1), onSecondary = Color(0xFF233422),
        secondaryContainer = Color(0xFF394B37), onSecondaryContainer = Color(0xFFD3E8CC),
        tertiary = Color(0xFFA0CFD6), onTertiary = Color(0xFF00363B),
        tertiaryContainer = Color(0xFF1F4D52), onTertiaryContainer = Color(0xFFBCEBF1),
        background = Color(0xFF1A1C18), onBackground = Color(0xFFE2E3DC),
        surface = Color(0xFF1A1C18), onSurface = Color(0xFFE2E3DC),
        surfaceVariant = Color(0xFF42493F), onSurfaceVariant = Color(0xFFC2C9BD),
        outline = Color(0xFF8C9388), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    )
    ThemeSeed.SKY.seed -> darkColorScheme(
        primary = Color(0xFF9ACCFF), onPrimary = Color(0xFF00324F),
        primaryContainer = Color(0xFF004971), onPrimaryContainer = Color(0xFFCFE5FF),
        secondary = Color(0xFFB9C7DA), onSecondary = Color(0xFF233140),
        secondaryContainer = Color(0xFF394858), onSecondaryContainer = Color(0xFFD5E3F7),
        tertiary = Color(0xFFCEBDF0), onTertiary = Color(0xFF372857),
        tertiaryContainer = Color(0xFF4F3F70), onTertiaryContainer = Color(0xFFE9DDFF),
        background = Color(0xFF1A1C1E), onBackground = Color(0xFFE2E2E6),
        surface = Color(0xFF1A1C1E), onSurface = Color(0xFFE2E2E6),
        surfaceVariant = Color(0xFF42474E), onSurfaceVariant = Color(0xFFC2C7CF),
        outline = Color(0xFF8C9199), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    )
    else -> darkColorScheme()
}

fun getLightColorScheme(seedId: String) =
    lightSchemeFromSeed(ThemeSeed.values().firstOrNull { it.id == seedId }?.seed ?: ThemeSeed.TEAL.seed)

fun getDarkColorScheme(seedId: String) =
    darkSchemeFromSeed(ThemeSeed.values().firstOrNull { it.id == seedId }?.seed ?: ThemeSeed.TEAL.seed)
