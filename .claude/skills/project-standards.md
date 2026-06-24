# Project Coding Standards — Commander Life Counter

These standards apply to every code change in this repository. Follow them exactly.

---

## Agent Task Workflow

When working on any task in this repository, follow this sequence exactly:

1. **Read first.** Before writing any code, read every file the task will touch. For ViewModel changes: read `GameViewModel.kt` and the relevant model files. For UI changes: read the affected screen and all components it uses.
2. **Identify blast radius.** List every file that will change. New files must land in an existing package under `com.natron.commander`.
3. **Check state implications.** If the task requires new data, add a field to `GameState` or `Player` — never create a new class to hold it.
4. **Implement.** Follow all conventions in this document exactly.
5. **Write tests.** Every ViewModel function added or modified gets a unit test. Every new Composable component gets a render test. Tests are not optional.
6. **Run `./gradlew test`.** All tests must pass. Fix failures; never skip them.
7. **Stop and ask before proceeding if any of these apply:**
   - The task requires a dependency not in `gradle/libs.versions.toml`
   - The task seems to require a second ViewModel or a new module
   - The task would change the player grid layout algorithm
   - The task's requirements are ambiguous about player count (2–6) or about which game state assumptions apply
   - The task would modify `AndroidManifest.xml` or build configuration

---

## Architecture Rules

- **One ViewModel, never two.** All game state and business logic lives in `GameViewModel`. Never create additional ViewModels.
- **Single source of truth.** All state is `GameState`. If you need a new piece of state, add a field to `GameState` or `Player`, not a new flow or class.
- **StateFlow pattern — always:**
  ```kotlin
  private val _state = MutableStateFlow(GameState())
  val state: StateFlow<GameState> = _state.asStateFlow()
  ```
  Use `_state.update { it.copy(...) }` for every mutation. Never replace `_state.value =` directly.
- **Composables never touch the ViewModel directly** (except top-level screens that receive it as a parameter). Components receive only data + callbacks.
- **Navigation is state-driven.** Screen transitions are triggered by `LaunchedEffect` observing `GameState` fields (e.g., `gameStarted`). Never call `navController.navigate()` from the ViewModel.

---

## Kotlin Conventions

- **Data classes for all models.** `GameState`, `Player`, `DiceResult` are immutable data classes. All updates use `.copy()`.
- **No mutable fields on models.** Every field in a data class is a `val`.
- **Enums for bounded sets.** Colors → `PlayerColor`, die types → `Die`. Use enums when the set of values is fixed.
- **Named parameters** when calling `.copy()` with more than one argument.
- **Extension functions** are acceptable for pure utility on data classes (no side effects).
- **No companion objects with state.** Constants that belong to a file go at file-top level.
- **Prefer `when` over long `if/else if` chains** for enum or sealed class dispatch.
- **Package:** `com.natron.commander`. New files go in the correct sub-package: `model/`, `viewmodel/`, `ui/screen/`, `ui/component/`, `ui/theme/`, `navigation/`.

### Null Safety

- **Never use `!!`** in production code. It will crash on any unexpected null.
- **`?: return`** for early exits when a nullable value must be present to proceed:
  ```kotlin
  val player = state.players.getOrNull(id) ?: return
  ```
- **`requireNotNull(value) { "invariant: ..." }`** when null indicates a programming error that should never happen at runtime.
- **`?: error("invariant: ...")`** inside lambdas where `return` is not available.
- Fields that are always non-null after a given state transition (e.g., players after `startGame()`) should be typed as non-nullable at that point. Use nullable only where null genuinely represents "not yet set."

### Import Order

Organize imports in this order. No star imports anywhere — explicit imports only.

1. Kotlin stdlib (`kotlin.*`)
2. Android framework (`android.*`)
3. AndroidX / Jetpack (`androidx.*`)
4. Compose and Material 3 (`androidx.compose.*`)
5. Project (`com.natron.commander.*`)

Remove all unused imports before finishing a task. The existing `import com.natron.commander.model.*` in `GameViewModel.kt` is a known violation to be cleaned up.

### File Structure

Organize code within files in this order:

**Composable files:** main public composable → private helper composables → preview functions.

**ViewModel files:** state initialization → public functions grouped by domain (`// region Setup`, `// region In-game`, etc.) → private helpers.

---

## Compose Conventions

### Composable signatures

**Screens** receive viewModel + pre-collected state:
```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel, state: GameState) { ... }
```

**Components** receive data + callbacks only — no ViewModel, no StateFlow:
```kotlin
@Composable
fun PlayerCard(
    player: Player,
    rotationDegrees: Float,
    onLifeAdjust: (Int) -> Unit,
    modifier: Modifier = Modifier
) { ... }
```

- `modifier: Modifier = Modifier` is always the **last** parameter with a default value.
- Callback params are named `on<Action>` (e.g., `onDismiss`, `onConfirm`, `onLifeAdjust`).
- Private helper composables within a file are marked `private fun`.

### Layout & sizing

- Use `fillMaxSize()`, `fillMaxWidth()`, `fillMaxHeight()`, and `weight(1f)` for proportional layouts — avoid hardcoded pixel sizes except for small fixed decorations (e.g., icon size, divider thickness).
- `BoxWithConstraints` when the layout must respond to available space (player grid).
- Card rotation via `graphicsLayer { rotationZ = rotationDegrees }`, not `rotate()` modifier.
- Edge-to-edge: apply `WindowInsets` padding at the screen level.

### Animations

- `AnimatedContent` for value transitions (life totals, dice results). Always provide a unique `label` string per animated element: `label = "life_${player.id}"`.
- Keep transitions simple: `fadeIn() togetherWith fadeOut()` is the project default.
- `LaunchedEffect` + coroutine for time-based interactions (long-press repeat, auto-dismiss).

### Compose Performance

- **`remember(key)`** for any expensive computation or value that must stay stable when its input hasn't changed:
  ```kotlin
  val sorted = remember(players) { players.sortedBy { it.id } }
  ```
- **`derivedStateOf`** for values derived from observed state that shouldn't themselves trigger recomposition:
  ```kotlin
  val isGameOver by remember { derivedStateOf { state.players.all { it.isEliminated } } }
  ```
- **`key(stable_id)`** for any composable produced in a loop — always use the player's stable `id`, never position:
  ```kotlin
  players.forEach { player -> key(player.id) { PlayerCard(...) } }
  ```
- **Lambda stability** — never write `{ viewModel.fn(player.id) }` directly inside a repeated composable. Capture the id in a named `val` outside the lambda, or pass a stable callback reference. Creating a new lambda on every recomposition defeats Compose's skip optimization.

### Material 3 usage

- All dialogs: `AlertDialog` from Material 3.
- All bottom sheets: `ModalBottomSheet`.
- Text fields: `OutlinedTextField`.
- Colors: always from `MaterialTheme.colorScheme` or a `PlayerColor` triple — never hardcoded hex values.
- Typography: `MaterialTheme.typography` — monospace for numbers, serif for titles, default for body.
- Never import Material 2 components (`androidx.compose.material.*`) — use Material 3 (`androidx.compose.material3.*`).

### Accessibility

- Every icon-only interactive element requires a `contentDescription`:
  ```kotlin
  Icon(painter = painterResource(R.drawable.ic_minus), contentDescription = "Decrease life")
  ```
- Decorative images use `contentDescription = null` explicitly.
- Non-button interactive surfaces (`Box` / `Surface` with `clickable`) must declare their role:
  ```kotlin
  Modifier.semantics { role = Role.Button }
  ```

### Preview Functions

Every public Composable component must have at least one `@Preview` function in the same file. Rules:

- Previews are `private`.
- Always wrap in `CommanderTheme`.
- Use realistic data — real player names, real life totals, real colors. Never placeholder strings.

```kotlin
@Preview(showBackground = true)
@Composable
private fun PlayerCardPreview() {
    CommanderTheme {
        PlayerCard(
            player = Player(id = 0, name = "Aragorn", lifeTotal = 40, colorTheme = PlayerColor.Island),
            rotationDegrees = 0f,
            onLifeAdjust = {},
            onLifeTap = {},
            onCommanderDamageTap = {},
            onPoisonAdjust = {}
        )
    }
}
```

Previews are never tested and do not count toward test coverage.

---

## State & Event Patterns

- **Elimination logic lives in one private helper — never inline.** Every player mutation ends by passing the updated player through `checkElimination()`. Never duplicate the elimination `when`-block. The correct implementation:
  ```kotlin
  private fun checkElimination(player: Player): Player {
      val eliminated = player.lifeTotal <= 0
          || player.poisonCounters >= 10
          || player.commanderDamage.values.any { it >= 21 }
      val reason = when {
          player.lifeTotal <= 0 -> EliminationReason.LIFE
          player.poisonCounters >= 10 -> EliminationReason.POISON
          player.commanderDamage.values.any { it >= 21 } -> EliminationReason.COMMANDER
          else -> null
      }
      return player.copy(isEliminated = eliminated, eliminationReason = reason)
  }
  ```
  All mutation functions use: `updatePlayer(id) { p -> checkElimination(transform(p)) }`. Adding a new elimination condition means editing `checkElimination` only.
- **Modal visibility** (sheets, dialogs) is controlled by nullable ID or Boolean fields on `GameState` — never local `remember { mutableStateOf(false) }` for shared UI state.
- **State boundary — use `GameState`/`Player` when any of these are true:** the ViewModel needs to read or modify the value; the value must survive this composable leaving and re-entering composition; the value is shared between two or more composables. **Use `remember { mutableStateOf(...) }` only when all of these are true:** the composable owns the value entirely (no ViewModel involvement); the value is discarded when the composable leaves composition; it is a draft/uncommitted form value (e.g., text field contents before Confirm is pressed) or a purely ephemeral UI state. **When uncertain, default to `GameState`** — an extra field on a data class costs less than a state coordination bug.
- **No CoroutineScope inside ViewModel** except through `viewModelScope`. Never call `GlobalScope`.

---

## Naming

| Thing | Convention | Example |
|---|---|---|
| Class / Object / Enum | PascalCase | `GameViewModel`, `PlayerColor` |
| Composable function | PascalCase | `PlayerCard`, `DiceRollerSheet` |
| Regular function | camelCase | `adjustLife`, `setPlayerName` |
| Property / variable | camelCase | `lifeTotal`, `isEliminated` |
| Private backing field | `_` prefix | `_state` |
| Callback parameter | `on` prefix | `onLifeAdjust`, `onDismiss` |
| Boolean state field | `is` or `show` prefix | `isEliminated`, `showDiceRoller` |
| Route constants | lowercase string | `"setup"`, `"game"` |

---

## Testing

Every code change must include tests. No PR or task is complete until the relevant tests are written and passing.

### What must be tested

- **Every new or modified ViewModel method** gets a unit test covering: happy path, edge cases, and elimination-triggering conditions where applicable.
- **Every new data model** (data class, enum) gets tests for any non-trivial logic (e.g., computed properties, copy behavior under edge inputs).
- **Every new Composable component** gets at least one UI test verifying it renders and responds to interaction correctly.
- **Bug fixes** must include a regression test that would have caught the bug.
- **Randomized functions** (`rollDie`) cannot be tested for exact values — test only range and state shape (e.g., that `activeDiceResult` is non-null and `value` is within `1..die.faces`). Do not attempt to seed or mock `kotlin.random` without user approval for that refactor.

### Test locations

| Type | Source set | Example path |
|---|---|---|
| ViewModel / model unit tests | `app/src/test/` | `viewmodel/GameViewModelTest.kt` |
| Compose UI tests | `app/src/androidTest/` | `ui/component/PlayerCardTest.kt` |

Mirror the main source package structure inside the test source set.

### Frameworks & setup

- **Unit tests:** JUnit 4 (`junit:junit`), `kotlinx-coroutines-test` for `StateFlow` / `viewModelScope`, `kotlin.test` assertions.
- **Compose UI tests:** `androidx.compose.ui:ui-test-junit4`, `androidx.compose.ui:ui-test-manifest`.
- Use `TestCoroutineDispatcher` / `UnconfinedTestDispatcher` + `Turbine` (or `StateFlow.value` snapshots) to assert state emissions from the ViewModel.
- If a required test dependency is missing from `libs.versions.toml`, add it — this is the one exception to the "no new dependencies without approval" rule, but only for standard Android/Compose test libraries.

### ViewModel test pattern

```kotlin
class GameViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()  // replaces Dispatchers.Main

    private lateinit var viewModel: GameViewModel

    @Before fun setUp() { viewModel = GameViewModel() }

    @Test fun `adjustLife reduces life total and emits updated state`() {
        viewModel.startGame()  // reach in-game state first
        val before = viewModel.state.value.players[0].lifeTotal
        viewModel.adjustLife(playerId = 0, delta = -3)
        assertThat(viewModel.state.value.players[0].lifeTotal).isEqualTo(before - 3)
    }

    @Test fun `adjustLife to zero or below marks player eliminated`() {
        viewModel.startGame()
        viewModel.adjustLife(playerId = 0, delta = -40)
        assertThat(viewModel.state.value.players[0].isEliminated).isTrue()
    }
}
```

### Compose test pattern

```kotlin
class PlayerCardTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun `PlayerCard displays correct life total`() {
        val player = Player(id = 0, lifeTotal = 40)
        composeRule.setContent {
            PlayerCard(player = player, rotationDegrees = 0f, onLifeAdjust = {}, onLifeTap = {}, onCommanderDamageTap = {}, onPoisonAdjust = {})
        }
        composeRule.onNodeWithText("40").assertIsDisplayed()
    }
}
```

### Running tests

```bash
# Unit tests
./gradlew test

# Compose UI tests (requires connected device or emulator)
./gradlew connectedAndroidTest

# Single test class
./gradlew test --tests "com.natron.commander.viewmodel.GameViewModelTest"
```

---

## What to Avoid

- **No new dependencies** without explicit user approval. The dependency set is fixed (Compose BOM 2024.10.00, Navigation 2.8.3, Lifecycle 2.8.6, Material3). If a task seems to require a new dependency: stop, name the exact `group:artifact:version` from Maven Central, explain why no existing dependency covers the need, and ask the user before proceeding.
- **No Hilt / Dagger / Koin.** ViewModel is created with `viewModels()` delegate.
- **No Room / database.** State is in-memory only.
- **No `LaunchedEffect` in ViewModel.** Side effects that need a coroutine use `viewModelScope.launch`.
- **No hardcoded strings** in UI files — use Kotlin string templates or local constants. (Full i18n/string resources are not set up; keep text in Kotlin.)
- **No comments explaining what code does** — only comments for non-obvious WHY (invariant, constraint, workaround).
- **No multi-paragraph docstrings.** One-line KDoc max when truly needed.
- **Don't change the player grid layout algorithm** unless the task explicitly requires it.

---

## File & Build

- **Build tool:** `./gradlew` only. Do not modify `build.gradle.kts`, `libs.versions.toml`, or `AndroidManifest.xml` unless the task explicitly requires it.
- **Min SDK 26, Target/Compile SDK 35, Kotlin 2.0.21, JVM target 11.** Do not change these.
- **Package name:** `com.natron.commander` — do not rename.
- **No new modules.** This is a single-module app; do not add `:feature:` or `:data:` submodules.
