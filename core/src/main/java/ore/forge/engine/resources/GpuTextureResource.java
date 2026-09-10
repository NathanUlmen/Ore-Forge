package ore.forge.engine.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;

public final class GpuTextureResource implements GpuResource {
    private final Texture texture;
    private final Pixmap.Format format;
    private final boolean useMipMaps;

    public GpuTextureResource(Pixmap pixmap, boolean useMipMaps) {
        this.texture = new Texture(new CustomPixmapTextureData(pixmap, useMipMaps));
        this.format = pixmap.getFormat();
        this.useMipMaps = useMipMaps;
    }

    public Texture texture() {
        return texture;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

    @Override
    public long sizeBytes() {
        var w = texture.getWidth();
        var h = texture.getHeight();
        long bytes = w * h * bytesFromFormat(format);
        if (useMipMaps) {
           bytes *= 4/3; 
        }
        return bytes;
    }

    private static int bytesFromFormat(Pixmap.Format format) {
        return switch(format) {
            case Alpha, Intensity -> 1;
            case LuminanceAlpha, RGB565, RGBA4444 -> 2;
            case RGB888 -> 3;
            case RGBA8888 -> 4;
        };
    }

    private static class CustomPixmapTextureData implements TextureData {
        private Pixmap pixmap;
        private final int width, height;
        private final Format format;
        private final boolean useMipMaps;

        public CustomPixmapTextureData(Pixmap pixmap, boolean useMipMaps) {
            this.pixmap = pixmap;
            this.width = pixmap.getWidth();
            this.height = pixmap.getHeight();
            this.format = pixmap.getFormat();
            this.useMipMaps = useMipMaps;
        }

        @Override
        public TextureDataType getType() {
            return TextureDataType.Pixmap;
        }

        @Override
        public boolean isPrepared() {
            return true;
        }

        @Override
        public void prepare() {
        }

        @Override
        public Pixmap consumePixmap() {
            Pixmap toReturn = pixmap;
            pixmap = null;
            return toReturn; 
        }

        @Override
        public boolean disposePixmap() {
            return true;
        }

        @Override
        public void consumeCustomData(int target) {
            throw new UnsupportedOperationException("OneShotTextureData only supports pixmap uploads");
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public Format getFormat() {
            return format;
        }

        @Override
        public boolean useMipMaps() {
            return useMipMaps;
        }

        @Override
        public boolean isManaged() {
            return false;
        }

    }

}
