package com.elementary.tasks.home.agenda

import com.elementary.tasks.R
import com.github.naz013.ui.birthday.UiBirthdayListAdapter
import com.github.naz013.ui.reminder.UiReminderListAdapter
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Converts already-filtered [ReminderV2]/[Birthday] domain lists into the flat [UiAgendaItem] list
 * the Agenda screen renders, inserting a [UiAgendaHeader] at every day boundary within the
 * chronological due-date section, followed by the Permanent/Location/Shopping-lists/Disabled
 * buckets (see [resolveReminderDateTime]) in that order.
 */
class UiAgendaItemAdapter(
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
) {
  fun convertV2(
    reminders: List<ReminderV2>,
    groupsById: Map<String, GroupV2>,
    birthdays: List<Birthday>,
  ): List<UiAgendaItem> {
    val reminderItems = reminders.map { toUiAgendaReminderV2(it, it.groupId?.let { id -> groupsById[id] }) }
    val birthdayItems = birthdays.map { toUiAgendaBirthday(it) }
    val merged = (reminderItems + birthdayItems).sortedBy { it.dateTime }
    return insertHeaders(merged)
  }

  private fun toUiAgendaReminderV2(
    reminder: ReminderV2,
    group: GroupV2?,
  ): UiAgendaReminder {
    val uiReminderList = uiReminderListAdapter.createV2(reminder, group)
    return UiAgendaReminder(
      id = uiReminderList.id,
      dateTime = resolveReminderDateTime(reminder, uiReminderList.dueDateTime, uiReminderList.state.isActive),
      category =
        when {
          reminder.action is ReminderAction.Shopping -> AgendaCategory.SHOPPING
          reminder.location != null -> AgendaCategory.LOCATION
          else -> AgendaCategory.REMINDERS
        },
      mainText = uiReminderList.mainText,
      secondaryText = uiReminderList.secondaryText,
      tertiaryText = uiReminderList.tertiaryText,
      tags = uiReminderList.tags,
      actions = uiReminderList.actions,
      state = uiReminderList.state,
    )
  }

  private fun toUiAgendaBirthday(birthday: Birthday): UiAgendaBirthday {
    val uiBirthdayList = uiBirthdayListAdapter.convert(birthday)
    return UiAgendaBirthday(
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

  /**
   * Reminders sort into buckets, in this order: due-date reminders (chronological, alongside
   * birthdays and shopping lists that have a due date), permanent reminders, location-based
   * reminders (always here regardless of due date), shopping lists without a due date, and
   * finally disabled reminders without a due date. Encoded as far-future sentinel dates so the
   * whole merged list can still be sorted with a single [sortedBy] on [UiAgendaItem.dateTime].
   */
  private fun resolveReminderDateTime(
    reminder: ReminderV2,
    dueDateTime: LocalDateTime?,
    isActive: Boolean,
  ): LocalDateTime =
    when {
      !isActive && dueDateTime == null -> DISABLED_SENTINEL
      reminder.location != null -> LOCATION_SENTINEL
      dueDateTime != null -> dueDateTime
      reminder.action is ReminderAction.Shopping -> SHOPPING_SENTINEL
      else -> PERMANENT_SENTINEL
    }

  private fun insertHeaders(items: List<UiAgendaItem>): List<UiAgendaItem> {
    if (items.isEmpty()) return items

    val today = dateTimeManager.getHeaderDateFormatted(LocalDate.now())
    val tomorrow = dateTimeManager.getHeaderDateFormatted(LocalDate.now().plusDays(1))

    val result = mutableListOf<UiAgendaItem>()
    var previousHeader: String? = null
    items.forEach { item ->
      val header = headerTextFor(item, today, tomorrow)
      if (header != previousHeader) {
        result.add(UiAgendaHeader(id = "header_$header", dateTime = item.dateTime, text = header))
        previousHeader = header
      }
      result.add(item)
    }
    return result
  }

  private fun headerTextFor(
    item: UiAgendaItem,
    today: String,
    tomorrow: String,
  ): String =
    when (item) {
      is UiAgendaReminder ->
        when (item.dateTime) {
          DISABLED_SENTINEL -> textProvider.getText(R.string.disabled)
          SHOPPING_SENTINEL -> textProvider.getText(R.string.shopping_lists)
          LOCATION_SENTINEL -> textProvider.getText(R.string.location)
          PERMANENT_SENTINEL -> textProvider.getText(R.string.permanent)
          else -> dateHeaderText(item.dateTime.toLocalDate(), today, tomorrow)
        }

      is UiAgendaBirthday -> dateHeaderText(item.dateTime.toLocalDate(), today, tomorrow)
      is UiAgendaHeader -> item.text
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
    // sorted list, grouped into their own header buckets, in this order: Permanent, Location,
    // Shopping (no due date), Disabled.
    val PERMANENT_SENTINEL: LocalDateTime = LocalDateTime.of(9999, 12, 28, 0, 0)
    val LOCATION_SENTINEL: LocalDateTime = LocalDateTime.of(9999, 12, 29, 0, 0)
    val SHOPPING_SENTINEL: LocalDateTime = LocalDateTime.of(9999, 12, 30, 0, 0)
    val DISABLED_SENTINEL: LocalDateTime = LocalDateTime.of(9999, 12, 31, 0, 0)
  }
}
