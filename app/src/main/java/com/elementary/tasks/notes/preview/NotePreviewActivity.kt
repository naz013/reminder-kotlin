package com.elementary.tasks.notes.preview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNotePreview
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.PermissionFlowDelegateImpl
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.TelephonyUtil
import com.github.naz013.ui.common.Dialogues
import com.elementary.tasks.core.utils.ui.tintOverflowButton
import com.elementary.tasks.databinding.ActivityNotePreviewBinding
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.preview.carousel.ImagesCarouselAdapter
import com.elementary.tasks.notes.preview.reminders.AttachedRemindersAdapter
import com.elementary.tasks.notes.preview.reminders.UiNoteAttachedReminder
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.context.colorOf
import com.github.naz013.ui.common.context.startActivity
import com.github.naz013.ui.common.isAlmostTransparent
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.login.LoginApi
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.applyTopInsets
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.visible
import com.google.android.material.carousel.CarouselLayoutManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class NotePreviewActivity : BindingActivity<ActivityNotePreviewBinding>() {

  private var isBgDark = false

  private val adapter = ImagesCarouselAdapter()
  private val attachedRemindersAdapter = AttachedRemindersAdapter(
    onEdit = { editReminder(it.id) },
    onDetach = { viewModel.detachReminder(it.id) }
  )

  private val viewModel by viewModel<NotePreviewViewModel> { parametersOf(getId()) }
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()
  private val dialogues by inject<Dialogues>()

  private val uiHandler = Handler(Looper.getMainLooper())

  private val imagesSingleton by inject<ImagesSingleton>()
  private val adsProvider = AdsProvider()
  private val permissionFlowDelegate = PermissionFlowDelegateImpl(this)

  override fun inflateBinding() = ActivityNotePreviewBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    binding.scrollView.applyBottomInsets()
    isBgDark = isDarkMode
    initActionBar()
    updateTextColors()
    initImagesList()
    initReminderCard()
    initViewModel()
    loadAds()
  }

  private fun loadAds() {
    if (!BuildParams.isPro && AdsProvider.hasAds()) {
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

  private fun getId() = intent.getStringExtra(IntentKeys.INTENT_ID) ?: ""

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.note.nonNullObserve(this) { showNote(it) }
    viewModel.reminders.nonNullObserve(this) { showReminders(it) }
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
    binding.attachedRemindersList.layoutManager = LinearLayoutManager(
      this,
      LinearLayoutManager.HORIZONTAL,
      false
    )
    binding.attachedRemindersList.adapter = attachedRemindersAdapter
  }

  private fun editReminder(id: String) {
    reminderBuilderLauncher.openLogged(this) {
      putExtra(IntentKeys.INTENT_ID, id)
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
      putExtra(IntentKeys.INTENT_POSITION, position)
    }
  }

  private fun initActionBar() {
    binding.appBar.applyTopInsets()
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
    LoginApi.openLogged(this, CreateNoteActivity::class.java) {
      putExtra(IntentKeys.INTENT_ID, viewModel.key)
    }
  }

  private fun moveToStatus() {
    val uiNotePreview = viewModel.note.value ?: return
    permissionFlowDelegate.with {
      askPermission(Permissions.POST_NOTIFICATION) {
        viewModel.showNoteInNotification(uiNotePreview.id)
      }
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

  private fun showReminders(reminders: List<UiNoteAttachedReminder>) {
    if (reminders.isNotEmpty()) {
      attachedRemindersAdapter.submitList(reminders)
      binding.attachedRemindersList.visible()
    } else {
      binding.attachedRemindersList.gone()
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
}
