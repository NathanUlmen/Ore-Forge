package ore.forge.game.items;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.items.Acquisition.AcquisitionInfo;
import ore.forge.game.items.Properties.BundledProperties;
import ore.forge.game.items.Properties.DropperProperties;
import ore.forge.game.items.Properties.ItemProperties;
import ore.forge.game.items.Properties.UpgraderProperties;
import ore.forge.game.OreDefinition;
import ore.forge.engine.ReflectionLoader;
import ore.forge.engine.render.RenderPart;
import ore.forge.game.behaviors.BodyLogic;
import ore.forge.game.UpgradeTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemDefinition {
    //General Stuff
    protected String name, id, description;
    protected Tier category;
    protected ItemRole[] role;
    protected AcquisitionInfo acquisitionInfo;

    //Visual
    protected Model model;
    protected List<RenderPart> renderParts;

    //Physics
    protected List<btCollisionShape> collisionShapes;

    //Gameplay Logic/Behavior
    protected HashMap<String, BodyLogic> behaviors;

    protected HashMap<btCollisionShape, NodeInfo> shapeMap;

    //Properties specific to item type
    protected ItemProperties itemProperties;

    public static ItemDefinition createDefinition(JsonValue json) {
        //General Stuff
        String name = json.getString("name");
        String id = json.getString("id");
        Tier category = Tier.valueOf(json.getString("tier"));
        AcquisitionInfo acquisitionInfo = new AcquisitionInfo(json.get("acquisitionInfo"), category);
        String description = json.getString("description");

        ItemRole[] roles = new ItemRole[json.get("role").size];
        for (int i = 0; i < roles.length; i++) {
            roles[i] = ItemRole.valueOf(json.get("role").get(i).toString());
        }
        GeneralItemData generalItemData = new GeneralItemData(name, id, description, roles, acquisitionInfo);

        HashMap<String, NodeInfo> nodeInfoHashMap = new HashMap<>();
        //Visual Stuff
        Model model = createModel(json, nodeInfoHashMap);

        HashMap<String, BodyLogic> behaviors = loadBehaviors(json);
        HashMap<btCollisionShape, NodeInfo> collisionShapeMap = new HashMap<>();
        List<btCollisionShape> collisionShapes = createShapes(model, nodeInfoHashMap, collisionShapeMap);

        ItemBehaviorData behaviorData = new ItemBehaviorData(behaviors, collisionShapeMap);

        //TODO: finish this so that it handles multiple properties correctly.
        ItemProperties itemProperties;
        JsonValue propertiesJson = json.get("properties"); //grab our list of properties
        if (propertiesJson.isEmpty()) {
            itemProperties = null;
        } else if (propertiesJson.size > 1) { //if more than one we make a bundled list of properties
            List<ItemProperties> properties = new ArrayList<>(json.size);
            itemProperties = new BundledProperties(properties);
            for (JsonValue value :  propertiesJson) {
                properties.add(createProperty(value));
            }
        } else {
            itemProperties = createProperty(propertiesJson.get(0));
        }

        return new ItemDefinition(generalItemData, model, collisionShapes, behaviorData, itemProperties);
    }

    private static ItemProperties createProperty(JsonValue json) {
        return switch (json.getString("type")) {
            case "UpgradeTag" -> new UpgraderProperties(new UpgradeTag(json));
            case "OreDefinition" -> new DropperProperties(OreDefinition.fromJson(json));
            default -> throw new IllegalArgumentException("Unknown property type: " + json.getString("type"));
        };
    }

    private static HashMap<String, BodyLogic> loadBehaviors(JsonValue jsonValue) {
        HashMap<String, BodyLogic> behaviors = new HashMap<>();
        for (JsonValue behavior : jsonValue.get("behaviors")) {
            BodyLogic loadedBodyLogic = ReflectionLoader.load(behavior, "behaviorName");
            behaviors.put(behavior.getString("key"), loadedBodyLogic);
        }
        return behaviors;
    }

    private static Model createModel(JsonValue jsonValue, HashMap<String, NodeInfo> nodeInfoMap) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        int nodeMapKey = 0;
        for (JsonValue geometryPart : jsonValue.get("parts")) {
            String type = geometryPart.getString("type");
            String behaviorKey = geometryPart.getString("behaviorKey", "");
            String collisionType = geometryPart.getString("bulletCollisionType", "");

            Vector3 pos = new Vector3(geometryPart.get("relativePosition").asFloatArray());
            Vector3 rot = new Vector3(geometryPart.get("relativeRotation").asFloatArray());
            Vector3 dir = new Vector3(geometryPart.get("relativeDirection").asFloatArray());

            Matrix4 transform = new Matrix4()
                .setToTranslation(pos)
                .rotate(Vector3.X, rot.x)
                .rotate(Vector3.Y, rot.y)
                .rotate(Vector3.Z, rot.z);

            NodeInfo nodeInfo = new NodeInfo(behaviorKey, collisionType, dir, transform);
            nodeInfoMap.put(Integer.toString(nodeMapKey), nodeInfo);


            Vector3 dims = new Vector3(geometryPart.get("dimensions").asFloatArray());

            Node node = modelBuilder.node();
            node.id = Integer.toString(nodeMapKey);
            nodeMapKey++;

            MeshPartBuilder meshPart = modelBuilder.part(
                node.id,
                GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material()
            );

            if (type.equalsIgnoreCase("box")) {
                BoxShapeBuilder.build(meshPart, dims.x, dims.y, dims.z);
            }

            node.translation.set(pos);
            node.rotation.setEulerAngles(rot.x, rot.y, rot.z);
            node.calculateLocalTransform();

            assert node.localTransform.equals(transform);
        }

        return modelBuilder.end();
    }

    private static List<btCollisionShape> createShapes(Model model, HashMap<String, NodeInfo> nodeInfoMap, HashMap<btCollisionShape, NodeInfo> trimmedNodeInfo) {
        ArrayList<btCollisionShape> shapeList = new ArrayList<>();
        btCompoundShape compoundShape = new btCompoundShape();

        for (Node part : model.nodes) {
            NodeInfo info = nodeInfoMap.get(part.id);
            btCollisionShape bulletShape = Bullet.obtainStaticNodeShape(part, false);
            switch (info.collisionType()) {
                case "both": {
                    //For elements that need both a sensor and collision enabled. EX: conveyor
                    compoundShape.addChildShape(part.localTransform, bulletShape);
                    //NO BREAK
                }
                case "btGhostObject": {
                    //Purely there to act as a sensor/drive logic. EX: Upgrade Beam
                    shapeList.add(bulletShape);
                    trimmedNodeInfo.put(bulletShape, info);
                    break;
                }
                case "btRigidBody": {
                    //Purley there for collision has no behavior. EX: walls
                    compoundShape.addChildShape(part.localTransform, bulletShape);
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + info.collisionType());
            }
        }
        shapeList.add(compoundShape);
        return shapeList;
    }

    private ItemDefinition(GeneralItemData itemData, Model model, List<btCollisionShape> itemBody, ItemBehaviorData behaviorData, ItemProperties itemProperties) {
        this.name = itemData.name();
        this.id = itemData.id();
        this.description = itemData.description();
        this.role = itemData.type();
        this.acquisitionInfo = itemData.acquisitionInfo();
        this.model = model;
        this.collisionShapes = itemBody;
        this.behaviors = behaviorData.behaviors();
        this.shapeMap = behaviorData.shapeMap();
        this.itemProperties = itemProperties;
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

    public ItemRole[] type() {
        return role;
    }

    public List<btCollisionShape> collisionShapes() {
        return collisionShapes;
    }

    public HashMap<btCollisionShape, NodeInfo> shapeMap() {
        return shapeMap;
    }

    public HashMap<String, BodyLogic> behaviors() {
        return behaviors;
    }

    public ItemRole[] role() {
        return role;
    }

    public Tier category() {
        return category;
    }

    public AcquisitionInfo getAcquisitionInfo() {
        return acquisitionInfo;
    }

    public Model model() {
        return model;
    }

    public ItemProperties itemProperties() {
        return itemProperties;
    }

    public <E extends ItemProperties> E itemProperties(Class<E> target) {
        if (target.isInstance(itemProperties)) {
            return target.cast(itemProperties);
        } else if (itemProperties instanceof BundledProperties bundledProperties) {
            for (ItemProperties property : bundledProperties) {
                if (target.isInstance(property)) {
                    return target.cast(property);
                }
            }
        }
        throw new ClassCastException(
            "ItemProperties type mismatch. Expected: " + target.getName() +
                ", actual: " + itemProperties.getClass().getName()
        );
    }

}
