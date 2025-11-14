package ore.forge.Items.Experimental;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.CollisionRules;
import ore.forge.Items.AcquisitionInfo;
import ore.forge.Items.Item;
import ore.forge.ReflectionLoader;
import ore.forge.Strategies.Behavior;
import ore.forge.VisualComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemSpawner {

    record NodeInfo(String behaviorKey,
                    String collisionType,
                    Vector3 relativeDirection,
                    Matrix4 transform) {}

    protected final String name, id, description;
    protected final AcquisitionInfo acquisitionInfo;
    protected final Model model;
    protected final List<btCollisionShape> collisionShapes;
    protected final HashMap<String, Behavior> behaviors;
    protected final HashMap<btCollisionShape, NodeInfo> collisionShapeMap;

    public ItemSpawner(JsonValue jsonValue) {
        name = jsonValue.getString("name");
        id = jsonValue.getString("id");
        description = jsonValue.getString("description");
        acquisitionInfo = new AcquisitionInfo(jsonValue.get("acquisitionInfo"), Item.Tier.valueOf(jsonValue.getString("tier")));
        HashMap<String,  NodeInfo> nodeInfoMap = new HashMap<>();

        this.behaviors = loadBehaviors(jsonValue);

        this.model = createModel(jsonValue, nodeInfoMap);

        collisionShapeMap = new HashMap<>();
        collisionShapes = createShapes(model, nodeInfoMap, collisionShapeMap);
    }

    private HashMap<String, Behavior> loadBehaviors(JsonValue jsonValue) {
        HashMap<String, Behavior> behaviors = new HashMap<>();
        for (JsonValue behavior : jsonValue.get("behaviors")) {
            Behavior loadedBehavior = ReflectionLoader.load(behavior, "behaviorName");
            behaviors.put(behavior.getString("key"), loadedBehavior);
        }
        return behaviors;
    }

    private Model createModel(JsonValue jsonValue, HashMap<String, NodeInfo> nodeInfoMap) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        int nodeMapKey = 0;
        for (JsonValue geometryPart : jsonValue.get("parts")) {
            String type = geometryPart.getString("type");
            String behaviorKey = geometryPart.getString("behaviorKey", "");
            String collisionType = geometryPart.getString("bulletCollisionType", "");

            Vector3 pos = new Vector3(geometryPart.get("relativePosition").asFloatArray());
            Vector3 rot = new Vector3(geometryPart.get("relativeRotation").asFloatArray());
            Vector3 dir = new  Vector3(geometryPart.get("relativeDirection").asFloatArray());

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

    private List<btCollisionShape> createShapes(Model model, HashMap<String, NodeInfo> nodeInfoMap, HashMap<btCollisionShape, NodeInfo> trimmedNodeInfo) {
        ArrayList<btCollisionShape> shapeList = new ArrayList<>();
        btCompoundShape compoundShape = new btCompoundShape();

        for (Node part : model.nodes) {
            NodeInfo info = nodeInfoMap.get(part.id);
            btCollisionShape bulletShape = Bullet.obtainStaticNodeShape(part, false);
            switch (info.collisionType) {
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
                    throw new IllegalStateException("Unexpected value: " + info.collisionType);
            }
        }
        shapeList.add(compoundShape);
        return shapeList;
    }

    //TODO: Add Collision Contact Rules for shapes
    public EntityInstance spawnInstance() {
        VisualComponent visualComponent = new VisualComponent(new ModelInstance(this.model));
        List<btCollisionObject> collisionObjects = new ArrayList<>();
        for (btCollisionShape collisionShape : collisionShapes) {
            if (collisionShape instanceof btCompoundShape compoundShape) {//If Compound Shape do nothing as they shouldn't have behaviors.
                btRigidBody rigidBody = new btRigidBody(0, new btDefaultMotionState(), compoundShape);
                //rigidBody.setCollisionFlags(rigidBody.getCollisionFlags() | CollisionRules.combineBits(CollisionRules.WORLD_GEOMETRY));
                collisionObjects.add(rigidBody);
            } else {
                //The rest will be sensor
                NodeInfo nodeInfo = collisionShapeMap.get(collisionShape);
                Behavior behavior = behaviors.get(nodeInfo.behaviorKey).clone();
                btGhostObject ghostObject = new btGhostObject();
                ghostObject.setCollisionShape(collisionShape);
                ghostObject.setWorldTransform(nodeInfo.transform);
                behavior.attach(this, ghostObject);
                System.out.println("Ghost object Transform");
                System.out.println(ghostObject.getWorldTransform());
                ghostObject.userData = new ItemUserData(nodeInfo.relativeDirection.cpy(), behavior, this, nodeInfo.transform.cpy());
                ghostObject.setCollisionFlags(ghostObject.getCollisionFlags() | CollisionRules.combineBits(CollisionRules.ORE_PROCESSOR)
                    | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
                collisionObjects.add(ghostObject);
            }
        }
        return new EntityInstance(this, collisionObjects, visualComponent);
    }



}
