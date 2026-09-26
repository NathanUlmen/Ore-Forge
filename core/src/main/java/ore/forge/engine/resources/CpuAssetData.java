package ore.forge.engine.resources;

import ore.forge.engine.Sizeable;

/**@author Nathan Ulmen
 *
 * */
public sealed interface CpuAssetData extends Sizeable permits AnimationData, MaterialData, MeshData, TextureData {
}
