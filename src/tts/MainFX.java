//                                          !! RAM !!

package tts;

import javafx.application.Application;
import javafx.stage.Stage;
import tts.ui.MainFrameFX;

/**
 * JavaFX Application entry point for Nepali TTS
 *
 * @author Sumit Shrestha
 */
public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainFrameFX ui = new MainFrameFX();
        ui.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
