package ore.forge.engine;

import com.badlogic.gdx.utils.Disposable;

/**@author Nathan Ulmen
 *
 *
 * */
public sealed interface CpuAssetData permits AnimationData, MaterialData, MeshData, TextureData {
}
