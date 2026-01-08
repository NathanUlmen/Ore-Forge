package ore.forge.QuestComponents.Rewards;

import com.badlogic.gdx.utils.JsonValue;

public class UnlockReward implements Reward{
//    private final static Player player = Player.getSingleton();
    private final String unlockedItemID;

    public UnlockReward(String unlockedItemID) {
        this.unlockedItemID = unlockedItemID;
    }

    public UnlockReward(JsonValue jsonValue) {
        this.unlockedItemID = jsonValue.getString("rewardID");
    }

    @Override
    public void grantReward() {
        throw new UnsupportedOperationException("Not supported yet.");
//        player.getInventory().getNode(unlockedItemID).getHeldItem().setUnlocked(true);
    }

}
