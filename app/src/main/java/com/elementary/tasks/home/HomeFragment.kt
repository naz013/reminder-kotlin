package com.elementary.tasks.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.birthdays.preview.BirthdayPreviewActivity
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.deeplink.BirthdayDateDeepLinkData
import com.elementary.tasks.core.deeplink.GoogleTaskDateTimeDeepLinkData
import com.elementary.tasks.core.deeplink.ReminderDatetimeTypeDeepLinkData
import com.elementary.tasks.core.os.startActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.elementary.tasks.core.utils.params.PrefsObserver
import com.elementary.tasks.core.utils.ui.GlobalButtonObservable
import com.elementary.tasks.core.utils.ui.applyTopInsets
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.databinding.HomeFragmentBinding
import com.elementary.tasks.globalsearch.ActivityNavigation
import com.elementary.tasks.globalsearch.GlobalSearchViewModel
import com.elementary.tasks.globalsearch.NavigationAction
import com.elementary.tasks.globalsearch.adapter.SearchAdapter
import com.elementary.tasks.googletasks.TasksConstants
import com.elementary.tasks.googletasks.preview.GoogleTaskPreviewActivity
import com.elementary.tasks.googletasks.task.GoogleTaskActivity
import com.elementary.tasks.home.scheduleview.HeaderTimeType
import com.elementary.tasks.home.scheduleview.ScheduleAdapter
import com.elementary.tasks.home.scheduleview.ScheduleHomeViewModel
import com.elementary.tasks.home.scheduleview.ScheduleModel
import com.elementary.tasks.navigation.topfragment.BaseSearchableFragment
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.other.PrivacyPolicyActivity
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity
import com.elementary.tasks.whatsnew.WhatsNewManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class HomeFragment :
  BaseSearchableFragment<HomeFragmentBinding>(),
  PrefsObserver,
  WhatsNewManager.Listener {

  private val buttonObservable by inject<GlobalButtonObservable>()
  private val featureManager by inject<FeatureManager>()
  private val whatsNewManager by inject<WhatsNewManager>()
  private val searchViewModel by viewModel<GlobalSearchViewModel>()
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()

  private val viewModel by viewModel<ScheduleHomeViewModel>()
  private val scheduleAdapter = ScheduleAdapter(
    onReminderClickListener = { _, id ->
      PinLoginActivity.openLogged(requireContext(), ReminderPreviewActivity::class.java) {
        putExtra(Constants.INTENT_ID, id)
      }
    },
    onHeaderClickListener = { _, time ->
      showEventTypeSelectionDialog(time)
    },
    onNoteClickListener = { _, id ->
      PinLoginActivity.openLogged(requireContext(), NotePreviewActivity::class.java) {
        putExtra(Constants.INTENT_ID, id)
      }
    },
    onGoogleTaskClickListener = { _, id ->
      PinLoginActivity.openLogged(requireContext(), GoogleTaskPreviewActivity::class.java) {
        putExtra(Constants.INTENT_ID, id)
      }
    },
    onBirthdayClickListener = { _, id ->
      PinLoginActivity.openLogged(requireContext(), BirthdayPreviewActivity::class.java) {
        putExtra(Constants.INTENT_ID, id)
      }
    }
  )

  private val searchAdapter = SearchAdapter(
    onSuggestionClick = { result, text ->
      searchableFragmentCallback?.setQuery(text)
      searchViewModel.onSearchHistoryUpdate(result)
    },
    onObjectClick = { searchViewModel.onSearchResultClicked(it) }
  )

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = HomeFragmentBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.scheduleHeaderView.applyTopInsets()

    binding.searchBar.inflateMenu(R.menu.fragment_home)
    binding.searchBar.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.action_voice -> {
          buttonObservable.fireAction(requireView(), GlobalButtonObservable.Action.VOICE)
          true
        }

        R.id.action_settings -> {
          safeNavigation(HomeFragmentDirections.actionActionHomeToSettingsFragment())
          true
        }

        else -> false
      }
    }
    binding.searchBar.menu.also { menu ->
      menu.getItem(0)?.isVisible = Module.hasMicrophone(requireContext())
    }
    searchableFragmentCallback?.setSearchViewParams(
      binding.searchBar.id,
      getString(R.string.search_everywhere),
      searchAdapter,
      this
    )

    binding.globalAddButton.setOnClickListener { showEventTypeSelectionDialog(null) }

    updatePrivacyBanner()
    updateLoginBanner()
    initViewModel()
    setUpWhatsNewBanner()

    initRemindersList()

    whatsNewManager.addListener(this)
    lifecycle.addObserver(whatsNewManager)
  }

  private fun showEventTypeSelectionDialog(headerTimeType: HeaderTimeType?) {
    val items = if (viewModel.hasGoogleTasks()) {
      arrayOf(
        getString(R.string.add_reminder_menu),
        getString(R.string.add_birthday),
        getString(R.string.add_google_task)
      )
    } else {
      arrayOf(
        getString(R.string.add_reminder_menu),
        getString(R.string.add_birthday)
      )
    }
    dialogues.getMaterialDialog(requireContext())
      .setItems(
        items
      ) { dialogInterface, i ->
        dialogInterface.dismiss()
        when (i) {
          0 -> openReminderCreateScreen(viewModel.getDateTime(headerTimeType))
          1 -> openBirthdayCreateScreen()
          2 -> openGoogleTaskCreateScreen(viewModel.getTime(headerTimeType))
        }
      }
      .show()
  }

  private fun openReminderCreateScreen(
    dateTime: LocalDateTime
  ) {
    val deepLinkData = ReminderDatetimeTypeDeepLinkData(
      type = Reminder.BY_DATE,
      dateTime = dateTime
    )
    reminderBuilderLauncher.openDeepLink(requireContext(), deepLinkData) { }
  }

  private fun openGoogleTaskCreateScreen(time: LocalTime?) {
    val deepLinkData = GoogleTaskDateTimeDeepLinkData(
      date = LocalDate.now(),
      time = time
    )
    withActivity {
      PinLoginActivity.openLogged(it, GoogleTaskActivity::class.java, deepLinkData) {
        putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE)
      }
    }
  }

  private fun openBirthdayCreateScreen() {
    val deepLinkData = BirthdayDateDeepLinkData(LocalDate.now())
    withActivity {
      PinLoginActivity.openLogged(it, AddBirthdayActivity::class.java, deepLinkData) { }
    }
  }

  private fun initRemindersList() {
    binding.scheduleListView.layoutManager = LinearLayoutManager(context)
    binding.scheduleListView.adapter = scheduleAdapter
  }

  private fun setUpWhatsNewBanner() {
    binding.whatsNewOkButton.setOnClickListener {
      whatsNewManager.hideWhatsNew()
    }
    binding.whatsNewReadMoreButton.setOnClickListener {
      whatsNewManager.hideWhatsNew()
      analyticsEventSender.send(ScreenUsedEvent(Screen.WHATS_NEW))
      safeNavigation(HomeFragmentDirections.actionActionHomeToChangesFragment())
    }
  }

  override fun onQueryChanged(text: String) {
    searchViewModel.onQueryChanged(text)
  }

  override fun onResume() {
    super.onResume()
    prefs.addObserver(PrefsConstants.PRIVACY_SHOWED, this)
    prefs.addObserver(PrefsConstants.USER_LOGGED, this)
  }

  override fun onPause() {
    super.onPause()
    prefs.removeObserver(PrefsConstants.PRIVACY_SHOWED, this)
    prefs.removeObserver(PrefsConstants.USER_LOGGED, this)
  }

  override fun onDestroy() {
    super.onDestroy()
    whatsNewManager.removeListener(this)
  }

  private fun updatePrivacyBanner() {
    if (prefs.isPrivacyPolicyShowed) {
      binding.privacyBanner.gone()
    } else {
      binding.privacyBanner.visible()
      binding.privacyButton.setOnClickListener {
        startActivity(PrivacyPolicyActivity::class.java)
      }
      binding.acceptButton.setOnClickListener { prefs.isPrivacyPolicyShowed = true }
    }
  }

  private fun updateLoginBanner() {
    if (prefs.isPrivacyPolicyShowed) {
      if (prefs.isUserLogged ||
        !featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_DRIVE)
      ) {
        binding.loginBanner.gone()
      } else {
        binding.loginBanner.visible()
        binding.loginDismissButton.setOnClickListener { prefs.isUserLogged = true }
        binding.loginButton.setOnClickListener {
          prefs.isUserLogged = true
          safeNavigation(HomeFragmentDirections.actionActionHomeToCloudDrives())
        }
      }
    } else {
      binding.loginBanner.gone()
    }
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.scheduleData.nonNullObserve(viewLifecycleOwner) { updateList(it) }

    searchViewModel.searchResults.nonNullObserve(viewLifecycleOwner) {
      searchAdapter.submitList(it)
    }
    searchViewModel.navigateLiveData.nonNullObserve(viewLifecycleOwner) { onNavigationAction(it) }
  }

  private fun updateList(list: List<ScheduleModel>) {
    scheduleAdapter.submitList(list)
    binding.emptyScheduleView.visibleGone(list.isEmpty())
    binding.scheduleListView.visibleGone(list.isNotEmpty())
  }

  private fun onNavigationAction(navigationAction: NavigationAction) {
    when (navigationAction) {
      is ActivityNavigation -> {
        startActivity(navigationAction.clazz) {
          putExtra(Constants.INTENT_ID, navigationAction.objectId)
        }
      }
    }
  }

  override fun invoke(p1: String) {
    when (p1) {
      PrefsConstants.PRIVACY_SHOWED -> {
        updatePrivacyBanner()
        updateLoginBanner()
      }

      PrefsConstants.USER_LOGGED -> {
        updateLoginBanner()
      }
    }
  }

  override fun whatsNewVisible(isVisible: Boolean) {
    binding.whatsNewBanner.visibleGone(isVisible)
  }
}
