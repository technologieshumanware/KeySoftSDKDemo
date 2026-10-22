# Translation example

## Translate input to and from Braille

1. Declare an instance of `Translator` in the activity you wish to have translation
   capabilities in.
2. Define the `Translator` instance in the `onCreate` method and call `bindService` on it.
3. Call `unbindService` in the `onDestroy` method.

From now on, you have access to the two types of translation inside your activity:

* Forward translation (Text to Braille) via `forwardTranslate`
* Back translation (Braille to text) via `backTranslate`

See [BrailleTranslatorActivity](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/BrailleTranslatorActivity.kt) for an example.

## Related references
- **Related concept:** [Translation](../concepts/translation.md)