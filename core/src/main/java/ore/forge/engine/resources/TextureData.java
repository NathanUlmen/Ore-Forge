package ore.forge.engine.resources;

import com.badlogic.gdx.graphics.Pixmap;

/**
 * @author Nathan Ulmen
 * Stores Image/Texture data in memory.
 *
 *
 */
public final class TextureData implements CpuAssetData {
    private final byte[] encodedData;
    private Pixmap pixmap;

    public TextureData(byte[] encodedData) {
        this.encodedData = encodedData;
        pixmap = null;
    }

    public Pixmap pixmap() {
        if (pixmap == null) {
            pixmap = new Pixmap(encodedData, 0, encodedData.length);
        }
        return pixmap;
    }

    public byte[] encodedBytes() {
        return encodedData;
    }


    @Override
    public void dispose() {
        if (pixmap != null) {
            pixmap.dispose();
        }
    }

}
