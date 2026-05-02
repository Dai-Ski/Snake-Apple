package com.snake.ui;

import com.snake.domain.core.Game;
import com.snake.domain.model.GameState;
import com.snake.ui.GameRenderer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.value.WritableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main
extends Application {
    private static final int COLS = 20;
    private static final int ROWS = 20;
    private static final int CELL_SIZE = 30;
    private Game game;
    private GameRenderer renderer;
    private GameState currentState;
    private long lastTickTime = 0L;
    private final Queue<GameState.Direction> inputBuffer = new LinkedList<GameState.Direction>();
    private boolean gameIsRunning = false;
    private final List<Integer> highScores = new ArrayList<Integer>();
    private static final String SCORE_FILE = System.getProperty("user.home") + File.separator + ".eves_apple_scores.txt";
    private StackPane rootPane;
    private VBox startScreen;
    private VBox levelUpOverlay;
    private VBox scoreboardOverlay;
    private VBox pauseOverlay;
    private AnimationTimer gameTimer;
    private Font pixelFont;
    private static final String FONT_URL = "https://github.com/google/fonts/raw/main/ofl/pressstart2p/PressStart2P-Regular.ttf";

    private void loadHighScores() {
        this.highScores.clear();
        File file = new File(SCORE_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file));){
                String line;
                while ((line = reader.readLine()) != null) {
                    this.highScores.add(Integer.parseInt(line.trim()));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        while (this.highScores.size() < 10) {
            this.highScores.add(0);
        }
        this.highScores.sort(Collections.reverseOrder());
    }

    private void saveHighScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCORE_FILE));){
            for (int score : this.highScores) {
                writer.println(score);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNewRecord(int score) {
        if (score <= 0) {
            return false;
        }
        if (this.highScores.isEmpty()) {
            return true;
        }
        return score > this.highScores.get(0);
    }

    private void updateHighScores(int newScore) {
        this.highScores.add(newScore);
        this.highScores.sort(Collections.reverseOrder());
        while (this.highScores.size() > 10) {
            this.highScores.remove(this.highScores.size() - 1);
        }
        this.saveHighScores();
    }

    public void start(Stage primaryStage) {
        String[][] links;
        this.loadHighScores();
        try {
            this.pixelFont = Font.loadFont(FONT_URL, 14.0);
            if (this.pixelFont == null) {
                this.pixelFont = Font.font("Courier New", 14.0);
            }
        }
        catch (Exception e2) {
            this.pixelFont = Font.font("Courier New", 14.0);
        }
        Canvas canvas = new Canvas(600.0, 600.0);
        this.renderer = new GameRenderer(canvas, 20, 20);
        VBox gameLayout = new VBox(new Node[]{canvas});
        gameLayout.setAlignment(Pos.CENTER);
        gameLayout.setStyle("-fx-background-color: #000000;");
        this.rootPane = new StackPane();
        this.rootPane.getChildren().add(gameLayout);
        this.pauseOverlay = new VBox(20.0);
        this.pauseOverlay.setAlignment(Pos.CENTER);
        this.pauseOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-padding: 20;");
        this.pauseOverlay.setVisible(false);
        Text pauseTitle = new Text("GAME PAUSED");
        pauseTitle.setFont(Font.font(this.pixelFont.getFamily(), 28.0));
        pauseTitle.setStyle("-fx-fill: #FFCC00;");
        Text pauseInstructions = new Text("\u2022 Press SPACE to Resume\n\u2022 Press ESC to Return to Home");
        pauseInstructions.setFont(Font.font(this.pixelFont.getFamily(), 14.0));
        pauseInstructions.setStyle("-fx-fill: #FFFFFF; -fx-line-spacing: 10px;");
        this.pauseOverlay.getChildren().addAll(new Node[]{pauseTitle, pauseInstructions});
        VBox helpOverlay = new VBox(15.0);
        helpOverlay.setAlignment(Pos.CENTER);
        helpOverlay.setStyle("-fx-background-color: #000000; -fx-padding: 20;");
        helpOverlay.setVisible(false);
        Text helpTitle = new Text("How to Play & Rules");
        helpTitle.setFont(Font.font(this.pixelFont.getFamily(), 20.0));
        helpTitle.setStyle("-fx-fill: white;");
        Text helpBody = new Text("\u2022 Use W, A, S, D or Arrow Keys to move.\n\u2022 Eat the Apple to grow and score points.\n\u2022 Spacebar pauses and resumes the game.\n\u2022 Wall collisions wrap around safely.");
        helpBody.setFont(Font.font(this.pixelFont.getFamily(), 12.0));
        helpBody.setStyle("-fx-fill: #cccccc; -fx-line-spacing: 5px;");
        Button closeHelpBtn = new Button("Back");
        closeHelpBtn.setFont(Font.font(this.pixelFont.getFamily(), 12.0));
        closeHelpBtn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-cursor: hand;");
        closeHelpBtn.setOnAction(e -> helpOverlay.setVisible(false));
        helpOverlay.getChildren().addAll(new Node[]{helpTitle, helpBody, closeHelpBtn});
        VBox profileOverlay = new VBox(15.0);
        profileOverlay.setAlignment(Pos.CENTER);
        profileOverlay.setStyle("-fx-background-color: #000000; -fx-padding: 20;");
        profileOverlay.setVisible(false);
        Text profileTitle = new Text("Connect with me:");
        profileTitle.setFont(Font.font(this.pixelFont.getFamily(), 16.0));
        profileTitle.setStyle("-fx-fill: white;");
        VBox linksBox = new VBox(10.0);
        linksBox.setAlignment(Pos.CENTER);
        for (String[] link : links = new String[][]{{"GitHub", "https://github.com"}, {"LinkedIn", "https://www.linkedin.com/in/daiski"}, {"Discord", "https://discord.com/users/dai._.ski"}, {"Instagram", "https://www.instagram.com/dai._ski/"}, {"X (Twitter)", "https://x.com/ado_daiski"}}) {
            Button linkBtn = new Button(link[0]);
            linkBtn.setFont(Font.font(this.pixelFont.getFamily(), 12.0));
            linkBtn.setStyle("-fx-background-color: #0077cc; -fx-text-fill: white; -fx-cursor: hand; -fx-pref-width: 200px;");
            linkBtn.setOnAction(e -> this.getHostServices().showDocument(link[1]));
            linksBox.getChildren().add(linkBtn);
        }
        Button closeProfileBtn = new Button("Back");
        closeProfileBtn.setFont(Font.font(this.pixelFont.getFamily(), 12.0));
        closeProfileBtn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-cursor: hand;");
        closeProfileBtn.setOnAction(e -> profileOverlay.setVisible(false));
        profileOverlay.getChildren().addAll(new Node[]{profileTitle, linksBox, closeProfileBtn});
        this.startScreen = new VBox(20.0);
        this.startScreen.setAlignment(Pos.CENTER);
        this.startScreen.setStyle("-fx-background-color: #000000; -fx-padding: 20;");
        Text titleText = new Text("EVE'S APPLE");
        titleText.setFont(Font.font(this.pixelFont.getFamily(), 32.0));
        titleText.setStyle("-fx-fill: #E60000;");
        Text recordsTitle = new Text("\u2605 \u2605 RECORDS \u2605 \u2605");
        recordsTitle.setFont(Font.font(this.pixelFont.getFamily(), 16.0));
        recordsTitle.setStyle("-fx-fill: #FFCC00;");
        VBox recordsBox = new VBox(5.0);
        recordsBox.setAlignment(Pos.CENTER);
        recordsBox.setStyle("-fx-border-color: #FFCC00; -fx-border-width: 2px; -fx-padding: 15; -fx-max-width: 320px;");
        this.populateScoreboard(recordsBox);
        Button startBtn = new Button();
        VBox btnContent = new VBox(2.0);
        btnContent.setAlignment(Pos.CENTER);
        Text pressText = new Text("PRESS");
        pressText.setFont(Font.font(this.pixelFont.getFamily(), 16.0));
        pressText.setStyle("-fx-fill: #E60000;");
        Text startText = new Text("START");
        startText.setFont(Font.font(this.pixelFont.getFamily(), 28.0));
        startText.setStyle("-fx-fill: #FFFFFF;");
        btnContent.getChildren().addAll(new Node[]{pressText, startText});
        startBtn.setGraphic(btnContent);
        startBtn.setStyle("-fx-background-color: #333333; -fx-cursor: hand; -fx-padding: 10 30; -fx-border-color: #666666; -fx-border-width: 4px;");
        TranslateTransition floatingAnimation = new TranslateTransition(Duration.seconds(1.0), startBtn);
        floatingAnimation.setByY(-10.0);
        floatingAnimation.setCycleCount(-1);
        floatingAnimation.setAutoReverse(true);
        floatingAnimation.play();
        BorderPane homeBottomBar = new BorderPane();
        homeBottomBar.setStyle("-fx-padding: 10 20 0 20;");
        Button helpBtn = new Button("?");
        helpBtn.setFocusTraversable(false);
        helpBtn.setStyle("-fx-font-size: 16px; -fx-background-radius: 20; -fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand; -fx-min-width: 35px; -fx-min-height: 35px;");
        Button profileBtn = new Button();
        profileBtn.setFocusTraversable(false);
        profileBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-min-width: 35px; -fx-min-height: 35px; -fx-padding: 0;");
        InputStream imgStream = ((this)).getClass().getResourceAsStream("/profile.png");
        if (imgStream != null) {
            Image avatarImage = new Image(imgStream);
            ImageView largeAvatar = new ImageView(avatarImage);
            largeAvatar.setFitWidth(120.0);
            largeAvatar.setFitHeight(120.0);
            largeAvatar.setPreserveRatio(true);
            profileOverlay.getChildren().add(0, largeAvatar);
            ImageView smallAvatar = new ImageView(avatarImage);
            smallAvatar.setFitWidth(35.0);
            smallAvatar.setFitHeight(35.0);
            smallAvatar.setPreserveRatio(true);
            profileBtn.setGraphic(smallAvatar);
        } else {
            profileBtn.setText("\ud83d\udc64");
            profileBtn.setStyle("-fx-font-size: 16px; -fx-background-radius: 20; -fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand; -fx-min-width: 35px; -fx-min-height: 35px;");
        }
        homeBottomBar.setLeft(helpBtn);
        homeBottomBar.setRight(profileBtn);
        helpBtn.setOnAction(e -> {
            profileOverlay.setVisible(false);
            helpOverlay.setVisible(true);
        });
        profileBtn.setOnAction(e -> {
            helpOverlay.setVisible(false);
            profileOverlay.setVisible(true);
        });
        this.startScreen.getChildren().addAll(new Node[]{titleText, recordsTitle, recordsBox, startBtn, homeBottomBar});
        this.levelUpOverlay = new VBox(20.0);
        this.levelUpOverlay.setAlignment(Pos.CENTER);
        this.levelUpOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        this.levelUpOverlay.setVisible(false);
        Text levelUpText = new Text("LEVEL UP");
        levelUpText.setFont(Font.font(this.pixelFont.getFamily(), 32.0));
        levelUpText.setStyle("-fx-fill: #FF6600; -fx-stroke: #FFFFFF; -fx-stroke-width: 1px;");
        Polygon arrow = new Polygon();
        arrow.getPoints().addAll(new Double[]{0.0, -50.0, 25.0, -25.0, 12.0, -25.0, 12.0, 25.0, -12.0, 25.0, -12.0, -25.0, -25.0, -25.0});
        arrow.setFill(Color.WHITE);
        this.levelUpOverlay.getChildren().addAll(new Node[]{arrow, levelUpText});
        this.scoreboardOverlay = new VBox(20.0);
        this.scoreboardOverlay.setAlignment(Pos.CENTER);
        this.scoreboardOverlay.setStyle("-fx-background-color: #000000; -fx-padding: 30;");
        this.scoreboardOverlay.setVisible(false);
        Text endTitle = new Text("GAME OVER");
        endTitle.setFont(Font.font(this.pixelFont.getFamily(), 24.0));
        endTitle.setStyle("-fx-fill: #E60000;");
        final VBox endScoresBox = new VBox(5.0);
        endScoresBox.setAlignment(Pos.CENTER);
        endScoresBox.setStyle("-fx-border-color: #FFCC00; -fx-border-width: 2px; -fx-padding: 15; -fx-max-width: 320px;");
        HBox endControls = new HBox(20.0);
        endControls.setAlignment(Pos.CENTER);
        Button endRestartBtn = new Button("Restart Game");
        endRestartBtn.setFont(Font.font(this.pixelFont.getFamily(), 12.0));
        endRestartBtn.setStyle("-fx-background-color: #FF6600; -fx-text-fill: white; -fx-cursor: hand;");
        Button endHomeBtn = new Button("Home");
        endHomeBtn.setFont(Font.font(this.pixelFont.getFamily(), 12.0));
        endHomeBtn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-cursor: hand;");
        endControls.getChildren().addAll(new Node[]{endRestartBtn, endHomeBtn});
        this.scoreboardOverlay.getChildren().addAll(new Node[]{endTitle, endScoresBox, endControls});
        this.rootPane.getChildren().addAll(new Node[]{this.startScreen, helpOverlay, profileOverlay, this.levelUpOverlay, this.scoreboardOverlay, this.pauseOverlay});
        Scene scene = new Scene(this.rootPane);
        scene.setOnKeyPressed(this::handleKeyPress);
        primaryStage.setTitle("Eve's Apple");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        this.gameTimer = new AnimationTimer(){

            public void handle(long now) {
                if (!Main.this.gameIsRunning) {
                    return;
                }
                long currentNanosPerTick = 125000000L;
                if (now - Main.this.lastTickTime >= currentNanosPerTick) {
                    Main.this.lastTickTime = now;
                    Main.this.game.tick(Main.this.inputBuffer.poll());
                    Main.this.currentState = Main.this.game.getState();
                    Main.this.renderer.render(Main.this.currentState);
                    if (Main.this.currentState.status == GameState.Status.GAME_OVER || Main.this.currentState.status == GameState.Status.GAME_WON) {
                        Main.this.gameIsRunning = false;
                        int finalScore = Main.this.currentState.score;
                        if (Main.this.isNewRecord(finalScore)) {
                            Main.this.triggerLevelUpAnimation(finalScore, endScoresBox);
                        } else {
                            Main.this.updateHighScores(finalScore);
                            Main.this.showEndScoreboard(endScoresBox);
                        }
                    }
                }
            }
        };
        startBtn.setOnAction(e -> {
            this.startScreen.setVisible(false);
            this.gameIsRunning = true;
            this.restartGame();
            this.gameTimer.start();
        });
        endRestartBtn.setOnAction(e -> {
            this.scoreboardOverlay.setVisible(false);
            this.gameIsRunning = true;
            this.restartGame();
        });
        endHomeBtn.setOnAction(e -> {
            this.scoreboardOverlay.setVisible(false);
            this.populateScoreboard(recordsBox);
            this.startScreen.setVisible(true);
        });
        this.restartGame();
        this.renderer.render(this.currentState);
    }

    private void populateScoreboard(VBox scoresBox) {
        scoresBox.getChildren().clear();
        for (int i = 0; i < this.highScores.size(); ++i) {
            Text scoreText = new Text(String.format(" %2d . . %6d ", i + 1, this.highScores.get(i)));
            scoreText.setFont(Font.font(this.pixelFont.getFamily(), 12.0));
            scoreText.setStyle("-fx-fill: #FFFFFF;");
            scoresBox.getChildren().add(scoreText);
        }
    }

    private void showEndScoreboard(VBox endScoresBox) {
        this.populateScoreboard(endScoresBox);
        this.scoreboardOverlay.setVisible(true);
    }

    private void triggerLevelUpAnimation(int finalScore, VBox endScoresBox) {
        this.levelUpOverlay.setVisible(true);
        Timeline shake = new Timeline(new KeyFrame[]{new KeyFrame(Duration.millis(0.0), new KeyValue[]{new KeyValue(this.rootPane.translateXProperty(), 0)}), new KeyFrame(Duration.millis(50.0), new KeyValue[]{new KeyValue(this.rootPane.translateXProperty(), -12)}), new KeyFrame(Duration.millis(100.0), new KeyValue[]{new KeyValue(this.rootPane.translateXProperty(), 12)}), new KeyFrame(Duration.millis(150.0), new KeyValue[]{new KeyValue(this.rootPane.translateXProperty(), -12)}), new KeyFrame(Duration.millis(200.0), new KeyValue[]{new KeyValue(this.rootPane.translateXProperty(), 12)}), new KeyFrame(Duration.millis(250.0), new KeyValue[]{new KeyValue(this.rootPane.translateXProperty(), 0)})});
        shake.play();
        Node arrow = this.levelUpOverlay.getChildren().get(0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(400.0), arrow);
        scale.setFromX(0.1);
        scale.setFromY(0.1);
        scale.setToX(2.0);
        scale.setToY(2.0);
        scale.play();
        scale.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2.0));
            pause.setOnFinished(pe -> {
                this.levelUpOverlay.setVisible(false);
                this.updateHighScores(finalScore);
                this.showEndScoreboard(endScoresBox);
            });
            pause.play();
        });
    }

    private void restartGame() {
        this.inputBuffer.clear();
        this.game = new Game(20, 20, true);
        this.currentState = this.game.getState();
        this.lastTickTime = System.nanoTime();
    }

    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case SPACE: {
                if (this.gameIsRunning) {
                    this.game.togglePause();
                    this.currentState = this.game.getState();
                    if (this.currentState.status == GameState.Status.PAUSED) {
                        this.pauseOverlay.setVisible(true);
                    } else {
                        this.pauseOverlay.setVisible(false);
                    }
                }
                return;
            }
            case ESCAPE: {
                if (this.gameIsRunning && this.currentState.status == GameState.Status.PAUSED) {
                    this.gameIsRunning = false;
                    this.pauseOverlay.setVisible(false);
                    this.startScreen.setVisible(true);
                }
                return;
            }
        }
        if (this.currentState.status == GameState.Status.PAUSED) {
            return;
        }
        GameState.Direction dir = null;
        switch (event.getCode()) {
            case UP: 
            case W: {
                dir = GameState.Direction.UP;
                break;
            }
            case DOWN: 
            case S: {
                dir = GameState.Direction.DOWN;
                break;
            }
            case LEFT: 
            case A: {
                dir = GameState.Direction.LEFT;
                break;
            }
            case RIGHT: 
            case D: {
                dir = GameState.Direction.RIGHT;
                break;
            }
        }
        if (dir != null && this.inputBuffer.size() < 2) {
            this.inputBuffer.add(dir);
        }
    }

    public static void main(String[] args) {
        Main.launch((String[])args);
    }
}

