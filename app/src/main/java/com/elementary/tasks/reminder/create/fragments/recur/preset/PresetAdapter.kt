package com.elementary.tasks.reminder.create.fragments.recur.preset

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.github.naz013.ui.common.view.visibleInvisible
import com.elementary.tasks.databinding.ListItemRecurPresetBinding

@Deprecated("Use new builder screen")
class PresetAdapter(
  private val canDelete: Boolean = true,
  private val onItemClickListener: (UiPresetList) -> Unit,
  private val onItemDeleteListener: (UiPresetList) -> Unit
) : ListAdapter<UiPresetList, PresetAdapter.ViewHolder>(PresetDiffCallback()) {

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(
      parent = parent,
      canDelete = canDelete,
      clickListener = { onItemClickListener.invoke(getItem(it)) },
      onDeleteListener = { onItemDeleteListener.invoke(getItem(it)) }
    )
  }

  class ViewHolder(
    parent: ViewGroup,
    private val canDelete: Boolean,
    private val clickListener: (Int) -> Unit,
    private val onDeleteListener: (Int) -> Unit,
    private val binding: ListItemRecurPresetBinding = ListItemRecurPresetBinding.inflate(
      /* inflater = */ LayoutInflater.from(parent.context),
      /* parent = */ parent,
      /* attachToParent = */ false
    )
  ) : RecyclerView.ViewHolder(binding.root) {

    init {
      binding.clickView.setOnClickListener { clickListener.invoke(bindingAdapterPosition) }
      binding.buttonDelete.setOnClickListener { onDeleteListener.invoke(bindingAdapterPosition) }
      binding.buttonDelete.visibleInvisible(canDelete)
    }

    fun bind(presetList: UiPresetList) {
      binding.nameView.text = presetList.name
      binding.descriptionView.text = presetList.description
    }
  }
}
