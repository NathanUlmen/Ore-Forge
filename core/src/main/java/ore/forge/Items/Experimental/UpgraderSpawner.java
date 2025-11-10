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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
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
    protected HashMap<String, Behavior> behaviors;
    protected HashMap<String, NodeInfo> nodeInfoMap;

    record NodeInfo(String behaviorKey, String collisionType, Vector3 relativeDirection) {}

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
            NodeInfo nodeInfo = new NodeInfo(behaviorKey, collisionType, new Vector3(geometryPart.get("relativeDirection").asFloatArray()));
            nodeInfoMap.put(Integer.toString(nodeMapKey), nodeInfo);

            Vector3 pos = new Vector3( geometryPart.get("relativePosition").asFloatArray());

            Vector3 rot = new Vector3( geometryPart.get("relativeRotation").asFloatArray());

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
            node.rotation.setEulerAngles(rot.y, rot.x, rot.z);
        }

        model = modelBuilder.end();
    }

    //TODO: Look into using btCompoundCollisionShape instead
    //TODO: Add Collision Contact Rules for shapes
    public EntityInstance createInstance() {
        VisualComponent component = new VisualComponent(new ModelInstance(this.model));
        List<btCollisionObject> collisionObjects = new ArrayList<>();
        //Create physics objects for each part of the model
        for (Node part : component.modelInstance.nodes) {
            String key = part.id;
            NodeInfo info = nodeInfoMap.get(key);
            System.out.println(info);


            btCollisionShape nodeShape = Bullet.obtainStaticNodeShape(part, false);
            switch (info.collisionType) {
                case "both" : {
                    //Plays a role in physics and in behavior: EX: Conveyor, Furnace?
                    btRigidBody rigidBody = new btRigidBody(0f, new btDefaultMotionState(), nodeShape);
                    Behavior behavior = behaviors.get(info.behaviorKey);
                    behavior.attach(this, rigidBody);
                    rigidBody.userData = new ItemUserData(info.relativeDirection.cpy(), behavior, null);
                    collisionObjects.add(rigidBody);
                    break;
                }
                case "btGhostObject" : {
                    //Purely there for behavior no role in physics. EX: Upgrade Beam or Drop Source
                    btGhostObject ghostObject = new btGhostObject();
                    ghostObject.setCollisionShape(nodeShape);
                    //TODO: look into refining or removing blueprint parameter
                    Behavior behavior = behaviors.get(info.behaviorKey);
                    behavior.attach(this, ghostObject);
                    ghostObject.userData = new ItemUserData(info.relativeDirection.cpy(), behavior, null);
                    ghostObject.setCollisionFlags(ghostObject.getCollisionFlags()
                        | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
                    collisionObjects.add(ghostObject);
                    break;
                }
                case "btRigidBody" : {
                    //Purley there for collision has no behavior. EX: walls
                    btRigidBody rigidBody = new btRigidBody(0f, new btDefaultMotionState(), nodeShape);
                    collisionObjects.add(rigidBody);
                    break;
                }
                default : throw new IllegalStateException("Unexpected value: " + info.collisionType);
            }
            collisionObjects.getLast().setWorldTransform(part.globalTransform);
        }
        EntityInstance instance = new EntityInstance(this, collisionObjects, component);
        return instance;
    }

    public UpgradeTag getUpgradeTag() {
        return upgradeTag;
    }



}
