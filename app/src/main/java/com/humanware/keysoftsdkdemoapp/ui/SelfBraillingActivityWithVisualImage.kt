package com.humanware.keysoftsdkdemoapp.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.KeyEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.MutableLiveData
import com.humanware.keysoftsdk.selfbrailling.SelfBraillingManager
import com.humanware.keysoftsdk.selfbrailling.aidl.DotsMatrix
import com.humanware.keysoftsdk.selfbrailling.widget.SelfBraillingWidget
import com.humanware.keysoftsdkdemoapp.R
import com.humanware.keysoftsdkdemoapp.model.SelfBraillingDrawing
import com.humanware.keysoftsdkdemoapp.model.randomDots
import com.humanware.keysoftsdkdemoapp.model.verticalLines
import com.humanware.keysoftsdkdemoapp.ui.SelfBraillingActivity.Companion.pinUp
import com.humanware.keysoftsdkdemoapp.ui.SelfBraillingActivityWithVisualImage.Companion.VISUAL_SCALE
import com.humanware.keysoftsdkdemoapp.ui.controller.SelfBraillingAnnotationController

/**
 * Variant of [SelfBraillingActivity] demonstrating that the visual screen driven by the
 * [SelfBraillingWidget] does not have to show the same image that is sent to the physical Braille display.
 *
 * Instead of reflecting the Braille dots matrix on screen, this activity rebuilds and shows a
 * higher resolution visual counterpart ([visualBitmap]): the original source bitmaps for the
 * burger/sun drawing, and an upscaled rendering of the dots for the procedural drawings
 * (random dots, vertical lines) and for single dot toggles. This is meant purely as a
 * demonstration for the client that both a Braille-accurate preview and a richer, higher
 * resolution visual can be produced from the same underlying data.
 *
 * See [SelfBraillingActivity] for the base scenario where the visual screen simply mirrors the
 * Braille dots.
 *
 * ## How to show a different image on screen than what is sent to the Braille display
 *
 * 1. **Keep two separate pieces of data, one per destination.** [brailleScreen] (a [DotsMatrix])
 *    holds the low resolution content (braille 96x40 pixels) that will be felt on the physical Braille display, while
 *    [visualBitmap] (an Android [Bitmap]) holds whatever you want to be seen on the screen. They
 *    are intentionally unrelated types: nothing forces the visual bitmap to be derived from the
 *    Braille dots, so it can be a completely different image (e.g. a full-resolution photo, a
 *    diagram, or plain text) as long as it makes sense next to what is being brailled.
 *
 * 2. **Expose each destination through its own [MutableLiveData].** [mutableLiveDots] carries the
 *    Braille content, [mutableViewedImage] carries the visual content. Keeping them as separate
 *    `LiveData` instances is what lets each destination be updated independently.
 *
 * 3. **Wire each `LiveData` to the API that actually targets that destination**, once, in
 *    [onCreate]:
 *    - `mutableViewedImage.observe(this) { widget.refresh(it) }` sends the [Bitmap] to the
 *      [SelfBraillingWidget], which renders it on the visual screen.
 *    - `mutableLiveDots.observe(this) { manager.displayDots(it) }` sends the dots matrix to the
 *      [SelfBraillingManager], which pushes it to the physical Braille display.
 *
 * 4. **Whenever the content changes, update both values (they can differ!) and push them
 *    together.** See [refreshScreen]: it assigns `mutableViewedImage.value` and
 *    `mutableLiveDots.value` from two different sources ([visualBitmap] and
 *    `brailleScreen.matrix`, respectively). Call [refreshScreen] every time either
 *    representation is rebuilt (e.g. after [composeVisualForBitmapMode],
 *    [composeVisualFromDots] or [drawDotOnVisual]) so the two stay in sync with one another,
 *    even though they show different things.
 *
 * 5. **Rebuild the visual bitmap independently of the Braille dots.** The `compose*` and
 *    `draw*OnVisual` helper functions in this class never touch [brailleScreen]; they only draw
 *    onto [visualCanvas]/[visualBitmap]. This is the key to showing something other than the
 *    Braille dots on screen: as long as you produce a [Bitmap] and assign it through
 *    [mutableViewedImage], the physical Braille display is completely unaffected and can keep
 *    displaying its own, unrelated dots.
 */
class SelfBraillingActivityWithVisualImage : AppCompatActivity() {
    companion object {
        const val TAG = "SelfBraillingActivityWithVisualImage"

        /**
         * Scale factor used to reconstruct a higher resolution visual counterpart of the
         * (much coarser) Braille dots matrix, so the visual screen can show more detail than
         * what is physically felt on the Braille display.
         *
         * The value of 8 is an arbitrary choice made for this demo: it is large enough that the
         * upscaled bitmaps and dots are clearly sharper and more detailed than the original 1
         * pixel = 1 dot Braille matrix (making the difference between the two obvious on
         * screen), while staying small enough that the resulting [visualBitmap] remains a
         * reasonable size to allocate and draw on (e.g. a 96x40 Braille display becomes a
         * 768x320 visual bitmap). In a real integration, this scale would instead be derived
         * from the actual resolution of the source image, as is the case with
         * `ViewerViewModel.refreshData()` in KeyView, where `document.getDisplayImage()` returns
         * the true high resolution image rather than an upscaled version of the dots.
         */
        const val VISUAL_SCALE = 8

        /**
         * X coordinate, in Braille dots, at which the burger drawing is positioned on the
         * screen (used for both [brailleBurger] on [brailleScreen] and [bitmapBurger] on
         * [visualBitmap]).
         */
        const val BURGER_POSITION_X = 10

        /**
         * Y coordinate, in Braille dots, at which the burger drawing is positioned on the
         * screen (used for both [brailleBurger] on [brailleScreen] and [bitmapBurger] on
         * [visualBitmap]).
         */
        const val BURGER_POSITION_Y = 10

        /**
         * X coordinate, in Braille dots, at which the sun drawing is positioned on the screen
         * (used for both [brailleSun] on [brailleScreen] and [bitmapSun] on [visualBitmap]).
         * Chosen so the sun is drawn to the right of the burger, without overlapping it.
         */
        const val SUN_POSITION_X = 60

        /**
         * Y coordinate, in Braille dots, at which the sun drawing is positioned on the screen
         * (used for both [brailleSun] on [brailleScreen] and [bitmapSun] on [visualBitmap]).
         */
        const val SUN_POSITION_Y = 10
    }

    private lateinit var manager: SelfBraillingManager
    private lateinit var widget: SelfBraillingWidget

    // Dots sent to the physical Braille display (low resolution, e.g. 96x40 pins).
    private var mutableLiveDots = MutableLiveData<Array<ByteArray>>()

    // High resolution image shown on the visual screen through the widget.
    private var mutableViewedImage = MutableLiveData<Bitmap>()

    private lateinit var screenDimensions: Size
    private lateinit var brailleScreen: DotsMatrix

    /**
     * Original, full resolution source bitmaps kept aside so the visual screen can display
     * them as-is, as opposed to their down-sampled Braille dots counterpart ([brailleBurger]
     * and [brailleSun]) which is what actually gets sent to the physical Braille display.
     */
    private lateinit var bitmapBurger: Bitmap
    private lateinit var bitmapSun: Bitmap

    private lateinit var brailleBurger: SelfBraillingDrawing
    private lateinit var brailleSun: SelfBraillingDrawing
    private lateinit var brailleRandom: SelfBraillingDrawing
    private lateinit var brailleVerticalLines: SelfBraillingDrawing
    private lateinit var annotationController: SelfBraillingAnnotationController

    /**
     * High resolution visual counterpart of [brailleScreen]. It is rebuilt every time the
     * Braille content changes (see [composeVisualForBitmapMode], [composeVisualFromDots] and
     * [drawDotOnVisual]) and is what gets sent to [SelfBraillingWidget.refresh] for display on
     * screen, while [brailleScreen] is sent to the physical Braille display instead.
     */
    private lateinit var visualBitmap: Bitmap

    /**
     * Canvas used to draw onto [visualBitmap]. It is cleared and redrawn every time the Braille content changes (see [composeVisualForBitmapMode], [composeVisualFromDots] and
     * [drawDotOnVisual]) and is what gets sent to [SelfBraillingWidget.refresh] for display on screen, while [brailleScreen] is sent to the physical Braille display instead.
     */
    private lateinit var visualCanvas: Canvas
    private val visualPaint = Paint()

    private var burgerArea: Rect? = null
    private var sunArea: Rect? = null

    /**
     * Handles the result of [SelfBraillingContextMenuActivity], which lets the user pick between
     * the bitmap, random dots, or vertical lines drawings. In every branch, [brailleScreen] (the
     * Braille destination) and the visual bitmap (via [composeVisualForBitmapMode] or
     * [composeVisualFromDots]) are rebuilt side by side, so switching drawings keeps both
     * destinations in sync even though they may show different content.
     */
    private val startContextMenu = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        clearScreen()

        when (it.resultCode) {
            SelfBraillingActivity.ResultCode.Bitmap.value -> {
                // More than one DotsMatrix may be included on the screen simultaneously.
                brailleScreen.include(brailleBurger.asDotsMatrix(), BURGER_POSITION_X, BURGER_POSITION_Y)
                brailleScreen.include(brailleSun.asDotsMatrix(), SUN_POSITION_X, SUN_POSITION_Y)
                composeVisualForBitmapMode()
                setBitmapModeAnnotations()
            }

            SelfBraillingActivity.ResultCode.Random.value -> {
                // A DotsMatrix may cover the whole screen.
                brailleScreen.include(brailleRandom.asDotsMatrix(), 0, 0)
                composeVisualFromDots(brailleScreen.matrix)
                annotationController.setAnnotations(emptyList())
            }

            SelfBraillingActivity.ResultCode.VerticalLines.value -> {
                // A DotsMatrix can also entirely substitute the Braille screen object.
                brailleScreen = brailleVerticalLines.asDotsMatrix()
                composeVisualFromDots(brailleScreen.matrix)
                annotationController.setAnnotations(emptyList())
            }

            else -> {
                // TODO("This result code is not handled.")
                Log.d(TAG, "This result code is not handled.")
            }
        }
    }

    /**
     * Sets up the activity, including the initial wiring that keeps the visual screen and the
     * physical Braille display as two independent destinations (see the class-level "How to"
     * section above):
     * - `mutableViewedImage` is observed to push [Bitmap] values to [SelfBraillingWidget.refresh]
     *   (visual screen).
     * - `mutableLiveDots` is observed to push dots matrices to [SelfBraillingManager.displayDots]
     *   (physical Braille display).
     *
     * It also creates [visualBitmap]/[visualCanvas] (the visual destination) and [brailleScreen]
     * (the Braille destination), then composes the initial burger/sun drawing on both.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_KeySoftSDKDemoApp)

        manager = SelfBraillingManager(this).apply { bindService() }
        widget = SelfBraillingWidget(this).also { setContentView(it) }

        // This ensures we have access to the full screen without any overlapping bars.
        supportActionBar?.hide()

        // The two LiveData objects are observed by the activity, and when their values change, the corresponding UI components are updated.
        mutableViewedImage.observe(this) { widget.refresh(it) } // Visual screen updates when the visual bitmap changes.
        mutableLiveDots.observe(this) { manager.displayDots(it) } // Braille display updates when the Braille dots matrix changes.

        // The screen dimensions are obtained from the SelfBraillingManager, which provides the size of the Braille display in terms of dots (width and height).
        // This information is used to create a DotsMatrix that represents the Braille display.
        screenDimensions = Size(manager.brailleDisplayDotsSizeX, manager.brailleDisplayDotsSizeY)

        // The brailleScreen is initialized as a DotsMatrix with the dimensions of the Braille display.
        // This matrix will be used to represent the state of the Braille pins on the physical display.
        brailleScreen = DotsMatrix(screenDimensions.height, screenDimensions.width)

        // The visualBitmap is created with dimensions scaled up by VISUAL_SCALE, which allows for a higher resolution representation of the Braille content on the visual screen.
        visualBitmap = createBitmap(screenDimensions.width * VISUAL_SCALE, screenDimensions.height * VISUAL_SCALE)
        visualCanvas = Canvas(visualBitmap)

        // The bitmapBurger and bitmapSun are loaded from the raw resources using BitmapFactory.
        bitmapBurger = BitmapFactory.decodeStream(resources.openRawResource(R.raw.burger))
        bitmapSun = BitmapFactory.decodeStream(resources.openRawResource(R.raw.sun))

        // The brailleBurger and brailleSun are created by converting the loaded bitmaps into Braille representations using the SelfBraillingDrawing.BitmapBraille function.
        brailleBurger = SelfBraillingDrawing.BitmapBraille(bitmapBurger)
        brailleSun = SelfBraillingDrawing.BitmapBraille(bitmapSun)

        // The brailleRandom and brailleVerticalLines are created using procedural generation functions, which generate Braille patterns based on the screen dimensions and specific pattern functions.
        brailleRandom = SelfBraillingDrawing.ProceduralBraille(screenDimensions.width, randomDots(screenDimensions))
        brailleVerticalLines = SelfBraillingDrawing.ProceduralBraille(screenDimensions.width, verticalLines(screenDimensions))

        // The demo starts with a drawing of two bitmap images, a burger and a sun.
        brailleScreen.include(brailleBurger.asDotsMatrix(), BURGER_POSITION_X, BURGER_POSITION_Y)
        brailleScreen.include(brailleSun.asDotsMatrix(), SUN_POSITION_X, SUN_POSITION_Y)
        composeVisualForBitmapMode()

        // The burgerArea and sunArea are defined as Rect objects, which represent the bounding boxes of the burger and sun images on the Braille display.
        burgerArea = Rect(
            BURGER_POSITION_X,
            BURGER_POSITION_Y,
            BURGER_POSITION_X + bitmapBurger.width,
            BURGER_POSITION_Y + bitmapBurger.height
        )
        sunArea = Rect(
            SUN_POSITION_X,
            SUN_POSITION_Y,
            SUN_POSITION_X + bitmapSun.width,
            SUN_POSITION_Y + bitmapSun.height
        )

        annotationController = SelfBraillingAnnotationController(
            this,
            getTouchedPinCoordinates = { manager.touchedPinCoordinates }
        )
        setBitmapModeAnnotations()

        widget.onSelfBraillingWidgetListener = object :
            SelfBraillingWidget.OnSelfBraillingWidgetListener {
            // The onFocused callback is triggered when the SelfBraillingWidget gains focus, indicating that the user has interacted with it. In this case, we want to refresh
            // the screen to ensure that the visual representation and the Braille display are synchronized.
            override fun onFocused() = refreshScreen()

            // The onSingleTapAtBraillePosition callback is triggered when the user performs a single tap gesture on the SelfBraillingWidget at a specific Braille position (pointX, pointY).
            override fun onSingleTapAtBraillePosition(pointX: Int, pointY: Int) {
                Log.d(TAG, "Single tap at: $pointX, $pointY")
            }

            // The onDoubleTapAtBraillePosition callback is triggered when the user performs a double tap gesture on the SelfBraillingWidget at a specific Braille position (pointX, pointY).
            override fun onDoubleTapAtBraillePosition(pointX: Int, pointY: Int) {
                Log.e("POINT", "$pointX, $pointY")
                // TODO: Offset is wrong when clicking using emulator
                brailleScreen.include(DotsMatrix(arrayOf(byteArrayOf(1))), pointX, pointY)
                drawDotOnVisual(pointX, pointY)
                refreshScreen()
            }
        }
    }

    /**
     * Handles key events for the activity, including opening the context menu and responding to specific key combinations for drawing on the Braille display and visual screen.
     * @param keyCode The code of the key that was pressed.
     * @param event The KeyEvent object containing information about the key event.
     * @return true if the key event was handled, false otherwise.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {

            // The KEYCODE_MENU event is handled to launch the SelfBraillingContextMenuActivity,
            // allowing the user to select different drawing modes for the Braille display and visual screen.
            KeyEvent.KEYCODE_MENU -> {
                val contextMenuIntent = Intent(this, SelfBraillingContextMenuActivity::class.java)
                startContextMenu.launch(contextMenuIntent)
                true
            }

            // The KEYCODE_B event is handled to draw the burger and sun images on the Braille display and visual screen when the Ctrl key is pressed.
            KeyEvent.KEYCODE_B -> {
                if (event?.hasModifiers(KeyEvent.META_CTRL_ON) == true) {
                    brailleScreen.include(brailleBurger.asDotsMatrix(), BURGER_POSITION_X, BURGER_POSITION_Y)
                    brailleScreen.include(brailleSun.asDotsMatrix(), SUN_POSITION_X, SUN_POSITION_Y)
                    composeVisualForBitmapMode()
                    setBitmapModeAnnotations()
                    refreshScreen()
                    true
                } else {
                    false
                }
            }

            // The KEYCODE_R event is handled to draw random dots on the Braille display and visual screen when both the Ctrl and Shift keys are pressed.
            KeyEvent.KEYCODE_R -> {
                if (event?.hasModifiers(KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON) == true) {
                    brailleScreen.include(brailleRandom.asDotsMatrix(), 0, 0)
                    composeVisualFromDots(brailleScreen.matrix)
                    annotationController.setAnnotations(emptyList())
                    refreshScreen()
                    true
                } else {
                    false
                }
            }

            // The KEYCODE_V event is handled to draw vertical lines on the Braille display and visual screen when the Ctrl key is pressed.
            KeyEvent.KEYCODE_V -> {
                if (event?.hasModifiers(KeyEvent.META_CTRL_ON) == true) {
                    brailleScreen.include(brailleVerticalLines.asDotsMatrix(), 0, 0)
                    composeVisualFromDots(brailleScreen.matrix)
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
     *
     * This is where the visual screen and the physical Braille display are (re)synchronized:
     * [visualBitmap] (whatever should be seen) and `brailleScreen.matrix` (whatever should be
     * felt) are pushed independently, one to each `LiveData`. Call this function after rebuilding
     * either representation (e.g. after [composeVisualForBitmapMode], [composeVisualFromDots] or
     * [drawDotOnVisual] for the visual side, or after mutating [brailleScreen] for the Braille
     * side) so both destinations reflect their latest, and possibly different, content.
     */
    private fun refreshScreen() {
        mutableViewedImage.value = visualBitmap
        mutableLiveDots.value = brailleScreen.matrix
    }

    /**
     * Clears both the physical Braille display and the visual screen, so the next drawing
     * (bitmaps or upscaled dots) is composed over a blank screen.
     */
    private fun clearScreen() {
        clearBrailleImage()
        clearVisual()
    }

    /**
     * Lowers all pins of [brailleScreen] by filling it with a matrix of pin-down values,
     * so the next drawing (bitmaps or upscaled dots) is composed over a blank Braille display.
     */
    private fun clearBrailleImage() {
        brailleScreen.include(DotsMatrix(Array(screenDimensions.height) {
            ByteArray(screenDimensions.width)
        }), 0, 0)
    }

    /**
     * Clears [visualCanvas] by filling it with a white background, so the next drawing
     * (bitmaps or upscaled dots) is composed over a blank visual.
     */
    private fun clearVisual() {
        visualCanvas.drawColor(Color.WHITE)
    }

    /**
     * Rebuilds [visualBitmap] using the original, full resolution [bitmapBurger] and [bitmapSun]
     * bitmaps, at the same positions used for their Braille dots counterpart ([brailleBurger]
     * and [brailleSun]), so the visual screen shows the true images instead of an upscaled
     * version of the dots sent to the physical Braille display.
     */
    private fun composeVisualForBitmapMode() {
        clearVisual()
        drawHighResBitmapOnVisual(bitmapBurger, BURGER_POSITION_X, BURGER_POSITION_Y)
        drawHighResBitmapOnVisual(bitmapSun, SUN_POSITION_X, SUN_POSITION_Y)
    }

    /**
     * Draws [bitmap] onto [visualCanvas] at position ([x], [y]) expressed in Braille dots
     * coordinates, scaled up by [VISUAL_SCALE] so it renders sharper than the corresponding
     * Braille dots.
     */
    private fun drawHighResBitmapOnVisual(bitmap: Bitmap, x: Int, y: Int) {
        val scaledWidth = bitmap.width * VISUAL_SCALE
        val scaledHeight = bitmap.height * VISUAL_SCALE
        val scaledBitmap = bitmap.scale(scaledWidth, scaledHeight, false)

        visualCanvas.drawBitmap(
            scaledBitmap,
            (x * VISUAL_SCALE).toFloat(),
            (y * VISUAL_SCALE).toFloat(),
            null
        )
    }

    /**
     * Rebuilds [visualBitmap] by drawing [matrix] upscaled by [VISUAL_SCALE], one filled
     * square per dot (black for a raised pin, white otherwise). Used for the procedural
     * drawings (random dots, vertical lines), which have no higher resolution source image to
     * fall back on, unlike [composeVisualForBitmapMode].
     */
    private fun composeVisualFromDots(matrix: Array<ByteArray>) {
        clearVisual()
        for (row in matrix.indices) {
            for (column in matrix[row].indices) {
                visualPaint.color = if (matrix[row][column] == pinUp) Color.BLACK else Color.WHITE
                visualCanvas.drawRect(
                    (column * VISUAL_SCALE).toFloat(),
                    (row * VISUAL_SCALE).toFloat(),
                    ((column + 1) * VISUAL_SCALE).toFloat(),
                    ((row + 1) * VISUAL_SCALE).toFloat(),
                    visualPaint
                )
            }
        }
    }

    /**
     * Reflects a single dot toggled through
     * [SelfBraillingWidget.OnSelfBraillingWidgetListener.onDoubleTapAtBraillePosition] onto
     * [visualCanvas] at position ([x], [y]), so the visual screen and the physical Braille
     * display stay in sync.
     */
    private fun drawDotOnVisual(x: Int, y: Int) {
        visualPaint.color = Color.BLACK
        visualCanvas.drawRect(
            (x * VISUAL_SCALE).toFloat(),
            (y * VISUAL_SCALE).toFloat(),
            ((x + 1) * VISUAL_SCALE).toFloat(),
            ((y + 1) * VISUAL_SCALE).toFloat(),
            visualPaint
        )
    }

    /**
     * Sets the annotations for the bitmap mode, which includes the burger and sun areas. The annotations are defined in Braille dots coordinates, so they match the positions
     * of the corresponding DotsMatrix objects on the Braille display. The visual screen is scaled up by [VISUAL_SCALE], but the annotations are still defined in the original
     * Braille dots coordinates, so they will be scaled up automatically by the [SelfBraillingWidget] when displayed on screen.
     */
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
