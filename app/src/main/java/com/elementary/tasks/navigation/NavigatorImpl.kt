package com.elementary.tasks.navigation

import com.github.naz013.navigation.Destination
import com.github.naz013.navigation.Navigator
import java.lang.ref.WeakReference

class NavigatorImpl : Navigator, NavigationObservable {

  private var navigationConsumer: WeakReference<NavigationConsumer>? = null
  private var globalConsumer: WeakReference<NavigationConsumer>? = null

  override fun navigate(destination: Destination) {
    navigationConsumer?.get()?.consume(destination)
      ?: globalConsumer?.get()?.consume(destination)
  }

  override fun subscribe(consumer: NavigationConsumer) {
    navigationConsumer = WeakReference(consumer)
  }

  override fun unsubscribe(consumer: NavigationConsumer) {
    if (navigationConsumer?.get() == consumer) {
      navigationConsumer = null
    }
  }

  override fun subscribeGlobal(consumer: NavigationConsumer) {
    globalConsumer = WeakReference(consumer)
  }

  override fun unsubscribeGlobal(consumer: NavigationConsumer) {
    if (globalConsumer?.get() == consumer) {
      globalConsumer = null
    }
  }
}
