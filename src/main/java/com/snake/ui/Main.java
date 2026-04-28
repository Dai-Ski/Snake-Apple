package com.snake.ui;

import com.snake.domain.core.Game;
import com.snake.domain.model.GameState;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Clean, consolidated UI and application loop entry point.
 */
public class Main extends Application {

    private static final int COLS = 20;
    private static final int ROWS = 20;
    private static final int CELL_SIZE = 30;

    private Game game;
    private GameRenderer renderer;
    private GameState currentState;
    private long lastTickTime = 0;
    private final Queue<GameState.Direction> inputBuffer = new LinkedList<>();

    @Override
    public void start(Stage primaryStage) {
        restartGame();

        Canvas canvas = new Canvas(COLS * CELL_SIZE, ROWS * CELL_SIZE);
        this.renderer = new GameRenderer(canvas, COLS, ROWS);

        Button restartBtn = new Button("Restart Game");
        restartBtn.setFocusTraversable(false); // keep focus on scene for keys
        restartBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand;");
        restartBtn.setOnAction(e -> {
            restartGame();
            renderer.render(currentState);
        });

        VBox root = new VBox(10, canvas, restartBtn);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #111111; -fx-padding: 10;");

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(this::handleKeyPress);

        primaryStage.setTitle("Snake & Apple (Cleaned Up)");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Fixed, slow and steady speed: 8 ticks/sec
                long currentNanosPerTick = (long) (1_000_000_000L / 8.0);

                if (now - lastTickTime >= currentNanosPerTick) {
                    lastTickTime = now;
                    game.tick(inputBuffer.poll());
                    currentState = game.getState();
                    renderer.render(currentState);
                }
            }
        };

        renderer.render(currentState);
        timer.start();
    }

    private void restartGame() {
        inputBuffer.clear();
        game = new Game(COLS, ROWS, true); // true = wrap mode
        currentState = game.getState();
        lastTickTime = System.nanoTime();
    }

    private void handleKeyPress(KeyEvent event) {
        GameState.Direction dir = null;
        switch (event.getCode()) {
            case UP: case W: dir = GameState.Direction.UP; break;
            case DOWN: case S: dir = GameState.Direction.DOWN; break;
            case LEFT: case A: dir = GameState.Direction.LEFT; break;
            case RIGHT: case D: dir = GameState.Direction.RIGHT; break;
            default: return;
        }

        // Limit buffer to 2 pending moves to prevent rapid key-mashing bugs
        if (inputBuffer.size() < 2) {
            inputBuffer.add(dir);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
