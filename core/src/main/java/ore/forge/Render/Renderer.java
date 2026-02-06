package ore.forge.Render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.BufferUtils;
import ore.forge.Stopwatch;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Is responsible For sorting, batching, and instancing draw calls. *
 */
public class Renderer {
    public static final int MAX_INSTANCED_DRAW = 10_000;
    public final AssetHandler assetHandler;
    public final ArrayList<RenderPass> renderPasses = new ArrayList<>();
    public final FloatBuffer instanceBuffer;
    private final int instanceVbo;

    public Renderer(AssetHandler assetHandler) {
        this.assetHandler = assetHandler;
        instanceBuffer = BufferUtils.newFloatBuffer(10_000_000);
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
        toRender = vectorizedFrustumCull(camera, toRender);
        for (RenderPass pass : renderPasses) {
            ArrayList<RenderCommand> commands = new ArrayList<>();
            for (RenderPart part : toRender) {
                if (pass.accepts(part)) {
                    commands.add(new RenderCommand(part));
                }
            }

            if (!commands.isEmpty()) {
                pass.sort(commands);

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
                System.out.println("Draw Commands Size: " + count +  "\nFPS: " + Gdx.graphics.getFramesPerSecond());
                drawInstanced(commands, startIndex, endIndex);
            } else {
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
        return culled;
    }

    public List<RenderPart> vectorizedFrustumCull(Camera camera, List<RenderPart> toCull) {
        var visible = new ArrayList<RenderPart>(toCull.size());

        // 2 corners per AABB (min + max)
        float[] aabbData = new float[toCull.size() * 6];

        // pack all AABBs contiguously
        for (int i = 0; i < toCull.size(); i++) {
            var bb = toCull.get(i).mesh.boundingBox;
            int base = i * 6;

            aabbData[base]     = bb.min.x;
            aabbData[base + 1] = bb.min.y;
            aabbData[base + 2] = bb.min.z;

            aabbData[base + 3] = bb.max.x;
            aabbData[base + 4] = bb.max.y;
            aabbData[base + 5] = bb.max.z;
        }

        Vector3 center = new Vector3();
        Vector3 extent = new Vector3();

        for (int i = 0; i < toCull.size(); i++) {
            RenderPart part = toCull.get(i);
            int base = i * 6;

            // reconstruct AABB
            Vector3 min = center.set(
                aabbData[base],
                aabbData[base + 1],
                aabbData[base + 2]
            );

            Vector3 max = extent.set(
                aabbData[base + 3],
                aabbData[base + 4],
                aabbData[base + 5]
            );

            // center + half extents
            center.set(min).add(max).scl(0.5f);
            extent.set(max).sub(min).scl(0.5f);

            // transform center into world space
            center.mul(part.transform);

            if (camera.frustum.boundsInFrustum(center, extent)) {
                visible.add(part);
            }
        }

        return visible;
    }



}
