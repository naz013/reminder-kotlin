package com.elementary.tasks.reminder.build.valuedialog.controller.core

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.databinding.BuilderItemSelectableListRadioBinding
import com.elementary.tasks.databinding.ListItemBuilderSelectableRadioBinding
import com.elementary.tasks.reminder.build.BuilderItem

abstract class AbstractSelectableRadioController<T, D : SelectableValue>(
  builderItem: BuilderItem<T>
) : AbstractBindingValueController<T, BuilderItemSelectableListRadioBinding>(builderItem) {

  private val radioAdapter by lazy {
    RadioAdapter(getAdapterData()) { updateValue(it) }
  }

  abstract fun getAdapterData(): List<D>

  abstract fun updateValue(selected: D?)

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemSelectableListRadioBinding {
    return BuilderItemSelectableListRadioBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.itemsListView.layoutManager = LinearLayoutManager(getContext())
    binding.itemsListView.adapter = radioAdapter
  }

  class RadioAdapter<V : SelectableValue>(
    private val items: List<V>,
    private val onDataChanged: (V?) -> Unit
  ) : RecyclerView.Adapter<RadioAdapter.RadioAdapterViewHolder>() {

    fun getSelected(): V? {
      return items.firstOrNull { it.isSelected() }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelection() {
      items.forEach {
        it.setSelected(false)
      }
      notifyDataSetChanged()
      onDataChanged(getSelected())
    }

    override fun getItemCount(): Int {
      return items.size
    }

    override fun onBindViewHolder(holder: RadioAdapterViewHolder, position: Int) {
      holder.bind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioAdapterViewHolder {
      return RadioAdapterViewHolder(
        parent = parent,
        clickListener = { onItemClicked(it) }
      )
    }

    private fun onItemClicked(position: Int) {
      val item = items[position]
      if (item.isSelected()) return
      getSelectedPosition().takeIf { it != -1 }?.also {
        items[it].setSelected(false)
        notifyItemChanged(it)
      }
      item.setSelected(true)
      notifyItemChanged(position)
      onDataChanged(getSelected())
    }

    private fun getSelectedPosition(): Int {
      return items.indexOfFirst { it.isSelected() }
    }

    class RadioAdapterViewHolder(
      parent: ViewGroup,
      clickListener: (Int) -> Unit,
      private val binding: ListItemBuilderSelectableRadioBinding =
        ListItemBuilderSelectableRadioBinding.inflate(
          /* inflater = */ LayoutInflater.from(parent.context),
          /* parent = */ parent,
          /* attachToParent = */ false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

      init {
        binding.nameTextView.setOnClickListener { clickListener(bindingAdapterPosition) }
        binding.radioView.setOnClickListener { clickListener(bindingAdapterPosition) }
      }

      fun bind(selectableValue: SelectableValue) {
        binding.nameTextView.text = selectableValue.getTitle()
        binding.radioView.isChecked = selectableValue.isSelected()
      }
    }
  }
}
