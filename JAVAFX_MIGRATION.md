# Nepali TTS UI Modernization Guide

## What's New: JavaFX Migration

Your TTS application now has a **modern JavaFX UI** alongside the legacy Swing version. Both versions work and can be switched between.

## Key Improvements in JavaFX Version

### Visual & UX Enhancements
- **Modern, clean interface** with professional styling
- **Smoother animations** and transitions (hardware-accelerated)
- **Better responsive layout** that adapts to window resizing
- **Improved button styling** with hover effects
- **Status bar** showing playback state and app version
- **Playback progress indicator** at the top
- **Better visual organization** with panels and separators

### Architecture Improvements
- **JavaFX Task** instead of Swing SwingWorker - better async handling
- **CSS-based styling** for easy theme customization
- **Platform thread safety** built into JavaFX paradigm
- **Better separation of concerns** - UI layer is cleaner

### User Experience
- **Larger, more accessible buttons** (56x56px vs icon buttons)
- **Professional color scheme** with proper contrast
- **Keyboard shortcuts reference panel** on the right
- **Real-time status updates** during playback
- **Smoother text interaction** with TextArea instead of JTextPane

## Switching Between UI Versions

### Run JavaFX Version (Default)
```bash
./gradlew run
```
Or edit `build.gradle`:
```gradle
application {
    mainClass = 'tts.MainFX'
}
```

### Run Legacy Swing Version
Edit `build.gradle`:
```gradle
application {
    mainClass = 'tts.Main'
}
```
Then run:
```bash
./gradlew run
```

## File Structure

### New Files Created
- `src/tts/MainFX.java` - JavaFX Application entry point
- `src/tts/ui/MainFrameFX.java` - Modern JavaFX UI implementation (380+ lines)
- `src/tts/domain/PlayerFX.java` - JavaFX-compatible Task for audio playback

### Modified Files
- `build.gradle` - Added JavaFX dependencies and plugin

### Unchanged Files
- All audio engine code (`TTSEngine.java`, `Player.java`, etc.) remains compatible
- Domain logic is framework-agnostic

## Dependencies Added

```gradle
implementation 'org.openjfx:javafx-controls:21'
implementation 'org.openjfx:javafx-fxml:21'
implementation 'org.openjfx:javafx-graphics:21'
```

## Future Enhancement Ideas

1. **Waveform visualization** - Show audio waveform during playback
2. **Theme switching** - Dark/Light mode toggle in UI
3. **FXML-based UI** - Move layout to declarative FXML files
4. **Keyboard shortcuts** - Space=Play, P=Pause, S=Stop
5. **Recording feature** - Save spoken text as MP3/WAV
6. **Text statistics** - Live word/character count
7. **Audio visualization** - Real-time spectrum analyzer
8. **Settings panel** - Customizable playback speed, pitch
9. **History panel** - Recent texts played
10. **Custom themes** - User-defined color schemes via CSS

## Comparison Table

| Feature | Swing (Old) | JavaFX (New) |
|---------|------------|------------|
| Threading | SwingWorker | Task/Platform.runLater() |
| Styling | Hard-coded look | CSS-based themes |
| Rendering | Software | Hardware-accelerated |
| Layout | Manual GroupLayout | Scene Graph layout |
| Modern Look | ❌ Dated | ✅ Clean, modern |
| Responsive | Limited | ✅ Adaptive |
| Code Maintainability | Medium | ✅ High |
| IDE Support | Good | ✅ Excellent |

## Building & Deployment

### Development
```bash
./gradlew run          # Run JavaFX version
./gradlew build        # Build project
```

### Building Executable JAR
```bash
./gradlew executableJar
```

Both UI versions will be included in the compiled JAR. Switch via `build.gradle` before building deployment.

## Notes

- **Backward Compatible**: Original Swing UI still works perfectly
- **No Audio Changes**: Audio playback engine unchanged for both UIs
- **Java 21 Required**: Using modern Java language features (records, text blocks, etc.)
- **Cross-Platform**: JavaFX works on Windows, Mac, Linux

## Troubleshooting

### UI Not Loading?
- Ensure JavaFX runtime is available: `./gradlew run`
- Check Java version: `java -version` (must be 21+)

### Images Not Showing?
- Verify image files exist in `src/tts/ui/img/`
- Check resource path strings in MainFrameFX.java

### Build Fails?
- Run `./gradlew clean build` to clear cache
- Ensure internet connection (dependencies download)

## Return to Swing

Simply change `application.mainClass` back to `'tts.Main'` in `build.gradle` and rebuild.

---

**Created**: April 7, 2026  
**JavaFX Version**: 21  
**Target Java**: 21+
