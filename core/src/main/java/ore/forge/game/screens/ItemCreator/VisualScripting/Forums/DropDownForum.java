package ore.forge.game.screens.ItemCreator.VisualScripting.Forums;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.screens.ItemCreator.VisualScripting.ForumField;

public class DropDownForum extends SelectBox<String> implements ForumField<String> {

    public DropDownForum(SelectBoxStyle style, String... options) {
        super(style);
        this.setItems(options);
        this.showScrollPane();
    }

    @Override
    public String value() {
        return getSelected();
    }

    @Override
    public String getError() {
        return "";
    }

    @Override
    public boolean isValid() {
        return getSelected() != null;
    }

    @Override
    public JsonValue.ValueType getType() {
        return JsonValue.ValueType.stringValue;
    }

}
