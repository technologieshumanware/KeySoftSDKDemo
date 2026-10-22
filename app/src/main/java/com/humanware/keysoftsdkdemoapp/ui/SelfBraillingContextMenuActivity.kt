package com.humanware.keysoftsdkdemoapp.ui

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.humanware.keysoftsdk.contextmenu.ContextMenuActivity
import com.humanware.keysoftsdkdemoapp.R

/**
 * Implementation of a context menu for the self-brailling widget activity.
 * Allows retaining the state of the activity and opening a context menu offering
 * some options to change the state of the activity.
 */
class SelfBraillingContextMenuActivity : ContextMenuActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<TextView>(com.humanware.keysoftsdk.R.id.contextMenuTitle).apply {
            setText(R.string.context_menu_self_brailling)
        }

        addContextMenuItem("bitmaps")
        addContextMenuItem("random")
        addContextMenuItem("vertical_lines")

        setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> setResult(SelfBraillingActivity.ResultCode.Bitmap.value)
                1 -> setResult(SelfBraillingActivity.ResultCode.Random.value)
                2 -> setResult(SelfBraillingActivity.ResultCode.VerticalLines.value)
                else -> {
                    // TODO("This menu item does not exist.")
                    Log.d(TAG, "This menu item does not exist.")
                }
            }

            finish()
        }
    }
}