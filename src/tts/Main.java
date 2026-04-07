
//                                          !! RAM !!

package tts;

/**
 *
 * @author Sumit Shrestha
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new tts.ui.MainFrame().setVisible(true);
            }
        });
    }
}