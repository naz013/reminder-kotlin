import java.net.URI

include(":cloud")


include(":cloud-api")


include(":feature-common")


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
include(":repository-api")
include(":domain")
include(":analytics")
include(":repository")
