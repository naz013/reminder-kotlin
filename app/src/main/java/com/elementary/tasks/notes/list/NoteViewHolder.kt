package com.elementary.tasks.notes.list

import android.content.Intent
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseViewHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.utils.AssetsUtil
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.dp2px
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.isAlmostTransparent
import com.elementary.tasks.core.utils.isColorDark
import com.elementary.tasks.databinding.ListItemNoteBinding
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import java.lang.ref.WeakReference

class NoteViewHolder(
  parent: ViewGroup,
  currentStateHolder: CurrentStateHolder,
  private val imagesSingleton: ImagesSingleton,
  val listener: ((View, Int, ListActions) -> Unit)?
) : BaseViewHolder<ListItemNoteBinding>(
  ListItemNoteBinding.inflate(parent.inflater(), parent, false),
  currentStateHolder
) {

  var hasMore = true
    set(value) {
      field = value
      updateMore()
    }

  init {
    hoverClick(binding.bgView) {
      listener?.invoke(it, adapterPosition, ListActions.OPEN)
    }
    binding.buttonMore.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
    updateMore()
  }

  private fun updateMore() {
    if (listener == null || !hasMore) {
      binding.buttonMore.visibility = View.INVISIBLE
    } else {
      binding.buttonMore.visibility = View.VISIBLE
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

  fun setData(item: NoteWithImages) {
    loadImage(binding.imagesView, item)
    loadNote(binding.noteTv, item)

    val color = theme.getNoteLightColor(item.getColor(), item.getOpacity(), item.getPalette())
    binding.bgView.setBackgroundColor(color)

    val isDarkIcon = if (item.getOpacity().isAlmostTransparent()) {
      theme.isDark
    } else {
      color.isColorDark()
    }
    binding.buttonMore.setImageDrawable(ViewUtils.tintIcon(itemView.context, R.drawable.ic_twotone_more_vert_24px, isDarkIcon))

    if ((item.getOpacity().isAlmostTransparent() && theme.isDark) || color.isColorDark()) {
      binding.noteTv.setTextColor(ContextCompat.getColor(itemView.context, R.color.pureWhite))
    } else {
      binding.noteTv.setTextColor(ContextCompat.getColor(itemView.context, R.color.pureBlack))
    }
  }

  private fun loadNote(textView: TextView, note: NoteWithImages) {
    var title = note.getSummary()
    if (TextUtils.isEmpty(title)) {
      textView.visibility = View.GONE
      return
    }
    textView.visibility = View.VISIBLE
    val context = textView.context
    if (title.length > 500) {
      val substring = title.substring(0, 500)
      title = "$substring..."
    }
    textView.text = title
    textView.typeface = AssetsUtil.getTypeface(context, note.getStyle())
    textView.textSize = (prefs.noteTextSize + 12).toFloat()
  }

  private fun setImage(imageView: ImageView, image: ByteArray?) {
    if (image == null) return
    Glide.with(imageView)
      .load(image)
      .apply(RequestOptions.centerCropTransform())
      .into(imageView)
  }

  private fun setClick(imageView: ImageView, position: Int, key: String?, images: List<ImageFile>) {
    val context = imageView.context.applicationContext
    hoverClick(imageView) {
      imagesSingleton.setCurrent(images)
      context.startActivity(Intent(context, ImagePreviewActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(Constants.INTENT_ID, key)
        .putExtra(Constants.INTENT_DELETE, false)
        .putExtra(Constants.INTENT_POSITION, position))
    }
  }

  private fun loadImage(container: LinearLayout, item: NoteWithImages) {
    val images = item.images

    val imageView = container.findViewById<ImageView>(R.id.noteImage)
    val horView = container.findViewById<LinearLayout>(R.id.imagesContainer)
    horView.removeAllViewsInLayout()

    if (images.isNotEmpty()) {
      imageView.visibility = View.VISIBLE
      horView.visibility = View.VISIBLE
      val image = WeakReference(images[0])
      setImage(imageView, image.get()?.image)
      var index = 1

      while (index < images.size) {
        val imV = ImageView(container.context)
        val params = LinearLayout.LayoutParams(container.dp2px(128),
          container.dp2px(128))
        imV.layoutParams = params
        setClick(imV, index, item.getKey(), images)
        imV.scaleType = ImageView.ScaleType.CENTER_CROP
        horView.addView(imV)
        val im = WeakReference(images[index])
        setImage(imV, im.get()?.image)
        index++
      }
    } else {
      imageView.setImageDrawable(null)
      imageView.visibility = View.GONE
      horView.visibility = View.GONE
    }
  }
}
