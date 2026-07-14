package com.xiaominote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.xiaominote.app.ui.navigation.NotePadNavGraph
import com.xiaominote.app.ui.theme.AppThemeContainer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.Transparent.toArgb(), Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.auto(Color.Transparent.toArgb(), Color.Transparent.toArgb())
        )

        setContent {
            AppThemeContainer {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotePadNavGraph(
                        intent = intent,
                    )
                }
            }
        }

        // Keep splash visible until first composition is done (optional hook)
        splash.setKeepOnScreenCondition { false }
    }
}
