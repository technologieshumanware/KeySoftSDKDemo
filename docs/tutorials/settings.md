# Settings tutorial

## Typical workflow

- Bind the service before reading settings (`bindService()`).
- Read one or more groups based on your app's needs.
- Unbind when done (`unbindService()`), typically in the activity lifecycle.
- Returned data is a `Map<String, Any>` where keys are preference names from KeySoft settings.
  
## Related references
- **Related concept:** [Settings](../concepts/settings.md)