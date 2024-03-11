package com.elementary.tasks.reminder.build.valuedialog.controller.shopitems

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.data.models.ShopItem

class ShopItemsAdapter(
  private val inputMethodManager: InputMethodManager,
  private val onTextChanged: (Int, String) -> Unit,
  private val onCheckClicked: (Int) -> Unit,
  private val onRemoveClicked: (Int) -> Unit,
  private val onEnterPressed: (Int) -> Unit,
  private val onDeletePressed: (Int) -> Unit
) : RecyclerView.Adapter<ShopItemViewHolder>() {

  private var items: List<ShopItem> = emptyList()

  @SuppressLint("NotifyDataSetChanged")
  fun submitList(list: List<ShopItem>?) {
    this.items = list ?: emptyList()
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopItemViewHolder {
    return ShopItemViewHolder(
      parent = parent,
      inputMethodManager = inputMethodManager,
      onCheckClicked = onCheckClicked,
      onRemoveClicked = onRemoveClicked,
      onEnterPressed = onEnterPressed,
      onTextChanged = onTextChanged,
      onDeletePressed = onDeletePressed
    )
  }

  override fun onBindViewHolder(holder: ShopItemViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun getItemCount(): Int {
    return items.size
  }
}
