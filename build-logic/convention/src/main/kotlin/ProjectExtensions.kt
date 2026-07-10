package com.github.naz013.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.catalog: VersionCatalog
  get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.intVersion(alias: String): Int = findVersion(alias).get().requiredVersion.toInt()

fun VersionCatalog.stringVersion(alias: String): String = findVersion(alias).get().requiredVersion
