package ore.forge.game.input;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.RayResultCallback;
import ore.forge.game.items.ItemDefinition;
import ore.forge.game.PhysicsBodyData;

import java.util.ArrayList;
import java.util.List;

/*
 * Selecting Items can transition to the following states:
 * BuildingInputState (On R(Move))
 * DefaultState (On ESC, click on null Item, Z (remove Item), X (sell item) )
 *
 * */
public class SelectingItemsInputState extends InputState {
    private final List<Entity> selectedItems;
    private BuildingInputState buildingState;
    private DefaultInputState defaultState;
    private boolean isHeld;

    public SelectingItemsInputState(InputHandler inputHandler) {
        super(inputHandler);
        selectedItems = new ArrayList<>();
        isHeld = false;
    }

    @Override
    public void update(float delta) {
        cameraController.update(delta);
        //Transition to Building State with selected Items
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            List<ItemDefinition> defs = new ArrayList<>(selectedItems.size());
            List<Matrix4> transforms = new ArrayList<>(selectedItems.size());
            for (Entity item : selectedItems) {
                inputHandler.context.entityManager.stageRemove(item);
//                defs.add((ItemDefinition) item.definition);
//                transforms.add(item.rootTransform.currentTransform);
            }
            inputHandler.setInputState(buildingState);
            buildingState.setActiveFromDef(defs, transforms);
            cleanUpSelectedItems();
            return;
        }

        //Sell selected items
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {

        }

        //remove selected items
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            for (Entity item : selectedItems) {
                inputHandler.context.entityManager.stageRemove(item);
            }
            selectedItems.clear();
            inputHandler.setInputState(defaultState);
            return;
        }

        //Exit build selecting mode
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            inputHandler.setInputState(defaultState);
            cleanUpSelectedItems();
            return;
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!isHeld) {
                cleanUpSelectedItems();
            }
            isHeld = true;
            RayResultCallback rayCallback = rayCastForItem();
            if (rayCallback.hasHit() && rayCallback.getCollisionObject() != null) { //If Ray-Cast returns an item add to selectedItem
                var collisionObject = rayCallback.getCollisionObject();
                if (collisionObject.userData instanceof PhysicsBodyData data) {
//                    if (data.parentEntity.definition instanceof ItemDefinition) {
//                        addToSelectedItems(data.parentEntity);
//                    }
                }

//            } else { //if Ray-Cast returns no item exit
//                inputHandler.setInputState(defaultState);
//                cleanUpSelectedItems();
//                rayCallback.dispose();
//                return;

            }
            rayCallback.dispose();
        } else {
            isHeld = false;
        }

    }

    public void addToSelectedItems(Entity Entity) {
        if (!selectedItems.contains(Entity)) {
            selectedItems.add(Entity);
            //TODO: add a shader to the item so that its drawn differently/highlighted
//            Entity.visualComponent.visualEffects.add();
        }
    }

    public void cleanUpSelectedItems() {
        for (Entity Entity : selectedItems) {
            //TODO: Remove visula effects from selectedItems
        }
        selectedItems.clear();
    }

    public void setDefaultState(DefaultInputState defaultState) {
        this.defaultState = defaultState;
    }

    public void setBuildingState(BuildingInputState buildingState) {
        this.buildingState = buildingState;
    }
}
