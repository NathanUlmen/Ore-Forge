/*This class is responsible for starting the game and bringing you to the main menu.
 */

package ore.forge.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.TestScene;
import ore.forge.engine.profiling.Profiler;
import ore.forge.engine.render.AssetHandler;
import ore.forge.game.items.ItemDefinition;
import ore.forge.game.player.ItemInventory;
import ore.forge.game.screens.Gameplay3D;
import ore.forge.game.ui.UI;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nathan Ulmen
 */
public class OreForge extends Game {

    public void create() {
        //assert that gl30 is available and set active
        initGL();

        //TODO: load in assets(item definitions depend/have handles to these)

        //load in item definitions
        ArrayList<ItemDefinition> allItems = new ArrayList<>();
        loadItemDefinitions(allItems);

        //TODO: load player save data

        //TODO: setup UI (render icons for each item into an atlas)

        //TODO: init all ECS systems

        //TODO: load game world

        //TODO: init player controller

        //TODO: finally set screen to main gameplay

        GameContext2 context2 = new GameContext2();

        GameContext context = GameContext.INSTANCE;
        Runtime.getRuntime().addShutdownHook(new Thread(context::save)); //Save progress before exit
        Profiler instance = Profiler.INSTANCE;
//        Runtime.getRuntime().addShutdownHook(new Thread(instance::dumpToFile));



        //Setup our inventory with all items we loaded.
        context.player.inventory = new ItemInventory(allItems);
        context.load();

        //Create Our UI
        UI ui = new UI(context.player.inventory);
        Gameplay3D gameplay3D = new Gameplay3D(ui);
        setScreen(gameplay3D);
//        setScreen(new TestScene());
    }

    public void render() {
        // Clear the screen
        super.render();
    }

    public void loadItemDefinitions(List<ItemDefinition> destination) {
        JsonReader reader = new JsonReader();
        JsonValue items = reader.parse(Gdx.files.internal("Items/items.json"));
        for (JsonValue itemValue : items) {
            destination.add(ItemDefinition.createDefinition(itemValue));
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private void initGL() {
        if (Gdx.graphics.isGL30Available()) {
            System.out.println("GL30 is available");
            Gdx.gl = Gdx.gl30;
            Gdx.graphics.setGL30(Gdx.graphics.getGL30());
        } else {
            Gdx.app.error("GL ERROR", "GL30 is not available.");
        }
    }

}
