package com.humanware.keysoftsdkdemoapp.ui

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.humanware.keysoftsdk.ui.menu.AbstractMenuActivity
import com.humanware.keysoftsdk.ui.menu.AccessibleListView
import com.humanware.keysoftsdk.ui.menu.StableArrayAdapter
import com.humanware.keysoftsdk.ui.menu.accessibleitem.AccessibleItem
import com.humanware.keysoftsdk.ui.menu.accessibleitem.GenericAccessibleItem
import com.humanware.keysoftsdk.ui.menu.accessibleitem.IActionItem
import com.humanware.keysoftsdk.ui.menu.accessibleitem.attributes.AccessibleItemAttributes
import com.humanware.keysoftsdk.ui.menu.accessibleitem.attributes.CheckableAccessibleItemAttributes
import com.humanware.keysoftsdkdemoapp.R as demoR
import com.humanware.keysoftsdkdemoapp.databinding.BurgerOptionsBinding
import com.humanware.keysoftsdk.R as sdkR

/**
 * Demo menu showing how to compose an accessible, stateful UI with the SDK menu API.
 *
 * This activity highlights three SDK patterns:
 * - A regular [AbstractMenuActivity] screen that shows the current burger state.
 * - Action rows built with [GenericAccessibleItem] + [AccessibleItemAttributes] to open custom flows.
 * - Checkable rows built with [GenericAccessibleItem] + [CheckableAccessibleItemAttributes]
 *   inside a custom dialog, to toggle options in place.
 *
 * The dialog content uses an [AccessibleListView], so interaction stays consistent with the
 * keyboard-first navigation model described in `docs/concepts/menu.md` and `docs/concepts/dialogs.md`.
 */
class CheckListActivity : AbstractMenuActivity() {
    private lateinit var burgerOptions: BurgerOptions
    private var dialog: AlertDialog? = null

    override fun getTitleId() = demoR.string.burger_checklist

    override fun initialize(): AccessibleListView.Sort {
        burgerOptions = BurgerOptions()
        return AccessibleListView.Sort.BY_POSITION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateBurgerDisplay()
    }

    /**
     * Rebuilds the main list with:
     * 1) a read-only summary row of the current burger composition
     * 2) an action row that opens the custom configuration dialog
     */
    private fun updateBurgerDisplay() {
        listview.clear()

        // Display current burger state
        val ingredients = buildList {
            if (burgerOptions.hasTomatoes) add(getString(demoR.string.burger_ingredient_tomatoes))
            if (burgerOptions.hasLettuce) add(getString(demoR.string.burger_ingredient_lettuce))
            if (burgerOptions.hasPickles) add(getString(demoR.string.burger_ingredient_pickles))
            if (burgerOptions.hasCheese) add(getString(demoR.string.burger_ingredient_cheese))
        }

        val burgerState = if (ingredients.isEmpty()) {
            getString(demoR.string.burger_state_empty)
        } else {
            getString(demoR.string.burger_state_with_ingredients, ingredients.joinToString(", "))
        }

        listview.setNext(
            AccessibleItem(
                AccessibleItemAttributes(
                    burgerState,
                    sdkR.layout.one_textview,
                    sdkR.id.textview
                )
            )
        )

        // Add button to customize burger
        listview.setNext(
            GenericAccessibleItem(
                AccessibleItem(
                    AccessibleItemAttributes(
                        getString(demoR.string.burger_customize_action),
                        sdkR.layout.one_textview,
                        sdkR.id.textview
                    )
                )
            ) {
                openBurgerConfiguration()
            }
        )

        listview.adapter = StableArrayAdapter(this, listview)
    }

    /**
     * Opens a custom [AlertDialog] embedding an [AccessibleListView] with checkable options.
     *
     * The dialog writes changes directly into [burgerOptions]; pressing "Done" refreshes the
     * summary shown in the main activity list.
     */
    private fun openBurgerConfiguration() {
        val burgerOptionsBinding = BurgerOptionsBinding.inflate(layoutInflater)

        dialog = AlertDialog.Builder(this)
            .setTitle(demoR.string.burger_dialog_remove_ingredients_title)
            .setView(burgerOptionsBinding.root)
            .setPositiveButton(demoR.string.common_done) { _, _ ->
                updateBurgerDisplay()
            }
            .setNegativeButton(demoR.string.common_cancel) { _, _ ->
                // Don't update on cancel
            }
            .create()

        val burgerConfigListView = burgerOptionsBinding.configList
        burgerConfigListView.initialize(this, AccessibleListView.Sort.BY_POSITION)
        burgerConfigListView.setOnItemClickListener { _, _, position, _ ->
            val itemEntry = burgerConfigListView.getItemAtPosition(position) ?: return@setOnItemClickListener
            val item = itemEntry.key
            if (item is IActionItem) {
                item.onAction()
            }
        }

        initializeConfigMenu(burgerConfigListView)

        dialog?.show()
    }

    /**
     * Populates the dialog list with checkable "remove ingredient" items.
     *
     * Each row uses [CheckableAccessibleItemAttributes] and updates [burgerOptions] when toggled.
     */
    private fun initializeConfigMenu(burgerConfigListView: AccessibleListView) {
        burgerConfigListView.clear()

        burgerConfigListView.setNext(
            GenericAccessibleItem(
                AccessibleItem(
                    CheckableAccessibleItemAttributes(
                        getString(demoR.string.burger_option_no_tomatoes),
                        demoR.layout.checkbox,
                        demoR.id.compoundCheckBox,
                        !burgerOptions.hasTomatoes,
                        demoR.id.compoundCheckBox
                    )
                )
            ) { item ->
                val itemAttr = item.attributes as CheckableAccessibleItemAttributes
                val newState = !itemAttr.checkboxValue
                itemAttr.checkboxValue = newState
                burgerOptions.hasTomatoes = !newState
            }
        )

        burgerConfigListView.setNext(
            GenericAccessibleItem(
                AccessibleItem(
                    CheckableAccessibleItemAttributes(
                        getString(demoR.string.burger_option_no_lettuce),
                        demoR.layout.checkbox,
                        demoR.id.compoundCheckBox,
                        !burgerOptions.hasLettuce,
                        demoR.id.compoundCheckBox
                    )
                )
            ) { item ->
                val itemAttr = item.attributes as CheckableAccessibleItemAttributes
                val newState = !itemAttr.checkboxValue
                itemAttr.checkboxValue = newState
                burgerOptions.hasLettuce = !newState
            }
        )

        burgerConfigListView.setNext(
            GenericAccessibleItem(
                AccessibleItem(
                    CheckableAccessibleItemAttributes(
                        getString(demoR.string.burger_option_no_pickles),
                        demoR.layout.checkbox,
                        demoR.id.compoundCheckBox,
                        !burgerOptions.hasPickles,
                        demoR.id.compoundCheckBox
                    )
                )
            ) { item ->
                val itemAttr = item.attributes as CheckableAccessibleItemAttributes
                val newState = !itemAttr.checkboxValue
                itemAttr.checkboxValue = newState
                burgerOptions.hasPickles = !newState
            }
        )

        burgerConfigListView.setNext(
            GenericAccessibleItem(
                AccessibleItem(
                    CheckableAccessibleItemAttributes(
                        getString(demoR.string.burger_option_no_cheese),
                        demoR.layout.checkbox,
                        demoR.id.compoundCheckBox,
                        !burgerOptions.hasCheese,
                        demoR.id.compoundCheckBox
                    )
                )
            ) { item ->
                val itemAttr = item.attributes as CheckableAccessibleItemAttributes
                val newState = !itemAttr.checkboxValue
                itemAttr.checkboxValue = newState
                burgerOptions.hasCheese = !newState
            }
        )

        burgerConfigListView.adapter = StableArrayAdapter(this, burgerConfigListView)
    }
}

// Simple data class to store the CheckBoxValues
data class BurgerOptions(
    var hasTomatoes: Boolean = true,
    var hasLettuce: Boolean = true,
    var hasPickles: Boolean = true,
    var hasCheese: Boolean = true
)