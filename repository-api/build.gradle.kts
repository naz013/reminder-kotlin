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

dependencies {
  implementation(project(":domain"))
}

ktlint {
  android = false
  outputColorName.set("RED")
}
