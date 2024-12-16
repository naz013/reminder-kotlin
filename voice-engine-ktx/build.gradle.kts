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
  compilerOptions {
    jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
  }
}

ktlint {
  android = false
  outputColorName.set("RED")
}

dependencies {

  implementation(project(":logging-api"))
  implementation(libs.kotlin.stdlib)
  implementation(libs.commons.lang3)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
