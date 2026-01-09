package ore.forge.QuestComponents.Rewards;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Currency;

public class CurrencyReward implements Reward {
//    private final ore.forge.Player.Player player = ore.forge.Player.Player.getSingleton();
    private final Currency type;
    private final double rewardAmount;

    public CurrencyReward(double rewardAmount, Currency type) {
        this.rewardAmount = rewardAmount;
        this.type = type;

    }

    public CurrencyReward(JsonValue jsonValue) {
        this.type = Currency.valueOf(jsonValue.getString("currencyType"));
        this.rewardAmount = jsonValue.getDouble("rewardCount");
    }

    @Override
    public void grantReward() {
        switch (type) {
//            case CASH -> player.addToWallet(rewardAmount);
//            case SPECIAL_POINTS -> player.addSpecialPoints((int) rewardAmount);
//            case PRESTIGE_POINTS-> player.setPrestigeCurrency((int) (player.getPrestigeCurrency() + rewardAmount));
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
