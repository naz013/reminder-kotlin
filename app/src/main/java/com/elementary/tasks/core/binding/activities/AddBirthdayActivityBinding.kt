package com.elementary.tasks.core.binding.activities

import android.app.Activity
import android.view.View
import android.widget.CheckBox
import android.widget.ScrollView
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.ActivityBinding
import com.elementary.tasks.core.views.PhoneAutoCompleteView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddBirthdayActivityBinding(activity: Activity) : ActivityBinding(activity) {
    val scrollView: ScrollView by bindView(R.id.scrollView)
    val birthNameLayout: TextInputLayout by bindView(R.id.birthNameLayout)
    val birthName: TextInputEditText by bindView(R.id.birthName)
    val birthDate: TextView by bindView(R.id.birthDate)
    val contactCheck: CheckBox by bindView(R.id.contactCheck)
    val container: View by bindView(R.id.container)
    val numberLayout: TextInputLayout by bindView(R.id.numberLayout)
    val numberView: PhoneAutoCompleteView by bindView(R.id.numberView)
    val pickContact: View by bindView(R.id.pickContact)
}