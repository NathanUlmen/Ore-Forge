package ore.forge.game.screens.ItemCreator.VisualScripting;

import com.badlogic.gdx.utils.JsonValue;

public interface ForumField<E> {
    E value();

    String getError();

    boolean isValid();

    default boolean isUiElement() {
        return true;
    }

    JsonValue.ValueType getType();

}
