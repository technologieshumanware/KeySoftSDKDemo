package com.humanware.keysoftsdkdemoapp.ui

import android.os.Bundle
import android.widget.Button
import com.humanware.keysoftsdk.ui.menu.AccessibleActivity
import com.humanware.keysoftsdk.ui.menu.AccessibleDialog
import com.humanware.keysoftsdkdemoapp.databinding.ActivityAccessibleSampleBinding
import com.humanware.keysoftsdkdemoapp.R as demoR

/**
 * Demonstrates [AccessibleActivity] and [AccessibleDialog].
 *
 * Both classes add first letter navigation to an ordinary layout, with no
 * [AccessibleListView](com.humanware.keysoftsdk.ui.menu.AccessibleListView) involved: typing a
 * letter moves the KeySoft focus to the next item whose label starts with it, and Home and End
 * move it to the first and last item.
 *
 * This screen shows three patterns:
 * - the activity itself: pass the layout to [AccessibleActivity] and look up the views after
 *   calling super in [onCreate]
 * - a floating dialog built by subclassing [AccessibleDialog], see [EditNameDialog]
 * - a full screen dialog using [AccessibleDialog] as is, with no subclass, see [showTermsDialog]
 *
 * When overriding onResume, onPause or onKeyUp, always call super: that is where the superclass
 * attaches the navigation and handles the KeySoft keys.
 *
 * @see demoR.layout.activity_accessible_sample
 */
class AccessibleSampleActivity : AccessibleActivity(demoR.layout.activity_accessible_sample) {

    private lateinit var binding: ActivityAccessibleSampleBinding

    private var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        // Sets the content view and wires the navigation, the views exist from here on.
        super.onCreate(savedInstanceState)
        binding = ActivityAccessibleSampleBinding.bind(findViewById(demoR.id.accessible_sample_root))

        updateNameSummary()
        binding.editNameButton.setOnClickListener { showEditNameDialog() }
        binding.termsButton.setOnClickListener { showTermsDialog() }
    }

    /**
     * Refreshes the summary row with the current [name].
     *
     * The navigation picks up label changes on its own, so typing the first letter of the new
     * summary finds it right away.
     */
    private fun updateNameSummary() {
        val displayedName = name.ifEmpty { getString(demoR.string.accessible_sample_name_not_set) }
        binding.nameSummary.text = getString(demoR.string.accessible_sample_name_summary, displayedName)
    }

    /**
     * Displays a floating [EditNameDialog] and stores the name it returns.
     */
    private fun showEditNameDialog() {
        EditNameDialog(this, name) { newName ->
            name = newName
            updateNameSummary()
        }.show()
    }

    /**
     * Displays a full screen [AccessibleDialog] holding a long, scrolling text.
     *
     * No subclass is needed when the dialog has little logic: create it with its layout, then
     * look up its views once [AccessibleDialog.show] has run, since that is when the content view
     * gets set.
     */
    private fun showTermsDialog() {
        // The published SDK exposes this constructor as Java overloads, so the flag is positional.
        val dialog = AccessibleDialog(this, demoR.layout.dialog_terms, /* fullScreen = */ true)

        dialog.show()
        dialog.findViewById<Button>(demoR.id.close_button).setOnClickListener { dialog.dismiss() }
    }
}
