# Image Viewer

## Overview

The image viewer lets your app display images and documents on screen while showing a tactile version of the same view on the Braille display. It is provided by the optional `KeySoftSDK-imageviewer` module through a single class, `ImageViewerWidget`.

Use it when your app needs to present existing graphics rather than draw its own dots:

- PDF documents, including multi-page documents and their table of contents
- SVG vector images
- Raster images: JPEG, PNG and BMP

The widget handles format detection, image processing (grayscale conversion and thresholding), Braille stamping of text found in documents, and zoom, pan and rotation. You do not need to build a `DotsMatrix` yourself.

## How it relates to self-brailling

`ImageViewerWidget` extends `SelfBraillingWidget`. It is the higher-level option when [Self Brailling](./self-brailling.md) would require you to roll your own graphics pipeline:

| | `SelfBraillingWidget` | `ImageViewerWidget` |
|---|---|---|
| Input | A dots matrix you compute | A document `Uri` |
| Physical display | You call `SelfBraillingManager.displayDots(...)` | Handled by the widget |
| Service binding | You bind/unbind the manager | Tied to the widget's attach/detach lifecycle |
| Zoom, pan, rotate, pages | You implement them | Built in |

## Developer notes

- Add the `com.humanware:KeySoftSDK-imageviewer` dependency. It brings `KeySoftSDK` transitively, and includes its own native image libraries (OpenCV and pdfium). R8 strips them from apps that never reference `ImageViewerWidget`.
- Only `file://` and `content://` URIs are supported. The format is detected from the MIME type reported by the content resolver, or from the file extension. To show a bundled resource, copy it to a file with the right extension first.
- The widget is a plain `View`. Forward your activity's key events to `handleKeyEvent(...)` to get the built-in zoom, pan and page shortcuts.
- The widget uses the `SelfBraillingWidget` view id, so the `<context viewIdResourceName="SelfBraillingWidget">` section of your `commands.xml` also applies to it. The Monarch's zoom keys work as soon as they are mapped there.
- Avoid showing Toasts, dialogs or popup windows over the viewer. KeySoft announces new windows on the Braille display, which replaces the dots drawn by the widget.

## Related references

- **How to:** [Example](../examples/image-viewer.md), [Tutorial](../tutorials/image-viewer-widget.md)
- **Code example:** [ImageViewerActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/ImageViewerActivity.kt), [ImageViewerMenuActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/ImageViewerMenuActivity.kt)
- **Related concept:** [Self Brailling](./self-brailling.md)
