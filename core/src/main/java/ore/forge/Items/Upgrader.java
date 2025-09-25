package ore.forge.Items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.GameWorld;
import ore.forge.Items.Blocks.Block;
import ore.forge.Items.Blocks.ConveyorBlock;
import ore.forge.Items.Blocks.UpgradeBlock;
import ore.forge.ReflectionLoader;
import ore.forge.Strategies.UpgradeStrategies.UpgradeStrategy;
import ore.forge.UpgradeTag;

//@author Nathan Ulmen
public class Upgrader extends Item {

    private final UpgradeTag upgradeTag;
    private final UpgradeStrategy upgrade;
    private FixtureDef fixtureDef;

    private final float conveyorSpeed;

    //Used to create from scratch.
    public Upgrader(String name, String description, int[][] blockLayout, Tier tier, double itemValue, float rarity, float conveyorSpeed, UpgradeStrategy upgrade, UpgradeTag tag) {//Upgrader Constructor
        super(name, description, blockLayout, tier, itemValue, rarity);
        this.conveyorSpeed = conveyorSpeed;
        this.upgrade = upgrade;
        this.upgradeTag = tag;
        initBlockConfiguration(this.numberConfig);
//        setTexture(new Texture(Gdx.files.internal("Upgrader.png")));
    }

    public Upgrader(JsonValue jsonValue, GameWorld gameWorld) {
        super(jsonValue, gameWorld);
        this.conveyorSpeed = jsonValue.getFloat("conveyorSpeed");
        this.upgradeTag = new UpgradeTag(jsonValue.get("upgradeTag"));
        this.upgrade = ReflectionLoader.load(jsonValue.get("upgrade"), "upgradeName");
        setTexture(new Texture(Gdx.files.internal("Upgrader.png")));
        initBlockConfiguration(this.numberConfig);
    }

    //Used to "clone" an item.
    public Upgrader(Upgrader itemToClone) {
        super(itemToClone);
        this.conveyorSpeed = itemToClone.conveyorSpeed;
        this.upgrade = itemToClone.upgrade.cloneUpgradeStrategy();
        this.upgradeTag = itemToClone.upgradeTag;
        this.setTexture(itemToClone.getTexture());
        initBlockConfiguration(itemToClone.numberConfig);
        alignWith(itemToClone.direction);
    }

    @Override
    public void initBlockConfiguration(int[][] numberConfig) {
        for (int i = 0; i < numberConfig.length; i++) {
            for (int j = 0; j < numberConfig[0].length; j++) {
                switch (numberConfig[i][j]) {
                    case 0:
                        this.blockConfig[i][j] = new Block(this);
                        break;
                    case 1:
                        this.blockConfig[i][j] = new ConveyorBlock(this, conveyorSpeed, direction);
                        break;
                    case 2:
                        this.blockConfig[i][j] = new UpgradeBlock(this, upgrade, upgradeTag, conveyorSpeed);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid Block Config Value For item Type: " + numberConfig[i][j]);
                }
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() +
            "\n[Upgrade]: " + upgrade.toString();
    }


}
