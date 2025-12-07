package ore.forge.Shaders;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import ore.forge.VisualComponent;


public class CustomShaderProvider extends DefaultShaderProvider {
    private Shader defaultShader;
    private Shader gridShader;

    public CustomShaderProvider() {
        // Initialize the GridShader here if you want
        gridShader = new GridShader();
    }

    @Override
    protected Shader createShader(Renderable renderable) {
        if (renderable.userData instanceof VisualComponent vc) {
            if (vc.attributes instanceof GridAttribute) {
                return gridShader;
            }
        }
        defaultShader = new DefaultShader(renderable, new DefaultShader.Config());
        return defaultShader;
    }
}

