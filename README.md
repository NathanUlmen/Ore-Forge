 # Ore Forge / Forge Engine

 **Ore Forge** is an isometric 3D incremental factory tycoon game.  
 **Forge** is the custom engine that powers it, focused on efficient rendering, asset reuse, and scalable content pipelines.

 ## Highlights
 - Entity component driven architecture (ECS) for modular, data oriented gameplay systems.
 - Render pipeline designed for batching, instancing, and multiple render passes.
 - Centralized asset handling for meshes, materials, and textures.
 - Performance goals aimed at smooth gameplay on modest hardware.

 ## Project Structure
 - `core/` game + engine code
 - `lwjgl3/` desktop launcher
 - `assets/` art, models, textures, and runtime assets
 - `documentation/` design docs and outlines

 ## Requirements
 - Java 21 (Gradle toolchain resolver is configured)

 ## Build and Run (Desktop)
 ```bash
 ./gradlew lwjgl3:run
 ```

 ## Documentation
 - `documentation/DesignDoc.md` game design and systems
 - `documentation/Outline.md` rendering/asset pipeline notes

 ## Status
 Active development.
