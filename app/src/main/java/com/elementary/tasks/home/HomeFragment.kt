package com.elementary.tasks.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.birthdays.list.BirthdayAdsViewHolder
import com.elementary.tasks.birthdays.list.BirthdayListItem
import com.elementary.tasks.birthdays.list.BirthdaysRecyclerAdapter
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.GlobalButtonObservable
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.PrefsConstants
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.databinding.HomeFragmentBinding
import com.elementary.tasks.navigation.fragments.BaseFragment
import com.elementary.tasks.other.PrivacyPolicyActivity
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.lists.adapter.ReminderAdsViewHolder
import com.elementary.tasks.reminder.lists.adapter.RemindersRecyclerAdapter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<HomeFragmentBinding>(), (String) -> Unit {

  private val buttonObservable by inject<GlobalButtonObservable>()
  private val featureManager by inject<FeatureManager>()
  private val viewModel by viewModel<HomeViewModel>()
  private val remindersAdapter = RemindersRecyclerAdapter(currentStateHolder, showHeader = false, isEditable = true) {
    showReminders(viewModel.reminders.value ?: listOf())
  }
  private val birthdaysAdapter = BirthdaysRecyclerAdapter(currentStateHolder) {
    showBirthdays(viewModel.birthdays.value ?: listOf())
  }
  private var mPosition: Int = 0

  private val reminderResolver = ReminderResolver(
    dialogAction = { return@ReminderResolver dialogues },
    saveAction = { reminder -> viewModel.saveReminder(reminder) },
    toggleAction = { reminder ->
      if (Reminder.isGpsType(reminder.type)) {
        if (Permissions.ensureForeground(requireActivity(), 1122)) {
          viewModel.toggleReminder(reminder)
        }
      } else {
        viewModel.toggleReminder(reminder)
      }
    },
    deleteAction = { reminder -> viewModel.moveToTrash(reminder) },
    skipAction = { reminder -> viewModel.skip(reminder) },
    allGroups = { return@ReminderResolver viewModel.groups }
  )
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { birthday -> viewModel.deleteBirthday(birthday.uuId) }
  )

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = HomeFragmentBinding.inflate(inflater, container, false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (Module.isMarshmallow) {
      binding.horizontalSelector.setOnScrollChangeListener { _, scrollX, _, _, _ ->
        viewModel.topScrollX = scrollX
      }
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
    binding.notesButton.setOnLongClickListener {
      buttonObservable.fireAction(it, GlobalButtonObservable.Action.QUICK_NOTE)
      true
    }
    binding.calendarButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToActionCalendar())
    }

    binding.googleButton.visibleGone(featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_TASKS))
    binding.googleButton.setOnClickListener {
      safeNavigation(HomeFragmentDirections.actionActionHomeToActionGoogle())
    }

    updatePrivacyBanner()
    updateLoginBanner()

    initRemindersList()
    initBirthdaysList()
    initViewModel()
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
  }

  private fun updatePrivacyBanner() {
    if (prefs.isPrivacyPolicyShowed) {
      binding.privacyBanner.hide()
    } else {
      binding.privacyBanner.show()
      binding.privacyButton.setOnClickListener { startActivity(Intent(context, PrivacyPolicyActivity::class.java)) }
      binding.acceptButton.setOnClickListener { prefs.isPrivacyPolicyShowed = true }
    }
  }

  private fun updateLoginBanner() {
    if (prefs.isPrivacyPolicyShowed) {
      if (prefs.isUserLogged) {
        binding.loginBanner.hide()
      } else {
        binding.loginBanner.show()
        binding.loginDismissButton.setOnClickListener { prefs.isUserLogged = true }
        binding.loginButton.setOnClickListener {
          prefs.isUserLogged = true
          safeNavigation(HomeFragmentDirections.actionActionHomeToCloudDrives())
        }
      }
    } else {
      binding.loginBanner.hide()
    }
  }

  private fun initRemindersList() {
    remindersAdapter.actionsListener = object : ActionsListener<Reminder> {
      override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
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
    birthdaysAdapter.actionsListener = object : ActionsListener<BirthdayListItem> {
      override fun onAction(view: View, position: Int, t: BirthdayListItem?, actions: ListActions) {
        if (t != null) {
          birthdayResolver.resolveAction(view, t, actions)
        }
      }
    }
    binding.birthdaysList.layoutManager = LinearLayoutManager(context)
    binding.birthdaysList.adapter = birthdaysAdapter

    if (prefs.isBirthdayReminderEnabled) {
      binding.birthdaysBlock.show()
    } else {
      binding.birthdaysBlock.hide()
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
  }

  private fun showBirthdays(list: List<BirthdayListItem>) {
    val newList = BirthdayAdsViewHolder.updateList(list)
    birthdaysAdapter.submitList(newList)
    updateBirthdaysEmpty(newList.size)
  }

  private fun showReminders(list: List<Reminder>) {
    val newList = ReminderAdsViewHolder.updateList(list)
    remindersAdapter.submitList(newList)
    updateRemindersEmpty(newList.size)
  }

  private fun updateBirthdaysEmpty(size: Int) {
    if (size == 0) {
      binding.emptyBirthdaysState.visibility = View.VISIBLE
      binding.birthdaysList.visibility = View.GONE
    } else {
      binding.birthdaysList.visibility = View.VISIBLE
      binding.emptyBirthdaysState.visibility = View.GONE
    }
  }

  private fun updateRemindersEmpty(size: Int) {
    if (size == 0) {
      binding.emptyRemindersState.visibility = View.VISIBLE
      binding.remindersList.visibility = View.GONE
    } else {
      binding.remindersList.visibility = View.VISIBLE
      binding.emptyRemindersState.visibility = View.GONE
    }
  }

  override fun getTitle(): String = getString(R.string.events)

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.fragment_home, menu)
    menu.getItem(0)?.isVisible = Module.hasMicrophone(requireContext())
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_voice -> {
        buttonObservable.fireAction(requireView(), GlobalButtonObservable.Action.VOICE)
        return true
      }
      R.id.action_settings -> {
        safeNavigation(HomeFragmentDirections.actionActionHomeToSettingsFragment())
        return true
      }
    }
    return super.onOptionsItemSelected(item)
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

  override fun onDestroy() {
    remindersAdapter.onDestroy()
    super.onDestroy()
  }
}
