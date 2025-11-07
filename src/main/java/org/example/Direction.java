package org.example;

import java.util.Optional;

public enum Direction {
    North, East, South, West;

    public static Optional<Direction> fromString(String text) {
        Direction direction;
        switch (text.toLowerCase()) {
            case "north":
                direction = North;
                break;
            case "east":
                direction = East;
                break;
            case "south":
                direction = South;
                break;
            case "west":
                direction = West;
                break;
            default:
                return Optional.empty();
        }
        return Optional.of(direction);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
