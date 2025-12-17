package ore.forge.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ore.forge.UIHelper;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Nathan Ulmen
 * FilterTab holds a list of checkboxes,
 * When a checkbox is toggled in its callback it will return a list of all items tied to it.
 * FilterTab will then combine all lists of all enabled checkboxes.
 *
 */
public class FilterTab extends Table {
    private WidgetGrid widgetGrid;
    private List<FilterOption> filterOptions;


    public FilterTab(List<FilterOption> filterOptions) {
        widgetGrid = new WidgetGrid();
        widgetGrid.setElements(filterOptions);
        widgetGrid.setScrollingDisabled(true, true);
        this.add(widgetGrid);
    }

    public FilterTab(FilterOption... options) {
        this(Arrays.asList(options));
    }

    public void setOptions (List<FilterOption> filterOptions) {
        this.filterOptions = filterOptions;
        widgetGrid.setElements(filterOptions);
    }

    public List<FilterOption> getOptions() {
        return filterOptions;
    }

    public static class FilterOption extends Table {
        private String filterId;
        private CheckBox checkBox;
        private Consumer<FilterOption> onClicked;
        private FilterOptionConfig config;

        public FilterOption(String filterId, String checkBoxText) {
            this.filterId = filterId;
            config = new FilterOptionConfig();
            CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle();
            style.font = UIHelper.generateFont(config.fontSize);
            style.fontColor = Color.BLACK;
            style.up = UIHelper.getRoundFull();
            style.checked = UIHelper.getRoundFull().tint(Color.FOREST);
            checkBox = new CheckBox(checkBoxText, style);
            this.setPosition(300, 300);
            this.setTouchable(Touchable.enabled);
            checkBox.setTouchable(Touchable.enabled);

            checkBox.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onClicked.accept(FilterOption.this);
                }
            });
            this.add(checkBox);
        }

        public void setOnClicked(Consumer<FilterOption> onClicked) {
            this.onClicked = onClicked;
        }

        public boolean isChecked() {
            return checkBox.isChecked();
        }

        public String getFilterId() {
            return filterId;
        }

        public static class FilterOptionConfig {
            public final int fontSize;

            public FilterOptionConfig() {
                this.fontSize = 48;
            }
        }

    }

}
