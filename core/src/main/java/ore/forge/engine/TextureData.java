package ore.forge.engine;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;

/**
 * @author Nathan Ulmen
 * Stores Image/Texture data in memory.
 *
 *
 */
public final class TextureData implements AssetData, Disposable {
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
        pixmap.dispose();
    }

}
