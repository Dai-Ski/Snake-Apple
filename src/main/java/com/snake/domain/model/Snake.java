package com.snake.domain.model;

import com.snake.domain.model.GameState;
import java.util.LinkedList;
import java.util.List;

public class Snake {
    private final LinkedList<GameState.Position> segments = new LinkedList<>();
    private GameState.Direction direction;
    private int growPending = 0;

    public Snake(GameState.Position startPos, GameState.Direction startDir, int initialLength) {
        this.direction = startDir;
        for (int i = 0; i < initialLength; ++i) {
            this.segments.add(new GameState.Position(startPos.x - i, startPos.y));
        }
    }

    public GameState.Direction getDirection() {
        return this.direction;
    }

    public void setDirection(GameState.Direction newDir) {
        if (!this.direction.isOpposite(newDir)) {
            this.direction = newDir;
        }
    }

    public GameState.Position getHead() {
        return this.segments.getFirst();
    }

    public List<GameState.Position> getPositions() {
        return this.segments;
    }

    public void move(GameState.Position newHead) {
        this.segments.addFirst(newHead);
        if (this.growPending > 0) {
            --this.growPending;
        } else {
            this.segments.removeLast();
        }
    }

    public void grow(int amount) {
        this.growPending += amount;
    }
}

