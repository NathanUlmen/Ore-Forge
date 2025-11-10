package ore.forge.Strategies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.EventSystem.Events.ItemRemovedGameEvent;
import ore.forge.EventSystem.GameEventListener;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Items.Experimental.UpgraderSpawner;
import ore.forge.ReflectionLoader;
import ore.forge.Strategies.DropperStrategies.DropStrategy;
import ore.forge.TimerUpdater;

public class DropOreBehavior implements Behavior, TimeUpdatable, GameEventListener<ItemRemovedGameEvent> {
    private final Vector2 spawnOffset; //Offset from item center that Ore location is set to.
    private final DropStrategy dropperStrategy;
    private Fixture fixture;
    private final OreBlueprint blueprint;
    private final static BodyDef oreDef = new BodyDef();

    static {
        oreDef.type = BodyDef.BodyType.DynamicBody;
    }

    public DropOreBehavior(JsonValue value) {
        dropperStrategy = ReflectionLoader.load(value.get("dropBehavior"), "dropBehaviorName"); //TODO: field name
        spawnOffset = ReflectionLoader.loadVector2(value.get("spawnOffset"));

        value = value.parent.parent.get("oreProperties");
        System.out.println(value);
        String oreName = value.getString("oreName");
        double oreValue = value.getDouble("oreValue");
        float oreTemperature = value.getFloat("oreTemperature");
        int multiOre = value.getInt("multiOre");
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.set(value.get("vertices").asFloatArray());
        fixtureDef.shape = shape;
        blueprint = new OreBlueprint(oreName, oreValue, oreTemperature, multiOre, fixtureDef);
    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }

    @Override
    public void attach(Body body, Fixture fixture) {
        this.fixture = fixture;
    }

    @Override
    public void attach(UpgraderSpawner spawner, btCollisionObject parent) {

    }

    @Override
    public void update(float delta) {
//        if (dropperStrategy.drop(delta)) {
//            var body = GameWorld.instance().physicsWorld().createBody(oreDef);
//            body.createFixture(blueprint.fixtureDef);
//            Ore ore = OreRealm.getSingleton().giveOre();
//            ore.applyBaseStats(blueprint.oreValue, blueprint.oreTemperature, blueprint.multiOre, blueprint.name, "TESTING", null);
//            ore.setBody(body);
//            Vector2 dropperLocation = fixture.getBody().getPosition();
//            var itemData = fixture.getUserData();
//            Vector2 finalSpawnOffset = null;
//            if (itemData instanceof ItemUserData data) {
//                finalSpawnOffset = spawnOffset.rotateDeg(fixture.getBody().getAngle() + data.direction());
//            }
//            assert finalSpawnOffset != null;
//            body.setTransform(dropperLocation.x + finalSpawnOffset.x, dropperLocation.y + finalSpawnOffset.y, fixture.getBody().getAngle());
//
//        }
    }

    @Override
    public void interact(Object subjectData, ItemUserData userData) {
        assert false;
    }

    @Override
    public Behavior clone(Fixture parent) {
        return null;
    }

    @Override
    public boolean isCollisionBehavior() {
        return false;
    }

    @Override
    public void handle(ItemRemovedGameEvent event) {
        //TODO: Might not work this way in the future once system is more concrete
        if (event.getSubject() == fixture.getBody().getUserData()) {
            TimerUpdater.unregister(this);
        }
    }

    @Override
    public Class<?> getEventType() {
        return ItemRemovedGameEvent.class;
    }

    public record OreBlueprint(String name, double oreValue, float oreTemperature, int multiOre,
                               FixtureDef fixtureDef) {

    }

}
