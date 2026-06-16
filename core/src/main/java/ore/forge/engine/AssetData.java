package ore.forge.engine;

import com.badlogic.gdx.utils.Disposable;

/**@author Nathan Ulmen
 *
 *
 * */
public sealed interface AssetData permits AnimationData, MaterialData, MeshData, TextureData {
}
