# DynamicKeybinds

A multi-platform Minecraft 1.20.1 mod (Fabric + Forge/NeoForge) that allows other mods to create keybinds at runtime after the game fully loads.

## Project Structure

- **common/**: Loader-agnostic API and core behavior
  - `DynamicKeyRegistry`: Interface for managing dynamic keybinds
  - `DynamicKeyRegistryImpl`: Default implementation
  - `DynamicKeyCommandService`: Command execution interface
  - `DynamicKeyCommandServiceImpl`: Command implementation

- **fabric/**: Fabric-specific wiring and entrypoints
  - `FabricDynamicKeyInitializer`: Fabric ClientModInitializer
  - `FabricCommandRegistry`: Fabric command registration

- **forge/**: Forge/NeoForge-specific wiring and entrypoints
  - `ForgeClientEvents`: Forge event handlers
  - `ForgeCommandRegistry`: Forge command registration

## Features

- Runtime keybind registration via API
- Three debug commands:
  - `/dynamickey add <id> <keycode> <category> [action]` - Register a new keybind
  - `/dynamickey list` - List all dynamic keybinds
  - `/dynamickey remove <id>` - Remove a keybind

## Building

```bash
./gradlew build
```

Output JARs will be in:
- `fabric/build/libs/dynamickeybinds-fabric-*.jar`
- `forge/build/libs/dynamickeybinds-forge-*.jar`

## Publishing (Modrinth + CurseForge)

This project includes publish tasks for both platforms in both loaders.

### 1) Configure IDs in `gradle.properties`

```properties
modrinthProjectId=<your-modrinth-project-id>
curseforgeProjectId=<your-curseforge-project-id>
releaseType=release
changelog=Your release notes (markdown supported)
```

### 2) Set tokens in your shell/CI

```bash
export MODRINTH_TOKEN=...
export CURSEFORGE_TOKEN=...
```

### 3) Run publish tasks

- Fabric only: `./gradlew :fabric:publishToPlatforms`
- Forge only: `./gradlew :forge:publishToPlatforms`
- Both loaders: `./gradlew publishAllPlatforms`

You can also run single-platform endpoints directly:
- `:fabric:publishToModrinth`, `:fabric:publishToCurseForge`
- `:forge:publishToModrinth`, `:forge:publishToCurseForge`

## Version

- **Minecraft**: 1.20.1 (exact)
- **Mod Version**: 0.1.0
- **Group**: dev.munebase.dynamickeybinds

## License

MIT
