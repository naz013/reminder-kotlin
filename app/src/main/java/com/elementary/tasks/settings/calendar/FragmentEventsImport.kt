package com.elementary.tasks.settings.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.toast
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.databinding.FragmentSettingsEventsImportBinding
import com.elementary.tasks.settings.BaseCalendarFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentEventsImport :
  BaseCalendarFragment<FragmentSettingsEventsImportBinding>(),
  CompoundButton.OnCheckedChangeListener {

  private val viewModel by viewModel<EventsImportViewModel>()

  private val jobScheduler by inject<JobScheduler>()

  private val calendarsAdapter = CalendarsAdapter()
  private var mItemSelect: Int = 0

  private val intervalPosition: Int
    get() {
      val position: Int
      val interval = prefs.autoCheckInterval
      position = when (interval) {
        1 -> 0
        6 -> 1
        12 -> 2
        24 -> 3
        48 -> 4
        else -> 0
      }
      mItemSelect = position
      return position
    }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsEventsImportBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.button.setOnClickListener { importEvents() }

    binding.progressMessageView.text = getString(R.string.please_wait)
    binding.progressView.visibility = View.GONE

    binding.syncInterval.setOnClickListener { showIntervalDialog() }

    binding.autoCheck.setOnCheckedChangeListener(this)
    binding.autoCheck.isChecked = prefs.isAutoEventsCheckEnabled
    binding.syncInterval.isEnabled = false

    binding.eventCalendars.layoutManager =
      LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    binding.eventCalendars.adapter = calendarsAdapter

    viewModel.calendars.nonNullObserve(viewLifecycleOwner) {
      calendarsAdapter.data = it
    }
    viewModel.selectedCalendars.nonNullObserve(viewLifecycleOwner) {
      calendarsAdapter.selectIds(it)
    }
    viewModel.isInProgress.nonNullObserve(viewLifecycleOwner) { updateProgress(it) }
    viewModel.action.nonNullObserve(viewLifecycleOwner) { onAction(it) }
  }

  private fun onAction(importAction: EventsImportViewModel.ImportAction) {
    when (importAction) {
      is EventsImportViewModel.NoEventsAction -> {
        toast(getString(R.string.no_events_found))
      }
      is EventsImportViewModel.EventsImportedAction -> {
        toast("${importAction.count} " + getString(R.string.events_found))
        PermanentReminderReceiver.show(requireContext())
      }
    }
  }

  private fun updateProgress(isLoading: Boolean) {
    binding.button.isEnabled = !isLoading
    binding.progressView.visibleGone(isLoading)
  }

  private fun showIntervalDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(getString(R.string.interval))
      val items = arrayOf(
        getString(R.string.one_hour),
        getString(R.string.six_hours),
        getString(R.string.twelve_hours),
        getString(R.string.one_day),
        getString(R.string.two_days)
      )
      builder.setSingleChoiceItems(items, intervalPosition) { _, item -> mItemSelect = item }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        saveIntervalPrefs()
        dialog.dismiss()
      }
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun saveIntervalPrefs() {
    when (mItemSelect) {
      0 -> prefs.autoCheckInterval = 1
      1 -> prefs.autoCheckInterval = 6
      2 -> prefs.autoCheckInterval = 12
      3 -> prefs.autoCheckInterval = 24
      4 -> prefs.autoCheckInterval = 48
    }
    permissionFlow.askPermissions(listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
      startCheckService()
    }
  }

  private fun startCheckService() {
    jobScheduler.scheduleEventCheck()
  }

  override fun onBackStackResume() {
    super.onBackStackResume()
    permissionFlow.askPermission(Permissions.READ_CALENDAR) {
      viewModel.loadCalendars()
    }
  }

  override fun getTitle(): String = getString(R.string.import_events)

  private fun importEvents() {
    permissionFlow.askPermissions(listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
      if (calendarsAdapter.data.isEmpty()) {
        toast(R.string.no_calendars_found)
        return@askPermissions
      }
      val selectedIds = calendarsAdapter.getSelectedIds()
      if (selectedIds.isEmpty()) {
        toast(R.string.you_dont_select_any_calendar)
        return@askPermissions
      }
      viewModel.importEvents(selectedIds)
    }
  }

  override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
    when (buttonView.id) {
      R.id.autoCheck -> if (isChecked) {
        permissionFlow.askPermissions(
          listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)
        ) { autoCheck(true) }
      } else {
        autoCheck(false)
      }
    }
  }

  private fun autoCheck(isChecked: Boolean) {
    prefs.isAutoEventsCheckEnabled = isChecked
    binding.syncInterval.isEnabled = isChecked
    if (isChecked) {
      jobScheduler.scheduleEventCheck()
    } else {
      jobScheduler.cancelEventCheck()
    }
  }
}
