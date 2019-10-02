package com.elementary.tasks.reminder.create.selector

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class OptionsAdapter: RecyclerView.Adapter<OptionHolder>() {

    private val data: MutableList<Option> = mutableListOf()
    var selectListener: (Int, Option) -> Unit = { i, option -> }

    fun setOptions(list: List<Option>) {
        this.data.clear()
        this.data.addAll(list)
        this.notifyDataSetChanged()
        notifyListener()
    }

    fun selectKey(key: String) {
        val pointer = findPointerByKey(key)
        if (pointer != -1) {
            updateSelection(pointer)
        }
    }

    private fun notifyListener() {
        val pointer = findPointer()
        if (pointer != -1) {
            selectListener.invoke(pointer, data[pointer])
        }
    }

    private fun findPointerByKey(key: String): Int {
        for (i in 0 until data.size) {
            if (data[i].key == key) return i
        }
        return -1
    }

    private fun findPointer(): Int {
        for (i in 0 until data.size) {
            if (data[i].isSelected) return i
        }
        return -1
    }

    private fun updateSelection(i: Int) {
        val pointer = findPointer()
        if (pointer == i) return
        if (pointer == -1) {
            data[i].isSelected = true
            notifyItemChanged(i)
        } else {
            data[pointer].isSelected = false
            notifyItemChanged(pointer)

            data[i].isSelected = true
            notifyItemChanged(i)
        }
        notifyListener()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionHolder {
        return OptionHolder(parent) { updateSelection(it) }
    }

    override fun onBindViewHolder(holder: OptionHolder, position: Int) {
        holder.bind(data[position])
    }
}