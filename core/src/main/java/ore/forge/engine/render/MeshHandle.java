package ore.forge.engine.render;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import ore.forge.engine.importing.AssetID;


/**
 * Mesh Handle stores the index range for the target mesh that is to be drawn*/
public final class MeshHandle implements AssetHandle {
    public BoundingBox boundingBox;

    public MeshHandle() {

    }

}
