package com.elementary.tasks.core.apps

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.databinding.ListItemApplicationBinding

@Deprecated("After S")
class AppsRecyclerAdapter :
  ListAdapter<UiApplicationList, AppsRecyclerAdapter.ApplicationViewHolder>(
    UiApplicationListDiffCallback()
  ) {

  var actionsListener: ActionsListener<UiApplicationList>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ApplicationViewHolder(parent)

  override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class ApplicationViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemApplicationBinding>(
    ListItemApplicationBinding.inflate(parent.inflater(), parent, false)
  ) {
    fun bind(item: UiApplicationList) {
      binding.itemName.text = item.name
      binding.itemImage.setImageDrawable(item.drawable)
    }

    init {
      binding.clickView.setOnClickListener {
        try {
          actionsListener?.onAction(
            it,
            bindingAdapterPosition,
            getItem(bindingAdapterPosition),
            ListActions.OPEN
          )
        } catch (e: Exception) {
          actionsListener?.onAction(it, bindingAdapterPosition, null, ListActions.OPEN)
        }
      }
    }
  }
}
