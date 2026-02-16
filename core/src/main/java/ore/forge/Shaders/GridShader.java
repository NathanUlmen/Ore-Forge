package ore.forge.Shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import ore.forge.engine.Components.VisualComponent;

public class GridShader implements Shader, Disposable {
    private ShaderProgram shader;
    private Camera camera;
    private RenderContext renderContext;

    @Override
    public void init() {
        String vertexShader = Gdx.files.internal("Shaders/PlacementGrid.vert").readString();
        String fragmentShader = Gdx.files.internal("Shaders/PlacementGrid.frag").readString();
        shader = new ShaderProgram(vertexShader, fragmentShader);

        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Failed to compile GridShader: " + shader.getLog());
        }
    }

    @Override
    public boolean canRender(Renderable renderable) {
        boolean result = renderable.userData instanceof VisualComponent vc && vc.attributes instanceof GridAttribute;
//        System.out.println("Can render: " + result);
        return result;
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.renderContext = context;

        shader.bind();
        shader.setUniformMatrix("u_projTrans", camera.combined);
    }

    @Override
    public void render(Renderable renderable) {
        shader.setUniformMatrix("u_worldTrans", renderable.worldTransform);

        // Pass diffuse color from material if present
//        ColorAttribute colorAttr = (ColorAttribute) renderable.material.get(ColorAttribute.Diffuse);
//        if (colorAttr != null) {
//            shader.setUniformf("u_diffuseColor", colorAttr.color);
//        } else {
//            shader.setUniformf("u_diffuseColor", 0f, 0f, 0f, 1f);
//        }

        renderable.meshPart.render(shader);
    }

    @Override
    public void end() {

    }


    @Override
    public void dispose() {
        shader.dispose();
    }

    @Override
    public int compareTo(Shader other) {
        return 0; // simplest, can be refined for batching
    }
}

