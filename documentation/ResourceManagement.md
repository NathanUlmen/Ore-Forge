# Resource Management System

The goal of the Resource Management System is to provide an interface/abstraction/API where the loading, tracking, and disposal of expensive resources such as Meshes, Materials, Textures, Animations, and Sound Files is done automatically. All other systems need do is provide an **AssetID** and the Resource management system will handle the loading and automatically dispose of the resource using a configurable caching policy.

Key ideas: only 1 thread can issue loading "calls/requests" at a time. When and where callbacks are ran is determined 
by the dispatcher passed in.
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

## Testing

* Simple load, 1 call and wait to get a result. - tests to see if we have basic functionality working

* Simple spaced load. 1 call wait then make another call - tests already loaded pathway

* Issue multiple load requests for one target. - tests in progress loading

* load delete load - tests that our removal/disposal is functioning correctly.

* Testing the handling of null and non-null callbacks and dispatchers. - callbacks arent necessary but if a callback is present and dispatcher isn't should throw an error.

* testing that scheduling of callbacks and their dispatchers for both loaded and in progress callbacks is correct - callbacks should be sent to their dispatchers in the same order their requests were issued.

* Test invalid/already released handles - acquire resource using an already released handle
