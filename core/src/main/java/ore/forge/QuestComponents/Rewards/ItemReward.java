package ore.forge.QuestComponents.Rewards;

import com.badlogic.gdx.utils.JsonValue;

public class ItemReward implements Reward {
//    private final ore.forge.Player.Player player = ore.forge.Player.Player.getSingleton();
    private final String itemID;
    private final int count;

    public ItemReward(String reward, int count) {
        this.itemID = reward;
        this.count = count;
    }

    public ItemReward(JsonValue jsonValue) {
        this.itemID = jsonValue.getString("rewardID");
        this.count = jsonValue.getInt("rewardCount");
    }

    @Override
    public void grantReward() {
//        player.getInventory().addItem(itemID, count);
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
