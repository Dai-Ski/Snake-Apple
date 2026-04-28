package com.snake.ui;

import com.snake.domain.model.GameState;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Handles rendering of the GameState onto a JavaFX Canvas.
 */
public class GameRenderer {
    private final Canvas canvas;
    private final int cols;
    private final int rows;
    private final double cellSize;

    public GameRenderer(Canvas canvas, int cols, int rows) {
        this.canvas = canvas;
        this.cols = cols;
        this.rows = rows;
        this.cellSize = canvas.getWidth() / cols;
    }

    public void render(GameState state) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw checkered background
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if ((row + col) % 2 == 0) {
                    gc.setFill(Color.web("#1e1e1e"));
                } else {
                    gc.setFill(Color.web("#2c2c2c"));
                }
                gc.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
            }
        }

        // Draw apple
        GameState.Position applePos = state.apple;
        if (applePos != null) {
            double ax = applePos.x * cellSize;
            double ay = applePos.y * cellSize;

            // Apple body
            gc.setFill(Color.web("#ff4757")); // vibrant red
            gc.fillOval(ax + 3, ay + 6, cellSize - 6, cellSize - 8);

            // Apple leaf
            gc.setFill(Color.web("#2ed573")); // green leaf
            gc.fillOval(ax + cellSize / 2, ay + 2, 8, 6);
        }

        // Draw snake
        List<GameState.Position> snakePositions = state.snake;
        if (!snakePositions.isEmpty()) {
            // Draw head
            GameState.Position head = snakePositions.get(0);
            double hx = head.x * cellSize;
            double hy = head.y * cellSize;

            gc.setFill(Color.web("#2ed573")); // brighter green for head
            gc.fillRoundRect(hx + 1, hy + 1, cellSize - 2, cellSize - 2, 10, 10);

            // Infer direction for eyes
            int dx = 1, dy = 0; // Default RIGHT
            if (snakePositions.size() > 1) {
                GameState.Position body = snakePositions.get(1);
                dx = head.x - body.x;
                dy = head.y - body.y;

                // Handle wrap-around
                if (dx > 1) dx = -1;
                else if (dx < -1) dx = 1;
                if (dy > 1) dy = -1;
                else if (dy < -1) dy = 1;
            }

            // Draw eyes
            gc.setFill(Color.WHITE);
            double eyeSize = 6;
            double offset = 4;
            double eye1X = hx, eye1Y = hy, eye2X = hx, eye2Y = hy;

            if (dx == 1) { // RIGHT
                eye1X += cellSize - offset - eyeSize; eye1Y += offset;
                eye2X += cellSize - offset - eyeSize; eye2Y += cellSize - offset - eyeSize;
            } else if (dx == -1) { // LEFT
                eye1X += offset; eye1Y += offset;
                eye2X += offset; eye2Y += cellSize - offset - eyeSize;
            } else if (dy == -1) { // UP
                eye1X += offset; eye1Y += offset;
                eye2X += cellSize - offset - eyeSize; eye2Y += offset;
            } else { // DOWN
                eye1X += offset; eye1Y += cellSize - offset - eyeSize;
                eye2X += cellSize - offset - eyeSize; eye2Y += cellSize - offset - eyeSize;
            }

            gc.fillOval(eye1X, eye1Y, eyeSize, eyeSize);
            gc.fillOval(eye2X, eye2Y, eyeSize, eyeSize);

            // Draw pupils
            gc.setFill(Color.BLACK);
            double pSize = 3;
            double pOffX = dx == 1 ? 3 : dx == -1 ? 0 : 1.5;
            double pOffY = dy == 1 ? 3 : dy == -1 ? 0 : 1.5;

            gc.fillOval(eye1X + pOffX, eye1Y + pOffY, pSize, pSize);
            gc.fillOval(eye2X + pOffX, eye2Y + pOffY, pSize, pSize);

            // Draw body
            gc.setFill(Color.web("#7bed9f")); // slightly lighter green for body
            for (int i = 1; i < snakePositions.size(); i++) {
                GameState.Position p = snakePositions.get(i);
                gc.fillRoundRect(p.x * cellSize + 2, p.y * cellSize + 2, cellSize - 4, cellSize - 4, 8, 8);
            }
        }

        // Draw Game Over / Won overlay
        if (state.status == GameState.Status.GAME_OVER) {
            gc.setFill(Color.color(0, 0, 0, 0.5));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Monospaced", 30));
            gc.fillText("GAME OVER", canvas.getWidth() / 2 - 80, canvas.getHeight() / 2);
            gc.setFont(javafx.scene.text.Font.font("Monospaced", 15));
            gc.fillText("Score: " + state.score, canvas.getWidth() / 2 - 40, canvas.getHeight() / 2 + 30);
        } else if (state.status == GameState.Status.GAME_WON) {
            gc.setFill(Color.color(0, 0, 0, 0.5));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.web("#f1c40f")); // Gold color
            gc.setFont(javafx.scene.text.Font.font("Monospaced", 30));
            gc.fillText("YOU WON!", canvas.getWidth() / 2 - 70, canvas.getHeight() / 2);
            gc.setFont(javafx.scene.text.Font.font("Monospaced", 15));
            gc.setFill(Color.WHITE);
            gc.fillText("Perfect Score: " + state.score, canvas.getWidth() / 2 - 70, canvas.getHeight() / 2 + 30);
        }
    }
}
