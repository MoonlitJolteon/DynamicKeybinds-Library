# DynamicKeybinds

A multi-platform Minecraft 1.21.1 mod (Fabric + NeoForge) that allows other mods to create keybinds at runtime after the game fully loads.

## Project Structure

- **common/**: Loader-agnostic API and core behavior
  - `DynamicKeyRegistry`: Interface for managing dynamic keybinds
  - `DynamicKeyRegistryImpl`: Default implementation
  - `DynamicKeyCommandService`: Command execution interface
  - `DynamicKeyCommandServiceImpl`: Command implementation

- **fabric/**: Fabric-specific wiring and entrypoints
  - `FabricDynamicKeyInitializer`: Fabric ClientModInitializer
  - `FabricCommandRegistry`: Fabric command registration

- **neoforge/**: NeoForge-specific wiring and entrypoints
  - `NeoForgeClientEvents`: NeoForge event handlers
  - `NeoForgeCommandRegistry`: NeoForge command registration

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
- `neoforge/build/libs/dynamickeybinds-neoforge-*.jar`

## Using with JitPack

This repository is configured for JitPack via `jitpack.yml` (Java 21 + Gradle publish to local Maven).

### 1) Create a Git tag and push

```bash
git tag v0.0.1
git push origin v0.0.1
```

### 2) Add JitPack repository in the consuming project

```gradle
repositories {
  mavenCentral()
  maven { url 'https://jitpack.io' }
}
```

### 3) Add dependency

Because this is a multi-module project, depend on a module artifact:

```gradle
dependencies {
  // API/common module
  implementation "com.github.<GitHubUser>.DynamicKeybinds:common:v0.0.1"

  // Loader-specific modules (if needed directly)
  // implementation "com.github.<GitHubUser>.DynamicKeybinds:fabric:v0.0.1"
  // implementation "com.github.<GitHubUser>.DynamicKeybinds:neoforge:v0.0.1"
}
```

Replace `<GitHubUser>` with your GitHub username or org and `v0.0.1` with your tag.

If you are unsure of the exact coordinate JitPack generated, open:

`https://jitpack.io/#<GitHubUser>/DynamicKeybinds/<tag>`

JitPack will show the exact dependency snippet after the build finishes.

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
- NeoForge only: `./gradlew :neoforge:publishToPlatforms`
- Both loaders: `./gradlew publishAllPlatforms`

You can also run single-platform endpoints directly:
- `:fabric:publishToModrinth`, `:fabric:publishToCurseForge`
- `:neoforge:publishToModrinth`, `:neoforge:publishToCurseForge`

## Version

- **Minecraft**: 1.21.1 (1.20.1 builds live on the `main` branch)
- **Java**: 21
- **Mod Version**: 0.1.3
- **Group**: dev.munebase.dynamickeybinds

## License

MIT
