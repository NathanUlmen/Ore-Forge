package ore.forge.UI;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import ore.forge.Player.ItemInventoryNode;
import ore.forge.UI.Widgets.Icon;

import java.util.List;

public class TestInventory extends Table {

    public TestInventory(
        List<Icon<ItemInventoryNode>> allIcons,
        BuildListener buildListener
    ) {
        ItemGridPanel panel = new ItemGridPanel(
            allIcons,
            icon -> buildListener.initiateBuild(
                icon.getData().getHeldItem()
            )
        );

        add(panel).grow();
    }
}
