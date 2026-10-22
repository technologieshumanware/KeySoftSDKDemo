# Use the image viewer widget

The `ImageViewerWidget` class from the `KeySoftSDK-imageviewer` module displays a PDF, SVG
or raster image on screen and sends a tactile version of the same view to the Braille
display. This tutorial shows the steps to add it to a regular Android `AppCompatActivity`.

### Add the dependency

The image viewer is published separately from `KeySoftSDK`. With the Maven repository
configured as described in [Getting started](../getting-started.md), add:

```groovy
dependencies {
    implementation 'com.humanware:KeySoftSDK-imageviewer:latest.release'
}
```

or, with the Kotlin DSL:

```kt
dependencies {
    implementation("com.humanware:KeySoftSDK-imageviewer:latest.release")
}
```

The module depends on `KeySoftSDK`, so you get the rest of the SDK as well. Its consumer
ProGuard rules are applied automatically.

### Create the widget and load a document

Create the widget in code and call `loadDocument` with the document's `Uri`. It is safe to
call `loadDocument` right after construction, before the widget is attached to a window.

```kt
class ImageViewerActivity : AppCompatActivity() {
    private lateinit var widget: ImageViewerWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widget = ImageViewerWidget(this).also { setContentView(it) }
        supportActionBar?.hide()
        widget.loadDocument(intent.data ?: return finish())
    }
}
```

Unlike `SelfBraillingWidget`, you do not create a `SelfBraillingManager` or push dots
yourself. The widget binds to the self-brailling service when it is attached to a window,
unbinds when it is detached, and updates both the screen and the Braille display after
each change.

### Provide a supported URI

The widget accepts `file://` and `content://` URIs only. It detects the format from the
MIME type reported by the `ContentResolver`, then from the file extension.

* Files selected with `ActivityResultContracts.OpenDocument()` can be passed as is. If
  you pass the `Uri` to another activity, add `Intent.FLAG_GRANT_READ_URI_PERMISSION`.
* Resources bundled in `res/raw` or `assets` must first be copied to a file with the right
  extension:

```kt
val file = File(cacheDir, "sample_document.pdf")
resources.openRawResource(R.raw.sample_document).use { input ->
    file.outputStream().use { output -> input.copyTo(output) }
}
widget.loadDocument(Uri.fromFile(file))
```

If the document cannot be loaded, the widget shows a "Failed to load document" message.

### Forward key events

A `View` only receives key events when it holds focus. Forward your activity's key events
to `handleKeyEvent` to enable the built-in zoom, pan and page shortcuts:

```kt
override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    return widget.handleKeyEvent(keyCode, event) || super.onKeyDown(keyCode, event)
}
```

The widget uses the `SelfBraillingWidget` view id. To map Monarch keys to these key codes,
add commands under `<context viewIdResourceName="SelfBraillingWidget">` in your app's
`res/raw/commands.xml`. See the [Shortcut tutorial](./shortcut.md).

### Drive the viewer from code

You can also call the widget's API directly, for example from your own shortcuts or menu:

* Zoom and pan: `zoomIn(event)`, `zoomOut(event)`, `panUp()`, `panDown()`, `panLeft()`,
  `panRight()`, `panToCenter()`, `panToTopLeft()` and the other `panTo...()` methods
* Rotation: `rotate()`
* Pages: `getPageCount()`, `getCurrentPageIndex()`, `goToPage(index)`, `nextPage()`,
  `previousPage()`, `isMultiPage()`
* PDF table of contents: `getPagesTitles()` returns a map of page index to title. Use it
  to build your own index page, along with `goToIndexPage()` and `isCurrentPageIndexPage()`.
* PDF annotations: `getAnnotProperties(pageIndex)`
* Feedback: `toggleHaptics()` turns the vibration and sound played when the view enters
  standard mode on or off

Each action runs in the background. Set `onViewRefreshed` to be notified on the main
thread after the screen and the Braille display have been updated:

```kt
widget.onViewRefreshed = { updatePageLabel(widget.getCurrentPageIndex()) }
```

### Keep the Braille display clean

Showing Toasts, dialogs or popup windows over the viewer makes KeySoft announce the new
window on the Braille display, replacing the image dots. Prefer logging, sounds or
vibration for feedback while a document is displayed.

See [ImageViewerActivity](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/ImageViewerActivity.kt) for an example.

## Related references
- **Related concept:** [Image Viewer](../concepts/image-viewer.md)
- **Example:** [Image viewer examples](../examples/image-viewer.md)
