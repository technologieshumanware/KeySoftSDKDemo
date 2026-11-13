# Glossary

Terms used across the KeySoftSDK documentation and this demo app.

### Back translation

Translation from braille to text, the opposite of [forward translation](#forward-translation). See [Translation](./concepts/translation.md).

### Braille cell

A group of dots that forms one braille character: 6 dots in two columns of three, or 8 dots with a fourth row. Dots 1, 2 and 3 go down the left column, dots 4, 5 and 6 down the right one, and dots 7 and 8 form the bottom row.

### Braille display

The tactile surface of the [Monarch](#monarch): 96 columns by 40 rows of [pins](#pin), each raised or lowered, showing braille text and tactile graphics. It is also touch sensitive: the SDK reports which pins are touched, and double taps on the display.

### Braille grade

How contracted braille text is. Uncontracted braille (grade 1) writes words letter by letter, and contracted braille (grade 2) shortens common words and letter groups. The *Switch Braille Grade* shortcut toggles between them.

### Braille table

The rules used to translate text to braille and back, for one language and braille code. The tables are installed on the device and chosen by the user in the KeySoft settings. The SDK's `Translator` relies on them, which is why it should be used for real translation rather than `BrailleUtils`. See [Translation](./concepts/translation.md).

### commands.xml

The key mapping file of an app, in `res/raw`. For each screen, identified by a view id in a `<context>` element, it binds braille keyboard and QWERTY shortcuts to commands, and gives the text shown for them in the [context menu](#context-menu). See [Create a context menu](./examples/context-menu.md#commandsxml) and [Create a shortcut](./tutorials/shortcut.md).

### Context menu

A list of the commands available on the current screen, with their shortcuts. Selecting an entry runs its command. The user opens it with the *Menu* shortcut (`Dots1349` or Alt+Ctrl+M). Also called the contextual menu. See [Contextual menu](./concepts/context-menu.md).

### Dots notation

The way braille keyboard shortcuts are written: `Dots`, followed by the keys pressed together, where 7 stands for Backspace, 8 for Enter and 9 for Space. For example, `Dots1359` is Space with dots 1, 3 and 5 (the letter O), the shortcut that opens the KeySoft settings. See [Create a context menu](./examples/context-menu.md#key).

### First Letter Navigation (FLN)

> "When navigating in a menu, you can always press the first letter of any item to be directed to it. For example, in the Main menu, pressing the letter T will direct you to Tactile Viewer, and pressing the letter A will direct you to All applications. The first letter navigation can be used in all the menus and when a list is displayed, for example a files list in Files Manager, or when trying to open a document in some applications."
>
> — *Monarch V2.0 user guide*

### Forward translation

Translation from text to braille, the opposite of [back translation](#back-translation). See [Translation](./concepts/translation.md).

### KeySoft

HumanWare's accessibility software on the [Monarch](#monarch). It presents apps on the [braille display](#braille-display) and through [TTS](#tts), and turns braille keyboard input into text and commands. KeySoftSDK gives your app access to its services, such as translation, settings and [self-brailling](#self-brailling).

### Panning

Moving the part of the content shown on the braille display. With text, the display pans to read what does not fit on it, and the `savePanning` attribute of a command keeps that reading position. In the image viewer, panning moves the visible part of a zoomed image.

### Perkins keyboard

The Monarch's braille keyboard, named after the Perkins Brailler: one key per dot, plus Space, pressed together to type a braille character or run a shortcut. The keys for dots 7 and 8 are also the Backspace and Enter keys. Shortcuts typed on it are written in [dots notation](#dots-notation).

### Pin

One point of the [braille display](#braille-display), raised or lowered. The SDK holds the state of every pin in a `DotsMatrix`, where 1 is a raised pin and 0 a lowered one. The docs also call pins dots.

### Self-brailling

Drawing directly on the braille display: the app sends the state of every [pin](#pin), instead of letting KeySoft show the screen content as braille text. See [Self Brailling](./concepts/self-brailling.md).

### Thumb keys

The four keys pressed with the thumbs: Previous, Back, Advance and Next, written `PreviousThumb`, `BackThumb`, `AdvanceThumb` and `NextThumb` in shortcut lists. For example, `PreviousThumb` and `NextThumb` move to the previous and next item.

### TTS

Text-to-speech, the speech synthesis KeySoft uses to read content aloud. Apps can speak through it too, as the demo does to announce touched regions.

### Zoom levels (image viewer)

The steps `ImageViewerWidget` zooms through. Overview, the lowest, fits the whole page on the braille display. Standard shows the page close to its original scale, and detailed is the closest zoom. A long press on a zoom key jumps straight to the next of these three, and a sound and a vibration play when the view reaches standard. See [Image Viewer](./concepts/image-viewer.md).
