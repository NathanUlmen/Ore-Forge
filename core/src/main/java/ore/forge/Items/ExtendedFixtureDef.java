package ore.forge.Items;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public class ExtendedFixtureDef extends FixtureDef {
    private final float relativeAngle;
    private String collisionBehaviorKey;
    private final boolean collisionEnabled;

    public ExtendedFixtureDef(float relativeAngle, boolean collisionEnabled, String collisionBehaviorKey) {
        this.relativeAngle = relativeAngle;
        this.collisionEnabled = collisionEnabled;
        this.collisionBehaviorKey = collisionBehaviorKey;
    }

    public String getCollisionBehaviorKey() {
        return collisionBehaviorKey;
    }

    public float getRelativeAngle() {
        return relativeAngle;
    }

    public boolean isCollisionEnabled() {
        return collisionEnabled;
    }

}
