package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AspectRatio
import com.example.model.Project
import com.example.ui.screens.CameraRecordScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.IsmailCapCutTheme
import com.example.viewmodel.VideoEditorViewModel

sealed class AppScreen {
    data object Home : AppScreen()
    data object Editor : AppScreen()
    data class CameraRecord(val targetProjectId: Long?) : AppScreen()
    data class Export(
        val project: Project,
        val resolution: String,
        val fps: String,
        val exportResult: com.example.util.ExportResult? = null
    ) : AppScreen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: VideoEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IsmailCapCutTheme {
                MainAppNavHost(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppNavHost(
    viewModel: VideoEditorViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Editor) }

    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val currentPlayheadMs by viewModel.currentPlayheadMs.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val selectedClipId by viewModel.selectedClipId.collectAsStateWithLifecycle()
    val activeToolMode by viewModel.activeToolMode.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition",
        modifier = modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            is AppScreen.Home -> {
                HomeScreen(
                    projects = projects,
                    onOpenProject = { project ->
                        viewModel.selectProject(project)
                        currentScreen = AppScreen.Editor
                    },
                    onCreateNewProject = { title, ratio, uris ->
                        viewModel.createNewProject(title, ratio, uris)
                        currentScreen = AppScreen.Editor
                    },
                    onCreateFromSample = { title, ratio, clipTitle, duration, filter, startGrad, endGrad, textBanner ->
                        viewModel.createProjectFromSample(title, ratio, clipTitle, duration, filter, startGrad, endGrad, textBanner)
                        currentScreen = AppScreen.Editor
                    },
                    onOpenRecordCamera = {
                        currentScreen = AppScreen.CameraRecord(targetProjectId = null)
                    },
                    onDuplicateProject = { project ->
                        viewModel.duplicateProject(project)
                    },
                    onDeleteProject = { id ->
                        viewModel.deleteProject(id)
                    }
                )
            }

            is AppScreen.CameraRecord -> {
                BackHandler {
                    if (screen.targetProjectId != null && currentProject != null) {
                        currentScreen = AppScreen.Editor
                    } else {
                        currentScreen = AppScreen.Home
                    }
                }

                CameraRecordScreen(
                    onVideoCaptured = { uri ->
                        if (screen.targetProjectId != null && currentProject != null) {
                            viewModel.addClips(listOf(uri.toString()))
                            currentScreen = AppScreen.Editor
                        } else {
                            viewModel.createNewProject(
                                title = "ক্যামেরা ফুটেজ এডিট",
                                ratio = AspectRatio.RATIO_9_16,
                                importedUris = listOf(uri.toString())
                            )
                            currentScreen = AppScreen.Editor
                        }
                    },
                    onBack = {
                        if (screen.targetProjectId != null && currentProject != null) {
                            currentScreen = AppScreen.Editor
                        } else {
                            currentScreen = AppScreen.Home
                        }
                    }
                )
            }

            is AppScreen.Editor -> {
                val proj = currentProject ?: VideoEditorViewModel.createDefaultDemoProject()
                BackHandler {
                    viewModel.pause()
                    viewModel.saveCurrentProject()
                    currentScreen = AppScreen.Home
                }

                EditorScreen(
                    project = proj,
                        currentPlayheadMs = currentPlayheadMs,
                        isPlaying = isPlaying,
                        selectedClipId = selectedClipId,
                        activeToolMode = activeToolMode,
                        onBackClicked = {
                            viewModel.pause()
                            viewModel.saveCurrentProject()
                            currentScreen = AppScreen.Home
                        },
                        onTogglePlay = { viewModel.togglePlay() },
                        onSeek = { viewModel.seekTo(it) },
                        onSelectClip = { viewModel.selectClip(it) },
                        onSelectTool = { viewModel.setToolMode(it) },
                        onSplitClip = { viewModel.splitActiveClip() },
                        onDuplicateClip = { viewModel.duplicateActiveClip() },
                        onDeleteClip = { viewModel.deleteActiveClip() },
                        onUpdateFilter = { viewModel.updateClipFilter(it) },
                        onUpdateSpeed = { viewModel.updateClipSpeed(it) },
                        onUpdateTransition = { viewModel.updateClipTransition(it) },
                        onUpdateTrim = { start, end -> viewModel.updateClipTrim(start, end) },
                        onUpdateCrop = { scale, x, y, preset -> viewModel.updateClipCrop(scale, x, y, preset) },
                        onAddAudio = { name, cat, type -> viewModel.addAudioTrack(name, cat, type) },
                        onAddText = { txt, col, bg, sz, style -> viewModel.addTextOverlay(txt, col, bg, sz, style) },
                        onUpdateTextPosition = { id, x, y -> viewModel.updateTextPosition(id, x, y) },
                        onAddSticker = { emoji -> viewModel.addSticker(emoji) },
                        onUpdateStickerPosition = { id, x, y -> viewModel.updateStickerPosition(id, x, y) },
                        onChangeAspectRatio = { viewModel.changeAspectRatio(it) },
                        onAddClips = { uris -> viewModel.addClips(uris) },
                        onOpenRecordCamera = {
                            viewModel.pause()
                            currentScreen = AppScreen.CameraRecord(targetProjectId = proj.id)
                        },
                        onUpdateTitle = { viewModel.updateProjectTitle(it) },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onExportComplete = { exportProj, res, fps, result ->
                            viewModel.pause()
                            currentScreen = AppScreen.Export(exportProj, res, fps, result)
                        }
                    )
            }

            is AppScreen.Export -> {
                BackHandler {
                    currentScreen = AppScreen.Home
                }

                ExportScreen(
                    project = screen.project,
                    resolution = screen.resolution,
                    fps = screen.fps,
                    exportResult = screen.exportResult,
                    onBackToHome = {
                        currentScreen = AppScreen.Home
                    },
                    onContinueEditing = {
                        viewModel.selectProject(screen.project)
                        currentScreen = AppScreen.Editor
                    }
                )
            }
        }
    }
}
