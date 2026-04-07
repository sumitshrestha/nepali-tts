# Nepali TTS (Text-to-Speech)

A Java-based Text-to-Speech engine for the Nepali language, built as a college project. It converts romanized Nepali text into speech by mapping phonemes to pre-recorded WAV audio samples.

## How It Works

1. **Input** — The user types romanized Nepali text (e.g. `namaste`) into the Swing UI.
2. **Sentence Splitting** — `SentenceSplitter` tokenizes the input into individual words.
3. **Alphabet Parsing** — `AlphabetParser` resolves phonetic ambiguities in each word:
   - Double vowels (`aa` → `a1`, `ee` → `e1`, `oo` → `o1`)
   - Aspirated consonants (`gh`, `kh`, `bh`, etc.)
   - Diphthongs (`au` → `o`, `ai` → `e`)
4. **TTS Engine** — `TTSEngine` maps each parsed phoneme to a corresponding `.wav` file in the `soundDb/` directory and plays them sequentially using `javax.sound.sampled`.

## Project Structure

```
src/
  tts/
    Main.java                  # Entry point — launches the Swing UI
    domain/
      AlphabetParser.java      # Phonetic ambiguity resolver
      SentenceSplitter.java    # Tokenizer for input sentences
      TTSEngine.java           # Core engine — phoneme-to-audio playback
      Player.java              # Audio playback helper
      FrameInterface.java      # UI callback interface
    ui/
      MainFrame.java           # Main Swing window
soundDb/                       # WAV audio samples for each phoneme
```

## Requirements

- Java 8 or higher (JDK)
- Apache Ant (for building via `build.xml`)
- NetBeans IDE (optional — project includes NetBeans project files)

## Building & Running

**With NetBeans:** Open the project folder and click Run.

**With Ant from the command line:**
```bash
ant clean
ant jar
java -jar dist/TTS.jar
```

## Audio Quality Profiles

The engine supports three built-in join-smoothing profiles:

- `crisp` - shortest fades/crossfades, sharper articulation
- `balanced` - default, good overall smoothness and clarity
- `smooth` - longer fades/crossfades for softer transitions

Select profile at runtime using JVM property `tts.audio.profile`:

```bash
java -Dtts.audio.profile=balanced -jar build/libs/TTS-executable.jar
```

You can also change the profile from the UI while the app is running using the `Audio` dropdown.
Profiles also adjust sentence pause timing:

- `crisp` - shorter sentence breaks
- `balanced` - medium sentence breaks
- `smooth` - longer sentence breaks

## Author

Sumit Shrestha
