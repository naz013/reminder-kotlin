package com.elementary.tasks.home.eventsview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.list.BirthdaysFragment
import com.elementary.tasks.databinding.FragmentHomeEventsBinding
import com.elementary.tasks.navigation.topfragment.BaseTopFragment
import com.elementary.tasks.navigation.topfragment.FragmentMenuController
import com.elementary.tasks.reminder.lists.active.RemindersFragment
import com.elementary.tasks.reminder.lists.todo.TodoRemindersFragment
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.view.applyTopInsets
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeEventsFragment : BaseTopFragment<FragmentHomeEventsBinding>(), FragmentMenuController {

  private val viewModel by viewModel<HomeEventsViewModel>()
  private var menuModifier: ((Menu) -> Unit)? = null
  private lateinit var viewPagerAdapter: TabsPagerAdapter

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentHomeEventsBinding {
    return FragmentHomeEventsBinding.inflate(inflater, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Logger.i(TAG, "On view created")
    binding.appBar.applyTopInsets()

    viewPagerAdapter = TabsPagerAdapter(
      this,
      listOf(
        RemindersFragment(),
        TodoRemindersFragment(),
        BirthdaysFragment()
      )
    )

    binding.fragmentPager.apply {
      adapter = viewPagerAdapter
      isUserInputEnabled = false
    }

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

    initViewModel()
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    lifecycleScope.launch {
      viewModel.selectedTab.collect { onTabChanged(it) }
    }
  }

  private fun onTabChanged(selectedTab: HomeEventsViewModel.SelectedTab?) {
    selectedTab ?: return
    Logger.i(TAG, "On tab changed: $selectedTab")
    binding.tabLayout.getTabAt(getTabPosition(selectedTab))?.select()
    binding.fragmentPager.setCurrentItem(getTabPosition(selectedTab), false)
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

  override fun addMenu(
    menuRes: Int?,
    onMenuItemListener: (MenuItem) -> Boolean,
    menuModifier: ((Menu) -> Unit)?
  ) {
    this.menuModifier = menuModifier
    binding.toolbar.menu.clear()
    if (menuRes != null) {
      binding.toolbar.inflateMenu(menuRes)
    }
    menuModifier?.invoke(binding.toolbar.menu)
    binding.toolbar.setOnMenuItemClickListener {
      return@setOnMenuItemClickListener onMenuItemListener(it)
    }
  }

  override fun removeMenu() {
    binding.toolbar.menu.clear()
    menuModifier = null
  }

  override fun updateMenuItem(
    itemId: Int,
    modifier: MenuItem.() -> Unit
  ) {
    val menuItem = binding.toolbar.menu.findItem(itemId) ?: return
    modifier(menuItem)
  }

  private class TabsPagerAdapter(
    fragment: Fragment,
    private val tabFragments: List<Fragment>
  ) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = tabFragments.size

    override fun createFragment(position: Int): Fragment {
      return tabFragments[position]
    }
  }

  companion object {
    private const val TAG = "HomeEventsFragment"
  }
}
