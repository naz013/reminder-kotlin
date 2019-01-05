package com.elementary.tasks.core.contacts

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.BitmapUtils
import com.elementary.tasks.core.utils.ViewUtils
import kotlinx.android.synthetic.main.list_item_contact.view.*

class ContactHolder(itemView: View, val isDark: Boolean, callback: ((Int) -> Unit)?) : RecyclerView.ViewHolder(itemView) {
    fun bind(contactItem: ContactItem) {
        itemView.itemName.text = contactItem.name
        loadImage(itemView.itemImage, contactItem)
    }

    init {
        itemView.clickView.setOnClickListener { callback?.invoke(adapterPosition) }
    }

    private fun loadImage(imageView: ImageView, contactItem: ContactItem) {
        if (contactItem.uri == null) {
            loadNameIcon(contactItem, imageView)
            return
        }
        Glide.with(imageView)
                .load(Uri.parse(contactItem.uri))
                .apply(RequestOptions.centerCropTransform())
                .apply(RequestOptions.overrideOf(100, 100))
                .into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        imageView.setImageDrawable(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        loadNameIcon(contactItem, imageView)
                    }
                })
    }

    private fun loadNameIcon(contactItem: ContactItem, imageView: ImageView) {
        val drawable = BitmapUtils.imageFromName(contactItem.name)
        if (drawable != null) {
            imageView.setImageDrawable(drawable)
        } else {
            imageView.setImageDrawable(ViewUtils.tintIcon(imageView.context, R.drawable.ic_twotone_person_24px, isDark))
        }
    }
}