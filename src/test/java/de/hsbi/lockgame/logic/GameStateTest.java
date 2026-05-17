package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    private Level testLevel;
    private Snake testSnake;
    private Pin testPin;

    @BeforeEach
    void setUp() {
        // 3x3 Test-Level:
        // . . .
        // . S .
        // # . P
        CellType[][] cells = new CellType[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                cells[x][y] = CellType.EMPTY;
            }
        }
        cells[0][2] = CellType.WALL;
        cells[2][2] = CellType.PIN_SLOT;

        Position startPos = new Position(1, 1);
        testSnake = new Snake(List.of(startPos));

        testPin = new Pin(new Position(2, 2), Pin.State.LOW, Direction.RIGHT);

        testLevel = new Level(3, 3, cells, List.of(testPin), startPos);
    }

    // Test 1
    @Test
    void testInitialStateIsRunning() {

        GameState state = new GameState(testLevel, testSnake, testLevel.pins(), GameState.Status.RUNNING, Direction.NONE);

        GameState nextState = state.tick();

        assertEquals(GameState.Status.RUNNING, nextState.status(), "Spiel sollte laufen.");
        assertEquals(1, nextState.snake().head().x(), "X-Position darf sich ohne Richtung nicht ändern.");
        assertEquals(1, nextState.snake().head().y(), "Y-Position darf sich ohne Richtung nicht ändern.");
    }

    // Test 2
    @Test
    void testMoveIntoEmptySpace() {

        GameState state = new GameState(testLevel, testSnake, testLevel.pins(), GameState.Status.RUNNING, Direction.UP);

        GameState nextState = state.tick();

        assertEquals(1, nextState.snake().head().x(), "Kopf X sollte 1 sein.");
        assertEquals(0, nextState.snake().head().y(), "Kopf Y sollte 0 sein.");
        assertEquals(2, nextState.snake().body().size(), "Schlange sollte gewachsen sein.");
    }

    // Test 3
    @Test
    void testMoveIntoWallIsBlocked() {

        Snake snakeNearWall = new Snake(List.of(new Position(0, 1)));
        GameState state = new GameState(testLevel, snakeNearWall, testLevel.pins(), GameState.Status.RUNNING, Direction.DOWN);

        GameState nextState = state.tick();

        assertEquals(1, nextState.snake().body().size(), "Schlange darf nicht wachsen.");
        assertEquals(Direction.NONE, nextState.pendingDirection(), "Richtung muss NONE sein.");
        assertEquals(0, nextState.snake().head().x());
        assertEquals(1, nextState.snake().head().y());
    }

    // Test 4
    @Test
    void testMoveOutOfBoundsLosesGame() {

        Snake snakeAtEdge = new Snake(List.of(new Position(0, 1)));
        GameState state = new GameState(testLevel, snakeAtEdge, testLevel.pins(), GameState.Status.RUNNING, Direction.LEFT);

        GameState nextState = state.tick();

        assertEquals(GameState.Status.LOST_OUT_OF_BOUNDS, nextState.status());
    }

    // Test 5
    @Test
    void testMoveIntoPinWrongDirectionIsBlocked() {

        Snake snakeAbovePin = new Snake(List.of(new Position(2, 1)));
        GameState state = new GameState(testLevel, snakeAbovePin, testLevel.pins(), GameState.Status.RUNNING, Direction.DOWN);

        GameState nextState = state.tick();

        assertFalse(nextState.pins().getFirst().state().isSet(), "Pin darf nicht aktiviert werden.");
        assertEquals(Direction.NONE, nextState.pendingDirection(), "Schlange muss blockiert werden.");
    }

    // Test 6
    @Test
    void testMoveIntoHighPinIsBlocked() {

        Pin highPin = new Pin(new Position(2, 2), Pin.State.HIGH, Direction.RIGHT);
        GameState state = new GameState(testLevel, new Snake(List.of(new Position(1, 2))), List.of(highPin), GameState.Status.RUNNING, Direction.RIGHT);

        GameState nextState = state.tick();

        assertEquals(Direction.NONE, nextState.pendingDirection(), "Gesetzter Pin muss wie Wand blockieren.");
        assertEquals(1, nextState.snake().head().x(), "Schlange darf Feld nicht betreten.");
    }

    // Test 7
    @Test
    void testActivatePinChangesState() {

        Snake snakeBeforePin = new Snake(List.of(new Position(1, 2)));
        GameState state = new GameState(testLevel, snakeBeforePin, testLevel.pins(), GameState.Status.RUNNING, Direction.RIGHT);

        GameState nextState = state.tick();

        assertTrue(nextState.pins().getFirst().state().isSet(), "Pin muss auf HIGH gesetzt werden.");
        assertEquals(Direction.NONE, nextState.pendingDirection(), "Schlange verliert nach Stoß den Schwung.");
        assertEquals(1, nextState.snake().head().x(), "Schlange darf das Feld mit dem Pin nicht betreten.");
    }

    // Test 8
    @Test
    void testActivateLastPinWinsGame() {

        Snake snakeBeforePin = new Snake(List.of(new Position(1, 2)));
        GameState state = new GameState(testLevel, snakeBeforePin, testLevel.pins(), GameState.Status.RUNNING, Direction.RIGHT);

        GameState nextState = state.tick();

        assertEquals(GameState.Status.WON, nextState.status(), "Spiel muss gewonnen sein, wenn der letzte Pin HIGH ist.");
    }

    // Test 9
    @Test
    void testSelfCollisionLosesGame() {

        Snake loopingSnake = new Snake(List.of(
            new Position(1, 1),
            new Position(1, 0),
            new Position(0, 0),
            new Position(0, 1)
        ));
        GameState state = new GameState(testLevel, loopingSnake, testLevel.pins(), GameState.Status.RUNNING, Direction.LEFT);

        GameState nextState = state.tick();

        assertEquals(GameState.Status.LOST_SELF_COLLISION, nextState.status(), "Bei Selbstkollision muss das Spiel verloren sein.");
    }

    // Test 10
    @Test
    void testNoMovementWhenGameIsOver() {

        GameState state = new GameState(testLevel, testSnake, testLevel.pins(), GameState.Status.WON, Direction.UP);

        GameState nextState = state.tick();

        assertEquals(GameState.Status.WON, nextState.status(), "Status darf sich nicht mehr ändern.");
        assertEquals(1, nextState.snake().head().y(), "Schlange darf sich nicht mehr bewegen.");
    }
}
