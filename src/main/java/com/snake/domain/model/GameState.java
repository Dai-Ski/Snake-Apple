package com.snake.domain.model;

import java.util.List;

/**
 * Immutable snapshot of the game state, which also holds all the basic
 * data structures (Position, Direction, Status) to keep the codebase clean and consolidated.
 */
public class GameState {
    public final List<Position> snake;
    public final Position apple;
    public final int score;
    public final Status status;

    public GameState(List<Position> snake, Position apple, int score, Status status) {
        this.snake = snake;
        this.apple = apple;
        this.score = score;
        this.status = status;
    }

    public enum Status {
        RUNNING, PAUSED, GAME_OVER, GAME_WON
    }

    public enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

        public final int dx, dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public boolean isOpposite(Direction o) {
            return this.dx + o.dx == 0 && this.dy + o.dy == 0;
        }
    }

    public static class Position {
        public final int x, y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Position)) return false;
            Position p = (Position) o;
            return x == p.x && y == p.y;
        }
    }
}
