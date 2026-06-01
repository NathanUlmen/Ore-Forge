package ore.forge.engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Pool;
import ore.forge.engine.GpuResourceManager;
import ore.forge.engine.render.passes.RenderPass;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class Renderer {
    private static final int FLOATS_PER_INSTANCE = 16;
    private static final int BYTES_PER_INSTANCE = FLOATS_PER_INSTANCE * Float.BYTES;
    private static final int INSTANCE_ATTRIB_LOCATION = 4;
    private final int instanceVbo;
    private FloatBuffer instanceBuffer;
    private int instanceCapacity;

    private final GpuResourceManager gpuResourceManager;
    private final ArrayList<RenderPass> renderPasses = new ArrayList<>();
    private final ArrayList<RenderCommand> commandBuffer = new ArrayList<>();
    private final Pool<RenderCommand> commandPool;


    public Renderer(GpuResourceManager gpuResourceManager) {
        commandPool = new  Pool<RenderCommand>(1024) {
            @Override
            public RenderCommand newObject() {
                return new RenderCommand();
            }
        };

        this.gpuResourceManager = gpuResourceManager;
        this.instanceCapacity = 1024;
        this.instanceBuffer = BufferUtils.newFloatBuffer(instanceCapacity * FLOATS_PER_INSTANCE);
        this.instanceVbo = Gdx.gl30.glGenBuffer();
        Gdx.gl30.glBindBuffer(GL30.GL_ARRAY_BUFFER, instanceVbo);
        Gdx.gl30.glBufferData(
            GL30.GL_ARRAY_BUFFER,
            instanceCapacity * BYTES_PER_INSTANCE,
            null,
            GL30.GL_STREAM_DRAW
        );
        Gdx.gl30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
    }

    public void render(List<RenderPart> toRender, Camera camera) {
        for (RenderPass renderPass : renderPasses) {
            for (RenderCommand command :  commandBuffer) {
                commandPool.free(command);
            }
            commandBuffer.clear();

            //Create RenderCommands
            for (RenderPart renderPart : toRender) {
                if (renderPass.accepts(renderPart)) {
                    RenderCommand renderCommand = commandPool.obtain();
                    renderCommand.init(renderPart);
                    commandBuffer.add(renderCommand);
                }
            }

            if (!commandBuffer.isEmpty()) {
                renderPass.sort(commandBuffer);

                renderPass.begin(camera);

                drawCommands(renderPass, camera);

                renderPass.end();
            }

        }
    }

    private void drawCommands(RenderPass renderPass, Camera camera) {
        int startIndex = 0;

        while (startIndex < commandBuffer.size()) {
            RenderCommand first = commandBuffer.get(startIndex);

            int endIndex = startIndex + 1;
            while (endIndex < commandBuffer.size()
                && canInstance(first, commandBuffer.get(endIndex))) {
                endIndex++;
            }

            GpuMeshResource meshResource = (GpuMeshResource) gpuResourceManager.getGpuResource(first.meshHandle);
            GpuTextureResource textureResource = (GpuTextureResource) gpuResourceManager.getGpuResource(first.materialHandle.baseColorTexture);
            textureResource.texture().bind();
            renderPass.bindShader(renderPass.currentShader, camera);
            VertexBufferObjectWithVAO vbo = meshResource.vbo();
            vbo.bind(renderPass.currentShader);
            meshResource.ibo().bind();

            int instanceCount = endIndex - startIndex;
            ensureInstanceCapacity(instanceCount);
            bindInstanceAttributes();
            uploadInstanceData(startIndex, endIndex);
            drawInstanced(meshResource, instanceCount);

            meshResource.ibo().unbind();
            vbo.unbind(renderPass.currentShader);
            startIndex = endIndex;
        }
    }

    private void ensureInstanceCapacity(int instanceCount) {
        if (instanceCount <= instanceCapacity) {
            return;
        }

        instanceCapacity = Math.max(instanceCapacity * 2, instanceCount);
        instanceBuffer = BufferUtils.newFloatBuffer(instanceCapacity * FLOATS_PER_INSTANCE);
        Gdx.gl30.glBindBuffer(GL30.GL_ARRAY_BUFFER, instanceVbo);
        Gdx.gl30.glBufferData(
            GL30.GL_ARRAY_BUFFER,
            instanceCapacity * BYTES_PER_INSTANCE,
            null,
            GL30.GL_STREAM_DRAW
        );
        Gdx.gl30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
    }

    private void bindInstanceAttributes() {
        Gdx.gl30.glBindBuffer(GL30.GL_ARRAY_BUFFER, instanceVbo);

        for (int column = 0; column < 4; column++) {
            int location = INSTANCE_ATTRIB_LOCATION + column;
            int offsetBytes = column * 4 * Float.BYTES;

            Gdx.gl30.glEnableVertexAttribArray(location);
            Gdx.gl30.glVertexAttribPointer(
                location,
                4,
                GL20.GL_FLOAT,
                false,
                BYTES_PER_INSTANCE,
                offsetBytes
            );
            Gdx.gl30.glVertexAttribDivisor(location, 1);
        }
    }

    private void uploadInstanceData(int startIndex, int endIndex) {
        instanceBuffer.clear();
        for (int i = startIndex; i < endIndex; i++) {
            instanceBuffer.put(commandBuffer.get(i).worldTransform.val);
        }
        instanceBuffer.flip();

        Gdx.gl30.glBindBuffer(GL30.GL_ARRAY_BUFFER, instanceVbo);
        Gdx.gl30.glBufferSubData(
            GL30.GL_ARRAY_BUFFER,
            0,
            instanceBuffer.remaining() * Float.BYTES,
            instanceBuffer
        );
    }

    private void drawInstanced(GpuMeshResource meshResource, int instanceCount) {
        Gdx.gl30.glDrawElementsInstanced(
            GL20.GL_TRIANGLES,
            meshResource.indexCount(),
            meshResource.indexType(),
            meshResource.indexOffsetBytes(),
            instanceCount
        );
    }

    public boolean canInstance(RenderCommand a, RenderCommand b) {
        return a.meshHandle == b.meshHandle &&
            a.materialHandle == b.materialHandle &&
            a.flags == b.flags;
    }

    public void addRenderPass(RenderPass renderPass) {
        renderPasses.add(renderPass);
    }

    public void removeRenderPass(RenderPass targetPass) {
        renderPasses.remove(targetPass);
    }


}
