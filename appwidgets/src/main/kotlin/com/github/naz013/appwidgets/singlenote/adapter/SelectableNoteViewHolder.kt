package com.github.naz013.appwidgets.singlenote.adapter

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.databinding.ListItemWidgetNoteSelectableBinding
import com.github.naz013.appwidgets.singlenote.data.UiNoteListSelectable
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.dp2px
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.inflater
import com.github.naz013.ui.common.view.visible

internal class SelectableNoteViewHolder(
  parent: ViewGroup,
  private val binding: ListItemWidgetNoteSelectableBinding =
    ListItemWidgetNoteSelectableBinding.inflate(parent.inflater(), parent, false),
  private val listener: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

  init {
    hoverClick(binding.bgView) {
      listener.invoke(bindingAdapterPosition)
    }
    binding.buttonCheck.setOnClickListener {
      listener.invoke(bindingAdapterPosition)
    }
  }

  fun setData(uiNoteListSelectable: UiNoteListSelectable) {
    loadImage(binding.imagesView, uiNoteListSelectable)
    loadNote(binding.noteTv, uiNoteListSelectable)

    binding.bgView.setBackgroundColor(uiNoteListSelectable.backgroundColor)

    val icon = if (uiNoteListSelectable.isSelected) {
      R.drawable.ic_builder_google_task_list
    } else {
      R.drawable.ic_fluent_radio_button
    }
    binding.buttonCheck.setImageDrawable(
      ViewUtils.tintIcon(binding.buttonCheck.context, icon, uiNoteListSelectable.dartIcon)
    )
    binding.noteTv.setTextColor(uiNoteListSelectable.textColor)
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

  private fun loadNote(textView: TextView, uiNoteListSelectable: UiNoteListSelectable) {
    var text = uiNoteListSelectable.text
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
    textView.typeface = uiNoteListSelectable.typeface
    textView.textSize = uiNoteListSelectable.fontSize
  }

  private fun setImage(imageView: ImageView, image: String?) {
    if (image == null) return
    imageView.load(image)
  }

  private fun loadImage(container: LinearLayout, uiNoteListSelectable: UiNoteListSelectable) {
    val images = uiNoteListSelectable.images

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
