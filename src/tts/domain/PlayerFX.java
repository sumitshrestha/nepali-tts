//                                  !! RAM !!

package tts.domain;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.concurrent.Task;

import java.util.concurrent.CountDownLatch;

/**
 * JavaFX compatible Player for TTS playback
 * Adapted from Swing Player to work with JavaFX
 *
 * @author Sumit Shrestha
 */
public class PlayerFX extends Task<String> {
    private static final String DONE = "DONE";
    private final TextArea textArea;
    private final TTSEngine engine;
    private final FrameInterface listener;
    private final AlphabetParser wordParser = new AlphabetParser();
    private volatile boolean paused = false;

    public PlayerFX(TextArea textarea, TTSEngine engine, FrameInterface listener) {
        this.textArea = textarea;
        this.engine = engine;
        this.listener = listener;
    }

    public void play() {
        paused = false;
    }

    public void pause() {
        paused = true;
    }

    public boolean isPaused() {
        return paused;
    }

    public void playString(String inputText) throws Exception {
        var sentences = SentenceSplitter.splitby(inputText, '.');
        for (int j = 0; j < sentences.length; j++) {
            if (isCancelled()) break;
            
            for (var word : SentenceSplitter.splitby(sentences[j], ' ')) {
                if (isCancelled()) break;
                
                // Wait if paused
                while (paused && !isCancelled()) {
                    Thread.sleep(100);
                }
                
                var parsed = wordParser.parse(word);
                updateMessage(word);
                var phonemePaths = engine.toPhonemePaths(parsed);
                if (engine.isMergeBeforePlayEnabled()) {
                    var merged = engine.mergeSequence(phonemePaths,
                        this::isCancelled,
                        () -> paused);
                    engine.playMerged(merged,
                        this::isCancelled,
                        () -> paused);
                } else {
                    engine.playSequence(phonemePaths,
                        this::isCancelled,
                        () -> paused);
                }
            }
            if (j < sentences.length - 1) {
                Thread.sleep(engine.sentencePauseMs());
            }
        }
    }

    @Override
    protected String call() {
        try {
            playString(textArea.getText());
            return DONE;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    protected void succeeded() {
        listener.onPlayCompletion(DONE);
    }

    @Override
    protected void cancelled() {
        listener.onPlayCompletion("CANCELLED");
    }

    @Override
    protected void failed() {
        getException().printStackTrace();
        listener.onPlayCompletion("ERROR");
    }
}
