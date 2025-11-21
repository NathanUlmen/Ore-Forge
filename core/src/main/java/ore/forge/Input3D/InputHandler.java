package ore.forge.Input3D;


import java.util.Deque;

public class InputHandler {
    protected CameraController3D cameraController;
    private InputState inputState;
    private Deque<InputState> previousState;
    private final DefaultInputState defaultInput;

    public InputHandler(CameraController3D controller3D) {
        cameraController = controller3D;
        defaultInput = new DefaultInputState(this);
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

    public DefaultInputState defaultInput() {
        return defaultInput;
    }

}
