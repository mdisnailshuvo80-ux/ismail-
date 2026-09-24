package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEngine
import com.example.model.AspectRatio
import com.example.model.ClipTransition
import com.example.model.SoundType
import com.example.model.VideoClip
import com.example.model.VideoFilter
import com.example.ui.theme.CapCutCyan
import com.example.ui.theme.CapCutPink
import com.example.ui.theme.CapCutSurface
import com.example.ui.theme.CapCutSurfaceHighlight
import com.example.ui.theme.CapCutSurfaceVariant
import com.example.ui.theme.CapCutTextPrimary
import com.example.ui.theme.CapCutTextSecondary
import com.example.ui.theme.CapCutYellow

@Composable
fun ToolDrawers(
    activeToolMode: ToolMode,
    activeClip: VideoClip?,
    onCloseDrawer: () -> Unit,
    onFilterChanged: (VideoFilter) -> Unit,
    onSpeedChanged: (Float) -> Unit,
    onTransitionChanged: (ClipTransition) -> Unit,
    onAspectRatioChanged: (AspectRatio) -> Unit,
    onAddAudio: (name: String, category: String, soundType: SoundType) -> Unit,
    onAddText: (text: String, color: Long, bg: Long, sizeSp: Int, style: String) -> Unit,
    onAddSticker: (emoji: String) -> Unit,
    onTrimChanged: (startMs: Long, endMs: Long) -> Unit,
    onCropChanged: (scale: Float, offsetX: Float, offsetY: Float, preset: String) -> Unit = { _, _, _, _ -> },
    onRequestMicPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeToolMode == ToolMode.NONE) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CapCutSurfaceVariant)
            .border(width = 1.dp, color = Color(0xFF232A3B))
            .padding(12.dp)
    ) {
        // Drawer header with title and close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (activeToolMode) {
                    ToolMode.FILTERS -> "✨ ভিডিও ফিল্টার ও কালার গ্রেডিং (Video Filters)"
                    ToolMode.SPEED -> "⚡ ক্লিপ প্লেব্যাক স্পিড (Playback Speed: 0.2x - 3.0x)"
                    ToolMode.AUDIO -> "🎵 মিউজিক ও সাউন্ড ইফেক্টস (Sound FX & Music Library)"
                    ToolMode.TEXT -> "✍️ টেক্সট ও সাবটাইটেল যোগ করুন (Dynamic Text Overlay)"
                    ToolMode.STICKERS -> "🎨 ট্রেন্ডিং স্টিকার ও ইমোজি (Stickers & Emojis)"
                    ToolMode.TRANSITIONS -> "🔄 ক্লিপ ট্রানজিশন ইফেক্টস (Transitions)"
                    ToolMode.ASPECT_RATIO -> "📐 ক্যানভাস রেশিও: ৯:১৬ ও ১৬:৯ (Canvas Aspect Ratio)"
                    ToolMode.TRIM -> "✂️ ভিডিও ট্রিম ইন/আউট পয়েন্ট (Trim Video Points)"
                    ToolMode.CROP -> "📐 ভিডিও ক্রপ ও জুম স্কেল (Crop & Zoom Scale)"
                    else -> ""
                },
                color = CapCutTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = CapCutTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (activeToolMode) {
            ToolMode.FILTERS -> {
                FiltersSubDrawer(
                    currentFilter = activeClip?.filter ?: VideoFilter.NORMAL,
                    onSelect = onFilterChanged
                )
            }
            ToolMode.SPEED -> {
                SpeedSubDrawer(
                    currentSpeed = activeClip?.speed ?: 1.0f,
                    onSpeedSelected = onSpeedChanged
                )
            }
            ToolMode.AUDIO -> {
                AudioSubDrawer(
                    onAddAudio = onAddAudio,
                    onRequestMic = onRequestMicPermission
                )
            }
            ToolMode.TEXT -> {
                TextSubDrawer(
                    onAddText = { text, col, bg, sz, style ->
                        onAddText(text, col, bg, sz, style)
                        onCloseDrawer()
                    }
                )
            }
            ToolMode.STICKERS -> {
                StickersSubDrawer(
                    onSelectSticker = { emoji ->
                        onAddSticker(emoji)
                        onCloseDrawer()
                    }
                )
            }
            ToolMode.TRANSITIONS -> {
                TransitionsSubDrawer(
                    currentTransition = activeClip?.transition ?: ClipTransition.NONE,
                    onSelect = onTransitionChanged
                )
            }
            ToolMode.ASPECT_RATIO -> {
                AspectRatioSubDrawer(
                    onSelect = onAspectRatioChanged
                )
            }
            ToolMode.TRIM -> {
                TrimSubDrawer(
                    clip = activeClip,
                    onTrim = onTrimChanged
                )
            }
            ToolMode.CROP -> {
                CropSubDrawer(
                    clip = activeClip,
                    onCrop = onCropChanged
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun FiltersSubDrawer(
    currentFilter: VideoFilter,
    onSelect: (VideoFilter) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VideoFilter.values().forEach { filter ->
            val isSelected = filter == currentFilter
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) CapCutSurfaceHighlight else CapCutSurface)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) CapCutCyan else Color(0xFF2C3446),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        SoundEngine.playSound(SoundType.POP, 0.4f)
                        onSelect(filter)
                    }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (filter) {
                                VideoFilter.NORMAL -> Color(0xFF3B82F6)
                                VideoFilter.VINTAGE -> Color(0xFFD97706)
                                VideoFilter.CYBERPUNK -> Color(0xFF00E5FF)
                                VideoFilter.NOIR -> Color(0xFF6B7280)
                                VideoFilter.WARM -> Color(0xFFF59E0B)
                                VideoFilter.SUNSET -> Color(0xFFEC4899)
                                VideoFilter.GLITCH -> Color(0xFFEF4444)
                                VideoFilter.VIVID -> Color(0xFF10B981)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = filter.displayName,
                    color = if (isSelected) CapCutCyan else CapCutTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun SpeedSubDrawer(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit
) {
    val presets = listOf(0.2f, 0.5f, 0.8f, 1.0f, 1.2f, 1.5f, 2.0f, 3.0f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        presets.forEach { speed ->
            val isSelected = (currentSpeed == speed)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) CapCutCyan else CapCutSurfaceHighlight)
                    .clickable {
                        SoundEngine.playSound(SoundType.POP, 0.4f)
                        onSpeedSelected(speed)
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${speed}x",
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AudioSubDrawer(
    onAddAudio: (String, String, SoundType) -> Unit,
    onRequestMic: () -> Unit
) {
    val soundPresets = listOf(
        Triple("Whoosh Swipe", "Sound FX", SoundType.WHOOSH),
        Triple("Pop Bubble", "Sound FX", SoundType.POP),
        Triple("808 Beat Drop", "BGM", SoundType.BEAT_DROP),
        Triple("Cinematic Boom", "Sound FX", SoundType.CINEMATIC_BOOM),
        Triple("Success Bell", "Sound FX", SoundType.DING),
        Triple("Cheer & Claps", "Sound FX", SoundType.APPLAUSE),
        Triple("Chill Lofi Groove", "BGM", SoundType.CHILL_LOFI),
        Triple("Trap Beat 140", "BGM", SoundType.TRAP_GROOVE)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRequestMic,
                colors = ButtonDefaults.buttonColors(containerColor = CapCutPink),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Record Voiceover", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Preset Sounds & BGM (Tap to preview & add)",
            color = CapCutTextSecondary,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            soundPresets.forEach { (name, category, soundType) ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CapCutSurfaceHighlight)
                        .clickable {
                            SoundEngine.playSound(soundType, 0.9f)
                            onAddAudio(name, category, soundType)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = CapCutCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TextSubDrawer(
    onAddText: (text: String, color: Long, bg: Long, sizeSp: Int, style: String) -> Unit
) {
    var inputText by remember { mutableStateOf("ইসমাইল ক্যাপকাট 🎬") }
    var selectedColor by remember { mutableLongStateOf(0xFFFFFFFF) }
    var selectedBg by remember { mutableLongStateOf(0xAA000000) }
    var fontSize by remember { mutableIntStateOf(24) }
    var selectedStyle by remember { mutableStateOf("CapCut Bold") }

    val colors = listOf(
        0xFFFFFFFF, 0xFF00E5FF, 0xFFFF2A85, 0xFFFFD166, 0xFF10B981, 0xFF8B5CF6, 0xFFEF4444
    )
    val fontStyles = listOf("CapCut Bold", "Neon Glow", "Minimal Clean", "Retro Subtitle")

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("Enter title or caption...", color = CapCutTextSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CapCutSurface,
                unfocusedContainerColor = CapCutSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = CapCutCyan
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colors row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                colors.forEach { col ->
                    val isSelected = (selectedColor == col)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(col))
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = col }
                    )
                }
            }

            // Add text button
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        SoundEngine.playSound(SoundType.POP, 0.5f)
                        onAddText(inputText, selectedColor, selectedBg, fontSize, selectedStyle)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CapCutCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add Overlay", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StickersSubDrawer(
    onSelectSticker: (String) -> Unit
) {
    val emojis = listOf(
        "🔥", "✨", "🎬", "❤️", "⚡", "⭐", "🎉", "💯", "🚀", "💥", "👏", "👑", "👀", "🥳", "💎"
    )

    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        emojis.forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CapCutSurfaceHighlight)
                    .clickable {
                        SoundEngine.playSound(SoundType.POP, 0.4f)
                        onSelectSticker(emoji)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 24.sp)
            }
        }
    }
}

@Composable
private fun TransitionsSubDrawer(
    currentTransition: ClipTransition,
    onSelect: (ClipTransition) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ClipTransition.values().forEach { trans ->
            val isSelected = trans == currentTransition
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) CapCutCyan else CapCutSurfaceHighlight)
                    .clickable {
                        SoundEngine.playSound(SoundType.WHOOSH, 0.5f)
                        onSelect(trans)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = trans.displayName,
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AspectRatioSubDrawer(
    onSelect: (AspectRatio) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AspectRatio.values().forEach { ratio ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CapCutSurfaceHighlight)
                    .clickable {
                        SoundEngine.playSound(SoundType.POP, 0.4f)
                        onSelect(ratio)
                    }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = ratio.label, color = CapCutCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = ratio.subtitle, color = CapCutTextSecondary, fontSize = 9.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun TrimSubDrawer(
    clip: VideoClip?,
    onTrim: (Long, Long) -> Unit
) {
    if (clip == null) return

    val totalMs = clip.durationMs.toFloat()
    var startVal by remember(clip.id, clip.trimStartMs) { mutableFloatStateOf(clip.trimStartMs.toFloat()) }
    var endVal by remember(clip.id, clip.trimEndMs) { mutableFloatStateOf(clip.trimEndMs.toFloat()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Trim Start: ${String.format("%.1fs", startVal / 1000f)}",
                color = CapCutTextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = "Trim End: ${String.format("%.1fs", endVal / 1000f)}",
                color = CapCutTextSecondary,
                fontSize = 11.sp
            )
        }

        Slider(
            value = startVal,
            onValueChange = {
                startVal = it.coerceAtMost(endVal - 500f)
                onTrim(startVal.toLong(), endVal.toLong())
            },
            valueRange = 0f..totalMs,
            colors = SliderDefaults.colors(
                thumbColor = CapCutCyan,
                activeTrackColor = CapCutCyan
            )
        )

        Slider(
            value = endVal,
            onValueChange = {
                endVal = it.coerceAtLeast(startVal + 500f)
                onTrim(startVal.toLong(), endVal.toLong())
            },
            valueRange = 0f..totalMs,
            colors = SliderDefaults.colors(
                thumbColor = CapCutPink,
                activeTrackColor = CapCutPink
            )
        )
    }
}

@Composable
private fun CropSubDrawer(
    clip: VideoClip?,
    onCrop: (scale: Float, offsetX: Float, offsetY: Float, preset: String) -> Unit
) {
    if (clip == null) return

    var scale by remember(clip.id, clip.cropScale) { mutableFloatStateOf(clip.cropScale) }
    var offsetX by remember(clip.id, clip.cropOffsetX) { mutableFloatStateOf(clip.cropOffsetX) }
    var offsetY by remember(clip.id, clip.cropOffsetY) { mutableFloatStateOf(clip.cropOffsetY) }
    var currentPreset by remember(clip.id, clip.cropPreset) { mutableStateOf(clip.cropPreset) }

    val cropPresets = listOf(
        Pair("FIT", "📐 ফিট / Fit (1.0x)"),
        Pair("FILL_9_16", "📱 ৯:১৬ রিলস (1.78x)"),
        Pair("WIDE_16_9", "📺 ১৬:৯ ওয়াইড (1.33x)"),
        Pair("SQUARE_1_1", "⏹️ ১:১ স্কয়ার (1.2x)"),
        Pair("ZOOM_2X", "🔍 ২x জুম (2.0x)")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Quick crop presets
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cropPresets.forEach { (key, label) ->
                val isSelected = (currentPreset == key)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CapCutCyan else CapCutSurfaceHighlight)
                        .clickable {
                            currentPreset = key
                            when (key) {
                                "FIT" -> {
                                    scale = 1.0f; offsetX = 0f; offsetY = 0f
                                }
                                "FILL_9_16" -> {
                                    scale = 1.78f; offsetX = 0f; offsetY = 0f
                                }
                                "WIDE_16_9" -> {
                                    scale = 1.33f; offsetX = 0f; offsetY = 0f
                                }
                                "SQUARE_1_1" -> {
                                    scale = 1.2f; offsetX = 0f; offsetY = 0f
                                }
                                "ZOOM_2X" -> {
                                    scale = 2.0f; offsetX = 0f; offsetY = 0f
                                }
                            }
                            SoundEngine.playSound(SoundType.POP, 0.4f)
                            onCrop(scale, offsetX, offsetY, key)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scale Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "স্কেল ও জুম (Scale): ${String.format("%.2fx", scale)}",
                color = CapCutTextSecondary,
                fontSize = 11.sp
            )

            Text(
                text = "রিসেট (Reset)",
                color = CapCutPink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    scale = 1.0f
                    offsetX = 0f
                    offsetY = 0f
                    currentPreset = "FIT"
                    onCrop(1.0f, 0f, 0f, "FIT")
                }
            )
        }

        Slider(
            value = scale,
            onValueChange = {
                scale = it
                currentPreset = "CUSTOM"
                onCrop(scale, offsetX, offsetY, "CUSTOM")
            },
            valueRange = 1.0f..3.0f,
            colors = SliderDefaults.colors(
                thumbColor = CapCutCyan,
                activeTrackColor = CapCutCyan
            )
        )

        // Pan X & Y adjustments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "পজিশন X (Pan X): ${offsetX.toInt()}px",
                    color = CapCutTextSecondary,
                    fontSize = 10.sp
                )
                Slider(
                    value = offsetX,
                    onValueChange = {
                        offsetX = it
                        currentPreset = "CUSTOM"
                        onCrop(scale, offsetX, offsetY, "CUSTOM")
                    },
                    valueRange = -150f..150f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = CapCutSurfaceHighlight
                    )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "পজিশন Y (Pan Y): ${offsetY.toInt()}px",
                    color = CapCutTextSecondary,
                    fontSize = 10.sp
                )
                Slider(
                    value = offsetY,
                    onValueChange = {
                        offsetY = it
                        currentPreset = "CUSTOM"
                        onCrop(scale, offsetX, offsetY, "CUSTOM")
                    },
                    valueRange = -150f..150f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = CapCutSurfaceHighlight
                    )
                )
            }
        }
    }
}
