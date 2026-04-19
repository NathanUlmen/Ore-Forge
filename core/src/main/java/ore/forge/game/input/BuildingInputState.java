package ore.forge.game.input;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import ore.forge.game.EntityCreator;
import ore.forge.game.GameContext;
import ore.forge.game.items.ItemDefinition;
import ore.forge.game.player.ItemInventory;
import ore.forge.game.player.ItemInventoryNode;

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
    private final Array<Entity> previewEntities;
    private final List<ItemDefinition> definitions;
    private final List<Vector3> offsets;
    private final Deque<List<Entity>> undoActions;
    private DefaultInputState defaultState;
    private final GameContext context;

    public BuildingInputState(InputHandler inputHandler) {
        super(inputHandler);
        definitions = new ArrayList<>();
        previewEntities = new Array<>(false, 24);
        offsets = new ArrayList<>();
        undoActions = new ArrayDeque<>();
        context = inputHandler.context;
    }

    @Override
    public void update(float delta) {
        cameraController.update(delta);
        Vector3 mouseWorld = inputHandler.getMouseGroundPosition(cameraController.getCamera());

        for (int i = 0; i < previewEntities.size; i++) {
            Vector3 offset = offsets.get(i);
            Entity item = previewEntities.get(i);

            Vector3 newPosition = new Vector3(mouseWorld).add(offset);

//            Matrix4 newTransform = new Matrix4(item.rootTransform.currentTransform);
//            newTransform.setTranslation(newPosition);

//            item.setTransform(newTransform);
//            TransformManager.teleport(item, newTransform);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            //Rotate all items 90 degrees around center point and rotate around their relative direction too.
            for (int i = 0; i < previewEntities.size; i++) {
                Vector3 offset = offsets.get(i);
                offset.rotate(Vector3.Y, ROTATION_ANGLE); //rotate relative to center

                //rotate item itself
                Entity item = previewEntities.get(i);
//                Matrix4 transform = new Matrix4(item.rootTransform.currentTransform);
                Quaternion rotation = new Quaternion(Vector3.Y, ROTATION_ANGLE);
//                transform.rotate(rotation);
//                TransformManager.teleport(item, transform);
            }
        }

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
        for (Entity item : previewEntities) {
//            ItemDefinition definition = (ItemDefinition) item.definition;
            ItemInventory inventory = context.player.inventory;
//            inventory.getNode(definition.id()).giveBack();
//            context.previewManager.removePreviewEntity(item);
        }
        //Stage exit of build mode?
        this.cleanUp();
        inputHandler.setInputState(defaultState);
    }

    public void placeItems() {
        //1. Place all currently held items
        for (Entity item : previewEntities) {
            context.entityManager.stageAdd(item);
//            context.previewManager.removePreviewEntity(item);
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
            Entity instance = EntityCreator.createInstance(definition);
            previewEntities.add(instance);
//            context.previewManager.addPreviewEntity(instance);
        }
    }

    public void setActive(ItemDefinition item) {
        assert item != null;
        Entity instance;
//      VisualComponent component = new VisualComponent(new ModelInstance(item.model()));
//      instance = new Entity(null, null, component);
        instance = EntityCreator.createInstance(item);
//        TransformManager.teleport(instance, new Matrix4().setToTranslation(0, 0, 0));
        List<Entity> single = new ArrayList<>(1);
        single.add(instance);
        setActive(single);
    }

    public void setActiveFromDef(List<ItemDefinition> defs, List<Matrix4> transforms) {
        assert defs != null;
        var tempList = new  ArrayList<Entity>(defs.size());
        for (int i = 0; i < defs.size(); i++) {
            tempList.add(EntityCreator.createInstance(defs.get(i)));
//            TransformManager.teleport(tempList.get(i), transforms.get(i));

        }
        setActive(tempList);
    }

    public void setActive(List<Entity> items) {
        assert items != null && !items.isEmpty();
        for (Entity item : items) {
//            context.previewManager.addPreviewEntity(item);
        }

        for (Entity instance : items) {
//            definitions.add((ItemDefinition) instance.definition);
        }

        setPreviewEntities(items);
        List<Matrix4> transforms = new ArrayList<>(items.size());
        for (Entity item : items) {
//            transforms.add(item.rootTransform.currentTransform);
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

    private void setPreviewEntities(List<Entity> items) {
        for (Entity item : items) {
            previewEntities.add(item);
        }

        for (Entity item : items) {
            //TODO apply shader to each entity
        }
    }

    public void cleanUp() {
//        Gameplay3D.Entitys.getFirst().visualComponent.attributes = null;
        for (Entity item : previewEntities) {
            //TODO: Remove building Shader
//            context.previewManager.removePreviewEntity(item);
        }
        previewEntities.clear();
        definitions.clear();
        offsets.clear();
    }

    public void setDefaultState(DefaultInputState defaultState) {
        this.defaultState = defaultState;
    }

}
