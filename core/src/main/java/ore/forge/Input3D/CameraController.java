package ore.forge.Input3D;

import com.badlogic.gdx.graphics.Camera;

public interface CameraController {

    public void update(float deltaT);

    public Camera getCamera();
}
