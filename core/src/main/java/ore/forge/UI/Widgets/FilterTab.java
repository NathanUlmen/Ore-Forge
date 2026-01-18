package ore.forge.UI.Widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ore.forge.UI.UIHelper;

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
    private List<FilterOption> filterOptions;


    public FilterTab(WidgetGrid.IconGridConfigData configData) {
        this(null, configData);
    }

    public FilterTab(List<FilterOption> filterOptions, WidgetGrid.IconGridConfigData configData) {
        this.filterOptions = filterOptions;
        if (filterOptions != null) {
            setOptions(filterOptions);
        }
        this.add();
    }

    public FilterTab(FilterOption... options) {
        this(Arrays.asList(options), new WidgetGrid.IconGridConfigData());
    }

    public void setOptions (List<FilterOption> filterOptions) {
        this.filterOptions = filterOptions;
        for (FilterOption filterOption : filterOptions) {
            this.add(filterOption).grow().pad(1f);
        }
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
            super();
            this.background(UIHelper.getRoundFull().tint(Color.BLACK));
            config = new FilterOptionConfig();
            this.pad(config.padPercentage);
            this.filterId = filterId;

            CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle();
            style.font = UIHelper.generateFont(config.fontSize);
            style.fontColor = Color.BLACK;
            style.up = UIHelper.getRoundFull();
            style.checked = UIHelper.getRoundFull().tint(Color.FOREST);
            checkBox = new CheckBox(checkBoxText, style);
            this.setTouchable(Touchable.enabled);
            checkBox.setTouchable(Touchable.enabled);

            checkBox.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onClicked.accept(FilterOption.this);
                }
            });
            this.add(checkBox).growX();
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
            public final Value padPercentage;

            public FilterOptionConfig() {
                this.fontSize = 28;
                padPercentage = Value.percentHeight(.1f);
            }
        }

    }

}
