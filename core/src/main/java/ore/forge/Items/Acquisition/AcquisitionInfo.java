package ore.forge.Items.Acquisition;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.CurrencyType;
import ore.forge.Items.Tier;
import ore.forge.Items.UnlockMethod;

public record AcquisitionInfo(
    Tier tier,
    UnlockMethod unlockMethod,
    double unlockRequirement,
    boolean unlockedByDefault, //whether or not unlocked by default.
    boolean canBeSold,
    boolean isPrestigeProof,
    CurrencyType currencyType,
    double itemValue,
    double sellPrice,
    float rarity
) {

    public AcquisitionInfo(JsonValue json, Tier tier) {
        this(
            tier,
            computeUnlockMethod(json, tier),
            computeUnlockRequirement(json, tier),
            computeIsUnlocked(json, tier),
            computeCanBeSold(tier),
            computeIsPrestigeProof(tier),
            computeCurrency(tier),
            json.getDouble("itemValue", Double.NaN),
            json.getDouble("sellPrice", Double.NaN),
            json.getFloat("rarity", Float.NaN)
        );
    }

    private static UnlockMethod computeUnlockMethod(JsonValue json, Tier tier) {
        return switch (tier) {
            case PINNACLE, EXOTIC -> UnlockMethod.QUEST;
            case PRESTIGE, SPECIAL, EPIC, SUPER_RARE, RARE, UNCOMMON, COMMON ->
                UnlockMethod.valueOf(json.getString("unlockMethod", "NONE"));
        };
    }

    private static double computeUnlockRequirement(JsonValue json, Tier tier) {
        String methodStr = json.getString("unlockMethod", "NONE");
        if (tier == Tier.PRESTIGE || tier == Tier.SPECIAL) {
            if (methodStr.equals("PRESTIGE_LEVEL") || methodStr.equals("SPECIAL_POINTS")) {
                return json.getDouble("unlockRequirement", Double.NaN);
            }
        }
        return -1;
    }

    private static boolean computeIsUnlocked(JsonValue json, Tier tier) {
        UnlockMethod method = computeUnlockMethod(json, tier);
        return method == UnlockMethod.NONE;
    }

    private static boolean computeCanBeSold(Tier tier) {
        return switch (tier) {
            case PRESTIGE, SPECIAL, EPIC, SUPER_RARE, RARE, UNCOMMON, COMMON -> true;
            default -> false;
        };
    }

    private static boolean computeIsPrestigeProof(Tier tier) {
        return switch (tier) {
            case PINNACLE, EXOTIC, PRESTIGE, SPECIAL -> true;
            default -> false;
        };
    }

    private static CurrencyType computeCurrency(Tier tier) {
        return switch (tier) {
            case PINNACLE -> CurrencyType.NONE;
            case EXOTIC, SPECIAL -> CurrencyType.SPECIAL_POINTS;
            case PRESTIGE -> CurrencyType.PRESTIGE_POINTS;
            case EPIC, SUPER_RARE, RARE, UNCOMMON, COMMON -> CurrencyType.CASH;
        };
    }
}

