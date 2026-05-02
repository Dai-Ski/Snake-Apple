package com.snake.domain.core;

import com.snake.domain.model.GameState;
import com.snake.domain.model.Snake;
import java.util.List;
import java.util.Random;

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
        this.spawnApple();
    }

    public void tick(GameState.Direction nextDir) {
        if (this.status != GameState.Status.RUNNING) {
            return;
        }
        if (nextDir != null) {
            this.snake.setDirection(nextDir);
        }
        GameState.Position head = this.snake.getHead();
        GameState.Direction dir = this.snake.getDirection();
        int nx = head.x + dir.dx;
        int ny = head.y + dir.dy;
        if (this.wrapMode) {
            if (nx < 0) {
                nx = this.width - 1;
            } else if (nx >= this.width) {
                nx = 0;
            }
            if (ny < 0) {
                ny = this.height - 1;
            } else if (ny >= this.height) {
                ny = 0;
            }
        } else if (nx < 0 || nx >= this.width || ny < 0 || ny >= this.height) {
            this.status = GameState.Status.GAME_OVER;
            return;
        }
        GameState.Position nextHead = new GameState.Position(nx, ny);
        this.snake.move(nextHead);
        List<GameState.Position> body = this.snake.getPositions();
        for (int i = 1; i < body.size(); ++i) {
            if (!nextHead.equals(body.get(i))) continue;
            this.status = GameState.Status.GAME_OVER;
            return;
        }
        if (this.apple != null && nextHead.equals(this.apple)) {
            this.snake.grow(1);
            this.score += 10;
            this.spawnApple();
        }
    }

    private void spawnApple() {
        GameState.Position newPos;
        if (this.snake.getPositions().size() >= this.width * this.height) {
            this.status = GameState.Status.GAME_WON;
            this.apple = null;
            return;
        }
        do {
            newPos = new GameState.Position(this.random.nextInt(this.width), this.random.nextInt(this.height));
        } while (this.snake.getPositions().contains(newPos));
        this.apple = newPos;
    }

    public GameState getState() {
        return new GameState(this.snake.getPositions(), this.apple, this.score, this.status);
    }

    public void togglePause() {
        if (this.status == GameState.Status.RUNNING) {
            this.status = GameState.Status.PAUSED;
        } else if (this.status == GameState.Status.PAUSED) {
            this.status = GameState.Status.RUNNING;
        }
    }
}

