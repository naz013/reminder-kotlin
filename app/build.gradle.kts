import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.navigation.safeargs)
  alias(libs.plugins.crashlytics.gradle)
  alias(libs.plugins.google.services)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "com.elementary.tasks"
  compileSdk = libs.versions.compileSdk.get().toInt()
  setFlavorDimensions(listOf("level"))

  defaultConfig {
    applicationId = "com.cray.software.justreminder"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    versionCode 1
    versionName "9.5.0"
    multiDexEnabled = true
    renderscriptTargetApi = 23
    renderscriptSupportModeEnabled = true
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  buildFeatures {
    viewBinding = true
    buildConfig = true
    compose = true
  }

  val propsFile = file("${rootProject.rootDir}/keystore.properties")
  val props = Properties()
  if (propsFile.exists() && propsFile.canRead()) {
    println("> Property file exist")
    props.load(propsFile.inputStream())
  } else {
    println("> Property file does not exist")
  }

  val shouldSign = props.getProperty("signApk").toBoolean()
  println("> Should sign APK = $shouldSign")

  if (shouldSign) {
    signingConfigs {
      create("freeApp") {
        storeFile = file(props.getProperty("releaseFreeKeyStoreFile"))
        storePassword = props.getProperty("releaseFreeKeyStorePassword")
        keyAlias = props.getProperty("releaseFreeKeyAlias")
        keyPassword = props.getProperty("releaseFreeKeyPassword")
      }
      create("proApp") {
        storeFile = file(props.getProperty("releaseProKeyStoreFile"))
        storePassword = props.getProperty("releaseProKeyStorePassword")
        keyAlias = props.getProperty("releaseProKeyAlias")
        keyPassword = props.getProperty("releaseProKeyPassword")
      }
      create("debugApp") {
        storeFile = file(props.getProperty("debugKeyStoreFile"))
        storePassword = props.getProperty("debugKeyStorePassword")
        keyAlias = props.getProperty("debugKeyAlias")
        keyPassword = props.getProperty("debugKeyPassword")
      }
    }
  }

  productFlavors {
    create("free") {
      dimension = "level"
      applicationId = "com.cray.software.justreminder"
      buildConfigField("boolean", "IS_PRO", "false")

      val api = props.getProperty("freeApiKey", "API_KEY")
      val placesApiKey = props.getProperty("freePlacesApiKey", "\"API_KEY\"")

      println("> FREE API KEY = $api")
      println("> FREE PLACES API KEY = $placesApiKey")

      buildConfigField("String", "PLACES_API_KEY", placesApiKey)
      manifestPlaceholders["apiKey"] = api
    }
    create("pro") {
      dimension = "level"
      applicationId = "com.cray.software.justreminderpro"
      buildConfigField("boolean", "IS_PRO", "true")

      val api = props.getProperty("proApiKey", "API_KEY")
      val placesApiKey = props.getProperty("proPlacesApiKey", "\"API_KEY\"")

      println("> PRO API KEY = $api")
      println("> PRO PLACES API KEY = $placesApiKey")

      buildConfigField("String", "PLACES_API_KEY", placesApiKey)
      manifestPlaceholders["apiKey"] = api
    }
  }
  packaging {
    resources {
      excludes += "META-INF/NOTICE"
      excludes += "META-INF/LICENSE.txt"
      excludes += "META-INF/NOTICE.txt"
      excludes += "META-INF/proguard/androidx-annotations.pro"
      excludes += "META-INF/DEPENDENCIES"
      excludes += "META-INF/LICENSE"
      excludes += "META-INF/license.txt"
      excludes += "META-INF/ASL2.0"
      excludes += "META-INF/LICENSE.md"
    }
  }
  buildTypes {
    release {
      buildConfigField("String", "BUILD_DATE", "\"${getDate()}\"")
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      isDebuggable = false
      isJniDebuggable = false
      if (shouldSign) {
        productFlavors["free"].apply {
          signingConfig = signingConfigs["freeApp"]
        }
        productFlavors["pro"].apply {
          signingConfig = signingConfigs["proApp"]
        }
      }
    }
    debug {
      buildConfigField("String", "BUILD_DATE", "\"${getDateAndTime()}\"")
      isMinifyEnabled = false
      if (shouldSign) {
        signingConfig = signingConfigs["debugApp"]
      }
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
  @Suppress("UnstableApiUsage")
  testOptions {
    unitTests {
      isReturnDefaultValues = true
      isIncludeAndroidResources = true
    }
  }
  configurations.configureEach {
    resolutionStrategy {
      force("com.google.code.findbugs:jsr305:3.0.2")
    }
    exclude(module = "httpclient")
    exclude(group = "com.google.guava", module = "listenablefuture")
  }
  sourceSets {
    get("main").java.srcDirs("src/main/kotlin")
  }
  lint {
    checkReleaseBuilds = false
    abortOnError = false
  }
}

configurations.testImplementation {
  exclude(module = "logback-android")
}

fun getDateAndTime(): String {
  return DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm:ss").format(LocalDateTime.now())
}

fun getDate(): String {
  return DateTimeFormatter.ofPattern("MMMM dd, yyyy").format(LocalDateTime.now())
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":repository-api"))
  implementation(project(":logging"))
  implementation(project(":analytics"))
  implementation(project(":repository"))
  implementation(project(":cloud-api"))
  implementation(project(":cloud"))
  implementation(project(":feature-common"))
  implementation(project(":appwidgets"))
  implementation(project(":navigation-api"))
  implementation(project(":platform-common"))
  implementation(project(":ui-common"))
  implementation(project(":usecase:googletasks"))
  implementation(project(":usecase:birthdays"))
  implementation(project(":usecase:notes"))
  implementation(project(":usecase:reminders"))
  implementation(project(":icalendar"))

  implementation(libs.google.api.services.calendar) {
    exclude(group = "com.google.guava", module = "listenablefuture")
  }
  implementation(libs.google.api.client.android) {
    exclude(group = "org.apache.httpcomponents")
  }

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.workmanager)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.material)

  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.multidex)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.swiperefreshlayout)
  implementation(libs.androidx.viewpager2)
  implementation(libs.androidx.splashscreen)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.collection.ktx)
  implementation(libs.androidx.palette.ktx)
  implementation(libs.androidx.dynamicanimation.ktx)

  implementation(libs.androidx.work.runtime) {
    exclude(group = "com.google.guava", module = "listenablefuture")
  }
  implementation(libs.androidx.work.runtime.ktx) {
    exclude(group = "com.google.guava", module = "listenablefuture")
  }

  implementation(libs.androidx.lifecycle.extensions)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.common.java8)
  implementation(libs.androidx.lifecycle.livedata.ktx)

  implementation(libs.androidx.navigation.fragment.ktx)
  implementation(libs.androidx.navigation.ui.ktx)

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics)
  implementation(libs.firebase.auth.ktx)
  implementation(libs.firebase.config)

  implementation(libs.play.services.location)
  implementation(libs.play.services.maps)
  implementation(libs.play.services.auth)

  "freeImplementation"(libs.play.services.ads)
  "freeImplementation"(libs.user.messaging.platform)

  implementation(libs.circleimageview)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.gson)
  implementation(libs.gson)
  implementation(libs.jsr305)
  implementation(libs.okhttp3.logging.interceptor)
  implementation(libs.lib.recur)
  implementation(libs.commons.lang3)
  implementation(libs.colorslider)
  implementation(libs.android.calendar.ext)
  implementation(libs.lottie)
  implementation(libs.photoview)
  implementation(libs.sheets.core)
  implementation(libs.sheets.info)
  implementation(libs.sheets.lottie)
  implementation(libs.sheets.input)

  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlin.reflect)

  implementation(libs.threetenbp)
  implementation(libs.coil)

  implementation(libs.slf4j.api)
  implementation(libs.logback.android)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)

  debugImplementation(libs.compose.ui.test.manifest)
  debugImplementation(libs.compose.ui.tooling.preview)

  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.mockk)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.androidx.core.testing)
  testImplementation(libs.androidx.lifecycle.runtime.testing)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
}

ktlint {
  android = true
  outputColorName.set("RED")
}
