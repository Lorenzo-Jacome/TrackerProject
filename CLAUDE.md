# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

All commands run via Gradle wrapper from the project root:

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run a single unit test class
./gradlew test --tests "com.example.trackerproject.ExampleUnitTest"

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Lint check
./gradlew lint
```

There is no local dev server — the app runs on Android device or emulator via Android Studio or `adb`.

## Architecture

**No ViewModel, no Room, no Navigation component.** The app is intentionally minimal:

- **State lives directly in Composables** using `remember { mutableStateOf(...) }`. Each screen loads data on composition and saves on every mutation.
- **Persistence** is `SharedPreferences` (file: `tracker_data`) with two keys: `"tasks"` and `"habits"`. Serialization/deserialization uses `org.json` (built-in Android). All read/write goes through `AppDataStore` (singleton object).
- **Tab navigation** is a single `selectedTab: Int` in `MainScreen`, switching between three screen composables with `when`. No back stack.
- **Widget updates** are triggered automatically inside `AppDataStore.saveTasks()` and `AppDataStore.saveHabits()` — no separate widget refresh call needed after mutations.

## Data Flow Pattern

Every screen follows this pattern:
```kotlin
var tasks by remember { mutableStateOf(AppDataStore.getTasks(context)) }
// ... user action mutates local list ...
AppDataStore.saveTasks(context, tasks)  // persists + triggers widget update
```

Task callbacks match by ID (`.map { if (it.id == id) it.copy(...) else it }`), never by index, because lists are filtered/sectioned before display.

## UI / Theme Constraints

- **Always-dark, no dynamic color.** `TrackerProjectTheme` hardcodes a `darkColorScheme` — never call `dynamicColorScheme` or wrap with `isSystemInDarkTheme`.
- **NeonLime (`#AEFF00`)** is the primary accent. Use `MaterialTheme.colorScheme.primary` or the `NeonLime` constant from `Color.kt`.
- **GeistPixel fonts** (5 variants in `res/font/`) are used only for the Tasks header. Don't use them elsewhere unless intentional.
- Custom `SquareCheckbox` composable is used instead of Material `Checkbox` — keep this consistent across screens.

## Key Constraints

- **No new dependencies.** Only what the default Android Studio template provides (Compose BOM, Material3, lifecycle, core-ktx).
- `minSdk = 24` — avoid APIs above that without version checks.
- Week starts on **Monday** (hardcoded). Any date/calendar logic must match this.
- Habit `color` is stored as ARGB `Int`. Read with `optInt` to handle missing field in older persisted data.
