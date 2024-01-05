package com.elementary.tasks.reminder.build.valuedialog.controller.countdown

import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.core.views.TimerPickerView
import com.elementary.tasks.databinding.BuilderItemCountdownTimerBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class CountdownTimeController(
  builderItem: BuilderItem<Long>
) : AbstractBindingValueController<Long, BuilderItemCountdownTimerBinding>(builderItem) {

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemCountdownTimerBinding {
    return BuilderItemCountdownTimerBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.timerPickerView.setListener(object : TimerPickerView.TimerListener {
      override fun onTimerChange(time: Long) {
        if (time == 0L) {
          updateValue(null)
        } else {
          updateValue(time)
        }
      }
    })
  }

  override fun onDataChanged(data: Long?) {
    super.onDataChanged(data)
    data?.also { binding.timerPickerView.timerValue = it }
  }
}
