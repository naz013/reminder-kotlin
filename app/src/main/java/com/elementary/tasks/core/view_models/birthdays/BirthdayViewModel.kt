package com.elementary.tasks.core.view_models.birthdays

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault

class BirthdayViewModel(
  id: String,
  appDb: AppDb,
  prefs: Prefs,
  context: Context
) : BaseBirthdaysViewModel(appDb, prefs, context) {

  val birthday = appDb.birthdaysDao().loadById(id)
  var date: MutableLiveData<Long> = MutableLiveData()
  var isContactAttached: MutableLiveData<Boolean> = MutableLiveData()

  var isLogged = false
  var isEdited = false
  var hasSameInDb: Boolean = false
  var isFromFile: Boolean = false

  fun findSame(id: String) {
    launchDefault {
      val birthday = appDb.birthdaysDao().getById(id)
      hasSameInDb = birthday != null
    }
  }
}
