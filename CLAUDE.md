# CLAUDE.md — AI working context for this project

This file is the working context for any AI coding assistant (Claude, Copilot, etc.)
contributing to this repository. It exists for two reasons:

1. It is the source of truth for how this codebase should be built and extended.
2. The challenge explicitly asks candidates to include the AI context files used
   during development ("CLAUDE.md, .cursorrules, ..."). This *is* that file, kept
   up to date as the project evolves rather than written after the fact.

## 1. What this project is

Implementation of the [idealista Android challenge](https://github.com/idealista/android-challenge):
an app with two screens — a **list of ads** and an **ad detail** — plus a **favorites**
feature that records the date an ad was marked as favorite.

Data source (static JSON, no auth, no pagination):

- List: `https://idealista.github.io/android-challenge/list.json`
- Detail: `https://idealista.github.io/android-challenge/detail.json`

**Known API quirk, by design of the challenge, not a bug in this app:** the detail
endpoint always returns the same payload regardless of which ad was tapped. The
domain/data layers are written as if the endpoint were correctly parameterized by
`propertyCode` (that's what a real API would do), so the code is not hacked around
the quirk. This is called out here and in the README so it reads as an understood
constraint, not an oversight.

## 2. Tech stack and why

| Concern            | Choice                                   | Why |
|---------------------|-------------------------------------------|-----|
| Language             | Kotlin                                     | Required by the challenge. |
| UI (list)             | XML + View Binding                       | Required by the challenge. |
| UI (detail)           | XML + View Binding, with one Jetpack Compose component embedded via `ComposeView` | The screen itself follows the same XML approach as the list screen (the challenge marks XML as mandatory, Compose as a bonus "alongside" it); the swipeable image gallery is implemented in Compose and embedded as a `ComposeView` inside the XML layout - real Compose/View interop, not a second full screen rewrite. |
| Async                 | Kotlin Coroutines + Flow / StateFlow     | Standard modern replacement for callbacks/RxJava on Android. |
| DI                    | Hilt                                     | Official DI on top of Dagger, least boilerplate for an app this size. |
| Networking            | Retrofit + OkHttp + kotlinx.serialization | Kotlin-first serialization (no reflection), Retrofit is the de-facto standard HTTP client. |
| Persistence           | Room                                     | Bonus "persistent storage": relational, testable (in-memory DB in tests), first-class Flow support — used to store favorites (`propertyCode` + `favoritedAt`). |
| Image loading         | Coil                                     | Kotlin-first, works the same way from XML (`ImageView`) and Compose (`AsyncImage`), avoids mixing two image libraries. |
| Navigation            | Navigation Component (Fragments), single Activity | Standard Android navigation; both destinations are XML-based Fragments. |
| Architecture          | Clean Architecture (single Gradle module, layered by package) | See §3. |
| Testing               | JUnit4, MockK, kotlinx-coroutines-test, Turbine, Espresso, Compose UI Test | See §6. |

**Module structure decision:** single `:app` module with Clean Architecture enforced
at the *package* level (`data` / `domain` / `presentation`), not a multi-module Gradle
setup. For a two-screen app, splitting into `core`/`domain`/`data`/`feature-*` Gradle
modules adds real build-time ceremony (module boundaries, `api`/`implementation`
wiring, more Gradle files to keep in sync) without a payoff at this scale — nothing
here is reused across independent apps or needs separate build variants per module.
Package boundaries already give the same dependency direction and testability
multi-module would, and are trivial to promote to real Gradle modules later if the
app grows. Chosen over multi-module deliberately, not by default.

- minSdk 24 / targetSdk & compileSdk 36 — covers the vast majority of active
  devices while allowing modern APIs.

## 3. Architecture

Clean Architecture, three layers, dependencies point inward (`presentation` and
`data` depend on `domain`; `domain` depends on nothing Android):

```
com.idealista.challenge
├── domain/                # Pure Kotlin, no Android/Retrofit/Room imports
│   ├── model/              # Ad, AdDetail, Favorite
│   ├── repository/         # AdsRepository interface (port)
│   └── usecase/             # One class per use case (ObserveAdsUseCase, ToggleFavoriteUseCase, ...)
│
├── data/                   # Implements the domain's ports
│   ├── remote/              # Retrofit service + DTOs + DTO->domain mappers
│   ├── local/                # Room entities + DAO + Database
│   └── repository/           # AdsRepositoryImpl (combines remote + local)
│
├── presentation/            # MVVM
│   ├── list/                  # ListFragment (XML), ListViewModel, ListUiState, RecyclerView adapter
│   ├── detail/                 # DetailFragment (XML), HeroImagePager (Compose, embedded via ComposeView), DetailViewModel
│   └── common/                  # Shared UI bits (formatting, resources)
│
└── di/                      # Hilt modules (Network, Database, Repository)
```

MVVM on the presentation side: Views (Fragment/Composable) are as dumb as possible,
ViewModels expose a single immutable `UiState` via `StateFlow`. One-off UI feedback
that shouldn't live in that state — e.g. `FavoriteToggleEvent`, which drives the
"added/removed from favorites" Snackbar on both screens — goes through a separate
`SharedFlow` instead of being smuggled into `UiState`: a state field would just
replay the last event on every rotation/recomposition instead of firing once.

## 4. Coding conventions

- Official [Kotlin style guide](https://developer.android.com/kotlin/style-guide).
- **Comments in English, used sparingly.** Comment *why*, not *what* — no comment
  restating what the next line obviously does. Public classes/functions in `domain`
  and `data` get a short KDoc when their purpose isn't obvious from the name and
  signature alone. No commented-out code, no author/date headers.
- Immutable data classes everywhere feasible (`val`, not `var`); UI state is a single
  immutable data class per screen.
- No hardcoded strings/dimens in layouts or Composables — always resources.
- Prefer constructor injection (Hilt `@Inject constructor`) over field injection.

### Pre-commit checklist

Before turning any change into a commit, whether it's "big" or not:

- [ ] It stays inside the layer it belongs to — no Android/Retrofit/Room imports
      leaking into `domain`, no navigation/UI logic in a ViewModel, no business
      logic duplicated in the View layer instead of living once in a
      ViewModel/use case.
- [ ] It follows the patterns already established elsewhere in the codebase
      (e.g. one-off UI events go through a `SharedFlow`, not smuggled into
      `UiState` — see §3) instead of a local shortcut that only works here.
- [ ] Comments are in English, and only where they explain *why*, not *what*.
- [ ] No commented-out code, no leftover debug logging.
- [ ] Naming doesn't collide or shadow in a confusing way (e.g. a private
      method and a lambda parameter sharing a name).
- [ ] If the change touches behavior already covered by unit tests, the tests
      are updated/extended — not left silently out of date.

This is a deliberate review step before `git commit`, not a one-time pass.

## 5. Git workflow

Branching follows a light git-flow:

- `main` — always in a working, submittable state. Only receives merges from
  `develop`, never direct commits (the very first scaffolding commit was made
  straight to `main`, before this branch existed — see below).
- `develop` — integration branch; this is the base everything forks from.
- `feature/<short-name>` — one per roadmap item in §7 (project setup, domain
  layer, data layer, list screen, detail screen, unit tests, ...). Branch from
  `develop`, commit there, merge back into `develop` with `--no-ff` (keeps the
  feature's own commits visible as a group in history instead of flattening
  them). Deleted after merging.
- `main` gets updated from `develop` once, right before submission.

Within each branch:

- Every commit must be atomic, buildable, and scoped to one thing.
- [Conventional Commits](https://www.conventionalcommits.org/): `feat:`, `fix:`,
  `refactor:`, `test:`, `docs:`, `chore:`, `style:`, `perf:`, `ci:`.
- Commit granularity follows the roadmap in §7 below — one feature/layer per
  commit, in dependency order (domain before data before presentation), so the
  history reads as a story of how the app was built, not a single dump.

## 6. Testing strategy

- **Unit tests** (JVM, fast): domain use cases, `AdsRepositoryImpl` (with a fake
  remote/local), `ListViewModel`, `DetailViewModel`. Tools: JUnit4, MockK,
  `kotlinx-coroutines-test` (`runTest`, a `MainDispatcherRule`), Turbine for
  asserting `Flow`/`StateFlow` emissions.
- **Instrumented/UI tests** (need a device/emulator): a Room DAO test
  (`FavoriteDaoTest`) against a real in-memory database, and a Compose UI
  test on `HeroImagePager` (`HeroImagePagerTest`).
  An Espresso test on `ListFragment` was attempted, hosting the fragment in a
  Hilt-enabled test Activity with the repository swapped for a fake via
  `@BindValue`/`@UninstallModules` - the standard pattern for testing
  `@AndroidEntryPoint` fragments. It was dropped: `ActivityScenario` could not
  resolve that test Activity in this environment even once the intent and
  manifest were verified correct against the actual merged manifest, and it
  wasn't worth further build-environment debugging time versus two solid,
  passing instrumented tests. Revisiting this is a fair next step, not a gap
  hidden from the roadmap.

## 7. Roadmap (kept in sync with commits)

- [x] Project setup: verified-compiling Android Studio project, Gradle version
      catalog, CLAUDE.md, .gitignore
- [x] Domain layer: models, repository interface, use cases
- [x] Data layer: network, Room, repository impl, DI modules
- [x] Presentation: list screen (XML + ViewModel) + app wiring (Hilt, MainActivity, NavHostFragment)
- [x] Presentation: detail screen (Compose) + navigation
- [x] Unit tests (domain, data, viewmodels)
- [x] Instrumented/UI tests (Room DAO, Compose) - Espresso attempted, dropped (see §6)
- [ ] Static analysis (ktlint/detekt) + CI
- [x] Final README
