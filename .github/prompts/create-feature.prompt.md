---
mode: 'agent'
description: 'Scaffold a new feature module or add a new screen to an existing module'
---

## Task

Create or extend a feature following the multi-module Clean Architecture conventions used in this project. Read `docs/architecture.md` and `rules and agents.md` before proceeding.

## Steps to Follow

1. **Identify the layer** where the change belongs:
   - New **domain model** → `domain` module (pure Kotlin, no Android deps).
   - New **repository interface** → `repository-api` module.
   - New **Room DAO / entity** → `repository` module.
   - New **use case** → appropriate `usecase:*` module (depends only on `repository-api`).
   - New **UI screen** → `app` module using Jetpack Compose + ViewModel.
   - Shared **UI component** → `ui-common` module.
   - New **cloud integration** → `cloud-api` (interface) + `cloud` (implementation).

2. **Domain model** (if needed):
   - Create a `data class` in `domain/src/main/kotlin/com/github/naz013/domain/`.
   - Keep it free of Android / Room / Gson annotations — those belong in `repository`.

3. **Repository implementation** (if needed):
   - Implement the interface in `repository/src/main/java/com/github/naz013/repository/`.
   - **Mandatory**: Create mappers between Room Entity and Domain model. Use the `domain` model in the API and `Entity` for Room storage.

4. **Use cases** (if needed):
   - Create a class with a single `invoke` operator in the appropriate `usecase:*` module.
   - Inject the repository interface via constructor.
   - Register the class in the module's `KoinModule.kt`.

5. **ViewModel** (app module):
   - Extend `ViewModel`; inject use cases and repositories via Koin constructor injection.
   - Expose UI state as `StateFlow<UiState>` (a sealed class or data class).
   - Register the ViewModel in the feature's `KoinModule.kt` with `viewModel { }`.

6. **Compose screen**:
   - Create a stateless `@Composable` that receives state + lambdas for events.
   - Connect it to the ViewModel with a thin "screen" composable that calls `collectAsStateWithLifecycle()`.
   - Reuse components from `ui-common` before creating new ones.
   - Follow Material 3 guidelines.

7. **Navigation**:
   - Add a destination constant or `DeepLinkDestination` subclass in `navigation-api` if the screen needs to be reachable from outside `app`.
   - Register the Compose destination in the NavGraph in `app`.

8. **Koin wiring**:
   - Each new injectable class must be registered in the closest module's `KoinModule.kt`.
   - In `app`, include the new `KoinModule` in the `startKoin` list if it belongs to a new module.

## Requirements

Feature name: ${input:feature_name:What is the name of the new feature?}
Brief description: ${input:feature_description:What should this feature do?}

## Output Checklist

- [ ] Domain model created (if applicable)
- [ ] Repository interface added / updated (if applicable)
- [ ] Use case(s) created (if applicable)
- [ ] ViewModel created with `StateFlow` state
- [ ] Compose screen created (stateless + connected screen)
- [ ] Navigation destination registered
- [ ] Koin DI wiring added in all affected modules
- [ ] Unit tests written for use case(s) and ViewModel
- [ ] KDoc comments added to all public APIs
