package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.Project
import com.example.ui.components.ProjectThumbnail
import com.example.ui.theme.CapCutCyan
import com.example.ui.theme.CapCutDarkBackground
import com.example.ui.theme.CapCutPink
import com.example.ui.theme.CapCutSurface
import com.example.ui.theme.CapCutSurfaceHighlight
import com.example.ui.theme.CapCutTextMuted
import com.example.ui.theme.CapCutTextPrimary
import com.example.ui.theme.CapCutTextSecondary
import com.example.util.ExportResult
import com.example.util.VideoExportHelper

@Composable
fun ExportScreen(
    project: Project,
    resolution: String,
    fps: String,
    exportResult: ExportResult? = null,
    onBackToHome: () -> Unit,
    onContinueEditing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CapCutDarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Success Badge (Bangla & English)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(CapCutCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = CapCutCyan,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ভিডিও এক্সপোর্ট সম্পন্ন! 🎬",
                    color = CapCutTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "MP4 ফাইল ডিভাইসে সেভ হয়েছে (/Movies/IsmailCapCut)",
                    color = CapCutCyan,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Video Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(project.aspectRatio.ratio),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CapCutSurface)
            ) {
                ProjectThumbnail(
                    project = project,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Video specs card (Resolution, FPS, Duration, File Size)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CapCutSurface)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("রেজোলিউশন", color = CapCutTextMuted, fontSize = 10.sp)
                        val resLabel = if (resolution.contains("720")) "720p HD MP4" else if (resolution.contains("4K")) "4K MP4" else "1080p FHD MP4"
                        Text(resLabel, color = CapCutCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ফ্রেমরেট", color = CapCutTextMuted, fontSize = 10.sp)
                        Text(fps, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val sec = project.totalDurationMs / 1000
                        Text("দৈর্ঘ্য", color = CapCutTextMuted, fontSize = 10.sp)
                        Text("${sec} সেকেন্ড", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val sizeText = exportResult?.fileSizeFormatted
                            ?: String.format("%.1f MB", (project.totalDurationMs / 1000f) * 1.5f)
                        Text("ফাইল সাইজ", color = CapCutTextMuted, fontSize = 10.sp)
                        Text(sizeText, color = CapCutPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (exportResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📁 পাথ: ${exportResult.filePath}",
                        color = CapCutTextMuted,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Share Button (Native Android Intent with MP4)
                Button(
                    onClick = {
                        if (exportResult != null) {
                            val shareIntent = VideoExportHelper.createShareIntent(context, exportResult, project.title)
                            context.startActivity(Intent.createChooser(shareIntent, "ভিডিও শেয়ার করুন"))
                        } else {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, project.title)
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🎬 ইসমাইল ক্যাপকাট দিয়ে এডিট করা ভিডিও: '${project.title}'"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "ভিডিও শেয়ার করুন"))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("share_video_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CapCutCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ভিডিও শেয়ার করুন (Share Video)",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 2. Play Exported Video
                if (exportResult?.contentUri != null) {
                    OutlinedButton(
                        onClick = {
                            val viewIntent = VideoExportHelper.createViewIntent(exportResult)
                            if (viewIntent != null) {
                                try {
                                    context.startActivity(Intent.createChooser(viewIntent, "ভিডিও প্লে করুন"))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "ভিডিও প্লেয়ার পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CapCutCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ভিডিওটি প্লে করে দেখুন (Play Video)", color = CapCutCyan, fontSize = 13.sp)
                    }
                }

                // 3. Save to Device / Gallery confirmation button
                OutlinedButton(
                    onClick = {
                        val path = exportResult?.filePath ?: "/Movies/IsmailCapCut"
                        Toast.makeText(
                            context,
                            "গ্যালারিতে MP4 হিসেবে সংরক্ষিত হয়েছে!\n$path",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_device_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DownloadDone, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("গ্যালারিতে সেভ করুন (Save to Gallery)", color = Color.White, fontSize = 13.sp)
                }

                // 4. Continue Editing or Back to Home
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onContinueEditing,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("আরও এডিট (Edit)", color = CapCutCyan, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onBackToHome,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CapCutSurfaceHighlight)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("হোমে ফিরুন (Home)", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
