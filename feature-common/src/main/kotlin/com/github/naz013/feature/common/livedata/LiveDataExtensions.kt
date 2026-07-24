package com.github.naz013.feature.common.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

fun <T> MutableLiveData<T>.toLiveData(): LiveData<T> = this

fun <T> LiveData<T>.nonNullObserve(owner: LifecycleOwner, observer: Observer<T>) {
  this.observe(owner) { o: T? ->
    if (o != null) {
      observer.onChanged(o)
    }
  }
}

fun <T> LiveData<out Event<T>?>.observeEvent(owner: LifecycleOwner, observer: Observer<T>) {
  this.observe(owner) {
    it?.getContentIfNotHandled()?.also { value ->
      observer.onChanged(value)
    }
  }
}

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
