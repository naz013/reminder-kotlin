package com.github.naz013.wearsync

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.logic.notificationaction.WearPreferences
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Keeps the Wear companion's glanceable reminder list current. Started once from application
 * startup; for the lifetime of the process it observes the same "active, not removed" reminder
 * set the phone's own widgets/agenda read, and republishes it to [WearSyncGateway] on every
 * change - reminder create/edit/complete/snooze/delete all funnel through
 * [ReminderV2Repository], so no other module needs to know this sync exists.
 */
class WearReminderSyncCoordinator(
  private val reminderV2Repository: ReminderV2Repository,
  private val wearSyncGateway: WearSyncGateway,
  private val wearPreferences: WearPreferences,
  private val mapper: WearReminderSummaryMapper,
  private val dispatcherProvider: DispatcherProvider,
) {
  private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.io())

  fun start() {
    reminderV2Repository.observeByRemovedStatus(removed = false)
      .map { reminders -> reminders.filter { it.isActive }.toUpcomingSummaries() }
      .onEach { summaries ->
        if (wearPreferences.isWearEnabled) {
          Logger.d(TAG, "Pushing ${summaries.size} reminders to Wear")
          wearSyncGateway.pushReminders(summaries)
        }
      }
      .launchIn(scope)
  }

  private fun List<ReminderV2>.toUpcomingSummaries(): List<WearReminderSummary> =
    sortedWith(compareBy(nullsLast()) { it.schedule.eventDateTime })
      .take(WearSyncProtocol.MAX_SYNCED_REMINDERS)
      .map { mapper.toSummary(it) }

  companion object {
    private const val TAG = "WearReminderSync"
  }
}
