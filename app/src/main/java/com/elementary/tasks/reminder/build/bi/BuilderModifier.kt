package com.elementary.tasks.reminder.build.bi

import android.util.Patterns
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
import com.github.naz013.domain.reminder.v2.TaskExportSettings
import com.github.naz013.googlecalendar.CalendarItem
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

abstract class BuilderModifier<T>(
  protected val storage: BiStorage<T>,
) {
  abstract fun getUiRepresentation(emptyText: String): String

  abstract fun getValue(): T?

  abstract fun update(value: T?)

  abstract fun isCorrect(): Boolean

  abstract fun putInto(reminder: ReminderV2): ReminderV2

  abstract fun setDefault()
}

abstract class DefaultModifier<T>(
  storage: BiStorage<T>,
) : BuilderModifier<T>(storage) {
  override fun getUiRepresentation(emptyText: String): String = storage.value?.toString() ?: emptyText

  override fun getValue(): T? = storage.value

  override fun update(value: T?) {
    storage.value = value
  }

  override fun isCorrect(): Boolean = true

  override fun putInto(reminder: ReminderV2): ReminderV2 = reminder

  override fun setDefault() {
    storage.value = null
  }
}

class TimerExclusionModifier(
  private val formatter: Formatter<TimerExclusion>,
) : DefaultModifier<TimerExclusion>(DefaultBiStorage()) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun putInto(reminder: ReminderV2): ReminderV2 =
    storage.value?.let {
      reminder.copy(
        notification =
          reminder.notification.copy(
            activeHours = it.hours,
            quietHoursFrom = it.from,
            quietHoursTo = it.to,
          ),
      )
    } ?: reminder
}

open class IntModifier(
  private val formatter: Formatter<Int>,
  private val initValue: Int? = null,
) : DefaultModifier<Int>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }
}

open class BooleanModifier(
  private val formatter: Formatter<Boolean>,
  private val initValue: Boolean? = null,
) : DefaultModifier<Boolean>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }
}

open class ListIntModifier(
  private val formatter: Formatter<List<Int>>,
) : DefaultModifier<List<Int>>(DefaultBiStorage()) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }
}

open class ListLongModifier(
  private val formatter: Formatter<List<Long>>,
) : DefaultModifier<List<Long>>(DefaultBiStorage()) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }
}

open class LongModifier(
  private val formatter: Formatter<Long>,
  private val initValue: Long? = null,
) : DefaultModifier<Long>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }
}

class DateModifier(
  private val formatter: Formatter<LocalDate>,
) : DefaultModifier<LocalDate>(DefaultBiStorage()) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun isCorrect(): Boolean = storage.value != null
}

class TimeModifier(
  private val formatter: Formatter<LocalTime>,
) : DefaultModifier<LocalTime>(DefaultBiStorage()) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun isCorrect(): Boolean = storage.value != null
}

class SummaryModifier : StringModifier() {
  override fun putInto(reminder: ReminderV2): ReminderV2 = reminder.copy(summary = getValue() ?: "")
}

class EmailModifier : StringModifier() {
  override fun isCorrect(): Boolean = getValue()?.matches(EMAIL_REGEX) == true

  companion object {
    private val EMAIL_REGEX = ".*@.*..*".toRegex()
  }
}

class WebAddressModifier : StringModifier() {
  override fun isCorrect(): Boolean = getValue()?.let { Patterns.WEB_URL.matcher(it).matches() } ?: false
}

abstract class StringModifier(
  storage: BiStorage<String> = DefaultBiStorage(),
) : DefaultModifier<String>(storage) {
  override fun getUiRepresentation(emptyText: String): String = storage.value ?: ""
}

open class ListStringModifier(
  private val formatter: Formatter<List<String>>,
) : DefaultModifier<List<String>>(DefaultBiStorage()) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }
}

open class DefaultStringModifier : StringModifier()

open class FormattedStringModifier(
  private val formatter: Formatter<String>,
) : DefaultStringModifier() {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }
}

class GroupModifier(
  private val initValue: UiGroupList?,
) : DefaultModifier<UiGroupList>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return value.title
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: ReminderV2): ReminderV2 =
    reminder.copy(groupId = storage.value?.id ?: initValue?.id)
}

class PhoneNumberModifier : StringModifier() {
  override fun isCorrect(): Boolean {
    val value = getValue()
    if (value.isNullOrBlank()) return false
    return value.isNotEmpty()
  }
}

class GoogleTaskListModifier(
  private val initValue: GoogleTaskList? = null,
) : DefaultModifier<GoogleTaskList>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return value.title
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: ReminderV2): ReminderV2 =
    reminder.copy(taskExport = storage.value?.let { TaskExportSettings(taskListId = it.listId) })
}

class GoogleCalendarModifier(
  private val initValue: CalendarItem? = null,
) : DefaultModifier<CalendarItem>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return value.name
  }

  override fun setDefault() {
    storage.value = initValue
  }
}

class GoogleCalendarDurationModifier(
  private val formatter: Formatter<CalendarDuration>,
  private val initValue: CalendarDuration? = null,
) : DefaultModifier<CalendarDuration>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }
}

class OtherParamsModifier(
  private val formatter: Formatter<OtherParams>,
  private val initValue: OtherParams? = OtherParams(),
) : DefaultModifier<OtherParams>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: ReminderV2): ReminderV2 =
    storage.value?.takeIf { !it.useGlobal }?.let {
      reminder.copy(
        notification =
          reminder.notification.copy(
            repeatNotification = it.repeatNotification,
            vibrate = it.vibrate,
          ),
      )
    } ?: reminder
}

class ShopItemsModifier(
  private val formatter: Formatter<List<ShopItem>>,
  private val dateTimeManager: DateTimeManager,
  private val initValue: List<ShopItem>? = emptyList(),
) : DefaultModifier<List<ShopItem>>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: ReminderV2): ReminderV2 =
    storage.value?.takeIf { it.isNotEmpty() }?.let { items ->
      reminder.copy(shoppingItems = items.map { it.toShopItemV2() })
    } ?: reminder

  override fun isCorrect(): Boolean = storage.value?.isNotEmpty() ?: false

  private fun ShopItem.toShopItemV2(): ShopItemV2 =
    ShopItemV2(
      uuId = uuId,
      summary = summary,
      isChecked = isChecked,
      isDeleted = isDeleted,
      createdAt = dateTimeManager.localToUtc(dateTimeManager.fromGmtToLocal(createTime) ?: dateTimeManager.getCurrentDateTime()),
    )
}

class PlaceModifier(
  private val formatter: Formatter<Place>,
  private val initValue: Place? = null,
) : DefaultModifier<Place>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun isCorrect(): Boolean = storage.value != null
}

class NoteModifier(
  private val formatter: Formatter<UiNoteList>,
  private val initValue: UiNoteList? = null,
) : DefaultModifier<UiNoteList>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: ReminderV2): ReminderV2 = reminder.copy(noteId = storage.value?.id ?: "")

  override fun isCorrect(): Boolean = storage.value != null
}

class RecurParamModifier<T>(
  private val initValue: T,
  private val formatter: Formatter<T>? = null,
) : DefaultModifier<T>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter?.format(value) ?: emptyText
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun isCorrect(): Boolean = storage.value != null
}
