package ore.forge.engine.Components;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import ore.forge.engine.PhysicsBody;

public class DynamicPhysicsComponent {
    private PhysicsBody physicsBody;
    // current + previous pose (authoritative)
    private final Vector3 prevPos = new Vector3();
    private final Vector3 currPos = new Vector3();
    private final Quaternion prevRot = new Quaternion();
    private final Quaternion currRot = new Quaternion();
    private final Vector3 prevScale = new Vector3();
    private final Vector3 currScale = new Vector3();

    // temp for building transforms
    private final Vector3 renderPos = new Vector3();
    private final Quaternion renderRot = new Quaternion();
    private final Vector3 renderScale = new Vector3();
    private final Matrix4 renderTransform = new Matrix4();

    public void snapShotPrevious() {
        //update our previous transform
        //called before each physics step
    }

    public void syncCurrentTransform() {
        //called after each physics step
        //updates our current transform
    }

    public void syncRender(Matrix4 renderTransform, float alpha) {
        //set up RenderParts transform to be ready for current frame.
        //called every frame
    }


}
