package com.elementary.tasks.google_tasks.create

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskListViewModel
import com.elementary.tasks.databinding.ActivityCreateTaskListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TaskListActivity : BindingActivity<ActivityCreateTaskListBinding>() {

  private val viewModel by viewModel<GoogleTaskListViewModel> { parametersOf(getId()) }
  private val stateViewModel by viewModel<GoogleTasksStateViewModel>()

  private var mItem: GoogleTaskList? = null
  private var mIsLoading = false

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
    outState.putBoolean(ARG_LOADING, mIsLoading)
    super.onSaveInstanceState(outState)
  }

  private fun updateProgress(b: Boolean) {
    mIsLoading = b
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
    viewModel.googleTaskList.observe(this, { googleTaskList ->
      googleTaskList?.let { editTaskList(it) }
    })
    viewModel.isInProgress.observe(this, { aBoolean ->
      aBoolean?.let { updateProgress(it) }
    })
    viewModel.result.observe(this, { commands ->
      commands?.let {
        when (it) {
          Commands.DELETED, Commands.SAVED -> onBackPressed()
          else -> {
          }
        }
      }
    })
  }

  private fun editTaskList(googleTaskList: GoogleTaskList) {
    this.mItem = googleTaskList
    binding.toolbar.title = getString(R.string.edit_task_list)
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
    if (mIsLoading) return
    val listName = binding.editField.text.toString().trim()
    if (listName == "") {
      binding.nameLayout.error = getString(R.string.must_be_not_empty)
      binding.nameLayout.isErrorEnabled = true
      return
    }
    var isNew = false
    var item = mItem
    if (item == null) {
      item = GoogleTaskList()
      isNew = true
    }
    item.title = listName
    item.color = binding.colorSlider.selectedItem
    item.updated = System.currentTimeMillis()
    if (binding.defaultCheck.isChecked) {
      item.def = 1
      val defList = viewModel.defaultTaskList.value
      if (defList != null) {
        defList.def = 0
        viewModel.saveLocalGoogleTaskList(defList)
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
        onBackPressed()
        true
      }
      MENU_ITEM_DELETE -> {
        deleteDialog()
        true
      }
      R.id.action_add -> {
        saveTaskList()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun deleteDialog() {
    if (mIsLoading) return
    val builder = dialogues.getMaterialDialog(this)
    builder.setMessage(getString(R.string.delete_this_list))
    builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
      dialog.dismiss()
      deleteList()
      finish()
    }
    builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
    val dialog = builder.create()
    dialog.show()
  }

  private fun deleteList() {
    if (mIsLoading) return
    mItem?.let { viewModel.deleteGoogleTaskList(it) }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.activity_simple_save_action, menu)
    mItem?.let {
      if (it.systemDefault != 1) {
        menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_list)
      }
    }
    return true
  }

  override fun onDestroy() {
    super.onDestroy()
    UpdatesHelper.updateTasksWidget(this)
  }

  override fun onBackPressed() {
    if (mIsLoading) return
    super.onBackPressed()
  }

  companion object {
    private const val MENU_ITEM_DELETE = 12
    private const val ARG_COLOR = "arg_color"
    private const val ARG_LOADING = "arg_loading"
  }
}
