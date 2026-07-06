package com.elementary.tasks.googletasks.preview

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.navigate
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.menu.enableOrDisableItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PreviewGoogleTaskFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<PreviewGoogleTaskViewModel> { parametersOf(idFromIntent()) }
  private val adsProvider = AdsProvider()

  private fun idFromIntent(): String = arguments?.getString(IntentKeys.INTENT_ID) ?: ""

  override fun getTitle(): String = getString(R.string.details)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the Google Task preview screen for id: ${Logger.data(idFromIntent())}")
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)

    addMenu(
      menuRes = R.menu.fragment_google_task_preview,
      onMenuItemListener = { menuItem ->
        when (menuItem.itemId) {
          R.id.action_edit -> {
            editGoogleTask()
            true
          }

          R.id.action_delete -> {
            dialogues.askConfirmation(requireContext(), getString(R.string.delete)) {
              if (it) viewModel.onDelete()
            }
            true
          }

          else -> false
        }
      },
      menuModifier = { menu ->
        val isInProgress = viewModel.isInProgress.value ?: false
        menu.enableOrDisableItem(R.id.action_delete, !isInProgress)
        menu.enableOrDisableItem(R.id.action_edit, !isInProgress)
      },
    )

    viewModel.errorEvent.observeEvent(viewLifecycleOwner) { toast(it) }
    viewModel.navigationEvent.observeEvent(viewLifecycleOwner) { event ->
      when (event) {
        PreviewGoogleTaskEvent.Deleted -> moveBack()
      }
    }
    viewModel.isInProgress.nonNullObserve(viewLifecycleOwner) { invalidateOptionsMenu() }

    lifecycle.addObserver(viewModel)
  }

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    PreviewGoogleTaskScreen(
      state = state,
      onCompleteClick = viewModel::onComplete,
      adsContent = { AdsBanner() },
    )
  }

  @Composable
  private fun AdsBanner() {
    if (BuildParams.isPro || !AdsProvider.hasAds()) return
    val context = LocalContext.current
    AndroidView(
      factory = { FrameLayout(context) },
      update = { viewGroup -> adsProvider.showBanner(viewGroup, AdsProvider.GOOGLE_TASKS_PREVIEW_BANNER_ID) },
    )
  }

  private fun editGoogleTask() {
    navigate {
      navigate(
        R.id.editGoogleTaskFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, idFromIntent())
        },
        NavigationAnimations.inDepthNavOptions(),
      )
    }
  }

  override fun canGoBack(): Boolean = viewModel.isInProgress.value?.not() ?: true

  companion object {
    private const val TAG = "PreviewGoogleTaskFragment"
  }
}
