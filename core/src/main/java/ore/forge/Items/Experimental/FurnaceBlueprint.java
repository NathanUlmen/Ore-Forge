package ore.forge.Items.Experimental;

import com.badlogic.gdx.utils.JsonValue;

public class FurnaceBlueprint extends ItemBlueprint {
    private final int spRewardThreshold, spRewardAmount;

    public FurnaceBlueprint(JsonValue jsonValue) {
        super(jsonValue);
//        spRewardThreshold = jsonValue.getInt("spRewardThreshold");
        spRewardThreshold = 1;
//        spRewardAmount = jsonValue.getInt("spRewardAmount");
        spRewardAmount = 1;
    }

    public int getSpRewardThreshold() {
        return spRewardThreshold;
    }

    public int getSpRewardAmount() {
        return spRewardAmount;
    }

}
