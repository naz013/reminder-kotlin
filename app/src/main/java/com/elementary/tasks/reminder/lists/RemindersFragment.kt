package com.elementary.tasks.reminder.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Screen
import com.elementary.tasks.core.analytics.ScreenUsedEvent
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListActiveGps
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.ui.GlobalButtonObservable
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.reminders.ActiveRemindersViewModel
import com.elementary.tasks.databinding.FragmentRemindersBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ReminderAdsViewHolder
import com.elementary.tasks.reminder.lists.adapter.UiReminderListRecyclerAdapter
import com.elementary.tasks.reminder.lists.filters.SearchModifier
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class RemindersFragment : BaseNavigationFragment<FragmentRemindersBinding>(),
    (List<UiReminderList>) -> Unit {

  private val buttonObservable by inject<GlobalButtonObservable>()
  private val systemServiceProvider by inject<SystemServiceProvider>()
  private val viewModel by viewModel<ActiveRemindersViewModel>()

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

  private val remindersAdapter = UiReminderListRecyclerAdapter(isDark, isEditable = true) {
    showData(viewModel.events.value ?: listOf())
  }
  private val searchModifier = SearchModifier(null, this)
  private val searchMenuHandler = SearchMenuHandler(systemServiceProvider.provideSearchManager()) {
    searchModifier.setSearchValue(it)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.fragment_active_menu, menu)
    searchMenuHandler.initSearchMenu(requireActivity(), menu, R.id.action_search)
    menu.getItem(0)?.isVisible = searchModifier.hasOriginal()
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentRemindersBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.fab.setOnClickListener {
      PinLoginActivity.openLogged(
        requireContext(),
        CreateReminderActivity::class.java
      )
    }
    binding.fab.setOnLongClickListener {
      buttonObservable.fireAction(it, GlobalButtonObservable.Action.QUICK_NOTE)
      true
    }

    binding.archiveButton.setOnClickListener {
      safeNavigation(RemindersFragmentDirections.actionRemindersFragmentToArchiveFragment())
    }
    binding.groupsButton.setOnClickListener {
      safeNavigation(RemindersFragmentDirections.actionRemindersFragmentToGroupsFragment())
    }

    analyticsEventSender.send(ScreenUsedEvent(Screen.REMINDERS_LIST))

    initList()
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.events.nonNullObserve(viewLifecycleOwner) { showData(it) }
    viewModel.error.nonNullObserve(viewLifecycleOwner) {
      Timber.d("initViewModel: onError -> $it")
      remindersAdapter.notifyDataSetChanged()
      Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
    }
    viewModel.result.nonNullObserve(viewLifecycleOwner) {
      if (it == Commands.OUTDATED) {
        remindersAdapter.notifyItemChanged(mPosition)
        toast(R.string.reminder_is_outdated)
      }
    }
  }

  private fun showData(result: List<UiReminderList>) {
    searchModifier.original = result.toMutableList()
    activity?.invalidateOptionsMenu()
  }

  private fun initList() {
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
    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    binding.recyclerView.adapter = remindersAdapter
    ViewUtils.listenScrollableView(
      binding.recyclerView,
      listener = { setToolbarAlpha(toAlpha(it.toFloat())) }) {
      if (it) binding.fab.show()
      else binding.fab.hide()
    }
    reloadView(0)
  }

  override fun getTitle(): String = getString(R.string.tasks)

  private fun reloadView(count: Int) {
    if (count > 0) {
      binding.emptyItem.visibility = View.GONE
    } else {
      binding.emptyItem.visibility = View.VISIBLE
    }
  }

  override fun invoke(result: List<UiReminderList>) {
    val newList = ReminderAdsViewHolder.addAdsIfNeeded(result)
    remindersAdapter.submitList(newList)
    binding.recyclerView.smoothScrollToPosition(0)
    reloadView(newList.size)
  }

  override fun onDestroy() {
    remindersAdapter.onDestroy()
    super.onDestroy()
  }
}