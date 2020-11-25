package com.elementary.tasks.navigation.settings.additional

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemMessageBinding
import java.util.*

internal class TemplatesAdapter : RecyclerView.Adapter<TemplatesAdapter.ViewHolder>() {

  var data: List<SmsTemplate> = ArrayList()
    set(list) {
      field = list
      notifyDataSetChanged()
    }
  var actionsListener: ActionsListener<SmsTemplate>? = null

  override fun getItemCount() = data.size

  fun getItem(position: Int) = data[position]

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  internal inner class ViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemMessageBinding>(
    ListItemMessageBinding.inflate(parent.inflater(), parent, false)
  ) {
    fun bind(item: SmsTemplate) {
      binding.messageView.text = item.title
    }

    init {
      binding.buttonMore.visibility = View.VISIBLE
      binding.clickView.setOnClickListener {
        actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
      }
      binding.buttonMore.setOnClickListener {
        actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.MORE)
      }
    }
  }
}
