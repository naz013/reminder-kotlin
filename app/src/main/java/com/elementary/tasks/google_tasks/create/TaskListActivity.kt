package com.elementary.tasks.google_tasks.create

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.showError
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.trimmedText
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskListViewModel
import com.elementary.tasks.databinding.ActivityCreateTaskListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TaskListActivity : BindingActivity<ActivityCreateTaskListBinding>() {

  private val viewModel by viewModel<GoogleTaskListViewModel> { parametersOf(getId()) }
  private val stateViewModel by viewModel<GoogleTasksStateViewModel>()

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
    outState.putBoolean(ARG_LOADING, stateViewModel.isLoading)
    super.onSaveInstanceState(outState)
  }

  private fun updateProgress(b: Boolean) {
    stateViewModel.isLoading = b
    if (b) {
      binding.progressView.visibility = View.VISIBLE
    } else {
      binding.progressView.visibility = View.GONE
    }
  }

  private fun initActionBar() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeButtonEnabled(true)
    supportActionBar?.setDisplayShowHomeEnabled(true)
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
    binding.toolbar.setTitle(R.string.new_tasks_list)
  }

  private fun initViewModel() {
    viewModel.googleTaskList.observe(this) { editTaskList(it) }
    viewModel.isInProgress.observe(this) { updateProgress(it) }
    viewModel.result.observe(this) { commands ->
      handleBackPress().takeIf { commands == Commands.DELETED || commands == Commands.SAVED }
    }
  }

  private fun editTaskList(googleTaskList: GoogleTaskList) {
    binding.toolbar.title = getString(R.string.edit_task_list)
    stateViewModel.editedTaskList = googleTaskList
    if (!stateViewModel.isEdited) {
      binding.editField.setText(googleTaskList.title)
      if (googleTaskList.def == 1) {
        binding.defaultCheck.isChecked = true
        binding.defaultCheck.isEnabled = false
      }
      binding.colorSlider.setSelection(googleTaskList.color)
      stateViewModel.isEdited = true
    }
  }

  private fun saveTaskList() {
    val listName = binding.editField.trimmedText()
    if (listName.isEmpty()) {
      binding.nameLayout.showError(R.string.must_be_not_empty)
      return
    }
    var isNew = false
    val item = (stateViewModel.editedTaskList ?: GoogleTaskList().also { isNew = true }).apply {
      title = listName
      color = binding.colorSlider.selectedItem
      updated = System.currentTimeMillis()
    }
    if (binding.defaultCheck.isChecked) {
      item.def = 1
      viewModel.defaultTaskList.value?.also {
        it.def = 0
        viewModel.saveLocalGoogleTaskList(it)
      }
    }

    if (isNew) {
      viewModel.newGoogleTaskList(item)
    } else {
      viewModel.updateGoogleTaskList(item)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        handleBackPress()
        true
      }
      MENU_ITEM_DELETE -> {
        doIfPossible { deleteDialog() }
        true
      }
      R.id.action_add -> {
        doIfPossible { saveTaskList() }
        true
      }
      else -> super.onOptionsItemSelected(item)
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
    stateViewModel.editedTaskList?.let { viewModel.deleteGoogleTaskList(it) }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.activity_simple_save_action, menu)
    stateViewModel.editedTaskList
      ?.takeIf { !it.isAppDefault() }
      ?.also { menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_list) }
    return true
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
    if (stateViewModel.isLoading) {
      toast(R.string.please_wait)
    } else {
      f.invoke()
    }
  }

  companion object {
    private const val MENU_ITEM_DELETE = 12
    private const val ARG_COLOR = "arg_color"
    private const val ARG_LOADING = "arg_loading"
  }
}
