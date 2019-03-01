package com.elementary.tasks.core.contacts

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.utils.BitmapUtils
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ListItemContactBinding

class ContactHolder(parent: ViewGroup, val isDark: Boolean, callback: ((Int) -> Unit)?)
    : HolderBinding<ListItemContactBinding>(parent, R.layout.list_item_contact) {
    fun bind(contactItem: ContactItem) {
        binding.itemName.text = contactItem.name
        loadImage(binding.itemImage, contactItem)
    }

    init {
        binding.clickView.setOnClickListener { callback?.invoke(adapterPosition) }
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
        BitmapUtils.imageFromName(contactItem.name) {
            if (it != null) {
                imageView.setImageDrawable(it)
            } else {
                imageView.setImageDrawable(ViewUtils.tintIcon(imageView.context, R.drawable.ic_twotone_person_24px, isDark))
            }
        }
    }
}