package com.example.data

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
import org.json.JSONArray
import org.json.JSONObject

object ProjectConverter {

    fun toEntity(project: Project): ProjectEntity {
        val clipsArray = JSONArray()
        for (clip in project.clips) {
            val obj = JSONObject().apply {
                put("id", clip.id)
                put("title", clip.title)
                put("uri", clip.uri)
                put("mediaType", clip.mediaType.name)
                put("durationMs", clip.durationMs)
                put("trimStartMs", clip.trimStartMs)
                put("trimEndMs", clip.trimEndMs)
                put("speed", clip.speed.toDouble())
                put("volume", clip.volume.toDouble())
                put("filter", clip.filter.name)
                put("transition", clip.transition.name)
                put("startGradientColor", clip.startGradientColor)
                put("endGradientColor", clip.endGradientColor)
                put("motionType", clip.motionType)
                put("cropScale", clip.cropScale.toDouble())
                put("cropOffsetX", clip.cropOffsetX.toDouble())
                put("cropOffsetY", clip.cropOffsetY.toDouble())
                put("cropPreset", clip.cropPreset)
            }
            clipsArray.put(obj)
        }

        val audioArray = JSONArray()
        for (track in project.audioTracks) {
            val obj = JSONObject().apply {
                put("id", track.id)
                put("name", track.name)
                put("category", track.category)
                put("startMs", track.startMs)
                put("durationMs", track.durationMs)
                put("volume", track.volume.toDouble())
                put("isMuted", track.isMuted)
                put("soundType", track.soundType.name)
                put("audioUri", track.audioUri ?: "")
            }
            audioArray.put(obj)
        }

        val textArray = JSONArray()
        for (text in project.textOverlays) {
            val obj = JSONObject().apply {
                put("id", text.id)
                put("text", text.text)
                put("startMs", text.startMs)
                put("durationMs", text.durationMs)
                put("xPercent", text.xPercent.toDouble())
                put("yPercent", text.yPercent.toDouble())
                put("fontSizeSp", text.fontSizeSp)
                put("textColor", text.textColor)
                put("bgColor", text.bgColor)
                put("style", text.style)
                put("animation", text.animation)
            }
            textArray.put(obj)
        }

        val stickersArray = JSONArray()
        for (stk in project.stickers) {
            val obj = JSONObject().apply {
                put("id", stk.id)
                put("emoji", stk.emoji)
                put("startMs", stk.startMs)
                put("durationMs", stk.durationMs)
                put("xPercent", stk.xPercent.toDouble())
                put("yPercent", stk.yPercent.toDouble())
                put("scale", stk.scale.toDouble())
            }
            stickersArray.put(obj)
        }

        return ProjectEntity(
            id = project.id,
            title = project.title,
            aspectRatio = project.aspectRatio.name,
            resolution = project.resolution,
            fps = project.fps,
            clipsJson = clipsArray.toString(),
            audioTracksJson = audioArray.toString(),
            textOverlaysJson = textArray.toString(),
            stickersJson = stickersArray.toString(),
            createdAt = project.createdAt,
            updatedAt = project.updatedAt
        )
    }

    fun fromEntity(entity: ProjectEntity): Project {
        val clips = mutableListOf<VideoClip>()
        try {
            val arr = JSONArray(entity.clipsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                clips.add(
                    VideoClip(
                        id = obj.optString("id", "c_$i"),
                        title = obj.optString("title", "Clip ${i + 1}"),
                        uri = obj.optString("uri", ""),
                        mediaType = runCatching { MediaType.valueOf(obj.getString("mediaType")) }.getOrDefault(MediaType.CANVAS_MOTION),
                        durationMs = obj.optLong("durationMs", 4000L),
                        trimStartMs = obj.optLong("trimStartMs", 0L),
                        trimEndMs = obj.optLong("trimEndMs", 4000L),
                        speed = obj.optDouble("speed", 1.0).toFloat(),
                        volume = obj.optDouble("volume", 1.0).toFloat(),
                        filter = runCatching { VideoFilter.valueOf(obj.getString("filter")) }.getOrDefault(VideoFilter.NORMAL),
                        transition = runCatching { ClipTransition.valueOf(obj.getString("transition")) }.getOrDefault(ClipTransition.NONE),
                        startGradientColor = obj.optLong("startGradientColor", 0xFF6366F1),
                        endGradientColor = obj.optLong("endGradientColor", 0xFFEC4899),
                        motionType = obj.optString("motionType", "ZOOM_PAN"),
                        cropScale = obj.optDouble("cropScale", 1.0).toFloat(),
                        cropOffsetX = obj.optDouble("cropOffsetX", 0.0).toFloat(),
                        cropOffsetY = obj.optDouble("cropOffsetY", 0.0).toFloat(),
                        cropPreset = obj.optString("cropPreset", "FIT")
                    )
                )
            }
        } catch (_: Exception) {}

        val audioTracks = mutableListOf<AudioTrack>()
        try {
            val arr = JSONArray(entity.audioTracksJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                audioTracks.add(
                    AudioTrack(
                        id = obj.optString("id", "a_$i"),
                        name = obj.optString("name", "Audio ${i + 1}"),
                        category = obj.optString("category", "Sound FX"),
                        startMs = obj.optLong("startMs", 0L),
                        durationMs = obj.optLong("durationMs", 2000L),
                        volume = obj.optDouble("volume", 1.0).toFloat(),
                        isMuted = obj.optBoolean("isMuted", false),
                        soundType = runCatching { SoundType.valueOf(obj.getString("soundType")) }.getOrDefault(SoundType.WHOOSH),
                        audioUri = obj.optString("audioUri").takeIf { it.isNotEmpty() }
                    )
                )
            }
        } catch (_: Exception) {}

        val textOverlays = mutableListOf<TextOverlay>()
        try {
            val arr = JSONArray(entity.textOverlaysJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                textOverlays.add(
                    TextOverlay(
                        id = obj.optString("id", "t_$i"),
                        text = obj.optString("text", "Text"),
                        startMs = obj.optLong("startMs", 0L),
                        durationMs = obj.optLong("durationMs", 3000L),
                        xPercent = obj.optDouble("xPercent", 0.5).toFloat(),
                        yPercent = obj.optDouble("yPercent", 0.5).toFloat(),
                        fontSizeSp = obj.optInt("fontSizeSp", 24),
                        textColor = obj.optLong("textColor", 0xFFFFFFFF),
                        bgColor = obj.optLong("bgColor", 0xAA000000),
                        style = obj.optString("style", "CapCut Bold"),
                        animation = obj.optString("animation", "Pop")
                    )
                )
            }
        } catch (_: Exception) {}

        val stickers = mutableListOf<StickerOverlay>()
        try {
            val arr = JSONArray(entity.stickersJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                stickers.add(
                    StickerOverlay(
                        id = obj.optString("id", "s_$i"),
                        emoji = obj.optString("emoji", "🔥"),
                        startMs = obj.optLong("startMs", 0L),
                        durationMs = obj.optLong("durationMs", 3000L),
                        xPercent = obj.optDouble("xPercent", 0.5).toFloat(),
                        yPercent = obj.optDouble("yPercent", 0.35).toFloat(),
                        scale = obj.optDouble("scale", 1.0).toFloat()
                    )
                )
            }
        } catch (_: Exception) {}

        val ratio = runCatching { AspectRatio.valueOf(entity.aspectRatio) }.getOrDefault(AspectRatio.RATIO_9_16)

        return Project(
            id = entity.id,
            title = entity.title,
            aspectRatio = ratio,
            resolution = entity.resolution,
            fps = entity.fps,
            clips = clips,
            audioTracks = audioTracks,
            textOverlays = textOverlays,
            stickers = stickers,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
