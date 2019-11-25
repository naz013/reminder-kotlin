package com.elementary.tasks.notes.list

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.notes.NotesViewModel
import com.elementary.tasks.databinding.FragmentNotesBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.list.filters.SearchModifier
import com.elementary.tasks.notes.list.filters.SortModifier
import com.elementary.tasks.notes.preview.NotePreviewActivity
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File

class NotesFragment : BaseNavigationFragment<FragmentNotesBinding>(), (List<NoteWithImages>) -> Unit {

    private val viewModel: NotesViewModel by lazy {
        ViewModelProviders.of(this).get(NotesViewModel::class.java)
    }
    private val backupTool: BackupTool by inject()
    private val themeUtil: ThemeUtil by inject()
    private val buttonObservable: GlobalButtonObservable by inject()

    private var mAdapter = NotesRecyclerAdapter()
    private var enableGrid = false

    private val filterController = SearchModifier(null, null)
    private val sortController = SortModifier(filterController, this)

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notes_menu, menu)

        menu.getItem(1)?.title = if (enableGrid) getString(R.string.grid_view) else getString(R.string.list_view)

        ViewUtils.tintMenuIcon(context!!, menu, 0, R.drawable.ic_twotone_search_24px, isDark)
        ViewUtils.tintMenuIcon(context!!, menu, 1, if (enableGrid) R.drawable.ic_twotone_view_quilt_24px else R.drawable.ic_twotone_view_list_24px, isDark)
        ViewUtils.tintMenuIcon(context!!, menu, 2, R.drawable.ic_twotone_sort_24px, isDark)

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
            mSearchView?.setOnCloseListener(mCloseListener)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun shareNote(note: NoteWithImages) {
        showProgress()
        launchDefault {
            val file = backupTool.noteToFile(context!!, note)
            withUIContext {
                hideProgress()
                if (file != null) {
                    sendNote(note, file)
                } else {
                    showErrorSending()
                }
            }
        }
    }

    private fun sendNote(note: NoteWithImages, file: File) {
        if (!file.exists() || !file.canRead()) {
            showErrorSending()
            return
        }
        TelephonyUtil.sendNote(file, context!!, note.note?.summary)
    }

    private fun showErrorSending() {
        Toast.makeText(context, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
    }

    private fun hideProgress() {
        binding.progressView.visibility = View.GONE
    }

    private fun showProgress() {
        binding.progressView.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_order -> showDialog()
            R.id.action_list -> {
                enableGrid = !enableGrid
                prefs.isNotesGridEnabled = enableGrid
                binding.recyclerView.layoutManager = layoutManager()
                mAdapter.notifyDataSetChanged()
                activity?.invalidateOptionsMenu()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun layoutRes(): Int = R.layout.fragment_notes

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.setOnClickListener { CreateNoteActivity.openLogged(context!!) }
        binding.fab.setOnLongClickListener {
            buttonObservable.fireAction(it, GlobalButtonObservable.Action.QUICK_NOTE)
            true
        }

        initProgress()

        initList()
        initViewModel()
    }

    private fun initProgress() {
        binding.progressMessageView.setText(R.string.please_wait)
        hideProgress()
    }

    private fun initViewModel() {
        viewModel.notes.observe(viewLifecycleOwner, Observer { list ->
            if (list != null) {
                Timber.d("initViewModel: $list")
                sortController.original = list
            }
        })
    }

    private fun layoutManager(): RecyclerView.LayoutManager {
        return if (enableGrid) {
            LinearLayoutManager(context)
        } else {
            if (resources.getBoolean(R.bool.is_tablet)) {
                StaggeredGridLayoutManager(resources.getInteger(R.integer.num_of_cols), StaggeredGridLayoutManager.VERTICAL)
            } else {
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            }
        }
    }

    private fun initList() {
        enableGrid = prefs.isNotesGridEnabled
        binding.recyclerView.layoutManager = layoutManager()
        mAdapter = NotesRecyclerAdapter()
        mAdapter.actionsListener = object : ActionsListener<NoteWithImages> {
            override fun onAction(view: View, position: Int, t: NoteWithImages?, actions: ListActions) {
                when (actions) {
                    ListActions.OPEN -> if (t != null) previewNote(t.getKey())
                    ListActions.MORE -> if (t != null) showMore(view, t)
                    else -> {
                    }
                }
            }
        }
        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        ViewUtils.listenScrollableView(binding.recyclerView, { setToolbarAlpha(toAlpha(it.toFloat())) }) {
            if (it) binding.fab.show()
            else binding.fab.hide()
        }
        refreshView(0)
    }

    private fun showMore(view: View, note: NoteWithImages) {
        var showIn = getString(R.string.show_in_status_bar)
        showIn = showIn.substring(0, showIn.length - 1)
        val items = arrayOf(getString(R.string.open), getString(R.string.share), showIn, getString(R.string.change_color), getString(R.string.edit), getString(R.string.delete))
        Dialogues.showPopup(view, { item ->
            when (item) {
                0 -> previewNote(note.getKey())
                1 -> shareNote(note)
                2 -> showInStatusBar(note)
                3 -> selectColor(note)
                4 -> CreateNoteActivity.openLogged(context!!, Intent(context, CreateNoteActivity::class.java)
                        .putExtra(Constants.INTENT_ID, note.getKey()))
                5 -> askConfirmation(note)
            }
        }, *items)
    }

    private fun askConfirmation(note: NoteWithImages) {
        withContext {
            dialogues.askConfirmation(it, getString(R.string.delete)) { b ->
                if (b) viewModel.deleteNote(note)
            }
        }
    }

    private fun showDialog() {
        val items = arrayOf<CharSequence>(getString(R.string.by_date_az), getString(R.string.by_date_za), getString(R.string.name_az), getString(R.string.name_za))
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setTitle(getString(R.string.order))
            builder.setItems(items) { dialog, which ->
                var value = ""
                when (which) {
                    0 -> value = SortModifier.DATE_AZ
                    1 -> value = SortModifier.DATE_ZA
                    2 -> value = SortModifier.TEXT_AZ
                    3 -> value = SortModifier.TEXT_ZA
                }
                prefs.noteOrder = value
                sortController.setOrder(value)
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    override fun getTitle(): String = getString(R.string.notes)

    private fun previewNote(id: String?) {
        startActivity(Intent(context, NotePreviewActivity::class.java)
                .putExtra(Constants.INTENT_ID, id))
    }

    private fun showInStatusBar(note: NoteWithImages?) {
        if (note != null) {
            Notifier.showNoteNotification(context!!, prefs, note)
        }
    }

    private fun selectColor(note: NoteWithImages) {
        dialogues.showColorDialog(activity!!, note.getColor(), getString(R.string.color),
                themeUtil.noteColorsForSlider(note.getPalette())) {
            viewModel.saveNoteColor(note, it)
        }
    }

    private fun refreshView(count: Int) {
        if (count == 0) {
            binding.emptyItem.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyItem.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    override fun invoke(result: List<NoteWithImages>) {
        Timber.d("invoke: $result")
        mAdapter.submitList(result)
        refreshView(result.size)
    }
}
