package ore.forge.engine.Components;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.engine.render.RenderPart;

import java.util.ArrayList;
import java.util.List;

public class VisualComponent implements Disposable {
    public List<RenderPart> renderParts = new ArrayList<>();
    public ModelInstance modelInstance;
    public Attribute attributes;

    public VisualComponent(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
    }

    public void syncFromEntity(Matrix4 transform) {
        for (RenderPart renderPart : renderParts) {
            renderPart.transform.set(transform);
        }
        modelInstance.transform.set(transform);
    }

    public void addRenderPart(RenderPart renderPart) {
        renderParts.add(renderPart);
    }

    @Override
    public void dispose() {

    }

}

