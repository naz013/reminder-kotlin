package com.elementary.tasks.core.data.livedata

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MediatorLiveData<T>() {

  private val observers = ConcurrentHashMap<LifecycleOwner, MutableSet<ObserverWrapper<in T>>>()

  @MainThread
  override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
    val wrapper = ObserverWrapper(observer)
    val set = observers[owner]
    set?.apply {
      add(wrapper)
    } ?: run {
      val newSet = Collections.newSetFromMap(ConcurrentHashMap<ObserverWrapper<in T>, Boolean>())
      newSet.add(wrapper)
      observers[owner] = newSet
    }
    super.observe(owner, wrapper)
  }

  override fun removeObservers(owner: LifecycleOwner) {
    observers.remove(owner)
    super.removeObservers(owner)
  }

  override fun removeObserver(observer: Observer<in T>) {
    observers.forEach {
      if (it.value.remove(observer)) {
        if (it.value.isEmpty()) {
          observers.remove(it.key)
        }
        return@forEach
      }
    }
    super.removeObserver(observer)
  }

  @MainThread
  override fun setValue(t: T?) {
    observers.forEach { it.value.forEach { wrapper -> wrapper.newValue() } }
    super.setValue(t)
  }

  /**
   * Used for cases where T is Void, to make calls cleaner.
   */
  @MainThread
  fun call() {
    value = null
  }

  private class ObserverWrapper<T>(private val observer: Observer<in T>) : Observer<T> {

    private val pending = AtomicBoolean(false)

    override fun onChanged(t: T) {
      if (pending.compareAndSet(true, false)) {
        observer.onChanged(t)
      }
    }

    fun newValue() {
      pending.set(true)
    }
  }
}

fun <T> LiveData<T>.toSingleEvent(): LiveData<T> {
  val result = SingleLiveEvent<T>()
  result.addSource(this) {
    result.value = it
  }
  return result
}
