package com.elementary.tasks.reminder.lists.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.deeplink.ReminderTodoTypeDeepLinkData
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.core.views.recyclerview.SpaceBetweenItemDecoration
import com.elementary.tasks.databinding.FragmentRemindersBinding
import com.elementary.tasks.home.eventsview.BaseSubEventsFragment
import com.elementary.tasks.reminder.lists.ReminderActionResolver
import com.elementary.tasks.reminder.lists.RemindersAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderEventsList
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.fragment.dp2px
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TodoRemindersFragment : BaseSubEventsFragment<FragmentRemindersBinding>() {

  private val systemServiceProvider by inject<SystemServiceProvider>()
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

  private val searchMenuHandler = SearchMenuHandler(
    systemServiceProvider.provideSearchManager(),
    R.string.search
  ) {
    viewModel.onSearchUpdate(it)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentRemindersBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerView.applyBottomInsets()
    addMenu(R.menu.fragment_reminders_todo, { true }) {
      searchMenuHandler.initSearchMenu(requireActivity(), it, R.id.action_search)
    }

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

    analyticsEventSender.send(ScreenUsedEvent(Screen.REMINDERS_LIST))

    initList()
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.events.nonNullObserve(viewLifecycleOwner) { showData(it) }
    viewModel.error.nonNullObserve(viewLifecycleOwner) {
      Logger.d("initViewModel: onError -> $it")
      toast(it)
    }
    viewModel.result.nonNullObserve(viewLifecycleOwner) {
      if (it == Commands.OUTDATED) {
        remindersAdapter.notifyItemChanged(mPosition)
        toast(R.string.reminder_is_outdated)
      }
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
