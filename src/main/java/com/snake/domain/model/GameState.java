package com.snake.domain.model;

import java.util.List;

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

    public static class Position {
        public final int x;
        public final int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Position)) {
                return false;
            }
            Position p = (Position)o;
            return this.x == p.x && this.y == p.y;
        }
    }

    public static enum Status {
        RUNNING,
        PAUSED,
        GAME_OVER,
        GAME_WON;

    }

    public static enum Direction {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        public final int dx;
        public final int dy;

        private Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public boolean isOpposite(Direction o) {
            return this.dx + o.dx == 0 && this.dy + o.dy == 0;
        }
    }
}

