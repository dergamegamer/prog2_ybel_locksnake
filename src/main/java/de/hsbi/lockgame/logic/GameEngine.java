package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.Direction;
import de.hsbi.lockgame.model.Level;
import de.hsbi.lockgame.model.Snake;
import de.hsbi.lockgame.ui.GamePanel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class GameEngine {

    private GameState state;
    private final List<Consumer<GameState>> observers = new ArrayList<>();

    public GameEngine(Level level) {
        Snake initialSnake = new Snake(List.of(level.snakeStart()));
        this.state = new GameState(
            level,
            initialSnake,
            level.pins(),
            GameState.Status.RUNNING,
            Direction.NONE
        );
    }

    public GameState state() {
        return this.state;
    }

    public void setGamePanel(GamePanel panel) {
        this.observers.add(panel::update);
    }

    private void notifyObservers() {
        this.observers.forEach(observer -> observer.accept(this.state));
    }

    public void update(Direction d) {
        this.state = this.state.withPendingDirection(d);
        notifyObservers();
    }

    public void tick() {
        this.state = this.state.tick();
        notifyObservers();
    }
}
