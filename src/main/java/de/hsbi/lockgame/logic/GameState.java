package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import java.util.List;

public final class GameState {

    private final Level level;
    private final Snake snake;
    private final List<Pin> pins;
    private final Status status;
    private final Direction pendingDirection;

    public GameState(
        Level level, Snake snake, List<Pin> pins, Status status, Direction pendingDirection) {
        this.level = level;
        this.snake = snake;
        this.pins = pins;
        this.status = status;
        this.pendingDirection = pendingDirection;
    }

    public Level level() {
        return this.level;
    }

    public Snake snake() {
        return this.snake;
    }

    public List<Pin> pins() {
        return this.pins;
    }

    public Status status() {
        return this.status;
    }

    public Direction pendingDirection() {
        return this.pendingDirection;
    }

    public GameState withPendingDirection(Direction newDirection) {
        return new GameState(this.level, this.snake, this.pins, this.status, newDirection);
    }

    public GameState tick() {

        if (!status.isRunning() || pendingDirection == Direction.NONE) {
            return this;
        }

        Position nextPos = snake.nextHead(pendingDirection);

        if (!level.isInside(nextPos)) {
            return new GameState(level, snake, pins, Status.LOST_OUT_OF_BOUNDS, Direction.NONE);
        }

        if (level.cellAt(nextPos) == CellType.WALL) {
            return new GameState(level, snake, pins, status, Direction.NONE);
        }

        boolean selfCollision = snake.body().stream()
            .anyMatch(p -> p.x() == nextPos.x() && p.y() == nextPos.y());

        if (selfCollision) {
            return new GameState(level, snake, pins, Status.LOST_SELF_COLLISION, Direction.NONE);
        }


        if (level.cellAt(nextPos) == CellType.PIN_SLOT) {
            Pin targetPin = pins.stream()
                .filter(p -> p.position().x() == nextPos.x() && p.position().y() == nextPos.y())
                .findFirst()
                .orElse(null);
            if (targetPin != null) {
                if (!targetPin.state().isSet() && targetPin.activationDirection() == pendingDirection) {
                    List<Pin> newPins = pins.stream()
                        .map(p -> p == targetPin ? p.withState(Pin.State.HIGH) : p)
                        .toList();
                    boolean allHigh = newPins.stream().allMatch(p -> p.state().isSet());
                    Status newStatus = allHigh ? Status.WON : status;
                    return new GameState(level, snake, newPins, newStatus, Direction.NONE);
                } else {
                    return new GameState(level, snake, pins, status, Direction.NONE);
                }
            }
        }

        Snake nextSnake = snake.grow(pendingDirection);
        return new GameState(level, nextSnake, pins, status, pendingDirection);
    }

  public enum Status {
    RUNNING,
    WON,
    LOST_SELF_COLLISION,
    LOST_OUT_OF_BOUNDS;

    public boolean isRunning() {
      return this == RUNNING;
    }
  }
}
