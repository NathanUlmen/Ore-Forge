package ore.forge.Items;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public class CustomFixtureDef extends FixtureDef {
    public final float relativeAngle;
    private String collisionBehaviorKey;
    public final boolean collisionEnabled;

    public CustomFixtureDef(float relativeAngle, boolean collisionEnabled, String collisionBehaviorKey) {
        this.relativeAngle = relativeAngle;
        this.collisionEnabled = collisionEnabled;
        this.collisionBehaviorKey = collisionBehaviorKey;
    }

    public String getCollisionBehaviorKey() {
        return collisionBehaviorKey;
    }

}
