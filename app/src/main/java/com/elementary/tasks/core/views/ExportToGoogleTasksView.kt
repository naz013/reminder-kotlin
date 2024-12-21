package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.databinding.ViewTasksExportBinding
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Deprecated("Should be removed")
class ExportToGoogleTasksView : LinearLayout, KoinComponent {

  private val googleTaskListRepository by inject<GoogleTaskListRepository>()
  private val viewScope = CoroutineScope(Dispatchers.Default)
  private var taskLists: List<GoogleTaskList> = emptyList()

  private lateinit var binding: ViewTasksExportBinding
  private var internalState: State = State.NO
  private var listIdToSelect: String? = null

  var listener: SelectionListener? = null

  var tasksState: State
    get() = internalState
    set(value) {
      selectButton(value)
    }

  var taskListId: String?
    get() {
      return try {
        taskLists[binding.selector.selectedPosition].listId
      } catch (t: Throwable) {
        null
      }
    }
    set(value) {
      listIdToSelect = value
      val index = taskLists.indexOfFirst { it.listId == value }
        .takeIf { it != -1 }
        ?: 0
      if (taskLists.isNotEmpty()) {
        binding.selector.selectItem(index)
      }
    }

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context)
  }

  private fun loadGoogleTaskLists() {
    viewScope.launch {
      taskLists = googleTaskListRepository.getAll()

      withContext(Dispatchers.Main) {
        binding.selector.pickerProvider = {
          taskLists.map { item -> item.title }
        }
        binding.selector.titleProvider = { pointer -> taskLists[pointer].title }
        binding.selector.dataSize = taskLists.size
        binding.selector.selectListener = { pointer, _ ->
          listener?.onChanged(internalState == State.YES, taskLists[pointer].listId)
        }
        listIdToSelect?.also { taskListId = it } ?: run { binding.selector.selectItem(0) }
      }
    }
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_tasks_export, this)
    orientation = VERTICAL
    binding = ViewTasksExportBinding.bind(this)

    if (!isInEditMode) {
      loadGoogleTaskLists()
    }

    binding.optionGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
      if (isChecked) {
        if (checkedId == R.id.tasksDisabled) {
          setState(State.NO)
        } else {
          setState(State.YES)
        }
      }
    }
    selectButton(State.NO)
    setState(State.NO)
  }

  private fun selectButton(state: State) {
    val buttonId = when (state) {
      State.NO -> R.id.tasksDisabled
      State.YES -> R.id.tasksEnabled
    }
    binding.optionGroup.check(buttonId)
  }

  private fun setState(state: State) {
    Logger.d("setState: $state")
    this.internalState = state
    enableViews(state != State.NO)
    taskListId?.also { listener?.onChanged(state == State.YES, it) }
  }

  private fun enableViews(isEnabled: Boolean) {
    binding.selector.isEnabled = isEnabled
  }

  interface SelectionListener {
    fun onChanged(enabled: Boolean, taskListId: String)
  }

  enum class State { NO, YES }
}
