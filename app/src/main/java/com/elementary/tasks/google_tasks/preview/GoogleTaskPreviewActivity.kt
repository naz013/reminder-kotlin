package com.elementary.tasks.google_tasks.preview

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskPreview
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.isColorDark
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.ActivityGoogleTaskPreviewBinding
import com.elementary.tasks.google_tasks.TasksConstants
import com.elementary.tasks.google_tasks.task.GoogleTaskActivity
import com.elementary.tasks.pin.PinLoginActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class GoogleTaskPreviewActivity : BindingActivity<ActivityGoogleTaskPreviewBinding>() {

  private val viewModel by viewModel<GoogleTaskPreviewViewModel> { parametersOf(idFromIntent()) }

  private var initPaddingTop: Int? = null

  override fun inflateBinding() = ActivityGoogleTaskPreviewBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (initPaddingTop == null) {
      initPaddingTop = binding.rootView.paddingTop
    }

    drawBehindSystemBars(binding.rootView) { insets ->
      Timber.d("drawBehindSystemBars: $insets")
      binding.rootView.updatePadding(
        top = (initPaddingTop ?: 0) + insets.top
      )
    }

    initTopAppBar()

    binding.buttonComplete.setOnClickListener { viewModel.onComplete() }

    initViewModel()
  }

  private fun initTopAppBar() {
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
    PinLoginActivity.openLogged(
      this, Intent(this, GoogleTaskActivity::class.java)
        .putExtra(Constants.INTENT_ID, idFromIntent())
        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)
    )
  }

  private fun showTextIfNotNull(textView: TextView, value: String?) {
    textView.visibleGone(value != null)
    textView.text = value
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
    showTextIfNotNull(binding.completedDateView, googleTask.completedDate)
    showTextIfNotNull(binding.createdDateView, googleTask.createdDate)
    showTextIfNotNull(binding.dueDateView, googleTask.dueDate)
    showTextIfNotNull(binding.taskNotesView, googleTask.notes)
    showTextIfNotNull(binding.taskTitleView, googleTask.text)
    showTextIfNotNull(binding.listNameView, googleTask.taskListName)

    if (googleTask.taskListColor != 0) {
      binding.rootView.setBackgroundColor(googleTask.taskListColor)
    }

    binding.buttonComplete.visibleGone(!googleTask.isCompleted)

    if (!googleTask.isCompleted) {
      binding.completedDateView.gone()
      binding.statusView.text = getString(R.string.not_completed)
    } else {
      binding.statusView.text = getString(R.string.completed)
    }

    val isColorDark = googleTask.taskListColor.isColorDark()

    Timber.d("showGoogleTask: isDark=$isColorDark")

    updateStatusBar(binding.rootView, !isColorDark)

    updateMenu(isColorDark)
    updateIcons(isColorDark)
  }

  private fun updateMenu(isDarkColor: Boolean) {
    binding.toolbar.menu.also { menu ->
      Timber.d("updateMenu: ${menu.size()}")
      ViewUtils.tintMenuIconId(
        this,
        menu,
        R.id.action_edit,
        R.drawable.ic_twotone_edit_24px,
        isDarkColor
      )
      ViewUtils.tintMenuIconId(
        this,
        menu,
        R.id.action_delete,
        R.drawable.ic_twotone_delete_24px,
        isDarkColor
      )
      binding.toolbar.invalidateMenu()
    }
  }

  private fun updateIcons(isDarkColor: Boolean) {
    binding.toolbar.setNavigationIconTint(
      ContextCompat.getColor(
        this, if (isDarkColor) {
          R.color.pureWhite
        } else {
          R.color.pureBlack
        }
      )
    )
  }

  override fun requireLogin() = true
}
