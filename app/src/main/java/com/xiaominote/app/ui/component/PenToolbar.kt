package com.xiaominote.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.xiaominote.app.drawing.ColorPalette
import com.xiaominote.app.drawing.PenType

private data class PenOption(val type: PenType, val icon: ImageVector, val label: String)

private val penOptions = listOf(
    PenOption(PenType.BALLPOINT, Icons.Filled.Edit, "圆珠笔"),
    PenOption(PenType.FOUNTAIN, Icons.Filled.Create, "钢笔"),
    PenOption(PenType.BRUSH, Icons.Filled.Brush, "毛笔"),
    PenOption(PenType.MARKER, Icons.Filled.Gesture, "马克笔"),
    PenOption(PenType.HIGHLIGHTER, Icons.Filled.Highlight, "荧光笔"),
    PenOption(PenType.PENCIL, Icons.Filled.AutoFixHigh, "铅笔"),
)

@Composable
fun PenToolbar(
    selectedType: PenType,
    selectedColor: Color,
    penSize: Float,
    onSelectType: (PenType) -> Unit,
    onSelectColor: (Color) -> Unit,
    onSizeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Pen types
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            penOptions.forEach { option ->
                val selected = option.type == selectedType
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onSelectType(option.type) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.label,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        // Color palette - grid layout
        val colors = if (selectedType == PenType.HIGHLIGHTER) ColorPalette.highlightPresets else ColorPalette.presets
        val rows = (colors.size + 7) / 8
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .height((rows * 28).dp),
        ) {
            items(if (selectedType == PenType.HIGHLIGHTER) ColorPalette.highlightPresets else ColorPalette.presets) { color ->
                val isSel = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSel) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        .clickable { onSelectColor(color) }
                )
            }
        }

        // Size slider
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("粗细", style = MaterialTheme.typography.labelSmall)
            Slider(
                value = penSize,
                onValueChange = onSizeChange,
                valueRange = 1f..24f,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
