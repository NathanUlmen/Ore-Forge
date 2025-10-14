package ore.forge.Input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.Input.Keys.*;
import com.badlogic.gdx.math.Vector3;

public class CameraController3D {
    private final PerspectiveCamera camera;
    private static final float MOVE_SPEED = 10;

    public CameraController3D(PerspectiveCamera camera) {
        this.camera = camera;
    }

    public void update() {
        final float delta = Gdx.graphics.getDeltaTime();
        Vector3 direction = camera.direction.nor();
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            var cross = new Vector3(0,1,0).crs(direction);
            camera.position.add(cross.cpy().scl(MOVE_SPEED * delta));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            var cross = direction.cpy().crs(0, 1f, 0);
            camera.position.add(cross.cpy().scl(MOVE_SPEED * delta));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.position.add(direction.scl(MOVE_SPEED * delta));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.position.sub(direction.scl(MOVE_SPEED * delta));
        }
    }

}
