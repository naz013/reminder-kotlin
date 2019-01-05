package com.elementary.tasks.core.contacts

import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.CallLog
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.BitmapUtils
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import kotlinx.android.synthetic.main.list_item_call.view.*

class CallHolder(itemView: View, val isDark: Boolean, private val is24: Boolean, callback: ((Int) -> Unit)?) : RecyclerView.ViewHolder(itemView) {
    fun bind(callsItem: CallsItem) {
        if (callsItem.name == null) {
            itemView.itemName.text = callsItem.number
        } else {
            itemView.itemName.text = "${callsItem.name} (${callsItem.number})"
        }
        loadImage(itemView.itemImage, callsItem)
        loadDate(itemView.itemDate, callsItem.date)
        loadIcon(itemView.itemType, callsItem.type)
    }

    init {
        itemView.clickView.setOnClickListener { callback?.invoke(adapterPosition) }
    }

    private fun loadImage(imageView: ImageView, callsItem: CallsItem) {
        if (callsItem.uri == null) {
            loadNameIcon(callsItem, imageView)
            return
        }
        Glide.with(imageView)
                .load(Uri.parse(callsItem.uri))
                .apply(RequestOptions.centerCropTransform())
                .apply(RequestOptions.overrideOf(100, 100))
                .into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        imageView.setImageDrawable(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        loadNameIcon(callsItem, imageView)
                    }
                })
    }

    private fun loadNameIcon(callsItem: CallsItem, imageView: ImageView) {
        val drawable = BitmapUtils.imageFromName(if (callsItem.name != null) callsItem.name else callsItem.number)
        if (drawable != null) {
            imageView.setImageDrawable(drawable)
        } else {
            imageView.setImageDrawable(ViewUtils.tintIcon(imageView.context, R.drawable.ic_twotone_person_24px, isDark))
        }
    }

    private fun loadIcon(imageView: ImageView, type: Int) {
        when (type) {
            CallLog.Calls.INCOMING_TYPE -> imageView.setImageDrawable(ViewUtils.tintIcon(imageView.context, R.drawable.ic_twotone_call_received_24px, isDark))
            CallLog.Calls.MISSED_TYPE -> imageView.setImageDrawable(ViewUtils.tintIcon(imageView.context, R.drawable.ic_twotone_call_missed_24px, isDark))
            else -> imageView.setImageDrawable(ViewUtils.tintIcon(imageView.context, R.drawable.ic_twotone_call_made_24px, isDark))
        }
    }

    private fun loadDate(textView: AppCompatTextView, date: Long) {
        textView.text = TimeUtil.getSimpleDateTime(date, is24)
    }
}