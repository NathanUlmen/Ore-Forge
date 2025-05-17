package ore.forge.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ore.forge.ButtonHelper;
import ore.forge.ItemManager;
import ore.forge.OreForge;
import ore.forge.Player.Player;


public class PauseMenu extends CustomScreen {
    private TextureAtlas atlas;
    private Skin skin;
    private final Table table;
    private final TextButton resume;
    private final TextButton settingsButton;
    private final TextButton mainMenu;
    private final TextButton saveGame;
    private BitmapFont defualtFont;
    private Texture texture;
    private Label heading;
    private TextButton.TextButtonStyle buttonStyle, settingsButtonStyle;
    private final FPSLogger fpsLogger= new FPSLogger();


    public PauseMenu(final OreForge game, final ItemManager itemManager) {
        super(game, itemManager);

        table = new Table();
        Gdx.input.setInputProcessor(stage);
        resume = ButtonHelper.createRoundTextButton("Resume", Color.BLUE, 384, 128);
        resume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ButtonHelper.getButtonClickSound().play();
                game.setScreen(game.gameplayScreen);
//                dispose();
            }
        });
        saveGame = ButtonHelper.createRoundTextButton("Save", Color.BLUE, 384, 128);
        saveGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ButtonHelper.getButtonClickSound().play();
                Player.getSingleton().saveData();
            }
        });
        settingsButton = ButtonHelper.createRoundTextButton("Settings", Color.BLUE, 384, 128);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ButtonHelper.getButtonClickSound().play();
                game.setScreen(game.settingsMenu);
                game.settingsMenu.previousScreen = game.pauseMenu;
            }
        });

        mainMenu = ButtonHelper.createRoundTextButton("Quit To Main Menu", Color.BLUE, 384, 128);
        mainMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ButtonHelper.getButtonClickSound().play();
                game.setScreen(game.mainMenuScreen);
//                dispose();
            }
        });


        table.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        table.setFillParent(true);
        table.add(resume).row();
        table.add(saveGame).row();
        table.add(settingsButton).row();
        table.add(mainMenu).row();



        stage.addActor(table);
    }

    @Override
    public void show() {
        screenFadeIn(0.2f);
        Gdx.input.setInputProcessor(this.stage);
        game.memoryCounter.setPosition(100, 100);
        stage.addActor(game.memoryCounter);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        //Kinda buggy
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            ButtonHelper.getButtonClickSound().play();
            game.setScreen(game.gameplayScreen);
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        screenFadeOut(0.2f);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
