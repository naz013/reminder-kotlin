package com.elementary.tasks.reminder.create.fragments.recur.intdialog

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.databinding.ListItemRecurIntDialogBinding
import com.elementary.tasks.reminder.create.fragments.recur.UiBuilderParam

class IntListAdapter(
  private val items: List<Number>
) : RecyclerView.Adapter<IntListAdapter.ViewHolder>() {

  fun getSelected(): List<Number> {
    return items.filter { it.isSelected }
  }

  @SuppressLint("NotifyDataSetChanged")
  fun clearSelection() {
    items.forEach {
      it.isSelected = false
    }
    notifyDataSetChanged()
  }

  @SuppressLint("NotifyDataSetChanged")
  fun selectAll() {
    items.forEach {
      it.isSelected = true
    }
    notifyDataSetChanged()
  }

  override fun getItemCount(): Int {
    return items.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(
      parent = parent,
      clickListener = { onItemClicked(it) }
    )
  }

  private fun onItemClicked(position: Int) {
    val item = items[position]
    item.isSelected = !item.isSelected
    notifyItemChanged(position)
  }

  class ViewHolder(
    parent: ViewGroup,
    clickListener: (Int) -> Unit,
    private val binding: ListItemRecurIntDialogBinding = ListItemRecurIntDialogBinding.inflate(
      /* inflater = */ LayoutInflater.from(parent.context),
      /* parent = */ parent,
      /* attachToParent = */ false
    )
  ) : RecyclerView.ViewHolder(binding.root) {

    init {
      binding.textView.setOnClickListener { clickListener(bindingAdapterPosition) }
    }

    fun bind(number: Number) {
      binding.textView.text = number.value.toString()
      if (number.isSelected) {
        binding.textView.setBackgroundResource(R.drawable.drawable_tertiary)
      } else {
        binding.textView.background = null
      }
    }
  }

  interface OnItemClickListener {
    fun onItemClicked(position: Int, param: UiBuilderParam<*>)
  }

  interface OnItemRemoveListener {
    fun onItemRemoved(position: Int, param: UiBuilderParam<*>)
  }
}
