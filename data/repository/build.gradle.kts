plugins {
  id("reminder.android.library")
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.github.naz013.repository"

  @Suppress("UnstableApiUsage")
  testFixtures {
    enable = true
  }
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":data:repository-api"))
  implementation(project(":core:logging-api"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.androidx.localbroadcastmanager)

  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  ksp(libs.androidx.room.compiler)

  implementation(libs.gson)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)

  // TestRepositoryModule (instrumented-test Koin seam, published to other modules' androidTest
  // source sets) needs these on its own compile classpath - it isn't implicitly inherited from
  // the `implementation` deps above, only from this module's own `main` source set.
  testFixturesImplementation(project(":data:repository-api"))
  testFixturesImplementation(libs.androidx.room.runtime)
  testFixturesImplementation(libs.koin.core)
}
