package ore.forge.Items.Experimental;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.CollisionRules;
import ore.forge.GameWorld;
import ore.forge.Items.AcquisitionInfo;
import ore.forge.Items.ExtendedFixtureDef;
import ore.forge.ReflectionLoader;
import ore.forge.Screens.CollisionBehavior;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Nathan Ulmen
 * An Item is a struct of data that encapsulates/holds all data (Physics model, Behaviors, Sounds, Materials, How its acquired, etc...) needed
 * for an Item.
 **/
public abstract class ItemBlueprint {
    private final String name, id, description;
    private final BodyDef bodyDef;
    private final AcquisitionInfo acquisitionInfo;
    private final ArrayList<ExtendedFixtureDef> fixtureDefs;


    private static final Filter FILTER = new Filter();

    static {
        FILTER.categoryBits = CollisionRules.ORE_PROCESSOR.getBit();
        FILTER.maskBits = CollisionRules.ORE.getBit();
    }


    public ItemBlueprint(JsonValue jsonValue) {
        this.name = jsonValue.getString("name");
        this.id = jsonValue.getString("id");
        this.description = jsonValue.getString("description");
        acquisitionInfo = new AcquisitionInfo(jsonValue.get("acquisitionInfo"), ore.forge.Items.Item.Tier.valueOf(jsonValue.getString("tier")));

        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.angle = 90f * MathUtils.degreesToRadians;

        fixtureDefs = loadFixtures();
    }

    public String toString() {
        return name;
    }

    private static Object extractData(Fixture fixture) {
        return fixture.getUserData();
    }

    private static Class<?> getType(Object var) {
        return var.getClass();
    }

    protected abstract ArrayList<ExtendedFixtureDef> loadFixtures();

    private static ArrayList<ExtendedFixtureDef> loadFixtures(JsonValue fixtures, HashMap<String, CollisionBehavior> behaviors) {
        ArrayList<ExtendedFixtureDef> fixtureDefs = new ArrayList<>();
        for (JsonValue jsonFixtureData : fixtures) {
            float angleOffset = jsonFixtureData.getFloat("angleOffset", 0);
            boolean collisionEnabled = jsonFixtureData.getBoolean("collisionEnabled");
            String behaviorKey = jsonFixtureData.getString("behaviorKey", "");
            ExtendedFixtureDef customFixtureDef = new ExtendedFixtureDef(angleOffset, collisionEnabled, behaviorKey);

            String type = jsonFixtureData.getString("shape");
            if (type.equals("Polygon")) {
                PolygonShape polygon = new PolygonShape();
                polygon.set(jsonFixtureData.get("vertices").asFloatArray());
                customFixtureDef.shape = polygon;
            }
            if (type.equals("Edge")) {
                EdgeShape edge = new EdgeShape();
                float[] points = jsonFixtureData.get("points").asFloatArray();
                edge.set(points[0], points[1], points[2], points[3]);
                customFixtureDef.shape = edge;
            }
            fixtureDefs.add(customFixtureDef);
        }
        return fixtureDefs;
    }

    private static HashMap<String, CollisionBehavior> loadBehaviors(JsonValue behaviors) {
        HashMap<String, CollisionBehavior> behaviorMap = new HashMap<>();
        for (JsonValue behaviorData : behaviors) {
            behaviorMap.put(behaviorData.getString("key"), ReflectionLoader.load(behaviorData, "behaviorName"));
        }
        return behaviorMap;
    }


    //TODO: NEEDS WORK
    public Body spawnItem() {
        Body body = GameWorld.getInstance().physicsWorld().createBody(bodyDef);
        for (ExtendedFixtureDef customFixtureDef : fixtureDefs) {
            var fixture = body.createFixture(customFixtureDef);
            CollisionBehavior collisionBehavior = behaviors.get(customFixtureDef.getCollisionBehaviorKey());
            if  (collisionBehavior != null) {
                collisionBehavior = collisionBehavior.clone(fixture);
            }
            fixture.setUserData(new ItemUserData(customFixtureDef.relativeAngle, collisionBehavior, body));
            fixture.setSensor(!customFixtureDef.collisionEnabled);
            fixture.setFilterData(FILTER);
        }
        return body;
    }

    public record ItemUserData(float angleOffset, CollisionBehavior collisionBehavior, Body body) {}

}
