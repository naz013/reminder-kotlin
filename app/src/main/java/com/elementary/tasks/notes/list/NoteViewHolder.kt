package com.elementary.tasks.notes.list

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.core.utils.ui.dp2px
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.ListItemNoteBinding

class NoteViewHolder(
  parent: ViewGroup,
  private val listener: ((View, Int, ListActions) -> Unit)?,
  private val imageClickListener: ((View, position: Int, imageId: Int) -> Unit)?
) : HolderBinding<ListItemNoteBinding>(
  ListItemNoteBinding.inflate(parent.inflater(), parent, false)
) {

  var hasMore = true
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
    loadImage(binding.imagesView, uiNoteList)
    loadNote(binding.noteTv, uiNoteList)

    binding.bgView.setBackgroundColor(uiNoteList.backgroundColor)
    binding.buttonMore.setImageDrawable(uiNoteList.moreIcon)
    binding.noteTv.setTextColor(uiNoteList.textColor)
  }

  private fun loadNote(textView: TextView, note: UiNoteList) {
    var text = note.text
    if (text.isEmpty()) {
      textView.gone()
      return
    }
    textView.visible()
    if (text.length > 500) {
      val substring = text.substring(0, 500)
      text = "$substring..."
    }
    textView.text = text
    textView.typeface = note.typeface
    textView.textSize = note.fontSize
  }

  private fun setImage(imageView: ImageView, image: String?) {
    if (image == null) return
    Glide.with(imageView)
      .load(image)
      .apply(RequestOptions.centerCropTransform())
      .into(imageView)
  }

  private fun setClick(
    imageView: ImageView,
    position: Int,
    imageId: Int
  ) {
    hoverClick(imageView) {
      imageClickListener?.invoke(it, position, imageId)
    }
  }

  private fun loadImage(container: LinearLayout, item: UiNoteList) {
    val images = item.images

    val imageView = container.findViewById<ImageView>(R.id.noteImage)
    val horView = container.findViewById<LinearLayout>(R.id.imagesContainer)
    horView.removeAllViewsInLayout()

    if (images.isNotEmpty()) {
      imageView.visibility = View.VISIBLE
      horView.visibility = View.VISIBLE
      setImage(imageView, images[0].filePath)
      var index = 1

      while (index < images.size) {
        val imV = ImageView(container.context)
        val params = LinearLayout.LayoutParams(container.dp2px(128), container.dp2px(128))
        imV.layoutParams = params
        setClick(imV, bindingAdapterPosition, images[index].id)
        imV.scaleType = ImageView.ScaleType.CENTER_CROP
        horView.addView(imV)
        setImage(imV, images[index].filePath)
        index++
      }
    } else {
      imageView.setImageDrawable(null)
      imageView.visibility = View.GONE
      horView.visibility = View.GONE
    }
  }
}
