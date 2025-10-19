package ore.forge.Input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class CameraController3D {
    private final PerspectiveCamera camera;
    private static final float MOVE_SPEED = 10;
    private static final float SPEED_SCALAR = 2.5f;
    private final Vector2 mouseScreen;

    public CameraController3D(PerspectiveCamera camera) {
        this.camera = camera;
        mouseScreen = new Vector2();
    }

    public void update() {
        final float delta = Gdx.graphics.getDeltaTime();
        float finalMoveSpeed = MOVE_SPEED;
        Vector3 direction = new Vector3();
        direction.set(camera.direction).nor();

        //Sprint/move faster
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            finalMoveSpeed *= SPEED_SCALAR;
        }
        //Move Left
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            direction.set(camera.direction);
            direction.y = 0;
            direction.nor();
            var cross = new Vector3(0,1,0).crs(direction);
            camera.position.add(cross.cpy().scl(finalMoveSpeed * delta));
        }
        //Move Right
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            direction.set(camera.direction);
            direction.y = 0;
            direction.nor();
            var cross = new  Vector3(0,1,0).crs(direction).scl(-1);
            camera.position.add(cross.cpy().scl(finalMoveSpeed * delta));
        }
        //Move Forward
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.position.add(direction.cpy().scl(finalMoveSpeed * delta));
        }
        //Move Backwards
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.position.sub(direction.cpy().scl(finalMoveSpeed * delta));
        }
        //Move up
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            camera.position.y += finalMoveSpeed * delta;
        }
        //Move down
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            camera.position.y -= finalMoveSpeed * delta;
        }
        //Update camera direction
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
