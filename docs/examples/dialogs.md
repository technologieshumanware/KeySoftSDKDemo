# Dialog examples

## Advanced custom dialogs

In this demo app, [CheckListActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/CheckListActivity.kt) shows a custom configuration dialog that:
- inflates a dedicated view binding layout,
- embeds an `AccessibleListView` inside the dialog,
- uses checkable menu rows (`GenericAccessibleItem` + `CheckableAccessibleItemAttributes`),
- updates activity state when the dialog is confirmed.

## Related references
- **Related concept:** [Dialogs](../concepts/dialogs.md)