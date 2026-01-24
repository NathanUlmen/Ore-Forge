package ore.forge;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import ore.forge.Items.ItemDefinition;
import ore.forge.Items.ItemUserData;
import ore.forge.Items.NodeInfo;
import ore.forge.Strategies.BodyLogic;

import java.util.ArrayList;
import java.util.List;

import static ore.forge.CollisionRules.*;

public class EntityInstanceCreator {

    public static EntityInstance createInstance(ItemDefinition itemDefinition) {
        VisualComponent visualComponent = new VisualComponent(new ModelInstance(itemDefinition.model()));

        List<PhysicsBody> physicsComponents = new ArrayList<>();
        PhysicsComponent physicsComponent = new PhysicsComponent(physicsComponents);
        EntityInstance instance = new EntityInstance(itemDefinition, physicsComponent, visualComponent);
        for (btCollisionShape collisionShape : itemDefinition.collisionShapes()) {
            if (collisionShape instanceof btCompoundShape compoundShape) {//If Compound Shape do nothing as they shouldn't have behaviors.
                btRigidBody rigidBody = new btRigidBody(0, new btDefaultMotionState(), compoundShape);
                rigidBody.userData = new PhysicsBodyData(instance, itemDefinition, null, rigidBody.getWorldTransform());
                //---------------
                PhysicsBody body = new PhysicsBody(rigidBody, new Matrix4(), CollisionRules.combineBits(WORLD_GEOMETRY), CollisionRules.combineBits(ORE));
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
                physicsComponents.add(new PhysicsBody(ghostObject, nodeInfo.transform(), CollisionRules.combineBits(ORE_PROCESSOR), CollisionRules.combineBits(ORE)));
                //--------
            }
        }
        return instance;
    }

    //Create one for ore instance too...
    public static EntityInstance createInstance(OreDefinition oreDefinition) {
        //Visual Setup
        VisualComponent visualComponent = new VisualComponent(new ModelInstance(oreDefinition.model()));
        //Physics Setup
        Vector3 inertia = new Vector3();
        oreDefinition.oreShape().calculateLocalInertia(10f, inertia);
        btRigidBody oreBody = new btRigidBody(10f, new btDefaultMotionState(), oreDefinition.oreShape(), inertia);

        var tempList = new ArrayList<PhysicsBody>(1);

        tempList.add(new PhysicsBody(oreBody, new Matrix4(), CollisionRules.combineBits(ORE), CollisionRules.combineBits(ORE, WORLD_GEOMETRY, ORE_PROCESSOR)));
        //Create our instance
        EntityInstance instance = new EntityInstance(oreDefinition, new PhysicsComponent(tempList), visualComponent);

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
    public static EntityInstance createPreviewInstance(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }

        // Visuals
        ModelInstance modelInstance = new ModelInstance(model);
        VisualComponent visualComponent = new VisualComponent(modelInstance);

        // Calculate model bounds
        BoundingBox boundingBox = new BoundingBox();
        modelInstance.calculateBoundingBox(boundingBox);

        Vector3 dimensions = new Vector3();
        boundingBox.getDimensions(dimensions);

        // Bullet uses HALF extents
        Vector3 halfExtents = dimensions.scl(0.5f);
        btCollisionShape shape = new btBoxShape(halfExtents);
        btRigidBody rb = new btRigidBody(0,  new btDefaultMotionState(), shape);
        PhysicsBody body = new PhysicsBody(rb, new Matrix4(), CollisionRules.combineBits(PREVIEW), CollisionRules.combineBits(ORE_PROCESSOR));
        var tempList = new ArrayList<PhysicsBody>(1);
        tempList.add(body);

        // Physics (preview = no simulation)
        PhysicsComponent physicsComponent;
        physicsComponent = new PhysicsComponent(tempList);

        // Preview instance has no ID
        EntityInstance instance = new EntityInstance(
            null,
            physicsComponent,
            visualComponent
        );

        return instance;
    }

}
