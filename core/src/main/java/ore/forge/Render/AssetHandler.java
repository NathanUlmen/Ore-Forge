package ore.forge.Render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.utils.BufferUtils;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.*;


/**
 * All models coming in will have same properties and will be exported from blender
 * They will have
 * Position, Normal, Texture Coord, Tangent, Color
 * <p>
 * This is the format the engine expects and will expect. Maybe in future add default support
 * <p>
 * The pipeline plan for meshes:
 * 1. load and extract buffers:
 * for each mesh in assets
 * Load data into libgdx mesh object using GLTFLoader.
 * Extract vertices and indices from the mesh and store temporarily.
 * <p>
 * 2. Create "Master" vbo & EBO
 * allocate a vbo and EBO large enough to hold all our data.
 * for each buffer we extracted
 * add to the "Master" vbo and EBOP the vertex and index buffes
 * record the start and end indices for each buffer, this will be used in {@link MeshHandle}
 * <p>
 * 3. Creat VAOs f
 * <p>
 * 3. Dispose of all old meshes.
 *
 */
public class AssetHandler {
    // floats per vertex
    // position (3) + normal (3) + uv (2) + tangent (4) + color (4)
    static final int FLOATS_PER_VERTEX = 6;
    static int STRIDE_BYTES;
    public int masterVBO, masterEBO;
    public ArrayList<MeshHandle> meshHandles;
    public final Map<String, MeshHandle> meshes = new HashMap<>();
    public final Map<String, TextureHandle> textures = new HashMap<>();
    public final Map<String, MaterialHandle> materials = new HashMap<>();

    public AssetHandler() {

    }

    public MeshHandle loadTestMesh() {
        //first load our test asset using gltfloader
        GLTFLoader loader = new GLTFLoader();
        SceneAsset sceneAsset = loader.load(Gdx.files.internal("models/myModel.gltf"));
        var nodes = sceneAsset.scene.model.nodes;
        //assert that the scene asset is only one mesh
        if (nodes.size != 1 || nodes.first().parts.size != 1) {
            Gdx.app.error("Mesh Loading Error", "More than one node in a model.");
            Gdx.app.exit();
        }

        //extract mesh into packed mesh.
        ArrayList<PackedMesh> packedMeshes = new ArrayList<>();
        for (Node node : sceneAsset.scene.model.nodes) {
            for (NodePart part : node.parts) {
                packedMeshes.add(extractMesh(part.meshPart.mesh));
            }
        }

        //Figure out how large of a buffer we need to allocate
        int totalVertices = 0;
        int totalIndices = 0;
        for (PackedMesh packedMesh : packedMeshes) {
            totalVertices += packedMesh.vertexCount();
            totalIndices += packedMesh.indexCount();
        }

        //create VBO and EBO
        GL30 gl = Gdx.gl30;
        masterVBO = gl.glGenBuffer();
        masterEBO = gl.glGenBuffer();


        //bind vbo
        gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, masterVBO);
        gl.glBufferData(
            GL30.GL_ARRAY_BUFFER,
            totalVertices * STRIDE_BYTES,
            null,
            GL30.GL_STATIC_DRAW
        );

        //bind ebo
        gl.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, masterEBO);
        gl.glBufferData(
            GL30.GL_ELEMENT_ARRAY_BUFFER,
            totalIndices * Short.BYTES,
            null,
            GL30.GL_STATIC_DRAW
        );

        //load all our data into masterEBO and masterVBO and create a handle for each mesh.
        int vertexOffset = 0;
        int indexOffsetBytes = 0;
        meshHandles = new ArrayList<>(packedMeshes.size());
        for (PackedMesh packedMesh : packedMeshes) {
            gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, masterVBO);
            gl.glBufferSubData(GL30.GL_ARRAY_BUFFER, vertexOffset * STRIDE_BYTES, packedMesh.vertices().remaining() * Float.BYTES, packedMesh.vertices());

            ShortBuffer baked = BufferUtils.newShortBuffer(packedMesh.indexCount());
            ShortBuffer src = packedMesh.indices().duplicate();
            src.clear();

            while (src.hasRemaining()) {
                int idx = (src.get() & 0xFFFF) + vertexOffset;
                // For now, this must fit in 0..65535 or you’ll overflow.
                if (idx > 0xFFFF) {
                    throw new IllegalStateException("Index overflow: " + idx + " (too many vertices for ushort)");
                }
                baked.put((short) idx);
            }
            baked.flip();

            gl.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, masterEBO);
            gl.glBufferSubData(
                GL30.GL_ELEMENT_ARRAY_BUFFER,
                indexOffsetBytes,
                baked.remaining() * Short.BYTES,
                baked
            );



            MeshHandle handle = new MeshHandle();
            handle.indexCount = packedMesh.indexCount();
            handle.indexOffsetBytes = indexOffsetBytes;
            handle.boundingBox = packedMesh.boundingBox();

            meshHandles.add(handle);

            vertexOffset += packedMesh.vertexCount();
            indexOffsetBytes += packedMesh.indexCount() * Short.BYTES;
        }
        createVaos(meshHandles);
        System.out.println(meshHandles.size());
        System.out.println(meshHandles.getFirst().toString());

        //dispose of our old allocations now that everything is in one big buffer
//        for (SceneAsset asset : assets) {
//            asset.dispose();
//        }
        return meshHandles.getFirst();
    }


    private PackedMesh extractMesh(Mesh mesh) {
        final int vertexCount = mesh.getNumVertices();
        final int indexCount  = mesh.getNumIndices();

        final int strideBytes = mesh.getVertexSize();
        STRIDE_BYTES = strideBytes;

        // ---- vertices ----
        FloatBuffer vb = mesh.getVerticesBuffer(false).duplicate();
        vb.clear();

        int floatsPerVertex = strideBytes / 4;
        int floatCountToCopy = Math.min(vertexCount * floatsPerVertex, vb.remaining());

        FloatBuffer vertices = BufferUtils.newFloatBuffer(floatCountToCopy);
        int oldLimit = vb.limit();
        vb.limit(vb.position() + floatCountToCopy);
        vertices.put(vb).flip();
        vb.limit(oldLimit);

        // ---- indices as SHORT ----
        Buffer ib = mesh.getIndicesBuffer(false);
        if (!(ib instanceof ShortBuffer sb)) {
            throw new IllegalStateException("Expected ShortBuffer indices, got: " + ib.getClass().getName());
        }

        ShortBuffer src = sb.duplicate();

        ShortBuffer indices = BufferUtils.newShortBuffer(indexCount);
        for (int i = 0; i < indexCount; i++) {
            indices.put(src.get()); // keep as-is
        }
        indices.flip();

        return new PackedMesh(
            vertices,
            indices,
            vertexCount,
            indexCount,
            mesh.calculateBoundingBox()
        );
    }



    void createVaos(List<MeshHandle> meshes) {
        final GL30 gl = Gdx.gl30;

        for (MeshHandle mesh : meshes) {
            // 1. Allocate buffer for 1 VAO id
            IntBuffer vaoBuffer = BufferUtils.newIntBuffer(8);
            vaoBuffer.clear();
            // 2. Generate VAO
            gl.glGenVertexArrays(8, vaoBuffer);

            // 3. Read the generated ID
            mesh.vao = vaoBuffer.get(0);

            if (mesh.vao == 0) {
                throw new IllegalStateException("Failed to create VAO");
            }

            // 4. Bind VAO
            gl.glBindVertexArray(mesh.vao);

            // Bind shared buffers
            gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, masterVBO);
            gl.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, masterEBO);

            // position (offset 0)
            int offset = 0;
            gl.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, STRIDE_BYTES, offset);
            gl.glEnableVertexAttribArray(0);
            offset += 3 *  Float.BYTES;

            // normal (offset = 3 floats = 12 bytes)
            gl.glVertexAttribPointer(1, 3, GL30.GL_FLOAT, false, STRIDE_BYTES, offset);
            gl.glEnableVertexAttribArray(1);
            offset += 3 *  Float.BYTES;


            mesh.instanceVBO = gl.glGenBuffer();
            gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, mesh.instanceVBO);
            gl.glBufferData(GL30.GL_ARRAY_BUFFER, Renderer.MAX_INSTANCED_DRAW * 16 * Float.BYTES, null, GL30.GL_STREAM_DRAW);

            for (int i = 0; i < 4; i++) {
                int loc = 2 + i;
                gl.glEnableVertexAttribArray(loc);
                gl.glVertexAttribPointer(loc, 4, GL30.GL_FLOAT, false, 16 * Float.BYTES, i * 4 * Float.BYTES);
                gl.glVertexAttribDivisor(loc, 1);
            }

            gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
            gl.glBindVertexArray(0);

        }
    }

}
