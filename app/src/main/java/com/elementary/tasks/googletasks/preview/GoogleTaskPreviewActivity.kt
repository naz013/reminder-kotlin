package com.elementary.tasks.googletasks.preview

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskPreview
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.android.applyBottomInsets
import com.github.naz013.feature.common.android.applyBottomInsetsMargin
import com.github.naz013.feature.common.android.applyTopInsets
import com.github.naz013.feature.common.android.gone
import com.github.naz013.feature.common.android.visible
import com.github.naz013.feature.common.android.visibleGone
import com.elementary.tasks.databinding.ActivityGoogleTaskPreviewBinding
import com.elementary.tasks.googletasks.TasksConstants
import com.elementary.tasks.googletasks.task.GoogleTaskActivity
import com.elementary.tasks.pin.PinLoginActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class GoogleTaskPreviewActivity : BindingActivity<ActivityGoogleTaskPreviewBinding>() {

  private val viewModel by viewModel<GoogleTaskPreviewViewModel> { parametersOf(idFromIntent()) }
  private val adsProvider = AdsProvider()

  override fun inflateBinding() = ActivityGoogleTaskPreviewBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    binding.scrollView.applyBottomInsets()
    binding.buttonComplete.applyBottomInsetsMargin()
    initTopAppBar()
    binding.buttonComplete.setOnClickListener { viewModel.onComplete() }
    loadAds()
    initViewModel()
  }

  private fun loadAds() {
    if (!Module.isPro && AdsProvider.hasAds()) {
      adsProvider.showBanner(
        binding.adsHolder,
        AdsProvider.BIRTHDAY_PREVIEW_BANNER_ID
      )
    }
  }

  private fun initTopAppBar() {
    binding.appBar.applyTopInsets()
    binding.toolbar.setOnMenuItemClickListener { menuItem ->
      return@setOnMenuItemClickListener when (menuItem.itemId) {
        R.id.action_edit -> {
          editGoogleTask()
          true
        }

        R.id.action_delete -> {
          dialogues.askConfirmation(this, getString(R.string.delete)) {
            if (it) viewModel.onDelete()
          }
          true
        }

        else -> false
      }
    }
    binding.toolbar.setNavigationOnClickListener { finish() }
  }

  private fun editGoogleTask() {
    PinLoginActivity.openLogged(this, GoogleTaskActivity::class.java) {
      putExtra(Constants.INTENT_ID, idFromIntent())
      putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)
    }
  }

  private fun showTextIfNotNull(textView: TextView, value: String?, func: (Boolean) -> Unit) {
    textView.text = value
    func(value != null)
  }

  private fun idFromIntent() = intentString(Constants.INTENT_ID)

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.googleTask.nonNullObserve(this) { showGoogleTask(it) }
    viewModel.result.nonNullObserve(this) {
      when (it) {
        Commands.DELETED -> finishAfterTransition()
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

  override fun requireLogin() = true
}
