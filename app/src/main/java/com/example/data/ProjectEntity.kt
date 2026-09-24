package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val aspectRatio: String,
    val resolution: String = "1080p",
    val fps: Int = 30,
    val clipsJson: String,
    val audioTracksJson: String,
    val textOverlaysJson: String,
    val stickersJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
