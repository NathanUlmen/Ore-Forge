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
import ore.forge.Items.AcquisitionInfo;
import ore.forge.Items.Item;
import ore.forge.ReflectionLoader;
import ore.forge.Strategies.Behavior;
import ore.forge.UpgradeTag;
import ore.forge.VisualComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpgraderSpawner {

    protected String name, id, description;

    protected final UpgradeTag upgradeTag;

    protected final AcquisitionInfo acquisitionInfo;
    protected Model model;
    private final List<btCollisionShape> collisionShapes;
    protected HashMap<String, Behavior> behaviors;
    protected HashMap<String, NodeInfo> nodeInfoMap;
    protected final HashMap<btCollisionShape, NodeInfo> collisionShapeMap;


    record NodeInfo(String behaviorKey, String collisionType, Vector3 relativeDirection, Matrix4 transform) {
    }

    public UpgraderSpawner(JsonValue jsonValue) {
        name = jsonValue.getString("name");
        id = jsonValue.getString("id");
        description = jsonValue.getString("description");
        upgradeTag = new UpgradeTag(jsonValue.get("upgradeTag"));
        acquisitionInfo = new AcquisitionInfo(jsonValue.get("acquisitionInfo"), Item.Tier.valueOf(jsonValue.getString("tier")));
        nodeInfoMap = new HashMap<>();

        behaviors = new HashMap<>();
        for (JsonValue behavior : jsonValue.get("behaviors")) {
            Behavior loadedBehavior = ReflectionLoader.load(behavior, "behaviorName");
            behaviors.put(behavior.getString("key"), loadedBehavior);
        }

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        int nodeMapKey = 0;
        for (JsonValue geometryPart : jsonValue.get("parts")) {
            String type = geometryPart.getString("type");
            String behaviorKey = geometryPart.getString("behaviorKey", "");
            String collisionType = geometryPart.getString("bulletCollisionType", "");

            Vector3 pos = new Vector3(geometryPart.get("relativePosition").asFloatArray());
            Vector3 rot = new Vector3(geometryPart.get("relativeRotation").asFloatArray());


            Matrix4 transform = new Matrix4()
                .setToTranslation(pos)
                .rotate(Vector3.X, rot.x)
                .rotate(Vector3.Y, rot.y)
                .rotate(Vector3.Z, rot.z);

            NodeInfo nodeInfo = new NodeInfo(behaviorKey, collisionType, new Vector3(geometryPart.get("relativeDirection").asFloatArray()), transform);
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

            node.localTransform.set(transform);

            node.translation.set(pos);
            node.rotation.setEulerAngles(rot.x, rot.y, rot.z);
        }

        model = modelBuilder.end();

        collisionShapeMap = new HashMap<>();
        collisionShapes = createShapes(model, nodeInfoMap, collisionShapeMap);
    }

    private static List<btCollisionShape> createShapes(Model model, HashMap<String, NodeInfo> nodeInfoMap, HashMap<btCollisionShape, NodeInfo> trimmedNodeInfo) {
        ArrayList<btCollisionShape> shapeList = new ArrayList<>();

        btCompoundShape compoundShape = new btCompoundShape();
        for (Node part : model.nodes) {
            NodeInfo info = nodeInfoMap.get(part.id);
            btCollisionShape bulletShape = Bullet.obtainStaticNodeShape(part, false);
            switch (info.collisionType) {
                case "both": {
                    compoundShape.addChildShape(part.localTransform, bulletShape);
                    //NO BREAK
                }
                case "btGhostObject": {
                    System.out.println("Added!");
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
        System.out.println("Shape List Size: " + shapeList.size());
        return shapeList;
    }

    public EntityInstance spawnInstance() {
        VisualComponent visualComponent = new VisualComponent(new ModelInstance(this.model));
        List<btCollisionObject> collisionObjects = new ArrayList<>();

        for (btCollisionShape collisionShape : collisionShapes) {


            if (collisionShape instanceof btCompoundShape compoundShape) {//If Compound Shape do nothing as they shouldn't have behaviors.
                btRigidBody rigidBody = new btRigidBody(0, new btDefaultMotionState(), compoundShape);
                collisionObjects.add(rigidBody);
            } else {
                NodeInfo nodeInfo = collisionShapeMap.get(collisionShape);
                Behavior behavior = behaviors.get(nodeInfo.behaviorKey);
                btGhostObject ghostObject = new btGhostObject();
                ghostObject.setCollisionShape(collisionShape);
                ghostObject.setWorldTransform(nodeInfo.transform);
                behavior.attach(this, ghostObject);
                ghostObject.userData = new ItemUserData(nodeInfo.relativeDirection.cpy(), behavior, null);
                ghostObject.setCollisionFlags(ghostObject.getCollisionFlags()
                    | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
                collisionObjects.add(ghostObject);
            }
        }

//        for (Node node : visualComponent.modelInstance.nodes) {
//            NodeInfo info = nodeInfoMap.get(node.id);
//            Behavior behavior = behaviors.get(info.behaviorKey);
//            btCollisionShape collisionShape = collisionShapeMap.get(node.id);
//
//            switch (collisionShape) {
//                case btCompoundShape compoundShape -> {
//                    //If Compound Shape do nothing as they shouldn't have behaviors.
//                    System.out.println("ADDED COMPOUND SHAPE");
//                    btRigidBody rigidBody = new btRigidBody(0, new btDefaultMotionState(), compoundShape);
//                    collisionObjects.add(rigidBody);
//                }
//
//                case btCollisionShape ghostShape -> {
//                    //If it's a collision shape that means it's a ghost object
//                    if (behavior != null) {
//                        btGhostObject ghostObject = new btGhostObject();
//                        ghostObject.setCollisionShape(ghostShape);
//                        ghostObject.setWorldTransform(node.localTransform);
//                        behavior.attach(this, ghostObject);
//                        ghostObject.userData = new ItemUserData(info.relativeDirection.cpy(), behavior, null);
//                        ghostObject.setCollisionFlags(ghostObject.getCollisionFlags()
//                            | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
//                        collisionObjects.add(ghostObject);
//                    }
//                }
//                case null -> {}
//            }
//        }
        System.out.println("Collision Object Size:" + collisionObjects.size());
        return new EntityInstance(this, collisionObjects, visualComponent);
    }

    //TODO: OPTIMIZATION Only need to create the shapes one time
    //TODO: Add Collision Contact Rules for shapes
    public EntityInstance createInstance() {
        VisualComponent component = new VisualComponent(new ModelInstance(this.model));
        List<btCollisionObject> collisionObjects = new ArrayList<>();

        btCompoundShape compoundShape = new btCompoundShape();
        for (Node part : component.modelInstance.nodes) {
            NodeInfo info = nodeInfoMap.get(part.id);

            btCollisionShape nodeShape = Bullet.obtainStaticNodeShape(part, false);
            switch (info.collisionType) {
                case "both": {
                    compoundShape.addChildShape(part.localTransform, nodeShape);
                    //NO BREAK BECAUSE WE ALSO NEED TO CREATE A GHOST OBJECT
                }
                case "btGhostObject": {
                    //Purely there for behavior no role in physics. EX: Upgrade Beam or Drop Source
                    btGhostObject ghostObject = new btGhostObject();
                    ghostObject.setCollisionShape(nodeShape);
                    //TODO: look into refining or removing blueprint parameter
                    Behavior behavior = behaviors.get(info.behaviorKey);
                    behavior.attach(this, ghostObject);
                    ghostObject.userData = new ItemUserData(info.relativeDirection.cpy(), behavior, null);
                    ghostObject.setCollisionFlags(ghostObject.getCollisionFlags()
                        | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
                    ghostObject.setWorldTransform(part.localTransform);
                    collisionObjects.add(ghostObject);
                    break;
                }
                case "btRigidBody": {
                    //Purley there for collision has no behavior. EX: walls
                    compoundShape.addChildShape(part.localTransform, nodeShape);
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + info.collisionType);
            }
        }
        //Create the rigid body from the compound shape and add it to our collision objects
        collisionObjects.add(new btRigidBody(0, new btDefaultMotionState(), compoundShape));
        return new EntityInstance(this, collisionObjects, component);
    }

    public UpgradeTag getUpgradeTag() {
        return upgradeTag;
    }


}
