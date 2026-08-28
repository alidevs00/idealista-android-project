# idealista Android Challenge

An implementation of the [idealista Android technical challenge](https://github.com/idealista/android-challenge):
an app with an ad listing screen, an ad detail screen, and a favoriting
feature that records the date each ad was marked as favorite.

## Screens

- **List** — browsable list of ads (price, operation, size, rooms, location,
  favorite toggle), with pull-to-refresh, loading, empty and error states.
- **Detail** — full ad detail (swipeable photo gallery, price, characteristics,
  expandable description, favorite toggle), reached by tapping an ad on the
  list.
- **Favorites** — any ad can be favorited from either screen; a favorited ad
  shows the date it was favorited, persisted locally so it survives app
  restarts.

## Tech stack

Kotlin, XML views + View Binding (with a single Jetpack Compose component —
the detail screen's photo gallery — embedded via `ComposeView`), Coroutines
and Flow, Hilt for dependency injection, Retrofit + OkHttp + kotlinx.serialization
for networking, Room for local persistence, Coil for image loading, and the
Navigation Component for screen navigation. Single `:app` Gradle module,
layered with Clean Architecture (`domain` / `data` / `presentation` / `di`)
and MVVM on the presentation side.

The full rationale behind each of these choices — and the project's coding
conventions, git workflow, and pre-commit checklist — is documented in
[`CLAUDE.md`](CLAUDE.md), which doubles as the AI working-context file used
throughout development (this project was built with AI assistance; see that
file for details on how it was used).

## Getting started

Requirements: a recent Android Studio install (bundles a compatible JDK).

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync — no extra configuration or API keys are needed, the app
   talks to the challenge's public static JSON endpoints.
3. Run the `app` configuration on a device or emulator (minSdk 24).

## Running the tests

Unit tests (JVM, no device needed):

```
./gradlew test
```

Instrumented tests (needs a connected device or emulator):

```
./gradlew connectedAndroidTest
```

Unit tests cover the domain use cases, the data layer (mappers and the
repository), and both ViewModels. Instrumented tests cover a Room DAO test
against a real in-memory database, an Espresso test on the list screen, and a
Compose UI test on the detail screen's photo gallery.

## Known limitations

- The challenge's detail endpoint (`detail.json`) always returns the same
  fixed payload, regardless of which ad was tapped — this is a constraint of
  the challenge's static data, not a bug in this app. See the KDoc on
  `AdsApi.getAdDetail()` and `AdDetail` for how the repository works around it
  (threading the requested `propertyCode` through for caching/favoriting even
  though the endpoint itself ignores it).
- No CI or static analysis (ktlint/detekt) is set up yet — tracked as an open
  item in `CLAUDE.md`'s roadmap.

## Git workflow

This repository follows a light git-flow: `main` is always submittable,
`develop` is the integration branch, and each feature/fix was built on its
own short-lived branch and merged back with `--no-ff`, using
[Conventional Commits](https://www.conventionalcommits.org/). `git log --graph`
on `develop` shows the app being built up incrementally, layer by layer. See
`CLAUDE.md` §5 for the full convention.
