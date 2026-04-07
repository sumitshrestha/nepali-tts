//                                  !! RAM !!

package tts.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

/**
 * Modern JavaFX UI for Nepali TTS Application
 *
 * @author Sumit Shrestha
 */
public class MainFrameFX implements tts.domain.FrameInterface {

    private static final String PREF_AUDIO_PROFILE = "audio.profile";
    private final Preferences prefs = Preferences.userNodeForPackage(MainFrameFX.class);
    
    private Stage stage;
    private TextArea inputText;
    private Button playBtn;
    private Button stopBtn;
    private Button pauseBtn;
    private ComboBox<String> profileCombo;
    private Label profileStatusLabel;
    private Label statusLabel;
    private ProgressBar playbackProgress;
    
    private tts.domain.TTSEngine player = new tts.domain.TTSEngine();
    private tts.domain.PlayerFX presentPlayerWorker;

    public void show(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.setTitle("Sumit's Nepali TTS");
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        
        // Set window icon
        setFrameIcon(primaryStage);
        
        // Create main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-font-family: 'Segoe UI', 'Arial', sans-serif;");
        
        // Top toolbar
        root.setTop(createToolbar());
        
        // Center - split pane with text input and shortcuts
        root.setCenter(createCentralPanel());
        
        // Bottom status bar
        root.setBottom(createStatusBar());
        
        // Apply modern styling
        applyModernStyling(root);
        
        Scene scene = new Scene(root, 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        loadSavedAudioProfile();
    }

    private VBox createToolbar() {
        VBox toolbar = new VBox(12);
        toolbar.setPadding(new Insets(16, 16, 16, 16));
        toolbar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        // Control buttons row
        HBox controlsRow = new HBox(12);
        controlsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        playBtn = createIconButton("/tts/ui/img/play.png", "Play", 56, 56);
        stopBtn = createIconButton("/tts/ui/img/stop.png", "Stop", 56, 56);
        pauseBtn = createIconButton("/tts/ui/img/pause.png", "Pause", 56, 56);
        
        stopBtn.setDisable(true);
        pauseBtn.setDisable(true);
        
        playBtn.setOnAction(e -> onPlayAction());
        stopBtn.setOnAction(e -> onStopAction());
        pauseBtn.setOnAction(e -> onPauseAction());
        
        controlsRow.getChildren().addAll(playBtn, stopBtn, pauseBtn);
        
        // Separator
        Separator sep = new Separator();
        sep.setOrientation(Orientation.VERTICAL);
        
        // Audio profile section
        HBox profileSection = new HBox(8);
        profileSection.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label audioLabel = new Label("Audio Profile:");
        audioLabel.setStyle("-fx-font-weight: bold;");
        
        profileCombo = new ComboBox<>();
        profileCombo.getItems().addAll("crisp", "balanced", "smooth");
        profileCombo.setPrefWidth(120);
        profileCombo.setOnAction(e -> onProfileChanged());
        
        profileStatusLabel = new Label("Profile: balanced");
        profileStatusLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10;");
        
        profileSection.getChildren().addAll(audioLabel, profileCombo, profileStatusLabel);
        
        // Logo - increased size for better visibility
        HBox logoBox = new HBox();
        logoBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        logoBox.setPadding(new Insets(0, 8, 0, 20));
        logoBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 0 1;");
        HBox.setHgrow(logoBox, Priority.ALWAYS);
        ImageView logoView = createImageView("/tts/ui/img/logo.png", 120, 120);
        logoBox.getChildren().add(logoView);
        
        // Add all sections to control row
        controlsRow.getChildren().addAll(sep, profileSection, logoBox);
        HBox.setHgrow(logoBox, Priority.ALWAYS);
        
        toolbar.getChildren().add(controlsRow);
        
        // Playback progress bar
        playbackProgress = new ProgressBar(0);
        playbackProgress.setPrefHeight(4);
        playbackProgress.setStyle("-fx-control-inner-background: #e0e0e0;");
        toolbar.getChildren().add(playbackProgress);
        
        return toolbar;
    }

    private SplitPane createCentralPanel() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPosition(0, 0.75);
        
        // Left: Text input area
        VBox leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(12));
        
        Label inputLabel = new Label("Enter Text to Convert:");
        inputLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        
        inputText = new TextArea();
        inputText.setWrapText(true);
        inputText.setPrefRowCount(20);
        inputText.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 13; -fx-padding: 8;");
        inputText.setPromptText("Enter Nepali text here and click Play to hear it...");
        
        VBox.setVgrow(inputText, Priority.ALWAYS);
        leftPanel.getChildren().addAll(inputLabel, inputText);
        
        // Right: Shortcuts/Info panel
        VBox rightPanel = new VBox(12);
        rightPanel.setPadding(new Insets(12));
        rightPanel.setStyle("-fx-background-color: #fafafa;");
        
        Label shortcutsLabel = new Label("Keyboard Shortcuts");
        shortcutsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        
        TextArea shortcutsText = new TextArea(
            "Space - Play/Resume\n" +
            "P - Pause\n" +
            "S - Stop\n" +
            "\n" +
            "Ctrl+A - Select All\n" +
            "Ctrl+C - Copy\n" +
            "Ctrl+V - Paste\n" +
            "\n" +
            "Audio Profiles:\n" +
            "• Crisp - Clear, bright\n" +
            "• Balanced - Natural\n" +
            "• Smooth - Warm, soft"
        );
        shortcutsText.setEditable(false);
        shortcutsText.setWrapText(true);
        shortcutsText.setStyle(
            "-fx-control-inner-background: #ffffff; " +
            "-fx-font-family: monospace; " +
            "-fx-font-size: 11; " +
            "-fx-padding: 8;"
        );
        
        VBox.setVgrow(shortcutsText, Priority.ALWAYS);
        rightPanel.getChildren().addAll(shortcutsLabel, shortcutsText);
        
        splitPane.getItems().addAll(leftPanel, rightPanel);
        return splitPane;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(12);
        statusBar.setPadding(new Insets(8, 12, 8, 12));
        statusBar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #666666;");
        
        Label versionLabel = new Label("Nepali TTS v1.0");
        versionLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 10;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statusBar.getChildren().addAll(statusLabel, spacer, versionLabel);
        return statusBar;
    }

    private Button createIconButton(String iconPath, String tooltip, double width, double height) {
        Button btn = new Button();
        ImageView imageView = createImageView(iconPath, width - 8, height - 8);
        btn.setGraphic(imageView);
        btn.setPrefSize(width, height);
        btn.setStyle(
            "-fx-padding: 6; " +
            "-fx-background-color: transparent; " +
            "-fx-cursor: hand; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );
        btn.setTooltip(new Tooltip(tooltip));
        
        // Hover effect
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + " -fx-background-color: #e8e8e8;"));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-padding: 6; " +
            "-fx-background-color: transparent; " +
            "-fx-cursor: hand; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        ));
        
        return btn;
    }

    private ImageView createImageView(String resourcePath, double width, double height) {
        try {
            Image image = new Image(getClass().getResourceAsStream(resourcePath));
            ImageView view = new ImageView(image);
            view.setFitWidth(width);
            view.setFitHeight(height);
            view.setPreserveRatio(true);
            return view;
        } catch (Exception e) {
            System.err.println("Failed to load image: " + resourcePath);
            // Return a placeholder with no visible content
            ImageView fallback = new ImageView();
            fallback.setFitWidth(width);
            fallback.setFitHeight(height);
            return fallback;
        }
    }

    private void setFrameIcon(Stage stage) {
        try {
            Image icon = new Image(getClass().getResourceAsStream("/tts/ui/img/nepal.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Failed to load window icon");
        }
    }

    private void applyModernStyling(BorderPane root) {
        String stylesheet = "-fx-font-size: 12; " +
            "-fx-text-fill: #333333; " +
            "-fx-control-inner-background: #ffffff;";
        root.setStyle(stylesheet);
    }

    private void onPlayAction() {
        if (presentPlayerWorker != null && !presentPlayerWorker.isDone()) {
            presentPlayerWorker.play();
        } else {
            presentPlayerWorker = new tts.domain.PlayerFX(inputText, player, this);
            Thread thread = new Thread(presentPlayerWorker);
            thread.setDaemon(true);
            thread.start();
        }
        updateButtonStates(false, true, true);
        statusLabel.setText("Playing...");
        playbackProgress.setProgress(0.3); // Simulate progress
        inputText.requestFocus();
    }

    private void onStopAction() {
        if (presentPlayerWorker != null) {
            presentPlayerWorker.cancel();
        }
        updateButtonStates(true, false, false);
        statusLabel.setText("Stopped");
        playbackProgress.setProgress(0);
    }

    private void onPauseAction() {
        if (presentPlayerWorker != null && !presentPlayerWorker.isPaused()) {
            presentPlayerWorker.pause();
            updateButtonStates(true, false, false);
            statusLabel.setText("Paused");
        }
    }

    private void onProfileChanged() {
        Object selected = profileCombo.getSelectionModel().getSelectedItem();
        if (selected instanceof String s) {
            var profile = tts.domain.TTSEngine.AudioProfile.from(s);
            player.setProfile(profile);
            prefs.put(PREF_AUDIO_PROFILE, profile.name().toLowerCase());
            updateProfileStatusLabel();
        }
    }

    private void updateButtonStates(boolean playEnabled, boolean stopEnabled, boolean pauseEnabled) {
        Platform.runLater(() -> {
            playBtn.setDisable(!playEnabled);
            stopBtn.setDisable(!stopEnabled);
            pauseBtn.setDisable(!pauseEnabled);
        });
    }

    private void updateProfileStatusLabel() {
        var currentProfile = player.getProfile().name().toLowerCase();
        profileStatusLabel.setText("Profile: " + currentProfile);
    }

    private void loadSavedAudioProfile() {
        var saved = prefs.get(PREF_AUDIO_PROFILE, player.getProfile().name().toLowerCase());
        var profile = tts.domain.TTSEngine.AudioProfile.from(saved);
        player.setProfile(profile);
        profileCombo.setValue(profile.name().toLowerCase());
        updateProfileStatusLabel();
    }

    @Override
    public void onPlayCompletion(String state) {
        Platform.runLater(() -> {
            inputText.setStyle("");
            presentPlayerWorker = null;
            updateButtonStates(true, false, false);
            statusLabel.setText("Playback Complete");
            playbackProgress.setProgress(1.0);
        });
    }
}
