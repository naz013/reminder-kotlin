package com.elementary.tasks.reminder.create.fragments.recur.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.utils.datetime.recurrence.RecurParamType
import com.elementary.tasks.core.utils.visibleInvisible
import com.elementary.tasks.databinding.ListItemRecurBuilderBinding
import com.elementary.tasks.reminder.create.fragments.recur.UiBuilderParam

class ParamBuilderAdapter(
  private val onItemClickListener: OnItemClickListener,
  private val onItemRemoveListener: OnItemRemoveListener
) : ListAdapter<UiBuilderParam<*>, ParamBuilderAdapter.ViewHolder>(BuilderParamDiffCallback()) {

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(
      parent = parent,
      clickListener = { onItemClickListener.onItemClicked(it, getItem(it)) },
      removeListener = { onItemRemoveListener.onItemRemoved(it, getItem(it)) }
    )
  }

  class ViewHolder(
    parent: ViewGroup,
    clickListener: (Int) -> Unit,
    removeListener: (Int) -> Unit,
    private val binding: ListItemRecurBuilderBinding = ListItemRecurBuilderBinding.inflate(
      /* inflater = */ LayoutInflater.from(parent.context),
      /* parent = */ parent,
      /* attachToParent = */ false
    )
  ) : RecyclerView.ViewHolder(binding.root) {

    init {
      binding.clickView.setOnClickListener { clickListener(bindingAdapterPosition) }
      binding.removeButton.setOnClickListener { removeListener(bindingAdapterPosition) }
    }

    fun bind(param: UiBuilderParam<*>) {
      binding.nameView.text = param.text
      binding.removeButton.visibleInvisible(param.param.recurParamType != RecurParamType.COUNT)
    }
  }

  interface OnItemClickListener {
    fun onItemClicked(position: Int, param: UiBuilderParam<*>)
  }

  interface OnItemRemoveListener {
    fun onItemRemoved(position: Int, param: UiBuilderParam<*>)
  }
}
