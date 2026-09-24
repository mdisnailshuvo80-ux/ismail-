package com.example.model

enum class MediaType {
    VIDEO,
    IMAGE,
    CANVAS_MOTION
}

enum class VideoFilter(val displayName: String, val colorMatrix: FloatArray?) {
    NORMAL("Normal", null),
    VINTAGE("Vintage", floatArrayOf(
        0.9f, 0.1f, 0.1f, 0f, 20f,
        0.1f, 0.8f, 0.1f, 0f, 10f,
        0.1f, 0.1f, 0.7f, 0f, -10f,
        0f, 0f, 0f, 1f, 0f
    )),
    CYBERPUNK("Cyberpunk", floatArrayOf(
        0.8f, 0.2f, 0.4f, 0f, -10f,
        0.1f, 1.2f, 0.5f, 0f, 10f,
        0.4f, 0.2f, 1.4f, 0f, 40f,
        0f, 0f, 0f, 1f, 0f
    )),
    NOIR("Noir B&W", floatArrayOf(
        0.33f, 0.33f, 0.33f, 0f, 0f,
        0.33f, 0.33f, 0.33f, 0f, 0f,
        0.33f, 0.33f, 0.33f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )),
    WARM("Golden Glow", floatArrayOf(
        1.2f, 0f, 0f, 0f, 30f,
        0f, 1.05f, 0f, 0f, 15f,
        0f, 0f, 0.85f, 0f, -20f,
        0f, 0f, 0f, 1f, 0f
    )),
    SUNSET("Sunset", floatArrayOf(
        1.3f, 0.1f, 0.0f, 0f, 35f,
        0.1f, 0.9f, 0.1f, 0f, 10f,
        0.2f, 0.1f, 1.1f, 0f, 25f,
        0f, 0f, 0f, 1f, 0f
    )),
    GLITCH("Glitch FX", floatArrayOf(
        1.4f, -0.2f, 0.2f, 0f, 20f,
        -0.2f, 1.3f, -0.1f, 0f, -10f,
        0.3f, -0.3f, 1.5f, 0f, 30f,
        0f, 0f, 0f, 1f, 0f
    )),
    VIVID("Vivid Pop", floatArrayOf(
        1.25f, -0.1f, -0.1f, 0f, 0f,
        -0.1f, 1.25f, -0.1f, 0f, 0f,
        -0.1f, -0.1f, 1.25f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))
}

enum class ClipTransition(val displayName: String, val iconName: String) {
    NONE("None", "none"),
    FADE_BLACK("Fade Black", "fade"),
    DISSOLVE("Dissolve", "dissolve"),
    SLIDE_LEFT("Slide", "slide"),
    ZOOM_IN("Zoom In", "zoom"),
    FLASH_WHITE("Flash", "flash")
}

data class VideoClip(
    val id: String,
    val title: String,
    val uri: String,
    val mediaType: MediaType = MediaType.CANVAS_MOTION,
    val durationMs: Long = 4000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 4000L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val filter: VideoFilter = VideoFilter.NORMAL,
    val transition: ClipTransition = ClipTransition.NONE,
    val startGradientColor: Long = 0xFF6366F1,
    val endGradientColor: Long = 0xFFEC4899,
    val motionType: String = "ZOOM_PAN",
    val cropScale: Float = 1.0f,
    val cropOffsetX: Float = 0f,
    val cropOffsetY: Float = 0f,
    val cropPreset: String = "FIT"
) {
    val effectiveDurationMs: Long
        get() = ((trimEndMs - trimStartMs) / speed).toLong().coerceAtLeast(200L)
}

enum class SoundType(val label: String, val frequencyHz: Int, val durationMs: Long) {
    WHOOSH("Whoosh Swipe", 350, 400),
    POP("Pop Bubble", 800, 200),
    CINEMATIC_BOOM("Cinematic Boom", 120, 1200),
    BEAT_DROP("Beat Drop 808", 160, 900),
    APPLAUSE("Cheer & Claps", 600, 1500),
    DING("Success Bell", 1200, 500),
    CHILL_LOFI("Chill Lofi Beat", 220, 8000),
    TRAP_GROOVE("Trap Beat", 180, 8000),
    VOICEOVER("Voiceover Mic", 440, 3000)
}

data class AudioTrack(
    val id: String,
    val name: String,
    val category: String = "Sound FX",
    val startMs: Long = 0L,
    val durationMs: Long = 2000L,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val soundType: SoundType = SoundType.WHOOSH,
    val audioUri: String? = null
)

data class TextOverlay(
    val id: String,
    val text: String,
    val startMs: Long = 0L,
    val durationMs: Long = 3000L,
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.5f,
    val fontSizeSp: Int = 24,
    val textColor: Long = 0xFFFFFFFF,
    val bgColor: Long = 0xAA000000,
    val style: String = "CapCut Bold",
    val animation: String = "Pop"
)

data class StickerOverlay(
    val id: String,
    val emoji: String,
    val startMs: Long = 0L,
    val durationMs: Long = 3000L,
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.35f,
    val scale: Float = 1.0f
)

enum class AspectRatio(val label: String, val ratio: Float, val subtitle: String) {
    RATIO_9_16("9:16", 9f / 16f, "TikTok / Reels"),
    RATIO_16_9("16:9", 16f / 9f, "YouTube / Wide"),
    RATIO_1_1("1:1", 1f, "Instagram Feed"),
    RATIO_4_5("4:5", 4f / 5f, "Portrait")
}

data class Project(
    val id: Long = 0,
    val title: String,
    val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val resolution: String = "1080p",
    val fps: Int = 30,
    val clips: List<VideoClip> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val stickers: List<StickerOverlay> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.effectiveDurationMs }.coerceAtLeast(1000L)
}
