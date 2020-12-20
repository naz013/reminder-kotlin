package com.elementary.tasks.reminder.lists

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.GlobalButtonObservable
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.reminders.ActiveRemindersViewModel
import com.elementary.tasks.databinding.FragmentRemindersBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ReminderAdsViewHolder
import com.elementary.tasks.reminder.lists.adapter.RemindersRecyclerAdapter
import com.elementary.tasks.reminder.lists.filters.SearchModifier
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class RemindersFragment : BaseNavigationFragment<FragmentRemindersBinding>(), (List<Reminder>) -> Unit {

  private val buttonObservable by inject<GlobalButtonObservable>()
  private val viewModel by viewModel<ActiveRemindersViewModel>()
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

  private val remindersAdapter = RemindersRecyclerAdapter(currentStateHolder, showHeader = true, isEditable = true) {
    showData(viewModel.events.value ?: listOf())
  }
  private val searchModifier = SearchModifier(null, this)

  private var mSearchView: SearchView? = null
  private var mSearchMenu: MenuItem? = null

  private val queryTextListener = object : SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String): Boolean {
      searchModifier.setSearchValue(query)
      if (mSearchMenu != null) {
        mSearchMenu?.collapseActionView()
      }
      return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
      searchModifier.setSearchValue(newText)
      return false
    }
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.fragment_active_menu, menu)
    mSearchMenu = menu.findItem(R.id.action_search)
    val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
    if (mSearchMenu != null) {
      mSearchView = mSearchMenu?.actionView as SearchView?
    }
    if (mSearchView != null) {
      if (searchManager != null) {
        mSearchView?.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
      }
      mSearchView?.setOnQueryTextListener(queryTextListener)
    }
    val isNotEmpty = searchModifier.hasOriginal()
    menu.getItem(0)?.isVisible = isNotEmpty
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentRemindersBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.fab.setOnClickListener { CreateReminderActivity.openLogged(requireContext()) }
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

    initList()
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.events.observe(viewLifecycleOwner, { reminders ->
      if (reminders != null) {
        showData(reminders)
      }
    })
    viewModel.error.observe(viewLifecycleOwner, {
      Timber.d("initViewModel: onError -> $it")
      if (it != null) {
        remindersAdapter.notifyDataSetChanged()
        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
      }
    })
    viewModel.result.observe(viewLifecycleOwner, {
      if (it != null) {
        if (it == Commands.OUTDATED) {
          remindersAdapter.notifyItemChanged(mPosition)
          toast(R.string.reminder_is_outdated)
        }
      }
    })
  }

  private fun showData(result: List<Reminder>) {
    searchModifier.original = result.toMutableList()
    activity?.invalidateOptionsMenu()
  }

  private fun initList() {
    remindersAdapter.actionsListener = object : ActionsListener<Reminder> {
      override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
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
    ViewUtils.listenScrollableView(binding.recyclerView, listener = { setToolbarAlpha(toAlpha(it.toFloat())) }) {
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

  override fun invoke(result: List<Reminder>) {
    val newList = ReminderAdsViewHolder.updateList(result)
    remindersAdapter.submitList(newList)
    binding.recyclerView.smoothScrollToPosition(0)
    reloadView(newList.size)
  }

  override fun onDestroy() {
    remindersAdapter.onDestroy()
    super.onDestroy()
  }
}