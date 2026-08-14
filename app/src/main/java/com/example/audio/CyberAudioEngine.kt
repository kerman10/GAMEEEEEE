package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural low-latency Audio Engine synthesizing cyberpunk sound effects
 * and ambient synth drones using Android AudioTrack without needing external asset files.
 */
object CyberAudioEngine {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isMuted = false

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    /** Plays a crisp whoosh sound for jumps and double jumps */
    fun playJumpSound(isDoubleJump: Boolean = false) {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            val durationMs = if (isDoubleJump) 180 else 120
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            val startFreq = if (isDoubleJump) 280.0 else 180.0
            val endFreq = if (isDoubleJump) 650.0 else 420.0

            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * t
                val envelope = (1.0 - t) * (1.0 - t) // Quadratic decay
                val phase = 2.0 * PI * currentFreq * (i.toDouble() / sampleRate)
                val sample = sin(phase) * envelope * 0.7
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        }
    }

    /** Plays a dynamic wall-run glide/friction sound */
    fun playWallRunSound() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            val durationMs = 150
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val noise = (Math.random() * 2.0 - 1.0) * 0.4
                val tone = sin(2.0 * PI * 180.0 * (i.toDouble() / sampleRate)) * 0.3
                val env = sin(t * PI)
                val sample = (noise + tone) * env * 0.5
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        }
    }

    /** Plays a grappling hook latch and reel sound */
    fun playGrappleSound() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            val durationMs = 220
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val freq = 500.0 + sin(t * 20.0 * PI) * 120.0
                val env = 1.0 - t
                val sample = sin(2.0 * PI * freq * (i.toDouble() / sampleRate)) * env * 0.6
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        }
    }

    /** Plays a slide / crouch friction burst */
    fun playSlideSound() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            val durationMs = 200
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val noise = (Math.random() * 2.0 - 1.0)
                val env = (1.0 - t) * 0.45
                buffer[i] = (noise * env * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        }
    }

    /** Plays terminal interaction / cyber hacking sound */
    fun playTerminalClick() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            val durationMs = 80
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val freq = 880.0 + (if (t > 0.5) 440.0 else 0.0)
                val env = 1.0 - t
                val sample = sin(2.0 * PI * freq * (i.toDouble() / sampleRate)) * env * 0.5
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        }
    }

    /** Plays majestic synth chord on puzzle solve or sector clear */
    fun playSuccessChord() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            val durationMs = 450
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            val notes = doubleArrayOf(440.0, 554.37, 659.25, 880.0) // A Major Cyber Chord

            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                var mix = 0.0
                for (n in notes) {
                    mix += sin(2.0 * PI * n * (i.toDouble() / sampleRate))
                }
                mix /= notes.size
                val env = if (t < 0.1) t / 0.1 else 1.0 - (t - 0.1) / 0.9
                val sample = mix * env * 0.75
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        }
    }

    /** Mystical resonant chime when the Observing Book speaks */
    fun playBookWhisperSound() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            val durationMs = 350
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val freq = 523.25 + sin(t * 12.0) * 40.0 // C5 modulating
                val harm = 1046.5
                val env = sin(t * PI) * 0.4
                val sample = (sin(2.0 * PI * freq * (i.toDouble() / sampleRate)) * 0.7 +
                        sin(2.0 * PI * harm * (i.toDouble() / sampleRate)) * 0.3) * env
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        }
    }

    private fun playPcmBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            scope.launch {
                kotlinx.coroutines.delay((buffer.size * 1000L / sampleRate) + 50)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // AudioTrack fallback
        }
    }
}
