package com.elementary.tasks.birthdays

import android.content.Intent
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions

class BirthdayResolver(private val deleteAction: (birthday: Birthday) -> Unit ) {

    fun resolveAction(view: View, birthday: Birthday, listActions: ListActions) {
        when (listActions) {
            ListActions.OPEN -> openBirthday(view, birthday)
            ListActions.MORE -> showMore(view, birthday)
            else -> {
            }
        }
    }

    private fun showMore(view: View, birthday: Birthday) {
        val context = view.context
        val items = arrayOf(context.getString(R.string.edit), context.getString(R.string.delete))
        Dialogues.showPopup(view, { item ->
            if (item == 0) {
                openBirthday(view, birthday)
            } else if (item == 1) {
                deleteAction.invoke(birthday)
            }
        }, *items)
    }

    private fun openBirthday(view: View, birthday: Birthday) {
        AddBirthdayActivity.openLogged(view.context, Intent(view.context, AddBirthdayActivity::class.java)
                .putExtra(Constants.INTENT_ID, birthday.uuId))
    }
}