package com.elementary.tasks.googletasks.task

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.deeplink.DeepLinkDataParser
import com.elementary.tasks.core.os.toast
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.nullObserve
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.applyBottomInsets
import com.elementary.tasks.core.utils.ui.applyTopInsets
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.databinding.ActivityCreateGoogleTaskBinding
import com.elementary.tasks.googletasks.TasksConstants
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.logging.Logger
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class GoogleTaskActivity : BindingActivity<ActivityCreateGoogleTaskBinding>() {

  private val dateTimePickerProvider by inject<DateTimePickerProvider>()
  private val viewModel by viewModel<GoogleTaskViewModel> { parametersOf(getId()) }

  override fun inflateBinding() = ActivityCreateGoogleTaskBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    if (!viewModel.isLogged) {
      finish()
      return
    }

    binding.scrollView.applyBottomInsets()

    initToolbar()
    initFields()

    binding.progressMessageView.text = getString(R.string.please_wait)

    if (savedInstanceState == null) {
      viewModel.action = intentString(TasksConstants.INTENT_ACTION).also {
        if (it.isEmpty()) viewModel.action = TasksConstants.CREATE
      }
      viewModel.initDefaults()
    }

    if (viewModel.action == TasksConstants.CREATE) {
      val tmp = if (savedInstanceState != null) {
        savedInstanceState.getString(ARG_LIST, "")
      } else {
        getId()
      }
      initViewModel(tmp)
      checkDeepLink()
    } else {
      initViewModel("")
    }
  }

  private fun checkDeepLink() {
    if (intent.getBooleanExtra(Constants.INTENT_DEEP_LINK, false)) {
      runCatching {
        val parser = DeepLinkDataParser()
        viewModel.initFromDeepLink(parser.readDeepLinkData(intent))
      }
    }
  }

  private fun getId() = intentString(Constants.INTENT_ID)

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putString(ARG_LIST, viewModel.listId)
    super.onSaveInstanceState(outState)
  }

  private fun initViewModel(listId: String) {
    viewModel.listId = listId
    viewModel.isInProgress.nonNullObserve(this) { binding.progressView.visibleGone(it) }
    viewModel.result.nonNullObserve(this) { commands ->
      when (commands) {
        Commands.SAVED, Commands.DELETED -> handleBackPress()
        else -> {
        }
      }
    }
    viewModel.googleTask.nullObserve(this) { editTask(it) }
    viewModel.googleTaskLists.nonNullObserve(this) { selectCurrent(it) }
    viewModel.defaultTaskList.observe(this) { googleTaskList ->
      if (googleTaskList != null && listId == "") {
        showTaskList(googleTaskList)
      }
    }

    viewModel.timeState.nonNullObserve(this) { showTimeState(it) }
    viewModel.dateState.nonNullObserve(this) { showDateState(it) }
    viewModel.taskList.nonNullObserve(this) { showTaskList(it) }

    lifecycle.addObserver(viewModel)
  }

  private fun showTaskList(googleTaskList: GoogleTaskList) {
    viewModel.listId = googleTaskList.listId
    binding.listText.text = googleTaskList.title
  }

  private fun selectCurrent(googleTaskLists: List<GoogleTaskList>) {
    for (googleTaskList in googleTaskLists) {
      if (googleTaskList.listId == viewModel.listId) {
        showTaskList(googleTaskList)
        break
      }
    }
  }

  private fun editTask(googleTask: GoogleTask) {
    viewModel.onEditTask(googleTask)
    binding.toolbar.setTitle(R.string.edit_task)
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
    updateMenu()
  }

  private fun initFields() {
    binding.listText.setOnClickListener { doIfPossible { selectList(false) } }
    binding.dateField.setOnClickListener { selectDateAction(1) }
    binding.timeField.setOnClickListener { selectDateAction(2) }
  }

  private fun initToolbar() {
    binding.appBar.applyTopInsets()
    binding.toolbar.setTitle(R.string.new_task)
    binding.toolbar.setNavigationOnClickListener { handleBackPress() }
    binding.toolbar.setOnMenuItemClickListener {
      when (it.itemId) {
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
    }
    updateMenu()
  }

  private fun updateMenu() {
    binding.toolbar.menu.also {
      it.getItem(1).isVisible = viewModel.editedTask != null
      it.getItem(2).isVisible = viewModel.editedTask != null
    }
  }

  private fun selectDateAction(type: Int) {
    val builder = dialogues.getMaterialDialog(this)
    val types = if (type == 2) {
      arrayOf(getString(R.string.no_time), getString(R.string.select_time))
    } else {
      arrayOf(getString(R.string.no_date), getString(R.string.select_date))
    }
    val adapter = ArrayAdapter(
      this,
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

  private fun showDateState(dateState: GoogleTaskViewModel.DateState) {
    Logger.d("showDateState: $dateState")
    when (dateState) {
      is GoogleTaskViewModel.DateState.SelectedDate -> {
        binding.dateField.text = dateState.formattedDate
      }

      is GoogleTaskViewModel.DateState.NoDate -> {
        binding.dateField.text = getString(R.string.no_date)
      }
    }
    binding.timeContainer.visibleGone(dateState is GoogleTaskViewModel.DateState.SelectedDate)
  }

  private fun showTimeState(timeState: GoogleTaskViewModel.TimeState) {
    Logger.d("showTimeState: $timeState")
    when (timeState) {
      is GoogleTaskViewModel.TimeState.SelectedTime -> {
        binding.timeField.text = timeState.formattedTime
      }

      is GoogleTaskViewModel.TimeState.NoTime -> {
        binding.timeField.text = getString(R.string.no_time)
      }
    }
  }

  private fun moveTask(listId: String) {
    viewModel.editedTask?.also {
      val initListId = it.listId
      if (!listId.matches(initListId.toRegex())) {
        it.listId = listId
        viewModel.moveGoogleTask(it, initListId)
      } else {
        toast(R.string.this_is_same_list)
      }
    }
  }

  private fun selectList(move: Boolean) {
    val list = viewModel.googleTaskLists.value.orEmpty()
    if (list.isEmpty()) return
    val names = mutableListOf<String>()
    var position = 0
    list.forEachIndexed { index, googleTaskList ->
      names.add(googleTaskList.title)
      if (googleTaskList.listId.isNotEmpty() && googleTaskList.listId == viewModel.listId) {
        position = index
      }
    }
    dialogues.getMaterialDialog(this)
      .setTitle(R.string.choose_list)
      .setSingleChoiceItems(names.toTypedArray(), position) { dialog, which ->
        dialog.dismiss()
        if (move) {
          moveTask(list[which].listId)
        } else {
          showTaskList(list[which])
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
      dialogues.getMaterialDialog(this)
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
      fragmentManager = supportFragmentManager,
      date = viewModel.date,
      title = getString(R.string.select_date)
    ) { viewModel.onDateSet(it) }
  }

  private fun timeDialog() {
    dateTimePickerProvider.showTimePicker(
      fragmentManager = supportFragmentManager,
      time = viewModel.time,
      title = getString(R.string.select_time)
    ) { viewModel.onTimeSet(it) }
  }

  override fun onDestroy() {
    super.onDestroy()
    runCatching {
      lifecycle.removeObserver(viewModel)
    }
    hideKeyboard()
    updatesHelper.updateTasksWidget()
  }

  override fun handleBackPress(): Boolean {
    doIfPossible { finish() }
    return true
  }

  private fun doIfPossible(f: () -> Unit) {
    if (viewModel.isInProgress.value == true) {
      toast(R.string.please_wait)
    } else {
      f.invoke()
    }
  }

  override fun requireLogin() = true

  companion object {
    private const val ARG_LIST = "arg_list"
  }
}
