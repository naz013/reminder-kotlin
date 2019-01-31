package com.elementary.tasks.core.binding.activities

import android.app.Activity
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.ActivityBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView

class ShowBirthdayActivityBinding(activity: Activity) : ActivityBinding(activity) {
    val buttonOk: FloatingActionButton by bindView(R.id.buttonOk)
    val buttonCall: MaterialButton by bindView(R.id.buttonCall)
    val buttonSms: MaterialButton by bindView(R.id.buttonSms)
    val userYears: TextView by bindView(R.id.userYears)
    val userNumber: TextView by bindView(R.id.userNumber)
    val userName: TextView by bindView(R.id.userName)
    val contactPhoto: CircleImageView by bindView(R.id.contactPhoto)
}