package ore.forge.engine.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class MaterialHandle {

    // Shader used to render this material
    public ShaderProgram shader;

    // glTF backing material (recommended)
    public com.badlogic.gdx.graphics.g3d.Material gdxMaterial;

    // Textures
    public Texture baseColorTexture;
    public Texture normalTexture;
    public Texture metallicRoughnessTexture;
    public Texture emissiveTexture;

    // Static material parameters
    public float metallic = 1.0f;
    public float roughness = 1.0f;
    public float emissiveStrength = 0.0f;

    // Render state
    public boolean transparent = false;
    public boolean depthWrite = true;
    public boolean depthTest = true;
    public int cullFace = GL20.GL_BACK;

    // Optional: atlas region
    public float u0 = 0f, v0 = 0f, u1 = 1f, v1 = 1f;

    public void bind() {
        // textures (example bindings)
        shader.bind();
//        if (baseColorTexture != null) {
//            baseColorTexture.bind(0);
//            shader.setUniformi("u_texture", 0);
//        }

//        if (normalTexture != null) {
//            normalTexture.bind(1);
//            shader.setUniformi("u_normalTex", 1);
//        }
//
//        // static uniforms
//        shader.setUniformf("u_metallic", metallic);
//        shader.setUniformf("u_roughness", roughness);
//        shader.setUniformf("u_emissiveStrength", emissiveStrength);
    }
}

