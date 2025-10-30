plugins {
  alias(libs.plugins.java.library)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktlint)
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
  jvmToolchain(libs.versions.kotlinTargetJvm.get().toInt())
}

ktlint {
  android = false
  outputColorName.set("RED")
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":cloud-api"))
  implementation(project(":repository-api"))
  implementation(project(":logging-api"))

  implementation(libs.koin.core)
  implementation(libs.threetenbp)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
