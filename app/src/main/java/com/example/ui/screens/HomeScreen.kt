package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AspectRatio
import com.example.model.Project
import com.example.model.VideoFilter
import com.example.ui.components.ProjectThumbnail
import com.example.ui.theme.CapCutCyan
import com.example.ui.theme.CapCutDarkBackground
import com.example.ui.theme.CapCutPink
import com.example.ui.theme.CapCutRedPlayhead
import com.example.ui.theme.CapCutSurface
import com.example.ui.theme.CapCutSurfaceHighlight
import com.example.ui.theme.CapCutSurfaceVariant
import com.example.ui.theme.CapCutTextMuted
import com.example.ui.theme.CapCutTextPrimary
import com.example.ui.theme.CapCutTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    projects: List<Project>,
    onOpenProject: (Project) -> Unit,
    onCreateNewProject: (title: String, ratio: AspectRatio, uris: List<String>) -> Unit,
    onCreateFromSample: ((title: String, ratio: AspectRatio, clipTitle: String, durationMs: Long, filter: VideoFilter, startGrad: Long, endGrad: Long, textBanner: String) -> Unit)? = null,
    onOpenRecordCamera: () -> Unit,
    onDuplicateProject: (Project) -> Unit,
    onDeleteProject: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Media picker launcher using zero-permission Android Photo Picker
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val stringUris = uris.map { it.toString() }
            onCreateNewProject("নতুন ভিডিও এডিট", AspectRatio.RATIO_9_16, stringUris)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CapCutDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎬 ইসমাইল ক্যাপকাট",
                            color = CapCutTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenRecordCamera,
                        modifier = Modifier.testTag("home_camera_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Record Video",
                            tint = CapCutCyan
                        )
                    }
                    IconButton(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier.testTag("about_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            tint = CapCutTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CapCutDarkBackground
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Banner matching user's wireframe & branded poster
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Visual Hero Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CapCutSurface)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.ismail_capcut_hero),
                                contentDescription = "Ismail CapCut Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Dark gradient scrim
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0xDD0B0C10)
                                            )
                                        )
                                    )
                            )
                            // Label overlay
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "🎬 ইসমাইল ক্যাপকাট",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Simple Video Editor • Cut • Text • Music • Speed",
                                    color = CapCutCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prominent [ ＋ New Project ] Button exactly as depicted in wireframe
                    Button(
                        onClick = { showNewProjectDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("new_project_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CapCutCyan
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Project",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "New Project",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Actions row: Record Camera, Gallery Media, 9:16 Shorts, 16:9 Cinema
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionCard(
                            title = "Camera",
                            subtitle = "Record Raw",
                            icon = Icons.Default.Videocam,
                            color = CapCutRedPlayhead,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenRecordCamera
                        )
                        QuickActionCard(
                            title = "Import",
                            subtitle = "From Device",
                            icon = Icons.Default.PhotoLibrary,
                            color = CapCutPink,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                mediaPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            }
                        )
                        QuickActionCard(
                            title = "9:16 Shorts",
                            subtitle = "TikTok/Reels",
                            icon = Icons.Default.Movie,
                            color = CapCutCyan,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onCreateNewProject("TikTok / Reels Edit", AspectRatio.RATIO_9_16, emptyList())
                            }
                        )
                        QuickActionCard(
                            title = "16:9 Wide",
                            subtitle = "YouTube",
                            icon = Icons.Default.VideoLibrary,
                            color = Color(0xFFFFD166),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onCreateNewProject("Widescreen Edit", AspectRatio.RATIO_16_9, emptyList())
                            }
                        )
                    }
                }
            }

            // "Recent Projects" Section Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Projects",
                        color = CapCutTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${projects.size} projects",
                        color = CapCutTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Projects List or Empty State
            if (projects.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CapCutSurface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = CapCutTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No recent projects yet",
                                color = CapCutTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ New Project' or 'Camera' to shoot footage!",
                                color = CapCutTextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onOpenProject(project) },
                        onDuplicate = { onDuplicateProject(project) },
                        onDelete = { onDeleteProject(project.id) }
                    )
                }
            }
        }
    }

    // New Project Customization Dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onConfirm = { title, ratio ->
                showNewProjectDialog = false
                onCreateNewProject(title, ratio, emptyList())
            },
            onConfirmSample = { title, ratio, sample ->
                showNewProjectDialog = false
                if (onCreateFromSample != null) {
                    onCreateFromSample(
                        title,
                        ratio,
                        sample.titleEnglish,
                        sample.durationMs,
                        sample.filter,
                        sample.startGradient,
                        sample.endGradient,
                        sample.textBanner
                    )
                } else {
                    onCreateNewProject(title, ratio, emptyList())
                }
            },
            onPickMedia = {
                showNewProjectDialog = false
                mediaPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            },
            onRecordCamera = {
                showNewProjectDialog = false
                onOpenRecordCamera()
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = CapCutSurface,
            title = {
                Text("🎬 ইসমাইল ক্যাপকাট (Ismail CapCut)", color = CapCutTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Simple Video Editor for Android.\n\n" +
                               "✨ Features:\n" +
                               "• Camera recording: Raw video capture with mic access\n" +
                               "• Multi-track timeline (Video, Sound FX, Text, Stickers)\n" +
                               "• Clip splitting, trimming, and speed ramping (0.2x - 3.0x)\n" +
                               "• Cinematic filters: Cyberpunk, Vintage, Noir, Sunset, Glitch\n" +
                               "• Synthesized real sound engine (808 Bass, Whoosh, Bell, Beats)\n" +
                               "• Dynamic animated text & trend emojis\n" +
                               "• 9:16 (TikTok), 16:9 (YouTube), 1:1 (Instagram) export\n" +
                               "• Local Room database auto-saving",
                        color = CapCutTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CapCutCyan)
                ) {
                    Text("OK", color = Color.Black)
                }
            }
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CapCutSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                color = CapCutTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = CapCutTextMuted,
                fontSize = 8.5.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val dateStr = remember(project.updatedAt) { dateFormat.format(Date(project.updatedAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CapCutSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            ProjectThumbnail(
                project = project,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
            )

            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.title,
                        color = CapCutTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = CapCutTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(CapCutSurfaceHighlight)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Open Project", color = Color.White) },
                                onClick = {
                                    menuExpanded = false
                                    onClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Movie, contentDescription = null, tint = CapCutCyan)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate", color = Color.White) },
                                onClick = {
                                    menuExpanded = false
                                    onDuplicate()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = CapCutRedPlayhead) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = CapCutRedPlayhead)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateStr,
                        color = CapCutTextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${project.resolution} • ${project.fps}fps",
                        color = CapCutTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

data class SampleVideoOption(
    val id: String,
    val titleBangla: String,
    val titleEnglish: String,
    val info: String,
    val durationMs: Long,
    val ratio: AspectRatio,
    val filter: VideoFilter,
    val startGradient: Long,
    val endGradient: Long,
    val textBanner: String,
    val emoji: String
)

val defaultSampleVideos = listOf(
    SampleVideoOption(
        id = "sample_1",
        titleBangla = "📱 নিয়ন সাইবারপাঙ্ক (Reel)",
        titleEnglish = "Neon Cyberpunk Video",
        info = "৩.৮ সে • ৯:১৬ • নিয়ন গ্লো ও ড্রাইভ",
        durationMs = 3800L,
        ratio = AspectRatio.RATIO_9_16,
        filter = VideoFilter.CYBERPUNK,
        startGradient = 0xFF6366F1,
        endGradient = 0xFF00E5FF,
        textBanner = "🔥 NEON CYBERPUNK 🎬",
        emoji = "⚡"
    ),
    SampleVideoOption(
        id = "sample_2",
        titleBangla = "🎬 অ্যাকশন সিনেমা ড্রপ (Wide)",
        titleEnglish = "Action Beat Drop",
        info = "৪.৫ সে • ১৬:৯ • সিনেম্যাটিক ড্রপ",
        durationMs = 4500L,
        ratio = AspectRatio.RATIO_16_9,
        filter = VideoFilter.GLITCH,
        startGradient = 0xFFFF0055,
        endGradient = 0xFFFFD166,
        textBanner = "🎬 ACTION BEAT DROP",
        emoji = "💥"
    ),
    SampleVideoOption(
        id = "sample_3",
        titleBangla = "🌅 গোল্ডেন সানসেট ভ্লগ (Shorts)",
        titleEnglish = "Golden Sunset Story",
        info = "৫.০ সে • ৯:১৬ • সানসেট ওয়ার্ম ফিল্টার",
        durationMs = 5000L,
        ratio = AspectRatio.RATIO_9_16,
        filter = VideoFilter.SUNSET,
        startGradient = 0xFFF43F5E,
        endGradient = 0xFFFB923C,
        textBanner = "🌅 GOLDEN SUNSET VIBE",
        emoji = "🌇"
    ),
    SampleVideoOption(
        id = "sample_4",
        titleBangla = "🌿 ন্যাচার ও ট্রাভেল (Post)",
        titleEnglish = "Nature & Travel Vlog",
        info = "৪.২ সে • ১:১ • ভাইব্রেন্ট ন্যাচারাল কালার",
        durationMs = 4200L,
        ratio = AspectRatio.RATIO_1_1,
        filter = VideoFilter.VIVID,
        startGradient = 0xFF059669,
        endGradient = 0xFF3B82F6,
        textBanner = "🌿 NATURE & TRAVEL",
        emoji = "🏕️"
    ),
    SampleVideoOption(
        id = "sample_5",
        titleBangla = "🎧 ট্রেন্ডিং মিউজিক বিটস (Reel)",
        titleEnglish = "Trending Beats Wave",
        info = "৩.৬ সে • ৯:১৬ • ভিন্টেজ ভাইব ও ড্রপ",
        durationMs = 3600L,
        ratio = AspectRatio.RATIO_9_16,
        filter = VideoFilter.VINTAGE,
        startGradient = 0xFF8B5CF6,
        endGradient = 0xFFEC4899,
        textBanner = "🎵 TRENDING BEATS 🎧",
        emoji = "🎧"
    )
)

@Composable
private fun NewProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, AspectRatio) -> Unit,
    onConfirmSample: (String, AspectRatio, SampleVideoOption) -> Unit,
    onPickMedia: () -> Unit,
    onRecordCamera: () -> Unit
) {
    var selectedSample by remember { mutableStateOf<SampleVideoOption?>(defaultSampleVideos[0]) }
    var title by remember { mutableStateOf("🎬 নতুন ভিডিও এডিট") }
    var selectedRatio by remember { mutableStateOf(AspectRatio.RATIO_9_16) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CapCutSurface,
        title = {
            Column {
                Text("ভিডিও নির্বাচন ও টাইমলাইনে যোগ", color = CapCutTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("টাইমলাইনে আনার জন্য ভিডিও নির্বাচন করুন:", color = CapCutCyan, fontSize = 11.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Section 1: Sample Video Cards
                Text("১. রেডিমেড ভিডিও নির্বাচন করুন (Sample Videos):", color = CapCutTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                defaultSampleVideos.forEach { sample ->
                    val isChosen = (selectedSample?.id == sample.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isChosen) CapCutSurfaceHighlight else CapCutSurfaceVariant)
                            .border(
                                width = if (isChosen) 2.dp else 1.dp,
                                color = if (isChosen) CapCutCyan else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                selectedSample = sample
                                title = sample.titleEnglish
                                selectedRatio = sample.ratio
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail preview
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(sample.startGradient), Color(sample.endGradient))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = sample.emoji, fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sample.titleBangla,
                                color = if (isChosen) CapCutCyan else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = sample.info,
                                color = CapCutTextMuted,
                                fontSize = 10.sp
                            )
                        }

                        if (isChosen) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(CapCutCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Device Gallery & Camera
                Text("২. অথবা নিজের ভিডিও দিন (Import / Record):", color = CapCutTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPickMedia,
                        colors = ButtonDefaults.buttonColors(containerColor = CapCutSurfaceHighlight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = CapCutPink, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("📁 গ্যালারি", color = Color.White, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onRecordCamera,
                        colors = ButtonDefaults.buttonColors(containerColor = CapCutRedPlayhead.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🎥 ক্যামেরা", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Ratio & Title
                Text("৩. অ্যাসপেক্ট রেশিও:", color = CapCutTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AspectRatio.values().forEach { ratio ->
                        val isSelected = (selectedRatio == ratio)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CapCutCyan else CapCutSurfaceHighlight)
                                .clickable { selectedRatio = ratio }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ratio.label,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("প্রজেক্টের নাম", color = CapCutTextSecondary, fontSize = 11.sp) },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sample = selectedSample
                    if (sample != null) {
                        onConfirmSample(title, selectedRatio, sample)
                    } else {
                        onConfirm(title, selectedRatio)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CapCutCyan)
            ) {
                Text("✨ টাইমলাইনে আনুন", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = CapCutTextSecondary)
            }
        }
    )
}
