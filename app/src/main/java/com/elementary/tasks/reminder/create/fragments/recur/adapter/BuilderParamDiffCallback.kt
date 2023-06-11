package com.elementary.tasks.reminder.create.fragments.recur.adapter

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.reminder.create.fragments.recur.UiBuilderParam
import java.util.Objects

class BuilderParamDiffCallback : DiffUtil.ItemCallback<UiBuilderParam<*>>() {

  override fun areItemsTheSame(oldItem: UiBuilderParam<*>, newItem: UiBuilderParam<*>): Boolean {
    return oldItem.param.recurParamType == newItem.param.recurParamType
  }

  override fun areContentsTheSame(oldItem: UiBuilderParam<*>, newItem: UiBuilderParam<*>): Boolean {
    return Objects.equals(oldItem.param.value, newItem.param.value)
  }
}
