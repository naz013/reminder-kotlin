package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.UsedTime
import com.elementary.tasks.core.utils.bindProperty
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.minusMillis
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.view_models.used_time.UsedTimeViewModel
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.TimerPickerView
import com.elementary.tasks.databinding.FragmentReminderTimerBinding
import com.elementary.tasks.databinding.ListItemUsedTimeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TimerFragment : RepeatableTypeFragment<FragmentReminderTimerBinding>() {

  private val timesAdapter = TimesAdapter()
  private val viewModel by viewModel<UsedTimeViewModel>()

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
      type = if (binding.actionView.type == ActionView.TYPE_CALL) {
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

    val startTime = dateTimeManager.generateNextTimer(reminder, true)
    Timber.d("EVENT_TIME ${dateTimeManager.logDateTime(startTime)}")

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

  override fun provideViews() {
    setViews(
      scrollView = binding.scrollView,
      expansionLayout = binding.moreLayout,
      ledPickerView = binding.ledView,
      calendarCheck = binding.exportToCalendar,
      tasksCheck = binding.exportToTasks,
      extraView = binding.tuneExtraView,
      melodyView = binding.melodyView,
      attachmentView = binding.attachmentView,
      groupView = binding.groupView,
      summaryView = binding.taskSummary,
      beforePickerView = binding.beforeView,
      loudnessPickerView = binding.loudnessView,
      priorityPickerView = binding.priorityView,
      repeatLimitView = binding.repeatLimitView,
      repeatView = binding.repeatView,
      windowTypeView = binding.windowTypeView,
      actionView = binding.actionView,
      calendarPicker = binding.calendarPicker
    )
  }

  override fun onNewHeader(newHeader: String) {
    binding.cardSummary.text = newHeader
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

    binding.exclusionView.bindProperty(iFace.state.reminder.hours, iFace.state.reminder.from,
      iFace.state.reminder.to) { hours, from, to ->
      iFace.state.reminder.hours = hours
      iFace.state.reminder.from = from
      iFace.state.reminder.to = to
    }

    editReminder()
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.usedTimeList.observe(viewLifecycleOwner) {
      if (it != null) {
        timesAdapter.updateData(it)
        if (it.isEmpty()) {
          binding.mostUserTimes.gone()
        } else {
          binding.mostUserTimes.visible()
        }
      } else {
        binding.mostUserTimes.gone()
      }
    }
  }

  private fun initMostUsedList() {
    binding.mostUserTimes.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    timesAdapter.listener = {
      binding.timerPickerView.timerValue = it.timeMills
    }
    binding.mostUserTimes.adapter = timesAdapter
  }

  override fun updateActions() {
    if (binding.actionView.hasAction()) {
      if (binding.actionView.type == ActionView.TYPE_MESSAGE) {
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

  inner class TimesAdapter : RecyclerView.Adapter<TimesAdapter.TimeHolder>() {

    private val data: MutableList<UsedTime> = mutableListOf()
    var listener: ((UsedTime) -> Unit)? = null

    fun updateData(list: List<UsedTime>) {
      this.data.clear()
      this.data.addAll(list)
      notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TimeHolder, position: Int) {
      holder.bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TimeHolder(parent)

    override fun getItemCount() = data.size

    inner class TimeHolder(
      viewGroup: ViewGroup
    ) : HolderBinding<ListItemUsedTimeBinding>(
      ListItemUsedTimeBinding.inflate(viewGroup.inflater(), viewGroup, false)
    ) {

      init {
        binding.chipItem.setOnClickListener {
          listener?.invoke(data[bindingAdapterPosition])
        }
      }

      fun bind(usedTime: UsedTime) {
        binding.chipItem.text = usedTime.timeString
      }
    }
  }
}
