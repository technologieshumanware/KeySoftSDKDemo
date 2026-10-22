package com.humanware.keysoftsdkdemoapp.ui

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.humanware.keysoftsdk.imageviewer.widget.ImageViewerWidget
import com.humanware.keysoftsdk.ui.menu.AbstractMenuActivity
import com.humanware.keysoftsdk.ui.menu.AccessibleListView
import com.humanware.keysoftsdk.ui.menu.accessibleitem.AccessibleItem
import com.humanware.keysoftsdk.ui.menu.accessibleitem.attributes.AccessibleItemAttributes
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import com.humanware.keysoftsdk.R as sdkR
import com.humanware.keysoftsdkdemoapp.R as demoR

/**
 * Lists the documents that can be opened in [ImageViewerActivity] to try the [ImageViewerWidget].
 *
 * Bundled samples (BMP, SVG and multi-page PDF) are copied from `res/raw` to the app's
 * cache so they can be passed to the widget as `file://` URIs: the widget detects the
 * document format from the MIME type or the file extension, and does not support
 * `android.resource://` URIs. The last item opens the system document picker, which
 * returns a `content://` URI.
 */
@AndroidEntryPoint
class ImageViewerMenuActivity : AbstractMenuActivity() {
    companion object {
        const val TAG = "ImageViewerMenuActivity"

        private val SUPPORTED_MIME_TYPES = arrayOf(
            "application/pdf",
            "image/svg+xml",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
        )
    }

    private data class Sample(@StringRes val labelId: Int, @RawRes val rawId: Int, val fileName: String)

    private val samples = listOf(
        Sample(demoR.string.image_viewer_sample_bitmap, demoR.raw.burger, "burger.bmp"),
        Sample(demoR.string.image_viewer_sample_svg, demoR.raw.sample_shapes, "sample_shapes.svg"),
        Sample(demoR.string.image_viewer_sample_pdf, demoR.raw.sample_document, "sample_document.pdf"),
    )

    private val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { startActivity(ImageViewerActivity.newIntent(this, it)) }
    }

    override fun getTitleId() = demoR.string.image_viewer

    override fun initialize(): AccessibleListView.Sort {
        listview = findViewById<AccessibleListView>(sdkR.id.settings_listview).apply {
            samples.forEach { setNext(makeAccessibleItem(it.labelId)) }
            setNext(makeAccessibleItem(demoR.string.image_viewer_open_from_storage))

            setOnItemClickListener { _, _, position, _ ->
                if (position < samples.size) {
                    openSample(samples[position])
                } else {
                    openDocument.launch(SUPPORTED_MIME_TYPES)
                }
            }
        }

        return AccessibleListView.Sort.BY_POSITION
    }

    private fun openSample(sample: Sample) {
        try {
            startActivity(ImageViewerActivity.newIntent(this, copyRawToCache(sample)))
        } catch (e: IOException) {
            Log.e(TAG, "Unable to prepare ${sample.fileName}", e)
            Toast.makeText(this, demoR.string.image_viewer_no_document, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Copies a raw resource to the cache directory, keeping a file name with the right
     * extension so the widget can detect the document format.
     */
    private fun copyRawToCache(sample: Sample): Uri {
        val file = File(cacheDir, sample.fileName)
        resources.openRawResource(sample.rawId).use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return Uri.fromFile(file)
    }

    private fun makeAccessibleItem(@StringRes labelNameId: Int) = AccessibleItem(
        AccessibleItemAttributes(
            resources.getString(labelNameId),
            sdkR.layout.one_textview,
            sdkR.id.textview
        )
    )
}
