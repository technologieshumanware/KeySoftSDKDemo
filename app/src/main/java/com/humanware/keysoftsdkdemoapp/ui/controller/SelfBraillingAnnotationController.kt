package com.humanware.keysoftsdkdemoapp.ui.controller

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import com.humanware.keysoftsdk.utils.VibrationUtils
import java.util.Locale
import kotlin.math.abs

/**
 * Demo controller that monitors touched pin coordinates and provides:
 * 1) TTS labels when a finger is on an annotation
 * 2) simple radar-like vibration feedback while approaching annotations
 *
 * This is a clean-room demo implementation for KeySoftSDKDemo.
 */
class SelfBraillingAnnotationController(
    context: Context,
    private val getTouchedPinCoordinates: () -> List<Point>
) {
    companion object {
        private const val POLL_INTERVAL_MS = 350L
        private const val ANNOUNCEMENT_COOLDOWN_MS = 1200L
        private const val ON_TARGET_DISTANCE = 0
        private const val NEAR_DISTANCE = 10
    }

    data class Annotation(
        val label: String,
        val bounds: Rect
    )

    private enum class RadarDistance {
        NONE,
        NEAR,
        ON_TARGET
    }

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isTtsReady = false
    private var tts: TextToSpeech? = null

    private var annotations: List<Annotation> = emptyList()
    private var isRunning = false
    private var lastAnnouncedLabel: String? = null
    private var lastAnnouncementTimeMs: Long = 0
    private val vibrationRunnables = mutableListOf<Runnable>()

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) {
                return
            }
            updateFromTouch()
            mainHandler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                tts?.language = Locale.US
            }
        }
    }

    fun setAnnotations(newAnnotations: List<Annotation>) {
        annotations = newAnnotations
        if (annotations.isEmpty()) {
            lastAnnouncedLabel = null
            cancelVibration()
        }
    }

    fun start() {
        if (isRunning) {
            return
        }
        isRunning = true
        mainHandler.post(pollRunnable)
    }

    fun stop() {
        isRunning = false
        mainHandler.removeCallbacks(pollRunnable)
        cancelVibration()
    }

    fun release() {
        stop()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun updateFromTouch() {
        if (annotations.isEmpty()) {
            return
        }

        val touchPoints = getTouchedPinCoordinates()
        if (touchPoints.isEmpty()) {
            lastAnnouncedLabel = null
            cancelVibration()
            return
        }

        val touch = touchPoints.first()
        val touchedAnnotation = annotations.firstOrNull { it.bounds.contains(touch.x, touch.y) }

        if (touchedAnnotation != null) {
            // On target: announce once (with cooldown) and switch to stronger radar pulse.
            maybeAnnounce(touchedAnnotation.label)
            playRadarFeedback(RadarDistance.ON_TARGET)
            return
        }

        val distanceToNearest = annotations.minOfOrNull { distanceToRect(touch, it.bounds) } ?: Int.MAX_VALUE
        val distanceCategory = when {
            distanceToNearest <= ON_TARGET_DISTANCE -> RadarDistance.ON_TARGET
            distanceToNearest <= NEAR_DISTANCE -> RadarDistance.NEAR
            else -> RadarDistance.NONE
        }

        playRadarFeedback(distanceCategory)
    }

    private fun maybeAnnounce(label: String) {
        if (!isTtsReady) {
            return
        }
        val now = SystemClock.elapsedRealtime()
        val isNewLabel = label != lastAnnouncedLabel
        val cooldownElapsed = now - lastAnnouncementTimeMs >= ANNOUNCEMENT_COOLDOWN_MS
        if (isNewLabel || cooldownElapsed) {
            tts?.speak(label, TextToSpeech.QUEUE_FLUSH, null, "annotation-$label")
            lastAnnouncedLabel = label
            lastAnnouncementTimeMs = now
        }
    }

    private fun playRadarFeedback(distance: RadarDistance) {
        // Vibration patterns intentionally stay simple in the demo:
        // NEAR = one short pulse, ON_TARGET = two short pulses.
        when (distance) {
            RadarDistance.NONE -> cancelVibration()
            RadarDistance.NEAR -> vibratePattern(intArrayOf(18), intArrayOf(0))
            RadarDistance.ON_TARGET -> vibratePattern(intArrayOf(22, 22), intArrayOf(0, 140))
        }
    }

    private fun vibratePattern(pulseDurationsMs: IntArray, delaysMs: IntArray) {
        cancelVibration()
        pulseDurationsMs.forEachIndexed { index, durationMs ->
            val runnable = Runnable {
                VibrationUtils.startVibration(appContext, durationMs)
            }
            vibrationRunnables += runnable
            mainHandler.postDelayed(runnable, delaysMs[index].toLong())
        }
    }

    private fun cancelVibration() {
        vibrationRunnables.forEach { mainHandler.removeCallbacks(it) }
        vibrationRunnables.clear()
    }

    private fun distanceToRect(point: Point, rect: Rect): Int {
        val dx = when {
            point.x < rect.left -> rect.left - point.x
            point.x >= rect.right -> point.x - rect.right
            else -> 0
        }
        val dy = when {
            point.y < rect.top -> rect.top - point.y
            point.y >= rect.bottom -> point.y - rect.bottom
            else -> 0
        }
        return abs(dx) + abs(dy)
    }

}
