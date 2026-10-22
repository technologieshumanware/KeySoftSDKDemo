package com.humanware.keysoftsdkdemoapp.ui

import android.content.DialogInterface
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.humanware.keysoftsdk.R
import com.humanware.keysoftsdk.ui.dialog.DialogCreator
import com.humanware.keysoftsdk.ui.menu.AbstractMenuActivity
import com.humanware.keysoftsdk.ui.menu.AccessibleListView
import com.humanware.keysoftsdk.ui.menu.accessibleitem.AccessibleItem
import com.humanware.keysoftsdk.ui.menu.accessibleitem.attributes.AccessibleItemAttributes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.humanware.keysoftsdk.R as sdkR
import com.humanware.keysoftsdkdemoapp.R as demoR

/**
 * Implements the menu showcasing various common dialog types offered by the SDK.
 *
 * @see sdkR.layout.sdk_settings_layout
 */
class DialogSampleActivity : AbstractMenuActivity() {
    /**
     * Gets the resource id for the menu's title.
     *
     * @return the resource id for the string corresponding to the menu's title
     */
    override fun getTitleId() = demoR.string.dialog_activity_title

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
        listview = inflateDialogSampleItemLayout()

        return AccessibleListView.Sort.BY_LABEL
    }

    /**
     * Inflates the [AccessibleListView] item in [sdkR.layout.sdk_settings_layout]
     * to create a list of dialog sample items for the menu.
     *
     * Because items are sorted by label as defined in [initialize], addAccessibleItem
     * is used to place the items in the [listview] in any order, before the items
     * are eventually sorted.
     *
     * @see MainMenuActivity for an example of layout sorted by position.
     *
     * @return an [AccessibleListView] item constructed from the specifications
     * in [sdkR.layout.accessible_listview_layout]
     */
    private fun inflateDialogSampleItemLayout(): AccessibleListView {
        return findViewById<AccessibleListView>(R.id.settings_listview).apply {
            addAccessibleItem(makeAccessibleItem(demoR.string.simple_dialog))
            addAccessibleItem(makeAccessibleItem(demoR.string.loading_dialog))
            addAccessibleItem(makeAccessibleItem(demoR.string.progress_dialog))
            addAccessibleItem(makeAccessibleItem(demoR.string.ok_dialog))
            addAccessibleItem(makeAccessibleItem(demoR.string.ok_cancel_dialog))
            addAccessibleItem(makeAccessibleItem(demoR.string.list_dialog))


            // Notice the difference in order from how they were added to the [AccessibleListView].
            // This is due to the [AccessibleListView.Sort.BY_LABEL] in the [initialize] function.
            // To keep the order in which the items were added, use [AccessibleListView.Sort.BY_POSITION]
            // as in [MainMenuActivity].
            setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> showLoadingDialog()
                    1 -> showOkDialog()
                    2 -> showOkCancelDialog()
                    3 -> showProgressDialog()
                    4 -> showSimpleDialog()
                    5 -> showListDialog()
                    else -> throw NotImplementedError("Invalid menu item.")
                }
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

    /**
     * Displays a basic dialog which may be closed upon the user pressing the only
     * button available, called the neutral button.
     */
    private fun showSimpleDialog() {
        val onNeutralClick = { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        val dialog = DialogCreator.createDialog(
            this,
            resources.getString(demoR.string.simple_dialog),
            resources.getString(demoR.string.ok),
            resources.getString(demoR.string.simple_dialog_content),
            onNeutralClick
        )

        dialog.show()
    }

    /**
     * Displays a message prompting the user to wait for a task to be completed.
     *
     * The dialog automatically closes upon completion of the task.
     */
    private fun showLoadingDialog() {
        val dialog = DialogCreator.createLoading(
            this,
            resources.getString(sdkR.string.please_wait)
        )

        dialog.show()

        // This only simulates a workload that takes time to perform.
        // The actual work performed by your application would go here.
        lifecycleScope.launch {
            simulateIntensiveWork(5000)
        }.invokeOnCompletion { dialog.dismiss() }

    }

    /**
     * Displays a message prompting the user to wait for a task to be completed.
     *
     * The dialog automatically closes upon completion of the task and may be closed
     * earlier by the user upon selecting cancel.
     *
     * TODO: Progress value not displayed in Braille unless focus moved from "Please wait"
     *       to the percentage value.
     */
    private fun showProgressDialog() {
        val progressRange = (0..100)

        val onCancelClick = { dialog: DialogInterface -> dialog.cancel() }
        val dialog = DialogCreator.createProgressDialog(
            this,
            getString(sdkR.string.please_wait),
            onCancelClick
        )

        dialog.show()

        // This only simulates a workload that takes time to perform.
        // Your actual application may want to perform intensive work as a separate coroutine
        // and update the ProgressDialog bar on the main thread using another coroutine
        // running concurrently.
        // TODO: potentially refactor the usage of simulateIntensiveWork to reflect this more closely,
        //       but it visually works for now.
        lifecycleScope.launch {
            progressRange.forEach { progressLevel ->
                simulateIntensiveWork(250)
                mainExecutor.execute { dialog.setProgress(progressLevel) }
            }
        }.invokeOnCompletion { dialog.dismiss() }

    }

    /**
     * Displays a message to the user, which they may then close by pressing the OK item.
     */
    private fun showOkDialog() {
        val onOkClick = { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        val dialog = DialogCreator.createOkDialog(
            this,
            resources.getString(demoR.string.ok_title),
            resources.getString(demoR.string.ok_only),
            onOkClick
        )

        dialog.show()
    }

    /**
     * Displays a message to the user and prompts them to choose between an positive (OK)
     * and negative (Cancel) item.
     */
    private fun showOkCancelDialog() {
        val onCancelClick = { dialog: DialogInterface, _: Int -> dialog.cancel() }
        val onOkClick = { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        val dialog = DialogCreator.createOkCancelDialog(
            this,
            resources.getString(demoR.string.ok_or_cancel),
            onCancelClick,
            onOkClick
        )

        dialog.show()
    }

    /**
     * Displays a list of items to the user, which they may select from and confirm their selection
     * by pressing the Apply item, or cancel their selection by pressing the Cancel item.
     */
    private fun showListDialog() {
        // Each entry is displayed as a row: key = first text, value = second text.
        // LinkedHashMap keeps the insertion order, which maps to the position
        // received by the item click listener.
        val items = linkedMapOf(
            resources.getString(demoR.string.list_item_1) to resources.getString(demoR.string.list_item_1_detail),
            resources.getString(demoR.string.list_item_2) to resources.getString(demoR.string.list_item_2_detail),
            resources.getString(demoR.string.list_item_3) to resources.getString(demoR.string.list_item_3_detail)
        )
        val labels = items.keys.toList()
        var selectedPosition = -1

        val dialog = DialogCreator.createListDialog(
            this,
            resources.getString(demoR.string.list_dialog_title),
            items
        )

        // Back button and touch outside no longer close the dialog: the user must use Apply
        // or the Cancel button. Must be called after createListDialog, while the dialog exists.
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        // Clicking a row only records the selection; Apply confirms it.
        dialog.setItemClickListener { position -> selectedPosition = position }
        dialog.setDialogListClickListener {
            val message = if (selectedPosition in labels.indices) {
                resources.getString(demoR.string.list_item_selected, labels[selectedPosition])
            } else {
                resources.getString(demoR.string.list_item_none_selected)
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (selectedPosition in labels.indices) dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * This is used to simulate some CPU-intensive work that would require the use of a
     * [LoadingDialog](com.humanware.keysoftsdk.ui.dialog.LoadingDialog) or
     * [ProgressDialog](com.humanware.keysoftsdk.ui.dialog.ProgressDialog).
     * The actual work performed by your application would replace this function.
     *
     * @param ms duration of the delay in milliseconds
     */
    private suspend fun simulateIntensiveWork(ms: Long) = delay(ms)
}