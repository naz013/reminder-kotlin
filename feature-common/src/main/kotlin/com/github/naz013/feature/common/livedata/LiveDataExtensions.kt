package com.github.naz013.feature.common.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.toLiveData(): LiveData<T> = this

inline fun <reified T> MutableLiveData<Event<T>>.emit(event: T) {
  this.value = Event(event)
}

fun <T> LiveData<T>.toSingleEvent(): LiveData<T> {
  val result = SingleLiveEvent<T>()
  result.addSource(this) {
    result.value = it
  }
  return result
}
