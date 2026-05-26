package ore.forge.engine;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;

/**@author Nathan Ulmen
 * Stores Image/Texture data in memory.
 *
 * */
public final class TextureData implements AssetData, Disposable {
    private final Pixmap pixmap;

    public  TextureData(ByteBuffer imageData, int offset, int length) {
        this.pixmap = new Pixmap(imageData, offset, length);
    }

    @Override
    public void dispose() {
        pixmap.dispose();
    }

}
