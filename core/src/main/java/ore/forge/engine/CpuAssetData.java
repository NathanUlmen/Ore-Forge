package ore.forge.engine;

import com.badlogic.gdx.utils.Disposable;

/**@author Nathan Ulmen
 *
 *
 * */
public sealed interface CpuAssetData extends Disposable permits AnimationData, MaterialData, MeshData, TextureData {
}
