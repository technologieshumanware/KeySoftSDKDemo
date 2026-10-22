package com.humanware.keysoftsdkdemoapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.humanware.keysoftsdk.SimpleArrayAdapter
import com.humanware.keysoftsdk.settings.SettingsInfoManager
import com.humanware.keysoftsdk.ui.menu.AbstractMenuActivity
import com.humanware.keysoftsdk.ui.menu.AccessibleListView
import com.humanware.keysoftsdk.ui.menu.SimpleAccessibleListViewActivity
import com.humanware.keysoftsdk.ui.menu.accessibleitem.AccessibleItem
import com.humanware.keysoftsdk.ui.menu.accessibleitem.GenericAccessibleItem
import com.humanware.keysoftsdk.ui.menu.accessibleitem.attributes.AccessibleItemAttributes
import com.humanware.keysoftsdkdemoapp.R as demoR


/**
 * Demonstrates how to read and display different groups of SDK settings.
 *
 * This activity uses the modern `GenericAccessibleItem` pattern to build the menu
 * and launches `SimpleAccessibleListViewActivity` to show each selected settings list.
 */
class SettingsInfoActivity : AbstractMenuActivity() {
    private lateinit var settingsInfoManager: SettingsInfoManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsInfoManager = SettingsInfoManager(this)
        settingsInfoManager.bindService()

        initLayout()
    }

    override fun getTitleId() = demoR.string.settings_info

    override fun initialize(): AccessibleListView.Sort {
        return AccessibleListView.Sort.BY_POSITION
    }

    private fun initLayout() {
        listview.clear()

        addSettingsItem(
            label = "All settings",
            title = "All settings",
            loader = { settingsInfoManager.getAllSettings() }
        )
        addSettingsItem(
            label = "Display settings",
            title = "Display settings",
            loader = { settingsInfoManager.getDisplaySettings() }
        )
        addSettingsItem(
            label = "CurrentLanguage profile settings",
            title = "CurrentLanguage profile settings",
            loader = { settingsInfoManager.getCurrentLanguageProfileSettings() }
        )
        addSettingsItem(
            label = "Miscellaneous settings",
            title = "Miscellaneous settings",
            loader = { settingsInfoManager.getMiscellaneousSettings() }
        )
        addSettingsItem(
            label = "Keyboard settings",
            title = "Keyboard settings",
            loader = { settingsInfoManager.getKeyboardSettings() }
        )
        addSettingsItem(
            label = "TTS Settings",
            title = "TTS Settings",
            loader = { settingsInfoManager.getTTSSettings() }
        )

        listview.adapter = SimpleArrayAdapter(this, listview)
        listview.setListener()
    }

    private fun addSettingsItem(label: String, title: String, loader: () -> Map<String, Any?>) {
        listview.setNext(
            GenericAccessibleItem(
                AccessibleItem(
                    AccessibleItemAttributes(
                        label,
                        com.humanware.keysoftsdk.R.layout.one_textview,
                        com.humanware.keysoftsdk.R.id.textview
                    )
                )
            ) {
                launchSettingsResult(title, loader)
            }
        )
    }

    private fun launchSettingsResult(title: String, loader: () -> Map<String, Any?>) {
        val listItems = try {
            loader()
                .toSortedMap()
                .map { (key, value) -> "$key: ${value ?: "null"}" }
                .toCollection(ArrayList())
        } catch (exception: Exception) {
            Log.e(TAG, "Unable to read settings for $title", exception)
            arrayListOf("Error while reading settings", exception.message.orEmpty())
        }

        val intent = Intent(this, SimpleAccessibleListViewActivity::class.java).apply {
            putExtra(SimpleAccessibleListViewActivity.LIST_ITEMS_TITLE_ARG, title)
            putExtra(SimpleAccessibleListViewActivity.LIST_ITEMS_ARG, listItems.toTypedArray())
        }
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        settingsInfoManager.unbindService()
    }
}

