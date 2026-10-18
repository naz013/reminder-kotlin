package com.github.naz013.logic.workflow

interface WorkflowConfig {
  /** The remote feature flag only: whether workflow entry points are offered in the UI. */
  val isEnabled: Boolean

  /** Whether workflows may actually run right now: the feature flag AND PRO (build or active trial). */
  val isRunnable: Boolean
}
