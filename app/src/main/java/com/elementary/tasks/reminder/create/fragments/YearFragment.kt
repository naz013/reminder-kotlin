package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.ClosableLegacyBuilderRemovalWarningView
import com.elementary.tasks.core.views.DateTimeView
import com.elementary.tasks.databinding.FragmentReminderYearBinding
import com.github.naz013.common.datetime.minusMillis
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class YearFragment : RepeatableTypeFragment<FragmentReminderYearBinding>() {

  override fun getExplanationVisibilityType(): ReminderExplanationVisibility.Type {
    return ReminderExplanationVisibility.Type.BY_YEAR
  }

  override fun getExplanationView(): View {
    return binding.explanationView
  }

  override fun setCloseListenerToExplanationView(listener: View.OnClickListener) {
    binding.explanationView.setOnClickListener(listener)
  }

  override fun prepare(): Reminder? {
    val reminder = iFace.state.reminder
    var type = Reminder.BY_DAY_OF_YEAR
    val isAction = binding.actionView.hasAction()
    if (TextUtils.isEmpty(reminder.summary) && !isAction) {
      binding.taskLayout.error = getString(R.string.task_summary_is_empty)
      binding.taskLayout.isErrorEnabled = true
      return null
    }
    var number = ""
    if (isAction) {
      number = binding.actionView.number
      if (TextUtils.isEmpty(number)) {
        iFace.showSnackbar(getString(R.string.you_dont_insert_number))
        return null
      }
      type = if (binding.actionView.actionState == ActionView.ActionState.CALL) {
        Reminder.BY_DAY_OF_YEAR_CALL
      } else {
        Reminder.BY_DAY_OF_YEAR_SMS
      }
    }
    reminder.weekdays = listOf()
    reminder.target = number
    reminder.type = type
    reminder.dayOfMonth = iFace.state.day
    reminder.monthOfYear = iFace.state.month
    reminder.after = 0L
    reminder.delay = 0
    reminder.eventCount = 0
    reminder.repeatInterval = 0
    reminder.recurDataObject = null

    reminder.eventTime = dateTimeManager.getGmtFromDateTime(getDateTime())
    val startTime = modelDateTimeFormatter.getNextYearDayTime(reminder)

    if (reminder.remindBefore > 0 &&
      !dateTimeManager.isCurrent(startTime.minusMillis(reminder.remindBefore))
    ) {
      iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
      return null
    }

    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(startTime)
    Logger.d("EVENT_TIME ${dateTimeManager.logDateTime(startTime)}")
    if (!dateTimeManager.isCurrent(reminder.eventTime)) {
      iFace.showSnackbar(getString(R.string.reminder_is_outdated))
      return null
    }
    return reminder
  }

  private fun getDateTime(): LocalDateTime {
    return LocalDateTime.of(LocalDate.now(), iFace.state.time.withSecond(0))
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderYearBinding.inflate(inflater, container, false)

  override fun getDynamicViews(): List<View> {
    return listOfNotNull(
      binding.ledView,
      binding.exportToCalendar,
      binding.exportToTasks,
      binding.tuneExtraView,
      binding.attachmentView,
      binding.groupView,
      binding.taskSummary,
      binding.beforeView,
      binding.dateView,
      binding.priorityView,
      binding.repeatLimitView,
      binding.actionView
    )
  }

  override fun getLegacyMessageView(): ClosableLegacyBuilderRemovalWarningView {
    return binding.legacyBuilderWarningView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.tuneExtraView.hasAutoExtra = false

    binding.dateView.setDateFormat(dateTimeManager.simpleDateFormatter())
    binding.dateView.setOnSelectListener(object : DateTimeView.OnSelectListener {
      override fun onDateSelect(date: LocalDate) {
        iFace.state.date = date
      }

      override fun onTimeSelect(time: LocalTime) {
        iFace.state.time = time
      }
    })

    binding.dateView.selectedDateTime = LocalDateTime.of(iFace.state.date, iFace.state.time)
    editReminder()
  }

  override fun updateActions() {
    if (binding.actionView.hasAction()) {
      if (binding.actionView.actionState == ActionView.ActionState.SMS) {
        binding.tuneExtraView.hasAutoExtra = false
      } else {
        binding.tuneExtraView.hasAutoExtra = true
        binding.tuneExtraView.hint = getString(R.string.enable_making_phone_calls_automatically)
      }
    } else {
      binding.tuneExtraView.hasAutoExtra = false
    }
  }

  private fun updateDateTime(reminder: Reminder) {
    val dateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: LocalDateTime.now()
    binding.dateView.selectedDateTime = dateTime
    iFace.state.date = dateTime.toLocalDate()
    iFace.state.time = dateTime.toLocalTime()
  }

  private fun editReminder() {
    updateDateTime(iFace.state.reminder)
  }
}
