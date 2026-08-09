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
include(":holidays")
include(":holidays-api")
include(":domain")
include(":date-calculations")
include(":analytics")
include(":navigation-api")
include(":appwidgets")
include(":appwidgets-api")
include(":platform-common")

include(":ui-common")
include(":ui-googletask")
include(":ui-reminder")
include(":ui-tag")

include(":icalendar")
include(":icalendar-api")
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

include(":feature-note")
include(":feature-common")
include(":feature-googletask")
include(":feature-reminder")
include(":feature-tags")
include(":feature-insights")

include(":localbackup")

include(":logic-googletask")
include(":logic-reminder")
include(":logic-schedule")
include(":logic-tag")

include(":testing")
include(":scheduler-api")
include(":location-api")
include(":notification-api")
include(":googlecalendar-api")
include(":platform-api")
