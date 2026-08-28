# Gradle Modularization Setup — Portable Guide

This document describes **how this repo's Gradle build is structured** (convention plugins,
version catalog, module grouping) as a standalone recipe, so it can be reproduced in another
Android/Kotlin project. It intentionally repeats some material from `docs/architecture.md`
but frames it as a generic "how to set this up from scratch" guide rather than a description
of this project's specific modules.

For *why* this project's modules are split the way they are, see `docs/architecture.md`. This
doc is about the *mechanics*: composite build, convention plugins, version catalog, folder
layout.

---

## 1. The core idea: a composite `build-logic` build

Instead of copy-pasting the same `android { compileSdk = ...; minSdk = ...; compileOptions {...} }`
boilerplate into every module's `build.gradle.kts`, this project defines a small set of **convention
plugins** — plain Gradle plugins written in Kotlin — that each module applies by ID. All shared
config (SDK versions, Java/Kotlin compiler options, Compose setup, static analysis) lives in one
place.

This is done with an **included build** (`pluginManagement.includeBuild(...)`), not a
`buildSrc` directory. `includeBuild` is preferred over `buildSrc` because:
- Changes to `build-logic` don't invalidate the *entire* root build's configuration cache the way
  `buildSrc` edits do.
- It's a normal Gradle project with its own `settings.gradle.kts`, so it can have its own version
  catalog reference, its own dependencies, and be opened/edited independently.

### Folder layout

```
<repo-root>/
├── settings.gradle.kts              # root settings — includes build-logic + all modules
├── build.gradle.kts                 # root build script — plugin aliases (apply false) + cross-module tasks
├── gradle/
│   └── libs.versions.toml           # version catalog — single source of truth for versions
├── build-logic/
│   ├── settings.gradle.kts          # its own tiny settings.gradle.kts
│   └── convention/
│       ├── build.gradle.kts         # declares the plugin IDs -> implementation classes
│       └── src/main/kotlin/
│           ├── ProjectExtensions.kt                          # small helpers
│           ├── KotlinCompilerOptions.kt                      # shared compiler args
│           ├── ReminderKotlinJvmConventionPlugin.kt           # id("reminder.kotlin.jvm")
│           ├── ReminderAndroidLibraryConventionPlugin.kt      # id("reminder.android.library")
│           ├── ReminderAndroidLibraryComposeConventionPlugin.kt   # id("reminder.android.library.compose")
│           ├── ReminderAndroidApplicationConventionPlugin.kt  # id("reminder.android.application")
│           ├── ReminderAndroidApplicationComposeConventionPlugin.kt # id("reminder.android.application.compose")
│           └── ReminderDetektConventionPlugin.kt              # id("reminder.detekt")
├── core/<module>/build.gradle.kts   # thin: just applies a convention plugin + dependencies {}
├── data/<module>/build.gradle.kts
├── ui/<module>/build.gradle.kts
├── ...
└── app/build.gradle.kts             # applies reminder.android.application.compose + app-only config
```

Everything under `build-logic/` is its own Gradle build, wired into the root build via
`pluginManagement.includeBuild("build-logic")` in the root `settings.gradle.kts` — it is **not**
one of the modules listed in `include(...)`.

---

## 2. `build-logic`'s own `settings.gradle.kts`

```kotlin
import java.net.URI

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven { url = URI.create("https://jitpack.io") }
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

rootProject.name = "build-logic"
include(":convention")
```

Key point: `build-logic` **reuses the root project's `libs.versions.toml`** via a relative path
(`../gradle/libs.versions.toml`) instead of maintaining a second copy. This means the convention
plugins can read the exact same version numbers (`compileSdk`, `minSdk`, Kotlin version, etc.) as
every other module.

## 3. `build-logic/convention/build.gradle.kts`

This is where plugin IDs are registered and mapped to their implementation classes:

```kotlin
plugins {
  `kotlin-dsl`
}

group = "com.github.naz013.buildlogic"

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.detekt.gradlePlugin)
}

kotlin {
  jvmToolchain(17)
}

gradlePlugin {
  plugins {
    register("kotlinJvm") {
      id = "reminder.kotlin.jvm"
      implementationClass = "com.github.naz013.buildlogic.ReminderKotlinJvmConventionPlugin"
    }
    register("androidLibrary") {
      id = "reminder.android.library"
      implementationClass = "com.github.naz013.buildlogic.ReminderAndroidLibraryConventionPlugin"
    }
    register("androidLibraryCompose") {
      id = "reminder.android.library.compose"
      implementationClass = "com.github.naz013.buildlogic.ReminderAndroidLibraryComposeConventionPlugin"
    }
    register("androidApplication") {
      id = "reminder.android.application"
      implementationClass = "com.github.naz013.buildlogic.ReminderAndroidApplicationConventionPlugin"
    }
    register("androidApplicationCompose") {
      id = "reminder.android.application.compose"
      implementationClass = "com.github.naz013.buildlogic.ReminderAndroidApplicationComposeConventionPlugin"
    }
    register("detekt") {
      id = "reminder.detekt"
      implementationClass = "com.github.naz013.buildlogic.ReminderDetektConventionPlugin"
    }
  }
}
```

The `kotlin-dsl` plugin is what makes this module produce Gradle plugins written in Kotlin and
puts the Gradle API + Kotlin DSL on its classpath. `compileOnly` (not `implementation`) is used
for the AGP/Kotlin/Detekt Gradle plugin artifacts because they're provided by the root build's own
classpath at apply-time — declaring them `compileOnly` avoids duplicate-classpath conflicts.

**To port this**: rename `reminder.*` → `<yourprefix>.*` and `com.github.naz013.buildlogic` → your
own package. Everything else can be copied close to verbatim.

## 4. The convention plugins themselves

Each plugin is a small `Plugin<Project>` that applies one or more real Gradle/AGP plugins and
configures their extension. A tiny helper (`ProjectExtensions.kt`) exposes the root version
catalog to plugin code:

```kotlin
// ProjectExtensions.kt
val Project.catalog: VersionCatalog
  get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.intVersion(alias: String): Int = findVersion(alias).get().requiredVersion.toInt()
fun VersionCatalog.stringVersion(alias: String): String = findVersion(alias).get().requiredVersion
```

This lets every convention plugin read `catalog.intVersion("compileSdk")` etc. instead of
hardcoding numbers, so bumping `compileSdk` in `libs.versions.toml` updates every module at once.

Shared Kotlin compiler opt-in flags live in one function so all five plugins apply the exact same
set:

```kotlin
// KotlinCompilerOptions.kt
fun KotlinJvmCompilerOptions.applyReminderOptIns() {
  freeCompilerArgs.add("-Xreturn-value-checker=full")
  freeCompilerArgs.add("-Xname-based-destructuring=only-syntax")
  freeCompilerArgs.add("-Xcollection-literals")
  freeCompilerArgs.add("-Xcontext-sensitive-resolution")
}
```

(These four flags are this project's own choice of Kotlin 2.x preview features — swap for
whatever compiler flags your project wants; the pattern of "one function, called from every
convention plugin" is what's worth reusing.)

### Plugin hierarchy

There are 6 plugins, layered so each builds on the previous one by *applying* it, not duplicating
its config:

```
reminder.detekt                         (applies io.gitlab.arturbosch.detekt)
reminder.kotlin.jvm                     (applies java-library + kotlin.jvm + reminder.detekt)
reminder.android.library                (applies com.android.library + reminder.detekt)
  └── reminder.android.library.compose  (applies reminder.android.library + kotlin.plugin.compose)
reminder.android.application            (applies com.android.application + reminder.detekt)
  └── reminder.android.application.compose (applies reminder.android.application + kotlin.plugin.compose)
```

**`reminder.kotlin.jvm`** — for pure-Kotlin modules (no Android dependency):
```kotlin
class ReminderKotlinJvmConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("java-library")
      pluginManager.apply("org.jetbrains.kotlin.jvm")
      pluginManager.apply("reminder.detekt")

      extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
      }

      extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(catalog.intVersion("kotlinTargetJvm"))
        compilerOptions { applyReminderOptIns() }
      }
    }
  }
}
```

**`reminder.android.library`** — for Android library modules:
```kotlin
class ReminderAndroidLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.android.library")
      pluginManager.apply("reminder.detekt")

      extensions.configure<LibraryExtension> {
        compileSdk = catalog.intVersion("compileSdk")
        defaultConfig {
          minSdk = catalog.intVersion("minSdk")
          testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
          consumerProguardFiles("consumer-rules.pro")
        }
        buildTypes {
          release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
          }
        }
        compileOptions {
          sourceCompatibility = JavaVersion.VERSION_17
          targetCompatibility = JavaVersion.VERSION_17
        }
      }

      extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(catalog.intVersion("kotlinTargetJvm"))
        compilerOptions { applyReminderOptIns() }
      }
    }
  }
}
```

**`reminder.android.library.compose`** — adds Compose on top of the library plugin:
```kotlin
class ReminderAndroidLibraryComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("reminder.android.library")
      pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

      extensions.configure<LibraryExtension> {
        buildFeatures { compose = true }
        composeOptions {
          kotlinCompilerExtensionVersion = catalog.stringVersion("kotlinCompilerExtensionVersion")
        }
        compileOptions {
          sourceCompatibility = JavaVersion.VERSION_17
          targetCompatibility = JavaVersion.VERSION_17
        }
      }

      extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(catalog.intVersion("kotlinTargetJvm"))
        compilerOptions { applyReminderOptIns() }
      }
    }
  }
}
```

**`reminder.android.application`** — for the one `app` module (application-specific bits like
`targetSdk`, packaging excludes for META-INF conflicts):
```kotlin
class ReminderAndroidApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.android.application")
      pluginManager.apply("reminder.detekt")

      extensions.configure<ApplicationExtension> {
        compileSdk = catalog.intVersion("compileSdk")
        defaultConfig {
          minSdk = catalog.intVersion("minSdk")
          targetSdk = catalog.intVersion("targetSdk")
          testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        buildTypes {
          release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
          }
        }
        packaging {
          resources {
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/LICENSE.txt"
            // ...more META-INF excludes for duplicate license files pulled in by dependencies
          }
        }
        compileOptions {
          sourceCompatibility = JavaVersion.VERSION_17
          targetCompatibility = JavaVersion.VERSION_17
        }
      }

      extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(catalog.intVersion("kotlinTargetJvm"))
        compilerOptions { applyReminderOptIns() }
      }
    }
  }
}
```

**`reminder.android.application.compose`** — thin, just layers Compose on top:
```kotlin
class ReminderAndroidApplicationComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("reminder.android.application")
      pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

      extensions.configure<ApplicationExtension> {
        buildFeatures { compose = true }
      }
    }
  }
}
```

**`reminder.detekt`** — static analysis, applied transitively by every plugin above (a module
never needs to apply it directly):
```kotlin
class ReminderDetektConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("io.gitlab.arturbosch.detekt")

      extensions.configure<DetektExtension> {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
      }

      dependencies.apply {
        add("detektPlugins", catalog.findLibrary("detekt-formatting").get())
        add("detektPlugins", catalog.findLibrary("detekt-compose").get())
      }

      tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        reports {
          html.required.set(false)
          xml.required.set(false)
          txt.required.set(false)
          md.required.set(false)
          sarif.required.set(true)
          sarif.outputLocation.set(layout.buildDirectory.file("reports/detekt/${name}.sarif"))
        }
      }
    }
  }
}
```

---

## 5. Root `settings.gradle.kts`

```kotlin
import java.net.URI

pluginManagement {
  includeBuild("build-logic")          // <-- wires the composite build in
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven { url = URI.create("https://jitpack.io") }
  }
}

rootProject.name = "YourApp"
include(":app")

// group folders map 1:1 to physical directories; Gradle path = ":<group>:<module>"
include(":core:domain")
include(":core:logging-api")
// ...
include(":data:repository-api")
include(":data:repository")
// ...
```

Notes:
- `pluginManagement.includeBuild("build-logic")` must come before the `plugins {}` block that
  references `reminder.*` plugin IDs anywhere in the build (Gradle resolves included builds during
  the settings phase).
- `repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)` forces every module to get its
  repositories from this central `dependencyResolutionManagement` block — a module's own
  `build.gradle.kts` is not allowed to declare `repositories {}`, which keeps repo config in one
  place.
- Grouping (`core:`, `data:`, `ui:`, `logic:`, `feature:`, `extensions:`, `admin:`) is purely a
  folder/`include()`-path convention — the folders themselves aren't buildable Gradle projects.
  See `docs/architecture.md`'s "Module Groups" table for why this project uses these particular
  seven groups; you'd choose your own grouping for a different project, but the *mechanism*
  (`include(":group:module")` + matching directory nesting) ports directly.

## 6. Root `build.gradle.kts`

```kotlin
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.parcelize) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.crashlytics.gradle) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.detekt) apply false
}

// Merges every module's per-module detekt SARIF report into one file, because GitHub Code
// Scanning rejects multiple SARIF runs uploaded under the same category.
val detektReportMerge by tasks.registering(ReportMergeTask::class) {
  output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

subprojects {
  plugins.withId("io.gitlab.arturbosch.detekt") {
    afterEvaluate {
      val detektTasks = tasks.withType<Detekt>()
      detektReportMerge.configure {
        mustRunAfter(detektTasks)
        input.from(detektTasks.map { it.reports.sarif.outputLocation.get() })
      }
    }
  }
}
```

The `alias(...) apply false` pattern declares every plugin **version** once (resolved from the
version catalog) without actually applying it to the root project — each module then applies the
plugin it needs (directly, or transitively via a `reminder.*` convention plugin) without needing
to redeclare its version. This avoids "plugin version conflict" errors across modules.

The `detektReportMerge` task is optional — only relevant if you're uploading SARIF to GitHub Code
Scanning from CI. Skip it if you don't need merged static-analysis reports.

## 7. Version catalog (`gradle/libs.versions.toml`)

A single `[versions]` table holds SDK/tooling versions the convention plugins read via
`catalog.intVersion(...)` / `catalog.stringVersion(...)`:

```toml
[versions]
minSdk = "29"
compileSdk = "37"
targetSdk = "37"
kotlinTargetJvm = "17"
kotlin = "2.4.10"
agp = "9.3.1"
ksp = "2.3.4"
detekt = "1.23.8"
detekt-compose = "0.6.4"
kotlinCompilerExtensionVersion = "1.5.15"
# ... every other dependency version
```

`[libraries]` and `[plugins]` tables follow standard Gradle version-catalog format — e.g.:

```toml
[libraries]
detekt-gradlePlugin = { group = "io.gitlab.arturbosch.detekt", name = "detekt-gradle-plugin", version.ref = "detekt" }
detekt-formatting  = { group = "io.gitlab.arturbosch.detekt", name = "detekt-formatting", version.ref = "detekt" }
detekt-compose     = { group = "io.nlopez.compose.rules", name = "detekt", version.ref = "detekt-compose" }

[plugins]
ksp    = { id = "com.google.devtools.ksp", version.ref = "ksp" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

Nothing here is special to this project — it's the standard Gradle version catalog mechanism.
What's worth reusing is the discipline: **every version number lives in this one file**, and both
`build-logic` (via its own settings.gradle.kts referencing `../gradle/libs.versions.toml`) and
every application/library module read from the exact same catalog.

---

## 8. What a leaf module's `build.gradle.kts` looks like

Because all shared config moved into the convention plugins, a typical module file is now just a
plugin application + a dependency list. Pure-Kotlin module:

```kotlin
plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(libs.gson)
  implementation(libs.threetenbp)
  testImplementation(libs.junit)
}
```

Android library module (adds its own namespace + KSP for Room):

```kotlin
plugins {
  id("reminder.android.library")
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.example.repository"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":data:repository-api"))
  implementation(project(":core:logging-api"))

  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  ksp(libs.androidx.room.compiler)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
```

Android library with Compose:

```kotlin
plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.example.feature.mything"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":ui:ui-common"))
  // ...
}
```

The `app` module applies `reminder.android.application.compose` and layers only what's genuinely
application-specific on top: product flavors, signing configs, `BuildConfig` fields, Firebase
plugins (`google-services`, `crashlytics-gradle`), `kotlin.serialization`, packaging excludes for
third-party META-INF conflicts, etc. — see `app/build.gradle.kts` in this repo for a full example
of flavor/signing wiring, which is app-specific and not part of the reusable convention-plugin
layer.

---

## 9. Porting this to a new project — step by step

1. Copy the `build-logic/` directory wholesale (rename the `reminder.*` plugin IDs and the
   `com.github.naz013.buildlogic` package to your own naming).
2. Create `gradle/libs.versions.toml` with at minimum `minSdk`, `compileSdk`, `targetSdk`,
   `kotlinTargetJvm`, `kotlin`, `agp`, `detekt`, `kotlinCompilerExtensionVersion` under
   `[versions]`, plus whatever libraries your project needs.
3. In the new project's root `settings.gradle.kts`, add `pluginManagement.includeBuild("build-logic")`
   before any `plugins {}` block, and set `dependencyResolutionManagement` as shown in §5.
4. Decide your own module grouping (or skip grouping entirely and keep modules flat at the root —
   the convention-plugin mechanism doesn't require the `core:`/`data:`/`ui:` folder scheme, that
   part is this project's own organizational choice, described in `docs/architecture.md`).
5. For each new module, write a `build.gradle.kts` that applies exactly one of the `reminder.*`
   (renamed) plugins and lists only its own dependencies — no `android { compileSdk = ... }`
   boilerplate needed.
6. If you don't use Detekt, delete `ReminderDetektConventionPlugin.kt` and remove
   `pluginManager.apply("reminder.detekt")` from the other four plugins, plus the
   `detektReportMerge` task from the root `build.gradle.kts`.
7. Adjust `KotlinCompilerOptions.kt`'s `applyReminderOptIns()` to whatever compiler flags (if any)
   your project wants — the four flags here are this project's own Kotlin 2.x preview-feature
   choices, not required for the pattern to work.

Everything above is independent of this project's specific module list — it's a general recipe for
"convention plugins + version catalog + composite build-logic build" that scales to any number of
Android/Kotlin Gradle modules.
