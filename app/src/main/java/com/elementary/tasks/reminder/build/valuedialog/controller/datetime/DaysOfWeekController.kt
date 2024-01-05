package com.elementary.tasks.reminder.build.valuedialog.controller.datetime

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import com.elementary.tasks.core.protocol.WeekDaysProtocol
import com.elementary.tasks.core.utils.datetime.IntervalUtil
import com.elementary.tasks.databinding.BuilderItemDaysOfWeekBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class DaysOfWeekController(
  builderItem: BuilderItem<List<Int>>
) : AbstractBindingValueController<List<Int>, BuilderItemDaysOfWeekBinding>(builderItem) {

  private val mCheckListener: CompoundButton.OnCheckedChangeListener =
    CompoundButton.OnCheckedChangeListener { _, _ ->
      updateValue(calculateList())
    }

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemDaysOfWeekBinding {
    return BuilderItemDaysOfWeekBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.sundayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.saturdayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.fridayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.thursdayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.wednesdayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.tuesdayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.mondayCheck.setOnCheckedChangeListener(mCheckListener)

    binding.allDaysButton.setOnClickListener {
      setCheckForDays(getAllDaysChecked())
    }
    binding.workingDaysButton.setOnClickListener {
      setCheckForDays(getWorkingDaysChecked())
    }
  }

  override fun onDataChanged(data: List<Int>?) {
    super.onDataChanged(data)
    data?.takeIf { it.size == 7 }?.also {
      setCheckForDays(it)
    }
  }

  private fun setCheckForDays(weekdays: List<Int>) {
    binding.sundayCheck.isChecked = weekdays[0] == 1
    binding.mondayCheck.isChecked = weekdays[1] == 1
    binding.tuesdayCheck.isChecked = weekdays[2] == 1
    binding.wednesdayCheck.isChecked = weekdays[3] == 1
    binding.thursdayCheck.isChecked = weekdays[4] == 1
    binding.fridayCheck.isChecked = weekdays[5] == 1
    binding.saturdayCheck.isChecked = weekdays[6] == 1
  }

  private fun calculateList(): List<Int> {
    return IntervalUtil.getWeekRepeat(
      binding.mondayCheck.isChecked,
      binding.tuesdayCheck.isChecked,
      binding.wednesdayCheck.isChecked,
      binding.thursdayCheck.isChecked,
      binding.fridayCheck.isChecked,
      binding.saturdayCheck.isChecked,
      binding.sundayCheck.isChecked
    )
  }

  private fun getAllDaysChecked(): List<Int> {
    return WeekDaysProtocol.getAllDays()
  }

  private fun getWorkingDaysChecked(): List<Int> {
    return WeekDaysProtocol.getWorkDays()
  }
}
