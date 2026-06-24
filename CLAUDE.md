# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Permissions & Command Preferences

The project allow-list in `.claude/settings.json` pre-approves certain commands so no permission prompt is shown. Prefer these over equivalents that would trigger a prompt:

- **File reads** — use `Read`, `grep *`, `ls *`, `head *`, `tail *` rather than other inspection tools
- **Text processing** — `sed *`, `sort *`, `uniq *`, `wc *`, `diff *`, `jq *`, `echo *`, `printf *`
- **Build & test** — use the exact `./gradlew` forms listed in [Build & Run Commands](#build--run-commands); all are pre-approved
- **Location/existence checks** — `which *`, `pwd`
- **Web lookups** — `WebFetch` is pre-approved for fetching URLs

Chain these freely — the bash-chain hook validates each segment against the same list, so a pipeline of allowed commands (`grep * | sort | uniq`) is auto-approved without prompting.

Only reach for a command outside this list when nothing on it can accomplish the task — in that case a prompt is expected and fine.

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device/emulator
./gradlew installDebug

# Run all tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew test --tests "com.natron.commander.YourTestClass"

# Lint
./gradlew lint

# Clean build
./gradlew clean assembleDebug
```

## Architecture

This is a single-module Android app using Jetpack Compose and MVVM. State flows one way: `GameViewModel` holds all mutable state as `StateFlow<GameState>`, and Composables observe it via `collectAsState()`.

### Data Model
- `GameState` is the single root state object — it contains everything: player list, UI visibility flags (sheet open/closed), active dice result, and which player is targeted for commander damage or direct input.
- `Player` carries life total, poison counters, and a `Map<sourcePlayerId, damage>` for per-opponent commander damage.
- `PlayerColor` is an enum of the 6 Magic mana colors (Plains, Island, Swamp, Mountain, Forest, Gold), each with a triple of primary/surface/onSurface colors used throughout the UI.

### ViewModel
`GameViewModel` is the only ViewModel. It handles:
- Setup flow (player count → names/colors → start game)
- All game mechanics (life adjust, commander damage, poison)
- Elimination detection — three loss conditions checked after every mutation: life ≤ 0, poison ≥ 10, single-source commander damage ≥ 21
- UI state toggles (dice roller sheet, commander damage sheet, direct input dialog, new game dialog)

### Navigation
Two-screen navigation in `AppNavigation.kt`: `setup` and `game`. Transitions are triggered by `LaunchedEffect` watching `gameState.gameStarted` and a `navigateToSetup` flag on `GameState`.

### UI Layout
`GameScreen` renders a dynamic grid that adapts for 2–6 players. Opposing player cards are rotated 180° so each player faces their own card. `LifeAdjustButton` implements tap-for-one / hold-for-continuous with acceleration after 500ms.

## Key Dependencies

Managed via version catalog at `gradle/libs.versions.toml`:
- Compose BOM 2024.10.00
- Navigation Compose 2.8.3
- Lifecycle 2.8.6 (ViewModel + StateFlow integration)
- Material3
- Min SDK 26 / Target SDK 35 / Kotlin 2.0.21
