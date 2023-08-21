package com.elementary.tasks.core.arch

import androidx.lifecycle.LiveData

class OneWayLiveData<T> : LiveData<T>() {

  fun viewModelPost(value: T) {
    postValue(value)
  }
}
