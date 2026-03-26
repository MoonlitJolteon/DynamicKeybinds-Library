# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.3] - 2026

### Added
- Modrinth project ID
- Networked registry abstraction
- Command helpers
- unregister by id functiom

### Changed
- Unified Fabric/Forge command path and syncing

### Fixed
- Field reflection works if the mod using the library uses mojmaps now
- redundant update packet after adding keybind
- Registering/Unregistering keybinds actually syncs now instead of needing manual networking calls

### Removed
- Testing functions from when this was just a POC

## [0.0.2] - 2026-03-25

### Added
- Changelog file

### Changed
- All NBT files are now stored compressed

### Fixed
- Client keybind state is now actually handled in memory and no longer saved locally from early testing.
- Default `/dynamickey add` action payload handling fixed in Forge command flow.

## [0.0.1] - 2026-03-23

### Added
- Initial release of DynamicKeybinds for Minecraft 1.20.1.

[Unreleased]: https://github.com/munebase/dynamickeybinds/compare/v0.0.2...HEAD
[0.0.2]: https://github.com/munebase/dynamickeybinds/releases/tag/v0.0.2
[0.0.1]: https://github.com/munebase/dynamickeybinds/releases/tag/v0.0.1