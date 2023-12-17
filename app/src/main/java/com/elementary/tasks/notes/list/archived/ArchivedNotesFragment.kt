package com.elementary.tasks.notes.list.archived

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Screen
import com.elementary.tasks.core.analytics.ScreenUsedEvent
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.startActivity
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.FragmentNotesBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.list.NoteSortProcessor
import com.elementary.tasks.notes.list.NotesRecyclerAdapter
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.pin.PinLoginActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File

class ArchivedNotesFragment : BaseToolbarFragment<FragmentNotesBinding>() {

  private val viewModel by viewModel<ArchivedNotesViewModel>()
  private val imagesSingleton by inject<ImagesSingleton>()
  private val systemServiceProvider by inject<SystemServiceProvider>()

  private val notesRecyclerAdapter = NotesRecyclerAdapter()
  private var enableGrid = false

  private val searchMenuHandler = SearchMenuHandler(
    systemServiceProvider.provideSearchManager(),
    R.string.search
  ) { viewModel.onSearchUpdate(it) }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentNotesBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.emptyText.text = getString(R.string.notes_archive_is_empty)
    binding.fab.setOnClickListener {
      PinLoginActivity.openLogged(
        requireContext(),
        CreateNoteActivity::class.java
      )
    }

    initProgress()

    initList()
    initViewModel()

    analyticsEventSender.send(ScreenUsedEvent(Screen.NOTES_LIST))

    addMenu(R.menu.fragment_notes_archive, { onMenuItemClicked(it) }) { modifyMenu(it) }
  }

  private fun modifyMenu(menu: Menu) {
    menu.getItem(1)?.title =
      if (enableGrid) getString(R.string.grid_view) else getString(R.string.list_view)

    ViewUtils.tintMenuIcon(requireContext(), menu, 0, R.drawable.ic_twotone_search_24px, isDark)
    ViewUtils.tintMenuIcon(
      context = requireContext(),
      menu = menu,
      index = 1,
      resource = if (enableGrid) {
        R.drawable.ic_twotone_view_quilt_24px
      } else {
        R.drawable.ic_twotone_view_list_24px
      },
      isDark = isDark
    )
    ViewUtils.tintMenuIcon(requireContext(), menu, 2, R.drawable.ic_twotone_sort_24px, isDark)
    searchMenuHandler.initSearchMenu(requireActivity(), menu, R.id.action_search)
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
        invalidateOptionsMenu()
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
    binding.progressView.gone()
  }

  private fun showProgress() {
    binding.progressView.visible()
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.notes.nonNullObserve(viewLifecycleOwner) { list ->
      Timber.d("initViewModel: $list")
      notesRecyclerAdapter.submitList(list)
      binding.emptyItem.visibleGone(list.isEmpty())
      binding.recyclerView.visibleGone(list.isNotEmpty())
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
    notesRecyclerAdapter.actionsListener = object : ActionsListener<UiNoteList> {
      override fun onAction(view: View, position: Int, t: UiNoteList?, actions: ListActions) {
        when (actions) {
          ListActions.OPEN -> if (t != null) previewNote(t.id)
          ListActions.MORE -> if (t != null) showMore(view, t)
          else -> {
          }
        }
      }
    }
    notesRecyclerAdapter.imageClickListener = { note, imagePosition ->
      imagesSingleton.setCurrent(
        images = note.images,
        color = note.colorPosition,
        palette = note.colorPalette
      )
      startActivity(ImagePreviewActivity::class.java) {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(Constants.INTENT_ID, note.id)
        putExtra(Constants.INTENT_POSITION, imagePosition)
      }
    }
    binding.recyclerView.adapter = notesRecyclerAdapter
    binding.recyclerView.itemAnimator = DefaultItemAnimator()
    ViewUtils.listenScrollableView(binding.recyclerView) {
      if (it) {
        binding.fab.show()
      } else {
        binding.fab.hide()
      }
    }
  }

  private fun showMore(view: View, note: UiNoteList) {
    val items = arrayOf(
      getString(R.string.open),
      getString(R.string.edit),
      getString(R.string.notes_unarchive),
      getString(R.string.delete)
    )
    Dialogues.showPopup(view, { item ->
      when (item) {
        0 -> {
          previewNote(note.id)
        }

        1 -> {
          PinLoginActivity.openLogged(requireContext(), CreateNoteActivity::class.java) {
            putExtra(Constants.INTENT_ID, note.id)
          }
        }

        2 -> {
          viewModel.unArchive(note.id)
        }

        3 -> {
          askConfirmation(note.id)
        }
      }
    }, *items)
  }

  private fun askConfirmation(id: String) {
    withContext {
      dialogues.askConfirmation(it, getString(R.string.delete)) { b ->
        if (b) viewModel.deleteNote(id)
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
          0 -> value = NoteSortProcessor.DATE_AZ
          1 -> value = NoteSortProcessor.DATE_ZA
          2 -> value = NoteSortProcessor.TEXT_AZ
          3 -> value = NoteSortProcessor.TEXT_ZA
        }
        prefs.noteOrder = value
        viewModel.onOrderChanged()
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  override fun getTitle(): String = getString(R.string.notes_archive)

  private fun previewNote(id: String?) {
    startActivity(NotePreviewActivity::class.java) {
      putExtra(Constants.INTENT_ID, id)
    }
  }
}
