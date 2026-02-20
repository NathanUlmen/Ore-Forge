package ore.forge.game;

import com.badlogic.gdx.Screen;

public class EcsTestScene implements Screen {
    private final GameContext2 context;

    public EcsTestScene(GameContext2 context) {
        this.context = context;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        context.engine.update(delta);
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

    }

    @Override
    public void dispose() {

    }
}
