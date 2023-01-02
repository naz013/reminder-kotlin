package com.elementary.tasks.core.additional

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.view_models.reminders.FollowReminderViewModel
import com.elementary.tasks.core.views.viewgroup.UiSelectorView
import com.elementary.tasks.databinding.ActivityFollowBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class FollowReminderActivity : BindingActivity<ActivityFollowBinding>() {

  private val dateTimeManager by inject<DateTimeManager>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()
  private val viewModel by viewModel<FollowReminderViewModel>()

  private var dateCallBack: (LocalDate) -> Unit = {
    binding.customDate.text = viewModel.updateCustomDate(it)
  }

  private var timeCallBack: (LocalTime) -> Unit = {
    binding.customTime.text = viewModel.updateCustomTime(it)
  }

  override fun inflateBinding() = ActivityFollowBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val receivedDate = intent.getLongExtra(Constants.SELECTED_TIME, 0)

    val millis = if (receivedDate != 0L) {
      receivedDate
    } else {
      System.currentTimeMillis()
    }
    viewModel.initDateTime(millis)

    binding.textField.hint = getString(R.string.message)
    binding.contactPhoto.gone()

    viewModel.onNumberReceived(intent.getStringExtra(Constants.SELECTED_CONTACT_NUMBER) ?: "")

    initViews()
    initExportChecks()
    initSpinner()
    initCustomTime()
    initTomorrowTime()
    initNextBusinessTime()

    initViewModel()
  }

  private fun initViewModel() {
    viewModel.contactInfo.nonNullObserve(this) {
      binding.contactInfo.text = it
    }
    viewModel.contactPhoto.nonNullObserve(this) {
      binding.contactPhoto.visible()
      binding.contactPhoto.setImageURI(it)
    }
    viewModel.state.nonNullObserve(this) {
      unCheckAll()
      when (it) {
        FollowReminderViewModel.TimeState.TOMORROW -> {
          binding.tomorrowCheck.setChecked(true)
        }

        FollowReminderViewModel.TimeState.NEXT_BUSINESS -> {
          binding.nextBusinessCheck.setChecked(true)
        }

        FollowReminderViewModel.TimeState.CUSTOM -> {
          binding.customCheck.setChecked(true)
        }

        FollowReminderViewModel.TimeState.AFTER -> {
          binding.afterCheck.setChecked(true)
        }
      }
    }
    viewModel.result.nonNullObserve(this) { commands ->
      if (commands != null) {
        when (commands) {
          Commands.SAVED -> {
            closeWindow()
          }

          else -> {
          }
        }
      }
    }
  }

  private fun unCheckAll() {
    binding.tomorrowCheck.setChecked(false)
    binding.nextBusinessCheck.setChecked(false)
    binding.customCheck.setChecked(false)
    binding.afterCheck.setChecked(false)
  }

  private fun initViews() {
    binding.fab.setOnClickListener { saveDateTask() }
    binding.typeCall.isChecked = true

    binding.tomorrowCard.setOnClickListener {
      viewModel.onNewState(FollowReminderViewModel.TimeState.TOMORROW)
    }
    binding.nextBusinessCard.setOnClickListener {
      viewModel.onNewState(FollowReminderViewModel.TimeState.NEXT_BUSINESS)
    }
    binding.customCard.setOnClickListener {
      viewModel.onNewState(FollowReminderViewModel.TimeState.CUSTOM)
    }
    binding.afterCard.setOnClickListener {
      viewModel.onNewState(FollowReminderViewModel.TimeState.AFTER)
    }
  }

  private fun initNextBusinessTime() {
    binding.nextWorkingTime.text = viewModel.initNextBusinessDateTime()
  }

  private fun initTomorrowTime() {
    binding.tomorrowTime.text = viewModel.initTomorrowDateTime()
  }

  private fun initSpinner() {
    binding.afterTime.setItems(getAfterItems())
    binding.afterTime.onItemSelectedListener = object : UiSelectorView.OnItemSelectedListener {
      override fun onItemSelected(view: UiSelectorView, position: Int) {
        viewModel.onNewState(FollowReminderViewModel.TimeState.AFTER)
      }
    }
  }

  private fun initCustomTime() {
    binding.customDate.text = viewModel.updateCustomDate(viewModel.customDate)
    binding.customTime.text = viewModel.updateCustomTime(viewModel.customTime)

    binding.customDate.setOnClickListener {
      viewModel.onNewState(FollowReminderViewModel.TimeState.CUSTOM)
      dateDialog()
    }
    binding.customTime.setOnClickListener {
      viewModel.onNewState(FollowReminderViewModel.TimeState.CUSTOM)
      timeDialog()
    }
  }

  private fun initExportChecks() {
    if (prefs.isCalendarEnabled || prefs.isStockCalendarEnabled) {
      binding.exportCheck.visible()
    } else {
      binding.exportCheck.isChecked = false
      binding.exportCheck.gone()
    }
    if (viewModel.canExportToTasks()) {
      binding.taskExport.visible()
    } else {
      binding.taskExport.isChecked = false
      binding.taskExport.gone()
    }
  }

  private fun getAfterMins(progress: Int): Int {
    var mins = 0
    when (progress) {
      0 -> mins = 5
      1 -> mins = 10
      2 -> mins = 15
      3 -> mins = 30
      4 -> mins = 45
      5 -> mins = 60
      6 -> mins = 120
      7 -> mins = 180
      8 -> mins = 240
      9 -> mins = 300
    }
    return mins
  }

  private fun dateDialog() {
    viewModel.customDate.also {
      dateTimePickerProvider.showDatePicker(this, it, dateCallBack)
    }
  }

  private fun timeDialog() {
    viewModel.customTime.also {
      dateTimePickerProvider.showTimePicker(this, it, timeCallBack)
    }
  }

  private fun saveDateTask() {
    val text = binding.textField.trimmedText()
    if (text.isEmpty()) {
      binding.textLayout.error = getString(R.string.must_be_not_empty)
      binding.textLayout.isErrorEnabled = true
      return
    }
    val due = getDateTime()
    if (!dateTimeManager.isCurrent(due)) {
      toast(R.string.select_date_in_future)
      return
    }

    val type: Int = if (binding.typeCall.isChecked) {
      Reminder.BY_DATE_CALL
    } else {
      Reminder.BY_DATE_SMS
    }

    viewModel.saveDateTask(
      text,
      type,
      due,
      binding.taskExport.isChecked,
      binding.exportCheck.isChecked
    )
  }

  private fun closeWindow() {
    removeFlags()
    finish()
  }

  private fun getDateTime(): LocalDateTime {
    return when (viewModel.getState()) {
      FollowReminderViewModel.TimeState.NEXT_BUSINESS -> viewModel.nextWorkDateTime
      FollowReminderViewModel.TimeState.TOMORROW -> viewModel.tomorrowDateTime
      FollowReminderViewModel.TimeState.CUSTOM -> viewModel.getCustomDateTime()
      else -> viewModel.getAfterDateTime(getAfterMins(binding.afterTime.selectedItemPosition()))
    }
  }

  private fun removeFlags() {
    window.clearFlags(
      WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
    )
  }

  private fun getAfterItems(): List<String> {
    val list = mutableListOf<String>()
    list.add(String.format(getString(R.string.x_minutes), 5.toString()))
    list.add(String.format(getString(R.string.x_minutes), 10.toString()))
    list.add(String.format(getString(R.string.x_minutes), 15.toString()))
    list.add(String.format(getString(R.string.x_minutes), 30.toString()))
    list.add(String.format(getString(R.string.x_minutes), 45.toString()))
    list.add(String.format(getString(R.string.x_minutes), 60.toString()))
    list.add(String.format(getString(R.string.x_hours), 2.toString()))
    list.add(String.format(getString(R.string.x_hours), 3.toString()))
    list.add(String.format(getString(R.string.x_hours), 4.toString()))
    list.add(String.format(getString(R.string.x_hours), 5.toString()))
    return list
  }

  override fun handleBackPress(): Boolean {
    closeWindow()
    return true
  }

  companion object {

    fun mockScreen(context: Context, number: String, dataTime: Long) {
      context.startActivity(
        Intent(context, FollowReminderActivity::class.java)
          .putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
          .putExtra(Constants.SELECTED_TIME, dataTime)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
      )
    }
  }
}
