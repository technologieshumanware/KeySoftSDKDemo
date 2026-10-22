package com.humanware.keysoftsdkdemoapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.result.contract.ActivityResultContracts
import com.humanware.keysoftsdk.ui.menu.AccessibleActivity
import com.humanware.keysoftsdkdemoapp.ui.SelfBraillingActivity.Companion.TAG
import com.humanware.keysoftsdkdemoapp.ui.SelfBraillingActivity.ResultCode
import com.humanware.keysoftsdkdemoapp.R as demoR

/**
 * Demonstrates an `EditText` widget for text input.
 *
 * Uses the new `AccessibleActivity` superclass.
 */
class TextViewActivity : AccessibleActivity(demoR.layout.activity_textview) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}