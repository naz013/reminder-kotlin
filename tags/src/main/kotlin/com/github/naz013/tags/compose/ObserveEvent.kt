package com.github.naz013.tags.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.naz013.feature.common.livedata.Event

/**
 * Local copy of `com.elementary.tasks.notes.ObserveEvent` - :tags can't depend on `app`, and this
 * Nav3-entries-in-a-library-module shape has no Fragment `viewLifecycleOwner` of its own.
 */
@Composable
internal fun <T> LiveData<out Event<T>?>.ObserveEvent(action: (T) -> Unit) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(this, lifecycleOwner) {
    val observer = Observer<Event<T>?> { event -> event?.getContentIfNotHandled()?.let(action) }
    observe(lifecycleOwner, observer)
    onDispose { removeObserver(observer) }
  }
}
