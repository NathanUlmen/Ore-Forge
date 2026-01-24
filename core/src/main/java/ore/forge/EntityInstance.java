package ore.forge;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.EventSystem.GameEventListener;
import ore.forge.Strategies.Updatable;

import java.util.ArrayList;
import java.util.List;

public class EntityInstance implements Disposable {
    private final Object definition;
    public final List<Updatable> updatables;
    public final List<GameEventListener<?>> listeners;
    public final PhysicsComponent physicsComponent;
    public VisualComponent visualComponent;
    private final Matrix4 worldTransform;

    public EntityInstance(Object definition, PhysicsComponent physicsComponent, VisualComponent visualComponent) {
        this.definition = definition;
        worldTransform = new Matrix4();
        this.physicsComponent = physicsComponent;
        this.updatables = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.visualComponent = visualComponent;
        visualComponent.modelInstance.transform = worldTransform;
    }

    public Object getDefinition() {
        return definition;
    }

    public void addToWorld(btDynamicsWorld dynamicsWorld) {
        physicsComponent.addToWorld(dynamicsWorld);
        syncFromPhysics();
    }

    public void removeFromWorld(btDynamicsWorld dynamicsWorld) {
        physicsComponent.removeFromWorld(dynamicsWorld);
    }

    public void setTransform(Matrix4 worldTransform) {
        this.worldTransform.set(worldTransform);
        if (physicsComponent != null) {
            physicsComponent.setWorldTransform(worldTransform);
        }
        visualComponent.modelInstance.transform.set(worldTransform);
    }

    public void syncFromPhysics() {
        physicsComponent.syncToEntity(worldTransform);
        visualComponent.syncFromEntity(worldTransform);
    }

    public Matrix4 transform() {
        return worldTransform;
    }

    @Override
    public void dispose() {
        physicsComponent.dispose();
        visualComponent.dispose();
    }
}
