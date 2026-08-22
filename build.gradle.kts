import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.parcelize) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.crashlytics.gradle) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.detekt) apply false
}

// GitHub Code Scanning rejects multiple SARIF runs uploaded under the same category, so all
// per-module detekt SARIF reports are merged into a single run before upload in CI.
val detektReportMerge by tasks.registering(ReportMergeTask::class) {
  output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

// detekt's `outputLocation` report property isn't wired with producer-task metadata in 1.23.x
// (https://github.com/detekt/detekt/issues/6980), so implicit dependency inference on it throws.
// Resolve the destination path eagerly instead and order after the tasks explicitly.
//
// Uses `mustRunAfter` rather than `dependsOn`: detekt writes its SARIF report before checking
// the maxIssues threshold, so the file exists even when a module's detekt task fails the build.
// `dependsOn` would make Gradle SKIP this task the moment any single module has a violation,
// which defeats the point of a merged report in CI (run with --continue).
subprojects {
  if (name in setOf("cloudtestadmin", "reviewsadmin")) return@subprojects
  plugins.withId("io.gitlab.arturbosch.detekt") {
    afterEvaluate {
      val detektTasks = tasks.withType<Detekt>()
      detektReportMerge.configure {
        mustRunAfter(detektTasks)
        input.from(detektTasks.map { it.reports.sarif.outputLocation.get() })
      }
    }
  }
}
