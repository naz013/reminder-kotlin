package com.elementary.tasks.core.contacts

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.list_item_contact.view.*

class ContactHolder(itemView: View, val isDark: Boolean, callback: ((Int) -> Unit)?) : RecyclerView.ViewHolder(itemView) {
    fun bind(contactItem: ContactItem) {
        itemView.itemName.text = contactItem.name
        loadImage(itemView.itemImage, contactItem.uri)
    }

    init {
        itemView.setOnClickListener { callback?.invoke(adapterPosition) }
    }

    private fun loadImage(imageView: ImageView, v: String?) {
        if (v == null) {
            imageView.setImageResource(if (isDark) R.drawable.ic_perm_identity_white_24dp else R.drawable.ic_perm_identity_black_24dp)
            return
        }
        Glide.with(imageView)
                .load(Uri.parse(v))
                .apply(RequestOptions.centerCropTransform())
                .apply(RequestOptions.overrideOf(100, 100))
                .into(imageView)
    }
}