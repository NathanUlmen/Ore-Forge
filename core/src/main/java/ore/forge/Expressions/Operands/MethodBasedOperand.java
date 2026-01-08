package ore.forge.Expressions.Operands;

import ore.forge.Ore;
import ore.forge.OreRealm;

/*
 * Basically We are going to have an object that contains a custom lambda/function, a "collection", and a search param(string)
 * */
public enum MethodBasedOperand {
    UPGRADE_TAGS,
//    BASE,
//    INVENTORY,
    ACTIVE_ORE;


    private interface BooleanRetriever {
        boolean retrieve(Ore ore, String id);
    }

    private interface ValueRetriever {
        double retrieve(Ore ore, String id);
    }

    public static boolean isCollection(String token) {
        return switch (token) {
            case "UPGRADE_TAGS", "BASE", "INVENTORY", "ACTIVE_ORE" -> true;
            default -> false;
        };
    }

    public static boolean methodIsValid(String token) { return token.equals("CONTAINS") || token.equals("GET_COUNT"); }

    public double calculate(Ore ore, String id) {
        return valueRetriever.retrieve(ore, id);
    }

    public boolean evaluate(Ore ore, String id) {
        return booleanRetriever.retrieve(ore, id);
    }

    private final OreRealm oreRealm = OreRealm.getSingleton();
    private final BooleanRetriever booleanRetriever;
    private final ValueRetriever valueRetriever;

    MethodBasedOperand() {
        booleanRetriever = switch (this) {
            case UPGRADE_TAGS -> Ore::containsTag;
//            case BASE -> (Ore ore, String id) -> itemMap.containsItem(id);
//            case INVENTORY -> (Ore ore, String id) -> {
//                var node = player.getInventory().getNode(id);
//                return node != null && node.getTotalOwned() > 0;
//            };
            case ACTIVE_ORE -> (Ore ore, String id) -> oreRealm.containsOre(id);
        };
        valueRetriever = switch (this) {
            case UPGRADE_TAGS -> Ore::tagUpgradeCount;
//            case BASE -> (Ore ore, String id) -> {
//                var node = player.getInventory().getNode(id);
//                return node != null ? node.getPlaced() : 0;
//            };
//            case INVENTORY -> (Ore ore, String id) -> {
//                var node = player.getInventory().getNode(id);
//                return node != null ? node.getTotalOwned() : 0;
//            };
            case ACTIVE_ORE -> (Ore ore, String id) -> oreRealm.getOreCount(id);
        };

    }

}
