package com.humanware.keysoftsdkdemoapp.ui

import android.os.Bundle
import com.humanware.keysoftsdk.SimpleArrayAdapter
import com.humanware.keysoftsdk.ui.menu.AbstractMenuActivity
import com.humanware.keysoftsdk.ui.menu.AccessibleListView
import com.humanware.keysoftsdk.ui.menu.accessibleitem.AccessibleItem
import com.humanware.keysoftsdk.ui.menu.accessibleitem.GenericAccessibleItem
import com.humanware.keysoftsdk.ui.menu.accessibleitem.attributes.AccessibleItemAttributes
import com.humanware.keysoftsdk.utils.SoundUtils
import com.humanware.keysoftsdk.utils.VibrationUtils
import com.humanware.keysoftsdkdemoapp.R as demoR

/**
 * Demo activity for SDK utility features related to audio and haptic feedback.
 *
 * It exposes two actions in an accessible list:
 * - play an error beep via [SoundUtils.errorBeep]
 * - trigger a short vibration via [VibrationUtils.startVibration]
 *
 * Items are built with [GenericAccessibleItem], showcasing a modern way to define
 * a ListView-based menu in [AbstractMenuActivity].
 */
class SoundAndVibrationActivity : AbstractMenuActivity() {

    override fun getTitleId() = demoR.string.util_activity_title

    override fun initialize(): AccessibleListView.Sort {
        return AccessibleListView.Sort.BY_POSITION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initUtilItemLayout()
    }

    private fun initUtilItemLayout() {
        listview.clear()

        listview.setNext(
            GenericAccessibleItem(
                AccessibleItem(
                    AccessibleItemAttributes(
                        getString(demoR.string.util_play_error_beep),
                        com.humanware.keysoftsdk.R.layout.one_textview,
                        com.humanware.keysoftsdk.R.id.textview
                    )
                )
            )
            {
                SoundUtils.errorBeep(this@SoundAndVibrationActivity)
            }
        )

        listview.setNext(
            GenericAccessibleItem(
                AccessibleItem(
                    AccessibleItemAttributes(
                        getString(demoR.string.util_vibrate_short),
                        com.humanware.keysoftsdk.R.layout.one_textview,
                        com.humanware.keysoftsdk.R.id.textview
                    )
                )
            )
            {
                VibrationUtils.startVibration(this@SoundAndVibrationActivity, 200)
            }
        )

        listview.adapter = SimpleArrayAdapter(this, listview)
        listview.setListener()
    }
}
