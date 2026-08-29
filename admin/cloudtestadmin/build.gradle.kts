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

  buildFeatures {
    buildConfig = true
  }

  val propsFile = file("${rootProject.rootDir}/keystore.properties")
  val props = Properties()
  if (propsFile.exists() && propsFile.canRead()) {
    println("> Property file exist")
    props.load(propsFile.inputStream())
  } else {
    println("> Property file does not exist")
  }

  // Shares the pro flavor's applicationId (com.cray.software.justreminderpro), so it reuses the
  // same keystore.properties key app/build.gradle.kts reads for the pro flavor.
  defaultConfig {
    buildConfigField(
      "String",
      "GOOGLE_SIGN_IN_SERVER_CLIENT_ID",
      props.getProperty("proGoogleSignInServerClientId", "\"\""),
    )
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
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:logging"))
  implementation(project(":data:cloud-api"))
  implementation(project(":data:cloud"))
  implementation(project(":core:feature-common"))
  implementation(project(":core:platform-common"))
  implementation(project(":ui:ui-common"))
  implementation(project(":data:sync"))
  implementation(project(":data:files-api"))

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
