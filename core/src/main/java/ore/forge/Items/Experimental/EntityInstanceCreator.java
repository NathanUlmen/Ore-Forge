package ore.forge.Items.Experimental;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import ore.forge.*;
import ore.forge.Strategies.BodyLogic;

import java.util.ArrayList;
import java.util.List;

public class EntityInstanceCreator {

    public static EntityInstance createInstance(ItemDefinition itemDefinition) {
        VisualComponent visualComponent = new VisualComponent(new ModelInstance(itemDefinition.model));
        List<btCollisionObject> collisionObjects = new ArrayList<>();
        EntityInstance instance = new EntityInstance(collisionObjects, visualComponent);
        for (btCollisionShape collisionShape : itemDefinition.collisionShapes) {
            if (collisionShape instanceof btCompoundShape compoundShape) {//If Compound Shape do nothing as they shouldn't have behaviors.
                btRigidBody rigidBody = new btRigidBody(0, new btDefaultMotionState(), compoundShape);
                //rigidBody.setCollisionFlags(rigidBody.getCollisionFlags() | CollisionRules.combineBits(CollisionRules.WORLD_GEOMETRY));
                rigidBody.userData = new PhysicsBodyData(instance, itemDefinition, null, rigidBody.getWorldTransform());
                collisionObjects.add(rigidBody);
            } else {
                //The rest will be sensor
                NodeInfo nodeInfo = itemDefinition.shapeMap.get(collisionShape);
                BodyLogic bodyLogic = itemDefinition.behaviors.get(nodeInfo.behaviorKey()).clone();
                btGhostObject ghostObject = new btGhostObject();
                ghostObject.setCollisionShape(collisionShape);
                ghostObject.setWorldTransform(nodeInfo.transform());
                bodyLogic.attach(itemDefinition, ghostObject);
                ItemUserData itemUserData = new ItemUserData(nodeInfo.relativeDirection(), itemDefinition);
                ghostObject.userData = new PhysicsBodyData(instance, itemUserData, bodyLogic, nodeInfo.transform());
                ghostObject.setCollisionFlags(ghostObject.getCollisionFlags() | CollisionRules.combineBits(CollisionRules.ORE_PROCESSOR)
                    | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
                collisionObjects.add(ghostObject);
            }
        }
        return instance;
    }

    //Create one for ore instance too...
    public static EntityInstance createInstance(OreDefinition oreDefinition) {
        //Visual Setup
        VisualComponent visualComponent = new VisualComponent(new ModelInstance(oreDefinition.model()));
        //Physics Setup
        btRigidBody oreBody = new btRigidBody(10, new btDefaultMotionState(), oreDefinition.oreShape());
        var tempList = new ArrayList<btCollisionObject>(1);
        tempList.add(oreBody);
        //Create oru instance
        EntityInstance instance = new EntityInstance(tempList, visualComponent);

        Ore ore = new Ore();
        ore.setOreValue(oreDefinition.oreValue());
        oreBody.userData = new PhysicsBodyData(instance, ore, null, oreBody.getWorldTransform());

        return instance;
    }
}
