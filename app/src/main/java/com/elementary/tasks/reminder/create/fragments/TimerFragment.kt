package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.UiUsedTimeList
import com.elementary.tasks.core.utils.bindProperty
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.ClosableLegacyBuilderWarningView
import com.elementary.tasks.core.views.TimerPickerView
import com.elementary.tasks.databinding.FragmentReminderTimerBinding
import com.elementary.tasks.databinding.ListItemUsedTimeBinding
import com.elementary.tasks.reminder.create.fragments.timer.UiUsedTimeListDiffCallback
import com.elementary.tasks.reminder.create.fragments.timer.UsedTimeViewModel
import com.github.naz013.common.datetime.minusMillis
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.inflater
import com.github.naz013.ui.common.view.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

class TimerFragment : RepeatableTypeFragment<FragmentReminderTimerBinding>() {

  private val timesAdapter = TimesAdapter()
  private val viewModel by viewModel<UsedTimeViewModel>()

  override fun getExplanationVisibilityType(): ReminderExplanationVisibility.Type {
    return ReminderExplanationVisibility.Type.BY_TIMER
  }

  override fun getExplanationView(): View {
    return binding.explanationView
  }

  override fun setCloseListenerToExplanationView(listener: View.OnClickListener) {
    binding.explanationView.setOnClickListener(listener)
  }

  override fun prepare(): Reminder? {
    val reminder = iFace.state.reminder
    val after = binding.timerPickerView.timerValue
    if (after == 0L) {
      iFace.showSnackbar(getString(R.string.you_dont_insert_timer_time))
      return null
    }
    var type = Reminder.BY_TIME
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
        Reminder.BY_TIME_CALL
      } else {
        Reminder.BY_TIME_SMS
      }
    }

    reminder.target = number
    reminder.type = type
    reminder.after = after
    reminder.delay = 0
    reminder.eventCount = 0
    reminder.recurDataObject = null

    val startTime = modelDateTimeFormatter.generateNextTimer(reminder, true)
    Logger.d("EVENT_TIME ${dateTimeManager.logDateTime(startTime)}")

    if (!validBefore(startTime, reminder)) {
      iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
      return null
    }

    if (!dateTimeManager.isCurrent(startTime.minusMillis(reminder.remindBefore))) {
      iFace.showSnackbar(getString(R.string.reminder_is_outdated))
      return null
    }

    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(startTime)

    viewModel.saveTime(after)
    return reminder
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderTimerBinding.inflate(inflater, container, false)

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
      binding.priorityView,
      binding.repeatLimitView,
      binding.repeatView,
      binding.actionView
    )
  }

  override fun getLegacyMessageView(): ClosableLegacyBuilderWarningView {
    return binding.legacyBuilderWarningView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initMostUsedList()
    binding.tuneExtraView.hasAutoExtra = false

    binding.timerPickerView.setListener(object : TimerPickerView.TimerListener {
      override fun onTimerChange(time: Long) {
        iFace.state.reminder.after = time
      }
    })

    binding.exclusionView.bindProperty(
      iFace.state.reminder.hours,
      iFace.state.reminder.from,
      iFace.state.reminder.to
    ) { hours, from, to ->
      iFace.state.reminder.hours = hours
      iFace.state.reminder.from = from
      iFace.state.reminder.to = to
    }

    editReminder()
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.usedTimeList.nonNullObserve(viewLifecycleOwner) {
      timesAdapter.submitList(it)
      if (it.isEmpty()) {
        binding.mostUserTimes.gone()
      } else {
        binding.mostUserTimes.visible()
      }
    }
  }

  private fun initMostUsedList() {
    binding.mostUserTimes.layoutManager = LinearLayoutManager(
      context,
      LinearLayoutManager.HORIZONTAL,
      false
    )
    timesAdapter.listener = {
      binding.timerPickerView.timerValue = it.timeMills
    }
    binding.mostUserTimes.adapter = timesAdapter
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

  private fun editReminder() {
    binding.timerPickerView.timerValue = iFace.state.reminder.after
  }

  inner class TimesAdapter : ListAdapter<UiUsedTimeList, TimesAdapter.TimeHolder>(
    UiUsedTimeListDiffCallback()
  ) {

    var listener: ((UiUsedTimeList) -> Unit)? = null

    override fun onBindViewHolder(holder: TimeHolder, position: Int) {
      holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TimeHolder(parent)

    inner class TimeHolder(
      viewGroup: ViewGroup
    ) : HolderBinding<ListItemUsedTimeBinding>(
      ListItemUsedTimeBinding.inflate(viewGroup.inflater(), viewGroup, false)
    ) {

      init {
        binding.chipItem.setOnClickListener {
          listener?.invoke(getItem(bindingAdapterPosition))
        }
      }

      fun bind(usedTime: UiUsedTimeList) {
        binding.chipItem.text = usedTime.timeString
      }
    }
  }
}
