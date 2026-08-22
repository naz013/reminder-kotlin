package com.github.naz013.logic.notificationaction

import androidx.core.app.NotificationCompat
import com.github.naz013.common.ContextProvider
import com.github.naz013.ui.common.theme.ThemeProvider

/**
 * Decorates a [NotificationCompat.Builder] with the presentation rules that differ between the
 * "loud" (sound/heads-up allowed) and "silent" variants of an alert notification. New variants
 * (e.g. a heads-up/urgent style) can be added without touching the handlers that use them.
 */
interface NotificationStyle {
  val name: String

  fun resolveIcon(domainIcon: Int): Int

  fun resolvePriority(domainPriority: Int): Int

  fun decorate(
    builder: NotificationCompat.Builder,
    contextProvider: ContextProvider,
  )
}

object LoudNotificationStyle : NotificationStyle {
  override val name = "Loud"

  override fun resolveIcon(domainIcon: Int): Int = domainIcon

  override fun resolvePriority(domainPriority: Int): Int = domainPriority

  override fun decorate(
    builder: NotificationCompat.Builder,
    contextProvider: ContextProvider,
  ) {
    builder.color = ThemeProvider.getPrimaryColor(contextProvider.themedContext)
    builder.setCategory(NotificationCompat.CATEGORY_REMINDER)
  }
}

object SilentNotificationStyle : NotificationStyle {
  override val name = "Silent"

  override fun resolveIcon(domainIcon: Int): Int = domainIcon

  override fun resolvePriority(domainPriority: Int): Int = NotificationCompat.PRIORITY_LOW

  override fun decorate(
    builder: NotificationCompat.Builder,
    contextProvider: ContextProvider,
  ) {
    // Silent notifications get no extra branding.
  }
}
