import java.net.URI

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
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
include(":voice-engine-ktx")
include(":logging")
include(":logging-api")
include(":repository")
include(":repository-api")
include(":cloud")
include(":cloud-api")
include(":domain")
include(":analytics")
include(":navigation-api")
include(":appwidgets")
include(":feature-common")
include(":platform-common")
include(":ui-common")
include(":icalendar")
include(":usecase:googletasks")
include(":usecase:birthdays")
include(":usecase:reminders")
include(":usecase:notes")
