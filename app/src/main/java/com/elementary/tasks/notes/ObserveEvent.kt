package com.elementary.tasks.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.naz013.feature.common.livedata.Event

/**
 * Compose equivalent of `LiveData<Event<T>>.observeEvent(lifecycleOwner) { ... }` for the
 * Nav3 entries in the Notes island, which have no Fragment `viewLifecycleOwner` of their own.
 */
@Composable
fun <T> LiveData<out Event<T>?>.ObserveEvent(action: (T) -> Unit) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(this, lifecycleOwner) {
    val observer =
      Observer<Event<T>?> { event -> event?.getContentIfNotHandled()?.let(action) }
    observe(lifecycleOwner, observer)
    onDispose { removeObserver(observer) }
  }
}

/** Compose equivalent of `LiveData<T>.nonNullObserve(lifecycleOwner) { ... }`. */
@Composable
fun <T> LiveData<out T?>.ObserveNonNull(action: (T) -> Unit) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(this, lifecycleOwner) {
    val observer = Observer<T?> { value -> if (value != null) action(value) }
    observe(lifecycleOwner, observer)
    onDispose { removeObserver(observer) }
  }
}
