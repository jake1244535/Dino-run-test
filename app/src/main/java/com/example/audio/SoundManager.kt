package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundManager(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val audioScope = CoroutineScope(Dispatchers.Default)

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playJump(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 45)
                } catch (_: Exception) {}
            }
        }
        if (vibrationEnabled) {
            vibrateShort(18)
        }
    }

    fun playDoubleJump(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 50)
                } catch (_: Exception) {}
            }
        }
        if (vibrationEnabled) {
            vibrateShort(25)
        }
    }

    fun playCoin(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 60)
                } catch (_: Exception) {}
            }
        }
        if (vibrationEnabled) {
            vibrateShort(15)
        }
    }

    fun playPowerup(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 120)
                } catch (_: Exception) {}
            }
        }
        if (vibrationEnabled) {
            vibrateShort(40)
        }
    }

    fun playHit(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 140)
                } catch (_: Exception) {}
            }
        }
        if (vibrationEnabled) {
            vibratePattern(longArrayOf(0, 50, 40, 60))
        }
    }

    fun playGameOver(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
                } catch (_: Exception) {}
            }
        }
        if (vibrationEnabled) {
            vibratePattern(longArrayOf(0, 80, 50, 120))
        }
    }

    fun playPurchase(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 90)
                } catch (_: Exception) {}
            }
        }
        if (vibrationEnabled) {
            vibrateShort(30)
        }
    }

    private fun vibrateShort(millis: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(millis)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
