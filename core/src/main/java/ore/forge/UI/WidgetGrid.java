package ore.forge.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.List;

public class WidgetGrid extends ScrollPane {
    private IconGridConfigData configData;
    private final Table table; //holds all our icons

    public WidgetGrid() {
        this(null);
    }


    public WidgetGrid(List<? extends Actor> icons) {
        super(new Table());
        table = (Table) this.getActor();
        table.top().left(); //Set so it builds from top left
        this.configData = new IconGridConfigData();
        setScrollingDisabled(true, false);
        setSize(configData.width, configData.height);
        this.setDebug(true, true);
    }

    public void setElements(List<? extends Actor> icons) {
        table.clear();
        int count = 0;
        for (Actor icon : icons) {
            icon.setSize(configData.iconWidth, configData.iconHeight);
            if (count != 0 && count % configData.numColumns == 0) {
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
            this.numColumns = 6;
            this.iconPadding = 10f;
            this.borderPadding = 20f;
            this.width = Gdx.graphics.getWidth() * .4f;
            this.height = Gdx.graphics.getHeight() * .8f;
            int totalPadding = (int) ((iconPadding * numColumns * 2) + (borderPadding * 2));
            iconWidth = (width - totalPadding) / numColumns;
            iconHeight = iconWidth;
        }
    }
}
