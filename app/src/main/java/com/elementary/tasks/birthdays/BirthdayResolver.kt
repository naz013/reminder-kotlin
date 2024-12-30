package com.elementary.tasks.birthdays

import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.utils.ListActions
import com.github.naz013.ui.common.Dialogues

class BirthdayResolver(
  private val dialogAction: () -> Dialogues,
  private val deleteAction: (birthday: UiBirthdayList) -> Unit,
  private val birthdayEditAction: (birthday: UiBirthdayList) -> Unit,
  private val birthdayOpenAction: (birthday: UiBirthdayList) -> Unit
) {

  fun resolveAction(view: View, birthday: UiBirthdayList, listActions: ListActions) {
    when (listActions) {
      ListActions.EDIT -> editBirthday(birthday)
      ListActions.OPEN -> openBirthday(birthday)
      ListActions.MORE -> showMore(view, birthday)
      else -> {
      }
    }
  }

  private fun showMore(view: View, birthday: UiBirthdayList) {
    val context = view.context
    val items = arrayOf(context.getString(R.string.edit), context.getString(R.string.delete))
    Dialogues.showPopup(view, { item ->
      if (item == 0) {
        editBirthday(birthday)
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

  private fun openBirthday(birthday: UiBirthdayList) {
    birthdayOpenAction(birthday)
  }

  private fun editBirthday(birthday: UiBirthdayList) {
    birthdayEditAction(birthday)
  }
}
