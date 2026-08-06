package com.elementary.tasks.home.eventsview

import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Converts already-filtered [ReminderV2]/[Birthday] domain lists into the flat, chronologically
 * sorted [UiEventItem] list the Events screen renders, inserting a [UiEventHeader] at every day
 * boundary (or "Permanent"/"Disabled" bucket for reminders without a due date).
 */
class UiEventItemAdapter(
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
) {
  fun convertV2(
    reminders: List<ReminderV2>,
    groupsById: Map<String, GroupV2>,
    birthdays: List<Birthday>,
  ): List<UiEventItem> {
    val reminderItems = reminders.map { toUiEventReminderV2(it, it.groupId?.let { id -> groupsById[id] }) }
    val birthdayItems = birthdays.map { toUiEventBirthday(it) }
    val merged = (reminderItems + birthdayItems).sortedBy { it.dateTime }
    return insertHeaders(merged)
  }

  private fun toUiEventReminderV2(
    reminder: ReminderV2,
    group: GroupV2?,
  ): UiEventReminder {
    val uiReminderList = uiReminderListAdapter.createV2(reminder, group)
    return UiEventReminder(
      id = uiReminderList.id,
      dateTime = resolveReminderDateTime(uiReminderList.dueDateTime, uiReminderList.state.isActive),
      category =
        when {
          reminder.action is ReminderAction.Shopping -> EventCategory.SHOPPING
          reminder.location != null -> EventCategory.LOCATION
          else -> EventCategory.REMINDERS
        },
      mainText = uiReminderList.mainText,
      secondaryText = uiReminderList.secondaryText,
      tertiaryText = uiReminderList.tertiaryText,
      tags = uiReminderList.tags,
      actions = uiReminderList.actions,
      state = uiReminderList.state,
    )
  }

  private fun toUiEventBirthday(birthday: Birthday): UiEventBirthday {
    val uiBirthdayList = uiBirthdayListAdapter.convert(birthday)
    return UiEventBirthday(
      id = uiBirthdayList.uuId,
      dateTime = uiBirthdayList.nextBirthdayDate,
      name = uiBirthdayList.name,
      ageFormatted = uiBirthdayList.ageFormatted,
      remainingTimeFormatted = uiBirthdayList.remainingTimeFormatted,
      color = uiBirthdayList.color,
      contrastColor = uiBirthdayList.contrastColor,
      dateFormatted = uiBirthdayList.nextBirthdayDateFormatted,
    )
  }

  private fun resolveReminderDateTime(
    dueDateTime: LocalDateTime?,
    isActive: Boolean,
  ): LocalDateTime = dueDateTime ?: if (isActive) PERMANENT_SENTINEL else DISABLED_SENTINEL

  private fun insertHeaders(items: List<UiEventItem>): List<UiEventItem> {
    if (items.isEmpty()) return items

    val today = dateTimeManager.getHeaderDateFormatted(LocalDate.now())
    val tomorrow = dateTimeManager.getHeaderDateFormatted(LocalDate.now().plusDays(1))

    val result = mutableListOf<UiEventItem>()
    var previousHeader: String? = null
    items.forEach { item ->
      val header = headerTextFor(item, today, tomorrow)
      if (header != previousHeader) {
        result.add(UiEventHeader(id = "header_$header", dateTime = item.dateTime, text = header))
        previousHeader = header
      }
      result.add(item)
    }
    return result
  }

  private fun headerTextFor(
    item: UiEventItem,
    today: String,
    tomorrow: String,
  ): String =
    when (item) {
      is UiEventReminder ->
        when (item.dateTime) {
          DISABLED_SENTINEL -> textProvider.getText(R.string.disabled)
          PERMANENT_SENTINEL -> textProvider.getText(R.string.permanent)
          else -> dateHeaderText(item.dateTime.toLocalDate(), today, tomorrow)
        }

      is UiEventBirthday -> dateHeaderText(item.dateTime.toLocalDate(), today, tomorrow)
      is UiEventHeader -> item.text
    }

  private fun dateHeaderText(
    date: LocalDate,
    today: String,
    tomorrow: String,
  ): String =
    when (val formatted = dateTimeManager.getHeaderDateFormatted(date)) {
      today -> textProvider.getText(R.string.today)
      tomorrow -> textProvider.getText(R.string.tomorrow)
      else -> formatted
    }

  companion object {
    // Sentinel values pushing reminders without a due date to the end of the chronologically
    // sorted list, grouped into their own "Permanent"/"Disabled" header buckets.
    val PERMANENT_SENTINEL: LocalDateTime = LocalDateTime.of(9999, 12, 30, 0, 0)
    val DISABLED_SENTINEL: LocalDateTime = LocalDateTime.of(9999, 12, 31, 0, 0)
  }
}
