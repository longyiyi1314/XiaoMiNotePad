package com.xiaominote.app.ui.screen.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xiaominote.app.drawing.ColorPalette
import com.xiaominote.app.drawing.DrawingCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    noteId: Long,
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    LaunchedEffect(noteId) { viewModel.load(noteId) }

    val uiState by viewModel.uiState.collectAsState()
    val penConfig = viewModel.drawingState.penConfig

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importDocument(uri, null)
    }

    // Auto-save when the committed stroke list changes.
    LaunchedEffect(viewModel.drawingState.strokes.size) {
        if (uiState.isLoaded) {
            viewModel.saveDrawing(viewModel.drawingState.strokes)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                title = {
                    Text(
                        text = uiState.title.ifBlank { "无标题笔记" },
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "收藏",
                            tint = if (uiState.isFavorite) Color(0xFFFDD835) else Color.Gray,
                        )
                    }
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = viewModel.drawingState.canUndo
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "撤销")
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = viewModel.drawingState.canRedo
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "重做")
                    }
                    IconButton(onClick = { viewModel.clearCanvas() }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "清空")
                    }
                    IconButton(onClick = {
                        importLauncher.launch(arrayOf(
                            "application/pdf",
                            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "image/*",
                        ))
                    }) {
                        Icon(Icons.Filled.FileUpload, contentDescription = "导入文件")
                    }
                    IconButton(onClick = {
                        viewModel.saveDrawing()
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                placeholder = { Text("输入笔记标题…") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textStyle = MaterialTheme.typography.titleMedium,
            )

            Box(modifier = Modifier.fillMaxSize()) {
                DrawingCanvas(
                    state = viewModel.drawingState,
                    paperColor = Color(uiState.paperColor),
                    backgroundImagePath = uiState.backgroundImagePath,
                    palmRejection = true,
                    onStrokesChanged = { /* auto-save handles persistence */ },
                )

                // Page navigation bar
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { viewModel.previousPage() },
                        enabled = viewModel.drawingState.canGoPrevious,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = "上一页", modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "${viewModel.drawingState.currentPageIndex + 1} / ${viewModel.drawingState.totalPages}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    IconButton(
                        onClick = { viewModel.nextPage() },
                        enabled = viewModel.drawingState.canGoNext,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = "下一页", modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = { viewModel.addPage() },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "添加页面", modifier = Modifier.size(20.dp))
                    }
                }

                // Floating pen toolbar at the bottom.
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    com.xiaominote.app.ui.component.PenToolbar(
                        selectedType = penConfig.type,
                        selectedColor = ColorPalette.fromArgbLong(penConfig.color),
                        penSize = penConfig.size,
                        onSelectType = viewModel::selectPenType,
                        onSelectColor = viewModel::selectColor,
                        onSizeChange = viewModel::setPenSize,
                    )
                }
            }
        }
    }
}
