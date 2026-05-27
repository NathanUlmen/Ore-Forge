package ore.forge.engine.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public final class GpuTextureResource implements GpuResource {
    private final Texture texture;

    public GpuTextureResource(Pixmap pixmap) {
        this.texture = new Texture(pixmap);
    }

    public Texture texture() {
        return texture;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

}
