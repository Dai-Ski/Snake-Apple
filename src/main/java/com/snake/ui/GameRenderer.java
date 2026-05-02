package com.snake.ui;

import com.snake.domain.core.GameConfig;
import com.snake.domain.model.GameState;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

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
        List<GameState.Position> snakePositions;
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        for (int row = 0; row < this.rows; ++row) {
            for (int col = 0; col < this.cols; ++col) {
                if ((row + col) % 2 == 0) {
                    gc.setFill(Color.web(GameConfig.COLOR_BG_DARK));
                } else {
                    gc.setFill(Color.web(GameConfig.COLOR_BG_LIGHT));
                }
                gc.fillRect(col * this.cellSize, row * this.cellSize, this.cellSize, this.cellSize);
            }
        }
        GameState.Position applePos = state.apple;
        if (applePos != null) {
            double ax = applePos.x * this.cellSize;
            double ay = applePos.y * this.cellSize;
            gc.setFill(Color.web(GameConfig.COLOR_APPLE));
            gc.fillOval(ax + 3.0, ay + 6.0, this.cellSize - 6.0, this.cellSize - 8.0);
            gc.setFill(Color.web(GameConfig.COLOR_APPLE_LEAF));
            gc.fillOval(ax + this.cellSize / 2.0, ay + 2.0, 8.0, 6.0);
        }
        if (!(snakePositions = state.snake).isEmpty()) {

            GameState.Position head = snakePositions.get(0);
            double hx = head.x * this.cellSize;
            double hy = head.y * this.cellSize;
            gc.setFill(Color.web(GameConfig.COLOR_SNAKE_HEAD));
            gc.fillRoundRect(hx + 1.0, hy + 1.0, this.cellSize - 2.0, this.cellSize - 2.0, 10.0, 10.0);
            int dx = 1;
            int dy = 0;
            if (snakePositions.size() > 1) {
                GameState.Position body = snakePositions.get(1);
                dx = head.x - body.x;
                dy = head.y - body.y;
                if (dx > 1) {
                    dx = -1;
                } else if (dx < -1) {
                    dx = 1;
                }
                if (dy > 1) {
                    dy = -1;
                } else if (dy < -1) {
                    dy = 1;
                }
            }
            gc.setFill(Color.WHITE);
            double eyeSize = 6.0;
            double offset = 4.0;
            double eye1X = hx;
            double eye1Y = hy;
            double eye2X = hx;
            double eye2Y = hy;
            if (dx == 1) {
                eye1X += this.cellSize - offset - eyeSize;
                eye1Y += offset;
                eye2X += this.cellSize - offset - eyeSize;
                eye2Y += this.cellSize - offset - eyeSize;
            } else if (dx == -1) {
                eye1X += offset;
                eye1Y += offset;
                eye2X += offset;
                eye2Y += this.cellSize - offset - eyeSize;
            } else if (dy == -1) {
                eye1X += offset;
                eye1Y += offset;
                eye2X += this.cellSize - offset - eyeSize;
                eye2Y += offset;
            } else {
                eye1X += offset;
                eye1Y += this.cellSize - offset - eyeSize;
                eye2X += this.cellSize - offset - eyeSize;
                eye2Y += this.cellSize - offset - eyeSize;
            }
            gc.fillOval(eye1X, eye1Y, eyeSize, eyeSize);
            gc.fillOval(eye2X, eye2Y, eyeSize, eyeSize);
            gc.setFill(Color.BLACK);
            double pSize = 3.0;
            double pOffX = dx == 1 ? 3.0 : (dx == -1 ? 0.0 : 1.5);
            double pOffY = dy == 1 ? 3.0 : (dy == -1 ? 0.0 : 1.5);
            gc.fillOval(eye1X + pOffX, eye1Y + pOffY, pSize, pSize);
            gc.fillOval(eye2X + pOffX, eye2Y + pOffY, pSize, pSize);
            gc.setFill(Color.web(GameConfig.COLOR_SNAKE_BODY));
            for (int i = 1; i < snakePositions.size(); ++i) {
                GameState.Position p = snakePositions.get(i);
                gc.fillRoundRect(p.x * this.cellSize + 2.0, p.y * this.cellSize + 2.0, this.cellSize - 4.0, this.cellSize - 4.0, 8.0, 8.0);
            }
        }
        if (state.status == GameState.Status.GAME_OVER) {
            gc.setFill(Color.color(0.0, 0.0, 0.0, 0.5));
            gc.fillRect(0.0, 0.0, this.canvas.getWidth(), this.canvas.getHeight());
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(GameConfig.FONT_NAME, 30.0));
            gc.fillText("GAME OVER", this.canvas.getWidth() / 2.0 - 80.0, this.canvas.getHeight() / 2.0);
            gc.setFont(Font.font(GameConfig.FONT_NAME, 15.0));
            gc.fillText("Score: " + state.score, this.canvas.getWidth() / 2.0 - 40.0, this.canvas.getHeight() / 2.0 + 30.0);
        } else if (state.status == GameState.Status.GAME_WON) {
            gc.setFill(Color.color(0.0, 0.0, 0.0, 0.5));
            gc.fillRect(0.0, 0.0, this.canvas.getWidth(), this.canvas.getHeight());
            gc.setFill(Color.web("#f1c40f"));
            gc.setFont(Font.font(GameConfig.FONT_NAME, 30.0));
            gc.fillText("YOU WON!", this.canvas.getWidth() / 2.0 - 70.0, this.canvas.getHeight() / 2.0);
            gc.setFont(Font.font(GameConfig.FONT_NAME, 15.0));
            gc.setFill(Color.WHITE);
            gc.fillText("Perfect Score: " + state.score, this.canvas.getWidth() / 2.0 - 70.0, this.canvas.getHeight() / 2.0 + 30.0);
        } else if (state.status == GameState.Status.PAUSED) {
            gc.setFill(Color.color(0.0, 0.0, 0.0, 0.5));
            gc.fillRect(0.0, 0.0, this.canvas.getWidth(), this.canvas.getHeight());
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(GameConfig.FONT_NAME, 30.0));
            gc.fillText("PAUSED", this.canvas.getWidth() / 2.0 - 50.0, this.canvas.getHeight() / 2.0);
            gc.setFont(Font.font(GameConfig.FONT_NAME, 15.0));
            gc.fillText("Press Space to Resume", this.canvas.getWidth() / 2.0 - 80.0, this.canvas.getHeight() / 2.0 + 30.0);
        }
    }
}

