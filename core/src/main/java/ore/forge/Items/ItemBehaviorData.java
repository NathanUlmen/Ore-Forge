package ore.forge.Items;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import ore.forge.Strategies.BodyLogic;

import java.util.HashMap;

public record ItemBehaviorData(HashMap<String, BodyLogic> behaviors, HashMap<btCollisionShape, NodeInfo> shapeMap) {
}
