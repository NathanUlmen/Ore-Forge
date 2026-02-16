package ore.forge.game.screens.ItemCreator.VisualScripting.Forums;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.screens.ItemCreator.VisualScripting.ForumField;

public record ConstantHiddenForum(String value) implements ForumField<String> {

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getError() {
        return "This dont matter!!";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isUiElement() {
        return false;
    }

    @Override
    public JsonValue.ValueType getType() {
        return JsonValue.ValueType.stringValue;
    }
}
