package ore.forge.engine.components;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.engine.render.RenderPart;

import java.util.ArrayList;
import java.util.List;

public class VisualComponent implements Disposable {
    public ModelInstance modelInstance; //LEGACY, KEPT FOR TEMPORARY REASONS
    public Attribute attributes;

    //Modern Version. Work in Progress.
    public final List<RenderComponent> renderComponents = new ArrayList<>();



    public VisualComponent(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
    }


    @Override
    public void dispose() {

    }

}

