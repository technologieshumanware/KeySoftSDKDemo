package com.humanware.keysoftsdkdemoapp.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.KeyEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.humanware.keysoftsdk.selfbrailling.SelfBraillingManager
import com.humanware.keysoftsdk.selfbrailling.aidl.DotsMatrix
import com.humanware.keysoftsdk.selfbrailling.widget.SelfBraillingWidget
import com.humanware.keysoftsdkdemoapp.R
import com.humanware.keysoftsdkdemoapp.model.SelfBraillingDrawing
import com.humanware.keysoftsdkdemoapp.model.randomDots
import com.humanware.keysoftsdkdemoapp.model.verticalLines
import com.humanware.keysoftsdkdemoapp.ui.controller.SelfBraillingAnnotationController

/**
 * Implements a sample usage of the [SelfBraillingWidget] allowing visualization of simple graphics.
 *
 * See the [README](./README.md) for a description of the main fields found in this class.
 *
 * Bitmap images created from [Material Symbols and Icons - Google Fonts](https://fonts.google.com/icons)
 * icons: [Wb Sunny](https://fonts.google.com/icons?selected=Material%20Symbols%20Outlined%3Awb_sunny%3AFILL%400%3Bwght%40400%3BGRAD%400%3Bopsz%4048)
 * and [Lunch Dining](https://fonts.google.com/icons?selected=Material%20Symbols%20Outlined%3Alunch_dining%3AFILL%400%3Bwght%40400%3BGRAD%400%3Bopsz%4048)
 * converted to 1-bit bitmap images.
 */
class SelfBraillingActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SelfBraillingActivity"
        const val pinDown: Byte = 0
        const val pinUp: Byte = 1
    }

    private lateinit var manager: SelfBraillingManager
    private lateinit var widget: SelfBraillingWidget

    private var mutableLiveDots = MutableLiveData<Array<ByteArray>>()
    private var mutableViewedImage = MutableLiveData<Array<ByteArray>>()

    private lateinit var screenDimensions: Size
    private lateinit var brailleScreen: DotsMatrix

    private lateinit var brailleBurger: SelfBraillingDrawing
    private lateinit var brailleSun: SelfBraillingDrawing
    private lateinit var brailleRandom: SelfBraillingDrawing
    private lateinit var brailleVerticalLines: SelfBraillingDrawing
    private lateinit var annotationController: SelfBraillingAnnotationController

    private var burgerArea: Rect? = null
    private var sunArea: Rect? = null

    private val startContextMenu = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        clearScreen()

        when (it.resultCode) {
            ResultCode.Bitmap.value -> {
                // More than one DotsMatrix may be included on the screen simultaneously.
                brailleScreen.include(brailleBurger.asDotsMatrix(), 10, 10)
                brailleScreen.include(brailleSun.asDotsMatrix(), 60, 10)
                setBitmapModeAnnotations()
            }

            ResultCode.Random.value -> {
                // A DotsMatrix may cover the whole screen.
                brailleScreen.include(brailleRandom.asDotsMatrix(), 0, 0)
                annotationController.setAnnotations(emptyList())
            }

            ResultCode.VerticalLines.value -> {
                // A DotsMatrix can also entirely substitute the Braille screen object.
                brailleScreen = brailleVerticalLines.asDotsMatrix()
                annotationController.setAnnotations(emptyList())
            }

            else -> {
                // TODO("This result code is not handled.")
                Log.d(TAG, "This result code is not handled.")
            }
        }
    }

    /**
     * Activity result codes expected by the activity to define what
     * visualization should be shown on the screen.
     */
    enum class ResultCode(val value: Int) {
        Bitmap(12345),
        Random(23456),
        VerticalLines(34567),
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_KeySoftSDKDemoApp)

        manager = SelfBraillingManager(this).apply { bindService() }
        widget = SelfBraillingWidget(this).also { setContentView(it) }

        // This ensures we have access to the full screen without any overlapping bars.
        supportActionBar?.hide()

        // The widget and manager are now responsible for observing any changes
        // to the underlying LiveData instances.
        mutableViewedImage.observe(this) { widget.refresh(it) }
        mutableLiveDots.observe(this) { manager.displayDots(it) }

        screenDimensions = Size(manager.brailleDisplayDotsSizeX, manager.brailleDisplayDotsSizeY)
        brailleScreen = DotsMatrix(screenDimensions.height, screenDimensions.width)

        val bitmapBurger = BitmapFactory.decodeStream(resources.openRawResource(R.raw.burger))
        val bitmapSun = BitmapFactory.decodeStream(resources.openRawResource(R.raw.sun))

        brailleBurger = SelfBraillingDrawing.BitmapBraille(bitmapBurger)
        brailleSun = SelfBraillingDrawing.BitmapBraille(bitmapSun)
        brailleRandom = SelfBraillingDrawing.ProceduralBraille(screenDimensions.width, randomDots(screenDimensions))
        brailleVerticalLines = SelfBraillingDrawing.ProceduralBraille(screenDimensions.width, verticalLines(screenDimensions))

        // The demo starts with a drawing of two bitmap images, a burger and a sun.
        brailleScreen.include(brailleBurger.asDotsMatrix(), 10, 10)
        brailleScreen.include(brailleSun.asDotsMatrix(), 60, 10)

        burgerArea = Rect(
            10,
            10,
            10 + bitmapBurger.width,
            10 + bitmapBurger.height
        )
        sunArea = Rect(
            60,
            10,
            60 + bitmapSun.width,
            10 + bitmapSun.height
        )

        annotationController = SelfBraillingAnnotationController(
            this,
            getTouchedPinCoordinates = { manager.touchedPinCoordinates }
        )
        setBitmapModeAnnotations()

        widget.onSelfBraillingWidgetListener = object :
            SelfBraillingWidget.OnSelfBraillingWidgetListener {
            override fun onFocused() = refreshScreen()

            override fun onSingleTapAtBraillePosition(pointX: Int, pointY: Int) {
                Log.d(TAG, "Single tap at: $pointX, $pointY")
            }

            override fun onDoubleTapAtBraillePosition(pointX: Int, pointY: Int) {
                Log.e("POINT", "$pointX, $pointY")
                // TODO: Offset is wrong when clicking using emulator
                brailleScreen.include(DotsMatrix(arrayOf(byteArrayOf(1))), pointX, pointY)
                refreshScreen()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                val contextMenuIntent = Intent(this, SelfBraillingContextMenuActivity::class.java)
                startContextMenu.launch(contextMenuIntent)
                true
            }

            KeyEvent.KEYCODE_B -> {
                if (event?.hasModifiers(KeyEvent.META_CTRL_ON) == true) {
                    brailleScreen.include(brailleBurger.asDotsMatrix(), 10, 10)
                    brailleScreen.include(brailleSun.asDotsMatrix(), 60, 10)
                    setBitmapModeAnnotations()
                    refreshScreen()
                    true
                } else {
                    false
                }
            }

            KeyEvent.KEYCODE_R -> {
                if (event?.hasModifiers(KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON) == true) {
                    brailleScreen.include(brailleRandom.asDotsMatrix(), 0, 0)
                    annotationController.setAnnotations(emptyList())
                    refreshScreen()
                    true
                } else {
                    false
                }
            }

            KeyEvent.KEYCODE_V -> {
                if (event?.hasModifiers(KeyEvent.META_CTRL_ON) == true) {
                    brailleScreen.include(brailleVerticalLines.asDotsMatrix(), 0, 0)
                    annotationController.setAnnotations(emptyList())
                    refreshScreen()
                    true
                } else {
                    false
                }
            }

            KeyEvent.KEYCODE_ZOOM_IN -> {
                Log.d(TAG, "Zoom in")
                true
            }

            KeyEvent.KEYCODE_ZOOM_OUT -> {
                Log.d(TAG, "Zoom out")
                true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onResume() {
        manager.bindService()
        if (::annotationController.isInitialized) {
            annotationController.start()
        }
        super.onResume()
    }

    override fun onStop() {
        if (::annotationController.isInitialized) {
            annotationController.stop()
        }
        manager.unbindService()
        super.onStop()
    }

    override fun onDestroy() {
        if (::annotationController.isInitialized) {
            annotationController.release()
        }
        super.onDestroy()
    }

    /**
     * Sends updated values from the backing Livedata to the screen.
     */
    private fun refreshScreen() {
        mutableViewedImage.value = brailleScreen.matrix
        mutableLiveDots.value = brailleScreen.matrix
    }

    /**
     * Lowers all pins on the screen.
     */
    private fun clearScreen() {
        brailleScreen.include(DotsMatrix(Array(screenDimensions.height) {
            ByteArray(screenDimensions.width) { pinDown }
        }), 0, 0)
    }

    private fun setBitmapModeAnnotations() {
        val annotations = buildList {
            burgerArea?.let {
                add(
                    SelfBraillingAnnotationController.Annotation(
                        label = getString(R.string.annotation_burger),
                        bounds = it
                    )
                )
            }
            sunArea?.let {
                add(
                    SelfBraillingAnnotationController.Annotation(
                        label = getString(R.string.annotation_sun),
                        bounds = it
                    )
                )
            }
        }
        annotationController.setAnnotations(annotations)
    }

}
