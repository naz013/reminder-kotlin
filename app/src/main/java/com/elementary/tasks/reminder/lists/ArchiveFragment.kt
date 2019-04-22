package com.elementary.tasks.reminder.lists

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.reminders.ArchiveRemindersViewModel
import com.elementary.tasks.databinding.FragmentTrashBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.lists.adapter.RemindersRecyclerAdapter
import com.elementary.tasks.reminder.lists.filters.SearchModifier

class ArchiveFragment : BaseNavigationFragment<FragmentTrashBinding>(), (List<Reminder>) -> Unit {

    private lateinit var viewModel: ArchiveRemindersViewModel

    private val reminderResolver = ReminderResolver(
            dialogAction = { return@ReminderResolver dialogues },
            saveAction = { reminder -> viewModel.saveReminder(reminder) },
            toggleAction = { },
            deleteAction = { reminder -> viewModel.deleteReminder(reminder, true) },
            skipAction = { },
            allGroups = { return@ReminderResolver viewModel.groups }
    )

    private var mAdapter = RemindersRecyclerAdapter()
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
        inflater.inflate(R.menu.fragment_trash, menu)

        ViewUtils.tintMenuIcon(context!!, menu, 0, R.drawable.ic_twotone_search_24px, isDark)
        ViewUtils.tintMenuIcon(context!!, menu, 1, R.drawable.ic_twotone_delete_sweep_24px, isDark)

        mSearchMenu = menu.findItem(R.id.action_search)
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
        if (mSearchMenu != null) {
            mSearchView = mSearchMenu?.actionView as SearchView?
        }
        if (mSearchView != null) {
            if (searchManager != null) {
                mSearchView?.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
            }
            mSearchView?.setOnQueryTextListener(queryTextListener)
        }

        val isNotEmpty = viewModel.events.value?.size ?: 0 > 0
        menu.getItem(0)?.isVisible = isNotEmpty
        menu.getItem(1)?.isVisible = isNotEmpty

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_all -> {
                viewModel.deleteAll(mAdapter.data)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun layoutRes(): Int = R.layout.fragment_trash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ArchiveRemindersViewModel::class.java)
        viewModel.events.observe(this, Observer { reminders ->
            if (reminders != null) {
                showData(reminders)
            }
        })
        viewModel.result.observe(this, Observer {
            if (it != null) {
                when (it) {
                    Commands.DELETED ->  Toast.makeText(context!!, R.string.trash_cleared, Toast.LENGTH_SHORT).show()
                    else -> {
                    }
                }
            }
        })
    }

    override fun getTitle(): String = getString(R.string.trash)

    private fun showData(result: List<Reminder>) {
        searchModifier.original = result.toMutableList()
        activity?.invalidateOptionsMenu()
    }

    private fun initList() {
        mAdapter.setEditable(false)
        mAdapter.showHeader = false
        mAdapter.prefsProvider = { prefs }
        mAdapter.actionsListener = object : ActionsListener<Reminder> {
            override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
                if (t != null) {
                    reminderResolver.resolveAction(view, t, actions)
                }
            }
        }
        if (prefs.isTwoColsEnabled && ViewUtils.isHorizontal(context!!) && resources.getBoolean(R.bool.is_tablet)) {
            binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            binding.recyclerView.layoutManager = LinearLayoutManager(context)
        }
        binding.recyclerView.adapter = mAdapter
        ViewUtils.listenScrollableView(binding.recyclerView, { setScroll(it) }, null)
        reloadView(0)
    }

    private fun reloadView(count: Int) {
        if (count > 0) {
            binding.emptyItem.visibility = View.GONE
        } else {
            binding.emptyItem.visibility = View.VISIBLE
        }
    }

    override fun invoke(result: List<Reminder>) {
        mAdapter.submitList(result)
        binding.recyclerView.smoothScrollToPosition(0)
        reloadView(result.size)
    }
}
