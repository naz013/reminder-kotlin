package com.elementary.tasks.reminder.preview

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.utils.TelephonyUtil
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.ui.common.Dialogues
import com.elementary.tasks.databinding.ActivityReminderPreviewBinding
import com.elementary.tasks.googletasks.task.GoogleTaskActivity
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.elementary.tasks.reminder.preview.adapter.ReminderPreviewDataAdapter
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.context.startActivity
import com.github.naz013.ui.common.login.LoginApi
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.applyTopInsets
import com.github.naz013.usecase.googletasks.TasksIntentKeys
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalTime

class ReminderPreviewActivity : BindingActivity<ActivityReminderPreviewBinding>() {

  private val viewModel by viewModel<ReminderPreviewViewModel> { parametersOf(getId()) }
  private val dateTimeManager by inject<DateTimeManager>()
  private val imagesSingleton by inject<ImagesSingleton>()
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()
  private val dialogues by inject<Dialogues>()
  private val prefs by inject<Prefs>()

  private val adapter = ReminderPreviewDataAdapter(
    fragmentManager = supportFragmentManager,
    prefs = prefs,
    onToggleClicked = { viewModel.switchClick() },
    onMapClick = { openFullMap(it) },
    subTaskCheckClick = { viewModel.onSubTaskChecked(it) },
    subTaskRemoveClick = { viewModel.onSubTaskRemoved(it) },
    noteClick = { openNote(it) },
    noteImageClick = { imageId, note -> onNoteImageClicked(imageId, note) },
    googleTaskClick = { onGoogleTaskClicked(it) },
    googleCalendarClick = { openCalendar(it) },
    googleRemoveClick = { viewModel.deleteEvent(it) }
  )

  override fun inflateBinding() = ActivityReminderPreviewBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    binding.dataListView.applyBottomInsets()
    initTopAppBar()
    initViews()
    initViewModel()
  }

  private fun getId() = intentString(IntentKeys.INTENT_ID)

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.reminderData.nonNullObserve(this) {
      adapter.submitList(it)
      updateMenu()
    }
    viewModel.result.nonNullObserve(this) { commands ->
      when (commands) {
        Commands.DELETED -> closeWindow()
        Commands.FAILED -> toast(getString(R.string.reminder_is_outdated), Toast.LENGTH_SHORT)
        else -> {
        }
      }
    }
    viewModel.sharedFile.nonNullObserve(this) {
      TelephonyUtil.sendFile(this, it)
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
    LoginApi.openLogged(this, GoogleTaskActivity::class.java) {
      putExtra(IntentKeys.INTENT_ID, id)
      putExtra(TasksIntentKeys.INTENT_ACTION, TasksIntentKeys.EDIT)
    }
  }

  private fun openNote(id: String) {
    startActivity(NotePreviewActivity::class.java) {
      putExtra(IntentKeys.INTENT_ID, id)
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
    reminderBuilderLauncher.openLogged(this) {
      putExtra(IntentKeys.INTENT_ID, getId())
    }
  }

  private fun removeReminder() {
    if (!viewModel.canDelete) {
      dialogues.askConfirmation(this, getString(R.string.move_to_trash)) {
        if (it) viewModel.moveToTrash()
      }
    } else {
      dialogues.askConfirmation(this, getString(R.string.delete)) {
        if (it) viewModel.deleteReminder(true)
      }
    }
  }

  private fun makeCopy() {
    showDialog()
  }

  private fun closeWindow() {
    postUi { finishAfterTransition() }
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
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(R.string.choose_time)
    builder.setItems(times.toTypedArray()) { dialog, which ->
      dialog.dismiss()
      saveCopy(list[which])
    }
    builder.create().show()
  }

  private fun saveCopy(time: LocalTime) {
    Logger.d("saveCopy: $time")
    viewModel.copyReminder(time)
  }

  private fun initViews() {
    binding.dataListView.layoutManager = LinearLayoutManager(this)
    binding.dataListView.adapter = adapter
  }

  private fun openFullMap(view: View) {
    val options = ActivityOptions.makeSceneTransitionAnimation(this, view, "map")
    startActivity(
      buildIntent(FullscreenMapActivity::class.java) {
        putExtra(IntentKeys.INTENT_ID, getId())
      },
      options.toBundle()
    )
  }

  private fun initTopAppBar() {
    binding.appBar.applyTopInsets()
    binding.toolbar.setOnMenuItemClickListener { menuItem ->
      return@setOnMenuItemClickListener when (menuItem.itemId) {
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
    }
    binding.toolbar.setNavigationOnClickListener { closeWindow() }
    updateMenu()
  }

  private fun updateMenu() {
    binding.toolbar.menu.also { menu ->
      menu.getItem(2)?.isVisible = viewModel.canCopy
    }
  }
}
