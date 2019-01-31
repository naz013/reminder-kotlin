package com.elementary.tasks.core.binding.holders

import android.view.View
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding
import com.google.android.material.card.MaterialCardView

class ListItemMessageBinding(view: View) : Binding(view) {
    val clickView: MaterialCardView by bindView(R.id.clickView)
    val bgView: View by bindView(R.id.bgView)
    val messageView: TextView by bindView(R.id.messageView)
    val buttonMore: View by bindView(R.id.buttonMore)
}