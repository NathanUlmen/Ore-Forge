package ore.forge.Render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.ArrayList;

public class BasicRenderPass extends RenderPass {

    public BasicRenderPass() {
        super("Basic", new ShaderProgram(
            Gdx.files.internal("Shaders/basic.vert"),
            Gdx.files.internal("Shaders/basic.frag"))
        );
        System.out.println(currentShader.getLog());
        int result = Gdx.gl30.glGetAttribLocation(currentShader.getHandle(), "a_VertexPos");
        System.out.println("a_VertexPos Location: " + result);
        System.out.println("a_normal location: " + Gdx.gl30.glGetAttribLocation(currentShader.getHandle(), "a_normal"));
        System.out.println("a_transform location: " + Gdx.gl30.glGetAttribLocation(currentShader.getHandle(), "a_transform"));

        if (!currentShader.isCompiled()) {
            throw new GdxRuntimeException(currentShader.getLog());
        }
    }

    @Override
    public boolean accepts(RenderPart part) {
        return !part.material.transparent;
    }

    @Override
    protected void configureState() {
        GL30 gl = Gdx.gl30;
        gl.glEnable(GL30.GL_DEPTH_TEST);
    }

    @Override
    protected void cleanupState() {

    }

    @Override
    public void sort(ArrayList<RenderCommand> commands) {
//        commands.sort((a, b) -> {
//            int r;
//
//            r = System.identityHashCode(a.material) - System.identityHashCode(b.material);
//            if (r != 0) return r;
//
//            r = System.identityHashCode(a.mesh) - System.identityHashCode(b.mesh);
//            return r;
//        });
    }
}
