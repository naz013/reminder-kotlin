# build-logic: convention plugins for multi-module Gradle setups

How this repo centralizes Android/Kotlin module configuration into Gradle convention plugins, so a new
multi-module project can replicate the pattern. This describes the *mechanism* — adapt package names,
plugin ids, and version numbers to the target project.

## Why

With N modules (this repo has 60+), repeating `compileSdk`, `minSdk`, Java/Kotlin compiler options, and
detekt setup in every `build.gradle.kts` is duplicated, drifts over time, and makes bumping a shared value
an N-file change. A convention plugin moves that config into one place: a plugin applied with a single
`id("...")` line, so a module's own build file only declares what's genuinely module-specific
(namespace, dependencies).

## Directory layout

```
build-logic/                          # a separate, included Gradle build — NOT a subproject
├── settings.gradle.kts                # its own settings file; names the build "build-logic"
└── convention/
    ├── build.gradle.kts               # declares the plugin IDs and their implementation classes
    └── src/main/kotlin/
        ├── ProjectExtensions.kt              # small helpers (version-catalog accessors)
        ├── KotlinCompilerOptions.kt           # shared compiler-args helper
        ├── <Foo>ConventionPlugin.kt           # one class per convention plugin
        └── ...
```

`build-logic` is wired into the root project via `pluginManagement.includeBuild("build-logic")` in the
root `settings.gradle.kts` — it is a composite build, not a module. This is what makes `id("reminder.*")`
resolve as if it were a published plugin, while the code lives in-repo and rebuilds instantly.

## Step-by-step setup

### 1. `build-logic/settings.gradle.kts`

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
    maven { url = URI.create("https://jitpack.io") }   // only if the project needs jitpack
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))       // reuse the ROOT project's catalog
    }
  }
}

rootProject.name = "build-logic"
include(":convention")
```

Key point: `versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }` points
at the *same* catalog file the root build uses, so convention plugin code and module build files share
one source of truth for versions (AGP, Kotlin, detekt, compileSdk/minSdk/targetSdk, etc.) — no separate
catalog to keep in sync.

### 2. `build-logic/convention/build.gradle.kts`

```kotlin
plugins {
  `kotlin-dsl`
}

group = "com.example.buildlogic"   // any unique group; used as the plugin classes' package too

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.detekt.gradlePlugin)   // one compileOnly per Gradle plugin API being configured
}

kotlin {
  jvmToolchain(17)
}

gradlePlugin {
  plugins {
    register("kotlinJvm") {
      id = "example.kotlin.jvm"
      implementationClass = "com.example.buildlogic.ExampleKotlinJvmConventionPlugin"
    }
    register("androidLibrary") {
      id = "example.android.library"
      implementationClass = "com.example.buildlogic.ExampleAndroidLibraryConventionPlugin"
    }
    register("androidLibraryCompose") {
      id = "example.android.library.compose"
      implementationClass = "com.example.buildlogic.ExampleAndroidLibraryComposeConventionPlugin"
    }
    register("androidApplication") {
      id = "example.android.application"
      implementationClass = "com.example.buildlogic.ExampleAndroidApplicationConventionPlugin"
    }
    register("androidApplicationCompose") {
      id = "example.android.application.compose"
      implementationClass = "com.example.buildlogic.ExampleAndroidApplicationComposeConventionPlugin"
    }
    register("detekt") {
      id = "example.detekt"
      implementationClass = "com.example.buildlogic.ExampleDetektConventionPlugin"
    }
  }
}
```

`` `kotlin-dsl` `` is what lets these plugin classes be written in Kotlin and picked up automatically as
Gradle plugins from `src/main/kotlin` — no manual `META-INF/gradle-plugins/*.properties` files to author
(the `register(...)` blocks above generate those at build time).

Naming convention: pick one short namespace prefix for every plugin id (`example.*` here, `reminder.*`
in this repo) so `id("example.android.library")` reads unambiguously as "our convention plugin," distinct
from upstream ids like `com.android.library`.

### 3. Shared helper: version-catalog access from plugin code

Plugin classes run in `build-logic`'s own classpath, so they can't use the `libs.foo` accessor
generated for the main build — they read the catalog dynamically instead:

```kotlin
// ProjectExtensions.kt
package com.example.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.catalog: VersionCatalog
  get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.intVersion(alias: String): Int = findVersion(alias).get().requiredVersion.toInt()

fun VersionCatalog.stringVersion(alias: String): String = findVersion(alias).get().requiredVersion
```

Used as `catalog.intVersion("compileSdk")`, `catalog.findLibrary("detekt-formatting").get()`, etc. inside
every convention plugin below — this is the mechanism that lets `compileSdk`/`minSdk`/`targetSdk`/Kotlin
JVM target live as plain `[versions]` entries in `gradle/libs.versions.toml`, bumped in one place.

### 4. Shared helper: compiler options

```kotlin
// KotlinCompilerOptions.kt
package com.example.buildlogic

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

fun KotlinJvmCompilerOptions.applyExampleOptIns() {
  freeCompilerArgs.add("-Xsome-opt-in-flag")
  // ... any -X flags or opt-ins every module should share
}
```

A tiny extension function, called from every convention plugin's `compilerOptions { }` block, so a new
shared compiler flag is a one-line change instead of an N-file grep-and-replace.

### 5. The convention plugin classes

Each plugin is a small `Plugin<Project>` that (a) applies the underlying Gradle/Android/Kotlin plugin(s)
by string id, then (b) configures the resulting extension via `extensions.configure<...> { }`. Plugins
build on each other by calling `pluginManager.apply("example.<other-plugin-id>")` rather than duplicating
config — e.g. the Compose variant of a plugin applies the non-Compose variant first.

**Base Kotlin/JVM module** (for pure-Kotlin modules — domain models, business logic with no Android
dependency):

```kotlin
class ExampleKotlinJvmConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("java-library")
      pluginManager.apply("org.jetbrains.kotlin.jvm")
      pluginManager.apply("example.detekt")

      extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
      }

      extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(catalog.intVersion("kotlinTargetJvm"))
        compilerOptions {
          applyExampleOptIns()
        }
      }
    }
  }
}
```

**Android library**:

```kotlin
class ExampleAndroidLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.android.library")
      pluginManager.apply("example.detekt")

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
        compilerOptions {
          applyExampleOptIns()
        }
      }
    }
  }
}
```

**Android library + Compose** (layers on top of the plain library plugin):

```kotlin
class ExampleAndroidLibraryComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("example.android.library")
      pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

      extensions.configure<LibraryExtension> {
        buildFeatures { compose = true }
        composeOptions {
          kotlinCompilerExtensionVersion = catalog.stringVersion("kotlinCompilerExtensionVersion")
        }
      }
    }
  }
}
```

**Android application** and **application + Compose** follow the identical library/library-compose
pattern, configuring `ApplicationExtension` instead of `LibraryExtension` (plus `targetSdk`, packaging
excludes, etc. — whatever's shared across every app-flavor variant). Keep anything variant-specific
(applicationId, versionCode/Name, signing, flavors, BuildConfig fields) out of the convention plugin and
in the single app module's own build file — that's inherently module-specific, not shared config.

**Detekt** (or any shared static-analysis/lint plugin), applied transitively by the other plugins so
modules don't need to remember to add it themselves:

```kotlin
class ExampleDetektConventionPlugin : Plugin<Project> {
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
      }

      tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        reports {
          sarif.required.set(true)
          sarif.outputLocation.set(layout.buildDirectory.file("reports/detekt/${name}.sarif"))
        }
      }
    }
  }
}
```

### 6. Wire it into the root build

`settings.gradle.kts` (root, sibling to `build-logic/`):

```kotlin
pluginManagement {
  includeBuild("build-logic")   // <-- the composite-build link
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
  }
}

rootProject.name = "Example"
include(":app")
include(":core:domain")
// ... every other module
```

`includeBuild("build-logic")` inside `pluginManagement { }` (not the top-level `includeBuild`) is what
makes the `example.*` plugin ids resolvable in every module's `plugins { }` block, project-wide, without
each module declaring a `buildscript` dependency.

### 7. Module build files become thin

Pure-Kotlin module:

```kotlin
plugins {
  id("example.kotlin.jvm")
}

dependencies {
  implementation(libs.gson)
  testImplementation(libs.junit)
}
```

Android library module:

```kotlin
plugins {
  id("example.android.library")
  alias(libs.plugins.ksp)   // only if this module needs KSP (Room, etc.) — not everyone does
}

android {
  namespace = "com.example.repository"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(libs.androidx.room.runtime)
  ksp(libs.androidx.room.compiler)
}
```

Every module's `build.gradle.kts` is now reduced to: one `plugins { id("example.*") }` line, the
`android { namespace = ... }` block (namespace is inherently per-module, so it stays local), and its
`dependencies { }` block. Anything genuinely module-specific (a `testFixtures` block, extra compiler
flags) still goes in the module's own file — the convention plugin only owns what's *shared*.

## What to keep local vs. centralize

| Centralize in a convention plugin | Keep in the module's own build file |
|---|---|
| `compileSdk` / `minSdk` / `targetSdk` | `namespace` |
| Java/Kotlin source & target compatibility | `dependencies { }` |
| `jvmToolchain` version | flavor/variant-specific config (app module only) |
| shared Kotlin compiler opt-in flags | `testFixtures { enable = true }` if only this module needs it |
| detekt/lint wiring and config file path | signing configs, applicationId, versionCode/Name |
| consumer-proguard / release minify defaults | per-module KSP/annotation-processor plugin aliases |
| packaging excludes shared by every app variant | anything used by exactly one module |

## Applying this pattern to a new project

1. Create `build-logic/` next to the root `settings.gradle.kts`, with its own `settings.gradle.kts`
   pointing `versionCatalogs` at the root project's `gradle/libs.versions.toml`.
2. Add `build-logic/convention/build.gradle.kts` with the `` `kotlin-dsl` `` plugin and a `gradlePlugin { plugins { register(...) } }` block per convention plugin you intend to write.
3. Write one `Plugin<Project>` class per module archetype the project actually has (start with just
   `kotlin.jvm` and `android.library` — add `android.application`/`*.compose`/`detekt` as needed; don't
   pre-build variants nothing uses yet).
4. Add `pluginManagement.includeBuild("build-logic")` to the root `settings.gradle.kts`.
5. Migrate one module at a time: replace its repeated `android { compileSdk = ...; minSdk = ...; ... }`
   boilerplate with `id("<prefix>.android.library")`, verify it still builds, then move to the next
   module.
6. Only add a new convention plugin (or a new shared block inside an existing one) once at least two
   modules would need the same config — a config block used by exactly one module belongs in that
   module's own build file, not in `build-logic`.
