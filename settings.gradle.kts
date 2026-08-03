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
include(":logging")
include(":logging-api")
include(":repository")
include(":repository-api")
include(":cloud")
include(":cloud-api")
include(":work")
include(":work-api")
include(":domain")
include(":date-calculations")
include(":analytics")
include(":navigation-api")
include(":appwidgets")
include(":feature-note")
include(":feature-common")
include(":platform-common")
include(":ui-common")
include(":icalendar")
include(":sync")
include(":usecase:googletasks")
include(":usecase:birthdays")
include(":usecase:reminders")
include(":usecase:notes")
include(":cloudtestadmin")
include(":reviews")
include(":reviewsadmin")
include(":legal")
include(":legal-api")
include(":files")
include(":files-api")
include(":appfunctions")
include(":tags")
include(":insights")
include(":localbackup")
