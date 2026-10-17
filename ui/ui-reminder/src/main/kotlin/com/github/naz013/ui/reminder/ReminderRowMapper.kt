package com.github.naz013.ui.reminder

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/** Fields every reminder row (Home, Agenda) derives identically from a [ReminderV2]. */
data class ReminderRowFields(
  val dueDateTime: LocalDateTime?,
  val groupName: String?,
  val groupColor: Int?,
  val action: ReminderAction,
  val secondaryText: String?,
  val isOverdue: Boolean,
)

/** Single source of truth for the reminder row fields shared by Home and Agenda. */
class ReminderRowMapper(
  private val dateTimeManager: DateTimeManager,
  private val shopItemsFormatter: ShopItemsFormatter,
) {
  fun map(
    reminder: ReminderV2,
    group: GroupV2?,
    now: LocalDateTime = dateTimeManager.getCurrentDateTime(),
  ): ReminderRowFields {
    val dueDateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) }
    return ReminderRowFields(
      dueDateTime = dueDateTime,
      groupName = group?.title,
      groupColor = group?.color,
      action = reminder.action,
      secondaryText = createSecondaryText(reminder),
      isOverdue = isOverdue(reminder.isActive, dueDateTime, now),
    )
  }

  fun isOverdue(
    isActive: Boolean,
    dueDateTime: LocalDateTime?,
    now: LocalDateTime = dateTimeManager.getCurrentDateTime(),
  ): Boolean = isActive && dueDateTime != null && !dueDateTime.isAfter(now)

  private fun createSecondaryText(reminder: ReminderV2): String? =
    when (val action = reminder.action) {
      is ReminderAction.Shopping -> formatSubTasks(reminder)
      is ReminderAction.Call -> action.target
      is ReminderAction.Sms -> action.target
      is ReminderAction.Link -> action.target
      is ReminderAction.App -> action.target
      is ReminderAction.Email -> "${action.target}\n${action.subject}"
      ReminderAction.None -> null
    }

  private fun formatSubTasks(reminder: ReminderV2): String {
    val itemsToShow = reminder.shoppingItems.filter { !it.isChecked && !it.isDeleted }
    return if (itemsToShow.size > 5) {
      shopItemsFormatter.formatV2(itemsToShow.take(5)) + "\n..."
    } else {
      shopItemsFormatter.formatV2(itemsToShow)
    }
  }
}
