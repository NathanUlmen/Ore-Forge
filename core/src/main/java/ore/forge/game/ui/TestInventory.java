package ore.forge.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import ore.forge.game.player.ItemInventoryNode;
import ore.forge.game.ui.Widgets.Icon;

import java.util.List;

public class TestInventory extends Table {
    private final List<Icon<ItemInventoryNode>> icons;
    private ItemGridPanel panel;

    public TestInventory(
        List<Icon<ItemInventoryNode>> allIcons
    ) {
        icons = allIcons;
    }

    public void setBuildListener(BuildListener buildListener) {
        panel = new ItemGridPanel(
            icons,
            icon -> buildListener.initiateBuild(
                icon.getData().getHeldItem()
            )
        );
        add(panel).grow();
    }

}
