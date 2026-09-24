package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AspectRatio
import com.example.model.ClipTransition
import com.example.model.Project
import com.example.model.StickerOverlay
import com.example.model.TextOverlay
import com.example.model.VideoClip
import com.example.model.VideoFilter
import com.example.ui.theme.CapCutCyan
import com.example.ui.theme.CapCutPink
import com.example.ui.theme.CapCutRedPlayhead
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun VideoPlayerPreview(
    project: Project,
    currentPlayheadMs: Long,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    activeToolMode: ToolMode = ToolMode.NONE,
    onUpdateTextPosition: (String, Float, Float) -> Unit = { _, _, _ -> },
    onUpdateStickerPosition: (String, Float, Float) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    // Determine active clip and progress within it
    var accumulatedMs = 0L
    var activeClip: VideoClip? = null
    var clipLocalProgress = 0f
    var isNearTransition = false
    var currentTransition = ClipTransition.NONE

    for (clip in project.clips) {
        val clipDuration = clip.effectiveDurationMs
        if (currentPlayheadMs >= accumulatedMs && currentPlayheadMs < accumulatedMs + clipDuration) {
            activeClip = clip
            val elapsedInClip = currentPlayheadMs - accumulatedMs
            clipLocalProgress = (elapsedInClip.toFloat() / clipDuration.toFloat()).coerceIn(0f, 1f)
            // check if near start of clip for transition
            if (elapsedInClip < 400 && clip.transition != ClipTransition.NONE) {
                isNearTransition = true
                currentTransition = clip.transition
            }
            break
        }
        accumulatedMs += clipDuration
    }

    if (activeClip == null) {
        activeClip = project.clips.lastOrNull()
        clipLocalProgress = 1f
    }

    // Active text and sticker overlays at current playhead
    val activeTexts = project.textOverlays.filter {
        currentPlayheadMs >= it.startMs && currentPlayheadMs <= (it.startMs + it.durationMs)
    }
    val activeStickers = project.stickers.filter {
        currentPlayheadMs >= it.startMs && currentPlayheadMs <= (it.startMs + it.durationMs)
    }

    // Infinite animation for subtle cinematic camera motion & glow
    val transition = rememberInfiniteTransition(label = "player_motion")
    val pulseAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF07090D)),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val ratio = project.aspectRatio.ratio
            val canvasModifier = Modifier
                .aspectRatio(ratio)
                .widthIn(max = 420.dp)
                .clip(RoundedCornerShape(12.dp))
                .clipToBounds()
                .clickable { onTogglePlay() }

            Box(modifier = canvasModifier) {
                if (activeClip != null) {
                    val clip = activeClip
                    val cropScale = clip.cropScale
                    val cropOffsetX = clip.cropOffsetX
                    val cropOffsetY = clip.cropOffsetY

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = cropScale,
                                scaleY = cropScale,
                                translationX = cropOffsetX,
                                translationY = cropOffsetY
                            )
                    ) {
                        if (clip.uri.isNotEmpty()) {
                            // User imported media via photo/video picker
                            AsyncImage(
                                model = clip.uri,
                                contentDescription = clip.title,
                                modifier = Modifier.fillMaxSize(),
                                colorFilter = clip.filter.colorMatrix?.let { ColorFilter.colorMatrix(ColorMatrix(it)) }
                            )
                        } else {
                            // Procedural Motion Canvas (Dynamic CapCut Motion Engine)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val startCol = Color(clip.startGradientColor)
                                val endCol = Color(clip.endGradientColor)

                                // Dynamic pan & zoom simulation
                                val zoomScale = 1.0f + 0.15f * sin((clipLocalProgress * Math.PI).toFloat())
                                val panOffsetX = (clipLocalProgress - 0.5f) * (w * 0.2f)

                                val brush = Brush.radialGradient(
                                    colors = listOf(startCol, endCol, Color(0xFF090B10)),
                                    center = Offset(w / 2f + panOffsetX, h / 2f),
                                    radius = (w.coerceAtLeast(h) * 0.8f) * zoomScale
                                )
                                drawRect(brush = brush)

                                // Dynamic particles / grid glow for motion
                                val particleCount = 12
                                for (i in 0 until particleCount) {
                                    val pProgress = (clipLocalProgress + (i.toFloat() / particleCount)) % 1f
                                    val px = (w * 0.1f) + (w * 0.8f) * ((i * 73) % 100 / 100f)
                                    val py = h * (1f - pProgress)
                                    val pRadius = 3f + 4f * sin((pProgress * Math.PI).toFloat())
                                    drawCircle(
                                        color = Color.White.copy(alpha = (0.25f + 0.4f * sin(pProgress * 3.14f)).coerceIn(0f, 0.8f)),
                                        radius = pRadius,
                                        center = Offset(px, py)
                                    )
                                }
                            }
                        }

                        // Apply active VideoFilter tint / overlay if any
                        when (clip.filter) {
                            VideoFilter.VINTAGE -> {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0x33D97706)))
                            }
                            VideoFilter.CYBERPUNK -> {
                                Box(modifier = Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(listOf(Color(0x3300E5FF), Color(0x33FF007A)))
                                ))
                            }
                            VideoFilter.NOIR -> {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0x44000000)))
                            }
                            VideoFilter.WARM -> {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0x2EF59E0B)))
                            }
                            VideoFilter.SUNSET -> {
                                Box(modifier = Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(listOf(Color(0x33F43F5E), Color(0x33FB923C)))
                                ))
                            }
                            VideoFilter.GLITCH -> {
                                val glitchAlpha = if (pulseAnim > 0.6f) 0.35f else 0.05f
                                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFF0055).copy(alpha = glitchAlpha)))
                            }
                            else -> {}
                        }

                        // Transition overlay animation
                        if (isNearTransition) {
                            when (currentTransition) {
                                ClipTransition.FLASH_WHITE -> {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.7f)))
                                }
                                ClipTransition.FADE_BLACK -> {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
                                }
                                ClipTransition.ZOOM_IN -> {
                                    Box(modifier = Modifier.fillMaxSize().background(Color(0x4400E5FF)))
                                }
                                else -> {}
                            }
                        }
                    }
                }

                // Crop grid overlay if in crop mode
                if (activeToolMode == ToolMode.CROP) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        // Rule of thirds lines
                        drawLine(Color.White.copy(alpha = 0.45f), Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1.5f)
                        drawLine(Color.White.copy(alpha = 0.45f), Offset(w * 2f / 3f, 0f), Offset(w * 2f / 3f, h), strokeWidth = 1.5f)
                        drawLine(Color.White.copy(alpha = 0.45f), Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1.5f)
                        drawLine(Color.White.copy(alpha = 0.45f), Offset(0f, h * 2f / 3f), Offset(w, h * 2f / 3f), strokeWidth = 1.5f)
                        // Outer border cyan highlight
                        drawRect(Color(0xFF00E5FF), style = Stroke(width = 3f))
                    }
                }

                // Render Active Stickers with interactive repositioning
                activeStickers.forEach { sticker ->
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val maxW = maxWidth.value
                        val maxH = maxHeight.value
                        var posX by remember(sticker.id, sticker.xPercent) { mutableStateOf(sticker.xPercent) }
                        var posY by remember(sticker.id, sticker.yPercent) { mutableStateOf(sticker.yPercent) }

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = ((posX * maxW) - 24).roundToInt(),
                                        y = ((posY * maxH) - 24).roundToInt()
                                    )
                                }
                                .pointerInput(sticker.id) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        posX = (posX + dragAmount.x / maxW).coerceIn(0.05f, 0.95f)
                                        posY = (posY + dragAmount.y / maxH).coerceIn(0.05f, 0.95f)
                                        onUpdateStickerPosition(sticker.id, posX, posY)
                                    }
                                }
                        ) {
                            Text(
                                text = sticker.emoji,
                                fontSize = (32 * sticker.scale).sp
                            )
                        }
                    }
                }

                // Render Active Text Overlays with interactive repositioning & styles
                activeTexts.forEach { textOverlay ->
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val maxW = maxWidth.value
                        val maxH = maxHeight.value
                        var posX by remember(textOverlay.id, textOverlay.xPercent) { mutableStateOf(textOverlay.xPercent) }
                        var posY by remember(textOverlay.id, textOverlay.yPercent) { mutableStateOf(textOverlay.yPercent) }

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = ((posX * maxW) - 80).roundToInt().coerceAtLeast(0),
                                        y = ((posY * maxH) - 20).roundToInt().coerceAtLeast(0)
                                    )
                                }
                                .pointerInput(textOverlay.id) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        posX = (posX + dragAmount.x / maxW).coerceIn(0.1f, 0.9f)
                                        posY = (posY + dragAmount.y / maxH).coerceIn(0.1f, 0.9f)
                                        onUpdateTextPosition(textOverlay.id, posX, posY)
                                    }
                                }
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(textOverlay.bgColor))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = textOverlay.text,
                                color = Color(textOverlay.textColor),
                                fontSize = textOverlay.fontSizeSp.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }

                // Center Play indicator when paused
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.70f))
                            .clickable { onTogglePlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = CapCutCyan,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Bottom timecode indicator
                val currentSec = currentPlayheadMs / 1000
                val currentDec = (currentPlayheadMs % 1000) / 100
                val totalSec = project.totalDurationMs / 1000
                val totalDec = (project.totalDurationMs % 1000) / 100
                val timecode = String.format("%02d:%02d.%d / %02d:%02d.%d",
                    currentSec / 60, currentSec % 60, currentDec,
                    totalSec / 60, totalSec % 60, totalDec
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = timecode,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Active filter pill on top right
                if (activeClip?.filter != VideoFilter.NORMAL) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CapCutCyan.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activeClip?.filter?.displayName ?: "",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
