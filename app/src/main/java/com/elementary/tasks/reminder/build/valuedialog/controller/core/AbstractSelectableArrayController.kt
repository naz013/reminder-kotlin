package com.elementary.tasks.reminder.build.valuedialog.controller.core

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.databinding.BuilderItemSelectableArrayBinding
import com.elementary.tasks.databinding.ListItemBuilderSelectableBinding
import com.elementary.tasks.reminder.build.BuilderItem

abstract class AbstractSelectableArrayController<T, D : SelectableValue>(
  builderItem: BuilderItem<T>,
  private val multiChoice: Boolean,
  private val numOfColumns: Int = 7
) : AbstractBindingValueController<T, BuilderItemSelectableArrayBinding>(builderItem) {

  private val arrayAdapter by lazy {
    ArrayAdapter(getAdapterData(), multiChoice) { updateValue(it) }
  }

  abstract fun getAdapterData(): List<D>

  abstract fun updateValue(selectedItems: List<D>)

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemSelectableArrayBinding {
    return BuilderItemSelectableArrayBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.itemsListView.layoutManager = GridLayoutManager(getContext(), numOfColumns)
    binding.itemsListView.adapter = arrayAdapter
  }

  class ArrayAdapter<V : SelectableValue>(
    private val items: List<V>,
    private val isMultiChoice: Boolean,
    private val onDataChanged: (List<V>) -> Unit
  ) : RecyclerView.Adapter<ArrayAdapter.ArrayAdapterViewHolder>() {

    fun getSelected(): List<V> {
      return items.filter { it.isSelected() }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelection() {
      items.forEach {
        it.setSelected(false)
      }
      notifyDataSetChanged()
      onDataChanged(getSelected())
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
      items.forEach {
        it.setSelected(true)
      }
      notifyDataSetChanged()
      onDataChanged(getSelected())
    }

    override fun getItemCount(): Int {
      return items.size
    }

    override fun onBindViewHolder(holder: ArrayAdapterViewHolder, position: Int) {
      holder.bind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArrayAdapterViewHolder {
      return ArrayAdapterViewHolder(
        parent = parent,
        clickListener = { onItemClicked(it) }
      )
    }

    private fun onItemClicked(position: Int) {
      val item = items[position]
      if (isMultiChoice) {
        item.setSelected(!item.isSelected())
      } else {
        getSelectedPosition().takeIf { it != -1 }?.also {
          items[it].setSelected(false)
          notifyItemChanged(it)
        }
        item.setSelected(true)
      }
      notifyItemChanged(position)
      onDataChanged(getSelected())
    }

    private fun getSelectedPosition(): Int {
      return items.indexOfFirst { it.isSelected() }
    }

    class ArrayAdapterViewHolder(
      parent: ViewGroup,
      clickListener: (Int) -> Unit,
      private val binding: ListItemBuilderSelectableBinding =
        ListItemBuilderSelectableBinding.inflate(
          /* inflater = */ LayoutInflater.from(parent.context),
          /* parent = */ parent,
          /* attachToParent = */ false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

      init {
        binding.textView.setOnClickListener { clickListener(bindingAdapterPosition) }
      }

      fun bind(selectableValue: SelectableValue) {
        binding.textView.text = selectableValue.getTitle()
        binding.textView.isSelected = selectableValue.isSelected()
        if (selectableValue.isSelected()) {
          binding.textView.setBackgroundResource(R.drawable.drawable_tertiary)
        } else {
          binding.textView.background = null
        }
      }
    }
  }

  open class SimpleSelectableValue<T>(
    val value: T,
    val uiValue: String,
    var selectionState: Boolean
  ) : SelectableValue {
    override fun getTitle(): String {
      return uiValue
    }

    override fun isSelected(): Boolean {
      return selectionState
    }

    override fun setSelected(isSelected: Boolean) {
      this.selectionState = isSelected
    }
  }
}
