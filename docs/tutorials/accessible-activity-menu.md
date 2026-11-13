# Building a menu with AccessibleActivity

This tutorial builds a menu screen with `AccessibleActivity`, the recommended base class for new screens. It follows [MainMenuActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/MainMenuActivity.kt), the launcher screen of this demo app.

Most of it applies to any screen built with `AccessibleActivity`, not only to menus: the theme ([step 1](#1-declare-the-activity)), what the navigation finds ([step 2](#2-write-the-screen-layout)) and the [rules](#rules) a subclass must follow. For an overview of the SDK's menu building blocks, see the [Menu concept](../concepts/menu.md).

## What you get

The menu is an ordinary layout: a title, then a column of clickable `TextView`s. It looks like the SDK's list based menus, such as [DialogSampleActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/DialogSampleActivity.kt). `AccessibleActivity` adds the navigation KeySoft users expect on top of it:

- [First Letter Navigation (FLN)](../glossary.md#first-letter-navigation-fln): typing a letter moves the focus to the next item whose label starts with it, wrapping around to the top. In the demo's main menu, T moves to *Translate input to Braille*, and T again to *TextView Examples*. A beep plays when nothing matches, or when the search comes back to its first match.
- Home and End move the focus to the first and last item.

You write no navigation code: the activity finds the items by walking the layout.

## Two ways to add an item

The main menu shows both. The user sees a single list, and the navigation handles both kinds of item the same way.

| Approach                                                          | How it works                                                                                        | Best for                                                     |
|-------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| [Declared in the layout](#4-declared-items-the-classic-approach) | The `TextView` is written in the layout XML with an id. Its click listener is set through view binding. | A few fixed items, or items that each do something different |
| [Generated from a list](#5-generated-items)                       | A list of entries, each a label and a target, with one `TextView` inflated per entry.              | Many items that behave the same way                          |

In the demo, *View images and documents* and *Accessible activity and dialog* are declared in the layout, and the other demos are generated from a list.

## 1. Declare the activity

Declare the activity in the manifest as usual. It needs no theme:

```xml
<activity
    android:name=".ui.MainMenuActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

`AccessibleActivity` extends `AppCompatActivity`, which needs an AppCompat theme. Before the screen is created, `AccessibleActivity` checks the activity's theme. When it is not an AppCompat theme, as here where none is declared, it applies the SDK's `AppCompatTheme` (`Theme.AppCompat.DayNight.NoActionBar`), the theme of the SDK's list based menus, so the main menu looks like them.

A theme you set yourself is kept, as long as it descends from `Theme.AppCompat`, as the MaterialComponents and Material3 themes do. Set it on the activity or the application in the manifest, or call `setTheme()` before `super.onCreate()`. In the demo, `AccessibleSampleActivity` sets the demo's theme to keep its action bar:

```xml
<activity
    android:name=".ui.AccessibleSampleActivity"
    android:exported="false"
    android:theme="@style/Theme.KeySoftSDKDemoApp" />
```

> **Note:** The fallback theme requires KeySoftSDK 0.1.179 or later. With earlier versions, declare an AppCompat theme, for example `android:theme="@style/AppCompatTheme"`, or the activity crashes on launch with `You need to use a Theme.AppCompat theme (or descendant) with this activity`.

## 2. Write the screen layout

The screen layout holds the title, an empty container for the generated items, and the declared items ([activity_main_menu.xml](../../app/src/main/res/layout/activity_main_menu.xml)):

```xml
<LinearLayout
    android:id="@+id/main_menu_root"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <TextView
        style="@style/Widget.KeySoftSDKDemoApp.MenuTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:importantForAccessibility="no"
        android:text="@string/app_name" />

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:divider="?android:attr/listDivider"
            android:orientation="vertical"
            android:showDividers="middle">

            <!-- Generated items -->
            <LinearLayout
                android:id="@+id/menu_items"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:divider="?android:attr/listDivider"
                android:orientation="vertical"
                android:showDividers="middle" />

            <!-- Declared items -->
            <TextView
                android:id="@+id/image_viewer_item"
                style="@style/Widget.KeySoftSDKDemoApp.MenuItem"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="@string/image_viewer" />

            <TextView
                android:id="@+id/accessible_sample_item"
                style="@style/Widget.KeySoftSDKDemoApp.MenuItem"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="@string/accessible_activity_and_dialog" />

        </LinearLayout>

    </ScrollView>

</LinearLayout>
```

The navigation finds the items by walking this layout, so the layout decides what is navigable:

- The root must be a `ViewGroup`: it is the group the navigation walks.
- The views are visited depth first, in reading order, so their order in the layout is the navigation order.
- A view is kept if it is visible, is not hidden from accessibility (`importantForAccessibility` set to `no` or `noHideDescendants`), and has a label: its `contentDescription`, or otherwise its text for a `TextView`, `Button`, `CheckBox` and similar views.
- A clickable or long-clickable group, such as a spinner, counts as a single item, labelled by its first labelled child.
- A view with no label, such as an empty `EditText`, is skipped by FLN but can still be reached with the usual navigation keys.

A few choices in this layout:

- The title stays outside the `ScrollView`, so it remains on screen like the title bar of the SDK's menus.
- Like that title bar, it is hidden from accessibility with `importantForAccessibility="no"`. That also takes it out of the navigation, so Home goes to the first demo, and typing K finds nothing. Remove the attribute if you want the title to be read and navigable.
- The generated items come before the declared ones in the layout, so the navigation visits them first.
- `showDividers="middle"` draws a line between items, like a `ListView` does. Both containers set it, so a divider also separates the last generated item from the first declared one.

## 3. Share the item style

Both kinds of item use one style, so they look the same ([styles.xml](../../app/src/main/res/values/styles.xml)). It copies the look of the SDK's `one_textview` row, the row the original menu used:

```xml
<style name="Widget.KeySoftSDKDemoApp.MenuItem" parent="">
    <item name="android:minHeight">105dp</item>
    <item name="android:paddingStart">10dp</item>
    <item name="android:paddingEnd">10dp</item>
    <item name="android:gravity">center_vertical|start</item>
    <item name="android:textSize">42sp</item>
    <item name="android:textColor">?attr/editTextColor</item>
    <item name="android:background">?attr/selectableItemBackground</item>
    <item name="android:clickable">true</item>
    <item name="android:focusable">true</item>
</style>
```

> **Important:** Unlike a `Button`, a `TextView` is neither clickable nor focusable by default. The style sets both: `clickable` so the item can be activated, and `focusable` so the arrow keys can reach it. Keep them if you use a `TextView` as a menu item without this style.

The same file holds `Widget.KeySoftSDKDemoApp.MenuTitle`, the look of the SDK's menu title: centered, 32sp, on the `colorButtonNormal` background.

## 4. Declared items, the classic approach

Pass the screen layout to the `AccessibleActivity` constructor. In `onCreate`, call `super` first, since that is where the content view is set, then bind the layout. Each item declared with an id gets a field in the binding, so all that is left is its click listener:

```kotlin
class MainMenuActivity : AccessibleActivity(R.layout.activity_main_menu) {

    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.bind(findViewById(R.id.main_menu_root))

        binding.imageViewerItem.setOnClickListener {
            startActivity(Intent(this, ImageViewerMenuActivity::class.java))
        }
        binding.accessibleSampleItem.setOnClickListener {
            startActivity(Intent(this, AccessibleSampleActivity::class.java))
        }
    }
}
```

This is the same code you would write for any Android screen: `AccessibleActivity` asks nothing more of a declared item than a label. [AccessibleSampleActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/AccessibleSampleActivity.kt) uses the same approach for a screen mixing text, buttons and a checkbox.

## 5. Generated items

When many items behave the same way, describe them in a list instead and inflate one `TextView` per entry. Each generated item uses its own small layout ([item_main_menu.xml](../../app/src/main/res/layout/item_main_menu.xml)):

```xml
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    style="@style/Widget.KeySoftSDKDemoApp.MenuItem"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

In the activity, list the entries and add their items to the `menu_items` container in `onCreate`:

```kotlin
private data class MenuEntry(@StringRes val labelId: Int, val target: Class<out Activity>)

private val menuEntries = listOf(
    MenuEntry(R.string.self_brailling_widget, SelfBraillingActivity::class.java),
    MenuEntry(R.string.accessible_dialog, DialogSampleActivity::class.java),
    // ...
)

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainMenuBinding.bind(findViewById(R.id.main_menu_root))

    menuEntries.forEach { addMenuItem(it) }
    // ... declared items, as in step 4
}

private fun addMenuItem(entry: MenuEntry) {
    val item = ItemMainMenuBinding.inflate(layoutInflater, binding.menuItems, false).root
    item.setText(entry.labelId)
    item.setOnClickListener { startActivity(Intent(this, entry.target)) }
    binding.menuItems.addView(item)
}
```

The items are added after `super.onCreate()`, once the navigation already exists. That needs no extra call: the navigation rebuilds its items whenever the layout changes, which also covers labels changed at runtime.

## 6. Add an entry

Either way, start by declaring the new activity in the manifest. Then:

- **Declared:** add a `TextView` with an id, the `MenuItem` style and a label to the layout, then set its click listener in `onCreate`:

  ```kotlin
  binding.myNewDemoItem.setOnClickListener {
      startActivity(Intent(this, MyNewDemoActivity::class.java))
  }
  ```

- **Generated:** add a line to `menuEntries`:

  ```kotlin
  MenuEntry(R.string.my_new_demo, MyNewDemoActivity::class.java),
  ```

  The order of `menuEntries` is the order of the generated items. To list them alphabetically instead, sort the entries before adding the items:

  ```kotlin
  menuEntries.sortedBy { getString(it.labelId) }.forEach { addMenuItem(it) }
  ```

## Rules

These rules apply to any screen built with `AccessibleActivity`:

- Call `super` in every lifecycle and key callback you override: `onCreate`, `onResume`, `onPause` and `onKeyUp`. The navigation is set up in `onCreate`, attached in `onResume`, detached in `onPause`, and the KeySoft keys are handled in `onKeyUp`.
- Do not set your own `AccessibilityDelegate` on a navigable view. The navigation relies on it to track which view holds the focus, and cannot do so on a view that already has one.

## Migrating a menu from AbstractMenuActivity

`MainMenuActivity` used to extend `AbstractMenuActivity`. Here is how each part of that menu maps to `AccessibleActivity`:

| With `AbstractMenuActivity`                                                                 | With `AccessibleActivity`                                                                          |
|---------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| The SDK's menu layout, with an `AccessibleListView`                                         | Your own layout, passed to the constructor                                                         |
| `getTitleId()` returns the title                                                            | A `TextView` above the list, styled like the SDK's title bar                                       |
| `listview.setNext(AccessibleItem(AccessibleItemAttributes(label, one_textview, textview)))` | A clickable, focusable `TextView` styled like `one_textview`, declared in the layout or inflated per entry of a list |
| `initialize()` returns `AccessibleListView.Sort.BY_POSITION`                                | The order of the views in the layout                                                               |
| `AccessibleListView.Sort.BY_LABEL`                                                          | Order the views yourself, for instance by sorting the list entries                                 |
| `setOnItemClickListener { _, _, position, _ -> when (position) { ... } }`                   | One click listener per item, with no position to keep in sync with the item order                  |
| The list's dividers                                                                         | `showDividers="middle"` with `?android:attr/listDivider` on the container                          |
| The theme is set by the SDK                                                                 | The same SDK theme is applied when you declare none, and an AppCompat theme you declare is kept (KeySoftSDK 0.1.179 or later) |

The [rules](#rules) above apply to the migrated menu too.

`AbstractMenuActivity` still works, and [DialogSampleActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/DialogSampleActivity.kt) and [ImageViewerMenuActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/ImageViewerMenuActivity.kt) keep using it. Migrating an existing menu is optional.

## Related references

- **Related concepts:** [Menu](../concepts/menu.md#accessibleactivity), [Dialogs](../concepts/dialogs.md#accessibledialog)
- **See it in code:**
  - [MainMenuActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/MainMenuActivity.kt), the menu built in this tutorial.
  - [AccessibleSampleActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/AccessibleSampleActivity.kt), a screen mixing text, buttons, a checkbox and `AccessibleDialog`s.
