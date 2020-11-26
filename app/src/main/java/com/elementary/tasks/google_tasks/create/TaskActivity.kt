package com.elementary.tasks.google_tasks.create

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.TimeUtil.fromGmt
import com.elementary.tasks.core.utils.TimeUtil.toGmt
import com.elementary.tasks.core.utils.TimeUtil.toGoogleTaskDate
import com.elementary.tasks.core.utils.TimeUtil.toTime
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.normalizeSummary
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.trimmedText
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskViewModel
import com.elementary.tasks.databinding.ActivityCreateGoogleTaskBinding
import com.github.naz013.calendarext.dropMilliseconds
import com.github.naz013.calendarext.dropSeconds
import com.github.naz013.calendarext.newCalendar
import com.github.naz013.calendarext.takeTimeFrom
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

class TaskActivity : BindingActivity<ActivityCreateGoogleTaskBinding>() {

  private val stateViewModel by viewModel<GoogleTasksStateViewModel>()
  private val viewModel by viewModel<GoogleTaskViewModel> { parametersOf(getId()) }

  override fun inflateBinding() = ActivityCreateGoogleTaskBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (!viewModel.isLogged) {
      finish()
      return
    }

    initToolbar()
    initFields()

    binding.progressMessageView.text = getString(R.string.please_wait)
    updateProgress(false)

    if (savedInstanceState == null) {
      stateViewModel.action = intentString(TasksConstants.INTENT_ACTION).also {
        if (it.isEmpty()) stateViewModel.action = TasksConstants.CREATE
      }
      initDefaults()
    } else {
      updateProgress(savedInstanceState.getBoolean(ARG_LOADING, false))
    }

    if (stateViewModel.action == TasksConstants.CREATE) {
      val tmp = if (savedInstanceState != null) {
        savedInstanceState.getString(ARG_LIST, "")
      } else getId()
      initViewModel(tmp)
    } else {
      initViewModel("")
    }
  }

  private fun getId() = intentString(Constants.INTENT_ID)

  override fun onStart() {
    super.onStart()
    observeStates()
  }

  private fun observeStates() {
    stateViewModel.time.observe(this, { switchDate() })
    stateViewModel.date.observe(this, { switchDate() })
    stateViewModel.isDateEnabled.observe(this, { switchDate(isDate = it) })
    stateViewModel.isReminder.observe(this, { switchDate(isReminder = it) })
    stateViewModel.reminderValue.observe(this, { showReminder(it) })
  }

  private fun initDefaults() {
    stateViewModel.time.postValue(newCalendar())
    stateViewModel.date.postValue(newCalendar())
  }

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putString(ARG_LIST, stateViewModel.listId)
    outState.putBoolean(ARG_LOADING, stateViewModel.isLoading)
    super.onSaveInstanceState(outState)
  }

  private fun updateProgress(b: Boolean) {
    stateViewModel.isLoading = b
    binding.progressView.visibleGone(b)
  }

  private fun initViewModel(listId: String) {
    stateViewModel.listId = listId
    viewModel.isInProgress.observe(this) { updateProgress(it) }
    viewModel.result.observe(this) { commands ->
      when (commands) {
        Commands.SAVED, Commands.DELETED -> onBackPressed()
        else -> {
        }
      }
    }
    viewModel.googleTask.observe(this) { editTask(it) }
    viewModel.googleTaskLists.observe(this) { selectCurrent(it) }
    viewModel.defaultTaskList.observe(this) { googleTaskList ->
      if (googleTaskList != null && listId == "") {
        showTaskList(googleTaskList)
      }
    }
    viewModel.reminder.observe(this) { reminder ->
      if (reminder != null) {
        if (!stateViewModel.isReminderEdited) {
          stateViewModel.reminderValue.postValue(reminder)
          stateViewModel.isReminderEdited = true
        }
      }
    }
    lifecycle.addObserver(stateViewModel)
  }

  private fun showReminder(reminder: Reminder) {
    stateViewModel.time.postValue(reminder.eventTime.fromGmt())
    stateViewModel.isReminder.postValue(true)
  }

  private fun showTaskList(googleTaskList: GoogleTaskList) {
    stateViewModel.listId = googleTaskList.listId
    binding.listText.text = googleTaskList.title
  }

  private fun selectCurrent(googleTaskLists: List<GoogleTaskList>) {
    for (googleTaskList in googleTaskLists) {
      if (googleTaskList.listId == stateViewModel.listId) {
        showTaskList(googleTaskList)
        break
      }
    }
  }

  private fun editTask(googleTask: GoogleTask) {
    stateViewModel.editedItem = googleTask
    stateViewModel.listId = googleTask.listId
    binding.toolbar.setTitle(R.string.edit_task)
    if (!stateViewModel.isEdited) {
      binding.editField.setText(googleTask.title)
      googleTask.notes
        .takeIf { it.isNotEmpty() }
        ?.also {
          binding.detailsField.setText(it)
          binding.detailsField.setSelection(binding.detailsField.trimmedText().length)
        }
      googleTask.dueDate
        .takeIf { it != 0L }
        ?.also {
          stateViewModel.date.postValue(newCalendar(it))
          stateViewModel.isDateEnabled.postValue(true)
        }
      viewModel.googleTaskLists.value?.forEach {
        if (it.listId == googleTask.listId) {
          showTaskList(it)
          return@forEach
        }
      }
      stateViewModel.isEdited = true
    }
    viewModel.loadReminder(googleTask.uuId)
  }

  private fun initFields() {
    binding.listText.setOnClickListener { doIfPossible { selectList(false) } }
    binding.dateField.setOnClickListener { selectDateAction(1) }
    binding.timeField.setOnClickListener { selectDateAction(2) }
  }

  private fun initToolbar() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeButtonEnabled(true)
    supportActionBar?.setDisplayShowHomeEnabled(true)
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
    binding.toolbar.setTitle(R.string.new_task)
  }

  private fun selectDateAction(type: Int) {
    val builder = dialogues.getMaterialDialog(this)
    var types = arrayOf(getString(R.string.no_date), getString(R.string.select_date))
    if (type == 2) {
      types = arrayOf(getString(R.string.no_reminder), getString(R.string.select_time))
    }
    val adapter = ArrayAdapter(this,
      android.R.layout.simple_list_item_single_choice, types)
    var selection = 0
    if (type == 1) {
      if (isDate()) selection = 1
    }
    if (type == 2) {
      if (isReminder()) selection = 1
    }
    builder.setSingleChoiceItems(adapter, selection) { dialog, which ->
      if (which != -1) {
        dialog.dismiss()
        if (type == 1) {
          when (which) {
            0 -> stateViewModel.isDateEnabled.postValue(false)
            1 -> {
              stateViewModel.isDateEnabled.postValue(true)
              dateDialog()
            }
          }
        }
        if (type == 2) {
          when (which) {
            0 -> stateViewModel.isReminder.postValue(false)
            1 -> {
              stateViewModel.isReminder.postValue(true)
              timeDialog()
            }
          }
        }
      }
    }
    builder.create().show()
  }

  private fun isDate(): Boolean = stateViewModel.isDateEnabled.value ?: false

  private fun isReminder(): Boolean = stateViewModel.isReminder.value ?: false

  private fun switchDate(isDate: Boolean = isDate(), isReminder: Boolean = isReminder()) {
    if (!isDate) {
      binding.dateField.text = getString(R.string.no_date)
    } else {
      showDate()
    }
    if (!isReminder) {
      binding.timeField.text = getString(R.string.no_reminder)
    } else {
      showTime()
    }
  }

  private fun showDate() {
    binding.dateField.text = stateViewModel.takeDate().toGoogleTaskDate(prefs.appLanguage)
  }

  private fun showTime() {
    binding.timeField.text = stateViewModel.takeTime().toTime(
      prefs.is24HourFormat,
      prefs.appLanguage
    )
  }

  private fun moveTask(listId: String) {
    stateViewModel.editedItem?.also {
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
    var list = viewModel.googleTaskLists.value
    if (list == null) list = ArrayList()
    val names = ArrayList<String>()
    var position = 0
    for (i in list.indices) {
      val item = list[i]
      names.add(item.title)
      if (stateViewModel.listId != "" && item.listId != "" && item.listId.matches(stateViewModel.listId.toRegex())) {
        position = i
      }
    }
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(R.string.choose_list)
    val finalList = list
    builder.setSingleChoiceItems(names.toTypedArray(), position) { dialog, which ->
      dialog.dismiss()
      if (move) {
        moveTask(finalList[which].listId)
      } else {
        showTaskList(finalList[which])
      }
    }
    builder.create().show()
  }

  private fun saveTask() {
    val summary = binding.editField.trimmedText()
    if (summary.isEmpty()) {
      binding.editField.error = getString(R.string.must_be_not_empty)
      return
    }
    val note = binding.detailsField.trimmedText()
    val reminder = createReminder(summary).takeIf { isReminder() }
    val item = stateViewModel.editedItem
    if (stateViewModel.action == TasksConstants.EDIT && item != null) {
      val initListId = item.listId
      item.update(summary, note, reminder)
      if (stateViewModel.listId.isNotEmpty()) {
        viewModel.updateAndMoveGoogleTask(item, initListId, reminder)
      } else {
        viewModel.updateGoogleTask(item, reminder)
      }
    } else {
      viewModel.newGoogleTask(GoogleTask().update(summary, note, reminder), reminder)
    }
  }

  private fun GoogleTask.update(
    summary: String,
    note: String,
    reminder: Reminder?
  ) =
    this.apply {
      listId = stateViewModel.listId
      status = GTasks.TASKS_NEED_ACTION
      title = summary
      notes = note
      dueDate = stateViewModel.takeDate().takeIf { isDate() }?.timeInMillis ?: 0L
      uuId = reminder?.uuId ?: ""
    }

  private fun dateTime() =
    stateViewModel.takeDate()
      .takeTimeFrom(stateViewModel.takeTime())
      .apply {
        dropSeconds()
        dropMilliseconds()
      }

  private fun createReminder(task: String) = Reminder().apply {
    type = Reminder.BY_DATE
    delay = 0
    eventCount = 0
    useGlobal = true
    isActive = true
    isRemoved = false
    summary = task.normalizeSummary()
    startTime = dateTime().toGmt()
    eventTime = startTime
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
    doIfPossible { stateViewModel.editedItem?.let { viewModel.deleteGoogleTask(it) } }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_create_task, menu)
    stateViewModel.editedItem?.also {
      menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_task)
      menu.add(Menu.NONE, MENU_ITEM_MOVE, 100, R.string.move_to_another_list)
    }
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      MENU_ITEM_DELETE -> {
        deleteDialog()
        return true
      }
      MENU_ITEM_MOVE -> {
        doIfPossible { selectList(true) }
        return true
      }
      R.id.action_add -> {
        doIfPossible { saveTask() }
        return true
      }
      android.R.id.home -> {
        onBackPressed()
        return true
      }
      else -> return super.onOptionsItemSelected(item)
    }
  }

  private fun dateDialog() {
    TimeUtil.showDatePicker(this, prefs, stateViewModel.takeDate()) {
      stateViewModel.date.postValue(it)
    }
  }

  private fun timeDialog() {
    TimeUtil.showTimePicker(this, prefs.is24HourFormat, stateViewModel.takeTime()) {
      stateViewModel.time.postValue(it)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    try {
      lifecycle.removeObserver(stateViewModel)
      lifecycle.removeObserver(viewModel)
    } catch (e: Exception) {
    }
    hideKeyboard()
    UpdatesHelper.updateTasksWidget(this)
  }

  override fun onBackPressed() {
    doIfPossible {
      try {
        super.onBackPressed()
      } catch (e: Exception) {
      }
    }
  }

  private fun doIfPossible(f: () -> Unit) {
    if (stateViewModel.isLoading) {
      toast(R.string.please_wait)
    } else {
      f.invoke()
    }
  }

  override fun requireLogin() = true

  companion object {
    private const val MENU_ITEM_DELETE = 12
    private const val MENU_ITEM_MOVE = 14
    private const val ARG_LOGGED = "arg_logged"
    private const val ARG_LIST = "arg_list"
    private const val ARG_LOADING = "arg_loading"

    fun openLogged(context: Context, intent: Intent? = null) {
      if (intent == null) {
        context.startActivity(Intent(context, TaskActivity::class.java)
          .putExtra(ARG_LOGGED, true))
      } else {
        intent.putExtra(ARG_LOGGED, true)
        context.startActivity(intent)
      }
    }
  }
}
