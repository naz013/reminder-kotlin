package com.elementary.tasks.core.contacts

import android.net.Uri
import android.provider.CallLog
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.TimeUtil
import kotlinx.android.synthetic.main.list_item_call.view.*

class CallHolder(itemView: View, val isDark: Boolean, val is24: Boolean, callback: ((Int) -> Unit)?) : RecyclerView.ViewHolder(itemView) {
    fun bind(callsItem: CallsItem) {
        if (callsItem.name == "") {
            itemView.itemName.text = callsItem.number
        } else {
            itemView.itemName.text = callsItem.name
        }
        loadImage(itemView.itemImage, callsItem.uri)
        loadDate(itemView.itemDate, callsItem.date)
        loadIcon(itemView.itemType, callsItem.type)
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

    private fun loadIcon(imageView: ImageView, type: Int) {
        when (type) {
            CallLog.Calls.INCOMING_TYPE -> imageView.setImageResource(if (isDark) R.drawable.ic_call_received_white_24dp else R.drawable.ic_call_received_black_24dp)
            CallLog.Calls.MISSED_TYPE -> imageView.setImageResource(if (isDark) R.drawable.ic_call_missed_white_24dp else R.drawable.ic_call_missed_black_24dp)
            else -> imageView.setImageResource(if (isDark) R.drawable.ic_call_made_white_24dp else R.drawable.ic_call_made_black_24dp)
        }
    }

    private fun loadDate(textView: AppCompatTextView, date: Long) {
        textView.text = TimeUtil.getSimpleDateTime(date, is24)
    }
}