package ore.forge.game.ui.Widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import ore.forge.game.ui.ButtonType;
import ore.forge.game.ui.UIHelper;

import java.util.function.Consumer;

/**
 * @author Nathan Ulmen
 * A SearchBar Takes a list of E elements, which are the items it will operate on
 * It provides callbacks so you can do things like: Update UI elements, etc
 *
 */
public class SearchBar extends Table {
    private TextField searchField;
    private SearchBarConfigData config;

    private Runnable onSelectedCallback;
    private Runnable onClickOffCallback;
    private Consumer<String> textChangedCallback;

    public SearchBar() {
        config = new SearchBarConfigData();
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();

        textFieldStyle.background = UIHelper.getRoundFull().tint(Color.WHITE);
        textFieldStyle.cursor = UIHelper.getButton(ButtonType.ROUND_FULL_128).tint(Color.BLACK);
        textFieldStyle.font = UIHelper.generateFont(config.fontSize);
        textFieldStyle.fontColor = Color.BLACK;
        searchField = new TextField("", textFieldStyle);
        searchField.setMessageText("Search...");
        searchField.setFocusTraversal(true);

        //focus listeners
        searchField.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (onSelectedCallback != null) onSelectedCallback.run();
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!searchField.hasKeyboardFocus() && onClickOffCallback != null) {
                    onClickOffCallback.run();
                }
            }
        });
        this.pad(10);
        this.setBackground(UIHelper.getRoundFull().tint(Color.BLACK));
        this.add(searchField).fill().expand();
        this.debug();
    }


    /**
     * Called when the search field is clicked on
     */
    public void onSearchFocused(Runnable callback) {
        this.onSelectedCallback = callback;
    }

    /**
     * Called when the search bar is no longer in "focus"
     *
     */
    public void onFocusLost(Runnable callback) {
        this.onClickOffCallback = callback;
    }

    /**
     * Called when the text of the searchbar is mutated.
     */
    public void onKeyTyped(Consumer<String> callback) {
        this.textChangedCallback = callback;
        searchField.setTextFieldListener((textField, c) -> {

            if (textChangedCallback != null) {
                textChangedCallback.accept(textField.getText());
            }
        });
    }

    public void clearFocus() {
        searchField.clear();
    }

    public String getText() {
        return searchField.getText();
    }

    public static class SearchBarConfigData {
        public final int fontSize;

        public SearchBarConfigData() {
            this.fontSize = 48;
        }
    }

}
