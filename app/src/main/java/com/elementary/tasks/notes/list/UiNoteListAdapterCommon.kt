package com.elementary.tasks.notes.list

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Px
import coil.load
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.github.naz013.feature.common.android.gone
import com.github.naz013.feature.common.android.visible

class UiNoteListAdapterCommon {

  fun populateNoteUi(
    uiNoteList: UiNoteList,
    imagesViewContainer: LinearLayout,
    textView: TextView,
    @Px secondaryImageSize: Int,
    backgroundView: View,
    imageClickListener: ((View, imageId: Int) -> Unit)? = null
  ) {
    loadImage(uiNoteList, imagesViewContainer, secondaryImageSize, imageClickListener)
    loadNote(textView, uiNoteList)

    backgroundView.setBackgroundColor(uiNoteList.backgroundColor)
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
    textView.setTextColor(note.textColor)
  }

  private fun setImage(imageView: ImageView, image: String?) {
    if (image == null) return
    imageView.load(image)
  }

  private fun setClick(
    imageView: ImageView,
    imageId: Int,
    imageClickListener: ((View, imageId: Int) -> Unit)?
  ) {
    imageClickListener?.invoke(imageView, imageId)
  }

  private fun loadImage(
    item: UiNoteList,
    container: LinearLayout,
    @Px secondaryImageSize: Int,
    imageClickListener: ((View, imageId: Int) -> Unit)?
  ) {
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
        val params = LinearLayout.LayoutParams(secondaryImageSize, secondaryImageSize)
        imV.layoutParams = params
        setClick(imV, images[index].id, imageClickListener)
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
