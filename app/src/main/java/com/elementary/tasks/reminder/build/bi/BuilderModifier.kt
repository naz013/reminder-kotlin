package com.elementary.tasks.reminder.build.bi

import android.util.Patterns
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.ShopItem
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.reminder.build.formatter.Formatter
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

abstract class BuilderModifier<T>(
  protected val storage: BiStorage<T>
) {
  abstract fun getUiRepresentation(emptyText: String): String
  abstract fun getValue(): T?
  abstract fun update(value: T?)
  abstract fun isCorrect(): Boolean
  abstract fun putInto(reminder: Reminder)
  abstract fun setDefault()
}

abstract class DefaultModifier<T>(
  storage: BiStorage<T>
) : BuilderModifier<T>(storage) {
  override fun getUiRepresentation(emptyText: String): String {
    return storage.value?.toString() ?: emptyText
  }

  override fun getValue(): T? {
    return storage.value
  }

  override fun update(value: T?) {
    storage.value = value
  }

  override fun isCorrect(): Boolean {
    return true
  }

  override fun putInto(reminder: Reminder) {
    // Do nothing
  }

  override fun setDefault() {
    storage.value = null
  }
}

class TimerExclusionModifier(
  private val formatter: Formatter<TimerExclusion>
) : DefaultModifier<TimerExclusion>(DefaultBiStorage()) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun putInto(reminder: Reminder) {
    storage.value?.let {
      reminder.hours = it.hours
      reminder.from = it.from
      reminder.to = it.to
    }
  }
}

open class IntModifier(
  private val formatter: Formatter<Int>,
  private val initValue: Int? = null
) : DefaultModifier<Int>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }
}

open class ListIntModifier(
  private val formatter: Formatter<List<Int>>
) : DefaultModifier<List<Int>>(DefaultBiStorage()) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }
}

open class LongModifier(
  private val formatter: Formatter<Long>,
  private val initValue: Long? = null
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
  private val formatter: Formatter<LocalDate>
) : DefaultModifier<LocalDate>(DefaultBiStorage()) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun isCorrect(): Boolean {
    return storage.value != null
  }
}

class TimeModifier(
  private val formatter: Formatter<LocalTime>
) : DefaultModifier<LocalTime>(DefaultBiStorage()) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun isCorrect(): Boolean {
    return storage.value != null
  }
}

class SummaryModifier : StringModifier() {
  override fun putInto(reminder: Reminder) {
    reminder.summary = getValue() ?: ""
  }
}

class EmailModifier : StringModifier() {
  override fun isCorrect(): Boolean {
    return getValue()?.matches(EMAIL_REGEX) == true
  }

  override fun putInto(reminder: Reminder) {
    reminder.target = getValue() ?: ""
  }

  companion object {
    private val EMAIL_REGEX = ".*@.*..*".toRegex()
  }
}

class WebAddressModifier : StringModifier() {
  override fun isCorrect(): Boolean {
    return getValue()?.let { Patterns.WEB_URL.matcher(it).matches() } ?: false
  }

  override fun putInto(reminder: Reminder) {
    reminder.target = getValue() ?: ""
  }
}

abstract class StringModifier(
  storage: BiStorage<String> = DefaultBiStorage()
) : DefaultModifier<String>(storage) {

  override fun getUiRepresentation(emptyText: String): String {
    return storage.value ?: ""
  }
}

open class ListStringModifier(
  private val formatter: Formatter<List<String>>
) : DefaultModifier<List<String>>(DefaultBiStorage()) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }
}

open class DefaultStringModifier : StringModifier()

open class FormattedStringModifier(
  private val formatter: Formatter<String>
) : DefaultStringModifier() {

  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }
}

class GroupModifier(
  private val initValue: UiGroupList?
) : DefaultModifier<UiGroupList>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return value.title
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: Reminder) {
    reminder.groupUuId = storage.value?.id ?: initValue?.id ?: ""
    reminder.groupColor = storage.value?.color ?: initValue?.color ?: 0
  }
}

class PhoneNumberModifier : StringModifier() {

  override fun isCorrect(): Boolean {
    val value = getValue()
    if (value.isNullOrBlank()) return false
    return value.isNotEmpty()
  }

  override fun putInto(reminder: Reminder) {
    reminder.target = getValue() ?: ""
  }
}

class GoogleTaskListModifier(
  private val initValue: GoogleTaskList? = null
) : DefaultModifier<GoogleTaskList>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return value.title
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: Reminder) {
    storage.value?.run {
      reminder.taskListId = this.listId
      reminder.exportToTasks = true
    } ?: run {
      reminder.taskListId = null
      reminder.exportToTasks = false
    }
  }
}

class GoogleCalendarModifier(
  private val initValue: GoogleCalendarUtils.CalendarItem? = null
) : DefaultModifier<GoogleCalendarUtils.CalendarItem>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return value.name
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: Reminder) {
    reminder.calendarId = storage.value?.id ?: 0L
    reminder.exportToCalendar = storage.value?.id != 0L
  }
}

class GoogleCalendarDurationModifier(
  private val formatter: Formatter<CalendarDuration>,
  private val initValue: CalendarDuration? = null
) : DefaultModifier<CalendarDuration>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: Reminder) {
    storage.value?.also {
      reminder.duration = it.millis
      reminder.allDay = it.allDay
    }
  }
}

class OtherParamsModifier(
  private val formatter: Formatter<OtherParams>,
  private val initValue: OtherParams? = OtherParams()
) : DefaultModifier<OtherParams>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: Reminder) {
    storage.value?.takeIf { !it.useGlobal }?.also {
      reminder.repeatNotification = it.repeatNotification
      reminder.notifyByVoice = it.notifyByVoice
      reminder.vibrate = it.vibrate
    }
  }
}

class ShopItemsModifier(
  private val formatter: Formatter<List<ShopItem>>,
  private val initValue: List<ShopItem>? = emptyList()
) : DefaultModifier<List<ShopItem>>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: Reminder) {
    storage.value?.takeIf { it.isNotEmpty() }?.also {
      reminder.shoppings = it
    }
  }

  override fun isCorrect(): Boolean {
    return storage.value?.isNotEmpty() ?: false
  }
}

class PlaceModifier(
  private val formatter: Formatter<Place>,
  private val initValue: Place? = null
) : DefaultModifier<Place>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: Reminder) {
    storage.value?.also {
      reminder.places = listOf(it)
    }
  }

  override fun isCorrect(): Boolean {
    return storage.value != null
  }
}

class NoteModifier(
  private val formatter: Formatter<UiNoteList>,
  private val initValue: UiNoteList? = null
) : DefaultModifier<UiNoteList>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter.format(value)
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun putInto(reminder: Reminder) {
    storage.value?.also {
      reminder.noteId = it.id
    }
  }

  override fun isCorrect(): Boolean {
    return storage.value != null
  }
}

class RecurParamModifier<T>(
  private val initValue: T,
  private val formatter: Formatter<T>? = null
) : DefaultModifier<T>(DefaultBiStorage(initValue)) {
  override fun getUiRepresentation(emptyText: String): String {
    val value = storage.value ?: return emptyText
    return formatter?.format(value) ?: emptyText
  }

  override fun setDefault() {
    storage.value = initValue
  }

  override fun isCorrect(): Boolean {
    return storage.value != null
  }
}
