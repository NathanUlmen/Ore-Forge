package ore.forge.Input3D;


public class InputHandler {
    protected CameraController3D cameraController;
    private InputState inputState;

    public InputHandler(CameraController3D controller3D) {
        cameraController = controller3D;
        var defaultInput = new DefaultInputState(this);
        var selectingInput = new SelectingItemsInputState(this);
        var buildingInput = new BuildingInputState(this);
        var openedInventory = new OpenedInventoryState(this);

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

        setInputState(defaultInput);

    }

    public void update(float delta) {
        inputState.update(delta);
    }

    public CameraController3D cameraController() {
        return cameraController;
    }

    public void setInputState(InputState inputState) {
        this.inputState = inputState;
    }

}
