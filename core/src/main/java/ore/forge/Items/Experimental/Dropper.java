package ore.forge.Items.Experimental;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import ore.forge.OreRealm;
import ore.forge.Strategies.DropOreBehavior;
import ore.forge.Strategies.DropperStrategies.BurstDrop;

public class Dropper extends Item {
    private BurstDrop burstDrop;
    private DropOreBehavior.OreBlueprint oreBlueprint;


    private final static BodyDef oreDef = new BodyDef();

    static {
        oreDef.type = BodyDef.BodyType.DynamicBody;
    }

    @Override
    public void update(float deltaTime) {
        if (burstDrop.drop(deltaTime)) {
           //spawn ore and give it stats
            var ore = OreRealm.getSingleton().giveOre();
            ore.applyBaseStats(oreBlueprint.oreValue(), oreBlueprint.oreTemperature(), oreBlueprint.multiOre(), oreBlueprint.name(), this.id, null);
            var body = ore.getBody();
            //Reset body so that we can apply the new one
            Array<Fixture> fixtures = body.getFixtureList();
            for (Fixture fixture : fixtures) {
                body.destroyFixture(fixture);
            }
            body.createFixture(oreBlueprint.fixtureDef());
        }
    }
}
