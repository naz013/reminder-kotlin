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
  alias(libs.plugins.kover)
}

// Aggregate coverage from JVM-only modules (Android modules are excluded to avoid
// variant-selection complexity; per-module koverXmlReport tasks work for those).
dependencies {
  kover(project(":domain"))
  kover(project(":date-calculations"))
  kover(project(":logging-api"))
  kover(project(":navigation-api"))
  kover(project(":repository-api"))
  kover(project(":cloud-api"))
  kover(project(":work-api"))
  kover(project(":legal-api"))
  kover(project(":files-api"))
  kover(project(":sync"))
  kover(project(":usecase:reminders"))
  kover(project(":usecase:notes"))
  kover(project(":usecase:birthdays"))
  kover(project(":usecase:googletasks"))
}
