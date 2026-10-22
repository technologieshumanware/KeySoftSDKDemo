# Create a context menu

Creating a context menu is possible through extending the `ContextMenuActivity`
class offered by the SDK. Three steps are required in order to make an option
available in the context menu:

1. Add a `<command>` entry in `commands.xml`.
2. Add a `<string>` entry in your resource strings.
3. Call `addContextMenuItem` in your class extending `ContextMenuActivity` using
   the command ID defined in the first step.

### `commands.xml`

#### `<action>`

The body of a `<context>` item contains one or more `<action>` items. 
An [`<action>`](https://developer.android.com/guide/topics/manifest/action-element)
item has a `name` field, which may take any of the following values:

| Action type  | Description                                                                                               |
|--------------|-----------------------------------------------------------------------------------------------------------|
| `substitute` | Allows assigning an [Android keycode](https://developer.android.com/reference/android/view/KeyEvent.html) |

#### `<command>`

The body of an `<action>` item contains one or more `<command>` items. 
A `<command>` item may have the following fields:

| Parameter            | Description                                                                                                                                                                     |
|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `commandId`          | The unique identifier for the command you wish to define                                                                                                                        |
| `convertToCharacter` | Whether or not to convert the input to character values (`true` or `false`)                                                                                                     |
| `param`              | When using a `substitute` action, the keycode bound to the command. </br>Multi-key commands may be defined by using a bitwise or (e.g. `param="113\|59\|31"` for Ctrl+Shift+C)  |
| `savePanning`        | Whether or not to keep track of the current panning/reading position, allowing restoring that position when either losing or gaining focus. If the view has to be restored at some stage (e.g. after a screen lock, or after checking some notifications), the cursor will be restored to where the display was, instead of where the cursor was originally.  Without that, the reading position would be lost. (`true` or `false`)        |
| `string`             | The identifier of the string to be displayed in the context menu describing the selection to the user (see [Resource strings](#resource-strings) section)                                          |

#### `<key>`

The body of a `<command>` item contains one or more `<key>` items.
A `<key>` item contains the keybindings or shortcuts associated to a command.
The body of a `<key>` item may be either:

* A combination of Braille dots with the Space character, using the form `Dots[x]9` (e.g. `Dots13569` for Space with '⠵'/'z').
  Backspace corresponds to dot 7, Enter corresponds to dot 8 and Space corresponds to dot 9.
* A concatenation of regular keyboard keys (e.g. `Shift+Ctrl+A`). There is no need to
  include the space character at the end here.

Here is an example of the XML file structure required to define context menu commands:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<keymapping>
    <application
        package="com.mycompany.myactivity"> <!-- The package where your Android activity lives -->
        <context viewIdResourceName="*"> <!-- use * for global context, otherwise the AccessibilityNodeInfo id of the desired View component -->
            <action name="substitute">
                <command 
                    commandId="do_something" 
                    param="113|44"
                    string="context_menu_do_something" 
                    convertToCharacter="true" 
                    savePanning="true">
                    <key>Dots24689</key>
                    <key>Ctrl+P</key>
                </command>
                <command>
                    <!-- Another command definition -->
                </command>
                <!-- ... -->
            </action>
        </context>
    </application>
</keymapping>
```

### Resource strings

Strings displayed on each action in the context menu are defined the same way as
any other resource string in your application. For example:

```xml
<resources>
    <string name="an_action">Display text for a context menu action</string>
    <string name="another_action">Display text for another context menu action</string>
    <!-- other string resources -->
</resources>
```

See [SelfBraillingContextMenuActivity](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/SelfBraillingContextMenuActivity.kt) for an example.

## Related references
- **Related concept:** [Contextual Menu](../concepts/context-menu.md)