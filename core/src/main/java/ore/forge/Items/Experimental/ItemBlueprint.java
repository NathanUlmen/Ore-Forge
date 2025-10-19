package ore.forge.Items.Experimental;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.CollisionRules;
import ore.forge.GameWorld;
import ore.forge.Items.AcquisitionInfo;
import ore.forge.Items.ExtendedFixtureDef;
import ore.forge.ReflectionLoader;
import ore.forge.Strategies.Behavior;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Nathan Ulmen
 * An Item is a struct of data that encapsulates/holds all data (Physics model, Behaviors, Sounds, Materials, How its acquired, etc...) needed
 * for an Item.
 **/
public abstract class ItemBlueprint {
    protected final String name, id, description;
    protected final BodyDef bodyDef;
    protected final AcquisitionInfo acquisitionInfo;
    protected final ArrayList<ExtendedFixtureDef> fixtureDefs;
    protected final HashMap<String, Behavior> behaviors;

    protected static final Filter FILTER = new Filter();
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

        fixtureDefs = loadFixtures(jsonValue.get("fixtures"));
        behaviors = loadBehaviors(jsonValue.get("behaviors"));

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

    private static ArrayList<ExtendedFixtureDef> loadFixtures(JsonValue fixtures) {
        ArrayList<ExtendedFixtureDef> fixtureDefs = new ArrayList<>();
        for (JsonValue jsonFixtureData : fixtures) {
            float angleOffset = jsonFixtureData.getFloat("relativeDirection", 0);
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

    private static HashMap<String, Behavior> loadBehaviors(JsonValue behaviors) {
        HashMap<String, Behavior> behaviorMap = new HashMap<>();
        for (JsonValue behaviorData : behaviors) {
            behaviorMap.put(behaviorData.getString("key"), ReflectionLoader.load(behaviorData, "behaviorName"));
        }
        return behaviorMap;
    }

    public final ItemInstance createItem() {
        Body body = GameWorld.instance().physicsWorld().createBody(bodyDef);
        body.setUserData(this);
        for (ExtendedFixtureDef customFixtureDef : fixtureDefs) {
            var fixture = body.createFixture(customFixtureDef);
            Behavior behavior = behaviors.get(customFixtureDef.getCollisionBehaviorKey());
            if  (behavior != null) {
                behavior = behavior.clone(fixture);
                behavior.attach(body, fixture);
            }
            fixture.setUserData(new ItemUserData(customFixtureDef.getRelativeAngle(), behavior, body));
            fixture.setSensor(!customFixtureDef.isCollisionEnabled());
            fixture.setFilterData(FILTER);
        }
        return new ItemInstance(this, body);
    }



}
