import java.net.URI

pluginManagement {
  includeBuild("build-logic")
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

rootProject.name = "Reminder"
include(":app")

// core - foundation shared by every other layer
include(":core:domain")
include(":core:logging-api")
include(":core:logging")
include(":core:feature-common")
include(":core:feature-flags-api")
include(":core:navigation-api")
include(":core:analytics")
include(":core:date-calculations")
include(":core:platform-common")
include(":core:platform-api")
include(":core:testing")

// data - persistence, cloud, sync, and thin platform-contract api modules
include(":data:repository-api")
include(":data:repository")
include(":data:cloud-api")
include(":data:cloud")
include(":data:sync")
include(":data:files-api")
include(":data:files")
include(":data:work-api")
include(":data:work")
include(":data:icalendar-api")
include(":data:icalendar")
include(":data:holidays-api")
include(":data:holidays")
include(":data:legal-api")
include(":data:legal")
include(":data:googlecalendar-api")
include(":data:location-api")
include(":data:notification-api")
include(":data:scheduler-api")

// ui - shared Compose building blocks, no navigation/ViewModels
include(":ui:ui-common")
include(":ui:ui-agenda")
include(":ui:ui-birthday")
include(":ui:ui-googletask")
include(":ui:ui-group")
include(":ui:ui-map")
include(":ui:ui-note")
include(":ui:ui-notification-settings")
include(":ui:ui-reminder")
include(":ui:ui-tag")

// logic - cross-feature business logic
include(":logic:logic-birthday")
include(":logic:logic-googletask")
include(":logic:logic-group")
include(":logic:logic-note")
include(":logic:logic-reminder")
include(":logic:logic-schedule")
include(":logic:logic-tag")
include(":logic:logic-workflow")

// feature - vertical feature slices, only :app depends on these
include(":feature:feature-note")
include(":feature:feature-birthday")
include(":feature:feature-calendar")
include(":feature:feature-googletask")
include(":feature:feature-group")
include(":feature:feature-reminder")
include(":feature:feature-tags")
include(":feature:feature-insights")
include(":feature:feature-workflow")
include(":feature:feature-settings")

// extensions - cross-feature, flavor/runtime-gated additions app pulls in as a unit
include(":extensions:appwidgets")
include(":extensions:appwidgets-api")
include(":extensions:appfunctions")
include(":extensions:localbackup")

// admin - debug/internal-only tooling, excluded from release builds
include(":admin:cloudtestadmin")
include(":admin:reviews")
include(":admin:reviewsadmin")
