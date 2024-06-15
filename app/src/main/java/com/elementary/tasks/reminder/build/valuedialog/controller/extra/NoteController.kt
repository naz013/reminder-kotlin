package com.elementary.tasks.reminder.build.valuedialog.controller.extra

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.dp2px
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.databinding.BuilderItemSelectableListNoteBinding
import com.elementary.tasks.databinding.ListItemNoteSelectableBinding
import com.elementary.tasks.notes.list.UiNoteListAdapterCommon
import com.elementary.tasks.notes.list.UiNoteListDiffCallback
import com.elementary.tasks.reminder.build.NoteBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController
import timber.log.Timber

class NoteController(
  private val noteBuilderItem: NoteBuilderItem
) : AbstractBindingValueController<UiNoteList, BuilderItemSelectableListNoteBinding>(
  builderItem = noteBuilderItem
) {

  private val adapter = SelectableNotesRecyclerAdapter { _, item ->
    onSelectionChanged(item)
  }

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemSelectableListNoteBinding {
    return BuilderItemSelectableListNoteBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.itemsListView.layoutManager = LinearLayoutManager(getContext())
    binding.itemsListView.adapter = adapter
  }

  override fun onDataChanged(data: UiNoteList?) {
    super.onDataChanged(data)
    adapter.submitList(noteBuilderItem.notes)
    adapter.selectPosition(noteBuilderItem.notes.indexOfFirst { it.id == data?.id })
  }

  private fun onSelectionChanged(uiNoteList: UiNoteList?) {
    updateValue(uiNoteList)
  }

  private class SelectableNotesRecyclerAdapter(
    private val onSelectionChangeListener: (position: Int, item: UiNoteList?) -> Unit
  ) : ListAdapter<UiNoteList, SelectableNoteViewHolder>(UiNoteListDiffCallback()) {

    var selectedPosition: Int = -1
      private set

    fun selectPosition(position: Int) {
      updateSelection(position)
    }

    private fun updateSelection(position: Int) {
      Timber.d("updateSelection: pos=$position, selected=${position == selectedPosition}")
      if (position == selectedPosition) {
        val prevSelected = selectedPosition
        selectedPosition = -1
        notifyItemChanged(prevSelected)
        onSelectionChangeListener(-1, null)
      } else {
        val prevSelected = selectedPosition
        if (prevSelected != -1) {
          notifyItemChanged(prevSelected)
        }
        selectedPosition = position
        if (position != -1) {
          notifyItemChanged(position)
          onSelectionChangeListener.invoke(selectedPosition, getItem(position))
        }
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableNoteViewHolder {
      return SelectableNoteViewHolder(parent) { updateSelection(it) }
    }

    override fun onBindViewHolder(holder: SelectableNoteViewHolder, position: Int) {
      holder.setData(getItem(position), position == selectedPosition)
    }
  }

  private class SelectableNoteViewHolder(
    parent: ViewGroup,
    private val common: UiNoteListAdapterCommon = UiNoteListAdapterCommon(),
    private val listener: (Int) -> Unit
  ) : HolderBinding<ListItemNoteSelectableBinding>(
    ListItemNoteSelectableBinding.inflate(parent.inflater(), parent, false)
  ) {

    init {
      hoverClick(binding.bgView) {
        listener.invoke(bindingAdapterPosition)
      }
      binding.buttonCheck.setOnClickListener {
        listener.invoke(bindingAdapterPosition)
      }
    }

    fun setData(uiNoteList: UiNoteList, isSelected: Boolean) {
      common.populateNoteUi(
        uiNoteList = uiNoteList,
        imagesViewContainer = binding.imagesView,
        textView = binding.noteTv,
        backgroundView = binding.bgView,
        secondaryImageSize = itemView.dp2px(128),
        imageClickListener = null
      )

      val icon = if (isSelected) {
        R.drawable.ic_builder_google_task_list
      } else {
        R.drawable.ic_fluent_radio_button
      }
      binding.buttonCheck.setImageDrawable(
        ViewUtils.tintIcon(binding.buttonCheck.context, icon, uiNoteList.textColor)
      )
    }

    private fun hoverClick(view: View, click: (View) -> Unit) {
      view.setOnTouchListener { v, event ->
        when (event.action) {
          MotionEvent.ACTION_DOWN -> {
            binding.clickView.isPressed = true
            return@setOnTouchListener true
          }

          MotionEvent.ACTION_UP -> {
            binding.clickView.isPressed = false
            click.invoke(v)
            return@setOnTouchListener v.performClick()
          }

          MotionEvent.ACTION_CANCEL -> {
            binding.clickView.isPressed = false
          }
        }
        return@setOnTouchListener true
      }
    }
  }
}
