package ore.forge.Input3D;


import ore.forge.Input.CameraController;

import java.util.Deque;

public class InputHandler {
    private CameraController3D cameraController;
    private InputState inputState;
    private Deque<InputState> previousState;

    public InputHandler(CameraController3D controller3D) {
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
