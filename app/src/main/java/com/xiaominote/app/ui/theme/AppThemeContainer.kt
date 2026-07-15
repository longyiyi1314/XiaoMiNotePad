package com.xiaominote.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.xiaominote.app.data.prefs.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    settings: SettingsRepository,
) : ViewModel() {
    val darkTheme = settings.darkTheme
    val dynamicColor = settings.dynamicColor
    val themeSeed = settings.themeSeed
}

/**
 * Reads theme preferences from DataStore and applies [NotePadTheme].
 */
@Composable
fun AppThemeContainer(content: @Composable () -> Unit) {
    val vm: ThemeViewModel = hiltViewModel()
    val darkPref by vm.darkTheme.collectAsState(initial = "system")
    val dynamic by vm.dynamicColor.collectAsState(initial = true)
    val seed by vm.themeSeed.collectAsState(initial = "teal")

    val isDark = when (darkPref) {
        "light" -> false
        "dark" -> true
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    NotePadTheme(darkTheme = isDark, dynamicColor = dynamic, seedId = seed, content = content)
}
