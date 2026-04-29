package com.snake.domain.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Clean, consolidated domain logic for the Snake.
 */
public class Snake {
    private final LinkedList<GameState.Position> segments;
    private GameState.Direction direction;
    private int growPending = 0;

    public Snake(GameState.Position startPos, GameState.Direction startDir, int initialLength) {
        this.segments = new LinkedList<>();
        this.direction = startDir;
        for (int i = 0; i < initialLength; i++) {
            segments.add(new GameState.Position(startPos.x - i, startPos.y));
        }
    }

    public GameState.Direction getDirection() {
        return direction;
    }

    public void setDirection(GameState.Direction newDir) {
        if (!direction.isOpposite(newDir)) {
            this.direction = newDir;
        }
    }

    public GameState.Position getHead() {
        return segments.getFirst();
    }

    public List<GameState.Position> getPositions() {
        return segments;
    }

    public void move(GameState.Position newHead) {
        segments.addFirst(newHead);
        if (growPending > 0) {
            growPending--;
        } else {
            segments.removeLast();
        }
    }

    public void grow(int amount) {
        growPending += amount;
    }
}
