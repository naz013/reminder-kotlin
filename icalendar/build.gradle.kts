import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.ktlint)
}

extensions.configure<LibraryExtension> {
  namespace = "com.github.naz013.icalendar"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
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
  implementation(project(":domain"))
  implementation(project(":logging-api"))

  implementation(libs.koin.android)

  implementation(libs.gson)
  implementation(libs.threetenbp)
  implementation(libs.lib.recur)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
}

ktlint {
  android = true
  outputColorName.set("RED")
}
