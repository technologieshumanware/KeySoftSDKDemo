# Self Brailling

## Overview

A self-brailling workflow lets your app control what is shown on the braille display in real time. It gives you direct control over the dot matrix, so your screen state and tactile output stay synchronized.

This is useful when your app needs custom tactile rendering simple graphics, dynamic patterns, or interactive feedback tied to app events. It lets you:

- Provide immediate tactile feedback for dynamic UI states
- Keep visual and braille output aligned as your app updates content
- Build custom experiences beyond static text, such as procedural or bitmap-based patterns

## Developer reality

In day-to-day implementation, self-brailling is usually split into two responsibilities:

- `SelfBraillingWidget` handles the on-screen simulation and touch interactions.
- `SelfBraillingManager` handles communication with the Braille service and sends dots to the physical display.

A common pattern is to keep one shared matrix state and push updates to both surfaces (widget + device) whenever content changes.

## Related references

- **Learn it:** [Tutorial](../tutorials/self-brailling-widget.md), [Example](../examples/self-brailling.md)
- **See it in code:** [SelfBraillingActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/SelfBraillingActivity.kt)
- **Related concept:** [Image Viewer](./image-viewer.md), for displaying PDF, SVG and raster images without building the dots matrix yourself
