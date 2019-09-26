package com.elementary.tasks.birthdays.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.interfaces.ActionsListener

class BirthdaysRecyclerAdapter : ListAdapter<Birthday, BirthdayHolder>(BirthdayDiffCallback()) {

    var actionsListener: ActionsListener<Birthday>? = null

    override fun getItem(position: Int): Birthday? {
        if (position < 0 || position >= itemCount) return null
        return super.getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayHolder {
        return BirthdayHolder(parent) { view, i, listActions ->
            actionsListener?.onAction(view, i, getItem(i), listActions)
        }
    }

    override fun onBindViewHolder(holder: BirthdayHolder, position: Int) {
        getItem(position)?.let { holder.setData(it) }
    }
}
