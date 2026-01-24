package ore.forge.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import ore.forge.GameContext;
import ore.forge.Input3D.OpenedMenuState;
import ore.forge.Items.ItemDefinition;
import ore.forge.Player.ItemInventory;
import ore.forge.Player.ItemInventoryNode;
import ore.forge.UI.Widgets.Icon;
import ore.forge.VisualComponent;

import java.util.ArrayList;
import java.util.List;

public class UI extends Stage {
    private ItemInventoryMenu inventoryMenu;
    private TextureAtlas iconAtlas;

    public UI(ItemInventory inventory) {
        super();
        // Create our texture atlas of icon images
        IconRenderer iconRenderer = new IconRenderer();
        for (ItemInventoryNode node : inventory.nodes()) {
            ItemDefinition item = node.getHeldItem();
            VisualComponent vc = new VisualComponent(new ModelInstance(item.model()));
            iconRenderer.renderIcon(item.id(), vc);
            vc.dispose();
        }
        iconAtlas = iconRenderer.buildAtlas();

        //Create our Icons for Inventory
        List<Icon<ItemInventoryNode>> allIcons = new ArrayList<>();
        for (ItemInventoryNode node : inventory.nodes()) {
            ItemDefinition item = node.getHeldItem();
            AtlasRegion region = iconAtlas.findRegion(item.id());
            Icon<ItemInventoryNode> icon = new Icon<>(region, node);
            icon.setTopText("Stored: " + node.getStored());
            icon.setBottomText(node.name());
            allIcons.add(icon);
        }
        inventoryMenu = new ItemInventoryMenu(allIcons);
        inventoryMenu.setSize(Gdx.graphics.getWidth() * 0.76f, Gdx.graphics.getHeight() * .8f);
        inventoryMenu.setVisible(false);

//        allIcons.clear();
//        for(ItemDefinition item : allItems){
//            AtlasRegion region = iconAtlas.findRegion(item.id());
//            allIcons.add(new Icon<>(region, item));
//        }
//        ShopMenu shopMenu = new ShopMenu(allIcons, GameContext.INSTANCE);
//        shopMenu.setSize(Gdx.graphics.getWidth() * 0.76f, Gdx.graphics.getHeight() * .8f);
//        shopMenu.setVisible(true);
//        this.addActor(shopMenu);

        this.addActor(inventoryMenu);
    }

    public void setBuildListener(BuildListener listener) {
        this.inventoryMenu.setBuildListener(listener);
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

}
