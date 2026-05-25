package ore.forge.engine.render.passes;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import ore.forge.engine.render.RenderCommand;
import ore.forge.engine.render.RenderPart;

import java.util.ArrayList;

public abstract class RenderPass {
    public final String name;
    public ShaderProgram currentShader;

    protected RenderPass(String name, ShaderProgram shader) {
        this.name = name;
        this.currentShader = shader;
    }

    public void begin(Camera camera) {
        currentShader.bind();
        currentShader.setUniformMatrix("u_projView", camera.combined);
        configureState();
    }

    public void bindShader(ShaderProgram shader, Camera camera) {
        if (this.currentShader == shader) return;
        shader.bind();
        shader.setUniformMatrix("u_projView", camera.combined);

        this.currentShader = shader;
    }

    public void end() {
        cleanupState();
    }

    public abstract boolean accepts(RenderPart part);

    protected abstract void configureState();

    protected abstract void cleanupState();

    public abstract void sort(ArrayList<RenderCommand> commands);
}

