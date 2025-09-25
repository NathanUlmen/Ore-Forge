package ore.forge.Items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.GameWorld;
import ore.forge.Items.Blocks.Block;
import ore.forge.Items.Blocks.FurnaceBlock;
import ore.forge.ReflectionLoader;
import ore.forge.Strategies.UpgradeStrategies.UpgradeStrategy;

//@author Nathan Ulmen
public class Furnace extends Item {
    private int currentProgress;
    private final int rewardThreshold;
    private final int specialPointReward;
    private final UpgradeStrategy upgrade;

    //Used to create item from scratch.
    public Furnace(String name, String description, int[][] blockLayout, Tier tier, double itemValue, float rarity, int specialPointReward, int rewardThreshold, UpgradeStrategy upgrade) {
        super(name, description, blockLayout, tier, itemValue, rarity);
        this.specialPointReward = specialPointReward;
        currentProgress = 0;
        this.rewardThreshold = rewardThreshold;
        this.upgrade = upgrade;
        initBlockConfiguration(blockLayout);
//        setTexture(new Texture(Gdx.files.internal("Furnace.png")));
    }

    public Furnace(JsonValue jsonValue, GameWorld world) {
        super(jsonValue, world);
        this.rewardThreshold = jsonValue.getInt("rewardThreshold");
        this.specialPointReward = jsonValue.getInt("specialPointReward");
        this.upgrade = ReflectionLoader.load(jsonValue.get("upgrade"), "upgradeName");
        initBlockConfiguration(this.numberConfig);
        setTexture(new Texture(Gdx.files.internal("Furnace.png")));
    }

    //Used to "clone" an item.
    public Furnace(Furnace itemToClone) {
        super(itemToClone);
        this.upgrade = itemToClone.upgrade;
        this.specialPointReward = itemToClone.specialPointReward;
        this.rewardThreshold = itemToClone.rewardThreshold;
        this.currentProgress = 0;
        initBlockConfiguration(this.numberConfig);
        alignWith(itemToClone.direction);
    }

    @Override
    public void initBlockConfiguration(int[][] numberConfig) {
        for (int i = 0; i < numberConfig.length; i++) {
            for (int j = 0; j < numberConfig[0].length; j++) {
                switch (numberConfig[i][j]) {
                    case 0:
                        blockConfig[i][j] = new Block(this);
                        break;
                    case 4:
                        blockConfig[i][j] = new FurnaceBlock(this, upgrade, specialPointReward);
                        break;
                    case 3:
                    case 2:
                    case 1:
                    default:
                        throw new IllegalArgumentException("Invalid Block Config Value: " + numberConfig[i][j]);
                }
            }
        }
    }

    public int getRewardThreshold() {
        return rewardThreshold;
    }

    public int getRewardProgess() {
        return currentProgress;
    }

    public void setProgress(int progress) {
        currentProgress = progress;
    }

    public void incrementProgress() {
        currentProgress++;
    }

    @Override
    public String toString() {
        return "Furnace{" +
            "currentProgress=" + currentProgress +
            ", rewardThreshold=" + rewardThreshold +
            ", specialPointReward=" + specialPointReward +
            ", upgrade=" + upgrade +
            '}';
    }

}
