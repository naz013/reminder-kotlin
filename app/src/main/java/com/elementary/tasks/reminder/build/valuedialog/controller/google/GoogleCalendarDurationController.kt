package com.elementary.tasks.reminder.build.valuedialog.controller.google

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.views.common.ValueAndTypePickerView
import com.elementary.tasks.databinding.BuilderItemCalendarDurationBinding
import com.elementary.tasks.reminder.build.GoogleCalendarDurationBuilderItem
import com.elementary.tasks.reminder.build.bi.CalendarDuration
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class GoogleCalendarDurationController(
  durationBuilderItem: GoogleCalendarDurationBuilderItem,
  private val dateTimeManager: DateTimeManager
) : AbstractBindingValueController<CalendarDuration, BuilderItemCalendarDurationBinding>(
  durationBuilderItem
) {

  override fun isDraggable(): Boolean {
    return false
  }

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemCalendarDurationBinding {
    return BuilderItemCalendarDurationBinding.inflate(layoutInflater, parent, false)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onViewCreated() {
    super.onViewCreated()
    binding.allDaySwitchWrapper.setOnClickListener {
      binding.allDaySwitch.toggle()
      notifyUpdate()
    }
    binding.allDaySwitch.setOnCheckedChangeListener { _, isChecked ->
      notifyUpdate()
      binding.valueAndTypePickerView.isEnabled = !isChecked
    }

    binding.valueAndTypePickerView.setOnTouchListener { v, event ->
      v.parent.requestDisallowInterceptTouchEvent(true)
      v.onTouchEvent(event)
      true
    }

    binding.valueAndTypePickerView.onChangedListener =
      object : ValueAndTypePickerView.OnChangedListener {
        override fun onChanged(value: String, typeIndex: Int) {
          notifyUpdate()
        }
      }
    binding.valueAndTypePickerView.setItems(getSelectionItems())
  }

  override fun onDataChanged(data: CalendarDuration?) {
    super.onDataChanged(data)
    parseValueAndType(data)
  }

  private fun notifyUpdate() {
    val value = binding.valueAndTypePickerView.value
    val typeIndex = binding.valueAndTypePickerView.typeIndex
    val isChecked = binding.allDaySwitch.isChecked
    updateValue(convertToValue(value, typeIndex, isChecked))
  }

  private fun parseValueAndType(value: CalendarDuration?) {
    if (value == null) {
      binding.valueAndTypePickerView.setValue("")
      binding.valueAndTypePickerView.setTypeSelection(0)
      binding.allDaySwitch.isChecked = false
    } else {
      val parsedTime = dateTimeManager.parseRepeatTime(value.millis)
      binding.valueAndTypePickerView.setValue(parsedTime.value.toString())
      binding.valueAndTypePickerView.setTypeSelection(parsedTime.type.index)
      binding.allDaySwitch.isChecked = value.allDay
    }
    binding.valueAndTypePickerView.isEnabled = !binding.allDaySwitch.isChecked
  }

  private fun getSelectionItems(): List<String> {
    return getContext().resources.getStringArray(R.array.repeat_times).toList()
  }

  private fun convertToValue(value: String, typeIndex: Int, allDay: Boolean): CalendarDuration? {
    val long = runCatching { value.toLong() }.getOrNull() ?: return null
    return CalendarDuration(
      allDay = allDay,
      millis = long * getMultiplier(typeIndex)
    )
  }

  private fun getMultiplier(index: Int): Long {
    return when (index) {
      DateTimeManager.MultiplierType.MINUTE.index -> DateTimeManager.MINUTE
      DateTimeManager.MultiplierType.HOUR.index -> DateTimeManager.HOUR
      DateTimeManager.MultiplierType.DAY.index -> DateTimeManager.DAY
      DateTimeManager.MultiplierType.WEEK.index -> DateTimeManager.DAY * 7
      DateTimeManager.MultiplierType.MONTH.index -> DateTimeManager.DAY * 30
      else -> DateTimeManager.SECOND
    }
  }
}
