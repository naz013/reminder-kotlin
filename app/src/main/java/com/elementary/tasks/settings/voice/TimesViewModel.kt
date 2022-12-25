package com.elementary.tasks.settings.voice

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.utils.datetime.TimeUtil
import com.elementary.tasks.core.utils.mutableLiveDataOf

class TimesViewModel : ViewModel(), LifecycleObserver {
  var morningTime = mutableLiveDataOf<TimeUtil.HM>()
  var dayTime = mutableLiveDataOf<TimeUtil.HM>()
  var eveningTime = mutableLiveDataOf<TimeUtil.HM>()
  var nightTime = mutableLiveDataOf<TimeUtil.HM>()
}