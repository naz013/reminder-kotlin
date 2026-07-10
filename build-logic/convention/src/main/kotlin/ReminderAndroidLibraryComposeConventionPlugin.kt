package com.github.naz013.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

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
      }
    }
  }
}
