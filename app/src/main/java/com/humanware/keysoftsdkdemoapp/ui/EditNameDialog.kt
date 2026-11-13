package com.humanware.keysoftsdkdemoapp.ui

import android.content.Context
import android.os.Bundle
import com.humanware.keysoftsdk.ui.menu.AccessibleDialog
import com.humanware.keysoftsdkdemoapp.databinding.DialogEditNameBinding
import com.humanware.keysoftsdkdemoapp.R as demoR

/**
 * Floating [AccessibleDialog] asking the user for a name.
 *
 * Shows the subclass pattern: the dialog gives its layout to the superclass, then binds its views
 * in [onCreate], after calling super, because the content view only exists from that point on.
 * [onCreate] runs on the first [show], not when the dialog is constructed.
 *
 * Pass true as the superclass's third argument, fullScreen, to cover the whole screen instead, as
 * [AccessibleSampleActivity] does for its terms of use.
 *
 * @param context the context the dialog is shown in, usually the calling activity
 * @param currentName the name the text field is filled with when the dialog opens
 * @param onSave called with the new name when the user presses Save
 *
 * @see demoR.layout.dialog_edit_name
 */
class EditNameDialog(
    context: Context,
    private val currentName: String,
    private val onSave: (String) -> Unit
) : AccessibleDialog(context, demoR.layout.dialog_edit_name) {

    private lateinit var binding: DialogEditNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Sets the content view and wires the navigation, rootView is ready from here on.
        super.onCreate(savedInstanceState)
        binding = DialogEditNameBinding.bind(rootView)

        binding.nameInput.setText(currentName)
        binding.saveButton.setOnClickListener {
            onSave(binding.nameInput.text.toString().trim())
            dismiss()
        }
        binding.cancelButton.setOnClickListener { cancel() }
    }
}
