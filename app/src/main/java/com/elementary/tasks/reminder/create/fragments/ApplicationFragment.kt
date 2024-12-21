package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.os.datapicker.ApplicationPicker
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.onChanged
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.core.views.ClosableLegacyBuilderWarningView
import com.elementary.tasks.databinding.FragmentReminderApplicationBinding
import com.github.naz013.logging.Logger
import org.koin.android.ext.android.inject

class ApplicationFragment : RepeatableTypeFragment<FragmentReminderApplicationBinding>() {

  private val packageManagerWrapper by inject<PackageManagerWrapper>()
  private val applicationPicker = ApplicationPicker(this) {
    iFace.state.app = it
    iFace.state.isAppSaved = true
    binding.applicationName.text = appName
  }

  private val type: Int
    get() = if (isApplication()) {
      Reminder.BY_DATE_APP
    } else {
      Reminder.BY_DATE_LINK
    }
  private val appName: String
    get() = packageManagerWrapper.getApplicationName(iFace.state.app)

  override fun getExplanationVisibilityType(): ReminderExplanationVisibility.Type {
    return ReminderExplanationVisibility.Type.LINK
  }

  override fun getExplanationView(): View {
    return binding.explanationView
  }

  override fun setCloseListenerToExplanationView(listener: View.OnClickListener) {
    binding.explanationView.setOnClickListener(listener)
  }

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
      if (!number.startsWith("http://") && !number.startsWith("https://")) {
        number = "http://$number"
      }
    }
    val startTime = binding.dateView.selectedDateTime
    if (!validBefore(startTime, reminder)) {
      iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
      return null
    }
    Logger.d("EVENT_TIME ${dateTimeManager.logDateTime(startTime)}")
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
    reminder.recurDataObject = null
    return reminder
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderApplicationBinding.inflate(inflater, container, false)

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
      binding.repeatView
    )
  }

  override fun getLegacyMessageView(): ClosableLegacyBuilderWarningView {
    return binding.legacyBuilderWarningView
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

    binding.actionTypeOptionsGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
      iFace.state.isLink = isChecked && checkedId == R.id.browserCheck
      binding.applicationLayout.visibleGone(!iFace.state.isLink)
      binding.urlLayout.visibleGone(iFace.state.isLink)
    }

    if (Module.is12) {
      checkBrowser()
      binding.actionTypeOptionsGroup.gone()
    }
    editReminder()
  }

  private fun isApplication(): Boolean {
    return binding.actionTypeOptionsGroup.checkedButtonId == R.id.applicationCheck
  }

  private fun checkApp() {
    binding.actionTypeOptionsGroup.check(R.id.applicationCheck)
  }

  private fun checkBrowser() {
    binding.actionTypeOptionsGroup.check(R.id.browserCheck)
  }

  private fun editReminder() {
    val reminder = iFace.state.reminder
    if (reminder.target != "") {
      if (!iFace.state.isLink && Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
        checkApp()
        iFace.state.app = reminder.target
        iFace.state.isLink = false
        binding.applicationName.text = appName
      } else {
        checkBrowser()
        iFace.state.link = reminder.target
        iFace.state.isLink = true
        binding.urlField.setText(reminder.target)
      }
    }
    if (iFace.state.isAppSaved) {
      if (!iFace.state.isLink) {
        checkApp()
        binding.applicationName.text = appName
      } else {
        checkBrowser()
        binding.urlField.setText(iFace.state.link)
      }
    }
  }
}
