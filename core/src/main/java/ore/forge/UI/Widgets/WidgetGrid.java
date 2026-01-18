package ore.forge.UI.Widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import ore.forge.UI.UIHelper;

import java.util.List;

/**@author Nathan Ulmen
 * */
//TODO:
public class WidgetGrid extends ScrollPane {
    private IconGridConfigData config;
    private final Table table; //holds all our icons

    public WidgetGrid(IconGridConfigData configData) {
        this(null, configData);
    }

    public WidgetGrid(List<? extends Actor> icons) {
        this(icons, new IconGridConfigData());
    }

    public WidgetGrid() {
        this(null, new IconGridConfigData());
    }

    public WidgetGrid(List<? extends Actor> icons, IconGridConfigData configData) {
        super(new Table());
        table = (Table) this.getActor();
        table.setBackground(UIHelper.getRoundFull().tint(Color.LIGHT_GRAY));
        table.pad(10);

//        table.defaults().expandX().fillX();
        table.top().left(); //Set so it builds from top left
        this.config = configData;
        setScrollingDisabled(true, false);
        if (icons != null) {
            setElements(icons);
        }

    }

    public void setElements(List<? extends Actor> icons) {
        table.clear();
        int count = 0;
        for (Actor icon : icons) {
            if (count != 0 && count % config.numColumns == 0) {
                table.row();
            }
            table.add(icon).top().left()
                .pad(config.iconPadding);
            count++;
        }
        this.invalidateHierarchy();
    }

    @Override
    public void layout() {
        super.layout();

        float availableWidth = getWidth()
                - table.getPadLeft()
                - table.getPadRight();

        float totalPadding = config.numColumns * config.iconPadding * 2;
        float iconWidth =
            (availableWidth - totalPadding) / config.numColumns;

        float iconHeight = iconWidth / config.aspectRatio;

        for (Cell<?> cell : table.getCells()) {
            cell.size(iconWidth, iconHeight);
//            cell.growX();
        }

        table.invalidate();
    }

    public static class IconGridConfigData {
        public final int numColumns;
        public final float borderPadding;
        public final float iconPadding;
        public final float aspectRatio; // width / height

        public IconGridConfigData() {
            this(6, 1);
        }

        public IconGridConfigData(int numColumns) {
            this(numColumns, 1);
        }

        public IconGridConfigData(float aspectRatio) {
            this(6, aspectRatio);
        }

        public IconGridConfigData(int numColumns, float aspectRatio) {
            this.numColumns = numColumns;
            this.iconPadding = 10f;
            this.borderPadding = 20f;
            this.aspectRatio = aspectRatio;
        }

    }
}
