package com.elementary.tasks.home.eventsview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.list.BirthdaysFragment
import com.elementary.tasks.databinding.FragmentHomeEventsBinding
import com.elementary.tasks.navigation.topfragment.BaseTopToolbarFragment
import com.elementary.tasks.reminder.lists.active.RemindersFragment
import com.elementary.tasks.reminder.lists.todo.TodoRemindersFragment
import com.github.naz013.logging.Logger
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeEventsFragment : BaseTopToolbarFragment<FragmentHomeEventsBinding>() {

  private val viewModel by viewModel<HomeEventsViewModel>()

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentHomeEventsBinding {
    return FragmentHomeEventsBinding.inflate(inflater, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Logger.d(TAG, "On view created")
    binding.tabLayout.addOnTabSelectedListener(
      object : OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
          tab?.also {
            viewModel.onTabSelected(getSelectedTab(it))
          }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
      }
    )

    viewModel.selectedTab.observe(viewLifecycleOwner) { onTabChanged(it) }
    lifecycle.addObserver(viewModel)
  }

  private fun onTabChanged(selectedTab: HomeEventsViewModel.SelectedTab?) {
    selectedTab ?: return
    Logger.d(TAG, "On tab changed: $selectedTab")
    binding.tabLayout.getTabAt(getTabPosition(selectedTab))?.select()
    when (selectedTab) {
      HomeEventsViewModel.SelectedTab.Reminders -> {
        addFragment(RemindersFragment())
      }
      HomeEventsViewModel.SelectedTab.Todo -> {
        addFragment(TodoRemindersFragment())
      }
      HomeEventsViewModel.SelectedTab.Birthdays -> {
        addFragment(BirthdaysFragment())
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    Logger.d(TAG, "On attach")
  }

  override fun onResume() {
    super.onResume()
    Logger.d(TAG, "On resume")
  }

  override fun onPause() {
    super.onPause()
    Logger.d(TAG, "On pause")
  }

  private fun addFragment(fragment: BaseSubEventsFragment<*>) {
    if (childFragmentManager.isStateSaved) {
      childFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commitAllowingStateLoss()
    } else {
      childFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit()
    }
  }

  override fun getTitle(): String {
    return getString(R.string.events)
  }

  private fun getSelectedTab(tab: TabLayout.Tab): HomeEventsViewModel.SelectedTab {
    return when (tab.position) {
      0 -> HomeEventsViewModel.SelectedTab.Reminders
      1 -> HomeEventsViewModel.SelectedTab.Todo
      2 -> HomeEventsViewModel.SelectedTab.Birthdays
      else -> HomeEventsViewModel.SelectedTab.Reminders
    }
  }

  private fun getTabPosition(selectedTab: HomeEventsViewModel.SelectedTab): Int {
    return when (selectedTab) {
      HomeEventsViewModel.SelectedTab.Reminders -> 0
      HomeEventsViewModel.SelectedTab.Todo -> 1
      HomeEventsViewModel.SelectedTab.Birthdays -> 2
    }
  }

  companion object {
    private const val TAG = "HomeEventsFragment"
  }
}
