package ore.forge.engine.Components;

import com.badlogic.gdx.math.Matrix4;
import ore.forge.engine.PhysicsBody;

import java.util.List;

public class StaticPhysicsComponent {
    List<PhysicsBody> bodies;

    public void place(Matrix4 targetLocation) {
        for (PhysicsBody physicsBody : bodies) {
            physicsBody.syncFromEntity(targetLocation);
        }
    }

    public void foo() {

    }



}
