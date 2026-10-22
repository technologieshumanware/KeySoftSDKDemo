# Dialog tutorials

## Simple workflow - Example with checkboxes
1. Create the activity extending AppCompatActivity
```kotlin
class ExampleActivity : AppCompatActivity()
```

2. Create the checkable items
```kotlin
listview.setNext(
    GenericAccessibleItem(
        AccessibleItem(
            CheckableAccessibleItemAttributes(
                getString(R.string.block_wifi_switch),
                R.layout.checkbox,
                R.id.compoundCheckBox,
                !config.isWifiBlocked,
                R.id.compoundCheckBox
            )
        )
    ) { item ->
        val itemAttr = item.attributes as CheckableAccessibleItemAttributes
        val newState = !itemAttr.checkboxValue
        itemAttr.checkboxValue = newState
        config.isWifiBlocked = newState
    }
)
```
3. Set the adapter
```kotlin
listview.adapter = SimpleArrayAdapter(this, listview)
```

## Related references
- **Related concept:** [Dialogs](../concepts/dialogs.md), [Menu](../concepts/menu.md)