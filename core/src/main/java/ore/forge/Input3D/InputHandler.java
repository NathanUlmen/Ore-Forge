package ore.forge.Input3D;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import ore.forge.GameContext;
import ore.forge.UI.UI;

public class InputHandler {
    protected CameraController cameraController;
    protected final GameContext context;
    private InputState inputState;
    private final Plane groundPlane = new Plane(new Vector3(0, 1, 0), 0); // y=0 plane
    private final Vector3 intersection = new Vector3();

    public InputHandler(CameraController controller3D, UI ui, GameContext context) {
        this.context = context;
        cameraController = controller3D;
        var defaultInput = new DefaultInputState(this);
        var selectingInput = new SelectingItemsInputState(this);
        var buildingInput = new BuildingInputState(this);
        var openedInventory = new OpenedMenuState(this, ui);


        //Setup Default
        defaultInput.setSelecting(selectingInput);
        defaultInput.setOpenedInventoryState(openedInventory);

        //Setup Selecting
        selectingInput.setDefaultState(defaultInput);
        selectingInput.setBuildingState(buildingInput);

        //Setup Building
        buildingInput.setDefaultState(defaultInput);

        //Setup OpenedInventory
        openedInventory.setBuildingInputState(buildingInput);
        openedInventory.setDefaultInputState(defaultInput);

        setInputState(defaultInput);
    }

    public void update(float delta) {
        inputState.update(delta);
    }

    public CameraController cameraController() {
        return cameraController;
    }

    public void setInputState(InputState inputState) {
        this.inputState = inputState;
    }

    public Vector3 getMouseGroundPosition(Camera camera) {
        // Get mouse position in screen coordinates
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();

        // Unproject to get the world-space ray
        Ray ray = camera.getPickRay(mouseX, mouseY);

        // Intersect the ray with the ground plane
        if (Intersector.intersectRayPlane(ray, groundPlane, intersection)) {
            // Clamp Y >= 0 just in case
            if (intersection.y < 0f) intersection.y = 0f;
            return new Vector3(MathUtils.floor(intersection.x), MathUtils.floor(intersection.y), MathUtils.floor(intersection.z));
        } else {
            // No intersection (looking up at sky)
            return new Vector3(ray.origin);
        }
    }

}
