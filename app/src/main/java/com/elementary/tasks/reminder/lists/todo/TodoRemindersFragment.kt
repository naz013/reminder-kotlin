package com.elementary.tasks.reminder.lists.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.deeplink.ReminderTodoTypeDeepLinkData
import com.elementary.tasks.core.views.recyclerview.SpaceBetweenItemDecoration
import com.elementary.tasks.databinding.FragmentRemindersBinding
import com.elementary.tasks.home.eventsview.BaseSubEventsFragment
import com.elementary.tasks.reminder.lists.ReminderActionResolver
import com.elementary.tasks.reminder.lists.RemindersAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderEventsList
import com.elementary.tasks.reminder.lists.filter.FilterGroup
import com.elementary.tasks.reminder.lists.filter.ReminderFilterDialog
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.fragment.dp2px
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.visibleGone
import org.koin.androidx.viewmodel.ext.android.viewModel

class TodoRemindersFragment : BaseSubEventsFragment<FragmentRemindersBinding>() {

  private val viewModel by viewModel<ActiveTodoRemindersViewModel>()

  private var mPosition: Int = 0

  private val reminderResolver by lazy {
    ReminderActionResolver(
      context = requireContext(),
      dialogues = dialogues,
      permissionFlow = permissionFlow,
      toggleAction = { viewModel.toggleReminder(it) },
      deleteAction = { viewModel.moveToTrash(it) },
      skipAction = { viewModel.skip(it) },
      openAction = {
        navigate {
          navigate(
            R.id.previewReminderFragment,
            Bundle().apply {
              putString(IntentKeys.INTENT_ID, it)
            }
          )
        }
      },
      editAction = {
        navigate {
          navigate(
            R.id.buildReminderFragment,
            Bundle().apply {
              putString(IntentKeys.INTENT_ID, it)
            }
          )
        }
      }
    )
  }

  private val remindersAdapter = RemindersAdapter(
    isEditable = true,
    onItemClicked = { reminderResolver.resolveItemClick(it.id, it.state.isRemoved) },
    onToggleClicked = { reminderResolver.resolveItemToggle(it.id, it.state.isGps) },
    onMoreClicked = { view, reminder ->
      reminderResolver.resolveItemMore(
        view = view,
        id = reminder.id,
        isRemoved = reminder.state.isRemoved,
        actions = reminder.actions
      )
    }
  )

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentRemindersBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerView.applyBottomInsets()

    binding.fab.setOnClickListener {
      val deepLinkData = ReminderTodoTypeDeepLinkData
      navigate {
        navigate(
          R.id.buildReminderFragment,
          Bundle().apply {
            putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
            putParcelable(deepLinkData.intentKey, deepLinkData)
          }
        )
      }
    }

    binding.reminderSearchBar.doAfterTextChanged {
      viewModel.onSearchUpdate(it?.toString().orEmpty())
    }

    analyticsEventSender.send(ScreenUsedEvent(Screen.REMINDERS_LIST))

    // Set up result listener for filter selection
    setFragmentResultListener(ReminderFilterDialog.REQUEST_KEY) { _, result ->
      viewModel.handleFilterResult(ReminderFilterDialog.getAppliedFiltersFromResult(result))
    }

    initList()
    initViewModel()
  }

  override fun onResume() {
    super.onResume()
    addMenu(R.menu.fragment_reminders_todo, { menuItem ->
      when (menuItem.itemId) {
        R.id.action_filter -> {
          viewModel.showFilters()
        }
      }
      true
    })
  }

  private fun showFilters(filters: List<FilterGroup>) {
    val dialog = ReminderFilterDialog.newInstance(
      filterGroups = filters,
      title = getString(R.string.filter_reminders)
    )
    dialog.show(parentFragmentManager, "ReminderFilterDialog")
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.events.nonNullObserve(viewLifecycleOwner) { showData(it) }
    viewModel.errorEvent.observeEvent(viewLifecycleOwner) {
      Logger.d("initViewModel: onError -> $it")
      toast(it)
    }
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) {
      if (it == Commands.OUTDATED) {
        remindersAdapter.notifyItemChanged(mPosition)
        toast(R.string.reminder_is_outdated)
      }
    }
    viewModel.showFilters.observeEvent(viewLifecycleOwner) { showFilters(it) }
    viewModel.canFilter.observe(viewLifecycleOwner) { canFilter ->
      updateFilterMenuIcon(canFilter == true)
    }
    viewModel.canSearch.observe(viewLifecycleOwner) { canSearch ->
      binding.reminderSearchBar.visibleGone(canSearch == true)
    }
    viewModel.isInProgress.observe(viewLifecycleOwner) {
      binding.linearProgressIndicator.visibility = if (it == true) View.VISIBLE else View.INVISIBLE
    }
  }

  private fun updateFilterMenuIcon(canFilter: Boolean) {
    fragmentMenuController?.updateMenuItem(R.id.action_filter) {
      isVisible = canFilter
    }
  }

  private fun showData(result: List<UiReminderEventsList>) {
    remindersAdapter.submitList(result)
    binding.recyclerView.smoothScrollToPosition(0)
    reloadEmptyView(result.size)
  }

  private fun initList() {
    binding.recyclerView.layoutManager = LinearLayoutManager(context)
    binding.recyclerView.adapter = remindersAdapter
    binding.recyclerView.addItemDecoration(SpaceBetweenItemDecoration(dp2px(8)))
    ViewUtils.listenScrollableView(binding.recyclerView) {
      if (it) {
        binding.fab.show()
      } else {
        binding.fab.hide()
      }
    }
    reloadEmptyView(0)
  }

  private fun reloadEmptyView(count: Int) {
    binding.emptyItem.visibleGone(count == 0)
  }
}
