package com.example.services

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class PanicAlarmManager(private val context: Context) {

    private var isSirenActive = false
    private var sirenJob: Job? = null
    private var audioTrack: AudioTrack? = null

    private var isVibrating = false
    private var vibrateJob: Job? = null

    private var isFlashlightOn = false
    private var strobeJob: Job? = null
    private var cameraId: String? = null

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    init {
        try {
            cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val hasFlash = cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                hasFlash
            }
        } catch (e: Exception) {
            Log.e("PanicAlarmManager", "Camera flash initialization error", e)
        }
    }

    // --- SIREN SOUND SYNTHESIZER ---
    fun startSiren(scope: CoroutineScope) {
        if (isSirenActive) return
        isSirenActive = true

        sirenJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
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
                .setBufferSizeInBytes(minBufferSize)
                .build()

            audioTrack?.play()

            val bufferSize = 2205
            val buffer = ShortArray(bufferSize)
            var phase = 0.0
            var freq = 600.0
            var increasing = true

            while (isActive && isSirenActive) {
                // Modulate frequency between 600Hz and 1400Hz (Police Siren)
                if (increasing) {
                    freq += 15.0
                    if (freq >= 1400.0) increasing = false
                } else {
                    freq -= 15.0
                    if (freq <= 600.0) increasing = true
                }

                val increment = 2.0 * Math.PI * freq / sampleRate
                for (i in 0 until bufferSize) {
                    buffer[i] = (sin(phase) * 32000.0).toInt().toShort()
                    phase += increment
                    if (phase >= 2.0 * Math.PI) phase -= 2.0 * Math.PI
                }

                audioTrack?.write(buffer, 0, bufferSize)
            }
        }
    }

    fun stopSiren() {
        isSirenActive = false
        sirenJob?.cancel()
        sirenJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.e("PanicAlarmManager", "AudioTrack release error", e)
        }
        audioTrack = null
    }

    // --- VIBRATION CONTROL ---
    fun startVibration(scope: CoroutineScope) {
        if (isVibrating) return
        isVibrating = true

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrateJob = scope.launch(Dispatchers.Default) {
            while (isActive && isVibrating) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
                delay(700)
            }
        }
    }

    fun stopVibration() {
        isVibrating = false
        vibrateJob?.cancel()
        vibrateJob = null
    }

    // --- FLASHLIGHT STROBE CONTROL ---
    fun toggleFlashlight(): Boolean {
        return setFlashlight(!isFlashlightOn)
    }

    fun setFlashlight(enable: Boolean): Boolean {
        val id = cameraId ?: return false
        return try {
            cameraManager?.setTorchMode(id, enable)
            isFlashlightOn = enable
            true
        } catch (e: CameraAccessException) {
            Log.e("PanicAlarmManager", "Flashlight toggle error", e)
            false
        }
    }

    fun startStrobeFlashlight(scope: CoroutineScope) {
        val id = cameraId ?: return
        strobeJob = scope.launch(Dispatchers.Default) {
            var state = false
            while (isActive) {
                state = !state
                try {
                    cameraManager?.setTorchMode(id, state)
                } catch (_: Exception) {}
                delay(200)
            }
        }
    }

    fun stopStrobeFlashlight() {
        strobeJob?.cancel()
        strobeJob = null
        setFlashlight(false)
    }

    fun stopAll() {
        stopSiren()
        stopVibration()
        stopStrobeFlashlight()
    }

    fun isSirenRunning(): Boolean = isSirenActive
    fun isFlashlightRunning(): Boolean = isFlashlightOn
}
