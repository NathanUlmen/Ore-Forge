package ore.forge.Input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.Input.Keys.*;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class CameraController3D {
    private final PerspectiveCamera camera;
    private static final float MOVE_SPEED = 10;
    private final Vector2 mouseScreen;

    public CameraController3D(PerspectiveCamera camera) {
        this.camera = camera;
        mouseScreen = new Vector2();
    }

    public void update() {
        final float delta = Gdx.graphics.getDeltaTime();
        Vector3 direction = new Vector3();
        direction.set(camera.direction).nor();
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            direction.set(camera.direction);
            direction.y = 0;
            direction.nor();
            var cross = new Vector3(0,1,0).crs(direction);
            camera.position.add(cross.cpy().scl(MOVE_SPEED * delta));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            direction.set(camera.direction);
            direction.y = 0;
            direction.nor();
            var cross = new  Vector3(0,1,0).crs(direction).scl(-1);
            camera.position.add(cross.cpy().scl(MOVE_SPEED * delta));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.position.add(direction.cpy().scl(MOVE_SPEED * delta));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.position.sub(direction.cpy().scl(MOVE_SPEED * delta));
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            mouseScreen.set(Gdx.input.getX(), Gdx.input.getY());
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            handleRotation(delta);
        }
    }

    private void handleRotation(float delta) {
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();
        var deltaX = mouseScreen.x - x;
        var deltaY =  mouseScreen.y - y;
        mouseScreen.set(x, y);

        // Yaw: rotate around world Y axis
        camera.rotateAround(camera.position, new Vector3(0,1,0), deltaX * 0.5f);

        // Pitch: rotate around camera right axis
        Vector3 right = camera.direction.cpy().crs(camera.up).nor();
        camera.rotateAround(camera.position, right, deltaY * 0.5f);
    }

}
