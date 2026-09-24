package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object SoundEngine {
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 44100

    fun playSound(type: SoundType, volume: Float = 1.0f) {
        scope.launch {
            try {
                val samples = generateSamples(type)
                playPcm(samples, volume.coerceIn(0f, 1f))
            } catch (_: Exception) {}
        }
    }

    private fun generateSamples(type: SoundType): ShortArray {
        val durationMs = type.durationMs.coerceAtMost(3000L)
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt().coerceAtLeast(100)
        val buffer = ShortArray(numSamples)

        when (type) {
            SoundType.WHOOSH -> {
                // Pitch sweep downward with white noise blend
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 600.0 - 450.0 * progress
                    val envelope = sin(progress * PI)
                    val tone = sin(2.0 * PI * freq * i / SAMPLE_RATE)
                    val noise = (Math.random() * 2.0 - 1.0) * 0.3
                    val sample = (tone + noise) * envelope * Short.MAX_VALUE * 0.7
                    buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            SoundType.POP -> {
                // Quick high pitched pop with rapid decay
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 900.0 - 500.0 * progress
                    val envelope = exp(-progress * 15.0)
                    val sample = sin(2.0 * PI * freq * i / SAMPLE_RATE) * envelope * Short.MAX_VALUE * 0.85
                    buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            SoundType.BEAT_DROP, SoundType.CINEMATIC_BOOM -> {
                // Deep 808 sub-bass with distortion decay
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 140.0 * (1.0 - 0.7 * progress)
                    val envelope = exp(-progress * 4.0)
                    val sample = sin(2.0 * PI * freq * i / SAMPLE_RATE) * envelope * Short.MAX_VALUE * 0.95
                    buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            SoundType.DING -> {
                // Bright dual-harmonic bell
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val envelope = exp(-progress * 6.0)
                    val f1 = sin(2.0 * PI * 1320.0 * i / SAMPLE_RATE)
                    val f2 = sin(2.0 * PI * 2640.0 * i / SAMPLE_RATE) * 0.5
                    val sample = (f1 + f2) * envelope * Short.MAX_VALUE * 0.7
                    buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            SoundType.APPLAUSE -> {
                // Modulated noise burst simulating cheer
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val envelope = sin(progress * PI)
                    val noise = (Math.random() * 2.0 - 1.0)
                    val sample = noise * envelope * Short.MAX_VALUE * 0.45
                    buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            SoundType.CHILL_LOFI, SoundType.TRAP_GROOVE -> {
                // Rhythmic groove chord pattern
                for (i in 0 until numSamples) {
                    val timeSec = i.toDouble() / SAMPLE_RATE
                    val beat = (timeSec * 2.0) % 1.0
                    val envelope = exp(-beat * 4.0)
                    val bass = sin(2.0 * PI * 110.0 * i / SAMPLE_RATE) * envelope
                    val chord = sin(2.0 * PI * 330.0 * i / SAMPLE_RATE) * 0.4 * envelope
                    val sample = (bass + chord) * Short.MAX_VALUE * 0.6
                    buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            SoundType.VOICEOVER -> {
                // Gentle human voice formants (simulation)
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val envelope = sin(progress * PI)
                    val f1 = sin(2.0 * PI * 220.0 * i / SAMPLE_RATE)
                    val f2 = sin(2.0 * PI * 440.0 * i / SAMPLE_RATE) * 0.6
                    val sample = (f1 + f2) * envelope * Short.MAX_VALUE * 0.6
                    buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }
        return buffer
    }

    private fun playPcm(samples: ShortArray, volume: Float) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size)
        audioTrack.setVolume(volume)
        audioTrack.play()
    }
}
