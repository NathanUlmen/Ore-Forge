package ore.forge;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.Render.RenderPart;

import java.util.ArrayList;
import java.util.List;

public class VisualComponent implements Disposable {
    public List<RenderPart> renderParts = new ArrayList<>();
    public ModelInstance modelInstance;
    public Attribute attributes;

    public VisualComponent(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
        this.modelInstance.userData = this;
    }

    public void syncFromEntity(Matrix4 transform) {
        modelInstance.transform.set(transform);
        for (RenderPart renderPart : renderParts) {
            renderPart.transform.set(transform);
        }
    }

    public void addRenderPart(RenderPart renderPart) {
        renderParts.add(renderPart);
    }

    @Override
    public void dispose() {
        if (modelInstance != null && modelInstance.model != null) {
            //TODO
        }
    }

}

