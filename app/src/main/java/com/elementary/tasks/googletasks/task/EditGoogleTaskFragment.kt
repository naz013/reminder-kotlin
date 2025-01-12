package com.elementary.tasks.googletasks.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.databinding.FragmentGoogleTaskEditBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.nullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.fragment.hideKeyboard
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditGoogleTaskFragment : BaseToolbarFragment<FragmentGoogleTaskEditBinding>() {

  private val dateTimePickerProvider by inject<DateTimePickerProvider>()
  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val viewModel by viewModel<EditGoogleTaskViewModel> { parametersOf(arguments) }

  override fun getTitle(): String {
    return if (viewModel.hasId()) {
      getString(R.string.edit_task)
    } else {
      getString(R.string.new_task)
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentGoogleTaskEditBinding {
    return FragmentGoogleTaskEditBinding.inflate(inflater, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the Google Task edit screen for id: ${viewModel.id}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initFields()

    binding.progressMessageView.text = getString(R.string.please_wait)

    addMenu(
      menuRes = R.menu.fragment_google_task_edit,
      onMenuItemListener = { menuItem ->
        return@addMenu when (menuItem.itemId) {
          R.id.action_delete -> {
            deleteDialog()
            true
          }
          R.id.action_move -> {
            doIfPossible { selectList(true) }
            true
          }
          R.id.action_add -> {
            doIfPossible { saveTask() }
            true
          }
          else -> false
        }
      },
      menuModifier = { menu ->
        menu.getItem(1).isVisible = viewModel.editedTask != null
        menu.getItem(2).isVisible = viewModel.editedTask != null
      }
    )

    initViewModel()
    viewModel.onCreated(arguments, savedInstanceState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    hideKeyboard()
    appWidgetUpdater.updateScheduleWidget()
  }

  private fun initViewModel() {
    viewModel.isInProgress.nonNullObserve(viewLifecycleOwner) {
      binding.progressView.visibleGone(it)
    }
    viewModel.result.nonNullObserve(viewLifecycleOwner) { commands ->
      when (commands) {
        Commands.SAVED, Commands.DELETED -> moveBack()
        else -> {
        }
      }
    }
    viewModel.task.nullObserve(viewLifecycleOwner) { showTask(it) }
    viewModel.timeState.nonNullObserve(viewLifecycleOwner) { showTimeState(it) }
    viewModel.dateState.nonNullObserve(viewLifecycleOwner) { showDateState(it) }
    viewModel.taskList.nonNullObserve(viewLifecycleOwner) { showTaskList(it) }
    viewModel.toast.nonNullObserve(viewLifecycleOwner) { toast(it) }

    lifecycle.addObserver(viewModel)
  }

  private fun showTaskList(googleTaskList: GoogleTaskList) {
    binding.listText.text = googleTaskList.title
  }

  private fun showTask(googleTask: GoogleTask) {
    if (binding.editField.trimmedText().isEmpty()) {
      binding.editField.setText(googleTask.title)
    }
    if (binding.detailsField.trimmedText().isEmpty()) {
      googleTask.notes
        .takeIf { it.isNotEmpty() }
        ?.also {
          binding.detailsField.setText(it)
          binding.detailsField.setSelection(binding.detailsField.trimmedText().length)
        }
    }
    invalidateOptionsMenu()
  }

  private fun initFields() {
    binding.listText.setOnClickListener { doIfPossible { selectList(false) } }
    binding.dateField.setOnClickListener { selectDateAction(1) }
    binding.timeField.setOnClickListener { selectDateAction(2) }
  }

  private fun selectDateAction(type: Int) {
    val builder = dialogues.getMaterialDialog(requireContext())
    val types = if (type == 2) {
      arrayOf(getString(R.string.no_time), getString(R.string.select_time))
    } else {
      arrayOf(getString(R.string.no_date), getString(R.string.select_date))
    }
    val adapter = ArrayAdapter(
      requireContext(),
      android.R.layout.simple_list_item_single_choice,
      types
    )
    var selection = 0
    if (type == 1 && viewModel.isDateSelected()) {
      selection = 1
    }
    if (type == 2 && viewModel.isTimeSelected()) {
      selection = 1
    }
    builder.setSingleChoiceItems(adapter, selection) { dialog, which ->
      if (which != -1) {
        dialog.dismiss()
        if (type == 1) {
          when (which) {
            0 -> viewModel.onDateStateChanged(false)
            1 -> {
              viewModel.onDateStateChanged(true)
              dateDialog()
            }
          }
        }
        if (type == 2) {
          when (which) {
            0 -> viewModel.onTimeStateChanged(false)
            1 -> {
              viewModel.onTimeStateChanged(true)
              timeDialog()
            }
          }
        }
      }
    }
    builder.create().show()
  }

  private fun showDateState(dateState: EditGoogleTaskViewModel.DateState) {
    Logger.d(TAG, "Show date state: $dateState")
    when (dateState) {
      is EditGoogleTaskViewModel.DateState.SelectedDate -> {
        binding.dateField.text = dateState.formattedDate
      }

      is EditGoogleTaskViewModel.DateState.NoDate -> {
        binding.dateField.text = getString(R.string.no_date)
      }
    }
    binding.timeContainer.visibleGone(dateState is EditGoogleTaskViewModel.DateState.SelectedDate)
  }

  private fun showTimeState(timeState: EditGoogleTaskViewModel.TimeState) {
    Logger.d(TAG, "Show time state: $timeState")
    when (timeState) {
      is EditGoogleTaskViewModel.TimeState.SelectedTime -> {
        binding.timeField.text = timeState.formattedTime
      }

      is EditGoogleTaskViewModel.TimeState.NoTime -> {
        binding.timeField.text = getString(R.string.no_time)
      }
    }
  }

  private fun selectList(move: Boolean) {
    val list = viewModel.googleTaskLists
    if (list.isEmpty()) return
    val names = mutableListOf<String>()
    var position = 0
    list.forEachIndexed { index, googleTaskList ->
      names.add(googleTaskList.title)
      if (googleTaskList.listId.isNotEmpty() && googleTaskList.listId == viewModel.listId) {
        position = index
      }
    }
    dialogues.getMaterialDialog(requireContext())
      .setTitle(R.string.choose_list)
      .setSingleChoiceItems(names.toTypedArray(), position) { dialog, which ->
        dialog.dismiss()
        if (move) {
          viewModel.moveTask(list[which].listId)
        } else {
          viewModel.onListSelected(list[which].listId)
        }
      }
      .create().show()
  }

  private fun saveTask() {
    val summary = binding.editField.trimmedText()
    if (summary.isEmpty()) {
      binding.editField.error = getString(R.string.must_be_not_empty)
      return
    }
    val note = binding.detailsField.trimmedText()
    viewModel.save(summary = summary, note = note)
  }

  private fun deleteDialog() {
    doIfPossible {
      dialogues.getMaterialDialog(requireContext())
        .setMessage(getString(R.string.delete_this_task))
        .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
          dialog.dismiss()
          deleteTask()
        }
        .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        .create().show()
    }
  }

  private fun deleteTask() {
    doIfPossible { viewModel.editedTask?.let { viewModel.deleteGoogleTask(it) } }
  }

  private fun dateDialog() {
    dateTimePickerProvider.showDatePicker(
      fragmentManager = childFragmentManager,
      date = viewModel.date,
      title = getString(R.string.select_date)
    ) { viewModel.onDateSet(it) }
  }

  private fun timeDialog() {
    dateTimePickerProvider.showTimePicker(
      fragmentManager = childFragmentManager,
      time = viewModel.time,
      title = getString(R.string.select_time)
    ) { viewModel.onTimeSet(it) }
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
