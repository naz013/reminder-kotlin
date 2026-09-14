package com.github.naz013.feature.reminder.quickadd

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.logging.Logger
import com.github.naz013.logic.quickadd.QuickAddParser
import com.github.naz013.logic.quickadd.QuickAddResult
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.locale.Language
import com.github.naz013.ui.common.locale.LocalePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/** Backs the quick-add bottom sheet: live-parses [QuickAddScreenState.input] as the user types or
 * dictates (via [QuickAddParser]), exposing a [QuickAddPreview] of the recognized title/date/
 * time/recurrence below the field that the user can adjust ([onDateSelected]/[onTimeSelected]/
 * [onRepeatOptionSelected]) before saving. Either saves a fully-understood reminder directly, or
 * hands the leftover text (plus whatever schedule *was* recognized) off to the full build-reminder
 * wizard for anything it couldn't resolve. */
class QuickAddViewModel(
  private val quickAddParser: QuickAddParser,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val dateTimeManager: DateTimeManager,
  private val localePreferences: LocalePreferences,
  private val textProvider: TextProvider,
  private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(QuickAddScreenState())
  val state: StateFlow<QuickAddScreenState> = _state.asStateFlow()
  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  private var lastResult: QuickAddResult = QuickAddResult.Empty

  /** Non-null once the user has adjusted the date/time/recurrence preview directly - takes
   * precedence over whatever [lastResult] parsed, until the next edit to the text field replaces
   * it with a fresh parse (see [onInputChanged]). */
  private var manualStartDateTime: LocalDateTime? = null
  private var manualRecurrence: RecurrenceRule? = null

  fun onInputChanged(text: String) {
    lastResult = parse(text)
    manualStartDateTime = null
    manualRecurrence = null
    refreshState(text)
  }

  fun onDateSelected(date: LocalDate) {
    val anchor = effectiveStartDateTime() ?: LocalDateTime.now()
    manualStartDateTime = LocalDateTime.of(date, anchor.toLocalTime())
    refreshState(_state.value.input)
  }

  fun onTimeSelected(time: LocalTime) {
    val anchor = effectiveStartDateTime() ?: LocalDateTime.now()
    manualStartDateTime = LocalDateTime.of(anchor.toLocalDate(), time)
    refreshState(_state.value.input)
  }

  fun onRepeatOptionSelected(option: QuickAddRepeatOption) {
    val anchor = effectiveStartDateTime() ?: LocalDateTime.now()
    manualRecurrence = when (option) {
      QuickAddRepeatOption.NONE -> RecurrenceRule.Once
      QuickAddRepeatOption.DAILY -> RecurrenceRule.Daily()
      QuickAddRepeatOption.WEEKLY ->
        RecurrenceRule.Weekly(weekdays = weekdayBitmask(listOf(anchor.dayOfWeek.toAppWeekday())))
      QuickAddRepeatOption.MONTHLY -> RecurrenceRule.Monthly(dayOfMonth = anchor.dayOfMonth)
      QuickAddRepeatOption.YEARLY ->
        RecurrenceRule.Yearly(dayOfMonth = anchor.dayOfMonth, monthOfYear = anchor.monthValue)
    }
    refreshState(_state.value.input)
  }

  fun onSaveClick() {
    val preview = _state.value.preview
    if (preview != null && preview.title.isNotBlank()) {
      Logger.i(TAG, "Saving quick-add reminder")
      save(preview)
    } else {
      Logger.i(TAG, "Quick-add phrase not fully understood, handing off to the full editor")
      openFullEditor()
    }
  }

  fun onEditManuallyClick() {
    openFullEditor()
  }

  /** Hands whatever's currently understood off to the full wizard - title via [textForFullEditor],
   * schedule (parsed and/or manually adjusted) via [quickAddDeepLinkOf] - rather than throwing it
   * away, whether nothing was recognized, only the schedule was, or the user picked "continue in
   * full editor" on a fully-understood phrase instead of saving it directly. */
  private fun openFullEditor() {
    event.emit(
      ViewModelEvent.OpenFullEditor(
        text = textForFullEditor(lastResult, _state.value.input),
        quickAddDeepLink = quickAddDeepLinkOf(),
      ),
    )
  }

  private fun save(preview: QuickAddPreview) {
    viewModelScope.launch(dispatcherProvider.io()) {
      val utcDateTime = dateTimeManager.localToUtc(preview.startDateTime)
      saveReminderUseCase(
        ReminderV2(
          summary = preview.title,
          recurrence = preview.recurrence,
          schedule = ReminderSchedule(startDateTime = utcDateTime, eventDateTime = utcDateTime),
        ),
      )
      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.Dismiss)
      }
    }
  }

  private fun parse(text: String): QuickAddResult {
    val languageTag = Language.getScreenLanguage(localePreferences.appLanguage).language
    return quickAddParser.parse(text, languageTag, LocalDateTime.now())
  }

  private fun refreshState(text: String) {
    val preview = buildPreview()
    _state.update {
      it.copy(
        input = text,
        matchedRanges = matchedRangesOf(lastResult),
        canSave = preview != null && preview.title.isNotBlank(),
        preview = preview,
      )
    }
  }

  private fun buildPreview(): QuickAddPreview? {
    val startDateTime = effectiveStartDateTime() ?: return null
    val recurrence = manualRecurrence ?: recurrenceOf(lastResult) ?: RecurrenceRule.Once
    return QuickAddPreview(
      title = titleOf(lastResult),
      startDateTime = startDateTime,
      recurrence = recurrence,
      dateText = dateTimeManager.getDate(startDateTime.toLocalDate()),
      timeText = dateTimeManager.getTime(startDateTime.toLocalTime()),
      repeatText = repeatTextFor(recurrence),
    )
  }

  private fun effectiveStartDateTime(): LocalDateTime? = manualStartDateTime ?: scheduleOf(lastResult)

  private fun repeatTextFor(recurrence: RecurrenceRule): String =
    when (recurrence) {
      RecurrenceRule.Once -> textProvider.getString(R.string.quick_add_does_not_repeat)
      is RecurrenceRule.Daily -> textProvider.getString(R.string.recur_daily)
      is RecurrenceRule.Weekly -> textProvider.getString(R.string.recur_weekly)
      is RecurrenceRule.Monthly -> textProvider.getString(R.string.recur_monthly)
      is RecurrenceRule.Yearly -> textProvider.getString(R.string.recur_yearly)
      else -> textProvider.getString(R.string.repeat)
    }

  private fun titleOf(result: QuickAddResult): String =
    when (result) {
      is QuickAddResult.Parsed -> result.title
      is QuickAddResult.PartiallyParsed -> result.title
      QuickAddResult.Empty -> ""
    }

  private fun scheduleOf(result: QuickAddResult): LocalDateTime? =
    when (result) {
      is QuickAddResult.Parsed -> result.startDateTime
      is QuickAddResult.PartiallyParsed -> result.startDateTime
      QuickAddResult.Empty -> null
    }

  private fun recurrenceOf(result: QuickAddResult): RecurrenceRule? =
    when (result) {
      is QuickAddResult.Parsed -> result.recurrence
      is QuickAddResult.PartiallyParsed -> result.recurrence
      QuickAddResult.Empty -> null
    }

  private fun matchedRangesOf(result: QuickAddResult): List<IntRange> =
    when (result) {
      is QuickAddResult.Parsed -> result.matchedRanges
      is QuickAddResult.PartiallyParsed -> result.matchedRanges
      QuickAddResult.Empty -> emptyList()
    }

  /** [QuickAddResult.title] already holds exactly the right wizard title in every case: the full
   * raw text when nothing was recognized, or "" when a schedule was found but left no title behind
   * (e.g. "tomorrow at 9am") - falling back to [rawInput] there would just re-duplicate the phrase
   * that [quickAddDeepLinkOf] is separately seeding as the date/recurrence. [rawInput] is only
   * needed for [QuickAddResult.Empty] (blank input, or a language quick-add doesn't cover), which
   * carries no title at all. */
  private fun textForFullEditor(result: QuickAddResult, rawInput: String): String =
    when (result) {
      is QuickAddResult.Parsed -> result.title
      is QuickAddResult.PartiallyParsed -> result.title
      QuickAddResult.Empty -> rawInput
    }

  /** The current schedule (parsed and/or manually adjusted), translated into the wizard's
   * deep-link shape - `null` if nothing schedule-related is understood at all. */
  private fun quickAddDeepLinkOf(): BuildReminderNavKey.Main.QuickAddDeepLink? {
    val startDateTime = effectiveStartDateTime() ?: return null
    val recurrence = manualRecurrence ?: recurrenceOf(lastResult) ?: RecurrenceRule.Once
    return deepLinkFor(dateTimeManager.toMillis(startDateTime), recurrence)
  }

  private fun deepLinkFor(millis: Long, recurrence: RecurrenceRule): BuildReminderNavKey.Main.QuickAddDeepLink =
    when (recurrence) {
      RecurrenceRule.Once -> BuildReminderNavKey.Main.QuickAddDeepLink(startDateTimeMillis = millis)
      is RecurrenceRule.Daily ->
        BuildReminderNavKey.Main.QuickAddDeepLink(
          startDateTimeMillis = millis,
          recurrenceType = BuildReminderNavKey.Main.QuickAddDeepLink.RecurrenceType.DAILY,
          interval = recurrence.repeatInterval,
        )
      is RecurrenceRule.Weekly ->
        BuildReminderNavKey.Main.QuickAddDeepLink(
          startDateTimeMillis = millis,
          recurrenceType = BuildReminderNavKey.Main.QuickAddDeepLink.RecurrenceType.WEEKLY,
          interval = recurrence.repeatInterval,
          weekdays = recurrence.weekdays,
        )
      is RecurrenceRule.Monthly ->
        BuildReminderNavKey.Main.QuickAddDeepLink(
          startDateTimeMillis = millis,
          recurrenceType = BuildReminderNavKey.Main.QuickAddDeepLink.RecurrenceType.MONTHLY,
          interval = recurrence.repeatInterval,
          dayOfMonth = recurrence.dayOfMonth,
        )
      is RecurrenceRule.Yearly ->
        BuildReminderNavKey.Main.QuickAddDeepLink(
          startDateTimeMillis = millis,
          recurrenceType = BuildReminderNavKey.Main.QuickAddDeepLink.RecurrenceType.YEARLY,
          interval = recurrence.repeatInterval,
          dayOfMonth = recurrence.dayOfMonth,
          monthOfYear = recurrence.monthOfYear,
        )
      // The parser never produces these - fall back to a one-off anchored at the recognized time.
      else -> BuildReminderNavKey.Main.QuickAddDeepLink(startDateTimeMillis = millis)
    }

  /** The app's reminder domain uses 0=Sunday..6=Saturday (see
   * [RecurrenceRule.RelativeMonthly]), while [DayOfWeek] is ISO-8601's 1=Monday..7=Sunday. */
  private fun DayOfWeek.toAppWeekday(): Int = if (this == DayOfWeek.SUNDAY) 0 else value

  /** [RecurrenceRule.Weekly.weekdays] is a 7-element bitmask (index 0=Sunday..6=Saturday, 1 if
   * that day repeats) - see `ByWeekdaysDecomposer`/`WeekDaysProtocol`, the actual consumers of
   * this field once it reaches the build-reminder wizard or the occurrence scheduler. */
  private fun weekdayBitmask(selectedAppWeekdays: List<Int>): List<Int> =
    List(7) { day -> if (day in selectedAppWeekdays) 1 else 0 }

  sealed interface ViewModelEvent {
    data class OpenFullEditor(
      val text: String,
      val quickAddDeepLink: BuildReminderNavKey.Main.QuickAddDeepLink? = null,
    ) : ViewModelEvent

    data object Dismiss : ViewModelEvent
  }

  data class QuickAddScreenState(
    val input: String = "",
    val matchedRanges: List<IntRange> = emptyList(),
    val canSave: Boolean = false,
    val preview: QuickAddPreview? = null,
  )

  /** The live "what will be saved" preview shown below the input field, editable via
   * [onDateSelected]/[onTimeSelected]/[onRepeatOptionSelected]. [dateText]/[timeText]/[repeatText]
   * are already locale/24h-formatted, ready to render as-is. */
  data class QuickAddPreview(
    val title: String,
    val startDateTime: LocalDateTime,
    val recurrence: RecurrenceRule,
    val dateText: String,
    val timeText: String,
    val repeatText: String,
  )

  companion object {
    private const val TAG = "QuickAddViewModel"
  }
}

/** The handful of recurrence choices the quick-add preview's repeat picker offers - a compact
 * stand-in for the full recurrence-rule builder, since anything more exotic than these five isn't
 * feasible to expose in a one-tap dropdown. Weekday/day-of-month/month-of-year are always derived
 * from the preview's current date, matching how [QuickAddParser] itself fills in the same gaps. */
enum class QuickAddRepeatOption {
  NONE,
  DAILY,
  WEEKLY,
  MONTHLY,
  YEARLY,
}
