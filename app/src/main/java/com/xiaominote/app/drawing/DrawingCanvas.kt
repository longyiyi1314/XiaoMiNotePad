package com.xiaominote.app.drawing

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
/**
 * The handwriting/drawing surface. Supports finger and stylus input.
 *
 * Xiaomi Smart Pen (and any Android stylus) reports [PointerType.Stylus] with
 * per-event pressure, tilt and button state. When a stylus is down we reject
 * concurrent touch input (palm rejection).
 *
 * @param paperColor background paper color
 * @param palmRejection when true, touch input is ignored while a stylus is active
 * @param onStrokesChanged callback fired whenever the committed stroke list changes
 */
@Composable
fun DrawingCanvas(
    state: DrawingState,
    modifier: Modifier = Modifier,
    paperColor: Color = Color.White,
    backgroundImagePath: String? = null,
    palmRejection: Boolean = true,
    onStrokesChanged: (List<Stroke>) -> Unit = {},
) {
    // Fire change callback whenever strokes change.
    LaunchedEffect(state.strokes.size, state.currentStroke) {
        onStrokesChanged(state.strokes)
    }

    var backgroundBitmap by remember(backgroundImagePath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(backgroundImagePath) {
        backgroundBitmap = backgroundImagePath?.let { path ->
            runCatching {
                BitmapFactory.decodeFile(path)?.asImageBitmap()
            }.getOrNull()
        }
    }

    val startTime = remember { System.currentTimeMillis() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(paperColor)
            .pointerInput(palmRejection) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        handlePointerEvent(
                            event = event,
                            state = state,
                            startTime = startTime,
                            palmRejection = palmRejection,
                        )
                    }
                }
            }
    ) {
        // 0. Background image (PDF page / imported photo)
        backgroundBitmap?.let { bmp ->
            val canvasW = size.width
            val canvasH = size.height
            val bmpW = bmp.width.toFloat()
            val bmpH = bmp.height.toFloat()
            val scale = minOf(canvasW / bmpW, canvasH / bmpH)
            val drawW = bmpW * scale
            val drawH = bmpH * scale
            val left = (canvasW - drawW) / 2
            val top = (canvasH - drawH) / 2
            translate(left, top) {
                drawImage(
                    image = bmp,
                    dstSize = IntSize(drawW.toInt(), drawH.toInt()),
                )
            }
        }

        // 1. Committed strokes: highlighters first (behind), then the rest.
        val committed = state.strokes
        committed
            .filter { it.config.type == PenType.HIGHLIGHTER }
            .forEach { drawStroke(it) }
        committed
            .filter { it.config.type != PenType.HIGHLIGHTER }
            .forEach { drawStroke(it) }

        // 2. In-progress stroke on top.
        state.currentStroke?.let { drawStroke(it) }
    }
}

private fun handlePointerEvent(
    event: PointerEvent,
    state: DrawingState,
    startTime: Long,
    palmRejection: Boolean,
) {
    val changes = event.changes
    if (changes.isEmpty()) return

    val stylusChanges = changes.filter { it.type == PointerType.Stylus }
    val touchChanges = changes.filter { it.type == PointerType.Touch }

    // Palm rejection: if a stylus is currently active, drop touch.
    val active = if (palmRejection && stylusChanges.isNotEmpty()) stylusChanges else stylusChanges + touchChanges
    if (active.isEmpty()) return

    for (change in active) {
        val position: Offset = change.position
        // pressure is 0..1; clamp to a sane minimum so strokes are visible.
        val pressure = change.pressure.coerceIn(0.05f, 1f).let {
            // Some digitisers report 1.0 for finger; stylus reports real pressure.
            if (change.type == PointerType.Touch) 0.6f else it
        }

        when {
            change.pressed -> {
                if (state.currentStroke == null) {
                    state.beginStroke(position, pressure, startTime)
                } else {
                    state.appendPoint(position, pressure, startTime)
                }
            }
            else -> {
                if (state.currentStroke != null) {
                    state.appendPoint(position, pressure, startTime)
                    state.endStroke()
                }
            }
        }
        change.consume()
    }
}
