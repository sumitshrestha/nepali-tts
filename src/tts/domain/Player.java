package tts.domain;

import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

public class Player extends SwingWorker<String, String> {

    public Player(JTextPane textarea, TTSEngine engine, FrameInterface listener) {
        this.text = textarea;
        this.engine = engine;
        this.listener = listener;
    }

    public void playString(String inputText) throws Exception {
        this.txtPtr = 0;
        var sentences = SentenceSplitter.splitby(inputText, '.');
        for (int j = 0; j < sentences.length; j++) {
            for (var word : SentenceSplitter.splitby(sentences[j], ' ')) {
                if (isCancelled()) return;
                while (paused) {
                    if (isCancelled()) return;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                var parsed = wordParser.parse(word);
                publish(word);
                var phonemePaths = engine.toPhonemePaths(parsed);

                if (engine.isMergeBeforePlayEnabled()) {
                    var merged = engine.mergeSequence(phonemePaths, this::isCancelled, () -> paused);
                    engine.playMerged(merged, this::isCancelled, () -> paused);
                } else {
                    engine.playSequence(phonemePaths, this::isCancelled, () -> paused);
                }
            }
            if (j < sentences.length - 1) {
                Thread.sleep(engine.sentencePauseMs());
            }
        }
    }

    @Override
    protected String doInBackground() {
        try {
            playString(text.getText());
            return DONE;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    protected void done() {
        if (isCancelled()) {
            listener.onPlayCompletion(DONE);
            return;
        }
        try {
            var state = get();
            listener.onPlayCompletion(state);
            if (DONE.equals(state)) {
                JOptionPane.showMessageDialog(null, "Text played successfully", "Done",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, state, "Playback Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.onPlayCompletion(DONE);
        } catch (ExecutionException e) {
            listener.onPlayCompletion(e.getMessage());
        }
    }

    @Override
    protected void process(List<String> words) {
        var currentText = text.getText().substring(txtPtr);
        var spokenWord = words.getLast();

        int idx = currentText.indexOf(spokenWord) + txtPtr;
        if (idx >= txtPtr) {
            text.setCaretPosition(idx);
            text.select(idx, idx + spokenWord.length());
            txtPtr = idx + spokenWord.length();
        } else {
            JOptionPane.showMessageDialog(null, "Word not found: " + spokenWord,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void pause()           { paused = true; }
    public void play()            { paused = false; }
    public boolean isPaused()     { return paused; }

    public static final String DONE = "Done";

    private final FrameInterface listener;
    private final JTextPane text;
    private final TTSEngine engine;
    private final AlphabetParser wordParser = new AlphabetParser();
    private volatile boolean paused = false;
    private int txtPtr = 0;
}