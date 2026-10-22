package com.humanware.keysoftsdkdemoapp.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.humanware.keysoftsdk.imageviewer.widget.ImageViewerWidget
import com.humanware.keysoftsdkdemoapp.R

/**
 * Implements a sample usage of the [ImageViewerWidget] provided by the
 * `KeySoftSDK-imageviewer` module.
 *
 * The widget is a drop-in document viewer: give it a [Uri] pointing to a PDF, SVG or
 * raster image (JPEG, PNG, GIF, BMP) and it renders the document on screen while
 * stamping a tactile version of the same view on the Braille display.
 *
 * The document to display is passed as the intent's data. Only `file://` and
 * `content://` URIs are supported by the widget.
 *
 * @see ImageViewerMenuActivity for how sample documents are prepared and passed to this activity.
 */
class ImageViewerActivity : AppCompatActivity() {
    companion object {
        const val TAG = "ImageViewerActivity"

        /**
         * Creates an intent opening [uri] in this activity.
         *
         * Read permission is forwarded so that `content://` URIs returned by the
         * system document picker remain readable from this activity.
         */
        fun newIntent(context: Context, uri: Uri): Intent =
            Intent(context, ImageViewerActivity::class.java)
                .setData(uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private lateinit var widget: ImageViewerWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_KeySoftSDKDemoApp)

        val uri = intent.data
        if (uri == null) {
            Toast.makeText(this, R.string.image_viewer_no_document, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // The widget owns its own SelfBraillingManager: binding and unbinding to the
        // self-brailling service follow the widget's attach/detach lifecycle.
        widget = ImageViewerWidget(this).also { setContentView(it) }

        // This ensures we have access to the full screen without any overlapping bars.
        supportActionBar?.hide()

        // Called on the main thread each time a page load or a zoom/pan/rotate action
        // has finished refreshing both the screen and the Braille display.
        widget.onViewRefreshed = {
            Log.d(TAG, "Page ${widget.getCurrentPageIndex() + 1} of ${widget.getPageCount()} refreshed")
        }

        // loadDocument may be called right after construction, even before the widget is attached.
        widget.loadDocument(uri)
    }

    /**
     * Forwards physical key events to the widget so its built-in zoom, pan and page
     * shortcuts work, and adds a few shortcuts showing how to call the widget's API directly.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val ctrl = event?.hasModifiers(KeyEvent.META_CTRL_ON) == true
        return when {
            ctrl && keyCode == KeyEvent.KEYCODE_R -> { widget.rotate(); true }
            ctrl && keyCode == KeyEvent.KEYCODE_HOME -> { logNavigation(widget.goToPage(0)); true }
            ctrl && keyCode == KeyEvent.KEYCODE_PAGE_DOWN -> { logNavigation(widget.nextPage()); true }
            ctrl && keyCode == KeyEvent.KEYCODE_PAGE_UP -> { logNavigation(widget.previousPage()); true }
            ctrl && keyCode == KeyEvent.KEYCODE_H -> { widget.toggleHaptics(); true }
            widget.handleKeyEvent(keyCode, event) -> true
            else -> super.onKeyDown(keyCode, event)
        }
    }

    // Avoid Toasts or other windows while viewing: KeySoft announces them on the Braille
    // display, which replaces the dots drawn by the widget.
    private fun logNavigation(navigated: Boolean) {
        if (navigated) {
            Log.d(TAG, "Going to page ${widget.getCurrentPageIndex() + 1} of ${widget.getPageCount()}")
        } else {
            Log.d(TAG, "No page in that direction")
        }
    }
}
