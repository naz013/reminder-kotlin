package com.elementary.tasks.notes.list

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.utils.ListActions
import com.github.naz013.feature.common.android.dp2px
import com.github.naz013.feature.common.android.inflater
import com.github.naz013.feature.common.android.transparent
import com.github.naz013.feature.common.android.visible
import com.elementary.tasks.databinding.ListItemNoteBinding

class NoteViewHolder(
  parent: ViewGroup,
  private val common: UiNoteListAdapterCommon = UiNoteListAdapterCommon(),
  private val listener: ((View, Int, ListActions) -> Unit)?,
  private val imageClickListener: ((View, position: Int, imageId: Int) -> Unit)?
) : HolderBinding<ListItemNoteBinding>(
  ListItemNoteBinding.inflate(parent.inflater(), parent, false)
) {

  private var hasMore = true
    set(value) {
      field = value
      updateMore()
    }

  init {
    hoverClick(binding.bgView) {
      listener?.invoke(it, bindingAdapterPosition, ListActions.OPEN)
    }
    binding.buttonMore.setOnClickListener {
      listener?.invoke(it, bindingAdapterPosition, ListActions.MORE)
    }
    updateMore()
  }

  private fun updateMore() {
    if (listener == null || !hasMore) {
      binding.buttonMore.transparent()
    } else {
      binding.buttonMore.visible()
    }
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

  fun setData(uiNoteList: UiNoteList) {
    common.populateNoteUi(
      uiNoteList = uiNoteList,
      imagesViewContainer = binding.imagesView,
      textView = binding.noteTv,
      secondaryImageSize = itemView.dp2px(128),
      backgroundView = binding.bgView,
      imageClickListener = { view: View, imageId: Int ->
        hoverClick(view) {
          imageClickListener?.invoke(it, bindingAdapterPosition, imageId)
        }
      }
    )

    binding.buttonMore.setImageDrawable(uiNoteList.moreIcon)
  }
}
