package com.elementary.tasks.settings.voice

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.common.livedata.toLiveData
import org.threeten.bp.LocalTime

class TimesViewModel(
  private val dateTimeManager: DateTimeManager,
  private val prefs: Prefs
) : ViewModel(), DefaultLifecycleObserver {

  private val _morningTime = mutableLiveDataOf<String>()
  val morningTime = _morningTime.toLiveData()

  private val _dayTime = mutableLiveDataOf<String>()
  val dayTime = _dayTime.toLiveData()

  private val _eveningTime = mutableLiveDataOf<String>()
  val eveningTime = _eveningTime.toLiveData()

  private val _nightTime = mutableLiveDataOf<String>()
  val nightTime = _nightTime.toLiveData()

  var morningLocalTime: LocalTime = LocalTime.now()
    private set
  var dayLocalTime: LocalTime = LocalTime.now()
    private set
  var eveningLocalTime: LocalTime = LocalTime.now()
    private set
  var nightLocalTime: LocalTime = LocalTime.now()
    private set

  fun initTimes() {
    dateTimeManager.toLocalTime(prefs.morningTime)?.also { onMorningTime(it) }
    dateTimeManager.toLocalTime(prefs.noonTime)?.also { onDayTime(it) }
    dateTimeManager.toLocalTime(prefs.eveningTime)?.also { onEveningTime(it) }
    dateTimeManager.toLocalTime(prefs.nightTime)?.also { onNightTime(it) }
  }

  fun onMorningTime(time: LocalTime) {
    morningLocalTime = time
    _morningTime.postValue(dateTimeManager.getTime(time))
    prefs.morningTime = dateTimeManager.to24HourString(time)
  }

  fun onDayTime(time: LocalTime) {
    dayLocalTime = time
    _dayTime.postValue(dateTimeManager.getTime(time))
    prefs.noonTime = dateTimeManager.to24HourString(time)
  }

  fun onEveningTime(time: LocalTime) {
    eveningLocalTime = time
    _eveningTime.postValue(dateTimeManager.getTime(time))
    prefs.eveningTime = dateTimeManager.to24HourString(time)
  }

  fun onNightTime(time: LocalTime) {
    nightLocalTime = time
    _nightTime.postValue(dateTimeManager.getTime(time))
    prefs.nightTime = dateTimeManager.to24HourString(time)
  }
}
