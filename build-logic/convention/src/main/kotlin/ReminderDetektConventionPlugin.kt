package com.github.naz013.buildlogic

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

class ReminderDetektConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("io.gitlab.arturbosch.detekt")

      extensions.configure<DetektExtension> {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
      }

      dependencies.apply {
        add("detektPlugins", catalog.findLibrary("detekt-formatting").get())
        add("detektPlugins", catalog.findLibrary("detekt-compose").get())
      }

      tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        reports {
          html.required.set(false)
          xml.required.set(false)
          txt.required.set(false)
          md.required.set(false)
          sarif.required.set(true)
          // detekt doesn't apply a default outputLocation convention on this version, so set it
          // explicitly to keep it queryable when wiring modules into the root reportMerge task.
          sarif.outputLocation.set(layout.buildDirectory.file("reports/detekt/${name}.sarif"))
        }
      }
    }
  }
}
