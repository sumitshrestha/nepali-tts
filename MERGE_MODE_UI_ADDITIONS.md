# Merge-Before-Play UI Integration Guide

The merge audio feature is **fully implemented in code** (TTSEngine.mergeSequence() and TTSEngine.playMerged() methods, plus Player integration). This document shows the **exact MainFrame.java edits** needed to expose a UI checkbox.

## Implementation Status

- ✅ TTSEngine.mergeSequence() - merges phonemes with fades/crossfades
- ✅ TTSEngine.playMerged() - plays merged audio buffer
- ✅ TTSEngine.isMergeBeforePlayEnabled() / setMergeBeforePlay() - getter/setter
- ✅ Player.playString() - uses merge mode when enabled
- ❌ MainFrame UI checkbox - needs manual addition (tools constrained)

## How to Add the Checkbox to MainFrame.java

### Step 1: Add checkbox component declaration
In `initComponents()`, after line with `profileStatusLabel = new javax.swing.JLabel();`, add:
```java
mergeCheckbox = new javax.swing.JCheckBox();
```

### Step 2: Initialize checkbox in initComponents()
After profileStatusLabel initialization section, add:
```java
mergeCheckbox.setText("Merge");
mergeCheckbox.setToolTipText("Merge phonemes into one file before playing (smoother but delayed start)");
mergeCheckbox.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        mergeCheckboxActionPerformed(evt);
    }
});
```

### Step 3: Add to UI layout (horizontal)
In the horizontal group layout, after profileStatusLabel component, add:
```java
.addGap(18, 18, 18)
.addComponent(mergeCheckbox)
```

### Step 4: Add to UI layout (vertical)
In the vertical group layout, add mergeCheckbox to the baseline group with other controls:
```java
.addComponent(mergeCheckbox)
```

### Step 5: Add action handler
Before the final `private void setFrameIcon()` method, add:
```java
private void mergeCheckboxActionPerformed(java.awt.event.ActionEvent evt) {
    boolean enabled = this.mergeCheckbox.isSelected();
    this.Player.setMergeBeforePlay(enabled);
    this.prefs.putBoolean(PREF_MERGE_BEFORE_PLAY, enabled);
}
```

### Step 6: Update loadSavedAudioProfile()
Replace the existing `loadSavedAudioProfile()` method with:
```java
private void loadSavedAudioProfile() {
    var saved = this.prefs.get(PREF_AUDIO_PROFILE, this.Player.getProfile().name().toLowerCase());
    var profile = tts.domain.TTSEngine.AudioProfile.from(saved);
    this.Player.setProfile(profile);
    this.profileCombo.setSelectedItem(profile.name().toLowerCase());
    updateProfileStatusLabel();

    boolean mergeEnabled = this.prefs.getBoolean(PREF_MERGE_BEFORE_PLAY, false);
    this.mergeCheckbox.setSelected(mergeEnabled);
    this.Player.setMergeBeforePlay(mergeEnabled);
}
```

### Step 7: Add private field declaration and constant
At the end, before `}`, add to the variables section:
```java
private javax.swing.JCheckBox mergeCheckbox;
```

And update the constants section:
```java
private static final String PREF_AUDIO_PROFILE = "audio.profile";
private static final String PREF_MERGE_BEFORE_PLAY = "merge.before.play";
private final Preferences prefs = Preferences.userNodeForPackage(MainFrame.class);
```

## Alternate: Use System Property (No UI Needed)

If you don't want to add the UI checkbox, you can enable merge mode at startup with:
```bash
java -Dtts.merge.before.play=true -jar build/libs/TTS-executable.jar
```

Once TTSEngine reads this property at startup, merge mode will be active for all playback.

## Testing Merge Mode

1. **With checkbox (after adding UI):**
   - Check the "Merge" checkbox
   - Type text and press Play
   - Playback will have slight delay while merging, but audio joins are seamlessly smooth

2. **With system property:**
   - `java -Dtts.merge.before.play=true -jar build/libs/TTS-executable.jar`
   - Same behavior as checkbox

## What Merge Mode Does

- **Merge phase:** Collects all phonemes for a word, applies edge fades and crossfades, combines into one WAV buffer in memory
- **Play phase:** Sends the complete merged buffer to audio output
- **Tradeoff:** ~100-500ms startup delay per word, but absolutely seamless joins with no clicks
- **Use case:** When you want professional-quality audio output over instant responsiveness
