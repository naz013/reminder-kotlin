package com.elementary.tasks.home.scheduleview.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import coil.load
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.github.naz013.feature.common.android.dp2px
import com.github.naz013.feature.common.android.gone
import com.github.naz013.feature.common.android.visible

class ScheduleNoteViewHolderCommon {

  fun loadBackground(bgView: View, uiNoteList: UiNoteList) {
    bgView.setBackgroundColor(uiNoteList.backgroundColor)
  }

  fun loadImages(container: LinearLayout, item: UiNoteList) {
    val images = item.images
    container.removeAllViewsInLayout()

    if (images.isNotEmpty()) {
      container.visible()

      val imageSize = container.dp2px(64)
      var index = 0

      while (index < images.size) {
        val imV = ImageView(container.context)
        val params = LinearLayout.LayoutParams(imageSize, imageSize)
        imV.layoutParams = params
        imV.scaleType = ImageView.ScaleType.CENTER_CROP
        container.addView(imV)
        setImage(imV, images[index].filePath)
        index++
      }
    } else {
      container.gone()
    }
  }

  fun loadNote(textView: TextView, note: UiNoteList) {
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

    textView.setTextColor(note.textColor)
  }

  private fun setImage(imageView: ImageView, image: String?) {
    if (image == null) return
    imageView.load(image)
  }
}
