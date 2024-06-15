package com.elementary.tasks.reminder.build.bi

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.repository.NoteRepository
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.radius.DefaultRadiusFormatter
import com.elementary.tasks.reminder.build.ApplicationBuilderItem
import com.elementary.tasks.reminder.build.ArrivingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.AttachmentsBuilderItem
import com.elementary.tasks.reminder.build.BeforeTimeBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.DayOfMonthBuilderItem
import com.elementary.tasks.reminder.build.DayOfYearBuilderItem
import com.elementary.tasks.reminder.build.DaysOfWeekBuilderItem
import com.elementary.tasks.reminder.build.DescriptionBuilderItem
import com.elementary.tasks.reminder.build.EmailBuilderItem
import com.elementary.tasks.reminder.build.EmailSubjectBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarDurationBuilderItem
import com.elementary.tasks.reminder.build.GoogleTaskListBuilderItem
import com.elementary.tasks.reminder.build.GroupBuilderItem
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
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.TimerBuilderItem
import com.elementary.tasks.reminder.build.TimerExclusionBuilderItem
import com.elementary.tasks.reminder.build.WebAddressBuilderItem
import com.elementary.tasks.reminder.build.adapter.BiTypeForUiAdapter
import com.elementary.tasks.reminder.build.formatter.ApplicationFormatter
import com.elementary.tasks.reminder.build.formatter.AttachmentsFormatter
import com.elementary.tasks.reminder.build.formatter.CalendarDurationFormatter
import com.elementary.tasks.reminder.build.formatter.LedColorFormatter
import com.elementary.tasks.reminder.build.formatter.OtherParamsFormatter
import com.elementary.tasks.reminder.build.formatter.`object`.PlaceFormatter
import com.elementary.tasks.reminder.build.formatter.PriorityFormatter
import com.elementary.tasks.reminder.build.formatter.RepeatLimitFormatter
import com.elementary.tasks.reminder.build.formatter.`object`.ShopItemsFormatter
import com.elementary.tasks.reminder.build.formatter.TimerExclusionFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.BeforeTimeFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.DateFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.DayOfMonthFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.DayOfYearFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.RepeatIntervalFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.RepeatTimeFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.TimeFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.TimerFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.WeekdayArrayFormatter
import com.elementary.tasks.reminder.build.formatter.`object`.NoteFormatter
import timber.log.Timber

class BiFactory(
  private val contextProvider: ContextProvider,
  private val biTypeForUiAdapter: BiTypeForUiAdapter,
  private val dateTimeManager: DateTimeManager,
  private val groupDao: ReminderGroupDao,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val gTasks: GTasks,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val prefs: Prefs,
  private val biFactoryICal: BiFactoryICal,
  private val noteRepository: NoteRepository,
  private val uiNoteListAdapter: UiNoteListAdapter
) {

  private val context: Context = contextProvider.themedContext

  fun <V, T : BuilderItem<V>> createWithValue(biType: BiType, value: V?, clazz: Class<T>): T? {
    return createTyped(biType, clazz)
      ?.apply { modifier.update(value) }
  }

  private fun <T : BuilderItem<*>> createTyped(biType: BiType, clazz: Class<T>): T? {
    val created = create(biType)
    Timber.d("createTyped: created=$created, wanted=$clazz")
    return created.takeIf { it::class.java == clazz } as? T
  }

  fun create(biType: BiType): BuilderItem<*> {
    return when (biType) {
      BiType.SUMMARY -> {
        SummaryBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_what_you_want_to_remind_you)
        )
      }

      BiType.DESCRIPTION -> {
        DescriptionBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_write_more_details)
        )
      }

      BiType.DATE -> {
        DateBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_provide_date),
          dateFormatter = DateFormatter(dateTimeManager)
        )
      }

      BiType.TIME -> {
        TimeBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_provide_time),
          timeFormatter = TimeFormatter(dateTimeManager)
        )
      }

      BiType.DAYS_OF_WEEK -> {
        DaysOfWeekBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_repeat_on_selected_days),
          weekdayArrayFormatter = WeekdayArrayFormatter(context)
        )
      }

      BiType.DAY_OF_MONTH -> {
        DayOfMonthBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_repeat_on_selected_day_of_month),
          dayOfMonthFormatter = DayOfMonthFormatter(context)
        )
      }

      BiType.DAY_OF_YEAR -> {
        DayOfYearBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_repeat_on_selected_day_of_year),
          dayOfYearFormatter = DayOfYearFormatter(dateTimeManager)
        )
      }

      BiType.COUNTDOWN_TIMER -> {
        TimerBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_set_countdown_time),
          timerFormatter = TimerFormatter(context)
        )
      }

      BiType.COUNTDOWN_TIMER_EXCLUSION -> {
        TimerExclusionBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(
            R.string.builder_add_exclusion_when_do_not_trigger_notification
          ),
          timerExclusionFormatter = TimerExclusionFormatter(context, dateTimeManager)
        )
      }

      BiType.GROUP -> {
        GroupBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.choose_group),
          groups = groupDao.all().map { uiGroupListAdapter.convert(it) },
          defaultGroup = groupDao.defaultGroup()?.let { uiGroupListAdapter.convert(it) }
        )
      }

      BiType.BEFORE_TIME -> {
        BeforeTimeBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(
            R.string.builder_set_how_much_time_before_the_event_should_show_the_notification
          ),
          beforeTimeFormatter = BeforeTimeFormatter(context, dateTimeManager)
        )
      }

      BiType.REPEAT_TIME -> {
        RepeatTimeBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_set_how_often_the_event_should_repeat),
          repeatTimeFormatter = RepeatTimeFormatter(context, dateTimeManager)
        )
      }

      BiType.REPEAT_INTERVAL -> {
        RepeatIntervalBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_set_interval_for_the_repeating),
          repeatIntervalFormatter = RepeatIntervalFormatter()
        )
      }

      BiType.REPEAT_LIMIT -> {
        RepeatLimitBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_set_number_of_repetitions),
          repeatLimitFormatter = RepeatLimitFormatter(context)
        )
      }

      BiType.PRIORITY -> {
        PriorityBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_set_priority_for_the_notification),
          priorityFormatter = PriorityFormatter(context)
        )
      }

      BiType.LED_COLOR -> {
        LedColorBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_set_led_color_for_the_notification),
          ledColorFormatter = LedColorFormatter(context)
        )
      }

      BiType.ATTACHMENTS -> {
        AttachmentsBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_add_attachments_to_the_reminder),
          attachmentsFormatter = AttachmentsFormatter(context)
        )
      }

      BiType.PHONE_CALL -> {
        PhoneCallBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_make_a_phone_call_to_the_number)
        )
      }

      BiType.SMS -> {
        SmsBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_send_sms_to_the_number)
        )
      }

      BiType.GOOGLE_TASK_LIST -> {
        GoogleTaskListBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(
            R.string.builder_select_google_task_list_where_the_reminder_should_be_added
          ),
          taskLists = googleTaskListsDao.all(),
          gTasks = gTasks
        )
      }

      BiType.GOOGLE_CALENDAR -> {
        GoogleCalendarBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(
            R.string.builder_select_google_calendar_where_the_reminder_should_be_added
          )
        )
      }

      BiType.GOOGLE_CALENDAR_DURATION -> {
        GoogleCalendarDurationBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(
            R.string.builder_set_duration_for_the_event_in_the_google_calendar
          ),
          calendarDurationFormatter = CalendarDurationFormatter(context, dateTimeManager)
        )
      }

      BiType.EMAIL -> {
        EmailBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_set_email_action_to_the_reminder)
        )
      }

      BiType.EMAIL_SUBJECT -> {
        EmailSubjectBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_set_subject_for_the_email)
        )
      }

      BiType.LINK -> {
        WebAddressBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_set_open_link_action_to_the_reminder)
        )
      }

      BiType.APPLICATION -> {
        ApplicationBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_set_application_action_to_the_reminder),
          applicationFormatter = ApplicationFormatter(packageManagerWrapper)
        )
      }

      BiType.OTHER_PARAMS -> {
        OtherParamsBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(
            R.string.builder_customize_additional_parameters_for_a_reminder
          ),
          otherParamsFormatter = OtherParamsFormatter(context)
        )
      }

      BiType.SUB_TASKS -> {
        SubTasksBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_add_list_of_subtasks_to_the_reminder),
          shopItemsFormatter = ShopItemsFormatter(context)
        )
      }

      BiType.ARRIVING_COORDINATES -> {
        ArrivingCoordinatesBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_arriving_destination_description),
          placeFormatter = PlaceFormatter(
            DefaultRadiusFormatter(
              context = context,
              useMetric = prefs.useMetric
            )
          )
        )
      }

      BiType.LEAVING_COORDINATES -> {
        LeavingCoordinatesBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_leaving_place_description),
          placeFormatter = PlaceFormatter(
            DefaultRadiusFormatter(
              context = context,
              useMetric = prefs.useMetric
            )
          )
        )
      }

      BiType.LOCATION_DELAY_DATE -> {
        LocationDelayDateBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_delay_date_description),
          dateFormatter = DateFormatter(dateTimeManager)
        )
      }

      BiType.LOCATION_DELAY_TIME -> {
        LocationDelayTimeBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_delay_time_description),
          timeFormatter = TimeFormatter(dateTimeManager)
        )
      }

      BiType.NOTE -> {
        NoteBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_attach_note_to_the_reminder),
          noteFormatter = NoteFormatter(),
          notes = noteRepository.getAll(isArchived = false).map {
            uiNoteListAdapter.convert(it)
          }
        )
      }

      else -> {
        if (BiGroup.ICAL.types.contains(biType)) {
          biFactoryICal.create(biType)
        } else {
          throw IllegalArgumentException("Unknown biType: $biType")
        }
      }
    }
  }
}
