package ore.forge.Shaders;

import com.badlogic.gdx.graphics.g3d.Attribute;

public class GridAttribute extends Attribute {
    public static final long ID = 0xFFF321;

    public GridAttribute(long type) {
        super(type);
    }

    @Override
    public Attribute copy() {
        return null;
    }

    @Override
    public int compareTo(Attribute o) {
        return 0;
    }
}
