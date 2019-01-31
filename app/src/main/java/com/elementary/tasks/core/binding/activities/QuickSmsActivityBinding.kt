package com.elementary.tasks.core.binding.activities

import android.app.Activity
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.ActivityBinding
import com.google.android.material.button.MaterialButton

class QuickSmsActivityBinding(activity: Activity) : ActivityBinding(activity) {
    val messagesList: RecyclerView by bindView(R.id.messagesList)
    val buttonSend: MaterialButton by bindView(R.id.buttonSend)
    val contactInfo: TextView by bindView(R.id.contactInfo)
}