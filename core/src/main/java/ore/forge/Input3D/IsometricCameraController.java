package ore.forge.Input3D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * @author Nathan Ulmen
 * This controller will draw some inspiration from the way the camera is controlled in games
 * like this sims.
 * Moving (W,A,S,D) will apply a translation.
 * Rotation(using mouse or other keys) will be done around the viewTarget
 * Zooming will be more complicated...
 * For zoom/position we will use a function thats slow in the beginning but then speeds up, something non-linear.
 * I was thinking something like x^2/constant.
 * zooming will be the sole source of height.
 */
public class IsometricCameraController implements CameraController {
    private PerspectiveCamera camera;
    private float angle;
    private Vector3 viewTarget; //will function as our origin.
    private float zoomValue;
    private static final float ZOOM_SPEED = 10f;
    private static final float MOVE_SPEED = 10f;
    private static final float ROTATION_SPEED = 100f;
    private static final float DISTANCE_SPEED = 20f;
    private static final float MAX_DISTANCE = 100f;
    private static final float MIN_DISTANCE = 3f;

    public IsometricCameraController(PerspectiveCamera camera) {
        this.camera = camera;
        viewTarget = new Vector3();
        zoomValue = 0;
    }

    public void update(float deltaT) {
        //---MOVEMENT---
        //Move forward
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            Vector3 movement = camera.direction.cpy();
            movement.y = 0;
            movement.nor().scl(MOVE_SPEED * deltaT);
            translate(movement);
        }
        //Move Backwards
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            Vector3 movement = camera.direction.cpy();
            movement.y = 0;
            movement.nor().scl(MOVE_SPEED * deltaT * -1);
            translate(movement);
        }
        //Move right
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            Vector3 movement = new Vector3(0, 1, 0).crs(camera.direction.cpy().nor());
            movement.y = 0;
            movement.nor().scl(MOVE_SPEED * deltaT * -1);
            translate(movement);
        }
        //Move left
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            Vector3 movement = new Vector3(0, 1, 0).crs(camera.direction.cpy().nor());
            movement.y = 0;
            movement.nor().scl(MOVE_SPEED * deltaT);
            translate(movement);
        }

        //---ROTATION---
        //rotate right
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.rotateAround(viewTarget, Vector3.Y, ROTATION_SPEED * deltaT);
        }
        //rotate left
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.rotateAround(viewTarget, Vector3.Y, ROTATION_SPEED * deltaT * -1);
        }
        camera.lookAt(viewTarget);

        // --- ZOOM ---
        if (Gdx.input.isKeyPressed(Input.Keys.E))
            zoomValue += DISTANCE_SPEED * deltaT;

        if (Gdx.input.isKeyPressed(Input.Keys.Q))
            zoomValue -= DISTANCE_SPEED * deltaT;

        zoomValue = MathUtils.clamp(zoomValue, MIN_DISTANCE, MAX_DISTANCE);

        // Direction from target to camera in XZ
        Vector3 dir = camera.position.cpy().sub(viewTarget);
        dir.y = 0;
        if (dir.len2() == 0) dir.set(0, 0, 1);
        dir.nor();

        // Apply zoom radius + height
        camera.position.set(viewTarget)
            .add(dir.scl(computeXZDistance(zoomValue)))
            .add(0f, computeY(zoomValue), 0f).add(0, 1, 0);

        camera.lookAt(viewTarget);

    }

    @Override
    public Camera getCamera() {
        return camera;
    }

    private void translate(Vector3 translation) {
        camera.position.add(translation);
        viewTarget.add(translation);
    }

    private float computeXZOffset() {
        float dx = camera.position.x - viewTarget.x;
        float dz = camera.position.z - viewTarget.z;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    private float computeY(float distance) {
        float n = MathUtils.norm(MIN_DISTANCE, MAX_DISTANCE, distance);
        n = MathUtils.clamp(n, 0, 1);
        n = (3 * n * n) - (2 * n * n * n);
        return n * MAX_DISTANCE;
    }

    private float computeXZDistance(float zoomValue) {
        float n = MathUtils.norm(MIN_DISTANCE, MAX_DISTANCE, zoomValue);
        n = MathUtils.clamp(n, 0f, 1f);
        n = (3 * n * n) - (2 * n * n * n); // smoothstep
        return MathUtils.lerp(MIN_DISTANCE, MAX_DISTANCE, n);
    }

}
