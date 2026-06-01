# Ore Forge / Forge Engine

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

## Tools
- `tools/schema_designer.html` is a standalone browser-based schema-authoring prototype that exports schema JSON for future editor/runtime integration.

## Status
Active development.
