package com.elementary.tasks.home.scheduleview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.utils.datetime.ScheduleTimes
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class ScheduleHomeViewModel(
  dispatcherProvider: DispatcherProvider,
  private val scheduleLiveData: ScheduleLiveData,
  private val googleTasksAuthManager: GoogleTasksAuthManager
) : BaseProgressViewModel(dispatcherProvider) {

  val scheduleData = scheduleLiveData.toLiveData()

  init {
    internalLoad()
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    internalLoad()
  }

  fun getDateTime(headerTimeType: HeaderTimeType?): LocalDateTime {
    val time = getTime(headerTimeType) ?: LocalTime.now()
    return if (time < LocalTime.now()) {
      LocalDateTime.now()
    } else {
      LocalDateTime.of(LocalDate.now(), time)
    }
  }

  fun getTime(headerTimeType: HeaderTimeType?): LocalTime? {
    return when (headerTimeType) {
      HeaderTimeType.MORNING -> ScheduleTimes.MORNING
      HeaderTimeType.NOON -> ScheduleTimes.NOON
      HeaderTimeType.EVENING -> ScheduleTimes.EVENING
      else -> null
    }
  }

  fun hasGoogleTasks(): Boolean {
    return googleTasksAuthManager.isAuthorized()
  }

  private fun internalLoad() {
    viewModelScope.launch(dispatcherProvider.default()) {
      scheduleLiveData.onDateSelected(LocalDateTime.now())
    }
  }
}
