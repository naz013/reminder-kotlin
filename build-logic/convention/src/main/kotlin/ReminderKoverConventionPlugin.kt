package com.github.naz013.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class ReminderKoverConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.pluginManager.apply("org.jetbrains.kotlinx.kover")
  }
}
