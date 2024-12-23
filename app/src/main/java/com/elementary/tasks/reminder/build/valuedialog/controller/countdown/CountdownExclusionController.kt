package com.elementary.tasks.reminder.build.valuedialog.controller.countdown

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.elementary.tasks.R
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.databinding.BuilderItemCountdownExclusionBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.bi.TimerExclusion
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractSelectableArrayController
import org.threeten.bp.LocalTime

class CountdownExclusionController(
  builderItem: BuilderItem<TimerExclusion>,
  private val fragment: Fragment,
  private val dateTimeManager: DateTimeManager,
  private val dateTimePickerProvider: DateTimePickerProvider
) : AbstractBindingValueController<TimerExclusion, BuilderItemCountdownExclusionBinding>(
  builderItem
) {

  private val arrayAdapter by lazy {
    AbstractSelectableArrayController.ArrayAdapter(
      getAdapterData(),
      true
    ) { updateValue(it) }
  }
  private var fromTime: LocalTime = LocalTime.now()
  private var toTime: LocalTime = fromTime.plusHours(3)

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemCountdownExclusionBinding {
    return BuilderItemCountdownExclusionBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.hoursListView.layoutManager = GridLayoutManager(getContext(), 8)
    binding.hoursListView.adapter = arrayAdapter

    showFromTime(binding.from, fromTime)
    showToTime(binding.to, toTime)

    binding.from.setOnClickListener { fromTime(binding.from) }
    binding.to.setOnClickListener { toTime(binding.to) }
  }

  override fun onDataChanged(data: TimerExclusion?) {
    super.onDataChanged(data)
    data?.also {
      if (it.hours.isEmpty()) {
        fromTime = dateTimeManager.toLocalTime(it.from) ?: LocalTime.now()
        toTime = dateTimeManager.toLocalTime(it.to) ?: fromTime.plusHours(3)
        binding.selectInterval.isChecked = true
      } else {
        binding.selectHours.isChecked = true
      }
    } ?: run {
      binding.selectInterval.isChecked = true
    }
  }

  @SuppressLint("SetTextI18n")
  private fun fromTime(textView: TextView) {
    dateTimePickerProvider.showTimePicker(
      fragmentManager = fragment.childFragmentManager,
      time = fromTime,
      title = getContext().getString(R.string.from)
    ) {
      fromTime = it
      showFromTime(textView, it)
      notifyUpdate()
    }
  }

  @SuppressLint("SetTextI18n")
  private fun toTime(textView: TextView) {
    dateTimePickerProvider.showTimePicker(
      fragmentManager = fragment.childFragmentManager,
      time = toTime,
      title = getContext().getString(R.string.to)
    ) {
      toTime = it
      showToTime(textView, it)
      notifyUpdate()
    }
  }

  private fun updateValue(
    selectedItems: List<AbstractSelectableArrayController.SimpleSelectableValue<Int>>
  ) {
    val oldValue = builderItem.modifier.getValue() ?: TimerExclusion(emptyList(), "", "")
    val hours = selectedItems.map { it.value }
    builderItem.modifier.update(oldValue.copy(hours = hours))
    notifyUpdate()
  }

  private fun showFromTime(textView: TextView, time: LocalTime) {
    showTime(textView, getContext().getString(R.string.from), time)
  }

  private fun showToTime(textView: TextView, time: LocalTime) {
    showTime(textView, getContext().getString(R.string.to), time)
  }

  @SuppressLint("SetTextI18n")
  private fun showTime(textView: TextView, prefix: String, time: LocalTime) {
    textView.text = "$prefix ${dateTimeManager.getTime(time)}"
  }

  private fun getAdapterData(): List<AbstractSelectableArrayController.SimpleSelectableValue<Int>> {
    val selectedDays = builderItem.modifier.getValue()?.hours?.associateBy { it } ?: emptyMap()
    return (0..23).map {
      AbstractSelectableArrayController.SimpleSelectableValue(
        value = it,
        uiValue = "$it",
        selectionState = selectedDays.containsKey(it)
      )
    }
  }

  private fun notifyUpdate() {
    val oldValue = builderItem.modifier.getValue() ?: TimerExclusion(emptyList(), "", "")
    if (binding.selectHours.isChecked) {
      builderItem.modifier.update(
        oldValue.copy(
          from = "",
          to = "",
          hours = arrayAdapter.getSelected().map { it.value }
        )
      )
    } else if (binding.selectInterval.isChecked) {
      builderItem.modifier.update(
        oldValue.copy(
          from = dateTimeManager.to24HourString(fromTime),
          to = dateTimeManager.to24HourString(toTime),
          hours = emptyList()
        )
      )
    } else {
      builderItem.modifier.update(null)
    }
  }
}
