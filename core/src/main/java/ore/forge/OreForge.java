/*This class is responsible for starting the game and bringing you to the main menu.
 */

package ore.forge;

import com.badlogic.gdx.Game;

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

        GameScreen gameScreen = new GameScreen();
        setScreen(gameScreen);

//
//
//		ItemInventory itemInventory = new ItemInventory(null);
//        Gameplay3D gameplay3D = new Gameplay3D();
//		setScreen(mainMenuScreen); commented out so we can experiment with rework 2D
//      setScreen(gameplayScreen); commented out so we can experiment with rework 3D
//        setScreen(gameplay3D);

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
