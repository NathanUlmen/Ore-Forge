# Resource Management System

The goal of the Resource Management System is to provide an interface/abstraction/API where the loading, tracking, and disposal of expensive resources such as Meshes, Materials, Textures, Animations, and Sound Files is done automatically. All other systems need do is provide an **AssetID** and the Resource management system will handle the loading and automatically dispose of the resource using a configurable caching policy.

## Engine Asset Categories 

Animation

Mesh

Materials

Textures

Sound File


## Supported Formats

Currently the only way to import Animations, Meshes, Materials, and Textures is via the .gltf/.glb format.

Currently the only supported Sound File format is **[TODO]**

## How it works

When an asset is going to be used in the engine it first must be imported. When an asset is first imported it is assigned an **AssetID**. This is a stable reference, meaning that it doesn't change between program exectutions. The **AssetID** is what other systems in the engine will use to reference that asset. 

After being assigned an **AssetID** the engine will then extract the asset and transform it into a native format and write it to a .\*bin file. Then an entry containing necessary information about the asset is added to the registry. On engine startup the registry is loaded into memory where it can be used by other parts of the system to resolve **AssetIDs** into usable resources.

<details>
<summary><b>Asset Registry JSON example</b></summary>

```json
{
    "class": "ore.forge.engine.importing.AssetRegistry$AssetRegistryData",
    "assetId": "546959c6-3cd7-4bb0-b656-1f2958247a4e",
    "artifact": {
        "filePath": "baked_assets/meshes/Cube.meshbin",
        "sourceKey": {
            "assetType": "MESH",
            "logicalName": "texture_test",
            "assetName": "Cube",
            "sourcePath": "models/texture_test.glb",
            "importVersion": 1
        },
        "dependencies": null,
        "assetId": "546959c6-3cd7-4bb0-b656-1f2958247a4e"
    }
}
```
</details>

## Reference Counting

## Threaded Resource Loading

## Resource Caching 
