package com.elementary.tasks.core.apps

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.databinding.ListItemApplicationBinding

class AppsRecyclerAdapter : ListAdapter<ApplicationItem, AppsRecyclerAdapter.ApplicationViewHolder>(AppsDiffCallback()) {

    var actionsListener: ActionsListener<ApplicationItem>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        return ApplicationViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ApplicationViewHolder(parent: ViewGroup) : HolderBinding<ListItemApplicationBinding>(parent, R.layout.list_item_application) {
        fun bind(item: ApplicationItem) {
            binding.itemName.text = item.name
            loadImage(binding.itemImage, item.drawable)
        }

        init {
            binding.clickView.setOnClickListener {
                try {
                    actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
                } catch (e: Exception) {
                    actionsListener?.onAction(it, adapterPosition, null, ListActions.OPEN)
                }
            }
        }
    }

    private fun loadImage(imageView: ImageView, v: Drawable?) {
        imageView.setImageDrawable(v)
    }
}
