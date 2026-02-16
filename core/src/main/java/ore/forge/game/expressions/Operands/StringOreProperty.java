package ore.forge.game.expressions.Operands;

import ore.forge.game.expressions.StringOperand;
import ore.forge.game.Ore;

import java.util.function.Function;

public enum StringOreProperty implements StringOperand {
    NAME,
    ID,
    TYPE;

    public static boolean isProperty(String property) {
        return switch (property.trim()) {
            case "NAME", "ORE_NAME", "ID", "TYPE" -> true;
            default -> false;
        };
    }

    public static StringOreProperty fromString(String string) {
        return switch (string.trim()) {
            case "NAME", "ORE_NAME" -> StringOreProperty.NAME;
            case "ID" -> StringOreProperty.ID;
            case "TYPE" -> StringOreProperty.TYPE;
            default -> throw new IllegalStateException("Unexpected value: " + string.trim());
        };
    }

    private final Function<Ore, String> retriever;

    StringOreProperty() {
        retriever = switch (this) {
            case NAME -> Ore::getName;
            case ID -> Ore::getID;
            case TYPE -> (Ore ore) -> ("TYPE HAS NOT been Implemented yet!");
        };
    }

    @Override
    public String asString(Ore ore) {
        return retriever.apply(ore);
    }
}
