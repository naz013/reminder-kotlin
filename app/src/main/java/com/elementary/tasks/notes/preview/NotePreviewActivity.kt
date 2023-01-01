package com.elementary.tasks.notes.preview

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Screen
import com.elementary.tasks.core.analytics.ScreenUsedEvent
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.colorOf
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.elementary.tasks.core.utils.isAlmostTransparent
import com.elementary.tasks.core.utils.isColorDark
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.tintOverflowButton
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.notes.NotePreviewViewModel
import com.elementary.tasks.core.views.GridMarginDecoration
import com.elementary.tasks.databinding.ActivityNotePreviewBinding
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.list.ImagesGridAdapter
import com.elementary.tasks.notes.list.KeepLayoutManager
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class NotePreviewActivity : BindingActivity<ActivityNotePreviewBinding>() {

  private var isBgDark = false

  private val mAdapter = ImagesGridAdapter()
  private val viewModel by viewModel<NotePreviewViewModel> { parametersOf(getId()) }
  private val permissionFlow = PermissionFlow(this, dialogues)
  private val dateTimeManager by inject<DateTimeManager>()

  private val mUiHandler = Handler(Looper.getMainLooper())

  private val themeUtil by inject<ThemeProvider>()
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
    analyticsEventSender.send(ScreenUsedEvent(Screen.NOTE_PREVIEW))
  }

  private fun loadAds() {
    if (!Module.isPro) {
      binding.adsCard.visible()
      adsProvider.showBanner(
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
    PinLoginActivity.openLogged(
      this, Intent(this, CreateReminderActivity::class.java)
        .putExtra(Constants.INTENT_ID, reminder.uuId)
    )
  }

  override fun onDestroy() {
    super.onDestroy()
    adsProvider.destroy()
    mAdapter.actionsListener = null
  }

  private fun initImagesList() {
    mAdapter.actionsListener = object : ActionsListener<ImageFile> {
      override fun onAction(view: View, position: Int, t: ImageFile?, actions: ListActions) {
        when (actions) {
          ListActions.OPEN -> openImagePreview(position)
          else -> {
          }
        }
      }
    }
    binding.imagesList.layoutManager = KeepLayoutManager(this, 6, mAdapter)
    binding.imagesList.addItemDecoration(GridMarginDecoration(resources.getDimensionPixelSize(R.dimen.grid_item_spacing)))
    binding.imagesList.adapter = mAdapter
  }

  private fun openImagePreview(position: Int) {
    imagesSingleton.setCurrent(mAdapter.data)
    startActivity(
      Intent(this, ImagePreviewActivity::class.java)
        .putExtra(Constants.INTENT_POSITION, position)
    )
  }

  private fun initActionBar() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    binding.toolbar.title = ""
    binding.toolbar.inflateMenu(R.menu.activity_preview_note)
    updateIcons()
  }

  private fun updateIcons() {
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isBgDark)
    binding.toolbar.tintOverflowButton(isBgDark)
    invalidateOptionsMenu()
  }

  private fun editNote() {
    val noteWithImages = viewModel.note.value ?: return
    PinLoginActivity.openLogged(
      this, Intent(this, CreateNoteActivity::class.java)
        .putExtra(Constants.INTENT_ID, noteWithImages.note?.key)
    )
  }

  private fun moveToStatus() {
    val noteWithImages = viewModel.note.value ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionFlow.askPermission(Permissions.POST_NOTIFICATION) {
        notifier.showNoteNotification(noteWithImages)
      }
    } else {
      notifier.showNoteNotification(noteWithImages)
    }
  }

  override fun handleBackPress(): Boolean {
    closeWindow()
    return true
  }

  private fun showNote(noteWithImages: NoteWithImages) {
    val noteColor = themeUtil.getNoteLightColor(
      noteWithImages.getColor(),
      noteWithImages.getOpacity(),
      noteWithImages.getPalette()
    )
    showImages(noteWithImages.images)
    binding.noteText.text = noteWithImages.getSummary()
    binding.noteText.typeface = AssetsUtil.getTypeface(this, noteWithImages.getStyle())
    window.statusBarColor = noteColor
    window.navigationBarColor = noteColor
    binding.windowBackground.setBackgroundColor(noteColor)
    isBgDark = if (noteWithImages.getOpacity().isAlmostTransparent()) {
      isDarkMode
    } else {
      noteColor.isColorDark()
    }
    updateTextColors()
    updateIcons()
  }

  private fun updateTextColors() {
    val textColor = if (isBgDark) colorOf(R.color.pureWhite)
    else colorOf(R.color.pureBlack)
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

  private fun showImages(images: List<ImageFile>) {
    mAdapter.submitList(images)
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
    Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.activity_preview_note, menu)
    ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_edit_24px, isBgDark)
    ViewUtils.tintMenuIcon(this, menu, 1, R.drawable.ic_twotone_favorite_24px, isBgDark)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        closeWindow()
        return true
      }

      R.id.action_share -> {
        shareNote()
        return true
      }

      R.id.action_delete -> {
        showDeleteDialog()
        return true
      }

      R.id.action_status -> {
        moveToStatus()
        return true
      }

      R.id.action_edit -> {
        editNote()
        return true
      }

      else -> return super.onOptionsItemSelected(item)
    }
  }

  private fun closeWindow() {
    mUiHandler.post { finishAfterTransition() }
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
    binding.reminderContainer.visibility = View.GONE
  }
}
