package com.elementary.tasks.reminder.create.fragments.recur.preview

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.databinding.ListItemRecurPreviewBinding

class PreviewDataAdapter : ListAdapter<PreviewItem, PreviewDataAdapter.ViewHolder>(
  PreviewItemDiffCallback()
) {

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(parent = parent)
  }

  class ViewHolder(
    parent: ViewGroup,
    private val binding: ListItemRecurPreviewBinding = ListItemRecurPreviewBinding.inflate(
      /* inflater = */ LayoutInflater.from(parent.context),
      /* parent = */ parent,
      /* attachToParent = */ false
    )
  ) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: PreviewItem) {
      binding.textView.text = item.text
      when (item.style) {
        Style.DISABLED -> {
          binding.textView.setTypeface(binding.textView.typeface, Typeface.NORMAL)
          binding.textView.isEnabled = false
        }
        Style.BOLD -> {
          binding.textView.setTypeface(binding.textView.typeface, Typeface.BOLD)
          binding.textView.isEnabled = true
        }
        Style.NORMAL -> {
          binding.textView.setTypeface(binding.textView.typeface, Typeface.NORMAL)
          binding.textView.isEnabled = true
        }
      }
    }
  }
}
