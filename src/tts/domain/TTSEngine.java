//                                          !! RAM !!
package tts.domain;

import javax.sound.sampled.*;

/**
 *
 * @author Sumit Shrestha
 */
public class TTSEngine implements LineListener {

    private URL FileAddress;
    private final String DbName = "soundDb";

    // sets the address from which to get the sound files
    public void setFileAddress(URL add) {
        FileAddress = add;
    }

    public String[] returnwordarray(String word) {
        String[] a = new String[word.length()];
        char[] wordchar = word.toCharArray();
        int j = 0;
        for (int i = 0; i < wordchar.length; i++) {
            if (!Character.isDigit(wordchar[i])) {
                a[j] = String.valueOf(wordchar[i]);
            } else {
                if (j == 0) {
                    System.out.println(" wrong input :: input has illegal character at the begginnig ");
                    return null;
                } else {
                    a[--j] += wordchar[i];
                }
            }
            j++;
        }
        String[] h = new String[j];

        // add needed words
        for (int i = 0; i < j; i++) {
            h[i] = a[i] + ".wav";
        }

        return h;
    }

    public void update(LineEvent e) {
        System.out.println(" update started ");
    }

    public void play(String file) throws Exception {
        AudioInputStream stream = AudioSystem.getAudioInputStream(
                getClass().getResource("/" + this.DbName + "/" + file));
        AudioFormat format = stream.getFormat();
        SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, format,
                ((int) stream.getFrameLength() * format.getFrameSize()));
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        try {
            line.start();
            int numRead;
            byte[] buf = new byte[line.getBufferSize()];
            while ((numRead = stream.read(buf, 0, buf.length)) >= 0) {
                int offset = 0;
                while (offset < numRead) {
                    offset += line.write(buf, offset, numRead - offset);
                }
            }
            line.drain();
        } finally {
            line.stop();
            line.close();
            stream.close();
        }
    }

}
