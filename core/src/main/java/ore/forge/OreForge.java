/*This class is responsible for starting the game and bringing you to the main menu.
 */

package ore.forge;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.ItemDefinition;
import ore.forge.Player.ItemInventory;
import ore.forge.QuestComponents.QuestManager;
import ore.forge.Render.AssetHandler;
import ore.forge.Screens.Gameplay3D;
import ore.forge.UI.UI;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nathan Ulmen
 */
public class OreForge extends Game {

	public void create() {

        Gdx.app.log("GL", Gdx.gl.getClass().getName());
        Gdx.app.log("GL20", Gdx.gl20 == null ? "null" : Gdx.gl20.getClass().getName());
        Gdx.app.log("GL30", Gdx.gl30 == null ? "null" : Gdx.gl30.getClass().getName());

        if (Gdx.graphics.isGL30Available()) {
            System.out.println("GL30 is available");
            Gdx.gl = Gdx.gl30;
            Gdx.graphics.setGL30(Gdx.graphics.getGL30());
        }

        //Set to gl30
        Gdx.app.log("GL", Gdx.gl.glGetString(GL30.GL_VERSION));
        Gdx.app.log("GLSL", Gdx.gl.glGetString(GL30.GL_SHADING_LANGUAGE_VERSION));

//        while (true) {
//            foo.render();
//            if (1 > 50) {break;}
//        }

        /*
		* Things to Initialize here:
		* AllGameItems
		* Load Save Data
		* Create the Objects for elements like the map and UI.
		* */

        GameContext context = GameContext.INSTANCE;
        Runtime.getRuntime().addShutdownHook(new Thread(context::save)); //Save progress before exit
        Profiler instance = Profiler.INSTANCE;
        Runtime.getRuntime().addShutdownHook(new Thread(instance::dumpToFile));

        var allItems = loadItemDefinitions();

        //Setup our inventory with all items we loaded.
        context.player.inventory = new ItemInventory(allItems);
        context.load();

        //Create Our UI
//        UI ui = new UI(context.player.inventory);
//        Gameplay3D gameplay3D = new Gameplay3D(ui);
//        setScreen(gameplay3D);

        setScreen(new TestScene());
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
