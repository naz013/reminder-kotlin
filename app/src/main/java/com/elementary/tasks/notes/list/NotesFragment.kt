package com.elementary.tasks.notes.list

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.notes.NotesViewModel
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.reminder.lists.filters.FilterCallback
import kotlinx.android.synthetic.main.fragment_notes.*
import kotlinx.android.synthetic.main.view_progress.*
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class NotesFragment : BaseNavigationFragment(), FilterCallback<NoteWithImages> {

    private lateinit var viewModel: NotesViewModel
    @Inject
    lateinit var backupTool: BackupTool

    private var mAdapter: NotesRecyclerAdapter = NotesRecyclerAdapter()
    private var enableGrid = false

    private val filterController = NoteFilterController(this)

    private var mSearchView: SearchView? = null
    private var mSearchMenu: MenuItem? = null

    private val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            filterController.setSearchValue(query)
            if (mSearchMenu != null) {
                mSearchMenu?.collapseActionView()
            }
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            filterController.setSearchValue(newText)
            return false
        }
    }

    private val mCloseListener = {
        filterController.setSearchValue("")
        true
    }

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.notes_menu, menu)

        val searchIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_twotone_search_24px)
        val listIcon = ContextCompat.getDrawable(context!!, if (enableGrid) R.drawable.ic_twotone_view_quilt_24px else R.drawable.ic_twotone_view_list_24px)
        val sortIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_twotone_filter_list_24px)
        if (isDark) {
            val white = ContextCompat.getColor(context!!, R.color.whitePrimary)
            DrawableCompat.setTint(searchIcon!!, white)
            DrawableCompat.setTint(listIcon!!, white)
            DrawableCompat.setTint(sortIcon!!, white)
        } else {
            val black = ContextCompat.getColor(context!!, R.color.pureBlack)
            DrawableCompat.setTint(listIcon!!, black)
            DrawableCompat.setTint(sortIcon!!, black)
            DrawableCompat.setTint(searchIcon!!, black)
        }

        menu?.getItem(0)?.icon = searchIcon
        menu?.getItem(1)?.icon = listIcon
        menu?.getItem(1)?.title = if (enableGrid) getString(R.string.grid_view) else getString(R.string.list_view)
        menu?.getItem(2)?.icon = sortIcon

        mSearchMenu = menu?.findItem(R.id.action_search)
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
        if (mSearchMenu != null) {
            mSearchView = mSearchMenu?.actionView as SearchView?
        }
        if (mSearchView != null) {
            if (searchManager != null) {
                mSearchView?.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
            }
            mSearchView?.setOnQueryTextListener(queryTextListener)
            mSearchView?.setOnCloseListener(mCloseListener)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun shareNote(note: NoteWithImages) {
        showProgress()
        launchDefault {
            val file = backupTool.createNote(note)
            withUIContext {
                if (file != null) sendNote(note, file)
            }
        }
    }

    private fun sendNote(note: NoteWithImages, file: File) {
        hideProgress()
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(context, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
            return
        }
        TelephonyUtil.sendNote(file, context!!, note.note?.summary)
    }

    private fun hideProgress() {
        progressView.visibility = View.GONE
    }

    private fun showProgress() {
        progressView.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_order -> showDialog()
            R.id.action_list -> {
                enableGrid = !enableGrid
                prefs.isNotesGridEnabled = enableGrid
                recyclerView.layoutManager = layoutManager()
                mAdapter.notifyDataSetChanged()
                activity?.invalidateOptionsMenu()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun layoutRes(): Int = R.layout.fragment_notes

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener { startActivity(Intent(activity!!, CreateNoteActivity::class.java)) }
        fab.setOnLongClickListener {
            buttonObservable.fireAction(it, GlobalButtonObservable.Action.QUICK_NOTE)
            true
        }

        initProgress()

        initList()
        initViewModel()
    }

    private fun initProgress() {
        progressMessageView.setText(R.string.please_wait)
        hideProgress()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(NotesViewModel::class.java)
        viewModel.notes.observe(this, Observer { list ->
            if (list != null) {
                Timber.d("initViewModel: $list")
                filterController.original = list
            }
        })
    }

    private fun layoutManager(): RecyclerView.LayoutManager {
        return if (enableGrid) {
            LinearLayoutManager(context)
        } else {
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun initList() {
        enableGrid = prefs.isNotesGridEnabled
        recyclerView.layoutManager = layoutManager()
        mAdapter = NotesRecyclerAdapter()
        mAdapter.actionsListener = object : ActionsListener<NoteWithImages> {
            override fun onAction(view: View, position: Int, t: NoteWithImages?, actions: ListActions) {
                when (actions) {
                    ListActions.OPEN -> if (t != null) previewNote(t.getKey(), view)
                    ListActions.MORE -> if (t != null) showMore(view, t)
                }
            }
        }
        recyclerView.adapter = mAdapter
        recyclerView.itemAnimator = DefaultItemAnimator()
        refreshView()
    }

    private fun showMore(view: View, note: NoteWithImages) {
        var showIn = getString(R.string.show_in_status_bar)
        showIn = showIn.substring(0, showIn.length - 1)
        val items = arrayOf(getString(R.string.open), getString(R.string.share), showIn, getString(R.string.change_color), getString(R.string.edit), getString(R.string.delete))
        Dialogues.showPopup(view, { item ->
            when (item) {
                0 -> previewNote(note.getKey(), view)
                1 -> shareNote(note)
                2 -> showInStatusBar(note)
                3 -> selectColor(note)
                4 -> context?.startActivity(Intent(context, CreateNoteActivity::class.java)
                        .putExtra(Constants.INTENT_ID, note.getKey()))
                5 -> viewModel.deleteNote(note)
            }
        }, *items)
    }

    private fun showDialog() {
        val items = arrayOf<CharSequence>(getString(R.string.by_date_az), getString(R.string.by_date_za), getString(R.string.name_az), getString(R.string.name_za))
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.order))
        builder.setItems(items) { dialog, which ->
            var value = ""
            when (which) {
                0 -> value = Constants.ORDER_DATE_A_Z
                1 -> value = Constants.ORDER_DATE_Z_A
                2 -> value = Constants.ORDER_NAME_A_Z
                3 -> value = Constants.ORDER_NAME_Z_A
            }
            prefs.noteOrder = value
            dialog.dismiss()
            viewModel.reload()
        }
        val alert = builder.create()
        alert.show()
    }

    override fun getTitle(): String = getString(R.string.notes)

    private fun previewNote(id: String?, view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val intent = Intent(context, NotePreviewActivity::class.java)
            intent.putExtra(Constants.INTENT_ID, id)
            val transitionName = "image"
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, view, transitionName)
            startActivity(intent, options.toBundle())
        } else {
            startActivity(Intent(context, NotePreviewActivity::class.java)
                    .putExtra(Constants.INTENT_ID, id))
        }
    }

    private fun showInStatusBar(note: NoteWithImages?) {
        if (note != null) {
            notifier.showNoteNotification(note)
        }
    }

    private fun selectColor(note: NoteWithImages) {
        var items = arrayOf(getString(R.string.red), getString(R.string.purple), getString(R.string.green), getString(R.string.green_light), getString(R.string.blue), getString(R.string.blue_light), getString(R.string.yellow), getString(R.string.orange), getString(R.string.cyan), getString(R.string.pink), getString(R.string.teal), getString(R.string.amber))
        if (Module.isPro) {
            items = arrayOf(getString(R.string.red), getString(R.string.purple), getString(R.string.green), getString(R.string.green_light), getString(R.string.blue), getString(R.string.blue_light), getString(R.string.yellow), getString(R.string.orange), getString(R.string.cyan), getString(R.string.pink), getString(R.string.teal), getString(R.string.amber), getString(R.string.dark_purple), getString(R.string.dark_orange), getString(R.string.lime), getString(R.string.indigo))
        }
        dialogues.showLCAM(context!!, { item ->
            note.note?.color = item
            viewModel.saveNote(note)
        }, *items)
    }

    private fun refreshView() {
        if (mAdapter.itemCount == 0) {
            emptyItem.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyItem.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onChanged(result: List<NoteWithImages>) {
        mAdapter.submitList(result)
        recyclerView.smoothScrollToPosition(0)
        refreshView()
    }
}
