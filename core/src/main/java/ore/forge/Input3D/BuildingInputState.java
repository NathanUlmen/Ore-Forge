package ore.forge.Input3D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import ore.forge.Items.Experimental.EntityInstance;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/*
 * Will transition to the following states:
 * Default State (on ESC)
 *
 * */
public class BuildingInputState extends InputState {
    private final List<EntityInstance> selectedItems;
    private final List<Vector3> offsets;
    private final Deque<List<EntityInstance>> undoActions;
    private DefaultInputState defaultState;

    public BuildingInputState(InputHandler inputHandler) {
        super(inputHandler);
        selectedItems = new ArrayList<>();
        offsets = new ArrayList<>();
        undoActions = new ArrayDeque<>();
    }

    //TODO: make sure centerPosition is mouse position
    @Override
    public void update(float delta) {
        Vector3 mouseWorld = cameraController.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) { //Attempt to place selected items
            //TODO place items
            for (int i = 0; i < selectedItems.size(); i++) {
                Vector3 offset = offsets.get(i);
                EntityInstance item = selectedItems.get(i);
                Matrix4 newTransform = new Matrix4();
                newTransform.set(item.transform());

                Vector3 itemPosition = item.transform().getTranslation(new Vector3());
                Vector3 newPosition = offset.add(mouseWorld);
                newTransform.setTranslation(itemPosition);

                item.place(newTransform);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            //Rotate all items 90 degrees around center point and rotate around their relative direction too.
            for (int i = 0; i < selectedItems.size(); i++) {
                //rotate offsets 90 degrees
                Vector3 offset = offsets.get(i);
                float temp = 0;
                temp = offset.x;
                offset.x = offset.y;
                offset.y = temp;

                //rotate item itself 90 degrees.

            }

        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {// leave build mode
            inputHandler.setInputState(defaultState);
            this.cleanUp();
        }
    }


    public void setActive(List<EntityInstance> items) {
        setSelectedItems(items);
        List<Matrix4> transforms = new ArrayList<>(items.size());
        for (EntityInstance item : items) {
            transforms.add(item.transform());
        }
        Vector3 centerPoint = new Vector3();
        for (Matrix4 transform : transforms) {
            Vector3 itemPosition = new Vector3();
            transform.getTranslation(itemPosition);
            centerPoint.add(itemPosition);
        }

        centerPoint.x /= items.size();
        centerPoint.y /= items.size();
        centerPoint.z /= items.size();

        for (Matrix4 transform : transforms) {
            Vector3 itemPosition = transform.getTranslation(new Vector3());
            Vector3 offset = centerPoint.cpy().sub(itemPosition);
            offsets.add(offset);
        }

    }

    private void setSelectedItems(List<EntityInstance> items) {
        selectedItems.addAll(items);
        for (EntityInstance item : items) {
            //TODO apply shader to each entity
        }
    }

    public void cleanUp() {
        for (EntityInstance item : selectedItems) {
            //TODO: Remove building Shader
        }
        selectedItems.clear();
        offsets.clear();
    }

}
