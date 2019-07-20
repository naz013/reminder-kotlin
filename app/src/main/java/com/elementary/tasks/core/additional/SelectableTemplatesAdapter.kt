package com.elementary.tasks.core.additional

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemMessageBinding

class SelectableTemplatesAdapter : RecyclerView.Adapter<SelectableTemplatesAdapter.ViewHolder>() {

    private val mDataList = mutableListOf<SmsTemplate>()
    var selectedPosition = -1
        private set

    fun setData(list: List<SmsTemplate>) {
        this.mDataList.clear()
        this.mDataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class ViewHolder(parent: ViewGroup) : HolderBinding<ListItemMessageBinding>(parent,
            R.layout.list_item_message) {
        fun bind(item: SmsTemplate) {
            binding.messageView.text = item.title
            if (item.isSelected) {
                binding.bgView.setBackgroundColor(ThemeUtil.colorWithAlpha(ThemeUtil.getThemeSecondaryColor(itemView.context), 50))
            } else {
                binding.bgView.setBackgroundResource(android.R.color.transparent)
            }
        }

        init {
            binding.clickView.setOnClickListener { selectItem(adapterPosition) }
            binding.buttonMore.visibility = View.GONE
        }
    }

    fun getItem(position: Int): SmsTemplate? {
        return if (position < mDataList.size) {
           try {
               mDataList[position]
           } catch (e: Exception) {
               null
           }
        } else {
            null
        }
    }

    fun selectItem(position: Int) {
        if (position == selectedPosition) return
        if (selectedPosition != -1 && selectedPosition < mDataList.size) {
            mDataList[selectedPosition].isSelected = false
            notifyItemChanged(selectedPosition)
        }
        this.selectedPosition = position
        if (position < mDataList.size) {
            mDataList[position].isSelected = true
            notifyItemChanged(position)
        }
    }
}
