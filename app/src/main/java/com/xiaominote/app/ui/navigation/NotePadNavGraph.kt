package com.xiaominote.app.ui.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xiaominote.app.ui.screen.backup.BackupScreen
import com.xiaominote.app.ui.screen.editor.EditorScreen
import com.xiaominote.app.ui.screen.home.HomeScreen
import com.xiaominote.app.ui.screen.recyclebin.RecycleBinScreen
import com.xiaominote.app.ui.screen.settings.SettingsScreen

@Composable
fun NotePadNavGraph(
    intent: Intent?,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Home.route
    ) {
        composable(Route.Home.route) {
            HomeScreen(
                onNoteClick = { noteId ->
                    navController.navigate(Route.Editor.create(noteId))
                },
                onCreateNote = {
                    navController.navigate(Route.Editor.create(Route.Editor.NEW_NOTE_ID))
                },
                onOpenSettings = { navController.navigate(Route.Settings.route) },
                onOpenRecycleBin = { navController.navigate(Route.RecycleBin.route) },
                onOpenBackup = { navController.navigate(Route.Backup.route) },
                intent = intent,
            )
        }

        composable(
            route = Route.Editor.route,
            arguments = listOf(
                navArgument(Route.Editor.ARG_NOTE_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong(Route.Editor.ARG_NOTE_ID)
                ?: Route.Editor.NEW_NOTE_ID
            EditorScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Route.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.RecycleBin.route) {
            RecycleBinScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Backup.route) {
            BackupScreen(onBack = { navController.popBackStack() })
        }
    }
}
