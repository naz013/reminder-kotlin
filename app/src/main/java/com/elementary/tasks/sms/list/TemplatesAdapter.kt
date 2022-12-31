package com.elementary.tasks.sms.list

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.sms.UiSmsList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemMessageBinding
import com.elementary.tasks.sms.UiSmsListDiffCallback

internal class TemplatesAdapter : ListAdapter<UiSmsList, TemplatesAdapter.ViewHolder>(
  UiSmsListDiffCallback()
) {

  var actionsListener: ActionsListener<UiSmsList>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  internal inner class ViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemMessageBinding>(
    ListItemMessageBinding.inflate(parent.inflater(), parent, false)
  ) {
    fun bind(item: UiSmsList) {
      binding.messageView.text = item.text
    }

    init {
      binding.buttonMore.visibility = View.VISIBLE
      binding.clickView.setOnClickListener {
        actionsListener?.onAction(
          it,
          bindingAdapterPosition,
          getItem(bindingAdapterPosition),
          ListActions.OPEN
        )
      }
      binding.buttonMore.setOnClickListener {
        actionsListener?.onAction(
          it,
          bindingAdapterPosition,
          getItem(bindingAdapterPosition),
          ListActions.MORE
        )
      }
    }
  }
}
