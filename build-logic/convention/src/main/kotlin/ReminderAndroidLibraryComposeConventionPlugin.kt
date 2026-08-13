package com.github.naz013.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class ReminderAndroidLibraryComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("reminder.android.library")
      pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

      extensions.configure<LibraryExtension> {
        buildFeatures {
          compose = true
        }
        composeOptions {
          kotlinCompilerExtensionVersion = catalog.stringVersion("kotlinCompilerExtensionVersion")
        }

        compileOptions {
          sourceCompatibility = JavaVersion.VERSION_17
          targetCompatibility = JavaVersion.VERSION_17
        }
      }

      extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(catalog.intVersion("kotlinTargetJvm"))
        compilerOptions {
          applyReminderOptIns()
        }
      }
    }
  }
}
