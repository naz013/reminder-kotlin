package com.elementary.tasks.reminder.create

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.utils.FeatureManager
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.reminder.ShopItem
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

@Deprecated("Replaced by new Builder")
class ReminderStateViewModel(
  private val googleTasksAuthManager: GoogleTasksAuthManager,
  private val featureManager: FeatureManager
) : ViewModel(), LifecycleObserver {

  var shopItems: List<ShopItem> = listOf()
  var weekdays: List<Int> = listOf()
  var reminder: Reminder = Reminder()
  var group: ReminderGroup? = null

  var isShopItemsEdited: Boolean = false
  var isLink: Boolean = false
  var isMessage: Boolean = false
  var isDelayAdded: Boolean = false
  var isEmailOrSubjectChanged: Boolean = false
  var isLeave: Boolean = false
  var isLastDay: Boolean = false
  var isWeekdaysSaved: Boolean = false
  var isAppSaved: Boolean = false

  var app: String = ""
  var link: String = ""
  var email: String = ""
  var subject: String = ""

  var date: LocalDate = LocalDate.now()
  var time: LocalTime = LocalTime.now()

  var day: Int = 0
  var month: Int = 0
  var year: Int = 0

  var hour: Int = 0
  var minute: Int = 0

  var radius: Int = 0
  var markerStyle: Int = 0

  var timer: Long = 0
  var isPaused: Boolean = false
  var original: Reminder? = null
  var isSaving: Boolean = false
  var isFromFile: Boolean = false

  init {
    setDateTime()
  }

  fun isLoggedToGoogleTasks(): Boolean {
    return featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_TASKS) &&
      googleTasksAuthManager.isAuthorized()
  }

  private fun setDateTime() {
    val dateTime = LocalDateTime.now()
    day = dateTime.dayOfMonth
    month = dateTime.monthValue - 1
    year = dateTime.year
    hour = dateTime.hour
    minute = dateTime.minute
  }
}
