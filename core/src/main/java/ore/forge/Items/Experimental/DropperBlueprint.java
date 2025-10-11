package ore.forge.Items.Experimental;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.ExtendedFixtureDef;
import ore.forge.Strategies.DropperStrategies.BurstDrop;

import java.util.ArrayList;

public class DropperBlueprint extends ItemBlueprint {
    private final OreBlueprint oreBlueprint;
    private final BurstDrop burstDrop;


    public DropperBlueprint(JsonValue jsonValue) {
        super(jsonValue);
        oreBlueprint = OreBlueprint.load(jsonValue.get("oreInfo"));
        burstDrop = new BurstDrop(jsonValue.get("burstDrop"));
    }

    private record OreBlueprint(String oreName, double oreValue, int oreTemp, int multiOre, FixtureDef oreShape) {

        public static OreBlueprint load(JsonValue jsonValue) {
            String oreName = jsonValue.get("oreName").asString();
            double oreValue = jsonValue.get("oreValue").asDouble();
            int oreTemp = jsonValue.get("oreTemp").asInt();
            int multiOre = jsonValue.get("multiOre").asInt();
            FixtureDef fixtureDef = new FixtureDef();
            PolygonShape polygonShape = new PolygonShape();
            polygonShape.set(jsonValue.get("vertices").asFloatArray());
            fixtureDef.shape = polygonShape;
            return new OreBlueprint(oreName, oreValue, oreTemp, multiOre, fixtureDef);
        }

    }

}
