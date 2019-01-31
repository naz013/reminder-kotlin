package com.elementary.tasks.core.binding.holders

import android.view.View
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding
import com.google.android.material.card.MaterialCardView

class ListItemBirthday(view: View) : Binding(view) {
    val buttonMore: View by bindView(R.id.buttonMore)
    val eventDate: TextView by bindView(R.id.eventDate)
    val eventNumber: TextView by bindView(R.id.eventNumber)
    val eventText: TextView by bindView(R.id.eventText)
    val itemCard: MaterialCardView by bindView(R.id.itemCard)
}