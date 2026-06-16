package ore.forge.engine.components.definitions;

import com.badlogic.gdx.math.Vector3;
import ore.forge.ComponentDefinition;
import ore.forge.engine.components.DirectionC;

public class DirectionDefinition implements ComponentDefinition<DirectionC> {
    private final Vector3 direction;
    private final Vector3 offset;

    public DirectionDefinition(Vector3 direction, Vector3 offset) {
        this.direction = direction;
        this.offset = offset;
    }

    @Override
    public DirectionC create() {
        DirectionC directionC = new DirectionC();
        directionC.direction.set(this.direction);
        directionC.directionOffset.set(this.offset);
        return directionC;
    }

}
