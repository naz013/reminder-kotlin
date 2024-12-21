package com.elementary.tasks.core.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

fun <T> CoroutineScope.observeTable(
  table: Table,
  tableChangeListenerFactory: TableChangeListenerFactory,
  queryProducer: suspend() -> T
): LiveData<T> {
  val liveData = MutableLiveData<T>()
  val listener = tableChangeListenerFactory.create(table) {
    launch(Dispatchers.Default) {
      liveData.postValue(queryProducer())
    }
  }
  launch {
    liveData.postValue(queryProducer())
    listener.register()
    suspendCancellableCoroutine { continuation ->
      continuation.invokeOnCancellation {
        listener.unregister()
      }
    }
  }
  return liveData
}
