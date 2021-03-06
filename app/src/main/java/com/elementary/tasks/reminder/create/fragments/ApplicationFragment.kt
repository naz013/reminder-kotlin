package com.elementary.tasks.reminder.create.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.apps.SelectApplicationActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.onChanged
import com.elementary.tasks.databinding.FragmentReminderApplicationBinding
import timber.log.Timber

class ApplicationFragment : RepeatableTypeFragment<FragmentReminderApplicationBinding>() {

  private val type: Int
    get() = if (binding.application.isChecked) {
      Reminder.BY_DATE_APP
    } else {
      Reminder.BY_DATE_LINK
    }
  private val appName: String
    get() {
      val packageManager = requireContext().packageManager
      var applicationInfo: ApplicationInfo? = null
      try {
        applicationInfo = packageManager.getApplicationInfo(iFace.reminderState.app, 0)
      } catch (ignored: Exception) {
      }
      return (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
    }

  override fun prepare(): Reminder? {
    val type = type
    var number: String
    val reminder = iFace.reminderState.reminder
    if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
      number = iFace.reminderState.app
      if (TextUtils.isEmpty(number)) {
        iFace.showSnackbar(getString(R.string.you_dont_select_application))
        return null
      }
    } else {
      number = binding.urlField.text.toString().trim()
      if (TextUtils.isEmpty(number) || number.matches(".*https?://".toRegex())) {
        iFace.showSnackbar(getString(R.string.you_dont_insert_link))
        return null
      }
      if (!number.startsWith("http://") && !number.startsWith("https://"))
        number = "http://$number"
    }
    val startTime = binding.dateView.dateTime
    if (!validBefore(startTime, reminder)) {
      iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
      return null
    }
    Timber.d("EVENT_TIME ${TimeUtil.logTime(startTime)}")
    if (!TimeCount.isCurrent(startTime)) {
      iFace.showSnackbar(getString(R.string.reminder_is_outdated))
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

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderApplicationBinding.inflate(inflater, container, false)

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
    binding.tuneExtraView.hasAutoExtra = true
    binding.tuneExtraView.hint = getString(R.string.enable_launching_application_automatically)

    binding.pickApplication.setOnClickListener {
      activity?.startActivityForResult(Intent(activity, SelectApplicationActivity::class.java),
        Constants.REQUEST_CODE_APPLICATION)
    }
    binding.urlLayout.visibility = View.GONE
    binding.urlField.setText(iFace.reminderState.link)
    binding.urlField.onChanged {
      iFace.reminderState.link = it
      iFace.reminderState.isAppSaved = true
    }
    binding.application.setOnCheckedChangeListener { _, b ->
      iFace.reminderState.isLink = !b
      if (!b) {
        binding.applicationLayout.visibility = View.GONE
        binding.urlLayout.visibility = View.VISIBLE
      } else {
        binding.urlLayout.visibility = View.GONE
        binding.applicationLayout.visibility = View.VISIBLE
      }
    }
    editReminder()
  }

  private fun editReminder() {
    val reminder = iFace.reminderState.reminder
    if (reminder.target != "") {
      if (!iFace.reminderState.isLink && Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
        binding.application.isChecked = true
        iFace.reminderState.app = reminder.target
        iFace.reminderState.isLink = false
        binding.applicationName.text = appName
      } else {
        binding.browser.isChecked = true
        iFace.reminderState.link = reminder.target
        iFace.reminderState.isLink = true
        binding.urlField.setText(reminder.target)
      }
    }
    if (iFace.reminderState.isAppSaved) {
      if (!iFace.reminderState.isLink) {
        binding.application.isChecked = true
        binding.applicationName.text = appName
      } else {
        binding.browser.isChecked = true
        binding.urlField.setText(iFace.reminderState.link)
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == Constants.REQUEST_CODE_APPLICATION && resultCode == Activity.RESULT_OK) {
      iFace.reminderState.app = data?.getStringExtra(Constants.SELECTED_APPLICATION) ?: ""
      iFace.reminderState.isAppSaved = true
      binding.applicationName.text = appName
    }
  }
}
