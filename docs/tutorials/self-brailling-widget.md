# Use the self-brailling widget

The `SelfBraillingWidget` class included in the SDK allows you to have more fine-grained
control over the Braille display. The widget can be instantiated in a regular Android
`AppCompatActivity`. Several steps are important to ensure you set up the widget correctly.

### Braille dots as `LiveData`

Let `mutableViewedImage` and `mutableLiveDots` be the data backing the screen dots
for, respectively, the simulation provided by the `SelfBraillingWidget` and the
actual device by the `SelfBraillingManager`. As implemented in the demo's source
code, you may hook screen updates to the `LiveData` in the following way:

```kt
        mutableViewedImage.observe(this) { widget.refresh(it) }
        mutableLiveDots.observe(this) { manager.displayDots(it) }
```

Any changes to the underlying `LiveData` objects will then push an update
to the views.

### Bind a listener to the self-brailling widget

Two actions are to be defined by the `OnSelfBraillingWidgetListener`:

* `onFocused`: what happens when the widget gains focus
* `onDoubleTapAtBraillePosition`: what happens when double tapping at a specific point on the screen

In most cases, in addition to the custom behavior you define in these methods,
you will likely want to refresh the display in order to show any updates.

### Normalization

The Monarch expects precise values to set the status of each of the pins composing
its Braille display. The values are:

* 0 for a lowered pin
* 1 for a raised pin

Thus, it is expected that any matrix you provide to the Monarch is normalized
to these values in order to ensure your graphics are properly displayed. The required
normalization will depend on the initial raw values you are using
**and may differ from the ones shown in the demo**.
Please inspect the raw values you have in file or in memory and normalize accordingly.

### Inclusion of a graphics matrix within the screen matrix

The Monarch's Braille display is to be represented in memory using a `DotsMatrix` object
with dimensions equal to the physical screen (96 columns x 40 rows). From there,
graphics of a size equal or lower to the screen dimensions may be included in the
screen matrix, provided they are within bounds. More than one asset may be included
simultaneously on the screen and all will be displayed in full, provided they do
not overlap. See the bitmap section of the self-brailling widget demo for an
example.

### More complex graphics

The demo provided for usage of the `SelfBraillingWidget` only provides a summary view
of how to handle very simple files and graphics. For more complex use cases, use the
`ImageViewerWidget` from the `KeySoftSDK-imageviewer` module, which displays PDF, SVG and
raster images with zoom, pan and rotation built in. See the
[image viewer tutorial](./image-viewer-widget.md). You may also defer to the built-in
Tactile Viewer, or roll your own graphics implementation.

See [SelfBraillingActivity](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/SelfBraillingActivity.kt) for an example.

## Related references
- **Related concept:** [Self-Brailling](../concepts/self-brailling.md)