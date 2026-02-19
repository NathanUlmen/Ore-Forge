package ore.forge.game;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import ore.forge.engine.components.PhysicsComponent;
import ore.forge.engine.components.RenderC;
import ore.forge.engine.Entity;
import ore.forge.engine.PhysicsBody;
import ore.forge.game.items.ItemDefinition;
import ore.forge.game.items.ItemUserData;
import ore.forge.game.items.NodeInfo;
import ore.forge.game.behaviors.BodyLogic;

import java.util.ArrayList;
import java.util.List;

public class EntityCreator {

    public static Entity createInstance(ItemDefinition itemDefinition) {
        RenderC renderC = new RenderC(new ModelInstance(itemDefinition.model()));

        List<PhysicsBody> physicsComponents = new ArrayList<>();
        PhysicsComponent physicsComponent = new PhysicsComponent(physicsComponents);
        Entity instance = new Entity(itemDefinition);
        instance.physicsComponent = physicsComponent;
        instance.renderC = renderC;
        for (btCollisionShape collisionShape : itemDefinition.collisionShapes()) {
            if (collisionShape instanceof btCompoundShape compoundShape) {//If Compound Shape do nothing as they shouldn't have behaviors.
                btRigidBody rigidBody = new btRigidBody(0, new btDefaultMotionState(), compoundShape);
                rigidBody.userData = new PhysicsBodyData(instance, itemDefinition, null, rigidBody.getWorldTransform());
                //---------------
                PhysicsBody body = new PhysicsBody(PhysicsBody.PhysicsBodyType.KINEMATIC, new Matrix4());
                body.bodyHandle = rigidBody;
                //---------------
                physicsComponents.add(body);
            } else {
                //The rest will be sensor
                NodeInfo nodeInfo = itemDefinition.shapeMap().get(collisionShape);
                BodyLogic bodyLogic = itemDefinition.behaviors().get(nodeInfo.behaviorKey()).clone();
                btGhostObject ghostObject = new btGhostObject();
                ghostObject.setCollisionShape(collisionShape);
                ghostObject.setWorldTransform(nodeInfo.transform());
                bodyLogic.attach(itemDefinition, ghostObject);
                ItemUserData itemUserData = new ItemUserData(nodeInfo.relativeDirection(), itemDefinition);
                ghostObject.userData = new PhysicsBodyData(instance, itemUserData, bodyLogic, nodeInfo.transform().cpy());
                ghostObject.setCollisionFlags(ghostObject.getCollisionFlags() | CollisionRules.combineBits(CollisionRules.ORE_PROCESSOR)
                    | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
                //--------
                physicsComponents.add(new PhysicsBody(PhysicsBody.PhysicsBodyType.KINEMATIC, nodeInfo.transform()));
                //--------
            }
        }
        return instance;
    }

    //Create one for ore instance too...
    public static Entity createInstance(OreDefinition oreDefinition) {
        //Visual Setup
        RenderC renderC = new RenderC(new ModelInstance(oreDefinition.model()));
        //Physics Setup
        Vector3 inertia = new Vector3();
        oreDefinition.oreShape().calculateLocalInertia(10f, inertia);
        btRigidBody oreBody = new btRigidBody(10f, new btDefaultMotionState(), oreDefinition.oreShape(), inertia);

        var tempList = new ArrayList<PhysicsBody>(1);

        PhysicsBody body = new PhysicsBody(PhysicsBody.PhysicsBodyType.DYNAMIC, new Matrix4());
        body.bodyHandle = oreBody;
        tempList.add(body);

        //Create our instance
        Entity instance = new Entity(oreDefinition);
        instance.renderC = renderC;
        instance.physicsComponent = new PhysicsComponent(tempList);

        Ore ore = new Ore();
        ore.setOreValue(oreDefinition.oreValue());
        oreBody.userData = new PhysicsBodyData(instance, ore, null, oreBody.getWorldTransform());

        return instance;
    }

    /**
     * Creates preview Instance
     * */
    /**
     * Creates preview Instance
     */
    public static Entity createPreviewInstance(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }

        // Visuals
        ModelInstance modelInstance = new ModelInstance(model);
        RenderC renderC = new RenderC(modelInstance);

        // Calculate model bounds
        BoundingBox boundingBox = new BoundingBox();
        modelInstance.calculateBoundingBox(boundingBox);

        Vector3 dimensions = new Vector3();
        boundingBox.getDimensions(dimensions);

        // Bullet uses HALF extents
        Vector3 halfExtents = dimensions.scl(0.5f);
        btCollisionShape shape = new btBoxShape(halfExtents);
        btRigidBody rb = new btRigidBody(0,  new btDefaultMotionState(), shape);
        PhysicsBody body = new PhysicsBody(PhysicsBody.PhysicsBodyType.KINEMATIC, new Matrix4());
        var tempList = new ArrayList<PhysicsBody>(1);
        tempList.add(body);

        // Physics (preview = no simulation)
        PhysicsComponent physicsComponent;
        physicsComponent = new PhysicsComponent(tempList);

        // Preview instance has no ID
        Entity instance = new Entity(model);
        instance.renderC = renderC;
        instance.physicsComponent = physicsComponent;

        return instance;
    }

}
