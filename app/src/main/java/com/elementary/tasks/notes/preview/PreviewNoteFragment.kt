package com.elementary.tasks.notes.preview

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.navigate
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.ComposeFragment
import com.github.naz013.ui.common.fragment.startActivity
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.login.LoginApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class PreviewNoteFragment : ComposeFragment() {
  private val imagesSingleton by inject<ImagesSingleton>()
  private val dialogues by inject<Dialogues>()
  private val adsProvider = AdsProvider()
  private val viewModel by viewModel<PreviewNoteViewModel> { parametersOf(idFromIntent()) }
  private lateinit var permissionFlow: PermissionFlow

  private fun idFromIntent(): String = arguments?.getString(IntentKeys.INTENT_ID) ?: ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionFlow = PermissionFlow(this, dialogues)
    viewModel.saveStatusBarColor(activity?.window?.statusBarColor ?: -1)
    lifecycle.addObserver(viewModel)
    Logger.i(TAG, "Opening the note preview screen for id: ${Logger.data(idFromIntent())}")
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) { commands ->
      Logger.d(TAG, "Received command: $commands")
      if (commands == Commands.DELETED) moveBack()
    }
    viewModel.errorEvent.observeEvent(viewLifecycleOwner) { showErrorSending() }
    viewModel.sharedFile.nonNullObserve(viewLifecycleOwner) { sendNote(it.first, it.second) }
  }

  @Composable
  override fun FragmentContent() {
    val state by viewModel.state.collectAsState()
    val colors = remember(state.backgroundColor, state.opacity) { viewModel.colorsFor(state) }

    SideEffect {
      activity?.window?.statusBarColor = colors.statusBarColor
      activity?.window?.navigationBarColor = colors.statusBarColor
    }

    PreviewNoteScreen(
      state = state,
      colors = colors,
      actions =
        PreviewNoteActions(
          onBackClick = { moveBack() },
          onEditClick = { editNote() },
          onStatusClick = { moveToStatus() },
          onShareClick = { viewModel.onShareClick() },
          onArchiveClick = { viewModel.onArchiveClick() },
          onDeleteClick = { viewModel.onDeleteClick() },
          onDeleteConfirmed = { viewModel.onDeleteConfirmed() },
          onDialogDismiss = { viewModel.onDialogDismiss() },
          onImageOpen = { openImagePreview(state, it) },
          onReminderEditClick = { editReminder(it) },
          onReminderDetachClick = { viewModel.onReminderDetachClick(it) },
        ),
      adsBanner =
        if (!BuildParams.isPro && AdsProvider.hasAds()) {
          { NativeAdBanner(adsProvider) }
        } else {
          null
        },
    )
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  override fun onPause() {
    super.onPause()
    viewModel.getStatusBarColor()?.also {
      activity?.window?.statusBarColor = it
      activity?.window?.navigationBarColor = it
    }
  }

  private fun moveBack() {
    activity?.onBackPressedDispatcher?.onBackPressed()
  }

  private fun editNote() {
    LoginApi.openLogged(requireContext(), CreateNoteActivity::class.java) {
      putExtra(IntentKeys.INTENT_ID, viewModel.key)
    }
  }

  private fun moveToStatus() {
    permissionFlow.askPermission(Permissions.POST_NOTIFICATION) {
      viewModel.onStatusClick()
    }
  }

  private fun editReminder(id: String) {
    navigate {
      navigate(
        R.id.buildReminderFragment,
        Bundle().apply { putString(IntentKeys.INTENT_ID, id) },
        NavigationAnimations.inDepthNavOptions(),
      )
    }
  }

  private fun openImagePreview(
    state: PreviewNoteState,
    position: Int,
  ) {
    imagesSingleton.setCurrent(images = state.images, backgroundColor = state.backgroundColor)
    startActivity(ImagePreviewActivity::class.java) {
      putExtra(IntentKeys.INTENT_POSITION, position)
    }
  }

  private fun sendNote(
    note: NoteWithImages,
    file: File,
  ) {
    if (isDetached) return
    if (!file.exists() || !file.canRead()) {
      showErrorSending()
      return
    }
    TelephonyUtil.sendNote(file, requireContext(), note.note?.summary)
  }

  private fun showErrorSending() {
    toast(R.string.error_sending)
  }

  companion object {
    private const val TAG = "PreviewNoteFragment"
  }
}

@Composable
private fun NativeAdBanner(adsProvider: AdsProvider) {
  val context = LocalContext.current
  AndroidView(
    modifier = Modifier.fillMaxWidth(),
    factory = { FrameLayout(context) },
    update = { viewGroup ->
      adsProvider.showNativeBanner(
        viewGroup,
        AdsProvider.NOTE_PREVIEW_BANNER_ID,
        R.layout.list_item_ads_hor,
      )
    },
  )
}
