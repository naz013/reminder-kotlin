package com.elementary.tasks.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.birthdays.list.BirthdaysRecyclerAdapter
import com.elementary.tasks.core.analytics.Screen
import com.elementary.tasks.core.analytics.ScreenUsedEvent
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListActiveGps
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.elementary.tasks.core.utils.params.PrefsObserver
import com.elementary.tasks.core.utils.startActivity
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.ui.GlobalButtonObservable
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.HomeFragmentBinding
import com.elementary.tasks.globalsearch.ActivityNavigation
import com.elementary.tasks.globalsearch.GlobalSearchViewModel
import com.elementary.tasks.globalsearch.NavigationAction
import com.elementary.tasks.globalsearch.adapter.SearchAdapter
import com.elementary.tasks.navigation.fragments.BaseFragment
import com.elementary.tasks.other.PrivacyPolicyActivity
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.lists.adapter.UiReminderListRecyclerAdapter
import com.elementary.tasks.whatsnew.WhatsNewManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment :
  BaseFragment<HomeFragmentBinding>(),
  PrefsObserver,
  WhatsNewManager.Listener {

  private val buttonObservable by inject<GlobalButtonObservable>()
  private val featureManager by inject<FeatureManager>()
  private val whatsNewManager by inject<WhatsNewManager>()
  private val viewModel by viewModel<HomeViewModel>()
  private val searchViewModel by viewModel<GlobalSearchViewModel>()
  private val remindersAdapter = UiReminderListRecyclerAdapter(isDark, isEditable = true)
  private val birthdaysAdapter = BirthdaysRecyclerAdapter()
  private var mPosition: Int = 0

  private val reminderResolver = ReminderResolver(
    dialogAction = { return@ReminderResolver dialogues },
    toggleAction = { reminder ->
      when (reminder) {
        is UiReminderListActiveGps -> {
          permissionFlow.askPermission(Permissions.FOREGROUND_SERVICE) {
            viewModel.toggleReminder(reminder)
          }
        }

        else -> {
          viewModel.toggleReminder(reminder)
        }
      }
    },
    deleteAction = { reminder -> viewModel.moveToTrash(reminder) },
    skipAction = { reminder -> viewModel.skip(reminder) }
  )
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { birthday -> viewModel.deleteBirthday(birthday.uuId) }
  )

  private val searchAdapter = SearchAdapter(
    onSuggestionClick = { result, text ->
      binding.searchView.editText.setText(text)
      binding.searchView.editText.setSelection(text.length)
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
    binding.searchResultsList.adapter = searchAdapter
    binding.searchView.editText.doOnTextChanged { text, _, _, _ ->
      searchViewModel.onQueryChanged(text?.toString() ?: "")
    }

    binding.horizontalSelector.setOnScrollChangeListener { _, scrollX, _, _, _ ->
      viewModel.topScrollX = scrollX
    }

    binding.addReminderButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToCreateReminderActivity("", true))
    }
    binding.addBirthdayButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToAddBirthdayActivity("", true))
    }

    binding.remindersHeader.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToRemindersFragment())
    }
    binding.emptyRemindersState.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToRemindersFragment())
    }
    binding.remindersButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToRemindersFragment())
    }

    binding.archiveButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToArchiveFragment())
    }

    binding.birthdaysHeader.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToBirthdaysFragment())
    }
    binding.emptyBirthdaysState.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToBirthdaysFragment())
    }
    binding.birthdaysButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToBirthdaysFragment())
    }

    binding.groupsButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToGroupsFragment())
    }
    binding.mapButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToMapFragment())
    }
    binding.notesButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToActionNotes())
    }
    binding.calendarButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToActionCalendar())
    }

    binding.googleButton.visibleGone(
      featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_TASKS)
    )
    binding.googleButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToActionGoogle())
    }

    updatePrivacyBanner()
    updateLoginBanner()

    initRemindersList()
    initBirthdaysList()
    initViewModel()

    setUpWhatsNewBanner()

    whatsNewManager.addListener(this)
    lifecycle.addObserver(whatsNewManager)
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

  override fun onResume() {
    super.onResume()
    prefs.addObserver(PrefsConstants.PRIVACY_SHOWED, this)
    prefs.addObserver(PrefsConstants.USER_LOGGED, this)
    binding.horizontalSelector.scrollTo(viewModel.topScrollX, 0)
  }

  override fun onPause() {
    super.onPause()
    prefs.removeObserver(PrefsConstants.PRIVACY_SHOWED, this)
    prefs.removeObserver(PrefsConstants.USER_LOGGED, this)
    if (binding.searchView.isShowing) {
      binding.searchView.clearText()
      binding.searchView.hide()
    }
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

  private fun initRemindersList() {
    remindersAdapter.actionsListener = object : ActionsListener<UiReminderListData> {
      override fun onAction(
        view: View,
        position: Int,
        t: UiReminderListData?,
        actions: ListActions
      ) {
        if (t != null) {
          mPosition = position
          reminderResolver.resolveAction(view, t, actions)
        }
      }
    }
    binding.remindersList.layoutManager = LinearLayoutManager(context)
    binding.remindersList.adapter = remindersAdapter
  }

  private fun initBirthdaysList() {
    birthdaysAdapter.actionsListener = object : ActionsListener<UiBirthdayList> {
      override fun onAction(view: View, position: Int, t: UiBirthdayList?, actions: ListActions) {
        if (t != null) {
          birthdayResolver.resolveAction(view, t, actions)
        }
      }
    }
    binding.birthdaysList.layoutManager = LinearLayoutManager(context)
    binding.birthdaysList.adapter = birthdaysAdapter

    if (prefs.isBirthdayReminderEnabled) {
      binding.birthdaysBlock.visible()
    } else {
      binding.birthdaysBlock.gone()
    }
  }

  private fun initViewModel() {
    viewModel.reminders.observe(viewLifecycleOwner) {
      if (it != null) {
        showReminders(it)
      } else {
        showReminders(listOf())
      }
    }
    viewModel.birthdays.observe(viewLifecycleOwner) {
      if (it != null) {
        showBirthdays(it)
      } else {
        showBirthdays(listOf())
      }
    }
    viewModel.result.observe(viewLifecycleOwner) {
      if (it != null) {
        if (it == Commands.OUTDATED) {
          remindersAdapter.notifyItemChanged(mPosition)
          toast(R.string.reminder_is_outdated)
        }
      }
    }

    searchViewModel.searchResults.nonNullObserve(viewLifecycleOwner) {
      searchAdapter.submitList(it)
    }
    searchViewModel.navigateLiveData.nonNullObserve(viewLifecycleOwner) { onNavigationAction(it) }
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

  private fun showBirthdays(list: List<UiBirthdayList>) {
    birthdaysAdapter.submitList(list)
    updateBirthdaysEmpty(list.size)
  }

  private fun showReminders(list: List<UiReminderList>) {
    remindersAdapter.submitList(list)
    updateRemindersEmpty(list.size)
  }

  private fun updateBirthdaysEmpty(size: Int) {
    binding.emptyBirthdaysState.visibleGone(size == 0)
    binding.birthdaysList.visibleGone(size != 0)
  }

  private fun updateRemindersEmpty(size: Int) {
    binding.emptyRemindersState.visibleGone(size == 0)
    binding.remindersList.visibleGone(size != 0)
  }

  override fun getTitle(): String = getString(R.string.events)

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

  override fun hasToolbar(): Boolean {
    return false
  }
}
