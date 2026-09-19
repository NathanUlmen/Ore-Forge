package ore.forge.engine.resources;

import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.engine.Sizeable;

/**@author Nathan Ulmen
 *
 * */
public sealed interface CpuAssetData extends Sizeable permits AnimationData, MaterialData, MeshData, TextureData {
}
