package ore.forge.engine.resources;

import com.badlogic.gdx.graphics.Pixmap;

/**
 * @author Nathan Ulmen
 * Stores Image/Texture data in memory.
 *
 *
 */
public final class TextureData implements CpuAssetData {
    private byte[] encodedData;
    private Pixmap pixmap;
    private boolean useMipMaps;

    public TextureData(byte[] encodedData, boolean useMipMaps) {
        this.encodedData = encodedData;
        pixmap = null;
    }

    public Pixmap pixmap() {
        if (pixmap == null) {
            pixmap = new Pixmap(encodedData, 0, encodedData.length);
        }
        return pixmap;
    }

    public boolean useMipMaps() {
        return useMipMaps;
    }

    public byte[] encodedBytes() {
        return encodedData;
    }

    @Override 
    public long sizeBytes() {
        return encodedData.length;
    }

    @Override
    public void dispose() {
        if (pixmap != null) {
            pixmap.dispose();
        }
        encodedData = null;
    }

}
