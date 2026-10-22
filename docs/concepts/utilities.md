# Braille, Sound and Vibration Utils Guide

This package contains small reusable helpers for common Android and accessibility-related tasks used by `KeySoftSDK`.

**Package:** `com.humanware.keysoftsdk.utils`

## Checklist
- Understand what each utility class does
- Import the right helper into your component
- Use the static helpers directly where available
- Keep the Android-specific caveats in mind

---

## Contents

- [`BrailleUtils`](#brailleutils) - converts and validates braille text formats
- [`SoundUtils`](#soundutils) - plays short notification-style sounds
- [`VibrationUtils`](#vibrationutils) - triggers device vibration

---

## General usage

Import the class you need:

```java
import com.humanware.keysoftsdk.utils.BrailleUtils;
import com.humanware.keysoftsdk.utils.SoundUtils;
import com.humanware.keysoftsdk.utils.VibrationUtils;
```

Most utilities in this package are static helpers, so you can call them directly without creating an instance:

```java
SoundUtils.errorBeep(context);
VibrationUtils.startVibration(context, 150);
```

---

## `BrailleUtils`

`BrailleUtils` contains basic conversion and validation helpers for braille content.

> **Warning**
> Use `BrailleUtils` only for basic ASCII braille/BRF conversion and validation.
> For actual braille translation workflows, use the translator supplied by the SDK instead since it relies on legitimate, localized braille tables installed by the user.

### Main methods

#### Convert ASCII braille/BRF to Unicode braille

```java
String unicode = BrailleUtils.asciiBrailleToUnicodeBraille(asciiBraille);
```

Use this when your source text is in BRF/ASCII braille format and you want Unicode braille characters.

#### Convert Unicode braille to ASCII braille/BRF

```java
String ascii = BrailleUtils.unicodeBrailleToAsciiBraille(unicodeBraille);
```

Use this when you need to export or process Unicode braille as BRF-compatible ASCII.

**Important:** this method can throw `IndexOutOfBoundsException` if a braille pattern cannot be represented by the supported ASCII mapping.

```java
try {
    String ascii = BrailleUtils.unicodeBrailleToAsciiBraille(unicodeBraille);
} catch (IndexOutOfBoundsException e) {
    // Handle unsupported braille pattern
}
```

#### Check whether text is braille

```java
boolean isWordBraille = BrailleUtils.isBrailleWord(text);
boolean containsBraille = BrailleUtils.containsBraille(text);
boolean isMaterialBraille = BrailleUtils.isBrailleMaterial(text);
boolean isCharBraille = BrailleUtils.isBrailleChar(character);
```

### When to use which method

- Use `isBrailleWord(...)` when every character must be a Unicode braille character.
- Use `containsBraille(...)` when you only need to know whether some braille is present.
- Use `isBrailleMaterial(...)` when braille plus whitespace is allowed.
- Use `isBrailleChar(...)` for single-character checks.

### Example

```java
String input = "A1B";
String unicodeBraille = BrailleUtils.asciiBrailleToUnicodeBraille(input);

if (BrailleUtils.containsBraille(unicodeBraille)) {
    // Safe to treat as braille content
}
```

### Notes

- Existing braille Unicode characters are preserved when converting ASCII to Unicode.
- New lines, form feeds, tabs, and some control characters are preserved.
- Large page-like content may have form feeds inserted automatically to keep page sizes reasonable.

---

## `SoundUtils`

`SoundUtils` provides quick playback for short sounds.

### Main methods

#### Play a raw sound resource

```java
SoundUtils.playSound(context, R.raw.my_sound);
```

#### Play the built-in error beep

```java
SoundUtils.errorBeep(context);
```

#### Typical use
Demo implementation: [SoundAndVibrationActivity.kt](../../app/src/main/java/com/humanware/keysoftsdkdemoapp/ui/SoundAndVibrationActivity.kt)

### Requirements

- The sound must exist as a raw Android resource when using `playSound(...)`.
- `errorBeep(...)` depends on `R.raw.beep_change_value` being present in the module resources.

### Notes

- Internally, this utility creates a `MediaPlayer`, starts playback when prepared, and releases it on completion.
- This helper is best suited for short notification-style sounds.

---

## `VibrationUtils`

`VibrationUtils` starts a vibration for a requested duration.

### Main method

```java
VibrationUtils.startVibration(context, 200);
```

### Typical use

```java
if (hasValidationError) {
    VibrationUtils.startVibration(this, 300);
}
```

### Requirements

Make sure your app/module has vibration permission if required by your target setup:

```xml
<uses-permission android:name="android.permission.VIBRATE" />
```

### Notes

- On Android 8.0+ it uses `VibrationEffect.createOneShot(...)`.
- On older Android versions it falls back to the legacy vibration API.
- Pass a `Context` that can access the vibrator system service.

---

## Recommended developer workflow

### 1. Pick the smallest utility that matches your need

- Braille conversion or detection -> `BrailleUtils`
- Short feedback sounds -> `SoundUtils`
- Haptic feedback -> `VibrationUtils`

### 2. Prefer direct static calls for simple feedback

```java
SoundUtils.errorBeep(context);
VibrationUtils.startVibration(context, 100);
```

### 3. Wrap braille conversions in clear intent

```java
String displayText = BrailleUtils.asciiBrailleToUnicodeBraille(sourceText);
```

### 4. Handle edge cases for conversion APIs

```java
try {
    export(BrailleUtils.unicodeBrailleToAsciiBraille(text));
} catch (IndexOutOfBoundsException e) {
    // Notify user or fallback
}
```

---

## Summary

These utilities are small, focused helpers intended to simplify common tasks in Android components:

- simple braille text conversion and validation
- audio feedback
- vibration feedback