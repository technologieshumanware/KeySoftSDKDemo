# Self-brailling examples

## Annotated tactile regions with touch feedback

This example shows how to annotate tactile regions on the braille display and react when users touch them.

In `SelfBraillingActivity`, the demo displays a burger and a sun bitmap on the braille matrix.  
It then defines annotation bounds for each element and delegates touch monitoring to
[`SelfBraillingAnnotationController.kt`](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/controller/SelfBraillingAnnotationController.kt).

When a finger lands on an annotated region:
- the controller reads touched pin coordinates from `SelfBraillingManager.getTouchedPinCoordinates()`,
- announces the matching label through TTS (for example: **"Burger"**),
- emits radar-like vibration feedback based on proximity.

## Integration pattern used in the demo

1. Draw tactile content in `SelfBraillingActivity` (burger/sun) and compute their `Rect` regions.
2. Initialize `SelfBraillingAnnotationController` with a callback to touched-pin coordinates.
3. Push active annotations via `setAnnotations(...)` when content changes.
4. Start/stop/release the controller with the activity lifecycle (`onResume`, `onStop`, `onDestroy`).

This gives a reusable pattern for adding semantic touch feedback on top of dynamic self-brailling content.

## See it in code

- [SelfBraillingActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/SelfBraillingActivity.kt)
- [SelfBraillingAnnotationController.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/controller/SelfBraillingAnnotationController.kt)

## Related references

- **Related concept:** [Self Brailling](../concepts/self-brailling.md)
- **Tutorial:** [Use the self-brailling widget](../tutorials/self-brailling-widget.md)