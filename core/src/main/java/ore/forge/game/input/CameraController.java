package ore.forge.game.input;

import com.badlogic.gdx.graphics.Camera;

public interface CameraController {

    public void update(float deltaT);

    public Camera getCamera();
}
