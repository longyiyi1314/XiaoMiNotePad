package com.xiaominote.app.ui.navigation

import android.content.Intent

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Settings : Route("settings")
    data object RecycleBin : Route("recycle_bin")
    data object Backup : Route("backup")
    data object FolderPicker : Route("folder_picker")

    // Editor takes a noteId argument (-1L for a new note) and optional folderId
    data object Editor : Route("editor/{noteId}") {
        const val ARG_NOTE_ID = "noteId"
        const val ARG_FOLDER_ID = "folderId"
        fun create(noteId: Long, folderId: Long? = null): String =
            if (folderId != null) "editor/$noteId?folderId=$folderId" else "editor/$noteId"
        const val NEW_NOTE_ID = -1L
    }
}
