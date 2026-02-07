package ore.forge.Render;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.collision.BoundingBox;


/**
 * Mesh Handle stores the index range for the target mesh that is to be drawn*/
public class MeshHandle {

    // Geometry binding
    public int vao; //pointer to
    public int instanceVBO;

    // Indexed draw info
    public int indexCount; //start index of mesh?
    public int indexOffsetBytes = 0; //end index of mesh?

    // Topology
    public int primitiveType = GL20.GL_TRIANGLES;
    public int strideBytes = 0;

    public VertexAttributes vertexAttributes;


    public BoundingBox boundingBox;

    @Override
    public String toString() {
        return "VAO: " + vao + "\t IndexCount: " + indexCount + "\t IndexOffsetBytes: " + indexOffsetBytes;
    }

    public MeshHandle() {
        //To create a VBO(vertex buffer object) or EBO (element buffer object) call GL20.glGenBuffer(), returns a pointer/id to that buffer;

        //To create a VAO(Vertex Attribute object) call GL30.glGenVertexArrays();
    }

}
