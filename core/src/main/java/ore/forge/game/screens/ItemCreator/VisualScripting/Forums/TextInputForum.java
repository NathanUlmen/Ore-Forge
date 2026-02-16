package ore.forge.game.screens.ItemCreator.VisualScripting.Forums;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.screens.ItemCreator.VisualScripting.ForumField;
import ore.forge.game.ui.UIHelper;

public class TextInputForum extends TextField implements ForumField<String> {

    public TextInputForum(String text) {
        super(text, defaultStyle());
    }

    public TextInputForum(String string, TextField.TextFieldStyle style) {
        super(string, style);
    }

    @Override
    public String value() {
        return this.getText();
    }

    @Override
    public String getError() {
        return "";
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public JsonValue.ValueType getType() {
        return JsonValue.ValueType.stringValue;
    }

    private static TextFieldStyle defaultStyle() {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = UIHelper.generateFont(16);
        style.fontColor = Color.BLACK;
        style.background = UIHelper.getRoundFull().tint(Color.WHITE);
//        style.cursor = UIHelper.getRoundFull().tint(Color.BLACK);
        return style;
    }

}
