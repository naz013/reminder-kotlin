package com.elementary.tasks.reminder.create.selector

import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.databinding.ListItemOptionBinding

class OptionHolder(parent: ViewGroup, private val listener: ((Int) -> Unit)? = null) :
        HolderBinding<ListItemOptionBinding>(parent, R.layout.list_item_option) {

    init {
        binding.root.setOnClickListener { listener?.invoke(adapterPosition) }
    }

    fun bind(item: Option) {
        binding.itemImage.setImageResource(item.icon)
        if (item.isSelected) {
            binding.itemBg.show()
            binding.itemDrop.show()
        } else {
            binding.itemBg.transparent()
            binding.itemDrop.transparent()
        }
    }
}
