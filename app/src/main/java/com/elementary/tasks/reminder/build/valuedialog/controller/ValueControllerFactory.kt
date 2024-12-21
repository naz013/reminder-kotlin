package com.elementary.tasks.reminder.build.valuedialog.controller

import androidx.fragment.app.Fragment
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.datapicker.ApplicationPicker
import com.elementary.tasks.core.os.datapicker.ContactPicker
import com.elementary.tasks.core.os.datapicker.MultipleUriPicker
import com.elementary.tasks.core.speech.SpeechEngine
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.UriHelper
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.reminder.build.ApplicationBuilderItem
import com.elementary.tasks.reminder.build.ArrivingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.AttachmentsBuilderItem
import com.elementary.tasks.reminder.build.BeforeTimeBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.DayOfMonthBuilderItem
import com.elementary.tasks.reminder.build.DayOfYearBuilderItem
import com.elementary.tasks.reminder.build.DaysOfWeekBuilderItem
import com.elementary.tasks.reminder.build.EmailBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarDurationBuilderItem
import com.elementary.tasks.reminder.build.GoogleTaskListBuilderItem
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.ICalByDayBuilderItem
import com.elementary.tasks.reminder.build.ICalFrequencyBuilderItem
import com.elementary.tasks.reminder.build.ICalIntBuilderItem
import com.elementary.tasks.reminder.build.ICalListIntBuilderItem
import com.elementary.tasks.reminder.build.ICalStartDateBuilderItem
import com.elementary.tasks.reminder.build.ICalStartTimeBuilderItem
import com.elementary.tasks.reminder.build.ICalUntilDateBuilderItem
import com.elementary.tasks.reminder.build.ICalUntilTimeBuilderItem
import com.elementary.tasks.reminder.build.ICalWeekStartBuilderItem
import com.elementary.tasks.reminder.build.LeavingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.LedColorBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayDateBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayTimeBuilderItem
import com.elementary.tasks.reminder.build.NoteBuilderItem
import com.elementary.tasks.reminder.build.OtherParamsBuilderItem
import com.elementary.tasks.reminder.build.PhoneCallBuilderItem
import com.elementary.tasks.reminder.build.PriorityBuilderItem
import com.elementary.tasks.reminder.build.RepeatIntervalBuilderItem
import com.elementary.tasks.reminder.build.RepeatLimitBuilderItem
import com.elementary.tasks.reminder.build.RepeatTimeBuilderItem
import com.elementary.tasks.reminder.build.SmsBuilderItem
import com.elementary.tasks.reminder.build.StringBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.TimerBuilderItem
import com.elementary.tasks.reminder.build.TimerExclusionBuilderItem
import com.elementary.tasks.reminder.build.WebAddressBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.action.ApplicationController
import com.elementary.tasks.reminder.build.valuedialog.controller.action.EmailInputController
import com.elementary.tasks.reminder.build.valuedialog.controller.action.PhoneInputController
import com.elementary.tasks.reminder.build.valuedialog.controller.action.WebAddressInputController
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.AttachmentsController
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.elementary.tasks.reminder.build.valuedialog.controller.countdown.CountdownExclusionController
import com.elementary.tasks.reminder.build.valuedialog.controller.countdown.CountdownTimeController
import com.elementary.tasks.reminder.build.valuedialog.controller.datetime.BeforeTimeController
import com.elementary.tasks.reminder.build.valuedialog.controller.datetime.DateController
import com.elementary.tasks.reminder.build.valuedialog.controller.datetime.DayOfMonthController
import com.elementary.tasks.reminder.build.valuedialog.controller.datetime.DayOfYearController
import com.elementary.tasks.reminder.build.valuedialog.controller.datetime.DaysOfWeekController
import com.elementary.tasks.reminder.build.valuedialog.controller.datetime.RepeatIntervalController
import com.elementary.tasks.reminder.build.valuedialog.controller.datetime.RepeatTimeController
import com.elementary.tasks.reminder.build.valuedialog.controller.datetime.TimeController
import com.elementary.tasks.reminder.build.valuedialog.controller.extra.GroupController
import com.elementary.tasks.reminder.build.valuedialog.controller.extra.LedColorController
import com.elementary.tasks.reminder.build.valuedialog.controller.extra.NoteController
import com.elementary.tasks.reminder.build.valuedialog.controller.extra.OtherParamsController
import com.elementary.tasks.reminder.build.valuedialog.controller.extra.PriorityController
import com.elementary.tasks.reminder.build.valuedialog.controller.extra.RepeatLimitController
import com.elementary.tasks.reminder.build.valuedialog.controller.google.GoogleCalendarController
import com.elementary.tasks.reminder.build.valuedialog.controller.google.GoogleCalendarDurationController
import com.elementary.tasks.reminder.build.valuedialog.controller.google.GoogleTaskListController
import com.elementary.tasks.reminder.build.valuedialog.controller.ical.ICalDateController
import com.elementary.tasks.reminder.build.valuedialog.controller.ical.ICalDayValueListController
import com.elementary.tasks.reminder.build.valuedialog.controller.ical.ICalFreqController
import com.elementary.tasks.reminder.build.valuedialog.controller.ical.ICalIntController
import com.elementary.tasks.reminder.build.valuedialog.controller.ical.ICalIntListController
import com.elementary.tasks.reminder.build.valuedialog.controller.ical.ICalTimeController
import com.elementary.tasks.reminder.build.valuedialog.controller.ical.ICalWeekStartController
import com.elementary.tasks.reminder.build.valuedialog.controller.shopitems.SubTasksController
import com.elementary.tasks.reminder.build.valuedialog.controller.shopitems.SubTasksViewModel
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter
import com.github.naz013.domain.Place
import com.github.naz013.feature.common.android.SystemServiceProvider
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

@Suppress("UNCHECKED_CAST")
class ValueControllerFactory(
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val dateTimePickerProvider: DateTimePickerProvider,
  private val uriHelper: UriHelper,
  private val dialogues: Dialogues,
  private val systemServiceProvider: SystemServiceProvider,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val paramToTextAdapter: ParamToTextAdapter
) {

  fun create(
    fragment: Fragment,
    builderItem: BuilderItem<*>
  ): ValueController {
    return when (builderItem) {
      is DateBuilderItem,
      is LocationDelayDateBuilderItem -> DateController(builderItem as BuilderItem<LocalDate>)

      is TimeBuilderItem,
      is LocationDelayTimeBuilderItem -> TimeController(
        builderItem = builderItem as BuilderItem<LocalTime>,
        is24Format = prefs.is24HourFormat
      )

      is DaysOfWeekBuilderItem -> DaysOfWeekController(builderItem)
      is DayOfMonthBuilderItem -> DayOfMonthController(builderItem)
      is DayOfYearBuilderItem -> DayOfYearController(builderItem)
      is TimerBuilderItem -> CountdownTimeController(builderItem)
      is TimerExclusionBuilderItem -> CountdownExclusionController(
        builderItem = builderItem,
        fragment = fragment,
        dateTimeManager = dateTimeManager,
        dateTimePickerProvider = dateTimePickerProvider
      )

      is GroupBuilderItem -> GroupController(builderItem)
      is BeforeTimeBuilderItem -> BeforeTimeController(builderItem, dateTimeManager)
      is RepeatTimeBuilderItem -> RepeatTimeController(builderItem, dateTimeManager)
      is RepeatIntervalBuilderItem -> RepeatIntervalController(builderItem)
      is RepeatLimitBuilderItem -> RepeatLimitController(builderItem)
      is PriorityBuilderItem -> PriorityController(builderItem)
      is LedColorBuilderItem -> LedColorController(builderItem)
      is AttachmentsBuilderItem -> AttachmentsController(
        builderItem = builderItem,
        attachmentFileAdapter = UriToAttachmentFileAdapter(uriHelper),
        multipleUriPicker = MultipleUriPicker(fragment)
      )

      is PhoneCallBuilderItem,
      is SmsBuilderItem -> PhoneInputController(
        builderItem = builderItem as BuilderItem<String>,
        permissionFlow = PermissionFlow(fragment, dialogues),
        contactPicker = ContactPicker(fragment) { },
        inputMethodManager = systemServiceProvider.provideInputMethodManager()!!
      )

      is GoogleTaskListBuilderItem -> GoogleTaskListController(builderItem)
      is GoogleCalendarBuilderItem -> GoogleCalendarController(
        googleCalendarBuilderItem = builderItem,
        calendars = googleCalendarUtils.getCalendarsList()
      )

      is GoogleCalendarDurationBuilderItem -> GoogleCalendarDurationController(
        durationBuilderItem = builderItem,
        dateTimeManager = dateTimeManager
      )

      is EmailBuilderItem -> EmailInputController(
        builderItem = builderItem,
        permissionFlow = PermissionFlow(fragment, dialogues),
        inputMethodManager = systemServiceProvider.provideInputMethodManager()!!
      )

      is WebAddressBuilderItem -> WebAddressInputController(
        builderItem = builderItem,
        inputMethodManager = systemServiceProvider.provideInputMethodManager()!!
      )

      is ApplicationBuilderItem -> ApplicationController(
        builderItem = builderItem,
        applicationPicker = ApplicationPicker(fragment) { },
        packageManagerWrapper = packageManagerWrapper
      )

      is OtherParamsBuilderItem -> OtherParamsController(builderItem)

      is SubTasksBuilderItem -> SubTasksController(
        builderItem = builderItem,
        viewModel = SubTasksViewModel(dateTimeManager),
        viewLifecycleOwner = fragment.viewLifecycleOwner,
        inputMethodManager = systemServiceProvider.provideInputMethodManager()!!
      )

      is ArrivingCoordinatesBuilderItem,
      is LeavingCoordinatesBuilderItem -> MapController(
        builderItem = builderItem as BuilderItem<Place>,
        parentFragment = fragment,
        dateTimeManager = dateTimeManager
      )

      is ICalUntilDateBuilderItem,
      is ICalStartDateBuilderItem -> ICalDateController(
        builderItem = builderItem as BuilderItem<LocalDate>
      )

      is ICalUntilTimeBuilderItem,
      is ICalStartTimeBuilderItem -> ICalTimeController(
        builderItem = builderItem as BuilderItem<LocalTime>,
        is24Format = prefs.is24HourFormat
      )

      is ICalFrequencyBuilderItem -> ICalFreqController(
        builderItem = builderItem,
        paramToTextAdapter = paramToTextAdapter
      )

      is ICalWeekStartBuilderItem -> ICalWeekStartController(
        builderItem = builderItem,
        paramToTextAdapter = paramToTextAdapter
      )

      is ICalByDayBuilderItem -> ICalDayValueListController(
        builderItem = builderItem,
        paramToTextAdapter = paramToTextAdapter
      )

      is ICalListIntBuilderItem -> ICalIntListController(
        builderItem = builderItem as BuilderItem<List<Int>>,
        array = generateNumbers(
          minValue = builderItem.minValue,
          maxValue = builderItem.maxValue,
          excludedValues = builderItem.excludedValues
        )
      )

      is ICalIntBuilderItem -> ICalIntController(builderItem)

      is NoteBuilderItem -> NoteController(builderItem)

      is StringBuilderItem -> TextInputController(
        builderItem = builderItem,
        inputMethodManager = systemServiceProvider.provideInputMethodManager()!!,
        speechEngine = SpeechEngine(fragment.requireContext()),
        permissionFlow = PermissionFlow(fragment, dialogues)
      )

      else -> {
        throw IllegalArgumentException("This type ${builderItem.biType} is not supported!")
      }
    }
  }

  private fun generateNumbers(
    minValue: Int,
    maxValue: Int,
    excludedValues: IntArray
  ): List<Int> {
    val list = mutableListOf<Int>()
    for (i in minValue..maxValue) {
      list.add(i)
    }
    excludedValues.forEach {
      list.remove(it)
    }
    return list
  }
}
