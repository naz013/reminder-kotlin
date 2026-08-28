package com.github.naz013.feature.workflow

import android.content.Context
import android.content.Intent
import com.github.naz013.logic.workflow.BroadcastIntentSender

/** Sends a local, explicit-action [Intent] broadcast - the Android-aware implementation of the
 * `logic-workflow` seam powering [com.github.naz013.domain.workflow.WorkflowAction.SendBroadcastIntent]
 * (outbound Tasker integration: a Tasker "Intent Received" profile matches on [action]). */
class BroadcastIntentSenderImpl(
  private val context: Context
) : BroadcastIntentSender {
  override fun send(action: String, extras: Map<String, String>) {
    val intent = Intent(action)
    extras.forEach { (key, value) -> intent.putExtra(key, value) }
    context.sendBroadcast(intent)
  }
}
