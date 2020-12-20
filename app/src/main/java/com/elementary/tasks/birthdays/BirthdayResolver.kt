package com.elementary.tasks.birthdays

import android.content.Intent
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.birthdays.list.BirthdayListItem
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions

class BirthdayResolver(
  private val dialogAction: () -> Dialogues,
  private val deleteAction: (birthday: BirthdayListItem) -> Unit
) {

  fun resolveAction(view: View, birthday: BirthdayListItem, listActions: ListActions) {
    when (listActions) {
      ListActions.OPEN -> openBirthday(view, birthday)
      ListActions.MORE -> showMore(view, birthday)
      else -> {
      }
    }
  }

  private fun showMore(view: View, birthday: BirthdayListItem) {
    val context = view.context
    val items = arrayOf(context.getString(R.string.edit), context.getString(R.string.delete))
    Dialogues.showPopup(view, { item ->
      if (item == 0) {
        openBirthday(view, birthday)
      } else if (item == 1) {
        askConfirmation(view, items[item]) {
          if (it) deleteAction.invoke(birthday)
        }
      }
    }, *items)
  }

  private fun askConfirmation(view: View, title: String, onAction: (Boolean) -> Unit) {
    dialogAction.invoke().askConfirmation(view.context, title, onAction)
  }

  private fun openBirthday(view: View, birthday: BirthdayListItem) {
    AddBirthdayActivity.openLogged(view.context, Intent(view.context, AddBirthdayActivity::class.java)
      .putExtra(Constants.INTENT_ID, birthday.uuId))
  }
}