# Image viewer examples

## Open bundled samples and files from storage

From the main menu, select **View images and documents**. `ImageViewerMenuActivity` lists:

- **Bitmap image (BMP)**: the burger bitmap also used by the self-brailling demo
- **Vector image (SVG)**: simple shapes drawn as vectors
- **Multi-page document (PDF)**: two pages, each with a title and a shape
- **Open a file from storage**: any supported file selected with the system document picker

Selecting an item opens `ImageViewerActivity`, which shows the document in an `ImageViewerWidget`.

## Integration pattern used in the demo

1. Resolve a `Uri` the widget can read:
   - bundled samples are copied from `res/raw` to `cacheDir` under a name with the right extension (`burger.bmp`, `sample_shapes.svg`, `sample_document.pdf`), then passed as `Uri.fromFile(...)`;
   - files from storage come from `ActivityResultContracts.OpenDocument()` as `content://` URIs.
2. Start `ImageViewerActivity` with the `Uri` as intent data and `FLAG_GRANT_READ_URI_PERMISSION`, so picked documents stay readable.
3. In `onCreate`, create the widget, set it as the content view and call `loadDocument(uri)`.
4. In `onKeyDown`, handle a few custom shortcuts, then forward the rest to `handleKeyEvent(...)`.

```kt
widget = ImageViewerWidget(this).also { setContentView(it) }
supportActionBar?.hide()
widget.onViewRefreshed = { Log.d(TAG, "Page ${widget.getCurrentPageIndex() + 1} refreshed") }
widget.loadDocument(uri)
```

## Keyboard shortcuts

Built into the widget through `handleKeyEvent(...)`:

| Key | Action |
|---|---|
| Zoom in / Zoom out | Zoom (long press for a quick zoom) |
| Arrow keys | Pan up, down, left, right |
| `1`, `2`, `3` | Pan to top, center, bottom left |
| `4`, `5`, `6` | Pan to top, center, bottom right |
| `C`, D-pad center, `-` | Pan to top center, center, bottom center |
| `W` / `O` | Previous / next page |
| Double tap on the Braille display | Zoom and pan to the touched point |

Added by `ImageViewerActivity` to show how to call the widget's API directly:

| Key | Action |
|---|---|
| `Ctrl+R` | `rotate()` |
| `Ctrl+Page Up` / `Ctrl+Page Down` | `previousPage()` / `nextPage()` |
| `Ctrl+Home` | `goToPage(0)` |
| `Ctrl+H` | `toggleHaptics()` |

## See it in code

- [ImageViewerMenuActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/ImageViewerMenuActivity.kt)
- [ImageViewerActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/ImageViewerActivity.kt)

## Related references

- **Related concept:** [Image Viewer](../concepts/image-viewer.md)
- **Tutorial:** [Use the image viewer widget](../tutorials/image-viewer-widget.md)
