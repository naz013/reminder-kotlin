package com.elementary.tasks.sms

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.sms.UiSmsList
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemMessageBinding

class SelectableTemplatesAdapter : ListAdapter<UiSmsList, SelectableTemplatesAdapter.ViewHolder>(
  UiSmsListDiffCallback()
) {

  private var selectedPosition = -1

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(parent)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position), selectedPosition == position)
  }

  inner class ViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemMessageBinding>(
    ListItemMessageBinding.inflate(parent.inflater(), parent, false)
  ) {
    fun bind(item: UiSmsList, isSelected: Boolean) {
      binding.messageView.text = item.text
      if (isSelected) {
        binding.bgView.setBackgroundColor(
          ThemeProvider.colorWithAlpha(
            ThemeProvider.getThemeSecondaryColor(itemView.context),
            50
          )
        )
      } else {
        binding.bgView.setBackgroundResource(android.R.color.transparent)
      }
    }

    init {
      binding.clickView.setOnClickListener { selectItem(bindingAdapterPosition) }
      binding.buttonMore.gone()
    }
  }

  fun getSelectedItem(): UiSmsList? {
    return if (selectedPosition in 0 until itemCount) {
      getItem(selectedPosition)
    } else {
      null
    }
  }

  fun selectItem(position: Int) {
    if (position == selectedPosition) return
    val oldSelected = selectedPosition
    selectedPosition = -1
    if (oldSelected != -1 && oldSelected < itemCount) {
      notifyItemChanged(oldSelected)
    }
    if (position != -1 && position < itemCount) {
      selectedPosition = position
      notifyItemChanged(position)
    }
  }
}
