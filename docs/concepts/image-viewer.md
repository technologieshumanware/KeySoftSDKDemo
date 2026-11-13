# Image Viewer

## Overview

The image viewer lets your app display images and documents on screen while showing a tactile version of the same view on the Braille display. It is provided by the optional `KeySoftSDK-imageviewer` module through a single class, `ImageViewerWidget`.

Use it when your app needs to present existing graphics rather than draw its own dots:

- PDF documents, including multi-page documents and their table of contents
- SVG vector images
- Raster images: JPEG, PNG and BMP

The widget handles format detection, image processing (see [Image filters](#image-filters)), Braille stamping of text found in documents, and zoom, pan and rotation. You do not need to build a `DotsMatrix` yourself.

## How it relates to self-brailling

`ImageViewerWidget` extends `SelfBraillingWidget`. It is the higher-level option when [Self Brailling](./self-brailling.md) would require you to roll your own graphics pipeline:

| | `SelfBraillingWidget` | `ImageViewerWidget` |
|---|---|---|
| Input | A dots matrix you compute | A document `Uri` |
| Physical display | You call `SelfBraillingManager.displayDots(...)` | Handled by the widget |
| Service binding | You bind/unbind the manager | Tied to the widget's attach/detach lifecycle |
| Zoom, pan, rotate, pages | You implement them | Built in |

## Image filters

A Braille display can only raise a dot or leave it flat, so each page is turned into a black and white image before it reaches the display: dark pixels become raised dots, and light pixels stay flat. The widget does this with a chain of OpenCV filters. Some are applied automatically when a page loads, and the others when your app calls the matching `ImageViewerWidget` method.

Whatever order they were applied in, the filters run in this order:

1. On the full-resolution page: Gaussian blur and dilation.
2. On a copy of it: grayscale, edge detection, threshold, then inversion.
3. The black and white result is thickened, then shrunk to the size of the Braille display.

### Applied automatically

- **Grayscale (`CVTColor`):** converts the page to shades of gray (`Imgproc.cvtColor`), since the threshold works on a single channel. Applied to every page.
- **Threshold (`Threshold`):** turns each gray pixel black or white by comparing it with a threshold value (`Imgproc.threshold`). Applied to every page, in automatic mode. See [Threshold and detail level](#threshold-and-detail-level).
- **Edge detection (`CannyEdge`):** keeps only the outlines of shapes, which are easier to explore by touch than large filled areas. It smooths the page with a bilateral filter, which reduces noise but keeps edges sharp, then applies an adaptive threshold (`Imgproc.adaptiveThreshold`), which follows the changes of lighting across the page. Despite its name, it does not use the Canny algorithm. It is applied when a raster image, or an SVG that contains color, is loaded. PDFs and grayscale SVGs are usually line art already, so they are left as they are. `applyEdgeDetectionFilter()` toggles it, along with inversion.
- **Line thickening:** before the page is shrunk to the size of the display, its content is thickened by about the shrink ratio (`Imgproc.erode`), so lines one or two pixels wide do not fade out when several pixels are averaged into one dot. This step is part of the resize and has no `FilterConstants` value.

### Applied on demand

- **Inversion (`InvertBraille`):** swaps raised and flat dots (`Core.bitwise_not`), for images with light content on a dark background. `applyInversionFilter()` toggles it.
- **Gaussian blur (`GaussianFilter`):** blurs the page (`Imgproc.GaussianBlur`), which removes noise and fine texture, so that mostly large shapes remain after the threshold. Each call to `applyGaussianFilter()` blurs further.
- **Dilation (`DilationFilter`):** grows the light areas of the page by about one pixel (`Imgproc.dilate`). On dark content over a light background, it removes small dark specks and thins the lines. Each call to `applyDilationFilter()` dilates further.

Gaussian blur and dilation are skipped in overview.

`handleKeyEvent()` maps no key to these filters: call the methods from your own key handling or menu.

### Threshold and detail level

Every pixel darker than the threshold becomes a raised dot. A higher threshold raises lighter grays too, which adds detail, and a lower one keeps only the darkest parts of the page.

- **Automatic (Otsu), the default:** Otsu's method computes, once per page, the value that best separates its dark and light pixels. The widget then adapts it to the zoom: the overview uses the highest level, 95% of white, so that the lines of the shrunk page survive, and zooming in moves it gradually down to a level based on the Otsu value, between 75% and 90% of white.
- **Manual levels:** `increaseImageDetail()` and `decreaseImageDetail()` step through fixed levels of 60, 70, 75, 80, 85, 90 and 95% of white, starting from the current automatic value. They do nothing past the last level, or while edge detection is on. When inversion is on, they step the other way, so more detail still means more raised dots.
- `applyOtsuThresholdFilter()` returns to automatic mode.

### Undo and pages

- `undoLastFilter()` removes the most recent filter. The automatic threshold applied when the page loads stays, since a page cannot be shown without a threshold.
- `getLastFilterName()` returns the `FilterConstants` name of the last filter applied after that threshold, or `null`, and `isEdgeDetectionActive()` tells whether edge detection is on. Use them to tell the user what is applied.
- Loading a new document resets all the filters. Moving to another page of the same document keeps the threshold, Gaussian blur and dilation, and drops inversion and edge detection.

## Developer notes

- Add the `com.humanware:KeySoftSDK-imageviewer` dependency. It brings `KeySoftSDK` transitively, and includes its own native image libraries (OpenCV and pdfium). R8 strips them from apps that never reference `ImageViewerWidget`.
- Only `file://` and `content://` URIs are supported. The format is detected from the MIME type reported by the content resolver, or from the file extension. To show a bundled resource, copy it to a file with the right extension first.
- The widget is a plain `View`. Forward your activity's key events to `handleKeyEvent(...)` to get the built-in zoom, pan and page shortcuts.
- The widget uses the `SelfBraillingWidget` view id, so the `<context viewIdResourceName="SelfBraillingWidget">` section of your `commands.xml` also applies to it. The Monarch's zoom keys work as soon as they are mapped there.

## Related references

- **How to:** [Example](../examples/image-viewer.md), [Tutorial](../tutorials/image-viewer-widget.md)
- **Code example:** [ImageViewerActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/ImageViewerActivity.kt), [ImageViewerMenuActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/ImageViewerMenuActivity.kt)
- **Related concept:** [Self Brailling](./self-brailling.md)
