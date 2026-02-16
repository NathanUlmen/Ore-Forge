package ore.forge.engine;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.game.event.GameEventListener;
import ore.forge.engine.Components.PhysicsComponent;
import ore.forge.game.Updatable;
import ore.forge.engine.Components.VisualComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nathan Ulmen
 * */
public class EntityInstance implements Disposable {
    public enum BodyType { DYNAMIC, STATIC, NONE };
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
    }

    public Object getDefinition() {
        return definition;
    }

    public void addToWorld(btDynamicsWorld dynamicsWorld) {
        physicsComponent.setWorldTransform(worldTransform);
        physicsComponent.addToWorld(dynamicsWorld);
    }

    public void snapshotPreview() {
        for (PhysicsBody body : physicsComponent.getBodies()) {
            body.snapshotPrev();
        }
    }

    public void removeFromWorld(btDynamicsWorld dynamicsWorld) {
        physicsComponent.removeFromWorld(dynamicsWorld);
    }


    public void setTransform(Matrix4 worldTransform) {
        this.worldTransform.set(worldTransform);
        if (physicsComponent != null) {
            physicsComponent.setWorldTransform(this.worldTransform);
        }
    }

    public void syncRender(float alpha) {
        visualComponent.syncFromEntity(physicsComponent.getBodies().getFirst().getRenderTransform(alpha));
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
