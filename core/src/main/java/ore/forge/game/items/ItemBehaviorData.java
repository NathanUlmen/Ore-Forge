package ore.forge.game.items;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import ore.forge.game.behaviors.BodyLogic;

import java.util.HashMap;

public record ItemBehaviorData(HashMap<String, BodyLogic> behaviors, HashMap<btCollisionShape, NodeInfo> shapeMap) {
}
