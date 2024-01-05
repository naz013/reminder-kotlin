package com.elementary.tasks.notes.preview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import androidx.core.view.get
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNotePreview
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.colorOf
import com.elementary.tasks.core.os.startActivity
import com.elementary.tasks.core.os.toast
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.isAlmostTransparent
import com.elementary.tasks.core.utils.isColorDark
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.tintOverflowButton
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.databinding.ActivityNotePreviewBinding
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.preview.carousel.ImagesCarouselAdapter
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.google.android.material.carousel.CarouselLayoutManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class NotePreviewActivity : BindingActivity<ActivityNotePreviewBinding>() {

  private var isBgDark = false

  private val adapter = ImagesCarouselAdapter()
  private val viewModel by viewModel<NotePreviewViewModel> { parametersOf(getId()) }
  private val dateTimeManager by inject<DateTimeManager>()
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()

  private val uiHandler = Handler(Looper.getMainLooper())

  private val imagesSingleton by inject<ImagesSingleton>()
  private val adsProvider = AdsProvider()

  override fun inflateBinding() = ActivityNotePreviewBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    isBgDark = isDarkMode
    initActionBar()
    updateTextColors()
    initImagesList()
    initReminderCard()
    initViewModel()
    loadAds()
  }

  private fun loadAds() {
    if (!Module.isPro && AdsProvider.hasAds()) {
      binding.adsCard.visible()
      adsProvider.showNativeBanner(
        binding.adsHolder,
        AdsProvider.NOTE_PREVIEW_BANNER_ID,
        R.layout.list_item_ads_hor
      ) {
        binding.adsCard.visible()
      }
    } else {
      binding.adsCard.visible()
    }
  }

  private fun getId() = intent.getStringExtra(Constants.INTENT_ID) ?: ""

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.note.nonNullObserve(this) { showNote(it) }
    viewModel.reminder.observe(this) { showReminder(it) }
    viewModel.result.nonNullObserve(this) { commands ->
      when (commands) {
        Commands.DELETED -> closeWindow()
        else -> {
        }
      }
    }
    viewModel.error.nonNullObserve(this) { showErrorSending() }
    viewModel.sharedFile.nonNullObserve(this) { sendNote(it.first, it.second) }
  }

  private fun initReminderCard() {
    binding.reminderContainer.gone()
    binding.editReminder.setOnClickListener { editReminder() }
    binding.deleteReminder.setOnClickListener { showReminderDeleteDialog() }
  }

  private fun editReminder() {
    val reminder = viewModel.reminder.value ?: return
    reminderBuilderLauncher.openLogged(this) {
      putExtra(Constants.INTENT_ID, reminder.uuId)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    adsProvider.destroy()
    adapter.actionsListener = null
  }

  private fun initImagesList() {
    adapter.actionsListener = object : ActionsListener<UiNoteImage> {
      override fun onAction(view: View, position: Int, t: UiNoteImage?, actions: ListActions) {
        when (actions) {
          ListActions.OPEN -> openImagePreview(position)
          else -> {
          }
        }
      }
    }
    binding.imagesList.layoutManager = CarouselLayoutManager()
    binding.imagesList.adapter = adapter
  }

  private fun openImagePreview(position: Int) {
    imagesSingleton.setCurrent(
      images = adapter.currentList,
      backgroundColor = viewModel.note.value?.backgroundColor ?: -1
    )
    startActivity(ImagePreviewActivity::class.java) {
      putExtra(Constants.INTENT_POSITION, position)
    }
  }

  private fun initActionBar() {
    binding.toolbar.title = ""
    binding.toolbar.setNavigationOnClickListener { closeWindow() }
    binding.toolbar.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.action_share -> {
          shareNote()
          true
        }

        R.id.action_delete -> {
          showDeleteDialog()
          true
        }

        R.id.action_status -> {
          moveToStatus()
          true
        }

        R.id.action_edit -> {
          editNote()
          true
        }

        R.id.action_archive -> {
          viewModel.toggleArchiveFlag()
          true
        }

        else -> false
      }
    }
    updateIcons()
    updateMenu()
  }

  private fun updateIcons() {
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isBgDark)
    binding.toolbar.tintOverflowButton(isBgDark)
  }

  private fun updateMenu(isArchived: Boolean = false) {
    val archiveActionTitle = if (isArchived) {
      getString(R.string.notes_unarchive)
    } else {
      getString(R.string.notes_move_to_archive)
    }
    binding.toolbar.menu.also { menu ->
      ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_fluent_edit, isBgDark)
      ViewUtils.tintMenuIcon(this, menu, 1, R.drawable.ic_fluent_heart, isBgDark)
      menu[3].setTitle(archiveActionTitle)
      menu[1].isVisible = !isArchived
      menu[2].isVisible = !isArchived
    }
  }

  private fun editNote() {
    PinLoginActivity.openLogged(this, CreateNoteActivity::class.java) {
      putExtra(Constants.INTENT_ID, viewModel.key)
    }
  }

  private fun moveToStatus() {
    val uiNotePreview = viewModel.note.value ?: return
    permissionFlow.askPermission(Permissions.POST_NOTIFICATION) {
      viewModel.showNoteInNotification(uiNotePreview.id)
    }
  }

  override fun handleBackPress(): Boolean {
    closeWindow()
    return true
  }

  private fun showNote(uiNotePreview: UiNotePreview) {
    showImages(uiNotePreview.images)
    binding.noteText.text = uiNotePreview.text
    binding.noteText.typeface = uiNotePreview.typeface
    binding.noteText.setTextSize(TypedValue.COMPLEX_UNIT_SP, uiNotePreview.textSize)
    window.statusBarColor = uiNotePreview.backgroundColor
    window.navigationBarColor = uiNotePreview.backgroundColor
    binding.windowBackground.setBackgroundColor(uiNotePreview.backgroundColor)
    isBgDark = if (uiNotePreview.opacity.isAlmostTransparent()) {
      isDarkMode
    } else {
      uiNotePreview.backgroundColor.isColorDark()
    }
    updateTextColors()
    updateIcons()
    updateMenu(uiNotePreview.isArchived)
  }

  private fun updateTextColors() {
    val textColor = if (isBgDark) {
      colorOf(R.color.pureWhite)
    } else {
      colorOf(R.color.pureBlack)
    }
    binding.noteText.setTextColor(textColor)
  }

  private fun showReminder(reminder: Reminder?) {
    if (reminder != null) {
      val dateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)?.let {
        dateTimeManager.getFullDateTime(it)
      }
      binding.reminderTime.text = dateTime
      binding.reminderContainer.visible()
    } else {
      binding.reminderContainer.gone()
    }
  }

  private fun showImages(images: List<UiNoteImage>) {
    adapter.submitList(images)
  }

  private fun shareNote() {
    viewModel.shareNote()
  }

  private fun sendNote(note: NoteWithImages, file: File) {
    if (isFinishing) return
    if (!file.exists() || !file.canRead()) {
      showErrorSending()
      return
    }
    TelephonyUtil.sendNote(file, this, note.note?.summary)
  }

  private fun showErrorSending() {
    toast(R.string.error_sending)
  }

  private fun closeWindow() {
    uiHandler.post { finishAfterTransition() }
  }

  private fun showDeleteDialog() {
    val builder = dialogues.getMaterialDialog(this)
    builder.setMessage(getString(R.string.delete_this_note))
    builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
      dialog.dismiss()
      viewModel.deleteNote()
    }
    builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
    builder.create().show()
  }

  private fun showReminderDeleteDialog() {
    val builder = dialogues.getMaterialDialog(this)
    builder.setMessage(R.string.delete_this_reminder)
    builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
      dialog.dismiss()
      deleteReminder()
    }
    builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
    builder.create().show()
  }

  private fun deleteReminder() {
    val reminder = viewModel.reminder.value ?: return
    viewModel.deleteReminder(reminder)
    binding.reminderContainer.gone()
  }
}
