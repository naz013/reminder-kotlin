package com.elementary.tasks.reminder.lists.adapter

import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemShopTaskBinding

class ShopListRecyclerAdapter : RecyclerView.Adapter<ShopListRecyclerAdapter.ViewHolder>() {

  private var mDataList: MutableList<ShopItem> = mutableListOf()
  private var onBind: Boolean = false
  var listener: ActionListener? = null

  var data: List<ShopItem>
    get() = mDataList
    set(list) {
      this.mDataList.clear()
      this.mDataList.addAll(list)
      mDataList.sortWith { item, t1 -> t1.createTime.compareTo(item.createTime) }
      sort(mDataList)
      notifyDataSetChanged()
    }

  fun delete(position: Int) {
    mDataList.removeAt(position)
    notifyItemRemoved(position)
    notifyItemRangeChanged(0, mDataList.size)
  }

  fun addItem(item: ShopItem) {
    mDataList.add(0, item)
    notifyItemInserted(0)
    notifyItemRangeChanged(0, mDataList.size)
  }

  fun updateData() {
    mDataList.sortWith { item, t1 -> t1.createTime.compareTo(item.createTime) }
    sort(mDataList)
    notifyDataSetChanged()
  }

  private fun sort(list: MutableList<ShopItem>) {
    val pos = -1
    for (i in list.indices) {
      val item = list[i]
      if (!item.isChecked && i > pos + 1) {
        list.removeAt(i)
        list.add(pos + 1, item)
      }
    }
  }

  fun getItem(position: Int) = mDataList[position]

  inner class ViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemShopTaskBinding>(
    ListItemShopTaskBinding.inflate(parent.inflater(), parent, false)
  ) {

    init {
      binding.clearButton.setOnClickListener {
        listener?.onItemDelete(adapterPosition)
      }
      binding.itemCheck.setOnCheckedChangeListener { _, isChecked1 ->
        if (!onBind && listener != null) {
          listener?.onItemCheck(adapterPosition, isChecked1)
        }
      }
    }

    fun bind(item: ShopItem) {
      val title = item.summary
      if (item.isChecked) {
        binding.shopText.paintFlags = binding.shopText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
      } else {
        binding.shopText.paintFlags = binding.shopText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
      }
      binding.itemCheck.isChecked = item.isChecked
      binding.shopText.text = title
      if (listener == null) {
        binding.clearButton.visibility = View.GONE
        binding.itemCheck.isEnabled = false
        binding.shopText.setTextColor(ContextCompat.getColor(itemView.context, R.color.pureBlack))
      } else {
        binding.itemCheck.visibility = View.VISIBLE
        binding.clearButton.visibility = View.VISIBLE
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    onBind = true
    val item = mDataList[position]
    holder.bind(item)
    onBind = false
  }

  override fun getItemCount() = mDataList.size

  interface ActionListener {
    fun onItemCheck(position: Int, isChecked: Boolean)
    fun onItemDelete(position: Int)
  }
}
