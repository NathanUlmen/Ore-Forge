package ore.forge.Input;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import ore.forge.EventSystem.EventManager;
import ore.forge.EventSystem.Events.ItemIconClickedGameEvent;
import ore.forge.EventSystem.GameEventListener;
import ore.forge.ItemMap;
import ore.forge.Items.Item;
import ore.forge.Player.Inventory;
import ore.forge.Screens.InventoryTable;
import ore.forge.Screens.ShopMenu;
import ore.forge.Screens.UserInterface;
import ore.forge.Screens.Widgets.ItemIcon;

import java.util.ArrayList;
import java.util.Deque;

import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.Input.Keys.*;

public class InputManager implements GameEventListener {
    public final InputMultiplexer multiplexer;
    private final OrthographicCamera camera;
    private final Game game;
    private final UserInterface ui;
    private final static ItemMap ITEM_MAP = ItemMap.getSingleton();
    private final DefaultState defaultState;
    private final CameraController controller;
    private final Inventory inventory;
    private final Building building;
    private final Selecting selecting;

    private Deque<InputAdapter> adapterStack;

    /*
     * Default Mode
     * Selecting
     * Building
     * Observing Ore
     *
     * */
    public InputManager(OrthographicCamera camera, Game game, UserInterface ui, Inventory inventory) {
        this.inventory = inventory;
        controller = new CameraController();
        this.ui = ui;
        this.camera = camera;
        this.game = game;
        multiplexer = new InputMultiplexer();
        defaultState = new DefaultState(this);
        building = new Building(this);
        selecting = new Selecting(this);

        ui.getInventoryTable().setInputManager(this);
        multiplexer.addProcessor(ui.stage);
        multiplexer.addProcessor(selecting);
        multiplexer.addProcessor(defaultState);

        input.setInputProcessor(multiplexer);
        EventManager.getSingleton().registerListener(this);
    }

    public void update() {
        defaultState.update(camera);
        selecting.update();
        if (multiplexer.getProcessors().first() instanceof Building mode) {
            mode.update();
        } else if (multiplexer.getProcessors().first() instanceof Selecting mode) {
            mode.update();
        }
    }

    public static boolean handleKey(int keycode, boolean isPressed, CameraController controller) {
        return switch (keycode) {
            case Q -> {
                controller.setZoomIn(isPressed);
                yield true;
            }
            case E -> {
                controller.setZoomOut(isPressed);
                yield true;
            }
            case W -> {
                controller.setMoveUp(isPressed);
                yield true;
            }
            case S -> {
                controller.setMoveDown(isPressed);
                yield true;
            }
            case A -> {
                controller.setMoveLeft(isPressed);
                yield true;
            }
            case D -> {
                controller.setMoveRight(isPressed);
                yield true;
            }
            default -> false;
        };
    }

    public Building getBuilding() {
        return building;
    }

    public Selecting getSelecting() {
        return selecting;
    }


    public Camera getCamera() {
        return camera;
    }

    public void setDefaultState(GameplayController mode) {

    }

    public void updateProcessor() {
        input.setInputProcessor(multiplexer);
    }

    public CameraController getController() {
        return controller;
    }

    public InputMultiplexer getMultiplexer() {
        return multiplexer;
    }

    public InventoryTable getInventoryTable() {
        return ui.getInventoryTable();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Vector3 getMouseWorld() {
        return camera.unproject(new Vector3(input.getX(), input.getY(), 0));
    }

    public ShopMenu getShop() {
        return ui.getShopUI();
    }

    public Game getGame() {
        return game;
    }

    @Override
    public void handle(Object event) {
        ui.getShopUI().hide();
        ui.getInventoryTable().hide();
        ui.getInventoryTable().stopSearching();
        ArrayList<Item> items = new ArrayList<Item>(1);
        items.add(((ItemIcon) ((ItemIconClickedGameEvent) event).getSubject()).getNode().createNewHeldItem());
        ArrayList<Vector2> offsets = new ArrayList<>(1);
        offsets.add(new Vector2());
        building.startBuilding(items, offsets);
        multiplexer.addProcessor(0, building);
    }

    @Override
    public Class<?> getEventType() {
        return ItemIconClickedGameEvent.class;
    }
}
