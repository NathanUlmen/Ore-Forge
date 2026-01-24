package ore.forge.Input3D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import ore.forge.EntityInstance;
import ore.forge.EntityInstanceCreator;
import ore.forge.GameContext;
import ore.forge.Items.ItemDefinition;
import ore.forge.Player.ItemInventory;
import ore.forge.Player.ItemInventoryNode;
import ore.forge.VisualComponent;

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
    private final List<EntityInstance> previewEntities;
    private final List<ItemDefinition> definitions;
    private final List<Vector3> offsets;
    private final Deque<List<EntityInstance>> undoActions;
    private DefaultInputState defaultState;
    private final GameContext context;

    public BuildingInputState(InputHandler inputHandler) {
        super(inputHandler);
        definitions = new ArrayList<>();
        previewEntities = new ArrayList<>();
        offsets = new ArrayList<>();
        undoActions = new ArrayDeque<>();
        context = inputHandler.context;
    }

    @Override
    public void update(float delta) {
        cameraController.update(delta);
        Vector3 mouseWorld = inputHandler.getMouseGroundPosition(cameraController.getCamera());

        for (int i = 0; i < previewEntities.size(); i++) {
            Vector3 offset = offsets.get(i);
            EntityInstance item = previewEntities.get(i);

            Vector3 newPosition = new Vector3(mouseWorld).add(offset);

            Matrix4 newTransform = new Matrix4(item.transform());
            newTransform.setTranslation(newPosition);

            item.setTransform(newTransform);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            //Rotate all items 90 degrees around center point and rotate around their relative direction too.
            for (int i = 0; i < previewEntities.size(); i++) {
                Vector3 offset = offsets.get(i);
                offset.rotate(Vector3.Y, ROTATION_ANGLE); //rotate relative to center

                //rotate item itself
                EntityInstance item = previewEntities.get(i);
                Matrix4 transform = new Matrix4(item.transform());
                Quaternion rotation = new Quaternion(Vector3.Y, ROTATION_ANGLE);
                transform.rotate(rotation);
                item.setTransform(transform);
            }
        }

        System.out.println(previewEntities.size());
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            //place the items
            placeItems();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {// leave build mode
            inputHandler.setInputState(defaultState);
            this.cleanUp();
        }
    }

    public void returnToInventory() {
        for (EntityInstance item : previewEntities) {
            ItemDefinition definition = (ItemDefinition) item.getDefinition();
            ItemInventory inventory = context.player.inventory;
            inventory.getNode(definition.id()).giveBack();
            context.entityManager.removePreviewEntity(item);
        }
        //Stage exit of build mode?
        this.cleanUp();
        inputHandler.setInputState(defaultState);
    }

    public void placeItems() {
        //1. Place all currently held items
        for (EntityInstance item : previewEntities) {
            context.entityManager.stageAdd(item);
            context.entityManager.removePreviewEntity(item);
        }

        previewEntities.clear();
        //2. Replenish held items
        for (ItemDefinition definition : definitions) {
            ItemInventoryNode node = context.player.inventory.getNode(definition.id());
            if (!node.takeFrom()) {
//                We don't have space for item so we must return all held items to inventory then exit build mode.
                returnToInventory();
                return;
            }
            EntityInstance instance = EntityInstanceCreator.createInstance(definition);
            previewEntities.add(instance);
            context.entityManager.addPreviewEntity(instance);
        }
    }

    public void setActive(ItemDefinition item) {
        assert item != null;
        EntityInstance instance;
//      VisualComponent component = new VisualComponent(new ModelInstance(item.model()));
//      instance = new EntityInstance(null, null, component);
        instance = EntityInstanceCreator.createInstance(item);
        instance.setTransform(new Matrix4().setToTranslation(0, 0, 0));
        List<EntityInstance> single = new ArrayList<>(1);
        single.add(instance);
        setActive(single);
    }

    public void setActiveFromDef(List<ItemDefinition> defs, List<Matrix4> transforms) {
        assert defs != null;
        var tempList = new  ArrayList<EntityInstance>(defs.size());
        for (int i = 0; i < defs.size(); i++) {
            tempList.add(EntityInstanceCreator.createInstance(defs.get(i)));
            tempList.get(i).setTransform(transforms.get(i));

        }
        setActive(tempList);
    }

    public void setActive(List<EntityInstance> items) {
        assert items != null && !items.isEmpty();
        for (EntityInstance item : items) {
            context.entityManager.addPreviewEntity(item);
        }

        for (EntityInstance instance : items) {
            definitions.add((ItemDefinition) instance.getDefinition());
        }

        setPreviewEntities(items);
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
            Vector3 offset = itemPosition.cpy().sub(centerPoint);
            offsets.add(offset);
        }
    }

    private void setPreviewEntities(List<EntityInstance> items) {
        previewEntities.addAll(items);

        for (EntityInstance item : items) {
            //TODO apply shader to each entity
        }
    }

    public void cleanUp() {
//        Gameplay3D.entityInstances.getFirst().visualComponent.attributes = null;
        for (EntityInstance item : previewEntities) {
            //TODO: Remove building Shader
            context.entityManager.removePreviewEntity(item);
        }
        previewEntities.clear();
        definitions.clear();
        offsets.clear();
    }

    public void setDefaultState(DefaultInputState defaultState) {
        this.defaultState = defaultState;
    }

}
