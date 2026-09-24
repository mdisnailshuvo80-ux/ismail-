package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.SoundEngine
import com.example.model.AspectRatio
import com.example.model.ClipTransition
import com.example.model.Project
import com.example.model.SoundType
import com.example.model.VideoClip
import com.example.model.VideoFilter
import com.example.ui.components.BottomToolbar
import com.example.ui.components.TimelineRuler
import com.example.ui.components.ToolDrawers
import com.example.ui.components.ToolMode
import com.example.ui.components.VideoPlayerPreview
import com.example.ui.theme.CapCutCyan
import com.example.ui.theme.CapCutDarkBackground
import com.example.ui.theme.CapCutPink
import com.example.ui.theme.CapCutRedPlayhead
import com.example.ui.theme.CapCutSurface
import com.example.ui.theme.CapCutSurfaceHighlight
import com.example.ui.theme.CapCutSurfaceVariant
import com.example.ui.theme.CapCutTextPrimary
import com.example.ui.theme.CapCutTextSecondary
import com.example.util.ExportResult
import com.example.util.VideoExportHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    project: Project,
    currentPlayheadMs: Long,
    isPlaying: Boolean,
    selectedClipId: String?,
    activeToolMode: ToolMode,
    onBackClicked: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onSelectClip: (String) -> Unit,
    onSelectTool: (ToolMode) -> Unit,
    onSplitClip: () -> Unit,
    onDuplicateClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onUpdateFilter: (VideoFilter) -> Unit,
    onUpdateSpeed: (Float) -> Unit,
    onUpdateTransition: (ClipTransition) -> Unit,
    onUpdateTrim: (Long, Long) -> Unit,
    onUpdateCrop: (scale: Float, offsetX: Float, offsetY: Float, preset: String) -> Unit = { _, _, _, _ -> },
    onAddAudio: (String, String, SoundType) -> Unit,
    onAddText: (String, Long, Long, Int, String) -> Unit,
    onUpdateTextPosition: (String, Float, Float) -> Unit,
    onAddSticker: (String) -> Unit,
    onUpdateStickerPosition: (String, Float, Float) -> Unit,
    onChangeAspectRatio: (AspectRatio) -> Unit,
    onAddClips: (List<String>) -> Unit,
    onOpenRecordCamera: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExportComplete: (Project, String, String, ExportResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showTitleDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0f) }

    // Media picker launcher for adding more clips
    val addMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onAddClips(uris.map { it.toString() })
        }
    }

    // Mic runtime permission for voiceover
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Voiceover Recording Started", Toast.LENGTH_SHORT).show()
            onAddAudio("Voiceover Clip", "Voiceover", SoundType.VOICEOVER)
        } else {
            Toast.makeText(context, "Microphone permission required for voiceover", Toast.LENGTH_SHORT).show()
        }
    }

    val activeClip = project.clips.find { it.id == selectedClipId } ?: project.clips.firstOrNull()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CapCutDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showTitleDialog = true }
                    ) {
                        Text(
                            text = project.title,
                            color = CapCutTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Title",
                            tint = CapCutTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Aspect ratio badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CapCutSurfaceHighlight)
                            .clickable { onSelectTool(ToolMode.ASPECT_RATIO) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = project.aspectRatio.label,
                            color = CapCutCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Undo
                    IconButton(
                        onClick = onUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = onRedo,
                        modifier = Modifier.testTag("redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Export Button
                    Button(
                        onClick = { showExportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CapCutCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .padding(end = 8.dp)
                            .testTag("export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Export",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CapCutDarkBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Video Player Preview Box (Adaptive Aspect Ratio)
            VideoPlayerPreview(
                project = project,
                currentPlayheadMs = currentPlayheadMs,
                isPlaying = isPlaying,
                onTogglePlay = onTogglePlay,
                activeToolMode = activeToolMode,
                onUpdateTextPosition = onUpdateTextPosition,
                onUpdateStickerPosition = onUpdateStickerPosition,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // 2. Playback Controller bar (Play/Pause, Step, Current Time)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CapCutSurface)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CapCutCyan)
                            .testTag("play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val curSec = currentPlayheadMs / 1000
                    val curDec = (currentPlayheadMs % 1000) / 100
                    Text(
                        text = String.format("%02d:%02d.%d", curSec / 60, curSec % 60, curDec),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Split shortcut button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CapCutSurfaceHighlight)
                            .clickable { onSplitClip() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("quick_split_button")
                    ) {
                        Text("✂️ স্প্লিট", color = CapCutCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Trim shortcut button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeToolMode == ToolMode.TRIM) CapCutCyan else CapCutSurfaceHighlight)
                            .clickable {
                                onSelectTool(if (activeToolMode == ToolMode.TRIM) ToolMode.NONE else ToolMode.TRIM)
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("quick_trim_button")
                    ) {
                        Text("✂️ ট্রিম", color = if (activeToolMode == ToolMode.TRIM) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Delete shortcut button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CapCutSurfaceHighlight)
                            .clickable { onDeleteClip() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("quick_delete_button")
                    ) {
                        Text("🗑️ ডিলিট", color = CapCutRedPlayhead, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Text shortcut button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeToolMode == ToolMode.TEXT) CapCutPink else CapCutSurfaceHighlight)
                            .clickable {
                                onSelectTool(if (activeToolMode == ToolMode.TEXT) ToolMode.NONE else ToolMode.TEXT)
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("quick_text_button")
                    ) {
                        Text("🔤 টেক্সট", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Music shortcut button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeToolMode == ToolMode.AUDIO) CapCutCyan else CapCutSurfaceHighlight)
                            .clickable {
                                onSelectTool(if (activeToolMode == ToolMode.AUDIO) ToolMode.NONE else ToolMode.AUDIO)
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("quick_music_button")
                    ) {
                        Text("🎵 মিউজিক", color = if (activeToolMode == ToolMode.AUDIO) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Filter shortcut button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeToolMode == ToolMode.FILTERS) CapCutCyan else CapCutSurfaceHighlight)
                            .clickable {
                                onSelectTool(if (activeToolMode == ToolMode.FILTERS) ToolMode.NONE else ToolMode.FILTERS)
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("quick_filter_button")
                    ) {
                        Text("🎨 ফিল্টার", color = if (activeToolMode == ToolMode.FILTERS) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Speed shortcut button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeToolMode == ToolMode.SPEED) CapCutCyan else CapCutSurfaceHighlight)
                            .clickable {
                                onSelectTool(if (activeToolMode == ToolMode.SPEED) ToolMode.NONE else ToolMode.SPEED)
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("quick_speed_button")
                    ) {
                        Text("⚡ স্পিড", color = if (activeToolMode == ToolMode.SPEED) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Crop shortcut button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeToolMode == ToolMode.CROP) CapCutPink else CapCutSurfaceHighlight)
                            .clickable {
                                onSelectTool(if (activeToolMode == ToolMode.CROP) ToolMode.NONE else ToolMode.CROP)
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("quick_crop_button")
                    ) {
                        Text("📐 ক্রপ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Camera Record shortcut
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CapCutSurfaceHighlight)
                            .clickable { onOpenRecordCamera() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text("🎥 ক্যামেরা", color = CapCutRedPlayhead, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Add Media Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CapCutSurfaceHighlight)
                            .clickable {
                                addMediaPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text("+ মিডিয়া", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Quick Export button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CapCutCyan)
                            .clickable { showExportDialog = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("📥 এক্সপোর্ট", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. Multi-track Timeline
            TimelineRuler(
                project = project,
                currentPlayheadMs = currentPlayheadMs,
                selectedClipId = selectedClipId,
                onSelectClip = onSelectClip,
                onSeek = onSeek,
                onAddClipClicked = {
                    addMediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                onOpenTransitionPicker = { clipId ->
                    onSelectClip(clipId)
                    onSelectTool(ToolMode.TRANSITIONS)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            )

            // 4. Sub-Tool Drawers (Filters, Audio, Speed, Stickers, Text, Crop, etc.)
            ToolDrawers(
                activeToolMode = activeToolMode,
                activeClip = activeClip,
                onCloseDrawer = { onSelectTool(ToolMode.NONE) },
                onFilterChanged = onUpdateFilter,
                onSpeedChanged = onUpdateSpeed,
                onTransitionChanged = onUpdateTransition,
                onAspectRatioChanged = onChangeAspectRatio,
                onAddAudio = onAddAudio,
                onAddText = onAddText,
                onAddSticker = onAddSticker,
                onTrimChanged = onUpdateTrim,
                onCropChanged = onUpdateCrop,
                onRequestMicPermission = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        Toast.makeText(context, "ভয়েস রেকর্ড শুরু হয়েছে", Toast.LENGTH_SHORT).show()
                        onAddAudio("ভয়েস রেকর্ড", "Voiceover", SoundType.VOICEOVER)
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )

            // 5. Main Bottom Tool Bar (CapCut style)
            BottomToolbar(
                activeToolMode = activeToolMode,
                onSelectTool = onSelectTool,
                onSplitClip = onSplitClip,
                onDuplicateClip = onDuplicateClip,
                onDeleteClip = onDeleteClip,
                onExportClick = { showExportDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Title editing dialog
    if (showTitleDialog) {
        var newTitle by remember { mutableStateOf(project.title) }
        AlertDialog(
            onDismissRequest = { showTitleDialog = false },
            containerColor = CapCutSurface,
            title = { Text("Rename Project", color = CapCutTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CapCutSurfaceVariant,
                        unfocusedContainerColor = CapCutSurfaceVariant,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CapCutCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            onUpdateTitle(newTitle)
                        }
                        showTitleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CapCutCyan)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTitleDialog = false }) {
                    Text("Cancel", color = CapCutTextSecondary)
                }
            }
        )
    }

    // Export Dialog & Progress
    if (showExportDialog) {
        var exportRes by remember { mutableStateOf("1080p Full HD") }
        var exportFps by remember { mutableStateOf("60 fps") }

        AlertDialog(
            onDismissRequest = { if (!isExporting) showExportDialog = false },
            containerColor = CapCutSurface,
            title = {
                Text(
                    text = if (isExporting) "ভিডিও রেন্ডার ও MP4 তৈরি হচ্ছে..." else "ভিডিও এক্সপোর্ট ও সেভ (Export MP4)",
                    color = CapCutTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    if (isExporting) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ইসমাইল ক্যাপকাট ইঞ্জিন দিয়ে ফ্রেম এনকোড হচ্ছে...",
                                color = CapCutTextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { exportProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = CapCutCyan,
                                trackColor = CapCutSurfaceHighlight
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "${(exportProgress * 100).toInt()}% সম্পন্ন",
                                color = CapCutCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text("রেজোলিউশন নির্বাচন করুন (MP4 Format):", color = CapCutTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Pair("720p HD", "720p HD\n(১২৮০×৭২০)"),
                                Pair("1080p Full HD", "1080p FHD\n(১৯২০×১০৮০)"),
                                Pair("4K Ultra", "4K Ultra\n(৩৮৪০×২১৬০)")
                            ).forEach { (resKey, resLabel) ->
                                val isSel = (exportRes == resKey)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) CapCutCyan else CapCutSurfaceHighlight)
                                        .clickable { exportRes = resKey }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = resLabel,
                                        color = if (isSel) Color.Black else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("ফ্রেমরেট (Frame Rate):", color = CapCutTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Pair("30 fps", "30 fps (সাধারণ)"),
                                Pair("60 fps", "60 fps (স্মুথ)")
                            ).forEach { (fpsKey, fpsLabel) ->
                                val isSel = (exportFps == fpsKey)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) CapCutPink else CapCutSurfaceHighlight)
                                        .clickable { exportFps = fpsKey }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = fpsLabel,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!isExporting) {
                    Button(
                        onClick = {
                            isExporting = true
                            exportProgress = 0f
                            coroutineScope.launch {
                                val result = VideoExportHelper.exportProjectToMp4(
                                    context = context,
                                    project = project,
                                    resolutionStr = exportRes,
                                    fpsStr = exportFps,
                                    onProgress = { p ->
                                        exportProgress = p
                                    }
                                )
                                SoundEngine.playSound(SoundType.DING, 0.9f)
                                isExporting = false
                                showExportDialog = false
                                onExportComplete(project, exportRes, exportFps, result)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CapCutCyan)
                    ) {
                        Text("MP4 সেভ ও এক্সপোর্ট শুরু করুন", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            dismissButton = {
                if (!isExporting) {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("বাতিল (Cancel)", color = CapCutTextSecondary)
                    }
                }
            }
        )
    }
}
