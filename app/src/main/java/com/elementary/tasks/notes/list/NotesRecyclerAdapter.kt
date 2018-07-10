package com.elementary.tasks.notes.list

import android.content.Context
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

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.AssetsUtil
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemNoteBinding
import com.elementary.tasks.notes.create.NoteImage
import com.elementary.tasks.notes.preview.ImagePreviewActivity

import java.lang.ref.WeakReference
import java.util.ArrayList
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Copyright 2016 Nazar Suhovich
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
class NotesRecyclerAdapter internal constructor() : RecyclerView.Adapter<NoteHolder>() {

    private val mData = ArrayList<Note>()
    private var actionsListener: ActionsListener<Note>? = null
        internal set
    private val mActionListener = { view, position, note, actions ->
        if (actionsListener != null) {
            actionsListener!!.onAction(view, position, getItem(position), actions)
        }
    }

    var data: List<Note>
        get() = mData
        set(list) {
            this.mData.clear()
            this.mData.addAll(list)
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun getItem(position: Int): Note {
        return mData[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        return NoteHolder(ListItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false).root, mActionListener)
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        holder.setData(getItem(position))
    }

    companion object {

        @BindingAdapter("loadNote")
        fun loadNote(textView: TextView, note: Note) {
            var title = note.summary
            if (TextUtils.isEmpty(title)) {
                textView.visibility = View.GONE
                return
            }
            val context = textView.context
            if (title!!.length > 500) {
                val substring = title.substring(0, 500)
                title = "$substring..."
            }
            textView.text = title
            textView.typeface = AssetsUtil.getTypeface(context, note.style)
            textView.textSize = (Prefs.getInstance(context).noteTextSize + 12).toFloat()
        }

        @BindingAdapter("loadNoteCard")
        fun loadNoteCard(cardView: CardView, color: Int) {
            cardView.setCardBackgroundColor(ThemeUtil.getInstance(cardView.context).getNoteLightColor(color))
            if (Module.isLollipop) {
                cardView.cardElevation = Configs.CARD_ELEVATION
            }
        }

        private fun setImage(imageView: ImageView, image: ByteArray?) {
            Thread {
                val bmp = BitmapFactory.decodeByteArray(image, 0, image!!.size)
                imageView.post {
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp,
                            imageView.width, imageView.height, false))
                }
            }.start()
        }

        private fun setClick(imageView: ImageView, position: Int, key: String?) {
            val context = imageView.context.applicationContext
            imageView.setOnClickListener { view ->
                context.startActivity(Intent(context, ImagePreviewActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Constants.INTENT_ID, key)
                        .putExtra(Constants.INTENT_DELETE, false)
                        .putExtra(Constants.INTENT_POSITION, position))
            }
        }

        @BindingAdapter("loadImage")
        fun loadImage(container: LinearLayout, item: Note) {
            val images = item.images
            val imageView = container.findViewById<ImageView>(R.id.noteImage)
            if (!images.isEmpty()) {
                imageView.visibility = View.VISIBLE
                val image = WeakReference(images[0])
                setImage(imageView, image.get().getImage())
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
                    setImage(imV, im.get().getImage())
                    index++
                }
            } else {
                imageView.setImageDrawable(null)
                imageView.visibility = View.GONE
            }
        }
    }
}
