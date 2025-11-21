package ore.forge.Input3D;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import ore.forge.Items.Experimental.EntityInstance;

import java.util.ArrayList;
import java.util.List;

/*
* Will transition to the following states:
* Default State (on ESC)
*
* */
public class BuildingInputState extends InputState {
    private final List<EntityInstance> selectedItems;
    private final List<Vector3> offsets;

    public BuildingInputState(InputHandler inputHandler) {
        super(inputHandler);
        selectedItems = new ArrayList<>();
        offsets = new ArrayList<>();

    }

    @Override
    public void update(float delta) {



    }

    //TODO: Pass all the transforms, offsets, and other stuff
    public void onTransition() {

    }

    public void setActive(List<EntityInstance> items) {
        setSelectedItems(items);
        List<Matrix4> transforms = new ArrayList<>(items.size());
        for (EntityInstance item : items) {
            transforms.add(item.transform());
        }
        Vector3 centerPoint = new  Vector3();
        for (Matrix4 transform : transforms) {
            Vector3 itemPosition = new Vector3();
            transform.getTranslation(itemPosition);
            centerPoint.add(itemPosition);
        }
        centerPoint.x /=  items.size();
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
        for  (EntityInstance item : selectedItems) {
            //TODO: Remove building Shader
        }
        selectedItems.clear();
        offsets.clear();
    }

}
