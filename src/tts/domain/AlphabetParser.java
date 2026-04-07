package tts.domain;

import java.util.LinkedList;

/**
 * Parses a single romanized Nepali word and resolves phonetic ambiguities
 * so the TTS engine can map tokens to audio files unambiguously.
 */
public class AlphabetParser {

    public String parse(String token) {
        var stack = new LinkedList<String>();
        for (char c : token.toLowerCase().toCharArray()) {
            if (stack.isEmpty()) {
                stack.push(String.valueOf(c));
                continue;
            }
            var top = stack.getFirst();
            if (VOWELS.contains(top) && top.equals(String.valueOf(c))) {
                // doubled vowel: aa→a1, ee→e1, oo→o1
                stack.pop();
                stack.push(top + "1");
            } else if (CONSONANTS.contains(top) && c == 'h') {
                // aspirated consonant: kh→k1, gh→g1, bh→b1, etc.
                stack.pop();
                stack.push(top + "1");
            } else if (top.equals("a") && c == 'u') {
                // diphthong au → o
                stack.pop();
                stack.push("o");
            } else if (top.equals("a") && c == 'i') {
                // diphthong ai → e
                stack.pop();
                stack.push("e");
            } else {
                stack.push(String.valueOf(c));
            }
        }
        var result = new StringBuilder();
        while (!stack.isEmpty()) {
            result.append(stack.pollLast());
        }
        return result.toString();
    }

    private static final String VOWELS = "aeiou";
    private static final String CONSONANTS = "kgcjtdpbs";
}