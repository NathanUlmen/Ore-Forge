package ore.forge.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import ore.forge.Items.Experimental.ItemSpawner;
import ore.forge.UIHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ItemInventoryMenu extends Table {
    private List<Icon<ItemSpawner>> allIcons; //All itemIcons
    private List<Icon<ItemSpawner>> currentIcons;
    private SearchBar searchBar;
    private FilterTab filterTab;
    private WidgetGrid iconGrid;
    private HashMap<String, List<Icon<ItemSpawner>>> filteredLists;

    public ItemInventoryMenu(List<Icon<ItemSpawner>> allItems) {
        super();
        this.top().left();

        this.allIcons = allItems;
        this.currentIcons = new ArrayList<>();

        //Setup our searchbar and its logic.
        searchBar = new SearchBar();
        searchBar.onKeyTyped(this::applySearch);
        searchBar.onFocusLost(() -> {
        });

        filteredLists = new HashMap<>();
        //Setup our filters tab.
        filterTab = new FilterTab(new WidgetGrid.IconGridConfigData(5, 1));
        var filterOptions = new ArrayList<FilterTab.FilterOption>();
        for (ItemSpawner.Type itemType : ItemSpawner.Type.values()) {
            //Create list of all items of our current type.
            List<Icon<ItemSpawner>> category = new ArrayList<>();
            for (Icon<ItemSpawner> icon : allIcons) {
                if (icon.getData().type().equals(itemType)) {
                    category.add(icon);
                }
            }
            filteredLists.put(itemType.name(), category);
            FilterTab.FilterOption filterOption = new FilterTab.FilterOption(itemType.name(), itemType.name());
            filterOption.setOnClicked((f) -> {
                this.updateFilters();
            });
            filterOptions.add(filterOption);
        }
        filterTab.setOptions(filterOptions);

        //Set up our sort option (a toggle(click through) or a drop-down menu)
        //Sort will sort our currentIcons.

        //finally display these icons
        iconGrid = new WidgetGrid(currentIcons, new WidgetGrid.IconGridConfigData(5, .95f));
        Table topTable = new Table();
//        topTable.debug();
        topTable.add(searchBar).top().left().grow();
        topTable.add(filterTab).top().left().grow();

        this.add(topTable).top().left().growX().row();

        this.add(iconGrid).grow().padTop(10).row();
        this.setBackground(UIHelper.getRoundFull().tint(Color.GRAY));
//        this.debugAll();
    }

    public void updateFilters() {
        var filteredIcons = new ArrayList<Icon<ItemSpawner>>();
        for (FilterTab.FilterOption option : filterTab.getOptions()) {
            if (option.isChecked()) {
                filteredIcons.addAll(filteredLists.get(option.getFilterId()));
            }
        }

        //no filters enabled so we display all
        if (filteredIcons.isEmpty()) {
            for (var list : filteredLists.values()) {
                filteredIcons.addAll(list);
            }
        }

        currentIcons = filteredIcons;
        this.applySearch(searchBar.getText());
    }

    public void applySearch(String s) {
        var newIcons = new ArrayList<Icon<ItemSpawner>>();
        for (Icon<ItemSpawner> icon : allIcons) {
            if (icon.getData().name().contains(s)) {
                newIcons.add(icon);
            }
        }
        currentIcons = newIcons;
        this.sortIcons();
    }

    public void sortIcons() {
        //TODO: Apply selected sort function
        currentIcons.sort(Comparator.comparing(o -> o.getData().name())); //sorts by name
        this.iconGrid.setElements(currentIcons);
    }

}
