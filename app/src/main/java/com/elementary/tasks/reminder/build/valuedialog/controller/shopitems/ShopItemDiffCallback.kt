package com.elementary.tasks.reminder.build.valuedialog.controller.shopitems

import androidx.recyclerview.widget.DiffUtil
import com.github.naz013.domain.reminder.ShopItem

class ShopItemDiffCallback : DiffUtil.ItemCallback<ShopItem>() {
  override fun areItemsTheSame(oldItem: ShopItem, newItem: ShopItem): Boolean {
    return oldItem.uuId == newItem.uuId
  }

  override fun areContentsTheSame(oldItem: ShopItem, newItem: ShopItem): Boolean {
    return oldItem.uuId == newItem.uuId &&
      oldItem.showInput == newItem.showInput &&
      oldItem.position == newItem.position &&
      oldItem.isDeleted == newItem.isDeleted &&
      oldItem.isChecked == newItem.isChecked &&
      oldItem.summary == newItem.summary &&
      oldItem.createTime == newItem.createTime
  }
}
