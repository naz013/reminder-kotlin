package com.elementary.tasks.notes.list

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import kotlinx.android.synthetic.main.list_item_note.view.*
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Copyright 2017 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class NoteHolder(parent: ViewGroup, listener: ((View, Int, ListActions) -> Unit)?) :
        BaseHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_note, parent, false)) {

    @Inject
    lateinit var imagesSingleton: ImagesSingleton

    init {
        ReminderApp.appComponent.inject(this)
        itemView.bgView.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
        itemView.button_more.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
        if (listener == null) {
            itemView.button_more.visibility = View.INVISIBLE
        } else {
            itemView.button_more.visibility = View.VISIBLE
        }
    }

    fun setData(item: NoteWithImages) {
        loadImage(itemView.imagesView, item)
        loadNote(itemView.noteTv, item)

        itemView.bgView.setBackgroundColor(themeUtil.getNoteLightColor(item.getColor(), item.getOpacity()))

        val isDarkIcon = if (themeUtil.isAlmostTransparent(item.getOpacity())) {
            themeUtil.isDark
        } else {
            false
        }
        itemView.button_more.setImageDrawable(ViewUtils.tintIcon(itemView.context, R.drawable.ic_twotone_more_vert_24px, isDarkIcon))

        if (themeUtil.isAlmostTransparent(item.getOpacity())) {
            if (themeUtil.isDark) {
                itemView.noteTv.setTextColor(ContextCompat.getColor(itemView.context, R.color.pureWhite))
            } else {
                itemView.noteTv.setTextColor(ContextCompat.getColor(itemView.context, R.color.pureBlack))
            }
        } else {
            itemView.noteTv.setTextColor(ContextCompat.getColor(itemView.context, R.color.pureBlack))
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
        imageView.setOnClickListener {
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
        if (!images.isEmpty()) {
            imageView.visibility = View.VISIBLE
            val image = WeakReference(images[0])
            setImage(imageView, image.get()?.image)
            var index = 1
            val horView = container.findViewById<LinearLayout>(R.id.imagesContainer)
            horView.removeAllViewsInLayout()
            while (index < images.size) {
                val imV = ImageView(container.context)
                val params = LinearLayout.LayoutParams(MeasureUtils.dp2px(container.context, 128),
                        MeasureUtils.dp2px(container.context, 72))
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
        }
    }
}
