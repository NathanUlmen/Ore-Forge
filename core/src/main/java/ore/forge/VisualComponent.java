package ore.forge;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Disposable;

public class VisualComponent implements Disposable {
    public ModelInstance modelInstance;
    public Attribute attributes;

    public VisualComponent(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
        this.modelInstance.userData = this;
    }

    @Override
    public void dispose() {
        if (modelInstance != null && modelInstance.model != null) {
            //TODO
        }
    }
}

