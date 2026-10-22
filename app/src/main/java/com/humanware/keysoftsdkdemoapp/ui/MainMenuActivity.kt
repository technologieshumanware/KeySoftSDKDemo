package com.humanware.keysoftsdkdemoapp.ui

import android.content.Intent
import android.widget.TextView
import com.humanware.keysoftsdk.ui.menu.AbstractMenuActivity
import com.humanware.keysoftsdk.ui.menu.AccessibleListView
import com.humanware.keysoftsdk.ui.menu.accessibleitem.AccessibleItem
import com.humanware.keysoftsdk.ui.menu.accessibleitem.attributes.AccessibleItemAttributes
import dagger.hilt.android.AndroidEntryPoint
import com.humanware.keysoftsdk.R as sdkR
import com.humanware.keysoftsdkdemoapp.R as demoR

/**
 * Implements the main menu view of this demo app with a custom menu
 * defined using [AbstractMenuActivity].
 *
 * An [AbstractMenuActivity] contains two components:
 * - a [TextView], the title bar of the activity
 * - an [AccessibleListView], the view containing each clickable item
 *
 * @see sdkR.layout.sdk_settings_layout
 */
@AndroidEntryPoint
class MainMenuActivity : AbstractMenuActivity() {

    /**
     * Gets the resource id for the menu's title.
     *
     * @return the resource id for the string corresponding to the menu's title
     */
    override fun getTitleId() = demoR.string.app_name

    /**
     * Any initialization required to setup the menu items is found here.
     *
     * This is where you should define the sort strategy to use to sort
     * the items displayed in the menu, which should be one of the
     * enum values in [AccessibleListView.Sort].
     *
     * Will be called in the [AbstractMenuActivity.onCreate] method.
     *
     * @return the sort strategy for the menu elements
     */
    override fun initialize(): AccessibleListView.Sort {
        listview = inflateSettingItemLayout()

        return AccessibleListView.Sort.BY_POSITION
    }

    /**
     * Inflates the [AccessibleListView] item in [sdkR.layout.sdk_settings_layout]
     * to create a list of settings for the main menu.
     *
     * Because items are sorted by position as defined in [initialize], setNext is
     * used to place items in order one after the other.
     *
     * @see DialogSampleActivity for an example of layout sorted by label.
     *
     * @return an [AccessibleListView] item constructed from the specifications in [sdkR.layout.accessible_listview_layout]
     */
    private fun inflateSettingItemLayout(): AccessibleListView {
        return findViewById<AccessibleListView>(sdkR.id.settings_listview).apply {
            setNext(makeAccessibleItem(demoR.string.self_brailling_widget))
            setNext(makeAccessibleItem(demoR.string.self_brailling_widget_visual_image))
            setNext(makeAccessibleItem(demoR.string.accessible_dialog))
            setNext(makeAccessibleItem(demoR.string.braille_translation))
            setNext(makeAccessibleItem(demoR.string.util_stuff))
            setNext(makeAccessibleItem(demoR.string.settings_info))
            setNext(makeAccessibleItem(demoR.string.textview_examples))
            setNext(makeAccessibleItem(demoR.string.burger_checklist))
            setNext(makeAccessibleItem(demoR.string.image_viewer))

            setOnItemClickListener { _, _, position, _ ->
                val targetActivity = when (position) {
                    0 -> SelfBraillingActivity::class.java
                    1 -> SelfBraillingActivityWithVisualImage::class.java
                    2 -> DialogSampleActivity::class.java
                    3 -> BrailleTranslatorActivity::class.java
                    4 -> SoundAndVibrationActivity::class.java
                    5 -> SettingsInfoActivity::class.java
                    6 -> TextViewActivity::class.java
                    7 -> CheckListActivity::class.java
                    8 -> ImageViewerMenuActivity::class.java
                    else -> throw NotImplementedError("Invalid menu item.")
                }
                startActivity(Intent(this@MainMenuActivity, targetActivity))
            }
        }
    }

    /**
     * Creates an [AccessibleItem] to be displayed in the menu's [AccessibleListView].
     *
     * @param labelNameId the name of the item to be displayed
     * @param layoutId the id of the layout for the menu item
     * @param labelId the id of the label item
     *
     * @return an [AccessibleItem] to be inserted in the main menu
     */
    private fun makeAccessibleItem(
        labelNameId: Int,
        layoutId: Int = sdkR.layout.one_textview,
        labelId: Int = sdkR.id.textview
    ): AccessibleItem {
        return AccessibleItem(
            AccessibleItemAttributes(
                resources.getString(labelNameId),
                layoutId,
                labelId
            )
        )
    }

}