package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CapCutCyan
import com.example.ui.theme.CapCutPink
import com.example.ui.theme.CapCutRedPlayhead
import com.example.ui.theme.CapCutSurface
import com.example.ui.theme.CapCutSurfaceHighlight
import com.example.ui.theme.CapCutSurfaceVariant
import com.example.ui.theme.CapCutTextPrimary
import com.example.ui.theme.CapCutTextSecondary

enum class ToolMode {
    NONE,
    SPLIT,
    TRIM,
    CROP,
    SPEED,
    FILTERS,
    AUDIO,
    TEXT,
    STICKERS,
    TRANSITIONS,
    ASPECT_RATIO
}

@Composable
fun BottomToolbar(
    activeToolMode: ToolMode,
    onSelectTool: (ToolMode) -> Unit,
    onSplitClip: () -> Unit,
    onDuplicateClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onExportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CapCutSurface)
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Split (স্প্লিট)
        ToolButton(
            icon = Icons.Default.ContentCut,
            bengaliLabel = "স্প্লিট",
            subLabel = "Split",
            isSelected = activeToolMode == ToolMode.SPLIT,
            accentColor = CapCutCyan,
            tag = "tool_split",
            onClick = {
                onSplitClip()
            }
        )

        // 2. Trim (ট্রিম)
        ToolButton(
            icon = Icons.Default.ContentCut,
            bengaliLabel = "ট্রিম",
            subLabel = "Trim",
            isSelected = activeToolMode == ToolMode.TRIM,
            accentColor = CapCutCyan,
            tag = "tool_trim",
            onClick = { onSelectTool(if (activeToolMode == ToolMode.TRIM) ToolMode.NONE else ToolMode.TRIM) }
        )

        // 3. Crop (ক্রপ ও স্কেল)
        ToolButton(
            icon = Icons.Default.Crop,
            bengaliLabel = "ক্রপ",
            subLabel = "Crop",
            isSelected = activeToolMode == ToolMode.CROP,
            accentColor = CapCutCyan,
            tag = "tool_crop",
            onClick = { onSelectTool(if (activeToolMode == ToolMode.CROP) ToolMode.NONE else ToolMode.CROP) }
        )

        // 4. Speed (গতি / স্পিড)
        ToolButton(
            icon = Icons.Default.Speed,
            bengaliLabel = "স্পিড",
            subLabel = "Speed",
            isSelected = activeToolMode == ToolMode.SPEED,
            accentColor = CapCutCyan,
            tag = "tool_speed",
            onClick = { onSelectTool(if (activeToolMode == ToolMode.SPEED) ToolMode.NONE else ToolMode.SPEED) }
        )

        // 5. Audio & Music (মিউজিক)
        ToolButton(
            icon = Icons.Default.Audiotrack,
            bengaliLabel = "মিউজিক",
            subLabel = "Music",
            isSelected = activeToolMode == ToolMode.AUDIO,
            accentColor = CapCutCyan,
            tag = "tool_audio",
            onClick = { onSelectTool(if (activeToolMode == ToolMode.AUDIO) ToolMode.NONE else ToolMode.AUDIO) }
        )

        // 6. Text & Subtitles (টেক্সট)
        ToolButton(
            icon = Icons.Default.TextFields,
            bengaliLabel = "টেক্সট",
            subLabel = "Text",
            isSelected = activeToolMode == ToolMode.TEXT,
            accentColor = CapCutPink,
            tag = "tool_text",
            onClick = { onSelectTool(if (activeToolMode == ToolMode.TEXT) ToolMode.NONE else ToolMode.TEXT) }
        )

        // 7. Filters & FX (ফিল্টার)
        ToolButton(
            icon = Icons.Default.ColorLens,
            bengaliLabel = "ফিল্টার",
            subLabel = "Filter",
            isSelected = activeToolMode == ToolMode.FILTERS,
            accentColor = CapCutCyan,
            tag = "tool_filters",
            onClick = { onSelectTool(if (activeToolMode == ToolMode.FILTERS) ToolMode.NONE else ToolMode.FILTERS) }
        )

        // 8. Aspect Ratio (৯:১৬ ও ১৬:৯ রেশিও)
        ToolButton(
            icon = Icons.Default.AspectRatio,
            bengaliLabel = "৯:১৬/১৬:৯",
            subLabel = "Ratio",
            isSelected = activeToolMode == ToolMode.ASPECT_RATIO,
            accentColor = CapCutCyan,
            tag = "tool_ratio",
            onClick = { onSelectTool(if (activeToolMode == ToolMode.ASPECT_RATIO) ToolMode.NONE else ToolMode.ASPECT_RATIO) }
        )

        // 9. Stickers (স্টিকার)
        ToolButton(
            icon = Icons.Default.EmojiEmotions,
            bengaliLabel = "স্টিকার",
            subLabel = "Sticker",
            isSelected = activeToolMode == ToolMode.STICKERS,
            accentColor = Color(0xFFFFD166),
            tag = "tool_stickers",
            onClick = { onSelectTool(if (activeToolMode == ToolMode.STICKERS) ToolMode.NONE else ToolMode.STICKERS) }
        )

        // 10. Transitions (ট্রানজিশন)
        ToolButton(
            icon = Icons.Default.AutoAwesome,
            bengaliLabel = "ট্রানজিশন",
            subLabel = "Transition",
            isSelected = activeToolMode == ToolMode.TRANSITIONS,
            accentColor = CapCutCyan,
            tag = "tool_transitions",
            onClick = { onSelectTool(if (activeToolMode == ToolMode.TRANSITIONS) ToolMode.NONE else ToolMode.TRANSITIONS) }
        )

        // 11. Duplicate (কপি / ডুপ্লিকেট)
        ToolButton(
            icon = Icons.Default.ContentCopy,
            bengaliLabel = "ডুপ্লিকেট",
            subLabel = "Copy",
            isSelected = false,
            accentColor = Color.White,
            tag = "tool_duplicate",
            onClick = { onDuplicateClip() }
        )

        // 12. Delete (মুছুন / ডিলিট)
        ToolButton(
            icon = Icons.Default.Delete,
            bengaliLabel = "ডিলিট",
            subLabel = "Delete",
            isSelected = false,
            accentColor = CapCutRedPlayhead,
            tag = "tool_delete",
            onClick = { onDeleteClip() }
        )

        // 13. Export (এক্সপোর্ট)
        ToolButton(
            icon = Icons.Default.Download,
            bengaliLabel = "এক্সপোর্ট",
            subLabel = "Export",
            isSelected = false,
            accentColor = CapCutCyan,
            tag = "tool_export",
            onClick = { onExportClick() }
        )
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    bengaliLabel: String,
    subLabel: String,
    isSelected: Boolean,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .testTag(tag)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CapCutSurfaceHighlight else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isSelected) accentColor.copy(alpha = 0.2f) else CapCutSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$bengaliLabel ($subLabel)",
                tint = if (isSelected) accentColor else CapCutTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = bengaliLabel,
            color = if (isSelected) accentColor else CapCutTextPrimary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        Text(
            text = subLabel,
            color = if (isSelected) accentColor.copy(alpha = 0.8f) else CapCutTextSecondary,
            fontSize = 9.sp
        )
    }
}
