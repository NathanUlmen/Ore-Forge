package ore.forge.game.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ore.forge.game.items.ItemRole;
import ore.forge.game.player.ItemInventoryNode;
import ore.forge.game.ui.Widgets.FilterTab;
import ore.forge.game.ui.Widgets.Icon;
import ore.forge.game.ui.Widgets.SearchBar;
import ore.forge.game.ui.Widgets.WidgetGrid;

import java.util.*;
import java.util.function.Consumer;

public class ItemGridPanel extends Table {

    protected final List<Icon<ItemInventoryNode>> allIcons;
    protected List<Icon<ItemInventoryNode>> currentIcons = new ArrayList<>();
    protected final Map<String, List<Icon<ItemInventoryNode>>> filteredLists = new HashMap<>();

    protected final SearchBar searchBar;
    protected final FilterTab filterTab;
    protected final WidgetGrid iconGrid;

    public ItemGridPanel(
        List<Icon<ItemInventoryNode>> allIcons,
        Consumer<Icon<ItemInventoryNode>> onIconClicked
    ) {
        this.allIcons = allIcons;
        top().left();

        searchBar = new SearchBar();
        searchBar.onKeyTyped(this::applySearch);

        filterTab = new FilterTab(new WidgetGrid.IconGridConfigData(5, 1));
        var filterOptions = new ArrayList<FilterTab.FilterOption>();
        for (ItemRole role : ItemRole.values()) {
            List<Icon<ItemInventoryNode>> category = new ArrayList<>();
            for (Icon<ItemInventoryNode> icon : allIcons) {
                ItemRole[] roles = icon.getData().getHeldItem().type();
                if ((ItemRole.combineBits(roles) & role.mask) != 0) {
                    category.add(icon);
                    icon.setEventListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            onIconClicked.accept(icon);
                        }
                    });
                }
            }
            filteredLists.put(role.name(), category);
            FilterTab.FilterOption option = new FilterTab.FilterOption(role.name(), role.name());
            option.setOnClicked((f) -> {
                this.updateFilters();
            });
            filterOptions.add(option);
        }

        filterTab.setOptions(filterOptions);
        iconGrid = new WidgetGrid(currentIcons, new WidgetGrid.IconGridConfigData(5, .95f));

        Table top = new Table();
        top.add(searchBar).growX();
        top.add(filterTab).growX();

        add(top).growX().row();
        add(iconGrid).grow().padTop(10);
    }

    protected void updateFilters() {
        var filteredIcons = new ArrayList<Icon<ItemInventoryNode>>();
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

    protected void applySearch(String s) {
        String target =  s.toLowerCase();
        var newIcons = new ArrayList<Icon<ItemInventoryNode>>();
        for (Icon<ItemInventoryNode> icon : allIcons) {
            if (icon.getData().name().toLowerCase().contains(target)) {
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

