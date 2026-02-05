package ore.forge.Render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Is responsible For sorting, batching, and instancing draw calls. *
 */
public class Renderer {
    public final AssetHandler assetHandler;
    public final ArrayList<RenderPass> renderPasses = new ArrayList<>();
    public final FloatBuffer instanceBuffer;
    private final int instanceVbo;

    public Renderer(AssetHandler assetHandler) {
        this.assetHandler = assetHandler;
        instanceBuffer = BufferUtils.newFloatBuffer(10000);
        instanceVbo = Gdx.gl30.glGenBuffer();

        Gdx.gl30.glBindBuffer(GL30.GL_ARRAY_BUFFER, instanceVbo);
        Gdx.gl30.glBufferData(
            GL30.GL_ARRAY_BUFFER,
            instanceBuffer.capacity() * Float.BYTES,
            null,
            GL30.GL_STREAM_DRAW
        );
    }

    public void render(List<RenderPart> toRender, Camera camera) {
        for (RenderPass pass : renderPasses) {
            ArrayList<RenderCommand> commands = new ArrayList<>();
            for (RenderPart part : toRender) {
                if (pass.accepts(part)) {
                    commands.add(new RenderCommand(part));
                }
            }

            if (!commands.isEmpty()) {
                pass.sort(commands);
                System.out.println("Draw Commands Size: " + commands.size());

                pass.begin(camera);

                drawCommands(commands, pass, camera);

                pass.end();
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

            MaterialHandle material = first.material;
            pass.bindShader(material.shader, camera);

            material.bind();

            MeshHandle mesh = first.mesh;
            Gdx.gl30.glBindVertexArray(mesh.vao);

            int count = endIndex - startIndex;
            if (count > 1) {
                System.out.println("Drawing Multiple Instances");
                drawInstanced(commands, startIndex, endIndex);
            } else {
                System.out.println("Drawing One Instance");
                drawInstanced(commands, startIndex, startIndex + 1);
            }

            startIndex = endIndex;
        }
    }


    public boolean canInstance(RenderCommand a, RenderCommand b) {
        return a.mesh == b.mesh &&
            a.material == b.material &&
            a.flags == b.flags;
    }


    public void drawInstanced(ArrayList<RenderCommand> commands, int start, int end) {
        final var gl = Gdx.gl30;

        int instanceCount = end - start;
        MeshHandle mesh = commands.get(start).mesh;

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

//        gl.glDrawElements(GL30.GL_TRIANGLES, mesh.indexCount, GL30.GL_UNSIGNED_SHORT, mesh.indexOffsetBytes);
        gl.glBindVertexArray(mesh.vao);

        //instanced draw call
        gl.glDrawElementsInstanced(
            GL20.GL_TRIANGLES,
            mesh.indexCount,
            GL20.GL_UNSIGNED_SHORT,
            mesh.indexOffsetBytes,
            instanceCount
        );

    }

    public List<RenderPart> frustumCull(Camera camera, List<RenderPart> toCull) {
        //Could be vectorized but might not be worth it.
        List<RenderPart> culled = new  ArrayList<>(toCull.size());
        for (RenderPart part : toCull) {
            Matrix4 transform = part.transform;
            BoundingBox box = part.mesh.boundingBox;
            OrientedBoundingBox orientedBoundingBox = new OrientedBoundingBox(box, transform);
            if (camera.frustum.boundsInFrustum(orientedBoundingBox)) {
                culled.add(part);
            }
        }
        System.out.println("Culled: " + (toCull.size() - culled.size()) + " elements.");
        return culled;
    }


}
