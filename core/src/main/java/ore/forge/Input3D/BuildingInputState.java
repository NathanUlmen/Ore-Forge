package ore.forge.Input3D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import ore.forge.EntityInstance;

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
    private static final float ROTATION_ANGLE = 90f;
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
        cameraController.update(delta);
        Vector3 mouseWorld = inputHandler.getMouseGroundPosition(cameraController.getCamera());

//        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
        for (int i = 0; i < selectedItems.size(); i++) {
            Vector3 offset = offsets.get(i);
            EntityInstance item = selectedItems.get(i);

            Vector3 newPosition = new Vector3(mouseWorld).add(offset);

            Matrix4 newTransform = new Matrix4(item.transform());
            newTransform.setTranslation(newPosition);

            item.setTransform(newTransform);
        }
//        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            //Rotate all items 90 degrees around center point and rotate around their relative direction too.
            for (int i = 0; i < selectedItems.size(); i++) {
                Vector3 offset = offsets.get(i);
                offset.rotate(Vector3.Y, ROTATION_ANGLE); //rotate relative to center

                //rotate item itself
                EntityInstance item = selectedItems.get(i);
                Matrix4 transform = new Matrix4(item.transform());
                Quaternion rotation = new Quaternion(Vector3.Y, ROTATION_ANGLE);
                transform.rotate(rotation);
                item.setTransform(transform);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {// leave build mode
            inputHandler.setInputState(defaultState);
            this.cleanUp();
        }
    }


    public void setActive(List<EntityInstance> items) {
//        context.entityManager.getEngetFirst().visualComponent.attributes = new GridAttribute(GridAttribute.ID);
//        System.out.println(Gameplay3D.entityInstances.getFirst());
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
//        Gameplay3D.entityInstances.getFirst().visualComponent.attributes = null;
        for (EntityInstance item : selectedItems) {
            //TODO: Remove building Shader
        }
        selectedItems.clear();
        offsets.clear();
    }

    public void setDefaultState(DefaultInputState defaultState) {
        this.defaultState = defaultState;
    }

}
