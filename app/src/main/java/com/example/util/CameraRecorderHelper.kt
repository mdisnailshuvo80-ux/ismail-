package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraRecorderHelper {

    fun createVideoFileUri(context: Context): Pair<File, Uri> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "ISMAIL_CAPCUT_${timeStamp}.mp4"
        val storageDir = File(context.cacheDir, "camera_videos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val file = File(storageDir, videoFileName)
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        return Pair(file, uri)
    }
}
