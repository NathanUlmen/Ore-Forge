package ore.forge.Items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Expressions.Function;
import ore.forge.Expressions.Operands.NumericOreProperties;
import ore.forge.Expressions.Operators.NumericOperator;
import ore.forge.GameWorld;
import ore.forge.Items.Blocks.Block;
import ore.forge.Items.Blocks.DropperBlock;
import ore.forge.ReflectionLoader;
import ore.forge.Stopwatch;
import ore.forge.Strategies.DropperStrategies.BurstDrop;
import ore.forge.Strategies.OreEffects.OreEffect;
import ore.forge.Strategies.OreEffects.UpgradeOreEffect;
import ore.forge.Strategies.UpgradeStrategies.BasicUpgrade;
import ore.forge.Strategies.UpgradeStrategies.InfluencedUpgrade;

import java.util.concurrent.TimeUnit;

//@author Nathan Ulmen
public class Dropper extends Item {
    protected final float ejectionSpeed = 6f;
    private final BurstDrop dropBehavior;
    protected final String oreName;
    protected final double oreValue;
    protected final int oreTemp, multiOre;
    private int totalOreDropped;
    public OreEffect testEffect = new UpgradeOreEffect(0.1f, 30, new InfluencedUpgrade(Function.parseFunction("ACTIVE_ORE.GET_COUNT(760-pfWQURud)"), new BasicUpgrade(0, NumericOperator.ADD, NumericOreProperties.ORE_VALUE), -9999999, 999999));
    protected final OreEffect oreEffect; //Effect that the dropper will apply when creating ore
    protected final Stopwatch stopwatch = new Stopwatch(TimeUnit.SECONDS);

    //Used to create from scratch.
    public Dropper(String name, String description, int[][] blockLayout, Tier tier, double itemValue, float rarity, String oreName, double oreVal, int oreTemp, int multiOre, OreEffect oreStrategies) {
        super(name, description, blockLayout, tier, itemValue, rarity);
        dropBehavior = new BurstDrop(450, 3);
        this.oreName = oreName;
        this.oreValue = oreVal;
        this.oreTemp = oreTemp;
        this.multiOre = multiOre;
        this.oreEffect = oreStrategies;
        totalOreDropped = 0;

        initBlockConfiguration(blockLayout);
//        setTexture(new Texture(Gdx.files.internal("Dropper.png")));
    }

    public Dropper(JsonValue jsonValue, GameWorld world) {
        super(jsonValue, world);
        this.oreName = jsonValue.getString("oreName");
        this.oreValue = jsonValue.getDouble("oreValue");
        this.oreTemp = jsonValue.getInt("oreTemp");
        this.multiOre = jsonValue.getInt("multiOre");

        if (jsonValue.has("oreStrategy") && !jsonValue.get("oreStrategy").isNull()) {
            this.oreEffect = ReflectionLoader.load(jsonValue.get("oreStrategy"), "effectName");
        } else {
            this.oreEffect = null;
        }

        this.dropBehavior = new BurstDrop(jsonValue);


        initBlockConfiguration(this.numberConfig);
        setTexture(new Texture(Gdx.files.internal("Dropper.png")));
    }

    //Used to "clone" an item.
    public Dropper(Dropper itemToClone) {
        super(itemToClone);
        this.oreName = itemToClone.oreName;
        this.oreValue = itemToClone.oreValue;
        this.oreTemp = itemToClone.oreTemp;
        this.multiOre = itemToClone.multiOre;
        this.oreEffect = itemToClone.oreEffect;
        this.dropBehavior = new BurstDrop(itemToClone.dropBehavior);
        initBlockConfiguration(this.numberConfig);
        alignWith(itemToClone.direction);
    }

    //Check to see if we should produce an ore.
    public void update(float deltaTime) {
        if (!stopwatch.isRunning()) {
            stopwatch.start();
        }
//        timeSinceLast += deltaTime;

        if (dropBehavior.drop(deltaTime)) {
            dropOre();
        }
//        if (stopwatch.getTimeStamp() == 60) {
//            Gdx.app.log("ORE PER MINUTE", "Total Ore Dropped " + totalOreDropped + "\tTimer:" + stopwatch);
//        }
    }

    private void dropOre() {
        for (Block[] blocks : blockConfig) {
            for (Block block : blocks) {
                if (block instanceof DropperBlock) {
                    ((DropperBlock) block).dropOre();
                }
            }
        }
    }


    @Override
    public void initBlockConfiguration(int[][] numberConfig) {
        for (int i = 0; i < numberConfig.length; i++) {
            for (int j = 0; j < numberConfig[0].length; j++) {
                switch (numberConfig[i][j]) {
                    case 0:
                        blockConfig[i][j] = new Block(this);
                        break;
                    case 3:
                        blockConfig[i][j] = new DropperBlock(this, oreName, oreValue, oreTemp, multiOre, ejectionSpeed, oreEffect);
                        break;
                    case 1:
                    case 4:
                    case 2:
                    default:
                        throw new IllegalArgumentException("Invalid Block Config Value: " + numberConfig[i][j]);
                }
            }
        }
    }

    public String getOreName() {
        return oreName;
    }

    public double getOreValue() {
        return oreValue;
    }

    public int getOreTemp() {
        return oreTemp;
    }

    public int getMultiOre() {
        return multiOre;
    }

    public OreEffect getOreEffects() {
        return oreEffect;
    }

    public void incrementTotalOreDropped() {
        totalOreDropped++;
    }

    public int getTotalOreDropped() {
        return totalOreDropped;
    }

}
