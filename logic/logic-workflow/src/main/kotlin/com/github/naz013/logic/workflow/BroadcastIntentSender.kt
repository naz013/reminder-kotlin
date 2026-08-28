package com.github.naz013.logic.workflow

/**
 * Seam over `Context.sendBroadcast()`, which this pure-JVM module can't call directly - the
 * Android SDK isn't on its compile classpath at all (unlike [com.github.naz013.workapi.WorkScheduler],
 * this has no framework-agnostic abstraction to route through). Implemented in `feature-workflow`
 * (Android-aware) and bound via Koin. Powers [com.github.naz013.domain.workflow.WorkflowAction.SendBroadcastIntent].
 */
interface BroadcastIntentSender {
  fun send(action: String, extras: Map<String, String>)
}
