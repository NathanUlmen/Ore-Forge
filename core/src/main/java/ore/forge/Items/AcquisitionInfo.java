package ore.forge.Items;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Currency;

public record AcquisitionInfo(
    Tier tier,
    UnlockMethod unlockMethod,
    double unlockRequirement,
    boolean isUnlocked,
    boolean canBeSold,
    boolean isPrestigeProof,
    Currency currency,
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

    private static Currency computeCurrency(Tier tier) {
        return switch (tier) {
            case PINNACLE -> Currency.NONE;
            case EXOTIC, SPECIAL -> Currency.SPECIAL_POINTS;
            case PRESTIGE -> Currency.PRESTIGE_POINTS;
            case EPIC, SUPER_RARE, RARE, UNCOMMON, COMMON -> Currency.CASH;
        };
    }
}

