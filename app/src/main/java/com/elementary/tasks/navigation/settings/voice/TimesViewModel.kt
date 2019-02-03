package com.elementary.tasks.navigation.settings.voice

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.utils.TimeUtil

class TimesViewModel : ViewModel(), LifecycleObserver {
    var morningTime: MutableLiveData<TimeUtil.HM> = MutableLiveData()
    var dayTime: MutableLiveData<TimeUtil.HM> = MutableLiveData()
    var eveningTime: MutableLiveData<TimeUtil.HM> = MutableLiveData()
    var nightTime: MutableLiveData<TimeUtil.HM> = MutableLiveData()
}