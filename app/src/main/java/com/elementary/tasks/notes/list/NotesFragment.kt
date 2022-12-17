package com.elementary.tasks.notes.list

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Screen
import com.elementary.tasks.core.analytics.ScreenUsedEvent
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.GlobalButtonObservable
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.core.utils.startActivity
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.view_models.notes.NotesViewModel
import com.elementary.tasks.databinding.FragmentNotesBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.list.filters.SearchModifier
import com.elementary.tasks.notes.list.filters.SortModifier
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.pin.PinLoginActivity
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File

class NotesFragment : BaseNavigationFragment<FragmentNotesBinding>(),
    (List<NoteWithImages>) -> Unit {

  private val viewModel by viewModel<NotesViewModel>()
  private val themeProvider = currentStateHolder.theme
  private val buttonObservable by inject<GlobalButtonObservable>()

  private val mAdapter = NotesRecyclerAdapter(currentStateHolder, get()) {
    filterController.original = viewModel.notes.value ?: listOf()
  }
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

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentNotesBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.fab.setOnClickListener {
      PinLoginActivity.openLogged(
        requireContext(),
        CreateNoteActivity::class.java
      )
    }
    binding.fab.setOnLongClickListener {
      buttonObservable.fireAction(it, GlobalButtonObservable.Action.QUICK_NOTE)
      true
    }

    initProgress()

    initList()
    initViewModel()

    analyticsEventSender.send(ScreenUsedEvent(Screen.NOTES_LIST))

    addMenu(R.menu.notes_menu, { onMenuItemClicked(it) }) { modifyMenu(it) }
  }

  private fun modifyMenu(menu: Menu) {
    menu.getItem(1)?.title =
      if (enableGrid) getString(R.string.grid_view) else getString(R.string.list_view)

    ViewUtils.tintMenuIcon(requireContext(), menu, 0, R.drawable.ic_twotone_search_24px, isDark)
    ViewUtils.tintMenuIcon(
      requireContext(),
      menu,
      1,
      if (enableGrid) R.drawable.ic_twotone_view_quilt_24px else R.drawable.ic_twotone_view_list_24px,
      isDark
    )
    ViewUtils.tintMenuIcon(requireContext(), menu, 2, R.drawable.ic_twotone_sort_24px, isDark)

    mSearchMenu = menu.findItem(R.id.action_search)
    val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
    if (mSearchMenu != null) {
      mSearchView = mSearchMenu?.actionView as SearchView?
    }
    if (mSearchView != null) {
      if (searchManager != null) {
        mSearchView?.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
      }
      mSearchView?.setOnQueryTextListener(queryTextListener)
      mSearchView?.setOnCloseListener(mCloseListener)
    }
  }

  private fun onMenuItemClicked(menuItem: MenuItem): Boolean {
    return when (menuItem.itemId) {
      R.id.action_order -> {
        showDialog()
        true
      }

      R.id.action_list -> {
        enableGrid = !enableGrid
        prefs.isNotesGridEnabled = enableGrid
        binding.recyclerView.layoutManager = layoutManager()
        mAdapter.notifyDataSetChanged()
        activity?.invalidateOptionsMenu()
        true
      }

      else -> false
    }
  }

  private fun initProgress() {
    binding.progressMessageView.setText(R.string.please_wait)
    hideProgress()
  }

  private fun hideProgress() {
    binding.progressView.hide()
  }

  private fun showProgress() {
    binding.progressView.show()
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.notes.nonNullObserve(viewLifecycleOwner) { list ->
      Timber.d("initViewModel: $list")
      sortController.original = list
    }
    viewModel.sharedFile.nonNullObserve(viewLifecycleOwner) {
      sendNote(it.first, it.second)
    }
    viewModel.isInProgress.nonNullObserve(viewLifecycleOwner) {
      if (it) {
        showProgress()
      } else {
        hideProgress()
      }
    }
    viewModel.error.nonNullObserve(viewLifecycleOwner) { showErrorSending() }
  }

  private fun sendNote(note: NoteWithImages, file: File) {
    if (!file.exists() || !file.canRead()) {
      showErrorSending()
      return
    }
    TelephonyUtil.sendNote(file, requireContext(), note.note?.summary)
  }

  private fun showErrorSending() {
    toast(R.string.error_sending)
  }

  private fun layoutManager(): RecyclerView.LayoutManager {
    return if (enableGrid) {
      LinearLayoutManager(context)
    } else {
      if (resources.getBoolean(R.bool.is_tablet)) {
        StaggeredGridLayoutManager(
          resources.getInteger(R.integer.num_of_cols),
          StaggeredGridLayoutManager.VERTICAL
        )
      } else {
        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
      }
    }
  }

  private fun initList() {
    enableGrid = prefs.isNotesGridEnabled
    binding.recyclerView.layoutManager = layoutManager()
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
    ViewUtils.listenScrollableView(
      binding.recyclerView,
      { setToolbarAlpha(toAlpha(it.toFloat())) }) {
      if (it) binding.fab.show()
      else binding.fab.hide()
    }
  }

  private fun showMore(view: View, note: NoteWithImages) {
    var showIn = getString(R.string.show_in_status_bar)
    showIn = showIn.substring(0, showIn.length - 1)
    val items = arrayOf(
      getString(R.string.open),
      getString(R.string.share),
      showIn,
      getString(R.string.change_color),
      getString(R.string.edit),
      getString(R.string.delete)
    )
    Dialogues.showPopup(view, { item ->
      when (item) {
        0 -> previewNote(note.getKey())
        1 -> viewModel.shareNote(note)
        2 -> showInStatusBar(note)
        3 -> selectColor(note)
        4 -> PinLoginActivity.openLogged(
          requireContext(), Intent(context, CreateNoteActivity::class.java)
            .putExtra(Constants.INTENT_ID, note.getKey())
        )

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
    val items = arrayOf<CharSequence>(
      getString(R.string.by_date_az),
      getString(R.string.by_date_za),
      getString(R.string.name_az),
      getString(R.string.name_za)
    )
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
    startActivity(NotePreviewActivity::class.java) {
      it.putExtra(Constants.INTENT_ID, id)
    }
  }

  private fun showInStatusBar(note: NoteWithImages?) {
    if (note != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionFlow.askPermission(Permissions.POST_NOTIFICATION) {
          notifier.showNoteNotification(note)
        }
      } else {
        notifier.showNoteNotification(note)
      }
    }
  }

  private fun selectColor(note: NoteWithImages) {
    dialogues.showColorDialog(
      requireActivity(), note.getColor(), getString(R.string.color),
      themeProvider.noteColorsForSlider(note.getPalette())
    ) {
      viewModel.saveNoteColor(note, it)
    }
  }

  override fun invoke(result: List<NoteWithImages>) {
    val newList = NoteAdsViewHolder.updateList(result)
    Timber.d("invoke: ${newList.size}")
    mAdapter.submitList(newList)
    binding.emptyItem.visibleGone(newList.isEmpty())
    binding.recyclerView.visibleGone(newList.isNotEmpty())
  }
}
