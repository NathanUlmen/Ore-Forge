An "Asset" is:

Animation

Mesh

Material

Texture.


Assets are imported into the engine as a .glb/.gltf
Engine will parse assets into their individual parts.

* All Assets Need an ID/Reference that is unique.

* All Assets Need a Display Name

* All Assets should have meta data that leads to source file (file path)


Will use a registry approach, via  a registry of ID's

* ID's have following convention: [SOURCE_FILE_NAME.ID]

EX: forest_fall.tree_big02

Enables things like: forest_winter.tree_big01


*Must enforce users to name/tag assets with unique ID/reference.


* If an ID collision occurs/duplicate is loaded prompt user to see if we should replace old data associated with asset with new data if there is a difference. 

* On import we also should handle case where filename changes but asset data doesnt to prevent redundancy/bloat/duplicate data. 

* Registry has list of all assets shipped with program.

* At beginning of engine execution load all references/IDS shipped with build/program. (This is inefficient but only really becomes a problem when engine has 100,000s of unique assets, can fix it later during a second pass)

* Registry handles loading of assets from disk to memory. Is the "broker" of AssetHandles. Reference counts assets.

* Gives in memory data to other system that manages GPU resources. (should this registry do that too?)


## First Pass:
 
Create a class that extracts assets from .glb/.gltf container files, and deposits them into
a file in an engine usable state (fast and easy to parse)

For each asset imported this way, we will also create an AssetRecord and append that to a registry/database.

First passes goal is to get something workable/usable so that other systems can be iterated on, but also flexible enough
that we can go back and refine and improve it.

---
# UUID/GUID generation and requirements

* Need to be persistent and deterministic. 
  - GUID generation should be deterministic. If I put the same crate file except its renamed into the engine it should
  collide with the one that currently exists in the registry. This helps guard against redundant fils and renaming of 
  assets.

Identity of an asset is its value, not its name.



(filePath, assetType, assetName) -> UUID -> assetData



