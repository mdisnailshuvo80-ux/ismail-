package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrack
import com.example.model.ClipTransition
import com.example.model.Project
import com.example.model.StickerOverlay
import com.example.model.TextOverlay
import com.example.model.VideoClip
import com.example.ui.theme.CapCutCyan
import com.example.ui.theme.CapCutPink
import com.example.ui.theme.CapCutRedPlayhead
import com.example.ui.theme.CapCutSurface
import com.example.ui.theme.CapCutSurfaceHighlight
import com.example.ui.theme.CapCutSurfaceVariant
import com.example.ui.theme.CapCutTextMuted
import com.example.ui.theme.CapCutTextSecondary
import com.example.ui.theme.CapCutTimelineAudioBg
import com.example.ui.theme.CapCutTimelineClipBg
import com.example.ui.theme.CapCutTimelineTextBg
import com.example.ui.theme.CapCutTimelineTrack
import com.example.ui.theme.CapCutYellow
import kotlin.math.roundToInt

// Scale: 100dp represents 1000ms (1 second)
private const val DP_PER_SECOND = 80f

@Composable
fun TimelineRuler(
    project: Project,
    currentPlayheadMs: Long,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit,
    onSeek: (Long) -> Unit,
    onAddClipClicked: () -> Unit,
    onOpenTransitionPicker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDurationMs = project.totalDurationMs
    val totalSeconds = (totalDurationMs / 1000f).coerceAtLeast(5f)
    val timelineWidthDp = (totalSeconds * DP_PER_SECOND) + 120f

    val scrollState = rememberScrollState()

    // Keep playhead in view during playback
    LaunchedEffect(currentPlayheadMs) {
        val playheadDp = (currentPlayheadMs / 1000f) * DP_PER_SECOND
        val currentScrollDp = scrollState.value / 2.5f
        if (playheadDp > currentScrollDp + 240f) {
            scrollState.animateScrollTo((playheadDp * 2.5f).toInt())
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CapCutTimelineTrack)
            .border(width = 1.dp, color = Color(0xFF1E2330))
    ) {
        // Horizontal scrollable container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .width(timelineWidthDp.dp)
                .pointerInput(totalDurationMs) {
                    detectTapGestures { offset ->
                        val clickedMs = ((offset.x / (DP_PER_SECOND * density)) * 1000).toLong()
                        onSeek(clickedMs.coerceIn(0L, totalDurationMs))
                    }
                }
        ) {
            // 1. Time Ruler (00:00, 00:01, ...)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Color(0xFF10121A))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val secCount = (totalSeconds + 3).toInt()
                    val pxPerSec = DP_PER_SECOND * density

                    for (s in 0..secCount) {
                        val x = s * pxPerSec
                        // Major tick
                        drawLine(
                            color = Color(0xFF475569),
                            start = Offset(x, size.height - 12f),
                            end = Offset(x, size.height),
                            strokeWidth = 2f
                        )
                        // Minor sub-ticks (quarter seconds)
                        for (sub in 1..3) {
                            val subX = x + (sub * pxPerSec / 4f)
                            drawLine(
                                color = Color(0xFF334155),
                                start = Offset(subX, size.height - 6f),
                                end = Offset(subX, size.height),
                                strokeWidth = 1f
                            )
                        }
                    }
                }

                // Time labels
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val secCount = (totalSeconds + 3).toInt()
                    for (s in 0..secCount) {
                        Text(
                            text = String.format("%02d:%02d", s / 60, s % 60),
                            color = CapCutTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .width(DP_PER_SECOND.dp)
                                .padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Video Clips Track (Track 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                project.clips.forEachIndexed { index, clip ->
                    val clipDurationSec = clip.effectiveDurationMs / 1000f
                    val clipWidthDp = (clipDurationSec * DP_PER_SECOND).coerceAtLeast(48f)
                    val isSelected = clip.id == selectedClipId

                    Box(
                        modifier = Modifier
                            .width(clipWidthDp.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(clip.startGradientColor),
                                        Color(clip.endGradientColor)
                                    )
                                )
                            )
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp))
                                } else {
                                    Modifier.border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                }
                            )
                            .clickable { onSelectClip(clip.id) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = clip.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (clip.speed != 1.0f) {
                                    Text(
                                        text = "${clip.speed}x",
                                        color = CapCutYellow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format("%.1fs", clipDurationSec),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                                if (clip.filter != com.example.model.VideoFilter.NORMAL) {
                                    Text(
                                        text = clip.filter.displayName,
                                        color = CapCutCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Left/right trim handles when selected
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .size(width = 6.dp, height = 24.dp)
                                    .background(Color.White, RoundedCornerShape(2.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(width = 6.dp, height = 24.dp)
                                    .background(Color.White, RoundedCornerShape(2.dp))
                            )
                        }
                    }

                    // Transition button between clips
                    if (index < project.clips.size - 1) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (clip.transition != ClipTransition.NONE) CapCutCyan else CapCutSurfaceHighlight
                                )
                                .clickable { onOpenTransitionPicker(clip.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Transition",
                                tint = if (clip.transition != ClipTransition.NONE) Color.Black else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Add Media (+) Button at the end of clip track
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(height = 58.dp, width = 50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CapCutSurfaceHighlight)
                        .border(1.dp, Color(0xFF3B445B), RoundedCornerShape(8.dp))
                        .clickable { onAddClipClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Clip",
                            tint = CapCutCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Add",
                            color = CapCutCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Audio & Sound FX Track (Track 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(Color(0xFF0F141C))
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = null,
                    tint = CapCutCyan,
                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                )

                project.audioTracks.forEach { track ->
                    val startOffsetDp = (track.startMs / 1000f) * DP_PER_SECOND
                    val trackDurationSec = track.durationMs / 1000f
                    val trackWidthDp = (trackDurationSec * DP_PER_SECOND).coerceAtLeast(36f)

                    Box(
                        modifier = Modifier
                            .width(trackWidthDp.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CapCutTimelineAudioBg)
                            .border(1.dp, Color(0xFF059669), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎵 ${track.name}",
                                color = Color(0xFFA7F3D0),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 4. Text & Subtitles Track (Track 3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Color(0xFF13101C))
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TextFields,
                    contentDescription = null,
                    tint = CapCutPink,
                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                )

                project.textOverlays.forEach { textOverlay ->
                    val widthDp = ((textOverlay.durationMs / 1000f) * DP_PER_SECOND).coerceAtLeast(40f)

                    Box(
                        modifier = Modifier
                            .width(widthDp.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CapCutTimelineTextBg)
                            .border(1.dp, Color(0xFF9333EA), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "T: ${textOverlay.text}",
                            color = Color(0xFFE9D5FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 5. Stickers & Emojis Track (Track 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .background(Color(0xFF16140D))
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎨", fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))

                project.stickers.forEach { sticker ->
                    val widthDp = ((sticker.durationMs / 1000f) * DP_PER_SECOND).coerceAtLeast(32f)

                    Box(
                        modifier = Modifier
                            .width(widthDp.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF382F10))
                            .border(1.dp, Color(0xFFD97706), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = sticker.emoji,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }

        // Red Playhead Indicator Line
        val playheadOffsetDp = ((currentPlayheadMs / 1000f) * DP_PER_SECOND) - (scrollState.value / 2.5f)
        Box(
            modifier = Modifier
                .offset { IntOffset(x = (playheadOffsetDp * 2.5f).roundToInt(), y = 0) }
                .width(2.dp)
                .fillMaxHeight()
                .background(CapCutRedPlayhead)
        ) {
            // White diamond playhead scrubber head on top
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(10.dp)
                    .background(CapCutRedPlayhead, CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}
