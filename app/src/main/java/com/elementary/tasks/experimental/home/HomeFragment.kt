package com.elementary.tasks.experimental.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.birthdays.list.BirthdaysRecyclerAdapter
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.GlobalButtonObservable
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.databinding.HomeFragmentBinding
import com.elementary.tasks.navigation.fragments.BaseFragment
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.lists.adapter.RemindersRecyclerAdapter

class HomeFragment : BaseFragment<HomeFragmentBinding>() {

    private lateinit var viewModel: HomeViewModel
    private val remindersAdapter = RemindersRecyclerAdapter(showHeader = false, isEditable = true)
    private val birthdaysAdapter = BirthdaysRecyclerAdapter()

    private val reminderResolver = ReminderResolver(
            dialogAction = { return@ReminderResolver dialogues },
            saveAction = { reminder -> viewModel.saveReminder(reminder) },
            toggleAction = { reminder ->
                if (Reminder.isGpsType(reminder.type)) {
                    if (Permissions.ensureForeground(activity!!, 1122)) {
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
            deleteAction = { birthday -> viewModel.deleteBirthday(birthday) }
    )

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

        binding.remindersHeader.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToRemindersFragment())
        }
        binding.emptyRemindersState.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToRemindersFragment())
        }
        binding.remindersButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToRemindersFragment())
        }

        binding.archiveButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToArchiveFragment())
        }

        binding.birthdaysHeader.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToBirthdaysFragment())
        }
        binding.emptyBirthdaysState.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToBirthdaysFragment())
        }
        binding.birthdaysButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToBirthdaysFragment())
        }

        binding.groupsButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToGroupsFragment())
        }
        binding.mapButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToMapFragment())
        }
        binding.notesButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToActionNotes())
        }
        binding.calendarButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToActionCalendar())
        }
        binding.googleButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionActionHomeToActionGoogle())
        }

        initRemindersList()
        initBirthdaysList()
        initViewModel()
    }

    private fun initRemindersList() {
        remindersAdapter.prefsProvider = { prefs }
        remindersAdapter.actionsListener = object : ActionsListener<Reminder> {
            override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
                if (t != null) {
                    reminderResolver.resolveAction(view, t, actions)
                }
            }
        }
        binding.remindersList.layoutManager = LinearLayoutManager(context)
        binding.remindersList.adapter = remindersAdapter
    }

    private fun initBirthdaysList() {
        birthdaysAdapter.actionsListener = object : ActionsListener<Birthday> {
            override fun onAction(view: View, position: Int, t: Birthday?, actions: ListActions) {
                if (t != null) {
                    birthdayResolver.resolveAction(view, t, actions)
                }
            }
        }
        binding.birthdaysList.layoutManager = LinearLayoutManager(context)
        binding.birthdaysList.adapter = birthdaysAdapter
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        viewModel.reminders.observe(this, Observer {
            if (it != null) {
                remindersAdapter.submitList(it)
                updateRemindersEmpty(it.size)
            }
        })
        viewModel.birthdays.observe(this, Observer {
            if (it != null) {
                birthdaysAdapter.submitList(it)
                updateBirthdaysEmpty(it.size)
            }
        })
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
