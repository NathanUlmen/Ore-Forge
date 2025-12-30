package ore.forge.UI;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import ore.forge.VisualComponent;
import ore.forge.Input3D.OpenedMenuState;
import ore.forge.Items.Experimental.ItemSpawner;

public class UI extends Stage {
    private ItemInventoryMenu inventoryMenu;
    private TextureAtlas iconAtlas;

    public UI(List<ItemSpawner> allItems) {
        super();
        // Create our texture atlas of icon images
        IconRenderer iconRenderer = new IconRenderer();
        for (ItemSpawner item : allItems) {
            VisualComponent vc = item.createVisualComponent();
            iconRenderer.renderIcon(item.id(), vc);
            vc.dispose();
        }
        iconAtlas = iconRenderer.buildAtlas();

        //Create our Icons for Inventory
        List<Icon<ItemSpawner>> allIcons = new ArrayList<>();
        for (ItemSpawner item : allItems) {
            System.out.println("Loop Ran!");
            AtlasRegion region = iconAtlas.findRegion(item.id());
            allIcons.add(new Icon<>(region, item));
        }
        System.out.println(allIcons.size());
        inventoryMenu = new ItemInventoryMenu(allIcons);
        inventoryMenu.setSize(Gdx.graphics.getWidth() * 0.76f, Gdx.graphics.getHeight() * .8f);
        inventoryMenu.setVisible(false);


        this.addActor(inventoryMenu);
    }

    public void toggleMenu(UIMenu menu) {
        switch (menu) {
            case INVENTORY -> {
                toggleInventory();
            }
            case SHOP -> {
                toggleShop();
            }
            case QUEST -> {
                toggleQuest();
            }
            case ENCYCLOPEDIA -> {
                // toggleEncyclopedia();
            }
        }
    }

    private void toggleInventory() {
        boolean visible = inventoryMenu.isVisible();
        inventoryMenu.clearActions();
        if (!visible) {
            inventoryMenu.addAction(Actions.sequence(Actions.show(),
                    Actions.moveTo(Gdx.graphics.getWidth() * .240f, Gdx.graphics.getHeight() * .1f, 0.13f)));
        } else {
            inventoryMenu.addAction(Actions.sequence(
                    Actions.moveTo(Gdx.graphics.getWidth() * 1f, Gdx.graphics.getHeight() * .1f, 0.13f),
                    Actions.hide()));
        }
    }

    public void toggleShop() {
        // TODO
    }

    public void toggleBuilding() {
        // TODO
    }

    public void toggleQuest() {
        // TODO
    }

    /**
     * Will Draw Overlay information about selected item
     * over it/in game world.
     * Ex: you click on a placed item and it shows a menu with control pop ups and
     * other info
     */
    public void toggleSelecting() {
        // TODO
    }

    public void configOpenedInventory(OpenedMenuState openedInventory) {}

}
