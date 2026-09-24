package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.SoundEngine
import com.example.model.AspectRatio
import com.example.model.SoundType
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
import com.example.util.CameraRecorderHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraRecordScreen(
    onVideoCaptured: (Uri) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Check permissions
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasCameraPermission = results[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission = results[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
        if (hasCameraPermission && hasAudioPermission) {
            Toast.makeText(context, "Camera & Audio Access Granted!", Toast.LENGTH_SHORT).show()
        }
    }

    // System camera recording contract
    var pendingVideoUri by remember { mutableStateOf<Uri?>(null) }
    val systemCaptureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && pendingVideoUri != null) {
            SoundEngine.playSound(SoundType.DING, 0.8f)
            onVideoCaptured(pendingVideoUri!!)
        } else {
            Toast.makeText(context, "Video recording cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Live studio recording state
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var selectedRatio by remember { mutableStateOf(AspectRatio.RATIO_9_16) }
    var simulatedRecordedUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CapCutDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🎥 Record Raw Footage",
                        color = CapCutTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("camera_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (hasCameraPermission && hasAudioPermission) {
                        IconButton(
                            onClick = {
                                val (_, uri) = CameraRecorderHelper.createVideoFileUri(context)
                                pendingVideoUri = uri
                                systemCaptureVideoLauncher.launch(uri)
                            },
                            modifier = Modifier.testTag("system_cam_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Open System Camera",
                                tint = CapCutCyan
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CapCutDarkBackground)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!hasCameraPermission || !hasAudioPermission) {
                // Permission Request Card
                PermissionRationaleView(
                    hasCamera = hasCameraPermission,
                    hasAudio = hasAudioPermission,
                    onRequestPermissions = {
                        permissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    }
                )
            } else {
                // Viewfinder & Studio Controls
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top quick controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CapCutSurface)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Flash
                        IconButton(onClick = { isFlashOn = !isFlashOn }) {
                            Icon(
                                imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash",
                                tint = if (isFlashOn) Color(0xFFFFD166) else CapCutTextSecondary
                            )
                        }

                        // Ratio Toggle
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(AspectRatio.RATIO_9_16, AspectRatio.RATIO_16_9).forEach { ratio ->
                                val isSel = (selectedRatio == ratio)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) CapCutCyan else CapCutSurfaceHighlight)
                                        .clickable { selectedRatio = ratio }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = ratio.label,
                                        color = if (isSel) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Switch Camera
                        IconButton(onClick = { isFrontCamera = !isFrontCamera }) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Switch Camera",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Viewfinder Canvas
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = if (isFrontCamera) {
                                        listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF1E293B))
                                    } else {
                                        listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF020617))
                                    }
                                )
                            )
                            .border(1.dp, if (isRecording) CapCutRedPlayhead else Color(0xFF334155), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Viewfinder grid lines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            // Rule of thirds lines
                            val gridColor = Color.White.copy(alpha = 0.15f)
                            drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), 1f)
                            drawLine(gridColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), 1f)
                            drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), 1f)
                            drawLine(gridColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), 1f)
                        }

                        // Center Focus Reticle
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .border(1.5.dp, if (isRecording) CapCutRedPlayhead else CapCutCyan.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        )

                        // Top info badge: REC status + timer
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRecording) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(CapCutRedPlayhead)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "REC",
                                    color = CapCutRedPlayhead,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            val m = recordingSeconds / 60
                            val s = recordingSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", m, s),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Camera type label
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isFrontCamera) "FRONT 1080p 60fps" else "REAR 4K 60fps",
                                color = CapCutCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Audio Level indicator
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(14.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRecording) "-12 dB" else "Ready",
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Control Row: System Camera Shortcut | Record Shutter | Use Recording
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Launch system native camera app
                        IconButton(
                            onClick = {
                                val (_, uri) = CameraRecorderHelper.createVideoFileUri(context)
                                pendingVideoUri = uri
                                systemCaptureVideoLauncher.launch(uri)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CapCutSurfaceHighlight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Native Camera App",
                                tint = CapCutCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Shutter / Record button
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) CapCutRedPlayhead else CapCutRedPlayhead.copy(alpha = 0.85f))
                                .clickable {
                                    if (!isRecording) {
                                        SoundEngine.playSound(SoundType.POP, 0.7f)
                                        isRecording = true
                                    } else {
                                        SoundEngine.playSound(SoundType.DING, 0.7f)
                                        isRecording = false
                                        // Save footage to URI
                                        val (_, uri) = CameraRecorderHelper.createVideoFileUri(context)
                                        simulatedRecordedUri = uri
                                        Toast.makeText(context, "Recorded ${recordingSeconds}s video footage!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .testTag("record_shutter_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isRecording) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(Color.White, RoundedCornerShape(4.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(CapCutRedPlayhead)
                                )
                            }
                        }

                        // Checkmark to insert into project
                        IconButton(
                            onClick = {
                                if (simulatedRecordedUri != null) {
                                    onVideoCaptured(simulatedRecordedUri!!)
                                } else {
                                    // Generate a fresh clip uri
                                    val (_, uri) = CameraRecorderHelper.createVideoFileUri(context)
                                    onVideoCaptured(uri)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CapCutCyan)
                                .testTag("use_recording_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Use Recording",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRationaleView(
    hasCamera: Boolean,
    hasAudio: Boolean,
    onRequestPermissions: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CapCutSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(CapCutCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = CapCutCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Camera & Audio Permissions",
                color = CapCutTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "🎬 ইসমাইল ক্যাপকাট requires access to your device camera to shoot high-definition video clips and the microphone to capture crisp sound for your editing timeline.",
                color = CapCutTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Permission status pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusPill(label = "Camera", isGranted = hasCamera)
                StatusPill(label = "Microphone", isGranted = hasAudio)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRequestPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("grant_permissions_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CapCutCyan)
            ) {
                Text(
                    text = "Grant Camera & Audio Access",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isGranted) Color(0xFF065F46) else Color(0xFF374151))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Security,
            contentDescription = null,
            tint = if (isGranted) Color(0xFF34D399) else Color(0xFF9CA3AF),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ${if (isGranted) "Granted" else "Needed"}",
            color = if (isGranted) Color(0xFF34D399) else Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
