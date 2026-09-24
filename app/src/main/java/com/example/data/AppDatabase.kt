package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ismail_capcut.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).projectDao()
                            seedInitialProjects(dao)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialProjects(dao: ProjectDao) {
            val sampleProjects = listOf(
                ProjectConverter.toEntity(
                    com.example.model.Project(
                        id = 0,
                        title = "🔥 Ismail TikTok Reel",
                        aspectRatio = com.example.model.AspectRatio.RATIO_9_16,
                        resolution = "1080p",
                        fps = 60,
                        clips = listOf(
                            com.example.model.VideoClip(
                                id = "clip_1",
                                title = "Intro Neon Pulse",
                                uri = "",
                                durationMs = 3500L,
                                trimStartMs = 0L,
                                trimEndMs = 3500L,
                                speed = 1.0f,
                                filter = com.example.model.VideoFilter.CYBERPUNK,
                                transition = com.example.model.ClipTransition.ZOOM_IN,
                                startGradientColor = 0xFF6366F1,
                                endGradientColor = 0xFF00E5FF
                            ),
                            com.example.model.VideoClip(
                                id = "clip_2",
                                title = "Action Drop",
                                uri = "",
                                durationMs = 4000L,
                                trimStartMs = 0L,
                                trimEndMs = 4000L,
                                speed = 1.2f,
                                filter = com.example.model.VideoFilter.GLITCH,
                                transition = com.example.model.ClipTransition.FLASH_WHITE,
                                startGradientColor = 0xFFFF2A85,
                                endGradientColor = 0xFFFFD166
                            ),
                            com.example.model.VideoClip(
                                id = "clip_3",
                                title = "Final Outro Beat",
                                uri = "",
                                durationMs = 3000L,
                                trimStartMs = 0L,
                                trimEndMs = 3000L,
                                speed = 1.0f,
                                filter = com.example.model.VideoFilter.VIVID,
                                transition = com.example.model.ClipTransition.DISSOLVE,
                                startGradientColor = 0xFF10B981,
                                endGradientColor = 0xFF3B82F6
                            )
                        ),
                        audioTracks = listOf(
                            com.example.model.AudioTrack(
                                id = "snd_1",
                                name = "Whoosh Swipe",
                                category = "Sound FX",
                                startMs = 0L,
                                durationMs = 500L,
                                soundType = com.example.model.SoundType.WHOOSH
                            ),
                            com.example.model.AudioTrack(
                                id = "snd_2",
                                name = "Trap Beat 808",
                                category = "BGM",
                                startMs = 500L,
                                durationMs = 8000L,
                                soundType = com.example.model.SoundType.BEAT_DROP
                            )
                        ),
                        textOverlays = listOf(
                            com.example.model.TextOverlay(
                                id = "txt_1",
                                text = "🎬 ইসমাইল ক্যাপকাট",
                                startMs = 300L,
                                durationMs = 3200L,
                                xPercent = 0.5f,
                                yPercent = 0.3f,
                                fontSizeSp = 28,
                                textColor = 0xFFFFFFFF,
                                bgColor = 0x88000000,
                                style = "Neon Glow"
                            ),
                            com.example.model.TextOverlay(
                                id = "txt_2",
                                text = "PRO VIDEO EDIT ✨",
                                startMs = 3600L,
                                durationMs = 3500L,
                                xPercent = 0.5f,
                                yPercent = 0.65f,
                                fontSizeSp = 24,
                                textColor = 0xFF00E5FF,
                                bgColor = 0x99161922
                            )
                        ),
                        stickers = listOf(
                            com.example.model.StickerOverlay(
                                id = "stk_1",
                                emoji = "🔥",
                                startMs = 500L,
                                durationMs = 3000L,
                                xPercent = 0.8f,
                                yPercent = 0.2f,
                                scale = 1.2f
                            ),
                            com.example.model.StickerOverlay(
                                id = "stk_2",
                                emoji = "⚡",
                                startMs = 3600L,
                                durationMs = 3500L,
                                xPercent = 0.2f,
                                yPercent = 0.2f,
                                scale = 1.3f
                            )
                        )
                    )
                ),
                ProjectConverter.toEntity(
                    com.example.model.Project(
                        id = 0,
                        title = "✨ Cinematic Travel Vlog",
                        aspectRatio = com.example.model.AspectRatio.RATIO_16_9,
                        resolution = "4K",
                        fps = 30,
                        clips = listOf(
                            com.example.model.VideoClip(
                                id = "clip_a",
                                title = "Golden Hour View",
                                uri = "",
                                durationMs = 5000L,
                                trimStartMs = 0L,
                                trimEndMs = 5000L,
                                speed = 0.8f,
                                filter = com.example.model.VideoFilter.WARM,
                                transition = com.example.model.ClipTransition.FADE_BLACK,
                                startGradientColor = 0xFFF59E0B,
                                endGradientColor = 0xFFEF4444
                            ),
                            com.example.model.VideoClip(
                                id = "clip_b",
                                title = "Sunset Mountain Path",
                                uri = "",
                                durationMs = 4500L,
                                trimStartMs = 0L,
                                trimEndMs = 4500L,
                                speed = 1.0f,
                                filter = com.example.model.VideoFilter.SUNSET,
                                transition = com.example.model.ClipTransition.SLIDE_LEFT,
                                startGradientColor = 0xFF8B5CF6,
                                endGradientColor = 0xFFEC4899
                            )
                        ),
                        audioTracks = listOf(
                            com.example.model.AudioTrack(
                                id = "snd_cin",
                                name = "Chill Lofi Guitar",
                                category = "BGM",
                                startMs = 0L,
                                durationMs = 9500L,
                                soundType = com.example.model.SoundType.CHILL_LOFI
                            )
                        ),
                        textOverlays = listOf(
                            com.example.model.TextOverlay(
                                id = "txt_cine",
                                text = "CINEMATIC MOMENTS",
                                startMs = 1000L,
                                durationMs = 4000L,
                                xPercent = 0.5f,
                                yPercent = 0.75f,
                                fontSizeSp = 22,
                                textColor = 0xFFFFF1F2,
                                bgColor = 0x66000000
                            )
                        ),
                        stickers = listOf(
                            com.example.model.StickerOverlay(
                                id = "stk_star",
                                emoji = "✨",
                                startMs = 800L,
                                durationMs = 4000L,
                                xPercent = 0.5f,
                                yPercent = 0.25f,
                                scale = 1.2f
                            )
                        )
                    )
                )
            )

            for (p in sampleProjects) {
                dao.insertProject(p)
            }
        }
    }
}
