package ore.forge.Input3D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.physics.bullet.collision.RayResultCallback;
import ore.forge.Items.Experimental.EntityInstance;
import ore.forge.PhysicsBodyData;

import java.util.ArrayList;
import java.util.List;

/*
 * Selecting Items can transition to the following states:
 * BuildingInputState (On R(Move))
 * DefaultState (On ESC, click on null Item, Z (remove Item), X (sell item) )
 *
 * */
public class SelectingItemsInputState extends InputState {
    private final List<EntityInstance> selectedItems;
    private BuildingInputState buildingState;

    public SelectingItemsInputState(InputHandler inputHandler) {
        super(inputHandler);
        selectedItems = new ArrayList<>();

    }

    @Override
    public void update(float delta) {
        var defaultState = inputHandler.defaultInput();
        cameraController.update(delta);
        //Transition to Building State with selected Items
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            inputHandler.setInputState(buildingState);
            buildingState.setActive(selectedItems);
            cleanUpSelectedItems();
            return;
        }
        //sell selected items
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {


        }
        //Exit build selecting mode
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            inputHandler.setInputState(defaultState);
            cleanUpSelectedItems();
            return;
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            RayResultCallback rayCallback = rayCastForItem();
            if (rayCallback.hasHit() && rayCallback.getCollisionObject() != null) { //If Ray-Cast returns an item add to selectedItem
                var collisionObject = rayCallback.getCollisionObject();
                PhysicsBodyData hitBodyData = (PhysicsBodyData) collisionObject.userData;
                addToSelectedItems(hitBodyData.parentEntityInstance);
            } else { //if Ray-Cast returns no item exit
                inputHandler.setInputState(defaultState);
                cleanUpSelectedItems();
                return;
            }
            rayCallback.dispose();
        }

    }

    public void addToSelectedItems(EntityInstance entityInstance) {
        if (!selectedItems.contains(entityInstance)) {
            selectedItems.add(entityInstance);
            //TODO: add a shader to the item so that its drawn differently/highlighted
//            entityInstance.visualComponent.visualEffects.add();
        }
    }

    public void cleanUpSelectedItems() {
        for (EntityInstance entityInstance : selectedItems) {
            //TODO: Remove visula effects from selectedItems
        }
        selectedItems.clear();
    }

}
