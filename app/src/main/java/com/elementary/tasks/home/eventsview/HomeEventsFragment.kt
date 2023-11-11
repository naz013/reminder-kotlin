package com.elementary.tasks.home.eventsview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.list.BirthdaysFragment
import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.databinding.FragmentHomeEventsBinding
import com.elementary.tasks.navigation.topfragment.BaseTopToolbarFragment
import com.elementary.tasks.reminder.lists.active.RemindersFragment
import com.elementary.tasks.reminder.lists.todo.TodoRemindersFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class HomeEventsFragment : BaseTopToolbarFragment<FragmentHomeEventsBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentHomeEventsBinding {
    return FragmentHomeEventsBinding.inflate(inflater, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.tabLayout.addOnTabSelectedListener(
      object : OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
          Traces.d(TAG, "onTabSelected: $tab")
          when (tab?.position) {
            0 -> {
              addFragment(RemindersFragment())
            }
            1 -> {
              addFragment(TodoRemindersFragment())
            }
            2 -> {
              addFragment(BirthdaysFragment())
            }
          }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
          Traces.d(TAG, "onTabReselected: $tab")
        }
      }
    )

    addFragment(RemindersFragment())
  }

  private fun addFragment(fragment: BaseSubEventsFragment<*>) {
    childFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, fragment)
      .commit()
  }

  override fun getTitle(): String {
    return getString(R.string.events)
  }

  companion object {
    private const val TAG = "HomeEventsFragment"
  }
}
