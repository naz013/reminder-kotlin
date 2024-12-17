package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.databinding.ViewCalendarExportBinding
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ExportToCalendarView : LinearLayout, KoinComponent {

  private val calendars = if (isInEditMode) {
    emptyList()
  } else {
    val googleCalendarUtils by inject<GoogleCalendarUtils>()
    googleCalendarUtils.getCalendarsList()
  }

  private lateinit var binding: ViewCalendarExportBinding
  private var internalState: State = State.NO

  var listener: SelectionListener? = null

  var calendarState: State
    get() = internalState
    set(value) {
      selectButton(value)
    }

  var calendarId: Long
    get() = calendars[binding.calendarSelector.selectedPosition].id
    set(value) {
      var index = 0
      for (c in calendars) {
        if (c.id == value) {
          index = calendars.indexOf(c)
          break
        }
      }
      binding.calendarSelector.selectItem(index)
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

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_calendar_export, this)
    orientation = VERTICAL
    binding = ViewCalendarExportBinding.bind(this)

    binding.calendarSelector.pickerProvider = {
      calendars.map { calendarItem -> calendarItem.name }
    }
    binding.calendarSelector.titleProvider = { pointer -> calendars[pointer].name }
    binding.calendarSelector.dataSize = calendars.size
    binding.calendarSelector.selectListener = { pointer, _ ->
      listener?.onChanged(internalState == State.YES, calendars[pointer].id)
    }

    binding.calendarOptionGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
      if (isChecked) {
        if (checkedId == R.id.calendarDisabled) {
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
      State.NO -> R.id.calendarDisabled
      State.YES -> R.id.calendarEnabled
    }
    binding.calendarOptionGroup.check(buttonId)
  }

  private fun setState(state: State) {
    Logger.d("setState: $state")
    this.internalState = state
    enableViews(state != State.NO)
    listener?.onChanged(state == State.YES, calendarId)
  }

  private fun enableViews(isEnabled: Boolean) {
    binding.calendarSelector.isEnabled = isEnabled
  }

  interface SelectionListener {
    fun onChanged(enabled: Boolean, calendarId: Long)
  }

  enum class State { NO, YES }
}
