package com.elementary.tasks.google_tasks.tasklist

import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.ui.showError
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.databinding.ActivityCreateTaskListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class GoogleTaskListActivity : BindingActivity<ActivityCreateTaskListBinding>() {

  private val viewModel by viewModel<GoogleTaskListViewModel> { parametersOf(getId()) }

  override fun inflateBinding() = ActivityCreateTaskListBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding.progressMessageView.text = getString(R.string.please_wait)
    updateProgress(false)

    initActionBar()
    binding.colorSlider.setColors(ThemeProvider.colorsForSliderThemed(this))
    binding.colorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)

    if (savedInstanceState != null) {
      binding.colorSlider.setSelection(savedInstanceState.getInt(ARG_COLOR, 0))
      updateProgress(savedInstanceState.getBoolean(ARG_LOADING, false))
    }

    initViewModel()
  }

  private fun getId(): String = intent.getStringExtra(Constants.INTENT_ID) ?: ""

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putInt(ARG_COLOR, binding.colorSlider.selectedItem)
    outState.putBoolean(ARG_LOADING, viewModel.isLoading)
    super.onSaveInstanceState(outState)
  }

  private fun updateProgress(b: Boolean) {
    viewModel.isLoading = b
    if (b) {
      binding.progressView.visibility = View.VISIBLE
    } else {
      binding.progressView.visibility = View.GONE
    }
  }

  private fun initActionBar() {
    binding.toolbar.setTitle(R.string.new_tasks_list)
    binding.toolbar.setNavigationOnClickListener { handleBackPress() }
    binding.toolbar.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.action_delete -> {
          doIfPossible { deleteDialog() }
          true
        }

        R.id.action_add -> {
          doIfPossible { saveTaskList() }
          true
        }

        else -> false
      }
    }
    updateMenu()
  }

  private fun updateMenu() {
    binding.toolbar.menu.also {
      it.getItem(1).isVisible = viewModel.canDelete()
    }
  }

  private fun initViewModel() {
    viewModel.googleTaskList.nonNullObserve(this) { editTaskList(it) }
    viewModel.isInProgress.nonNullObserve(this) { updateProgress(it) }
    viewModel.result.nonNullObserve(this) { commands ->
      handleBackPress().takeIf { commands == Commands.DELETED || commands == Commands.SAVED }
    }
  }

  private fun editTaskList(googleTaskList: GoogleTaskList) {
    binding.toolbar.title = getString(R.string.edit_task_list)
    viewModel.editedTaskList = googleTaskList
    if (!viewModel.isEdited) {
      binding.editField.setText(googleTaskList.title)
      if (googleTaskList.def == 1) {
        binding.defaultCheck.isChecked = true
        binding.defaultCheck.isEnabled = false
      }
      binding.colorSlider.setSelection(googleTaskList.color)
      viewModel.isEdited = true
    }
    updateMenu()
  }

  private fun saveTaskList() {
    val listName = binding.editField.trimmedText()
    if (listName.isEmpty()) {
      binding.nameLayout.showError(R.string.must_be_not_empty)
      return
    }
    var isNew = false
    val item = (viewModel.editedTaskList ?: GoogleTaskList().also { isNew = true }).apply {
      title = listName
      color = binding.colorSlider.selectedItem
      updated = System.currentTimeMillis()
    }
    if (binding.defaultCheck.isChecked) {
      item.def = 1
    }

    if (isNew) {
      viewModel.newGoogleTaskList(item)
    } else {
      viewModel.updateGoogleTaskList(item)
    }
  }

  private fun deleteDialog() {
    dialogues.getMaterialDialog(this)
      .setMessage(getString(R.string.delete_this_list))
      .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
        dialog.dismiss()
        deleteList()
      }
      .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
      .create().show()
  }

  private fun deleteList() {
    viewModel.editedTaskList?.let { viewModel.deleteGoogleTaskList(it) }
  }

  override fun onDestroy() {
    super.onDestroy()
    updatesHelper.updateTasksWidget()
  }

  override fun handleBackPress(): Boolean {
    doIfPossible { finish() }
    return true
  }

  private fun doIfPossible(f: () -> Unit) {
    if (viewModel.isLoading) {
      toast(R.string.please_wait)
    } else {
      f.invoke()
    }
  }

  companion object {
    private const val ARG_COLOR = "arg_color"
    private const val ARG_LOADING = "arg_loading"
  }
}
