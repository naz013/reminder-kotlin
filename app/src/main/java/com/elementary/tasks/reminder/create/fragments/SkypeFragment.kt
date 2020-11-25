package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.onChanged
import com.elementary.tasks.databinding.FragmentReminderSkypeBinding
import timber.log.Timber

class SkypeFragment : RepeatableTypeFragment<FragmentReminderSkypeBinding>() {

  override fun prepare(): Reminder? {
    val reminder = iFace.reminderState.reminder
    if (!SuperUtil.isSkypeClientInstalled(requireContext())) {
      showInstallSkypeDialog()
      return null
    }
    if (TextUtils.isEmpty(reminder.summary)) {
      binding.taskLayout.error = getString(R.string.task_summary_is_empty)
      binding.taskLayout.isErrorEnabled = true
      return null
    }
    val number = binding.skypeContact.text.toString().trim()
    if (TextUtils.isEmpty(number)) {
      iFace.showSnackbar(getString(R.string.you_dont_insert_number))
      return null
    }
    val type = getType(binding.skypeGroup.checkedRadioButtonId)
    val startTime = binding.dateView.dateTime
    Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
    if (!TimeCount.isCurrent(startTime)) {
      iFace.showSnackbar(getString(R.string.reminder_is_outdated))
      return null
    }
    if (reminder.remindBefore > 0 && startTime - reminder.remindBefore < System.currentTimeMillis()) {
      iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
      return null
    }
    val gmtTime = TimeUtil.getGmtFromDateTime(startTime)
    reminder.target = number
    reminder.type = type
    reminder.eventTime = gmtTime
    reminder.startTime = gmtTime
    reminder.after = 0L
    reminder.dayOfMonth = 0
    reminder.delay = 0
    reminder.eventCount = 0
    return reminder
  }

  private fun showInstallSkypeDialog() {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setMessage(R.string.skype_is_not_installed)
    builder.setPositiveButton(R.string.yes) { dialogInterface, _ ->
      dialogInterface.dismiss()
      SuperUtil.installSkype(requireContext())
    }
    builder.setNegativeButton(R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
    builder.create().show()
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderSkypeBinding.inflate(inflater, container, false)

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
      dateTimeView = binding.dateView,
      loudnessPickerView = binding.loudnessView,
      priorityPickerView = binding.priorityView,
      repeatLimitView = binding.repeatLimitView,
      repeatView = binding.repeatView,
      windowTypeView = binding.windowTypeView,
      calendarPicker = binding.calendarPicker
    )
  }

  override fun onNewHeader(newHeader: String) {
    binding.cardSummary.text = newHeader
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.tuneExtraView.hasAutoExtra = false
    binding.skypeContact.onChanged {
      iFace.reminderState.skypeContact = it
    }
    binding.skypeContact.setText(iFace.reminderState.skypeContact)
    editReminder()
  }

  private fun getType(checkedId: Int): Int {
    var type = Reminder.BY_SKYPE_CALL
    when (checkedId) {
      R.id.skypeCall -> type = Reminder.BY_SKYPE_CALL
      R.id.skypeChat -> type = Reminder.BY_SKYPE
      R.id.skypeVideo -> type = Reminder.BY_SKYPE_VIDEO
    }
    return type
  }

  private fun editReminder() {
    val reminder = iFace.reminderState.reminder
    when (reminder.type) {
      Reminder.BY_SKYPE_CALL -> binding.skypeCall.isChecked = true
      Reminder.BY_SKYPE_VIDEO -> binding.skypeVideo.isChecked = true
      Reminder.BY_SKYPE -> binding.skypeChat.isChecked = true
    }
    if (reminder.target != "") {
      binding.skypeContact.setText(reminder.target)
    }
  }
}
