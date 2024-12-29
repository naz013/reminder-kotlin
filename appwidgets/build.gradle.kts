plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "com.github.naz013.appwidgets"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }
  buildFeatures {
    viewBinding = true
    compose = true
  }
  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtensionVersion.get()
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = libs.versions.kotlinTargetJvm.get()
  }

  sourceSets["main"].java {
    srcDir("src/main/kotlin")
  }
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":navigation-api"))
  implementation(project(":cloud-api"))
  implementation(project(":platform-common"))
  implementation(project(":feature-common"))
  implementation(project(":ui-common"))
  implementation(project(":usecase:googletasks"))
  implementation(project(":usecase:birthdays"))
  implementation(project(":usecase:notes"))
  implementation(project(":usecase:reminders"))
  implementation(project(":analytics"))
  implementation(project(":icalendar"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.androidx.core.ktx)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.androidx.recyclerview)
  implementation(libs.material)

  implementation(libs.colorslider)
  implementation(libs.coil)

  implementation(libs.androidx.lifecycle.extensions)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.livedata.ktx)

  implementation(libs.threetenbp)

  implementation(libs.glance.appwidget)
  implementation(libs.glance.material3)
  implementation(libs.glance.preview)
  implementation(libs.glance.appwidget.preview)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.ui.tooling.preview)

  debugImplementation(libs.glance.preview)
  debugImplementation(libs.glance.appwidget.preview)
}

ktlint {
  android = true
  outputColorName.set("RED")
}
