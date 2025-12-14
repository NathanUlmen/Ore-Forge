package ore.forge.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import ore.forge.ButtonType;
import ore.forge.UIHelper;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Nathan Ulmen
 * A SearchBar Takes a list of E elements, which are the items it will operate on
 * It provides callbacks so you can do things like: Update UI elements, etc
 *
 */
public class SearchBar<E> extends Table {
    private TextField searchField;
    private List<E> searchItems;
    private SearchBarConfigData config;

    private Runnable onSelectedCallback;
    private Runnable onClickOffCallback;
    private BiConsumer<SearchBar<E>, String> textChangedCallback;

    public SearchBar(List<E> searchItems) {
        this.searchItems = searchItems;
        config = new SearchBarConfigData();
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();

        textFieldStyle.background = UIHelper.getRoundFull().tint(Color.BLACK);
        textFieldStyle.cursor = UIHelper.getButton(ButtonType.ROUND_FULL_128).tint(Color.BLACK);
        textFieldStyle.font = UIHelper.generateFont(config.fontSize);
        searchField = new TextField("Search...", textFieldStyle);

        //Text Typed Callback
        searchField.setTextFieldListener((textField, c) -> {
            if (textChangedCallback != null) {
                textChangedCallback.accept(this, textField.getText());
            }
        });

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
    }

    public void setSearchItems(List<E> searchItems) {
        this.searchItems = searchItems;
    }

    /**
     * Called when the search field is clicked on
     */
    public void onSelected(Runnable callback) {
        this.onSelectedCallback = callback;
    }

    /**
     * Called when the search bar is no longer in "focus"
     *
     */
    public void onClickOff(Runnable callback) {
        this.onClickOffCallback = callback;
    }

    /**
     * Called when the text of the searchbar is mutated.
     */
    public void onKeyTyped(BiConsumer<SearchBar<E>, String> callback) {
        this.textChangedCallback = callback;
    }

    public static class SearchBarConfigData {
        public final int fontSize;

        public SearchBarConfigData() {
            this.fontSize = 48;
        }
    }

}
