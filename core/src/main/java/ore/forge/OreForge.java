/*This class is responsible for starting the game and bringing you to the main menu.
 */

package ore.forge;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.ItemDefinition;
import ore.forge.Player.ItemInventory;
import ore.forge.QuestComponents.QuestManager;
import ore.forge.Screens.Gameplay3D;
import ore.forge.UI.UI;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nathan Ulmen
 */
public class OreForge extends Game {

	public void create() {
        /*
		* Things to Initialize here:
		* AllGameItems
		* Load Save Data
		* Create the Objects for elements like the map and UI.
		* */
        GameContext context = GameContext.INSTANCE;
        var allItems = loadItemDefinitions();

        //Setup our inventory with all items we loaded.
        context.player.inventory = new ItemInventory(allItems);
        context.load();

        //Create Our UI
        UI ui = new UI(context.player.inventory);
        Gameplay3D gameplay3D = new Gameplay3D(ui);
        setScreen(gameplay3D);
	}

	public void render() {
		// Clear the screen
		super.render();
	}

    public List<ItemDefinition> loadItemDefinitions() {
        ArrayList<ItemDefinition> itemDefinitions = new ArrayList<>();
        JsonReader reader = new JsonReader();
        JsonValue items = reader.parse(Gdx.files.internal("Items/items.json"));
        for (JsonValue itemValue : items) {
            itemDefinitions.add(ItemDefinition.createDefinition(itemValue));
        }
        return itemDefinitions;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

}
