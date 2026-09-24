package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProjectConverter
import com.example.model.AspectRatio
import com.example.model.Project
import com.example.model.VideoClip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ইসমাইল ক্যাপকাট", appName)
  }

  @Test
  fun `test project serialization roundtrip`() {
    val original = Project(
      id = 42L,
      title = "Test Video Project",
      aspectRatio = AspectRatio.RATIO_9_16,
      clips = listOf(
        VideoClip(
          id = "c1",
          title = "Intro Clip",
          uri = "",
          durationMs = 3000L,
          cropScale = 1.78f,
          cropOffsetX = 10f,
          cropOffsetY = -20f,
          cropPreset = "FILL_9_16"
        )
      )
    )

    val entity = ProjectConverter.toEntity(original)
    assertEquals(42L, entity.id)
    assertEquals("Test Video Project", entity.title)
    assertEquals("RATIO_9_16", entity.aspectRatio)

    val restored = ProjectConverter.fromEntity(entity)
    assertEquals(original.id, restored.id)
    assertEquals(original.title, restored.title)
    assertEquals(original.aspectRatio, restored.aspectRatio)
    assertEquals(1, restored.clips.size)
    assertEquals("Intro Clip", restored.clips[0].title)
    assertEquals(1.78f, restored.clips[0].cropScale, 0.01f)
    assertEquals(10f, restored.clips[0].cropOffsetX, 0.01f)
    assertEquals(-20f, restored.clips[0].cropOffsetY, 0.01f)
    assertEquals("FILL_9_16", restored.clips[0].cropPreset)
  }

  @Test
  fun `test project aspect ratios`() {
    assertEquals(9f / 16f, AspectRatio.RATIO_9_16.ratio, 0.001f)
    assertEquals(16f / 9f, AspectRatio.RATIO_16_9.ratio, 0.001f)
    assertEquals(1f, AspectRatio.RATIO_1_1.ratio, 0.001f)
    assertEquals(4f / 5f, AspectRatio.RATIO_4_5.ratio, 0.001f)
  }
}
