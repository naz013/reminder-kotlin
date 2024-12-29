package com.github.naz013.feature.common.livedata

import androidx.annotation.CheckResult
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

fun <T> LiveData<List<T>>.getNonNullList(): List<T> {
  return value ?: emptyList()
}

fun <K, V> LiveData<Map<K, V>>.getNonNullMap(): Map<K, V> {
  return value ?: emptyMap()
}

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

fun <T> LiveData<out T?>.nullObserve(owner: LifecycleOwner, observer: Observer<T>) {
  this.observe(owner) { o: T? ->
    if (o != null) {
      observer.onChanged(o)
    }
  }
}

@MainThread
@CheckResult
fun <X, Y> LiveData<X?>.mapNullable(
  transform: (@JvmSuppressWildcards X) -> (@JvmSuppressWildcards Y)
): LiveData<Y> {
  val result = MediatorLiveData<Y>()
  result.addSource(this) { x ->
    if (x != null) {
      result.value = transform(x)
    }
  }
  return result
}

fun <T> LiveData<T>.toSingleEvent(): LiveData<T> {
  val result = SingleLiveEvent<T>()
  result.addSource(this) {
    result.value = it
  }
  return result
}
