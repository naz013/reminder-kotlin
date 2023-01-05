package com.elementary.tasks.birthdays.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.interfaces.ActionsListener

class BirthdaysRecyclerAdapter :
  ListAdapter<UiBirthdayList, BirthdayHolder>(UiBirthdayListDiffCallback()) {

  var actionsListener: ActionsListener<UiBirthdayList>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayHolder {
    return BirthdayHolder(parent) { view, i, listActions ->
      actionsListener?.onAction(view, i, getItem(i), listActions)
    }
  }

  override fun onBindViewHolder(holder: BirthdayHolder, position: Int) {
    holder.setData(getItem(position))
  }
}
