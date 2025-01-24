package com.elementary.tasks.reminder.preview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.databinding.FragmentReminderPreviewBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.reminder.preview.adapter.ReminderPreviewDataAdapter
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.fragment.startActivity
import com.github.naz013.ui.common.fragment.toast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalTime

class PreviewReminderFragment : BaseToolbarFragment<FragmentReminderPreviewBinding>() {

  private val viewModel by viewModel<PreviewReminderViewModel> { parametersOf(arguments) }
  private val dateTimeManager by inject<DateTimeManager>()
  private val imagesSingleton by inject<ImagesSingleton>()

  private var adapter: ReminderPreviewDataAdapter? = null

  override fun getTitle(): String {
    return getString(R.string.details)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentReminderPreviewBinding {
    return FragmentReminderPreviewBinding.inflate(inflater, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the reminder preview screen for id: ${viewModel.id}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Logger.d(TAG, "On view created, fragment manager: $parentFragmentManager")
    adapter = ReminderPreviewDataAdapter(
      fragmentManager = parentFragmentManager,
      prefs = prefs,
      onToggleClicked = { viewModel.switchClick() },
      onMapClick = { openFullMap() },
      subTaskCheckClick = { viewModel.onSubTaskChecked(it) },
      subTaskRemoveClick = { viewModel.onSubTaskRemoved(it) },
      noteClick = { openNote(it) },
      noteImageClick = { imageId, note -> onNoteImageClicked(imageId, note) },
      googleTaskClick = { onGoogleTaskClicked(it) },
      googleCalendarClick = { openCalendar(it) },
      googleRemoveClick = { viewModel.deleteEvent(it) }
    )
    binding.dataListView.layoutManager = LinearLayoutManager(context)
    binding.dataListView.adapter = adapter

    addMenu(
      menuRes = R.menu.fragment_reminder_preview,
      onMenuItemListener = { menuItem ->
        return@addMenu when (menuItem.itemId) {
          R.id.action_delete -> {
            removeReminder()
            true
          }

          R.id.action_make_copy -> {
            makeCopy()
            true
          }

          R.id.action_share -> {
            shareReminder()
            true
          }

          R.id.action_edit -> {
            editReminder()
            true
          }

          else -> false
        }
      },
      menuModifier = { menu ->
        menu.getItem(2)?.isVisible = viewModel.canCopy
      }
    )

    initViewModel()
  }

  override fun onStop() {
    super.onStop()
    Logger.d(TAG, "Stopped")
    adapter?.submitList(emptyList())
    adapter = null
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.reminderData.nonNullObserve(viewLifecycleOwner) {
      if (isAdded) {
        Logger.d(TAG, "Reminder data updated")
        adapter?.submitList(it)
        invalidateOptionsMenu()
      }
    }
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) { commands ->
      when (commands) {
        Commands.DELETED -> moveBack()
        Commands.FAILED -> toast(getString(R.string.reminder_is_outdated), Toast.LENGTH_SHORT)
        else -> {
        }
      }
    }
    viewModel.sharedFile.nonNullObserve(viewLifecycleOwner) {
      TelephonyUtil.sendFile(requireContext(), it)
    }
  }

  private fun openCalendar(id: Long) {
    if (id <= 0L) return
    val uri = Uri.parse("content://com.android.calendar/events/$id")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    runCatching {
      startActivity(intent)
    }
  }

  private fun onGoogleTaskClicked(id: String) {
    navigate {
      navigate(
        R.id.editGoogleTaskFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, id)
        }
      )
    }
  }

  private fun openNote(id: String) {
    navigate {
      navigate(
        R.id.previewNoteFragment,
        Bundle().apply { putString(IntentKeys.INTENT_ID, id) }
      )
    }
  }

  private fun onNoteImageClicked(imageId: Int, note: UiNoteList) {
    val imagePosition = note.images.indexOfFirst { it.id == imageId }.takeIf { it != -1 } ?: 0
    imagesSingleton.setCurrent(
      images = note.images,
      color = note.colorPosition,
      palette = note.colorPalette
    )
    startActivity(ImagePreviewActivity::class.java) {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      putExtra(IntentKeys.INTENT_ID, note.id)
      putExtra(IntentKeys.INTENT_POSITION, imagePosition)
    }
  }

  private fun shareReminder() {
    viewModel.shareReminder()
  }

  private fun editReminder() {
    navigate {
      navigate(
        R.id.buildReminderFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, viewModel.id)
        }
      )
    }
  }

  private fun removeReminder() {
    if (!viewModel.canDelete) {
      dialogues.askConfirmation(requireContext(), getString(R.string.move_to_trash)) {
        if (it) viewModel.moveToTrash()
      }
    } else {
      dialogues.askConfirmation(requireContext(), getString(R.string.delete)) {
        if (it) viewModel.deleteReminder(true)
      }
    }
  }

  private fun makeCopy() {
    showDialog()
  }

  private fun showDialog() {
    var time = LocalTime.of(0, 0)
    val list = mutableListOf<LocalTime>()
    val times = mutableListOf<String>()
    var isRunning = true
    do {
      if (time.hour == 23 && time.minute == 30) {
        isRunning = false
      } else {
        list.add(time)
        times.add(dateTimeManager.getTime(time))
        time = time.plusMinutes(30)
      }
    } while (isRunning)
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(R.string.choose_time)
    builder.setItems(times.toTypedArray()) { dialog, which ->
      dialog.dismiss()
      saveCopy(list[which])
    }
    builder.create().show()
  }

  private fun saveCopy(time: LocalTime) {
    viewModel.copyReminder(time)
  }

  private fun openFullMap() {
    navigate {
      navigate(
        R.id.fullscreenMapFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, viewModel.id)
        }
      )
    }
  }

  companion object {
    private const val TAG = "PreviewReminderFragment"
  }
}
