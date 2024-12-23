package com.elementary.tasks.navigation

interface NavigationObservable {
  fun subscribe(consumer: NavigationConsumer)
  fun subscribeGlobal(consumer: NavigationConsumer)
  fun unsubscribe(consumer: NavigationConsumer)
  fun unsubscribeGlobal(consumer: NavigationConsumer)
}
