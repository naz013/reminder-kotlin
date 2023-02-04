package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.os.datapicker.ApplicationPicker
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.onChanged
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.FragmentReminderApplicationBinding
import org.koin.android.ext.android.inject
import timber.log.Timber

class ApplicationFragment : RepeatableTypeFragment<FragmentReminderApplicationBinding>() {

  private val packageManagerWrapper by inject<PackageManagerWrapper>()
  private val applicationPicker = ApplicationPicker(this) {
    iFace.state.app = it
    iFace.state.isAppSaved = true
    binding.applicationName.text = appName
  }

  private val type: Int
    get() = if (binding.application.isChecked) {
      Reminder.BY_DATE_APP
    } else {
      Reminder.BY_DATE_LINK
    }
  private val appName: String
    get() = packageManagerWrapper.getApplicationName(iFace.state.app)

  override fun prepare(): Reminder? {
    val type = type
    var number: String
    val reminder = iFace.state.reminder
    if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
      number = iFace.state.app
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
    val startTime = binding.dateView.selectedDateTime
    if (!validBefore(startTime, reminder)) {
      iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
      return null
    }
    Timber.d("EVENT_TIME ${dateTimeManager.logDateTime(startTime)}")
    if (!dateTimeManager.isCurrent(startTime)) {
      iFace.showSnackbar(getString(R.string.reminder_is_outdated))
      return null
    }
    val gmtTime = dateTimeManager.getGmtFromDateTime(startTime)
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

    binding.pickApplication.setOnClickListener { applicationPicker.pickApplication() }
    binding.urlLayout.gone()
    binding.urlField.setText(iFace.state.link)
    binding.urlField.onChanged {
      iFace.state.link = it
      iFace.state.isAppSaved = true
    }
    binding.browser.setOnCheckedChangeListener { _, b ->
      iFace.state.isLink = b
      if (b) {
        binding.applicationLayout.gone()
        binding.urlLayout.visible()
      } else {
        binding.urlLayout.gone()
        binding.applicationLayout.visible()
      }
    }
    if (Module.is12) {
      binding.browser.isChecked = true
      binding.switchGroup.gone()
    }
    editReminder()
  }

  private fun editReminder() {
    val reminder = iFace.state.reminder
    if (reminder.target != "") {
      if (!iFace.state.isLink && Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
        binding.application.isChecked = true
        iFace.state.app = reminder.target
        iFace.state.isLink = false
        binding.applicationName.text = appName
      } else {
        binding.browser.isChecked = true
        iFace.state.link = reminder.target
        iFace.state.isLink = true
        binding.urlField.setText(reminder.target)
      }
    }
    if (iFace.state.isAppSaved) {
      if (!iFace.state.isLink) {
        binding.application.isChecked = true
        binding.applicationName.text = appName
      } else {
        binding.browser.isChecked = true
        binding.urlField.setText(iFace.state.link)
      }
    }
  }
}
