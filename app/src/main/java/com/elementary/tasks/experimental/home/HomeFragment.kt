package com.elementary.tasks.experimental.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.GlobalButtonObservable
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.HomeFragmentBinding
import com.elementary.tasks.navigation.fragments.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<HomeFragmentBinding>() {

    private val viewModel: HomeViewModel by viewModel()

    override fun layoutRes(): Int = R.layout.home_fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addReminderButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToCreateReminderActivity())
        }
        binding.allRemindersButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToRemindersFragment())
        }
        binding.allBirthdaysButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToBirthdaysFragment())
        }
        binding.groupsButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToGroupsFragment())
        }
        binding.mapButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToMapFragment())
        }
    }

    override fun getTitle(): String = getString(R.string.events)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_home, menu)

        if (Module.hasMicrophone(context!!)) {
            menu.getItem(0)?.isVisible = true
            ViewUtils.tintMenuIcon(context!!, menu, 0, R.drawable.ic_twotone_mic_24px, isDark)
        } else {
            menu.getItem(0)?.isVisible = false
        }
        ViewUtils.tintMenuIcon(context!!, menu, 1, R.drawable.ic_twotone_settings_24px, isDark)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_voice -> {
                buttonObservable.fireAction(view!!, GlobalButtonObservable.Action.VOICE)
                return true
            }
            R.id.action_settings -> {
                findNavController().navigate(HomeFragmentDirections.actionActionHomeToSettingsFragment())
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
