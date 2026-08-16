package com.github.naz013.feature.reminder.settings.reminders

/**
 * Duplicated from `feature-workflow`'s `WorkflowConfig` rather than depended on directly:
 * `feature-*` modules never depend on each other (see docs/architecture.md rule 7).
 */
internal object WorkflowConfig {
  val isEnabled: Boolean
    get() = false
}
