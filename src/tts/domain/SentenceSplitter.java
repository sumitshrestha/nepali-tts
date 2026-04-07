package tts.domain;

import java.util.Arrays;
import java.util.regex.Pattern;

public class SentenceSplitter {

    /**
     * Splits {@code input} on {@code delimiter}, trims whitespace from each token,
     * and discards blank tokens.
     */
    public static String[] splitby(String input, char delimiter) {
        if (input == null || input.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(input.split(Pattern.quote(String.valueOf(delimiter))))
                     .map(String::strip)
                     .filter(s -> !s.isBlank())
                     .toArray(String[]::new);
    }
}
