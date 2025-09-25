package ore.forge.Items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.GameWorld;
import ore.forge.Items.Blocks.Block;
import ore.forge.Items.Blocks.ConveyorBlock;

//@author Nathan Ulmen
public class Conveyor extends Item {
    private final float conveyorSpeed;

    //Used to create from scratch
    public Conveyor(String name, String description, int[][] blockLayout, Tier tier, double itemValue, float rarity, float speed) {
        super(name, description, blockLayout, tier, itemValue, rarity);
        conveyorSpeed = speed;
        initBlockConfiguration(blockLayout);
//        setTexture(new Texture(Gdx.files.internal("BasicConveyor.png")));
    }

    //Used to create from json data.
    public Conveyor(JsonValue jsonValue, GameWorld world) {
        super(jsonValue, world);
        this.conveyorSpeed = jsonValue.getFloat("conveyorSpeed");
        initBlockConfiguration(this.numberConfig);
        setTexture(new Texture(Gdx.files.internal("BasicConveyor.png")));

    }

    //Used to "clone" an item.
    public Conveyor(Conveyor itemToClone) {
        super(itemToClone);
        this.conveyorSpeed = itemToClone.conveyorSpeed;
        initBlockConfiguration(itemToClone.numberConfig);
        alignWith(itemToClone.direction);

    }

    //Sets all blocks in item to not be full.
    public void update() {
        for (Block[] blocks : blockConfig) {
            for (Block block : blocks) {
                block.setFull(false);
            }
        }
    }

    //Configures the blocks in item based on the number Config the object was given.
    @Override
    public void initBlockConfiguration(int[][] numberConfig) {
        for (int i = 0; i < numberConfig.length; i++) {
            for (int j = 0; j < numberConfig[0].length; j++) {
                if (numberConfig[i][j] == 1) {
                    this.blockConfig[i][j] = new ConveyorBlock(this, conveyorSpeed, direction);
                } else {
                    throw new IllegalArgumentException("Invalid Block Config Value For item Type: " + numberConfig[i][j]);
                }
            }
        }
    }

    public float getConveyorSpeed() {
        return conveyorSpeed;
    }

}
