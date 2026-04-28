package com.snake.domain.core;

import com.snake.domain.model.GameState;
import com.snake.domain.model.Snake;

import java.util.List;
import java.util.Random;

/**
 * Clean, consolidated Game engine logic.
 */
public class Game {
    private final int width;
    private final int height;
    private final boolean wrapMode;
    private final Snake snake;
    private final Random random = new Random();

    private GameState.Position apple;
    private int score = 0;
    private GameState.Status status = GameState.Status.RUNNING;

    public Game(int width, int height, boolean wrapMode) {
        this.width = width;
        this.height = height;
        this.wrapMode = wrapMode;
        this.snake = new Snake(new GameState.Position(width / 2, height / 2), GameState.Direction.RIGHT, 3);
        spawnApple();
    }

    public void tick(GameState.Direction nextDir) {
        if (status != GameState.Status.RUNNING) return;

        if (nextDir != null) {
            snake.setDirection(nextDir);
        }

        GameState.Position head = snake.getHead();
        GameState.Direction dir = snake.getDirection();
        int nx = head.x + dir.dx;
        int ny = head.y + dir.dy;

        // 1. Boundary / Wall Check
        if (wrapMode) {
            if (nx < 0) nx = width - 1; else if (nx >= width) nx = 0;
            if (ny < 0) ny = height - 1; else if (ny >= height) ny = 0;
        } else if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
            status = GameState.Status.GAME_OVER;
            return;
        }

        GameState.Position nextHead = new GameState.Position(nx, ny);
        snake.move(nextHead);

        // 2. Self Collision Check
        List<GameState.Position> body = snake.getPositions();
        for (int i = 1; i < body.size(); i++) {
            if (nextHead.equals(body.get(i))) {
                status = GameState.Status.GAME_OVER;
                return;
            }
        }

        // 3. Apple Eating Check
        if (apple != null && nextHead.equals(apple)) {
            snake.grow(1);
            score += 10;
            spawnApple();
        }
    }

    private void spawnApple() {
        if (snake.getPositions().size() >= width * height) {
            status = GameState.Status.GAME_WON;
            apple = null;
            return;
        }

        GameState.Position newPos;
        do {
            newPos = new GameState.Position(random.nextInt(width), random.nextInt(height));
        } while (snake.getPositions().contains(newPos));

        apple = newPos;
    }

    public GameState getState() {
        return new GameState(snake.getPositions(), apple, score, status);
    }

    public void togglePause() {
        if (status == GameState.Status.RUNNING) {
            status = GameState.Status.PAUSED;
        } else if (status == GameState.Status.PAUSED) {
            status = GameState.Status.RUNNING;
        }
    }
}
