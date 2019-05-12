package com.elementary.tasks.experimental.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.GlobalButtonObservable
import com.elementary.tasks.core.utils.Module
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
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToCreateReminderActivity(""))
        }
        binding.addBirthdayButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToAddBirthdayActivity(""))
        }

        binding.remindersButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToRemindersFragment())
        }
        binding.remindersHeader.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToRemindersFragment())
        }
        binding.birthdaysButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToBirthdaysFragment())
        }
        binding.birthdaysHeader.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToBirthdaysFragment())
        }
        binding.groupsButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToGroupsFragment())
        }
        binding.mapButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToMapFragment())
        }

        initViewModel()
    }

    private fun initViewModel() {
        viewModel.reminders.observe(this, Observer {
            if (it != null) {

            }
        })
        viewModel.birthdays.observe(this, Observer {
            if (it != null) {

            }
        })
    }

    override fun getTitle(): String = getString(R.string.events)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_home, menu)
        menu.getItem(0)?.isVisible = Module.hasMicrophone(context!!)
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
