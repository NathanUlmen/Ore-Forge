package ore.forge.UI;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import ore.forge.Items.Experimental.ItemBlueprint;

import java.util.List;

public class InventoryTable extends Table {
    private List<Icon<ItemBlueprint>> itemIcons; //All itemIcons
    private IconGrid<ItemBlueprint> iconGrid; //Will hold our current active icons
    private SearchBar<Icon<ItemBlueprint>> searchBar;

    public InventoryTable(List<Icon<ItemBlueprint>> itemIcons) {
        this.itemIcons = itemIcons;
        iconGrid = new IconGrid<>(itemIcons);
        searchBar = new SearchBar<>(itemIcons); //Set up our initial search space.


    }

}
