package com.humanware.keysoftsdkdemoapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import com.humanware.keysoftsdk.ui.menu.AccessibleActivity
import com.humanware.keysoftsdkdemoapp.databinding.ActivityMainMenuBinding
import com.humanware.keysoftsdkdemoapp.databinding.ItemMainMenuBinding
import dagger.hilt.android.AndroidEntryPoint
import com.humanware.keysoftsdkdemoapp.R as demoR

/**
 * The launcher screen of this demo app: a menu with one item per demo.
 *
 * It is built with [AccessibleActivity], the recommended base class for new screens. The menu is
 * an ordinary layout, a title and a column of clickable TextViews styled like the rows of the
 * SDK's list based menus. [AccessibleActivity] adds the KeySoft navigation on top of it:
 * - first letter navigation: typing a letter moves the focus to the next item whose label starts
 *   with it. For example, T moves to "Translate input to Braille", and T again to
 *   "TextView Examples";
 * - Home and End move the focus to the first and last item.
 *
 * The menu shows two ways to add an item. The user sees a single list, and the navigation handles
 * both kinds of item the same way:
 * - **Declared in the layout**, the classic Android approach: the TextView is written in
 *   [demoR.layout.activity_main_menu] with an id, and [onCreate] sets its click listener through
 *   view binding. Simplest to read, and best for a few fixed items, or items that each do
 *   something different.
 * - **Generated from a list**: [menuEntries] lists demos, and [addMenuItem] inflates one
 *   [demoR.layout.item_main_menu] per entry into the `menu_items` container. Best when many items
 *   behave the same way, since adding one is a single line.
 *
 * To add a demo, use either approach, then declare the demo's activity in the manifest. Nothing
 * else is needed: the navigation finds the new item by its label.
 *
 * This menu used to extend [AbstractMenuActivity](com.humanware.keysoftsdk.ui.menu.AbstractMenuActivity),
 * with its content modelled as
 * [AccessibleItem](com.humanware.keysoftsdk.ui.menu.accessibleitem.AccessibleItem)s in an
 * [AccessibleListView](com.humanware.keysoftsdk.ui.menu.AccessibleListView) and clicks mapped to
 * activities by position. [DialogSampleActivity] and [ImageViewerMenuActivity] still use that
 * approach, which keeps working for existing list based menus.
 *
 * As for any [AccessibleActivity], call super when overriding onCreate, onResume, onPause or
 * onKeyUp.
 *
 * The manifest declares no theme for this activity. [AccessibleActivity] then applies the SDK's
 * AppCompatTheme, the theme of the SDK's list based menus, so this menu looks like them. This
 * fallback requires KeySoftSDK 0.1.179 or later: earlier versions crash without an AppCompat theme.
 *
 * @see demoR.layout.activity_main_menu
 * @see demoR.style.Widget_KeySoftSDKDemoApp_MenuItem for the look and behavior of an item
 * @see AccessibleSampleActivity for a screen mixing text, buttons, a checkbox and dialogs
 */
@AndroidEntryPoint
class MainMenuActivity : AccessibleActivity(demoR.layout.activity_main_menu) {

    /**
     * One demo listed in the generated part of the menu.
     *
     * @property labelId the item's text, which is also the label first letter navigation matches
     * @property target the activity started when the item is pressed
     */
    private data class MenuEntry(@StringRes val labelId: Int, val target: Class<out Activity>)

    /**
     * The demos generated from a list, in display order.
     *
     * The navigation follows the order of the views in the layout. The `menu_items` container
     * comes before the declared items in [demoR.layout.activity_main_menu], so these demos are
     * listed first.
     */
    private val menuEntries = listOf(
        MenuEntry(demoR.string.self_brailling_widget, SelfBraillingActivity::class.java),
        MenuEntry(demoR.string.self_brailling_widget_visual_image, SelfBraillingActivityWithVisualImage::class.java),
        MenuEntry(demoR.string.accessible_dialog, DialogSampleActivity::class.java),
        MenuEntry(demoR.string.braille_translation, BrailleTranslatorActivity::class.java),
        MenuEntry(demoR.string.util_stuff, SoundAndVibrationActivity::class.java),
        MenuEntry(demoR.string.settings_info, SettingsInfoActivity::class.java),
        MenuEntry(demoR.string.textview_examples, TextViewActivity::class.java),
        MenuEntry(demoR.string.burger_checklist, CheckListActivity::class.java),
    )

    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Sets the content view and creates the navigation, the views exist from here on.
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.bind(findViewById(demoR.id.main_menu_root))

        // Generated items: one TextView per entry, added to the menu_items container.
        menuEntries.forEach { addMenuItem(it) }

        // Declared items, the classic approach: the TextViews already exist in the layout, so
        // only their click listeners are set here.
        binding.imageViewerItem.setOnClickListener {
            startActivity(Intent(this, ImageViewerMenuActivity::class.java))
        }
        binding.accessibleSampleItem.setOnClickListener {
            startActivity(Intent(this, AccessibleSampleActivity::class.java))
        }
    }

    /**
     * Adds an item for [entry] at the end of the `menu_items` container.
     *
     * Each item gets its own click listener, so there is no position to map back to an activity.
     * Adding views at runtime needs no extra call either: the navigation rebuilds its items
     * whenever the layout changes.
     *
     * @param entry the demo the item opens
     */
    private fun addMenuItem(entry: MenuEntry) {
        val item = ItemMainMenuBinding.inflate(layoutInflater, binding.menuItems, false).root
        item.setText(entry.labelId)
        item.setOnClickListener { startActivity(Intent(this, entry.target)) }
        binding.menuItems.addView(item)
    }
}