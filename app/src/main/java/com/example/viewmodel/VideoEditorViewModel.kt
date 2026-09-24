package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEngine
import com.example.data.AppDatabase
import com.example.data.ProjectRepository
import com.example.model.AspectRatio
import com.example.model.AudioTrack
import com.example.model.ClipTransition
import com.example.model.MediaType
import com.example.model.Project
import com.example.model.SoundType
import com.example.model.StickerOverlay
import com.example.model.TextOverlay
import com.example.model.VideoClip
import com.example.model.VideoFilter
import com.example.ui.components.ToolMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Stack
import java.util.UUID

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(AppDatabase.getInstance(application).projectDao())

    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun createDefaultDemoProject(): Project {
            val clip1 = VideoClip(
                id = "clip_neon_intro",
                title = "Scene 1: নিয়ন ড্রাইভ (Neon Intro)",
                uri = "",
                durationMs = 3800L,
                trimStartMs = 0L,
                trimEndMs = 3800L,
                speed = 1.0f,
                filter = VideoFilter.CYBERPUNK,
                transition = ClipTransition.ZOOM_IN,
                startGradientColor = 0xFF6366F1,
                endGradientColor = 0xFF00E5FF
            )
            val clip2 = VideoClip(
                id = "clip_action_drop",
                title = "Scene 2: অ্যাকশন ড্রপ (Action Drop)",
                uri = "",
                durationMs = 4200L,
                trimStartMs = 0L,
                trimEndMs = 4200L,
                speed = 1.2f,
                filter = VideoFilter.GLITCH,
                transition = ClipTransition.FLASH_WHITE,
                startGradientColor = 0xFFFF2A85,
                endGradientColor = 0xFFFFD166
            )
            return Project(
                id = 1L,
                title = "🎬 ইসমাইল ক্যাপকাট এডিট",
                aspectRatio = AspectRatio.RATIO_9_16,
                resolution = "1080p",
                fps = 60,
                clips = listOf(clip1, clip2),
                audioTracks = listOf(
                    AudioTrack(
                        id = "track_fx_whoosh",
                        name = "Whoosh Swipe",
                        category = "Sound FX",
                        startMs = 0L,
                        durationMs = 600L,
                        soundType = SoundType.WHOOSH
                    )
                ),
                textOverlays = listOf(
                    TextOverlay(
                        id = "text_main_title",
                        text = "ISMAIL CAPCUT 🎬",
                        startMs = 0L,
                        durationMs = 3800L,
                        xPercent = 0.5f,
                        yPercent = 0.35f,
                        fontSizeSp = 24,
                        textColor = 0xFFFFFFFF,
                        bgColor = 0x88000000
                    )
                ),
                stickers = listOf(
                    StickerOverlay(
                        id = "sticker_fire",
                        emoji = "🔥",
                        startMs = 200L,
                        durationMs = 3600L,
                        xPercent = 0.82f,
                        yPercent = 0.22f
                    )
                )
            )
        }
    }

    private val defaultInitialProject = createDefaultDemoProject()
    private val _currentProject = MutableStateFlow<Project?>(defaultInitialProject)
    val currentProject: StateFlow<Project?> = _currentProject.asStateFlow()

    private val _currentPlayheadMs = MutableStateFlow(0L)
    val currentPlayheadMs: StateFlow<Long> = _currentPlayheadMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(defaultInitialProject.clips.firstOrNull()?.id)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _activeToolMode = MutableStateFlow(ToolMode.NONE)
    val activeToolMode: StateFlow<ToolMode> = _activeToolMode.asStateFlow()

    private val undoStack = Stack<Project>()
    private val redoStack = Stack<Project>()

    private var playbackJob: Job? = null

    init {
        viewModelScope.launch {
            allProjects.collect { projects ->
                if (_currentProject.value == defaultInitialProject && projects.isNotEmpty()) {
                    selectProject(projects.first())
                }
            }
        }
    }

    fun selectProject(project: Project) {
        undoStack.clear()
        redoStack.clear()
        _currentProject.value = project
        _currentPlayheadMs.value = 0L
        _selectedClipId.value = project.clips.firstOrNull()?.id
        _activeToolMode.value = ToolMode.NONE
        pause()
    }

    fun createNewProject(
        title: String = "ইসমাইল ক্যাপকাট এডিট",
        ratio: AspectRatio = AspectRatio.RATIO_9_16,
        importedUris: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val defaultClips = if (importedUris.isNotEmpty()) {
                importedUris.mapIndexed { idx, uri ->
                    VideoClip(
                        id = UUID.randomUUID().toString(),
                        title = "Clip ${idx + 1}",
                        uri = uri,
                        mediaType = MediaType.VIDEO,
                        durationMs = 4000L,
                        trimStartMs = 0L,
                        trimEndMs = 4000L,
                        speed = 1.0f,
                        startGradientColor = 0xFF6366F1,
                        endGradientColor = 0xFF00E5FF
                    )
                }
            } else {
                listOf(
                    VideoClip(
                        id = UUID.randomUUID().toString(),
                        title = "Scene 1: Intro",
                        uri = "",
                        durationMs = 3500L,
                        trimStartMs = 0L,
                        trimEndMs = 3500L,
                        speed = 1.0f,
                        filter = VideoFilter.CYBERPUNK,
                        transition = ClipTransition.ZOOM_IN,
                        startGradientColor = 0xFF6366F1,
                        endGradientColor = 0xFF00E5FF
                    ),
                    VideoClip(
                        id = UUID.randomUUID().toString(),
                        title = "Scene 2: Drop",
                        uri = "",
                        durationMs = 4000L,
                        trimStartMs = 0L,
                        trimEndMs = 4000L,
                        speed = 1.0f,
                        filter = VideoFilter.GLITCH,
                        transition = ClipTransition.FLASH_WHITE,
                        startGradientColor = 0xFFFF2A85,
                        endGradientColor = 0xFFFFD166
                    )
                )
            }

            val newProject = Project(
                id = 0L,
                title = title,
                aspectRatio = ratio,
                resolution = "1080p",
                fps = 30,
                clips = defaultClips,
                audioTracks = listOf(
                    AudioTrack(
                        id = UUID.randomUUID().toString(),
                        name = "Whoosh Swipe",
                        category = "Sound FX",
                        startMs = 0L,
                        durationMs = 600L,
                        soundType = SoundType.WHOOSH
                    )
                ),
                textOverlays = listOf(
                    TextOverlay(
                        id = UUID.randomUUID().toString(),
                        text = "ISMAIL CAPCUT 🎬",
                        startMs = 0L,
                        durationMs = 3500L,
                        xPercent = 0.5f,
                        yPercent = 0.35f,
                        fontSizeSp = 26,
                        textColor = 0xFFFFFFFF,
                        bgColor = 0x88000000
                    )
                ),
                stickers = listOf(
                    StickerOverlay(
                        id = UUID.randomUUID().toString(),
                        emoji = "🔥",
                        startMs = 200L,
                        durationMs = 3000L,
                        xPercent = 0.8f,
                        yPercent = 0.2f
                    )
                )
            )

            val newId = repository.saveProject(newProject)
            selectProject(newProject.copy(id = newId))
        }
    }

    fun createProjectFromSample(
        title: String,
        ratio: AspectRatio,
        clipTitle: String,
        durationMs: Long,
        filter: VideoFilter,
        startGradient: Long,
        endGradient: Long,
        textBanner: String
    ) {
        viewModelScope.launch {
            val clipId = UUID.randomUUID().toString()
            val sampleClip = VideoClip(
                id = clipId,
                title = clipTitle,
                uri = "",
                durationMs = durationMs,
                trimStartMs = 0L,
                trimEndMs = durationMs,
                speed = 1.0f,
                filter = filter,
                transition = ClipTransition.ZOOM_IN,
                startGradientColor = startGradient,
                endGradientColor = endGradient
            )
            val newProject = Project(
                id = 0L,
                title = title,
                aspectRatio = ratio,
                resolution = "1080p",
                fps = 60,
                clips = listOf(sampleClip),
                audioTracks = listOf(
                    AudioTrack(
                        id = UUID.randomUUID().toString(),
                        name = "Whoosh Swipe",
                        category = "Sound FX",
                        startMs = 0L,
                        durationMs = 600L,
                        soundType = SoundType.WHOOSH
                    )
                ),
                textOverlays = listOf(
                    TextOverlay(
                        id = UUID.randomUUID().toString(),
                        text = textBanner,
                        startMs = 0L,
                        durationMs = durationMs,
                        xPercent = 0.5f,
                        yPercent = 0.35f,
                        fontSizeSp = 22,
                        textColor = 0xFFFFFFFF,
                        bgColor = 0x88000000
                    )
                ),
                stickers = listOf(
                    StickerOverlay(
                        id = UUID.randomUUID().toString(),
                        emoji = "🔥",
                        startMs = 200L,
                        durationMs = durationMs - 500L,
                        xPercent = 0.8f,
                        yPercent = 0.2f
                    )
                )
            )
            val newId = repository.saveProject(newProject)
            selectProject(newProject.copy(id = newId))
        }
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            repository.duplicateProject(project)
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_currentProject.value?.id == id) {
                _currentProject.value = null
            }
        }
    }

    fun setToolMode(mode: ToolMode) {
        _activeToolMode.value = mode
    }

    fun selectClip(id: String) {
        _selectedClipId.value = id
    }

    fun togglePlay() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    private fun play() {
        val proj = _currentProject.value ?: return
        val totalMs = proj.totalDurationMs

        if (_currentPlayheadMs.value >= totalMs) {
            _currentPlayheadMs.value = 0L
        }

        _isPlaying.value = true
        playbackJob?.cancel()

        playbackJob = viewModelScope.launch {
            val stepMs = 33L
            var lastAudioTriggerMs = _currentPlayheadMs.value

            while (isActive && _isPlaying.value) {
                val nextMs = _currentPlayheadMs.value + stepMs
                if (nextMs >= totalMs) {
                    _currentPlayheadMs.value = totalMs
                    _isPlaying.value = false
                    break
                }
                _currentPlayheadMs.value = nextMs

                // Trigger audio playback for tracks crossing playhead
                proj.audioTracks.forEach { track ->
                    if (!track.isMuted && lastAudioTriggerMs < track.startMs && nextMs >= track.startMs) {
                        SoundEngine.playSound(track.soundType, track.volume)
                    }
                }
                lastAudioTriggerMs = nextMs
                delay(stepMs)
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun seekTo(ms: Long) {
        val total = _currentProject.value?.totalDurationMs ?: 1000L
        _currentPlayheadMs.value = ms.coerceIn(0L, total)
    }

    private fun pushHistory() {
        val cur = _currentProject.value ?: return
        undoStack.push(cur)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val cur = _currentProject.value ?: return
            redoStack.push(cur)
            val prev = undoStack.pop()
            _currentProject.value = prev
            saveCurrentProject()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val cur = _currentProject.value ?: return
            undoStack.push(cur)
            val next = redoStack.pop()
            _currentProject.value = next
            saveCurrentProject()
        }
    }

    fun splitActiveClip() {
        val project = _currentProject.value ?: return
        val playhead = _currentPlayheadMs.value

        var accumulated = 0L
        val updatedClips = mutableListOf<VideoClip>()
        var didSplit = false

        for (clip in project.clips) {
            val duration = clip.effectiveDurationMs
            val clipStart = accumulated
            val clipEnd = accumulated + duration

            if (!didSplit && playhead > clipStart + 200 && playhead < clipEnd - 200) {
                pushHistory()
                val splitRatio = (playhead - clipStart).toFloat() / duration.toFloat()
                val originalDuration = clip.trimEndMs - clip.trimStartMs
                val splitOffsetMs = (originalDuration * splitRatio).toLong()

                val firstHalf = clip.copy(
                    id = UUID.randomUUID().toString(),
                    title = "${clip.title} (Part 1)",
                    trimEndMs = clip.trimStartMs + splitOffsetMs
                )
                val secondHalf = clip.copy(
                    id = UUID.randomUUID().toString(),
                    title = "${clip.title} (Part 2)",
                    trimStartMs = clip.trimStartMs + splitOffsetMs
                )

                updatedClips.add(firstHalf)
                updatedClips.add(secondHalf)
                _selectedClipId.value = secondHalf.id
                didSplit = true
            } else {
                updatedClips.add(clip)
            }
            accumulated += duration
        }

        if (didSplit) {
            SoundEngine.playSound(SoundType.WHOOSH, 0.7f)
            _currentProject.value = project.copy(clips = updatedClips)
            saveCurrentProject()
        }
    }

    fun updateClipSpeed(speed: Float) {
        val project = _currentProject.value ?: return
        val selId = _selectedClipId.value ?: project.clips.firstOrNull()?.id ?: return
        pushHistory()

        val updatedClips = project.clips.map { clip ->
            if (clip.id == selId) clip.copy(speed = speed) else clip
        }
        _currentProject.value = project.copy(clips = updatedClips)
        saveCurrentProject()
    }

    fun updateClipFilter(filter: VideoFilter) {
        val project = _currentProject.value ?: return
        val selId = _selectedClipId.value ?: project.clips.firstOrNull()?.id ?: return
        pushHistory()

        val updatedClips = project.clips.map { clip ->
            if (clip.id == selId) clip.copy(filter = filter) else clip
        }
        _currentProject.value = project.copy(clips = updatedClips)
        saveCurrentProject()
    }

    fun updateClipTransition(transition: ClipTransition) {
        val project = _currentProject.value ?: return
        val selId = _selectedClipId.value ?: project.clips.firstOrNull()?.id ?: return
        pushHistory()

        val updatedClips = project.clips.map { clip ->
            if (clip.id == selId) clip.copy(transition = transition) else clip
        }
        _currentProject.value = project.copy(clips = updatedClips)
        saveCurrentProject()
    }

    fun updateClipTrim(startMs: Long, endMs: Long) {
        val project = _currentProject.value ?: return
        val selId = _selectedClipId.value ?: project.clips.firstOrNull()?.id ?: return
        pushHistory()

        val updatedClips = project.clips.map { clip ->
            if (clip.id == selId) clip.copy(trimStartMs = startMs, trimEndMs = endMs) else clip
        }
        _currentProject.value = project.copy(clips = updatedClips)
        saveCurrentProject()
    }

    fun updateClipCrop(scale: Float, offsetX: Float, offsetY: Float, preset: String = "CUSTOM") {
        val project = _currentProject.value ?: return
        val selId = _selectedClipId.value ?: project.clips.firstOrNull()?.id ?: return
        pushHistory()

        val updatedClips = project.clips.map { clip ->
            if (clip.id == selId) clip.copy(
                cropScale = scale,
                cropOffsetX = offsetX,
                cropOffsetY = offsetY,
                cropPreset = preset
            ) else clip
        }
        _currentProject.value = project.copy(clips = updatedClips)
        saveCurrentProject()
    }

    fun changeAspectRatio(ratio: AspectRatio) {
        val project = _currentProject.value ?: return
        pushHistory()
        _currentProject.value = project.copy(aspectRatio = ratio)
        saveCurrentProject()
    }

    fun addClips(uris: List<String>) {
        val project = _currentProject.value ?: return
        if (uris.isEmpty()) return
        pushHistory()

        val newClips = uris.mapIndexed { idx, uri ->
            VideoClip(
                id = UUID.randomUUID().toString(),
                title = "Imported ${project.clips.size + idx + 1}",
                uri = uri,
                mediaType = MediaType.VIDEO,
                durationMs = 4000L,
                trimStartMs = 0L,
                trimEndMs = 4000L,
                startGradientColor = 0xFF10B981,
                endGradientColor = 0xFF3B82F6
            )
        }
        _currentProject.value = project.copy(clips = project.clips + newClips)
        saveCurrentProject()
    }

    fun duplicateActiveClip() {
        val project = _currentProject.value ?: return
        val selId = _selectedClipId.value ?: project.clips.firstOrNull()?.id ?: return
        val clip = project.clips.find { it.id == selId } ?: return
        pushHistory()

        val duplicated = clip.copy(id = UUID.randomUUID().toString(), title = "${clip.title} (Copy)")
        val idx = project.clips.indexOfFirst { it.id == selId }
        val updated = project.clips.toMutableList().apply { add(idx + 1, duplicated) }

        _selectedClipId.value = duplicated.id
        _currentProject.value = project.copy(clips = updated)
        saveCurrentProject()
    }

    fun deleteActiveClip() {
        val project = _currentProject.value ?: return
        if (project.clips.size <= 1) return
        val selId = _selectedClipId.value ?: return
        pushHistory()

        val updated = project.clips.filterNot { it.id == selId }
        _selectedClipId.value = updated.firstOrNull()?.id
        _currentProject.value = project.copy(clips = updated)
        saveCurrentProject()
    }

    fun addAudioTrack(name: String, category: String, soundType: SoundType) {
        val project = _currentProject.value ?: return
        pushHistory()

        val track = AudioTrack(
            id = UUID.randomUUID().toString(),
            name = name,
            category = category,
            startMs = _currentPlayheadMs.value,
            durationMs = soundType.durationMs,
            soundType = soundType
        )
        _currentProject.value = project.copy(audioTracks = project.audioTracks + track)
        saveCurrentProject()
    }

    fun addTextOverlay(text: String, color: Long, bg: Long, sizeSp: Int, style: String) {
        val project = _currentProject.value ?: return
        pushHistory()

        val overlay = TextOverlay(
            id = UUID.randomUUID().toString(),
            text = text,
            startMs = _currentPlayheadMs.value,
            durationMs = 3000L,
            xPercent = 0.5f,
            yPercent = 0.5f,
            fontSizeSp = sizeSp,
            textColor = color,
            bgColor = bg,
            style = style
        )
        _currentProject.value = project.copy(textOverlays = project.textOverlays + overlay)
        saveCurrentProject()
    }

    fun updateTextPosition(id: String, x: Float, y: Float) {
        val project = _currentProject.value ?: return
        val updated = project.textOverlays.map {
            if (it.id == id) it.copy(xPercent = x, yPercent = y) else it
        }
        _currentProject.value = project.copy(textOverlays = updated)
        saveCurrentProject()
    }

    fun addSticker(emoji: String) {
        val project = _currentProject.value ?: return
        pushHistory()

        val sticker = StickerOverlay(
            id = UUID.randomUUID().toString(),
            emoji = emoji,
            startMs = _currentPlayheadMs.value,
            durationMs = 3000L,
            xPercent = 0.5f,
            yPercent = 0.35f,
            scale = 1.2f
        )
        _currentProject.value = project.copy(stickers = project.stickers + sticker)
        saveCurrentProject()
    }

    fun updateStickerPosition(id: String, x: Float, y: Float) {
        val project = _currentProject.value ?: return
        val updated = project.stickers.map {
            if (it.id == id) it.copy(xPercent = x, yPercent = y) else it
        }
        _currentProject.value = project.copy(stickers = updated)
        saveCurrentProject()
    }

    fun updateProjectTitle(title: String) {
        val project = _currentProject.value ?: return
        _currentProject.value = project.copy(title = title)
        saveCurrentProject()
    }

    fun saveCurrentProject() {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            val id = repository.saveProject(project)
            if (project.id == 0L) {
                _currentProject.value = project.copy(id = id)
            }
        }
    }
}
