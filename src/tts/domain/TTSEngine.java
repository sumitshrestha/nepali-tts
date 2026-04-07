package tts.domain;

import java.util.ArrayList;
import javax.sound.sampled.*;

public class TTSEngine {

    private static final String DB_NAME = "soundDb";

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

    public void play(String file) throws Exception {
        try (var stream = AudioSystem.getAudioInputStream(
                getClass().getResource("/" + DB_NAME + "/" + file))) {
            var format = stream.getFormat();
            var info = new DataLine.Info(SourceDataLine.class, format,
                    (int) (stream.getFrameLength() * format.getFrameSize()));
            try (var line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                var buf = new byte[line.getBufferSize()];
                int numRead;
                while ((numRead = stream.read(buf, 0, buf.length)) >= 0) {
                    int offset = 0;
                    while (offset < numRead) {
                        offset += line.write(buf, offset, numRead - offset);
                    }
                }
                line.drain();
            }
        }
    }
}
