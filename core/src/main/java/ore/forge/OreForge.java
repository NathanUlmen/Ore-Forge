/*This class is responsible for starting the game and bringing you to the main menu.
 */

package ore.forge;

import com.badlogic.gdx.Game;
import ore.forge.Screens.Gameplay3D;

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

//        ItemManager itemManager = new ItemManager(null);
//        var questManager = new QuestManager();
//        OreRealm.getSingleton().populate(); //Create/pool all ore.
//        ore.forge.Player.Player.getSingleton().loadSaveData();
//        ore.forge.Player.Player.getSingleton().initInventory(itemManager);
//        ore.forge.Player.Player.getSingleton().getInventory().printInventory();

//        var prestigeManager = new PrestigeManager(itemManager);

//        ItemMap.getSingleton().loadState(itemManager);
//		mainMenuScreen = new MainMenu(this, itemManager);
//		settingsMenu = new SettingsMenu(this, itemManager);
//		gameplayScreen = new Gameplay(this, itemManager, GameWorld.instance());
//		pauseMenu = new PauseMenu(this, itemManager);

//		ItemInventory itemInventory = new ItemInventory(null);
        Gameplay3D gameplay3D = new Gameplay3D();
//		setScreen(mainMenuScreen); commented out so we can experiment with rework 2D
//      setScreen(gameplayScreen); commented out so we can experiment with rework 3D
        setScreen(gameplay3D);

	}

	public void render() {
		// Clear the screen
		super.render();
	}

    @Override
    public void dispose() {
        super.dispose();
    }

}
