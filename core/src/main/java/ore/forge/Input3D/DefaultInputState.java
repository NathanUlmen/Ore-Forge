package ore.forge.Input3D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import ore.forge.Items.Experimental.EntityInstance;
import ore.forge.PhysicsBodyData;

public class DefaultInputState extends InputState {
    /*
     * From Default state we can go to the following States:
     * Opened Inventory State(I(inventory, F(Shop), ESC(Pause)) - Will open Userinterface
     * Selecting Items (On left click on Item)
     * Observing Ore (On UN-KNOWN SYMBOL)
     *
     * */
    private OpenedInventoryState openedInventoryState;
    private final SelectingItemsInputState selecting;


    public DefaultInputState(InputHandler inputHandler) {
        super (inputHandler);
        selecting =  new SelectingItemsInputState(inputHandler);

    }


    @Override
    public void update(float delta) {
        cameraController.update(delta);

        var camera = cameraController.getCamera();

        //Shoot ray to see if it collides with an object
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            var rayCallback = rayCastForItem();


            if (rayCallback.hasHit()) {
                btCollisionObject hitObject = rayCallback.getCollisionObject();
                PhysicsBodyData hitBodyData = (PhysicsBodyData) hitObject.userData;
                if (hitBodyData != null) {
                    EntityInstance entityInstance = hitBodyData.parentEntityInstance;
                    inputHandler.setInputState(selecting);
                    selecting.addToSelectedItems(entityInstance);
                }
            }

            rayCallback.dispose();
        }

    }

}
