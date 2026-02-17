package ore.forge.engine;

import com.badlogic.gdx.math.Matrix4;

public class TransformHistory {
    public final Matrix4 currentTransform; //
    public Matrix4 previousTransform; //optional field

    public TransformHistory(Matrix4 currentTransform) {
        this.currentTransform = new  Matrix4(currentTransform);
    }

    public void advance() {
        previousTransform.set(currentTransform);
    }

    public void setBoth(Matrix4 newValue) {
        currentTransform.set(newValue);
        previousTransform.set(newValue);
    }

    public Matrix4 lerp(float alpha) {
        return previousTransform.lerp(currentTransform, alpha);
    }

}
