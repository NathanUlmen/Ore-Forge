package ore.forge.engine.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

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

    protected void bindShader(ShaderProgram shader, Camera camera) {
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

