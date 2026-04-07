package tts.domain;

import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.util.function.BooleanSupplier;
import javax.sound.sampled.*;

public class TTSEngine {

    private static final String DB_NAME = "soundDb";
    private static final float DEFAULT_SAMPLE_RATE = 22050.0f;
    private static final int BYTES_PER_SAMPLE = 2; // 16-bit PCM mono
    private static final String PROFILE_PROPERTY = "tts.audio.profile";
    private volatile AudioProfile profile;
    private volatile boolean mergeBeforePlay = false;

    public TTSEngine() {
        this(AudioProfile.from(System.getProperty(PROFILE_PROPERTY)));
    }

    public TTSEngine(AudioProfile profile) {
        this.profile = profile == null ? AudioProfile.BALANCED : profile;
    }

    public AudioProfile getProfile() {
        return profile;
    }

    public void setProfile(AudioProfile profile) {
        if (profile != null) {
            this.profile = profile;
        }
    }

    public boolean isMergeBeforePlayEnabled() {
        return mergeBeforePlay;
    }

    public void setMergeBeforePlay(boolean enabled) {
        this.mergeBeforePlay = enabled;
    }

    public int sentencePauseMs() {
        return profile.sentencePauseMs();
    }

    /**
     * Converts a parsed phoneme string into an array of WAV file paths.
     * Digit suffixes (e.g. "a1") are appended to the preceding phoneme token.
     */
    public String[] toPhonemePaths(String word) {
        var parts = new ArrayList<String>();
        for (char c : word.toCharArray()) {
            if (!Character.isDigit(c)) {
                parts.add(String.valueOf(c));
            } else {
                if (parts.isEmpty()) {
                    throw new IllegalArgumentException("Word starts with a digit: " + word);
                }
                parts.set(parts.size() - 1, parts.getLast() + c);
            }
        }
        return parts.stream()
                    .map(p -> p + ".wav")
                    .toArray(String[]::new);
    }

    public void playSequence(String[] files,
                             BooleanSupplier isCancelled,
                             BooleanSupplier isPaused) throws Exception {
        if (files == null || files.length == 0) {
            return;
        }

        SourceDataLine line = null;
        AudioFormat lineFormat = null;
        byte[] pending = null;

        try {
            for (String file : files) {
                if (isCancelled.getAsBoolean()) {
                    return;
                }

                while (isPaused.getAsBoolean()) {
                    if (isCancelled.getAsBoolean()) {
                        return;
                    }
                    Thread.sleep(10);
                }

                if (lineFormat == null) {
                    lineFormat = resolvePlaybackFormat(file);
                    var info = new DataLine.Info(SourceDataLine.class, lineFormat);
                    line = (SourceDataLine) AudioSystem.getLine(info);
                    line.open(lineFormat);
                    line.start();
                }

                var currentProfile = this.profile;
                var current = decodeToPcm(file, lineFormat);
                applyEdgeFade(current, lineFormat, currentProfile.edgeFadeMs());

                if (pending == null) {
                    pending = current;
                    continue;
                }

                int overlapBytes = overlapBytes(lineFormat, pending.length, current.length, currentProfile.crossfadeMs());
                int writeHeadBytes = pending.length - overlapBytes;
                if (writeHeadBytes > 0) {
                    writeWithControl(line, pending, 0, writeHeadBytes, isCancelled, isPaused);
                }

                if (overlapBytes > 0) {
                    var blend = buildCrossfade(pending, current, overlapBytes);
                    writeWithControl(line, blend, 0, blend.length, isCancelled, isPaused);
                }

                int currentRemainder = current.length - overlapBytes;
                if (currentRemainder <= 0) {
                    pending = null;
                } else {
                    pending = new byte[currentRemainder];
                    System.arraycopy(current, overlapBytes, pending, 0, currentRemainder);
                }
            }

            if (pending != null && pending.length > 0 && line != null) {
                writeWithControl(line, pending, 0, pending.length, isCancelled, isPaused);
            }

            if (line != null) {
                line.drain();
            }
        } finally {
            if (line != null) {
                line.stop();
                line.close();
            }
        }
    }

    public void play(String file) throws Exception {
        playSequence(new String[]{file}, () -> false, () -> false);
    }

    private AudioFormat resolvePlaybackFormat(String firstFile) throws Exception {
        var url = getClass().getResource("/" + DB_NAME + "/" + firstFile);
        if (url == null) {
            throw new IllegalArgumentException("Audio file not found: " + firstFile);
        }
        try (var source = AudioSystem.getAudioInputStream(url)) {
            float sampleRate = source.getFormat().getSampleRate() > 0
                    ? source.getFormat().getSampleRate()
                    : DEFAULT_SAMPLE_RATE;
            return new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sampleRate,
                    16,
                    1,
                    BYTES_PER_SAMPLE,
                    sampleRate,
                    false);
        }
    }

    private byte[] decodeToPcm(String file, AudioFormat targetFormat) throws Exception {
        var url = getClass().getResource("/" + DB_NAME + "/" + file);
        if (url == null) {
            throw new IllegalArgumentException("Audio file not found: " + file);
        }

        try (var source = AudioSystem.getAudioInputStream(url);
             var converted = AudioSystem.getAudioInputStream(targetFormat, source);
             var out = new ByteArrayOutputStream()) {
            var buf = new byte[8192];
            int n;
            while ((n = converted.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            var pcm = out.toByteArray();
            int aligned = pcm.length - (pcm.length % BYTES_PER_SAMPLE);
            if (aligned == pcm.length) {
                return pcm;
            }
            var trimmed = new byte[aligned];
            System.arraycopy(pcm, 0, trimmed, 0, aligned);
            return trimmed;
        }
    }

    private int overlapBytes(AudioFormat format, int leftBytes, int rightBytes, int crossfadeMs) {
        int requested = (int) ((format.getSampleRate() * crossfadeMs / 1000.0f) * BYTES_PER_SAMPLE);
        requested -= requested % BYTES_PER_SAMPLE;
        int max = Math.min(leftBytes, rightBytes);
        max -= max % BYTES_PER_SAMPLE;
        return Math.max(0, Math.min(requested, max));
    }

    private void applyEdgeFade(byte[] pcm, AudioFormat format, int fadeMs) {
        int fadeSamples = (int) (format.getSampleRate() * fadeMs / 1000.0f);
        int totalSamples = pcm.length / BYTES_PER_SAMPLE;
        int edge = Math.min(fadeSamples, totalSamples / 2);
        if (edge <= 0) {
            return;
        }

        for (int i = 0; i < edge; i++) {
            float inGain = (i + 1) / (float) edge;
            float outGain = (edge - i) / (float) edge;

            int head = i * BYTES_PER_SAMPLE;
            short headSample = readSample(pcm, head);
            writeSample(pcm, head, (short) (headSample * inGain));

            int tailSampleIndex = totalSamples - edge + i;
            int tail = tailSampleIndex * BYTES_PER_SAMPLE;
            short tailSample = readSample(pcm, tail);
            writeSample(pcm, tail, (short) (tailSample * outGain));
        }
    }

    private byte[] buildCrossfade(byte[] left, byte[] right, int overlapBytes) {
        int overlapSamples = overlapBytes / BYTES_PER_SAMPLE;
        var mixed = new byte[overlapBytes];
        if (overlapSamples == 0) {
            return mixed;
        }

        int leftStart = left.length - overlapBytes;
        for (int i = 0; i < overlapSamples; i++) {
            float t = overlapSamples == 1 ? 1.0f : i / (float) (overlapSamples - 1);
            short a = readSample(left, leftStart + (i * BYTES_PER_SAMPLE));
            short b = readSample(right, i * BYTES_PER_SAMPLE);
            short y = (short) ((a * (1.0f - t)) + (b * t));
            writeSample(mixed, i * BYTES_PER_SAMPLE, y);
        }
        return mixed;
    }

    private void writeWithControl(SourceDataLine line,
                                  byte[] data,
                                  int off,
                                  int len,
                                  BooleanSupplier isCancelled,
                                  BooleanSupplier isPaused) throws InterruptedException {
        int cursor = off;
        int end = off + len;
        while (cursor < end) {
            if (isCancelled.getAsBoolean()) {
                return;
            }
            while (isPaused.getAsBoolean()) {
                if (isCancelled.getAsBoolean()) {
                    return;
                }
                Thread.sleep(10);
            }

            int chunk = Math.min(4096, end - cursor);
            int written = line.write(data, cursor, chunk);
            if (written > 0) {
                cursor += written;
            }
        }
    }

    private short readSample(byte[] pcm, int index) {
        int lo = pcm[index] & 0xff;
        int hi = pcm[index + 1];
        return (short) ((hi << 8) | lo);
    }

    private void writeSample(byte[] pcm, int index, short value) {
        pcm[index] = (byte) (value & 0xff);
        pcm[index + 1] = (byte) ((value >>> 8) & 0xff);
    }

    /**
     * Merges a phoneme sequence into one contiguous PCM byte array with all fades and crossfades pre-applied.
     * Returns the complete merged audio buffer ready for playback.
     */
    public MergedAudio mergeSequence(String[] files,
                                     BooleanSupplier isCancelled,
                                     BooleanSupplier isPaused) throws Exception {
        if (files == null || files.length == 0) {
            return new MergedAudio(new byte[0], null);
        }

        AudioFormat format = resolvePlaybackFormat(files[0]);
        var output = new ByteArrayOutputStream();
        byte[] pending = null;

        for (String file : files) {
            if (isCancelled.getAsBoolean()) {
                return new MergedAudio(new byte[0], format);
            }

            while (isPaused.getAsBoolean()) {
                if (isCancelled.getAsBoolean()) {
                    return new MergedAudio(new byte[0], format);
                }
                Thread.sleep(10);
            }

            var currentProfile = this.profile;
            var current = decodeToPcm(file, format);
            applyEdgeFade(current, format, currentProfile.edgeFadeMs());

            if (pending == null) {
                pending = current;
                continue;
            }

            int overlapBytes = overlapBytes(format, pending.length, current.length, currentProfile.crossfadeMs());
            int writeHeadBytes = pending.length - overlapBytes;
            if (writeHeadBytes > 0) {
                output.write(pending, 0, writeHeadBytes);
            }

            if (overlapBytes > 0) {
                var blend = buildCrossfade(pending, current, overlapBytes);
                output.write(blend, 0, blend.length);
            }

            int currentRemainder = current.length - overlapBytes;
            if (currentRemainder <= 0) {
                pending = null;
            } else {
                pending = new byte[currentRemainder];
                System.arraycopy(current, overlapBytes, pending, 0, currentRemainder);
            }
        }

        if (pending != null && pending.length > 0) {
            output.write(pending);
        }

        return new MergedAudio(output.toByteArray(), format);
    }

    /**
     * Plays pre-merged PCM byte array through a single audio line.
     */
    public void playMerged(MergedAudio mergedAudio,
                           BooleanSupplier isCancelled,
                           BooleanSupplier isPaused) throws Exception {
        if (mergedAudio == null || mergedAudio.data().length == 0) {
            return;
        }

        AudioFormat format = mergedAudio.format();
        if (format == null) {
            format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    DEFAULT_SAMPLE_RATE,
                    16,
                    1,
                    BYTES_PER_SAMPLE,
                    DEFAULT_SAMPLE_RATE,
                    false);
        }

        SourceDataLine line = null;
        try {
            var info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            writeWithControl(line, mergedAudio.data(), 0, mergedAudio.data().length, isCancelled, isPaused);
            
            if (line != null) {
                line.drain();
            }
        } finally {
            if (line != null) {
                line.stop();
                line.close();
            }
        }
    }

    public enum AudioProfile {
        CRISP(2, 4, 350),
        BALANCED(4, 8, 500),
        SMOOTH(6, 14, 650);

        private final int edgeFadeMs;
        private final int crossfadeMs;
        private final int sentencePauseMs;

        AudioProfile(int edgeFadeMs, int crossfadeMs, int sentencePauseMs) {
            this.edgeFadeMs = edgeFadeMs;
            this.crossfadeMs = crossfadeMs;
            this.sentencePauseMs = sentencePauseMs;
        }

        public int edgeFadeMs() {
            return edgeFadeMs;
        }

        public int crossfadeMs() {
            return crossfadeMs;
        }

        public int sentencePauseMs() {
            return sentencePauseMs;
        }

        public static AudioProfile from(String value) {
            if (value == null || value.isBlank()) {
                return BALANCED;
            }
            return switch (value.strip().toLowerCase()) {
                case "crisp" -> CRISP;
                case "smooth" -> SMOOTH;
                case "balanced" -> BALANCED;
                default -> BALANCED;
            };
        }
    }

    public record MergedAudio(byte[] data, AudioFormat format) {}
}
