package com.github.naz013.logic.notificationaction

/**
 * Decorates any [ActionHandler] so that, once the wrapped handler completes, the notification
 * for [data] is dismissed. This lets new "perform an action, then clear the notification" use
 * cases be composed from a plain [ActionHandler] lambda instead of a dedicated handler class.
 */
class CancelNotificationDecorator<T>(
  private val delegate: ActionHandler<T>,
  private val notificationGateway: NotificationGateway,
  private val uniqueId: (T) -> Int,
) : ActionHandler<T> {
  override suspend fun handle(data: T) {
    delegate.handle(data)
    notificationGateway.cancel(uniqueId(data))
    // Also clear the wear companion notification (posted under the negated id, see
    // NotificationAlertActionHandler) - a no-op if it was never shown.
    notificationGateway.cancel(-uniqueId(data))
  }
}
