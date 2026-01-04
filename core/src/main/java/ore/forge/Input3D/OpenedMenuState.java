package ore.forge.Input3D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import ore.forge.UI.UI;
import ore.forge.UI.UIMenu;

import java.util.EnumSet;

/**
 * Will transition to the following states:
 * Default State (on all Menus closed)
 * Building State (on valid selection from Item Inventory)
 * ------
 * UI should interface with the InputHandler System ONLY through this state
 * In other words behavior of this system should only be influenced by UI in
 * this state
 * Other states will change UI as a side effect.
 * EX:
 * On building Ui.setMode(building);
 *
 */
public class OpenedMenuState extends InputState {
    private EnumSet<UIMenu> menuStates;
    private BuildingInputState buildingInputState;
    private DefaultInputState defaultInputState;
    private boolean isSearching;
    private UI ui;

    public OpenedMenuState(InputHandler inputHandler, UI ui) {
        super(inputHandler);
        menuStates = EnumSet.noneOf(UIMenu.class);
        isSearching = false;
        this.ui = ui;
    }

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            // Manage our state
            updateMenuStates(UIMenu.INVENTORY);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            updateMenuStates(UIMenu.SHOP);
        }

        //if not searching in any of the menus and key pressed move camera
        if (!isSearching) {
            cameraController.update(delta);
        }

        //Close all tabs
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isSearching) {
            for (var menu : menuStates) {
                ui.toggleMenu(menu);
            }
            menuStates.clear();
            inputHandler.setInputState(defaultInputState);
        }

        // at the very end
        if (menuStates.isEmpty()) {
            inputHandler.setInputState(defaultInputState);
        }

    }

    public void setActive(UIMenu menu) {
        assert menuStates.isEmpty();
        System.out.println("Active now!!!");
        updateMenuStates(menu);
    }

    private void updateMenuStates(UIMenu menu) {
        if (menuStates.contains(menu)) {
            menuStates.remove(menu);
        } else {
            menuStates.add(menu);
        }
        ui.toggleMenu(menu);
    }

    public void setBuildingInputState(BuildingInputState buildingInputState) {
        this.buildingInputState = buildingInputState;
    }

    public void setDefaultInputState(DefaultInputState defaultInputState) {
        this.defaultInputState = defaultInputState;
    }

    public void setSearching(boolean isSearching) {
        this.isSearching = isSearching;
    }

}
