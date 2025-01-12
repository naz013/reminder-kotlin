package com.elementary.tasks.googletasks.tasklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.ui.showError
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.databinding.FragmentGoogleTaskListEditBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.fragment.hideKeyboard
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditGoogleTaskListFragment : BaseToolbarFragment<FragmentGoogleTaskListEditBinding>() {

  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val viewModel by viewModel<EditGoogleTaskListViewModel> { parametersOf(arguments) }

  override fun getTitle(): String {
    return if (viewModel.hasId()) {
      getString(R.string.edit_task_list)
    } else {
      getString(R.string.new_tasks_list)
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentGoogleTaskListEditBinding {
    return FragmentGoogleTaskListEditBinding.inflate(inflater, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the Google Task List edit screen for id: ${viewModel.listId}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.progressMessageView.text = getString(R.string.please_wait)
    updateProgress(false)

    binding.colorSlider.setColors(ThemeProvider.colorsForSliderThemed(requireContext()))
    binding.colorSlider.setSelectorColorResource(
      if (isDark) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }
    )
    binding.colorSlider.setListener { position, _ ->
      viewModel.onColorChanged(position)
    }

    addMenu(
      menuRes = R.menu.fragment_google_task_list_edit,
      onMenuItemListener = { menuItem ->
        return@addMenu when (menuItem.itemId) {
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
      },
      menuModifier = { menu ->
        menu.getItem(1).isVisible = viewModel.canDelete()
      }
    )

    initViewModel()
    viewModel.onCreated(savedInstanceState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    hideKeyboard()
    appWidgetUpdater.updateScheduleWidget()
  }

  private fun initViewModel() {
    viewModel.googleTaskList.nonNullObserve(this) { showTaskList(it) }
    viewModel.isInProgress.nonNullObserve(this) { updateProgress(it) }
    viewModel.result.nonNullObserve(this) { commands ->
      if (commands == Commands.DELETED || commands == Commands.SAVED) {
        moveBack()
      }
    }
    viewModel.colorChanged.nonNullObserve(viewLifecycleOwner) {
      binding.colorSlider.setSelection(it)
    }

    lifecycle.addObserver(viewModel)
  }

  private fun showTaskList(googleTaskList: GoogleTaskList) {
    viewModel.editedTaskList = googleTaskList
    if (!viewModel.isEdited) {
      binding.editField.setText(googleTaskList.title)
      if (googleTaskList.def == 1) {
        binding.defaultCheck.isChecked = true
        binding.defaultCheck.isEnabled = false
      }
      binding.colorSlider.setSelection(googleTaskList.color)
    }
    invalidateOptionsMenu()
  }

  private fun updateProgress(b: Boolean) {
    binding.progressView.visibleGone(b)
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
    dialogues.getMaterialDialog(requireContext())
      .setMessage(getString(R.string.delete_this_list))
      .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
        dialog.dismiss()
        viewModel.deleteGoogleTaskList()
      }
      .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
      .create().show()
  }

  private fun doIfPossible(f: () -> Unit) {
    if (viewModel.isInProgress.value == true) {
      toast(R.string.please_wait)
    } else {
      f.invoke()
    }
  }

  companion object {
    private const val TAG = "EditGoogleTaskFragment"
  }
}
