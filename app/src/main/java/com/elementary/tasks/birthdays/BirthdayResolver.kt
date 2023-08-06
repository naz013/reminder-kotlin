package com.elementary.tasks.birthdays

import android.content.Intent
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.birthdays.preview.BirthdayPreviewActivity
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.pin.PinLoginActivity

class BirthdayResolver(
  private val dialogAction: () -> Dialogues,
  private val deleteAction: (birthday: UiBirthdayList) -> Unit
) {

  fun resolveAction(view: View, birthday: UiBirthdayList, listActions: ListActions) {
    when (listActions) {
      ListActions.EDIT -> editBirthday(view, birthday)
      ListActions.OPEN -> openBirthday(view, birthday)
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
        editBirthday(view, birthday)
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

  private fun openBirthday(view: View, birthday: UiBirthdayList) {
    PinLoginActivity.openLogged(
      context = view.context,
      intent = Intent(view.context, BirthdayPreviewActivity::class.java)
        .putExtra(Constants.INTENT_ID, birthday.uuId)
    )
  }

  private fun editBirthday(view: View, birthday: UiBirthdayList) {
    PinLoginActivity.openLogged(
      context = view.context,
      intent = Intent(view.context, AddBirthdayActivity::class.java)
        .putExtra(Constants.INTENT_ID, birthday.uuId)
    )
  }
}
