package ore.forge.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ore.forge.ButtonHelper;
import ore.forge.ItemManager;
import ore.forge.OreForge;
import ore.forge.Player.Player;


public class MainMenu extends CustomScreen {

    public MainMenu(final OreForge game, final ItemManager itemManager) {
        super(game, itemManager);

        Gdx.input.setInputProcessor(this.stage);
        TextButton start = ButtonHelper.createRoundTextButton("Start", Color.DARK_GRAY, Gdx.graphics.getWidth() / 10f, Gdx.graphics.getHeight() / 22.5f);
        start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ButtonHelper.getButtonClickSound().play();
                game.setScreen(game.gameplayScreen);
            }
        });


        TextButton exit = ButtonHelper.createRoundTextButton("Exit", Color.DARK_GRAY, Gdx.graphics.getWidth() / 10f, Gdx.graphics.getHeight() / 22.5f);
        exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ButtonHelper.getButtonClickSound().play();
                // Handle exit button click
                Player.getSingleton().saveData();

                Gdx.app.exit(); // This will close the application
            }
        });

        TextButton settings = ButtonHelper.createRoundTextButton("Settings", Color.DARK_GRAY, Gdx.graphics.getWidth() / 10f, Gdx.graphics.getHeight() / 22.5f);
        settings.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ButtonHelper.getButtonClickSound().play();
                // Handle exit button click
                game.setScreen(game.settingsMenu);
                game.settingsMenu.previousScreen = game.mainMenuScreen;
            }
        });

        Table table = new Table();
        table.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        table.add(start).pad(20).width((float) Gdx.graphics.getWidth() /5).height((float) Gdx.graphics.getHeight() /20);
        table.row();
        table.add(settings).pad(20).width((float) Gdx.graphics.getWidth()/5).height((float) Gdx.graphics.getHeight()/20);
        table.row();
        table.add(exit).pad(20).width((float) Gdx.graphics.getWidth() /5).height((float) Gdx.graphics.getHeight() /20);

        Texture backgroundTexture = new Texture(Gdx.files.internal("Background3.jpg"));
        Image background = new Image(backgroundTexture);
        background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        stage.addActor(exit);
//        stage.addActor(start);
        stage.addActor(background);
        stage.addActor(table);
//        stage.addActor(game.fpsCounter);
    }

    @Override
    public void show() {
        screenFadeIn(0.1f);
        Gdx.input.setInputProcessor(this.stage);
        game.memoryCounter.setPosition(100, 100);
        stage.addActor(game.memoryCounter);
    }

    @Override
    public void hide() {
        this.screenFadeOut(0.1f);
    }



}
