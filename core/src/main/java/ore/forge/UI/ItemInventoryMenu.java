package ore.forge.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import ore.forge.Items.ItemDefinition;
import ore.forge.Items.ItemRole;
import ore.forge.UI.Widgets.FilterTab;
import ore.forge.UI.Widgets.Icon;
import ore.forge.UI.Widgets.SearchBar;
import ore.forge.UI.Widgets.WidgetGrid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ItemInventoryMenu extends Table {
    private List<Icon<ItemDefinition>> allIcons; // All itemIcons
    private List<Icon<ItemDefinition>> currentIcons;
    private SearchBar searchBar;
    private FilterTab filterTab;
    private WidgetGrid iconGrid;
    private HashMap<String, List<Icon<ItemDefinition>>> filteredLists;

    public ItemInventoryMenu(List<Icon<ItemDefinition>> allIcons) {
        super();
        this.top().left();

        this.allIcons = allIcons;
        this.currentIcons = new ArrayList<>();

        // Setup our searchbar and its logic.
        searchBar = new SearchBar();
        searchBar.onKeyTyped(this::applySearch);
        searchBar.onFocusLost(() -> {
        });

        filteredLists = new HashMap<>();
        // Setup our filters tab.
        filterTab = new FilterTab(new WidgetGrid.IconGridConfigData(5, 1));
        var filterOptions = new ArrayList<FilterTab.FilterOption>();
        for (ItemRole itemRole : ItemRole.values()) {
            // Create list of all items of our current type.
            List<Icon<ItemDefinition>> category = new ArrayList<>();
            for (Icon<ItemDefinition> icon : this.allIcons) {
                ItemRole[] roles = icon.getData().type();
                if ((ItemRole.combineBits(roles) & itemRole.mask) != 0) {
                    category.add(icon);
                }
            }
            filteredLists.put(itemRole.name(), category);
            FilterTab.FilterOption filterOption = new FilterTab.FilterOption(itemRole.name(), itemRole.name());
            filterOption.setOnClicked((f) -> {
                this.updateFilters();
            });
            filterOptions.add(filterOption);
        }
        filterTab.setOptions(filterOptions);

        // Set up our sort option (a toggle(click through) or a drop-down menu)
        // Sort will sort our currentIcons.

        // finally display these icons
        iconGrid = new WidgetGrid(currentIcons, new WidgetGrid.IconGridConfigData(5, .95f));
        Table topTable = new Table();
        // topTable.debug();
        topTable.add(searchBar).top().left().grow();
        topTable.add(filterTab).top().left().grow();

        this.add(topTable).top().left().growX().row();

        this.add(iconGrid).grow().padTop(10).row();
        this.setBackground(UIHelper.getRoundFull().tint(Color.GRAY));
        // this.debugAll();
        this.debugAll();
    }

    public void updateFilters() {
        var filteredIcons = new ArrayList<Icon<ItemDefinition>>();
        for (FilterTab.FilterOption option : filterTab.getOptions()) {
            if (option.isChecked()) {
                filteredIcons.addAll(filteredLists.get(option.getFilterId()));
            }
        }

        // no filters enabled so we display all
        if (filteredIcons.isEmpty()) {
            for (var list : filteredLists.values()) {
                filteredIcons.addAll(list);
            }
        }

        currentIcons = filteredIcons;
        this.applySearch(searchBar.getText());
    }

    public void applySearch(String s) {
        System.out.println(s);
        var newIcons = new ArrayList<Icon<ItemDefinition>>();
        for (Icon<ItemDefinition> icon : allIcons) {
            if (icon.getData().name().contains(s)) {
                newIcons.add(icon);
            }
        }
        currentIcons = newIcons;
        this.sortIcons();
    }

    public void sortIcons() {
        // TODO: Apply selected sort function
        currentIcons.sort(Comparator.comparing(o -> o.getData().name())); // sorts by name
        this.iconGrid.setElements(currentIcons);
    }

}
