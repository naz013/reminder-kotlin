import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
  id("reminder.android.application.compose")
  alias(libs.plugins.google.services)
  alias(libs.plugins.crashlytics.gradle)
}

extensions.configure<ApplicationExtension> {
  namespace = "com.example.cloudtestadmin"

  defaultConfig {
    applicationId = "com.cray.software.justreminderpro"
    versionCode = 1
    versionName = "1.0"
  }

  val propsFile = file("${rootProject.rootDir}/keystore.properties")
  val props = Properties()
  if (propsFile.exists() && propsFile.canRead()) {
    println("> Property file exist")
    props.load(propsFile.inputStream())
  } else {
    println("> Property file does not exist")
  }

  val debugKeyStoreFile = props.getProperty("debugKeyStoreFile")
  val debugKeyStoreFileExists = debugKeyStoreFile?.let { file(it).exists() } ?: false
  println("> Debug keystore file exists = $debugKeyStoreFileExists")

  val shouldSign = props.getProperty("signApk").toBoolean() && debugKeyStoreFileExists
  println("> Should sign APK = $shouldSign")

  if (shouldSign) {
    signingConfigs {
      create("debugApp") {
        storeFile = file(props.getProperty("debugKeyStoreFile"))
        storePassword = props.getProperty("debugKeyStorePassword")
        keyAlias = props.getProperty("debugKeyAlias")
        keyPassword = props.getProperty("debugKeyPassword")
      }
    }
  }

  buildTypes {
    debug {
      if (shouldSign) {
        signingConfig = signingConfigs.getByName("debugApp")
      }
    }
  }

  packaging {
    resources {
      excludes += "META-INF/INDEX.LIST"
    }
  }
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":logging"))
  implementation(project(":cloud-api"))
  implementation(project(":cloud"))
  implementation(project(":feature-common"))
  implementation(project(":platform-common"))
  implementation(project(":ui-common"))
  implementation(project(":sync"))
  implementation(project(":files-api"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.androidx.lifecycle.extensions)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.common.java8)
  implementation(libs.androidx.lifecycle.livedata.ktx)

  implementation(libs.play.services.auth)

  implementation(libs.gson)

  implementation(libs.androidx.activity.compose)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.material.iconsext)
  implementation(libs.compose.runtime.livedata)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)

  implementation(libs.coil)
  implementation("io.coil-kt:coil-compose:2.7.0")

  debugImplementation(libs.compose.ui.test.manifest)
  debugImplementation(libs.compose.ui.tooling.preview)

  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.mockk)
  testImplementation(libs.robolectric)
}
