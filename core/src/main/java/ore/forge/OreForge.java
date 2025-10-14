/*This class is responsible for starting the game and bringing you to the main menu.
 */

package ore.forge;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import ore.forge.Player.Player;
import ore.forge.QuestComponents.QuestManager;
import ore.forge.Screens.*;

/**@author Nathan Ulmen*/
public class OreForge extends Game {
	public MainMenu mainMenuScreen;
    public Label memoryCounter;
	public Player player;


	public PauseMenu pauseMenu;
	public Screen gameplayScreen;
	public SettingsMenu settingsMenu;

	private SpriteBatch spriteBatch;

	public void create() {

		BitmapFont font2 = new BitmapFont(Gdx.files.internal("UIAssets/Blazam.fnt"));
		Label.LabelStyle fpsStyle = new Label.LabelStyle(font2, Color.WHITE);
		memoryCounter = new Label("", fpsStyle);
		memoryCounter.setFontScale(0.6f);
		memoryCounter.setPosition(99, 100);
		memoryCounter.setVisible(false);

        /*
		* Things to Initialize here:
		* AllGameItems
		* Load Save Data
		* Create the Objects for elements like the map and UI.
		* */
		spriteBatch = new SpriteBatch();
        ItemManager itemManager = new ItemManager(null);
        var questManager = new QuestManager();
        OreRealm.getSingleton().populate(); //Create/pool all ore.
        Player.getSingleton().loadSaveData();
        Player.getSingleton().initInventory(itemManager);
        Player.getSingleton().getInventory().printInventory();

        var prestigeManager = new PrestigeManager(itemManager);

        ItemMap.getSingleton().loadState(itemManager);
		mainMenuScreen = new MainMenu(this, itemManager);
		settingsMenu = new SettingsMenu(this, itemManager);
		gameplayScreen = new Gameplay(this, itemManager, GameWorld.instance());
		pauseMenu = new PauseMenu(this, itemManager);

        Gameplay3D gameplay3D = new Gameplay3D();
//		setScreen(mainMenuScreen); commented out so we can experiment with rework 2D
//      setScreen(gameplayScreen); commented out so we can experiment with rework 3D
        setScreen(gameplay3D);

	}

	public void render() {
		// Clear the screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		memoryCounter.setText(((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024) + "MB");
		// Update and render the current screen
		super.render();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}

	public MainMenu getMainMenuScreen() {
		return mainMenuScreen;
	}


}
