package ore.forge.UI;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.List;

public class IconGrid<E> extends ScrollPane {
    private List<Icon<E>> icons;
    private IconGridConfigData configData;
    private final Table table; //holds all our icons

    public IconGrid(List<Icon<E>> icons) {
        super(new Table());
        table = (Table) this.getActor();
        this.icons = icons;
        this.configData = new IconGridConfigData();
        setScrollingDisabled(true, false);
        setSize(configData.width, configData.height);
        buildLayout();
        this.setDebug(true, true);
    }

    public void setIcons(List<Icon<E>> icons) {
        this.icons = icons;
        buildLayout();
    }

    public void buildLayout() {
        table.clear();
        int count = 0;
        for (Icon<E> icon : icons) {
            icon.setSize(configData.iconWidth, configData.iconHeight);
            if (count != 0 & count % configData.numColumns == 0) {
                table.row();
            }
            table.add(icon).size(configData.iconWidth, configData.iconHeight)
                .pad(configData.iconPadding);
            count++;
        }
    }

    public static class IconGridConfigData {
        public final int numColumns;
        public final float borderPadding;
        public final float iconPadding;
        public final float width, height;
        public final float iconWidth, iconHeight;

        public IconGridConfigData() {
            this.numColumns = 4;
            this.iconPadding = 10f;
            this.borderPadding = 20f;
            this.width = 1024;
            this.height = 1024;
            int totalPadding = (int) ((iconPadding * numColumns * 2) + (borderPadding * 2));
            iconWidth = (width - totalPadding) / numColumns;
            iconHeight = iconWidth;
        }
    }
}
