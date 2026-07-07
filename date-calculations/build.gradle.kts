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
  compilerOptions {
    optIn.add("-Xreturn-value-checker=check")
    optIn.add("-Xexplicit-backing-fields")
    optIn.add("-Xname-based-destructuring=only-syntax")
    optIn.add("-Xdata-flow-based-exhaustiveness")
    optIn.add("-Xcollection-literals")
  }
}

dependencies {
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}

ktlint {
  android = false
  outputColorName.set("RED")
}
