plugins {
  `kotlin-dsl`
}

group = "com.github.naz013.buildlogic"

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.detekt.gradlePlugin)
  compileOnly(libs.kover.gradlePlugin)
}

kotlin {
  jvmToolchain(17)
}

gradlePlugin {
  plugins {
    register("kotlinJvm") {
      id = "reminder.kotlin.jvm"
      implementationClass = "com.github.naz013.buildlogic.ReminderKotlinJvmConventionPlugin"
    }
    register("androidLibrary") {
      id = "reminder.android.library"
      implementationClass = "com.github.naz013.buildlogic.ReminderAndroidLibraryConventionPlugin"
    }
    register("androidLibraryCompose") {
      id = "reminder.android.library.compose"
      implementationClass = "com.github.naz013.buildlogic.ReminderAndroidLibraryComposeConventionPlugin"
    }
    register("androidApplication") {
      id = "reminder.android.application"
      implementationClass = "com.github.naz013.buildlogic.ReminderAndroidApplicationConventionPlugin"
    }
    register("androidApplicationCompose") {
      id = "reminder.android.application.compose"
      implementationClass = "com.github.naz013.buildlogic.ReminderAndroidApplicationComposeConventionPlugin"
    }
    register("detekt") {
      id = "reminder.detekt"
      implementationClass = "com.github.naz013.buildlogic.ReminderDetektConventionPlugin"
    }
    register("kover") {
      id = "reminder.kover"
      implementationClass = "com.github.naz013.buildlogic.ReminderKoverConventionPlugin"
    }
  }
}
