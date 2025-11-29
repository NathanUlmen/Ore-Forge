package ore.forge.Input3D;

public class OpenedInventoryState extends InputState {
    private BuildingInputState buildingInputState;

    public OpenedInventoryState(InputHandler inputHandler) {
        super(inputHandler);
    }

    @Override
    public void update(float delta) {


    }

    public void setBuildingInputState(BuildingInputState buildingInputState) {
        this.buildingInputState = buildingInputState;
    }
}
