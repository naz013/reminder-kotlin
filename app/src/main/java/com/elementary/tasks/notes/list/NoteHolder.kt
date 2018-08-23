package com.elementary.tasks.notes.list

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import kotlinx.android.synthetic.main.list_item_note.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.lang.ref.WeakReference

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

    init {
        itemView.noteClick.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
        itemView.noteClick.setOnLongClickListener { view ->
            listener?.invoke(view, adapterPosition, ListActions.MORE)
            true
        }
    }

    fun setData(item: Note) {
        loadNoteCard(itemView.noteCard, item.color)
        loadImage(itemView.imagesView, item)
        loadNote(itemView.noteTv, item)
    }

    private fun loadNote(textView: TextView, note: Note) {
        var title = note.summary
        if (TextUtils.isEmpty(title)) {
            textView.visibility = View.GONE
            return
        }
        val context = textView.context
        if (title.length > 500) {
            val substring = title.substring(0, 500)
            title = "$substring..."
        }
        textView.text = title
        textView.typeface = AssetsUtil.getTypeface(context, note.style)
        textView.textSize = (prefs.noteTextSize + 12).toFloat()
    }

    private fun loadNoteCard(cardView: CardView, color: Int) {
        cardView.setCardBackgroundColor(themeUtil.getNoteLightColor(color))
        if (Module.isLollipop) {
            cardView.cardElevation = Configs.CARD_ELEVATION
        }
    }

    private fun setImage(imageView: ImageView, image: ByteArray?) {
        if (image == null) return
        launch(CommonPool) {
            val bmp = BitmapFactory.decodeByteArray(image, 0, image.size)
            withUIContext {
                imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp,
                        imageView.width, imageView.height, false))
            }
        }
    }

    private fun setClick(imageView: ImageView, position: Int, key: String?) {
        val context = imageView.context.applicationContext
        imageView.setOnClickListener {
            context.startActivity(Intent(context, ImagePreviewActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Constants.INTENT_ID, key)
                    .putExtra(Constants.INTENT_DELETE, false)
                    .putExtra(Constants.INTENT_POSITION, position))
        }
    }

    fun loadImage(container: LinearLayout, item: Note) {
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
                setClick(imV, index, item.key)
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
