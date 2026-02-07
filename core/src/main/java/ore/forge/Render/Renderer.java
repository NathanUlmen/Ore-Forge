package ore.forge.Render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Pool;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nathan Ulmen
 * Is responsible For sorting, batching, and instancing draw calls. *
 */
public class Renderer {
    public static final int MAX_INSTANCED_DRAW = 10_000;
    private final ArrayList<RenderPass> renderPasses = new ArrayList<>();
    private final FloatBuffer instanceBuffer;
    private final int instanceVbo;
    private final Pool<RenderCommand> renderCommandPool;

    public Renderer() {
        instanceBuffer = BufferUtils.newFloatBuffer(10_000_000);
        instanceVbo = Gdx.gl30.glGenBuffer();
        renderCommandPool = new Pool<>(10_000) {
            @Override
            protected RenderCommand newObject() {
                return new RenderCommand();
            }
        };

        Gdx.gl30.glBindBuffer(GL30.GL_ARRAY_BUFFER, instanceVbo);
        Gdx.gl30.glBufferData(
            GL30.GL_ARRAY_BUFFER,
            instanceBuffer.capacity() * Float.BYTES,
            null,
            GL30.GL_STREAM_DRAW
        );
    }

    public void render(List<RenderPart> toRender, Camera camera) {
//        toRender = frustumCull(camera, toRender);
        for (RenderPass pass : renderPasses) {
            ArrayList<RenderCommand> commands = new ArrayList<>();
            for (RenderPart part : toRender) {
                if (pass.accepts(part)) {
                    RenderCommand command = renderCommandPool.obtain();
                    command.init(part);
                    commands.add(command);
                }
            }

            if (!commands.isEmpty()) {
                pass.sort(commands);

                pass.begin(camera);

                drawCommands(commands, pass, camera);

                pass.end();
            }
            for (RenderCommand command : commands) {
                renderCommandPool.free(command);
            }
        }
    }

    /**
     * @param commands - a list of objects that are within camera frustum.
     *
     */
    private void drawCommands(ArrayList<RenderCommand> commands, RenderPass pass, Camera camera) {
        int startIndex = 0;

        while (startIndex < commands.size()) {
            RenderCommand first = commands.get(startIndex);

            int endIndex = startIndex + 1;
            while (endIndex < commands.size()
                && canInstance(first, commands.get(endIndex))) {
                endIndex++;
            }

            MaterialHandle material = first.materialHandle;
            pass.bindShader(material.shader, camera);

            material.bind();

            MeshHandle mesh = first.meshHandle;
            Gdx.gl30.glBindVertexArray(mesh.vao);

            int count = endIndex - startIndex;
            if (count > 1) {
                drawInstanced(commands, startIndex, endIndex);
            } else {
                drawInstanced(commands, startIndex, startIndex + 1);
            }

            startIndex = endIndex;
        }
    }

    public boolean canInstance(RenderCommand a, RenderCommand b) {
        return a.meshHandle == b.meshHandle &&
            a.materialHandle == b.materialHandle &&
            a.flags == b.flags;
    }

    public void drawInstanced(ArrayList<RenderCommand> commands, int start, int end) {
        final var gl = Gdx.gl30;

        int instanceCount = end - start;
        MeshHandle mesh = commands.get(start).meshHandle;

        // 1. Build instance data (CPU loop)
        instanceBuffer.clear();
        for (int i = start; i < end; i++) {
            RenderCommand cmd = commands.get(i);
            // write mat4 (16 floats)
            instanceBuffer.put(cmd.worldTransform.val);
        }
        instanceBuffer.flip();

        //Upload instance buffer
        gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, mesh.instanceVBO);
        gl.glBufferSubData(
            GL30.GL_ARRAY_BUFFER,
            0,
            instanceBuffer.remaining() * Float.BYTES,
            instanceBuffer
        );

        //instanced draw call
        gl.glDrawElementsInstanced(
            GL20.GL_TRIANGLES,
            mesh.indexCount,
            GL20.GL_UNSIGNED_INT,
            mesh.indexOffsetBytes,
            instanceCount
        );

    }

    /*
    * To make this faster in the future we could:
    * Break it down so the compiler will vectorize it.
    * Ensure that toCull is built from an acceleration structure to cut large parts of world out
    * Have OrientedBoundingBox be a property of RenderPart and update it when it becomes dirty
    *  */
    public List<RenderPart> frustumCull(Camera camera, List<RenderPart> toCull) {
        List<RenderPart> culled = new ArrayList<>(toCull.size());
        for (RenderPart part : toCull) {
            Matrix4 transform = part.transform;
            BoundingBox box = part.mesh.boundingBox;
            OrientedBoundingBox orientedBoundingBox = new OrientedBoundingBox(box, transform);
            if (camera.frustum.boundsInFrustum(orientedBoundingBox)) {
                culled.add(part);
            }
        }
        return culled;
    }

    public void addRenderPass(RenderPass pass) {
        renderPasses.add(pass);
    }

    public List<RenderPass> renderPasses() {
        return renderPasses;
    }
}
