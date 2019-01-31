package com.elementary.tasks.core.binding.activities

import android.app.Activity
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.ActivityBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import de.hdodenhof.circleimageview.CircleImageView

class FollowReminderActivityBinding(activity: Activity) : ActivityBinding(activity) {
    val contactPhoto: CircleImageView by bindView(R.id.contactPhoto)
    val textLayout: TextInputLayout by bindView(R.id.textLayout)
    val textField: TextInputEditText by bindView(R.id.textField)
    val contactInfo: TextView by bindView(R.id.contactInfo)
    val typeMessage: RadioButton by bindView(R.id.typeMessage)
    val typeCall: RadioButton by bindView(R.id.typeCall)
    val timeTomorrow: RadioButton by bindView(R.id.timeTomorrow)
    val timeNextWorking: RadioButton by bindView(R.id.timeNextWorking)
    val timeAfter: RadioButton by bindView(R.id.timeAfter)
    val timeCustom: RadioButton by bindView(R.id.timeCustom)
    val tomorrowTime: TextView by bindView(R.id.tomorrowTime)
    val nextWorkingTime: TextView by bindView(R.id.nextWorkingTime)
    val afterTime: Spinner by bindView(R.id.afterTime)
    val customDate: TextView by bindView(R.id.customDate)
    val customTime: TextView by bindView(R.id.customTime)
    val exportCheck: CheckBox by bindView(R.id.exportCheck)
    val taskExport: CheckBox by bindView(R.id.taskExport)
    val fab: MaterialButton by bindView(R.id.fab)
}