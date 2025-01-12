package com.elementary.tasks.googletasks.preview

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskPreview
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.databinding.FragmentGoogleTaskPreviewBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.visible
import com.github.naz013.ui.common.view.visibleGone
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PreviewGoogleTaskFragment : BaseToolbarFragment<FragmentGoogleTaskPreviewBinding>() {

  private val viewModel by viewModel<PreviewGoogleTaskViewModel> { parametersOf(idFromIntent()) }
  private val adsProvider = AdsProvider()

  private fun idFromIntent(): String = arguments?.getString(IntentKeys.INTENT_ID) ?: ""

  override fun getTitle(): String {
    return getString(R.string.details)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentGoogleTaskPreviewBinding {
    return FragmentGoogleTaskPreviewBinding.inflate(inflater, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the Google Task preview screen for id: ${idFromIntent()}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.buttonComplete.setOnClickListener { viewModel.onComplete() }

    loadAds()

    addMenu(
      menuRes = R.menu.fragment_google_task_preview,
      onMenuItemListener = { menuItem ->
        return@addMenu when (menuItem.itemId) {
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
      }
    )

    initViewModel()
  }

  private fun loadAds() {
    if (!BuildParams.isPro && AdsProvider.hasAds()) {
      adsProvider.showBanner(
        binding.adsHolder,
        AdsProvider.BIRTHDAY_PREVIEW_BANNER_ID
      )
    }
  }

  private fun editGoogleTask() {
    navigate {
      navigate(
        R.id.editGoogleTaskFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, idFromIntent())
        }
      )
    }
  }

  private fun showTextIfNotNull(textView: TextView, value: String?, func: (Boolean) -> Unit) {
    textView.text = value
    func(value != null)
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.googleTask.nonNullObserve(this) { showGoogleTask(it) }
    viewModel.result.nonNullObserve(this) {
      when (it) {
        Commands.DELETED -> moveBack()
        else -> {
        }
      }
    }
    viewModel.isInProgress.nonNullObserve(this) {
      if (it) {
        binding.animationView.visible()
        binding.animationView.playAnimation()
      } else {
        binding.animationView.gone()
        binding.animationView.cancelAnimation()
      }
    }
  }

  private fun showGoogleTask(googleTask: UiGoogleTaskPreview) {
    showTextIfNotNull(binding.completedDateView, googleTask.completedDate) {
      binding.completedDateViewBlock.visibleGone(it)
    }
    showTextIfNotNull(binding.createdDateView, googleTask.createdDate) {
      binding.createdDateViewBlock.visibleGone(it)
    }
    showTextIfNotNull(binding.dueDateView, googleTask.dueDate) {
      binding.dueDateViewBlock.visibleGone(it)
    }
    showTextIfNotNull(binding.taskNotesView, googleTask.notes) {
      binding.taskNotesViewBlock.visibleGone(it)
    }
    showTextIfNotNull(binding.taskTitleView, googleTask.text) {
      binding.taskTitleViewBlock.visibleGone(it)
    }
    showTextIfNotNull(binding.listNameView, googleTask.taskListName) {
      binding.listNameViewBlock.visibleGone(it)
    }

    googleTask.taskListColor.also { color ->
      binding.listNameView.setTextColor(color)
      binding.listNameIconView.imageTintList = ColorStateList.valueOf(color)
    }

    binding.buttonComplete.visibleGone(!googleTask.isCompleted)

    if (!googleTask.isCompleted) {
      binding.completedDateView.gone()
      binding.statusView.text = getString(R.string.not_completed)
    } else {
      binding.statusView.text = getString(R.string.completed)
    }
  }

  companion object {
    private const val TAG = "PreviewGoogleTaskFragment"
  }
}
