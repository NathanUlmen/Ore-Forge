package ore.forge.Render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.utils.BufferUtils;
import net.mgsx.gltf.data.GLTFAsset;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.*;

import static java.nio.file.Files.find;


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
    // position (3) + normal (3) + uv (2) + tangent (4)
    static final int FLOATS_PER_VERTEX = 12;
    static int STRIDE_BYTES;
    public int masterVBO, masterEBO;
    public List<MeshHandle> meshHandles;
    public Hashtable<String, MeshHandle> handleLookup;

    public AssetHandler() {
        var sceneAssets = gatherMeshesFromDirectory("assets/models");
//        Gdx.app.log("AssetHandler", "Gathered Meshes From Directory");
        List<PackedMesh> packedMeshes = extractMeshes(sceneAssets);
//        Gdx.app.log("AssetHandler", "Extracted Meshes into packedMeshes");
        this.meshHandles = populateBuffers(packedMeshes);
//        Gdx.app.log("AssetHandler", "Populated Buffers");
        createVaos(this.meshHandles);
//        Gdx.app.log("AssetHandler", "Created VAOs");
    }

    private List<SceneAsset> gatherMeshesFromDirectory(String dirName) {
        List<SceneAsset> sceneAssets = new ArrayList<>();
        FileHandle dir = Gdx.files.internal(dirName);
        for (FileHandle file : dir.list()) {
            if (file.extension().equalsIgnoreCase("gltf")) {
                SceneAsset a = new GLTFLoader().load(file);
                sceneAssets.add(a);
            }
        }
        return sceneAssets;
    }


    private List<PackedMesh> extractMeshes(List<SceneAsset> sceneAssets) {
        ArrayList<PackedMesh> packedMeshes = new ArrayList<>();
        for (SceneAsset sceneAsset : sceneAssets) {
            if (sceneAsset.meshes.size != 1) {
                Gdx.app.error("Mesh Loading Error", "More than one node in a model.");
                Gdx.app.exit();
            }
            packedMeshes.add(extractToPackedMesh(sceneAsset.meshes.first()));
        }

        return packedMeshes;
    }

    public List<MeshHandle> populateBuffers(List<PackedMesh> packedMeshes) {
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
            totalIndices * Integer.BYTES,
            null,
            GL30.GL_STATIC_DRAW
        );

        //load all our data into masterEBO and masterVBO and create a handle for each mesh.
        int vertexOffset = 0;
        int indexOffsetBytes = 0;
        List<MeshHandle> handles = new ArrayList<>(packedMeshes.size());
        for (PackedMesh packedMesh : packedMeshes) {
            gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, masterVBO);
            gl.glBufferSubData(GL30.GL_ARRAY_BUFFER, vertexOffset * STRIDE_BYTES, packedMesh.vertices().remaining() * Float.BYTES, packedMesh.vertices());

            IntBuffer baked = BufferUtils.newIntBuffer(packedMesh.indexCount());
            IntBuffer src = packedMesh.indices().duplicate();

            while (src.hasRemaining()) {
                int idx = src.get() + vertexOffset;
                baked.put(idx);
            }
            baked.flip();

            gl.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, masterEBO);
            gl.glBufferSubData(
                GL30.GL_ELEMENT_ARRAY_BUFFER,
                indexOffsetBytes,
                baked.remaining() * Integer.BYTES,
                baked
            );

            MeshHandle handle = new MeshHandle();
            handle.id = packedMesh.id();
            handle.indexCount = packedMesh.indexCount();
            handle.indexOffsetBytes = indexOffsetBytes;
            handle.boundingBox = packedMesh.boundingBox();
            handle.vertexAttributes = packedMesh.attributes();
            handle.strideBytes = STRIDE_BYTES;

            handles.add(handle);

            vertexOffset += packedMesh.vertexCount();
            indexOffsetBytes += packedMesh.indexCount() * Integer.BYTES;
        }
        return handles;
    }

    private PackedMesh extractToPackedMesh(Mesh mesh, String id) {
        final int vertexCount = mesh.getNumVertices();
        final int indexCount = mesh.getNumIndices();

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

        Buffer ib = mesh.getIndicesBuffer(false);
        if (!(ib instanceof ShortBuffer sb)) {
            throw new IllegalStateException("Expected ShortBuffer indices, got: " + ib.getClass().getName());
        }

        ShortBuffer src = sb.duplicate();
        src.clear();

        IntBuffer indices = BufferUtils.newIntBuffer(indexCount);
        for (int i = 0; i < indexCount; i++) {
            indices.put(src.get() & 0xFFFF);
        }
        indices.flip();

        return new PackedMesh(
            vertices,
            indices,
            vertexCount,
            indexCount,
            mesh.calculateBoundingBox(),
            mesh.getVertexAttributes(),
            id
        );
    }

    private void createVaos(List<MeshHandle> meshes) {
        final GL30 gl = Gdx.gl30;

        for (MeshHandle mesh : meshes) {
            // 1. Allocate buffer for 1 VAO id
            IntBuffer vaoBuffer = BufferUtils.newIntBuffer(1);
            vaoBuffer.clear();
            // 2. Generate VAO
            gl.glGenVertexArrays(1, vaoBuffer);

            // 3. Read the generated ID
            mesh.vao = vaoBuffer.get(0);

            if (mesh.vao == 0) {
                throw new IllegalStateException("Failed to create VAO");
            }

            int index = 0;
            // 4. Bind VAO
            gl.glBindVertexArray(mesh.vao);

            // Bind shared buffers
            gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, masterVBO);
            gl.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, masterEBO);

            // You need the original mesh's VertexAttributes here.
            // Store them into your PackedMesh or MeshHandle when extracting.
            VertexAttributes attrs = mesh.vertexAttributes;
            System.out.println(attrs.toString());
            int stride = mesh.strideBytes;

            VertexAttribute pos = find(attrs, VertexAttributes.Usage.Position, 0);
            VertexAttribute nor = find(attrs, VertexAttributes.Usage.Normal, 0);
            VertexAttribute tan = find(attrs, VertexAttributes.Usage.Tangent, 0);
            VertexAttribute uv0 = find(attrs, VertexAttributes.Usage.TextureCoordinates, 0);

            if (pos == null) Gdx.app.error("VAO", "Missing POSITION");
            if (nor == null) Gdx.app.error("VAO", "Missing NORMAL");
            if (tan == null) Gdx.app.error("VAO", "Missing TANGENT");
            if (uv0 == null) Gdx.app.error("VAO", "Missing TEXCOORD_0");


            gl.glEnableVertexAttribArray(0);
            gl.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, stride, pos.offset);

            gl.glEnableVertexAttribArray(1);
            gl.glVertexAttribPointer(1, 3, GL30.GL_FLOAT, false, stride, nor.offset);

            gl.glEnableVertexAttribArray(2);
            gl.glVertexAttribPointer(2, 4, GL30.GL_FLOAT, false, stride, tan.offset);

            gl.glEnableVertexAttribArray(3);
            gl.glVertexAttribPointer(3, 2, GL30.GL_FLOAT, false, stride, uv0.offset);


            mesh.instanceVBO = gl.glGenBuffer();
            gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, mesh.instanceVBO);
            gl.glBufferData(GL30.GL_ARRAY_BUFFER, Renderer.MAX_INSTANCED_DRAW * 16 * Float.BYTES, null, GL30.GL_STREAM_DRAW);
            for (int i = 0; i < 4; i++) {
                int loc = 4 + i;
                gl.glEnableVertexAttribArray(loc);
                gl.glVertexAttribPointer(loc, 4, GL30.GL_FLOAT, false, 16 * Float.BYTES, i * 4 * Float.BYTES);
                gl.glVertexAttribDivisor(loc, 1);
            }

            gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
            gl.glBindVertexArray(0);

        }
    }

    private static VertexAttribute find(VertexAttributes attrs, int usage, int unit) {
        for (int i = 0; i < attrs.size(); i++) {
            VertexAttribute a = attrs.get(i);
            if (a.usage == usage && a.unit == unit) return a;
        }
        return null;
    }

    public MeshHandle getHandle(String targetId) {
        return handleLookup.get(targetId);
    }

}
