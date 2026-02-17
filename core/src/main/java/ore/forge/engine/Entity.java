package ore.forge.engine;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.engine.components.AnimationComponent;
import ore.forge.engine.components.PhysicsComponent;
import ore.forge.engine.components.VisualComponent;
import ore.forge.game.Updatable;
import ore.forge.game.event.GameEventListener;

import java.util.ArrayList;
import java.util.List;

public class Entity implements Disposable {
    public final Object definition; //Definition used to create this Instance.
    public final Matrix4 rootTransform; //central position of entity in world. Root.
    public PhysicsComponent physicsComponent;
    public VisualComponent visualComponent;
    public AnimationComponent animationComponent;


    public final List<Updatable> updatables; //Scripts and stuff whose lifetimes are tied to that of this entities
    public final List<GameEventListener> gameEventListeners; //Event scripts whose lifetimes are tied to the entity

    public Entity(Object definition) {
        this.definition = definition;
        this.rootTransform = new Matrix4();

        this.updatables = new ArrayList<>();
        this.gameEventListeners = new ArrayList<>();
    }


    @Override
    public void dispose() {

    }
}
