package ore.forge.game;

import ore.forge.game.event.EventManager;
import ore.forge.game.event.Events.PrestigeGameEvent;
import ore.forge.game.event.GameEventListener;
import ore.forge.game.items.ItemDefinition;

import java.util.List;

public class PrestigeManager implements GameEventListener<PrestigeGameEvent> {
    private final LootTable lootTable;
//    private final static ore.forge.Player.Player player = ore.forge.Player.Player.getSingleton();
//    private final static ItemMap itemMap = ItemMap.getSingleton();

    public PrestigeManager(List<ItemDefinition> allItems) {
        lootTable = new LootTable(allItems);
        EventManager.getSingleton().registerListener(this);
    }

    public void prestige() {
//        player.setPrestigeLevel(player.getPrestigeLevel() + 1);

        //Event Manager Notifies of a prestige Event
        //Quests check/update their requirements.
        /*
         * Quests/Achievements
         * */
        lootTable.updateItems();
        awardItem();
//        player.addPrestigeCurrency(3);


        //reset Item Map and "pick up" all items from map.
//        itemMap.reset(player.getInventory());
//        player.getInventory().prestigeReset(); //Reset Inventory

    }

    private void awardItem() {
        ItemDefinition reward = lootTable.getRandomItem();
//        player.getInventory().addItem(reward.getID(), 1);
    }


    @Override
    public void handle(PrestigeGameEvent event) {
        if (event.getSubject()) {
            prestige();
        }

    }

    @Override
    public Class<?> getEventType() {
        return PrestigeGameEvent.class;
    }
}
