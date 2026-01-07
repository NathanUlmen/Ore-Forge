package ore.forge.Items.Experimental;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import ore.forge.Items.AcquisitionInfo;
import ore.forge.Strategies.BodyLogic;

import java.util.HashMap;
import java.util.List;

public class ItemDefinition {
    //General Stuff
    protected String name, id, description;
    protected ItemType type;
    protected AcquisitionInfo acquisitionInfo;

    //Visual
    protected Model model;

    //Physics
    protected List<btCollisionShape> collisionShapes;

    //Gameplay Logic/Behavior
    protected HashMap<String, BodyLogic> behaviors;
    protected HashMap<btCollisionShape, NodeInfo> shapeMap;

    //Properties specific to item type
    protected ItemProperties itemProperties;

    public ItemDefinition() {

    }

    public <E extends ItemProperties> E getItemProperties(Class<E> type) {
        if (type.isInstance(ItemProperties.class)) {
            return type.cast(ItemProperties.class);
        }
        return null;
    }

    public String name() {
        return name;
    }

    public String id() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public ItemType type() {
        return type;
    }

    public AcquisitionInfo getAcquisitionInfo() {
        return acquisitionInfo;
    }

    public Model getModel() {
        return model;
    }

    public ItemProperties getItemProperties() {
        return itemProperties;
    }

}
